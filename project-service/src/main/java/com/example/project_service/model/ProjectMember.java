package com.example.project_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

@Entity
@Table(
        name="project_members", schema="project",
        uniqueConstraints = @UniqueConstraint(name="uk_project_members_project_employee", columnNames={"project_id","employee_id"})
)
public class ProjectMember {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="project_id", nullable=false)
    private Long projectId;

    @Column(name="employee_id", nullable=false)
    private Long employeeId;

    @NotBlank @Size(min=2, max=60)
    @Column(nullable=false, length=60)
    private String role;

    @Min(0) @Max(100)
    @Column(name="allocation_percent", nullable=false)
    private Integer allocationPercent;

    @Column(name="assigned_at", nullable=false)
    private OffsetDateTime assignedAt = OffsetDateTime.now();

    // getters & setters
    public Long getId() { return id; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Integer getAllocationPercent() { return allocationPercent; }
    public void setAllocationPercent(Integer allocationPercent) { this.allocationPercent = allocationPercent; }
    public OffsetDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(OffsetDateTime assignedAt) { this.assignedAt = assignedAt; }
}
