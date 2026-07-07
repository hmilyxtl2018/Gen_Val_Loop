package com.ontogenval.core;

public record Candidate(int roundIndex, String content) {
    public Candidate {
        if (roundIndex < 1) {
            throw new IllegalArgumentException("roundIndex must be >= 1");
        }
        content = content == null ? "" : content.trim();
    }
}
