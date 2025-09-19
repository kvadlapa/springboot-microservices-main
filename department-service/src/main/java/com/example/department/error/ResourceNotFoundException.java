package com.example.department.error;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message){ super(message); }
}