package com.tractionrec.recrec.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        applicationFrame.setPreferredSize(new Dimension(350, 250));
        applicationFrame.setResizable(false);
        applicationFrame.pack();
        applicationFrame.setVisible(true);
        this.currentForm = form;
        this.nextForm = form.whatIsNext();
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
