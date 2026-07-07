package com.ontogenval.core;

import java.util.List;

public record TaskSpec(
        String taskId,
        String objective,
        String target,
        List<String> facts,
        List<String> concepts,
        List<String> goals,
        List<String> criteria
) {
    public TaskSpec {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("taskId must not be blank");
        }
        objective = objective == null ? "" : objective.trim();
        target = target == null ? "" : target.trim();
        facts = List.copyOf(facts == null ? List.of() : facts);
        concepts = List.copyOf(concepts == null ? List.of() : concepts);
        goals = List.copyOf(goals == null ? List.of() : goals);
        criteria = List.copyOf(criteria == null ? List.of() : criteria);
    }

    public OntologyFrame toInputOntology() {
        List<OntologyStatement> statements = new java.util.ArrayList<>();
        statements.add(new OntologyStatement(StatementKind.INTENTION, taskId, "objective", objective));
        statements.add(new OntologyStatement(StatementKind.GOAL, taskId, "target", target));
        facts.forEach(item -> statements.add(new OntologyStatement(StatementKind.FACT, taskId, "hasFact", item)));
        concepts.forEach(item -> statements.add(new OntologyStatement(StatementKind.CONCEPT, taskId, "usesConcept", item)));
        goals.forEach(item -> statements.add(new OntologyStatement(StatementKind.GOAL, taskId, "hasGoal", item)));
        criteria.forEach(item -> statements.add(new OntologyStatement(StatementKind.CRITERION, taskId, "acceptsWhen", item)));
        return new OntologyFrame("task-input", statements);
    }
}
