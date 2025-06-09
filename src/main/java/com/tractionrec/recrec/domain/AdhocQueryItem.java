package com.tractionrec.recrec.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an ad-hoc query with multiple search criteria.
 * This extends the basic QueryItem concept to support complex searches
 * without requiring CSV input.
 */
public record AdhocQueryItem(
        String merchant,
        LocalDateTime transactionDateTimeBegin,
        LocalDateTime transactionDateTimeEnd,
        String transactionType,
        BigDecimal transactionAmount,
        String approvalNumber
) {
    
    /**
     * API date format: "2019-09-30 16:56:59"
     */
    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Check if this query has a date range specified
     */
    public boolean hasDateRange() {
        return transactionDateTimeBegin != null && transactionDateTimeEnd != null;
    }
    
    /**
     * Check if this query has a transaction type specified
     */
    public boolean hasTransactionType() {
        return transactionType != null && !transactionType.trim().isEmpty();
    }
    
    /**
     * Check if this query has a transaction amount specified
     */
    public boolean hasTransactionAmount() {
        return transactionAmount != null;
    }
    
    /**
     * Check if this query has an approval number specified
     */
    public boolean hasApprovalNumber() {
        return approvalNumber != null && !approvalNumber.trim().isEmpty();
    }
    
    /**
     * Get the begin date formatted for the API
     */
    public String getFormattedDateBegin() {
        return transactionDateTimeBegin != null ? transactionDateTimeBegin.format(API_DATE_FORMAT) : null;
    }
    
    /**
     * Get the end date formatted for the API
     */
    public String getFormattedDateEnd() {
        return transactionDateTimeEnd != null ? transactionDateTimeEnd.format(API_DATE_FORMAT) : null;
    }
    
    /**
     * Get the transaction amount formatted for the API (always with 2 decimal places)
     */
    public String getFormattedAmount() {
        return transactionAmount != null ? String.format("%.2f", transactionAmount) : null;
    }
    
    /**
     * Validate that at least one search criteria is provided
     */
    public boolean hasValidSearchCriteria() {
        return hasDateRange() || hasTransactionType() || hasTransactionAmount() || hasApprovalNumber();
    }
    
    /**
     * Create a basic QueryItem for compatibility with existing systems
     * Uses merchant as both merchant and id fields
     */
    public QueryItem toQueryItem() {
        return new QueryItem(merchant, merchant, QueryBy.ADHOC_SEARCH);
    }
}
