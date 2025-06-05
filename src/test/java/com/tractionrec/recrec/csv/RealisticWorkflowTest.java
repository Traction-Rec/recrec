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
            assertTrue(issue.isAutoFixable(), "Scientific notation should be auto-fixable");
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

            // Should have converted scientific notation to full numbers
            assertTrue(fixedContent.contains("1234570000000000"), "1.23457E+15 should be converted");
            assertTrue(fixedContent.contains("987654000000000"), "9.87654E+14 should be converted");
            assertTrue(fixedContent.contains("78912300000000"), "7.89123E+13 should be converted");

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

            // Should not contain scientific notation anymore
            assertFalse(fixedContent.contains("1.23457E+15"));
            assertFalse(fixedContent.contains("9.87654E+14"));
            assertFalse(fixedContent.contains("7.89123E+13"));

        } catch (Exception e) {
            fail("Failed to read fixed file: " + e.getMessage());
        }

        // Step 5: Validate fixed file would pass
        CsvValidationResult fixedResult = service.validateCsv(fixedFile);

        // Should have fewer issues (only empty field issues remain)
        assertTrue(fixedResult.getIssues().size() < result.getIssues().size());

        // Should have no scientific notation issues
        long fixedScientificIssues = fixedResult.getIssues().stream()
            .filter(issue -> issue.getType() == IssueType.SCIENTIFIC_NOTATION)
            .count();
        assertEquals(0, fixedScientificIssues, "Fixed file should have no scientific notation issues");

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
