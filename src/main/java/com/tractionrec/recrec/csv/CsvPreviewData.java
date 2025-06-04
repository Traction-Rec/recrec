package com.tractionrec.recrec.csv;

import com.tractionrec.recrec.domain.CellLocation;

import java.util.Map;

/**
 * Data structure for CSV preview display in the UI
 */
public class CsvPreviewData {
    private final String[][] data;
    private final String[] headers;
    private final Map<CellLocation, ValidationIssue> cellIssues;
    private final CsvFileStats stats;
    private final int totalRowsInFile;
    private final boolean isPreviewTruncated;
    
    public CsvPreviewData(String[][] data, String[] headers, 
                         Map<CellLocation, ValidationIssue> cellIssues, 
                         CsvFileStats stats, int totalRowsInFile) {
        this.data = data;
        this.headers = headers;
        this.cellIssues = cellIssues;
        this.stats = stats;
        this.totalRowsInFile = totalRowsInFile;
        this.isPreviewTruncated = data.length < totalRowsInFile;
    }
    
    // Getters
    public String[][] getData() { return data; }
    public String[] getHeaders() { return headers; }
    public Map<CellLocation, ValidationIssue> getCellIssues() { return cellIssues; }
    public CsvFileStats getStats() { return stats; }
    public int getTotalRowsInFile() { return totalRowsInFile; }
    public boolean isPreviewTruncated() { return isPreviewTruncated; }
    
    /**
     * Get the number of rows in the preview
     */
    public int getPreviewRowCount() {
        return data.length;
    }
    
    /**
     * Get the number of columns in the preview
     */
    public int getColumnCount() {
        return headers != null ? headers.length : (data.length > 0 ? data[0].length : 0);
    }
    
    /**
     * Get cell value safely (returns empty string if out of bounds)
     */
    public String getCellValue(int row, int column) {
        if (row >= 0 && row < data.length && column >= 0 && column < data[row].length) {
            return data[row][column] != null ? data[row][column] : "";
        }
        return "";
    }
    
    /**
     * Check if a cell has validation issues
     */
    public boolean hasCellIssue(int row, int column) {
        return cellIssues.containsKey(new CellLocation(row, column));
    }
    
    /**
     * Get validation issue for a specific cell
     */
    public ValidationIssue getCellIssue(int row, int column) {
        return cellIssues.get(new CellLocation(row, column));
    }
}
