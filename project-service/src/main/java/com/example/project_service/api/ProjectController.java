package com.example.project_service.api;

import com.example.project_service.api.dto.AddMemberRequest;
import com.example.project_service.api.dto.CreateProjectRequest;
import com.example.project_service.api.dto.PageResponse;
import com.example.project_service.api.dto.PatchProjectRequest;
import com.example.project_service.api.dto.UpdateProjectRequest;
import com.example.project_service.api.spec.ProjectSpecs;
import com.example.project_service.client.EmployeeClient;
import com.example.project_service.exception.BadRequestException;
import com.example.project_service.exception.NotFoundException;
import com.example.project_service.model.Project;
import com.example.project_service.model.ProjectStatus;
import com.example.project_service.repo.ProjectMemberRepository;
import com.example.project_service.repo.ProjectRepository;
import com.example.project_service.service.ProjectService;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectRepository projectRepo;
    private final ProjectMemberRepository memberRepo;
    private final ProjectService service;
    private final EmployeeClient employeeClient;
    private final EntityManager em;

    public ProjectController(ProjectRepository projectRepo,
                             ProjectMemberRepository memberRepo,
                             ProjectService service,
                             EmployeeClient employeeClient,
                             EntityManager em) {
        this.projectRepo = projectRepo;
        this.memberRepo = memberRepo;
        this.service = service;
        this.employeeClient = employeeClient;
        this.em = em;
    }

    // 1) List projects with filters + paging + sort
    @GetMapping
    public PageResponse<Project> list(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            Pageable pageable) {

        Specification<Project> spec = Specification.allOf(
                ProjectSpecs.hasStatus(status),
                ProjectSpecs.codeEquals(code),
                ProjectSpecs.nameContains(name),
                ProjectSpecs.startFrom(from),
                ProjectSpecs.endTo(to)
        );


        Page<Project> page = projectRepo.findAll(spec, pageable);
        String sort = pageable.getSort().stream()
                .map(o -> o.getProperty() + "," + o.getDirection())
                .collect(Collectors.joining(";"));

        return PageResponse.from(page, sort);
    }

    // 2) Get one project
    @GetMapping("/{id}")
    public Project getOne(@PathVariable Long id) {
        return projectRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found: " + id));
    }

    // 3) Create project
    @PostMapping
    public ResponseEntity<Project> create(@Valid @RequestBody CreateProjectRequest req) {
        var p = new Project();
        p.setCode(req.code());
        p.setName(req.name());
        p.setDescription(req.description());
        p.setStatus(req.status());
        p.setStartDate(req.startDate());
        p.setEndDate(req.endDate());

        var saved = service.create(p);
        return ResponseEntity.created(URI.create("/api/v1/projects/" + saved.getId())).body(saved);
    }

    // 4) Update project (full) — policy: code change FORBIDDEN (400 if attempted)
    @PutMapping("/{id}")
    public Project put(@PathVariable Long id, @Valid @RequestBody UpdateProjectRequest req) {
        var updated = new Project();
        updated.setCode(req.code());
        updated.setName(req.name());
        updated.setDescription(req.description());
        updated.setStatus(req.status());
        updated.setStartDate(req.startDate());
        updated.setEndDate(req.endDate());
        return service.updateFull(id, updated, false);
    }

    // 5) Patch project (partial): status, endDate, description
    @PatchMapping("/{id}")
    public Project patch(@PathVariable Long id, @RequestBody PatchProjectRequest req) {
        return service.patch(id, req.status(), req.endDate(), req.description());
    }

    // 6) Delete project — cascades to members (per DB FK), returns 204
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // 7) Add project members (BATCH) — 207 Multi-Status, per-item results
    @PostMapping("/{id}/members")
    public ResponseEntity<Map<String, Object>> addMembers(@PathVariable Long id,
                                                          @Valid @RequestBody List<AddMemberRequest> reqs) {
        var results = new ArrayList<Map<String, Object>>();

        for (var r : reqs) {
            var entry = new HashMap<String, Object>();
            entry.put("employeeId", r.employeeId());
            try {
                var saved = service.addMember(id, r);
                entry.put("status", 201);
                entry.put("id", saved.getId());
            } catch (NotFoundException nf) {
                entry.put("status", 404);
                entry.put("error", nf.getMessage());
            } catch (RuntimeException ex) { // Conflict, BadRequest, etc.
                entry.put("status", 409);
                entry.put("error", ex.getMessage());
            }
            results.add(entry);
        }
        return ResponseEntity.status(207).body(Map.of("results", results));
    }

    // 8) List members; enrich=true includes employee snapshot from Employee Service
    @GetMapping("/{id}/members")
    public List<Map<String, Object>> listMembers(@PathVariable Long id,
                                                 @RequestParam(defaultValue = "false") boolean enrich) {
        var list = service.listMembers(id);
        if (!enrich) {
            return list.stream()
                    .map(m -> {
                        Map<String,Object> row = new LinkedHashMap<>();
                        row.put("employeeId", m.getEmployeeId());
                        row.put("role", m.getRole());
                        row.put("allocationPercent", m.getAllocationPercent());
                        row.put("assignedAt", m.getAssignedAt());
                        return row;
                    })
                    .toList();
        }

        return list.stream().map(m -> {
            Map<String, Object> emp;
            try {
                var snap = employeeClient.getEmployee(m.getEmployeeId());
                emp = Map.of(
                        "id", snap.id(),
                        "firstName", snap.firstName(),
                        "lastName", snap.lastName(),
                        "email", snap.email()
                );
            } catch (Exception e) {
                emp = Map.of("id", m.getEmployeeId(), "error", "not found");
            }
            return Map.of(
                    "employee", emp,
                    "role", m.getRole(),
                    "allocationPercent", m.getAllocationPercent(),
                    "assignedAt", m.getAssignedAt()
            );
        }).toList();
    }

    // 9) Remove project member
    @DeleteMapping("/{id}/members/{employeeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable Long id, @PathVariable Long employeeId) {
        service.removeMember(id, employeeId);
    }

    // 10) Summary & stats
    @GetMapping("/stats")
    public Map<String, Object> stats(@RequestParam String groupBy) {
        if ("status".equalsIgnoreCase(groupBy)) {
            var rows = em.createQuery("""
                select p.status, count(p) from Project p group by p.status
            """, Object[].class).getResultList();

            Map<String, Long> counts = new LinkedHashMap<>();
            for (var row : rows) {
                counts.put(((ProjectStatus) row[0]).name(), (Long) row[1]);
            }
            return Map.of("groupBy", "status", "counts", counts);
        } else if ("month".equalsIgnoreCase(groupBy)) {
            var rows = em.createQuery("""
                select function('to_char', p.startDate, 'YYYY-MM'), count(p)
                from Project p
                group by function('to_char', p.startDate, 'YYYY-MM')
                order by 1
            """, Object[].class).getResultList();

            Map<String, Long> counts = new LinkedHashMap<>();
            for (var row : rows) {
                counts.put((String) row[0], (Long) row[1]);
            }
            return Map.of("groupBy", "month", "counts", counts);
        }
        throw new BadRequestException("groupBy must be 'status' or 'month'");
    }
}
