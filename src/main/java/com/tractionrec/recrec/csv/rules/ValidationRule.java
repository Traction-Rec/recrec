package com.tractionrec.recrec.csv.rules;

import com.tractionrec.recrec.csv.ValidationIssue;

import java.util.List;

/**
 * Interface for CSV validation rules
 */
public interface ValidationRule {
    
    /**
     * Validate the CSV data and return any issues found
     * @param data The CSV data as a 2D array
     * @param headers The column headers (may be null)
     * @return List of validation issues found
     */
    List<ValidationIssue> validate(String[][] data, String[] headers);
    
    /**
     * Check if this rule can automatically fix issues it detects
     * @return true if auto-fix is supported
     */
    boolean canAutoFix();
    
    /**
     * Automatically fix issues in the CSV data
     * @param data The CSV data to fix
     * @param headers The column headers
     * @return The fixed CSV data
     */
    default String[][] autoFix(String[][] data, String[] headers) {
        if (!canAutoFix()) {
            throw new UnsupportedOperationException("Auto-fix not supported by this rule");
        }
        return data; // Default implementation returns unchanged data
    }
    
    /**
     * Get the name of this validation rule
     */
    String getRuleName();
    
    /**
     * Get a description of what this rule validates
     */
    String getDescription();
}
