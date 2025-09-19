package com.example.employee.web;

import com.example.employee.ProblemTypes;
import com.example.employee.error.BusinessConflictException;
import com.example.employee.error.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.MDC;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {



    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                ex.getMessage() == null ? "Not found" : ex.getMessage());
        pd.setTitle("Not Found");
        pd.setType(URI.create(ProblemTypes.NOT_FOUND));
        enrich(pd, req);
        return pd;
    }



    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex, HttpServletRequest req){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Not Found");
        pd.setType(URI.create(ProblemTypes.NOT_FOUND));
        enrich(pd, req);

        return pd;
    }

    //400 Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                      HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        pd.setTitle("Bad Request");
        pd.setType(URI.create(ProblemTypes.VALIDATION));
        enrich(pd, req);

        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::toFieldError)
                .toList();
        pd.setProperty("errors", errors);
        return pd;
    }

    private static Map<String, String> toFieldError(FieldError fe) {
        return Map.of(
                "field", fe.getField(),
                "message", fe.getDefaultMessage() == null ? "Invalid value" : fe.getDefaultMessage()
        );
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    ProblemDetail handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        pd.setTitle("Bad Request");
        pd.setType(URI.create(ProblemTypes.VALIDATION));
        enrich(pd, req);
        pd.setProperty("errors", ex.getConstraintViolations().stream()
                .map(v -> Map.of("field", v.getPropertyPath().toString(), "message", v.getMessage()))
                .toList());
        return pd;
    }

    // common fields for all Exception Handlers
    private static void enrich(ProblemDetail pd, HttpServletRequest req) {
        pd.setInstance(URI.create(req.getRequestURI()));              // instance (path)
        pd.setProperty("timestamp", OffsetDateTime.now().toString()); // ISO timestamp

        String traceId = MDC.get(TraceIdFilter.MDC_KEY);
        if (traceId == null || traceId.isBlank()) {
            traceId = req.getHeader(TraceIdFilter.HEADER);            // fallback to header
        }
        pd.setProperty("traceId", traceId == null ? "" : traceId);
    }

    //409
    @ExceptionHandler({
            BusinessConflictException.class,          // if you use your custom one
            IllegalArgumentException.class,           // your service currently throws this
            DataIntegrityViolationException.class     // in case a DB unique index fires
    })
    public ProblemDetail handleConflict(RuntimeException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                ex.getMessage() == null ? "Conflict" : ex.getMessage());
        pd.setTitle("Conflict");
        pd.setType(URI.create(ProblemTypes.CONFLICT));
        enrich(pd, req);
        return pd;
    }

    // 409: Hibernate constraint (DB unique index)
    @ExceptionHandler(org.hibernate.exception.ConstraintViolationException.class)
    public ProblemDetail handleHibernateConstraint(org.hibernate.exception.ConstraintViolationException ex,
                                                   HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                ex.getMessage() == null ? "Conflict" : ex.getMessage());
        pd.setTitle("Conflict");
        pd.setType(URI.create(ProblemTypes.CONFLICT));
        enrich(pd, req);
        return pd;
    }

    // 409: Duplicate key (some drivers/adapters)
    @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class)
    public ProblemDetail handleDuplicateKey(org.springframework.dao.DuplicateKeyException ex,
                                            HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                ex.getMessage() == null ? "Conflict" : ex.getMessage());
        pd.setTitle("Conflict");
        pd.setType(URI.create(ProblemTypes.CONFLICT));
        enrich(pd, req);
        return pd;
    }

    // 409: When the real constraint error is wrapped in a TransactionSystemException
    @ExceptionHandler(org.springframework.transaction.TransactionSystemException.class)
    public ProblemDetail handleTxSystem(org.springframework.transaction.TransactionSystemException ex,
                                        HttpServletRequest req) {
        if (hasConstraintViolationCause(ex)) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Conflict");
            pd.setTitle("Conflict");
            pd.setType(URI.create(ProblemTypes.CONFLICT));
            enrich(pd, req);
            return pd;
        }
        // fall back to your 500 builder
        log.error("Unhandled TX exception -> {}", ex.getClass().getName(), ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred");
        pd.setTitle("Internal Server Error");
        pd.setType(URI.create(ProblemTypes.INTERNAL));
        enrich(pd, req);
        return pd;
    }

    // helper to walk the cause chain
    private static boolean hasConstraintViolationCause(Throwable ex) {
        Throwable t = ex;
        while (t != null) {
            if (t instanceof org.hibernate.exception.ConstraintViolationException) return true;
            t = t.getCause();
        }
        return false;
    }


    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnknown(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception on {} {} -> {}",
                req.getMethod(), req.getRequestURI(), ex.getClass().getName(), ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred");
        pd.setTitle("Internal Server Error");
        pd.setType(URI.create(ProblemTypes.INTERNAL));
        enrich(pd, req);
        return pd;
    }




}
