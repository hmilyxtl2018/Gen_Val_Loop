package com.ontogenval.core;

/**
 * One candidate and its validation result, produced within a single round.
 * Used to record all N evaluations when candidatesPerRound > 1.
 */
public record CandidateEvaluation(Candidate candidate, ValidationResult validation) {
}
