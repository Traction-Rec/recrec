package com.tractionrec.recrec.csv;

import com.tractionrec.recrec.csv.rules.ScientificNotationRule;
import com.tractionrec.recrec.domain.IssueType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test scientific notation pattern detection and replacement functionality
 */
public class ScientificNotationReplacementTest {

    @TempDir
    Path tempDir;

    @Test
    public void testPatternAnalysisIncludedInValidationResult() throws IOException {
        // Create test CSV with same scientific notation values
        String csvContent = """
            Merchant,ID
            4.44506E+12,SETUP123456
            4.44506E+12,RECORD789012
            4.44506E+12,VANTIV345678
            """;

        File testFile = tempDir.resolve("test_same_values.csv").toFile();
        Files.writeString(testFile.toPath(), csvContent);

        CsvValidationService service = new CsvValidationService();
        CsvValidationResult result = service.validateCsv(testFile);

        // Should have pattern analysis included
        assertNotNull(result.getPatternAnalysis());

        ScientificNotationRule.ScientificNotationAnalysis analysis = result.getPatternAnalysis();

        // Should detect that all merchant values are the same
        assertTrue(analysis.hasAllSameMerchantValues());
        assertEquals("4.44506E+12", analysis.getCommonMerchantValue());
        assertEquals(3, analysis.getMerchantScientificCount());

        // Should have scientific notation issues
        long scientificIssues = result.getIssues().stream()
            .filter(issue -> issue.getType() == IssueType.SCIENTIFIC_NOTATION)
            .count();
        assertEquals(3, scientificIssues);
    }

    @Test
    public void testScientificNotationReplacement() throws IOException {
        // Create test CSV with same scientific notation values
        String csvContent = """
            Merchant,ID
            4.44506E+12,SETUP123456
            4.44506E+12,RECORD789012
            NormalMerchant,VANTIV345678
            """;

        File testFile = tempDir.resolve("test_replacement.csv").toFile();
        Files.writeString(testFile.toPath(), csvContent);

        CsvValidationService service = new CsvValidationService();

        // Perform replacement
        File replacedFile = service.replaceScientificNotationValue(testFile, "4.44506E+12", "1234567890");

        assertNotNull(replacedFile);
        assertTrue(replacedFile.exists());
        assertTrue(replacedFile.getName().contains("_replaced"));

        // Read replaced content
        String replacedContent = Files.readString(replacedFile.toPath());

        // Should have replaced scientific notation with user value
        assertTrue(replacedContent.contains("1234567890"));
        assertFalse(replacedContent.contains("4.44506E+12"));

        // Should preserve other values
        assertTrue(replacedContent.contains("SETUP123456"));
        assertTrue(replacedContent.contains("RECORD789012"));
        assertTrue(replacedContent.contains("NormalMerchant"));
        assertTrue(replacedContent.contains("VANTIV345678"));
    }

    @Test
    public void testMixedScientificNotationPatterns() throws IOException {
        // Create test CSV with mixed scientific notation values
        String csvContent = """
            Merchant,ID
            4.44506E+12,SETUP123456
            4.44506E+12,RECORD789012
            1.23E+15,VANTIV345678
            """;

        File testFile = tempDir.resolve("test_mixed.csv").toFile();
        Files.writeString(testFile.toPath(), csvContent);

        CsvValidationService service = new CsvValidationService();
        CsvValidationResult result = service.validateCsv(testFile);

        ScientificNotationRule.ScientificNotationAnalysis analysis = result.getPatternAnalysis();

        // Should NOT detect all same values (mixed values)
        assertFalse(analysis.hasAllSameMerchantValues());
        assertNull(analysis.getCommonMerchantValue());
        assertEquals(3, analysis.getMerchantScientificCount());
    }

    @Test
    public void testIdColumnPatternDetection() throws IOException {
        // Create test CSV with same ID scientific notation values
        String csvContent = """
            Merchant,ID
            Merchant1,1.23E+10
            Merchant2,1.23E+10
            Merchant3,1.23E+10
            """;

        File testFile = tempDir.resolve("test_id_pattern.csv").toFile();
        Files.writeString(testFile.toPath(), csvContent);

        CsvValidationService service = new CsvValidationService();
        CsvValidationResult result = service.validateCsv(testFile);

        ScientificNotationRule.ScientificNotationAnalysis analysis = result.getPatternAnalysis();

        // Should detect that all ID values are the same
        assertTrue(analysis.hasAllSameIdValues());
        assertEquals("1.23E+10", analysis.getCommonIdValue());
        assertEquals(3, analysis.getIdScientificCount());

        // Merchant values should not be flagged as same (they're not scientific notation)
        assertFalse(analysis.hasAllSameMerchantValues());
    }

    @Test
    public void testReplacementInBothColumns() throws IOException {
        // Create test CSV with scientific notation in both columns
        String csvContent = """
            Merchant,ID
            4.44506E+12,SETUP123456
            NormalMerchant,4.44506E+12
            """;

        File testFile = tempDir.resolve("test_both_columns.csv").toFile();
        Files.writeString(testFile.toPath(), csvContent);

        CsvValidationService service = new CsvValidationService();

        // Perform replacement
        File replacedFile = service.replaceScientificNotationValue(testFile, "4.44506E+12", "9876543210");

        String replacedContent = Files.readString(replacedFile.toPath());

        // Should replace in both merchant and ID columns
        assertTrue(replacedContent.contains("9876543210"));
        assertFalse(replacedContent.contains("4.44506E+12"));

        // Should preserve other values
        assertTrue(replacedContent.contains("NormalMerchant"));
        assertTrue(replacedContent.contains("SETUP123456"));
    }

    @Test
    public void testWhitespaceHandlingInReplacement() throws IOException {
        // Create test CSV with whitespace around scientific notation values
        String csvContent = """
            Merchant,ID
            " 4.44506E+12 "," 1.23E+10 "
            " 4.44506E+12 ",NormalID
            NormalMerchant," 1.23E+10 "
            """;

        File testFile = tempDir.resolve("test_whitespace.csv").toFile();
        Files.writeString(testFile.toPath(), csvContent);

        CsvValidationService service = new CsvValidationService();
        CsvValidationResult result = service.validateCsv(testFile);

        // Should detect scientific notation despite whitespace
        ScientificNotationRule.ScientificNotationAnalysis analysis = result.getPatternAnalysis();
        assertNotNull(analysis);

        // Should detect that all merchant values are the same (after trimming)
        assertTrue(analysis.hasAllSameMerchantValues());
        assertEquals("4.44506E+12", analysis.getCommonMerchantValue());

        // Now test replacement - this should work with the trimmed value
        File replacedFile = service.replaceScientificNotationValue(testFile, "4.44506E+12", "9876543210");

        assertNotNull(replacedFile);
        assertTrue(replacedFile.exists());

        // Read replaced content
        String replacedContent = Files.readString(replacedFile.toPath());

        // Should have replaced scientific notation with user value
        assertTrue(replacedContent.contains("9876543210"), "Replacement should work even with whitespace");
        assertFalse(replacedContent.contains("4.44506E+12"), "Original scientific notation should be gone");
    }

    @Test
    public void testEdgeCaseWhitespaceHandling() throws IOException {
        // Test various whitespace scenarios
        String csvContent = """
            Merchant,ID
            "4.44506E+12","1.23E+10"
            " 4.44506E+12","1.23E+10 "
            "4.44506E+12 "," 1.23E+10"
            "\t4.44506E+12\t","\t1.23E+10\t"
            """;

        File testFile = tempDir.resolve("test_edge_whitespace.csv").toFile();
        Files.writeString(testFile.toPath(), csvContent);

        CsvValidationService service = new CsvValidationService();

        // Test replacement of merchant values
        File replacedFile = service.replaceScientificNotationValue(testFile, "4.44506E+12", "MERCHANT123");
        String replacedContent = Files.readString(replacedFile.toPath());

        // Should replace all variations of the merchant value
        assertTrue(replacedContent.contains("MERCHANT123"), "Should replace all merchant variations");
        assertFalse(replacedContent.contains("4.44506E+12"), "No merchant scientific notation should remain");

        // ID values should remain unchanged
        assertTrue(replacedContent.contains("1.23E+10"), "ID values should be preserved");

        // Test replacement of ID values
        File replacedFile2 = service.replaceScientificNotationValue(testFile, "1.23E+10", "ID456");
        String replacedContent2 = Files.readString(replacedFile2.toPath());

        // Should replace all variations of the ID value
        assertTrue(replacedContent2.contains("ID456"), "Should replace all ID variations");
        assertFalse(replacedContent2.contains("1.23E+10"), "No ID scientific notation should remain");

        // Merchant values should remain unchanged
        assertTrue(replacedContent2.contains("4.44506E+12"), "Merchant values should be preserved");
    }
}
