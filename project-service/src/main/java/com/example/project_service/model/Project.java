package com.example.project_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "projects", schema = "project")
public class Project {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(min=3, max=20)
    @Pattern(regexp = "^[A-Z0-9-]{3,20}$")
    @Column(nullable=false, length=20)
    private String code;

    @NotBlank @Size(min=3, max=120)
    @Column(nullable=false, length=120)
    private String name;

    @Size(max=2000)
    @Column(length=2000)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private ProjectStatus status;

    @NotNull
    @Column(name="start_date", nullable=false)
    private LocalDate startDate;

    @Column(name="end_date")
    private LocalDate endDate;

    // getters & setters
    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
