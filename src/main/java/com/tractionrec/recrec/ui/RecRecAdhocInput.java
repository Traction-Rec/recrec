package com.tractionrec.recrec.ui;

import com.tractionrec.recrec.RecRecState;
import com.tractionrec.recrec.domain.AdhocQueryItem;

import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.optionalusertools.DateTimeChangeListener;
import com.github.lgooddatepicker.zinternaltools.DateTimeChangeEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Form for entering ad-hoc search criteria without requiring a CSV file.
 * Allows users to search by date range, transaction type, amount, and approval number.
 */
public class RecRecAdhocInput extends RecRecForm {

    // UI Components
    private JPanel rootPanel;
    private JTextField merchantIdField;
    private DateTimePicker dateBeginPicker;
    private DateTimePicker dateEndPicker;
    private JComboBox<String> transactionTypeCombo;
    private JTextField transactionAmountField;
    private JTextField approvalNumberField;
    private JButton nextButton;
    private JButton backButton;
    private JLabel validationLabel;

    // Date format for user input
    private static final DateTimeFormatter USER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Common transaction types
    private static final String[] TRANSACTION_TYPES = {
            "", // Empty option
            "checksale",
            "creditcardsale",
            "creditcardreturn",
            "creditcardvoid",
            "creditcardadjustment",
            "debitcardsale",
            "debitcardreturn",
            "debitcardvoid"
    };

    public RecRecAdhocInput(RecRecState state, NavigationAction navigationAction) {
        super(state, navigationAction);
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
        rootPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Form Title with subtitle
        JLabel titleLabel = StyleUtils.createFormTitle("Ad-hoc Transaction Search");
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Search transactions by date range, type, amount, or approval number");
        subtitleLabel.setFont(TypographyConstants.FONT_SMALL);
        subtitleLabel.setForeground(TractionRecTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(subtitleLabel);
        StyleUtils.addVerticalSpacing(rootPanel, StyleUtils.SPACING_XXXLARGE);

        // Search Criteria Section
        JPanel searchSection = createSearchCriteriaSection();
        searchSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(searchSection);
        StyleUtils.addVerticalSpacing(rootPanel, StyleUtils.SPACING_XLARGE);

        // Validation message
        validationLabel = new JLabel(" ");
        validationLabel.setForeground(TractionRecTheme.ERROR_RED);
        validationLabel.setFont(TypographyConstants.FONT_CAPTION);
        validationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(validationLabel);
        StyleUtils.addVerticalSpacing(rootPanel, StyleUtils.SPACING_LARGE);

        // Navigation Section
        JPanel navigationSection = createNavigationSection();
        navigationSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(navigationSection);
    }

    private JPanel createSearchCriteriaSection() {
        JPanel section = StyleUtils.createElevatedCard();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

        // Section title with icon
        JLabel sectionTitle = StyleUtils.createSectionTitle(StyleUtils.Icons.SEARCH + "  Search Criteria");
        section.add(sectionTitle);

        // Section description
        JLabel sectionDesc = new JLabel("Enter at least one search criteria along with the required Merchant ID");
        sectionDesc.setFont(TypographyConstants.FONT_CAPTION);
        sectionDesc.setForeground(TractionRecTheme.TEXT_SECONDARY);
        sectionDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(sectionDesc);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_LARGE);

        // Merchant ID field (required)
        merchantIdField = new JTextField();
        merchantIdField.setToolTipText("Required: Your Element Express Merchant ID (Acceptor ID)");
        StyleUtils.styleTextField(merchantIdField);
        merchantIdField.setMaximumSize(new Dimension(350, StyleUtils.INPUT_HEIGHT));
        JPanel merchantRow = StyleUtils.createFormRow("Merchant ID *", merchantIdField);
        section.add(merchantRow);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_MEDIUM);

        // Date range fields with modern date/time pickers
        JPanel dateBeginPanel = createDateTimePickerPanel("Date Begin");
        section.add(dateBeginPanel);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_SMALL);

        JPanel dateEndPanel = createDateTimePickerPanel("Date End");
        section.add(dateEndPanel);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_MEDIUM);

        // Transaction type dropdown
        transactionTypeCombo = new JComboBox<>(TRANSACTION_TYPES);
        transactionTypeCombo.setEditable(true);
        transactionTypeCombo.setToolTipText("Select or enter a transaction type (e.g., checksale, creditcardsale)");
        transactionTypeCombo.setFont(TypographyConstants.FONT_INPUT);
        transactionTypeCombo.setPreferredSize(new Dimension(350, StyleUtils.INPUT_HEIGHT));
        transactionTypeCombo.setMaximumSize(new Dimension(350, StyleUtils.INPUT_HEIGHT));
        JPanel typeRow = StyleUtils.createFormRow("Transaction Type", transactionTypeCombo);
        section.add(typeRow);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_SMALL);

        // Transaction amount field
        transactionAmountField = new JTextField();
        transactionAmountField.setToolTipText("Transaction amount in decimal format (e.g., 25.00)");
        StyleUtils.styleTextField(transactionAmountField);
        transactionAmountField.setMaximumSize(new Dimension(350, StyleUtils.INPUT_HEIGHT));
        JPanel amountRow = StyleUtils.createFormRow("Transaction Amount", transactionAmountField);
        section.add(amountRow);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_SMALL);

        // Approval number field
        approvalNumberField = new JTextField();
        approvalNumberField.setToolTipText("Approval number from the transaction");
        StyleUtils.styleTextField(approvalNumberField);
        approvalNumberField.setMaximumSize(new Dimension(350, StyleUtils.INPUT_HEIGHT));
        JPanel approvalRow = StyleUtils.createFormRow("Approval Number", approvalNumberField);
        section.add(approvalRow);

        return section;
    }

    private JPanel createDateTimePickerPanel(String labelText) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Label
        JLabel label = new JLabel(labelText);
        label.setFont(TypographyConstants.FONT_LABEL);
        label.setForeground(TractionRecTheme.TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        StyleUtils.addVerticalSpacing(panel, StyleUtils.SPACING_TINY);

        // Create DateTimePicker
        DateTimePicker dateTimePicker = new DateTimePicker();

        // Configure the picker with more generous sizing
        dateTimePicker.setPreferredSize(new Dimension(350, StyleUtils.INPUT_HEIGHT + 4));
        dateTimePicker.setMaximumSize(new Dimension(350, StyleUtils.INPUT_HEIGHT + 4));
        dateTimePicker.setMinimumSize(new Dimension(350, StyleUtils.INPUT_HEIGHT + 4));

        // Set smart defaults
        if (labelText.equals("Date Begin")) {
            dateBeginPicker = dateTimePicker;
            // Default to 7 days ago
            dateTimePicker.setDateTimePermissive(LocalDateTime.now().minusDays(7));
        } else if (labelText.equals("Date End")) {
            dateEndPicker = dateTimePicker;
            // Default to current date/time
            dateTimePicker.setDateTimePermissive(LocalDateTime.now());
        }

        // Style the picker to match application theme
        styleeDateTimePicker(dateTimePicker);

        // Create container panel for proper alignment with padding
        JPanel pickerContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, StyleUtils.SPACING_SMALL));
        pickerContainer.setBackground(Color.WHITE);
        pickerContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        pickerContainer.setPreferredSize(new Dimension(400, StyleUtils.INPUT_HEIGHT + 12));
        pickerContainer.add(dateTimePicker);

        panel.add(pickerContainer);

        return panel;
    }

    /**
     * Style the DateTimePicker to match the application theme
     */
    private void styleeDateTimePicker(DateTimePicker picker) {
        // Set font to match other inputs
        picker.getDatePicker().getComponentDateTextField().setFont(TypographyConstants.FONT_INPUT);
        picker.getTimePicker().getComponentTimeTextField().setFont(TypographyConstants.FONT_INPUT);

        // Apply consistent styling with proper sizing
        picker.getDatePicker().getComponentDateTextField().setBorder(StyleUtils.createInputBorder());
        picker.getTimePicker().getComponentTimeTextField().setBorder(StyleUtils.createInputBorder());

        // Set background colors
        picker.getDatePicker().getComponentDateTextField().setBackground(Color.WHITE);
        picker.getTimePicker().getComponentTimeTextField().setBackground(Color.WHITE);

        // Ensure proper sizing for individual components
        picker.getDatePicker().setPreferredSize(new Dimension(140, StyleUtils.INPUT_HEIGHT));
        picker.getTimePicker().setPreferredSize(new Dimension(100, StyleUtils.INPUT_HEIGHT));

        // Set minimum sizes to prevent squishing
        picker.getDatePicker().setMinimumSize(new Dimension(140, StyleUtils.INPUT_HEIGHT));
        picker.getTimePicker().setMinimumSize(new Dimension(100, StyleUtils.INPUT_HEIGHT));
    }

    private JPanel createNavigationSection() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        backButton = StyleUtils.createIconButton("Back", StyleUtils.Icons.ARROW_LEFT);
        StyleUtils.styleButtonSecondary(backButton);

        nextButton = StyleUtils.createIconButton("Search", StyleUtils.Icons.SEARCH);
        nextButton.setEnabled(false);
        StyleUtils.styleButtonPrimary(nextButton, true); // Use large button

        buttonPanel.add(backButton);
        buttonPanel.add(Box.createHorizontalStrut(StyleUtils.SPACING_MEDIUM));
        buttonPanel.add(nextButton);

        return buttonPanel;
    }

    private void setupEventHandlers() {
        // Validation on field changes
        KeyAdapter validationListener = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validateForm();
            }
        };

        merchantIdField.addKeyListener(validationListener);
        transactionAmountField.addKeyListener(validationListener);
        approvalNumberField.addKeyListener(validationListener);

        // Add change listeners for date/time pickers
        DateTimeChangeListener dateTimeChangeListener = new DateTimeChangeListener() {
            @Override
            public void dateOrTimeChanged(DateTimeChangeEvent event) {
                validateForm();
            }
        };

        dateBeginPicker.addDateTimeChangeListener(dateTimeChangeListener);
        dateEndPicker.addDateTimeChangeListener(dateTimeChangeListener);

        transactionTypeCombo.addActionListener(e -> validateForm());

        // Navigation
        backButton.addActionListener(e -> navigationAction.onBack());
        nextButton.addActionListener(e -> {
            if (saveFormData()) {
                navigationAction.onNext();
            }
        });
    }

    private void validateForm() {
        String merchantId = merchantIdField.getText().trim();
        String transactionType = getSelectedTransactionType();
        String amount = transactionAmountField.getText().trim();
        String approvalNumber = approvalNumberField.getText().trim();

        // Clear previous validation message
        validationLabel.setText(" ");

        // Merchant ID is required
        if (merchantId.isEmpty()) {
            validationLabel.setText("Merchant ID is required");
            nextButton.setEnabled(false);
            return;
        }

        // Check if date range is provided
        LocalDateTime dateBegin = dateBeginPicker.getDateTimePermissive();
        LocalDateTime dateEnd = dateEndPicker.getDateTimePermissive();
        boolean hasDateBegin = dateBegin != null;
        boolean hasDateEnd = dateEnd != null;

        // Validate date range logic
        if (hasDateBegin && !hasDateEnd) {
            validationLabel.setText("Date End is required when Date Begin is specified");
            nextButton.setEnabled(false);
            return;
        }

        if (!hasDateBegin && hasDateEnd) {
            validationLabel.setText("Date Begin is required when Date End is specified");
            nextButton.setEnabled(false);
            return;
        }

        // Validate date range order
        if (hasDateBegin && hasDateEnd && dateBegin.isAfter(dateEnd)) {
            validationLabel.setText("Date Begin must be before Date End");
            nextButton.setEnabled(false);
            return;
        }

        // At least one search criteria must be provided
        boolean hasDateRange = hasDateBegin && hasDateEnd;
        boolean hasSearchCriteria = hasDateRange ||
                                   !transactionType.isEmpty() ||
                                   !amount.isEmpty() ||
                                   !approvalNumber.isEmpty();

        if (!hasSearchCriteria) {
            validationLabel.setText("At least one search criteria must be provided");
            nextButton.setEnabled(false);
            return;
        }

        // Validate amount format if provided
        if (!amount.isEmpty()) {
            try {
                BigDecimal decimal = new BigDecimal(amount);
                if (decimal.compareTo(BigDecimal.ZERO) < 0) {
                    validationLabel.setText("Transaction amount must be positive");
                    nextButton.setEnabled(false);
                    return;
                }
            } catch (NumberFormatException e) {
                validationLabel.setText("Invalid amount format");
                nextButton.setEnabled(false);
                return;
            }
        }

        // All validation passed
        nextButton.setEnabled(true);
    }

    private String getSelectedTransactionType() {
        Object selected = transactionTypeCombo.getSelectedItem();
        return selected != null ? selected.toString().trim() : "";
    }

    private boolean saveFormData() {
        try {
            String merchantId = merchantIdField.getText().trim();
            String transactionType = getSelectedTransactionType();
            String amount = transactionAmountField.getText().trim();
            String approvalNumber = approvalNumberField.getText().trim();

            LocalDateTime beginDateTime = dateBeginPicker.getDateTimePermissive();
            LocalDateTime endDateTime = dateEndPicker.getDateTimePermissive();
            BigDecimal amountDecimal = amount.isEmpty() ? null : new BigDecimal(amount);

            AdhocQueryItem adhocQuery = new AdhocQueryItem(
                    merchantId,
                    beginDateTime,
                    endDateTime,
                    transactionType.isEmpty() ? null : transactionType,
                    amountDecimal,
                    approvalNumber.isEmpty() ? null : approvalNumber
            );

            state.adhocQueryItem = adhocQuery;
            return true;

        } catch (Exception e) {
            System.err.println("ERROR: Failed to save form data: " + e.getMessage());
            e.printStackTrace();
            validationLabel.setText("Error saving form data: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void willDisplay() {
        // Clear any previous query results when returning to input form
        // This ensures a fresh start for each new search
        state.queryResults = null;

        // Restore form data if available
        if (state.adhocQueryItem != null) {
            AdhocQueryItem item = state.adhocQueryItem;
            merchantIdField.setText(item.merchant() != null ? item.merchant() : "");

            if (item.transactionDateTimeBegin() != null) {
                dateBeginPicker.setDateTimePermissive(item.transactionDateTimeBegin());
            }

            if (item.transactionDateTimeEnd() != null) {
                dateEndPicker.setDateTimePermissive(item.transactionDateTimeEnd());
            }

            if (item.transactionType() != null) {
                transactionTypeCombo.setSelectedItem(item.transactionType());
            }

            if (item.transactionAmount() != null) {
                transactionAmountField.setText(item.transactionAmount().toString());
            }

            if (item.approvalNumber() != null) {
                approvalNumberField.setText(item.approvalNumber());
            }
        }

        validateForm();
    }

    @Override
    public void willHide() {
        // Form data is saved when Next is clicked
    }

    @Override
    public RecRecForm whatIsNext() {
        return new RecRecAdhocProgress(state, navigationAction);
    }

    @Override
    public JComponent getRootComponent() {
        return rootPanel;
    }
}
