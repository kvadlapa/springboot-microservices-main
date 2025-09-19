package com.example.department.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "departments", schema = "department")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "name is required")
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String name;

    //Added
    @NotBlank(message = "code is required")
    @Size(max = 20)
    @Column(nullable = false, length = 20)   // DB unique index added via Flyway below
    private String code;

    @Size(max = 2000)
    @Column(columnDefinition = "text")
    private String description;
}
