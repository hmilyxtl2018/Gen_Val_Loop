package com.ontogenval.core;

public record OntologyStatement(
        StatementKind kind,
        String subject,
        String predicate,
        String object
) {
    public OntologyStatement {
        kind = kind == null ? StatementKind.FACT : kind;
        subject = normalize(subject);
        predicate = normalize(predicate);
        object = normalize(object);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
