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
     * Default is 350x250, but forms can override this.
     */
    Dimension getPreferredWindowSize() {
        return new Dimension(350, 250);
    }

}
