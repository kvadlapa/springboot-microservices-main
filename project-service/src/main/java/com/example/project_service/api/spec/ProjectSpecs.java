package com.example.project_service.api.spec;

import com.example.project_service.model.Project;
import com.example.project_service.model.ProjectStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class ProjectSpecs {
    private ProjectSpecs(){}

    public static Specification<Project> hasStatus(ProjectStatus s) {
        return (r,q,cb) -> s==null ? null : cb.equal(r.get("status"), s);
    }
    public static Specification<Project> codeEquals(String code) {
        return (r,q,cb) -> (code==null || code.isBlank()) ? null : cb.equal(cb.lower(r.get("code")), code.toLowerCase());
    }
    public static Specification<Project> nameContains(String name) {
        return (r,q,cb) -> (name==null || name.isBlank()) ? null : cb.like(cb.lower(r.get("name")), "%"+name.toLowerCase()+"%");
    }
    public static Specification<Project> startFrom(LocalDate from) {
        return (r,q,cb) -> (from==null) ? null : cb.greaterThanOrEqualTo(r.get("startDate"), from);
    }
    public static Specification<Project> endTo(LocalDate to) {
        return (r,q,cb) -> (to==null) ? null : cb.lessThanOrEqualTo(r.get("endDate"), to);
    }
}
