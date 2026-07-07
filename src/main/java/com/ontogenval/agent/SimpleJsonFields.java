package com.ontogenval.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class SimpleJsonFields {
    private SimpleJsonFields() {
    }

    static Map<String, String> parse(String raw) {
        Map<String, String> fields = new HashMap<>();
        if (raw == null) {
            return fields;
        }
        String body = raw.trim();
        int start = body.indexOf('{');
        int end = body.lastIndexOf('}');
        if (start >= 0 && end > start) {
            body = body.substring(start + 1, end);
        }
        for (String part : splitTopLevel(body)) {
            int colon = part.indexOf(':');
            if (colon < 0) {
                continue;
            }
            fields.put(unquote(part.substring(0, colon)), unquote(part.substring(colon + 1)));
        }
        return fields;
    }

    static List<String> parseStringArray(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        String body = value.trim();
        if (body.startsWith("[") && body.endsWith("]")) {
            body = body.substring(1, body.length() - 1);
        }
        List<String> values = new ArrayList<>();
        for (String part : splitTopLevel(body)) {
            String item = unquote(part);
            if (!item.isBlank()) {
                values.add(item);
            }
        }
        return List.copyOf(values);
    }

    private static List<String> splitTopLevel(String value) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inString = false;
        int arrayDepth = 0;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '"' && (i == 0 || value.charAt(i - 1) != '\\')) {
                inString = !inString;
            } else if (!inString && ch == '[') {
                arrayDepth++;
            } else if (!inString && ch == ']') {
                arrayDepth--;
            }

            if (!inString && arrayDepth == 0 && ch == ',') {
                parts.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        if (current.length() > 0) {
            parts.add(current.toString());
        }
        return parts;
    }

    private static String unquote(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            return trimmed.substring(1, trimmed.length() - 1).replace("\\\"", "\"");
        }
        return trimmed;
    }
}
