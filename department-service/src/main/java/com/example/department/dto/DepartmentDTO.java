// src/main/java/com/example/department/dto/DepartmentDTO.java
package com.example.department.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DepartmentDTO {
    // DepartmentDTO

    @JsonProperty(access = JsonProperty.Access.READ_ONLY) // ignored on input, shown on output
    private Long id;
    @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max = 150)
    private String name;
    @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max = 40)
    private String code;
    @jakarta.validation.constraints.Size(max = 500)
    private String description;
    @jakarta.validation.constraints.Email @jakarta.validation.constraints.Size(max = 200)
    private String managerEmail;

}
