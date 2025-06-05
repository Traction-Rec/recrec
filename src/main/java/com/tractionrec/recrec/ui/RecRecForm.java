package com.tractionrec.recrec.ui;

import com.tractionrec.recrec.RecRecState;

import javax.swing.*;
import java.awt.*;

public abstract class RecRecForm {

    // =========================================================
    //  ATTRIBUTES
    // =========================================================

    protected RecRecState state;
    protected NavigationAction navigationAction;

    // =========================================================
    //  CONSTRUCTORS
    // =========================================================

    {
        // Before constructor
        setupUI();
    }

    protected RecRecForm(RecRecState state, NavigationAction navigationAction) {
        this.state = state;
        this.navigationAction = navigationAction;
    }

    // =========================================================
    //  ABSTRACT METHODS
    // =========================================================

    abstract JComponent getRootComponent();
    protected abstract void setupUI();

    // =========================================================
    //  PACKAGE PRIVATE METHODS
    // =========================================================

    RecRecForm whatIsNext() {
        return null;
    }

    void willDisplay() {
        // NO-OP
    }

    void willHide() {
        // NO-OP
    }

    /**
     * Get the preferred window size for this form.
     * Default is 400x300 (more generous than previous 350x250), but forms can override this.
     */
    Dimension getPreferredWindowSize() {
        return new Dimension(400, 300);
    }

    /**
     * Get the minimum window size for this form.
     * This is used to prevent windows from becoming too small to be usable.
     * Default is based on platform recommendations, but forms can override this.
     */
    Dimension getMinimumWindowSize() {
        return PlatformUtils.getRecommendedMinimumSize();
    }

    /**
     * Indicates if this form requires extra space due to dynamic content.
     * Forms with tables, lists, or other expandable content should override this.
     */
    boolean requiresExtraSpace() {
        return false;
    }

    /**
     * Validate that all components in this form are properly visible.
     * This can be called after layout to detect clipping issues.
     */
    void validateLayout() {
        JComponent root = getRootComponent();
        if (root != null) {
            validateComponentVisibility(root);
        }
    }

    /**
     * Recursively check if components are visible and not clipped
     */
    private void validateComponentVisibility(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton) {
                // Check if button is fully visible
                Rectangle bounds = component.getBounds();
                Rectangle parentBounds = container.getBounds();

                if (bounds.x + bounds.width > parentBounds.width ||
                    bounds.y + bounds.height > parentBounds.height) {
                    System.err.println("Warning: Component " + component.getClass().getSimpleName() +
                                     " may be clipped in " + this.getClass().getSimpleName());
                }
            }

            if (component instanceof Container) {
                validateComponentVisibility((Container) component);
            }
        }
    }

}
