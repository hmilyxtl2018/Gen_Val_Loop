package com.ontogenval.loop;

public record LoopConfig(int maxRounds) {
    public LoopConfig {
        if (maxRounds < 1) {
            throw new IllegalArgumentException("maxRounds must be >= 1");
        }
    }
}
