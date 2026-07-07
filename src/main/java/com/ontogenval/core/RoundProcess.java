package com.ontogenval.core;

public record RoundProcess(int roundIndex, OntologyFrame ontology) {
    public RoundProcess {
        if (roundIndex < 1) {
            throw new IllegalArgumentException("roundIndex must be >= 1");
        }
        ontology = ontology == null ? new OntologyFrame("round-process", null) : ontology;
    }
}
