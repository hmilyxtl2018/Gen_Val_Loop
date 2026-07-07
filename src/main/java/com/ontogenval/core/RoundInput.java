package com.ontogenval.core;

public record RoundInput(
        int roundIndex,
        TaskSpec task,
        Candidate previousCandidate,
        ValidationResult previousValidation,
        OntologyFrame ontology
) {
    public RoundInput {
        if (roundIndex < 1) {
            throw new IllegalArgumentException("roundIndex must be >= 1");
        }
        ontology = ontology == null ? new OntologyFrame("round-input", null) : ontology;
    }
}
