package com.example.project_service.api.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> content, int page, int size, long totalElements, int totalPages, String sort
) {
    public static <T> PageResponse<T> from(Page<T> p, String sort) {
        return new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages(), sort);
    }
}
