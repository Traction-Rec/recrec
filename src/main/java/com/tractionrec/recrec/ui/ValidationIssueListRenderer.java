package com.tractionrec.recrec.ui;

import com.tractionrec.recrec.csv.ValidationIssue;
import com.tractionrec.recrec.domain.IssueSeverity;

import javax.swing.*;
import java.awt.*;

/**
 * Custom list cell renderer for validation issues
 */
public class ValidationIssueListRenderer extends DefaultListCellRenderer {

    private static final Icon ERROR_ICON = createColoredIcon(Color.RED);
    private static final Icon WARNING_ICON = createColoredIcon(Color.ORANGE);
    private static final Icon INFO_ICON = createColoredIcon(Color.BLUE);

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {

        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof ValidationIssue issue) {
            // Set icon based on severity
            Icon icon = switch (issue.getSeverity()) {
                case ERROR -> ERROR_ICON;
                case WARNING -> WARNING_ICON;
                case INFO -> INFO_ICON;
            };
            setIcon(icon);

            // Create multi-line text with prominent row information
            String locationText = "General";
            if (issue.getLocation() != null) {
                locationText = String.format("Row %d, Column %d",
                    issue.getLocation().row() + 1, // 1-based for display
                    issue.getLocation().column() + 1);
            }

            String html = String.format(
                "<html><div style='padding: 2px;'><b style='color: #1E3A8A;'>%s</b><br/>" +
                "<span style='color: #374151;'>%s</span><br/>" +
                "<small style='color: #6B7280; font-weight: bold;'>📍 %s</small></div></html>",
                issue.getType().getDisplayName(),
                issue.getDescription(),
                locationText
            );
            setText(html);

            // Set background color based on severity (subtle)
            if (!isSelected) {
                Color backgroundColor = switch (issue.getSeverity()) {
                    case ERROR -> new Color(255, 245, 245);
                    case WARNING -> new Color(255, 250, 240);
                    case INFO -> new Color(245, 250, 255);
                };
                setBackground(backgroundColor);
            }

            // Set tooltip with suggested fix
            if (issue.getSuggestedFix() != null && !issue.getSuggestedFix().isEmpty()) {
                setToolTipText("<html><b>Suggested fix:</b><br/>" + issue.getSuggestedFix() + "</html>");
            } else {
                setToolTipText(null);
            }
        }

        return this;
    }

    /**
     * Create a simple colored icon for severity indication
     */
    private static Icon createColoredIcon(Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(x + 2, y + 2, getIconWidth() - 4, getIconHeight() - 4);
                g2.setColor(color.darker());
                g2.drawOval(x + 2, y + 2, getIconWidth() - 4, getIconHeight() - 4);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 12;
            }

            @Override
            public int getIconHeight() {
                return 12;
            }
        };
    }
}
