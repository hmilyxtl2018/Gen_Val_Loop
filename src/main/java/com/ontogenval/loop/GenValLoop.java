package com.ontogenval.loop;

import com.ontogenval.agent.GenAgent;
import com.ontogenval.agent.ValAgent;
import com.ontogenval.core.Candidate;
import com.ontogenval.core.OntologyFrame;
import com.ontogenval.core.OntologyStatement;
import com.ontogenval.core.RoundInput;
import com.ontogenval.core.RoundOutput;
import com.ontogenval.core.RoundProcess;
import com.ontogenval.core.RoundTrace;
import com.ontogenval.core.StatementKind;
import com.ontogenval.core.TaskSpec;
import com.ontogenval.core.ValidationResult;

import java.util.ArrayList;
import java.util.List;

public final class GenValLoop {
    private final GenAgent genAgent;
    private final ValAgent valAgent;
    private final ConvergenceRule convergenceRule;
    private final LoopConfig config;

    public GenValLoop(GenAgent genAgent, ValAgent valAgent, ConvergenceRule convergenceRule, LoopConfig config) {
        this.genAgent = genAgent;
        this.valAgent = valAgent;
        this.convergenceRule = convergenceRule;
        this.config = config;
    }

    public LoopOutcome run(TaskSpec task) {
        List<RoundTrace> traces = new ArrayList<>();
        Candidate previousCandidate = null;
        ValidationResult previousValidation = null;
        RoundTrace finalTrace = null;

        for (int round = 1; round <= config.maxRounds(); round++) {
            RoundInput input = new RoundInput(
                    round,
                    task,
                    previousCandidate,
                    previousValidation,
                    buildInputOntology(task, round, previousCandidate, previousValidation)
            );
            Candidate candidate = genAgent.generate(input);
            ValidationResult validation = valAgent.validate(task, candidate);
            boolean converged = convergenceRule.converged(validation);

            RoundProcess process = new RoundProcess(round, buildProcessOntology(round, validation, converged));
            RoundOutput output = new RoundOutput(
                    round,
                    candidate,
                    validation,
                    converged,
                    buildOutputOntology(round, candidate, validation, converged)
            );
            finalTrace = new RoundTrace(input, process, output);
            traces.add(finalTrace);

            if (converged) {
                return new LoopOutcome(true, finalTrace, traces);
            }

            previousCandidate = candidate;
            previousValidation = validation;
        }

        return new LoopOutcome(false, finalTrace, traces);
    }

    private OntologyFrame buildInputOntology(
            TaskSpec task,
            int round,
            Candidate previousCandidate,
            ValidationResult previousValidation
    ) {
        OntologyFrame frame = task.toInputOntology()
                .plus(new OntologyStatement(StatementKind.FACT, "round", "index", String.valueOf(round)));
        if (previousCandidate != null) {
            frame = frame.plus(new OntologyStatement(
                    StatementKind.CANDIDATE,
                    "previousCandidate",
                    "content",
                    previousCandidate.content()
            ));
        }
        if (previousValidation != null) {
            frame = frame.plus(new OntologyStatement(
                    StatementKind.FEEDBACK,
                    "previousValidation",
                    "feedback",
                    previousValidation.feedback()
            ));
        }
        return new OntologyFrame("round-%d-input".formatted(round), frame.statements());
    }

    private OntologyFrame buildProcessOntology(int round, ValidationResult validation, boolean converged) {
        return new OntologyFrame("round-%d-process".formatted(round), List.of(
                new OntologyStatement(StatementKind.DECISION, "GenAgent", "action", "generate candidate"),
                new OntologyStatement(StatementKind.VALIDATION, "ValAgent", "score", String.valueOf(validation.score())),
                new OntologyStatement(StatementKind.VALIDATION, "ValAgent", "gap", String.valueOf(validation.gap())),
                new OntologyStatement(StatementKind.DECISION, "ConvergenceRule", "converged", String.valueOf(converged))
        ));
    }

    private OntologyFrame buildOutputOntology(
            int round,
            Candidate candidate,
            ValidationResult validation,
            boolean converged
    ) {
        return new OntologyFrame("round-%d-output".formatted(round), List.of(
                new OntologyStatement(StatementKind.CANDIDATE, "candidate", "content", candidate.content()),
                new OntologyStatement(StatementKind.VALIDATION, "validation", "valid", String.valueOf(validation.valid())),
                new OntologyStatement(StatementKind.VALIDATION, "validation", "score", String.valueOf(validation.score())),
                new OntologyStatement(StatementKind.VALIDATION, "validation", "gap", String.valueOf(validation.gap())),
                new OntologyStatement(StatementKind.FEEDBACK, "validation", "feedback", validation.feedback()),
                new OntologyStatement(StatementKind.DECISION, "loop", "converged", String.valueOf(converged))
        ));
    }
}
