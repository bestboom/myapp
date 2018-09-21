package com.surblime.richtexteditor;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;

import jp.wasabeef.richeditor.RichEditor;

/**
 * Created by moses on 8/30/18.
 */

public class Editor extends RichEditor implements RichEditor.OnTextChangeListener {

    private String text = "";
    private OnTextChangeListener onTextChangeListener;

    public Editor(Context context) {
        super(context);
    }

    public Editor(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public Editor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attributeSet) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attributeSet, R.styleable.Editor);

        String placeholder = typedArray.getString(R.styleable.Editor_placeholder);
        setPlaceholder(placeholder);

        int background = typedArray.getColor(R.styleable.Editor_editorBackground, Color.TRANSPARENT);
        setEditorBackgroundColor(background);
        setBackgroundColor(background);

        int padding = typedArray.getDimensionPixelSize(R.styleable.Editor_editorPadding, 0);
        setPadding(padding, padding, padding, padding);

        typedArray.recycle();
    }

    public String getText() {
        return text;
    }

    @Override
    public void setOnTextChangeListener(OnTextChangeListener listener) {
        super.setOnTextChangeListener(this);
        this.onTextChangeListener = listener;
    }

    @Override
    public void onTextChange(String text) {
        this.text = text == null ? "" : text;
        if (this.onTextChangeListener != null) {
            this.onTextChangeListener.onTextChange(text);
        }
    }

    public void setText(String text) {
        this.text = text;
        setHtml(text);
    }
}
