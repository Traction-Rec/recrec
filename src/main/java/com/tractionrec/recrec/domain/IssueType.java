package com.tractionrec.recrec.domain;

/**
 * Types of validation issues that can be detected
 */
public enum IssueType {
    SCIENTIFIC_NOTATION("Scientific Notation", "ID field contains scientific notation format"),
    EMPTY_FIELD("Empty Field", "Required field is empty or contains only whitespace"),
    EMPTY_COLUMN("Empty Column", "Column contains mostly empty values"),
    EMPTY_ROWS("Empty Rows", "File contains excessive empty rows"),
    ENCODING_ISSUE("Encoding Issue", "File contains problematic characters or encoding"),
    CSV_FORMAT("CSV Format", "File has structural CSV formatting issues"),
    MISSING_HEADER("Missing Header", "Expected column headers are missing"),
    DUPLICATE_ROW("Duplicate Row", "Row appears to be duplicated"),
    FILE_TOO_LARGE("File Too Large", "File exceeds recommended size limits"),
    WHITESPACE_ONLY("Whitespace Only", "Field contains only whitespace characters");
    
    private final String displayName;
    private final String description;
    
    IssueType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
