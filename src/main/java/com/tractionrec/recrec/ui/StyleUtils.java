package com.tractionrec.recrec.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Utility class for applying consistent styling to UI components
 */
public class StyleUtils {

    // Spacing Constants - Refined scale for better visual rhythm
    public static final int SPACING_MICRO = 2;
    public static final int SPACING_TINY = 4;
    public static final int SPACING_SMALL = 8;
    public static final int SPACING_MEDIUM = 12;
    public static final int SPACING_LARGE = 16;
    public static final int SPACING_XLARGE = 24;
    public static final int SPACING_XXLARGE = 32;
    public static final int SPACING_XXXLARGE = 48;

    // Border Radius
    public static final int BORDER_RADIUS = 6;

    // Component Heights - Refined for better proportions and text visibility
    public static final int INPUT_HEIGHT = 44;
    public static final int BUTTON_HEIGHT = 40;
    public static final int BUTTON_HEIGHT_LARGE = 48;

    /**
     * Create a card-style panel with subtle border and background
     */
    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(createCardBorder());
        return card;
    }

    /**
     * Create an elevated card with subtle shadow effect
     */
    public static JPanel createElevatedCard() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(createElevatedCardBorder());
        return card;
    }

    /**
     * Create a card border with subtle styling
     */
    public static Border createCardBorder() {
        Border lineBorder = new LineBorder(new Color(0xE5E7EB), 1, true);
        Border paddingBorder = new EmptyBorder(SPACING_XLARGE, SPACING_XLARGE, SPACING_XLARGE, SPACING_XLARGE);
        return new CompoundBorder(lineBorder, paddingBorder);
    }

    /**
     * Create an elevated card border with shadow effect simulation
     */
    public static Border createElevatedCardBorder() {
        // Simulate shadow with multiple borders
        Border shadowBorder = new LineBorder(new Color(0xF3F4F6), 2, true);
        Border mainBorder = new LineBorder(new Color(0xE5E7EB), 1, true);
        Border paddingBorder = new EmptyBorder(SPACING_XLARGE, SPACING_XLARGE, SPACING_XLARGE, SPACING_XLARGE);

        return new CompoundBorder(
            new CompoundBorder(shadowBorder, mainBorder),
            paddingBorder
        );
    }

    /**
     * Create a section with title and content
     */
    public static JPanel createSection(String title, JComponent content) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (title != null && !title.isEmpty()) {
            JLabel titleLabel = createSectionTitle(title);
            section.add(titleLabel);
            section.add(Box.createVerticalStrut(SPACING_MEDIUM));
        }

        content.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(content);

        return section;
    }

    /**
     * Create a section title label
     */
    public static JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TypographyConstants.FONT_SUBHEADING);
        label.setForeground(TractionRecTheme.TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /**
     * Create a form title label
     */
    public static JLabel createFormTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TypographyConstants.FONT_TITLE);
        label.setForeground(TractionRecTheme.PRIMARY_BLUE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /**
     * Style a button as primary (filled with brand color)
     */
    public static void styleButtonPrimary(JButton button) {
        styleButtonPrimary(button, false);
    }

    /**
     * Style a button as primary with optional large size
     */
    public static void styleButtonPrimary(JButton button, boolean isLarge) {
        button.setFont(TypographyConstants.FONT_BUTTON_PRIMARY);
        button.setBackground(TractionRecTheme.PRIMARY_BLUE);
        button.setForeground(TractionRecTheme.TEXT_INVERSE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);

        int height = isLarge ? BUTTON_HEIGHT_LARGE : BUTTON_HEIGHT;
        int horizontalPadding = isLarge ? SPACING_XLARGE : SPACING_LARGE;

        button.setPreferredSize(new Dimension(
            button.getPreferredSize().width + (horizontalPadding * 2),
            height
        ));

        // Enhanced border with rounded corners simulation
        button.setBorder(new EmptyBorder(SPACING_MEDIUM, horizontalPadding, SPACING_MEDIUM, horizontalPadding));

        // Add smooth hover effect with consistent white text
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0x1E40AF)); // Slightly darker blue for hover
                button.setForeground(TractionRecTheme.TEXT_INVERSE); // Keep white text
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(TractionRecTheme.PRIMARY_BLUE);
                button.setForeground(TractionRecTheme.TEXT_INVERSE);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }

    /**
     * Style a button as secondary (outlined)
     */
    public static void styleButtonSecondary(JButton button) {
        styleButtonSecondary(button, false);
    }

    /**
     * Style a button as secondary with optional large size
     */
    public static void styleButtonSecondary(JButton button, boolean isLarge) {
        button.setFont(TypographyConstants.FONT_BUTTON);
        button.setBackground(Color.WHITE);
        button.setForeground(TractionRecTheme.PRIMARY_BLUE);
        button.setFocusPainted(false);
        button.setOpaque(true);

        int height = isLarge ? BUTTON_HEIGHT_LARGE : BUTTON_HEIGHT;
        int horizontalPadding = isLarge ? SPACING_XLARGE : SPACING_LARGE;

        button.setPreferredSize(new Dimension(
            button.getPreferredSize().width + (horizontalPadding * 2),
            height
        ));

        // Enhanced border with proper padding
        Border lineBorder = new LineBorder(TractionRecTheme.PRIMARY_BLUE, 1, true);
        Border paddingBorder = new EmptyBorder(SPACING_MEDIUM, horizontalPadding, SPACING_MEDIUM, horizontalPadding);
        button.setBorder(new CompoundBorder(lineBorder, paddingBorder));

        // Add hover effect with better contrast
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0xDCFDF7)); // Light teal background for better contrast
                button.setForeground(TractionRecTheme.PRIMARY_BLUE); // Use theme blue for consistency
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(TractionRecTheme.PRIMARY_BLUE);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }

    /**
     * Style a text field with consistent appearance
     */
    public static void styleTextField(JTextField textField) {
        textField.setFont(TypographyConstants.FONT_INPUT);
        textField.setPreferredSize(new Dimension(textField.getPreferredSize().width, INPUT_HEIGHT));
        textField.setBorder(createInputBorder());

        // Add focus listener for enhanced border
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                textField.setBorder(createFocusedInputBorder());
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                textField.setBorder(createInputBorder());
            }
        });
    }

    /**
     * Style a password field with consistent appearance
     */
    public static void stylePasswordField(JPasswordField passwordField) {
        passwordField.setFont(TypographyConstants.FONT_INPUT);
        passwordField.setPreferredSize(new Dimension(passwordField.getPreferredSize().width, INPUT_HEIGHT));
        passwordField.setBorder(createInputBorder());

        // Add focus listener for enhanced border
        passwordField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                passwordField.setBorder(createFocusedInputBorder());
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                passwordField.setBorder(createInputBorder());
            }
        });
    }

    /**
     * Create a border for input fields
     */
    public static Border createInputBorder() {
        Border lineBorder = new LineBorder(new Color(0xD1D5DB), 1, true);
        // Minimal vertical padding to prevent text cutoff, more horizontal for visual appeal
        Border paddingBorder = new EmptyBorder(SPACING_SMALL, SPACING_MEDIUM, SPACING_SMALL, SPACING_MEDIUM);
        return new CompoundBorder(lineBorder, paddingBorder);
    }

    /**
     * Create a focused border for input fields
     */
    public static Border createFocusedInputBorder() {
        Border lineBorder = new LineBorder(TractionRecTheme.SECONDARY_BLUE, 2, true);
        // Minimal vertical padding with adjustment for thicker border
        Border paddingBorder = new EmptyBorder(SPACING_SMALL - 1, SPACING_MEDIUM - 1, SPACING_SMALL - 1, SPACING_MEDIUM - 1);
        return new CompoundBorder(lineBorder, paddingBorder);
    }

    /**
     * Style a label with consistent appearance
     */
    public static void styleLabel(JLabel label) {
        label.setFont(TypographyConstants.FONT_LABEL);
        label.setForeground(TractionRecTheme.TEXT_PRIMARY);
    }

    /**
     * Style a radio button with consistent appearance
     */
    public static void styleRadioButton(JRadioButton radioButton) {
        radioButton.setFont(TypographyConstants.FONT_BODY);
        radioButton.setForeground(TractionRecTheme.TEXT_PRIMARY);
        radioButton.setBackground(Color.WHITE);
        radioButton.setFocusPainted(false);
    }

    /**
     * Style a checkbox with consistent appearance
     */
    public static void styleCheckBox(JCheckBox checkBox) {
        checkBox.setFont(TypographyConstants.FONT_BODY);
        checkBox.setForeground(TractionRecTheme.TEXT_PRIMARY);
        checkBox.setBackground(Color.WHITE);
        checkBox.setFocusPainted(false);
    }

    /**
     * Create a form row with label and input
     */
    public static JPanel createFormRow(String labelText, JComponent input) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBackground(Color.WHITE);

        JLabel label = new JLabel(labelText);
        styleLabel(label);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        input.setAlignmentX(Component.LEFT_ALIGNMENT);

        row.add(label);
        row.add(Box.createVerticalStrut(SPACING_SMALL));
        row.add(input);

        return row;
    }

    /**
     * Create a radio button group with consistent styling
     */
    public static JPanel createRadioButtonGroup(String title, JRadioButton... buttons) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.setBackground(Color.WHITE);

        if (title != null && !title.isEmpty()) {
            JLabel titleLabel = createSectionTitle(title);
            group.add(titleLabel);
            group.add(Box.createVerticalStrut(SPACING_MEDIUM));
        }

        for (int i = 0; i < buttons.length; i++) {
            JRadioButton button = buttons[i];
            styleRadioButton(button);
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            group.add(button);

            if (i < buttons.length - 1) {
                group.add(Box.createVerticalStrut(SPACING_SMALL));
            }
        }

        return group;
    }

    /**
     * Create a navigation panel with buttons
     */
    public static JPanel createNavigationPanel(JButton... buttons) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBackground(Color.WHITE);

        panel.add(Box.createHorizontalGlue());

        for (int i = 0; i < buttons.length; i++) {
            panel.add(buttons[i]);
            if (i < buttons.length - 1) {
                panel.add(Box.createHorizontalStrut(SPACING_MEDIUM));
            }
        }

        return panel;
    }

    /**
     * Add consistent spacing between components
     */
    public static void addVerticalSpacing(Container container, int spacing) {
        container.add(Box.createVerticalStrut(spacing));
    }

    /**
     * Add consistent spacing between components
     */
    public static void addHorizontalSpacing(Container container, int spacing) {
        container.add(Box.createHorizontalStrut(spacing));
    }

    /**
     * Create a button with an icon (using Unicode symbols)
     */
    public static JButton createIconButton(String text, String icon) {
        JButton button = new JButton(icon + "  " + text);
        return button;
    }

    /**
     * Common UI icons using Unicode symbols
     */
    public static class Icons {
        public static final String ARROW_RIGHT = "→";
        public static final String ARROW_LEFT = "←";
        public static final String CHECK = "✓";
        public static final String CROSS = "✕";
        public static final String SETTINGS = "⚙";
        public static final String FOLDER = "📁";
        public static final String FILE = "📄";
        public static final String DOWNLOAD = "⬇";
        public static final String UPLOAD = "⬆";
        public static final String INFO = "ℹ";
        public static final String WARNING = "⚠";
        public static final String ERROR = "⚠";
        public static final String SEARCH = "🔍";
        public static final String REFRESH = "↻";
    }
}
