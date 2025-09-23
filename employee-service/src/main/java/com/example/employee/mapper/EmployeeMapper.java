package com.example.employee.mapper;


import com.example.employee.domain.Employee;
import com.example.employee.dto.EmployeeDTO;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public Employee toEntity(EmployeeDTO dto) {
        if (dto == null) return null;
        var e = new Employee();
        e.setId(dto.getId());
        e.setFirstName(dto.getFirstName());
        e.setLastName(dto.getLastName());
        e.setEmail(dto.getEmail());
        e.setDepartmentId(dto.getDepartmentId());
        return e;
    }

    public EmployeeDTO toDto(Employee e) {
        if (e == null) return null;
        return EmployeeDTO.builder()
                .id(e.getId())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .email(e.getEmail())
                .departmentId(e.getDepartmentId())
                // dto.getDepartment() is optional enrichment; leave null unless you populate it elsewhere
                .build();
    }
}
