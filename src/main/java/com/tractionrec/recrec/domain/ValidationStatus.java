package com.tractionrec.recrec.domain;

/**
 * Overall validation status for a CSV file
 */
public enum ValidationStatus {
    /**
     * File passed all validation checks
     */
    VALID,
    
    /**
     * File has warnings but can still be processed
     */
    WARNING,
    
    /**
     * File has errors that prevent processing
     */
    ERROR,
    
    /**
     * File could not be read or parsed
     */
    INVALID
}
