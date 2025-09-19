package com.example.department.web;

import com.example.department.ProblemTypes;
import com.example.department.error.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------- 404 ----------
    @ExceptionHandler({ ResourceNotFoundException.class, EntityNotFoundException.class })
    public ProblemDetail handleNotFound(RuntimeException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage() == null ? "Not found" : ex.getMessage()
        );
        pd.setTitle("Not Found");
        pd.setType(URI.create(ProblemTypes.NOT_FOUND));
        enrich(pd, req);
        return pd;
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            org.springframework.dao.DataIntegrityViolationException.class,
            org.springframework.dao.DuplicateKeyException.class
    })
    public ProblemDetail handleConflict(RuntimeException ex, HttpServletRequest req) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                ex.getMessage() == null ? "Conflict" : ex.getMessage());
        pd.setTitle("Conflict");
        pd.setType(URI.create(ProblemTypes.CONFLICT));
        enrich(pd, req);
        return pd;
    }

    // 500: catch-all (kept minimal so you don’t see default JSON)
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnknown(Exception ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred"
        );
        pd.setTitle("Internal Server Error");
        pd.setType(URI.create(ProblemTypes.INTERNAL));
        enrich(pd, req);
        return pd;
    }


    // 400: body validation errors (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                      HttpServletRequest req) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
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


    private static void enrich(ProblemDetail pd, HttpServletRequest req) {
        pd.setInstance(URI.create(req.getRequestURI()));
        pd.setProperty("timestamp", OffsetDateTime.now().toString());
        String traceId = MDC.get(TraceIdFilter.MDC_KEY);
        if (traceId == null || traceId.isBlank()) traceId = req.getHeader(TraceIdFilter.HEADER);
        pd.setProperty("traceId", traceId == null ? "" : traceId);
    }

}
