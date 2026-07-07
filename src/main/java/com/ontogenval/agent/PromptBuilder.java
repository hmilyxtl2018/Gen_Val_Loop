package com.ontogenval.agent;

import com.ontogenval.core.Candidate;
import com.ontogenval.core.RoundInput;
import com.ontogenval.core.TaskSpec;

final class PromptBuilder {
    private PromptBuilder() {
    }

    static String generationPrompt(RoundInput input) {
        return """
                You are GenAgent.
                Generate or revise one candidate for this task.

                Round: %d
                Input ontology:
                %s

                Previous candidate:
                %s

                Previous validation feedback:
                %s
                """.formatted(
                input.roundIndex(),
                input.ontology().render(),
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
