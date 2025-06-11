package com.tractionrec.recrec;

import com.formdev.flatlaf.FlatLightLaf;
import com.jcabi.manifests.Manifests;
import com.tractionrec.recrec.ui.RecFormStack;
import com.tractionrec.recrec.ui.RecRecAbout;
import com.tractionrec.recrec.ui.RecRecStart;
import com.tractionrec.recrec.ui.TractionRecTheme;
import com.tractionrec.recrec.ui.TypographyConstants;

import javax.swing.*;

public class RecRecApplication {

    public static String MANIFEST_PROD_PROPERTY_NAME = "Element-Express-Production";

    public static void main(String[] args) {
        // Configure DNS caching for better performance with high concurrent requests
        configureDNSCaching();

        TractionRecTheme.setup();
        RecRecState state = new RecRecState();
        JFrame applicationFrame = new JFrame("RecRec");
        applicationFrame.setJMenuBar(buildMenuBar());
        RecFormStack stack = new RecFormStack(applicationFrame);
        RecRecStart startForm = new RecRecStart(state, stack);
        stack.setInitialForm(startForm);
        stack.displayInitial();
    }

    public static boolean isDevEnv() {
        return !Manifests.exists(MANIFEST_PROD_PROPERTY_NAME);
    }

    public static boolean isProduction() {
        return Manifests.exists(MANIFEST_PROD_PROPERTY_NAME) && "true".equals(Manifests.read(MANIFEST_PROD_PROPERTY_NAME));
    }

    /**
     * Configure DNS caching to prevent DNS resolution issues with high concurrent requests
     */
    private static void configureDNSCaching() {
        // Cache successful DNS lookups for 5 minutes (300 seconds)
        System.setProperty("networkaddress.cache.ttl", "300");

        // Cache failed DNS lookups for 10 seconds to allow quick retry
        System.setProperty("networkaddress.cache.negative.ttl", "10");

        // Prefer IPv4 to avoid potential IPv6 resolution issues
        System.setProperty("java.net.preferIPv4Stack", "true");

        // Set connection pool size for HttpClient (moved from main method)
        System.setProperty("jdk.httpclient.connectionPoolSize", "100");
    }

    private static JMenuBar buildMenuBar() {
        JMenuBar topMenuBar = new JMenuBar();

        // Force the background color to ensure it's applied
        topMenuBar.setBackground(TractionRecTheme.PRIMARY_BLUE);
        topMenuBar.setOpaque(true);
        topMenuBar.setBorderPainted(false);

        topMenuBar.add(Box.createHorizontalStrut(10));

        JLabel titleLabel = new JLabel("RecRec");
        titleLabel.setForeground(TractionRecTheme.TEXT_INVERSE);
        titleLabel.setFont(TypographyConstants.FONT_SUBHEADING);
        topMenuBar.add(titleLabel);

        topMenuBar.add(Box.createHorizontalStrut(10));

        JLabel separator = new JLabel("|");
        separator.setForeground(TractionRecTheme.TEXT_INVERSE);
        topMenuBar.add(separator);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setForeground(TractionRecTheme.TEXT_INVERSE);
        helpMenu.setBackground(TractionRecTheme.PRIMARY_BLUE);
        helpMenu.setOpaque(true);
        helpMenu.setBorderPainted(false);

        // Force white text color for all states - multiple approaches
        helpMenu.putClientProperty("Menu.foreground", TractionRecTheme.TEXT_INVERSE);
        helpMenu.putClientProperty("Menu.selectionForeground", TractionRecTheme.TEXT_INVERSE);
        helpMenu.putClientProperty("Menu.hoverForeground", TractionRecTheme.TEXT_INVERSE);

        // Override the UI delegate to force white text
        SwingUtilities.invokeLater(() -> {
            helpMenu.setForeground(TractionRecTheme.TEXT_INVERSE);
            helpMenu.repaint();
        });

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> {
            RecRecAbout dialog = new RecRecAbout();
            dialog.pack();
            dialog.setVisible(true);
        });
        helpMenu.add(aboutItem);
        topMenuBar.add(helpMenu);

        return topMenuBar;
    }

}
