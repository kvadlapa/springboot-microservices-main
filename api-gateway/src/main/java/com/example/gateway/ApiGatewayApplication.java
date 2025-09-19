package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}


/*

        In this project api-gateway is the API gateway. discovery-service is Eureka server
       Spring Cloud Gateway/ Spring API Gateway
       An API gateway is the single entry point for all the clients(web apps, mobile apps, external apps)
       requests in microservice architecture.
       Instead of clients calling each service directly, clients sends requests to the API gateway.
       The gateway then routes the requests to right microservice, using eureka for service registry if needed.
       --> It also handles cross-cutting concerns like authentication, logging, load balancing, rate limiting,
       and monitoring in one place

       How can we tell the given microservice is an API gateway:
       --> Usually by using the name
       --> It has the spring cloud gateway dependencies instead of business logic
       --> It's application.yml defines roots instead of entities/services. Example:
       spring:
         cloud:
             gateway:
                routes:
                 - id: employee_route
                    uri: lb://employee-service
                    predicates:
                    - Path=/employees/**
        --> It typically connects on common port(8080)


 */

