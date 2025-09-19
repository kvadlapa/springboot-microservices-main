package com.example.employee.error;

public class BusinessConflictException extends RuntimeException{
    public BusinessConflictException(String message){
        super(message);
    }
}
