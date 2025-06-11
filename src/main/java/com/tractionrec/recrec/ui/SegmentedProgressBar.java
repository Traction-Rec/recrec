package com.tractionrec.recrec.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * A custom progress bar that shows different segments for different result types
 */
public class SegmentedProgressBar extends JComponent {

    private int total = 0;
    private int success = 0;
    private int notFound = 0;
    private int error = 0;
    private int pending = 0;

    private final Color successColor = TractionRecTheme.SUCCESS_GREEN;
    private final Color notFoundColor = TractionRecTheme.WARNING_ORANGE;
    private final Color errorColor = TractionRecTheme.ERROR_RED;
    private final Color pendingColor = new Color(0xE5E7EB);
    private final Color backgroundColor = new Color(0xF3F4F6);

    public SegmentedProgressBar() {
        setPreferredSize(new Dimension(400, 28)); // Slightly taller
        setMinimumSize(new Dimension(200, 28)); // Ensure minimum height
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 28)); // Fixed height
        setFont(TypographyConstants.FONT_SMALL);
        setOpaque(false); // Let our custom painting handle everything
        setBackground(backgroundColor);
    }

    public void updateProgress(int total, int success, int notFound, int error, int pending) {
        this.total = total;
        this.success = success;
        this.notFound = notFound;
        this.error = error;
        this.pending = pending;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Draw background first
        g2d.setColor(backgroundColor);
        g2d.fillRoundRect(0, 0, width, height, 6, 6);

        // Add a border for visibility
        g2d.setColor(new Color(0xD1D5DB));
        g2d.drawRoundRect(0, 0, width - 1, height - 1, 6, 6);

        if (total > 0) {
            int margin = 2; // Small margin from edges
            int drawWidth = width - (margin * 2);
            int drawHeight = height - (margin * 2);
            int currentX = margin;

            // Draw success segment
            if (success > 0) {
                int segmentWidth = Math.max(1, (success * drawWidth) / total);
                g2d.setColor(successColor);
                g2d.fillRoundRect(currentX, margin, segmentWidth, drawHeight, 4, 4);
                currentX += segmentWidth;
            }

            // Draw not found segment
            if (notFound > 0) {
                int segmentWidth = Math.max(1, (notFound * drawWidth) / total);
                g2d.setColor(notFoundColor);
                g2d.fillRect(currentX, margin, segmentWidth, drawHeight);
                currentX += segmentWidth;
            }

            // Draw error segment
            if (error > 0) {
                int segmentWidth = Math.max(1, (error * drawWidth) / total);
                g2d.setColor(errorColor);
                g2d.fillRect(currentX, margin, segmentWidth, drawHeight);
                currentX += segmentWidth;
            }

            // Calculate completion percentage
            int completed = success + notFound + error;
            int progressPercent = (completed * 100) / total;

            // Draw percentage text
            String text = progressPercent + "% Complete";
            FontMetrics fm = g2d.getFontMetrics();
            Rectangle2D textBounds = fm.getStringBounds(text, g2d);
            int textX = (width - (int) textBounds.getWidth()) / 2;
            int textY = (height + fm.getAscent()) / 2 - 2;

            // Draw text with outline for better visibility
            g2d.setColor(Color.WHITE);
            g2d.drawString(text, textX - 1, textY);
            g2d.drawString(text, textX + 1, textY);
            g2d.drawString(text, textX, textY - 1);
            g2d.drawString(text, textX, textY + 1);

            g2d.setColor(Color.BLACK);
            g2d.drawString(text, textX, textY);
        } else {
            // Draw "Initializing..." text
            String text = "Initializing...";
            FontMetrics fm = g2d.getFontMetrics();
            Rectangle2D textBounds = fm.getStringBounds(text, g2d);
            int textX = (width - (int) textBounds.getWidth()) / 2;
            int textY = (height + fm.getAscent()) / 2 - 2;

            g2d.setColor(TractionRecTheme.TEXT_SECONDARY);
            g2d.drawString(text, textX, textY);
        }

        g2d.dispose();
    }

    public String getTooltipText() {
        if (total > 0) {
            return String.format("<html><div style='padding: 4px;'>" +
                "<b>Progress Breakdown:</b><br>" +
                "• Success: %d<br>" +
                "• Not Found: %d<br>" +
                "• Error: %d<br>" +
                "• Pending: %d<br>" +
                "<b>Total: %d</b>" +
                "</div></html>",
                success, notFound, error, pending, total);
        }
        return "Initializing...";
    }
}
