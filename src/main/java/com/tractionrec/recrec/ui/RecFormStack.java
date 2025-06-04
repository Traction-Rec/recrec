package com.tractionrec.recrec.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Stack;

public class RecFormStack implements NavigationAction {
    private final JFrame applicationFrame;
    private final Stack<RecRecForm> formStack = new Stack<>();

    private RecRecForm initialForm;
    private RecRecForm nextForm;
    private RecRecForm currentForm;

    public RecFormStack(JFrame applicationFrame) {
        this.applicationFrame = applicationFrame;
    }

    public void setInitialForm(RecRecForm initialForm) {
        this.initialForm = initialForm;
    }

    public void displayInitial() {
        setForm(initialForm);
    }

    private void setForm(RecRecForm form) {
        if(this.currentForm != null) {
            this.currentForm.willHide();
        }
        form.willDisplay();
        applicationFrame.setContentPane(form.getRootComponent());
        applicationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Calculate optimal window size based on content
        Dimension optimalSize = calculateOptimalWindowSize(form);
        applicationFrame.setPreferredSize(optimalSize);

        // Allow resizable windows on platforms that benefit from it
        boolean shouldBeResizable = PlatformUtils.shouldAllowResizableWindows();
        applicationFrame.setResizable(shouldBeResizable);

        if (shouldBeResizable) {
            // Set minimum size to prevent over-shrinking
            Dimension minSize = form.getMinimumWindowSize();
            applicationFrame.setMinimumSize(minSize);
        }

        applicationFrame.pack();
        applicationFrame.setVisible(true);

        // Validate layout after window is displayed
        SwingUtilities.invokeLater(() -> {
            form.validateLayout();
        });

        this.currentForm = form;
        this.nextForm = form.whatIsNext();
    }

    /**
     * Calculate optimal window size based on form content and platform characteristics
     */
    private Dimension calculateOptimalWindowSize(RecRecForm form) {
        // Start with form's preferred size as baseline
        Dimension preferredSize = form.getPreferredWindowSize();
        Dimension minimumSize = form.getMinimumWindowSize();

        // Get platform-specific adjustments
        Insets decorationInsets = PlatformUtils.getWindowDecorationInsets();
        Insets additionalPadding = PlatformUtils.getAdditionalPadding();
        double fontScaling = PlatformUtils.getDefaultFontScaling();

        // Calculate content-based size by temporarily setting up the frame
        JComponent rootComponent = form.getRootComponent();
        Dimension contentSize = rootComponent.getPreferredSize();

        // Apply font scaling to content size
        int scaledWidth = (int) (contentSize.width * fontScaling);
        int scaledHeight = (int) (contentSize.height * fontScaling);

        // Add platform-specific decorations and padding
        int totalWidth = scaledWidth + decorationInsets.left + decorationInsets.right +
                        additionalPadding.left + additionalPadding.right;
        int totalHeight = scaledHeight + decorationInsets.top + decorationInsets.bottom +
                         additionalPadding.top + additionalPadding.bottom;

        // Ensure we meet minimum requirements
        Dimension platformMinimum = PlatformUtils.getRecommendedMinimumSize();
        int finalWidth = Math.max(Math.max(totalWidth, preferredSize.width),
                                 Math.max(minimumSize.width, platformMinimum.width));
        int finalHeight = Math.max(Math.max(totalHeight, preferredSize.height),
                                  Math.max(minimumSize.height, platformMinimum.height));

        // Ensure we don't exceed screen size (leave 10% margin)
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int maxWidth = (int) (screenSize.width * 0.9);
        int maxHeight = (int) (screenSize.height * 0.9);

        finalWidth = Math.min(finalWidth, maxWidth);
        finalHeight = Math.min(finalHeight, maxHeight);

        System.out.println("Window sizing for " + form.getClass().getSimpleName() +
                          ": content=" + contentSize + ", final=" + new Dimension(finalWidth, finalHeight) +
                          ", platform=" + PlatformUtils.getPlatformName());

        return new Dimension(finalWidth, finalHeight);
    }

    @Override
    public void onBack() {
        RecRecForm previousCurrent = formStack.isEmpty() ? initialForm : formStack.pop();
        setForm(previousCurrent);
    }

    @Override
    public void onNext() {
        if(nextForm == null) {
            displayInitial();
        } else {
            formStack.push(currentForm);
            setForm(nextForm);
        }
    }

}
