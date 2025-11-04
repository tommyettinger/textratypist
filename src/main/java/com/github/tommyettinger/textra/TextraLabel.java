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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.LongArray;

import static com.github.tommyettinger.textra.Font.ALTERNATE;

/**
 * A scene2d.ui Widget that displays text using a {@link Font} rather than a libGDX BitmapFont. This supports being
 * laid out in a Table just like the typical Label (when {@link #isWrap() wrap} is false, which is the default). This
 * permits square-bracket tag markup from Font, such as {@code [light blue]} to change the font color, or {@code [_]} to
 * underline text. It does not support the curly-brace token markup that its subclass {@link TypingLabel} does, nor does
 * this handle input in the way TypingLabel can. It also, naturally, doesn't have the typing effect TypingLabel does,
 * which makes this more suitable for some kinds of text. TypingLabel can be told to immediately
 * {@link TypingLabel#skipToTheEnd()}, which does make it look like a TextraLabel, but permits effects.
 * <br>
 * This is meant to work with {@link FWSkin} or one of its subclasses, such as {@code FreeTypistSkin}, and isn't
 * guaranteed to work with a regular {@link Skin}. FWSkin can load the same JSON files Skin uses, and it extends Skin.
 * If you encounter an unusually high amount of native memory being used, the cause is most likely Font objects being
 * created from BitmapFont objects repeatedly, <em>which FWSkin is designed to avoid</em>. When using any scene2d.ui
 * widget from TextraTypist with an FWSkin, the correct and optimal style from {@link Styles} is used, and that avoids
 * creating more and more Font objects as widgets are created and destroyed. Subclasses of FWSkin are also perfectly
 * fine to use, such as the subclass in <a href="https://github.com/tommyettinger/freetypist">FreeTypist</a> that allows
 * using FreeType to generate a Font from skin JSON configuration.
 * <em>Using a regular libGDX Skin object, not an FWSkin, will be a problem.</em>
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
    public Styles.LabelStyle style;
    protected boolean prefSizeInvalid = true;

    /**
     * Creates a TextraLabel that uses the default libGDX font (lsans-15 in the current version) with white color.
     * This allocates a new Font every time it is called, so you should avoid this constructor in code that is called
     * more than a handful of times. Its only valid use is in debugging.
     */
    public TextraLabel() {
        layout = new Layout();
        font = new Font();
        style = new Styles.LabelStyle(font, null);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the specified Skin's LabelStyle.
     * The skin should almost certainly be an {@link FWSkin} or one of its subclasses.
     *
     * @param text the text to use; may be multi-line, but will default to not wrapping
     * @param skin almost always an {@link FWSkin} or one of its subclasses; must have a
     *             {@link Styles.LabelStyle} or {@link com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle} registered as "default"
     */
    public TextraLabel(String text, Skin skin) {
        this(text, skin.get(Styles.LabelStyle.class));
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the specified Skin's LabelStyle.
     * The skin should almost certainly be an {@link FWSkin} or one of its subclasses.
     *
     * @param text           the text to use; may be multi-line, but will default to not wrapping
     * @param skin almost always an {@link FWSkin} or one of its subclasses; must have a
     *             {@link Styles.LabelStyle} or {@link com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle} registered as "default"
     * @param makeGridGlyphs currently ignored
     */
    public TextraLabel(String text, Skin skin, boolean makeGridGlyphs) {
        this(text, skin.get(Styles.LabelStyle.class), makeGridGlyphs);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the specified style from the given
     * Skin. The skin should almost certainly be an {@link FWSkin} or one of its subclasses.
     *
     * @param text      the text to use; may be multi-line, but will default to not wrapping
     * @param skin almost always an {@link FWSkin} or one of its subclasses; must have a
     *             {@link Styles.LabelStyle} or {@link com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle} registered with the given styleName
     * @param styleName the name of a Styles.LabelStyle to use from the Skin
     */
    public TextraLabel(String text, Skin skin, String styleName) {
        this(text, skin.get(styleName, Styles.LabelStyle.class));
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the specified style from the given
     * Skin. The skin should almost certainly be an {@link FWSkin} or one of its subclasses.
     *
     * @param text           the text to use; may be multi-line, but will default to not wrapping
     * @param skin almost always an {@link FWSkin} or one of its subclasses; must have a
     *             {@link Styles.LabelStyle} or {@link com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle} registered with the given styleName
     * @param styleName      the name of a Styles.LabelStyle to use from the Skin
     * @param makeGridGlyphs currently ignored
     */
    public TextraLabel(String text, Skin skin, String styleName, boolean makeGridGlyphs) {
        this(text, skin.get(styleName, Styles.LabelStyle.class), makeGridGlyphs);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line), using the specified style from the given
     * Skin, with the default Color overridden by the given one.
     * The skin should almost certainly be an {@link FWSkin} or one of its subclasses.
     *
     * @param text      the text to use; may be multi-line, but will default to not wrapping
     * @param skin almost always an {@link FWSkin} or one of its subclasses; must have a
     *             {@link Styles.LabelStyle} or {@link com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle} registered with the given styleName
     * @param styleName the name of a Styles.LabelStyle to use from the Skin
     * @param color     the color to use for the font when unspecified (at the start and when reset)
     */
    public TextraLabel(String text, Skin skin, String styleName, Color color) {
        this(text, skin.get(styleName, Styles.LabelStyle.class));
        if(color != null) layout.setBaseColor(color);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line), using the specified style from the given
     * Skin, with the default Color overridden by the color with the given name in the skin.
     * The skin should almost certainly be an {@link FWSkin} or one of its subclasses.
     *
     * @param text      the text to use; may be multi-line, but will default to not wrapping
     * @param skin almost always an {@link FWSkin} or one of its subclasses; must have a
     *             {@link Styles.LabelStyle} or {@link com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle} registered with the given styleName
     * @param styleName the name of a Styles.LabelStyle to use from the Skin
     * @param colorName the name in the skin of the color to use for the font when unspecified (at the start and when reset)
     */
    public TextraLabel(String text, Skin skin, String styleName, String colorName) {
        this(text, skin.get(styleName, Styles.LabelStyle.class));
        if(colorName == null) return;
        Color color = skin.get(colorName, Color.class);
        if(color != null) layout.setBaseColor(color);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the given style. This does not
     * require a Skin to be available.
     *
     * @param text  the text to use; may be multi-line, but will default to not wrapping
     * @param style the Styles.LabelStyle to use
     */
    public TextraLabel(String text, Styles.LabelStyle style) {
        this(text, style, false);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the given style. This does not
     * require a Skin to be available.
     *
     * @param text           the text to use; may be multi-line, but will default to not wrapping
     * @param style          the Styles.LabelStyle to use
     * @param makeGridGlyphs currently ignored
     */
    public TextraLabel(String text, Styles.LabelStyle style, boolean makeGridGlyphs) {
        this(text, style, style.font);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the default style from
     * a Skin, replacing any font that would be drawn from the style with {@code replacementFont}.
     * The skin should almost certainly be an {@link FWSkin} or one of its subclasses.
     *
     * @param text            the text to use; may be multi-line, but will default to not wrapping
     * @param skin            the default Styles.LabelStyle will be obtained from this and used
     * @param replacementFont a Font that will be used in place of the one in style
     */
    public TextraLabel(String text, Skin skin, Font replacementFont) {
        this(text, skin.get(Styles.LabelStyle.class), replacementFont);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the given style taken by name from
     * a Skin, replacing any font that would be drawn from the style with {@code replacementFont}.
     * The skin should almost certainly be an {@link FWSkin} or one of its subclasses.
     *
     * @param text            the text to use; may be multi-line, but will default to not wrapping
     * @param skin almost always an {@link FWSkin} or one of its subclasses; must have a
     *             {@link Styles.LabelStyle} or {@link com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle} registered with the given styleName
     * @param styleName       the name of a Styles.LabelStyle to use from the Skin
     * @param replacementFont a Font that will be used in place of the one in style
     */
    public TextraLabel(String text, Skin skin, String styleName, Font replacementFont) {
        this(text, skin.get(styleName, Styles.LabelStyle.class), replacementFont);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the given style taken by name from
     * a Skin, replacing any font that would be drawn from the style with {@code replacementFont}.
     * The skin should almost certainly be an {@link FWSkin} or one of its subclasses.
     *
     * @param text            the text to use; may be multi-line, but will default to not wrapping
     * @param skin almost always an {@link FWSkin} or one of its subclasses; must have a
     *             {@link Styles.LabelStyle} or {@link com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle} registered with the given styleName
     * @param styleName       the name of a Styles.LabelStyle to use from the Skin
     * @param replacementFont a Font that will be used in place of the one in style
     * @param color           the base color to use for the label, used when reset
     */
    public TextraLabel(String text, Skin skin, String styleName, Font replacementFont, Color color) {
        this(text, skin.get(styleName, Styles.LabelStyle.class), replacementFont);
        if (color != null) layout.setBaseColor(color);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the given style. This does not
     * require a Skin to be available.
     *
     * @param text            the text to use; may be multi-line, but will default to not wrapping
     * @param style           the Styles.LabelStyle to use, except for its font
     * @param replacementFont a Font that will be used in place of the one in style
     */
    public TextraLabel(String text, Styles.LabelStyle style, Font replacementFont) {
        font = replacementFont;
        layout = new Layout();
        if (style.fontColor != null) layout.setBaseColor(style.fontColor);
        this.style = style;
        storedText = text;
        font.markup(text, layout);
        invalidateHierarchy();
        setSize(layout.getWidth(), layout.getHeight());

//        int glyphCount = layout.countGlyphs();
//        layout.offsets.setSize(glyphCount + glyphCount);
//        Arrays.fill(layout.offsets.items, 0, glyphCount + glyphCount, 0f);
//        layout.sizing.setSize(glyphCount + glyphCount);
//        Arrays.fill(layout.sizing.items, 0, glyphCount + glyphCount, 1f);
//        layout.rotations.setSize(glyphCount);
//        Arrays.fill(layout.rotations.items, 0, glyphCount, 0f);
//        layout.advances.setSize(glyphCount);
//        Arrays.fill(layout.advances.items, 0, glyphCount, 1f);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line) and using the given Font. This does not
     * require a Skin to be available.
     *
     * @param text the text to use; may be multi-line, but will default to not wrapping
     * @param font a Font from this library, such as one obtained from {@link KnownFonts}
     */
    public TextraLabel(String text, Font font) {
        this(text, font, null);
    }

    /**
     * Creates a TextraLabel with the given text (which may be multi-line), using the given Font, and using the given
     * default color. This does not require a Skin to be available.
     *
     * @param text  the text to use; may be multi-line, but will default to not wrapping
     * @param font  a Font from this library, such as one obtained from {@link KnownFonts}
     * @param color the color to use for the font when unspecified (at the start and when reset)
     */
    public TextraLabel(String text, Font font, Color color) {
        this(text, font, color, Justify.NONE);
    }
    /**
     * Creates a TextraLabel with the given text (which may be multi-line), using the given Font, and using the given
     * default color. This does not require a Skin to be available.
     *
     * @param text  the text to use; may be multi-line, but will default to not wrapping
     * @param font  a Font from this library, such as one obtained from {@link KnownFonts}
     * @param color the color to use for the font when unspecified (at the start and when reset)
     */
    public TextraLabel(String text, Font font, Color color, Justify justify) {
        this.font = font;
        layout = new Layout();
        this.style = new Styles.LabelStyle();
        if (color != null) layout.setBaseColor(color);
        storedText = text;
        font.markup(text, layout);
        layout.setJustification(justify);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.validate();

        final float rot = getRotation();
        final float originX = getOriginX();
        final float originY = getOriginY();
        final float sn = MathUtils.sinDeg(rot);
        final float cs = MathUtils.cosDeg(rot);

        int bgc;
        final int lines = layout.lines();
        float baseX = getX(), baseY = getY();

        // These two blocks use different height measurements, so center vertical is offset once by half the layout
        // height, and once by half the widget height.
        float layoutHeight = layout.getHeight();
        if (Align.isBottom(align)) {
            baseX -= sn * layoutHeight;
            baseY += cs * layoutHeight;
        } else if (Align.isCenterVertical(align)) {
            baseX -= sn * layoutHeight * 0.5f;
            baseY += cs * layoutHeight * 0.5f;
        }
        float widgetHeight = getHeight();
        if (Align.isTop(align)) {
            baseX -= sn * widgetHeight;
            baseY += cs * widgetHeight;
        } else if (Align.isCenterVertical(align)) {
            baseX -= sn * widgetHeight * 0.5f;
            baseY += cs * widgetHeight * 0.5f;
        }

        float widgetWidth = getWidth();
        if (Align.isRight(align)) {
            baseX += cs * widgetWidth;
            baseY += sn * widgetWidth;
        } else if (Align.isCenterHorizontal(align)) {
            baseX += cs * widgetWidth * 0.5f;
            baseY += sn * widgetWidth * 0.5f;
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
            try {
                ((TransformDrawable) background).draw(batch,
                        getX(), getY(),             // position
                        originX, originY,           // origin
                        getWidth(), getHeight(),    // size
                        1f, 1f,                     // scale
                        rot);                       // rotation
            } catch (UnsupportedOperationException | ClassCastException itIsJustADrawable) {
                // TenPatch drawables do not support rotation, scale, or an origin, so we can use the
                // standard Drawable draw method and just assume people aren't trying to rotate TenPatches.
                // This also works in case the background is not a TransformDrawable.
                background.draw(batch,
                        getX(), getY(),             // position
                        getWidth(), getHeight());   // size
            }
        }

        if (layout.lines.isEmpty() || parentAlpha <= 0f) return;

        // we only change the shader or batch color if we actually are drawing something.
        boolean resetShader = font.getDistanceField() != Font.DistanceFieldType.STANDARD && batch.getShader() != font.shader;
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
            Line line = layout.getLine(ln);

            if (line.glyphs.size == 0)
                continue;

            baseX += sn * line.height;
            baseY -= cs * line.height;

            float x = baseX, y = baseY;

            final float worldOriginX = x + originX;
            final float worldOriginY = y + originY;
            float fx = -originX;
            float fy = -originY;
            x = cs * fx - sn * fy + worldOriginX;
            y = sn * fx + cs * fy + worldOriginY;

            if (Align.isCenterHorizontal(align)) {
                x -= cs * (line.width * 0.5f);
                y -= sn * (line.width * 0.5f);
            } else if (Align.isRight(align)) {
                x -= cs * line.width;
                y -= sn * line.width;
            }
            x -= sn * (0.5f * line.height);
            y += cs * (0.5f * line.height);

            float xChange = 0, yChange = 0;
            Font f = null;
            int kern = -1;
            boolean curly = false;
            int start = layout.countGlyphsBeforeLine(ln);
            for (int i = 0, n = line.glyphs.size; i < n; i++) {
                long glyph = line.glyphs.get(i);
                char ch = (char) glyph;
                if(font.omitCurlyBraces) {
                    if (curly) {
                        if (ch == '}') {
                            curly = false;
                            continue;
                        } else if (ch == '{')
                            curly = false;
                        else continue;
                    } else if (ch == '{') {
                        curly = true;
                        continue;
                    }
                }

                if (font.family != null) f = font.family.connected[(int) (glyph >>> 16 & 15)];
                if (f == null) f = font;
                if (i == 0) {
                    x -= 0.5f * f.cellWidth;
                    x += cs * 0.5f * f.cellWidth;
                    y += sn * 0.5f * f.cellWidth;

                    if(font.integerPosition){
                        x = (int)x;
                        y = (int)y;
                    }

                    Font.GlyphRegion reg = font.mapping.get((char) glyph);
                    if (reg != null && reg.offsetX < 0) {
                        float ox = reg.offsetX * f.scaleX * ((glyph & ALTERNATE) != 0L ? 1f : ((glyph + 0x300000L >>> 20 & 15) + 1) * 0.25f);
                        xChange -= cs * ox;
                        yChange -= sn * ox;
                    }
                }

                if (f.kerning != null) {
                    kern = kern << 16 | (int) ((glyph = line.glyphs.get(i)) & 0xFFFF);
                    float amt = f.kerning.get(kern, 0) * f.scaleX * ((glyph & ALTERNATE) != 0L ? 1f : ((glyph + 0x300000L >>> 20 & 15) + 1) * 0.25f);
                    xChange += cs * amt;
                    yChange += sn * amt;
                } else {
                    kern = -1;
                }
                bgc = 0;
                int even = start + i << 1, odd = even | 1;
                float xx = x + xChange + getOffsets().get(even), yy = y + yChange + getOffsets().get(odd);
                if(font.integerPosition){
                    xx = (int)xx;
                    yy = (int)yy;
                }

                float a = getAdvances().get(start + i);
                single = f.drawGlyph(batch, glyph, xx, yy, getRotations().get(start + i) + rot, getSizing().get(even), getSizing().get(odd), bgc, a);
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
        if (prefSizeInvalid) {
            validate();
        }
        float width = layout.getWidth();
        if(style != null && style.background != null)
            width = Math.max(width + style.background.getLeftWidth() + style.background.getRightWidth(), style.background.getMinWidth());
        return width;
    }

    @Override
    public float getPrefHeight() {
        if (prefSizeInvalid) {
            validate();
        }
        float height = layout.getHeight();
        if(style != null && style.background != null)
                height = Math.max(height + style.background.getBottomHeight() + style.background.getTopHeight(), style.background.getMinHeight());
//        System.out.println("Calculated PrefHeight to be " + height);
        return height;
    }

    /**
     * A no-op unless {@link #font} is a subclass that overrides {@link Font#handleIntegerPosition(float)}.
     * @param integer usually ignored
     * @return this for chaining
     */
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
     * too wide will just widen the size of the widget. This also calls {@link #layout()} if wrap becomes enabled
     * because of this call, because much of the behavior of layout() depends on wrap's value, then calls
     * {@link #setText(String)} with the stored text content to ensure text obeys the wrapping rules.
     *
     * @param wrap whether to wrap or not
     */
    public TextraLabel setWrap(boolean wrap) {
        if (this.wrap != (this.wrap = wrap)) // this means "if setting this.wrap changes it from its old value, do..."
        {
            invalidateHierarchy();
            if(this.wrap) {
                layout();
//                setText(storedText);
            }
        }
        return this;
    }
    
    @Override
    public void setWidth(float width) {
        // If the window is minimized, we have invalid dimensions and shouldn't process resizing.
        if(Gdx.graphics.getWidth() <= 0 || Gdx.graphics.getHeight() <= 0) return;
        super.setWidth(width);
        if (wrap) {
            layout.setTargetWidth(width);
        }
        font.calculateSize(layout);
        invalidateHierarchy();
    }

    @Override
    public void setHeight(float height) {
        // If the window is minimized, we have invalid dimensions and shouldn't process resizing.
        if(Gdx.graphics.getWidth() <= 0 || Gdx.graphics.getHeight() <= 0) return;
        super.setHeight(height);
        font.calculateSize(layout);
        invalidateHierarchy();
    }

    @Override
    public void setSize(float width, float height) {
        // If the window is minimized, we have invalid dimensions and shouldn't process resizing.
        if(Gdx.graphics.getWidth() <= 0 || Gdx.graphics.getHeight() <= 0) return;
        super.setSize(width, height);
        if (wrap) {
            layout.setTargetWidth(width);
        }
        font.calculateSize(layout);
        invalidateHierarchy();
    }

    /**
     * This only exists so code that needs to use {@link com.badlogic.gdx.scenes.scene2d.Actor#setWidth(float)} still
     * can, even with setWidth() implemented here.
     * @param width the new width, in world units as a float
     */
    public void setSuperWidth(float width) {
        super.setWidth(width);
    }
    
    /**
     * This only exists so code that needs to use {@link com.badlogic.gdx.scenes.scene2d.Actor#setHeight(float)} still
     * can, even with setHeight() implemented here.
     * @param height the new height, in world units as a float
     */
    public void setSuperHeight(float height) {
        super.setHeight(height);
    }
    
    @Override
    public void layout() {
        // If the window is minimized, we have invalid dimensions and shouldn't process resizing.
        if(Gdx.graphics.getWidth() <= 0 || Gdx.graphics.getHeight() <= 0) return;
        float width = getWidth();
        if (style != null && style.background != null) {
            width = (width - (style.background.getLeftWidth() + style.background.getRightWidth()));
        }
        float originalHeight = layout.getHeight();
        float actualWidth = font.calculateSize(layout);

        if (wrap && (width == 0 || layout.getTargetWidth() != width || actualWidth > width)) {
            if(width != 0f)
                layout.setTargetWidth(width);
            font.regenerateLayout(layout);

// We do not want to call invalidateHierarchy() here! It would force regeneration every frame.
        }

        // If the call to calculateSize() changed layout's height, we want to update height and invalidateHierarchy().
        float newHeight = layout.getHeight();
        if(!MathUtils.isEqual(originalHeight, newHeight)) {
            setSuperHeight(newHeight);
            invalidateHierarchy();
            // We don't want to call setHeight() because it would calculateSize() again, which isn't needed.
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
        if(wrap)
            layout.setTargetWidth(getWidth());
        font.markup(markupText, layout.clear());

//        setWidth(layout.getWidth() + (style != null && style.background != null ?
//                style.background.getLeftWidth() + style.background.getRightWidth() : 0.0f));
        invalidateHierarchy();
    }

    /**
     * By default, does nothing; this is overridden in TypingLabel to skip its text progression ahead.
     */
    public TextraLabel skipToTheEnd() {
        return this;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        prefSizeInvalid = true;
    }

    @Override
    public void validate() {
        prefSizeInvalid = false;
        super.validate();
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
                    if (c >= '\uE000' && c <= '\uF800') {
                        String name = font.namesByCharCode.get(c);
                        if (name != null) sb.append(name);
                        else sb.append(c);
                    } else {
                        if (c == '\u0002') sb.append('[');
                        else if(c != '\u200B') sb.append(c); // do not print zero-width space
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

    /**
     * Contains one float per glyph; each is a rotation in degrees to apply to that glyph (around its center).
     * This should not be confused with {@link #getRotation()}, which refers to the rotation of the label itself.
     * This getter accesses the rotation of each glyph around its center instead.
     * This is a direct reference to the current {@link #layout}'s {@link Layout#rotations}.
     */
    public FloatArray getRotations() {
        return layout.rotations;
    }

    /**
     * Contains two floats per glyph; even items are x offsets, odd items are y offsets.
     * This getter accesses the x- and y-offsets of each glyph from its normal position.
     * This is a direct reference to the current {@link #layout}'s {@link Layout#offsets}.
     */
    public FloatArray getOffsets() {
        return layout.offsets;
    }

    /**
     * Contains two floats per glyph, as size multipliers; even items apply to x, odd items apply to y.
     * This getter accesses the x-and y-scaling of each glyph in its normal location, without changing line height or
     * the x-advance of each glyph. It is usually meant for temporary or changing effects, not permanent scaling.
     * This is a direct reference to the current {@link #layout}'s {@link Layout#sizing}.
     */
    public FloatArray getSizing() {
        return layout.sizing;
    }

    /**
     * Contains one float per glyph; each is a multiplier that affects the x-advance of that glyph.
     * This getter uses the same types of values as {@link #getSizing()}, so if you change the x-scaling of a glyph with
     * that variable, you can also change its x-advance here by assigning the same value for that glyph here.
     * This is a direct reference to the current {@link #layout}'s {@link Layout#rotations}.
     */
    public FloatArray getAdvances() {
        return layout.advances;
    }

}
