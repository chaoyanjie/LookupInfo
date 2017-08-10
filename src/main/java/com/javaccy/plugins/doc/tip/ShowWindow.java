package com.javaccy.plugins.doc.tip;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.impl.ConsoleViewUtil;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.lang.java.JavaSyntaxHighlighterFactory;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by javaccy on 2017/3/3.
 */
public class ShowWindow implements ToolWindowFactory {

    private static ToolWindow toolWindow;
    private static Project project;
    private static ConsoleView console;
    private static SyntaxHighlighter javaSyhl;


    @Override
    public void createToolWindowContent(@NotNull Project p, @NotNull ToolWindow tw) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                project = p;
                toolWindow = tw;
                ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
                console = TextConsoleBuilderFactory.getInstance().createBuilder(p).getConsole();
                Content content = contentFactory.createContent(console.getComponent(), "", false);
                toolWindow.getContentManager().addContent(content);
                javaSyhl = JavaSyntaxHighlighterFactory.getSyntaxHighlighter(JavaFileType.INSTANCE,project, null);
            }
        });

    }



    /*public static void setDocComment(String text){
        if(console==null)return;
        console.clear();
        ConsoleViewUtil.printWithHighlighting(console,text,javaSyhl);
    }*/

    public static void clear() {
        if(console==null) return;
        console.clear();
    }

}
