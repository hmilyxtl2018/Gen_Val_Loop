package com.ontogenval.llm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MockModelClientTest {

    private final MockModelClient client = new MockModelClient();

    // --- Generation branch ---

    @Test
    void generate_withoutFeedback_doesNotContainDONE() {
        String prompt = "GENERATOR_PROMPT\nPrevious validation feedback:\nNone";
        String result = client.complete(prompt);
        assertFalse(result.toUpperCase().contains("DONE"),
                "first round without feedback should not produce DONE");
    }

    @Test
    void generate_withFeedback_containsDONE() {
        String prompt = "GENERATOR_PROMPT\nPrevious validation feedback:\nAdd DONE and acceptance evidence.";
        String result = client.complete(prompt);
        assertTrue(result.toUpperCase().contains("DONE"),
                "generation with feedback should include DONE");
    }

    @Test
    void generate_withFeedback_containsEvidence() {
        String prompt = "GENERATOR_PROMPT\nPrevious validation feedback:\nAdd DONE and acceptance evidence.";
        String result = client.complete(prompt);
        assertTrue(result.toLowerCase().contains("evidence"),
                "generation with feedback should include evidence");
    }

    // --- Validation branch ---

    @Test
    void validate_withoutDONEAndEvidence_scoreIs055() {
        String prompt = "VALIDATOR_JSON\nCandidate: Just a plain answer.";
        String result = client.complete(prompt);
        assertTrue(result.contains("0.55") || result.contains(".55"),
                "base score without DONE or evidence should be 0.55");
    }

    @Test
    void validate_withDONEOnly_scoreIs085() {
        String prompt = "VALIDATOR_JSON\nCandidate: This is done and complete.";
        String result = client.complete(prompt);
        // score = 0.55 + 0.30 = 0.85
        assertTrue(result.contains("0.85"), "score with DONE only should be 0.85");
    }

    @Test
    void validate_withDONEAndEvidence_scoreIs100_andValid() {
        String prompt = "VALIDATOR_JSON\nCandidate: DONE - acceptance evidence provided.";
        String result = client.complete(prompt);
        // score = 0.55 + 0.30 + 0.15 = 1.00
        assertTrue(result.contains("\"valid\": true"), "should be valid when DONE + evidence present");
    }

    @Test
    void validate_withoutDONE_notValid() {
        String prompt = "VALIDATOR_JSON\nCandidate: Just a plain answer.";
        String result = client.complete(prompt);
        assertTrue(result.contains("\"valid\": false"), "should be invalid without DONE");
    }
}
