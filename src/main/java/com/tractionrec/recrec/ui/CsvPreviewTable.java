package com.tractionrec.recrec.ui;

import com.tractionrec.recrec.csv.CsvPreviewData;
import com.tractionrec.recrec.csv.ValidationIssue;
import com.tractionrec.recrec.domain.CellLocation;
import com.tractionrec.recrec.domain.IssueSeverity;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Custom table for displaying CSV preview with issue highlighting
 */
public class CsvPreviewTable extends JTable {

    private CsvPreviewData previewData;
    private PagedCsvPreviewData pagedData;
    private CellLocation highlightedCell;

    public CsvPreviewTable() {
        super();
        setupTable();
    }

    private void setupTable() {
        // Set up table appearance
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setCellSelectionEnabled(true);

        // Set default cell renderer for issue highlighting
        setDefaultRenderer(Object.class, new CsvCellRenderer());

        // Configure row header (row numbers)
        setRowHeaderTable();

        // Set reasonable column widths
        setRowHeight(20);
    }

    private void setRowHeaderTable() {
        // Create a simple row header showing row numbers
        JList<String> rowHeader = new JList<String>() {
            @Override
            public int getFixedCellHeight() {
                return CsvPreviewTable.this.getRowHeight();
            }

            @Override
            public Dimension getPreferredScrollableViewportSize() {
                Dimension size = super.getPreferredScrollableViewportSize();
                size.width = 50; // Fixed width for row numbers
                return size;
            }
        };

        rowHeader.setFixedCellWidth(50);
        rowHeader.setCellRenderer(new RowHeaderRenderer());
        rowHeader.setBackground(getTableHeader().getBackground());
        rowHeader.setForeground(getTableHeader().getForeground());

        // Update row header when table model changes
        updateRowHeader(rowHeader);
    }

    private void updateRowHeader(JList<String> rowHeader) {
        if (getModel() != null) {
            DefaultListModel<String> listModel = new DefaultListModel<>();
            for (int i = 0; i < getRowCount(); i++) {
                listModel.addElement(String.valueOf(i + 1));
            }
            rowHeader.setModel(listModel);
        }
    }

    public void setPreviewData(CsvPreviewData data) {
        this.previewData = data;

        // Set table model
        setModel(new CsvPreviewTableModel(data));

        // Set column headers
        if (data.getHeaders() != null) {
            JTableHeader header = getTableHeader();
            for (int i = 0; i < data.getHeaders().length && i < getColumnCount(); i++) {
                getColumnModel().getColumn(i).setHeaderValue(data.getHeaders()[i]);
            }
            header.repaint();
        }

        // Set reasonable column widths
        for (int i = 0; i < getColumnCount(); i++) {
            getColumnModel().getColumn(i).setPreferredWidth(120);
        }

        // Update row header if it exists
        Container parent = getParent();
        if (parent instanceof JViewport) {
            JScrollPane scrollPane = (JScrollPane) parent.getParent();
            if (scrollPane.getRowHeader() != null) {
                Component rowHeaderView = scrollPane.getRowHeader().getView();
                if (rowHeaderView instanceof JList) {
                    updateRowHeader((JList<String>) rowHeaderView);
                }
            }
        }

        repaint();
    }

    public void setPagedPreviewData(PagedCsvPreviewData pagedData) {
        this.pagedData = pagedData;
        this.previewData = pagedData.getFullData();

        // Set table model with paged data
        setModel(new PagedCsvPreviewTableModel(pagedData));

        // Set column headers
        if (pagedData.getHeaders() != null) {
            JTableHeader header = getTableHeader();
            for (int i = 0; i < pagedData.getHeaders().length && i < getColumnCount(); i++) {
                getColumnModel().getColumn(i).setHeaderValue(pagedData.getHeaders()[i]);
            }
            header.repaint();
        }

        // Set reasonable column widths
        for (int i = 0; i < getColumnCount(); i++) {
            getColumnModel().getColumn(i).setPreferredWidth(120);
        }

        repaint();
    }

    public void refreshPageData() {
        if (getModel() instanceof PagedCsvPreviewTableModel) {
            ((PagedCsvPreviewTableModel) getModel()).fireTableDataChanged();
        }
        repaint();
    }

    public void highlightCell(CellLocation location) {
        this.highlightedCell = location;

        // Scroll to the cell
        if (location != null) {
            scrollRectToVisible(getCellRect(location.row(), location.column(), true));
            setRowSelectionInterval(location.row(), location.row());
            setColumnSelectionInterval(location.column(), location.column());
        }

        repaint();
    }

    /**
     * Custom table model for CSV preview data
     */
    private static class CsvPreviewTableModel extends AbstractTableModel {
        private final CsvPreviewData data;

        public CsvPreviewTableModel(CsvPreviewData data) {
            this.data = data;
        }

        @Override
        public int getRowCount() {
            return data != null ? data.getPreviewRowCount() : 0;
        }

        @Override
        public int getColumnCount() {
            return data != null ? data.getColumnCount() : 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (data != null) {
                return data.getCellValue(rowIndex, columnIndex);
            }
            return "";
        }

        @Override
        public String getColumnName(int column) {
            if (data != null && data.getHeaders() != null && column < data.getHeaders().length) {
                return data.getHeaders()[column];
            }
            return "Column " + (column + 1);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false; // Preview is read-only
        }
    }

    /**
     * Custom table model for paged CSV preview data
     */
    private static class PagedCsvPreviewTableModel extends AbstractTableModel {
        private final PagedCsvPreviewData pagedData;

        public PagedCsvPreviewTableModel(PagedCsvPreviewData pagedData) {
            this.pagedData = pagedData;
        }

        @Override
        public int getRowCount() {
            return pagedData != null ? pagedData.getCurrentPageRowCount() : 0;
        }

        @Override
        public int getColumnCount() {
            return pagedData != null ? pagedData.getColumnCount() : 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (pagedData != null) {
                return pagedData.getCellValue(rowIndex, columnIndex);
            }
            return "";
        }

        @Override
        public String getColumnName(int column) {
            if (pagedData != null && pagedData.getHeaders() != null && column < pagedData.getHeaders().length) {
                return pagedData.getHeaders()[column];
            }
            return "Column " + (column + 1);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false; // Preview is read-only
        }
    }

    /**
     * Custom cell renderer for highlighting validation issues
     */
    private class CsvCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            Component component = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

            // Reset background color
            Color backgroundColor = Color.WHITE;
            Color foregroundColor = Color.BLACK;

            // Use paged data if available, otherwise fall back to preview data
            if (pagedData != null) {
                CellLocation location = new CellLocation(row, column);

                // Check for validation issues using paged data
                if (pagedData.hasCellIssue(row, column)) {
                    ValidationIssue issue = pagedData.getCellIssue(row, column);
                    if (issue != null) {
                        switch (issue.getSeverity()) {
                            case ERROR:
                                backgroundColor = new Color(255, 200, 200); // Light red
                                break;
                            case WARNING:
                                backgroundColor = new Color(255, 255, 200); // Light yellow
                                break;
                            case INFO:
                                backgroundColor = new Color(200, 220, 255); // Light blue
                                break;
                        }
                    }
                }
            } else if (previewData != null) {
                CellLocation location = new CellLocation(row, column);

                // Check for validation issues using preview data
                if (previewData.hasCellIssue(row, column)) {
                    ValidationIssue issue = previewData.getCellIssue(row, column);
                    if (issue != null) {
                        switch (issue.getSeverity()) {
                            case ERROR:
                                backgroundColor = new Color(255, 200, 200); // Light red
                                break;
                            case WARNING:
                                backgroundColor = new Color(255, 255, 200); // Light yellow
                                break;
                            case INFO:
                                backgroundColor = new Color(200, 220, 255); // Light blue
                                break;
                        }
                    }
                }

            }

            // Highlight selected cell (works for both paged and non-paged)
            CellLocation currentLocation = new CellLocation(row, column);
            if (currentLocation.equals(highlightedCell)) {
                backgroundColor = new Color(100, 150, 255); // Blue highlight
                foregroundColor = Color.WHITE;
            }

            // Apply selection highlighting
            if (isSelected) {
                if (!currentLocation.equals(highlightedCell)) {
                    backgroundColor = table.getSelectionBackground();
                    foregroundColor = table.getSelectionForeground();
                }
            }

            setBackground(backgroundColor);
            setForeground(foregroundColor);

            // Add tooltip for cells with issues
            if (pagedData != null && pagedData.hasCellIssue(row, column)) {
                ValidationIssue issue = pagedData.getCellIssue(row, column);
                setToolTipText(issue.getDescription() + " - " + issue.getSuggestedFix());
            } else if (previewData != null && previewData.hasCellIssue(row, column)) {
                ValidationIssue issue = previewData.getCellIssue(row, column);
                setToolTipText(issue.getDescription() + " - " + issue.getSuggestedFix());
            } else {
                setToolTipText(null);
            }

            return component;
        }
    }

    /**
     * Renderer for row header (row numbers)
     */
    private static class RowHeaderRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {

            Component component = super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);

            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.GRAY));
            setBackground(list.getBackground());
            setForeground(list.getForeground());

            return component;
        }
    }
}
