package com.example.department.web;

import com.example.department.domain.Department;
import com.example.department.repo.DepartmentRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = DepartmentController.class)
class DepartmentControllerTest {

    @Autowired MockMvc mvc;

    // Mock what the controller actually injects
    @MockBean DepartmentRepository repository;

    @Test
    void list_ok() throws Exception {
        Department d1 = new Department();
        d1.setId(1L); d1.setName("Engineering"); d1.setCode("D1"); d1.setDescription("Builds and maintains products");
        Department d2 = new Department();
        d2.setId(2L); d2.setName("HR"); d2.setCode("D2"); d2.setDescription("People ops");

        Mockito.when(repository.findAll()).thenReturn(List.of(d1, d2));

        mvc.perform(get("/api/v1/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].name").value("HR"));
    }

    @Test
    void get_not_found() throws Exception {
        Mockito.when(repository.findById(99L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/v1/departments/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"));
    }

    @Test
    void create_conflict_duplicate() throws Exception {
        Mockito.when(repository.save(any()))
                .thenThrow(new org.springframework.dao.DuplicateKeyException("duplicate code"));

        var body = """
            {"name":"Engineering","code":"D1","description":"dupe"}
        """;

        mvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"));
    }
}
