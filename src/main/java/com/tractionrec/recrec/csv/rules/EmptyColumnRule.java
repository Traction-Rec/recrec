package com.tractionrec.recrec.csv.rules;

import com.tractionrec.recrec.csv.ValidationIssue;

import java.util.ArrayList;
import java.util.List;

/**
 * Validation rule to detect columns with excessive empty values
 */
public class EmptyColumnRule implements ValidationRule {
    
    private static final double EMPTY_THRESHOLD = 0.9; // 90% empty to trigger warning
    
    @Override
    public List<ValidationIssue> validate(String[][] data, String[] headers) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        if (data.length == 0) {
            return issues;
        }
        
        int columnCount = data[0].length;
        
        for (int col = 0; col < columnCount; col++) {
            int emptyCount = 0;
            int totalCount = 0;
            
            for (String[] row : data) {
                if (col < row.length) {
                    totalCount++;
                    String value = row[col];
                    if (value == null || value.trim().isEmpty()) {
                        emptyCount++;
                    }
                }
            }
            
            if (totalCount > 0) {
                double emptyPercentage = (double) emptyCount / totalCount;
                
                if (emptyPercentage >= EMPTY_THRESHOLD) {
                    issues.add(ValidationIssue.emptyColumn(col, emptyPercentage * 100));
                }
            }
        }
        
        return issues;
    }
    
    @Override
    public boolean canAutoFix() {
        return false; // Don't auto-remove columns as it might be intentional
    }
    
    @Override
    public String getRuleName() {
        return "Empty Column Detection";
    }
    
    @Override
    public String getDescription() {
        return "Detects columns that are mostly empty (90% or more)";
    }
}
