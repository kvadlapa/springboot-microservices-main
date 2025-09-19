package com.example.department.web;

import com.example.department.domain.Department;
import com.example.department.repo.DepartmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
class DepartmentApiSpringBootTest {

    @Autowired MockMvc mvc;

    @MockBean DepartmentRepository repository;

    @Test
    void get_by_id_ok() throws Exception {
        Department d = new Department();
        d.setId(7L); d.setName("Eng"); d.setCode("D7"); d.setDescription("desc");

        when(repository.findById(eq(7L))).thenReturn(Optional.of(d));

        mvc.perform(get("/api/v1/departments/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Eng"))
                .andExpect(jsonPath("$.code").value("D7"));
    }

    @Test
    void create_ok_returns_201() throws Exception {
        Department saved = new Department();
        saved.setId(123L); saved.setName("QA"); saved.setCode("D99"); saved.setDescription("quality");

        when(repository.save(any(Department.class))).thenReturn(saved);

        mvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"QA\",\"code\":\"D99\",\"description\":\"quality\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(123));
    }
}
