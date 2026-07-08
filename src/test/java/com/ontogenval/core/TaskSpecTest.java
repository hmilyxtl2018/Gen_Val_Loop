package com.ontogenval.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskSpecTest {

    private TaskSpec minimalTask() {
        return new TaskSpec(
                "t1",
                "Objective text",
                "target_keyword=DONE",
                List.of("fact-a", "fact-b"),
                List.of("ConceptA"),
                List.of("goal-x"),
                List.of("criterion-1", "criterion-2")
        );
    }

    @Test
    void taskId_blankThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new TaskSpec("  ", "obj", "tgt", List.of(), List.of(), List.of(), List.of()));
    }

    @Test
    void taskId_nullThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new TaskSpec(null, "obj", "tgt", List.of(), List.of(), List.of(), List.of()));
    }

    @Test
    void toInputOntology_containsIntention() {
        OntologyFrame frame = minimalTask().toInputOntology();
        assertTrue(frame.statements().stream()
                .anyMatch(s -> s.kind() == StatementKind.INTENTION && s.object().equals("Objective text")));
    }

    @Test
    void toInputOntology_containsGoalForTarget() {
        OntologyFrame frame = minimalTask().toInputOntology();
        assertTrue(frame.statements().stream()
                .anyMatch(s -> s.kind() == StatementKind.GOAL && s.object().equals("target_keyword=DONE")));
    }

    @Test
    void toInputOntology_containsAllFacts() {
        OntologyFrame frame = minimalTask().toInputOntology();
        long factCount = frame.statements().stream()
                .filter(s -> s.kind() == StatementKind.FACT && s.predicate().equals("hasFact"))
                .count();
        assertEquals(2, factCount);
    }

    @Test
    void toInputOntology_containsAllCriteria() {
        OntologyFrame frame = minimalTask().toInputOntology();
        long criterionCount = frame.statements().stream()
                .filter(s -> s.kind() == StatementKind.CRITERION)
                .count();
        assertEquals(2, criterionCount);
    }

    @Test
    void toInputOntology_isImmutableToFactsChange() {
        TaskSpec task = minimalTask();
        OntologyFrame frame = task.toInputOntology();
        int before = frame.statements().size();
        // taskSpec facts list is unmodifiable — adding to it should throw
        assertThrows(UnsupportedOperationException.class, () -> task.facts().add("extra"));
        assertEquals(before, frame.statements().size());
    }
}
