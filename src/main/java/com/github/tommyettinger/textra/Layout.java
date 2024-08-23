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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.LongArray;

/**
 * A replacement for libGDX's GlyphLayout, more or less; stores one or more (possibly empty) {@link Line}s of text,
 * which can use color and style markup from {@link Font}, and can be drawn with
 * {@link Font#drawGlyphs(Batch, Layout, float, float, int)}. You can obtain a Layout with
 * {@code new Layout()} followed by setting the font, or just using a constructor.
 */
public class Layout {

    protected Font font;
    protected final Array<Line> lines = new Array<>(true, 8);
    protected int maxLines = Integer.MAX_VALUE;
    protected boolean atLimit = false;
    protected String ellipsis = null;
    protected float targetWidth = 0f;
    protected float baseColor = Color.WHITE_FLOAT_BITS;

    public Layout() {
        lines.add(new Line());
    }

    public Layout(Font font) {
        this.font = font;
        lines.add(new Line());
    }

    public Layout(Layout other) {
        this.font = other.font;
        this.maxLines = other.maxLines;
        this.atLimit = other.atLimit;
        this.ellipsis = other.ellipsis;
        this.targetWidth = other.targetWidth;
        this.baseColor = other.baseColor;
        for (int i = 0; i < other.lines(); i++) {
            Line ln = new Line(), o = other.lines.get(i);
            ln.glyphs.addAll(o.glyphs);
            lines.add(ln.size(o.width, o.height));
        }
    }
    /**
     * One of the ways to set the font on a Layout; this one returns this Layout for chaining.
     *
     * @param font the font to use
     * @return this Layout, for chaining
     */
    public Layout font(Font font) {
        if (this.font == null || !this.font.equals(font)) {
            this.font = font;
            lines.clear();
            lines.add(new Line());
        }
        return this;
    }

    public Font getFont() {
        return font;
    }

    /**
     * One of the ways to set the font on a Layout; this is a traditional setter.
     *
     * @param font the font to use
     */
    public void setFont(Font font) {
        this.font(font);
    }

    /**
     * Adds a {@code long} glyph as processed by {@link Font} to store color and style info with the char.
     *
     * @param glyph usually produced by {@link Font} to store color and style info with the char
     * @return this Layout, for chaining
     */
    public Layout add(long glyph) {
        if (!atLimit) {
            if ((glyph & 0xFFFFL) == 10L) {
                pushLine();
            } else {
                lines.peek().glyphs.add(glyph);
            }
        }

        return this;
    }

    public Layout clear() {
        lines.clear();
        lines.add(new Line());
        atLimit = false;
        return this;
    }

    public float getWidth() {
        float w = 0;
        for (int i = 0, n = lines.size; i < n; i++) {
            w = Math.max(w, lines.get(i).width);
        }
        return w;
    }

    public float getHeight() {
        float h = 0;
        for (int i = 0, n = lines.size; i < n; i++) {
            h += lines.get(i).height;
        }
        return h;
    }

    public int lines() {
        return lines.size;
    }

    /**
     * Gets a Line from this by its index.
     *
     * @param i index for the Line to fetch; must be at least 0 and less than {@link #lines()}.
     * @return the Line at the given index
     */
    public Line getLine(int i) {
        if (i >= lines.size) return null;
        return lines.get(i);
    }

    public Line peekLine() {
        return lines.peek();
    }

    public Line pushLine() {
        if (lines.size >= maxLines) {
            atLimit = true;
            return null;
        }

        Line line = new Line(), prev = lines.peek();
        prev.glyphs.add('\n');
        line.height = 0;
        lines.add(line);
        return line;
    }

    public Line insertLine(int index) {
        if (lines.size >= maxLines) {
            atLimit = true;
            return null;
        }
        if (index < 0 || index >= maxLines) return null;
        Line line = new Line(), prev = lines.get(index);
        prev.glyphs.add('\n');
        line.height = 0;
        lines.insert(index + 1, line);
        return line;
    }

    public float getTargetWidth() {
        return targetWidth;
    }

    public Layout setTargetWidth(float targetWidth) {
        // we may want this to lay existing lines out again if the width changed.
        this.targetWidth = targetWidth;
        return this;
    }

    /**
     * Gets the base color of the Layout, as the float bits of a Color. The base color is what font color
     * will be used immediately after resetting formatting with {@code []}, as well as the initial color
     * used by text that hasn't been formatted. You can fill a Color object with this value using
     * {@link Color#abgr8888ToColor(Color, float)} (it modifies the Color you give it).
     *
     * @return the base color of the Layout, as float bits
     */
    public float getBaseColor() {
        return baseColor;
    }

    /**
     * Sets the base color of the Layout; this is what font color will be used immediately after resetting
     * formatting with {@code []}, as well as the initial color used by text that hasn't been formatted.
     * This takes the color as a primitive float, which you can get from a Color object with
     * {@link Color#toFloatBits()}, or in some cases from existing data produced by {@link Font}.
     *
     * @param baseColor the float bits of a Color, as obtainable via {@link Color#toFloatBits()}
     */
    public void setBaseColor(float baseColor) {
        this.baseColor = baseColor;
    }

    /**
     * Sets the base color of the Layout; this is what font color will be used immediately after resetting
     * formatting with {@code []}, as well as the initial color used by text that hasn't been formatted.
     * If the given Color is null, this treats it as white.
     *
     * @param baseColor a Color to use for text that hasn't been formatted; if null, will be treated as white
     */
    public void setBaseColor(Color baseColor) {
        this.baseColor = baseColor == null ? Color.WHITE_FLOAT_BITS : baseColor.toFloatBits();
    }

    /**
     * The maximum number of {@link Line}s this Layout can contain.
     *
     * @return the maximum number of {@link Line} objects this Layout can contain
     */
    public int getMaxLines() {
        return maxLines;
    }

    /**
     * Sets the maximum number of {@link Line}s this Layout can contain; this is always at least 1.
     * For effectively unlimited lines, pass {@link Integer#MAX_VALUE} to this.
     *
     * @param maxLines the limit for how many Line objects this Layout can contain; always 1 or more
     */
    public void setMaxLines(int maxLines) {
        this.maxLines = Math.max(1, maxLines);
    }

    /**
     * Gets the ellipsis, which may be null, or may be a String that can be placed at the end of the text if its
     * max lines are exceeded.
     *
     * @return an ellipsis String or null
     */
    public String getEllipsis() {
        return ellipsis;
    }

    /**
     * Sets the ellipsis text, which replaces the last few glyphs if non-null and the text added would exceed
     * the {@link #getMaxLines()} of this Layout. For the ellipsis to appear, this has to be called with a
     * non-null String (often {@code "..."}, or {@code "…"} if the font supports it), and
     * {@link #setMaxLines(int)} needs to have been called with a small enough number, such as 1.
     *
     * @param ellipsis a String for a Layout to end with if its max lines are exceeded, or null to avoid such truncation
     */
    public void setEllipsis(String ellipsis) {
        this.ellipsis = ellipsis;
    }

    /**
     * Calculates how many {@code long} glyphs are currently in this layout, and returns that count. This takes time
     * proportional to the value of {@link #lines()}, not the number of glyphs.
     * @return how many {@code long} glyphs are in this Layout
     */
    public int countGlyphs(){
        int layoutSize = 0;
        for (int i = 0, n = lines.size; i < n; i++) {
            layoutSize += lines.get(i).glyphs.size;
        }
        return layoutSize;
    }
    /**
     * Resets the object for reuse. The font is nulled, but the lines are freed, cleared, and then one blank line is
     * re-added to lines so it can be used normally later.
     */
    public void reset() {
        targetWidth = 0f;
        baseColor = Color.WHITE_FLOAT_BITS;
        maxLines = Integer.MAX_VALUE;
        atLimit = false;
        ellipsis = null;
        font = null;
    }

    /**
     * Can be useful if you want to append many Layouts into a StringBuilder. This does not treat character u0002 any
     * differently from other characters, which is where it differs from {@link #appendInto(StringBuilder)}.
     * This does not add or remove newlines from the Layout's contents, and can produce line breaks if they appear.
     *
     * @param sb a non-null StringBuilder from the JDK
     * @return sb, for chaining
     */
    public StringBuilder appendIntoDirect(StringBuilder sb) {
        long gl;
        for (int i = 0, n = lines.size; i < n; i++) {
            Line line = lines.get(i);
            for (int j = 0, ln = line.glyphs.size; j < ln; j++) {
                gl = line.glyphs.get(j);
                sb.append((char) gl);
            }
        }
        return sb;
    }

    /**
     * Gets a String representation of part of this Layout, made of only the char portions of the glyphs from start
     * (inclusive) to end (exclusive). This can retrieve text from across multiple lines. If this encounters the special
     * placeholder character u0002, it treats it as {@code '['}, like the rest of the library does. If this encounters
     * emoji, icons, or other inline images assigned to the font with {@link Font#addAtlas(TextureAtlas)}, then this
     * will use a name that can be used to look up that inline image (such as an actual emoji like 🤖 instead of the
     * gibberish character that {@code [+robot]} produces internally).
     * @param sb a non-null StringBuilder from the JDK; will be modified if this Layout is non-empty
     * @param start inclusive start index to begin taking chars from
     * @param end exclusive end index to stop taking chars before
     * @return sb, for chaining
     */
    public StringBuilder appendSubstringInto(StringBuilder sb, int start, int end) {
        start = Math.max(0, start);
        end = Math.min(Math.max(countGlyphs(), start), end);
        int index = start;
        sb.ensureCapacity(end - start);
        int glyphCount = 0;
        for (int i = 0, n = lines.size; i < n && index >= 0; i++) {
            LongArray glyphs = lines.get(i).glyphs;
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
                    return sb;
                index = 0;
            }
            else
                index -= glyphs.size;
        }
        return sb;
    }

    /**
     * Primarily used by {@link #toString()}, but can be useful if you want to append many Layouts into a StringBuilder.
     * If this encounters the special placeholder character u0002, it treats it as {@code '['}, like the rest of the
     * library does. If this encounters emoji, icons, or other inline images assigned to the font with
     * {@link Font#addAtlas(TextureAtlas)}, then this will use a name that can be used to look up that inline image
     * (such as an actual emoji like 🤖 instead of the gibberish character that {@code [+robot]} produces internally).
     * This does not add or remove newlines from the Layout's contents, and can produce line breaks if they appear.
     *
     * @param sb a non-null StringBuilder from the JDK; will be modified if this Layout is non-empty
     * @return sb, for chaining
     */
    public StringBuilder appendInto(StringBuilder sb) {
        return appendSubstringInto(sb, 0, Integer.MAX_VALUE);
    }

    /**
     * Simply delegates to calling {@link #appendInto(StringBuilder)} with a new StringBuilder, calling toString(), and
     * then returning. See the documentation for appendInto for more details.
     * @return a String holding the char portions of every glyph in this Layout, substituting in names for inline image codes
     */
    @Override
    public String toString() {
        return appendInto(new StringBuilder()).toString();
    }
}
