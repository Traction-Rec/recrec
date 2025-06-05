package com.tractionrec.recrec.ui;

import com.tractionrec.recrec.RecRecState;
import com.tractionrec.recrec.csv.CsvFileStats;
import com.tractionrec.recrec.csv.CsvPreviewData;
import com.tractionrec.recrec.csv.CsvValidationResult;
import com.tractionrec.recrec.csv.CsvValidationService;
import com.tractionrec.recrec.csv.ValidationIssue;
import com.tractionrec.recrec.csv.ValidationOptions;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

/**
 * CSV validation form with preview and issue display
 */
public class RecRecCsvValidation extends RecRecForm {

    private JPanel rootPanel;
    private JButton backButton;
    private JButton nextButton;
    private JButton validateButton;
    private JButton fixButton;
    private JButton exportButton;
    private JCheckBox ignoreWarningsCheckbox;

    private JButton previewButton;
    private JList<ValidationIssue> issueList;
    private JLabel statsLabel;
    private JLabel statusLabel;
    private JProgressBar validationProgress;

    private final CsvValidationService validationService;
    private CsvValidationResult validationResult;
    private CsvPreviewData previewData;

    public RecRecCsvValidation(RecRecState state, NavigationAction navAction) {
        super(state, navAction);
        this.validationService = new CsvValidationService();
        setupUI();
        setupEventHandlers();
    }

    protected void setupUI() {
        rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top panel with status and controls
        JPanel topPanel = createTopPanel();
        rootPanel.add(topPanel, BorderLayout.NORTH);

        // Center panel with issues list
        JPanel centerPanel = createCenterPanel();
        rootPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom panel with navigation
        JPanel bottomPanel = createBottomPanel();
        rootPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Validating CSV file...");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 14f));
        statusPanel.add(statusLabel);

        // Progress bar
        validationProgress = new JProgressBar();
        validationProgress.setIndeterminate(true);
        validationProgress.setVisible(false);
        statusPanel.add(validationProgress);

        panel.add(statusPanel, BorderLayout.NORTH);

        // Stats panel
        statsLabel = new JLabel("File statistics will appear here");
        statsLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        panel.add(statsLabel, BorderLayout.CENTER);

        // Controls panel
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        validateButton = new JButton("Re-validate");
        validateButton.setEnabled(false);
        controlsPanel.add(validateButton);

        fixButton = new JButton("Fix Issues Automatically");
        fixButton.setEnabled(false);
        controlsPanel.add(fixButton);

        exportButton = new JButton("Export Clean CSV");
        exportButton.setEnabled(false);
        exportButton.addActionListener(e -> performExportClean());
        controlsPanel.add(exportButton);

        previewButton = new JButton("Open Full Preview");
        previewButton.setEnabled(false);
        previewButton.addActionListener(e -> openFullPreview());
        controlsPanel.add(previewButton);

        ignoreWarningsCheckbox = new JCheckBox("Ignore warnings and proceed");
        controlsPanel.add(ignoreWarningsCheckbox);

        panel.add(controlsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Issues panel takes full center
        JPanel issuesPanel = createIssuesPanel();
        panel.add(issuesPanel, BorderLayout.CENTER);

        // Add preview info panel
        JPanel previewInfoPanel = createPreviewInfoPanel();
        panel.add(previewInfoPanel, BorderLayout.NORTH);

        return panel;
    }

    private JPanel createPreviewInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel infoLabel = new JLabel("Click 'Open Full Preview' to view CSV data in a spreadsheet-style window");
        infoLabel.setForeground(Color.BLUE);
        infoLabel.setFont(infoLabel.getFont().deriveFont(Font.ITALIC));
        panel.add(infoLabel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createIssuesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Validation Issues"));

        // Create issues list
        issueList = new JList<>();
        issueList.setCellRenderer(new ValidationIssueListRenderer());
        issueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(issueList);
        scrollPane.setPreferredSize(new Dimension(600, 300)); // More width, less height

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        backButton = new JButton("< Back");
        panel.add(backButton);

        nextButton = new JButton("Next >");
        nextButton.setEnabled(false);
        panel.add(nextButton);

        return panel;
    }

    private void setupEventHandlers() {
        backButton.addActionListener(e -> navigationAction.onBack());

        nextButton.addActionListener(e -> {
            // Store validation result in state for next form
            state.validationResult = validationResult;
            navigationAction.onNext();
        });

        validateButton.addActionListener(e -> performValidation());

        fixButton.addActionListener(e -> performAutoFix());

        ignoreWarningsCheckbox.addActionListener(e -> updateNextButtonState());
    }

    @Override
    public void willDisplay() {
        if (state.inputFile != null) {
            performValidation();
        }
    }

    @Override
    public RecRecForm whatIsNext() {
        return new RecRecRunning(state, navigationAction);
    }

    @Override
    public JComponent getRootComponent() {
        return rootPanel;
    }

    @Override
    Dimension getPreferredWindowSize() {
        // CSV validation needs more space for buttons and issues list
        return new Dimension(700, 500);
    }

    @Override
    boolean requiresExtraSpace() {
        // This form has dynamic content (issues list) that may need more space
        return true;
    }

    @Override
    Dimension getMinimumWindowSize() {
        // CSV validation needs a larger minimum size due to its complexity
        return new Dimension(600, 400);
    }

    private void performValidation() {
        if (state.inputFile == null) {
            return;
        }

        // Show progress
        validationProgress.setVisible(true);
        validateButton.setEnabled(false);
        statusLabel.setText("Validating CSV file...");

        // Perform validation in background
        CompletableFuture.supplyAsync(() -> {
            CsvValidationResult result = validationService.validateCsv(state.inputFile);
            CsvPreviewData preview = validationService.generatePreview(state.inputFile, 100);
            return new ValidationData(result, preview);
        }).thenAccept(data -> {
            SwingUtilities.invokeLater(() -> {
                this.validationResult = data.result;
                this.previewData = data.preview;
                updateUI();
                validationProgress.setVisible(false);
                validateButton.setEnabled(true);
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Validation failed: " + throwable.getMessage());
                validationProgress.setVisible(false);
                validateButton.setEnabled(true);
            });
            return null;
        });
    }

    private void performAutoFix() {
        if (state.inputFile == null || validationResult == null) {
            return;
        }

        // Show confirmation dialog
        int choice = JOptionPane.showConfirmDialog(rootPanel,
            "This will create a new CSV file with automatic fixes applied.\n" +
            "The original file will not be modified.\n\n" +
            "Continue?",
            "Auto-Fix CSV",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        // Show progress
        fixButton.setEnabled(false);
        statusLabel.setText("Applying automatic fixes...");
        validationProgress.setVisible(true);

        // Perform auto-fix in background
        CompletableFuture.supplyAsync(() -> {
            ValidationOptions options = ValidationOptions.defaults();
            return validationService.fixCommonIssues(state.inputFile, options);
        }).thenAccept(fixedFile -> {
            SwingUtilities.invokeLater(() -> {
                validationProgress.setVisible(false);
                fixButton.setEnabled(true);

                // Show success message with option to use fixed file
                int useFixed = JOptionPane.showConfirmDialog(rootPanel,
                    "Fixed file created: " + fixedFile.getName() + "\n\n" +
                    "Would you like to use the fixed file for processing?",
                    "Auto-Fix Complete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);

                if (useFixed == JOptionPane.YES_OPTION) {
                    // Update state to use fixed file and re-validate
                    state.inputFile = fixedFile;
                    performValidation();
                } else {
                    statusLabel.setText("Auto-fix completed. Fixed file saved as: " + fixedFile.getName());
                }
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                validationProgress.setVisible(false);
                fixButton.setEnabled(true);
                statusLabel.setText("Auto-fix failed: " + throwable.getMessage());

                JOptionPane.showMessageDialog(rootPanel,
                    "Failed to apply automatic fixes:\n" + throwable.getMessage(),
                    "Auto-Fix Error",
                    JOptionPane.ERROR_MESSAGE);
            });
            return null;
        });
    }

    private void performExportClean() {
        if (state.inputFile == null) {
            return;
        }

        // Show file chooser for export location
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Clean CSV");
        fileChooser.setSelectedFile(new File(state.inputFile.getParent(),
            getCleanFileName(state.inputFile.getName())));

        int result = fileChooser.showSaveDialog(rootPanel);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File exportFile = fileChooser.getSelectedFile();

        // Show progress
        exportButton.setEnabled(false);
        statusLabel.setText("Exporting clean CSV...");
        validationProgress.setVisible(true);

        // Perform export in background
        CompletableFuture.supplyAsync(() -> {
            ValidationOptions options = ValidationOptions.defaults();
            return validationService.fixCommonIssues(state.inputFile, options);
        }).thenAccept(fixedFile -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    // Copy fixed file to chosen location
                    Files.copy(fixedFile.toPath(), exportFile.toPath(),
                              java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                    validationProgress.setVisible(false);
                    exportButton.setEnabled(true);
                    statusLabel.setText("Clean CSV exported successfully: " + exportFile.getName());

                    JOptionPane.showMessageDialog(rootPanel,
                        "Clean CSV file exported successfully:\n" + exportFile.getAbsolutePath(),
                        "Export Complete",
                        JOptionPane.INFORMATION_MESSAGE);

                    // Clean up temporary fixed file
                    fixedFile.delete();

                } catch (Exception e) {
                    validationProgress.setVisible(false);
                    exportButton.setEnabled(true);
                    statusLabel.setText("Export failed: " + e.getMessage());

                    JOptionPane.showMessageDialog(rootPanel,
                        "Failed to export clean CSV:\n" + e.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                validationProgress.setVisible(false);
                exportButton.setEnabled(true);
                statusLabel.setText("Export failed: " + throwable.getMessage());

                JOptionPane.showMessageDialog(rootPanel,
                    "Failed to create clean CSV:\n" + throwable.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            });
            return null;
        });
    }

    private void openFullPreview() {
        if (previewData == null) {
            JOptionPane.showMessageDialog(rootPanel,
                "No preview data available. Please run validation first.",
                "No Preview Data",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get the parent window
        Window parentWindow = SwingUtilities.getWindowAncestor(rootPanel);

        // Open full-screen preview window
        CsvPreviewWindow.showPreview(parentWindow, previewData);
    }

    private String getCleanFileName(String originalFileName) {
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            String nameWithoutExt = originalFileName.substring(0, dotIndex);
            String extension = originalFileName.substring(dotIndex);
            return nameWithoutExt + "_clean" + extension;
        } else {
            return originalFileName + "_clean";
        }
    }

    private void updateUI() {
        if (validationResult == null || previewData == null) {
            return;
        }

        // Update status
        updateStatusDisplay();

        // Update stats
        updateStatsDisplay();

        // Update issues list
        updateIssuesList();

        // Update controls
        updateControlsState();

        // Update next button
        updateNextButtonState();
    }

    private void updateStatusDisplay() {
        String statusText;
        Color statusColor;

        switch (validationResult.getOverallStatus()) {
            case VALID:
                statusText = "✓ CSV file is valid";
                statusColor = new Color(0, 128, 0);
                break;
            case WARNING:
                statusText = "⚠ CSV file has warnings";
                statusColor = new Color(255, 140, 0);
                break;
            case ERROR:
                statusText = "✗ CSV file has errors";
                statusColor = Color.RED;
                break;
            case INVALID:
                statusText = "✗ CSV file is invalid";
                statusColor = Color.RED;
                break;
            default:
                statusText = "Unknown status";
                statusColor = Color.BLACK;
        }

        statusLabel.setText(statusText);
        statusLabel.setForeground(statusColor);
    }

    private void updateStatsDisplay() {
        if (validationResult.getStats() != null) {
            CsvFileStats stats = validationResult.getStats();
            String statsText = String.format(
                "File: %s | Rows: %d (%d data, %d empty) | Columns: %d | Issues: %d errors, %d warnings",
                stats.getFormattedFileSize(),
                stats.getTotalRows(),
                stats.getDataRows(),
                stats.getEmptyRows(),
                stats.getTotalColumns(),
                validationResult.getErrorCount(),
                validationResult.getWarningCount()
            );
            statsLabel.setText(statsText);
        }
    }

    private void updateIssuesList() {
        DefaultListModel<ValidationIssue> listModel = new DefaultListModel<>();
        for (ValidationIssue issue : validationResult.getIssues()) {
            listModel.addElement(issue);
        }
        issueList.setModel(listModel);
    }

    private void updateControlsState() {
        fixButton.setEnabled(validationResult.hasAutoFixableIssues());
        exportButton.setEnabled(true); // Always allow export
        previewButton.setEnabled(previewData != null); // Enable when preview data is available
    }

    private void updateNextButtonState() {
        boolean canProceed = validationResult.canProceed() ||
                           (ignoreWarningsCheckbox.isSelected() &&
                            validationResult.getOverallStatus() == com.tractionrec.recrec.domain.ValidationStatus.WARNING);
        nextButton.setEnabled(canProceed);
    }

    /**
     * Helper class to hold validation data
     */
    private static class ValidationData {
        final CsvValidationResult result;
        final CsvPreviewData preview;

        ValidationData(CsvValidationResult result, CsvPreviewData preview) {
            this.result = result;
            this.preview = preview;
        }
    }
}
