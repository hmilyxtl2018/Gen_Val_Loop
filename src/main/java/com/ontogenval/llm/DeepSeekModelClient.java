package com.ontogenval.llm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * ModelClient implementation backed by the DeepSeek Chat API.
 * Uses java.net.HttpURLConnection (synchronous DNS, works where NIO DNS fails).
 *
 * API key must be supplied via constructor — do NOT hard-code keys in source.
 * Recommended: read from environment variable DEEPSEEK_API_KEY.
 */
public final class DeepSeekModelClient implements ModelClient {

    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String MODEL    = "deepseek-chat";
    private static final int    TIMEOUT_MS = 120_000;

    private final String apiKey;

    public DeepSeekModelClient(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("DeepSeek API key must not be blank");
        }
        this.apiKey = apiKey;
    }

    private static final int MAX_RETRIES = 3;

    @Override
    public String complete(String prompt) {
        IOException lastError = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return doComplete(prompt);
            } catch (IOException e) {
                lastError = e;
                System.err.printf("  [DeepSeek retry %d/%d: %s]%n", attempt, MAX_RETRIES, e.getMessage());
                if (attempt < MAX_RETRIES) {
                    try { Thread.sleep(1500L * attempt); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }
        throw new RuntimeException("DeepSeek API call failed after " + MAX_RETRIES + " retries: " + lastError.getMessage(), lastError);
    }

    private String doComplete(String prompt) throws IOException {
        String requestBody = buildRequestBody(prompt);
        byte[] bodyBytes   = requestBody.getBytes(StandardCharsets.UTF_8);

        HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Length", String.valueOf(bodyBytes.length));
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(bodyBytes);
        }

        int status = conn.getResponseCode();
        InputStream is = status == 200 ? conn.getInputStream() : conn.getErrorStream();
        String responseBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        if (status != 200) {
            throw new RuntimeException("DeepSeek API error: HTTP " + status + "\n" + responseBody);
        }
        return extractContent(responseBody);
    }

    // ── request builder ─────────────────────────────────────────────────────

    private static String buildRequestBody(String prompt) {
        String escaped = escapeJson(prompt);
        return """
                {
                  "model": "%s",
                  "messages": [{"role": "user", "content": "%s"}],
                  "temperature": 0.3,
                  "max_tokens": 1024
                }
                """.formatted(MODEL, escaped);
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ── response parser ──────────────────────────────────────────────────────

    /**
     * Extracts the first "content" field value from the DeepSeek response JSON.
     * Avoids adding a JSON library dependency.
     */
    static String extractContent(String responseJson) {
        int idx = responseJson.indexOf("\"content\":");
        if (idx < 0) {
            throw new RuntimeException("No 'content' field in response: "
                    + snippet(responseJson));
        }
        String after = responseJson.substring(idx + "\"content\":".length()).trim();
        if (!after.startsWith("\"")) {
            throw new RuntimeException("Unexpected content value: " + snippet(after));
        }
        StringBuilder sb = new StringBuilder();
        int i = 1;
        while (i < after.length()) {
            char c = after.charAt(i);
            if (c == '\\' && i + 1 < after.length()) {
                char next = after.charAt(i + 1);
                i += 2;
                switch (next) {
                    case 'n'  -> sb.append('\n');
                    case 'r'  -> sb.append('\r');
                    case 't'  -> sb.append('\t');
                    case '"'  -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/'  -> sb.append('/');
                    case 'u'  -> {
                        if (i + 3 < after.length()) {
                            String hex = after.substring(i, i + 4);
                            sb.append((char) Integer.parseInt(hex, 16));
                            i += 4;
                        }
                    }
                    default   -> sb.append(next);
                }
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    private static String snippet(String s) {
        return s == null ? "<null>" : s.substring(0, Math.min(200, s.length()));
    }
}
