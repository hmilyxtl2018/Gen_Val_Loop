package com.ontogenval.core;

import com.ontogenval.loop.LoopConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DomainObjectTest {

    // --- Candidate ---

    @Test
    void candidate_roundIndexZeroThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Candidate(0, "content"));
    }

    @Test
    void candidate_negativeRoundIndexThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Candidate(-1, "content"));
    }

    @Test
    void candidate_contentIsTrimmed() {
        Candidate c = new Candidate(1, "  hello  ");
        assertEquals("hello", c.content());
    }

    @Test
    void candidate_nullContentBecomesEmpty() {
        Candidate c = new Candidate(1, null);
        assertEquals("", c.content());
    }

    // --- ValidationResult ---

    @Test
    void validationResult_scoreClampedBelow0() {
        ValidationResult r = new ValidationResult(1, false, -0.5, 0.5, List.of(), "");
        assertEquals(0.0, r.score());
    }

    @Test
    void validationResult_scoreClampedAbove1() {
        ValidationResult r = new ValidationResult(1, true, 1.5, 0.0, List.of(), "");
        assertEquals(1.0, r.score());
    }

    @Test
    void validationResult_gapClampedBelow0() {
        ValidationResult r = new ValidationResult(1, false, 0.5, -0.1, List.of(), "");
        assertEquals(0.0, r.gap());
    }

    @Test
    void validationResult_gapClampedAbove1() {
        ValidationResult r = new ValidationResult(1, false, 0.0, 1.5, List.of(), "");
        assertEquals(1.0, r.gap());
    }

    @Test
    void validationResult_nullIssuesTreatedAsEmpty() {
        ValidationResult r = new ValidationResult(1, false, 0.5, 0.5, null, "");
        assertTrue(r.issues().isEmpty());
    }

    @Test
    void validationResult_issuesListIsUnmodifiable() {
        ValidationResult r = new ValidationResult(1, false, 0.5, 0.5, List.of("issue"), "");
        assertThrows(UnsupportedOperationException.class, () -> r.issues().add("extra"));
    }

    // --- LoopConfig ---

    @Test
    void loopConfig_maxRoundsZeroThrows() {
        assertThrows(IllegalArgumentException.class, () -> new LoopConfig(0));
    }

    @Test
    void loopConfig_negativeThrows() {
        assertThrows(IllegalArgumentException.class, () -> new LoopConfig(-5));
    }

    @Test
    void loopConfig_oneIsValid() {
        assertDoesNotThrow(() -> new LoopConfig(1));
    }

    @Test
    void loopConfig_candidatesPerRoundZeroThrows() {
        assertThrows(IllegalArgumentException.class, () -> new LoopConfig(3, 0));
    }

    @Test
    void loopConfig_candidatesPerRoundThreeIsValid() {
        LoopConfig cfg = new LoopConfig(5, 3);
        assertEquals(5, cfg.maxRounds());
        assertEquals(3, cfg.candidatesPerRound());
    }

    @Test
    void loopConfig_singleArgDefaultsCandidatesToOne() {
        assertEquals(1, new LoopConfig(3).candidatesPerRound());
    }

    // --- OntologyStatement ---

    @Test
    void ontologyStatement_nullKindDefaultsToFact() {
        OntologyStatement s = new OntologyStatement(null, "s", "p", "o");
        assertEquals(StatementKind.FACT, s.kind());
    }

    @Test
    void ontologyStatement_nullFieldsNormalized() {
        OntologyStatement s = new OntologyStatement(StatementKind.FACT, null, null, null);
        assertEquals("", s.subject());
        assertEquals("", s.predicate());
        assertEquals("", s.object());
    }
}
