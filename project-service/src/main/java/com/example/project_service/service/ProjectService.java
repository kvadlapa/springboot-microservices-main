package com.example.project_service.service;

import com.example.project_service.api.dto.AddMemberRequest;
import com.example.project_service.client.EmployeeClient;
import com.example.project_service.exception.*;
import com.example.project_service.model.*;
import com.example.project_service.repo.ProjectMemberRepository;
import com.example.project_service.repo.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ProjectService {
    private final ProjectRepository projects;
    private final ProjectMemberRepository members;
    private final EmployeeClient employees;

    public ProjectService(ProjectRepository projects, ProjectMemberRepository members, EmployeeClient employees) {
        this.projects = projects; this.members = members; this.employees = employees;
    }

    @Transactional
    public Project create(Project p) {
        // normalize + validate
        p.setCode(p.getCode().toUpperCase());
        if (projects.existsByCodeIgnoreCase(p.getCode())) {
            throw new ConflictException("Project code already exists: " + p.getCode());
        }
        if (p.getEndDate()!=null && p.getEndDate().isBefore(p.getStartDate())) {
            throw new BadRequestException("endDate cannot be before startDate");
        }
        return projects.save(p);
    }

    @Transactional
    public Project updateFull(Long id, Project updated, boolean allowCodeChange) {
        var p = projects.findById(id).orElseThrow(() -> new NotFoundException("Project not found: " + id));
        if (!allowCodeChange && !p.getCode().equals(updated.getCode())) {
            throw new BadRequestException("Changing code is not allowed.");
        }
        if (allowCodeChange) {
            var newCode = updated.getCode().toUpperCase();
            if (!newCode.equalsIgnoreCase(p.getCode()) && projects.existsByCodeIgnoreCase(newCode)) {
                throw new ConflictException("Project code already exists: " + newCode);
            }
            p.setCode(newCode);
        }
        if (updated.getEndDate()!=null && updated.getEndDate().isBefore(updated.getStartDate())) {
            throw new BadRequestException("endDate cannot be before startDate");
        }
        p.setName(updated.getName());
        p.setDescription(updated.getDescription());
        p.setStatus(updated.getStatus());
        p.setStartDate(updated.getStartDate());
        p.setEndDate(updated.getEndDate());
        return projects.save(p);
    }

    @Transactional
    public Project patch(Long id, ProjectStatus status, java.time.LocalDate endDate, String description) {
        var p = projects.findById(id).orElseThrow(() -> new NotFoundException("Project not found: " + id));
        if (status != null) p.setStatus(status);
        if (description != null) p.setDescription(description);
        if (endDate != null) p.setEndDate(endDate);
        if (p.getEndDate()!=null && p.getEndDate().isBefore(p.getStartDate())) {
            throw new BadRequestException("endDate cannot be before startDate");
        }
        return projects.save(p);
    }

    @Transactional
    public void delete(Long id) {
        if (!projects.existsById(id)) throw new NotFoundException("Project not found: " + id);
        // DB is set to cascade delete members; choice (b) per spec
        projects.deleteById(id);
    }

    @Transactional
    public ProjectMember addMember(Long projectId, AddMemberRequest req) {
        var project = projects.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        // Validate employee existence in Employee Service
        try { employees.getEmployee(req.employeeId()); }
        catch (Exception e) { throw new NotFoundException("Employee not found: " + req.employeeId()); }

        members.findByProjectIdAndEmployeeId(projectId, req.employeeId())
                .ifPresent(m -> { throw new ConflictException("Employee already a member of this project"); });

        var m = new ProjectMember();
        m.setProjectId(project.getId());
        m.setEmployeeId(req.employeeId());
        m.setRole(req.role());
        m.setAllocationPercent(req.allocationPercent());
        m.setAssignedAt(OffsetDateTime.now());
        return members.save(m);
    }

    @Transactional(readOnly = true)
    public List<ProjectMember> listMembers(Long projectId) {
        projects.findById(projectId).orElseThrow(() -> new NotFoundException("Project not found: " + projectId));
        return members.findByProjectId(projectId);
    }

    @Transactional
    public void removeMember(Long projectId, Long employeeId) {
        var deleted = members.deleteByProjectIdAndEmployeeId(projectId, employeeId);
        if (deleted == 0) throw new NotFoundException("Member not found for employeeId: " + employeeId);
    }
}
