package com.littledokyhub;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LegacyAwarePasswordEncoderTests {
    private final LegacyAwarePasswordEncoder encoder = new LegacyAwarePasswordEncoder();

    @Test
    void matchesLegacyPlaintextAndMarksItForUpgrade() {
        assertTrue(encoder.matches("AdminDemo2026!", "AdminDemo2026!"));
        assertFalse(encoder.matches("wrong", "AdminDemo2026!"));
        assertTrue(encoder.upgradeEncoding("AdminDemo2026!"));
    }

    @Test
    void matchesBcryptAndDoesNotMarkItForUpgrade() {
        String encoded = encoder.encode("AdminDemo2026!");

        assertTrue(encoder.matches("AdminDemo2026!", encoded));
        assertFalse(encoder.matches("wrong", encoded));
        assertFalse(encoder.upgradeEncoding(encoded));
    }
}