package com.tractionrec.recrec.ui;

import com.tractionrec.recrec.RecRecApplication;

import javax.swing.*;
import java.awt.*;
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
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.setBorder(BorderFactory.createEmptyBorder(
            StyleUtils.SPACING_XXLARGE,
            StyleUtils.SPACING_XXLARGE,
            StyleUtils.SPACING_XXLARGE,
            StyleUtils.SPACING_XXLARGE
        ));
        contentPane.setBackground(Color.WHITE);

        // Main content card
        JPanel mainCard = createMainCard();
        mainCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPane.add(mainCard);

        StyleUtils.addVerticalSpacing(contentPane, StyleUtils.SPACING_XLARGE);

        // Button section
        JPanel buttonPanel = createButtonPanel();
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPane.add(buttonPanel);

        // Set dialog properties
        setTitle("About RecRec");
        setResizable(false);
    }

    private JPanel createMainCard() {
        JPanel card = StyleUtils.createElevatedCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(400, 300));

        // Company logo
        ImageIcon logoIcon = RecRecApplication.loadLogoIcon(48); // 48px height for about dialog
        if (logoIcon != null) {
            JLabel logoLabel = new JLabel(logoIcon);
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(logoLabel);
            StyleUtils.addVerticalSpacing(card, StyleUtils.SPACING_MEDIUM);
        }

        // Application title
        JLabel titleLabel = new JLabel("RecRec Query Tool");
        titleLabel.setFont(TypographyConstants.FONT_TITLE);
        titleLabel.setForeground(TractionRecTheme.PRIMARY_BLUE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);

        StyleUtils.addVerticalSpacing(card, StyleUtils.SPACING_MEDIUM);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Element Express Query Tool");
        subtitleLabel.setFont(TypographyConstants.FONT_SUBHEADING);
        subtitleLabel.setForeground(TractionRecTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitleLabel);

        StyleUtils.addVerticalSpacing(card, StyleUtils.SPACING_XLARGE);

        // Version information
        JPanel versionPanel = createVersionPanel();
        versionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(versionPanel);

        StyleUtils.addVerticalSpacing(card, StyleUtils.SPACING_XLARGE);

        // Company information
        JPanel companyPanel = createCompanyPanel();
        companyPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(companyPanel);

        return card;
    }

    private JPanel createVersionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // Version info
        String versionInfo = getClass().getPackage().getImplementationVersion();
        String version = versionInfo != null ? "v" + versionInfo : "Development Build";

        JLabel versionLabel = new JLabel("Version: " + version);
        versionLabel.setFont(TypographyConstants.FONT_BODY);
        versionLabel.setForeground(TractionRecTheme.TEXT_PRIMARY);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(versionLabel);

        StyleUtils.addVerticalSpacing(panel, StyleUtils.SPACING_SMALL);

        // Environment info
        String environment = RecRecApplication.isProduction() ? "Production" : "Test";
        JLabel envLabel = new JLabel("Environment: " + environment);
        envLabel.setFont(TypographyConstants.FONT_BODY);
        envLabel.setForeground(TractionRecTheme.TEXT_SECONDARY);
        envLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(envLabel);

        StyleUtils.addVerticalSpacing(panel, StyleUtils.SPACING_SMALL);

        // Java version
        String javaVersion = System.getProperty("java.version");
        JLabel javaLabel = new JLabel("Java: " + javaVersion);
        javaLabel.setFont(TypographyConstants.FONT_SMALL);
        javaLabel.setForeground(TractionRecTheme.TEXT_TERTIARY);
        javaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(javaLabel);

        return panel;
    }

    private JPanel createCompanyPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // Copyright
        JLabel copyrightLabel = new JLabel("© 2024 Traction Rec");
        copyrightLabel.setFont(TypographyConstants.FONT_BODY_BOLD);
        copyrightLabel.setForeground(TractionRecTheme.PRIMARY_BLUE);
        copyrightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(copyrightLabel);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setOpaque(false);

        panel.add(Box.createHorizontalGlue());

        buttonOK = StyleUtils.createIconButton("OK", StyleUtils.Icons.CHECK);
        StyleUtils.styleButtonPrimary(buttonOK, true); // Use large button
        panel.add(buttonOK);

        panel.add(Box.createHorizontalGlue());

        return panel;
    }

    /**
     * @noinspection ALL
     */
    public JComponent getRootComponent() {
        return contentPane;
    }

}
