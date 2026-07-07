package com.ontogenval.llm;

public final class MockModelClient implements ModelClient {
    @Override
    public String complete(String prompt) {
        if (prompt.contains("VALIDATOR_JSON")) {
            return validate(prompt);
        }
        return generate(prompt);
    }

    private String generate(String prompt) {
        boolean hasFeedback = !prompt.contains("Previous validation feedback:\nNone");
        StringBuilder candidate = new StringBuilder();
        candidate.append("Answer:\n");
        candidate.append("- Restate the objective.\n");
        candidate.append("- Provide a concrete candidate.\n");
        if (hasFeedback) {
            candidate.append("- Add acceptance evidence.\n");
            candidate.append("- DONE\n");
        }
        return candidate.toString();
    }

    private String validate(String prompt) {
        String candidate = prompt.substring(Math.max(0, prompt.indexOf("Candidate:"))).toLowerCase();
        boolean hasDone = candidate.contains("done");
        boolean hasEvidence = candidate.contains("evidence");
        double score = 0.55;
        if (hasDone) {
            score += 0.30;
        }
        if (hasEvidence) {
            score += 0.15;
        }
        double gap = Math.max(0.0, 1.0 - score);
        boolean valid = score >= 0.9;
        String issues = valid ? "[]" : "[\"Candidate lacks DONE or acceptance evidence.\"]";
        String feedback = valid ? "Accepted." : "Add DONE and acceptance evidence.";
        return """
                {
                  "valid": %s,
                  "score": %.2f,
                  "gap": %.2f,
                  "issues": %s,
                  "feedback": "%s"
                }
                """.formatted(valid, score, gap, issues, feedback);
    }
}
