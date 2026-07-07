package com.ontogenval.core;

import java.util.List;

public record ValidationResult(
        int roundIndex,
        boolean valid,
        double score,
        double gap,
        List<String> issues,
        String feedback
) {
    public ValidationResult {
        score = Math.max(0.0, Math.min(1.0, score));
        gap = Math.max(0.0, Math.min(1.0, gap));
        issues = List.copyOf(issues == null ? List.of() : issues);
        feedback = feedback == null ? "" : feedback.trim();
    }
}
