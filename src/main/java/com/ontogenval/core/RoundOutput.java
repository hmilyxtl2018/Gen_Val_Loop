package com.ontogenval.core;

public record RoundOutput(
        int roundIndex,
        Candidate candidate,
        ValidationResult validation,
        boolean converged,
        OntologyFrame ontology
) {
    public RoundOutput {
        if (roundIndex < 1) {
            throw new IllegalArgumentException("roundIndex must be >= 1");
        }
        ontology = ontology == null ? new OntologyFrame("round-output", null) : ontology;
    }
}
