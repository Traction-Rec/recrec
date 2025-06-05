package com.tractionrec.recrec.csv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to reproduce the ArrayIndexOutOfBoundsException bug in CSV header parsing
 * when the header map contains non-contiguous indices.
 */
public class HeaderParsingBugTest {

    @TempDir
    Path tempDir;

    @Test
    public void testEmptyHeaderColumnsNonContiguousIndices() throws IOException {
        // Create CSV with empty header columns that might cause non-contiguous indices
        // This simulates a malformed CSV where some header columns are empty
        String csvContent = """
            Name,,Email,,Phone
            John Doe,,john@example.com,,555-1234
            Jane Smith,,jane@example.com,,555-5678
            """;

        File testFile = tempDir.resolve("test_empty_headers.csv").toFile();
        Files.writeString(testFile.toPath(), csvContent);

        CsvValidationService service = new CsvValidationService();

        // This should trigger the ArrayIndexOutOfBoundsException if the bug exists
        assertDoesNotThrow(() -> {
            CsvValidationResult result = service.validateCsv(testFile);
            assertNotNull(result);
        }, "Should not throw ArrayIndexOutOfBoundsException when parsing CSV with empty headers");
    }

    @Test
    public void testDuplicateHeadersWithDisallowDuplicates() throws IOException {
        // Create CSV with duplicate headers - this might cause Apache Commons CSV
        // to skip some indices when duplicate headers are not allowed
        String csvContent = """
            Name,Email,Name,Phone,Email
            John Doe,john@example.com,John D,555-1234,john.doe@example.com
            Jane Smith,jane@example.com,Jane S,555-5678,jane.smith@example.com
            """;

        File testFile = tempDir.resolve("test_duplicate_headers.csv").toFile();
        Files.writeString(testFile.toPath(), csvContent);

        CsvValidationService service = new CsvValidationService();

        // This should trigger the ArrayIndexOutOfBoundsException if the bug exists
        assertDoesNotThrow(() -> {
            CsvValidationResult result = service.validateCsv(testFile);
            assertNotNull(result);
        }, "Should not throw ArrayIndexOutOfBoundsException when parsing CSV with duplicate headers");
    }

    @Test
    public void testMalformedHeaderRow() throws IOException {
        // Create CSV with inconsistent column counts between header and data rows
        // This might cause the header map to have gaps
        String csvContent = """
            Name,Email
            John Doe,john@example.com,Extra Column,Another Extra
            Jane Smith,jane@example.com,More Data,Even More
            """;

        File testFile = tempDir.resolve("test_malformed_headers.csv").toFile();
        Files.writeString(testFile.toPath(), csvContent);

        CsvValidationService service = new CsvValidationService();

        // This should trigger the ArrayIndexOutOfBoundsException if the bug exists
        assertDoesNotThrow(() -> {
            CsvValidationResult result = service.validateCsv(testFile);
            assertNotNull(result);
        }, "Should not throw ArrayIndexOutOfBoundsException when parsing malformed CSV");
    }

    @Test
    public void testPreviewWithEmptyHeaders() throws IOException {
        // Test the preview functionality which also uses the same parsing logic
        String csvContent = """
            Name,,Email,,Phone
            John Doe,,john@example.com,,555-1234
            Jane Smith,,jane@example.com,,555-5678
            """;

        File testFile = tempDir.resolve("test_preview_empty_headers.csv").toFile();
        Files.writeString(testFile.toPath(), csvContent);

        CsvValidationService service = new CsvValidationService();

        // This should trigger the ArrayIndexOutOfBoundsException if the bug exists
        assertDoesNotThrow(() -> {
            CsvPreviewData preview = service.generatePreview(testFile, 10);
            assertNotNull(preview);
        }, "Should not throw ArrayIndexOutOfBoundsException when generating preview with empty headers");
    }

    @Test
    public void testQuotedEmptyHeaders() throws IOException {
        // Test with quoted empty headers which might be handled differently
        String csvContent = """
            "Name","","Email","","Phone"
            "John Doe","","john@example.com","","555-1234"
            "Jane Smith","","jane@example.com","","555-5678"
            """;

        File testFile = tempDir.resolve("test_quoted_empty_headers.csv").toFile();
        Files.writeString(testFile.toPath(), csvContent);

        CsvValidationService service = new CsvValidationService();

        assertDoesNotThrow(() -> {
            CsvValidationResult result = service.validateCsv(testFile);
            assertNotNull(result);
        }, "Should not throw ArrayIndexOutOfBoundsException when parsing CSV with quoted empty headers");
    }

    @Test
    public void testSpacesOnlyHeaders() throws IOException {
        // Test with headers that contain only spaces
        String csvContent = """
            Name,   ,Email,    ,Phone
            John Doe,   ,john@example.com,    ,555-1234
            Jane Smith,   ,jane@example.com,    ,555-5678
            """;

        File testFile = tempDir.resolve("test_spaces_headers.csv").toFile();
        Files.writeString(testFile.toPath(), csvContent);

        CsvValidationService service = new CsvValidationService();

        assertDoesNotThrow(() -> {
            CsvValidationResult result = service.validateCsv(testFile);
            assertNotNull(result);
        }, "Should not throw ArrayIndexOutOfBoundsException when parsing CSV with space-only headers");
    }

    @Test
    public void testFixWorksWithCsvValidationService() throws IOException {
        // Test that our fix works correctly with the actual CsvValidationService
        // using edge cases that could have caused the original bug

        String csvContent = """
            Name,,Email,,Phone
            John Doe,,john@example.com,,555-1234
            Jane Smith,,jane@example.com,,555-5678
            """;

        File testFile = tempDir.resolve("test_edge_case.csv").toFile();
        Files.writeString(testFile.toPath(), csvContent);

        CsvValidationService service = new CsvValidationService();

        // This should work without throwing ArrayIndexOutOfBoundsException
        assertDoesNotThrow(() -> {
            CsvValidationResult result = service.validateCsv(testFile);
            assertNotNull(result);

            // Verify we can generate preview too
            CsvPreviewData preview = service.generatePreview(testFile, 10);
            assertNotNull(preview);

        }, "CsvValidationService should handle CSV with empty header columns without throwing ArrayIndexOutOfBoundsException");
    }
}
