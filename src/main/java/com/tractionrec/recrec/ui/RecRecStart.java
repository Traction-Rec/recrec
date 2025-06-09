package com.tractionrec.recrec.ui;

import com.tractionrec.recrec.RecRecState;
import com.tractionrec.recrec.domain.QueryBy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class RecRecStart extends RecRecForm {

    private JPasswordField inpAccountToken;
    private JTextField inpAccountId;
    private JRadioButton queryByRecordIdRadioButton;
    private JRadioButton queryByVantivIdRadioButton;
    private JRadioButton queryBySetupIdRadioButton;
    private JRadioButton queryPaymentAccountsButton;
    private JRadioButton queryBINButton;
    private JRadioButton queryAdhocSearchButton;
    private JButton nextButton;
    private JPanel rootPanel;

    public RecRecStart(RecRecState state, NavigationAction navAction) {
        super(state, navAction);
        inpAccountId.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                updateNextEnabled();
            }
        });
        inpAccountToken.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                updateNextEnabled();
            }
        });
        queryByRecordIdRadioButton.addActionListener(e -> {
            updateNextEnabled();
            updateNextFormDestination();
        });
        queryByVantivIdRadioButton.addActionListener(e -> {
            updateNextEnabled();
            updateNextFormDestination();
        });
        queryBySetupIdRadioButton.addActionListener(e -> {
            updateNextEnabled();
            updateNextFormDestination();
        });
        queryPaymentAccountsButton.addActionListener(e -> {
            updateNextEnabled();
            updateNextFormDestination();
        });
        queryBINButton.addActionListener(e -> {
            updateNextEnabled();
            updateNextFormDestination();
        });
        queryAdhocSearchButton.addActionListener(e -> {
            updateNextEnabled();
            updateNextFormDestination();
        });
        nextButton.addActionListener(e -> navigationAction.onNext());
    }

    private boolean isUserFinished() {
        return !inpAccountId.getText().isBlank() && inpAccountToken.getPassword().length > 0 && (queryByRecordIdRadioButton.isSelected() || queryBySetupIdRadioButton.isSelected() || queryByVantivIdRadioButton.isSelected() || queryPaymentAccountsButton.isSelected() || queryBINButton.isSelected() || queryAdhocSearchButton.isSelected());
    }

    @Override
    void willDisplay() {
        this.inpAccountId.setText(state.accountId);
        this.inpAccountToken.setText(state.accountToken);
        this.queryByRecordIdRadioButton.setSelected(state.queryMode == QueryBy.RECORD_ID);
        this.queryByVantivIdRadioButton.setSelected(state.queryMode == QueryBy.VANTIV_ID);
        this.queryBySetupIdRadioButton.setSelected(state.queryMode == QueryBy.SETUP_ID);
        this.queryPaymentAccountsButton.setSelected(state.queryMode == QueryBy.PAYMENT_ACCOUNT);
        this.queryBINButton.setSelected(state.queryMode == QueryBy.BIN_QUERY);
        this.queryAdhocSearchButton.setSelected(state.queryMode == QueryBy.ADHOC_SEARCH);
    }

    @Override
    public void willHide() {
        state.accountId = inpAccountId.getText();
        state.accountToken = String.valueOf(inpAccountToken.getPassword());
        if (queryByRecordIdRadioButton.isSelected()) state.queryMode = QueryBy.RECORD_ID;
        if (queryByVantivIdRadioButton.isSelected()) state.queryMode = QueryBy.VANTIV_ID;
        if (queryBySetupIdRadioButton.isSelected()) state.queryMode = QueryBy.SETUP_ID;
        if (queryPaymentAccountsButton.isSelected()) state.queryMode = QueryBy.PAYMENT_ACCOUNT;
        if (queryBINButton.isSelected()) state.queryMode = QueryBy.BIN_QUERY;
        if (queryAdhocSearchButton.isSelected()) state.queryMode = QueryBy.ADHOC_SEARCH;
    }

    @Override
    public RecRecForm whatIsNext() {
        // Route to different forms based on current radio button selection
        if (queryAdhocSearchButton != null && queryAdhocSearchButton.isSelected()) {
            return new RecRecAdhocInput(state, navigationAction);
        } else {
            return new RecRecFileInput(state, navigationAction);
        }
    }

    private void updateNextEnabled() {
        nextButton.setEnabled(isUserFinished());
    }

    private void updateNextFormDestination() {
        RecRecForm nextForm;
        if (queryAdhocSearchButton != null && queryAdhocSearchButton.isSelected()) {
            nextForm = new RecRecAdhocInput(state, navigationAction);
        } else {
            nextForm = new RecRecFileInput(state, navigationAction);
        }
        navigationAction.updateNextForm(nextForm);
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
        JLabel titleLabel = StyleUtils.createFormTitle("RecRec Query Tool");
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Configure your API credentials and select a query type");
        subtitleLabel.setFont(TypographyConstants.FONT_SMALL);
        subtitleLabel.setForeground(TractionRecTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(subtitleLabel);
        StyleUtils.addVerticalSpacing(rootPanel, StyleUtils.SPACING_XXXLARGE);

        // Credentials Section
        JPanel credentialsSection = createCredentialsSection();
        credentialsSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(credentialsSection);
        StyleUtils.addVerticalSpacing(rootPanel, StyleUtils.SPACING_XLARGE);

        // Query Type Section
        JPanel queryTypeSection = createQueryTypeSection();
        queryTypeSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(queryTypeSection);
        StyleUtils.addVerticalSpacing(rootPanel, StyleUtils.SPACING_XXLARGE);

        // Navigation Section
        JPanel navigationSection = createNavigationSection();
        navigationSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(navigationSection);
    }

    private JPanel createCredentialsSection() {
        JPanel section = StyleUtils.createElevatedCard();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

        // Section title with icon
        JLabel sectionTitle = StyleUtils.createSectionTitle(StyleUtils.Icons.SETTINGS + "  API Credentials");
        section.add(sectionTitle);

        // Section description
        JLabel sectionDesc = new JLabel("Enter your Element Express API credentials");
        sectionDesc.setFont(TypographyConstants.FONT_CAPTION);
        sectionDesc.setForeground(TractionRecTheme.TEXT_SECONDARY);
        sectionDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(sectionDesc);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_LARGE);

        // Account ID field
        inpAccountId = new JTextField();
        inpAccountId.setToolTipText("Your Element Express Account ID");
        StyleUtils.styleTextField(inpAccountId);
        inpAccountId.setMaximumSize(new Dimension(350, StyleUtils.INPUT_HEIGHT));
        JPanel accountIdRow = StyleUtils.createFormRow("Account ID", inpAccountId);
        section.add(accountIdRow);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_LARGE);

        // Account Token field
        inpAccountToken = new JPasswordField();
        inpAccountToken.setToolTipText("Your Element Express Account Token (kept secure)");
        StyleUtils.stylePasswordField(inpAccountToken);
        inpAccountToken.setMaximumSize(new Dimension(350, StyleUtils.INPUT_HEIGHT));
        JPanel accountTokenRow = StyleUtils.createFormRow("Account Token", inpAccountToken);
        section.add(accountTokenRow);

        return section;
    }

    private JPanel createQueryTypeSection() {
        JPanel section = StyleUtils.createElevatedCard();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

        // Section title with icon
        JLabel sectionTitle = StyleUtils.createSectionTitle(StyleUtils.Icons.SEARCH + "  Query Type");
        section.add(sectionTitle);

        // Section description
        JLabel sectionDesc = new JLabel("Choose the type of data you want to query");
        sectionDesc.setFont(TypographyConstants.FONT_CAPTION);
        sectionDesc.setForeground(TractionRecTheme.TEXT_SECONDARY);
        sectionDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(sectionDesc);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_LARGE);

        // Create radio buttons
        queryByRecordIdRadioButton = new JRadioButton("Query by record ID");
        queryByRecordIdRadioButton.setToolTipText("Salesforce record ID used as ReferenceNumber in WorldPay");
        StyleUtils.styleRadioButton(queryByRecordIdRadioButton);

        queryByVantivIdRadioButton = new JRadioButton("Query by Vantiv ID");
        queryByVantivIdRadioButton.setToolTipText("WorldPay transaction ID, numeric, can be found on Salesforce record");
        StyleUtils.styleRadioButton(queryByVantivIdRadioButton);

        queryBySetupIdRadioButton = new JRadioButton("Query by setup ID");
        queryBySetupIdRadioButton.setToolTipText("Salesforce record's Setup ID - used in hosted payment form credit card payments");
        StyleUtils.styleRadioButton(queryBySetupIdRadioButton);

        queryPaymentAccountsButton = new JRadioButton("Query payment accounts by token");
        queryPaymentAccountsButton.setToolTipText("Payment tokens found on stored account records. Token column name is still ID");
        StyleUtils.styleRadioButton(queryPaymentAccountsButton);

        queryBINButton = new JRadioButton("Query BIN by payment token");
        queryBINButton.setToolTipText("Payment tokens found on stored account records. Token column name is still ID");
        StyleUtils.styleRadioButton(queryBINButton);

        queryAdhocSearchButton = new JRadioButton("Ad-hoc transaction search");
        queryAdhocSearchButton.setToolTipText("Search transactions by date range, type, amount, or approval number without CSV input");
        StyleUtils.styleRadioButton(queryAdhocSearchButton);

        // Group radio buttons
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(queryByRecordIdRadioButton);
        buttonGroup.add(queryByVantivIdRadioButton);
        buttonGroup.add(queryBySetupIdRadioButton);
        buttonGroup.add(queryPaymentAccountsButton);
        buttonGroup.add(queryBINButton);
        buttonGroup.add(queryAdhocSearchButton);

        // Add radio buttons to section with spacing
        JRadioButton[] buttons = {
            queryByRecordIdRadioButton,
            queryByVantivIdRadioButton,
            queryBySetupIdRadioButton,
            queryPaymentAccountsButton,
            queryBINButton,
            queryAdhocSearchButton
        };

        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            section.add(buttons[i]);
            if (i < buttons.length - 1) {
                StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_SMALL);
            }
        }

        return section;
    }

    private JPanel createNavigationSection() {
        nextButton = StyleUtils.createIconButton("Next", StyleUtils.Icons.ARROW_RIGHT);
        nextButton.setEnabled(false);
        StyleUtils.styleButtonPrimary(nextButton, true); // Use large button

        return StyleUtils.createNavigationPanel(nextButton);
    }

    @Override
    public JComponent getRootComponent() {
        return rootPanel;
    }

}
