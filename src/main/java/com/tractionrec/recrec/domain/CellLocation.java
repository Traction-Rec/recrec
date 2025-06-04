package com.tractionrec.recrec.domain;

/**
 * Represents a location in a CSV file (row, column)
 */
public record CellLocation(int row, int column) {
    
    public CellLocation {
        if (row < 0 || column < 0) {
            throw new IllegalArgumentException("Row and column must be non-negative");
        }
    }
    
    @Override
    public String toString() {
        return String.format("Row %d, Column %d", row + 1, column + 1); // 1-based for user display
    }
    
    /**
     * Create a cell location with 1-based indexing (for user input)
     */
    public static CellLocation fromUserInput(int row, int column) {
        return new CellLocation(row - 1, column - 1);
    }
    
    /**
     * Get 1-based row number for user display
     */
    public int getUserRow() {
        return row + 1;
    }
    
    /**
     * Get 1-based column number for user display
     */
    public int getUserColumn() {
        return column + 1;
    }
}
