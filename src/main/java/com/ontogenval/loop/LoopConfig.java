package com.ontogenval.loop;

/**
 * @param maxRounds          maximum number of Gen-Val rounds before giving up
 * @param candidatesPerRound number of candidates generated in parallel per round;
 *                           the one with the highest validation score is selected
 */
public record LoopConfig(int maxRounds, int candidatesPerRound) {

    /** Convenience constructor: single candidate per round. */
    public LoopConfig(int maxRounds) {
        this(maxRounds, 1);
    }

    public LoopConfig {
        if (maxRounds < 1) {
            throw new IllegalArgumentException("maxRounds must be >= 1");
        }
        if (candidatesPerRound < 1) {
            throw new IllegalArgumentException("candidatesPerRound must be >= 1");
        }
    }
}
