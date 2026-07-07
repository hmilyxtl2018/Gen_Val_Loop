package com.ontogenval.loop;

import com.ontogenval.agent.GenAgent;
import com.ontogenval.agent.ValAgent;
import com.ontogenval.core.RoundTrace;
import com.ontogenval.core.StatementKind;
import com.ontogenval.core.TaskSpec;
import com.ontogenval.llm.MockModelClient;
import com.ontogenval.llm.ModelClient;

import java.util.List;

public final class RoundOntologyLoopTest {
    public static void main(String[] args) {
        ModelClient model = new MockModelClient();
        GenValLoop loop = new GenValLoop(
                new GenAgent(model),
                new ValAgent(model),
                new ConvergenceRule(0.9, 0.1),
                new LoopConfig(3)
        );

        LoopOutcome outcome = loop.run(new TaskSpec(
                "round-ontology-test",
                "Test one complete Gen-Val loop with round ontology.",
                "target_keyword=DONE",
                List.of("Round input, process, and output must be explicitly represented."),
                List.of("RoundInput", "RoundProcess", "RoundOutput"),
                List.of("Converge after feedback repair."),
                List.of("DONE is present.", "Acceptance evidence is present.")
        ));

        assertTrue(outcome.converged(), "loop should converge");
        assertEquals(2, outcome.traces().size(), "mock loop should converge in round 2");

        RoundTrace first = outcome.traces().get(0);
        assertFalse(first.output().validation().valid(), "round 1 should be invalid");
        assertFalse(first.output().converged(), "round 1 should not converge");
        assertContainsKind(first.input().ontology().statements(), StatementKind.FACT, "round 1 input should contain facts");
        assertContainsKind(first.process().ontology().statements(), StatementKind.VALIDATION, "round 1 process should contain validation");
        assertContainsKind(first.output().ontology().statements(), StatementKind.FEEDBACK, "round 1 output should contain feedback");

        RoundTrace second = outcome.traces().get(1);
        assertTrue(second.output().validation().valid(), "round 2 should be valid");
        assertTrue(second.output().converged(), "round 2 should converge");
        assertTrue(second.output().candidate().content().contains("DONE"), "final candidate should contain DONE");
        assertTrue(
                second.output().candidate().content().toLowerCase().contains("evidence"),
                "final candidate should contain evidence"
        );
        assertContainsKind(second.input().ontology().statements(), StatementKind.FEEDBACK, "round 2 input should carry feedback");
        assertContainsKind(second.output().ontology().statements(), StatementKind.DECISION, "round 2 output should contain decision");

        System.out.println("RoundOntologyLoopTest passed");
    }

    private static void assertContainsKind(
            List<com.ontogenval.core.OntologyStatement> statements,
            StatementKind kind,
            String message
    ) {
        boolean found = statements.stream().anyMatch(item -> item.kind() == kind);
        assertTrue(found, message);
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected=" + expected + ", actual=" + actual);
        }
    }
}
