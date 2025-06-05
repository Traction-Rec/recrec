package com.tractionrec.recrec.csv;

import com.tractionrec.recrec.domain.CellLocation;
import com.tractionrec.recrec.domain.IssueSeverity;
import com.tractionrec.recrec.domain.IssueType;

/**
 * Represents a validation issue found in a CSV file
 */
public class ValidationIssue {
    private final IssueSeverity severity;
    private final IssueType type;
    private final String description;
    private final CellLocation location;
    private final String suggestedFix;
    private final boolean autoFixable;
    private final String originalValue;
    private final String suggestedValue;
    
    public ValidationIssue(IssueSeverity severity, IssueType type, String description, 
                          CellLocation location, String suggestedFix, boolean autoFixable) {
        this(severity, type, description, location, suggestedFix, autoFixable, null, null);
    }
    
    public ValidationIssue(IssueSeverity severity, IssueType type, String description, 
                          CellLocation location, String suggestedFix, boolean autoFixable,
                          String originalValue, String suggestedValue) {
        this.severity = severity;
        this.type = type;
        this.description = description;
        this.location = location;
        this.suggestedFix = suggestedFix;
        this.autoFixable = autoFixable;
        this.originalValue = originalValue;
        this.suggestedValue = suggestedValue;
    }
    
    // Getters
    public IssueSeverity getSeverity() { return severity; }
    public IssueType getType() { return type; }
    public String getDescription() { return description; }
    public CellLocation getLocation() { return location; }
    public String getSuggestedFix() { return suggestedFix; }
    public boolean isAutoFixable() { return autoFixable; }
    public String getOriginalValue() { return originalValue; }
    public String getSuggestedValue() { return suggestedValue; }
    
    @Override
    public String toString() {
        return String.format("[%s] %s at %s: %s", 
            severity, type.getDisplayName(), location, description);
    }
    
    /**
     * Create a scientific notation issue
     */
    public static ValidationIssue scientificNotation(CellLocation location, String originalValue, String fixedValue) {
        return new ValidationIssue(
            IssueSeverity.ERROR,
            IssueType.SCIENTIFIC_NOTATION,
            String.format("Scientific notation detected: '%s'", originalValue),
            location,
            String.format("Convert to full number: '%s'", fixedValue),
            true,
            originalValue,
            fixedValue
        );
    }
    
    /**
     * Create an empty field issue
     */
    public static ValidationIssue emptyField(CellLocation location, String fieldName) {
        return new ValidationIssue(
            IssueSeverity.ERROR,
            IssueType.EMPTY_FIELD,
            String.format("Required field '%s' is empty", fieldName),
            location,
            "Provide a value for this field",
            false
        );
    }
    
    /**
     * Create a whitespace-only field issue
     */
    public static ValidationIssue whitespaceOnly(CellLocation location, String fieldName) {
        return new ValidationIssue(
            IssueSeverity.WARNING,
            IssueType.WHITESPACE_ONLY,
            String.format("Field '%s' contains only whitespace", fieldName),
            location,
            "Remove extra whitespace or provide actual content",
            true
        );
    }
    
    /**
     * Create an empty column issue
     */
    public static ValidationIssue emptyColumn(int columnIndex, double emptyPercentage) {
        return new ValidationIssue(
            IssueSeverity.WARNING,
            IssueType.EMPTY_COLUMN,
            String.format("Column %d is %.1f%% empty", columnIndex + 1, emptyPercentage),
            new CellLocation(0, columnIndex),
            "Consider removing this column if it's not needed",
            false
        );
    }
}
