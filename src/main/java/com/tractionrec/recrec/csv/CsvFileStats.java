package com.tractionrec.recrec.csv;

/**
 * Statistics about a CSV file
 */
public class CsvFileStats {
    private final int totalRows;
    private final int totalColumns;
    private final int dataRows; // excluding header
    private final int emptyRows;
    private final long fileSizeBytes;
    private final String encoding;
    private final boolean hasHeader;
    
    public CsvFileStats(int totalRows, int totalColumns, int dataRows, int emptyRows, 
                       long fileSizeBytes, String encoding, boolean hasHeader) {
        this.totalRows = totalRows;
        this.totalColumns = totalColumns;
        this.dataRows = dataRows;
        this.emptyRows = emptyRows;
        this.fileSizeBytes = fileSizeBytes;
        this.encoding = encoding;
        this.hasHeader = hasHeader;
    }
    
    // Getters
    public int getTotalRows() { return totalRows; }
    public int getTotalColumns() { return totalColumns; }
    public int getDataRows() { return dataRows; }
    public int getEmptyRows() { return emptyRows; }
    public long getFileSizeBytes() { return fileSizeBytes; }
    public String getEncoding() { return encoding; }
    public boolean hasHeader() { return hasHeader; }
    
    /**
     * Get file size in human-readable format
     */
    public String getFormattedFileSize() {
        if (fileSizeBytes < 1024) {
            return fileSizeBytes + " B";
        } else if (fileSizeBytes < 1024 * 1024) {
            return String.format("%.1f KB", fileSizeBytes / 1024.0);
        } else {
            return String.format("%.1f MB", fileSizeBytes / (1024.0 * 1024.0));
        }
    }
    
    /**
     * Get percentage of empty rows
     */
    public double getEmptyRowPercentage() {
        return totalRows > 0 ? (emptyRows * 100.0) / totalRows : 0.0;
    }
    
    @Override
    public String toString() {
        return String.format("CSV Stats: %d rows (%d data), %d columns, %s, %s encoding", 
            totalRows, dataRows, totalColumns, getFormattedFileSize(), encoding);
    }
}
