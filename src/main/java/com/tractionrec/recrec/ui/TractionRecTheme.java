package com.tractionrec.recrec.ui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Custom theme configuration for Traction Rec branding
 */
public class TractionRecTheme {

    // Brand Colors
    public static final Color PRIMARY_BLUE = new Color(0x1E3A8A);
    public static final Color SECONDARY_BLUE = new Color(0x3B82F6);
    public static final Color SUCCESS_GREEN = new Color(0x10B981);
    public static final Color WARNING_ORANGE = new Color(0xF59E0B);
    public static final Color ERROR_RED = new Color(0xEF4444);
    public static final Color NEUTRAL_GRAY = new Color(0x6B7280);
    public static final Color LIGHT_GRAY = new Color(0xF9FAFB);
    public static final Color DARK_GRAY = new Color(0x374151);

    // Text Colors
    public static final Color TEXT_PRIMARY = new Color(0x111827);
    public static final Color TEXT_SECONDARY = NEUTRAL_GRAY;
    public static final Color TEXT_TERTIARY = new Color(0x9CA3AF);
    public static final Color TEXT_INVERSE = Color.WHITE;

    /**
     * Initialize and apply the Traction Rec theme
     */
    public static void setup() {
        try {
            // Load custom theme properties
            Properties themeProps = loadThemeProperties();

            // Apply the base FlatLaf theme
            FlatLightLaf.setup();

            // Apply custom properties
            UIManager.put("Application.name", "RecRec");

            // Apply theme properties to UIManager
            for (String key : themeProps.stringPropertyNames()) {
                String value = themeProps.getProperty(key);
                if (value.startsWith("#")) {
                    // Color value
                    try {
                        Color color = Color.decode(value);
                        UIManager.put(key, color);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid color value for " + key + ": " + value);
                    }
                } else if (value.matches("\\d+")) {
                    // Integer value
                    UIManager.put(key, Integer.parseInt(value));
                } else if (value.matches("\\d+,\\d+,\\d+,\\d+")) {
                    // Insets value (top,left,bottom,right)
                    String[] parts = value.split(",");
                    Insets insets = new Insets(
                        Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3])
                    );
                    UIManager.put(key, insets);
                } else {
                    // String value
                    UIManager.put(key, value);
                }
            }

            // Additional custom configurations
            configureGlobalProperties();

            // Force menu bar colors to override FlatLaf defaults
            forceMenuBarColors();

        } catch (Exception e) {
            System.err.println("Failed to load Traction Rec theme, falling back to default: " + e.getMessage());
            FlatLightLaf.setup();
        }
    }

    /**
     * Load theme properties from resources
     */
    private static Properties loadThemeProperties() throws IOException {
        Properties props = new Properties();

        try (InputStream is = TractionRecTheme.class.getResourceAsStream("/themes/TractionRecTheme.properties")) {
            if (is != null) {
                props.load(is);

                // Resolve color variables (simple implementation)
                resolveColorVariables(props);
            } else {
                System.err.println("Theme properties file not found");
            }
        }

        return props;
    }

    /**
     * Simple variable resolution for theme properties
     */
    private static void resolveColorVariables(Properties props) {
        // First pass: collect variable definitions
        Properties variables = new Properties();
        for (String key : props.stringPropertyNames()) {
            if (key.startsWith("@")) {
                variables.setProperty(key, props.getProperty(key));
            }
        }

        // Second pass: resolve variables in values
        for (String key : props.stringPropertyNames()) {
            String value = props.getProperty(key);
            if (value.startsWith("@")) {
                String resolvedValue = variables.getProperty(value);
                if (resolvedValue != null) {
                    props.setProperty(key, resolvedValue);
                }
            }
        }

        // Remove variable definitions from final properties
        variables.stringPropertyNames().forEach(props::remove);
    }

    /**
     * Force menu bar colors to override FlatLaf defaults
     */
    private static void forceMenuBarColors() {
        // Force menu bar colors that FlatLaf sometimes ignores
        UIManager.put("MenuBar.background", PRIMARY_BLUE);
        UIManager.put("MenuBar.foreground", TEXT_INVERSE);
        UIManager.put("Menu.background", PRIMARY_BLUE);
        UIManager.put("Menu.foreground", TEXT_INVERSE);
        UIManager.put("Menu.selectionBackground", SECONDARY_BLUE);
        UIManager.put("Menu.selectionForeground", TEXT_INVERSE);
        UIManager.put("Menu.hoverBackground", SECONDARY_BLUE);
        UIManager.put("Menu.hoverForeground", TEXT_INVERSE);
        UIManager.put("Menu.pressedBackground", PRIMARY_BLUE);
        UIManager.put("Menu.pressedForeground", TEXT_INVERSE);

        // Also try alternative property names that FlatLaf might use
        UIManager.put("Menu[MouseOver].textForeground", TEXT_INVERSE);
        UIManager.put("Menu[Selected].textForeground", TEXT_INVERSE);
        UIManager.put("Menu[Pressed].textForeground", TEXT_INVERSE);
    }

    /**
     * Configure additional global UI properties
     */
    private static void configureGlobalProperties() {
        // Font settings
        Font defaultFont = UIManager.getFont("Label.font");
        if (defaultFont != null) {
            Font sansSerifFont = new Font(Font.SANS_SERIF, defaultFont.getStyle(), defaultFont.getSize());

            // Apply consistent font family
            UIManager.put("Label.font", sansSerifFont);
            UIManager.put("Button.font", sansSerifFont);
            UIManager.put("TextField.font", sansSerifFont);
            UIManager.put("RadioButton.font", sansSerifFont);
        }

        // Global spacing and sizing
        UIManager.put("Component.minimumWidth", 120);
        UIManager.put("Button.minimumWidth", 80);
        UIManager.put("TextField.minimumWidth", 150);

        // Focus and selection colors
        UIManager.put("Component.focusColor", SECONDARY_BLUE);
        UIManager.put("Component.focusWidth", 2);

        // Tooltip styling
        UIManager.put("ToolTip.background", DARK_GRAY);
        UIManager.put("ToolTip.foreground", TEXT_INVERSE);
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(DARK_GRAY));
    }

    /**
     * Get a themed color by name
     */
    public static Color getColor(String colorName) {
        return switch (colorName.toLowerCase()) {
            case "primary", "primary-blue" -> PRIMARY_BLUE;
            case "secondary", "secondary-blue" -> SECONDARY_BLUE;
            case "success", "success-green" -> SUCCESS_GREEN;
            case "warning", "warning-orange" -> WARNING_ORANGE;
            case "error", "error-red" -> ERROR_RED;
            case "neutral", "neutral-gray" -> NEUTRAL_GRAY;
            case "light", "light-gray" -> LIGHT_GRAY;
            case "dark", "dark-gray" -> DARK_GRAY;
            case "text-primary" -> TEXT_PRIMARY;
            case "text-secondary" -> TEXT_SECONDARY;
            case "text-tertiary" -> TEXT_TERTIARY;
            case "text-inverse" -> TEXT_INVERSE;
            default -> TEXT_PRIMARY;
        };
    }
}
