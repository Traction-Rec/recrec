package com.tractionrec.recrec.ui;

import com.tractionrec.recrec.RecRecApplication;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RecRecAbout extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;

    public RecRecAbout() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    {
        setupUI();
    }

    private void setupUI() {
        contentPane = new JPanel();
        contentPane.setBorder( BorderFactory.createEmptyBorder(20,20,20,20) );
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        JLabel lblVersion = new JLabel();
        String versionInfo = getClass().getPackage().getImplementationVersion();
        lblVersion.setText("<html><p>" + (versionInfo != null ? "v" + versionInfo : "DEV") + "</p><p>Targeting " + (RecRecApplication.isProduction() ? "Production" : "Test") + "</p><p>&copy; Traction Rec</p></html>");
        contentPane.add(lblVersion);
        buttonOK = new JButton();
        buttonOK.setText("OK");
        contentPane.add(buttonOK);
    }

    /**
     * @noinspection ALL
     */
    public JComponent getRootComponent() {
        return contentPane;
    }

}
