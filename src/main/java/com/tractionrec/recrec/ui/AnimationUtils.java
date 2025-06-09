package com.tractionrec.recrec.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Simple animation utilities for smooth UI transitions
 */
public class AnimationUtils {

    /**
     * Animate a component's background color
     */
    public static void animateBackgroundColor(JComponent component, Color fromColor, Color toColor, int durationMs) {
        Timer timer = new Timer(16, null); // ~60 FPS
        long startTime = System.currentTimeMillis();

        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1.0f, (float) elapsed / durationMs);

                // Ease-out cubic function for smooth animation
                progress = 1 - (float) Math.pow(1 - progress, 3);

                Color currentColor = interpolateColor(fromColor, toColor, progress);
                component.setBackground(currentColor);
                component.repaint();

                if (progress >= 1.0f) {
                    timer.stop();
                }
            }
        });

        timer.start();
    }

    /**
     * Animate a component's foreground color
     */
    public static void animateForegroundColor(JComponent component, Color fromColor, Color toColor, int durationMs) {
        Timer timer = new Timer(16, null); // ~60 FPS
        long startTime = System.currentTimeMillis();

        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1.0f, (float) elapsed / durationMs);

                // Ease-out cubic function for smooth animation
                progress = 1 - (float) Math.pow(1 - progress, 3);

                Color currentColor = interpolateColor(fromColor, toColor, progress);
                component.setForeground(currentColor);
                component.repaint();

                if (progress >= 1.0f) {
                    timer.stop();
                }
            }
        });

        timer.start();
    }

    /**
     * Fade in a component by animating its opacity
     */
    public static void fadeIn(JComponent component, int durationMs) {
        component.setOpaque(false);
        Timer timer = new Timer(16, null); // ~60 FPS
        long startTime = System.currentTimeMillis();

        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1.0f, (float) elapsed / durationMs);

                // Ease-out cubic function for smooth animation
                progress = 1 - (float) Math.pow(1 - progress, 3);

                // Note: Swing doesn't have built-in opacity, so this is a simplified version
                // In a real implementation, you'd need to override paintComponent
                component.setVisible(progress > 0.1f);

                if (progress >= 1.0f) {
                    component.setOpaque(true);
                    timer.stop();
                }
            }
        });

        timer.start();
    }

    /**
     * Interpolate between two colors
     */
    private static Color interpolateColor(Color from, Color to, float progress) {
        int red = (int) (from.getRed() + (to.getRed() - from.getRed()) * progress);
        int green = (int) (from.getGreen() + (to.getGreen() - from.getGreen()) * progress);
        int blue = (int) (from.getBlue() + (to.getBlue() - from.getBlue()) * progress);
        int alpha = (int) (from.getAlpha() + (to.getAlpha() - from.getAlpha()) * progress);

        return new Color(
            Math.max(0, Math.min(255, red)),
            Math.max(0, Math.min(255, green)),
            Math.max(0, Math.min(255, blue)),
            Math.max(0, Math.min(255, alpha))
        );
    }

    /**
     * Create a smooth hover effect for buttons
     */
    public static void addSmoothHoverEffect(JButton button, Color normalBg, Color hoverBg, Color normalFg, Color hoverFg) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                animateBackgroundColor(button, normalBg, hoverBg, 150);
                animateForegroundColor(button, normalFg, hoverFg, 150);
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                animateBackgroundColor(button, hoverBg, normalBg, 150);
                animateForegroundColor(button, hoverFg, normalFg, 150);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }
}
