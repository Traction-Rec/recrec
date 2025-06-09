package com.tractionrec.recrec.ui;

public interface NavigationAction {
    void onBack();
    void onNext();

    /**
     * Update the next form destination dynamically
     */
    default void updateNextForm(RecRecForm nextForm) {
        // Default implementation does nothing - only RecFormStack will override this
    }
}
