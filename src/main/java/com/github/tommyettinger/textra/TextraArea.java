package com.github.tommyettinger.textra;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;

/**
 * A multiple-line {@link TextraField} using a {@link Font}.
 */
public class TextraArea extends Container<ScrollPane> {
    public final InnerTextraArea inner;

    public TextraArea(@Null String text, Skin skin) {
        this(text, skin.get(Styles.TextFieldStyle.class), skin.get(ScrollPane.ScrollPaneStyle.class));
    }

    public TextraArea(@Null String text, Skin skin, Font replacementFont) {
        this(text, skin.get(Styles.TextFieldStyle.class), skin.get(ScrollPane.ScrollPaneStyle.class), replacementFont);
    }

    public TextraArea(@Null String text, Skin skin, String styleName, String paneStyleName) {
        this(text, skin.get(styleName, Styles.TextFieldStyle.class), skin.get(paneStyleName, ScrollPane.ScrollPaneStyle.class));
    }

    public TextraArea(String text, Styles.TextFieldStyle style, ScrollPane.ScrollPaneStyle paneStyle) {
        super();
        inner = new InnerTextraArea(text, style);
        ScrollPane scrollPane = new ScrollPane(inner, paneStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollbarsOnTop(false);
        scrollPane.setFlickScroll(false);
        super.setActor(scrollPane);
    }

    public TextraArea(String text, Styles.TextFieldStyle style, ScrollPane.ScrollPaneStyle paneStyle, Font replacementFont) {
        super();
        inner = new InnerTextraArea(text, style, replacementFont);
        ScrollPane scrollPane = new ScrollPane(inner, paneStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollbarsOnTop(false);
        scrollPane.setFlickScroll(false);
        super.setActor(scrollPane);
    }

    public Font getFont() {
        return inner.label.getFont();
    }

    public void setFont(Font font) {
        inner.label.setFont(font);
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        super.size(getWidth(), getHeight());
    }

    /**
     * A multiple-line {@link TextraField} using a {@link Font}; this is the inner multi-line text entry field
     * that gets scrolled through by the parent class.
     */
    public class InnerTextraArea extends TextraField {

        public InnerTextraArea(@Null String text, Skin skin) {
            this(text, skin.get(Styles.TextFieldStyle.class));
        }

        public InnerTextraArea(@Null String text, Skin skin, Font replacementFont) {
            this(text, skin.get(Styles.TextFieldStyle.class), replacementFont);
        }

        public InnerTextraArea(@Null String text, Skin skin, String styleName) {
            this(text, skin.get(styleName, Styles.TextFieldStyle.class));
        }

        public InnerTextraArea(String text, Styles.TextFieldStyle style) {
            super();
            Styles.TextFieldStyle s = new Styles.TextFieldStyle(style);
    //        s.font = new Font(style.font); // already done by TextFieldStyle constructor
            s.font.enableSquareBrackets = false;
            s.font.omitCurlyBraces = false;
            s.font.hardWrap = true;
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

        public InnerTextraArea(String text, Styles.TextFieldStyle style, Font replacementFont) {
            super();
            setStyle(style);
            replacementFont = new Font(replacementFont);
            replacementFont.enableSquareBrackets = false;
            replacementFont.omitCurlyBraces = false;
            replacementFont.hardWrap = true;
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
            if(cursor == 0) {
                cursorPatch.draw(batch,
                        x + textOffset + fontOffset,
                        y + label.getHeight() - label.font.cellHeight, cursorPatch.getMinWidth(), label.font.cellHeight);
                return;
            }
            if(cursor >= label.length()){
                final int c = label.length() - 1;
                final float layoutHeight = label.getHeight(), linesHeight = label.getCumulativeLineHeight(c),
                        lineHeight = label.getLineHeight(c);

                cursorPatch.draw(batch,
                        x + textOffset + glyphPositions.get(c) + Font.xAdvance(label.font, label.workingLayout.advances.get(c), label.getInWorkingLayout(c)) * 0.5f + fontOffset,
                        y + layoutHeight - linesHeight, cursorPatch.getMinWidth(), lineHeight);
                return;
            }

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
        protected void moveCursor(boolean forward, boolean jump) {
            super.moveCursor(forward, jump);
            if(jump) {
                TextraArea.this.getActor().scrollTo(0, forward ? 0 : label.getHeight(), 1, TextraArea.this.getHeight());
            }
            else {
                int currentLine = label.getLineIndexInLayout(label.workingLayout, cursor);
                if(forward) {
                    if(currentLine >= label.getWorkingLayout().lines() - 1) {
                        TextraArea.this.getActor().scrollTo(0, 0, 1, TextraArea.this.getHeight());
                        return;
                    }
                } else {
                    if(currentLine <= 0) {
                        TextraArea.this.getActor().scrollTo(0, label.getHeight(), 1, TextraArea.this.getHeight());
                        return;
                    }
                }
                float scrollPos = label.getHeight(), latestLineHeight = 1;
                for (int ln = 0; ln < currentLine; ln++) {
                    scrollPos -= (latestLineHeight = label.getLineInLayout(label.workingLayout, ln).height);
                }
                TextraArea.this.getActor().scrollTo(0, scrollPos, 1, latestLineHeight);
            }

        }

        @Override
        protected void moveCursorVertically(boolean forward, boolean jump) {
            if(jump) {
                cursor = forward ? text.length() : 0;
                TextraArea.this.getActor().scrollTo(0, forward ? 0 : label.getHeight(), 1, TextraArea.this.getHeight());
            }
            else {
                float gp = glyphPositions.get(cursor);
                int currentLine = label.getLineIndexInLayout(label.workingLayout, cursor);
                if(forward) {
                    if(currentLine >= label.getWorkingLayout().lines() - 1) {
                        cursor = text.length();
                        TextraArea.this.getActor().scrollTo(0, 0, 1, TextraArea.this.getHeight());
                        return;
                    }
                    int i = label.getWorkingLayout().countGlyphsBeforeLine(currentLine + 1);
                    float scrollPos = label.getHeight(), latestLineHeight = 1;
                    for (int ln = 0; ln < currentLine + 1; ln++) {
                        scrollPos -= (latestLineHeight = label.getLineInLayout(label.workingLayout, ln).height);
                    }
                    TextraArea.this.getActor().scrollTo(0, scrollPos, 1, latestLineHeight);

                    for (int n = i + label.getWorkingLayout().getLine(currentLine + 1).glyphs.size; i < n; i++) {
                        if(glyphPositions.get(i) + label.workingLayout.advances.get(i) * 0.5f > gp) break;
                    }
                    cursor = i;
                } else {
                    if(currentLine <= 0) {
                        cursor = 0;
                        TextraArea.this.getActor().scrollTo(0, label.getHeight(), 1, TextraArea.this.getHeight());
                        return;
                    }
                    int i = label.getWorkingLayout().countGlyphsBeforeLine(currentLine - 1);
                    float scrollPos = label.getHeight(), latestLineHeight = 1;
                    for (int ln = 0; ln < currentLine - 1; ln++) {
                        scrollPos -= (latestLineHeight = label.getLineInLayout(label.workingLayout, ln).height);
                    }
                    TextraArea.this.getActor().scrollTo(0, scrollPos, 1, latestLineHeight);

                    for (int n = i + label.getWorkingLayout().getLine(currentLine - 1).glyphs.size; i < n; i++) {
                        if(glyphPositions.get(i) + label.workingLayout.advances.get(i) * 0.5f > gp) break;
                    }
                    cursor = i;
                }
            }
        }

        @Override
        protected void sizeChanged() {
            super.sizeChanged();
            if(TextraArea.this.getActor() == null) return;
            float scrollPos = label.getHeight(), latestLineHeight = 1;
            int currentLine = label.getLineIndexInLayout(label.workingLayout, cursor);
            for (int ln = 0; ln < currentLine; ln++) {
                scrollPos -= (latestLineHeight = label.getLineInLayout(label.workingLayout, ln).height);
            }
            TextraArea.this.getActor().scrollTo(0, scrollPos, 1, latestLineHeight);

        }
    }
}
