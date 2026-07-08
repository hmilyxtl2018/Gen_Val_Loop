package com.ontogenval.loop;

import com.ontogenval.core.ValidationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConvergenceRuleTest {

    private static ValidationResult result(boolean valid, double score, double gap) {
        return new ValidationResult(1, valid, score, gap, List.of(), "");
    }

    @Test
    void converged_whenValidScoreHighGapLow() {
        ConvergenceRule rule = new ConvergenceRule(0.9, 0.1);
        assertTrue(rule.converged(result(true, 0.95, 0.05)));
    }

    @Test
    void notConverged_whenInvalid() {
        ConvergenceRule rule = new ConvergenceRule(0.9, 0.1);
        assertFalse(rule.converged(result(false, 0.95, 0.05)));
    }

    @Test
    void notConverged_whenScoreBelowThreshold() {
        ConvergenceRule rule = new ConvergenceRule(0.9, 0.1);
        assertFalse(rule.converged(result(true, 0.89, 0.05)));
    }

    @Test
    void notConverged_whenGapAboveThreshold() {
        ConvergenceRule rule = new ConvergenceRule(0.9, 0.1);
        assertFalse(rule.converged(result(true, 0.95, 0.11)));
    }

    @Test
    void converged_atExactThresholds() {
        ConvergenceRule rule = new ConvergenceRule(0.9, 0.1);
        assertTrue(rule.converged(result(true, 0.9, 0.1)));
    }
}
