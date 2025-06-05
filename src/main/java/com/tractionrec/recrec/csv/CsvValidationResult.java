package com.tractionrec.recrec.csv;

import com.tractionrec.recrec.domain.ValidationStatus;

import java.util.List;

/**
 * Result of CSV validation containing all issues and overall status
 */
public class CsvValidationResult {
    private final List<ValidationIssue> issues;
    private final CsvFileStats stats;
    private final ValidationStatus overallStatus;
    private final boolean canProceed;
    private final String summaryMessage;
    
    public CsvValidationResult(List<ValidationIssue> issues, CsvFileStats stats, 
                              ValidationStatus overallStatus, boolean canProceed, String summaryMessage) {
        this.issues = issues;
        this.stats = stats;
        this.overallStatus = overallStatus;
        this.canProceed = canProceed;
        this.summaryMessage = summaryMessage;
    }
    
    // Getters
    public List<ValidationIssue> getIssues() { return issues; }
    public CsvFileStats getStats() { return stats; }
    public ValidationStatus getOverallStatus() { return overallStatus; }
    public boolean canProceed() { return canProceed; }
    public String getSummaryMessage() { return summaryMessage; }
    
    /**
     * Get count of issues by severity
     */
    public long getErrorCount() {
        return issues.stream().filter(issue -> issue.getSeverity() == com.tractionrec.recrec.domain.IssueSeverity.ERROR).count();
    }
    
    public long getWarningCount() {
        return issues.stream().filter(issue -> issue.getSeverity() == com.tractionrec.recrec.domain.IssueSeverity.WARNING).count();
    }
    
    public long getInfoCount() {
        return issues.stream().filter(issue -> issue.getSeverity() == com.tractionrec.recrec.domain.IssueSeverity.INFO).count();
    }
    
    /**
     * Check if there are any auto-fixable issues
     */
    public boolean hasAutoFixableIssues() {
        return issues.stream().anyMatch(ValidationIssue::isAutoFixable);
    }
    
    /**
     * Get only auto-fixable issues
     */
    public List<ValidationIssue> getAutoFixableIssues() {
        return issues.stream().filter(ValidationIssue::isAutoFixable).toList();
    }
    
    /**
     * Create a successful validation result
     */
    public static CsvValidationResult success(CsvFileStats stats) {
        return new CsvValidationResult(
            List.of(),
            stats,
            ValidationStatus.VALID,
            true,
            "CSV file validation passed successfully"
        );
    }
    
    /**
     * Create a validation result with warnings
     */
    public static CsvValidationResult withWarnings(List<ValidationIssue> issues, CsvFileStats stats) {
        return new CsvValidationResult(
            issues,
            stats,
            ValidationStatus.WARNING,
            true,
            String.format("CSV file has %d warnings but can be processed", issues.size())
        );
    }
    
    /**
     * Create a validation result with errors
     */
    public static CsvValidationResult withErrors(List<ValidationIssue> issues, CsvFileStats stats) {
        long errorCount = issues.stream().filter(issue -> issue.getSeverity() == com.tractionrec.recrec.domain.IssueSeverity.ERROR).count();
        return new CsvValidationResult(
            issues,
            stats,
            ValidationStatus.ERROR,
            false,
            String.format("CSV file has %d errors that must be fixed before processing", errorCount)
        );
    }
    
    /**
     * Create an invalid file result
     */
    public static CsvValidationResult invalid(String reason) {
        return new CsvValidationResult(
            List.of(),
            null,
            ValidationStatus.INVALID,
            false,
            "CSV file could not be read: " + reason
        );
    }
}
