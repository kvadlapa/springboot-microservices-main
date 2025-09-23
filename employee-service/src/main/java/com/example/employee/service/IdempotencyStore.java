package com.example.employee.service;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class IdempotencyStore {
    private final ConcurrentMap<String, Long> keyToEmployeeId = new ConcurrentHashMap<>();

    public Optional<Long> lookup(String key) {
        if (key == null || key.isBlank()) return Optional.empty();
        return Optional.ofNullable(keyToEmployeeId.get(key));
    }

    public void remember(String key, Long employeeId) {
        if (key == null || key.isBlank() || employeeId == null) return;
        keyToEmployeeId.putIfAbsent(key, employeeId);
    }

    /** Returns existing id if present; otherwise stores the provided id and returns it. */
    public Long rememberOrGet(String key, Long employeeId) {
        if (key == null || key.isBlank() || employeeId == null) return employeeId;
        return keyToEmployeeId.computeIfAbsent(key, k -> employeeId);
    }
}
