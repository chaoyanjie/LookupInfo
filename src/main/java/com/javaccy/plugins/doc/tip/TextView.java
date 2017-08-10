package com.javaccy.plugins.doc.tip;

import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.*;

/**
 * Created by javaccy on 2017/3/4.
 */
public class TextView extends View {

    private JTextArea textArea;
    private JBScrollPane pane;
    //private JScrollBar bar;

    public TextView(LookupImpl lookup) {
        super(lookup);
        if (lookup != null) {
            textArea = new JTextArea(10,50);
            pane = new JBScrollPane(textArea);
            //pane.setPreferredSize(new Dimension(100,200));
            //bar = pane.getVerticalScrollBar();
            lookup.getComponent().add(pane,BorderLayout.SOUTH);
        }
    }


    @Override
    public void clear() {
        textArea.setText("");
    }

    @Override
    public void setContent(String text) {
        textArea.setText(text);
        textArea.setCaretPosition(0);
        textArea.updateUI();
    }

    @Override
    public void reset(LookupImpl lookup) {
        lookup.getComponent().add(pane, BorderLayout.SOUTH);
    }
}
