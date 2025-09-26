package com.example.employee.service;

import com.example.employee.domain.outbox.OutboxEvent;
import com.example.employee.domain.outbox.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class OutboxRelay {
    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxEventRepository repo;
    private final RestTemplate http = new RestTemplate();

    // You can move these to config-server later; hardcode for now to keep it simple
    private final List<String> subscribers = List.of(
            "http://department-service/events/employee",
            "http://project-service/events/employee"
    );

    public OutboxRelay(OutboxEventRepository repo) { this.repo = repo; }

    @Scheduled(fixedDelay = 2000) // run every 2s
    @Transactional
    public void deliver() {
        var due = repo.findDue(Instant.now());
        for (OutboxEvent e : due) {
            boolean allOk = true;
            for (String url : subscribers) {
                try {
                    HttpHeaders h = new HttpHeaders();
                    h.setContentType(MediaType.APPLICATION_JSON);
                    h.set("X-Event-Type", e.getType());
                    var req = new HttpEntity<>(e.getPayloadJson(), h);
                    var resp = http.postForEntity(url, req, String.class);
                    if (!resp.getStatusCode().is2xxSuccessful()) throw new RuntimeException("Non-2xx");
                } catch (Exception ex) {
                    allOk = false;
                    int attempts = e.getAttemptCount() + 1;
                    e.setAttemptCount(attempts);
                    e.setLastAttemptAt(Instant.now());
                    e.setStatus("FAILED");
                    long backoffSec = (long) Math.min(60, Math.pow(2, attempts)); // capped exponential
                    e.setNextAttemptAt(Instant.now().plus(Duration.ofSeconds(backoffSec)));
                    log.warn("Delivery failed; id={}, attempt={}, will retry in {}s; to={} err={}",
                            e.getId(), attempts, backoffSec, url, ex.toString());
                }
            }
            if (allOk) {
                e.setStatus("SENT");
                e.setLastAttemptAt(Instant.now());
                e.setNextAttemptAt(null);
            }
            repo.save(e);
        }
    }
}
