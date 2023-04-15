/*
 * Copyright (c) 2021-2023 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.tommyettinger.textra;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.LongArray;

import static com.github.tommyettinger.textra.Font.ALTERNATE;

/**
 * A scene2d.ui Widget that displays text using a {@link Font} rather than a libGDX BitmapFont. This supports being
 * laid out in a Table just like the typical Label (when {@link #isWrap() wrap} is false, which is the default).
 */
public class TextraLabel extends Widget {
    public Layout layout;
    protected Font font;
    public int align = Align.left;
    /**
     * If true; allows text to wrap when it would go past the layout's {@link Layout#getTargetWidth() targetWidth} and
     * continue on the next line; if false, uses a very long target width and only adds newlines when they are in the
     * label's text. This should typically be false for widgets that use scene2d.ui layout, but should be true for
     * any widget that dynamically adjusts to fill an area with wrapped text.
     */
    public boolean wrap = false;
    public String storedText;
    public Label.LabelStyle style;

    /**
     * Creates a TextraLabel that uses the default libGDX font (lsans-15 in the current version) with white color.
     */
    public TextraLabel() {
        layout = new Layout();
        font = new Font();
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the specified Skin's LabelStyle.
     *
     * @param text the text to use; may be multi-line, but will default to not wrapping
     * @param skin the default Label.LabelStyle will be obtained from this and used
     */
    public TextraLabel(String text, Skin skin) {
        this(text, skin.get(Label.LabelStyle.class));
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the specified Skin's LabelStyle.
     *
     * @param text           the text to use; may be multi-line, but will default to not wrapping
     * @param skin           the default Label.LabelStyle will be obtained from this and used
     * @param makeGridGlyphs if true, the font should have a solid block glyph available, and underline/strikethrough
     *                       may be drawn more clearly; if false, underline/strikethrough will use underscore/dash
     */
    public TextraLabel(String text, Skin skin, boolean makeGridGlyphs) {
        this(text, skin.get(Label.LabelStyle.class), makeGridGlyphs);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the specified style from the given
     * Skin.
     *
     * @param text      the text to use; may be multi-line, but will default to not wrapping
     * @param skin      the named Label.LabelStyle will be obtained from this and used
     * @param styleName the name of a Label.LabelStyle to use from the Skin
     */
    public TextraLabel(String text, Skin skin, String styleName) {
        this(text, skin.get(styleName, Label.LabelStyle.class));
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the specified style from the given
     * Skin.
     *
     * @param text           the text to use; may be multi-line, but will default to not wrapping
     * @param skin           the named Label.LabelStyle will be obtained from this and used
     * @param styleName      the name of a Label.LabelStyle to use from the Skin
     * @param makeGridGlyphs if true, the font should have a solid block glyph available, and underline/strikethrough
     *                       may be drawn more clearly; if false, underline/strikethrough will use underscore/dash
     */
    public TextraLabel(String text, Skin skin, String styleName, boolean makeGridGlyphs) {
        this(text, skin.get(styleName, Label.LabelStyle.class), makeGridGlyphs);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line), using the specified style from the given
     * Skin, with the default Color overridden by the given one.
     *
     * @param text      the text to use; may be multi-line, but will default to not wrapping
     * @param skin      the named Label.LabelStyle will be obtained from this and used
     * @param styleName the name of a Label.LabelStyle to use from the Skin
     * @param color     the color to use for the font when unspecified (at the start and when reset)
     */
    public TextraLabel(String text, Skin skin, String styleName, Color color) {
        this(text, skin.get(styleName, Label.LabelStyle.class));
        if(color != null) layout.setBaseColor(color);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line), using the specified style from the given
     * Skin, with the default Color overridden by the color with the given name in the skin.
     *
     * @param text      the text to use; may be multi-line, but will default to not wrapping
     * @param skin      the named Label.LabelStyle will be obtained from this and used
     * @param styleName the name of a Label.LabelStyle to use from the Skin
     * @param colorName the name in the skin of the color to use for the font when unspecified (at the start and when reset)
     */
    public TextraLabel(String text, Skin skin, String styleName, String colorName) {
        this(text, skin.get(styleName, Label.LabelStyle.class));
        Color color = skin.get(colorName, Color.class);
        if(color != null) layout.setBaseColor(color);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the given style.
     *
     * @param text  the text to use; may be multi-line, but will default to not wrapping
     * @param style the Label.LabelStyle to use
     */
    public TextraLabel(String text, Label.LabelStyle style) {
        this(text, style, false);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the given style.
     *
     * @param text           the text to use; may be multi-line, but will default to not wrapping
     * @param style          the Label.LabelStyle to use
     * @param makeGridGlyphs if true, the font should have a solid block glyph available, and underline/strikethrough
     *                       may be drawn more clearly; if false, underline/strikethrough will use underscore/dash
     */
    public TextraLabel(String text, Label.LabelStyle style, boolean makeGridGlyphs) {
        this(text, style, new Font(style.font, Font.DistanceFieldType.STANDARD, 0, 0, 0, 0, makeGridGlyphs));
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the default style from
     * a Skin, replacing any font that would be drawn from the style with {@code replacementFont}.
     *
     * @param text            the text to use; may be multi-line, but will default to not wrapping
     * @param skin            the default Label.LabelStyle will be obtained from this and used
     * @param replacementFont a Font that will be used in place of the one in style
     */
    public TextraLabel(String text, Skin skin, Font replacementFont) {
        this(text, skin.get(Label.LabelStyle.class), replacementFont);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the given style taken by name from
     * a Skin, replacing any font that would be drawn from the style with {@code replacementFont}.
     *
     * @param text            the text to use; may be multi-line, but will default to not wrapping
     * @param skin            the named Label.LabelStyle will be obtained from this and used
     * @param styleName       the name of a Label.LabelStyle to use from the Skin
     * @param replacementFont a Font that will be used in place of the one in style
     */
    public TextraLabel(String text, Skin skin, String styleName, Font replacementFont) {
        this(text, skin.get(styleName, Label.LabelStyle.class), replacementFont);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the given style taken by name from
     * a Skin, replacing any font that would be drawn from the style with {@code replacementFont}.
     *
     * @param text            the text to use; may be multi-line, but will default to not wrapping
     * @param skin            the named Label.LabelStyle will be obtained from this and used
     * @param styleName       the name of a Label.LabelStyle to use from the Skin
     * @param replacementFont a Font that will be used in place of the one in style
     * @param color           the base color to use for the label, used when reset
     */
    public TextraLabel(String text, Skin skin, String styleName, Font replacementFont, Color color) {
        this(text, skin.get(styleName, Label.LabelStyle.class), replacementFont);
        if (color != null) layout.setBaseColor(color);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the given style.
     *
     * @param text            the text to use; may be multi-line, but will default to not wrapping
     * @param style           the Label.LabelStyle to use, except for its font
     * @param replacementFont a Font that will be used in place of the one in style
     */
    public TextraLabel(String text, Label.LabelStyle style, Font replacementFont) {
        font = replacementFont;
        layout = new Layout();
        if (style.fontColor != null) layout.setBaseColor(style.fontColor);
        this.style = style;
        storedText = text;
        font.markup(text, layout);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the given Font.
     *
     * @param text the text to use; may be multi-line, but will default to not wrapping
     * @param font a Font from this library, such as one obtained from {@link KnownFonts}
     */
    public TextraLabel(String text, Font font) {
        this.font = font;
        layout = new Layout();
        storedText = text;
        font.markup(text, layout);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line), using the given Font, and using the given
     * default color.
     *
     * @param text  the text to use; may be multi-line, but will default to not wrapping
     * @param font  a Font from this library, such as one obtained from {@link KnownFonts}
     * @param color the color to use for the font when unspecified (at the start and when reset)
     */
    public TextraLabel(String text, Font font, Color color) {
        this.font = font;
        layout = new Layout();
        if (color != null) layout.setBaseColor(color);
        storedText = text;
        font.markup(text, layout);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.validate();

        //TODO: Figure out how expensive this is to call frequently; see if we can limit how many calls we make.
        float oldWidth = layout.getWidth();
        float oldHeight = layout.getHeight();
        font.calculateSize(layout);
        float height = layout.getHeight();
        float width = layout.getWidth();
        if(oldWidth != width || oldHeight != height)
            invalidateHierarchy();
        final float rot = getRotation();
        final float originX = getOriginX();
        final float originY = getOriginY();
        final float sn = MathUtils.sinDeg(rot);
        final float cs = MathUtils.cosDeg(rot);

        int bgc;
        final int lines = layout.lines();
        float baseX = getX(), baseY = getY();

        if (Align.isBottom(align)) {
            baseX -= sn * height;
            baseY += cs * height;
        } else if (Align.isCenterVertical(align)) {
            baseX -= sn * height * 0.5f;
            baseY += cs * height * 0.5f;
        }
        width = getWidth();
        height = getHeight();
        if (Align.isRight(align)) {
            baseX += cs * width;
            baseY += sn * width;
        } else if (Align.isCenterHorizontal(align)) {
            baseX += cs * width * 0.5f;
            baseY += sn * width * 0.5f;
        }

        if (Align.isTop(align)) {
            baseX -= sn * height;
            baseY += cs * height;
        } else if (Align.isCenterVertical(align)) {
            baseX -= sn * height * 0.5f;
            baseY += cs * height * 0.5f;
        }
        if (style != null && style.background != null) {
            Drawable background = style.background;
            if (Align.isLeft(align)) {
                baseX += cs * background.getLeftWidth();
                baseY += sn * background.getLeftWidth();
            } else if (Align.isRight(align)) {
                baseX -= cs * background.getRightWidth();
                baseY -= sn * background.getRightWidth();
            } else {
                baseX += cs * (background.getLeftWidth() - background.getRightWidth()) * 0.5f;
                baseY += sn * (background.getLeftWidth() - background.getRightWidth()) * 0.5f;
            }
            if (Align.isBottom(align)) {
                baseX -= sn * background.getBottomHeight();
                baseY += cs * background.getBottomHeight();
            } else if (Align.isTop(align)) {
                baseX += sn * background.getTopHeight();
                baseY -= cs * background.getTopHeight();
            } else {
                baseX -= sn * (background.getBottomHeight() - background.getTopHeight()) * 0.5f;
                baseY += cs * (background.getBottomHeight() - background.getTopHeight()) * 0.5f;
            }
            ((TransformDrawable) background).draw(batch,
                    getX(), getY(),             // position
                    originX, originY,           // origin
                    getWidth(), getHeight(),    // size
                    1f, 1f,                     // scale
                    rot);                       // rotation
        }

        if (layout.lines.isEmpty() || parentAlpha <= 0f) return;

        // we only change the shader or batch color if we actually are drawing something.
        boolean resetShader = font.distanceField != Font.DistanceFieldType.STANDARD && batch.getShader() != font.shader;
        if (resetShader)
            font.enableShader(batch);
        batch.getColor().set(getColor()).a *= parentAlpha;
        batch.setColor(batch.getColor());

//        baseX -= 0.5f * font.cellWidth;
//
//        baseX += cs * 0.5f * font.cellWidth;
//        baseY += sn * 0.5f * font.cellWidth;
//        baseX -= sn * 0.5f * (font.cellHeight);
//        baseY += cs * 0.5f * (font.cellHeight);

        float single;

        for (int ln = 0; ln < lines; ln++) {
            Line glyphs = layout.getLine(ln);

            baseX += sn * glyphs.height;
            baseY -= cs * glyphs.height;
            if (glyphs.glyphs.size == 0)
                continue;

            float x = baseX, y = baseY;

            final float worldOriginX = x + originX;
            final float worldOriginY = y + originY;
            float fx = -originX;
            float fy = -originY;
            x = cs * fx - sn * fy + worldOriginX;
            y = sn * fx + cs * fy + worldOriginY;


            float xChange = 0, yChange = 0;

            if (Align.isCenterHorizontal(align)) {
                x -= cs * (glyphs.width * 0.5f);
                y -= sn * (glyphs.width * 0.5f);
            } else if (Align.isRight(align)) {
                x -= cs * glyphs.width;
                y -= sn * glyphs.width;
            }

            Font f = null;
            int kern = -1;
            for (int i = 0, n = glyphs.glyphs.size; i < n; i++) {
                long glyph = glyphs.glyphs.get(i);
                if (font.family != null) f = font.family.connected[(int) (glyph >>> 16 & 15)];
                if (f == null) f = font;
                if(i == 0){
                    x -= 0.5f * f.cellWidth;
                    x += cs * 0.5f * f.cellWidth;
                    y += sn * 0.5f * f.cellWidth;

                    y += f.descent;
                    x += sn * (f.descent - 0.5f * f.cellHeight);
                    y -= cs * (f.descent - 0.5f * f.cellHeight);

//                    x -= sn * 0.5f * f.cellHeight;
//                    y += cs * 0.5f * f.cellHeight;
                }

                if (f.kerning != null) {
                    kern = kern << 16 | (int) ((glyph = glyphs.glyphs.get(i)) & 0xFFFF);
                    float amt = f.kerning.get(kern, 0) * f.scaleX * ((glyph & ALTERNATE) != 0L ? 1f : ((glyph + 0x300000L >>> 20 & 15) + 1) * 0.25f);
                    xChange += cs * amt;
                    yChange += sn * amt;
                } else {
                    kern = -1;
                }
                if (i == 0) {
                    Font.GlyphRegion reg = font.mapping.get((char) glyph);
                    if (reg != null && reg.offsetX < 0) {
                        float ox = reg.offsetX * f.scaleX * ((glyph & ALTERNATE) != 0L ? 1f : ((glyph + 0x300000L >>> 20 & 15) + 1) * 0.25f);
                        xChange -= cs * ox;
                        yChange -= sn * ox;
                    }
                }
                bgc = 0;
                float xx = x + xChange;
                float yy = y + yChange;
                single = f.drawGlyph(batch, glyph, xx, yy, rot, 1f, 1f, bgc);
                xChange += cs * single;
                yChange += sn * single;
            }
        }

        if (resetShader)
            batch.setShader(null);
    }

    @Override
    public float getPrefWidth() {
        if(wrap) return 0f;
        float width = layout.getWidth();
        if(style != null && style.background != null)
            width = Math.max(width + style.background.getLeftWidth() + style.background.getRightWidth(), style.background.getMinWidth());
        return width;
    }

    @Override
    public float getPrefHeight() {
        float height = layout.getHeight();
        if(style != null && style.background != null)
                height = Math.max(height + style.background.getBottomHeight() + style.background.getTopHeight(), style.background.getMinHeight());
//        System.out.println("Calculated PrefHeight to be " + height);
        return height;
    }

    public TextraLabel useIntegerPositions(boolean integer) {
        font.integerPosition = integer;
        return this;
    }

    /**
     * Gets the current wrapping mode. When wrap is enabled,
     * the preferred and/or min/max sizes must be used so this knows where to wrap. If wrap is disabled, lines that are
     * too wide will just widen the size of the widget.
     *
     * @return whether this is currently wrapping
     */
    public boolean isWrap() {
        return wrap;
    }

    /**
     * Sets the wrapping mode; if this changes the mode, then this invalidates the hierarchy. When wrap is enabled,
     * the preferred and/or min/max sizes must be used so this knows where to wrap. If wrap is disabled, lines that are
     * too wide will just widen the size of the widget.
     *
     * @param wrap whether to wrap or not
     */
    public TextraLabel setWrap(boolean wrap) {
        if (this.wrap != (this.wrap = wrap))
            invalidateHierarchy();
        return this;
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        if (wrap) {
            layout.setTargetWidth(width);
        }
        font.calculateSize(layout);
        invalidateHierarchy();
    }

    @Override
    public void layout() {
        float width = getWidth();
        if (style != null && style.background != null) {
            width = (width - (style.background.getLeftWidth() + style.background.getRightWidth()));
        }
        if (wrap && layout.getTargetWidth() != width) {
            layout.setTargetWidth(width);
            font.regenerateLayout(layout);
            // This was used earlier, but regenerateLayout() seems to work better in its place.
//            font.markup(storedText, layout.clear());
            invalidateHierarchy();
        }
    }

    /**
     * Gets the alignment for the text in this TextraLabel.
     * This is a constant in {@link Align}.
     *
     * @return the alignment used by this TextraLabel, as a constant from {@link Align}
     * @see Align
     */
    public int getAlignment() {
        return align;
    }

    /**
     * Sets the alignment for the text in this TextraLabel.
     *
     * @param alignment a constant from {@link Align}
     * @see Align
     */
    public void setAlignment(int alignment) {
        align = alignment;
    }

    public Font getFont() {
        return font;
    }

    /**
     * Sets the font to the specified Font and then regenerates the layout using {@link Font#regenerateLayout(Layout)}.
     * This is equivalent to calling {@link #setFont(Font, boolean)} with true for regenerate.
     * This won't regenerate the layout if the given font is equal to the current font for this TextraLabel.
     * @param font the non-null font to use for this TextraLabel
     */
    public void setFont(Font font) {
        if(!this.font.equals(this.font = font))
            font.regenerateLayout(layout);
    }

    /**
     * Just like {@link #setFont(Font)}, except this only regenerates the layout if {@code regenerate} is true.
     * (To contrast, {@link #setFont(Font)} always regenerates the layout.)
     * This won't regenerate the layout if the given font is equal to the current font for this TextraLabel.
     * @param font the non-null font to use for this TextraLabel
     * @param regenerate if true, the layout will be re-wrapped and its size re-calculated for the new font
     */
    public void setFont(Font font, boolean regenerate) {
        if(!this.font.equals(this.font = font) && regenerate)
            font.regenerateLayout(layout);
    }

    /**
     * Changes the text in this TextraLabel to the given String, parsing any markup in it.
     *
     * @param markupText a String that can contain Font markup
     */
    public void setText(String markupText) {
        storedText = markupText;
//        if(wrap)
//            layout.setTargetWidth(getWidth());
//        else
//            layout.setTargetWidth(0f);
        font.markup(markupText, layout.clear());
//        setWidth(layout.getWidth() + (style != null && style.background != null ?
//                style.background.getLeftWidth() + style.background.getRightWidth() : 0.0f));
        invalidateHierarchy();
    }

    /**
     * By default, does nothing; this is overridden in TypingLabel to skip its text progression ahead.
     */
    public void skipToTheEnd() {
    }

    @Override
    public boolean remove() {
        return super.remove();

    }

    /**
     * Called by the framework when this actor or any ascendant is added to a group that is in the stage.
     * This is overridden as public instead of protected because most of its usage in scene2d.ui code is not actually
     * in inheriting classes, but in other classes in the same package. That's a problem here, so we make it public.
     * @param stage May be null if the actor or any ascendant is no longer in a stage.
     */
    @Override
    public void setStage(Stage stage) {
        super.setStage(stage);
    }

    /**
     * Called by the framework when an actor is added to or removed from a group.
     * This is overridden as public instead of protected because most of its usage in scene2d.ui code is not actually
     * in inheriting classes, but in other classes in the same package. That's a problem here, so we make it public.
     * @param parent May be null if the actor has been removed from the parent.
     */
    @Override
    protected void setParent(Group parent) {
        super.setParent(parent);
    }

    @Override
    public String toString() {
        return substring(0, Integer.MAX_VALUE);
    }

    /**
     * Gets a glyph from this label's {@link #layout}, where a glyph is a {@code long} encoded how {@link Font} uses it.
     * In a TextraLabel, this is effectively equivalent to {@link TypingLabel#getInWorkingLayout(int)}, but it may be
     * different from that method in a TypingLabel, depending on word wrap.
     * @param index the 0-based index of the glyph to retrieve
     * @return the glyph, if it was found, or 16777215 (0xFFFFFF in hexadecimal) if the index was out of bounds
     */
    public long getGlyph(int index) {
        for (int i = 0, n = layout.lines(); i < n && index >= 0; i++) {
            LongArray glyphs = layout.getLine(i).glyphs;
            if (index < glyphs.size)
                return glyphs.get(index);
            else
                index -= glyphs.size;
        }
        return 0xFFFFFFL;
    }

    /**
     * The maximum number of {@link Line}s this label can display.
     *
     * @return the maximum number of {@link Line} objects this label can display
     */
    public int getMaxLines() {
        return layout.maxLines;
    }

    /**
     * Sets the maximum number of {@link Line}s this Layout can display; this is always at least 1.
     * For effectively unlimited lines, pass {@link Integer#MAX_VALUE} to this.
     *
     * @param maxLines the limit for how many Line objects this Layout can display; always 1 or more
     */
    public void setMaxLines(int maxLines) {
        layout.setMaxLines(maxLines);
    }

    /**
     * Gets the ellipsis, which may be null, or may be a String that can be placed at the end of the text if its
     * max lines are exceeded.
     *
     * @return an ellipsis String or null
     */
    public String getEllipsis() {
        return layout.ellipsis;
    }

    /**
     * Sets the ellipsis text, which replaces the last few glyphs if non-null and the text added would exceed the
     * {@link #getMaxLines()} of this label's layout. For the ellipsis to appear, this has to be called with a
     * non-null String (often {@code "..."}, or {@code "…"} if the font supports it), and
     * {@link #setMaxLines(int)} needs to have been called with a small enough number, such as 1.
     *
     * @param ellipsis a String for a Layout to end with if its max lines are exceeded, or null to avoid such truncation
     */
    public void setEllipsis(String ellipsis) {
        layout.setEllipsis(ellipsis);
    }

    /**
     * Gets a String from the layout of this label, made of only the char portions of the glyphs from start
     * (inclusive) to end (exclusive). This can retrieve text from across multiple lines.
     * @param start inclusive start index
     * @param end exclusive end index
     * @return a String made of only the char portions of the glyphs from start to end
     */
    public String substring(int start, int end) {
        start = Math.max(0, start);
        end = Math.min(layout.countGlyphs(), end);
        int index = start;
        StringBuilder sb = new StringBuilder(end - start);
        int glyphCount = 0;
        for (int i = 0, n = layout.lines(); i < n && index >= 0; i++) {
            LongArray glyphs = layout.getLine(i).glyphs;
            if (index < glyphs.size) {
                for (int fin = index - start - glyphCount + end; index < fin && index < glyphs.size; index++) {
                    char c = (char) glyphs.get(index);
                    if (c >= 0xE000 && c <= 0xF800) {
                        String name = font.namesByCharCode.get(c);
                        if (name != null) sb.append(name);
                        else sb.append(c);
                    } else {
                        if (c == 2) sb.append('[');
                        else sb.append(c);
                    }
                    glyphCount++;
                }
                if(glyphCount == end - start)
                    return sb.toString();
                index = 0;
            }
            else
                index -= glyphs.size;
        }
        return "";
    }

    /**
     * Gets the height of the Line containing the glyph at the given index. If the index is out of bounds, this just
     * returns {@link Font#cellHeight}.
     * @param index the 0-based index of the glyph to measure
     * @return the height of the Line containing the specified glyph
     */
    public float getLineHeight(int index) {
        for (int i = 0, n = layout.lines(); i < n && index >= 0; i++) {
            LongArray glyphs = layout.getLine(i).glyphs;
            if (index < glyphs.size)
                return layout.getLine(i).height;
            else
                index -= glyphs.size;
        }
        return font.cellHeight;
    }

}
