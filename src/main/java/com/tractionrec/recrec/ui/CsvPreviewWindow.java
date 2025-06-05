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
    private CsvPreviewData previewData;

    public CsvPreviewWindow(Window parent, CsvPreviewData previewData) {
        super(parent, "CSV Preview - " + (previewData.getStats() != null ? previewData.getStats().getFormattedFileSize() : ""), ModalityType.MODELESS);
        this.previewData = previewData;

        setupUI();
        setupEventHandlers();
        loadData();

        // Set to large size (dialogs can't be maximized like frames)
        setSize(1200, 800);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Center on screen
        setLocationRelativeTo(parent);
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        // Top panel with stats and controls
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Center panel with split pane
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // Bottom panel with close button
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Stats label
        statsLabel = new JLabel();
        statsLabel.setFont(statsLabel.getFont().deriveFont(Font.BOLD, 14f));
        panel.add(statsLabel, BorderLayout.WEST);

        // Instructions
        JLabel instructions = new JLabel("<html><i>Click on issues in the right panel to navigate to problematic cells. Press ESC to close.</i></html>");
        instructions.setForeground(Color.GRAY);
        panel.add(instructions, BorderLayout.EAST);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create split pane with more space for preview
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.75); // Give more space to preview in full screen
        splitPane.setDividerLocation(0.75);

        // Preview panel
        JPanel previewPanel = createPreviewPanel();
        splitPane.setLeftComponent(previewPanel);

        // Issues panel
        JPanel issuesPanel = createIssuesPanel();
        splitPane.setRightComponent(issuesPanel);

        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("CSV Data Preview"));

        // Create enhanced preview table
        previewTable = new CsvPreviewTable();

        // Create scroll pane with row header for row numbers
        JScrollPane scrollPane = new JScrollPane(previewTable);
        scrollPane.setRowHeaderView(createRowHeader());
        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, createCornerComponent());

        // Set preferred size for full screen
        scrollPane.setPreferredSize(new Dimension(800, 600));

        panel.add(scrollPane, BorderLayout.CENTER);

        // Info panel at bottom
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel infoLabel = new JLabel();
        if (previewData.isPreviewTruncated()) {
            infoLabel.setText(String.format("Showing first %d of %d rows",
                previewData.getPreviewRowCount(), previewData.getTotalRowsInFile()));
            infoLabel.setForeground(Color.BLUE);
        } else {
            infoLabel.setText(String.format("Showing all %d rows", previewData.getPreviewRowCount()));
        }
        infoPanel.add(infoLabel);
        panel.add(infoPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JList<Integer> createRowHeader() {
        DefaultListModel<Integer> listModel = new DefaultListModel<>();
        for (int i = 1; i <= previewData.getPreviewRowCount(); i++) {
            listModel.addElement(i);
        }

        JList<Integer> rowHeader = new JList<>(listModel);
        rowHeader.setFixedCellWidth(50);
        rowHeader.setFixedCellHeight(previewTable.getRowHeight());
        rowHeader.setCellRenderer(new RowHeaderRenderer());
        rowHeader.setBackground(previewTable.getTableHeader().getBackground());
        rowHeader.setSelectionModel(previewTable.getSelectionModel());

        return rowHeader;
    }

    private JComponent createCornerComponent() {
        JLabel corner = new JLabel("Row", SwingConstants.CENTER);
        corner.setBorder(BorderFactory.createRaisedBevelBorder());
        corner.setBackground(previewTable.getTableHeader().getBackground());
        corner.setOpaque(true);
        return corner;
    }

    private JPanel createIssuesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Validation Issues"));

        // Create issues list
        issueList = new JList<>();
        issueList.setCellRenderer(new ValidationIssueListRenderer());
        issueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(issueList);
        scrollPane.setPreferredSize(new Dimension(350, 600));

        panel.add(scrollPane, BorderLayout.CENTER);

        // Issue count label
        JLabel issueCountLabel = new JLabel();
        issueCountLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(issueCountLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton closeButton = new JButton("Close");
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

        // Issue list selection handler
        issueList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                ValidationIssue selectedIssue = issueList.getSelectedValue();
                if (selectedIssue != null && selectedIssue.getLocation() != null) {
                    navigateToCell(selectedIssue.getLocation());
                }
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
        // Set preview data
        previewTable.setPreviewData(previewData);

        // Update stats
        if (previewData.getStats() != null) {
            String statsText = String.format(
                "File: %s | Rows: %d | Columns: %d | Issues: %d",
                previewData.getStats().getFormattedFileSize(),
                previewData.getStats().getTotalRows(),
                previewData.getStats().getTotalColumns(),
                previewData.getCellIssues().size()
            );
            statsLabel.setText(statsText);
        }

        // Load issues
        DefaultListModel<ValidationIssue> issueModel = new DefaultListModel<>();
        for (ValidationIssue issue : previewData.getCellIssues().values()) {
            issueModel.addElement(issue);
        }
        issueList.setModel(issueModel);

        // Update issue count
        Component issuesPanel = ((BorderLayout) getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (issuesPanel instanceof JPanel) {
            JPanel centerPanel = (JPanel) issuesPanel;
            JSplitPane splitPane = (JSplitPane) centerPanel.getComponent(0);
            JPanel rightPanel = (JPanel) splitPane.getRightComponent();
            JLabel issueCountLabel = (JLabel) rightPanel.getComponent(1);
            issueCountLabel.setText(String.format("%d validation issues found", previewData.getCellIssues().size()));
        }
    }

    private void navigateToCell(CellLocation location) {
        previewTable.highlightCell(location);

        // Scroll to the cell
        Rectangle cellRect = previewTable.getCellRect(location.row(), location.column(), true);
        previewTable.scrollRectToVisible(cellRect);

        // Select the cell
        previewTable.setRowSelectionInterval(location.row(), location.row());
        previewTable.setColumnSelectionInterval(location.column(), location.column());
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
