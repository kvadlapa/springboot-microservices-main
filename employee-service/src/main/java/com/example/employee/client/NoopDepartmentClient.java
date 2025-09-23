// src/main/java/com/example/employee/client/NoopDepartmentClient.java
package com.example.employee.client;

import com.example.employee.dto.DepartmentDTO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(DepartmentClient.class)
public class NoopDepartmentClient implements DepartmentClient {
    @Override public DepartmentDTO getDepartment(Long id) { return null; }
}
