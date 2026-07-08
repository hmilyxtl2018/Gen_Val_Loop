package com.ontogenval.loop;

import com.ontogenval.agent.GenAgent;
import com.ontogenval.agent.ValAgent;
import com.ontogenval.core.RoundTrace;
import com.ontogenval.core.TaskSpec;
import com.ontogenval.llm.MockModelClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GenValLoop beyond the happy-path convergence scenario:
 * exhaustion (maxRounds reached without convergence) and single-round convergence.
 */
class GenValLoopEdgeCaseTest {

    private static TaskSpec alwaysFailTask() {
        // A task whose target keyword will never appear in mock output on round 1
        // (no DONE/evidence in first round without prior feedback)
        return new TaskSpec(
                "always-fail",
                "Task that will not converge within 1 round.",
                "target_keyword=DONE",
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    private static TaskSpec easyTask() {
        return new TaskSpec(
                "easy-task",
                "Task that converges immediately.",
                "target_keyword=DONE",
                List.of(),
                List.of(),
                List.of(),
                List.of("DONE is present.", "Acceptance evidence is present.")
        );
    }

    @Test
    void loop_exhaustsMaxRounds_returnsNotConverged() {
        // MockModelClient round-1 output lacks DONE → score 0.55 → not valid (< 0.9)
        // With maxRounds=1 it must stop and return converged=false
        GenValLoop loop = new GenValLoop(
                new GenAgent(new MockModelClient()),
                new ValAgent(new MockModelClient()),
                new ConvergenceRule(0.9, 0.1),
                new LoopConfig(1)
        );

        LoopOutcome outcome = loop.run(alwaysFailTask());

        assertFalse(outcome.converged(), "should not converge when maxRounds exhausted");
        assertEquals(1, outcome.traces().size(), "exactly 1 round should have been executed");
        assertFalse(outcome.finalTrace().output().converged());
    }

    @Test
    void loop_exhausted_finalTraceMatchesLastRound() {
        GenValLoop loop = new GenValLoop(
                new GenAgent(new MockModelClient()),
                new ValAgent(new MockModelClient()),
                new ConvergenceRule(0.9, 0.1),
                new LoopConfig(2)
        );

        LoopOutcome outcome = loop.run(alwaysFailTask());

        // With 2 rounds: round-1 fails (no DONE), round-2 has feedback so adds DONE+evidence → valid
        // MockModelClient with feedback adds DONE and evidence → score >= 0.9 → converges at round 2
        assertTrue(outcome.converged());
        assertEquals(outcome.finalTrace(), outcome.traces().get(outcome.traces().size() - 1));
    }

    @Test
    void loop_tracesHaveCorrectRoundIndices() {
        GenValLoop loop = new GenValLoop(
                new GenAgent(new MockModelClient()),
                new ValAgent(new MockModelClient()),
                new ConvergenceRule(0.9, 0.1),
                new LoopConfig(3)
        );

        LoopOutcome outcome = loop.run(alwaysFailTask());

        for (int i = 0; i < outcome.traces().size(); i++) {
            RoundTrace trace = outcome.traces().get(i);
            assertEquals(i + 1, trace.input().roundIndex());
            assertEquals(i + 1, trace.process().roundIndex());
            assertEquals(i + 1, trace.output().roundIndex());
        }
    }

    @Test
    void loop_round2Input_carryFeedbackFromRound1() {
        GenValLoop loop = new GenValLoop(
                new GenAgent(new MockModelClient()),
                new ValAgent(new MockModelClient()),
                new ConvergenceRule(0.9, 0.1),
                new LoopConfig(3)
        );

        LoopOutcome outcome = loop.run(alwaysFailTask());
        assertTrue(outcome.traces().size() >= 2, "need at least 2 rounds");

        RoundTrace round2 = outcome.traces().get(1);
        boolean hasFeedback = round2.input().ontology().statements().stream()
                .anyMatch(s -> s.kind() == com.ontogenval.core.StatementKind.FEEDBACK);
        assertTrue(hasFeedback, "round-2 input ontology should carry round-1 feedback");
    }

    @Test
    void loopOutcome_tracesListIsUnmodifiable() {
        GenValLoop loop = new GenValLoop(
                new GenAgent(new MockModelClient()),
                new ValAgent(new MockModelClient()),
                new ConvergenceRule(0.9, 0.1),
                new LoopConfig(1)
        );
        LoopOutcome outcome = loop.run(alwaysFailTask());
        assertThrows(UnsupportedOperationException.class, () ->
                outcome.traces().add(outcome.finalTrace()));
    }
}
