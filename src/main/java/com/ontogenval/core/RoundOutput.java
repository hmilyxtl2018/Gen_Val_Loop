package com.ontogenval.core;

import java.util.List;

/**
 * @param candidate       the winning candidate (highest validation score) for this round
 * @param validation      the winning candidate's validation result
 * @param allEvaluations  all candidates evaluated this round (size == candidatesPerRound)
 */
public record RoundOutput(
        int roundIndex,
        Candidate candidate,
        ValidationResult validation,
        boolean converged,
        OntologyFrame ontology,
        List<CandidateEvaluation> allEvaluations
) {
    public RoundOutput {
        if (roundIndex < 1) {
            throw new IllegalArgumentException("roundIndex must be >= 1");
        }
        ontology = ontology == null ? new OntologyFrame("round-output", null) : ontology;
        allEvaluations = List.copyOf(allEvaluations == null ? List.of() : allEvaluations);
    }
}
