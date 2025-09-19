package com.example.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServiceApplication.class, args);
    }
}


/*
    Eureka is a service registry from Spring Cloud Netflix
    It keeps track of all microservices(Eureka Clients) and their locations(IP and port).
    Other Services and API gateway use it as a phonebook to discover and communicate with each other
    dyncamically using the names instead of hard-coded URLs.
    In Short, Eureka Server= Phonebook/ Service Registry of all Eureka Clients

    How can I say given microservice is actually a DiscoveryService?



 */