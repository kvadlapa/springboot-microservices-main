package com.example.department.client;

import com.example.department.dto.EmployeeSummaryDTO;
import java.util.List;

public interface EmployeeClient {
    // simple shape used by /departments/{id}/employees
    List<EmployeeSummaryDTO> listByDepartment(Long departmentId);

    // optional paged variant (in case you call it anywhere else)
    List<EmployeeSummaryDTO> getEmployeesByDepartment(Long departmentId,
                                                      Integer page,
                                                      Integer size,
                                                      String sort);

    // used by protective delete in DepartmentService
    long countByDepartment(Long departmentId);
}
