package com.example.department.web;

import com.example.department.domain.Department;
import com.example.department.repo.DepartmentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepository repository;


    @GetMapping
    public List<Department> all() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Department byId(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Department " + id + " not found"));
    }

//    @PostMapping
//    @ResponseStatus(HttpStatus.CREATED)
//    public Department create(@Valid @RequestBody Department d) {
//        return repository.save(d);
//    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Department create(@Valid @RequestBody Department d) {
        String code = d.getCode().trim(); // validation ensures not blank
        if (repository.existsByCodeIgnoreCase(code)) {
            // maps to 409 in GlobalExceptionHandler
            throw new IllegalArgumentException("Department code already exists");
        }
        d.setCode(code); // normalize if you want
        return repository.save(d);
    }

}
