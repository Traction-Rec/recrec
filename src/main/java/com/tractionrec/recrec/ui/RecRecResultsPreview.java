package com.tractionrec.recrec.ui;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.tractionrec.recrec.RecRecState;
import com.tractionrec.recrec.domain.QueryBy;
import com.tractionrec.recrec.domain.QueryTargetVisitor;
import com.tractionrec.recrec.domain.express.ExpressEntity;
import com.tractionrec.recrec.domain.express.Transaction;
import com.tractionrec.recrec.domain.express.PaymentAccount;
import com.tractionrec.recrec.domain.express.EnhancedBIN;
import com.tractionrec.recrec.domain.output.OutputRow;
import com.tractionrec.recrec.domain.output.TransactionQueryOutputRow;
import com.tractionrec.recrec.domain.output.PaymentAccountQueryOutputRow;
import com.tractionrec.recrec.domain.output.BINQueryOutputRow;
import com.tractionrec.recrec.domain.result.QueryResult;
import com.tractionrec.recrec.domain.result.TransactionQueryResult;
import com.tractionrec.recrec.domain.result.PaymentAccountQueryResult;
import com.tractionrec.recrec.domain.result.BINQueryResult;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * Unified results preview form for displaying transaction query results
 * with pagination and export functionality. Works for both CSV and ad-hoc queries.
 */
public class RecRecResultsPreview extends RecRecForm {

    private JPanel rootPanel;
    private JTable resultsTable;
    private JLabel statsLabel;
    private JLabel pageInfoLabel;
    private JButton firstPageButton;
    private JButton prevPageButton;
    private JButton nextPageButton;
    private JButton lastPageButton;
    private JTextField pageField;
    private JLabel pageLabel;
    private JButton exportButton;
    private JButton backButton;
    private JButton newQueryButton;
    private JPanel warningBar; // Warning bar for result limit
    private JLabel warningLabel;

    // Data management - now polymorphic
    private List<? extends OutputRow> allOutputRows;
    private UniversalResultsTableModel tableModel;
    private int currentPage = 0;
    private static final int ROWS_PER_PAGE = 1000;
    private int totalPages;
    private Class<? extends OutputRow> outputRowClass;

    public RecRecResultsPreview(RecRecState state, NavigationAction navigationAction) {
        super(state, navigationAction);
        setupUI();
        loadResults();
    }

    protected void setupUI() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(
            StyleUtils.SPACING_XXLARGE,
            StyleUtils.SPACING_XXLARGE,
            StyleUtils.SPACING_XXLARGE,
            StyleUtils.SPACING_XXLARGE
        ));
        rootPanel.setBackground(Color.WHITE);
        rootPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Header Section
        JPanel headerSection = createHeaderSection();
        headerSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(headerSection);
        StyleUtils.addVerticalSpacing(rootPanel, StyleUtils.SPACING_LARGE);

        // Results Table Section
        JPanel tableSection = createTableSection();
        tableSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(tableSection);
        StyleUtils.addVerticalSpacing(rootPanel, StyleUtils.SPACING_LARGE);

        // Navigation Section
        JPanel navigationSection = createNavigationSection();
        navigationSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(navigationSection);

        setupEventHandlers();
    }

    private JPanel createHeaderSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(Color.WHITE);

        // Title
        JLabel titleLabel = StyleUtils.createFormTitle("Transaction Query Results");
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(titleLabel);

        // Stats label
        statsLabel = new JLabel("Loading results...");
        statsLabel.setFont(TypographyConstants.FONT_CAPTION);
        statsLabel.setForeground(TractionRecTheme.TEXT_SECONDARY);
        statsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(statsLabel);

        return section;
    }

    private JPanel createTableSection() {
        JPanel section = StyleUtils.createElevatedCard();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

        // Section title - will be updated when data is loaded
        JLabel sectionTitle = StyleUtils.createSectionTitle(StyleUtils.Icons.FILE + "  Query Results");
        section.add(sectionTitle);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_MEDIUM);

        // Warning bar (initially hidden)
        warningBar = createWarningBar();
        section.add(warningBar);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_SMALL);

        // Create results table
        tableModel = new UniversalResultsTableModel();
        resultsTable = new JTable(tableModel);
        setupTable();

        // Create scroll pane
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(StyleUtils.createInputBorder());
        scrollPane.setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(1200, 600));
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(scrollPane);

        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_MEDIUM);

        // Pagination controls
        JPanel paginationPanel = createPaginationPanel();
        paginationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(paginationPanel);

        return section;
    }

    private void setupTable() {
        // Table appearance
        resultsTable.setFont(TypographyConstants.FONT_SMALL);
        resultsTable.setRowHeight(24);
        resultsTable.setGridColor(new Color(0xE5E7EB));
        resultsTable.setBackground(Color.WHITE);
        resultsTable.setSelectionBackground(new Color(0xDCFDF7));
        resultsTable.setSelectionForeground(TractionRecTheme.TEXT_PRIMARY);
        resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set column widths
        setupColumnWidths();

        // Custom cell renderer for better formatting
        resultsTable.setDefaultRenderer(Object.class, new UniversalCellRenderer());
    }

    private void setupColumnWidths() {
        if (resultsTable.getColumnCount() > 0) {
            // Set reasonable column widths based on content
            int[] columnWidths = {
                100, // Transaction ID
                120, // Reference Number
                100, // Transaction Type
                100, // Transaction Amount
                120, // Transaction Date
                100, // Approval Number
                120, // Card Number (masked)
                100, // Response Code
                150, // Response Message
                100, // Merchant ID
                120  // Setup ID
            };

            for (int i = 0; i < Math.min(columnWidths.length, resultsTable.getColumnCount()); i++) {
                resultsTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
            }
        }
    }

    private JPanel createPaginationPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, StyleUtils.SPACING_MEDIUM, StyleUtils.SPACING_MEDIUM));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(1200, 60)); // Increased height to prevent input field cutoff

        // First page button
        firstPageButton = StyleUtils.createIconButton("First", "⏮");
        firstPageButton.setPreferredSize(new Dimension(80, StyleUtils.INPUT_HEIGHT));
        StyleUtils.styleButtonSecondary(firstPageButton);
        panel.add(firstPageButton);

        // Previous page button
        prevPageButton = StyleUtils.createIconButton("Previous", "◀");
        prevPageButton.setPreferredSize(new Dimension(90, StyleUtils.INPUT_HEIGHT));
        StyleUtils.styleButtonSecondary(prevPageButton);
        panel.add(prevPageButton);

        // Page info
        pageInfoLabel = new JLabel("Page 1 of 1");
        pageInfoLabel.setFont(TypographyConstants.FONT_CAPTION);
        pageInfoLabel.setForeground(TractionRecTheme.TEXT_SECONDARY);
        pageInfoLabel.setPreferredSize(new Dimension(150, StyleUtils.INPUT_HEIGHT));
        pageInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(pageInfoLabel);

        // Page field with label
        JPanel pageFieldPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, StyleUtils.SPACING_SMALL));
        pageFieldPanel.setOpaque(false);
        pageFieldPanel.setPreferredSize(new Dimension(120, StyleUtils.INPUT_HEIGHT + 8)); // Ensure adequate height

        pageLabel = new JLabel("Page:");
        pageLabel.setFont(TypographyConstants.FONT_CAPTION);
        pageFieldPanel.add(pageLabel);

        pageField = new JTextField("1");
        pageField.setPreferredSize(new Dimension(60, StyleUtils.INPUT_HEIGHT));
        pageField.setMaximumSize(new Dimension(60, StyleUtils.INPUT_HEIGHT));
        pageField.setMinimumSize(new Dimension(60, StyleUtils.INPUT_HEIGHT)); // Prevent shrinking
        pageField.setHorizontalAlignment(SwingConstants.CENTER);
        StyleUtils.styleTextField(pageField);
        pageFieldPanel.add(pageField);

        panel.add(pageFieldPanel);

        // Next page button
        nextPageButton = StyleUtils.createIconButton("Next", "▶");
        nextPageButton.setPreferredSize(new Dimension(80, StyleUtils.INPUT_HEIGHT));
        StyleUtils.styleButtonSecondary(nextPageButton);
        panel.add(nextPageButton);

        // Last page button
        lastPageButton = StyleUtils.createIconButton("Last", "⏭");
        lastPageButton.setPreferredSize(new Dimension(80, StyleUtils.INPUT_HEIGHT));
        StyleUtils.styleButtonSecondary(lastPageButton);
        panel.add(lastPageButton);

        return panel;
    }

    /**
     * Create a warning bar for result limit notifications
     */
    private JPanel createWarningBar() {
        JPanel warningPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, StyleUtils.SPACING_MEDIUM, StyleUtils.SPACING_SMALL));
        warningPanel.setBackground(new Color(0xFEF3C7)); // Light yellow background
        warningPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xF59E0B), 1), // Orange border
            BorderFactory.createEmptyBorder(StyleUtils.SPACING_SMALL, StyleUtils.SPACING_MEDIUM, StyleUtils.SPACING_SMALL, StyleUtils.SPACING_MEDIUM)
        ));
        warningPanel.setVisible(false); // Initially hidden

        // Set proper alignment and sizing for BoxLayout compatibility
        warningPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        warningPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); // Constrain height, allow full width

        // Warning message (using simple warning symbol without variation selector for better Swing compatibility)
        JLabel warningMessage = new JLabel("⚠ API limit reached (1,000 results). Consider refining your search for complete data.");
        warningMessage.setFont(TypographyConstants.FONT_CAPTION);
        warningMessage.setForeground(new Color(0x92400E)); // Dark yellow text
        warningPanel.add(warningMessage);

        // Add some spacing
        warningPanel.add(Box.createHorizontalStrut(StyleUtils.SPACING_LARGE));

        // Refine search button
        JButton refineButton = new JButton("Refine Search");
        refineButton.setFont(TypographyConstants.FONT_CAPTION);
        refineButton.setBackground(new Color(0xF59E0B)); // Orange background
        refineButton.setForeground(Color.WHITE);
        refineButton.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        refineButton.setFocusPainted(false);
        refineButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refineButton.addActionListener(e -> {
            // Navigate back to the ad-hoc input form
            navigationAction.onBack(); // Back to progress
            navigationAction.onBack(); // Back to input form
        });

        warningPanel.add(refineButton);

        return warningPanel;
    }

    private JPanel createNavigationSection() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        backButton = StyleUtils.createIconButton("Back", StyleUtils.Icons.ARROW_LEFT);
        StyleUtils.styleButtonSecondary(backButton);

        exportButton = StyleUtils.createIconButton("Export Results", StyleUtils.Icons.DOWNLOAD);
        StyleUtils.styleButtonPrimary(exportButton, true);

        newQueryButton = StyleUtils.createIconButton("New Query", StyleUtils.Icons.REFRESH);
        newQueryButton.setVisible(false); // Initially hidden
        StyleUtils.styleButtonPrimary(newQueryButton, true);

        buttonPanel.add(backButton);
        buttonPanel.add(Box.createHorizontalStrut(StyleUtils.SPACING_MEDIUM));
        buttonPanel.add(exportButton);
        buttonPanel.add(Box.createHorizontalStrut(StyleUtils.SPACING_MEDIUM));
        buttonPanel.add(newQueryButton);

        return buttonPanel;
    }

    private void setupEventHandlers() {
        // Pagination event handlers
        firstPageButton.addActionListener(e -> {
            currentPage = 0;
            refreshPage();
        });

        prevPageButton.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                refreshPage();
            }
        });

        nextPageButton.addActionListener(e -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                refreshPage();
            }
        });

        lastPageButton.addActionListener(e -> {
            currentPage = totalPages - 1;
            refreshPage();
        });

        // Page field handler
        pageField.addActionListener(e -> {
            try {
                int page = Integer.parseInt(pageField.getText()) - 1; // Convert to 0-based
                if (page >= 0 && page < totalPages) {
                    currentPage = page;
                    refreshPage();
                } else {
                    pageField.setText(String.valueOf(currentPage + 1)); // Reset to current page
                }
            } catch (NumberFormatException ex) {
                pageField.setText(String.valueOf(currentPage + 1)); // Reset to current page
            }
        });

        // Navigation handlers
        backButton.addActionListener(e -> navigationAction.onBack());
        exportButton.addActionListener(e -> performExport());
        newQueryButton.addActionListener(e -> returnToStart());
    }

    private void loadResults() {
        // Load results from state (works for all query types)
        if (state.queryResults != null && !state.queryResults.isEmpty()) {
            // Get all output rows from all query results
            allOutputRows = state.queryResults.stream()
                .flatMap(result -> result.getOutputRows().stream())
                .toList();

            // Determine the output row class from the first result
            if (!state.queryResults.isEmpty() && state.queryResults.get(0) != null) {
                outputRowClass = state.queryResults.get(0).getOutputRowType();
            }

            updateTableData();
            updateStats(getQueryTypeName(), allOutputRows.size());
        } else {
            allOutputRows = List.of();
            updateTableData();
            updateStats(getQueryTypeName(), 0);
        }
    }

    private String getQueryTypeName() {
        if (state.queryMode == null) {
            return "Query";
        }
        return switch (state.queryMode) {
            case ADHOC_SEARCH -> "Ad-hoc Search";
            case PAYMENT_ACCOUNT -> "Payment Account Query";
            case BIN_QUERY -> "BIN Query";
            case RECORD_ID, VANTIV_ID, SETUP_ID -> "Transaction Query";
            default -> "Query";
        };
    }



    private void updateTableData() {
        if (allOutputRows != null) {
            totalPages = (int) Math.ceil((double) allOutputRows.size() / ROWS_PER_PAGE);
            if (totalPages == 0) totalPages = 1;

            tableModel.setResults(allOutputRows, outputRowClass);
            setupColumnWidths();
            refreshPage();
        }
    }

    private void updateStats(String queryType, int resultCount) {
        String statsText = String.format("%s | Total Results: %d | Pages: %d",
            queryType, resultCount, totalPages);

        // Check for potential result truncation at API limit (only for ad-hoc queries)
        // CSV queries process each row individually, so 1,000 results per row is extremely unlikely
        if (resultCount == 1000 && state.queryMode == QueryBy.ADHOC_SEARCH) {
            warningBar.setVisible(true); // Show warning bar
        } else {
            warningBar.setVisible(false); // Hide warning bar
        }

        statsLabel.setText(statsText);
    }

    private void refreshPage() {
        // Update table model for current page
        tableModel.setCurrentPage(currentPage, ROWS_PER_PAGE);
        tableModel.fireTableDataChanged();

        // Update pagination controls
        updatePaginationControls();

        // Update page info
        updatePageInfo();

        // Scroll to top of current page
        if (resultsTable.getRowCount() > 0) {
            resultsTable.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
        }
    }

    private void updatePaginationControls() {
        // Update button states
        firstPageButton.setEnabled(currentPage > 0);
        prevPageButton.setEnabled(currentPage > 0);
        nextPageButton.setEnabled(currentPage < totalPages - 1);
        lastPageButton.setEnabled(currentPage < totalPages - 1);

        // Update page field
        pageField.setText(String.valueOf(currentPage + 1));
    }

    private void updatePageInfo() {
        if (allOutputRows == null || allOutputRows.isEmpty()) {
            pageInfoLabel.setText("No results to display");
            return;
        }

        if (totalPages <= 1) {
            pageInfoLabel.setText(String.format("Showing all %d results", allOutputRows.size()));
        } else {
            int startRow = currentPage * ROWS_PER_PAGE + 1; // 1-based for display
            int endRow = Math.min((currentPage + 1) * ROWS_PER_PAGE, allOutputRows.size());
            pageInfoLabel.setText(String.format("Page %d of %d (results %d-%d of %d)",
                currentPage + 1, totalPages, startRow, endRow, allOutputRows.size()));
        }
    }

    @Override
    public void willDisplay() {
        // Refresh data when form is displayed
        loadResults();
    }

    @Override
    public void willHide() {
        // Nothing to save
    }

    @Override
    public RecRecForm whatIsNext() {
        // This is typically the final screen in the workflow
        return null;
    }

    @Override
    public JComponent getRootComponent() {
        return rootPanel;
    }

    /**
     * Perform CSV export of the current results
     */
    private void performExport() {
        if (state.queryResults == null || state.queryResults.isEmpty()) {
            JOptionPane.showMessageDialog(rootPanel,
                "No results to export. Please run a query first.",
                "No Results",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create file chooser with CSV filter
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Query Results");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        // Set default filename based on query type
        String defaultFileName = getDefaultExportFileName();
        fileChooser.setSelectedFile(new File(defaultFileName));

        int result = fileChooser.showSaveDialog(rootPanel);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();

        // Ensure .csv extension
        final File exportFile;
        if (!selectedFile.getName().toLowerCase().endsWith(".csv")) {
            exportFile = new File(selectedFile.getAbsolutePath() + ".csv");
        } else {
            exportFile = selectedFile;
        }

        // Disable export button during export
        exportButton.setEnabled(false);
        exportButton.setText("Exporting...");

        // Perform export in background thread
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                exportToCsv(exportFile);
                return null;
            }

            @Override
            protected void done() {
                exportButton.setEnabled(true);
                exportButton.setText("Export Results");

                try {
                    get(); // Check for exceptions

                    // Show the "New Query" button after successful export
                    newQueryButton.setVisible(true);

                    // Show success message with option to start new query
                    int choice = JOptionPane.showOptionDialog(rootPanel,
                        String.format("Results exported successfully to:\n%s\n\nTotal records: %d\n\nWould you like to start a new query?",
                            exportFile.getAbsolutePath(),
                            allOutputRows != null ? allOutputRows.size() : 0),
                        "Export Complete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        new String[]{"New Query", "Stay Here"},
                        "New Query");

                    if (choice == 0) { // User clicked "New Query"
                        returnToStart();
                    }

                } catch (Exception e) {
                    // Show error message
                    JOptionPane.showMessageDialog(rootPanel,
                        "Failed to export results:\n" + e.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Generate default export filename based on query type and timestamp
     */
    private String getDefaultExportFileName() {
        String queryTypeName = getQueryTypeName().toLowerCase().replace(" ", "_");
        String timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_results_%s.csv", queryTypeName, timestamp);
    }

    /**
     * Export results to CSV file using the same pattern as RecRecRunning
     */
    private void exportToCsv(File outputFile) throws IOException {
        try (FileWriter fileWriter = new FileWriter(outputFile)) {
            CsvMapper mapper = new CsvMapper();
            CsvSchema schema = getResultSchema(mapper);
            SequenceWriter sequenceWriter = mapper.writer(schema).writeValues(fileWriter);

            // Export all query results
            for (QueryResult<?, ?> queryResult : state.queryResults) {
                if (queryResult != null) {
                    List<?> rows = queryResult.getOutputRows();
                    sequenceWriter.writeAll(rows);
                }
            }
        }
    }

    /**
     * Get the appropriate CSV schema based on query type
     */
    private CsvSchema getResultSchema(CsvMapper mapper) {
        return state.queryMode.accept(new QueryTargetVisitor<CsvSchema>() {
            @Override
            public CsvSchema visitTransactionQuery() {
                return mapper.schemaFor(TransactionQueryOutputRow.class).withHeader();
            }

            @Override
            public CsvSchema visitPaymentAccountQuery() {
                return mapper.schemaFor(PaymentAccountQueryOutputRow.class).withHeader();
            }

            @Override
            public CsvSchema visitBINQuery() {
                return mapper.schemaFor(BINQueryOutputRow.class).withHeader();
            }
        });
    }

    /**
     * Universal table model that uses reflection to automatically extract column information
     * from OutputRow classes, ensuring it stays in sync with field changes.
     */
    private static class UniversalResultsTableModel extends AbstractTableModel {
        private List<? extends OutputRow> allResults;
        private List<? extends OutputRow> currentPageResults;
        private String[] columnNames;
        private Field[] fields;

        public void setResults(List<? extends OutputRow> results, Class<? extends OutputRow> outputRowClass) {
            this.allResults = results;

            if (outputRowClass != null) {
                this.columnNames = extractColumnNames(outputRowClass);
                this.fields = extractFields(outputRowClass);
            } else {
                this.columnNames = new String[]{"Data"};
                this.fields = new Field[0];
            }

            fireTableStructureChanged();
        }

        /**
         * Extract column names from the OutputRow class using @JsonPropertyOrder annotation
         * or field names as fallback. Only includes columns for fields that actually exist.
         */
        private String[] extractColumnNames(Class<? extends OutputRow> clazz) {
            // First try to get column order from @JsonPropertyOrder annotation
            JsonPropertyOrder propertyOrder = clazz.getAnnotation(JsonPropertyOrder.class);
            if (propertyOrder != null && propertyOrder.value().length > 0) {
                return Arrays.stream(propertyOrder.value())
                    .filter(fieldName -> findField(clazz, fieldName) != null) // Only include existing fields
                    .map(this::formatColumnName)
                    .toArray(String[]::new);
            }

            // Fallback to field names
            return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> !field.getName().startsWith("$")) // Skip synthetic fields
                .map(field -> formatColumnName(field.getName()))
                .toArray(String[]::new);
        }

        /**
         * Extract fields from the OutputRow class in the same order as column names
         * Handles both declared fields and inherited fields from parent classes
         */
        private Field[] extractFields(Class<? extends OutputRow> clazz) {
            JsonPropertyOrder propertyOrder = clazz.getAnnotation(JsonPropertyOrder.class);

            if (propertyOrder != null && propertyOrder.value().length > 0) {
                // Use the order specified in @JsonPropertyOrder
                return Arrays.stream(propertyOrder.value())
                    .map(fieldName -> findField(clazz, fieldName))
                    .filter(field -> field != null)
                    .toArray(Field[]::new);
            }

            // Fallback to all fields (declared + inherited)
            return getAllFields(clazz).stream()
                .filter(field -> !field.getName().startsWith("$")) // Skip synthetic fields
                .toArray(Field[]::new);
        }

        /**
         * Find a field by name, searching both the class and its parent classes
         */
        private Field findField(Class<?> clazz, String fieldName) {
            Class<?> currentClass = clazz;
            while (currentClass != null) {
                try {
                    Field field = currentClass.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return field;
                } catch (NoSuchFieldException e) {
                    // Continue searching in parent class
                    currentClass = currentClass.getSuperclass();
                }
            }
            System.err.println("Warning: Field '" + fieldName + "' not found in " + clazz.getSimpleName() + " or its parent classes");
            return null;
        }

        /**
         * Get all fields from a class and its parent classes
         */
        private List<Field> getAllFields(Class<?> clazz) {
            List<Field> fields = new java.util.ArrayList<>();
            Class<?> currentClass = clazz;

            while (currentClass != null) {
                Field[] declaredFields = currentClass.getDeclaredFields();
                for (Field field : declaredFields) {
                    field.setAccessible(true);
                    fields.add(field);
                }
                currentClass = currentClass.getSuperclass();
            }

            return fields;
        }

        /**
         * Format field names into human-readable column names
         */
        private String formatColumnName(String fieldName) {
            // Convert camelCase to Title Case
            return fieldName.replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("^.", String.valueOf(Character.toUpperCase(fieldName.charAt(0))));
        }

        public void setCurrentPage(int page, int pageSize) {
            if (allResults == null) {
                currentPageResults = List.of();
                return;
            }

            int startIndex = page * pageSize;
            int endIndex = Math.min(startIndex + pageSize, allResults.size());

            if (startIndex < allResults.size()) {
                currentPageResults = allResults.subList(startIndex, endIndex);
            } else {
                currentPageResults = List.of();
            }
        }

        @Override
        public int getRowCount() {
            return currentPageResults != null ? currentPageResults.size() : 0;
        }

        @Override
        public int getColumnCount() {
            return columnNames != null ? columnNames.length : 0;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames != null && column < columnNames.length ? columnNames[column] : "";
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (currentPageResults == null || rowIndex >= currentPageResults.size() ||
                fields == null || columnIndex >= fields.length) {
                return "";
            }

            OutputRow row = currentPageResults.get(rowIndex);
            Field field = fields[columnIndex];

            try {
                Object value = field.get(row);
                return value != null ? value.toString() : "";
            } catch (IllegalAccessException e) {
                System.err.println("Error accessing field '" + field.getName() + "': " + e.getMessage());
                return "";
            }
        }
    }

    /**
     * Universal cell renderer for all data types
     */
    private static class UniversalCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            Component component = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                // Alternate row colors for better readability
                if (row % 2 == 0) {
                    component.setBackground(Color.WHITE);
                } else {
                    component.setBackground(new Color(0xF9FAFB));
                }
            }

            // Set font and alignment
            setFont(TypographyConstants.FONT_SMALL);
            setHorizontalAlignment(SwingConstants.LEFT);

            // Special formatting based on column content
            String columnName = table.getColumnName(column);
            if (columnName.contains("Amount")) {
                setHorizontalAlignment(SwingConstants.RIGHT);
            } else if (columnName.contains("Card Number") || columnName.contains("Account ID")) {
                setHorizontalAlignment(SwingConstants.CENTER);
            }

            return component;
        }
    }

    /**
     * Return to the start screen for a new query
     */
    private void returnToStart() {
        // Reset the application state for a fresh start
        state.reset();

        // Navigate back to the start screen using the clean backToStart method
        navigationAction.backToStart();
    }
}
