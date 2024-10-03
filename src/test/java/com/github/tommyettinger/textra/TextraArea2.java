package com.github.tommyettinger.textra;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;

public class TextraArea2 extends TextraField {

    public TextraArea2(@Null String text, Skin skin) {
        this(text, skin.get(Styles.TextFieldStyle.class));
    }

    public TextraArea2(@Null String text, Skin skin, Font replacementFont) {
        this(text, skin.get(Styles.TextFieldStyle.class), replacementFont);
    }

    public TextraArea2(@Null String text, Skin skin, String styleName) {
        this(text, skin.get(styleName, Styles.TextFieldStyle.class));
    }

    public TextraArea2(String text, Styles.TextFieldStyle style) {
        super();
        Styles.TextFieldStyle s = new Styles.TextFieldStyle(style);
//        s.font = new Font(style.font); // already done by TextFieldStyle constructor
        s.font.enableSquareBrackets = false;
        s.font.omitCurlyBraces = false;
        setStyle(s);
        this.style.font.enableSquareBrackets = false;
        this.style.font.omitCurlyBraces = false;
        writeEnters = true;
        label = new TypingLabel("", new Styles.LabelStyle(this.style.font, style.fontColor));
        label.workingLayout.targetWidth = 1f;
        label.setMaxLines(Integer.MAX_VALUE);
        label.setAlignment(Align.bottomLeft);
        label.setWrap(true);
        label.setSelectable(true);
        initialize();
        setText(text);
        label.setSize(getPrefWidth(), getPrefHeight());
        label.skipToTheEnd(true, true);
        updateDisplayText();
    }

    public TextraArea2(String text, Styles.TextFieldStyle style, Font replacementFont) {
        super();
        setStyle(style);
        replacementFont = new Font(replacementFont);
        replacementFont.enableSquareBrackets = false;
        replacementFont.omitCurlyBraces = false;
        label = new TypingLabel("", new Styles.LabelStyle(replacementFont, style.fontColor));
        label.workingLayout.targetWidth = 1f;
        label.setMaxLines(Integer.MAX_VALUE);
        label.setAlignment(Align.bottomLeft);
        label.setWrap(true);
        label.setSelectable(true);
        writeEnters = true;
        initialize();
        setText(text);
        label.setSize(getPrefWidth(), getPrefHeight());
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
        final float layoutHeight = label.workingLayout.getHeight(), linesHeight = label.getCumulativeLineHeight(cursor);
//		System.out.println("layoutHeight: " + layoutHeight + ", linesHeight: " + linesHeight);
        cursorPatch.draw(batch,
                x + textOffset + glyphPositions.get(cursor) - glyphPositions.get(visibleTextStart) + fontOffset,
                y + layoutHeight - linesHeight, cursorPatch.getMinWidth(), label.getLineHeight(cursor));
    }

}
