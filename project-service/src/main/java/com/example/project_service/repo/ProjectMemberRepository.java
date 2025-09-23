package com.example.project_service.repo;

import com.example.project_service.model.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    List<ProjectMember> findByProjectId(Long projectId);
    Optional<ProjectMember> findByProjectIdAndEmployeeId(Long projectId, Long employeeId);
    long deleteByProjectIdAndEmployeeId(Long projectId, Long employeeId);
}
