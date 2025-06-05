package com.tractionrec.recrec.csv.rules;

import com.tractionrec.recrec.csv.ValidationIssue;
import com.tractionrec.recrec.domain.CellLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Validation rule for basic content validation (completeness, whitespace issues)
 */
public class ContentValidationRule implements ValidationRule {
    
    private static final int MERCHANT_COLUMN = 0;
    private static final int ID_COLUMN = 1;
    private static final int EXPECTED_COLUMNS = 2;
    
    @Override
    public List<ValidationIssue> validate(String[][] data, String[] headers) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        for (int row = 0; row < data.length; row++) {
            // Check if row has expected number of columns
            if (data[row].length < EXPECTED_COLUMNS) {
                issues.add(new ValidationIssue(
                    com.tractionrec.recrec.domain.IssueSeverity.ERROR,
                    com.tractionrec.recrec.domain.IssueType.CSV_FORMAT,
                    String.format("Row has only %d columns, expected %d", data[row].length, EXPECTED_COLUMNS),
                    new CellLocation(row, 0),
                    "Ensure each row has both Merchant and ID columns",
                    false
                ));
                continue;
            }
            
            // Validate Merchant field
            String merchantValue = data[row][MERCHANT_COLUMN];
            if (merchantValue == null || merchantValue.isEmpty()) {
                issues.add(ValidationIssue.emptyField(new CellLocation(row, MERCHANT_COLUMN), "Merchant"));
            } else if (merchantValue.trim().isEmpty()) {
                issues.add(ValidationIssue.whitespaceOnly(new CellLocation(row, MERCHANT_COLUMN), "Merchant"));
            }
            
            // Validate ID field
            String idValue = data[row][ID_COLUMN];
            if (idValue == null || idValue.isEmpty()) {
                issues.add(ValidationIssue.emptyField(new CellLocation(row, ID_COLUMN), "ID"));
            } else if (idValue.trim().isEmpty()) {
                issues.add(ValidationIssue.whitespaceOnly(new CellLocation(row, ID_COLUMN), "ID"));
            }
        }
        
        return issues;
    }
    
    @Override
    public boolean canAutoFix() {
        return true; // Can fix whitespace issues
    }
    
    @Override
    public String[][] autoFix(String[][] data, String[] headers) {
        String[][] fixedData = new String[data.length][];
        
        for (int row = 0; row < data.length; row++) {
            fixedData[row] = new String[data[row].length];
            
            for (int col = 0; col < data[row].length; col++) {
                String value = data[row][col];
                if (value != null) {
                    // Trim whitespace but preserve the value
                    fixedData[row][col] = value.trim();
                } else {
                    fixedData[row][col] = value;
                }
            }
        }
        
        return fixedData;
    }
    
    @Override
    public String getRuleName() {
        return "Content Validation";
    }
    
    @Override
    public String getDescription() {
        return "Validates field completeness and fixes whitespace issues";
    }
}
