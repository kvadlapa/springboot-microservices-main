package com.example.department.error;

public class BusinessConflictException extends RuntimeException {
    public BusinessConflictException(String message){ super(message); }
}
