package com.example.department.web;

import com.example.department.client.EmployeeClient;
import com.example.department.dto.DepartmentDTO;
import com.example.department.dto.DepartmentPatchDTO;
import com.example.department.dto.EmployeeSummaryDTO;
import com.example.department.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentService service;
    private final EmployeeClient employeeClient;

    // explicit constructor so we don't rely on Lombok here
    public DepartmentController(DepartmentService service, EmployeeClient employeeClient) {
        this.service = service;
        this.employeeClient = employeeClient;
    }

    @GetMapping
    public Page<DepartmentDTO> list(@RequestParam(required = false) String nameContains,
                                    @RequestParam(required = false) String code,
                                    Pageable pageable) {
        return service.list(nameContains, code, pageable);
    }

    @GetMapping("/{id}")
    public DepartmentDTO byId(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentDTO create(@Valid @RequestBody DepartmentDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public DepartmentDTO update(@PathVariable Long id, @Valid @RequestBody DepartmentDTO dto) {
        return service.update(id, dto);
    }

    @PatchMapping("/{id}")
    public DepartmentDTO patch(@PathVariable Long id, @RequestBody DepartmentPatchDTO patch) {
        return service.patch(id, patch);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/by-code/{code}")
    public DepartmentDTO byCode(@PathVariable String code) {
        return service.getByCode(code);
    }


    @GetMapping("/{id}/employees")
    public List<EmployeeSummaryDTO> employeesOfDepartment(@PathVariable Long id) {
        // trigger 404 semantics if dept missing
        service.getById(id);
        return employeeClient.listByDepartment(id);
    }

}
