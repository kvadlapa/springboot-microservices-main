package com.example.department;

import com.example.department.client.EmployeeClient;
import com.example.department.client.NoopEmployeeClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class DepartmentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DepartmentServiceApplication.class, args);



    }
    @Bean
    public EmployeeClient employeeClient() {
        return new NoopEmployeeClient();
    }

}
