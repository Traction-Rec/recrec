package com.tractionrec.recrec.domain;

/**
 * Severity levels for validation issues
 */
public enum IssueSeverity {
    /**
     * Critical error that prevents processing
     */
    ERROR,
    
    /**
     * Warning that should be reviewed but doesn't prevent processing
     */
    WARNING,
    
    /**
     * Informational message about the file
     */
    INFO
}
