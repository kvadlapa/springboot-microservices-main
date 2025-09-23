package com.example.department.client;

import com.example.department.dto.EmployeeSummaryDTO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@ConditionalOnMissingBean(EmployeeClient.class)
public class NoopEmployeeClient implements EmployeeClient {
    @Override public List<EmployeeSummaryDTO> listByDepartment(Long departmentId) { return Collections.emptyList(); }
    @Override public List<EmployeeSummaryDTO> getEmployeesByDepartment(Long departmentId, Integer page, Integer size, String sort) { return Collections.emptyList(); }
    @Override public long countByDepartment(Long departmentId) { return 0L; }
}
