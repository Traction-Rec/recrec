package com.tractionrec.recrec.csv;

import com.tractionrec.recrec.domain.IssueSeverity;
import com.tractionrec.recrec.domain.IssueType;
import com.tractionrec.recrec.domain.ValidationStatus;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CsvValidationServiceTest {
    
    @Test
    public void testScientificNotationDetection() {
        CsvValidationService service = new CsvValidationService();
        File testFile = new File("test_scientific_notation.csv");
        
        if (!testFile.exists()) {
            // Skip test if file doesn't exist
            return;
        }
        
        CsvValidationResult result = service.validateCsv(testFile);
        
        assertNotNull(result);
        assertFalse(result.getIssues().isEmpty());
        
        // Should detect scientific notation issues
        List<ValidationIssue> scientificNotationIssues = result.getIssues().stream()
            .filter(issue -> issue.getType() == IssueType.SCIENTIFIC_NOTATION)
            .toList();
        
        assertFalse(scientificNotationIssues.isEmpty());
        
        // Should detect empty field issues
        List<ValidationIssue> emptyFieldIssues = result.getIssues().stream()
            .filter(issue -> issue.getType() == IssueType.EMPTY_FIELD)
            .toList();
        
        assertFalse(emptyFieldIssues.isEmpty());
        
        // Should have errors due to empty fields
        assertEquals(ValidationStatus.ERROR, result.getOverallStatus());
        assertFalse(result.canProceed());
    }
    
    @Test
    public void testPreviewGeneration() {
        CsvValidationService service = new CsvValidationService();
        File testFile = new File("test_scientific_notation.csv");
        
        if (!testFile.exists()) {
            // Skip test if file doesn't exist
            return;
        }
        
        CsvPreviewData preview = service.generatePreview(testFile, 10);
        
        assertNotNull(preview);
        assertTrue(preview.getPreviewRowCount() > 0);
        assertTrue(preview.getColumnCount() >= 2);
        
        // Should have cell issues marked
        assertFalse(preview.getCellIssues().isEmpty());
    }
}
