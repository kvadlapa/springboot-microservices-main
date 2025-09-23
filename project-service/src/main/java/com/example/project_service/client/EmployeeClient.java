package com.example.project_service.client;

import com.example.project_service.api.dto.EmployeeSnapshot;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "${employee.service-id:employee-service}",
        path = "/api/v1"              // <— base path for all calls to Employee service
)
public interface EmployeeClient {
    @GetMapping("/api/employees/{id}")
    EmployeeSnapshot getEmployee(@PathVariable("id") Long id);
}
