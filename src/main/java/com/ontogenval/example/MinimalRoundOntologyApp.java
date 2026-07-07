package com.ontogenval.example;

import com.ontogenval.agent.GenAgent;
import com.ontogenval.agent.ValAgent;
import com.ontogenval.core.RoundTrace;
import com.ontogenval.core.TaskSpec;
import com.ontogenval.llm.MockModelClient;
import com.ontogenval.llm.ModelClient;
import com.ontogenval.loop.ConvergenceRule;
import com.ontogenval.loop.GenValLoop;
import com.ontogenval.loop.LoopConfig;
import com.ontogenval.loop.LoopOutcome;

import java.util.List;

public final class MinimalRoundOntologyApp {
    public static void main(String[] args) {
        ModelClient model = new MockModelClient();
        GenValLoop loop = new GenValLoop(
                new GenAgent(model),
                new ValAgent(model),
                new ConvergenceRule(0.9, 0.1),
                new LoopConfig(3)
        );

        TaskSpec task = new TaskSpec(
                "round-ontology-demo",
                "Generate a candidate and validate it until it converges.",
                "target_keyword=DONE",
                List.of("The loop has one GenAgent and one ValAgent."),
                List.of("Candidate", "Validation", "Convergence"),
                List.of("Reach a valid candidate under the target."),
                List.of("Candidate contains DONE.", "Candidate includes acceptance evidence.")
        );

        LoopOutcome outcome = loop.run(task);
        for (RoundTrace trace : outcome.traces()) {
            System.out.printf(
                    "round=%d valid=%s converged=%s score=%.2f gap=%.2f%n",
                    trace.output().roundIndex(),
                    trace.output().validation().valid(),
                    trace.output().converged(),
                    trace.output().validation().score(),
                    trace.output().validation().gap()
            );
        }
        System.out.println("final_converged=" + outcome.converged());
    }
}
