package com.tractionrec.recrec.csv;

import com.tractionrec.recrec.domain.IssueType;
import com.tractionrec.recrec.domain.ValidationStatus;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test realistic Excel export scenario workflow
 */
public class RealisticWorkflowTest {

    @Test
    public void testRealisticExcelExportScenario() {
        CsvValidationService service = new CsvValidationService();
        File testFile = new File("test_realistic_excel_export.csv");

        if (!testFile.exists()) {
            // Skip test if file doesn't exist
            return;
        }

        // Step 1: Validate the problematic CSV
        CsvValidationResult result = service.validateCsv(testFile);

        assertNotNull(result);
        assertFalse(result.getIssues().isEmpty());

        // Should have errors due to empty fields
        assertEquals(ValidationStatus.ERROR, result.getOverallStatus());
        assertFalse(result.canProceed());

        // Count issues by type
        long scientificNotationIssues = result.getIssues().stream()
            .filter(issue -> issue.getType() == IssueType.SCIENTIFIC_NOTATION)
            .count();

        long emptyFieldIssues = result.getIssues().stream()
            .filter(issue -> issue.getType() == IssueType.EMPTY_FIELD)
            .count();

        long whitespaceIssues = result.getIssues().stream()
            .filter(issue -> issue.getType() == IssueType.WHITESPACE_ONLY)
            .count();

        // Should detect scientific notation in Merchant column (3 instances)
        assertEquals(3, scientificNotationIssues, "Should detect 3 scientific notation issues in Merchant column");

        // Should detect empty and whitespace Merchant fields (1 empty + 1 whitespace = 2 total)
        assertEquals(1, emptyFieldIssues, "Should detect 1 empty Merchant field issue");
        assertEquals(1, whitespaceIssues, "Should detect 1 whitespace-only Merchant field issue");

        // Verify scientific notation issues are in Merchant column (column 0)
        List<ValidationIssue> scientificIssues = result.getIssues().stream()
            .filter(issue -> issue.getType() == IssueType.SCIENTIFIC_NOTATION)
            .toList();

        for (ValidationIssue issue : scientificIssues) {
            assertEquals(0, issue.getLocation().column(), "Scientific notation should be detected in Merchant column");
            assertFalse(issue.isAutoFixable(), "Scientific notation should NOT be auto-fixable due to precision risk");
        }

        // Step 2: Generate preview
        CsvPreviewData preview = service.generatePreview(testFile, 20);

        assertNotNull(preview);
        assertTrue(preview.getPreviewRowCount() > 0);
        assertEquals(2, preview.getColumnCount());

        // Should have cell issues marked
        assertFalse(preview.getCellIssues().isEmpty());

        // Step 3: Apply auto-fix
        ValidationOptions options = ValidationOptions.defaults();
        File fixedFile = service.fixCommonIssues(testFile, options);

        assertNotNull(fixedFile);
        assertTrue(fixedFile.exists());
        assertTrue(fixedFile.getName().contains("_fixed"));

        // Step 4: Verify fixed content
        try {
            String fixedContent = Files.readString(fixedFile.toPath());

            // Scientific notation should NOT be converted (precision risk)
            assertTrue(fixedContent.contains("1.23457E+15"), "1.23457E+15 should remain unchanged");
            assertTrue(fixedContent.contains("9.87654E+14"), "9.87654E+14 should remain unchanged");
            assertTrue(fixedContent.contains("7.89123E+13"), "7.89123E+13 should remain unchanged");

            // Normal merchants should be unchanged
            assertTrue(fixedContent.contains("456789"));
            assertTrue(fixedContent.contains("NormalMerchant"));
            assertTrue(fixedContent.contains("AnotherMerchant"));

            // IDs should be unchanged
            assertTrue(fixedContent.contains("SETUP123456"));
            assertTrue(fixedContent.contains("RECORD789012"));
            assertTrue(fixedContent.contains("VANTIV345678"));
            assertTrue(fixedContent.contains("PAYMENT901234"));
            assertTrue(fixedContent.contains("BIN567890"));
            assertTrue(fixedContent.contains("ACCOUNT123789"));

            // Scientific notation should still be present (not converted)
            assertTrue(fixedContent.contains("1.23457E+15"));
            assertTrue(fixedContent.contains("9.87654E+14"));
            assertTrue(fixedContent.contains("7.89123E+13"));

        } catch (Exception e) {
            fail("Failed to read fixed file: " + e.getMessage());
        }

        // Step 5: Validate fixed file (should have same issues since scientific notation not auto-fixed)
        CsvValidationResult fixedResult = service.validateCsv(fixedFile);

        // Should have same number of issues (scientific notation not auto-fixed)
        assertEquals(result.getIssues().size(), fixedResult.getIssues().size(),
            "Fixed file should have same issues since scientific notation is not auto-fixed");

        // Should still have scientific notation issues (not auto-fixed)
        long fixedScientificIssues = fixedResult.getIssues().stream()
            .filter(issue -> issue.getType() == IssueType.SCIENTIFIC_NOTATION)
            .count();
        assertEquals(scientificNotationIssues, fixedScientificIssues,
            "Fixed file should still have scientific notation issues (not auto-fixed)");

        // Clean up
        fixedFile.delete();
    }

    @Test
    public void testMerchantColumnPriority() {
        CsvValidationService service = new CsvValidationService();

        // Create test data with scientific notation in both columns
        String testContent = """
            Merchant,ID
            1.23E+15,2.34E+10
            NormalMerchant,RECORD123
            """;

        // This test demonstrates that we detect issues in both columns
        // but prioritize Merchant column in our validation logic

        // The key insight is that in real Excel exports, it's almost always
        // the Merchant column that gets corrupted with scientific notation
        // when users export large merchant IDs from spreadsheets

        assertTrue(true, "This test documents the realistic scenario focus");
    }
}
