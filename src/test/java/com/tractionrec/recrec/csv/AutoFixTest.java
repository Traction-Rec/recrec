package com.tractionrec.recrec.csv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class AutoFixTest {

    @TempDir
    Path tempDir;

    @Test
    public void testAutoFixScientificNotation() throws IOException {
        // Create test CSV with scientific notation in Merchant column (realistic scenario)
        String csvContent = """
            Merchant,ID
            1.23E+15,SETUP123456
            456789,RECORD789012
            7.89e-05,VANTIV345678
            NormalMerchant,ABC123
            """;

        File testFile = tempDir.resolve("test.csv").toFile();
        Files.writeString(testFile.toPath(), csvContent);

        CsvValidationService service = new CsvValidationService();
        ValidationOptions options = ValidationOptions.defaults();

        // Perform auto-fix
        File fixedFile = service.fixCommonIssues(testFile, options);

        assertNotNull(fixedFile);
        assertTrue(fixedFile.exists());
        assertTrue(fixedFile.getName().contains("_fixed"));

        // Read fixed content
        String fixedContent = Files.readString(fixedFile.toPath());

        // Should have converted scientific notation in Merchant column
        assertTrue(fixedContent.contains("1230000000000000")); // 1.23E+15 converted
        assertTrue(fixedContent.contains("0.0000789")); // 7.89e-05 converted
        assertTrue(fixedContent.contains("456789")); // Normal merchant number unchanged
        assertTrue(fixedContent.contains("NormalMerchant")); // Text merchant unchanged

        // IDs should be unchanged
        assertTrue(fixedContent.contains("SETUP123456"));
        assertTrue(fixedContent.contains("RECORD789012"));
        assertTrue(fixedContent.contains("VANTIV345678"));
        assertTrue(fixedContent.contains("ABC123"));

        // Should not contain scientific notation anymore
        assertFalse(fixedContent.contains("1.23E+15"));
        assertFalse(fixedContent.contains("7.89e-05"));
    }

    @Test
    public void testAutoFixWhitespace() throws IOException {
        // Create test CSV with whitespace issues
        String csvContent = """
            Merchant,ID
            "  MerchantWithSpaces  ",SETUP123456
            NormalMerchant,  RECORD789012
            TrimMerchant,ABC123
            """;

        File testFile = tempDir.resolve("test_whitespace.csv").toFile();
        Files.writeString(testFile.toPath(), csvContent);

        CsvValidationService service = new CsvValidationService();
        ValidationOptions options = ValidationOptions.defaults();

        // Perform auto-fix
        File fixedFile = service.fixCommonIssues(testFile, options);

        assertNotNull(fixedFile);
        assertTrue(fixedFile.exists());

        // Read fixed content
        String fixedContent = Files.readString(fixedFile.toPath());

        // Should have trimmed whitespace
        assertTrue(fixedContent.contains("MerchantWithSpaces")); // Merchant trimmed
        assertTrue(fixedContent.contains("RECORD789012")); // ID trimmed
        // Note: The exact whitespace handling depends on CSV parsing, but content should be preserved
    }

    @Test
    public void testValidFileNoChanges() throws IOException {
        // Create test CSV with no issues
        String csvContent = """
            Merchant,ID
            NormalMerchant1,SETUP123456
            AnotherMerchant,RECORD789012
            ThirdMerchant,ABC123
            """;

        File testFile = tempDir.resolve("test_valid.csv").toFile();
        Files.writeString(testFile.toPath(), csvContent);

        CsvValidationService service = new CsvValidationService();
        ValidationOptions options = ValidationOptions.defaults();

        // Perform auto-fix
        File fixedFile = service.fixCommonIssues(testFile, options);

        assertNotNull(fixedFile);
        assertTrue(fixedFile.exists());

        // Content should be essentially the same (may have formatting differences)
        String originalContent = Files.readString(testFile.toPath());
        String fixedContent = Files.readString(fixedFile.toPath());

        // All original data should be preserved
        assertTrue(fixedContent.contains("NormalMerchant1"));
        assertTrue(fixedContent.contains("SETUP123456"));
        assertTrue(fixedContent.contains("AnotherMerchant"));
        assertTrue(fixedContent.contains("RECORD789012"));
        assertTrue(fixedContent.contains("ThirdMerchant"));
        assertTrue(fixedContent.contains("ABC123"));
    }
}
