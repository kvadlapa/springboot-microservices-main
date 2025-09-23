package com.example.project_service.repo;

import com.example.project_service.model.Project;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    Optional<Project> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
}
