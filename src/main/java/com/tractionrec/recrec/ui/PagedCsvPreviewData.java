package com.tractionrec.recrec.ui;

import com.tractionrec.recrec.csv.CsvPreviewData;
import com.tractionrec.recrec.csv.ValidationIssue;
import com.tractionrec.recrec.domain.CellLocation;

import java.util.Map;

/**
 * Wrapper for CsvPreviewData that provides paging functionality
 */
public class PagedCsvPreviewData {
    
    private final CsvPreviewData fullData;
    private final int pageSize;
    private int currentPage;
    private final int totalPages;
    
    public PagedCsvPreviewData(CsvPreviewData fullData, int pageSize) {
        this.fullData = fullData;
        this.pageSize = pageSize;
        this.currentPage = 0;
        this.totalPages = (int) Math.ceil((double) fullData.getPreviewRowCount() / pageSize);
    }
    
    /**
     * Get the current page number (0-based)
     */
    public int getCurrentPage() {
        return currentPage;
    }
    
    /**
     * Get the total number of pages
     */
    public int getTotalPages() {
        return totalPages;
    }
    
    /**
     * Get the page size
     */
    public int getPageSize() {
        return pageSize;
    }
    
    /**
     * Navigate to a specific page
     */
    public boolean goToPage(int page) {
        if (page >= 0 && page < totalPages) {
            currentPage = page;
            return true;
        }
        return false;
    }
    
    /**
     * Go to the next page
     */
    public boolean nextPage() {
        return goToPage(currentPage + 1);
    }
    
    /**
     * Go to the previous page
     */
    public boolean previousPage() {
        return goToPage(currentPage - 1);
    }
    
    /**
     * Go to the first page
     */
    public void firstPage() {
        currentPage = 0;
    }
    
    /**
     * Go to the last page
     */
    public void lastPage() {
        currentPage = totalPages - 1;
    }
    
    /**
     * Navigate to the page containing a specific row
     */
    public boolean goToRow(int globalRowIndex) {
        if (globalRowIndex >= 0 && globalRowIndex < fullData.getPreviewRowCount()) {
            int targetPage = globalRowIndex / pageSize;
            return goToPage(targetPage);
        }
        return false;
    }
    
    /**
     * Get the starting row index for the current page (global index)
     */
    public int getCurrentPageStartRow() {
        return currentPage * pageSize;
    }
    
    /**
     * Get the ending row index for the current page (global index, exclusive)
     */
    public int getCurrentPageEndRow() {
        return Math.min((currentPage + 1) * pageSize, fullData.getPreviewRowCount());
    }
    
    /**
     * Get the number of rows in the current page
     */
    public int getCurrentPageRowCount() {
        return getCurrentPageEndRow() - getCurrentPageStartRow();
    }
    
    /**
     * Get data for the current page
     */
    public String[][] getCurrentPageData() {
        int startRow = getCurrentPageStartRow();
        int endRow = getCurrentPageEndRow();
        int pageRows = endRow - startRow;
        
        String[][] pageData = new String[pageRows][];
        String[][] fullDataArray = fullData.getData();
        
        for (int i = 0; i < pageRows; i++) {
            pageData[i] = fullDataArray[startRow + i];
        }
        
        return pageData;
    }
    
    /**
     * Get headers (same for all pages)
     */
    public String[] getHeaders() {
        return fullData.getHeaders();
    }
    
    /**
     * Get cell value for current page (using page-relative coordinates)
     */
    public String getCellValue(int pageRow, int column) {
        int globalRow = getCurrentPageStartRow() + pageRow;
        return fullData.getCellValue(globalRow, column);
    }
    
    /**
     * Check if a cell has validation issues (using page-relative coordinates)
     */
    public boolean hasCellIssue(int pageRow, int column) {
        int globalRow = getCurrentPageStartRow() + pageRow;
        return fullData.hasCellIssue(globalRow, column);
    }
    
    /**
     * Get validation issue for a specific cell (using page-relative coordinates)
     */
    public ValidationIssue getCellIssue(int pageRow, int column) {
        int globalRow = getCurrentPageStartRow() + pageRow;
        return fullData.getCellIssue(globalRow, column);
    }
    
    /**
     * Get the global row index from a page-relative row index
     */
    public int getGlobalRowIndex(int pageRow) {
        return getCurrentPageStartRow() + pageRow;
    }
    
    /**
     * Get the page-relative row index from a global row index
     */
    public int getPageRowIndex(int globalRow) {
        return globalRow - getCurrentPageStartRow();
    }
    
    /**
     * Check if the current page contains a specific global row
     */
    public boolean containsRow(int globalRow) {
        return globalRow >= getCurrentPageStartRow() && globalRow < getCurrentPageEndRow();
    }
    
    /**
     * Get all validation issues (for the issues list)
     */
    public Map<CellLocation, ValidationIssue> getAllCellIssues() {
        return fullData.getCellIssues();
    }
    
    /**
     * Get the underlying full data
     */
    public CsvPreviewData getFullData() {
        return fullData;
    }
    
    /**
     * Get column count
     */
    public int getColumnCount() {
        return fullData.getColumnCount();
    }
    
    /**
     * Get page information string
     */
    public String getPageInfo() {
        if (totalPages <= 1) {
            return String.format("Showing all %d rows", fullData.getPreviewRowCount());
        }
        
        int startRow = getCurrentPageStartRow() + 1; // 1-based for display
        int endRow = getCurrentPageEndRow();
        return String.format("Page %d of %d (rows %d-%d of %d)", 
            currentPage + 1, totalPages, startRow, endRow, fullData.getPreviewRowCount());
    }
}
