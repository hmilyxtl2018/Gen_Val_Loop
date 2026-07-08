package com.ontogenval.agent;

import com.ontogenval.core.TaskSpec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Loads {@link TaskSpec} instances from a JSONL file (one JSON object per line).
 * Uses {@link SimpleJsonFields} for parsing — no external JSON library required.
 */
public final class TaskSpecLoader {
    private TaskSpecLoader() {
    }

    public static List<TaskSpec> loadJsonl(Path path) throws IOException {
        List<TaskSpec> tasks = new ArrayList<>();
        for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("//") || trimmed.startsWith("#")) {
                continue;
            }
            tasks.add(parseLine(trimmed));
        }
        return List.copyOf(tasks);
    }

    private static TaskSpec parseLine(String json) {
        Map<String, String> fields = SimpleJsonFields.parse(json);
        return new TaskSpec(
                fields.getOrDefault("taskId", "unknown"),
                fields.getOrDefault("objective", ""),
                fields.getOrDefault("target", ""),
                SimpleJsonFields.parseStringArray(fields.get("facts")),
                SimpleJsonFields.parseStringArray(fields.get("concepts")),
                SimpleJsonFields.parseStringArray(fields.get("goals")),
                SimpleJsonFields.parseStringArray(fields.get("criteria"))
        );
    }
}
