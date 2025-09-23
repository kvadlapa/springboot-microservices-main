package com.example.department.service;

import com.example.department.client.EmployeeClient;
import com.example.department.domain.Department;
import com.example.department.dto.DepartmentDTO;
import com.example.department.dto.DepartmentPatchDTO;
import com.example.department.error.BusinessConflictException;
import com.example.department.error.ResourceNotFoundException;
import com.example.department.repo.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository repository;
    private final EmployeeClient employeeClient;

    public Page<DepartmentDTO> list(String nameContains, String codeContains, Pageable pageable) {
        boolean hasName = nameContains != null && !nameContains.isBlank();
        boolean hasCode = codeContains != null && !codeContains.isBlank();

        if (hasName && hasCode) {
            return repository
                    .findByNameContainingIgnoreCaseAndCodeContainingIgnoreCase(nameContains, codeContains, pageable)
                    .map(this::toDTO);
        } else if (hasName) {
            return repository
                    .findByNameContainingIgnoreCase(nameContains, pageable)
                    .map(this::toDTO);
        } else if (hasCode) {
            return repository
                    .findByCodeContainingIgnoreCase(codeContains, pageable)   // ← contains, not exact
                    .map(this::toDTO);
        }
        return repository.findAll(pageable).map(this::toDTO);
    }

    public DepartmentDTO getById(Long id) {
        Department d = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        return toDTO(d);
    }

    public DepartmentDTO getByCode(String code) {
        Department d = repository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        return toDTO(d);
    }

    @Transactional
    public DepartmentDTO create(DepartmentDTO dto) {
        if (repository.existsByCodeIgnoreCase(dto.getCode())) {
            throw new IllegalArgumentException("Department code already exists");
        }
        Department d = fromDTO(dto);
        // do NOT set id; JPA will generate it
        return toDTO(repository.save(d));
    }

    @Transactional
    public DepartmentDTO update(Long id, DepartmentDTO dto) {
        Department existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        if (repository.existsByCodeIgnoreCaseAndIdNot(dto.getCode(), id)) {
            throw new IllegalArgumentException("Department code already exists");
        }

        existing.setName(dto.getName());
        existing.setCode(dto.getCode());
        existing.setDescription(dto.getDescription());
        existing.setManagerEmail(dto.getManagerEmail());

        return toDTO(repository.save(existing));
    }

    @Transactional
    public DepartmentDTO patch(Long id, DepartmentPatchDTO patch) {
        Department existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        if (patch.code() != null &&
                repository.existsByCodeIgnoreCaseAndIdNot(patch.code(), id)) {
            throw new IllegalArgumentException("Department code already exists");
        }

        if (patch.name() != null) existing.setName(patch.name());
        if (patch.code() != null) existing.setCode(patch.code());
        if (patch.description() != null) existing.setDescription(patch.description());
        if (patch.managerEmail() != null) existing.setManagerEmail(patch.managerEmail());

        return toDTO(repository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        Department d = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        long refCount = 0;
        try { refCount = employeeClient.countByDepartment(id); } catch (Exception ignored) {}

        if (refCount > 0) {
            throw new BusinessConflictException(
                    "Cannot delete: " + refCount + " employee(s) still reference this department. Move them first.");
        }
        repository.delete(d);
    }

    // ----- mapping -----
    private DepartmentDTO toDTO(Department d) {
        return DepartmentDTO.builder()
                .id(d.getId())
                .name(d.getName())
                .code(d.getCode())
                .description(d.getDescription())
                .managerEmail(d.getManagerEmail())
                .build();
    }


    private Department fromDTO(DepartmentDTO dto) {
        Department d = new Department();
        d.setName(dto.getName());
        d.setCode(dto.getCode());
        d.setDescription(dto.getDescription());
        d.setManagerEmail(dto.getManagerEmail());
        return d;
    }
}
