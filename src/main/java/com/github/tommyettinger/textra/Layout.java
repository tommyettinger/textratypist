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
import com.badlogic.gdx.utils.FloatArray;
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
    protected Justify justification = Justify.NONE;
    /**
     * Contains two floats per glyph; even items are x offsets (0-indexed), odd items are y offsets.
     * The neutral value for a glyph (the value that this defaults to, and means no change should be made) is 0.0f.
     */
    public final FloatArray offsets = new FloatArray();
    /**
     * Contains two floats per glyph, as size multipliers; even items (0-indexed) apply to x, odd items apply to y.
     * The neutral value for a glyph (the value that this defaults to, and means no change should be made) is 1.0f.
     */
    public final FloatArray sizing = new FloatArray();
    /**
     * Contains one float per glyph; each is a rotation in degrees to apply to that glyph (around its center).
     * The neutral value for a glyph (the value that this defaults to, and means no change should be made) is 0.0f.
     */
    public final FloatArray rotations = new FloatArray();
    /**
     * Contains one float per glyph; each is a multiplier to the x-advance of that glyph.
     * The neutral value for a glyph (the value that this defaults to, and means no change should be made) is 1.0f.
     */
    public final FloatArray advances = new FloatArray();

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
        rotations.addAll(other.rotations);
        offsets.addAll(other.offsets);
        sizing.addAll(other.sizing);
        advances.addAll(other.advances);
        justification = other.justification;
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
        return add(glyph, 1f, 1f, 0f, 0f, 0f);
    }

    /**
     * Adds a {@code long} glyph as processed by {@link Font} to store color and style info with the char.
     *
     * @param glyph usually produced by {@link Font} to store color and style info with the char
     * @param scale    1.0f if unchanged; multiplies the size of the glyph
     * @param advance  1.0f if unchanged; multiplies the x-advance of the glyph, and is usually related to scale
     * @param offsetX  0.0f if unchanged; added to the initial x-position of the glyph
     * @param offsetY  0.0f if unchanged; added to the y-position of the glyph (with descenders lower than ascenders)
     * @param rotation 0.0f if unchanged; added to the rotation of the glyph, in degrees
     * @return this Layout, for chaining
     */
    public Layout add(long glyph, float scale, float advance, float offsetX, float offsetY, float rotation) {
        if (!atLimit) {
            if ((glyph & 0xFFFFL) == 10L) {
                if (lines.size >= maxLines) {
                    atLimit = true;
                    return null;
                }
                Line line = new Line(), prev = lines.peek();
                prev.glyphs.add('\n');
                line.height = 0;
                lines.add(line);
            } else {
                lines.peek().glyphs.add(glyph);
            }
            sizing.add(scale, scale);
            advances.add(advance);
            offsets.add(offsetX, offsetY);
            rotations.add(rotation);
        }

        return this;
    }

    public Layout clear() {
        lines.clear();
        sizing.clear();
        advances.clear();
        offsets.clear();
        rotations.clear();

        lines.add(new Line());
        atLimit = false;
        return this;
    }

    public float getWidth() {
        if(justification != Justify.NONE && (lines.size > 1 && !justification.ignoreLastLine)) return targetWidth;
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

        Line line = new Line();
        if(advances.isEmpty())
            add('\n');
        else
            add('\n', sizing.peek(), advances.peek(), offsets.get(offsets.size - 2), offsets.peek(), rotations.peek());
        line.height = 0;
        lines.add(line);
        return line;
    }

    public Line pushLineBare() {
        if (lines.size >= maxLines) {
            atLimit = true;
            return null;
        }

        Line line = new Line();
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
    public Layout setBaseColor(float baseColor) {
        this.baseColor = baseColor;
        return this;
    }

    /**
     * Sets the base color of the Layout; this is what font color will be used immediately after resetting
     * formatting with {@code []}, as well as the initial color used by text that hasn't been formatted.
     * If the given Color is null, this treats it as white.
     *
     * @param baseColor a Color to use for text that hasn't been formatted; if null, will be treated as white
     */
    public Layout setBaseColor(Color baseColor) {
        this.baseColor = baseColor == null ? Color.WHITE_FLOAT_BITS : baseColor.toFloatBits();
        return this;
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
    public Layout setMaxLines(int maxLines) {
        this.maxLines = Math.max(1, maxLines);
        return this;
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
    public Layout setEllipsis(String ellipsis) {
        this.ellipsis = ellipsis;
        return this;
    }

    public Justify getJustification() {
        return justification;
    }

    public Layout setJustification(Justify justification) {
        this.justification = justification == null ? Justify.NONE : justification;
        return this;
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
     * Calculates how many {@code long} glyphs are currently in this layout before the start of the Line with the given
     * {@code lineIndex}, and returns that count. This is mainly useful for mapping an index into a Line to an index in
     * a Layout's non-Line-based FloatArray fields, such as {@link #advances} or {@link #rotations}. Some FloatArray
     * fields use two floats per glyph, such as {@link #offsets} and {@link #sizing}; see their docs for more.
     * <br>
     * This takes time proportional to the value of {@code lineIndex}, not the number of glyphs.
     * @return how many {@code long} glyphs exist in this Layout before the start of the given Line
     */
    public int countGlyphsBeforeLine(int lineIndex){
        int layoutSize = 0;
        for (int i = 0, n = Math.min(lines.size, lineIndex); i < n; i++) {
            layoutSize += lines.get(i).glyphs.size;
        }
        return layoutSize;
    }

    /**
     * Resets the object for reuse. The font is nullified, but the lines are cleared and then one blank line is
     * re-added to lines so it can be used normally later.
     */
    public void reset() {
        clear();
        justification = Justify.NONE;
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
            sb.append('\n');
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

    /**
     * Mostly intended for internal use; when any of {@link #lines} have been reduced in size but the other fields here
     * have not been changed, this will trim {@link #advances}, {@link #rotations}, {@link #sizing}, and
     * {@link #offsets} to match the size of the total glyphs in all lines.
     * @param i the length to truncate to; often {@link #countGlyphs()}
     * @return this, for chaining
     */
    public Layout truncateExtra(int i) {
        advances.truncate(i);
        rotations.truncate(i);
        sizing.truncate(i << 1);
        offsets.truncate(i << 1);
        return this;
    }
}
