package com.tractionrec.recrec;

import com.formdev.flatlaf.FlatLightLaf;
import com.jcabi.manifests.Manifests;
import com.tractionrec.recrec.ui.RecFormStack;
import com.tractionrec.recrec.ui.RecRecAbout;
import com.tractionrec.recrec.ui.RecRecStart;
import com.tractionrec.recrec.ui.TractionRecTheme;
import com.tractionrec.recrec.ui.TypographyConstants;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecRecApplication {

    public static String MANIFEST_PROD_PROPERTY_NAME = "Element-Express-Production";

    public static void main(String[] args) {
        // Configure platform-specific properties before UI setup
        configurePlatformProperties();

        // Configure DNS caching for better performance with high concurrent requests
        configureDNSCaching();

        TractionRecTheme.setup();
        RecRecState state = new RecRecState();
        JFrame applicationFrame = new JFrame("RecRec");

        // Set the taskbar icon with multiple approaches for better platform support
        setApplicationIcon(applicationFrame);

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
     * Set application icon using multiple approaches for maximum compatibility
     * @param frame The main application frame
     */
    private static void setApplicationIcon(JFrame frame) {
        Image logoImage = loadLogoImage();
        if (logoImage == null) {
            System.err.println("Warning: Could not load application logo");
            return;
        }

        try {
            // Approach 1: Traditional JFrame icon (for window title bar and Alt+Tab)
            List<Image> iconImages = loadIconImages();
            if (!iconImages.isEmpty()) {
                frame.setIconImages(iconImages);
            }

            // Approach 2: Modern Java 9+ Taskbar API (for dock/taskbar)
            if (Taskbar.isTaskbarSupported()) {
                Taskbar taskbar = Taskbar.getTaskbar();
                if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                    // Use a larger icon for taskbar (typically 64px or 128px works well)
                    Image taskbarIcon = createSquareIcon(logoImage, 128);
                    taskbar.setIconImage(taskbarIcon);
                }
            }

        } catch (Exception e) {
            System.err.println("Warning: Could not set application icon: " + e.getMessage());
            // Fallback to basic approach
            frame.setIconImage(logoImage);
        }
    }

    /**
     * Configure platform-specific properties for better application integration
     */
    private static void configurePlatformProperties() {
        // macOS specific properties
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            // Set application name in dock
            System.setProperty("apple.awt.application.name", "RecRec");

            // Enable native look and feel integration
            System.setProperty("apple.laf.useScreenMenuBar", "true");

            // Set dock icon behavior
            System.setProperty("apple.awt.application.appearance", "system");
        }

        // Windows specific properties
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            // Enable Windows-specific optimizations
            System.setProperty("sun.java2d.d3d", "true");
        }
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

        // Add company logo
        ImageIcon logoIcon = loadLogoIcon(20); // 20px height for menu bar
        if (logoIcon != null) {
            JLabel logoLabel = new JLabel(logoIcon);
            // Add vertical padding to better align with menu bar
            logoLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
            topMenuBar.add(logoLabel);
            topMenuBar.add(Box.createHorizontalStrut(8));
        }

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

    /**
     * Load multiple sizes of the company logo for taskbar/dock icons
     * @return List of Image objects in different sizes
     */
    private static List<Image> loadIconImages() {
        List<Image> iconImages = new ArrayList<>();
        Image baseImage = loadLogoImage();

        if (baseImage != null) {
            // Common icon sizes for different platforms
            int[] sizes = {16, 20, 24, 32, 40, 48, 64, 128, 256};

            for (int size : sizes) {
                // For taskbar icons, we want to maintain aspect ratio but fit within square bounds
                Image scaledImage = createSquareIcon(baseImage, size);
                iconImages.add(scaledImage);
            }
        }

        return iconImages;
    }

    /**
     * Create a square icon from the base image, maintaining aspect ratio and centering
     * @param baseImage The source image
     * @param size The target square size
     * @return Square image with the logo centered
     */
    private static Image createSquareIcon(Image baseImage, int size) {
        int originalWidth = baseImage.getWidth(null);
        int originalHeight = baseImage.getHeight(null);

        // Calculate scaling to fit within square while maintaining aspect ratio
        double scale = Math.min((double) size / originalWidth, (double) size / originalHeight);
        int scaledWidth = (int) (originalWidth * scale);
        int scaledHeight = (int) (originalHeight * scale);

        // Create a square buffered image with transparent background
        BufferedImage squareIcon = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = squareIcon.createGraphics();

        // Enable high-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Center the scaled image in the square
        int x = (size - scaledWidth) / 2;
        int y = (size - scaledHeight) / 2;

        g2d.drawImage(baseImage, x, y, scaledWidth, scaledHeight, null);
        g2d.dispose();

        return squareIcon;
    }

    /**
     * Load the company logo image from resources
     * @return Image object or null if loading fails
     */
    public static Image loadLogoImage() {
        try {
            InputStream logoStream = RecRecApplication.class.getResourceAsStream("/logo.png");
            if (logoStream != null) {
                BufferedImage logoImage = ImageIO.read(logoStream);
                logoStream.close();
                return logoImage;
            }
        } catch (IOException e) {
            System.err.println("Failed to load logo image: " + e.getMessage());
        }
        return null;
    }

    /**
     * Load the company logo as an ImageIcon scaled to the specified height
     * @param height The desired height in pixels
     * @return ImageIcon or null if loading fails
     */
    public static ImageIcon loadLogoIcon(int height) {
        Image logoImage = loadLogoImage();
        if (logoImage != null) {
            // Calculate width to maintain aspect ratio
            int originalWidth = logoImage.getWidth(null);
            int originalHeight = logoImage.getHeight(null);
            int scaledWidth = (originalWidth * height) / originalHeight;

            // Scale the image smoothly
            Image scaledImage = logoImage.getScaledInstance(scaledWidth, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        }
        return null;
    }

}
