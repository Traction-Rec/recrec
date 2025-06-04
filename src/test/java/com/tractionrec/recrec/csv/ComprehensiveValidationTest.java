package com.tractionrec.recrec.csv;

import com.tractionrec.recrec.domain.IssueSeverity;
import com.tractionrec.recrec.domain.IssueType;
import com.tractionrec.recrec.domain.ValidationStatus;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ComprehensiveValidationTest {
    
    @Test
    public void testComprehensiveValidation() {
        CsvValidationService service = new CsvValidationService();
        File testFile = new File("test_comprehensive.csv");
        
        if (!testFile.exists()) {
            // Skip test if file doesn't exist
            return;
        }
        
        CsvValidationResult result = service.validateCsv(testFile);
        
        assertNotNull(result);
        assertFalse(result.getIssues().isEmpty());
        
        // Should have errors due to empty fields
        assertEquals(ValidationStatus.ERROR, result.getOverallStatus());
        assertFalse(result.canProceed());
        
        // Count different types of issues
        long scientificNotationIssues = result.getIssues().stream()
            .filter(issue -> issue.getType() == IssueType.SCIENTIFIC_NOTATION)
            .count();
        
        long emptyFieldIssues = result.getIssues().stream()
            .filter(issue -> issue.getType() == IssueType.EMPTY_FIELD)
            .count();
        
        long whitespaceIssues = result.getIssues().stream()
            .filter(issue -> issue.getType() == IssueType.WHITESPACE_ONLY)
            .count();
        
        // Should detect scientific notation (1.23E+15, 7.89e-05, 9.87E+12)
        assertTrue(scientificNotationIssues >= 3, "Should detect at least 3 scientific notation issues");
        
        // Should detect empty fields
        assertTrue(emptyFieldIssues >= 2, "Should detect at least 2 empty field issues");
        
        // Should detect whitespace-only fields
        assertTrue(whitespaceIssues >= 1, "Should detect at least 1 whitespace issue");
        
        // Verify auto-fixable issues
        assertTrue(result.hasAutoFixableIssues());
        
        List<ValidationIssue> autoFixableIssues = result.getAutoFixableIssues();
        assertFalse(autoFixableIssues.isEmpty());
        
        // Scientific notation and whitespace issues should be auto-fixable
        assertTrue(autoFixableIssues.stream().anyMatch(issue -> 
            issue.getType() == IssueType.SCIENTIFIC_NOTATION));
        assertTrue(autoFixableIssues.stream().anyMatch(issue -> 
            issue.getType() == IssueType.WHITESPACE_ONLY));
    }
    
    @Test
    public void testPreviewWithIssues() {
        CsvValidationService service = new CsvValidationService();
        File testFile = new File("test_comprehensive.csv");
        
        if (!testFile.exists()) {
            // Skip test if file doesn't exist
            return;
        }
        
        CsvPreviewData preview = service.generatePreview(testFile, 20);
        
        assertNotNull(preview);
        assertTrue(preview.getPreviewRowCount() > 0);
        assertEquals(2, preview.getColumnCount()); // Merchant, ID
        
        // Should have cell issues marked
        assertFalse(preview.getCellIssues().isEmpty());
        
        // Verify specific cell issues
        boolean hasScientificNotationIssue = preview.getCellIssues().values().stream()
            .anyMatch(issue -> issue.getType() == IssueType.SCIENTIFIC_NOTATION);
        assertTrue(hasScientificNotationIssue);
        
        // Verify headers
        assertNotNull(preview.getHeaders());
        assertEquals("Merchant", preview.getHeaders()[0]);
        assertEquals("ID", preview.getHeaders()[1]);
    }
    
    @Test
    public void testFileStats() {
        CsvValidationService service = new CsvValidationService();
        File testFile = new File("test_comprehensive.csv");
        
        if (!testFile.exists()) {
            // Skip test if file doesn't exist
            return;
        }
        
        CsvValidationResult result = service.validateCsv(testFile);
        CsvFileStats stats = result.getStats();
        
        assertNotNull(stats);
        assertTrue(stats.getTotalRows() > 0);
        assertEquals(2, stats.getTotalColumns());
        assertTrue(stats.hasHeader());
        assertNotNull(stats.getEncoding());
        assertTrue(stats.getFileSizeBytes() > 0);
        
        // Should have some data rows
        assertTrue(stats.getDataRows() > 0);
        
        // File size should be reasonable
        assertTrue(stats.getFileSizeBytes() < 1024); // Small test file
    }
}
