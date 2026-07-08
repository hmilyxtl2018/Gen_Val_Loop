package com.ontogenval.example;

import com.ontogenval.agent.GenAgent;
import com.ontogenval.agent.TaskSpecLoader;
import com.ontogenval.agent.ValAgent;
import com.ontogenval.core.RoundTrace;
import com.ontogenval.core.TaskSpec;
import com.ontogenval.llm.DeepSeekModelClient;
import com.ontogenval.llm.LoggingModelClient;
import com.ontogenval.llm.MockModelClient;
import com.ontogenval.llm.ModelClient;
import com.ontogenval.loop.ConvergenceRule;
import com.ontogenval.loop.GenValLoop;
import com.ontogenval.loop.LoopConfig;
import com.ontogenval.loop.LoopOutcome;

import java.nio.file.Path;
import java.util.List;

/**
 * Batch runner: loads tasks from a JSONL file and runs GenValLoop for each one.
 *
 * Usage:
 *   java -cp target/classes com.ontogenval.example.GenValLoopRunner <tasks.jsonl> [maxRounds] [--deepseek]
 *
 * LLM selection (checked in order):
 *   1. Pass --deepseek flag  →  reads API key from env DEEPSEEK_API_KEY
 *   2. No flag               →  uses MockModelClient (deterministic, offline)
 *
 * Examples:
 *   # Mock (offline)
 *   java -cp target/classes com.ontogenval.example.GenValLoopRunner \
 *       src/test/resources/mock-tasks/aero_manufacturing_cad_tasks.jsonl 5
 *
 *   # Real DeepSeek
 *   set DEEPSEEK_API_KEY=sk-xxxx
 *   java -cp target/classes com.ontogenval.example.GenValLoopRunner \
 *       src/test/resources/mock-tasks/aero_manufacturing_cad_tasks.jsonl 5 --deepseek
 */
public final class GenValLoopRunner {

    private static final int DEFAULT_MAX_ROUNDS = 5;
    private static final String COL_SEP = " | ";
    private static final int COL_TASK  = 52;
    private static final int COL_ROUND =  6;
    private static final int COL_CONV  = 10;
    private static final int COL_SCORE =  7;
    private static final int COL_GAP   =  7;

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: GenValLoopRunner <tasks.jsonl> [maxRounds] [--candidates N] [--deepseek] [--verbose]");
            System.exit(1);
        }

        Path jsonlPath = Path.of(args[0]);
        int maxRounds = DEFAULT_MAX_ROUNDS;
        int candidatesPerRound = 1;
        boolean useDeepSeek = false;
        boolean verbose = false;
        boolean debug = false;
        for (int i = 1; i < args.length; i++) {
            if ("--deepseek".equalsIgnoreCase(args[i])) {
                useDeepSeek = true;
            } else if ("--verbose".equalsIgnoreCase(args[i]) || "-v".equals(args[i])) {
                verbose = true;
            } else if ("--debug".equalsIgnoreCase(args[i])) {
                debug = true;
            } else if ("--candidates".equalsIgnoreCase(args[i]) || "-c".equals(args[i])) {
                if (i + 1 < args.length) candidatesPerRound = Integer.parseInt(args[++i]);
            } else {
                try { maxRounds = Integer.parseInt(args[i]); } catch (NumberFormatException ignored) {}
            }
        }

        ModelClient model;
        if (useDeepSeek) {
            String apiKey = System.getenv("DEEPSEEK_API_KEY");
            if (apiKey == null || apiKey.isBlank()) {
                System.err.println("ERROR: --deepseek requires env var DEEPSEEK_API_KEY to be set.");
                System.exit(1);
            }
            model = new DeepSeekModelClient(apiKey);
            System.out.println("LLM: DeepSeek (" + apiKey.substring(0, 8) + "***)");
        } else {
            model = new MockModelClient();
            System.out.println("LLM: MockModelClient (offline)");
        }
        if (debug) {
            model = new LoggingModelClient(model);
            System.out.println("     [DEBUG mode ON — raw LLM requests/responses will be printed]");
        }

        List<TaskSpec> tasks = TaskSpecLoader.loadJsonl(jsonlPath);
        System.out.printf("Loaded %d tasks from %s  [maxRounds=%d, candidatesPerRound=%d]%n%n",
                tasks.size(), jsonlPath.getFileName(), maxRounds, candidatesPerRound);

        GenValLoop loop = new GenValLoop(
                new GenAgent(model),
                new ValAgent(model),
                new ConvergenceRule(0.9, 0.1),
                new LoopConfig(maxRounds, candidatesPerRound)
        );

        printHeader();
        int convergedCount = 0;

        for (TaskSpec task : tasks) {
            LoopOutcome outcome = loop.run(task);
            if (outcome.converged()) {
                convergedCount++;
            }
            RoundTrace last = outcome.finalTrace();
            printRow(
                    task.taskId(),
                    outcome.traces().size(),
                    outcome.converged(),
                    last.output().validation().score(),
                    last.output().validation().gap()
            );
            if (verbose) {
                printTraces(task.taskId(), outcome);
            }
        }

        printSeparator();
        System.out.printf("  Converged: %d / %d   (maxRounds=%d)%n",
                convergedCount, tasks.size(), maxRounds);
    }

    // ── formatting helpers ──────────────────────────────────────────────────

    private static void printHeader() {
        printSeparator();
        System.out.printf("%-" + COL_TASK + "s" + COL_SEP +
                        "%" + COL_ROUND + "s" + COL_SEP +
                        "%" + COL_CONV  + "s" + COL_SEP +
                        "%" + COL_SCORE + "s" + COL_SEP +
                        "%" + COL_GAP   + "s%n",
                "taskId", "rounds", "converged", "score", "gap");
        printSeparator();
    }

    private static void printRow(String taskId, int rounds, boolean converged,
                                  double score, double gap) {
        String mark = converged ? "[OK]" : "[--]";
        System.out.printf("%-" + COL_TASK + "s" + COL_SEP +
                        "%" + COL_ROUND + "d" + COL_SEP +
                        "%" + COL_CONV  + "s" + COL_SEP +
                        "%" + COL_SCORE + ".3f" + COL_SEP +
                        "%" + COL_GAP   + ".3f%n",
                truncate(taskId, COL_TASK), rounds, mark, score, gap);
    }

    private static void printSeparator() {
        System.out.println("-".repeat(COL_TASK + COL_ROUND + COL_CONV + COL_SCORE + COL_GAP + COL_SEP.length() * 4));
    }

    private static void printTraces(String taskId, LoopOutcome outcome) {
        String bar = "=".repeat(90);
        System.out.println();
        System.out.println(bar);
        System.out.println("  TASK: " + taskId);
        System.out.println(bar);
        for (RoundTrace trace : outcome.traces()) {
            int r = trace.output().roundIndex();
            boolean valid     = trace.output().validation().valid();
            boolean converged = trace.output().converged();
            double  score     = trace.output().validation().score();
            double  gap       = trace.output().validation().gap();
            String  feedback  = trace.output().validation().feedback();

            List<com.ontogenval.core.CandidateEvaluation> all = trace.output().allEvaluations();
            int n = all.size();

            System.out.printf("%n  ┌─ Round %d  candidates=%d  best: valid=%-5s  converged=%-5s  score=%.3f  gap=%.3f%n",
                    r, n, valid, converged, score, gap);

            // show all candidates when more than one was generated
            if (n > 1) {
                System.out.println("  │");
                System.out.println("  │  [All Candidates This Round]");
                for (int i = 0; i < n; i++) {
                    com.ontogenval.core.CandidateEvaluation ev = all.get(i);
                    boolean isWinner = ev.candidate().equals(trace.output().candidate());
                    String tag = isWinner ? " ★ BEST" : "";
                    System.out.printf("  │  Candidate #%d%s  score=%.3f  valid=%s%n",
                            i + 1, tag, ev.validation().score(), ev.validation().valid());
                    for (String line : ev.candidate().content().split("\n")) {
                        System.out.println("  │    " + line);
                    }
                    System.out.println("  │");
                }
            } else {
                System.out.println("  │");
                System.out.println("  │  [Generated Candidate]");
                for (String line : trace.output().candidate().content().split("\n")) {
                    System.out.println("  │    " + line);
                }
                System.out.println("  │");
            }

            System.out.println("  │  [Validation Feedback]");
            System.out.println("  │    " + feedback);
            if (!trace.output().validation().issues().isEmpty()) {
                System.out.println("  │  [Issues]");
                trace.output().validation().issues()
                        .forEach(issue -> System.out.println("  │    - " + issue));
            }
            System.out.println("  └" + (converged ? "─ CONVERGED" : "─ continue..."));
        }
        System.out.println();
    }

    private static String truncate(String s, int maxLen) {
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 1) + "…";
    }
}
