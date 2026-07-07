package com.ontogenval.loop;

import com.ontogenval.core.ValidationResult;

public final class ConvergenceRule {
    private final double minScore;
    private final double maxGap;

    public ConvergenceRule(double minScore, double maxGap) {
        this.minScore = minScore;
        this.maxGap = maxGap;
    }

    public boolean converged(ValidationResult validation) {
        return validation.valid()
                && validation.score() >= minScore
                && validation.gap() <= maxGap;
    }
}
