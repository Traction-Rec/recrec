package com.tractionrec.recrec.ui;

import com.tractionrec.recrec.csv.CsvPreviewData;
import com.tractionrec.recrec.csv.ValidationIssue;
import com.tractionrec.recrec.domain.CellLocation;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Full-screen CSV preview window with spreadsheet-like interface
 */
public class CsvPreviewWindow extends JDialog {

    private CsvPreviewTable previewTable;
    private JList<ValidationIssue> issueList;
    private JLabel statsLabel;
    private JLabel issueCountLabel;
    private JLabel issuesSectionTitle;
    private JLabel pageInfoLabel;
    private JButton firstPageButton;
    private JButton prevPageButton;
    private JButton nextPageButton;
    private JButton lastPageButton;
    private JTextField pageField;
    private JLabel pageLabel;
    private CsvPreviewData previewData;
    private PagedCsvPreviewData pagedData;

    private static final int ROWS_PER_PAGE = 1000;

    public CsvPreviewWindow(Window parent, CsvPreviewData previewData) {
        super(parent, "CSV Preview - " + (previewData.getStats() != null ? previewData.getStats().getFormattedFileSize() : ""), ModalityType.MODELESS);
        this.previewData = previewData;
        this.pagedData = new PagedCsvPreviewData(previewData, ROWS_PER_PAGE);

        setupUI();
        setupEventHandlers();
        loadData();

        // Set to large size with modern proportions
        setSize(1400, 900); // Larger for better data viewing
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Apply modern window styling
        setBackground(Color.WHITE);

        // Make resizable for user preference
        setResizable(true);

        // Center on screen
        setLocationRelativeTo(parent);
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        // Main content panel with modern styling
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(
            StyleUtils.SPACING_LARGE,
            StyleUtils.SPACING_LARGE,
            StyleUtils.SPACING_LARGE,
            StyleUtils.SPACING_LARGE
        ));

        // Header section
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Content section with split pane
        JPanel contentPanel = createContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Footer section
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        // Title section
        JLabel titleLabel = StyleUtils.createFormTitle("CSV Data Preview");
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);

        StyleUtils.addVerticalSpacing(panel, StyleUtils.SPACING_SMALL);

        // Stats and instructions row
        JPanel infoRow = new JPanel(new BorderLayout());
        infoRow.setBackground(Color.WHITE);
        infoRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Stats label
        statsLabel = new JLabel();
        statsLabel.setFont(TypographyConstants.FONT_BODY_BOLD);
        statsLabel.setForeground(TractionRecTheme.PRIMARY_BLUE);
        infoRow.add(statsLabel, BorderLayout.WEST);

        // Instructions
        JLabel instructions = new JLabel("<html><div style='color: #6B7280; font-style: italic;'>Click on issues in the right panel to navigate to problematic cells. Press ESC to close.</div></html>");
        instructions.setFont(TypographyConstants.FONT_SMALL);
        infoRow.add(instructions, BorderLayout.EAST);

        panel.add(infoRow);
        StyleUtils.addVerticalSpacing(panel, StyleUtils.SPACING_LARGE);

        return panel;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Create modern split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.75); // Give more space to preview in full screen
        splitPane.setDividerLocation(0.75);
        splitPane.setBorder(null); // Remove default border
        splitPane.setBackground(Color.WHITE);

        // Style the divider
        splitPane.setDividerSize(8);
        splitPane.setContinuousLayout(true);

        // Preview panel
        JPanel previewPanel = createPreviewSection();
        splitPane.setLeftComponent(previewPanel);

        // Issues panel
        JPanel issuesPanel = createIssuesSection();
        splitPane.setRightComponent(issuesPanel);

        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPreviewSection() {
        JPanel section = StyleUtils.createElevatedCard();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

        // Section title
        JLabel sectionTitle = StyleUtils.createSectionTitle(StyleUtils.Icons.FILE + "  CSV Data");
        section.add(sectionTitle);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_MEDIUM);

        // Create enhanced preview table
        previewTable = new CsvPreviewTable();

        // Style the table
        previewTable.setFont(TypographyConstants.FONT_SMALL);
        previewTable.setRowHeight(22);
        previewTable.setGridColor(new Color(0xE5E7EB));
        previewTable.setBackground(Color.WHITE);
        previewTable.setSelectionBackground(new Color(0xDCFDF7));
        previewTable.setSelectionForeground(TractionRecTheme.TEXT_PRIMARY);

        // Create scroll pane with modern styling
        JScrollPane scrollPane = new JScrollPane(previewTable);
        scrollPane.setRowHeaderView(createRowHeader());
        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, createCornerComponent());
        scrollPane.setBorder(StyleUtils.createInputBorder());
        scrollPane.setBackground(Color.WHITE);

        // Set preferred size for larger window
        scrollPane.setPreferredSize(new Dimension(1000, 700));
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(scrollPane);

        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_MEDIUM);

        // Pagination controls
        JPanel paginationPanel = createPaginationPanel();
        paginationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(paginationPanel);

        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_SMALL);

        // Page info label
        pageInfoLabel = new JLabel();
        pageInfoLabel.setFont(TypographyConstants.FONT_SMALL);
        pageInfoLabel.setForeground(TractionRecTheme.TEXT_SECONDARY);
        pageInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(pageInfoLabel);

        return section;
    }

    private JPanel createPaginationPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setOpaque(false);

        // First page button
        firstPageButton = StyleUtils.createIconButton("First", "⏮"); // ⏮
        StyleUtils.styleButtonSecondary(firstPageButton);
        panel.add(firstPageButton);

        StyleUtils.addHorizontalSpacing(panel, StyleUtils.SPACING_SMALL);

        // Previous page button
        prevPageButton = StyleUtils.createIconButton("Previous", "◀"); // ◀
        StyleUtils.styleButtonSecondary(prevPageButton);
        panel.add(prevPageButton);

        StyleUtils.addHorizontalSpacing(panel, StyleUtils.SPACING_MEDIUM);

        // Page input
        pageLabel = new JLabel("Page:");
        pageLabel.setFont(TypographyConstants.FONT_SMALL);
        pageLabel.setForeground(TractionRecTheme.TEXT_PRIMARY);
        panel.add(pageLabel);

        StyleUtils.addHorizontalSpacing(panel, StyleUtils.SPACING_SMALL);

        pageField = new JTextField(4);
        pageField.setFont(TypographyConstants.FONT_SMALL);
        pageField.setMaximumSize(new Dimension(60, 28));
        pageField.setPreferredSize(new Dimension(60, 28));
        StyleUtils.styleTextField(pageField);
        panel.add(pageField);

        StyleUtils.addHorizontalSpacing(panel, StyleUtils.SPACING_MEDIUM);

        // Next page button
        nextPageButton = StyleUtils.createIconButton("Next", "▶"); // ▶
        StyleUtils.styleButtonSecondary(nextPageButton);
        panel.add(nextPageButton);

        StyleUtils.addHorizontalSpacing(panel, StyleUtils.SPACING_SMALL);

        // Last page button
        lastPageButton = StyleUtils.createIconButton("Last", "⏭"); // ⏭
        StyleUtils.styleButtonSecondary(lastPageButton);
        panel.add(lastPageButton);

        panel.add(Box.createHorizontalGlue());

        return panel;
    }

    private JList<Integer> createRowHeader() {
        DefaultListModel<Integer> listModel = new DefaultListModel<>();
        updateRowHeader(listModel);

        JList<Integer> rowHeader = new JList<>(listModel);
        rowHeader.setFixedCellWidth(60); // Slightly wider for better readability
        rowHeader.setFixedCellHeight(22); // Match table row height
        rowHeader.setCellRenderer(new RowHeaderRenderer());
        rowHeader.setBackground(new Color(0xF9FAFB)); // Light gray background
        rowHeader.setFont(TypographyConstants.FONT_CAPTION);
        rowHeader.setSelectionModel(previewTable.getSelectionModel());

        return rowHeader;
    }

    private void updateRowHeader(DefaultListModel<Integer> listModel) {
        listModel.clear();
        int startRow = pagedData.getCurrentPageStartRow() + 1; // 1-based for display
        int endRow = pagedData.getCurrentPageEndRow();

        for (int i = startRow; i <= endRow; i++) {
            listModel.addElement(i);
        }
    }

    private JComponent createCornerComponent() {
        JLabel corner = new JLabel("Row", SwingConstants.CENTER);
        corner.setBorder(BorderFactory.createLineBorder(new Color(0xE5E7EB), 1));
        corner.setBackground(new Color(0xF9FAFB));
        corner.setFont(TypographyConstants.FONT_CAPTION);
        corner.setForeground(TractionRecTheme.TEXT_SECONDARY);
        corner.setOpaque(true);
        return corner;
    }

    private JPanel createIssuesSection() {
        JPanel section = StyleUtils.createElevatedCard();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

        // Section title (will be updated with count in loadData)
        issuesSectionTitle = StyleUtils.createSectionTitle(StyleUtils.Icons.WARNING + "  Validation Issues");
        section.add(issuesSectionTitle);

        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_SMALL);

        // Add instruction label
        JLabel instructionLabel = new JLabel("<html><div style='color: #6B7280; font-style: italic;'>Click issues to navigate to cells, or click cells to find related issues</div></html>");
        instructionLabel.setFont(TypographyConstants.FONT_CAPTION);
        instructionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(instructionLabel);

        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_MEDIUM);

        // Create issues list with modern styling
        issueList = new JList<>();
        issueList.setCellRenderer(new ValidationIssueListRenderer());
        issueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        issueList.setFont(TypographyConstants.FONT_SMALL);
        issueList.setBackground(Color.WHITE);
        issueList.setSelectionBackground(new Color(0xDCFDF7));
        issueList.setSelectionForeground(TractionRecTheme.TEXT_PRIMARY);

        JScrollPane scrollPane = new JScrollPane(issueList);
        scrollPane.setPreferredSize(new Dimension(400, 700)); // Larger for bigger window
        scrollPane.setBorder(StyleUtils.createInputBorder());
        scrollPane.setBackground(Color.WHITE);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(scrollPane);

        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_MEDIUM);

        // Issue count label with modern styling
        issueCountLabel = new JLabel();
        issueCountLabel.setFont(TypographyConstants.FONT_SMALL);
        issueCountLabel.setForeground(TractionRecTheme.TEXT_SECONDARY);
        issueCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(issueCountLabel);

        return section;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(
            StyleUtils.SPACING_LARGE, 0, 0, 0
        ));

        panel.add(Box.createHorizontalGlue());

        JButton closeButton = StyleUtils.createIconButton("Close", StyleUtils.Icons.CHECK);
        StyleUtils.styleButtonPrimary(closeButton, true); // Large button
        closeButton.addActionListener(e -> dispose());
        panel.add(closeButton);

        return panel;
    }

    private void setupEventHandlers() {
        // ESC key to close
        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Issue list selection handler - navigate from issue to cell
        issueList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                ValidationIssue selectedIssue = issueList.getSelectedValue();
                if (selectedIssue != null && selectedIssue.getLocation() != null) {
                    navigateToCell(selectedIssue.getLocation());
                }
            }
        });

        // Table cell selection handler - navigate from cell to issue
        previewTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = previewTable.getSelectedRow();
                int selectedColumn = previewTable.getSelectedColumn();
                if (selectedRow >= 0 && selectedColumn >= 0) {
                    navigateToIssue(selectedRow, selectedColumn);
                }
            }
        });

        previewTable.getColumnModel().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = previewTable.getSelectedRow();
                int selectedColumn = previewTable.getSelectedColumn();
                if (selectedRow >= 0 && selectedColumn >= 0) {
                    navigateToIssue(selectedRow, selectedColumn);
                }
            }
        });

        // Pagination event handlers
        firstPageButton.addActionListener(e -> {
            pagedData.firstPage();
            refreshPage();
        });

        prevPageButton.addActionListener(e -> {
            if (pagedData.previousPage()) {
                refreshPage();
            }
        });

        nextPageButton.addActionListener(e -> {
            if (pagedData.nextPage()) {
                refreshPage();
            }
        });

        lastPageButton.addActionListener(e -> {
            pagedData.lastPage();
            refreshPage();
        });

        // Page field input
        pageField.addActionListener(e -> {
            try {
                int page = Integer.parseInt(pageField.getText()) - 1; // Convert to 0-based
                if (pagedData.goToPage(page)) {
                    refreshPage();
                } else {
                    // Reset to current page if invalid
                    pageField.setText(String.valueOf(pagedData.getCurrentPage() + 1));
                }
            } catch (NumberFormatException ex) {
                // Reset to current page if invalid
                pageField.setText(String.valueOf(pagedData.getCurrentPage() + 1));
            }
        });

        // Window closing handler
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    private void loadData() {
        // Set paged preview data
        previewTable.setPagedPreviewData(pagedData);

        // Update stats
        if (previewData.getStats() != null) {
            String statsText = String.format(
                "File: %s | Total Rows: %d | Columns: %d | Issues: %d",
                previewData.getStats().getFormattedFileSize(),
                previewData.getStats().getTotalRows(),
                previewData.getStats().getTotalColumns(),
                previewData.getCellIssues().size()
            );
            statsLabel.setText(statsText);
        }

        // Load issues (all issues, not paged)
        DefaultListModel<ValidationIssue> issueModel = new DefaultListModel<>();
        for (ValidationIssue issue : pagedData.getAllCellIssues().values()) {
            issueModel.addElement(issue);
        }
        issueList.setModel(issueModel);

        // Update issue count with modern styling
        int issueCount = pagedData.getAllCellIssues().size();
        if (issueCount > 0) {
            issueCountLabel.setText(String.format("⚠ %d validation issues found", issueCount));
            issueCountLabel.setForeground(TractionRecTheme.WARNING_ORANGE);

            // Update section title with count
            issuesSectionTitle.setText(String.format("%s  Validation Issues (%d)",
                StyleUtils.Icons.WARNING, issueCount));
        } else {
            issueCountLabel.setText("✓ No validation issues found");
            issueCountLabel.setForeground(TractionRecTheme.SUCCESS_GREEN);

            // Update section title
            issuesSectionTitle.setText(String.format("%s  Validation Issues (0)",
                StyleUtils.Icons.CHECK));
        }

        // Initial page setup
        refreshPage();
    }

    private void refreshPage() {
        // Update table data for current page
        previewTable.refreshPageData();

        // Update row header
        JScrollPane scrollPane = (JScrollPane) previewTable.getParent().getParent();
        JList<?> rowHeader = (JList<?>) scrollPane.getRowHeader().getView();
        DefaultListModel<Integer> rowModel = (DefaultListModel<Integer>) rowHeader.getModel();
        updateRowHeader(rowModel);

        // Update pagination controls
        updatePaginationControls();

        // Update page info
        pageInfoLabel.setText(pagedData.getPageInfo());

        // Scroll to top of current page
        previewTable.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
    }

    private void updatePaginationControls() {
        int currentPage = pagedData.getCurrentPage();
        int totalPages = pagedData.getTotalPages();

        // Update button states
        firstPageButton.setEnabled(currentPage > 0);
        prevPageButton.setEnabled(currentPage > 0);
        nextPageButton.setEnabled(currentPage < totalPages - 1);
        lastPageButton.setEnabled(currentPage < totalPages - 1);

        // Update page field
        pageField.setText(String.valueOf(currentPage + 1));

        // Update page label
        pageLabel.setText(String.format("Page (1-%d):", totalPages));
    }

    private void navigateToCell(CellLocation location) {
        int globalRow = location.row();
        int column = location.column();

        // Navigate to the page containing this row
        if (pagedData.goToRow(globalRow)) {
            refreshPage();

            // Convert to page-relative coordinates
            int pageRow = pagedData.getPageRowIndex(globalRow);

            // Highlight and navigate to the cell
            previewTable.highlightCell(new CellLocation(pageRow, column));

            // Scroll to the cell
            Rectangle cellRect = previewTable.getCellRect(pageRow, column, true);
            previewTable.scrollRectToVisible(cellRect);

            // Select the cell
            previewTable.setRowSelectionInterval(pageRow, pageRow);
            previewTable.setColumnSelectionInterval(column, column);
        }
    }

    private void navigateToIssue(int pageRow, int column) {
        // Convert page-relative coordinates to global coordinates
        int globalRow = pagedData.getGlobalRowIndex(pageRow);

        // Check if this cell has a validation issue
        if (pagedData.hasCellIssue(pageRow, column)) {
            ValidationIssue cellIssue = pagedData.getCellIssue(pageRow, column);
            if (cellIssue != null) {
                // Find and select the corresponding issue in the list
                DefaultListModel<ValidationIssue> listModel = (DefaultListModel<ValidationIssue>) issueList.getModel();
                for (int i = 0; i < listModel.getSize(); i++) {
                    ValidationIssue issue = listModel.getElementAt(i);
                    if (issue.getLocation() != null &&
                        issue.getLocation().row() == globalRow &&
                        issue.getLocation().column() == column) {

                        // Select the issue in the list
                        issueList.setSelectedIndex(i);

                        // Scroll to make the selected issue visible
                        issueList.ensureIndexIsVisible(i);

                        // Highlight the cell
                        previewTable.highlightCell(new CellLocation(pageRow, column));
                        break;
                    }
                }
            }
        } else {
            // Clear issue selection if cell has no issues
            issueList.clearSelection();
            previewTable.highlightCell(null);
        }
    }

    /**
     * Show the CSV preview window
     */
    public static void showPreview(Window parent, CsvPreviewData previewData) {
        SwingUtilities.invokeLater(() -> {
            CsvPreviewWindow window = new CsvPreviewWindow(parent, previewData);
            window.setVisible(true);
        });
    }

    /**
     * Row header renderer for row numbers
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
            setForeground(Color.DARK_GRAY);
            setFont(getFont().deriveFont(Font.BOLD));

            return component;
        }
    }
}
