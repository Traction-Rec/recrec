package com.tractionrec.recrec;

import com.formdev.flatlaf.FlatLightLaf;
import com.tractionrec.recrec.service.QueryService;
import com.tractionrec.recrec.ui.RecFormStack;
import com.tractionrec.recrec.ui.RecRecAbout;
import com.tractionrec.recrec.ui.RecRecStart;
import gg.jte.CodeResolver;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;

import javax.swing.*;

public class RecRecApplication {

    public static void main(String[] args) {
        FlatLightLaf.setup();
        RecRecState state = new RecRecState(buildQueryService());
        JFrame applicationFrame = new JFrame("RecRec");
        applicationFrame.setJMenuBar(buildMenuBar());
        RecFormStack stack = new RecFormStack(applicationFrame);
        RecRecStart startForm = new RecRecStart(state, stack);
        stack.setInitialForm(startForm);
        stack.displayInitial();
    }

    private static QueryService buildQueryService() {
        CodeResolver codeResolver = new ResourceCodeResolver("templates");
        TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Plain);
        return QueryService.forTest(templateEngine);
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
