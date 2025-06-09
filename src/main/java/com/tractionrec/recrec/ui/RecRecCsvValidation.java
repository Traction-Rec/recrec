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
        rootPanel = new JPanel();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(
            StyleUtils.SPACING_XXLARGE,
            StyleUtils.SPACING_XXLARGE,
            StyleUtils.SPACING_XXLARGE,
            StyleUtils.SPACING_XXLARGE
        ));
        rootPanel.setBackground(Color.WHITE);

        // Form Title
        JLabel titleLabel = StyleUtils.createFormTitle("CSV Validation");
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Review and fix any issues found in your CSV file");
        subtitleLabel.setFont(TypographyConstants.FONT_SMALL);
        subtitleLabel.setForeground(TractionRecTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(subtitleLabel);
        StyleUtils.addVerticalSpacing(rootPanel, StyleUtils.SPACING_XLARGE);

        // Status Section
        JPanel statusSection = createStatusSection();
        statusSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(statusSection);
        StyleUtils.addVerticalSpacing(rootPanel, StyleUtils.SPACING_LARGE);

        // Controls Section
        JPanel controlsSection = createControlsSection();
        controlsSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(controlsSection);
        StyleUtils.addVerticalSpacing(rootPanel, StyleUtils.SPACING_LARGE);

        // Issues Section
        JPanel issuesSection = createIssuesSection();
        issuesSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(issuesSection);
        StyleUtils.addVerticalSpacing(rootPanel, StyleUtils.SPACING_XLARGE);

        // Navigation Section
        JPanel navigationSection = createNavigationSection();
        navigationSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(navigationSection);
    }

    private JPanel createStatusSection() {
        JPanel section = StyleUtils.createCard();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

        // Section title with icon
        JLabel sectionTitle = StyleUtils.createSectionTitle(StyleUtils.Icons.INFO + "  Validation Status");
        section.add(sectionTitle);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_MEDIUM);

        // Status label
        statusLabel = new JLabel("Validating CSV file...");
        statusLabel.setFont(TypographyConstants.FONT_BODY_BOLD);
        statusLabel.setForeground(TractionRecTheme.PRIMARY_BLUE);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(statusLabel);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_SMALL);

        // Progress bar
        validationProgress = new JProgressBar();
        validationProgress.setIndeterminate(true);
        validationProgress.setVisible(false);
        validationProgress.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(validationProgress);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_MEDIUM);

        // Stats label
        statsLabel = new JLabel("File statistics will appear here");
        statsLabel.setFont(TypographyConstants.FONT_SMALL);
        statsLabel.setForeground(TractionRecTheme.TEXT_SECONDARY);
        statsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(statsLabel);

        return section;
    }

    private JPanel createControlsSection() {
        JPanel section = StyleUtils.createElevatedCard();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

        // Section title with icon
        JLabel sectionTitle = StyleUtils.createSectionTitle(StyleUtils.Icons.SETTINGS + "  Actions");
        section.add(sectionTitle);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_MEDIUM);

        // First row of buttons
        JPanel buttonRow1 = new JPanel();
        buttonRow1.setLayout(new BoxLayout(buttonRow1, BoxLayout.X_AXIS));
        buttonRow1.setAlignmentX(Component.LEFT_ALIGNMENT);

        validateButton = StyleUtils.createIconButton("Re-validate", StyleUtils.Icons.REFRESH);
        validateButton.setEnabled(false);
        StyleUtils.styleButtonSecondary(validateButton);
        buttonRow1.add(validateButton);
        StyleUtils.addHorizontalSpacing(buttonRow1, StyleUtils.SPACING_MEDIUM);

        fixButton = StyleUtils.createIconButton("Fix Issues", StyleUtils.Icons.SETTINGS);
        fixButton.setEnabled(false);
        StyleUtils.styleButtonSecondary(fixButton);
        buttonRow1.add(fixButton);
        buttonRow1.add(Box.createHorizontalGlue());

        section.add(buttonRow1);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_MEDIUM);

        // Second row of buttons
        JPanel buttonRow2 = new JPanel();
        buttonRow2.setLayout(new BoxLayout(buttonRow2, BoxLayout.X_AXIS));
        buttonRow2.setAlignmentX(Component.LEFT_ALIGNMENT);

        exportButton = StyleUtils.createIconButton("Export Clean CSV", StyleUtils.Icons.DOWNLOAD);
        exportButton.setEnabled(false);
        exportButton.addActionListener(e -> performExportClean());
        StyleUtils.styleButtonSecondary(exportButton);
        buttonRow2.add(exportButton);
        StyleUtils.addHorizontalSpacing(buttonRow2, StyleUtils.SPACING_MEDIUM);

        previewButton = StyleUtils.createIconButton("Open Full Preview", StyleUtils.Icons.SEARCH);
        previewButton.setEnabled(false);
        previewButton.addActionListener(e -> openFullPreview());
        StyleUtils.styleButtonPrimary(previewButton);
        buttonRow2.add(previewButton);
        buttonRow2.add(Box.createHorizontalGlue());

        section.add(buttonRow2);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_MEDIUM);

        // Checkbox
        ignoreWarningsCheckbox = new JCheckBox("Ignore warnings and proceed");
        StyleUtils.styleCheckBox(ignoreWarningsCheckbox);
        ignoreWarningsCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(ignoreWarningsCheckbox);

        return section;
    }

    private JPanel createIssuesSection() {
        JPanel section = StyleUtils.createElevatedCard();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

        // Section title with icon
        JLabel sectionTitle = StyleUtils.createSectionTitle(StyleUtils.Icons.WARNING + "  Validation Issues");
        section.add(sectionTitle);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_MEDIUM);

        // Info text
        JLabel infoLabel = new JLabel("<html><div style='width: 500px;'><p style='color: #3B82F6; font-style: italic;'>Click 'Open Full Preview' to view CSV data in a spreadsheet-style window with issue highlighting.</p></div></html>");
        infoLabel.setFont(TypographyConstants.FONT_SMALL);
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(infoLabel);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_MEDIUM);

        // Create issues list
        issueList = new JList<>();
        issueList.setCellRenderer(new ValidationIssueListRenderer());
        issueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        issueList.setFont(TypographyConstants.FONT_SMALL);

        JScrollPane scrollPane = new JScrollPane(issueList);
        scrollPane.setPreferredSize(new Dimension(600, 200));
        scrollPane.setBorder(StyleUtils.createInputBorder());
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        section.add(scrollPane);

        return section;
    }

    private JPanel createNavigationSection() {
        backButton = StyleUtils.createIconButton("Back", StyleUtils.Icons.ARROW_LEFT);
        StyleUtils.styleButtonSecondary(backButton);

        nextButton = StyleUtils.createIconButton("Continue", StyleUtils.Icons.ARROW_RIGHT);
        nextButton.setEnabled(false);
        StyleUtils.styleButtonPrimary(nextButton, true); // Use large button

        return StyleUtils.createNavigationPanel(backButton, nextButton);
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
            // Request more rows for paging support - up to 50k rows
            CsvPreviewData preview = validationService.generatePreview(state.inputFile, 50000);
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

        // Check for scientific notation patterns and offer replacement
        checkForScientificNotationPatterns();

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

    private void checkForScientificNotationPatterns() {
        if (validationResult == null || validationResult.getPatternAnalysis() == null) {
            return;
        }

        var analysis = validationResult.getPatternAnalysis();

        // Check if all merchant scientific notation values are the same
        if (analysis.hasAllSameMerchantValues() && analysis.getMerchantScientificCount() > 1) {
            showScientificNotationReplacementDialog(
                "Merchant",
                analysis.getCommonMerchantValue(),
                analysis.getMerchantScientificCount()
            );
        }
        // Check if all ID scientific notation values are the same
        else if (analysis.hasAllSameIdValues() && analysis.getIdScientificCount() > 1) {
            showScientificNotationReplacementDialog(
                "ID",
                analysis.getCommonIdValue(),
                analysis.getIdScientificCount()
            );
        }
    }

    private void showScientificNotationReplacementDialog(String fieldName, String scientificValue, int count) {
        String message = String.format(
            "All %s values appear to be the same scientific notation: '%s'\n" +
            "This suggests they should all be the same actual value.\n\n" +
            "Found in %d rows. Would you like to replace all instances?",
            fieldName, scientificValue, count
        );

        int choice = JOptionPane.showConfirmDialog(
            rootPanel,
            message,
            "Replace Scientific Notation Values?",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            showReplacementValueDialog(fieldName, scientificValue);
        }
    }

    private void showReplacementValueDialog(String fieldName, String scientificValue) {
        String replacementValue = JOptionPane.showInputDialog(
            rootPanel,
            String.format("Enter the correct %s value to replace '%s':", fieldName, scientificValue),
            "Replace Value",
            JOptionPane.PLAIN_MESSAGE
        );

        if (replacementValue != null && !replacementValue.trim().isEmpty()) {
            performScientificNotationReplacement(scientificValue, replacementValue.trim());
        }
    }

    private void performScientificNotationReplacement(String scientificValue, String replacementValue) {
        if (state.inputFile == null) {
            return;
        }

        // Show progress
        statusLabel.setText("Replacing scientific notation values...");
        validationProgress.setVisible(true);

        // Perform replacement in background
        CompletableFuture.supplyAsync(() -> {
            return validationService.replaceScientificNotationValue(state.inputFile, scientificValue, replacementValue);
        }).thenAccept(replacedFile -> {
            SwingUtilities.invokeLater(() -> {
                validationProgress.setVisible(false);

                // Show success message with option to use replaced file
                int useReplaced = JOptionPane.showConfirmDialog(rootPanel,
                    String.format("Replaced all instances of '%s' with '%s'\n" +
                                "Replaced file created: %s\n\n" +
                                "Would you like to use the replaced file for processing?",
                                scientificValue, replacementValue, replacedFile.getName()),
                    "Replacement Complete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);

                if (useReplaced == JOptionPane.YES_OPTION) {
                    // Update state to use replaced file and re-validate
                    state.inputFile = replacedFile;
                    performValidation();
                } else {
                    statusLabel.setText("Replacement completed. Replaced file saved as: " + replacedFile.getName());
                }
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                validationProgress.setVisible(false);
                statusLabel.setText("Replacement failed: " + throwable.getMessage());

                JOptionPane.showMessageDialog(rootPanel,
                    "Failed to replace scientific notation values:\n" + throwable.getMessage(),
                    "Replacement Error",
                    JOptionPane.ERROR_MESSAGE);
            });
            return null;
        });
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
