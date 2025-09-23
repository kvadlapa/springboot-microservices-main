package com.example.employee.config;

import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
// If you use Eureka name lookups, add: import org.springframework.cloud.client.loadbalancer.LoadBalanced;

@Configuration
public class HttpConfig {

    @Bean
    // @LoadBalanced // <- uncomment if you want to call by service-name via Eureka
    public RestTemplate restTemplate(RestTemplateBuilder b) {
        return b.setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(2))
                .build();
    }
}
