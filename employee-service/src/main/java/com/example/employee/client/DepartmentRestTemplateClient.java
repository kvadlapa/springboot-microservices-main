package com.example.employee.client;

import com.example.employee.dto.DepartmentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class DepartmentRestTemplateClient implements DepartmentClient {

    private final RestTemplate rest;

    // If using Eureka + @LoadBalanced RestTemplate, this can be "http://DEPARTMENT-SERVICE"
    @Value("${department.service.base-url:http://department-service:8080}")
    private String baseUrl;

    @Override
    public DepartmentDTO getDepartment(Long id) {
        try {
            return rest.getForObject(baseUrl + "/api/v1/departments/{id}", DepartmentDTO.class, id);
        } catch (RestClientException ex) {
            // Let your service gracefully degrade (you already ignore in toDTO)
            return null;
        }
    }
}
