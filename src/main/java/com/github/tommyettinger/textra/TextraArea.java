package com.github.tommyettinger.textra;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;

/**
 * A multiple-line {@link TextraField} using a {@link Font}; not ready for production yet.
 * <br>
 * If you have to use {@link Font} but don't need multiple lines, {@link TextraField} should work.
 * If you do need multiple-line input, you can use a libGDX BitmapFont with a scene2d.ui TextField.
 * If you don't need input, just selectable text, you can make a {@link TypingLabel} selectable with
 * {@link TypingLabel#setSelectable(boolean)}, probably also using {@link TypingLabel#skipToTheEnd()}.
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
        label.setWidth(getPrefWidth());
        setText(text);
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
        if(style.selection != null)
            label.selectionDrawable = style.selection;
        writeEnters = true;
        initialize();
        label.setWidth(getPrefWidth());
        setText(text);
        updateDisplayText();
    }

    @Override
    protected void drawCursor (Drawable cursorPatch, Batch batch, Font font, float x, float y) {
        final float layoutHeight = label.getHeight(), linesHeight = label.getCumulativeLineHeight(cursor),
                lineHeight = label.getLineHeight(cursor);

        cursorPatch.draw(batch,
                x + textOffset + glyphPositions.get(cursor) - glyphPositions.get(visibleTextStart) + fontOffset,
                y + layoutHeight - linesHeight, cursorPatch.getMinWidth(), lineHeight);
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

    @Override
    public float getPrefHeight() {
        return label.getPrefHeight();
    }

    @Override
    protected void moveCursorVertically(boolean forward, boolean jump) {
        if(jump)
            cursor = forward ? text.length() : 0;
        else {
            float gp = glyphPositions.get(cursor);
            int currentLine = label.getLineIndexInLayout(label.workingLayout, cursor);
            if(forward) {
                if(currentLine >= label.getWorkingLayout().lines() - 1) {
                    cursor = text.length();
                    return;
                }
                int i = label.getWorkingLayout().countGlyphsBeforeLine(currentLine + 1);
                for (int n = i + label.getWorkingLayout().getLine(currentLine + 1).glyphs.size; i < n; i++) {
                    if(glyphPositions.get(i) + label.workingLayout.advances.get(i) * 0.5f > gp) break;
                }
                cursor = i;
            } else {
                if(currentLine <= 0) {
                    cursor = 0;
                    return;
                }
                int i = label.getWorkingLayout().countGlyphsBeforeLine(currentLine - 1);
                for (int n = i + label.getWorkingLayout().getLine(currentLine - 1).glyphs.size; i < n; i++) {
                    if(glyphPositions.get(i) + label.workingLayout.advances.get(i) * 0.5f > gp) break;
                }
                cursor = i;
            }
        }
    }
}
