package com.tractionrec.recrec;

import com.formdev.flatlaf.FlatLightLaf;
import com.jcabi.manifests.Manifests;
import com.tractionrec.recrec.ui.RecFormStack;
import com.tractionrec.recrec.ui.RecRecAbout;
import com.tractionrec.recrec.ui.RecRecStart;

import javax.swing.*;

public class RecRecApplication {

    public static String MANIFEST_PROD_PROPERTY_NAME = "Element-Express-Production";

    public static void main(String[] args) {
        FlatLightLaf.setup();
        RecRecState state = new RecRecState();
        JFrame applicationFrame = new JFrame("RecRec");
        applicationFrame.setJMenuBar(buildMenuBar());
        RecFormStack stack = new RecFormStack(applicationFrame);
        RecRecStart startForm = new RecRecStart(state, stack);
        stack.setInitialForm(startForm);
        stack.displayInitial();
    }

    public static boolean isDevEnv() {
        return !Manifests.exists(MANIFEST_PROD_PROPERTY_NAME);
    }

    public static boolean isProduction() {
        return Manifests.exists(MANIFEST_PROD_PROPERTY_NAME) && "true".equals(Manifests.read(MANIFEST_PROD_PROPERTY_NAME));
    }

    private static JMenuBar buildMenuBar() {
        JMenuBar topMenuBar = new JMenuBar();
        topMenuBar.add( Box.createHorizontalStrut( 10 ) );
        topMenuBar.add(new JLabel("RecRec"));
        topMenuBar.add( Box.createHorizontalStrut( 10 ) );
        topMenuBar.add( new JLabel("|") );
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> {
            RecRecAbout dialog = new RecRecAbout();
            dialog.pack();
            dialog.setVisible(true);
        });
        helpMenu.add(aboutItem);
        topMenuBar.add(helpMenu);
        return topMenuBar;
    }

}
