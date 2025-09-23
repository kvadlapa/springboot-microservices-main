package com.example.project_service.api.dto;

import com.example.project_service.model.ProjectStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CreateProjectRequest(
        @NotBlank @Size(min=3,max=20) @Pattern(regexp="^[A-Z0-9-]{3,20}$") String code,
        @NotBlank @Size(min=3,max=120) String name,
        @Size(max=2000) String description,
        @NotNull ProjectStatus status,
        @NotNull LocalDate startDate,
        LocalDate endDate
) {}
