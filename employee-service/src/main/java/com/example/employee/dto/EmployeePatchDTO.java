package com.example.employee.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeePatchDTO {
    @Size(max = 120) private String firstName;
    @Size(max = 120) private String lastName;

    @Email(message = "email must be valid")
    @Size(max = 200)
    String email;
    private Long departmentId;
}

