package com.ontogenval.agent;

import com.ontogenval.core.Candidate;
import com.ontogenval.core.RoundInput;
import com.ontogenval.llm.ModelClient;

public final class GenAgent {
    private final ModelClient modelClient;

    public GenAgent(ModelClient modelClient) {
        this.modelClient = modelClient;
    }

    public Candidate generate(RoundInput input) {
        String prompt = PromptBuilder.generationPrompt(input);
        return new Candidate(input.roundIndex(), modelClient.complete(prompt));
    }
}
