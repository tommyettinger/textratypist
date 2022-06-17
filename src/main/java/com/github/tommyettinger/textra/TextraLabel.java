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

import static com.badlogic.gdx.utils.Align.*;
import static com.badlogic.gdx.utils.Align.left;

/**
 * A scene2d.ui Widget that displays text using a {@link Font} rather than a libGDX BitmapFont. This supports being
 * laid out in a Table just like the typical Label.
 */
public class TextraLabel extends Widget {
    public Layout layout;
    public Font font;
    public int align = Align.left;
    public boolean wrap = false;
    public String storedText;
    public Label.LabelStyle style;

    /**
     * Creates a TextraLabel that uses the default libGDX font (lsans-15 in the current version) with white color.
     */
    public TextraLabel(){
        layout = Layout.POOL.obtain();
        font = new Font(new BitmapFont(), Font.DistanceFieldType.STANDARD, 0, 0, 0, 0, false);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the specified Skin's LabelStyle.
     * @param text the text to use; may be multi-line, but will default to not wrapping
     * @param skin the default Label.LabelStyle will be obtained from this and used
     */
    public TextraLabel(String text, Skin skin) {
        this(text, skin.get(Label.LabelStyle.class));
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the specified Skin's LabelStyle.
     * @param text the text to use; may be multi-line, but will default to not wrapping
     * @param skin the default Label.LabelStyle will be obtained from this and used
     * @param makeGridGlyphs if true, the font should have a solid block glyph available, and underline/strikethrough
     *                      may be drawn more clearly; if false, underline/strikethrough will use underscore/dash
     */
    public TextraLabel(String text, Skin skin, boolean makeGridGlyphs) {
        this(text, skin.get(Label.LabelStyle.class), makeGridGlyphs);
    }
    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the specified style from the given
     * Skin.
     * @param text the text to use; may be multi-line, but will default to not wrapping
     * @param skin the default Label.LabelStyle will be obtained from this and used
     * @param styleName the name of a Label.LabelStyle to use from the Skin
     */
    public TextraLabel(String text, Skin skin, String styleName) {
        this(text, skin.get(styleName, Label.LabelStyle.class));
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the specified style from the given
     * Skin.
     * @param text the text to use; may be multi-line, but will default to not wrapping
     * @param skin the default Label.LabelStyle will be obtained from this and used
     * @param styleName the name of a Label.LabelStyle to use from the Skin
     * @param makeGridGlyphs if true, the font should have a solid block glyph available, and underline/strikethrough
     *                      may be drawn more clearly; if false, underline/strikethrough will use underscore/dash
     */
    public TextraLabel(String text, Skin skin, String styleName, boolean makeGridGlyphs) {
        this(text, skin.get(styleName, Label.LabelStyle.class), makeGridGlyphs);
    }
    /**
     * Creates a TextraLabel with the given text (which may be multi-line), using the specified style from the given
     * Skin, with the default Color overridden by the given one.
     * @param text the text to use; may be multi-line, but will default to not wrapping
     * @param skin the default Label.LabelStyle will be obtained from this and used
     * @param styleName the name of a Label.LabelStyle to use from the Skin
     * @param color the color to use for the font when unspecified (at the start and when reset)
     */
    public TextraLabel(String text, Skin skin, String styleName, Color color) {
        this(text, new Font(skin.get(styleName, Label.LabelStyle.class).font, Font.DistanceFieldType.STANDARD, 0, 0, 0, 0), color);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line), using the specified style from the given
     * Skin, with the default Color overridden by the color with the given name in the skin.
     * @param text the text to use; may be multi-line, but will default to not wrapping
     * @param skin the default Label.LabelStyle will be obtained from this and used
     * @param styleName the name of a Label.LabelStyle to use from the Skin
     * @param colorName the name in the skin of the color to use for the font when unspecified (at the start and when reset)
     */
    public TextraLabel(String text, Skin skin, String styleName, String colorName) {
        this(text, new Font(skin.get(styleName, Label.LabelStyle.class).font, Font.DistanceFieldType.STANDARD, 0, 0, 0, 0), skin.get(colorName, Color.class));
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the given style.
     * @param text the text to use; may be multi-line, but will default to not wrapping
     * @param style the Label.LabelStyle to use
     */
    public TextraLabel(String text, Label.LabelStyle style) {
        this(text, style, false);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the given style.
     * @param text the text to use; may be multi-line, but will default to not wrapping
     * @param style the Label.LabelStyle to use
     * @param makeGridGlyphs if true, the font should have a solid block glyph available, and underline/strikethrough
     *                      may be drawn more clearly; if false, underline/strikethrough will use underscore/dash
     */
    public TextraLabel(String text, Label.LabelStyle style, boolean makeGridGlyphs) {
        font = new Font(style.font, Font.DistanceFieldType.STANDARD, 0, 0, 0, 0, makeGridGlyphs);
        layout = Layout.POOL.obtain();
        if(style.fontColor != null) layout.setBaseColor(style.fontColor);
        this.style = style;
        storedText = text;
        font.markup(text, layout);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the given style.
     * @param text the text to use; may be multi-line, but will default to not wrapping
     * @param style the Label.LabelStyle to use, except for its font
     * @param replacementFont a Font that will be used in place of the one in style
     */
    public TextraLabel(String text, Label.LabelStyle style, Font replacementFont) {
        font = replacementFont;
        layout = Layout.POOL.obtain();
        if(style.fontColor != null) layout.setBaseColor(style.fontColor);
        this.style = style;
        storedText = text;
        font.markup(text, layout);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the given Font.
     * @param text the text to use; may be multi-line, but will default to not wrapping
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
     * @param text the text to use; may be multi-line, but will default to not wrapping
     * @param font a Font from this library, such as one obtained from {@link KnownFonts}
     * @param color the color to use for the font when unspecified (at the start and when reset)
     */
    public TextraLabel(String text, Font font, Color color) {
        this.font = font;
        layout = Layout.POOL.obtain();
        if(color != null) layout.setBaseColor(color);
        storedText = text;
        font.markup(text, layout);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        float baseX = 0, baseY = 0;

        baseX += getX();
        baseY += getY();

        float sn = MathUtils.sinDeg(getRotation());
        float cs = MathUtils.cosDeg(getRotation());

        if (style != null && style.background != null) {
            Drawable background = style.background;
            batch.setColor(getColor());
            if((align & Align.left) != 0) {
                baseX += cs * background.getLeftWidth();
                baseY += sn * background.getLeftWidth();
            }
            else if((align & Align.right) != 0) {
                baseX -= cs * background.getRightWidth();
                baseY -= sn * background.getRightWidth();
            }
            else {
                baseX += cs * (background.getLeftWidth() - background.getRightWidth()) * 0.5f;
                baseY += sn * (background.getLeftWidth() - background.getRightWidth()) * 0.5f;
            }
            if((align & bottom) != 0) {
                baseX -= sn * background.getBottomHeight();
                baseY += cs * background.getBottomHeight();
            }
            else if((align & top) != 0) {
                baseX += sn * background.getTopHeight();
                baseY -= cs * background.getTopHeight();
            }
            else {
                baseX -= sn * (background.getBottomHeight() * 0.5f); // - background.getTopHeight() * 0.5f
                baseY += cs * (background.getBottomHeight() * 0.5f); // - background.getTopHeight() * 0.5f
            }
            ((TransformDrawable)background).draw(batch, getX(), getY(), baseX, baseY,
//                    (Align.isLeft(align) ? 0f : Align.isRight(align) ? getWidth() : getWidth() * 0.5f),
//                    (Align.isBottom(align) ? 0f : Align.isTop(align) ? getHeight() : getHeight() * 0.5f),
                    getWidth(), getHeight(), 1f, 1f, getRotation());
        }
        if(layout.lines.isEmpty()) return;
        boolean resetShader = font.distanceField != Font.DistanceFieldType.STANDARD && batch.getShader() != font.shader;
        if(resetShader)
            font.enableShader(batch);
        batch.setColor(1f, 1f, 1f, parentAlpha);

        float height = layout.getHeight();
        if (Align.isBottom(align)) {
            baseX -= sn * height;
            baseY += cs * height;
        }
        else if (Align.isCenterVertical(align)) {
            baseX -= sn * height * 0.5f;
            baseY += cs * height * 0.5f;
        }

//        baseX -= sn * layout.lines.first().height * 0.25f;
//        baseY += cs * layout.lines.first().height * 0.25f;

        float width = getWidth();
        height = getHeight();
        if ((align & right) != 0)
        {
            baseX += cs * width;
            baseY += sn * width;
        }
        else if ((align & left) == 0) {
            baseX += cs * width * 0.5f;
            baseY += sn * width * 0.5f;
        }

        if ((align & top) != 0)
        {
            baseX -= sn * height;
            baseY += cs * height;
        }
        else if ((align & bottom) == 0) {
            baseX -= sn * height * 0.5f;
            baseY += cs * height * 0.5f;
        }
        font.drawGlyphs(batch, layout, baseX, baseY, align, getRotation());
        if(resetShader)
            batch.setShader(null);
    }

    @Override
    public float getPrefWidth() {
        return wrap ? 0f : (layout.getWidth() + (style != null && style.background != null ?
                style.background.getLeftWidth() + style.background.getRightWidth() : 0.0f));
    }

    @Override
    public float getPrefHeight() {
        return layout.getHeight() + (style != null && style.background != null ?
                style.background.getBottomHeight() + style.background.getTopHeight() : 0.0f);
    }

    public TextraLabel useIntegerPositions(boolean integer) {
        font.integerPosition = integer;
        return this;
    }

    /**
     * Gets the current wrapping mode. When wrap is enabled,
     * the preferred and/or min/max sizes must be used so this knows where to wrap. If wrap is disabled, lines that are
     * too wide will just widen the size of the widget.
     * @return whether this is currently wrapping
     */
    public boolean isWrap() {
        return wrap;
    }

    /**
     * Sets the wrapping mode; if this changes the mode, then this invalidates the hierarchy. When wrap is enabled,
     * the preferred and/or min/max sizes must be used so this knows where to wrap. If wrap is disabled, lines that are
     * too wide will just widen the size of the widget.
     * @param wrap whether to wrap or not
     */
    public void setWrap(boolean wrap) {
        if(this.wrap != (this.wrap = wrap))
            invalidateHierarchy();
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
    }

    @Override
    public void layout() {
        float width = getWidth();
        if(style != null && style.background != null) {
            layout.targetWidth = (width - (style.background.getLeftWidth() + style.background.getRightWidth()));
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
     * @see Align
     * @return the alignment used by this TextraLabel, as a constant from {@link Align}
     */
    public int getAlignment() {
        return align;
    }

    /**
     * Sets the alignment for the text in this TextraLabel.
     * @see Align
     * @param alignment a constant from {@link Align}
     */
    public void setAlignment (int alignment) {
        align = alignment;
    }

    /**
     * Changes the text in this TextraLabel to the given String, parsing any markup in it.
     * @param markupText a String that can contain Font markup
     */
    public void setText(String markupText) {
        storedText = markupText;
        layout.setTargetWidth(this.getMaxWidth());
        font.markup(markupText, layout.clear());
        setWidth(layout.getWidth() + (style != null && style.background != null ?
                style.background.getLeftWidth() + style.background.getRightWidth() : 0.0f));
    }
}
