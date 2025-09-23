package com.example.employee.repo;

import com.example.employee.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, QueryByExampleExecutor<Employee> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);

    List<Employee> findByFirstNameIgnoreCaseContainingOrLastNameIgnoreCaseContainingOrEmailIgnoreCaseContaining(
            String q1, String q2, String q3
    );

    interface DeptCount {
        Long getDepartmentId();
        long getCount();
    }

    @Query("select e.departmentId as departmentId, count(e) as count from Employee e group by e.departmentId")
    List<DeptCount> countByDepartment();

    boolean existsByEmailIgnoreCase(String email);
    Optional<Employee> findByEmailIgnoreCase(String email);


    // simple name/email search (case-insensitive)
    @Query("""
           select e from Employee e
           where lower(e.firstName) like lower(concat('%', ?1, '%'))
              or lower(e.lastName)  like lower(concat('%', ?1, '%'))
              or lower(e.email)     like lower(concat('%', ?1, '%'))
           """)
    List<Employee> search(String q);

}
