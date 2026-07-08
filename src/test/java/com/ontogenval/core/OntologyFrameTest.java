package com.ontogenval.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OntologyFrameTest {

    @Test
    void plus_returnsNewFrame_originalUnchanged() {
        OntologyFrame original = new OntologyFrame("f", List.of(
                new OntologyStatement(StatementKind.FACT, "s", "p", "o")
        ));
        OntologyFrame updated = original.plus(
                new OntologyStatement(StatementKind.CONCEPT, "s2", "p2", "o2")
        );
        assertEquals(1, original.statements().size());
        assertEquals(2, updated.statements().size());
    }

    @Test
    void plus_preservesName() {
        OntologyFrame frame = new OntologyFrame("my-frame", List.of());
        assertEquals("my-frame", frame.plus(new OntologyStatement(StatementKind.FACT, "s", "p", "o")).name());
    }

    @Test
    void render_formatsKindSubjectPredicateObject() {
        OntologyFrame frame = new OntologyFrame("f", List.of(
                new OntologyStatement(StatementKind.FACT, "subject", "predicate", "object")
        ));
        String rendered = frame.render();
        assertTrue(rendered.contains("FACT"));
        assertTrue(rendered.contains("subject"));
        assertTrue(rendered.contains("predicate"));
        assertTrue(rendered.contains("object"));
    }

    @Test
    void render_multipleStatements_joinedByNewline() {
        OntologyFrame frame = new OntologyFrame("f", List.of(
                new OntologyStatement(StatementKind.FACT, "s1", "p1", "o1"),
                new OntologyStatement(StatementKind.CONCEPT, "s2", "p2", "o2")
        ));
        String[] lines = frame.render().split("\n");
        assertEquals(2, lines.length);
    }

    @Test
    void nullStatementsList_treatedAsEmpty() {
        OntologyFrame frame = new OntologyFrame("f", null);
        assertTrue(frame.statements().isEmpty());
    }

    @Test
    void statements_listIsUnmodifiable() {
        OntologyFrame frame = new OntologyFrame("f", List.of(
                new OntologyStatement(StatementKind.FACT, "s", "p", "o")
        ));
        assertThrows(UnsupportedOperationException.class, () ->
                frame.statements().add(new OntologyStatement(StatementKind.FACT, "x", "y", "z")));
    }
}
