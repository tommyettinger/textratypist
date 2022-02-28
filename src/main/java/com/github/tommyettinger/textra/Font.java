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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.DistanceFieldFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.*;
import com.github.tommyettinger.textra.utils.ColorUtils;
import regexodus.Category;

import java.util.Arrays;

/**
 * A replacement for libGDX's BitmapFont class, supporting additional markup to allow styling text with various effects.
 * This includes a "faux bold" and oblique mode using one font image; you don't need a bold and italic/oblique image
 * separate from the book face.
 * <br>
 * A Font represents either one size of a "standard" bitmap font (which can be drawn scaled up or down), or many sizes
 * of a distance field font (using either the commonly-used SDF format or newer MSDF format). The same class is used for
 * standard, SDF, and MSDF fonts, but you call {@link #enableShader(Batch)} before rendering with SDF or MSDF fonts, and
 * can switch back to a normal SpriteBatch shader with {@code batch.setShader(null);}. You don't have to use SDF or MSDF
 * fonts, but they scale more cleanly. You can generate SDF fonts with
 * Hiero or [a related
 * tool](https://github.com/libgdx/libgdx/wiki/Distance-field-fonts#using-distance-fields-for-arbitrary-images) that is
 * part of libGDX; MSDF fonts are harder to generate, but possible using a tool like
 * <a href="https://github.com/tommyettinger/Glamer">Glamer</a>.
 * <br>
 * This interacts with the {@link Layout} class, with a Layout referencing a Font, and various methods in Font taking
 * a Layout. You usually want to have a Layout for any text you draw repeatedly, and draw that Layout each frame with
 * {@link #drawGlyphs(Batch, Layout, float, float, int)} or a similar method.
 * @see #markup(String, Layout) The markup() method's documentation covers all the markup tags.
 */
public class Font implements Disposable {

    /**
     * Describes the region of a glyph in a larger TextureRegion, carrying a little more info about the offsets that
     * apply to where the glyph is rendered.
     */
    public static class GlyphRegion extends TextureRegion {
        /**
         * The offset from the left of the original image to the left of the packed image, after whitespace was removed
         * for packing.
         */
        public float offsetX;

        /**
         * The offset from the bottom of the original image to the bottom of the packed image, after whitespace was
         * removed for packing.
         */
        public float offsetY;

        /**
         * How far to move the "cursor" to the right after drawing this GlyphRegion. Uses the same unit as
         * {@link #offsetX}.
         */
        public float xAdvance;

        /**
         * Creates a GlyphRegion from a parent TextureRegion (typically from an atlas), along with the lower-left x and
         * y coordinates, the width, and the height of the GlyphRegion.
         * @param textureRegion a TextureRegion, typically from a TextureAtlas
         * @param x the x-coordinate of the left side of the texture, in pixels
         * @param y the y-coordinate of the lower side of the texture, in pixels
         * @param width the width of the GlyphRegion, in pixels
         * @param height the height of the GlyphRegion, in pixels
         */
        public GlyphRegion(TextureRegion textureRegion, int x, int y, int width, int height) {
            super(textureRegion, x, y, width, height);
        }

        /**
         * Copies another GlyphRegion.
         * @param other the other GlyphRegion to copy
         */
        public GlyphRegion(GlyphRegion other) {
            super(other);
            offsetX = other.offsetX;
            offsetY = other.offsetY;
            xAdvance = other.xAdvance;
        }

        /**
         * Flips the region, adjusting the offset so the image appears to be flipped as if no whitespace has been
         * removed for packing.
         * @param x true if this should flip x to be -x
         * @param y true if this should flip y to be -y
         */
        @Override
        public void flip (boolean x, boolean y) {
            super.flip(x, y);
            if (x) {
                offsetX = -offsetX;
                xAdvance = -xAdvance; // TODO: not sure if this is the expected behavior...
            }
            if (y) offsetY = -offsetY;
        }
    }

    /**
     * Holds up to 16 Font values, accessible by index or by name, that markup can switch between while rendering.
     * This uses the [@Name] syntax. It is suggested that multiple Font objects share the same FontFamily so users can
     * have the same names mean the same fonts reliably.
     */
    public static class FontFamily {
        /**
         * Stores this Font and up to 15 other connected Fonts that can be switched between using [@Name] syntax.
         * If an item is null and this tries to switch to it, the font does not change.
         */
        public final Font[] connected = new Font[16];

        /**
         * Stores the names of Fonts (or aliases for those Fonts) as keys, mapped to ints between 0 and 15 inclusive.
         * The int values that this map keeps are stored in long glyphs and looked up as indices in {@link #connected}.
         */
        public final ObjectIntMap<String> fontAliases = new ObjectIntMap<>(32);

        /**
         * Creates a FontFamily that only allows staying on the same font, unless later configured otherwise.
         */
        public FontFamily(){
        }

        /**
         * Creates a FontFamily given an array of String names and a (almost-always same-sized) array of Font values
         * that those names will refer to. This allows switching to different fonts using the [@Name] syntax. This
         * also registers aliases for the Strings "0" through up to "15" to refer to the Font values with the same
         * indices (it can register fewer aliases than up to "15" if there are fewer than 16 Fonts). You should avoid
         * using more than 16 fonts with this.
         * @param aliases a non-null array of up to 16 String names to use for fonts (individual items may be null)
         * @param fonts a non-null array of Font values that should have the same length as aliases (no more than 16)
         */
        public FontFamily(String[] aliases, Font[] fonts){
            this(aliases, fonts, 0, Math.min(aliases.length, fonts.length));
        }

        /**
         * Creates a FontFamily given an array of String names, a (almost-always same-sized) array of Font values that
         * those names will refer to, and offset/length values for those arrays (allowing {@link Array} to sometimes be
         * used to get the items for aliases and fonts). This allows switching to different fonts using the [@Name]
         * syntax. This also registers aliases for the Strings "0" through up to "15" to refer to the Font values with
         * the same indices (it can register fewer aliases than up to "15" if there are fewer than 16 Fonts). You should
         * avoid using more than 16 fonts with this.
         * @param aliases an array of up to 16 String names to use for fonts (individual items may be null)
         * @param fonts an array of Font values that should have the same length as aliases (no more than 16)
         * @param offset where to start accessing aliases and fonts, as a non-negative index
         * @param length how many items to use from aliases and fonts, if that many are provided
         */
        public FontFamily(String[] aliases, Font[] fonts, int offset, int length){
            if(aliases == null || fonts == null || (aliases.length & fonts.length) == 0) return;
            for (int i = offset, a = 0; i < length && i < aliases.length && i < fonts.length; i++, a++) {
                connected[a & 15] = fonts[i];
                fontAliases.put(aliases[i], a & 15);
                fontAliases.put(String.valueOf(a & 15), a & 15);
            }
        }

        /**
         * Constructs a FontFamily given an OrderedMap of String keys (names of Fonts) to Font values (the Fonts that
         * can be switched between). This only uses up to the first 16 keys of map.
         * @param map an OrderedMap of String keys to Font values
         */
        public FontFamily(OrderedMap<String, Font> map){
            Array<String> ks = map.orderedKeys();
            for (int i = 0; i < map.size && i < 16; i++) {
                String name = ks.get(i);
                fontAliases.put(name, i);
                fontAliases.put(String.valueOf(i), i);
                connected[i] = map.get(name);
            }
        }

        /**
         * Copy constructor for another FontFamily. Font items in {@link #connected} will not be copied, and the same
         * references will be used.
         * @param other another, non-null, FontFamily to copy into this.
         */
        public FontFamily(FontFamily other){
            System.arraycopy(other.connected, 0, connected, 0, 16);
            fontAliases.putAll(other.fontAliases);
        }

        /**
         * Gets the corresponding Font for a name/alias, or null if it was not found.
         * @param name a name or alias for a font, such as "Gentium" or "2"
         * @return the Font corresponding to the given name, or null if not found
         */
        public Font get(String name){
            if(name == null) return null;
            return connected[fontAliases.get(name, 0) & 15];
        }

    }

    /**
     * Defines what types of distance field font this can use and render.
     * STANDARD has no distance field.
     * SDF is the signed distance field technique Hiero is compatible with, and uses only an alpha channel.
     * MSDF is the multi-channel signed distance field technique, which is sharper but uses the RGB channels.
     */
    public enum DistanceFieldType {
        /**
         * Used by normal fonts with no distance field effect.
         * If the font has a large image that is downscaled, you may want to call {@link #setTextureFilter()}.
         */
        STANDARD,
        /**
         * Used by Signed Distance Field fonts that are compatible with {@link DistanceFieldFont}, and may be created
         * by Hiero with its Distance Field effect. You may want to set the {@link #distanceFieldCrispness} field to a
         * higher or lower value depending on the range used to create the font in Hiero; this can take experimentation.
         */
        SDF,
        /**
         * Used by Multi-channel Signed Distance Field fonts, which are harder to create but can be more crisp than SDF
         * fonts, with hard corners where the corners were hard in the original font. If you want to create your own
         * MSDF font, you can use <a href="https://github.com/tommyettinger/Glamer">the Glamer font generator tool</a>,
         * which puts a lot of padding for each glyph to ensure it renders evenly, or you can use one of several other
         * MSDF font generators, which may have an uneven baseline and look shaky when rendered for some fonts. You may
         * want to set the {@link #distanceFieldCrispness} field to a higher or lower value based on preference.
         */
        MSDF
    }

    //// members section

    /**
     * Maps char keys (stored as ints) to their corresponding {@link GlyphRegion} values. You can add arbitrary images
     * to this mapping if you create appropriate GlyphRegion values (as with
     * {@link GlyphRegion#GlyphRegion(TextureRegion, int, int, int, int)}), though they must map to a char.
     */
    public IntMap<GlyphRegion> mapping;
    /**
     * Which GlyphRegion to display if a char isn't found in {@link #mapping}. May be null to show a space by default.
     */
    public GlyphRegion defaultValue;
    /**
     * The larger TextureRegions that {@link GlyphRegion} images are pulled from; these could be whole Textures or be
     * drawn from a TextureAtlas that the font shares with other images.
     */
    public Array<TextureRegion> parents;
    /**
     * A {@link DistanceFieldType} that should be {@link DistanceFieldType#STANDARD} for most fonts, and can be
     * {@link DistanceFieldType#SDF} or {@link DistanceFieldType#MSDF} if you know you have a font made to be used with
     * one of those rendering techniques. See {@link #distanceFieldCrispness} for one way to configure SDF and MSDF
     * fonts, and {@link #resizeDistanceField(int, int)} for a convenience method to handle window-resizing sharply.
     */
    public DistanceFieldType distanceField;
    /**
     * If true, this is a fixed-width (monospace) font; if false, this is probably a variable-width font. This affects
     * some rendering decisions Font makes, such as whether subscript chars should take up half-width (for variable
     * fonts) or full-width (for monospace).
     */
    public boolean isMono;
    /**
     * Unlikely to be used externally, this is one way of storing the kerning information that some fonts have. Kerning
     * can improve the appearance of variable-width fonts, and is always null for monospace fonts. This uses a
     * combination of two chars as a key (the earlier char is in the upper 16 bits, and the later char is in the lower
     * 16 bits). Each such combination that has a special kerning value (not the default 0) has an int associated with
     * it, which applies to the x-position of the later char.
     */
    public IntIntMap kerning;
    /**
     * When {@link #distanceField} is {@link DistanceFieldType#SDF} or {@link DistanceFieldType#MSDF}, this determines
     * how much the edges of the glyphs should be aliased sharply (higher values) or anti-aliased softly (lower values).
     * The default value is 1. This is set internally by {@link #resizeDistanceField(int, int)} using
     * {@link #distanceFieldCrispness} as a multiplier; when you want to have a change to crispness persist, use that
     * other field.
     */
    public float actualCrispness = 1f;

    /**
     * When {@link #distanceField} is {@link DistanceFieldType#SDF} or {@link DistanceFieldType#MSDF}, this determines
     * how much the edges of the glyphs should be aliased sharply (higher values) or anti-aliased softly (lower values).
     * The default value is 1. This is used as a persistent multiplier that can be configured per-font, whereas
     * {@link #actualCrispness} is the working value that changes often but is influenced by this one.
     */
    public float distanceFieldCrispness = 1f;

    /**
     * Only actually refers to a "cell" when {@link #isMono} is true; otherwise refers to the largest width of any
     * glyph in the font, after scaling.
     */
    public float cellWidth = 1f;
    /**
     * Refers to the largest height of any glyph in the font, after scaling.
     */
    public float cellHeight = 1f;
    /**
     * Only actually refers to a "cell" when {@link #isMono} is true; otherwise refers to the largest width of any
     * glyph in the font, before any scaling.
     */
    public float originalCellWidth = 1f;
    /**
     * Refers to the largest height of any glyph in the font, before any scaling.
     */
    public float originalCellHeight = 1f;
    /**
     * Scale multiplier for width.
     */
    public float scaleX = 1f;
    /**
     * Scale multiplier for height.
     */
    public float scaleY = 1f;

    /**
     * A char that will be used to draw solid blocks with {@link #drawBlocks(Batch, int[][], float, float)}. The glyph
     * that corresponds to this char should be a maximum-size block of solid white pixels in most cases. Because Glamer
     * (which generated many of the knownFonts here) places a solid block at character 0, this defaults to u0000 .
     */
    public char solidBlock = '\u0000';

    /**
     * If non-null, may contain connected Font values and names/aliases to look them up with using [@Name] syntax.
     */
    public FontFamily family;

    /**
     * Determines how colors are looked up by name; defaults to using {@link Colors}.
     */
    public ColorLookup colorLookup = ColorLookup.GdxColorLookup.INSTANCE;
    /** Bit flag for bold mode, as a long. */
    public static final long BOLD = 1L << 30;
    /** Bit flag for oblique mode, as a long. */
    public static final long OBLIQUE = 1L << 29;
    /** Bit flag for underline mode, as a long. */
    public static final long UNDERLINE = 1L << 28;
    /** Bit flag for strikethrough mode, as a long. */
    public static final long STRIKETHROUGH = 1L << 27;
    /** Bit flag for subscript mode, as a long. */
    public static final long SUBSCRIPT = 1L << 25;
    /** Bit flag for midscript mode, as a long. */
    public static final long MIDSCRIPT = 2L << 25;
    /**
     * Two-bit flag for superscript mode, as a long.
     * This can also be checked to see if it is non-zero, which it will be if any of
     * {@link #SUBSCRIPT}, {@link #MIDSCRIPT}, or SUPERSCRIPT are enabled.
     */
    public static final long SUPERSCRIPT = 3L << 25;

    private final float[] vertices = new float[20];
    private final Layout tempLayout = Layout.POOL.obtain();
    private final LongArray glyphBuffer = new LongArray(128);
    /**
     * Must be in lexicographic order because we use {@link Arrays#binarySearch(char[], int, int, char)} to
     * verify if a char is present.
     */
    private final CharArray breakChars = CharArray.with(
            '\t',    // horizontal tab
            ' ',     // space
            '-',     // ASCII hyphen-minus
            '\u00AD',// soft hyphen
            '\u2000',// Unicode space
            '\u2001',// Unicode space
            '\u2002',// Unicode space
            '\u2003',// Unicode space
            '\u2004',// Unicode space
            '\u2005',// Unicode space
            '\u2006',// Unicode space
            '\u2008',// Unicode space
            '\u2009',// Unicode space
            '\u200A',// Unicode space (hair-width)
            '\u200B',// Unicode space (zero-width)
            '\u2010',// hyphen (not minus)
            '\u2012',// figure dash
            '\u2013',// en dash
            '\u2014',// em dash
            '\u2027' // hyphenation point
    );

    /**
     * Must be in lexicographic order because we use {@link Arrays#binarySearch(char[], int, int, char)} to
     * verify if a char is present.
     */
    private final CharArray spaceChars = CharArray.with(
            '\t',    // horizontal tab
            ' ',     // space
            '\u2000',// Unicode space
            '\u2001',// Unicode space
            '\u2002',// Unicode space
            '\u2003',// Unicode space
            '\u2004',// Unicode space
            '\u2005',// Unicode space
            '\u2006',// Unicode space
            '\u2008',// Unicode space
            '\u2009',// Unicode space
            '\u200A',// Unicode space (hair-width)
            '\u200B' // Unicode space (zero-width)
    );

    /**
     * The standard libGDX vertex shader source, which is also used by the MSDF shader.
     */
    public static final String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
            + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
            + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n"
            + "uniform mat4 u_projTrans;\n"
            + "varying vec4 v_color;\n"
            + "varying vec2 v_texCoords;\n"
            + "\n"
            + "void main() {\n"
            + "	v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
            + "	v_color.a = v_color.a * (255.0/254.0);\n"
            + "	v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n"
            + "	gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
            + "}\n";

    /**
     * Fragment shader source meant for MSDF fonts. This is automatically used when {@link #enableShader(Batch)} is
     * called and the {@link #distanceField} is {@link DistanceFieldType#MSDF}.
     */
    public static final String msdfFragmentShader =  "#ifdef GL_ES\n"
            + "	precision mediump float;\n"
            + "	precision mediump int;\n"
            + "#endif\n"
            + "\n"
            + "uniform sampler2D u_texture;\n"
            + "uniform float u_smoothing;\n"
            + "varying vec4 v_color;\n"
            + "varying vec2 v_texCoords;\n"
            + "\n"
            + "void main() {\n"
            + "  vec3 sdf = texture2D(u_texture, v_texCoords).rgb;\n"
            + "  gl_FragColor = vec4(v_color.rgb, clamp((max(min(sdf.r, sdf.g), min(max(sdf.r, sdf.g), sdf.b)) - 0.5) * u_smoothing + 0.5, 0.0, 1.0) * v_color.a);\n"
            + "}\n";

    /**
     * The ShaderProgram used to render this font, as used by {@link #enableShader(Batch)}.
     * If this is null, the font will be rendered with the Batch's default shader.
     * It may be set to a custom ShaderProgram if {@link #distanceField} is set to {@link DistanceFieldType#MSDF},
     * or to one created by {@link DistanceFieldFont#createDistanceFieldShader()} if distanceField is set to
     * {@link DistanceFieldType#SDF}. It can be set to a user-defined ShaderProgram; if it is meant to render
     * MSDF or SDF fonts, then the ShaderProgram should have a {@code uniform float u_smoothing;} that will be
     * set by {@link #enableShader(Batch)}. Values passed to u_smoothing can vary a lot, depending on how the
     * font was initially created, its current scale, and its {@link #actualCrispness} field. You can
     * also use a user-defined ShaderProgram with a font using {@link DistanceFieldType#STANDARD}, which may be
     * easier and can use any uniforms you normally could with a ShaderProgram, since enableShader() won't
     * change any of the uniforms.
     */
    public ShaderProgram shader = null;

    //// font parsing section

    private static final int[] hexCodes = new int[]
            {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
                    -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
                    -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
                    0, 1, 2, 3, 4, 5, 6, 7, 8, 9,-1,-1,-1,-1,-1,-1,
                    -1,10,11,12,13,14,15,-1,-1,-1,-1,-1,-1,-1,-1,-1,
                    -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
                    -1,10,11,12,13,14,15};
    /**
     * Reads in a CharSequence containing only hex digits (only 0-9, a-f, and A-F) with an optional sign at the start
     * and returns the long they represent, reading at most 16 characters (17 if there is a sign) and returning the
     * result if valid, or 0 if nothing could be read. The leading sign can be '+' or '-' if present. This can also
     * represent negative numbers as they are printed by such methods as String.format given %x in the formatting
     * string; that is, if the first char of a 16-char (or longer)
     * CharSequence is a hex digit 8 or higher, then the whole number represents a negative number, using two's
     * complement and so on. This means "FFFFFFFFFFFFFFFF" would return the long -1 when passed to this, though you
     * could also simply use "-1 ". If you use both '-' at the start and have the most significant digit as 8 or higher,
     * such as with "-FFFFFFFFFFFFFFFF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a hex digit, or
     * stopping the parse process early if a non-hex-digit char is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     * @param cs a CharSequence, such as a String, containing only hex digits with an optional sign (no 0x at the start)
     * @param start the (inclusive) first character position in cs to read
     * @param end the (exclusive) last character position in cs to read (this stops after 16 characters if end is too large)
     * @return the long that cs represents
     */
    private static long longFromHex(final CharSequence cs, final int start, int end) {
        int len, h, lim = 16;
        if (cs == null || start < 0 || end <= 0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if (c == '-') {
            len = -1;
            h = 0;
            lim = 17;
        } else if (c == '+') {
            len = 1;
            h = 0;
            lim = 17;
        } else if (c > 102 || (h = hexCodes[c]) < 0)
            return 0;
        else {
            len = 1;
        }
        long data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if ((c = cs.charAt(i)) > 102 || (h = hexCodes[c]) < 0)
                return data * len;
            data <<= 4;
            data |= h;
        }
        return data * len;
    }

    /**
     * Reads in a CharSequence containing only decimal digits (0-9) with an optional sign at the start and returns the
     * int they represent, reading at most 10 characters (11 if there is a sign) and returning the result if valid, or 0
     * if nothing could be read. The leading sign can be '+' or '-' if present. This can technically be used to handle
     * unsigned integers in decimal format, but it isn't the intended purpose. If you do use it for handling unsigned
     * ints, 2147483647 is normally the highest positive int and -2147483648 the lowest negative one, but if you give
     * this a number between 2147483647 and {@code 2147483647 + 2147483648}, it will interpret it as a negative number
     * that fits in bounds using the normal rules for converting between signed and unsigned numbers.
     * <br>
     * Should be fairly close to the JDK's Integer.parseInt method, but this also supports CharSequence data instead of
     * just String data, and allows specifying a start and end. This doesn't throw on invalid input, either, instead
     * returning 0 if the first char is not a decimal digit, or stopping the parse process early if a non-decimal-digit
     * char is read before end is reached. If the parse is stopped early, this behaves as you would expect for a number
     * with fewer digits, and simply doesn't fill the larger places.
     * @param cs a CharSequence, such as a String, containing only digits 0-9 with an optional sign
     * @param start the (inclusive) first character position in cs to read
     * @param end the (exclusive) last character position in cs to read (this stops after 10 or 11 characters if end is too large, depending on sign)
     * @return the int that cs represents
     */
    private static int intFromDec(final CharSequence cs, final int start, int end)
    {
        int len, h, lim = 10;
        if(cs == null || start < 0 || end <=0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if(c == '-')
        {
            len = -1;
            lim = 11;
            h = 0;
        }
        else if(c == '+')
        {
            len = 1;
            lim = 11;
            h = 0;
        }
        else if(c > 102 || (h = hexCodes[c]) < 0 || h > 9)
            return 0;
        else
        {
            len = 1;
        }
        int data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if((c = cs.charAt(i)) > 102 || (h = hexCodes[c]) < 0 || h > 9)
                return data * len;
            data = data * 10 + h;
        }
        return data * len;
    }

    private static int indexAfter(String text, String search, int from){
        return ((from = text.indexOf(search, from)) < 0 ? text.length() : from + search.length());
    }

    /**
     * Returns true if {@code c} is a lower-case letter, or false otherwise.
     * Similar to {@link Character#isLowerCase(char)}, but should actually work on GWT.
     * @param c a char to check
     * @return true if c is a lower-case letter, or false otherwise.
     */
    public static boolean isLowerCase(char c) {
        return Category.Ll.contains(c);
    }

    /**
     * Returns true if {@code c} is an upper-case letter, or false otherwise.
     * Similar to {@link Character#isUpperCase(char)}, but should actually work on GWT.
     * @param c a char to check
     * @return true if c is an upper-case letter, or false otherwise.
     */
    public static boolean isUpperCase(char c) {
        return Category.Lu.contains(c);
    }

    /**
     * Gets the ColorLookup this uses to look up colors by name.
     * @return a ColorLookup implementation
     */
    public ColorLookup getColorLookup() {
        return colorLookup;
    }

    /**
     * Unlikely to be used in most games, this allows changing how colors are looked up by name (or built) given a
     * {@link ColorLookup} interface implementation.
     * @param lookup a non-null ColorLookup
     */
    public void setColorLookup(ColorLookup lookup){
        if(lookup != null)
            colorLookup = lookup;
    }

    //// constructor section

    /**
     * Constructs a Font by reading in the given .fnt file and loading any images it specifies. Tries an internal handle
     * first, then a local handle. Does not use a distance field effect.
     * @param fntName the file path and name to a .fnt file this will load
     */
    public Font(String fntName){
        this(fntName, DistanceFieldType.STANDARD, 0f, 0f, 0f, 0f);
    }
    /**
     * Constructs a Font by reading in the given .fnt file and loading any images it specifies. Tries an internal handle
     * first, then a local handle. Uses the specified distance field effect.
     * @param fntName the file path and name to a .fnt file this will load
     * @param distanceField determines how edges are drawn; if unsure, you should use {@link DistanceFieldType#STANDARD}
     */
    public Font(String fntName, DistanceFieldType distanceField){
        this(fntName, distanceField, 0f, 0f, 0f, 0f);
    }

    /**
     * Constructs a Font by reading in the given .fnt file and the given Texture by filename. Tries an internal handle
     * first, then a local handle. Does not use a distance field effect.
     * @param fntName the file path and name to a .fnt file this will load
     */
    public Font(String fntName, String textureName){
        this(fntName, textureName, DistanceFieldType.STANDARD, 0f, 0f, 0f, 0f);
    }
    /**
     * Constructs a Font by reading in the given .fnt file and the given Texture by filename. Tries an internal handle
     * first, then a local handle. Uses the specified distance field effect.
     * @param fntName the file path and name to a .fnt file this will load
     * @param distanceField determines how edges are drawn; if unsure, you should use {@link DistanceFieldType#STANDARD}
     */
    public Font(String fntName, String textureName, DistanceFieldType distanceField){
        this(fntName, textureName, distanceField, 0f, 0f, 0f, 0f);
    }

    /**
     * Copy constructor; does not copy the font's {@link #shader} or {@link #colorLookup}, if it has them (it uses the
     * same reference for the new Font), but will fully copy everything else.
     * @param toCopy another Font to copy
     */
    public Font(Font toCopy){
        distanceField = toCopy.distanceField;
        isMono = toCopy.isMono;
        actualCrispness = toCopy.actualCrispness;
        distanceFieldCrispness = toCopy.distanceFieldCrispness;
        parents = new Array<>(toCopy.parents);
        cellWidth = toCopy.cellWidth;
        cellHeight = toCopy.cellHeight;
        scaleX = toCopy.scaleX;
        scaleY = toCopy.scaleY;
        originalCellWidth = toCopy.originalCellWidth;
        originalCellHeight = toCopy.originalCellHeight;
        mapping = new IntMap<>(toCopy.mapping.size);
        for(IntMap.Entry<GlyphRegion> e : toCopy.mapping){
            if(e.value == null) continue;
            mapping.put(e.key, new GlyphRegion(e.value));
        }
        defaultValue = toCopy.defaultValue;
        kerning = toCopy.kerning == null ? null : new IntIntMap(toCopy.kerning);
        solidBlock = toCopy.solidBlock;

        // shader and colorLookup are not copied, because there isn't much point in having different copies of
        // a ShaderProgram or stateless ColorLookup.
        if(toCopy.shader != null)
            shader = toCopy.shader;
        if(toCopy.colorLookup != null)
            colorLookup = toCopy.colorLookup;
    }

    /**
     * Constructs a new Font by reading in a .fnt file with the given name (an internal handle is tried first, then a
     * local handle) and loading any images specified in that file. No distance field effect is used.
     * This allows globally adjusting the x and y positions of glyphs in the font, as well as
     * globally adjusting the horizontal and vertical space glyphs take up. Changing these adjustments by small values
     * can drastically improve the appearance of text, but has to be manually edited; every font is quite different.
     * If you want to add empty space around each character, you can add approximately the normal
     * {@link #originalCellWidth} to widthAdjust and about half that to xAdjust; this can be used to make the glyphs fit
     * in square cells.
     * @param fntName the path and filename of a .fnt file this will load; may be internal or local
     * @param xAdjust how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust how many pixels to add to the used height of each character, using more above
     */
    public Font(String fntName,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this(fntName, DistanceFieldType.STANDARD, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    /**
     * Constructs a new Font by reading in a .fnt file with the given name (an internal handle is tried first, then a
     * local handle) and loading any images specified in that file. The specified distance field effect is used.
     * This allows globally adjusting the x and y positions of glyphs in the font, as well as
     * globally adjusting the horizontal and vertical space glyphs take up. Changing these adjustments by small values
     * can drastically improve the appearance of text, but has to be manually edited; every font is quite different.
     * If you want to add empty space around each character, you can add approximately the normal
     * {@link #originalCellWidth} to widthAdjust and about half that to xAdjust; this can be used to make the glyphs fit
     * in square cells.
     * @param fntName the path and filename of a .fnt file this will load; may be internal or local
     * @param distanceField determines how edges are drawn; if unsure, you should use {@link DistanceFieldType#STANDARD}
     * @param xAdjust how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust how many pixels to add to the used height of each character, using more above
     */
    public Font(String fntName, DistanceFieldType distanceField,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this.distanceField = distanceField;
        if (distanceField == DistanceFieldType.MSDF) {
            shader = new ShaderProgram(vertexShader, msdfFragmentShader);
            if (!shader.isCompiled())
                Gdx.app.error("textratypist", "MSDF shader failed to compile: " + shader.getLog());
        }
        else if(distanceField == DistanceFieldType.SDF){
            shader = DistanceFieldFont.createDistanceFieldShader();
            if(!shader.isCompiled())
                Gdx.app.error("textratypist", "SDF shader failed to compile: " + shader.getLog());
        }
        loadFNT(fntName, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    /**
     * Constructs a new Font by reading in a Texture from the given named path (internal is tried, then local),
     * and no distance field effect.
     * This allows globally adjusting the x and y positions of glyphs in the font, as well as
     * globally adjusting the horizontal and vertical space glyphs take up. Changing these adjustments by small values
     * can drastically improve the appearance of text, but has to be manually edited; every font is quite different.
     * If you want to add empty space around each character, you can add approximately the normal
     * {@link #originalCellWidth} to widthAdjust and about half that to xAdjust; this can be used to make the glyphs fit
     * in square cells.
     * @param fntName the path and filename of a .fnt file this will load; may be internal or local
     * @param textureName the path and filename of a texture file this will load; may be internal or local
     * @param xAdjust how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust how many pixels to add to the used height of each character, using more above
     */
    public Font(String fntName, String textureName,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this(fntName, textureName, DistanceFieldType.STANDARD, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    /**
     * Constructs a new Font by reading in a Texture from the given named path (internal is tried, then local),
     * and the specified distance field effect.
     * This allows globally adjusting the x and y positions of glyphs in the font, as well as
     * globally adjusting the horizontal and vertical space glyphs take up. Changing these adjustments by small values
     * can drastically improve the appearance of text, but has to be manually edited; every font is quite different.
     * If you want to add empty space around each character, you can add approximately the normal
     * {@link #originalCellWidth} to widthAdjust and about half that to xAdjust; this can be used to make the glyphs fit
     * in square cells.
     * @param fntName the path and filename of a .fnt file this will load; may be internal or local
     * @param textureName the path and filename of a texture file this will load; may be internal or local
     * @param distanceField determines how edges are drawn; if unsure, you should use {@link DistanceFieldType#STANDARD}
     * @param xAdjust how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust how many pixels to add to the used height of each character, using more above
     */
    public Font(String fntName, String textureName, DistanceFieldType distanceField,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this.distanceField = distanceField;
        if (distanceField == DistanceFieldType.MSDF) {
            shader = new ShaderProgram(vertexShader, msdfFragmentShader);
            if (!shader.isCompiled())
                Gdx.app.error("textratypist", "MSDF shader failed to compile: " + shader.getLog());
        }
        else if(distanceField == DistanceFieldType.SDF){
            shader = DistanceFieldFont.createDistanceFieldShader();
            if(!shader.isCompiled())
                Gdx.app.error("textratypist", "SDF shader failed to compile: " + shader.getLog());
        }
        FileHandle textureHandle;
        if ((textureHandle = Gdx.files.internal(textureName)).exists()
                || (textureHandle = Gdx.files.local(textureName)).exists()) {
            parents = Array.with(new TextureRegion(new Texture(textureHandle)));
            if (distanceField == DistanceFieldType.SDF || distanceField == DistanceFieldType.MSDF) {
                parents.first().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            }
        } else {
            throw new RuntimeException("Missing texture file: " + textureName);
        }
        loadFNT(fntName, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    /**
     * Constructs a font using the given TextureRegion that holds all of its glyphs, with no distance field effect.
     * This allows globally adjusting the x and y positions of glyphs in the font, as well as
     * globally adjusting the horizontal and vertical space glyphs take up. Changing these adjustments by small values
     * can drastically improve the appearance of text, but has to be manually edited; every font is quite different.
     * If you want to add empty space around each character, you can add approximately the normal
     * {@link #originalCellWidth} to widthAdjust and about half that to xAdjust; this can be used to make the glyphs fit
     * in square cells.
     * @param fntName the path and filename of a .fnt file this will load; may be internal or local
     * @param textureRegion an existing TextureRegion, typically inside a larger TextureAtlas
     * @param xAdjust how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust how many pixels to add to the used height of each character, using more above
     */
    public Font(String fntName, TextureRegion textureRegion,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this(fntName, textureRegion, DistanceFieldType.STANDARD, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    /**
     * Constructs a font based off of an AngelCode BMFont .fnt file and the given TextureRegion that holds all of its
     * glyphs, with the specified distance field effect.
     * This allows globally adjusting the x and y positions of glyphs in the font, as well as
     * globally adjusting the horizontal and vertical space glyphs take up. Changing these adjustments by small values
     * can drastically improve the appearance of text, but has to be manually edited; every font is quite different.
     * If you want to add empty space around each character, you can add approximately the normal
     * {@link #originalCellWidth} to widthAdjust and about half that to xAdjust; this can be used to make the glyphs fit
     * in square cells.
     * @param fntName the path and filename of a .fnt file this will load; may be internal or local
     * @param textureRegion an existing TextureRegion, typically inside a larger TextureAtlas
     * @param distanceField determines how edges are drawn; if unsure, you should use {@link DistanceFieldType#STANDARD}
     * @param xAdjust how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust how many pixels to add to the used height of each character, using more above
     */
    public Font(String fntName, TextureRegion textureRegion, DistanceFieldType distanceField,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this.distanceField = distanceField;
        if (distanceField == DistanceFieldType.MSDF) {
            shader = new ShaderProgram(vertexShader, msdfFragmentShader);
            if (!shader.isCompiled())
                Gdx.app.error("textratypist", "MSDF shader failed to compile: " + shader.getLog());
        }
        else if(distanceField == DistanceFieldType.SDF){
            shader = DistanceFieldFont.createDistanceFieldShader();
            if(!shader.isCompiled())
                Gdx.app.error("textratypist", "SDF shader failed to compile: " + shader.getLog());
        }
        this.parents = Array.with(textureRegion);
        if (distanceField == DistanceFieldType.SDF || distanceField == DistanceFieldType.MSDF) {
            textureRegion.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        loadFNT(fntName, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    /**
     * Constructs a font based off of an AngelCode BMFont .fnt file and the given TextureRegion Array, with no distance
     * field effect. This allows globally adjusting the x and y positions of glyphs in the font, as well as
     * globally adjusting the horizontal and vertical space glyphs take up. Changing these adjustments by small values
     * can drastically improve the appearance of text, but has to be manually edited; every font is quite different.
     * If you want to add empty space around each character, you can add approximately the normal
     * {@link #originalCellWidth} to widthAdjust and about half that to xAdjust; this can be used to make the glyphs fit
     * in square cells.
     * @param fntName the path and filename of a .fnt file this will load; may be internal or local
     * @param textureRegions an Array of TextureRegions that will be used in order as the .fnt file uses more pages
     * @param xAdjust how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust how many pixels to add to the used height of each character, using more above
     */
    public Font(String fntName, Array<TextureRegion> textureRegions,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this(fntName, textureRegions, DistanceFieldType.STANDARD, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }
    /**
     * Constructs a font based off of an AngelCode BMFont .fnt file, with the given TextureRegion Array and specified
     * distance field effect. This allows globally adjusting the x and y positions of glyphs in the font, as well as
     * globally adjusting the horizontal and vertical space glyphs take up. Changing these adjustments by small values
     * can drastically improve the appearance of text, but has to be manually edited; every font is quite different.
     * If you want to add empty space around each character, you can add approximately the normal
     * {@link #originalCellWidth} to widthAdjust and about half that to xAdjust; this can be used to make the glyphs fit
     * in square cells.
     * @param fntName the path and filename of a .fnt file this will load; may be internal or local
     * @param textureRegions an Array of TextureRegions that will be used in order as the .fnt file uses more pages
     * @param distanceField determines how edges are drawn; if unsure, you should use {@link DistanceFieldType#STANDARD}
     * @param xAdjust how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust how many pixels to add to the used height of each character, using more above
     */
    public Font(String fntName, Array<TextureRegion> textureRegions, DistanceFieldType distanceField,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this.distanceField = distanceField;
        if (distanceField == DistanceFieldType.MSDF) {
            shader = new ShaderProgram(vertexShader, msdfFragmentShader);
            if (!shader.isCompiled())
                Gdx.app.error("textratypist", "MSDF shader failed to compile: " + shader.getLog());
        }
        else if(distanceField == DistanceFieldType.SDF){
            shader = DistanceFieldFont.createDistanceFieldShader();
            if(!shader.isCompiled())
                Gdx.app.error("textratypist", "SDF shader failed to compile: " + shader.getLog());
        }
        this.parents = textureRegions;
        if ((distanceField == DistanceFieldType.SDF || distanceField == DistanceFieldType.MSDF)
                && textureRegions != null)
        {
            for(TextureRegion parent : textureRegions)
                parent.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        loadFNT(fntName, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    /**
     * Constructs a new Font from the existing BitmapFont, using its same Textures and TextureRegions for glyphs, and
     * without a distance field effect.
     * @param bmFont an existing BitmapFont that will be copied in almost every way this can
     * @param xAdjust how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust how many pixels to add to the used height of each character, using more above
     */
    public Font(BitmapFont bmFont,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this(bmFont, DistanceFieldType.STANDARD, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }
    /**
     * Constructs a new Font from the existing BitmapFont, using its same Textures and TextureRegions for glyphs, and
     * with the specified distance field effect.
     * @param bmFont an existing BitmapFont that will be copied in almost every way this can
     * @param distanceField determines how edges are drawn; if unsure, you should use {@link DistanceFieldType#STANDARD}
     * @param xAdjust how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust how many pixels to add to the used height of each character, using more above
     */
    public Font(BitmapFont bmFont, DistanceFieldType distanceField,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this.distanceField = distanceField;
        if (distanceField == DistanceFieldType.MSDF) {
            shader = new ShaderProgram(vertexShader, msdfFragmentShader);
            if (!shader.isCompiled())
                Gdx.app.error("textratypist", "MSDF shader failed to compile: " + shader.getLog());
        }
        else if(distanceField == DistanceFieldType.SDF){
            shader = DistanceFieldFont.createDistanceFieldShader();
            if(!shader.isCompiled())
                Gdx.app.error("textratypist", "SDF shader failed to compile: " + shader.getLog());
        }
        this.parents = bmFont.getRegions();
        if ((distanceField == DistanceFieldType.SDF || distanceField == DistanceFieldType.MSDF)
                && parents != null)
        {
            for(TextureRegion parent : parents)
                parent.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        BitmapFont.BitmapFontData data = bmFont.getData();
        mapping = new IntMap<>(128);
        int minWidth = Integer.MAX_VALUE;
        for (BitmapFont.Glyph[] page : data.glyphs) {
            if (page == null) continue;
            for (BitmapFont.Glyph glyph : page) {
                if (glyph != null) {
                    int x = glyph.srcX, y = glyph.srcY, w = glyph.width, h = glyph.height, a = glyph.xadvance;
                    x += xAdjust;
                    y += yAdjust;
                    a += widthAdjust;
                    h += heightAdjust;
                    minWidth = Math.min(minWidth, a);
                    cellWidth = Math.max(a, cellWidth);
                    cellHeight = Math.max(h, cellHeight);
                    GlyphRegion gr = new GlyphRegion(bmFont.getRegion(glyph.page), x, y, w, h);
                    if(glyph.id == 10)
                    {
                        a = 0;
                        gr.offsetX = 0;
                    }
                    else {
                        gr.offsetX = glyph.xoffset;
                    }
                    gr.offsetY = -h - glyph.yoffset;
                    gr.xAdvance = a;
                    mapping.put(glyph.id & 0xFFFF, gr);
                    if(glyph.kerning != null) {
                        if(kerning == null) kerning = new IntIntMap(128);
                        for (int b = 0; b < glyph.kerning.length; b++) {
                            byte[] kern = glyph.kerning[b];
                            if(kern != null) {
                                int k;
                                for (int i = 0; i < 512; i++) {
                                    k = kern[i];
                                    if (k != 0) {
                                        kerning.put(glyph.id << 16 | (b << 9 | i), k);
                                    }
                                    if((b << 9 | i) == '['){
                                        kerning.put(glyph.id << 16 | 2, k);
                                    }
                                }
                            }
                        }
                    }
                    if((glyph.id & 0xFFFF) == '['){
                        mapping.put(2, gr);
                        if(glyph.kerning != null) {
                            for (int b = 0; b < glyph.kerning.length; b++) {
                                byte[] kern = glyph.kerning[b];
                                if(kern != null) {
                                    int k;
                                    for (int i = 0; i < 512; i++) {
                                        k = kern[i];
                                        if (k != 0) {
                                            kerning.put(2 << 16 | (b << 9 | i), k);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // Newlines shouldn't render.
        if(mapping.containsKey('\n')){
            GlyphRegion gr = mapping.get('\n');
            gr.setRegionWidth(0);
            gr.setRegionHeight(0);
        }
        defaultValue =  mapping.get(data.missingGlyph == null ? ' ' : data.missingGlyph.id, mapping.get(' ', mapping.values().next()));
        originalCellWidth = cellWidth;
        originalCellHeight = cellHeight;
        isMono = minWidth == cellWidth && kerning == null;
        scale(bmFont.getScaleX(), bmFont.getScaleY());
    }

    /**
     * Constructs a new Font by reading in a SadConsole .font file with the given name (an internal handle is tried
     * first, then a local handle) and loading any images specified in that file. This never uses a distance field
     * effect, and always tries to load one image by the path specified in the .font file.
     * @param fntName the path and filename of a .font file this will load; may be internal or local
     * @param ignoredSadConsoleFlag the value is ignored here; the presence of this parameter says to load a SadConsole .font file
     */
    public Font(String fntName, boolean ignoredSadConsoleFlag) {
        this.distanceField = DistanceFieldType.STANDARD;
        loadSad(fntName);
    }

    /**
     * The gritty parsing code that pulls relevant info from an AngelCode BMFont .fnt file and uses it to assemble the
     * many {@link GlyphRegion}s this has for each glyph.
     * @param fntName the file name of the .fnt file; can be internal or local
     * @param xAdjust added to the x-position for each glyph in the font
     * @param yAdjust added to the y-position for each glyph in the font
     * @param widthAdjust added to the glyph width for each glyph in the font
     * @param heightAdjust added to the glyph height for each glyph in the font
     */
    protected void loadFNT(String fntName, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        FileHandle fntHandle;
        String fnt;
        if ((fntHandle = Gdx.files.internal(fntName)).exists()
                || (fntHandle = Gdx.files.local(fntName)).exists()) {
            fnt = fntHandle.readString("UTF8");
        } else {
            throw new RuntimeException("Missing font file: " + fntName);
        }
        int idx = indexAfter(fnt, " pages=", 0);
        int pages = intFromDec(fnt, idx, idx = indexAfter(fnt, "\npage id=", idx));
        if (parents == null || parents.size < pages) {
            if (parents == null) parents = new Array<>(true, pages, TextureRegion.class);
            else parents.clear();
            FileHandle textureHandle;
            for (int i = 0; i < pages; i++) {
                String textureName = fnt.substring(idx = indexAfter(fnt, "file=\"", idx), idx = fnt.indexOf('"', idx));
                if ((textureHandle = Gdx.files.internal(textureName)).exists()
                        || (textureHandle = Gdx.files.local(textureName)).exists()) {
                    parents.add(new TextureRegion(new Texture(textureHandle)));
                    if (distanceField == DistanceFieldType.SDF || distanceField == DistanceFieldType.MSDF)
                        parents.peek().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                } else {
                    throw new RuntimeException("Missing texture file: " + textureName);
                }

            }
        }
        int size = intFromDec(fnt, idx = indexAfter(fnt, "\nchars count=", idx), idx = indexAfter(fnt, "\nchar id=", idx));
        mapping = new IntMap<>(size);
        int minWidth = Integer.MAX_VALUE;
        for (int i = 0; i < size; i++) {
            int c = intFromDec(fnt, idx, idx = indexAfter(fnt, " x=", idx));
            int x = intFromDec(fnt, idx, idx = indexAfter(fnt, " y=", idx));
            int y = intFromDec(fnt, idx, idx = indexAfter(fnt, " width=", idx));
            int w = intFromDec(fnt, idx, idx = indexAfter(fnt, " height=", idx));
            int h = intFromDec(fnt, idx, idx = indexAfter(fnt, " xoffset=", idx));
            int xo = intFromDec(fnt, idx, idx = indexAfter(fnt, " yoffset=", idx));
            int yo = intFromDec(fnt, idx, idx = indexAfter(fnt, " xadvance=", idx));
            int a = intFromDec(fnt, idx, idx = indexAfter(fnt, " page=", idx));
            int p = intFromDec(fnt, idx, idx = indexAfter(fnt, "\nchar id=", idx));

            x += xAdjust;
            y += yAdjust;
            a += widthAdjust;
            h += heightAdjust;
            minWidth = Math.min(minWidth, a);
            cellWidth = Math.max(a, cellWidth);
            cellHeight = Math.max(h, cellHeight);
            GlyphRegion gr = new GlyphRegion(parents.get(p), x, y, w, h);
            if(c == 10)
            {
                a = 0;
                gr.offsetX = 0;
            }
            else
                gr.offsetX = xo;
            gr.offsetY = yo;
            gr.xAdvance = a;
            mapping.put(c, gr);
            if(c == '['){
                mapping.put(2, gr);
            }
        }
        idx = indexAfter(fnt, "\nkernings count=", 0);
        if(idx < fnt.length()){
            int kernings = intFromDec(fnt, idx, idx = indexAfter(fnt, "\nkerning first=", idx));
            kerning = new IntIntMap(kernings);
            for (int i = 0; i < kernings; i++) {
                int first = intFromDec(fnt, idx, idx = indexAfter(fnt, " second=", idx));
                int second = intFromDec(fnt, idx, idx = indexAfter(fnt, " amount=", idx));
                int amount = intFromDec(fnt, idx, idx = indexAfter(fnt, "\nkerning first=", idx));
                kerning.put(first << 16 | second, amount);
                if(first == '['){
                    kerning.put(2 << 16 | second, amount);
                }
                if(second == '['){
                    kerning.put(first << 16 | 2, amount);
                }
            }
        }
        // Newlines shouldn't render.
        if(mapping.containsKey('\n')){
            GlyphRegion gr = mapping.get('\n');
            gr.setRegionWidth(0);
            gr.setRegionHeight(0);
        }
        defaultValue = mapping.get(' ', mapping.get(0));
        originalCellWidth = cellWidth;
        originalCellHeight = cellHeight;
        isMono = minWidth == cellWidth && kerning == null;
    }

    /**
     * The parsing code that pulls relevant info from a SadConsole .font configuration file and uses it to assemble the
     * many {@link GlyphRegion}s this has for each glyph.
     * @param fntName the name of a font file this will load from an internal or local file handle (tried in that order)
     */
    protected void loadSad(String fntName) {
        FileHandle fntHandle;
        JsonValue fnt;
        JsonReader reader = new JsonReader();
        if ((fntHandle = Gdx.files.internal(fntName)).exists()
                || (fntHandle = Gdx.files.local(fntName)).exists()) {
            fnt = reader.parse(fntHandle);
        } else {
            throw new RuntimeException("Missing font file: " + fntName);
        }
        int pages = 1;
        TextureRegion parent;
        if (parents == null || parents.size == 0) {
            if (parents == null) parents = new Array<>(true, pages, TextureRegion.class);
            FileHandle textureHandle;
            String textureName = fnt.getString("FilePath");
            if ((textureHandle = Gdx.files.internal(textureName)).exists()
                    || (textureHandle = Gdx.files.local(textureName)).exists()) {
                parents.add(parent = new TextureRegion(new Texture(textureHandle)));
            } else {
                throw new RuntimeException("Missing texture file: " + textureName);
            }
        }
        else parent = parents.first();

        int columns = fnt.getInt("Columns");
        int padding = fnt.getInt("GlyphPadding");
        cellHeight = fnt.getInt("GlyphHeight");
        cellWidth = fnt.getInt("GlyphWidth");
        int rows = (parent.getRegionHeight() + padding) / ((int) cellHeight + padding);
        int size = rows * columns;
        mapping = new IntMap<>(size+1);
        for (int y = 0, c = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++, c++) {
                GlyphRegion gr = new GlyphRegion(parent, x * ((int)cellWidth + padding), y * ((int)cellHeight + padding), (int)cellWidth, (int)cellHeight);
                gr.offsetX = 0;
                gr.offsetY = 0;
                if (c == 10) {
                    gr.xAdvance = 0;
                } else {
                    gr.xAdvance = cellWidth;
                }
                mapping.put(c, gr);
                if (c == '[') {
                    if(mapping.containsKey(2))
                        mapping.put(size, mapping.get(2));
                    mapping.put(2, gr);
                }
            }
        }
        solidBlock = (char) fnt.getInt("SolidGlyphIndex");
        // Newlines shouldn't render.
        if(mapping.containsKey('\n')){
            GlyphRegion gr = mapping.get('\n');
            gr.setRegionWidth(0);
            gr.setRegionHeight(0);
        }
        defaultValue = mapping.get(' ', mapping.get(0));
        originalCellWidth = this.cellWidth;
        originalCellHeight = this.cellHeight;
        isMono = true;
    }

    //// usage section

    /**
     * Assembles two chars into a kerning pair that can be looked up as a key in {@link #kerning}.
     * If you give such a pair to {@code kerning}'s {@link IntIntMap#get(int, int)} method, you'll get the amount of
     * extra space (in the same unit the font uses) this will insert between {@code first} and {@code second}.
     * @param first the first char
     * @param second the second char
     * @return a kerning pair that can be looked up in {@link #kerning}
     */
    public int kerningPair(char first, char second) {
        return first << 16 | (second & 0xFFFF);
    }

    /**
     * Scales the font by the given horizontal and vertical multipliers.
     * @param horizontal how much to multiply the width of each glyph by
     * @param vertical how much to multiply the height of each glyph by
     * @return this Font, for chaining
     */
    public Font scale(float horizontal, float vertical) {
        scaleX *= horizontal;
        scaleY *= vertical;
        cellWidth *= horizontal;
        cellHeight *= vertical;
        return this;
    }

    /**
     * Scales the font so that it will have the given width and height.
     * @param width the target width of the font, in world units
     * @param height the target height of the font, in world units
     * @return this Font, for chaining
     */
    public Font scaleTo(float width, float height) {
        scaleX = width / originalCellWidth;
        scaleY = height / originalCellHeight;
        cellWidth  = width;
        cellHeight = height;
        return this;
    }

    /**
     * Multiplies the line height by {@code multiplier} without changing the size of any characters.
     * This can cut off the tops of letters if the multiplier is too small.
     * @param multiplier will be applied to {@link #cellHeight} and {@link #originalCellHeight}
     * @return this Font, for chaining
     */
    public Font adjustLineHeight(float multiplier){
        cellHeight *= multiplier;
        originalCellHeight *= multiplier;
        return this;
    }

    /**
     * Multiplies the width used by each glyph in a monospaced font by {@code multiplier} without changing the size of
     * any characters.
     * @param multiplier will be applied to {@link #cellWidth} and {@link #originalCellWidth}
     * @return this Font, for chaining
     */
    public Font adjustCellWidth(float multiplier){
        cellWidth *= multiplier;
        originalCellWidth *= multiplier;
        return this;
    }

    /**
     * Calls {@link #setTextureFilter(Texture.TextureFilter, Texture.TextureFilter)} with
     * {@link Texture.TextureFilter#Linear} for both min and mag filters.
     * This is the most common usage for setting the texture filters, and is appropriate when you have
     * a large TextureRegion holding the font and you normally downscale it. This is automatically done
     * for {@link DistanceFieldType#SDF} and {@link DistanceFieldType#MSDF} fonts, but you may also want
     * to use it for {@link DistanceFieldType#STANDARD} fonts when downscaling (they can look terrible
     * if the default {@link Texture.TextureFilter#Nearest} filter is used).
     * Note that this sets the filter on every Texture that holds a TextureRegion used by the font, so
     * it may affect the filter on other parts of an atlas.
     * @return this, for chaining
     */
    public Font setTextureFilter() {
        return setTextureFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    /**
     * Sets the texture filters on each Texture that holds a TextureRegion used by the font to the given
     * {@code minFilter} and {@code magFilter}. You may want to use this to set a font using
     * {@link DistanceFieldType#STANDARD} to use a better TextureFilter for smooth downscaling, like
     * {@link Texture.TextureFilter#MipMapLinearLinear} or just
     * {@link Texture.TextureFilter#Linear}. You might, for some reason, want to
     * set a font using {@link DistanceFieldType#SDF} or {@link DistanceFieldType#MSDF} to use TextureFilters
     * other than its default {@link Texture.TextureFilter#Linear}.
     * Note that this may affect the filter on other parts of an atlas.
     * @return this, for chaining
     */
    public Font setTextureFilter(Texture.TextureFilter minFilter, Texture.TextureFilter magFilter) {
        for(TextureRegion parent : parents){
            parent.getTexture().setFilter(minFilter, magFilter);
        }
        return this;
    }

    /**
     * Must be called before drawing anything with an SDF or MSDF font; does not need to be called for other fonts
     * unless you are mixing them with SDF/MSDF fonts or other shaders. This also resets the Batch color to white, in
     * case it had been left with a different setting before. If this Font is not an MSDF font, then this resets batch's
     * shader to the default (using {@code batch.setShader(null)}).
     * <br>
     * This is called automatically for {@link TextraLabel} and {@link TypingLabel} if it hasn't been called already.
     * You may still want to call this automatically for those cases if you have multiple such Labels that use the same
     * Font; in that case, you can draw several Labels without ending the current batch. You do need to set the shader
     * back to whatever you use for other items before you draw those, typically with {@code batch.setShader(null);} .
     * @param batch the Batch to instruct to use the appropriate shader for this font; should usually be a SpriteBatch
     */
    public void enableShader(Batch batch) {
        if(distanceField == DistanceFieldType.MSDF) {
            if (batch.getShader() != shader) {
                batch.setShader(shader);
                shader.setUniformf("u_smoothing", 7f * actualCrispness * Math.max(cellHeight / originalCellHeight, cellWidth / originalCellWidth));
            }
        } else if(distanceField == DistanceFieldType.SDF){
            if (batch.getShader() != shader) {
                batch.setShader(shader);
                final float scale = Math.max(cellHeight / originalCellHeight, cellWidth / originalCellWidth) * 0.5f + 0.125f;
                shader.setUniformf("u_smoothing", (actualCrispness / (scale)));
            }
        } else {
            batch.setShader(null);
        }
        batch.setPackedColor(Color.WHITE_FLOAT_BITS);
    }

    /**
     * Draws the specified text at the given x,y position (in world space) with a white foreground.
     * @param batch typically a SpriteBatch
     * @param text typically a String, but this can also be a StringBuilder or some custom class
     * @param x the x position in world space to start drawing the text at (lower left corner)
     * @param y the y position in world space to start drawing the text at (lower left corner)
     */
    public void drawText(Batch batch, CharSequence text, float x, float y) {
        drawText(batch, text, x, y, -2);
    }
    /**
     * Draws the specified text at the given x,y position (in world space) with the given foreground color.
     * @param batch typically a SpriteBatch
     * @param text typically a String, but this can also be a StringBuilder or some custom class
     * @param x the x position in world space to start drawing the text at (lower left corner)
     * @param y the y position in world space to start drawing the text at (lower left corner)
     * @param color an int color; typically this is RGBA, but custom shaders or Batches can use other kinds of color
     */
    public void drawText(Batch batch, CharSequence text, float x, float y, int color) {
        batch.setPackedColor(NumberUtils.intToFloatColor(Integer.reverseBytes(color)));
        GlyphRegion current;
        for (int i = 0, n = text.length(); i < n; i++) {
            batch.draw(current = mapping.get(text.charAt(i)), x + current.offsetX, y + current.offsetY, current.getRegionWidth(), current.getRegionHeight());
            x += current.getRegionWidth();
        }
    }

    /**
     * Draws a grid made of rectangular blocks of int colors (typically RGBA) at the given x,y position in world space.
     * This is only useful for monospace fonts. This assumes there is a full-block character at char u0000 by default,
     * or at {@link #solidBlock} if you have set that field; Glamer produces fonts that have a block at u0000 already.
     * The {@code colors} parameter should be a rectangular 2D array, and because any colors that are the default int
     * value {@code 0} will be treated as transparent RGBA values, if a value is not assigned to a slot in the array
     * then nothing will be drawn there. The 2D array is treated as [x][y] indexed here. This is usually called before
     * other methods that draw foreground text.
     * <br>
     * Internally, this uses {@link Batch#draw(Texture, float[], int, int)} to draw each rectangle with minimal
     * overhead, and this also means it is unaffected by the batch color. If you want to alter the colors using a
     * shader, the shader will receive each color in {@code colors} as its {@code a_color} attribute, the same as if it
     * was passed via the batch color.
     * <br>
     * If you want to change the alpha of the colors array, you can use
     * {@link ColorUtils#multiplyAllAlpha(int[][], float)}.
     * @param batch typically a SpriteBatch
     * @param colors a 2D rectangular array of int colors (typically RGBA)
     * @param x the x position in world space to draw the text at (lower left corner)
     * @param y the y position in world space to draw the text at (lower left corner)
     */
    public void drawBlocks(Batch batch, int[][] colors, float x, float y) {
        drawBlocks(batch, solidBlock, colors, x, y);
    }
    /**
     * Draws a grid made of rectangular blocks of int colors (typically RGBA) at the given x,y position in world space.
     * This is only useful for monospace fonts.
     * The {@code blockChar} should visually be represented by a very large block, occupying all of a monospaced cell.
     * The {@code colors} parameter should be a rectangular 2D array, and because any colors that are the default int
     * value {@code 0} will be treated as transparent RGBA values, if a value is not assigned to a slot in the array
     * then nothing will be drawn there. The 2D array is treated as [x][y] indexed here. This is usually called before
     * other methods that draw foreground text.
     * <br>
     * Internally, this uses {@link Batch#draw(Texture, float[], int, int)} to draw each rectangle with minimal
     * overhead, and this also means it is unaffected by the batch color. If you want to alter the colors using a
     * shader, the shader will receive each color in {@code colors} as its {@code a_color} attribute, the same as if it
     * was passed via the batch color.
     * <br>
     * If you want to change the alpha of the colors array, you can use
     * {@link ColorUtils#multiplyAllAlpha(int[][], float)}.
     * @param batch typically a SpriteBatch
     * @param blockChar a char that renders as a full block, occupying an entire monospaced cell with a color
     * @param colors a 2D rectangular array of int colors (typically RGBA)
     * @param x the x position in world space to draw the text at (lower left corner)
     * @param y the y position in world space to draw the text at (lower left corner)
     */
    public void drawBlocks(Batch batch, char blockChar, int[][] colors, float x, float y) {
        final TextureRegion block = mapping.get(blockChar);
        if(block == null) return;
        final Texture parent = block.getTexture();
        final float u = block.getU() + (block.getU2() - block.getU()) * 0.25f,
                v = block.getV() + (block.getV2() - block.getV()) * 0.25f,
                u2 = block.getU2() - (block.getU2() - block.getU()) * 0.25f,
                v2 = block.getV2() - (block.getV2() - block.getV()) * 0.25f;
        vertices[0] = x;
        vertices[1] = y;
        //vertices[2] = color;
        vertices[3] = u;
        vertices[4] = v;

        vertices[5] = x;
        vertices[6] = y + cellHeight;
        //vertices[7] = color;
        vertices[8] = u;
        vertices[9] = v2;

        vertices[10] = x + cellWidth;
        vertices[11] = y + cellHeight;
        //vertices[12] = color;
        vertices[13] = u2;
        vertices[14] = v2;

        vertices[15] = x + cellWidth;
        vertices[16] = y;
        //vertices[17] = color;
        vertices[18] = u2;
        vertices[19] = v;
        for (int xi = 0, xn = colors.length, yn = colors[0].length; xi < xn; xi++) {
            for (int yi = 0; yi < yn; yi++) {
                if((colors[xi][yi] & 254) != 0) {
                    vertices[2] = vertices[7] = vertices[12] = vertices[17] =
                            NumberUtils.intBitsToFloat(Integer.reverseBytes(colors[xi][yi] & -2));
                    batch.draw(parent, vertices, 0, 20);
                }
                vertices[1] = vertices[16] += cellHeight;
                vertices[6] = vertices[11] += cellHeight;
            }
            vertices[0] = vertices[5] += cellWidth;
            vertices[10] = vertices[15] += cellWidth;
            vertices[1] = vertices[16] = y;
            vertices[6] = vertices[11] = y + cellHeight;
        }
    }

    /**
     * Draws the specified text at the given x,y position (in world space), parsing an extension of libGDX markup
     * and using it to determine color, size, position, shape, strikethrough, underline, case, and scale of the given
     * CharSequence. The text drawn will start as white, with the normal size as by {@link #cellWidth} and
     * {@link #cellHeight}, normal case, and without bold, italic, superscript, subscript, strikethrough, or
     * underline. Markup starts with {@code [}; the next non-letter character determines what that piece of markup
     * toggles. Markup this knows:
     * <ul>
     *     <li>{@code [[} escapes a literal left bracket.</li>
     *     <li>{@code []} clears all markup to the initial state without any applied.</li>
     *     <li>{@code [*]} toggles bold mode.</li>
     *     <li>{@code [/]} toggles italic (technically, oblique) mode.</li>
     *     <li>{@code [^]} toggles superscript mode (and turns off subscript or midscript mode).</li>
     *     <li>{@code [=]} toggles midscript mode (and turns off superscript or subscript mode).</li>
     *     <li>{@code [.]} toggles subscript mode (and turns off superscript or midscript mode).</li>
     *     <li>{@code [_]} toggles underline mode.</li>
     *     <li>{@code [~]} toggles strikethrough mode.</li>
     *     <li>{@code [!]} toggles all upper case mode.</li>
     *     <li>{@code [,]} toggles all lower case mode.</li>
     *     <li>{@code [;]} toggles capitalize each word mode.</li>
     *     <li>{@code [%P]}, where P is a percentage from 0 to 375, changes the scale to that percentage (rounded to
     *     the nearest 25% mark).</li>
     *     <li>{@code [%]}, with no number just after it, resets scale to 100%.</li>
     *     <li>{@code [#HHHHHHHH]}, where HHHHHHHH is a hex RGB888 or RGBA8888 int color, changes the color.</li>
     *     <li>{@code [COLORNAME]}, where "COLORNAME" is a typically-upper-case color name that will be looked up with
     *     {@link #getColorLookup()}, changes the color. The name can optionally be preceded by {@code |}, which allows
     *     looking up colors with names that contain punctuation.</li>
     * </ul>
     * <br>
     * Parsing markup for a full screen every frame typically isn't necessary, and you may want to store the most recent
     * glyphs by calling {@link #markup(String, Layout)} and render its result with
     * {@link #drawGlyphs(Batch, Layout, float, float)} every frame.
     * @param batch typically a SpriteBatch
     * @param text typically a String with markup, but this can also be a StringBuilder or some custom class
     * @param x the x position in world space to start drawing the text at (lower left corner)
     * @param y the y position in world space to start drawing the text at (lower left corner)
     * @return the number of glyphs drawn
     */
    public int drawMarkupText(Batch batch, String text, float x, float y) {
        Layout layout = tempLayout;
        layout.clear();
        markup(text, tempLayout);
        final int lines = layout.lines();
        int drawn = 0;
        for (int ln = 0; ln < lines; ln++) {
            Line line = layout.getLine(ln);
            int n = line.glyphs.size;
            drawn += n;
            if (kerning != null) {
                int kern = -1, amt;
                long glyph;
                for (int i = 0; i < n; i++) {
                    kern = kern << 16 | (int) ((glyph = line.glyphs.get(i)) & 0xFFFF);
                    amt = kerning.get(kern, 0);
                    x += drawGlyph(batch, glyph, x + amt, y) + amt;
                }
            } else {
                for (int i = 0; i < n; i++) {
                    x += drawGlyph(batch, line.glyphs.get(i), x, y);
                }
            }
            y -= cellHeight;
        }
        return drawn;
    }

        /**
     * Draws the specified Layout of glyphs with a Batch at a given x, y position, drawing the full layout.
     * @param batch typically a SpriteBatch
     * @param glyphs typically returned as part of {@link #markup(String, Layout)}
     * @param x the x position in world space to start drawing the glyph at (lower left corner)
     * @param y the y position in world space to start drawing the glyph at (lower left corner)
     * @return the number of glyphs drawn
     */
    public float drawGlyphs(Batch batch, Layout glyphs, float x, float y) {
        return drawGlyphs(batch, glyphs, x, y, Align.left);
    }
    /**
     * Draws the specified Layout of glyphs with a Batch at a given x, y position, using {@code align} to
     * determine how to position the text. Typically, align is {@link Align#left}, {@link Align#center}, or
     * {@link Align#right}, which make the given x,y point refer to the lower-left corner, center-bottom edge point, or
     * lower-right corner, respectively.
     * @param batch typically a SpriteBatch
     * @param glyphs typically returned by {@link #markup(String, Layout)}
     * @param x the x position in world space to start drawing the glyph at (where this is depends on align)
     * @param y the y position in world space to start drawing the glyph at (where this is depends on align)
     * @param align an {@link Align} constant; if {@link Align#left}, x and y refer to the lower left corner
     * @return the number of glyphs drawn
     */
    public float drawGlyphs(Batch batch, Layout glyphs, float x, float y, int align) {
        float drawn = 0;
        final int lines = glyphs.lines();
        Line l;
        for (int ln = 0; ln < lines; ln++) {
            l = glyphs.getLine(ln);
            y -= l.height;
            drawn += drawGlyphs(batch, l, x, y, align);
        }
        return drawn;
    }

    /**
     * Draws the specified Line of glyphs with a Batch at a given x, y position, drawing the full Line using left
     * alignment.
     * @param batch typically a SpriteBatch
     * @param glyphs typically returned as part of {@link #markup(String, Layout)}
     * @param x the x position in world space to start drawing the glyph at (lower left corner)
     * @param y the y position in world space to start drawing the glyph at (lower left corner)
     * @return the number of glyphs drawn
     */
    public float drawGlyphs(Batch batch, Line glyphs, float x, float y) {
        if(glyphs == null) return 0;
        return drawGlyphs(batch, glyphs, x, y, Align.left);
    }
    /**
     * Draws the specified Line of glyphs with a Batch at a given x, y position, using {@code align} to
     * determine how to position the text. Typically, align is {@link Align#left}, {@link Align#center}, or
     * {@link Align#right}, which make the given x,y point refer to the lower-left corner, center-bottom edge point, or
     * lower-right corner, respectively.
     * @param batch typically a SpriteBatch
     * @param glyphs typically returned as part of {@link #markup(String, Layout)}
     * @param x the x position in world space to start drawing the glyph at (where this is depends on align)
     * @param y the y position in world space to start drawing the glyph at (where this is depends on align)
     * @param align an {@link Align} constant; if {@link Align#left}, x and y refer to the lower left corner
     * @return the number of glyphs drawn
     */
    public float drawGlyphs(Batch batch, Line glyphs, float x, float y, int align) {
        if (glyphs == null) return 0;
        float drawn = 0f;
        if (Align.isCenterHorizontal(align))
            x -= glyphs.width * 0.5f;
        else if (Align.isRight(align))
            x -= glyphs.width;
        int kern = -1;
        long glyph;
        float single;
        for (int i = 0, n = glyphs.glyphs.size; i < n; i++) {
            glyph = glyphs.glyphs.get(i);
            Font font = null;
            if(family != null) font = family.connected[(int)(glyph >>> 16 & 15)];
            if(font == null) font = this;

            if (font.kerning != null) {
                kern = kern << 16 | (int) (glyph & 0xFFFF);
                float amt = font.kerning.get(kern, 0) * font.scaleX * (glyph + 0x400000L >>> 20 & 15) * 0.25f;
                single = drawGlyph(batch, glyph, x + amt, y) + amt;
            } else {
                single = drawGlyph(batch, glyph, x, y);
            }
            x += single;
            drawn += single;
        }
        return drawn;
    }

    /**
     * Gets the distance to advance the cursor after drawing {@code glyph}, scaled by {@link #scaleX} as if drawing.
     * This handles monospaced fonts correctly and ensures that for variable-width fonts, subscript, midscript, and
     * superscript halve the advance amount. This does not consider kerning, if the font has it. If the glyph is fully
     * transparent, this does not draw it at all, and treats its x advance as 0. This version of xAdvance does not
     * read the scale information from glyph, and instead takes it from the scale parameter.
     * @param scale the scale to draw the glyph at, usually {@link #scaleX} and possibly adjusted by per-glyph scaling
     * @param glyph a long encoding the color, style information, and char of a glyph, as from a {@link Line}
     * @return the (possibly non-integer) amount to advance the cursor when you draw the given glyph, not counting kerning
     */
    private float xAdvanceInternal(Font font, float scale, long glyph){
        if(glyph >>> 32 == 0L) return 0;
        GlyphRegion tr = font.mapping.get((char) glyph);
        if (tr == null) return 0f;
        float changedW = tr.xAdvance * scale;
        if (font.isMono) {
            changedW += tr.offsetX * scale;
        }
        else if((glyph & SUPERSCRIPT) != 0L){
            changedW *= 0.5f;
        }
        return changedW;
    }

    /**
     * Gets the distance to advance the cursor after drawing {@code glyph}, scaled by {@link #scaleX} as if drawing.
     * This handles monospaced fonts correctly and ensures that for variable-width fonts, subscript, midscript, and
     * superscript halve the advance amount. This does not consider kerning, if the font has it. If the glyph is fully
     * transparent, this does not draw it at all, and treats its x advance as 0.
     * @param glyph a long encoding the color, style information, and char of a glyph, as from a {@link Line}
     * @return the (possibly non-integer) amount to advance the cursor when you draw the given glyph, not counting kerning
     */
    public float xAdvance(long glyph){
        if(glyph >>> 32 == 0L) return 0;
        GlyphRegion tr = mapping.get((char) glyph);
        if (tr == null) return 0f;
        float scale = scaleX * (glyph + 0x400000L >>> 20 & 15) * 0.25f;
        float changedW = tr.xAdvance * scale;
        if (isMono) {
            changedW += tr.offsetX * scale;
        }
        else if((glyph & SUPERSCRIPT) != 0L){
            changedW *= 0.5f;
        }
        return changedW;
    }

    /**
     * Measures the actual width that the given Line will use when drawn.
     * @param line a Line, as from inside a Layout
     * @return the width in world units
     */
    public float measureWidth(Line line) {
        float drawn = 0f;
        LongArray glyphs = line.glyphs;
        if (kerning != null) {
            int kern = -1;
            float amt;
            long glyph;
            for (int i = 0, n = glyphs.size; i < n; i++) {
                kern = kern << 16 | (int) ((glyph = glyphs.get(i)) & 0xFFFF);
                float scale = scaleX * (glyph + 0x400000L >>> 20 & 15) * 0.25f;
                amt = kerning.get(kern, 0) * scale;
                GlyphRegion tr = mapping.get((char)glyph);
                if(tr == null) continue;
                float changedW = tr.xAdvance * scale;
                if (isMono) {
                    changedW += tr.offsetX * scale;
                }
                else if((glyph & SUPERSCRIPT) != 0L)
                    changedW *= 0.5f;
                drawn += changedW + amt;
            }
        } else {
            for (int i = 0, n = glyphs.size; i < n; i++) {
                long glyph = glyphs.get(i);
                float scale = scaleX * (glyph + 0x400000L >>> 20 & 15) * 0.25f;
                GlyphRegion tr = mapping.get((char)glyph);
                if(tr == null) continue;
                float changedW = tr.xAdvance * scale;
                if (isMono) {
                    changedW += tr.offsetX * scale;
                }
                else if((glyph & SUPERSCRIPT) != 0L)
                    changedW *= 0.5f;
                drawn += changedW;
            }
        }
        return drawn;
    }

    /**
     * Measures the actual width that the given Line will use when drawn.
     * @param line a Line, as from inside a Layout
     * @return the width in world units
     */
    public Line calculateSize(Line line) {
        float drawn = 0f;
        float storedScaleX = scaleX, storedScaleY = scaleY;
        float scale;
        LongArray glyphs = line.glyphs;
        boolean curly = false;
        if (kerning != null) {
            int kern = -1;
            float amt;
            for (int i = 0, n = glyphs.size; i < n; i++) {
                long glyph = glyphs.get(i);
                char ch = (char) glyph;
                if(curly) {
                    if(ch == '}')
                    {
                        curly = false;
                        continue;
                    }
                    else if(ch == '{')
                        curly = false;
                    else continue;
                }
                else if(ch == '{') {
                    curly = true;
                    continue;
                }
                kern = kern << 16 | ch;
                scale = (glyph + 0x400000L >>> 20 & 15) * 0.25f;
                line.height = Math.max(line.height, cellHeight * scale);
                scaleX = storedScaleX * scale;
                scaleY = storedScaleY * scale;
                amt = kerning.get(kern, 0) * scaleX;
                GlyphRegion tr = mapping.get((char)glyph);
                if(tr == null) continue;
                float changedW = tr.xAdvance * scaleX;
                if (isMono) {
                    changedW += tr.offsetX * scaleX;
                }
                else if((glyph & SUPERSCRIPT) != 0L)
                    changedW *= 0.5f;
                drawn += changedW + amt;
            }
        } else {
            for (int i = 0, n = glyphs.size; i < n; i++) {
                long glyph = glyphs.get(i);
                char ch = (char) glyph;
                if(curly) {
                    if(ch == '}')
                    {
                        curly = false;
                        continue;
                    }
                    else if(ch == '{')
                        curly = false;
                    else continue;
                }
                else if(ch == '{') {
                    curly = true;
                    continue;
                }
                GlyphRegion tr = mapping.get(ch);
                if(tr == null) continue;
                scale = (glyph + 0x400000L >>> 20 & 15) * 0.25f;
                line.height = Math.max(line.height, cellHeight * scale);
                scaleX = storedScaleX * scale;
                scaleY = storedScaleY * scale;
                float changedW = tr.xAdvance * scaleX;
                if (isMono) {
                    changedW += tr.offsetX * scaleX;
                }
                if(!isMono && (glyph & SUPERSCRIPT) != 0L)
                    changedW *= 0.5f;
                drawn += changedW;
            }
        }
        line.width = drawn;
        scaleX = storedScaleX;
        scaleY = storedScaleY;
        return line;
    }

    /**
     * Draws the specified glyph with a Batch at the given x, y position. The glyph contains multiple types of data all
     * packed into one {@code long}: the bottom 16 bits store a {@code char}, the roughly 16 bits above that store
     * formatting (bold, underline, superscript, etc.), and the remaining upper 32 bits store color as RGBA.
     * @param batch typically a SpriteBatch
     * @param glyph a long storing a char, format, and color; typically part of a longer formatted text as a LongArray
     * @param x the x position in world space to start drawing the glyph at (lower left corner)
     * @param y the y position in world space to start drawing the glyph at (lower left corner)
     * @return the distance in world units the drawn glyph uses up for width, as in a line of text
     */
    public float drawGlyph(Batch batch, long glyph, float x, float y) {
        Font font = null;
        if(family != null) font = family.connected[(int)(glyph >>> 16 & 15)];
        if(font == null) font = this;

        GlyphRegion tr = font.mapping.get((char) glyph);
        if (tr == null) return 0f;
        Texture tex = tr.getTexture();
        float x0 = 0f, x1 = 0f, x2 = 0f, x3 = 0f;
        float y0 = 0f, y1 = 0f, y2 = 0f, y3 = 0f;
        float scale = (glyph + 0x400000L >>> 20 & 15) * 0.25f;
        float scaleX = font.scaleX * scale;
        float scaleY = font.scaleY * scale;

        float color = NumberUtils.intBitsToFloat(((int)(batch.getColor().a * (glyph >>> 33 & 127)) << 25)
                | (0xFFFFFF & Integer.reverseBytes((int) (glyph >>> 32))));
        final float iw = 1f / tex.getWidth();
        float u, v, u2, v2;
        u = tr.getU();
        v = tr.getV();
        u2 = tr.getU2();
        v2 = tr.getV2();
        float w = tr.getRegionWidth() * scaleX, changedW = tr.xAdvance * scaleX, h = tr.getRegionHeight() * scaleY;
        if (font.isMono) {
            changedW += tr.offsetX * scaleX;
        } else {
            x += tr.offsetX * scaleX;
        }
        float yt = y + font.cellHeight * scale - h - tr.offsetY * scaleY;
        if ((glyph & OBLIQUE) != 0L) {
            x0 += h * 0.2f;
            x1 -= h * 0.2f;
            x2 -= h * 0.2f;
            x3 += h * 0.2f;
        }
        final long script = (glyph & SUPERSCRIPT);
        float scaledHeight = font.cellHeight * scale;
        if (script == SUPERSCRIPT) {
            w *= 0.5f;
            h *= 0.5f;
            yt = y + scaledHeight * 0.625f - h - tr.offsetY * scaleY * 0.5f;
            y0 += scaledHeight * 0.375f;
            y1 += scaledHeight * 0.375f;
            y2 += scaledHeight * 0.375f;
            y3 += scaledHeight * 0.375f;
            if(!font.isMono)
                changedW *= 0.5f;
        }
        else if (script == SUBSCRIPT) {
            w *= 0.5f;
            h *= 0.5f;
            yt = y + scaledHeight * 0.625f - h - tr.offsetY * scaleY * 0.5f;
            y0 -= scaledHeight * 0.125f;
            y1 -= scaledHeight * 0.125f;
            y2 -= scaledHeight * 0.125f;
            y3 -= scaledHeight * 0.125f;
            if(!font.isMono)
                changedW *= 0.5f;
        }
        else if(script == MIDSCRIPT) {
            w *= 0.5f;
            h *= 0.5f;
            yt = y + scaledHeight * 0.625f - h - tr.offsetY * scaleY * 0.5f;
            y0 += scaledHeight * 0.125f;
            y1 += scaledHeight * 0.125f;
            y2 += scaledHeight * 0.125f;
            y3 += scaledHeight * 0.125f;
            if(!font.isMono)
                changedW *= 0.5f;
        }

        vertices[0] = x + x0;
        vertices[1] = yt + y0 + h;
        vertices[2] = color;
        vertices[3] = u;
        vertices[4] = v;

        vertices[5] = x + x1;
        vertices[6] = yt + y1;
        vertices[7] = color;
        vertices[8] = u;
        vertices[9] = v2;

        vertices[10] = x + x2 + w;
        vertices[11] = yt + y2;
        vertices[12] = color;
        vertices[13] = u2;
        vertices[14] = v2;

        vertices[15] = x + x3 + w;
        vertices[16] = yt + y3 + h;
        vertices[17] = color;
        vertices[18] = u2;
        vertices[19] = v;
        batch.draw(tex, vertices, 0, 20);
        if ((glyph & BOLD) != 0L) {
            vertices[0] +=  1f;
            vertices[5] +=  1f;
            vertices[10] += 1f;
            vertices[15] += 1f;
            batch.draw(tex, vertices, 0, 20);
            vertices[0] -=  2f;
            vertices[5] -=  2f;
            vertices[10] -= 2f;
            vertices[15] -= 2f;
            batch.draw(tex, vertices, 0, 20);
            vertices[0] +=  0.5f;
            vertices[5] +=  0.5f;
            vertices[10] += 0.5f;
            vertices[15] += 0.5f;
            batch.draw(tex, vertices, 0, 20);
            vertices[0] +=  1f;
            vertices[5] +=  1f;
            vertices[10] += 1f;
            vertices[15] += 1f;
            batch.draw(tex, vertices, 0, 20);
        }
        if ((glyph & UNDERLINE) != 0L) {
            final GlyphRegion under = font.mapping.get('_');
            if (under != null) {
                final float underU = under.getU() + (under.xAdvance - under.offsetX) * iw * 0.5f,
                        underV = under.getV(),
                        underU2 = underU + iw,
                        underV2 = under.getV2(),
                        hu = under.getRegionHeight() * scaleY, yu = y + font.cellHeight * scale - hu - under.offsetY * scaleY;
                x0 = x + scaleX * under.offsetX + scale;
                vertices[0] = x0 - scale;
                vertices[1] = yu + hu;
                vertices[2] = color;
                vertices[3] = underU;
                vertices[4] = underV;

                vertices[5] = x0 - scale;
                vertices[6] = yu;
                vertices[7] = color;
                vertices[8] = underU;
                vertices[9] = underV2;

                vertices[10] = x0 + changedW + scale;
                vertices[11] = yu;
                vertices[12] = color;
                vertices[13] = underU2;
                vertices[14] = underV2;

                vertices[15] = x0 + changedW + scale;
                vertices[16] = yu + hu;
                vertices[17] = color;
                vertices[18] = underU2;
                vertices[19] = underV;
                batch.draw(under.getTexture(), vertices, 0, 20);
            }
        }
        if ((glyph & STRIKETHROUGH) != 0L) {
            final GlyphRegion dash = font.mapping.get('-');
            if (dash != null) {
                final float dashU = dash.getU() + (dash.xAdvance - dash.offsetX) * iw * 0.5f,
                        dashV = dash.getV(),
                        dashU2 = dashU + iw,
                        dashV2 = dash.getV2(),
                        hd = dash.getRegionHeight() * scaleY, yd = y + font.cellHeight * scale - hd - dash.offsetY * scaleY;
                x0 = x + scaleX * dash.offsetX + scale;
                vertices[0] = x0 - scale;
                vertices[1] = yd + hd;
                vertices[2] = color;
                vertices[3] = dashU;
                vertices[4] = dashV;

                vertices[5] = x0 - scale;
                vertices[6] = yd;
                vertices[7] = color;
                vertices[8] = dashU;
                vertices[9] = dashV2;

                vertices[10] = x0 + changedW + scale;
                vertices[11] = yd;
                vertices[12] = color;
                vertices[13] = dashU2;
                vertices[14] = dashV2;

                vertices[15] = x0 + changedW + scale;
                vertices[16] = yd + hd;
                vertices[17] = color;
                vertices[18] = dashU2;
                vertices[19] = dashV;
                batch.draw(dash.getTexture(), vertices, 0, 20);
            }
        }
        return changedW;
    }

    /**
     * Draws the specified glyph with a Batch at the given x, y position and with the specified counterclockwise
     * rotation, measured in degrees. The glyph contains multiple types of data all packed into one {@code long}:
     * the bottom 16 bits store a {@code char}, the roughly 16 bits above that store formatting (bold, underline,
     * superscript, etc.), and the remaining upper 32 bits store color as RGBA. Rotation is not stored in the long
     * glyph; it may change frequently or as part of an animation.
     * @param batch typically a SpriteBatch
     * @param glyph a long storing a char, format, and color; typically part of a longer formatted text as a LongList
     * @param x the x position in world space to start drawing the glyph at (lower left corner)
     * @param y the y position in world space to start drawing the glyph at (lower left corner)
     * @param rotation what angle to rotate the glyph, measured in degrees
     * @return the distance in world units the drawn glyph uses up for width, as in a line of text along the given rotation
     */
    public float drawGlyph(Batch batch, long glyph, float x, float y, float rotation) {
        if(MathUtils.isZero(rotation % 360f)){
            return drawGlyph(batch, glyph, x, y);
        }
        final float sin = MathUtils.sinDeg(rotation);
        final float cos = MathUtils.cosDeg(rotation);

        Font font = null;
        if(family != null) font = family.connected[(int)(glyph >>> 16 & 15)];
        if(font == null) font = this;

        GlyphRegion tr = font.mapping.get((char) glyph);
        if (tr == null) return 0f;
        float scale = (glyph + 0x400000L >>> 20 & 15) * 0.25f;
        float scaleX = font.scaleX * scale;
        float scaleY = font.scaleY * scale;
        Texture tex = tr.getTexture();
        float x0 = 0f;
        float x1 = 0f;
        float x2 = 0f;
        float y0 = 0f;
        float y1 = 0f;
        float y2 = 0f;
        float color = NumberUtils.intBitsToFloat(((int)(batch.getColor().a * (glyph >>> 33 & 127)) << 25)
                | (0xFFFFFF & Integer.reverseBytes((int) (glyph >>> 32))));
        final float iw = 1f / tex.getWidth();
        float u, v, u2, v2;
        u = tr.getU();
        v = tr.getV();
        u2 = tr.getU2();
        v2 = tr.getV2();
        float w = tr.getRegionWidth() * scaleX, changedW = tr.xAdvance * scaleX, h = tr.getRegionHeight() * scaleY;
        if (font.isMono) {
            changedW += tr.offsetX * scaleX;
        } else {
            x += tr.offsetX * scaleX;
        }
        float yt = y + font.cellHeight * scale - h - tr.offsetY * scaleY;
        if ((glyph & OBLIQUE) != 0L) {
            x0 += h * 0.2f;
            x1 -= h * 0.2f;
            x2 -= h * 0.2f;
        }
        final long script = (glyph & SUPERSCRIPT);
        if (script == SUPERSCRIPT) {
            w *= 0.5f;
            h *= 0.5f;
            y1 += font.cellHeight * 0.375f;
            y2 += font.cellHeight * 0.375f;
            y0 += font.cellHeight * 0.375f;
            if(!font.isMono)
                changedW *= 0.5f;
        }
        else if (script == SUBSCRIPT) {
            w *= 0.5f;
            h *= 0.5f;
            y1 -= font.cellHeight * 0.125f;
            y2 -= font.cellHeight * 0.125f;
            y0 -= font.cellHeight * 0.125f;
            if(!font.isMono)
                changedW *= 0.5f;
        }
        else if(script == MIDSCRIPT) {
            w *= 0.5f;
            h *= 0.5f;
            y0 += font.cellHeight * 0.125f;
            y1 += font.cellHeight * 0.125f;
            y2 += font.cellHeight * 0.125f;
            if(!font.isMono)
                changedW *= 0.5f;
        }

        float p0x;
        float p0y;
        float p1x;
        float p1y;
        float p2x;
        float p2y;

        vertices[2] = color;
        vertices[3] = u;
        vertices[4] = v;

        vertices[7] = color;
        vertices[8] = u;
        vertices[9] = v2;

        vertices[12] = color;
        vertices[13] = u2;
        vertices[14] = v2;

        vertices[17] = color;
        vertices[18] = u2;
        vertices[19] = v;

        p0x = x + x0;
        p0y = yt + y0 + h;
        p1x = x + x1;
        p1y = yt + y1;
        p2x = x + x2 + w;
        p2y = yt + y2;

        vertices[15] = (vertices[0]  = cos * p0x - sin * p0y) - (vertices[5]  = cos * p1x - sin * p1y) + (vertices[10] = cos * p2x - sin * p2y);
        vertices[16] = (vertices[1]  = sin * p0x + cos * p0y) - (vertices[6]  = sin * p1x + cos * p1y) + (vertices[11] = sin * p2x + cos * p2y);

        batch.draw(tex, vertices, 0, 20);
        if ((glyph & BOLD) != 0L) {
            p0x += 1f;
            p1x += 1f;
            p2x += 1f;
            vertices[15] = (vertices[0]  = cos * p0x - sin * p0y) - (vertices[5]  = cos * p1x - sin * p1y) + (vertices[10] = cos * p2x - sin * p2y);
            vertices[16] = (vertices[1]  = sin * p0x + cos * p0y) - (vertices[6]  = sin * p1x + cos * p1y) + (vertices[11] = sin * p2x + cos * p2y);
            batch.draw(tex, vertices, 0, 20);
            p0x -= 2f;
            p1x -= 2f;
            p2x -= 2f;
            vertices[15] = (vertices[0]  = cos * p0x - sin * p0y) - (vertices[5]  = cos * p1x - sin * p1y) + (vertices[10] = cos * p2x - sin * p2y);
            vertices[16] = (vertices[1]  = sin * p0x + cos * p0y) - (vertices[6]  = sin * p1x + cos * p1y) + (vertices[11] = sin * p2x + cos * p2y);
            batch.draw(tex, vertices, 0, 20);
            p0x += 0.5f;
            p1x += 0.5f;
            p2x += 0.5f;
            vertices[15] = (vertices[0]  = cos * p0x - sin * p0y) - (vertices[5]  = cos * p1x - sin * p1y) + (vertices[10] = cos * p2x - sin * p2y);
            vertices[16] = (vertices[1]  = sin * p0x + cos * p0y) - (vertices[6]  = sin * p1x + cos * p1y) + (vertices[11] = sin * p2x + cos * p2y);
            batch.draw(tex, vertices, 0, 20);
            p0x += 1f;
            p1x += 1f;
            p2x += 1f;
            vertices[15] = (vertices[0]  = cos * p0x - sin * p0y) - (vertices[5]  = cos * p1x - sin * p1y) + (vertices[10] = cos * p2x - sin * p2y);
            vertices[16] = (vertices[1]  = sin * p0x + cos * p0y) - (vertices[6]  = sin * p1x + cos * p1y) + (vertices[11] = sin * p2x + cos * p2y);
            batch.draw(tex, vertices, 0, 20);
        }
        if ((glyph & UNDERLINE) != 0L) {
            final GlyphRegion under = font.mapping.get('_');
            if (under != null) {
                final float underU = under.getU() + (under.xAdvance - under.offsetX) * iw * 0.25f,
                        underV = under.getV(),
                        underU2 = under.getU() + (under.xAdvance - under.offsetX) * iw * 0.75f,
                        underV2 = under.getV2(),
                        hu = under.getRegionHeight() * scaleY, yu = y + font.cellHeight * scale - hu - under.offsetY * scaleY;
                vertices[2] = color;
                vertices[3] = underU;
                vertices[4] = underV;

                vertices[7] = color;
                vertices[8] = underU;
                vertices[9] = underV2;

                vertices[12] = color;
                vertices[13] = underU2;
                vertices[14] = underV2;

                vertices[17] = color;
                vertices[18] = underU2;
                vertices[19] = underV;

                p0x = x - scale;
                p0y = yu + hu;
                p1x = x - scale;
                p1y = yu;
                p2x = x + changedW + scale;
                p2y = yu;
                vertices[15] = (vertices[0]  = cos * p0x - sin * p0y) - (vertices[5]  = cos * p1x - sin * p1y) + (vertices[10] = cos * p2x - sin * p2y);
                vertices[16] = (vertices[1]  = sin * p0x + cos * p0y) - (vertices[6]  = sin * p1x + cos * p1y) + (vertices[11] = sin * p2x + cos * p2y);

                batch.draw(under.getTexture(), vertices, 0, 20);
            }
        }
        if ((glyph & STRIKETHROUGH) != 0L) {
            final GlyphRegion dash = font.mapping.get('-');
            if (dash != null) {
                final float dashU = dash.getU() + (dash.xAdvance - dash.offsetX) * iw * 0.625f,
                        dashV = dash.getV(),
                        dashU2 = dashU + iw,
                        dashV2 = dash.getV2(),
                        hd = dash.getRegionHeight() * scaleY, yd = y + font.cellHeight * scale - hd - dash.offsetY * scaleY;
                x0 = x - (dash.offsetX);
                vertices[2] = color;
                vertices[3] = dashU;
                vertices[4] = dashV;

                vertices[7] = color;
                vertices[8] = dashU;
                vertices[9] = dashV2;

                vertices[12] = color;
                vertices[13] = dashU2;
                vertices[14] = dashV2;

                vertices[17] = color;
                vertices[18] = dashU2;
                vertices[19] = dashV;

                p0x = x0 - scale;
                p0y = yd + hd;
                p1x = x0 - scale;
                p1y = yd;
                p2x = x0 + changedW + scale;
                p2y = yd;
                vertices[15] = (vertices[0]  = cos * p0x - sin * p0y) - (vertices[5]  = cos * p1x - sin * p1y) + (vertices[10] = cos * p2x - sin * p2y);
                vertices[16] = (vertices[1]  = sin * p0x + cos * p0y) - (vertices[6]  = sin * p1x + cos * p1y) + (vertices[11] = sin * p2x + cos * p2y);

                batch.draw(dash.getTexture(), vertices, 0, 20);
            }
        }
        return changedW;
    }
    /**
     * Reads markup from text, along with the chars to receive markup, processes it, and appends into appendTo, which is
     * a {@link Layout} holding one or more {@link Line}s. A common way of getting a Layout is with
     * {@code Layout.POOL.obtain()}; you can free the Layout when you are done using it with {@link Pool#free(Object)}
     * on {@link Layout#POOL}. This parses an extension of libGDX markup and uses it to determine color, size, position,
     * shape, strikethrough, underline, case, and scale of the given CharSequence. It also reads typing markup, for
     * effects, but passes it through without changing it and without considering it for line wrapping or text position.
     * The text drawn will start in {@code appendTo}'s {@link Layout#baseColor}, which is usually white, with the normal
     * size as determined by the font's metrics and scale ({@link #scaleX} and {@link #scaleY}), normal case, and
     * without bold, italic, superscript, subscript, strikethrough, or underline. Markup starts with {@code [}; the next
     * character determines what that piece of markup toggles. Markup this knows:
     * <ul>
     *     <li>{@code [[} escapes a literal left bracket.</li>
     *     <li>{@code []} clears all markup to the initial state without any applied.</li>
     *     <li>{@code [*]} toggles bold mode.</li>
     *     <li>{@code [/]} toggles italic (technically, oblique) mode.</li>
     *     <li>{@code [^]} toggles superscript mode (and turns off subscript or midscript mode).</li>
     *     <li>{@code [=]} toggles midscript mode (and turns off superscript or subscript mode).</li>
     *     <li>{@code [.]} toggles subscript mode (and turns off superscript or midscript mode).</li>
     *     <li>{@code [_]} toggles underline mode.</li>
     *     <li>{@code [~]} toggles strikethrough mode.</li>
     *     <li>{@code [!]} toggles all upper case mode.</li>
     *     <li>{@code [,]} toggles all lower case mode.</li>
     *     <li>{@code [;]} toggles capitalize each word mode.</li>
     *     <li>{@code [%P]}, where P is a percentage from 0 to 375, changes the scale to that percentage (rounded to
     *     the nearest 25% mark).</li>
     *     <li>{@code [%]}, with no number just after it, resets scale to 100%.</li>
     *     <li>{@code [#HHHHHHHH]}, where HHHHHHHH is a hex RGB888 or RGBA8888 int color, changes the color.</li>
     *     <li>{@code [COLORNAME]}, where "COLORNAME" is a typically-upper-case color name that will be looked up in
     *     {@link #getColorLookup()}, changes the color. The name can optionally be preceded by {@code |}, which allows
     *     looking up colors with names that contain punctuation.</li>
     * </ul>
     * You can render {@code appendTo} using {@link #drawGlyphs(Batch, Layout, float, float)}.
     * @param text text with markup
     * @param appendTo a Layout that stores one or more Line objects, carrying color, style, chars, and size
     * @return appendTo, for chaining
     */
    public Layout markup(String text, Layout appendTo) {
        boolean capitalize = false, previousWasLetter = false,
                capsLock = false, lowerCase = false;
        int c, scale = 3, fontIndex = -1;
        Font font = this;
        float scaleX;
        final long COLOR_MASK = 0xFFFFFFFF00000000L;
        long baseColor = Long.reverseBytes(NumberUtils.floatToIntColor(appendTo.getBaseColor())) & COLOR_MASK;
        long color = baseColor;
        long current = color;
        if(appendTo.font == null || !appendTo.font.equals(this))
        {
            appendTo.clear();
            appendTo.font(this);
        }
        appendTo.peekLine().height = 0;
        float targetWidth = appendTo.getTargetWidth();
        int kern = -1;
        for (int i = 0, n = text.length(); i < n; i++) {
            scaleX = font.scaleX * (scale + 1) * 0.25f;
            if(text.charAt(i) == '{') {
                int start = i;
                if (i+1 < n && text.charAt(i+1) != '{') {
                    int sizeChange = -1, fontChange = -1;
                    int end = text.indexOf('}', i);
                    for (; i < n && i <= end; i++) {
                        c = text.charAt(i);
                        appendTo.add(current | c);
                        if(c == '@') fontChange = i;
                        else if(c == '%') sizeChange = i;
                    }
                    if(start + 1 == end || "RESET".equalsIgnoreCase(text.substring(start+1, end)))
                    {
                        scale = 3;
                        font = this;
                    }
                    else if(fontChange >= 0 && family != null) {
                        fontIndex = family.fontAliases.get(text.substring(fontChange + 1, end), -1);
                        if (fontIndex == -1) font = this;
                        else {
                            font = family.connected[fontIndex];
                            if (font == null) font = this;
                        }
                    }
                    else if(sizeChange >= 0) {
                        if (sizeChange + 1 == end) {
                            int eq = text.indexOf('=', start);
                            if (eq + 1 == sizeChange) {
                                scale = 3;
                            } else {
                                scale = ((intFromDec(text, eq + 1, sizeChange) - 24) / 25) & 15;
                            }
                        } else {
                            scale = ((intFromDec(text, sizeChange + 1, end) - 24) / 25) & 15;
                        }
                    }
                    i--;
                }
            }
            else if(text.charAt(i) == '['){
                if(++i < n && (c = text.charAt(i)) != '['){
                    if(c == ']'){
                        color = baseColor;
                        current = color;
                        scale = 3;
                        font = this;
                        capitalize = false;
                        capsLock = false;
                        lowerCase = false;
                        continue;
                    }
                    int len = text.indexOf(']', i) - i;
                    switch (c) {
                        case '*':
                            current ^= BOLD;
                            break;
                        case '/':
                            current ^= OBLIQUE;
                            break;
                        case '^':
                            if ((current & SUPERSCRIPT) == SUPERSCRIPT)
                                current &= ~SUPERSCRIPT;
                            else
                                current |= SUPERSCRIPT;
                            break;
                        case '.':
                            if ((current & SUPERSCRIPT) == SUBSCRIPT)
                                current &= ~SUBSCRIPT;
                            else
                                current = (current & ~SUPERSCRIPT) | SUBSCRIPT;
                            break;
                        case '=':
                            if ((current & SUPERSCRIPT) == MIDSCRIPT)
                                current &= ~MIDSCRIPT;
                            else
                                current = (current & ~SUPERSCRIPT) | MIDSCRIPT;
                            break;
                        case '_':
                            current ^= UNDERLINE;
                            break;
                        case '~':
                            current ^= STRIKETHROUGH;
                            break;
                        case ';':
                            capitalize = !capitalize;
                            capsLock = false;
                            lowerCase = false;
                            break;
                        case '!':
                            capsLock = !capsLock;
                            capitalize = false;
                            lowerCase = false;
                            break;
                        case ',':
                            lowerCase = !lowerCase;
                            capitalize = false;
                            capsLock = false;
                            break;
                        case '%':
                            if(len >= 2)
                                current = (current & 0xFFFFFFFFFF0FFFFFL) | ((scale = ((intFromDec(text, i + 1, i + len) - 24) / 25) & 15) - 3 & 15) << 20;
                            else {
                                current = (current & 0xFFFFFFFFFF0FFFFFL);
                                scale = 3;
                            }
                            break;
                        case '#':
                            if (len >= 7 && len < 9)
                                color = longFromHex(text, i + 1, i + 7) << 40 | 0x000000FF00000000L;
                            else if (len >= 9)
                                color = longFromHex(text, i + 1, i + 9) << 32;
                            else
                                color = baseColor;
                            current = (current & ~COLOR_MASK) | color;
                            break;
                        case '@':
                            if(family == null){
                                font = this;
                                break;
                            }
                            fontIndex = family.fontAliases.get(text.substring(i + 1, i + len), 0);
                            current = (current & 0xFFFFFFFFFFF0FFFFL) | (fontIndex & 15L) << 16;
                            font = family.connected[fontIndex & 15];
                            if(font == null) font = this;
                            break;
                        case '|':
                            // attempt to look up a known Color name with a ColorLookup
                            int lookupColor = colorLookup.getRgba(text.substring(i + 1, i + len));
                            if (lookupColor == 256) color = baseColor;
                            else color = (long) lookupColor << 32;
                            current = (current & ~COLOR_MASK) | color;
                            break;
                        default:
                            // attempt to look up a known Color name with a ColorLookup
                            int gdxColor = colorLookup.getRgba(text.substring(i, i + len));
                            if (gdxColor == 256) color = baseColor;
                            else color = (long) gdxColor << 32;
                            current = (current & ~COLOR_MASK) | color;
                    }
                    i += len;
                }
                else {
                    float w;
                    if (font.kerning == null) {
                        w = (appendTo.peekLine().width += xAdvanceInternal(font, scaleX, current | '['));
                    } else {
                        kern = kern << 16 | '[';
                        w = (appendTo.peekLine().width += xAdvanceInternal(font, scaleX, current | '[') + font.kerning.get(kern, 0) * scaleX * (1f + 0.5f * (-(current & SUPERSCRIPT) >> 63)));
                    }
                    appendTo.add(current | 2);
                    if(targetWidth > 0 && w > targetWidth) {
                        Line earlier = appendTo.peekLine();
                        Line later = appendTo.pushLine();
                        if(later == null){
                            // here, the max lines have been reached, and an ellipsis may need to be added
                            // to the last line.
                            if(appendTo.ellipsis != null) {
                                int j = earlier.glyphs.size - 1 - appendTo.ellipsis.length();
                                if (j >= 0) {
                                    int leading = 0;
                                    long currE, curr;
                                    while ((curr = earlier.glyphs.get(j)) >>> 32 == 0L || Arrays.binarySearch(spaceChars.items, 0, spaceChars.size, (char) curr) >= 0) {
                                        ++leading;
                                        --j;
                                    }
                                    float change = 0f, changeNext = 0f;
                                    if (font.kerning == null) {
                                        for (int k = j + 1, e = 0; k < earlier.glyphs.size; k++, e++) {
                                            change += xAdvanceInternal(font, scaleX, earlier.glyphs.get(k));
                                            if (--leading < 0 && (e < appendTo.ellipsis.length())) {
                                                float adv = xAdvanceInternal(font, scaleX, baseColor | appendTo.ellipsis.charAt(e));
                                                changeNext += adv;
                                            }
                                        }
                                    } else {
                                        int k2 = ((int) earlier.glyphs.get(j) & 0xFFFF);
                                        int k2e = appendTo.ellipsis.charAt(0) & 0xFFFF;
                                        for (int k = j + 1, e = 0; k < earlier.glyphs.size; k++, e++) {
                                            currE = baseColor | appendTo.ellipsis.charAt(e);
                                            curr = earlier.glyphs.get(k);
                                            k2 = k2 << 16 | (char) curr;
                                            k2e = k2e << 16 | (char) currE;
                                            change += xAdvanceInternal(font, scaleX, curr) + font.kerning.get(k2, 0) * scaleX * (1f + 0.5f * (-(curr & SUPERSCRIPT) >> 63));
                                            if (--leading < 0 && (e < appendTo.ellipsis.length())) {
                                                changeNext += xAdvanceInternal(font, scaleX, currE) + font.kerning.get(k2e, 0) * scaleX * (1f + 0.5f * (-(currE & SUPERSCRIPT) >> 63));
                                            }
                                        }
                                    }
                                    earlier.glyphs.truncate(j + 1);
                                    for (int e = 0; e < appendTo.ellipsis.length(); e++) {
                                        earlier.glyphs.add(baseColor | appendTo.ellipsis.charAt(e));
                                    }
                                    earlier.width = earlier.width - change + changeNext;
                                    return appendTo;
                                }
                            }
                        }
                        else {
                            for (int j = earlier.glyphs.size - 2; j >= 0; j--) {

                                long curr;
                                if ((curr = earlier.glyphs.get(j)) >>> 32 == 0L ||
                                        Arrays.binarySearch(breakChars.items, 0, breakChars.size, (char) curr) >= 0) {
                                    int leading = 0;
                                    while ((curr = earlier.glyphs.get(j)) >>> 32 == 0L ||
                                            Arrays.binarySearch(spaceChars.items, 0, spaceChars.size, (char) curr) >= 0) {
                                        ++leading;
                                        --j;
                                    }
                                    float change = 0f, changeNext = 0f;
                                    if (font.kerning == null) {
                                        for (int k = j + 1; k < earlier.glyphs.size; k++) {
                                            float adv = xAdvanceInternal(font, scaleX, curr = earlier.glyphs.get(k));
                                            change += adv;
                                            if (--leading < 0) {
                                                appendTo.add(curr);
                                                changeNext += adv;
                                            }
                                        }
                                    } else {
                                        int k2 = ((int) earlier.glyphs.get(j) & 0xFFFF), k3 = -1;
                                        for (int k = j + 1; k < earlier.glyphs.size; k++) {
                                            curr = earlier.glyphs.get(k);
                                            k2 = k2 << 16 | (char) curr;
                                            float adv = xAdvanceInternal(font, scaleX, curr);
                                            change += adv + font.kerning.get(k2, 0) * scaleX * (1f + 0.5f * (-(curr & SUPERSCRIPT) >> 63));
                                            if (--leading < 0) {
                                                k3 = k3 << 16 | (char) curr;
                                                changeNext += adv + font.kerning.get(k3, 0) * scaleX * (1f + 0.5f * (-(curr & SUPERSCRIPT) >> 63));
                                                appendTo.add(curr);
                                            }
                                        }
                                    }
                                    earlier.glyphs.truncate(j + 2);
                                    earlier.glyphs.set(j+1, '\n');
                                    later.width = changeNext;
                                    earlier.width -= change;
                                    later.height = Math.max(later.height, font.cellHeight * (scale + 1) * 0.25f);
                                    break;
                                }
                            }
                        }
                    }
                    else {
                        appendTo.peekLine().height = Math.max(appendTo.peekLine().height, font.cellHeight * (scale + 1) * 0.25f);
                    }
                }
            } else {
                char ch = text.charAt(i);
                if (isLowerCase(ch)) {
                    if ((capitalize && !previousWasLetter) || capsLock) {
                        ch = Category.caseUp(ch);
                    }
                    previousWasLetter = true;
                } else if (isUpperCase(ch)) {
                    if ((capitalize && previousWasLetter) || lowerCase) {
                        ch = Category.caseDown(ch);
                    }
                    previousWasLetter = true;
                } else {
                    previousWasLetter = false;
                }
                float w;
                if (font.kerning == null) {
                    w = (appendTo.peekLine().width += xAdvanceInternal(font, scaleX, current | ch));
                } else {
                    kern = kern << 16 | ch;
                    w = (appendTo.peekLine().width += xAdvanceInternal(font, scaleX, current | ch) + font.kerning.get(kern, 0) * scaleX * (1f + 0.5f * (-((current | ch) & SUPERSCRIPT) >> 63)));
                }
                appendTo.add(current | ch);
                if((targetWidth > 0 && w > targetWidth) || appendTo.atLimit) {
                    Line earlier = appendTo.peekLine();
                    Line later;
                    if(appendTo.lines.size >= appendTo.maxLines) {
                        later = null;
                    }
                    else {
                        later = Line.POOL.obtain();
                        later.height = 0;
                        appendTo.lines.add(later);
                    }
                    if(later == null){
                        // here, the max lines have been reached, and an ellipsis may need to be added
                        // to the last line.
                        String ellipsis = (appendTo.ellipsis == null) ? "" : appendTo.ellipsis;
                        for (int j = earlier.glyphs.size - 1; j >= 0; j--) {
                            long curr;
                            // remove a full word or other group of non-space characters.
                            while ((curr = earlier.glyphs.get(j)) >>> 32 == 0L || Arrays.binarySearch(spaceChars.items, 0, spaceChars.size, (char) curr) < 0 && j > 0) {
                                --j;
                            }
                            // remove the remaining space characters.
                            while ((curr = earlier.glyphs.get(j)) >>> 32 == 0L ||
                                    Arrays.binarySearch(spaceChars.items, 0, spaceChars.size, (char) curr) >= 0 && j > 0) {
                                --j;
                            }
                            float change = 0f, changeNext = 0f;
                            long currE;
                            if (font.kerning == null) {
                                for (int k = j + 1, e = 0; k < earlier.glyphs.size; k++, e++) {
                                    change += xAdvanceInternal(font, scaleX, earlier.glyphs.get(k));
                                    if ((e < ellipsis.length())) {
                                        float adv = xAdvanceInternal(font, scaleX, baseColor | ellipsis.charAt(e));
                                        changeNext += adv;
                                    }
                                }
                            } else {
                                int k2 = ((int) earlier.glyphs.get(j) & 0xFFFF);
                                int k2e = ellipsis.charAt(0) & 0xFFFF;
                                for (int k = j + 1, e = 0; k < earlier.glyphs.size; k++, e++) {
                                    curr = earlier.glyphs.get(k);
                                    k2 = k2 << 16 | (char) curr;
                                    change += xAdvanceInternal(font, scaleX, curr) + font.kerning.get(k2, 0) * scaleX * (1f + 0.5f * (-(curr & SUPERSCRIPT) >> 63));
                                    if ((e < ellipsis.length())) {
                                        currE = baseColor | ellipsis.charAt(e);
                                        k2e = k2e << 16 | (char) currE;
                                        changeNext += xAdvanceInternal(font, scaleX, currE) + font.kerning.get(k2e, 0) * scaleX * (1f + 0.5f * (-(currE & SUPERSCRIPT) >> 63));
                                    }
                                }
                            }
                            if (earlier.width + changeNext < appendTo.getTargetWidth()) {
                                for (int e = 0; e < ellipsis.length(); e++) {
                                    earlier.glyphs.add(baseColor | ellipsis.charAt(e));
                                }
                                earlier.width = earlier.width + changeNext;
                                return appendTo;
                            }
                            if (earlier.width - change + changeNext < appendTo.getTargetWidth()) {
                                earlier.glyphs.truncate(j + 1);
                                for (int e = 0; e < ellipsis.length(); e++) {
                                    earlier.glyphs.add(baseColor | ellipsis.charAt(e));
                                }
                                earlier.width = earlier.width - change + changeNext;
                                return appendTo;
                            }
                        }
                    } else {
                        for (int j = earlier.glyphs.size - 2; j >= 0; j--) {
                            long curr;
                            if ((curr = earlier.glyphs.get(j)) >>> 32 == 0L ||
                                    Arrays.binarySearch(breakChars.items, 0, breakChars.size, (char) curr) >= 0) {
                                int leading = 0;
                                while ((curr = earlier.glyphs.get(j)) >>> 32 == 0L ||
                                        Arrays.binarySearch(spaceChars.items, 0, spaceChars.size, (char) curr) >= 0) {
                                    ++leading;
                                    --j;
                                }
                                glyphBuffer.clear();
                                float change = 0f, changeNext = 0f;
                                if (font.kerning == null) {
                                    boolean curly = false;
                                    for (int k = j + 1; k < earlier.glyphs.size; k++) {
                                        curr = earlier.glyphs.get(k);
                                        if(curly){
                                            glyphBuffer.add(curr);
                                            if((char)curr == '{'){
                                                curly = false;
                                            }
                                            else if((char)curr == '}'){
                                                curly = false;
                                                continue;
                                            }
                                            else continue;
                                        }
                                        if((char)curr == '{')
                                        {
                                            glyphBuffer.add(curr);
                                            curly = true;
                                            continue;
                                        }

                                        float adv = xAdvanceInternal(font, scaleX, curr);
                                        change += adv;
                                        if (--leading < 0) {
                                            glyphBuffer.add(curr);
                                            changeNext += adv;
                                        }
                                    }
                                } else {
                                    int k2 = ((int) earlier.glyphs.get(j) & 0xFFFF), k3 = -1;
                                    boolean curly = false;
                                    for (int k = j + 1; k < earlier.glyphs.size; k++) {
                                        curr = earlier.glyphs.get(k);
                                        if(curly){
                                            glyphBuffer.add(curr);
                                            if((char)curr == '{'){
                                                curly = false;
                                            }
                                            else if((char)curr == '}'){
                                                curly = false;
                                                continue;
                                            }
                                            else continue;
                                        }
                                        if((char)curr == '{')
                                        {
                                            glyphBuffer.add(curr);
                                            curly = true;
                                            continue;
                                        }
                                        k2 = k2 << 16 | (char) curr;
                                        float adv = xAdvanceInternal(font, scaleX, curr);
                                        change += adv + font.kerning.get(k2, 0) * scaleX * (1f + 0.5f * (-(curr & SUPERSCRIPT) >> 63));
                                        if (--leading < 0) {
                                            k3 = k3 << 16 | (char) curr;
                                            changeNext += adv + font.kerning.get(k3, 0) * scaleX * (1f + 0.5f * (-(curr & SUPERSCRIPT) >> 63));
                                            glyphBuffer.add(curr);
                                        }
                                    }
                                }
                                if(earlier.width - change > targetWidth)
                                    continue;
                                earlier.glyphs.truncate(j+1);
                                earlier.glyphs.add('\n');
                                later.width = changeNext;
                                earlier.width -= change;
                                later.glyphs.addAll(glyphBuffer);
                                later.height = Math.max(later.height, font.cellHeight * (scale + 1) * 0.25f);
                                break;
                            }
                        }
                    }
                }
                else {
                    appendTo.peekLine().height = Math.max(appendTo.peekLine().height, font.cellHeight * (scale + 1) * 0.25f);
                }
            }
        }

//        for (int i = 0; i < appendTo.lines(); i++) {
//            calculateSize(appendTo.getLine(i));
//        }

        return appendTo;
    }

    /**
     * Reads markup from {@code markup}, processes it, and applies it to the given char {@code chr}; returns a long
     * in the format used for styled glyphs here. This parses an extension of libGDX markup and uses it to determine
     * color, size, position, shape, strikethrough, underline, case, and scale of the given char.
     * The char drawn will start in white, with the normal size as determined by the font's metrics and scale
     * ({@link #scaleX} and {@link #scaleY}), normal case, and without bold, italic, superscript, subscript,
     * strikethrough, or underline. Markup starts with {@code [}; the next character determines what that piece of
     * markup toggles. Markup this knows:
     * <ul>
     *     <li>{@code []} clears all markup to the initial state without any applied.</li>
     *     <li>{@code [*]} toggles bold mode.</li>
     *     <li>{@code [/]} toggles italic (technically, oblique) mode.</li>
     *     <li>{@code [^]} toggles superscript mode (and turns off subscript or midscript mode).</li>
     *     <li>{@code [=]} toggles midscript mode (and turns off superscript or subscript mode).</li>
     *     <li>{@code [.]} toggles subscript mode (and turns off superscript or midscript mode).</li>
     *     <li>{@code [_]} toggles underline mode.</li>
     *     <li>{@code [~]} toggles strikethrough mode.</li>
     *     <li>{@code [!]} toggles all upper case mode.</li>
     *     <li>{@code [,]} toggles all lower case mode.</li>
     *     <li>{@code [;]} toggles capitalize each word mode (this is the same as upper case mode here).</li>
     *     <li>{@code [%P]}, where P is a percentage from 0 to 375, changes the scale to that percentage (rounded to
     *     the nearest 25% mark).</li>
     *     <li>{@code [%]}, with no number just after it, resets scale to 100% (this usually has no effect here).</li>
     *     <li>{@code [#HHHHHHHH]}, where HHHHHHHH is a hex RGB888 or RGBA8888 int color, changes the color.</li>
     *     <li>{@code [COLORNAME]}, where "COLORNAME" is a typically-upper-case color name that will be looked up in
     *     {@link #getColorLookup()}, changes the color. The name can optionally be preceded by {@code |}, which allows
     *     looking up colors with names that contain punctuation.</li>
     * </ul>
     * You can render the result using {@link #drawGlyph(Batch, long, float, float)}. It is recommended that you avoid
     * calling this method every frame, because the color lookups usually allocate some memory, and because this can
     * usually be stored for later without needing repeated computation.
     * <br>
     * This is equivalent to calling the static {@link #markupGlyph(char, String, ColorLookup)} and giving it this
     * Font's {@link #colorLookup} value.
     * @param chr a single char to apply markup to
     * @param markup a String containing only markup syntax, like "[*][_][RED]" for bold underline in red
     * @return a long that encodes the given char with the specified markup
     */
    public long markupGlyph(char chr, String markup) {
        return markupGlyph(chr, markup, colorLookup, family);
    }
    /**
     * Reads markup from {@code markup}, processes it, and applies it to the given char {@code chr}; returns a long
     * in the format used for styled glyphs here. This parses an extension of libGDX markup and uses it to determine
     * color, size, position, shape, strikethrough, underline, case, and scale of the given char.
     * The char drawn will start in white, with the normal size as determined by the font's metrics and scale
     * ({@link #scaleX} and {@link #scaleY}), normal case, and without bold, italic, superscript, subscript,
     * strikethrough, or underline. Markup starts with {@code [}; the next character determines what that piece of
     * markup toggles. Markup this knows:
     * <ul>
     *     <li>{@code []} clears all markup to the initial state without any applied.</li>
     *     <li>{@code [*]} toggles bold mode.</li>
     *     <li>{@code [/]} toggles italic (technically, oblique) mode.</li>
     *     <li>{@code [^]} toggles superscript mode (and turns off subscript or midscript mode).</li>
     *     <li>{@code [=]} toggles midscript mode (and turns off superscript or subscript mode).</li>
     *     <li>{@code [.]} toggles subscript mode (and turns off superscript or midscript mode).</li>
     *     <li>{@code [_]} toggles underline mode.</li>
     *     <li>{@code [~]} toggles strikethrough mode.</li>
     *     <li>{@code [!]} toggles all upper case mode.</li>
     *     <li>{@code [,]} toggles all lower case mode.</li>
     *     <li>{@code [;]} toggles capitalize each word mode (this is the same as upper case mode here).</li>
     *     <li>{@code [%P]}, where P is a percentage from 0 to 375, changes the scale to that percentage (rounded to
     *     the nearest 25% mark).</li>
     *     <li>{@code [%]}, with no number just after it, resets scale to 100% (this usually has no effect here).</li>
     *     <li>{@code [#HHHHHHHH]}, where HHHHHHHH is a hex RGB888 or RGBA8888 int color, changes the color.</li>
     *     <li>{@code [COLORNAME]}, where "COLORNAME" is a typically-upper-case color name that will be looked up in
     *     {@link #getColorLookup()}, changes the color. The name can optionally be preceded by {@code |}, which allows
     *     looking up colors with names that contain punctuation.</li>
     * </ul>
     * You can render the result using {@link #drawGlyph(Batch, long, float, float)}. It is recommended that you avoid
     * calling this method every frame, because the color lookups usually allocate some memory, and because this can
     * usually be stored for later without needing repeated computation.
     * <br>
     * This takes a ColorLookup so that it can look up colors given a name or description; if you don't know what to
     * use, then {@link ColorLookup.GdxColorLookup#INSTANCE} is often perfectly fine. Because this is static, it does
     * not need a Font to be involved.
     * @param chr a single char to apply markup to
     * @param markup a String containing only markup syntax, like "[*][_][RED]" for bold underline in red
     * @param colorLookup a ColorLookup (often a method reference or {@link ColorLookup.GdxColorLookup#INSTANCE}) to get
     *                   colors from textual names or descriptions
     * @return a long that encodes the given char with the specified markup
     */
    public static long markupGlyph(char chr, String markup, ColorLookup colorLookup) {
        return markupGlyph(chr, markup, colorLookup, null);
    }
    /**
     * Reads markup from {@code markup}, processes it, and applies it to the given char {@code chr}; returns a long
     * in the format used for styled glyphs here. This parses an extension of libGDX markup and uses it to determine
     * color, size, position, shape, strikethrough, underline, case, and scale of the given char.
     * The char drawn will start in white, with the normal size as determined by the font's metrics and scale
     * ({@link #scaleX} and {@link #scaleY}), normal case, and without bold, italic, superscript, subscript,
     * strikethrough, or underline. Markup starts with {@code [}; the next character determines what that piece of
     * markup toggles. Markup this knows:
     * <ul>
     *     <li>{@code []} clears all markup to the initial state without any applied.</li>
     *     <li>{@code [*]} toggles bold mode.</li>
     *     <li>{@code [/]} toggles italic (technically, oblique) mode.</li>
     *     <li>{@code [^]} toggles superscript mode (and turns off subscript or midscript mode).</li>
     *     <li>{@code [=]} toggles midscript mode (and turns off superscript or subscript mode).</li>
     *     <li>{@code [.]} toggles subscript mode (and turns off superscript or midscript mode).</li>
     *     <li>{@code [_]} toggles underline mode.</li>
     *     <li>{@code [~]} toggles strikethrough mode.</li>
     *     <li>{@code [!]} toggles all upper case mode.</li>
     *     <li>{@code [,]} toggles all lower case mode.</li>
     *     <li>{@code [;]} toggles capitalize each word mode (this is the same as upper case mode here).</li>
     *     <li>{@code [%P]}, where P is a percentage from 0 to 375, changes the scale to that percentage (rounded to
     *     the nearest 25% mark).</li>
     *     <li>{@code [%]}, with no number just after it, resets scale to 100% (this usually has no effect here).</li>
     *     <li>{@code [#HHHHHHHH]}, where HHHHHHHH is a hex RGB888 or RGBA8888 int color, changes the color.</li>
     *     <li>{@code [COLORNAME]}, where "COLORNAME" is a typically-upper-case color name that will be looked up in
     *     {@link #getColorLookup()}, changes the color. The name can optionally be preceded by {@code |}, which allows
     *     looking up colors with names that contain punctuation.</li>
     * </ul>
     * You can render the result using {@link #drawGlyph(Batch, long, float, float)}. It is recommended that you avoid
     * calling this method every frame, because the color lookups usually allocate some memory, and because this can
     * usually be stored for later without needing repeated computation.
     * <br>
     * This takes a ColorLookup so that it can look up colors given a name or description; if you don't know what to
     * use, then {@link ColorLookup.GdxColorLookup#INSTANCE} is often perfectly fine. Because this is static, it does
     * not need a Font to be involved.
     * @param chr a single char to apply markup to
     * @param markup a String containing only markup syntax, like "[*][_][RED]" for bold underline in red
     * @param colorLookup a ColorLookup (often a method reference or {@link ColorLookup.GdxColorLookup#INSTANCE}) to get
     *                   colors from textual names or descriptions
     * @return a long that encodes the given char with the specified markup
     */
    public static long markupGlyph(char chr, String markup, ColorLookup colorLookup, FontFamily family) {
        boolean capsLock = false, lowerCase = false;
        int c;
        final long COLOR_MASK = 0xFFFFFFFF00000000L;
        long baseColor = COLOR_MASK | chr;
        long color = baseColor;
        long current = color;
        for (int i = 0, n = markup.length(); i < n; i++) {
            if (markup.charAt(i) == '[') {
                if (++i < n && (c = markup.charAt(i)) != '[') {
                    if (c == ']') {
                        color = baseColor;
                        current = color;
                        capsLock = false;
                        lowerCase = false;
                        continue;
                    }
                    int len = markup.indexOf(']', i) - i;
                    switch (c) {
                        case '*':
                            current ^= BOLD;
                            break;
                        case '/':
                            current ^= OBLIQUE;
                            break;
                        case '^':
                            if ((current & SUPERSCRIPT) == SUPERSCRIPT)
                                current &= ~SUPERSCRIPT;
                            else
                                current |= SUPERSCRIPT;
                            break;
                        case '.':
                            if ((current & SUPERSCRIPT) == SUBSCRIPT)
                                current &= ~SUBSCRIPT;
                            else
                                current = (current & ~SUPERSCRIPT) | SUBSCRIPT;
                            break;
                        case '=':
                            if ((current & SUPERSCRIPT) == MIDSCRIPT)
                                current &= ~MIDSCRIPT;
                            else
                                current = (current & ~SUPERSCRIPT) | MIDSCRIPT;
                            break;
                        case '_':
                            current ^= UNDERLINE;
                            break;
                        case '~':
                            current ^= STRIKETHROUGH;
                            break;
                        case ';':
                        case '!':
                            capsLock = !capsLock;
                            lowerCase = false;
                            break;
                        case ',':
                            lowerCase = !lowerCase;
                            capsLock = false;
                            break;
                        case '%':
                            if(len >= 2)
                                current = (current & 0xFFFFFFFFFF0FFFFFL) | ((((intFromDec(markup, i + 1, i + len) - 24) / 25) & 15) - 3 & 15) << 20;
                            else
                                current = (current & 0xFFFFFFFFFF0FFFFFL);
                            break;
                        case '@':
                            if(family == null){
                                break;
                            }
                            int fontIndex = family.fontAliases.get(markup.substring(i + 1, i + len), 0);
                            current = (current & 0xFFFFFFFFFFF0FFFFL) | (fontIndex & 15L) << 16;
                            break;
                        case '#':
                            if (len >= 7 && len < 9)
                                color = longFromHex(markup, i + 1, i + 7) << 40 | 0x000000FF00000000L;
                            else if (len >= 9)
                                color = longFromHex(markup, i + 1, i + 9) << 32;
                            else
                                color = baseColor;
                            current = (current & ~COLOR_MASK) | color;
                            break;
                        case '|':
                            // attempt to look up a known Color name with a ColorLookup
                            int lookupColor = colorLookup.getRgba(markup.substring(i + 1, i + len));
                            if (lookupColor == 256) color = baseColor;
                            else color = (long) lookupColor << 32;
                            current = (current & ~COLOR_MASK) | color;
                            break;
                        default:
                            // attempt to look up a known Color name with a ColorLookup
                            int gdxColor = colorLookup.getRgba(markup.substring(i, i + len));
                            if (gdxColor == 256) color = baseColor;
                            else color = (long) gdxColor << 32;
                            current = (current & ~COLOR_MASK) | color;
                    }
                }
            }
        }
        return current;
    }

    public Layout regenerateLayout(Layout changing) {
        if(changing.font == null || !changing.font.equals(this)) {
            return changing;
        }
        Font font = null;
        float scaleX;
        float targetWidth = changing.getTargetWidth();
        int oldLength = changing.lines.size;
        Line firstLine = changing.getLine(0);
        for (int i = 1; i < oldLength; i++) {
            firstLine.glyphs.addAll(changing.getLine(i).glyphs);
            Line.POOL.free(changing.getLine(i));
        }
        changing.lines.truncate(1);
        for (int ln = 0; ln < changing.lines(); ln++) {
            Line line = changing.getLine(ln);
            line.height = 0;
            float drawn = 0f;
            int cutoff, breakPoint = -2, spacingPoint = -2, spacingSpan = 0;
            int scale;
            LongArray glyphs = line.glyphs;
            boolean hasMultipleGaps = false;
            if (kerning != null) {
                int kern = -1;
                float amt;
                long glyph;
                for (int i = 0, n = glyphs.size; i < n; i++) {
                    glyph = glyphs.get(i);
                    if(family != null) font = family.connected[(int)(glyph >>> 16 & 15)];
                    if(font == null) font = this;
                    if((glyph & 0xFFFFL) == '\n') {
                        glyphs.set(i, glyph ^= 42L);
                    }
                    scale = (int) (glyph + 0x300000L >>> 20 & 15);
                    line.height = Math.max(line.height, font.cellHeight * (scale + 1) * 0.25f);
                    scaleX = font.scaleX * (scale + 1) * 0.25f;
                    kern = kern << 16 | (int) (glyph & 0xFFFF);
                    amt = font.kerning.get(kern, 0) * scaleX;
                    GlyphRegion tr = font.mapping.get((char)glyph);
                    if(tr == null) continue;
                    float changedW = tr.xAdvance * scaleX;
                    if (font.isMono) {
                        changedW += tr.offsetX * scaleX;
                    }
                    if(!font.isMono && (glyph & SUPERSCRIPT) != 0L)
                        changedW *= 0.5f;
                    if (glyph >>> 32 == 0L){
                        hasMultipleGaps = breakPoint >= 0;
                        breakPoint = i;
                        if(spacingPoint + 1 < i){
                            spacingSpan = 0;
                        }
                        else spacingSpan++;
                        spacingPoint = i;
                    }
                    if(Arrays.binarySearch(breakChars.items, 0, breakChars.size, (char) glyph) >= 0){
                        hasMultipleGaps = breakPoint >= 0;
                        breakPoint = i;
                        if(Arrays.binarySearch(spaceChars.items, 0, spaceChars.size, (char) glyph) >= 0){
                            if(spacingPoint + 1 < i){
                                spacingSpan = 0;
                            }
                            else spacingSpan++;
                            spacingPoint = i;
                        }
                    }
                    if(hasMultipleGaps && drawn + changedW + amt > targetWidth) {
                        cutoff = breakPoint - spacingSpan;
                        Line next;
                        if(changing.lines() == ln + 1)
                        {
                            next = changing.pushLine();
                            glyphs.pop();
                        }
                        else
                            next = changing.getLine(ln+1);
                        if(next == null) {
                            glyphs.truncate(cutoff);
                            break;
                        }
                        next.height = Math.max(next.height, font.cellHeight * (scale + 1) * 0.25f);

                        int nextSize = next.glyphs.size;
                        long[] arr = next.glyphs.setSize(nextSize + glyphs.size - cutoff);
                        System.arraycopy(arr, 0, arr, glyphs.size - cutoff, nextSize);
                        System.arraycopy(glyphs.items, cutoff, arr, 0, glyphs.size - cutoff);
//                        next.glyphs.size += glyphs.size - cutoff;
                        glyphs.truncate(cutoff);
                        break;
                    }
                    drawn += changedW + amt;
                }
            } else {
                for (int i = 0, n = glyphs.size; i < n; i++) {
                    long glyph = glyphs.get(i);
                    if(family != null) font = family.connected[(int)(glyph >>> 16 & 15)];
                    if(font == null) font = this;
                    if((glyph & 0xFFFFL) == '\n') {
                        glyphs.set(i, glyph ^= 42L);
                    }
                    scale = (int) (glyph + 0x300000L >>> 20 & 15);
                    line.height = Math.max(line.height, font.cellHeight * (scale + 1) * 0.25f);
                    scaleX = font.scaleX * (scale + 1) * 0.25f;
                    GlyphRegion tr = font.mapping.get((char)glyph);
                    if(tr == null) continue;
                    float changedW = tr.xAdvance * scaleX;
                    if (font.isMono) {
                        changedW += tr.offsetX * scaleX;
                    }
                    if(!font.isMono && (glyph & SUPERSCRIPT) != 0L)
                        changedW *= 0.5f;
                    if (glyph >>> 32 == 0L){
                        hasMultipleGaps = breakPoint >= 0;
                        breakPoint = i;
                        if(spacingPoint + 1 < i){
                            spacingSpan = 0;
                        }
                        else spacingSpan++;
                        spacingPoint = i;
                    }
                    else if(Arrays.binarySearch(breakChars.items, 0, breakChars.size, (char) glyph) >= 0){
                        hasMultipleGaps = breakPoint >= 0;
                        breakPoint = i;
                        if(Arrays.binarySearch(spaceChars.items, 0, spaceChars.size, (char) glyph) >= 0){
                            if(spacingPoint + 1 < i){
                                spacingSpan = 0;
                            }
                            else spacingSpan++;
                            spacingPoint = i;
                        }
                    }
                    if(hasMultipleGaps && drawn + changedW > targetWidth) {
                        cutoff = breakPoint - spacingSpan;
                        Line next;
                        if(changing.lines() == ln + 1) {
                            next = changing.pushLine();
                            glyphs.pop();
                        }
                        else
                            next = changing.getLine(ln+1);
                        if(next == null) {
                            glyphs.truncate(cutoff);
                            break;
                        }
                        next.height = Math.max(next.height, font.cellHeight * (scale + 1) * 0.25f);

                        int nextSize = next.glyphs.size;
                        long[] arr = next.glyphs.setSize(nextSize + glyphs.size - cutoff);
                        System.arraycopy(arr, 0, arr, glyphs.size - cutoff, nextSize);
                        System.arraycopy(glyphs.items, cutoff, arr, 0, glyphs.size - cutoff);
//                        next.glyphs.size += glyphs.size - cutoff;
                        glyphs.truncate(cutoff);
                        break;
                    }
                    drawn += changedW;
                }
            }
            line.width = drawn;
        }
        return changing;
    }

    /**
     * Sets the FontFamily this can use to switch fonts using [@Name] syntax. If family is null, only the current Font
     * will be used.
     * @param family a {@link FontFamily} that may be null or shared with other Fonts.
     * @return this, for chaining
     */
    public Font setFamily(FontFamily family){
        this.family = family;
        return this;
    }

    /**
     * Given the new width and height for a window, this attempts to adjust the {@link #actualCrispness} of an
     * SDF or MSDF font so that it will display cleanly at a different size. This uses this font's
     * {@link #distanceFieldCrispness} as a multiplier applied after calculating the initial crispness.
     * This is a suggestion for what to call in your {@link com.badlogic.gdx.ApplicationListener#resize(int, int)}
     * method for each SDF or MSDF font you have currently rendering.
     * @param width the new window width; usually a parameter in {@link com.badlogic.gdx.ApplicationListener#resize(int, int)}
     * @param height the new window height; usually a parameter in {@link com.badlogic.gdx.ApplicationListener#resize(int, int)}
     */
    public void resizeDistanceField(int width, int height) {
        if(distanceField == DistanceFieldType.SDF) {
            if (Gdx.graphics.getBackBufferWidth() == 0 || Gdx.graphics.getBackBufferHeight() == 0) {
                actualCrispness = distanceFieldCrispness;
            } else {
                actualCrispness = distanceFieldCrispness * (float) Math.pow(4f,
                        Math.max((float) width / Gdx.graphics.getBackBufferWidth(),
                                (float) height / Gdx.graphics.getBackBufferHeight()) * 1.9f - 2f + cellHeight * 0.005f);
            }
        }
        else if(distanceField == DistanceFieldType.MSDF){
            if (Gdx.graphics.getBackBufferWidth() == 0 || Gdx.graphics.getBackBufferHeight() == 0) {
                actualCrispness = distanceFieldCrispness;
            } else {
                actualCrispness = distanceFieldCrispness * (float) Math.pow(8f,
                        Math.max((float) width / Gdx.graphics.getBackBufferWidth(),
                                (float) height / Gdx.graphics.getBackBufferHeight()) * 1.9f - 2.15f + cellHeight * 0.01f);
            }
        }
    }

    /**
     * Given a glyph as a long, this returns the RGBA8888 color it uses.
     * @param glyph a glyph as a long, as used by {@link Layout} and {@link Line}
     * @return the int color used by the given glyph, as RGBA8888
     */
    public static int extractColor(long glyph){
        return (int) (glyph>>>32);
    }

    /**
     * Replaces the section of glyph that stores its color with the given RGBA8888 int color.
     * @param glyph a glyph as a long, as used by {@link Layout} and {@link Line}
     * @param color the int color to use, as an RGBA8888 int
     * @return another long glyph that uses the specified color
     */
    public static long applyColor(long glyph, int color){
        return (glyph & 0xFFFFFFFFL) | ((long) color << 32);
    }

    /**
     * Given a glyph as a long, this returns the style bits it uses. You can cross-reference these with
     * {@link #BOLD}, {@link #OBLIQUE}, {@link #UNDERLINE}, {@link #STRIKETHROUGH}, {@link #SUBSCRIPT},
     * {@link #MIDSCRIPT}, and {@link #SUPERSCRIPT}.
     * @param glyph a glyph as a long, as used by {@link Layout} and {@link Line}
     * @return the style bits used by the given glyph
     */
    public static long extractStyle(long glyph){
        return glyph & 0x7E000000L;
    }

    /**
     * Replaces the section of glyph that stores its style with the given long bits.You can get the bit constants with
     * {@link #BOLD}, {@link #OBLIQUE}, {@link #UNDERLINE}, {@link #STRIKETHROUGH}, {@link #SUBSCRIPT},
     * {@link #MIDSCRIPT}, and {@link #SUPERSCRIPT}. Because only a small section is used from style, you can pass an
     * existing styled glyph as the second parameter to copy its style information into glyph.
     * @param glyph a glyph as a long, as used by {@link Layout} and {@link Line}
     * @param style the long style bits to use, which should usually be bits from the aforementioned constants
     * @return another long glyph that uses the specified style
     */
    public static long applyStyle(long glyph, long style){
        return (glyph & 0xFFFFFFFF81FFFFFFL) | (style & 0x7E000000L);
    }

    /**
     * Given a glyph as a long, this returns the float multiplier it uses for scale.
     * @param glyph a glyph as a long, as used by {@link Layout} and {@link Line}
     * @return the float scale used by the given glyph, from 0.0f to 3.75f
     */
    public static float extractScale(long glyph){
        return (glyph + 0x400000L >>> 20 & 15) * 0.25f;
    }

    /**
     * Replaces the section of glyph that stores its scale with the given float multiplier, rounded to a multiple of
     * 0.25 and wrapped to within 0.0 to 3.75, both inclusive.
     * @param glyph a glyph as a long, as used by {@link Layout} and {@link Line}
     * @param scale the float scale to use, which should be between 0.0 and 3.75, both inclusive
     * @return another long glyph that uses the specified scale
     */
    public static long applyScale(long glyph, float scale){
        return (glyph & 0xFFFFFFFFFF0FFFFFL) | ((long) Math.floor(scale * 4.0 - 4.0) & 15L) << 20;
    }

    /**
     * Given a glyph as a long, this returns the char it displays. This automatically corrects the placeholder char
     * u0002 to the glyph it displays as, {@code '['}.
     * @param glyph a glyph as a long, as used by {@link Layout} and {@link Line}
     * @return the char used by the given glyph
     */
    public static char extractChar(long glyph){
        final char c = (char) glyph;
        return c == 2 ? '[' : c;
    }

    /**
     * Replaces the section of glyph that stores its scale with the given float multiplier, rounded to a multiple of
     * 0.25 and wrapped to within 0.0 to 3.75, both inclusive.
     * @param glyph a glyph as a long, as used by {@link Layout} and {@link Line}
     * @param c the char to use
     * @return another long glyph that uses the specified char
     */
    public static long applyChar(long glyph, char c){
        return (glyph & 0xFFFFFFFFFFFF0000L) | c;
    }

    /**
     * Releases all resources of this object.
     */
    @Override
    public void dispose() {
        Layout.POOL.free(tempLayout);
        if(shader != null)
            shader.dispose();
    }
}
