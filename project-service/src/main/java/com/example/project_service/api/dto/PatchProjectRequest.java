package com.example.project_service.api.dto;

import com.example.project_service.model.ProjectStatus;
import java.time.LocalDate;

public record PatchProjectRequest(
        ProjectStatus status,
        LocalDate endDate,
        String description
) {}
