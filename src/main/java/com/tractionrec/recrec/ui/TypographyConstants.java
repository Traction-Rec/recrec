package com.tractionrec.recrec.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Typography constants and utilities for consistent text styling
 */
public class TypographyConstants {

    // Font Families
    public static final String FONT_FAMILY_PRIMARY = Font.SANS_SERIF;

    // Font Sizes - Refined scale for better hierarchy
    public static final int FONT_SIZE_TITLE = 28;
    public static final int FONT_SIZE_HEADING = 20;
    public static final int FONT_SIZE_SUBHEADING = 16;
    public static final int FONT_SIZE_BODY = 14;
    public static final int FONT_SIZE_SMALL = 13;
    public static final int FONT_SIZE_CAPTION = 12;
    public static final int FONT_SIZE_MICRO = 11;

    // Font Weights (using style constants)
    public static final int WEIGHT_BOLD = Font.BOLD;
    public static final int WEIGHT_NORMAL = Font.PLAIN;

    // Semantic Font Definitions
    public static final Font FONT_TITLE = new Font(FONT_FAMILY_PRIMARY, WEIGHT_BOLD, FONT_SIZE_TITLE);
    public static final Font FONT_HEADING = new Font(FONT_FAMILY_PRIMARY, WEIGHT_BOLD, FONT_SIZE_HEADING);
    public static final Font FONT_SUBHEADING = new Font(FONT_FAMILY_PRIMARY, WEIGHT_BOLD, FONT_SIZE_SUBHEADING);
    public static final Font FONT_BODY = new Font(FONT_FAMILY_PRIMARY, WEIGHT_NORMAL, FONT_SIZE_BODY);
    public static final Font FONT_BODY_BOLD = new Font(FONT_FAMILY_PRIMARY, WEIGHT_BOLD, FONT_SIZE_BODY);
    public static final Font FONT_SMALL = new Font(FONT_FAMILY_PRIMARY, WEIGHT_NORMAL, FONT_SIZE_SMALL);
    public static final Font FONT_CAPTION = new Font(FONT_FAMILY_PRIMARY, WEIGHT_NORMAL, FONT_SIZE_CAPTION);
    public static final Font FONT_MICRO = new Font(FONT_FAMILY_PRIMARY, WEIGHT_NORMAL, FONT_SIZE_MICRO);

    // Button Fonts
    public static final Font FONT_BUTTON = new Font(FONT_FAMILY_PRIMARY, WEIGHT_NORMAL, FONT_SIZE_BODY);
    public static final Font FONT_BUTTON_PRIMARY = new Font(FONT_FAMILY_PRIMARY, WEIGHT_BOLD, FONT_SIZE_BODY);

    // Input Field Fonts
    public static final Font FONT_INPUT = new Font(FONT_FAMILY_PRIMARY, WEIGHT_NORMAL, FONT_SIZE_BODY);
    public static final Font FONT_LABEL = new Font(FONT_FAMILY_PRIMARY, WEIGHT_NORMAL, FONT_SIZE_BODY);

    /**
     * Create a font with the specified size while maintaining the primary font family
     */
    public static Font createFont(int style, int size) {
        return new Font(FONT_FAMILY_PRIMARY, style, size);
    }

    /**
     * Create a scaled version of a base font
     */
    public static Font scaleFont(Font baseFont, float scaleFactor) {
        return baseFont.deriveFont(baseFont.getSize() * scaleFactor);
    }

    /**
     * Create a bold version of a font
     */
    public static Font makeBold(Font font) {
        return font.deriveFont(Font.BOLD);
    }

    /**
     * Create a plain (non-bold) version of a font
     */
    public static Font makePlain(Font font) {
        return font.deriveFont(Font.PLAIN);
    }

    /**
     * Get the appropriate font for a component type
     */
    public static Font getFontForComponent(String componentType) {
        return switch (componentType.toLowerCase()) {
            case "title" -> FONT_TITLE;
            case "heading", "h1" -> FONT_HEADING;
            case "subheading", "h2" -> FONT_SUBHEADING;
            case "body", "text" -> FONT_BODY;
            case "body-bold" -> FONT_BODY_BOLD;
            case "small" -> FONT_SMALL;
            case "caption" -> FONT_CAPTION;
            case "button" -> FONT_BUTTON;
            case "button-primary" -> FONT_BUTTON_PRIMARY;
            case "input", "textfield" -> FONT_INPUT;
            case "label" -> FONT_LABEL;
            default -> FONT_BODY;
        };
    }
}
