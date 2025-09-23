package com.example.project_service.api.dto;

import jakarta.validation.constraints.*;

public record AddMemberRequest(
        @NotNull Long employeeId,
        @NotBlank @Size(min=2,max=60) String role,
        @NotNull @Min(0) @Max(100) Integer allocationPercent
) {}
