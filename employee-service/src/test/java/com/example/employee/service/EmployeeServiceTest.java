package com.example.employee.service;

import com.example.employee.client.DepartmentClient;
import com.example.employee.domain.Employee;
import com.example.employee.dto.DepartmentDTO;
import com.example.employee.dto.EmployeeDTO;
import com.example.employee.error.ResourceNotFoundException;
import com.example.employee.repo.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class EmployeeServiceTest {
    @Mock
    EmployeeRepository repository;
    @Mock
    DepartmentClient departmentClient;



    @InjectMocks
    EmployeeService service;

    @ParameterizedTest(name = "create({0}) → duplicate? {1}")
    @CsvSource({
            "dina@example.com, false",
            "alice@example.com, true"
    })
    @DisplayName("create(): throws when email exists; persists otherwise")
    void create_handles_duplicates(String email, boolean duplicate) {
        when(repository.existsByEmail(email)).thenReturn(duplicate);
        if (duplicate) {
            assertThatThrownBy(() -> service.create(EmployeeDTO.builder()
                    .firstName("X").lastName("Y").email(email).build()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email already exists");
        } else {
            when(repository.save(any(Employee.class)))
                    .thenAnswer(inv -> { Employee e = inv.getArgument(0); e.setId(101L); return e; });
            var out = service.create(EmployeeDTO.builder()
                    .firstName("X").lastName("Y").email(email).build());
            assertThat(out.getId()).isEqualTo(101L);
        }
    }
    @Test
    void create_conflict_when_email_exists() {
        var dto = EmployeeDTO.builder().firstName("A").lastName("B").email("a@b.com").departmentId(1L).build();
        when(repository.existsByEmail("a@b.com")).thenReturn(true);

        var ex = assertThrows(IllegalArgumentException.class, () -> service.create(dto));
        assertTrue(ex.getMessage().toLowerCase().contains("email"));
        verify(repository, never()).save(any());
    }

    @Test
    void getById_not_found() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getById(99L));
    }

    @Test
    void getAll_ok_maps_department() {
        var e = Employee.builder().id(1L).firstName("A").lastName("B").email("a@b.com").departmentId(7L).build();
        when(repository.findAll()).thenReturn(List.of(e));
        when(departmentClient.getDepartment(7L)).thenReturn(new DepartmentDTO(7L, "Eng", "desc"));

        var list = service.getAll();
        assertEquals(1, list.size());
        assertEquals("Eng", list.get(0).getDepartment().getName());
    }

}
