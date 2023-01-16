/*
 * Copyright (c) 2021-2022 See AUTHORS file.
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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable;
import com.badlogic.gdx.utils.Align;

/**
 * A scene2d.ui Widget that displays text using a {@link Font} rather than a libGDX BitmapFont. This supports being
 * laid out in a Table just like the typical Label.
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
        layout = Layout.POOL.obtain();
        font = new Font(new BitmapFont(), Font.DistanceFieldType.STANDARD, 0, 0, 0, 0, false);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the specified Skin's LabelStyle.
     *
     * @param text the text to use; may be multi-line, and will default to wrapping
     * @param skin the default Label.LabelStyle will be obtained from this and used
     */
    public TextraLabel(String text, Skin skin) {
        this(text, skin.get(Label.LabelStyle.class));
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the specified Skin's LabelStyle.
     *
     * @param text           the text to use; may be multi-line, and will default to wrapping
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
     * @param text      the text to use; may be multi-line, and will default to wrapping
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
     * @param text           the text to use; may be multi-line, and will default to wrapping
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
     * @param text      the text to use; may be multi-line, and will default to wrapping
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
     * @param text      the text to use; may be multi-line, and will default to wrapping
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
     * @param text  the text to use; may be multi-line, and will default to wrapping
     * @param style the Label.LabelStyle to use
     */
    public TextraLabel(String text, Label.LabelStyle style) {
        this(text, style, false);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the given style.
     *
     * @param text           the text to use; may be multi-line, and will default to wrapping
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
     * @param text            the text to use; may be multi-line, and will default to wrapping
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
     * @param text            the text to use; may be multi-line, and will default to wrapping
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
     * @param text            the text to use; may be multi-line, and will default to wrapping
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
     * @param text            the text to use; may be multi-line, and will default to wrapping
     * @param style           the Label.LabelStyle to use, except for its font
     * @param replacementFont a Font that will be used in place of the one in style
     */
    public TextraLabel(String text, Label.LabelStyle style, Font replacementFont) {
        font = replacementFont;
        layout = Layout.POOL.obtain();
        if (style.fontColor != null) layout.setBaseColor(style.fontColor);
        this.style = style;
        storedText = text;
        font.markup(text, layout);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the given Font.
     *
     * @param text the text to use; may be multi-line, and will default to wrapping
     * @param font a Font from this library, such as one obtained from {@link KnownFonts}
     */
    public TextraLabel(String text, Font font) {
        this.font = font;
        layout = Layout.POOL.obtain();
        storedText = text;
        font.markup(text, layout);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line), using the given Font, and using the given
     * default color.
     *
     * @param text  the text to use; may be multi-line, and will default to wrapping
     * @param font  a Font from this library, such as one obtained from {@link KnownFonts}
     * @param color the color to use for the font when unspecified (at the start and when reset)
     */
    public TextraLabel(String text, Font font, Color color) {
        this.font = font;
        layout = Layout.POOL.obtain();
        if (color != null) layout.setBaseColor(color);
        storedText = text;
        font.markup(text, layout);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.getColor().set(getColor()).a *= parentAlpha;
        batch.setColor(batch.getColor());

        float baseX = 0, baseY = 0;

        float rot = getRotation();
        float sn = MathUtils.sinDeg(rot);
        float cs = MathUtils.cosDeg(rot);

        float height = layout.getHeight();
        if (Align.isBottom(align)) {
            baseX -= sn * height;
            baseY += cs * height;
        } else if (Align.isCenterVertical(align)) {
            baseX -= sn * height * 0.5f;
            baseY += cs * height * 0.5f;
        }

        float width = getWidth();
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
                    getOriginX(), getOriginY(), // origin
                    getWidth(), getHeight(),    // size
                    1f, 1f,                     // scale
                    rot);                       // rotation
        }
        if (layout.lines.isEmpty()) return;
        boolean resetShader = font.distanceField != Font.DistanceFieldType.STANDARD && batch.getShader() != font.shader;
        if (resetShader)
            font.enableShader(batch);

        baseX -= 0.5f * font.cellWidth;
        baseY -= 0.5f * font.cellHeight;

        font.drawGlyphs(batch, layout, getX() + baseX, getY() + baseY, align, rot, getOriginX(), getOriginY());

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
            if ((layout.getTargetWidth() != getWidth()))
                layout.setTargetWidth(width);
            font.regenerateLayout(layout);
        }

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
        layout.setTargetWidth(this.getWidth());
        font.markup(markupText, layout.clear());
        setWidth(layout.getWidth() + (style != null && style.background != null ?
                style.background.getLeftWidth() + style.background.getRightWidth() : 0.0f));
    }

    /**
     * By default, does nothing; this is overridden in TypingLabel to skip its text progression ahead.
     */
    public void skipToTheEnd() {
    }

}
