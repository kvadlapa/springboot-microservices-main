package com.example.employee;



import com.example.employee.domain.Employee;
import com.example.employee.repo.EmployeeRepository;
import com.example.employee.client.DepartmentClient; // <-- keep if this is your client package
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmployeeApiSpringBootTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper json;

    @MockBean
    EmployeeRepository employeeRepository;
    @MockBean
    DepartmentClient departmentClient; // <-- keep; adjust type if your client interface has a different name

    @Test
    void health_is_up() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void list_employees_ok() throws Exception {
        // Arrange repo
        Employee e1 = new Employee();
        e1.setId(1L);
        e1.setFirstName("Alice");
        e1.setLastName("Nguyen");
        e1.setEmail("alice@example.com");
        e1.setDepartmentId(1L);

        Employee e2 = new Employee();
        e2.setId(2L);
        e2.setFirstName("Bob");
        e2.setLastName("Martinez");
        e2.setEmail("bob@example.com");
        e2.setDepartmentId(1L);

        Mockito.when(employeeRepository.findAll()).thenReturn(List.of(e1, e2));

        // Arrange Feign (adapt method/return type to your interface)
        // Example if your client has: DepartmentDto getById(Long id)
        // Mockito.when(departmentClient.getById(anyLong()))
        //        .thenReturn(new DepartmentDto(1L, "Engineering", "Builds and maintains products"));

        // Act + Assert
        mvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].email").value("alice@example.com"))
                .andExpect(jsonPath("$[1].firstName").value("Bob"));
    }

    @Test
    void create_employee_validation_400_when_email_missing() throws Exception {
        // Missing "email" to trigger @Valid -> 400 ProblemDetail
        var payload = """
          {
            "firstName":"NoEmail",
            "lastName":"User",
            "departmentId":1
          }
        """;

        mvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                // default ProblemDetail fields
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title", anyOf(is("Bad Request"), notNullValue())));
    }

    @Test
    void create_employee_conflict_409_on_duplicate_email() throws Exception {
        // Arrange repo to simulate existing email so service throws your Conflict exception
        var existing = new Employee();
        existing.setId(99L);
        existing.setEmail("alice@example.com");

        // If your repo has findByEmail(String) use that:
        Mockito.when(employeeRepository.existsByEmail("alice@example.com"))
                .thenReturn(true);
        // If it’s findByEmailIgnoreCase, change the method name accordingly.

        // Minimal valid payload
        var payload = """
          {
            "firstName":"Alice",
            "lastName":"Nguyen",
            "email":"alice@example.com",
            "departmentId":1
          }
        """;

        mvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
