package com.example.employee.service;

import com.example.employee.client.DepartmentClient;
import com.example.employee.domain.Employee;
import com.example.employee.dto.DepartmentDTO;
import com.example.employee.dto.DepartmentStatsDTO;
import com.example.employee.dto.EmployeeDTO;
import com.example.employee.dto.EmployeePatchDTO;
import com.example.employee.error.ResourceNotFoundException;
import com.example.employee.repo.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository repository;
    private final DepartmentClient departmentClient;
    private final IdempotencyStore idempotencyStore;

    public List<EmployeeDTO> getAll() {
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    public EmployeeDTO getById(Long id) {
        Employee e = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return toDTO(e);
    }

    @Transactional
    public EmployeeDTO create(EmployeeDTO dto) {
        return create(dto, null);
    }

    /** Create with optional Idempotency-Key. Duplicate keys return the originally created resource. */
    @Transactional
    public EmployeeDTO create(EmployeeDTO dto, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing = idempotencyStore.lookup(idempotencyKey)
                    .flatMap(repository::findById)
                    .map(this::toDTO);
            if (existing.isPresent()) return existing.get();
        }

        if (repository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Employee e = Employee.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .departmentId(dto.getDepartmentId())
                .build();

        e = repository.save(e);

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyStore.remember(idempotencyKey, e.getId());
        }
        return toDTO(e);
    }

    public Page<EmployeeDTO> list(Optional<String> email,
                                  Optional<String> lastNameContains,
                                  Optional<Long> departmentId,
                                  Pageable pageable) {

        Employee probe = new Employee();
        email.ifPresent(probe::setEmail);
        lastNameContains.ifPresent(probe::setLastName);
        departmentId.ifPresent(probe::setDepartmentId);

        ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withIgnoreNullValues()
                .withMatcher("email", m -> m.exact().ignoreCase())
                .withMatcher("lastName", m -> m.contains().ignoreCase());

        return repository.findAll(Example.of(probe, matcher), pageable)
                .map(this::toDTO);
    }

    /** Full update (PUT). */
    @Transactional
    public EmployeeDTO update(Long id, EmployeeDTO dto) {
        Employee e = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        repository.findByEmailIgnoreCase(dto.getEmail())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> { throw new IllegalArgumentException("Email already exists"); });


        e.setFirstName(dto.getFirstName());
        e.setLastName(dto.getLastName());
        e.setEmail(dto.getEmail());
        e.setDepartmentId(dto.getDepartmentId());

        return toDTO(repository.save(e));
    }

    /** Partial update (PATCH). Only non-null fields are applied. */
    @Transactional
    public EmployeeDTO patch(Long id, EmployeePatchDTO patch) {
        Employee e = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (patch.getEmail() != null && !patch.getEmail().equalsIgnoreCase(e.getEmail())) {
            repository.findByEmailIgnoreCase(patch.getEmail())
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> { throw new IllegalArgumentException("Email already exists"); });
            e.setEmail(patch.getEmail());
        }
        if (patch.getFirstName() != null)    e.setFirstName(patch.getFirstName());
        if (patch.getLastName() != null)     e.setLastName(patch.getLastName());
        if (patch.getDepartmentId() != null) e.setDepartmentId(patch.getDepartmentId());

        return toDTO(repository.save(e));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Employee not found");
        }
        repository.deleteById(id);
    }

    /** Convenience case-insensitive search across firstName/lastName/email. */
    public List<EmployeeDTO> search(String q) {
        if (q == null || q.isBlank()) return List.of();
        var results = repository
                .findByFirstNameIgnoreCaseContainingOrLastNameIgnoreCaseContainingOrEmailIgnoreCaseContaining(q, q, q);
        return results.stream().map(this::toDTO).toList();
    }

    /** Counts by departmentId. */
    public List<DepartmentStatsDTO> stats() {
        return repository.countByDepartment().stream()
                .map(d -> new DepartmentStatsDTO(d.getDepartmentId(), d.getCount()))
                .toList();
    }

    // ----- mapping & enrichment -----
    private EmployeeDTO toDTO(Employee e) {
        DepartmentDTO dept = null;
        if (e.getDepartmentId() != null) {
            try { dept = departmentClient.getDepartment(e.getDepartmentId()); }
            catch (Exception ignored) {}
        }
        return EmployeeDTO.builder()
                .id(e.getId())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .email(e.getEmail())
                .departmentId(e.getDepartmentId())
                .department(dept)
                .build();
    }
}
