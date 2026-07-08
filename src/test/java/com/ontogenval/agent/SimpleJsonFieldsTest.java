package com.ontogenval.agent;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SimpleJsonFieldsTest {

    // --- parse ---

    @Test
    void parse_extractsStringField() {
        String json = """
                { "valid": "true", "score": "0.95" }
                """;
        Map<String, String> fields = SimpleJsonFields.parse(json);
        assertEquals("true", fields.get("valid"));
        assertEquals("0.95", fields.get("score"));
    }

    @Test
    void parse_extractsFieldWithoutQuotedValue() {
        String json = """
                { "valid": false, "score": 0.95 }
                """;
        Map<String, String> fields = SimpleJsonFields.parse(json);
        assertEquals("false", fields.get("valid"));
        assertEquals("0.95", fields.get("score"));
    }

    @Test
    void parse_returnsEmptyMapForEmptyInput() {
        Map<String, String> fields = SimpleJsonFields.parse("");
        assertTrue(fields.isEmpty());
    }

    @Test
    void parse_returnsEmptyMapForNull() {
        Map<String, String> fields = SimpleJsonFields.parse(null);
        assertTrue(fields.isEmpty());
    }

    // --- parseStringArray ---

    @Test
    void parseStringArray_parsesNonEmptyArray() {
        List<String> result = SimpleJsonFields.parseStringArray(
                "[\"issue one\", \"issue two\"]");
        assertEquals(2, result.size());
        assertEquals("issue one", result.get(0));
        assertEquals("issue two", result.get(1));
    }

    @Test
    void parseStringArray_parsesEmptyArray() {
        List<String> result = SimpleJsonFields.parseStringArray("[]");
        assertTrue(result.isEmpty());
    }

    @Test
    void parseStringArray_returnsEmptyForNull() {
        List<String> result = SimpleJsonFields.parseStringArray(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void parseStringArray_handlesWhitespace() {
        List<String> result = SimpleJsonFields.parseStringArray(
                "[  \"a\"  ,  \"b\"  ]");
        assertEquals(List.of("a", "b"), result);
    }
}
