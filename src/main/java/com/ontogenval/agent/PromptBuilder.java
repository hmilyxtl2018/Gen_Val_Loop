package com.ontogenval.agent;

import com.ontogenval.core.Candidate;
import com.ontogenval.core.RoundInput;
import com.ontogenval.core.StatementKind;
import com.ontogenval.core.TaskSpec;

import java.util.stream.Collectors;

final class PromptBuilder {
    private PromptBuilder() {
    }

    static String generationPrompt(RoundInput input) {
        // CRITERION statements are for ValAgent only — exclude them from GenAgent's view
        String ontologyForGen = input.ontology().statements().stream()
                .filter(s -> s.kind() != StatementKind.CRITERION)
                .map(s -> "%s: %s %s %s".formatted(s.kind(), s.subject(), s.predicate(), s.object()))
                .collect(Collectors.joining("\n"));
        return """
                You are GenAgent.
                Generate or revise one candidate for this task.

                Round: %d
                Input ontology (facts, concepts and goals only):
                %s

                Previous candidate:
                %s

                Previous validation feedback:
                %s
                """.formatted(
                input.roundIndex(),
                ontologyForGen,
                input.previousCandidate() == null ? "None" : input.previousCandidate().content(),
                input.previousValidation() == null ? "None" : input.previousValidation().feedback()
        );
    }

    static String validationPrompt(TaskSpec task, Candidate candidate) {
        return """
                You are ValAgent.
                VALIDATOR_JSON
                Validate the candidate against the task target and criteria.
                Return exactly one JSON object:
                {
                  "valid": boolean,
                  "score": number from 0 to 1,
                  "gap": number from 0 to 1,
                  "issues": ["actionable issue"],
                  "feedback": "short repair instruction"
                }

                Target: %s
                Criteria: %s

                Candidate:
                %s
                """.formatted(task.target(), task.criteria(), candidate.content());
    }
}
