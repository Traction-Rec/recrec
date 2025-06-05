package com.tractionrec.recrec.csv;

/**
 * Options for CSV validation and auto-fix behavior
 */
public class ValidationOptions {
    private boolean autoFixScientificNotation = true;
    private boolean autoFixWhitespace = true;
    private boolean ignoreWarnings = false;
    private boolean removeEmptyColumns = false;
    private boolean removeEmptyRows = false;
    
    // Getters and setters
    public boolean isAutoFixScientificNotation() { return autoFixScientificNotation; }
    public void setAutoFixScientificNotation(boolean autoFixScientificNotation) { 
        this.autoFixScientificNotation = autoFixScientificNotation; 
    }
    
    public boolean isAutoFixWhitespace() { return autoFixWhitespace; }
    public void setAutoFixWhitespace(boolean autoFixWhitespace) { 
        this.autoFixWhitespace = autoFixWhitespace; 
    }
    
    public boolean isIgnoreWarnings() { return ignoreWarnings; }
    public void setIgnoreWarnings(boolean ignoreWarnings) { 
        this.ignoreWarnings = ignoreWarnings; 
    }
    
    public boolean isRemoveEmptyColumns() { return removeEmptyColumns; }
    public void setRemoveEmptyColumns(boolean removeEmptyColumns) { 
        this.removeEmptyColumns = removeEmptyColumns; 
    }
    
    public boolean isRemoveEmptyRows() { return removeEmptyRows; }
    public void setRemoveEmptyRows(boolean removeEmptyRows) { 
        this.removeEmptyRows = removeEmptyRows; 
    }
    
    /**
     * Create default validation options
     */
    public static ValidationOptions defaults() {
        return new ValidationOptions();
    }
    
    /**
     * Create strict validation options (no auto-fix)
     */
    public static ValidationOptions strict() {
        ValidationOptions options = new ValidationOptions();
        options.setAutoFixScientificNotation(false);
        options.setAutoFixWhitespace(false);
        return options;
    }
}
