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
        });
        queryByVantivIdRadioButton.addActionListener(e -> {
            updateNextEnabled();
        });
        queryBySetupIdRadioButton.addActionListener(e -> {
            updateNextEnabled();
        });
        queryPaymentAccountsButton.addActionListener(e -> {
            updateNextEnabled();
        });
        queryBINButton.addActionListener(e -> {
            updateNextEnabled();
        });
        nextButton.addActionListener(e -> navigationAction.onNext());
    }

    private boolean isUserFinished() {
        return !inpAccountId.getText().isBlank() && inpAccountToken.getPassword().length > 0 && (queryByRecordIdRadioButton.isSelected() || queryBySetupIdRadioButton.isSelected() || queryByVantivIdRadioButton.isSelected() || queryPaymentAccountsButton.isSelected() || queryBINButton.isSelected());
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
    }

    @Override
    public RecRecForm whatIsNext() {
        return new RecRecFileInput(state, navigationAction);
    }

    private void updateNextEnabled() {
        nextButton.setEnabled(isUserFinished());
    }

    protected void setupUI() {
        rootPanel = new JPanel();
        rootPanel.setBorder( BorderFactory.createEmptyBorder(20,20,20,20) );
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        rootPanel.setAlignmentX( Component.LEFT_ALIGNMENT );
        rootPanel.setAlignmentY( Component.TOP_ALIGNMENT );

        JPanel inputsContainer = new JPanel();
        inputsContainer.setLayout(new FlowLayout(FlowLayout.LEFT));
        inputsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel inputLabelPanel = new JPanel();
        inputLabelPanel.setLayout(new BoxLayout(inputLabelPanel, BoxLayout.Y_AXIS));

        final JLabel lblAccountId = new JLabel();
        lblAccountId.setText("Account Id");
        inputLabelPanel.add(lblAccountId);
        final JLabel lblAccountToken = new JLabel();
        lblAccountToken.setText("Account Token");
        inputLabelPanel.add(lblAccountToken);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inpAccountId = new JTextField();
        inputPanel.add(inpAccountId, BorderLayout.CENTER);
        inpAccountToken = new JPasswordField();
        inputPanel.add(inpAccountToken, BorderLayout.CENTER);
        inputPanel.setPreferredSize(new Dimension(200, inputPanel.getPreferredSize().height));

        lblAccountToken.setLabelFor(inpAccountToken);
        lblAccountId.setLabelFor(inpAccountId);

        inputsContainer.add(inputLabelPanel);
        inputsContainer.add(inputPanel);
        inputsContainer.setMaximumSize(new Dimension(inputsContainer.getMaximumSize().width, 0));
        rootPanel.add(inputsContainer);

        final JPanel buttonGroupPanel = new JPanel();
        buttonGroupPanel.setLayout(new BoxLayout(buttonGroupPanel, BoxLayout.Y_AXIS));
        buttonGroupPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        queryByRecordIdRadioButton = new JRadioButton();
        queryByRecordIdRadioButton.setText("Query by record id");
        queryByRecordIdRadioButton.setToolTipText("Salesforce record id used as ReferenceNumber in WorldPay");
        buttonGroupPanel.add(queryByRecordIdRadioButton);
        queryByVantivIdRadioButton = new JRadioButton();
        queryByVantivIdRadioButton.setText("Query by vantiv id");
        queryByVantivIdRadioButton.setToolTipText("WorldPay transaction id, numeric, can be found on salesforce record");
        buttonGroupPanel.add(queryByVantivIdRadioButton);
        queryBySetupIdRadioButton = new JRadioButton();
        queryBySetupIdRadioButton.setText("Query by setup id");
        queryBySetupIdRadioButton.setToolTipText("Salesforce rec's Setup id - used in hosted payment form credit card payments");
        buttonGroupPanel.add(queryBySetupIdRadioButton);
        queryPaymentAccountsButton = new JRadioButton();
        queryPaymentAccountsButton.setText("Query payment accounts by token");
        queryPaymentAccountsButton.setToolTipText("Payment tokens found on stored account records. Token column name is still id");
        buttonGroupPanel.add(queryPaymentAccountsButton);
        queryBINButton = new JRadioButton();
        queryBINButton.setText("Query BIN by payment token");
        queryBINButton.setToolTipText("Payment tokens found on stored account records. Token column name is still id");
        buttonGroupPanel.add(queryBINButton);
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(queryByRecordIdRadioButton);
        buttonGroup.add(queryByVantivIdRadioButton);
        buttonGroup.add(queryBySetupIdRadioButton);
        buttonGroup.add(queryPaymentAccountsButton);
        buttonGroup.add(queryBINButton);
        rootPanel.add(buttonGroupPanel);

        JPanel navigationPanel = new JPanel();
        navigationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        navigationPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        nextButton = new JButton();
        nextButton.setEnabled(false);
        nextButton.setText("Next >");
        navigationPanel.add(nextButton);
        rootPanel.add(navigationPanel);
    }

    @Override
    public JComponent getRootComponent() {
        return rootPanel;
    }

}
