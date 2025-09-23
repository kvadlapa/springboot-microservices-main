package com.example.department.dto;


public record DepartmentPatchDTO(
        String name,
        String code,
        String description,
        String managerEmail
) {}
