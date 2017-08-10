package com.javaccy.plugins.doc.tip;

import com.intellij.codeInsight.lookup.impl.LookupImpl;

/**
 * Created by javaccy on 2017/3/4.
 */
public abstract class View {

    protected View(LookupImpl lookup){}

    public abstract void clear();

    public abstract void setContent(String text);

    public abstract void reset(LookupImpl lookup);
}
