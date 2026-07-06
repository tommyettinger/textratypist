package com.github.tommyettinger.textra;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;

/**
 * A multiple-line {@link TextraField} using a {@link Font}. This allows SDF and MSDF fonts to be used for text entry,
 * as well as for emoji to be pasted into input fields. Entering emoji with an OS emoji picker doesn't work due to a
 * limitation of libGDX on desktop platforms, though emoji can be copied from or pasted into a TextraArea. This creates
 * a scroll bar if there are too many lines to be seen, and resizes the input area to account for the scroll bar. Unlike
 * a scene2d.ui TextArea, this needs a {@link ScrollPane.ScrollPaneStyle} to provide a visual for the scroll bar.
 */
public class TextraArea extends Container<ScrollPane> {
    /**
     * The actual widget that handles text entry; the rest of TextraArea only handles the scrolling through this widget.
     */
    public final InnerTextraArea inner;

    /**
     * Creates a TextraArea with the given initial text and skin to get styles from.
     * Unlike {@link com.badlogic.gdx.scenes.scene2d.ui.TextArea} in scene2d.ui, this shows a scrollbar, so it needs a
     * {@link ScrollPane.ScrollPaneStyle} given to style that scrollbar.
     * <br>
     * This gets a TextFieldStyle and a ScrollPaneStyle from skin. If a scene2d.ui TextFieldStyle is defined and an
     * FWSkin (or subclass) reads in the Skin JSON file, this will be able to load the right TextFieldStyle from Styles
     * in this package.
     *
     * @param text the initial text to hold in the TextraArea
     * @param skin a scene2d.ui Skin, typically an {@link FWSkin} or one of its subclasses
     */
    public TextraArea(@Null String text, Skin skin) {
        this(text, skin.get(Styles.TextFieldStyle.class), skin.get(ScrollPane.ScrollPaneStyle.class));
    }

    /**
     * Creates a TextraArea with the given initial text, skin to get styles from, and replacementFont.
     * Unlike {@link com.badlogic.gdx.scenes.scene2d.ui.TextArea} in scene2d.ui, this shows a scrollbar, so it needs a
     * {@link ScrollPane.ScrollPaneStyle} given to style that scrollbar.
     * <br>
     * Sets {@link Font#enableSquareBrackets} and {@link Font#omitCurlyBraces} each to false on
     * {@code replacementFont}. This does not copy replacementFont; you can reuse an existing Font with those two
     * fields set to false. Note that if you reuse a Font that is used in conjunction with markup, the modifications
     * this makes to that Font will make that markup remain unparsed. You will typically want a different Font for
     * your replacementFont used in TextraArea(s) than your Font(s) used elsewhere.
     * <br>
     * This gets a TextFieldStyle and a ScrollPaneStyle from skin. If a scene2d.ui TextFieldStyle is defined and an
     * FWSkin (or subclass) reads in the Skin JSON file, this will be able to load the right TextFieldStyle from Styles
     * in this package.
     *
     * @param text the initial text to hold in the TextraArea
     * @param skin a scene2d.ui Skin, typically an {@link FWSkin} or one of its subclasses
     * @param replacementFont a Font that will be modified in-place to diable brace/bracket markup parsing
     */
    public TextraArea(@Null String text, Skin skin, Font replacementFont) {
        this(text, skin.get(Styles.TextFieldStyle.class), skin.get(ScrollPane.ScrollPaneStyle.class), replacementFont);
    }

    /**
     * Creates a TextraArea with the given initial text, skin to get styles from, and names of styles.
     * Unlike {@link com.badlogic.gdx.scenes.scene2d.ui.TextArea} in scene2d.ui, this shows a scrollbar, so it needs a
     * {@link ScrollPane.ScrollPaneStyle} given to style that scrollbar.
     * <br>
     * This gets a TextFieldStyle and a ScrollPaneStyle from skin. If a scene2d.ui TextFieldStyle is defined and an
     * FWSkin (or subclass) reads in the Skin JSON file, this will be able to load the right TextFieldStyle from Styles
     * in this package.
     *
     * @param text the initial text to hold in the TextraArea
     * @param skin a scene2d.ui Skin, typically an {@link FWSkin} or one of its subclasses
     * @param styleName the name of a TextFieldStyle in skin
     * @param paneStyleName the name of a ScrollPaneStyle in skin
     */
    public TextraArea(@Null String text, Skin skin, String styleName, String paneStyleName) {
        this(text, skin.get(styleName, Styles.TextFieldStyle.class), skin.get(paneStyleName, ScrollPane.ScrollPaneStyle.class));
    }

    /**
     * Creates an InnerTextraArea with the given initial text and styles.
     * Unlike {@link com.badlogic.gdx.scenes.scene2d.ui.TextArea} in scene2d.ui, this shows a scrollbar, so it needs a
     * {@link ScrollPane.ScrollPaneStyle} given to style that scrollbar.
     * <br>
     * This assumes the {@link Styles.TextFieldStyle#font} has not been modified; the constructor for TextFieldStyle
     * copies any Font given to it so this can set {@link Font#enableSquareBrackets} and
     * {@link Font#omitCurlyBraces} each to false on its copy without affecting other widget styles.
     *
     * @param text the initial text to hold in the TextraArea
     * @param style a TextFieldStyle from {@link Styles}
     * @param paneStyle a scene2d.ui ScrollPaneStyle
     */
    public TextraArea(String text, Styles.TextFieldStyle style, ScrollPane.ScrollPaneStyle paneStyle) {
        super();
        inner = new InnerTextraArea(text, style);
        ScrollPane scrollPane = new ScrollPane(inner, paneStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollbarsOnTop(false);
        scrollPane.setFlickScroll(false);
        super.setActor(scrollPane);
    }

    /**
     * Creates a TextraArea with the given initial text, styles, and replacementFont.
     * Unlike {@link com.badlogic.gdx.scenes.scene2d.ui.TextArea} in scene2d.ui, this shows a scrollbar, so it needs a
     * {@link ScrollPane.ScrollPaneStyle} given to style that scrollbar.
     * <br>
     * Sets {@link Font#enableSquareBrackets} and {@link Font#omitCurlyBraces} each to false on
     * {@code replacementFont}. This does not copy replacementFont; you can reuse an existing Font with those two
     * fields set to false. Note that if you reuse a Font that is used in conjunction with markup, the modifications
     * this makes to that Font will make that markup remain unparsed. You will typically want a different Font for
     * your replacementFont used in TextraArea(s) than your Font(s) used elsewhere.
     *
     * @param text the initial text to hold in the TextraArea
     * @param style a TextFieldStyle from {@link Styles}
     * @param paneStyle a {@link ScrollPane.ScrollPaneStyle} from scene2d.ui
     * @param replacementFont a Font that will be modified in-place to diable brace/bracket markup parsing
     */
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
     * that gets scrolled through by the parent class. This class is not {@code static} because it needs to pass data
     * between itself and the parent that contains a ScrollPane for this widget.
     */
    public class InnerTextraArea extends TextraField {

        public InnerTextraArea(@Null String text, Skin skin) {
            this(text, skin.get(Styles.TextFieldStyle.class));
        }

        /**
         * Creates an InnerTextraArea with the given initial text, skin to get a style from, and replacementFont.
         * <br>
         * Sets {@link Font#enableSquareBrackets} and {@link Font#omitCurlyBraces} each to false on
         * {@code replacementFont}. This does not copy replacementFont; you can reuse an existing Font with those two
         * fields set to false. Note that if you reuse a Font that is used in conjunction with markup, the modifications
         * this makes to that Font will make that markup remain unparsed. You will typically want a different Font for
         * your replacementFont used in TextraArea(s) than your Font(s) used elsewhere.
         *
         * @param text the initial text to hold in the TextraArea
         * @param skin a scene2d.ui Skin, typically an {@link FWSkin} or one of its subclasses
         * @param replacementFont a Font that will be modified in-place to diable brace/bracket markup parsing
         */
        public InnerTextraArea(@Null String text, Skin skin, Font replacementFont) {
            this(text, skin.get(Styles.TextFieldStyle.class), replacementFont);
        }

        public InnerTextraArea(@Null String text, Skin skin, String styleName) {
            this(text, skin.get(styleName, Styles.TextFieldStyle.class));
        }

        /**
         * Creates an InnerTextraArea with the given initial text, style, and replacementFont.
         * <br>
         * This assumes the {@link Styles.TextFieldStyle#font} has not been modified; the constructor for TextFieldStyle
         * copies any Font given to it so this can set {@link Font#enableSquareBrackets} and
         * {@link Font#omitCurlyBraces} each to false on its copy without affecting other widget styles.
         *
         * @param text the initial text to hold in the TextraArea
         * @param style a TextFieldStyle from {@link Styles}
         */
        public InnerTextraArea(@Null String text, Styles.TextFieldStyle style) {
            super();
            Styles.TextFieldStyle s = new Styles.TextFieldStyle(style);
    //        s.font = new Font(style.font); // already done by TextFieldStyle constructor
            s.font.enableSquareBrackets = false;
            s.font.omitCurlyBraces = false;
            setStyle(s);
            label = new TypingLabel("", new Styles.LabelStyle(this.style.font, style.fontColor));
            label.setMaxLines(Integer.MAX_VALUE);
            label.setAlignment(Align.topLeft);
            label.setWrap(true);
            label.setSelectable(true);
            if(style.selection != null)
                label.selectionDrawable = style.selection;
            writeEnters = true;
            initialize();
            label.setWidth(getPrefWidth());
            setText(text == null ? "" : text);
            updateDisplayText();
        }

        /**
         * Creates an InnerTextraArea with the given initial text, style, and replacementFont.
         * <br>
         * Sets {@link Font#enableSquareBrackets} and {@link Font#omitCurlyBraces} each to false on
         * {@code replacementFont}. This does not copy replacementFont; you can reuse an existing Font with those two
         * fields set to false. Note that if you reuse a Font that is used in conjunction with markup, the modifications
         * this makes to that Font will make that markup remain unparsed. You will typically want a different Font for
         * your replacementFont used in TextraArea(s) than your Font(s) used elsewhere.
         *
         * @param text the initial text to hold in the TextraArea
         * @param style a TextFieldStyle from {@link Styles}
         * @param replacementFont a Font that will be modified in-place to diable brace/bracket markup parsing
         */
        public InnerTextraArea(@Null String text, Styles.TextFieldStyle style, Font replacementFont) {
            super();
            setStyle(style);
            replacementFont.enableSquareBrackets = false;
            replacementFont.omitCurlyBraces = false;
            label = new TypingLabel("", new Styles.LabelStyle(replacementFont, style.fontColor));
            label.setMaxLines(Integer.MAX_VALUE);
            label.setAlignment(Align.topLeft);
            label.setWrap(true);
            label.setSelectable(true);
            if(style.selection != null)
                label.selectionDrawable = style.selection;
            writeEnters = true;
            initialize();
            label.setWidth(getPrefWidth());
            setText(text == null ? "" : text);
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
        protected String processLineBreaks(String text){
            return text;
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
        protected void updateDisplayText() {
            super.updateDisplayText();
            invalidateHierarchy();
        }

        @Override
        protected void sizeChanged() {
            label.setSize(getWidth(), getHeight());
            updateDisplayText();
        }

        @Override
        protected boolean insert(int position, CharSequence inserting) {
            if(super.insert(position, inserting)){
                if(TextraArea.this.getActor() == null) return true;
                float scrollPos = label.getHeight(), latestLineHeight = 1;
                int currentLine = label.getLineIndexInLayout(label.workingLayout, cursor);
                for (int ln = 0; ln < currentLine; ln++) {
                    scrollPos -= (latestLineHeight = label.getLineInLayout(label.workingLayout, ln).height);
                }

                TextraArea.this.getActor().scrollTo(0, scrollPos, 1, latestLineHeight);
                return true;
            }
            return false;
        }
    }
}
