package com.example.employee;

public final class ProblemTypes {
    /*
    ProblemType (RFC 7807) = a stable, machine-readable identifier (URI/URN) for an error category.
    Why it's useful:
    --> makes a clear distinction between errors beyond status codes (e.g., validation vs conflict—both 4xx).
    --> Enables reliable client logic (UI can branch on type, not parse messages).
    --> Ensures cross-service consistency and clean documentation (URI can link to a help page).
    --> Improves observability (count/alert by type; track SLOs).
 */
    private ProblemTypes() {}
    public static final String VALIDATION = "urn:problem:validation";
    public static final String NOT_FOUND  = "urn:problem:not-found";
    public static final String CONFLICT   = "urn:problem:conflict";
    public static final String INTERNAL   = "urn:problem:internal";
}

