package com.tractionrec.recrec.ui;

import java.awt.*;

/**
 * Utility class for platform-specific UI adjustments to handle differences
 * in window decorations, DPI scaling, and font rendering across operating systems.
 */
public class PlatformUtils {
    
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final boolean IS_WINDOWS = OS_NAME.contains("windows");
    private static final boolean IS_MAC = OS_NAME.contains("mac");
    private static final boolean IS_LINUX = OS_NAME.contains("linux");
    
    /**
     * Get platform-specific window decoration insets (title bar, borders, etc.)
     * These values are estimates based on typical platform defaults.
     */
    public static Insets getWindowDecorationInsets() {
        if (IS_MAC) {
            // macOS has a unified title bar, smaller borders
            return new Insets(28, 1, 1, 1);
        } else if (IS_WINDOWS) {
            // Windows has thicker title bar and borders, especially on high DPI
            return new Insets(32, 8, 8, 8);
        } else {
            // Linux varies by window manager, use conservative estimates
            return new Insets(30, 5, 5, 5);
        }
    }
    
    /**
     * Get additional padding to account for platform-specific rendering differences
     */
    public static Insets getAdditionalPadding() {
        if (IS_WINDOWS) {
            // Windows often needs extra space for proper rendering
            return new Insets(10, 10, 10, 10);
        } else if (IS_MAC) {
            // macOS generally renders more compactly
            return new Insets(5, 5, 5, 5);
        } else {
            // Linux varies, use moderate padding
            return new Insets(8, 8, 8, 8);
        }
    }
    
    /**
     * Get recommended minimum window size for the platform
     */
    public static Dimension getRecommendedMinimumSize() {
        if (IS_MAC) {
            return new Dimension(300, 200);
        } else if (IS_WINDOWS) {
            return new Dimension(350, 250);
        } else {
            return new Dimension(320, 220);
        }
    }
    
    /**
     * Get the default font scaling factor for the platform
     */
    public static double getDefaultFontScaling() {
        // Get system DPI scaling
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        int dpi = toolkit.getScreenResolution();
        double scaling = dpi / 96.0; // 96 DPI is the standard baseline
        
        // Platform-specific adjustments
        if (IS_WINDOWS && scaling > 1.0) {
            // Windows high DPI can be more aggressive
            return Math.min(scaling * 1.1, 2.0);
        } else if (IS_MAC) {
            // macOS handles retina displays differently
            return Math.max(scaling, 1.0);
        } else {
            // Linux DPI handling varies
            return Math.max(scaling, 1.0);
        }
    }
    
    /**
     * Check if the platform typically has larger window decorations
     */
    public static boolean hasLargeWindowDecorations() {
        return IS_WINDOWS;
    }
    
    /**
     * Check if the platform benefits from resizable windows as a fallback
     */
    public static boolean shouldAllowResizableWindows() {
        // Linux window managers vary widely, so resizable is safer
        return IS_LINUX;
    }
    
    /**
     * Get platform name for debugging/logging
     */
    public static String getPlatformName() {
        if (IS_MAC) return "macOS";
        if (IS_WINDOWS) return "Windows";
        if (IS_LINUX) return "Linux";
        return "Unknown (" + OS_NAME + ")";
    }
}
