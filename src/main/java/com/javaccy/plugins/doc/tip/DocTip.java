package com.javaccy.plugins.doc.tip;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.completion.impl.CompletionServiceImpl;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.psi.*;
import com.intellij.psi.impl.compiled.ClsMethodImpl;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiUtil;
import com.intellij.ui.JBColor;
import com.intellij.util.ProcessingContext;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

/**
 * Created by javaccy on 2017/3/1.
 */
public class DocTip extends CompletionContributor{


    private static View view;

    public DocTip() {


        extend(CompletionType.BASIC, PsiJavaPatterns.psiElement(), new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters, ProcessingContext context, @NotNull final CompletionResultSet result) {
                CompletionProgressIndicator cc = CompletionServiceImpl.getCompletionService().getCurrentCompletion();
                final LookupImpl lookup = cc.getLookup();
                final JList list = lookup.getList();
                if(view == null){
                   view =  new TextView(lookup);
                }else{
                    view.reset(lookup);
                }
                list.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        LookupElement item = lookup.getCurrentItem();
                        if(item!=null){
                            if(item.getObject() instanceof PsiClass){
                                readClassComment((PsiClass) item.getObject());
                            }else if(item.getObject() instanceof PsiMethod){
                                PsiMethod pm = (PsiMethod) item.getObject();
                                if (pm instanceof ClsMethodImpl) {
                                    pm = ((ClsMethodImpl) pm).getSourceMirrorMethod();
                                }
                                readMethodComment(pm);
                            } else if (item.getObject() instanceof PsiField) {
                                readFieldComment((PsiField) item.getObject());
                            }else if (item.getObject() instanceof PsiLocalVariable) {
                                //psiClass = PsiUtil.resolveClassInType(((PsiLocalVariable) item.getObject()).getType());
                                PsiElement block = PsiUtil.getVariableCodeBlock((PsiVariable) item.getObject(), item.getPsiElement());
                                readPsiLocalVariableComment(((PsiLocalVariable) item.getObject()),block);
                            }else{
                                System.out.println("IdeaLookupInfo 未知类型:"+item.getObject().toString());
                            }
                        }

                    }
                });

            }
        });

    }

    /**
     * 读取class注释
     * @param pc
     * @return
     */
    public static String readClassComment(PsiClass pc) {
        PsiComment comment = pc.getDocComment();
        if (comment != null) {
            view.setContent(comment.getText());
        }
        return null;
    }

    /**
     * 读取变量注释
     * @param plv
     * @return
     */
    public static String readPsiLocalVariableComment(PsiLocalVariable plv,PsiElement black) {
        PsiElement[] elements = black.getChildren();
        PsiElement element;
        PsiComment pc;
        PsiElement pe;
        for (int i = 0; i < elements.length; i++) {
            element = elements[i];
            if (plv.getContext()==element) {
                view.setContent(findBeforeComment(elements,i));
            }
        }
        return null;
    }
    //根据当前psiElement 坐在代码块的索引向前查找注释
    public static String findBeforeComment(PsiElement[] elements,int start) {
        StringBuffer sb = new StringBuffer();
        PsiElement pe;
        for (int i = start-1; i > 0; i--) {
            pe = elements[i];
            if (pe instanceof PsiTypeElement || pe instanceof PsiComment || pe instanceof PsiWhiteSpace) {
                pe = elements[i];
                if (pe instanceof PsiComment) {
                    sb.insert(0, pe.getText()+"\n");
                }
            } else {
                break;
            }

        }
        return sb.toString();
    }

    /**
     * 读取属性注释
     * @param pf
     * @return
     */
    public static String readFieldComment(PsiField pf) {
        String comment = getFieldContent(pf);
        view.setContent(comment);
        return null;
    }

    /**
     * 读取方法注释
     * @param pm
     * @return
     */
    public static String readMethodComment(PsiMethod pm) {
        String comment = getMethodContent(pm);
        view.setContent(comment);
        return null;
    }

    public static String getMethodComment(PsiMethod pm){
        if (pm == null) {
            return null;
        }
        ClsMethodImpl cm = (ClsMethodImpl) pm;
        PsiMethod smm = cm.getSourceMirrorMethod();
        if (smm == null) {
            //无法获取源代码
            return null;
        }
        if (smm.getDocComment() != null) {
            return smm.getText();
        } else {
            for (PsiMethod ppmm : pm.findSuperMethods()) {
                getMethodComment(ppmm);
            }
        }
        return null;
    }




    /**
     * 根据get set 方法查询属性注释
     * @param pm
     * @return
     */
    public static String getMethodContent(PsiMethod pm){
        StringBuffer sb = new StringBuffer();
        boolean flag = false;
        if(pm!=null){
            PsiComment pdc = pm.getDocComment();
            if (pdc != null) {
                sb.append("    "+pm.getText());
                flag= true;
            } else if(pm.getName().startsWith("get") || pm.getName().startsWith("set")){
                PsiClass clazz = pm.getContainingClass();
                String name = pm.getName().substring(3, pm.getName().length());
                for (PsiField pf : clazz.getFields()) {
                    if(pf.getName().equalsIgnoreCase(name)){
                        PsiDocComment ff = pf.getDocComment();
                        if (ff != null) {
                            sb.append("    "+pf.getText());
                            sb.append("\n");
                            sb.append("\n");
                            sb.append("    "+pm.getText());
                            flag = true;
                        }else{
                            if (pf.getName().equalsIgnoreCase("id")) {
                                System.out.println();
                            }
                            //大部分情况下注释写在上面
                            //    //xxxxxxxxxxxxx
                            //    private String id;
                            PsiElement first = pf.getFirstChild();
                            if (!(first instanceof PsiComment)) {
                                //有时属性的注释可能写在 属性的后面:private String id;//xxxxxxxxxxxx
                                first = pf.getLastChild();
                            }
                            if (first instanceof PsiComment) {
                                if (StringUtils.isNotBlank(first.getText())) {
                                    sb.append("    "+pf.getText());
                                    sb.append("\n");
                                    sb.append("\n");
                                    sb.append("    "+pm.getText());
                                    flag = true;
                                }
                            }else{
                                //当 单行注释和属性之间有空白
                                //    //xxxxxxxxxxxxx
                                //
                                //    private String id;
                                int i = 0;
                                PsiElement comment = pf.getPrevSibling();
                                do {
                                    if (i > 0) {
                                        comment = comment.getPrevSibling();
                                    }
                                    if (comment instanceof PsiComment) {
                                        if (StringUtils.isNotBlank(comment.getText())) {
                                            sb.append("    " + comment.getText());
                                            sb.append("\n");
                                            sb.append("    " + pf.getText());
                                            sb.append("\n");
                                            sb.append("\n");
                                            sb.append("    " + pm.getText());
                                            flag = true;
                                        }
                                        break;
                                    }
                                    i++;
                                } while (comment instanceof PsiWhiteSpace && i < 5);
                            }
                        }
                    }

                }

            }else{
                PsiMethod[] methods = pm.findSuperMethods();
                for (PsiMethod m : methods) {
                    PsiComment doc = m.getDocComment();
                    if(doc!=null){
                        sb.append("    父类:" + m.getContainingClass().getName());
                        sb.append("\n");
                        sb.append("    "+pm.getText());
                        flag = true;
                    }
                }
            }
        }
        return flag == true ? sb.toString() : null;
    }




    /**
     * 获取属性注释
     * @param pf
     * @return
     */
    public static String getFieldContent(PsiField pf){
        PsiDocComment pdc = pf.getDocComment();
        StringBuffer sb = new StringBuffer();
        boolean flag = false;
        if(pdc!=null){
            sb.append("    "+pf.getText());
            flag= true;
        }else{
            PsiClass clazz = pf.getContainingClass();
            String get = "get" + pf.getName();
            String set = "set" + pf.getName();
            sb.append("    "+pf.getText());
            for (PsiMethod pm : clazz.getMethods()) {
                if (pm.getName().equalsIgnoreCase(get)) {
                    sb.append("\n");
                    sb.append("\n");
                    sb.append("    "+pm.getText());
                    flag = true;
                }
            }
            for (PsiMethod pm : clazz.getMethods()) {
                if (pm.getName().equalsIgnoreCase(set)) {
                    sb.append("\n");
                    sb.append("\n");
                    sb.append("    "+pm.getText());
                    flag = true;
                }
            }
        }


        return flag == true ? sb.toString() : null;

    }



    public void show(Editor editor,String text){
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                JBPopupFactory factory = JBPopupFactory.getInstance();
                BalloonBuilder builder = factory.createHtmlTextBalloonBuilder(text, null, new JBColor(new Color(188, 238, 188), new Color(73, 120, 73)), null);
                builder.setFadeoutTime(1000);
                builder.createBalloon().show(factory.guessBestPopupLocation(editor), Balloon.Position.below);


            }
        });

    }


}
