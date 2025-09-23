package com.example.employee.web;

import com.example.employee.dto.DepartmentStatsDTO;
import com.example.employee.dto.EmployeeDTO;
import com.example.employee.dto.EmployeePatchDTO;
import com.example.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService service;

    // Array when no paging requested
    @GetMapping(params = {"!page", "!size"})
    public List<EmployeeDTO> all(
            @RequestParam Optional<String> email,
            @RequestParam Optional<String> lastNameContains,
            @RequestParam Optional<Long> departmentId
    ) {
        // when no paging, return full array (tests commonly expect this)
        // if any filter supplied, we can still honor them by delegating to list(...) unpaged
        if (email.isPresent() || lastNameContains.isPresent() || departmentId.isPresent()) {
            Page<EmployeeDTO> page = service.list(email, lastNameContains, departmentId, Pageable.unpaged());
            return page.getContent();
        }
        return service.getAll();
    }

    // Page when page/size present; sorting via ?sort=lastName,asc handled by Spring Data
    @GetMapping(params = {"page", "size"})
    public Page<EmployeeDTO> list(
            @RequestParam Optional<String> email,
            @RequestParam Optional<String> lastNameContains,
            @RequestParam Optional<Long> departmentId,
            Pageable pageable
    ) {
        return service.list(email, lastNameContains, departmentId, pageable);
    }

    @GetMapping("/{id}")
    public EmployeeDTO byId(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeDTO create(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody EmployeeDTO dto
    ) {
        return service.create(dto, idempotencyKey);
    }

    @PutMapping("/{id}")
    public EmployeeDTO update(@PathVariable Long id, @Valid @RequestBody EmployeeDTO dto) {
        return service.update(id, dto);
    }

    @PatchMapping("/{id}")
    public EmployeeDTO patch(@PathVariable Long id, @Valid @RequestBody EmployeePatchDTO patch) {
        return service.patch(id, patch);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/search")
    public List<EmployeeDTO> search(@RequestParam("q") String q) {
        return service.search(q);
    }

    @GetMapping("/stats")
    public List<DepartmentStatsDTO> stats() {
        return service.stats();
    }
}
