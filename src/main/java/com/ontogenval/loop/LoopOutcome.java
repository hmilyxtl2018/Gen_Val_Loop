package com.ontogenval.loop;

import com.ontogenval.core.RoundTrace;

import java.util.List;

public record LoopOutcome(boolean converged, RoundTrace finalTrace, List<RoundTrace> traces) {
    public LoopOutcome {
        traces = List.copyOf(traces == null ? List.of() : traces);
    }
}
