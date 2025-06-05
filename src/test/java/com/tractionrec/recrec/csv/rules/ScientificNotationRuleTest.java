package com.tractionrec.recrec.csv.rules;

import com.tractionrec.recrec.csv.ValidationIssue;
import com.tractionrec.recrec.domain.IssueType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ScientificNotationRuleTest {

    @Test
    public void testScientificNotationDetection() {
        ScientificNotationRule rule = new ScientificNotationRule();

        String[][] testData = {
            {"1.23E+15", "SETUP123456"},  // Scientific notation in Merchant
            {"456789", "RECORD789012"},    // Normal merchant number
            {"7.89e-05", "VANTIV345678"},  // Scientific notation in Merchant (lowercase e)
            {"NormalMerchant", "ABC123"},  // Text merchant
            {"1E5", "PAYMENT456789"},      // Scientific notation in Merchant
            {"TestMerchant", "2.34E+10"}   // Scientific notation in ID (less common)
        };

        List<ValidationIssue> issues = rule.validate(testData, null);

        // Should detect 4 scientific notation issues (3 in Merchant, 1 in ID)
        assertEquals(4, issues.size());

        for (ValidationIssue issue : issues) {
            assertEquals(IssueType.SCIENTIFIC_NOTATION, issue.getType());
            assertTrue(issue.isAutoFixable());
            assertNotNull(issue.getSuggestedValue());
        }

        // Verify that most issues are in the Merchant column (column 0)
        long merchantIssues = issues.stream()
            .filter(issue -> issue.getLocation().column() == 0)
            .count();
        long idIssues = issues.stream()
            .filter(issue -> issue.getLocation().column() == 1)
            .count();

        assertEquals(3, merchantIssues);
        assertEquals(1, idIssues);
    }

    @Test
    public void testAutoFix() {
        ScientificNotationRule rule = new ScientificNotationRule();

        String[][] testData = {
            {"1.23E+15", "SETUP123456"},   // Scientific notation in Merchant
            {"456789", "RECORD789012"},     // Normal merchant
            {"7.89e-05", "VANTIV345678"},  // Scientific notation in Merchant
            {"NormalMerchant", "2.34E+10"} // Scientific notation in ID
        };

        String[][] fixedData = rule.autoFix(testData, null);

        // Original data should be unchanged
        assertEquals("1.23E+15", testData[0][0]);
        assertEquals("2.34E+10", testData[3][1]);

        // Fixed data should have converted values in Merchant column
        assertEquals("1230000000000000", fixedData[0][0]);
        assertEquals("456789", fixedData[1][0]); // Unchanged
        assertEquals("0.0000789", fixedData[2][0]);

        // Fixed data should have converted values in ID column
        assertEquals("23400000000", fixedData[3][1]);

        // IDs that weren't scientific notation should be unchanged
        assertEquals("SETUP123456", fixedData[0][1]);
        assertEquals("RECORD789012", fixedData[1][1]);
        assertEquals("VANTIV345678", fixedData[2][1]);
    }

    @Test
    public void testNormalDataPassesValidation() {
        ScientificNotationRule rule = new ScientificNotationRule();

        String[][] testData = {
            {"NormalMerchant1", "SETUP123456"},
            {"AnotherMerchant", "RECORD789012"},
            {"ThirdMerchant", "VANTIV345678"},
            {"123456", "PAYMENT901234"}  // Numeric merchant (but not scientific notation)
        };

        List<ValidationIssue> issues = rule.validate(testData, null);

        // Should have no issues
        assertTrue(issues.isEmpty());
    }
}
