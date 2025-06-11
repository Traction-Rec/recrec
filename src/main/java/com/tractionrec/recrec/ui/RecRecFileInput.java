package com.tractionrec.recrec.ui;

import com.tractionrec.recrec.RecRecState;

import javax.swing.*;
import java.awt.*;

public class RecRecFileInput extends RecRecForm {
    private JButton chooseFileButton;
    private JButton nextButton;
    private JButton backButton;
    private JPanel rootPanel;
    private JLabel lblQueryingBy;

    public RecRecFileInput(RecRecState state, NavigationAction navAction) {
        super(state, navAction);
        backButton.addActionListener(e -> navigationAction.onBack());
        nextButton.addActionListener(e -> navigationAction.onNext());
        chooseFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));
            int resultCode = fileChooser.showDialog(rootPanel, "Select");
            if (resultCode == JFileChooser.APPROVE_OPTION) {
                state.inputFile = fileChooser.getSelectedFile();
                chooseFileButton.setText(StyleUtils.Icons.CHECK + "  Selected: " + state.inputFile.getName());
                StyleUtils.styleButtonPrimary(chooseFileButton); // Change to primary style when file selected
                updateNextEnabled();
            }
        });
    }

    @Override
    public void willDisplay() {
        if (state.inputFile != null) {
            chooseFileButton.setText(StyleUtils.Icons.CHECK + "  Selected: " + state.inputFile.getName());
            StyleUtils.styleButtonPrimary(chooseFileButton); // Change to primary style when file selected
        }

        // Handle null queryMode gracefully (can happen during state reset navigation)
        if (state.queryMode != null) {
            switch (state.queryMode) {
                case SETUP_ID -> lblQueryingBy.setText("You are querying by setup id.");
                case RECORD_ID -> lblQueryingBy.setText("You are querying by record id.");
                case VANTIV_ID -> lblQueryingBy.setText("You are querying by vantiv id.");
                case PAYMENT_ACCOUNT -> lblQueryingBy.setText("You are querying by payment account token.");
            }
        } else {
            lblQueryingBy.setText("Please select a query type.");
        }

        updateNextEnabled();
    }

    @Override
    public RecRecForm whatIsNext() {
        return new RecRecCsvValidation(state, navigationAction);
    }

    private boolean isUserFinished() {
        return state.inputFile != null;
    }

    private void updateNextEnabled() {
        nextButton.setEnabled(isUserFinished());
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

        // Form Title
        JLabel titleLabel = StyleUtils.createFormTitle("Select Input File");
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(titleLabel);
        StyleUtils.addVerticalSpacing(rootPanel, StyleUtils.SPACING_LARGE);

        // Query Type Info Section
        JPanel queryInfoSection = createQueryInfoSection();
        queryInfoSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(queryInfoSection);
        StyleUtils.addVerticalSpacing(rootPanel, StyleUtils.SPACING_XLARGE);

        // File Selection Section
        JPanel fileSelectionSection = createFileSelectionSection();
        fileSelectionSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(fileSelectionSection);
        StyleUtils.addVerticalSpacing(rootPanel, StyleUtils.SPACING_XXLARGE);

        // Navigation Section
        JPanel navigationSection = createNavigationSection();
        navigationSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(navigationSection);
    }

    private JPanel createQueryInfoSection() {
        JPanel section = StyleUtils.createCard();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

        // Section title with icon
        JLabel sectionTitle = StyleUtils.createSectionTitle(StyleUtils.Icons.INFO + "  Query Information");
        section.add(sectionTitle);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_MEDIUM);

        // Query type label
        lblQueryingBy = new JLabel("You are querying by...");
        lblQueryingBy.setFont(TypographyConstants.FONT_BODY_BOLD);
        lblQueryingBy.setForeground(TractionRecTheme.PRIMARY_BLUE);
        lblQueryingBy.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(lblQueryingBy);

        return section;
    }

    private JPanel createFileSelectionSection() {
        JPanel section = StyleUtils.createElevatedCard();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

        // Section title with icon
        JLabel sectionTitle = StyleUtils.createSectionTitle(StyleUtils.Icons.FILE + "  CSV File Selection");
        section.add(sectionTitle);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_MEDIUM);

        // Help text
        JLabel helpLabel = new JLabel("<html><div style='width: 400px;'><p>Please choose a CSV file where each row has two columns: <strong>\"Merchant\"</strong> and <strong>\"Id\"</strong>.</p><p style='margin-top: 8px; color: #6B7280; font-size: 12px;'>The first row should contain column headers, and subsequent rows should contain the data to query.</p></div></html>");
        helpLabel.setFont(TypographyConstants.FONT_BODY);
        helpLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(helpLabel);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_LARGE);

        // File selection button
        chooseFileButton = StyleUtils.createIconButton("Choose File", StyleUtils.Icons.FOLDER);
        StyleUtils.styleButtonSecondary(chooseFileButton);
        chooseFileButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(chooseFileButton);

        return section;
    }

    private JPanel createNavigationSection() {
        backButton = StyleUtils.createIconButton("Back", StyleUtils.Icons.ARROW_LEFT);
        StyleUtils.styleButtonSecondary(backButton);

        nextButton = StyleUtils.createIconButton("Next", StyleUtils.Icons.ARROW_RIGHT);
        nextButton.setEnabled(false);
        StyleUtils.styleButtonPrimary(nextButton, true); // Use large button

        return StyleUtils.createNavigationPanel(backButton, nextButton);
    }

    @Override
    public JComponent getRootComponent() {
        return rootPanel;
    }

}
