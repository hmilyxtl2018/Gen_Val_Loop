package com.ontogenval.agent;

import com.ontogenval.core.Candidate;
import com.ontogenval.core.TaskSpec;
import com.ontogenval.core.ValidationResult;
import com.ontogenval.llm.ModelClient;

import java.util.List;
import java.util.Map;

public final class ValAgent {
    private final ModelClient modelClient;

    public ValAgent(ModelClient modelClient) {
        this.modelClient = modelClient;
    }

    public ValidationResult validate(TaskSpec task, Candidate candidate) {
        String raw = modelClient.complete(PromptBuilder.validationPrompt(task, candidate));
        Map<String, String> fields = SimpleJsonFields.parse(raw);
        return new ValidationResult(
                candidate.roundIndex(),
                Boolean.parseBoolean(fields.getOrDefault("valid", "false")),
                parseDouble(fields.get("score"), 0.0),
                parseDouble(fields.get("gap"), 1.0),
                SimpleJsonFields.parseStringArray(fields.get("issues")),
                fields.getOrDefault("feedback", "")
        );
    }

    private double parseDouble(String value, double fallback) {
        try {
            return value == null ? fallback : Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
