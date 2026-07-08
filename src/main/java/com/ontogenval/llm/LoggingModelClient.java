package com.ontogenval.llm;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wraps any ModelClient and prints each request/response pair to stdout.
 * Activate with --debug flag in GenValLoopRunner.
 */
public final class LoggingModelClient implements ModelClient {

    private final ModelClient delegate;
    private final AtomicInteger callIndex = new AtomicInteger(0);

    public LoggingModelClient(ModelClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public String complete(String prompt) {
        int idx = callIndex.incrementAndGet();
        boolean isValidator = prompt.contains("VALIDATOR_JSON");
        String role = isValidator ? "VAL" : "GEN";

        System.out.println();
        System.out.println("  ┄┄┄ [DEBUG " + role + " call #" + idx + " prompt (last 300 chars)] ┄┄┄");
        String trimmedPrompt = prompt.length() > 300
                ? "..." + prompt.substring(prompt.length() - 300)
                : prompt;
        System.out.println(trimmedPrompt);

        String response = delegate.complete(prompt);

        System.out.println("  ┄┄┄ [DEBUG " + role + " call #" + idx + " raw response] ┄┄┄");
        System.out.println(response);
        System.out.println("  ┄┄┄ [END] ┄┄┄");

        return response;
    }
}
