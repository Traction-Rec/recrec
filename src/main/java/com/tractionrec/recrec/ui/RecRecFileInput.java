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
            int resultCode = fileChooser.showDialog(rootPanel, "Select");
            if (resultCode == JFileChooser.APPROVE_OPTION) {
                state.inputFile = fileChooser.getSelectedFile();
                chooseFileButton.setText("Selected: " + state.inputFile.getName());
                updateNextEnabled();
            }
        });
    }

    @Override
    public void willDisplay() {
        if (state.inputFile != null) {
            chooseFileButton.setText("Selected: " + state.inputFile.getName());
        }
        switch (state.queryMode) {
            case SETUP_ID -> lblQueryingBy.setText("You are querying by setup id.");
            case RECORD_ID -> lblQueryingBy.setText("You are querying by record id.");
            case VANTIV_ID -> lblQueryingBy.setText("You are querying by vantiv id.");
            case PAYMENT_ACCOUNT -> lblQueryingBy.setText("You are querying by payment account token.");
        }
        updateNextEnabled();
    }

    @Override
    public RecRecForm whatIsNext() {
        return new RecRecRunning(state, navigationAction);
    }

    private boolean isUserFinished() {
        return state.inputFile != null;
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

        lblQueryingBy = new JLabel();
        lblQueryingBy.setText("You are querying by...");
        rootPanel.add(lblQueryingBy);
        rootPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        final JLabel lblHelp = new JLabel();
        lblHelp.setText("<html><p>Please choose a csv file where each row has two columns \"Merchant\" and \"Id\".</p></html>");
        rootPanel.add(lblHelp);
        rootPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        chooseFileButton = new JButton();
        chooseFileButton.setText("Choose file...");
        rootPanel.add(chooseFileButton);

        JPanel navigationPanel = new JPanel();
        navigationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        navigationPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        backButton = new JButton();
        backButton.setText("< Back");
        navigationPanel.add(backButton);
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
