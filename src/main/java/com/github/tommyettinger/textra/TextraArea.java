package com.github.tommyettinger.textra;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;

/**
 * A multiple-line {@link TextraField}; not ready for production yet.
 */
public class TextraArea extends TextraField {

    public TextraArea(@Null String text, Skin skin) {
        this(text, skin.get(Styles.TextFieldStyle.class));
    }

    public TextraArea(@Null String text, Skin skin, Font replacementFont) {
        this(text, skin.get(Styles.TextFieldStyle.class), replacementFont);
    }

    public TextraArea(@Null String text, Skin skin, String styleName) {
        this(text, skin.get(styleName, Styles.TextFieldStyle.class));
    }

    public TextraArea(String text, Styles.TextFieldStyle style) {
        super();
        Styles.TextFieldStyle s = new Styles.TextFieldStyle(style);
//        s.font = new Font(style.font); // already done by TextFieldStyle constructor
        s.font.enableSquareBrackets = false;
        s.font.omitCurlyBraces = false;
        setStyle(s);
        this.style.font.enableSquareBrackets = false;
        this.style.font.omitCurlyBraces = false;
        label = new TypingLabel("", new Styles.LabelStyle(this.style.font, style.fontColor));
        label.workingLayout.targetWidth = 1f;
        label.setMaxLines(Integer.MAX_VALUE);
        label.setAlignment(Align.topLeft);
        label.setWrap(true);
        label.setSelectable(true);
        if(style.selection != null)
            label.selectionDrawable = style.selection;
        writeEnters = true;
        initialize();
        label.setSize(getPrefWidth(), getPrefHeight());
        setText(text);
        label.skipToTheEnd(true, true);
        updateDisplayText();
    }

    public TextraArea(String text, Styles.TextFieldStyle style, Font replacementFont) {
        super();
        setStyle(style);
        replacementFont = new Font(replacementFont);
        replacementFont.enableSquareBrackets = false;
        replacementFont.omitCurlyBraces = false;
        label = new TypingLabel("", new Styles.LabelStyle(replacementFont, style.fontColor));
        label.workingLayout.targetWidth = 1f;
        label.setMaxLines(Integer.MAX_VALUE);
        label.setAlignment(Align.topLeft);
        label.setWrap(true);
        label.setSelectable(true);
        if(style.selection != null) label.selectionDrawable = style.selection;
        writeEnters = true;
        initialize();
        label.setSize(getPrefWidth(), getPrefHeight());
        setText(text);
        label.skipToTheEnd(true, true);
        updateDisplayText();
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        label.setSize(getWidth(), getHeight());
    }

    @Override
    protected void drawCursor (Drawable cursorPatch, Batch batch, Font font, float x, float y) {
        final float layoutHeight = label.getHeight(), linesHeight = label.getCumulativeLineHeight(cursor);
//		System.out.println("layoutHeight: " + layoutHeight + ", linesHeight: " + linesHeight);
        cursorPatch.draw(batch,
                x + textOffset + glyphPositions.get(cursor) - glyphPositions.get(visibleTextStart) + fontOffset,
                y + layoutHeight - linesHeight, cursorPatch.getMinWidth(), label.getLineHeight(cursor));
    }

    @Override
    protected float getTextY (Font font, @Null Drawable background) {
        float textY = 0;
        if (background != null) {
            textY = textY - background.getTopHeight();
        }
        if (font.integerPosition) textY = (int)textY;
        return textY;
    }

}
