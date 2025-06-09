package com.tractionrec.recrec.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * A styled table for displaying query statistics
 */
public class StatisticsTable extends JTable {

    private DefaultTableModel tableModel;

    public StatisticsTable() {
        setupTable();
        styleTable();
    }

    private void setupTable() {
        // Create table model with columns
        String[] columnNames = {"Category", "Count", "Percentage"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        setModel(tableModel);

        // Set column widths
        getColumnModel().getColumn(0).setPreferredWidth(150); // Category
        getColumnModel().getColumn(0).setMinWidth(120);
        getColumnModel().getColumn(1).setPreferredWidth(80);  // Count
        getColumnModel().getColumn(1).setMinWidth(60);
        getColumnModel().getColumn(2).setPreferredWidth(100); // Percentage
        getColumnModel().getColumn(2).setMinWidth(80);

        // Make sure table auto-resizes
        setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    }

    private void styleTable() {
        // Basic styling
        setFont(TypographyConstants.FONT_SMALL);
        setRowHeight(20); // Slightly smaller rows to fit more content
        setShowGrid(true);
        setGridColor(new Color(0xE5E7EB));
        setBackground(Color.WHITE);
        setSelectionBackground(new Color(0xDCFDF7));
        setSelectionForeground(TractionRecTheme.TEXT_PRIMARY);

        // Header styling
        getTableHeader().setFont(TypographyConstants.FONT_SMALL);
        getTableHeader().setBackground(new Color(0xF9FAFB));
        getTableHeader().setForeground(TractionRecTheme.TEXT_PRIMARY);
        getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE5E7EB)));

        // Custom cell renderer for colored indicators
        setDefaultRenderer(Object.class, new StatisticsCellRenderer());

        // Initialize with placeholder data
        updateStatistics(0, 0, 0, 0, 0);
    }

    public void updateStatistics(int total, int success, int notFound, int error, int pending) {
        // Clear existing data
        tableModel.setRowCount(0);

        if (total > 0) {
            // Add rows with data
            addStatisticRow("✓ Success", success, total, TractionRecTheme.SUCCESS_GREEN);
            addStatisticRow("⚠ Not Found", notFound, total, TractionRecTheme.WARNING_ORANGE);
            addStatisticRow("✗ Error", error, total, TractionRecTheme.ERROR_RED);
            addStatisticRow("⏳ Pending", pending, total, TractionRecTheme.TEXT_SECONDARY);

            // Add separator row
            tableModel.addRow(new Object[]{"", "", ""});

            // Add total row
            addStatisticRow("📊 Total", total, total, TractionRecTheme.PRIMARY_BLUE);
        } else {
            tableModel.addRow(new Object[]{"Initializing...", "-", "-"});
        }
    }

    private void addStatisticRow(String category, int count, int total, Color color) {
        String percentage = total > 0 ? String.format("%.1f%%", (count * 100.0) / total) : "0%";
        Object[] row = {category, String.valueOf(count), percentage};
        tableModel.addRow(row);
    }



    /**
     * Custom cell renderer for styling different types of statistics
     */
    private static class StatisticsCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Reset styling
            setFont(TypographyConstants.FONT_SMALL);

            if (!isSelected) {
                setBackground(Color.WHITE);
                setForeground(TractionRecTheme.TEXT_PRIMARY);

                // Get the category text to determine styling
                String category = (String) table.getValueAt(row, 0);

                if (category != null) {
                    if (category.startsWith("✓")) {
                        setForeground(TractionRecTheme.SUCCESS_GREEN);
                    } else if (category.startsWith("⚠")) {
                        setForeground(TractionRecTheme.WARNING_ORANGE);
                    } else if (category.startsWith("✗")) {
                        setForeground(TractionRecTheme.ERROR_RED);
                    } else if (category.startsWith("📊")) {
                        setForeground(TractionRecTheme.PRIMARY_BLUE);
                        setFont(TypographyConstants.FONT_BODY_BOLD);
                    }

                    // Empty rows for spacing
                    if (category.isEmpty()) {
                        setBackground(new Color(0xF9FAFB));
                    }
                }
            }

            return c;
        }
    }
}
