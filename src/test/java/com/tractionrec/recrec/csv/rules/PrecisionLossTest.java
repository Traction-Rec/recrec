package com.tractionrec.recrec.csv.rules;

import com.tractionrec.recrec.csv.ValidationIssue;
import com.tractionrec.recrec.domain.IssueSeverity;
import com.tractionrec.recrec.domain.IssueType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test conservative scientific notation handling (no auto-fix)
 */
public class PrecisionLossTest {

    @Test
    public void testAllScientificNotationTreatedAsErrors() {
        ScientificNotationRule rule = new ScientificNotationRule();

        // All scientific notation should be treated as errors requiring manual intervention
        String[][] testData = {
            {"4.44506E+12", "SETUP123456"},  // Potentially lossy
            {"1.23E+3", "RECORD789012"},     // Potentially lossy (could have been 1231)
            {"5.0E+2", "VANTIV345678"},     // Potentially lossy (could have been 501)
            {"1.234567890123E+15", "PAYMENT456789"} // Potentially lossy
        };

        List<ValidationIssue> issues = rule.validate(testData, null);

        // Should detect 4 scientific notation issues
        assertEquals(4, issues.size());

        // ALL issues should be errors and NOT auto-fixable
        for (ValidationIssue issue : issues) {
            assertEquals(IssueSeverity.ERROR, issue.getSeverity());
            assertFalse(issue.isAutoFixable(), "No scientific notation should be auto-fixable due to precision risk");
            assertTrue(issue.getDescription().contains("scientific notation"));
            assertTrue(issue.getSuggestedFix().contains("Manual correction required"));
            assertTrue(issue.getSuggestedFix().contains("Format Excel column as 'Text'"));
        }
    }

    @Test
    public void testUserGuidanceProvided() {
        ScientificNotationRule rule = new ScientificNotationRule();

        String[][] testData = {
            {"4.44506E+12", "ID1"},    // Merchant column
            {"NormalMerchant", "1.23E+5"},   // ID column
        };

        List<ValidationIssue> issues = rule.validate(testData, null);

        assertEquals(2, issues.size());

        // Check that helpful guidance is provided
        for (ValidationIssue issue : issues) {
            assertEquals(IssueSeverity.ERROR, issue.getSeverity());
            assertFalse(issue.isAutoFixable());

            String guidance = issue.getSuggestedFix();
            assertTrue(guidance.contains("Manual correction required"));
            assertTrue(guidance.contains("Format Excel column as 'Text'"));
            assertTrue(guidance.contains("apostrophe prefix"));
            assertTrue(guidance.contains("CSV (Comma delimited)"));
        }
    }

    @Test
    public void testPatternAnalysis() {
        ScientificNotationRule rule = new ScientificNotationRule();

        // Test pattern detection for common scenarios
        String[][] testData = {
            {"4.44506E+12", "ID1"},    // Same merchant value
            {"4.44506E+12", "ID2"},    // Same merchant value
            {"4.44506E+12", "ID3"},    // Same merchant value
            {"NormalMerchant", "1.23E+5"},   // Different scenario
        };

        ScientificNotationRule.ScientificNotationAnalysis analysis = rule.analyzePatterns(testData, null);

        // Should detect that all merchant values are the same
        assertTrue(analysis.hasAllSameMerchantValues());
        assertEquals("4.44506E+12", analysis.getCommonMerchantValue());
        assertEquals(3, analysis.getMerchantScientificCount());
        assertEquals(1, analysis.getIdScientificCount());
        assertFalse(analysis.hasAllSameIdValues());
    }

    @Test
    public void testNoAutoFixApplied() {
        ScientificNotationRule rule = new ScientificNotationRule();

        String[][] testData = {
            {"1.0E+3", "SAFE_ID"},        // Even "safe" conversions
            {"4.44506E+12", "LOSSY_ID"},  // Potentially lossy
        };

        // Apply auto-fix (should do nothing)
        String[][] fixedData = rule.autoFix(testData, null);

        // NO conversions should be applied - all data should remain unchanged
        assertEquals("1.0E+3", fixedData[0][0]); // Should remain unchanged
        assertEquals("SAFE_ID", fixedData[0][1]);

        assertEquals("4.44506E+12", fixedData[1][0]); // Should remain unchanged
        assertEquals("LOSSY_ID", fixedData[1][1]);

        // Verify rule reports it cannot auto-fix
        assertFalse(rule.canAutoFix());
    }
}
