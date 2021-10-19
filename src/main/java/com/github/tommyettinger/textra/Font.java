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

import java.util.Arrays;
import java.util.BitSet;

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

    public IntMap<GlyphRegion> mapping;
    public GlyphRegion defaultValue;
    public Array<TextureRegion> parents;
    public DistanceFieldType distanceField = DistanceFieldType.STANDARD;
    public boolean isMono;
    public IntIntMap kerning;
    /**
     * When {@link #distanceField} is {@link DistanceFieldType#SDF} or {@link DistanceFieldType#MSDF}, this determines
     * how much the edges of the glyphs should be aliased sharply (higher values) or anti-aliased softly (lower values).
     * The default value is 1.
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

    public static final long BOLD = 1L << 30, OBLIQUE = 1L << 29,
            UNDERLINE = 1L << 28, STRIKETHROUGH = 1L << 27,
            SUBSCRIPT = 1L << 25, MIDSCRIPT = 2L << 25, SUPERSCRIPT = 3L << 25;

    private final float[] vertices = new float[20];
    private final Layout tempLayout = Pools.obtain(Layout.class);
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
     * font was initially created, its current scale, and its {@link #distanceFieldCrispness} field. You can
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
     * represent negative numbers as they are printed by such methods as String.format given a %x in the formatting
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
     * early, this behaves as you would expect for a number with less digits, and simply doesn't fill the larger places.
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
     * with less digits, and simply doesn't fill the larger places.
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

    //// GWT case checks, hooray for Unicode...
    private static final BitSet upperBits = BitSet.valueOf(new long[]{0L, 134217726L, 9007199254740992L, 2139095039L, -6173215364225870507L, -6100876295211232086L, 1274187559846268630L, 6185224963053235632L, 7783721355972007253L, 21882L, 0L, 0L, 0L, -9203950263461740512L, 17575006099264L, -1823019599161753596L, 281474976710655L, 6148914689804861440L, 6148914691236516865L, 6148914691236506283L, -469124961184427L, 8388607L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, -4294967296L, 8383L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 4539628424389459968L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, -1729382256910335489L, 0L, 0L, 0L, 0L, 0L, 6148914691236517205L, 6148914691236517205L, 6148914691014219093L, 6148914691236517205L, -71777217515815168L, 280378317225728L, 6845751813346492160L, 2233819500287893248L, 0L, 0L, 0L, 0L, 1139368924282880L, 281470681743360L, 8L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, -18014398509481984L, 65535L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 140737488355327L, -4601013484258328576L, 6148914691236517205L, 1169903278445909L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 23456248059221L, 89478485L, 0L, 6148633210533183488L, 7638198793012598101L, 6151773421467674709L, 9007199254741748L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, -281474976710656L, -1L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 288230376285929546L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 1099511627775L, 0L, -1L, 1048575L, 17179869183L, 0L, 0L, 0L, 0L, 4294967295L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 576460743713488896L, });
    private static final BitSet lowerBits = BitSet.valueOf(new long[]{0L, 576460743713488896L, 0L, -36028799166447616L, 6100313345257810602L, 6100876295211231573L, -6750577901002550999L, -6185506438029946304L, -8067448141086283094L, 2316714340689881733L, 1610882957L, 0L, 0L, 4074068812910034944L, -299067162755072L, 652083696043589627L, -281474976710656L, -6148914689804861441L, -6148914691236517886L, -6148914691236506284L, 187649984473770L, -8589934592L, 127L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, -1729382256910336000L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, -4294967296L, 18014398509481983L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 2449958197289549824L, 16384L, 0L, -6148914691236517206L, -6148914691236517206L, -6148914694097032534L, -6148914691236517206L, 71777214282006783L, 4611405638678478911L, 3097319977189631L, 2251950137737224L, 0L, 0L, 0L, 0L, 0L, -281474976694272L, 16L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 4398046445568L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, -281474976710656L, 20289711121629183L, -6148914691236517206L, 2339806556891818L, 36009005809663L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 46912496118442L, 178956970L, 0L, -6149477652643184640L, -7782032506111743318L, -6151914158958178134L, 18014398509483272L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 524288L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 288230376285929546L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, -1099511627776L, 65535L, 0L, 1152921504606846975L, -17179869184L, 15L, 0L, 0L, 0L, -4294967296L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 134217726L, });

    //// used to generate above BitSets
//    private static final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZµÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞĀĂĄĆĈĊČĎĐĒĔĖĘĚĜĞĠĢĤĦĨĪĬĮĲĴĶĹĻĽĿŁŃŅŇŊŌŎŐŒŔŖŘŚŜŞŠŢŤŦŨŪŬŮŰŲŴŶŸŹŻŽſƁƂƄƆƇƉƊƋƎƏƐƑƓƔƖƗƘƜƝƟƠƢƤƦƧƩƬƮƯƱƲƳƵƷƸƼǄǅǇǈǊǋǍǏǑǓǕǗǙǛǞǠǢǤǦǨǪǬǮǱǲǴǶǷǸǺǼǾȀȂȄȆȈȊȌȎȐȒȔȖȘȚȜȞȠȢȤȦȨȪȬȮȰȲȺȻȽȾɁɃɄɅɆɈɊɌɎͅͰͲͶͿΆΈΉΊΌΎΏΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩΪΫςϏϐϑϕϖϘϚϜϞϠϢϤϦϨϪϬϮϰϱϴϵϷϹϺϽϾϿЀЁЂЃЄЅІЇЈЉЊЋЌЍЎЏАБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯѠѢѤѦѨѪѬѮѰѲѴѶѸѺѼѾҀҊҌҎҐҒҔҖҘҚҜҞҠҢҤҦҨҪҬҮҰҲҴҶҸҺҼҾӀӁӃӅӇӉӋӍӐӒӔӖӘӚӜӞӠӢӤӦӨӪӬӮӰӲӴӶӸӺӼӾԀԂԄԆԈԊԌԎԐԒԔԖԘԚԜԞԠԢԤԦԨԪԬԮԱԲԳԴԵԶԷԸԹԺԻԼԽԾԿՀՁՂՃՄՅՆՇՈՉՊՋՌՍՎՏՐՑՒՓՔՕՖႠႡႢႣႤႥႦႧႨႩႪႫႬႭႮႯႰႱႲႳႴႵႶႷႸႹႺႻႼႽႾႿჀჁჂჃჄჅჇჍᏸᏹᏺᏻᏼᏽᲀᲁᲂᲃᲄᲅᲆᲇᲈᲐᲑᲒᲓᲔᲕᲖᲗᲘᲙᲚᲛᲜᲝᲞᲟᲠᲡᲢᲣᲤᲥᲦᲧᲨᲩᲪᲫᲬᲭᲮᲯᲰᲱᲲᲳᲴᲵᲶᲷᲸᲹᲺᲽᲾᲿḀḂḄḆḈḊḌḎḐḒḔḖḘḚḜḞḠḢḤḦḨḪḬḮḰḲḴḶḸḺḼḾṀṂṄṆṈṊṌṎṐṒṔṖṘṚṜṞṠṢṤṦṨṪṬṮṰṲṴṶṸṺṼṾẀẂẄẆẈẊẌẎẐẒẔẛẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼẾỀỂỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪỬỮỰỲỴỶỸỺỼỾἈἉἊἋἌἍἎἏἘἙἚἛἜἝἨἩἪἫἬἭἮἯἸἹἺἻἼἽἾἿὈὉὊὋὌὍὙὛὝὟὨὩὪὫὬὭὮὯᾸᾹᾺΆιῈΈῊΉῘῙῚΊῨῩῪΎῬῸΌῺΏΩKÅℲⅠⅡⅢⅣⅤⅥⅦⅧⅨⅩⅪⅫⅬⅭⅮⅯↃⒶⒷⒸⒹⒺⒻⒼⒽⒾⒿⓀⓁⓂⓃⓄⓅⓆⓇⓈⓉⓊⓋⓌⓍⓎⓏⰀⰁⰂⰃⰄⰅⰆⰇⰈⰉⰊⰋⰌⰍⰎⰏⰐⰑⰒⰓⰔⰕⰖⰗⰘⰙⰚⰛⰜⰝⰞⰟⰠⰡⰢⰣⰤⰥⰦⰧⰨⰩⰪⰫⰬⰭⰮⱠⱢⱣⱤⱧⱩⱫⱭⱮⱯⱰⱲⱵⱾⱿⲀⲂⲄⲆⲈⲊⲌⲎⲐⲒⲔⲖⲘⲚⲜⲞⲠⲢⲤⲦⲨⲪⲬⲮⲰⲲⲴⲶⲸⲺⲼⲾⳀⳂⳄⳆⳈⳊⳌⳎⳐⳒⳔⳖⳘⳚⳜⳞⳠⳢⳫⳭⳲꙀꙂꙄꙆꙈꙊꙌꙎꙐꙒꙔꙖꙘꙚꙜꙞꙠꙢꙤꙦꙨꙪꙬꚀꚂꚄꚆꚈꚊꚌꚎꚐꚒꚔꚖꚘꚚꜢꜤꜦꜨꜪꜬꜮꜲꜴꜶꜸꜺꜼꜾꝀꝂꝄꝆꝈꝊꝌꝎꝐꝒꝔꝖꝘꝚꝜꝞꝠꝢꝤꝦꝨꝪꝬꝮꝹꝻꝽꝾꞀꞂꞄꞆꞋꞍꞐꞒꞖꞘꞚꞜꞞꞠꞢꞤꞦꞨꞪꞫꞬꞭꞮꞰꞱꞲꞳꞴꞶꞸꞺꞼꞾꟂꟄꟅꟆꟇꟉꟵꭰꭱꭲꭳꭴꭵꭶꭷꭸꭹꭺꭻꭼꭽꭾꭿꮀꮁꮂꮃꮄꮅꮆꮇꮈꮉꮊꮋꮌꮍꮎꮏꮐꮑꮒꮓꮔꮕꮖꮗꮘꮙꮚꮛꮜꮝꮞꮟꮠꮡꮢꮣꮤꮥꮦꮧꮨꮩꮪꮫꮬꮭꮮꮯꮰꮱꮲꮳꮴꮵꮶꮷꮸꮹꮺꮻꮼꮽꮾꮿＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ𐐀𐐁𐐂𐐃𐐄𐐅𐐆𐐇𐐈𐐉𐐊𐐋𐐌𐐍𐐎𐐏𐐐𐐑𐐒𐐓𐐔𐐕𐐖𐐗𐐘𐐙𐐚𐐛𐐜𐐝𐐞𐐟𐐠𐐡𐐢𐐣𐐤𐐥𐐦𐐧𐒰𐒱𐒲𐒳𐒴𐒵𐒶𐒷𐒸𐒹𐒺𐒻𐒼𐒽𐒾𐒿𐓀𐓁𐓂𐓃𐓄𐓅𐓆𐓇𐓈𐓉𐓊𐓋𐓌𐓍𐓎𐓏𐓐𐓑𐓒𐓓𐲀𐲁𐲂𐲃𐲄𐲅𐲆𐲇𐲈𐲉𐲊𐲋𐲌𐲍𐲎𐲏𐲐𐲑𐲒𐲓𐲔𐲕𐲖𐲗𐲘𐲙𐲚𐲛𐲜𐲝𐲞𐲟𐲠𐲡𐲢𐲣𐲤𐲥𐲦𐲧𐲨𐲩𐲪𐲫𐲬𐲭𐲮𐲯𐲰𐲱𐲲𑢠𑢡𑢢𑢣𑢤𑢥𑢦𑢧𑢨𑢩𑢪𑢫𑢬𑢭𑢮𑢯𑢰𑢱𑢲𑢳𑢴𑢵𑢶𑢷𑢸𑢹𑢺𑢻𑢼𑢽𑢾𑢿𖹀𖹁𖹂𖹃𖹄𖹅𖹆𖹇𖹈𖹉𖹊𖹋𖹌𖹍𖹎𖹏𖹐𖹑𖹒𖹓𖹔𖹕𖹖𖹗𖹘𖹙𖹚𖹛𖹜𖹝𖹞𖹟𞤀𞤁𞤂𞤃𞤄𞤅𞤆𞤇𞤈𞤉𞤊𞤋𞤌𞤍𞤎𞤏𞤐𞤑𞤒𞤓𞤔𞤕𞤖𞤗𞤘𞤙𞤚𞤛𞤜𞤝𞤞𞤟𞤠𞤡ẞᾈᾉᾊᾋᾌᾍᾎᾏᾘᾙᾚᾛᾜᾝᾞᾟᾨᾩᾪᾫᾬᾭᾮᾯᾼῌῼ";
//    private static final String lower = "abcdefghijklmnopqrstuvwxyzμàáâãäåæçèéêëìíîïðñòóôõöøùúûüýþāăąćĉċčďđēĕėęěĝğġģĥħĩīĭįĳĵķĺļľŀłńņňŋōŏőœŕŗřśŝşšţťŧũūŭůűųŵŷÿźżžsɓƃƅɔƈɖɗƌǝəɛƒɠɣɩɨƙɯɲɵơƣƥʀƨʃƭʈưʊʋƴƶʒƹƽǆǆǉǉǌǌǎǐǒǔǖǘǚǜǟǡǣǥǧǩǫǭǯǳǳǵƕƿǹǻǽǿȁȃȅȇȉȋȍȏȑȓȕȗșțȝȟƞȣȥȧȩȫȭȯȱȳⱥȼƚⱦɂƀʉʌɇɉɋɍɏιͱͳͷϳάέήίόύώαβγδεζηθικλμνξοπρστυφχψωϊϋσϗβθφπϙϛϝϟϡϣϥϧϩϫϭϯκρθεϸϲϻͻͼͽѐёђѓєѕіїјљњћќѝўџабвгдежзийклмнопрстуфхцчшщъыьэюяѡѣѥѧѩѫѭѯѱѳѵѷѹѻѽѿҁҋҍҏґғҕҗҙқҝҟҡңҥҧҩҫҭүұҳҵҷҹһҽҿӏӂӄӆӈӊӌӎӑӓӕӗәӛӝӟӡӣӥӧөӫӭӯӱӳӵӷӹӻӽӿԁԃԅԇԉԋԍԏԑԓԕԗԙԛԝԟԡԣԥԧԩԫԭԯաբգդեզէըթժիլխծկհձղճմյնշոչպջռսվտրցւփքօֆⴀⴁⴂⴃⴄⴅⴆⴇⴈⴉⴊⴋⴌⴍⴎⴏⴐⴑⴒⴓⴔⴕⴖⴗⴘⴙⴚⴛⴜⴝⴞⴟⴠⴡⴢⴣⴤⴥⴧⴭᏰᏱᏲᏳᏴᏵвдосттъѣꙋაბგდევზთიკლმნოპჟრსტუფქღყშჩცძწჭხჯჰჱჲჳჴჵჶჷჸჹჺჽჾჿḁḃḅḇḉḋḍḏḑḓḕḗḙḛḝḟḡḣḥḧḩḫḭḯḱḳḵḷḹḻḽḿṁṃṅṇṉṋṍṏṑṓṕṗṙṛṝṟṡṣṥṧṩṫṭṯṱṳṵṷṹṻṽṿẁẃẅẇẉẋẍẏẑẓẕṡạảấầẩẫậắằẳẵặẹẻẽếềểễệỉịọỏốồổỗộớờởỡợụủứừửữựỳỵỷỹỻỽỿἀἁἂἃἄἅἆἇἐἑἒἓἔἕἠἡἢἣἤἥἦἧἰἱἲἳἴἵἶἷὀὁὂὃὄὅὑὓὕὗὠὡὢὣὤὥὦὧᾰᾱὰάιὲέὴήῐῑὶίῠῡὺύῥὸόὼώωkåⅎⅰⅱⅲⅳⅴⅵⅶⅷⅸⅹⅺⅻⅼⅽⅾⅿↄⓐⓑⓒⓓⓔⓕⓖⓗⓘⓙⓚⓛⓜⓝⓞⓟⓠⓡⓢⓣⓤⓥⓦⓧⓨⓩⰰⰱⰲⰳⰴⰵⰶⰷⰸⰹⰺⰻⰼⰽⰾⰿⱀⱁⱂⱃⱄⱅⱆⱇⱈⱉⱊⱋⱌⱍⱎⱏⱐⱑⱒⱓⱔⱕⱖⱗⱘⱙⱚⱛⱜⱝⱞⱡɫᵽɽⱨⱪⱬɑɱɐɒⱳⱶȿɀⲁⲃⲅⲇⲉⲋⲍⲏⲑⲓⲕⲗⲙⲛⲝⲟⲡⲣⲥⲧⲩⲫⲭⲯⲱⲳⲵⲷⲹⲻⲽⲿⳁⳃⳅⳇⳉⳋⳍⳏⳑⳓⳕⳗⳙⳛⳝⳟⳡⳣⳬⳮⳳꙁꙃꙅꙇꙉꙋꙍꙏꙑꙓꙕꙗꙙꙛꙝꙟꙡꙣꙥꙧꙩꙫꙭꚁꚃꚅꚇꚉꚋꚍꚏꚑꚓꚕꚗꚙꚛꜣꜥꜧꜩꜫꜭꜯꜳꜵꜷꜹꜻꜽꜿꝁꝃꝅꝇꝉꝋꝍꝏꝑꝓꝕꝗꝙꝛꝝꝟꝡꝣꝥꝧꝩꝫꝭꝯꝺꝼᵹꝿꞁꞃꞅꞇꞌɥꞑꞓꞗꞙꞛꞝꞟꞡꞣꞥꞧꞩɦɜɡɬɪʞʇʝꭓꞵꞷꞹꞻꞽꞿꟃꞔʂᶎꟈꟊꟶᎠᎡᎢᎣᎤᎥᎦᎧᎨᎩᎪᎫᎬᎭᎮᎯᎰᎱᎲᎳᎴᎵᎶᎷᎸᎹᎺᎻᎼᎽᎾᎿᏀᏁᏂᏃᏄᏅᏆᏇᏈᏉᏊᏋᏌᏍᏎᏏᏐᏑᏒᏓᏔᏕᏖᏗᏘᏙᏚᏛᏜᏝᏞᏟᏠᏡᏢᏣᏤᏥᏦᏧᏨᏩᏪᏫᏬᏭᏮᏯａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ𐐨𐐩𐐪𐐫𐐬𐐭𐐮𐐯𐐰𐐱𐐲𐐳𐐴𐐵𐐶𐐷𐐸𐐹𐐺𐐻𐐼𐐽𐐾𐐿𐑀𐑁𐑂𐑃𐑄𐑅𐑆𐑇𐑈𐑉𐑊𐑋𐑌𐑍𐑎𐑏𐓘𐓙𐓚𐓛𐓜𐓝𐓞𐓟𐓠𐓡𐓢𐓣𐓤𐓥𐓦𐓧𐓨𐓩𐓪𐓫𐓬𐓭𐓮𐓯𐓰𐓱𐓲𐓳𐓴𐓵𐓶𐓷𐓸𐓹𐓺𐓻𐳀𐳁𐳂𐳃𐳄𐳅𐳆𐳇𐳈𐳉𐳊𐳋𐳌𐳍𐳎𐳏𐳐𐳑𐳒𐳓𐳔𐳕𐳖𐳗𐳘𐳙𐳚𐳛𐳜𐳝𐳞𐳟𐳠𐳡𐳢𐳣𐳤𐳥𐳦𐳧𐳨𐳩𐳪𐳫𐳬𐳭𐳮𐳯𐳰𐳱𐳲𑣀𑣁𑣂𑣃𑣄𑣅𑣆𑣇𑣈𑣉𑣊𑣋𑣌𑣍𑣎𑣏𑣐𑣑𑣒𑣓𑣔𑣕𑣖𑣗𑣘𑣙𑣚𑣛𑣜𑣝𑣞𑣟𖹠𖹡𖹢𖹣𖹤𖹥𖹦𖹧𖹨𖹩𖹪𖹫𖹬𖹭𖹮𖹯𖹰𖹱𖹲𖹳𖹴𖹵𖹶𖹷𖹸𖹹𖹺𖹻𖹼𖹽𖹾𖹿𞤢𞤣𞤤𞤥𞤦𞤧𞤨𞤩𞤪𞤫𞤬𞤭𞤮𞤯𞤰𞤱𞤲𞤳𞤴𞤵𞤶𞤷𞤸𞤹𞤺𞤻𞤼𞤽𞤾𞤿𞥀𞥁𞥂𞥃ßᾀᾁᾂᾃᾄᾅᾆᾇᾐᾑᾒᾓᾔᾕᾖᾗᾠᾡᾢᾣᾤᾥᾦᾧᾳῃῳ";
//    static {
//        for (int i = 0, n = upper.length(); i < n; i++) {
//            upperBits.set(upper.charAt(i));
//        }
//        System.out.print("private static final BitSet upperBits = BitSet.valueOf(new long[]{");
//        for (long item : upperBits.toLongArray()) {
//            System.out.print(item + "L, ");
//        }
//        System.out.println("});");
//        for (int i = 0, n = lower.length(); i < n; i++) {
//            lowerBits.set(lower.charAt(i));
//        }
//        System.out.print("private static final BitSet lowerBits = BitSet.valueOf(new long[]{");
//        for (long item : lowerBits.toLongArray()) {
//            System.out.print(item + "L, ");
//        }
//        System.out.println("});");
//    }

    /**
     * Returns true if {@code c} is a lower-case letter, or false otherwise.
     * Similar to {@link Character#isLowerCase(char)}, but should actually work on GWT.
     * @param c a char to check
     * @return true if c is a lower-case letter, or false otherwise.
     */
    public static boolean isLowerCase(char c) {
        return lowerBits.get(c);
    }

    /**
     * Returns true if {@code c} is an upper-case letter, or false otherwise.
     * Similar to {@link Character#isUpperCase(char)}, but should actually work on GWT.
     * @param c a char to check
     * @return true if c is an upper-case letter, or false otherwise.
     */
    public static boolean isUpperCase(char c) {
        return upperBits.get(c);
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
     * Copy constructor; does not copy the font's {@link #shader}, if it has one (it keeps a reference), but will fully
     * copy everything else.
     * @param toCopy another Font to copy
     */
    public Font(Font toCopy){
        distanceField = toCopy.distanceField;
        isMono = toCopy.isMono;
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

        // the shader is not copied, because there isn't much point in having different copies of a ShaderProgram.
        if(toCopy.shader != null)
            shader = toCopy.shader;
    }

    /**
     * Constructs a new Font by reading in a .fnt file with the given name (an internal handle is tried first, then a
     * classpath handle) and loading any images specified in that file. No distance field effect is used.
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
     * classpath handle) and loading any images specified in that file. The specified distance field effect is used.
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
                Gdx.app.error("textramode", "MSDF shader failed to compile: " + shader.getLog());
        }
        else if(distanceField == DistanceFieldType.SDF){
            shader = DistanceFieldFont.createDistanceFieldShader();
            if(!shader.isCompiled())
                Gdx.app.error("textramode", "SDF shader failed to compile: " + shader.getLog());
        }
        loadFNT(fntName, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    /**
     * Constructs a new Font by reading in a Texture from the given named path (internal is tried, then classpath),
     * and no distance field effect.
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
     * Constructs a new Font by reading in a Texture from the given named path (internal is tried, then classpath),
     * and the specified distance field effect.
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
                Gdx.app.error("textramode", "MSDF shader failed to compile: " + shader.getLog());
        }
        else if(distanceField == DistanceFieldType.SDF){
            shader = DistanceFieldFont.createDistanceFieldShader();
            if(!shader.isCompiled())
                Gdx.app.error("textramode", "SDF shader failed to compile: " + shader.getLog());
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
                Gdx.app.error("textramode", "MSDF shader failed to compile: " + shader.getLog());
        }
        else if(distanceField == DistanceFieldType.SDF){
            shader = DistanceFieldFont.createDistanceFieldShader();
            if(!shader.isCompiled())
                Gdx.app.error("textramode", "SDF shader failed to compile: " + shader.getLog());
        }
        this.parents = Array.with(textureRegion);
        if (distanceField == DistanceFieldType.SDF || distanceField == DistanceFieldType.MSDF) {
            textureRegion.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        loadFNT(fntName, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    /**
     * Constructs a font based off of an AngelCode BMFont .fnt file and the given TextureRegion Array, with no distance
     * field effect.
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
     * distance field effect.
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
                Gdx.app.error("textramode", "MSDF shader failed to compile: " + shader.getLog());
        }
        else if(distanceField == DistanceFieldType.SDF){
            shader = DistanceFieldFont.createDistanceFieldShader();
            if(!shader.isCompiled())
                Gdx.app.error("textramode", "SDF shader failed to compile: " + shader.getLog());
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
                Gdx.app.error("textramode", "MSDF shader failed to compile: " + shader.getLog());
        }
        else if(distanceField == DistanceFieldType.SDF){
            shader = DistanceFieldFont.createDistanceFieldShader();
            if(!shader.isCompiled())
                Gdx.app.error("textramode", "SDF shader failed to compile: " + shader.getLog());
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
        defaultValue =  mapping.get(data.missingGlyph == null ? ' ' : data.missingGlyph.id, mapping.get(' ', mapping.values().next()));
        originalCellWidth = cellWidth;
        originalCellHeight = cellHeight;
        isMono = minWidth == cellWidth && kerning == null;
        scale(bmFont.getScaleX(), bmFont.getScaleY());
    }
    /**
     * The gritty parsing code that pulls relevant info from an AngelCode BMFont .fnt file and uses it to assemble the
     * many {@code TextureRegion}s this has for each glyph.
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
        defaultValue = mapping.get(' ', mapping.get(0));
        originalCellWidth = cellWidth;
        originalCellHeight = cellHeight;
        isMono = minWidth == cellWidth && kerning == null;
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
     * @param batch the Batch to instruct to use the appropriate shader for this font; should usually be a SpriteBatch
     */
    public void enableShader(Batch batch) {
        if(distanceField == DistanceFieldType.MSDF) {
            if (batch.getShader() != shader) {
                batch.setShader(shader);
                shader.setUniformf("u_smoothing", 7f * distanceFieldCrispness * Math.max(cellHeight / originalCellHeight, cellWidth / originalCellWidth));
            }
        } else if(distanceField == DistanceFieldType.SDF){
            if (batch.getShader() != shader) {
                batch.setShader(shader);
                final float scale = Math.max(cellHeight / originalCellHeight, cellWidth / originalCellWidth) * 0.5f + 0.5f;
                shader.setUniformf("u_smoothing", (distanceFieldCrispness / (scale * scale)));
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
     * Draws the specified text at the given x,y position (in world space), parsing an extension of libGDX markup
     * and using it to determine color, size, position, shape, strikethrough, underline, and case of the given
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
     *     <li>{@code [#HHHHHHHH]}, where HHHHHHHH is a hex RGB888 or RGBA8888 int color, changes the color.</li>
     *     <li>{@code [COLORNAME]}, where "COLORNAME" is a typically-upper-case color name that will be looked up in
     *     {@link Colors}, changes the color. The name can optionally be preceded by {@code |}, which allows looking up
     *     colors with names that contain punctuation.</li>
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
                int kern = -1, amt = 0;
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
    public int drawGlyphs(Batch batch, Layout glyphs, float x, float y) {
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
    public int drawGlyphs(Batch batch, Layout glyphs, float x, float y, int align) {
        int drawn = 0;
        final int lines = glyphs.lines();
        for (int ln = 0; ln < lines; ln++) {
            drawn += drawGlyphs(batch, glyphs.getLine(ln), x, y, align);
            y -= cellHeight;
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
    public int drawGlyphs(Batch batch, Line glyphs, float x, float y) {
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
    public int drawGlyphs(Batch batch, Line glyphs, float x, float y, int align) {
        if(glyphs == null) return 0;
        int drawn = 0;
        if(Align.isCenterHorizontal(align))
            x -= glyphs.width * 0.5f;
        else if(Align.isRight(align))
            x -= glyphs.width;
        if(kerning != null) {
            int kern = -1;
            float amt = 0;
            long glyph;
            for (int i = 0, n = glyphs.glyphs.size; i < n; i++, drawn++) {
                kern = kern << 16 | (int) ((glyph = glyphs.glyphs.get(i)) & 0xFFFF);
                amt = kerning.get(kern, 0) * scaleX;
                x += drawGlyph(batch, glyph, x + amt, y) + amt;
            }
        }
        else {
            for (int i = 0, n = glyphs.glyphs.size; i < n; i++, drawn++) {
                x += drawGlyph(batch, glyphs.glyphs.get(i), x, y);
            }
        }
        return drawn;
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
        float changedW = tr.xAdvance * scaleX;
        if (isMono) {
            changedW += tr.offsetX * scaleX;
        }
        else if((glyph & SUPERSCRIPT) != 0L){
            changedW *= 0.5f;
        }
        return changedW;
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
        GlyphRegion tr = mapping.get((char) glyph);
        if (tr == null) return 0f;
        Texture tex = tr.getTexture();
        float x0 = 0f, x1 = 0f, x2 = 0f, x3 = 0f;
        float y0 = 0f, y1 = 0f, y2 = 0f, y3 = 0f;
        float color = NumberUtils.intBitsToFloat(((int)(batch.getColor().a * 127.999f) << 25)
                | (0xFFFFFF & Integer.reverseBytes((int) (glyph >>> 32))));
        final float iw = 1f / tex.getWidth();
        float u, v, u2, v2;
        u = tr.getU();
        v = tr.getV();
        u2 = tr.getU2();
        v2 = tr.getV2();
        float w = tr.getRegionWidth() * scaleX, changedW = tr.xAdvance * scaleX, h = tr.getRegionHeight() * scaleY;
        if (isMono) {
            changedW += tr.offsetX * scaleX;
        } else {
            x += tr.offsetX * scaleX;
        }
        float yt = y + cellHeight - h - tr.offsetY * scaleY;
        if ((glyph & OBLIQUE) != 0L) {
            x0 += h * 0.2f;
            x1 -= h * 0.2f;
            x2 -= h * 0.2f;
            x3 += h * 0.2f;
        }
        final long script = (glyph & SUPERSCRIPT);
        if (script == SUPERSCRIPT) {
            w *= 0.5f;
            h *= 0.5f;
            yt = y + cellHeight * 0.625f - h - tr.offsetY * scaleY * 0.5f;
            y0 += cellHeight * 0.375f;
            y1 += cellHeight * 0.375f;
            y2 += cellHeight * 0.375f;
            y3 += cellHeight * 0.375f;
            if(!isMono)
                changedW *= 0.5f;
        }
        else if (script == SUBSCRIPT) {
            w *= 0.5f;
            h *= 0.5f;
            yt = y + cellHeight * 0.625f - h - tr.offsetY * scaleY * 0.5f;
            y0 -= cellHeight * 0.125f;
            y1 -= cellHeight * 0.125f;
            y2 -= cellHeight * 0.125f;
            y3 -= cellHeight * 0.125f;
            if(!isMono)
                changedW *= 0.5f;
        }
        else if(script == MIDSCRIPT) {
            w *= 0.5f;
            h *= 0.5f;
            yt = y + cellHeight * 0.625f - h - tr.offsetY * scaleY * 0.5f;
            y0 += cellHeight * 0.125f;
            y1 += cellHeight * 0.125f;
            y2 += cellHeight * 0.125f;
            y3 += cellHeight * 0.125f;
            if(!isMono)
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
            final GlyphRegion under = mapping.get('_');
            if (under != null) {
                final float underU = under.getU() + (under.xAdvance - under.offsetX) * iw * 0.25f,
                        underV = under.getV(),
                        underU2 = under.getU() + (under.xAdvance - under.offsetX) * iw * 0.75f,
                        underV2 = under.getV2(),
                        hu = under.getRegionHeight() * scaleY, yu = y + cellHeight - hu - under.offsetY * scaleY;
                vertices[0] = x - 1f;
                vertices[1] = yu + hu;
                vertices[2] = color;
                vertices[3] = underU;
                vertices[4] = underV;

                vertices[5] = x - 1f;
                vertices[6] = yu;
                vertices[7] = color;
                vertices[8] = underU;
                vertices[9] = underV2;

                vertices[10] = x + changedW + 1f;
                vertices[11] = yu;
                vertices[12] = color;
                vertices[13] = underU2;
                vertices[14] = underV2;

                vertices[15] = x + changedW + 1f;
                vertices[16] = yu + hu;
                vertices[17] = color;
                vertices[18] = underU2;
                vertices[19] = underV;
                batch.draw(under.getTexture(), vertices, 0, 20);
            }
        }
        if ((glyph & STRIKETHROUGH) != 0L) {
            final GlyphRegion dash = mapping.get('-');
            if (dash != null) {
                final float dashU = dash.getU() + (dash.xAdvance - dash.offsetX) * iw * 0.625f,
                        dashV = dash.getV(),
                        dashU2 = dashU + iw,
                        dashV2 = dash.getV2(),
                        hd = dash.getRegionHeight() * scaleY, yd = y + cellHeight - hd - dash.offsetY * scaleY;
                x0 = x - (dash.offsetX);
                vertices[0] = x0 - 1f;
                vertices[1] = yd + hd;
                vertices[2] = color;
                vertices[3] = dashU;
                vertices[4] = dashV;

                vertices[5] = x0 - 1f;
                vertices[6] = yd;
                vertices[7] = color;
                vertices[8] = dashU;
                vertices[9] = dashV2;

                vertices[10] = x0 + changedW + 1f;
                vertices[11] = yd;
                vertices[12] = color;
                vertices[13] = dashU2;
                vertices[14] = dashV2;

                vertices[15] = x0 + changedW + 1f;
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

        GlyphRegion tr = mapping.get((char) glyph);
        if (tr == null) return 0f;
        Texture tex = tr.getTexture();
        float x0 = 0f;
        float x1 = 0f;
        float x2 = 0f;
        float y0 = 0f;
        float y1 = 0f;
        float y2 = 0f;
        float color = NumberUtils.intBitsToFloat(Integer.reverseBytes(((int) (glyph >>> 32) & -256) | (int)(batch.getColor().a * 255.999f)));
        final float iw = 1f / tex.getWidth();
        float u, v, u2, v2;
        u = tr.getU();
        v = tr.getV();
        u2 = tr.getU2();
        v2 = tr.getV2();
        float w = tr.getRegionWidth() * scaleX, changedW = tr.xAdvance * scaleX, h = tr.getRegionHeight() * scaleY;
        if (isMono) {
            changedW += tr.offsetX * scaleX;
        } else {
            x += tr.offsetX * scaleX;
        }
        float yt = y + cellHeight - h - tr.offsetY * scaleY;
        if ((glyph & OBLIQUE) != 0L) {
            x0 += h * 0.2f;
            x1 -= h * 0.2f;
            x2 -= h * 0.2f;
        }
        final long script = (glyph & SUPERSCRIPT);
        if (script == SUPERSCRIPT) {
            w *= 0.5f;
            h *= 0.5f;
            y1 += cellHeight * 0.375f;
            y2 += cellHeight * 0.375f;
            y0 += cellHeight * 0.375f;
            if(!isMono)
                changedW *= 0.5f;
        }
        else if (script == SUBSCRIPT) {
            w *= 0.5f;
            h *= 0.5f;
            y1 -= cellHeight * 0.125f;
            y2 -= cellHeight * 0.125f;
            y0 -= cellHeight * 0.125f;
            if(!isMono)
                changedW *= 0.5f;
        }
        else if(script == MIDSCRIPT) {
            w *= 0.5f;
            h *= 0.5f;
            y0 += cellHeight * 0.125f;
            y1 += cellHeight * 0.125f;
            y2 += cellHeight * 0.125f;
            if(!isMono)
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
            final GlyphRegion under = mapping.get('_');
            if (under != null) {
                final float underU = under.getU() + (under.xAdvance - under.offsetX) * iw * 0.25f,
                        underV = under.getV(),
                        underU2 = under.getU() + (under.xAdvance - under.offsetX) * iw * 0.75f,
                        underV2 = under.getV2(),
                        hu = under.getRegionHeight() * scaleY, yu = y + cellHeight - hu - under.offsetY * scaleY;
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

                p0x = x - 1f;
                p0y = yu + hu;
                p1x = x - 1f;
                p1y = yu;
                p2x = x + changedW + 1f;
                p2y = yu;
                vertices[15] = (vertices[0]  = cos * p0x - sin * p0y) - (vertices[5]  = cos * p1x - sin * p1y) + (vertices[10] = cos * p2x - sin * p2y);
                vertices[16] = (vertices[1]  = sin * p0x + cos * p0y) - (vertices[6]  = sin * p1x + cos * p1y) + (vertices[11] = sin * p2x + cos * p2y);

                batch.draw(under.getTexture(), vertices, 0, 20);
            }
        }
        if ((glyph & STRIKETHROUGH) != 0L) {
            final GlyphRegion dash = mapping.get('-');
            if (dash != null) {
                final float dashU = dash.getU() + (dash.xAdvance - dash.offsetX) * iw * 0.625f,
                        dashV = dash.getV(),
                        dashU2 = dashU + iw,
                        dashV2 = dash.getV2(),
                        hd = dash.getRegionHeight() * scaleY, yd = y + cellHeight - hd - dash.offsetY * scaleY;
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

                p0x = x0 - 1f;
                p0y = yd + hd;
                p1x = x0 - 1f;
                p1y = yd;
                p2x = x0 + changedW + 1f;
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
     * {@code Pools.obtain(Layout.class)}; you can free the Layout when you are done using it with
     * {@link Pools#free(Object)}. This parses an extension of libGDX markup and uses it to determine color, size,
     * position, shape, strikethrough, underline, and case of the given CharSequence. It also reads typing markup, for
     * effects, but passes it through without changing it and without considering it for line wrapping or text position.
     * The text drawn will start as white, with the normal size as determined by the font's metrics and scale
     * ({@link #scaleX} and {@link #scaleY}), normal case, and without bold, italic, superscript, subscript,
     * strikethrough, or underline. Markup starts with {@code [}; the next character determines what that piece of
     * markup toggles. Markup this knows:
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
     *     <li>{@code [#HHHHHHHH]}, where HHHHHHHH is a hex RGB888 or RGBA8888 int color, changes the color.</li>
     *     <li>{@code [COLORNAME]}, where "COLORNAME" is a typically-upper-case color name that will be looked up in
     *     {@link Colors}, changes the color. The name can optionally be preceded by {@code |}, which allows looking up
     *     colors with names that contain punctuation.</li>
     * </ul>
     * You can render {@code appendTo} using {@link #drawGlyphs(Batch, Layout, float, float)}.
     * @param text text with markup
     * @param appendTo a Layout that stores one or more Line objects, carrying color, style, chars, and size
     * @return appendTo, for chaining
     */
    public Layout markup(String text, Layout appendTo) {
        boolean capitalize = false, previousWasLetter = false,
                capsLock = false, lowerCase = false;
        int c;
        final long COLOR_MASK = 0xFFFFFFFF00000000L;
        long baseColor = Long.reverseBytes(NumberUtils.floatToIntColor(appendTo.getBaseColor())) & COLOR_MASK;
        long color = baseColor;
        long current = color;
        if(appendTo.font == null || !appendTo.font.equals(this))
        {
            appendTo.clear();
            appendTo.font(this);
        }
        appendTo.peekLine().height = cellHeight;
        float targetWidth = appendTo.getTargetWidth();
        int kern = -1;
        for (int i = 0, n = text.length(); i < n; i++) {
            if((c = text.charAt(i)) == '{') {
                if (i+1 < n && text.charAt(i+1) != '{') {
                    int end = text.indexOf('}', i);
                    for (; i < n && i <= end; i++) {
                        appendTo.add(text.charAt(i));
                    }
                    i--;
                }
            }
            else if(text.charAt(i) == '['){
                if(++i < n && (c = text.charAt(i)) != '['){
                    if(c == ']'){
                        color = baseColor;
                        current = color;
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
                        case '#':
                            if (len >= 7 && len < 9)
                                color = longFromHex(text, i + 1, i + 7) << 40 | 0x000000FF00000000L;
                            else if (len >= 9)
                                color = longFromHex(text, i + 1, i + 9) << 32;
                            else
                                color = baseColor;
                            current = (current & ~COLOR_MASK) | color;
                            break;
                        case '|':
                            // attempt to look up a known Color name from Colors
                            Color lookupColor = Colors.get(text.substring(i + 1, i + len));
                            if (lookupColor == null) color = baseColor;
                            else color = (long) Color.rgba8888(lookupColor) << 32;
                            current = (current & ~COLOR_MASK) | color;
                            break;
                        default:
                            // attempt to look up a known Color name from Colors
                            Color gdxColor = Colors.get(text.substring(i, i + len));
                            if (gdxColor == null) color = baseColor;
                            else color = (long) Color.rgba8888(gdxColor) << 32;
                            current = (current & ~COLOR_MASK) | color;
                    }
                    i += len;
                }
                else {
                    float w;
                    if (kerning == null) {
                        w = (appendTo.peekLine().width += xAdvance(current | '['));
                    } else {
                        kern = kern << 16 | '[';
                        w = (appendTo.peekLine().width += xAdvance(current | '[') + kerning.get(kern, 0) * scaleX);
                    }
                    appendTo.add(current | 2);
                    if(targetWidth > 0 && w > targetWidth) {
                        Line earlier = appendTo.peekLine();
                        int ln = appendTo.lines() - 1;
                        Line later = appendTo.pushLine();
                        if(later == null){
                            // here, the max lines have been reached, and an ellipsis may need to be added
                            // to the last line.
                            if(appendTo.ellipsis != null) {
                                for (int j = earlier.glyphs.size - 1 - appendTo.ellipsis.length(); j >= 0; j--) {
                                    int leading = 0;
                                    long currE, curr;
                                    while ((curr = earlier.glyphs.get(j)) >>> 32 == 0L || Arrays.binarySearch(spaceChars.items, 0, spaceChars.size, (char) curr) >= 0) {
                                        ++leading;
                                        --j;
                                    }
                                    float change = 0f, changeNext = 0f;
                                    if (kerning == null) {
                                        for (int k = j + 1, e = 0; k < earlier.glyphs.size; k++, e++) {
                                            change += xAdvance(earlier.glyphs.get(k));
                                            if (--leading < 0 && (e < appendTo.ellipsis.length())) {
                                                float adv = xAdvance(baseColor | appendTo.ellipsis.charAt(e));
                                                changeNext += adv;
                                            }
                                        }
                                    } else {
                                        int k2 = ((int) earlier.glyphs.get(j) & 0xFFFF), k3 = -1;
                                        int k2e = appendTo.ellipsis.charAt(0) & 0xFFFF, k3e = -1;
                                        for (int k = j + 1, e = 0; k < earlier.glyphs.size; k++, e++) {
                                            currE = baseColor | appendTo.ellipsis.charAt(e);
                                            curr = earlier.glyphs.get(k);
                                            k2 = k2 << 16 | (char) curr;
                                            k2e = k2e << 16 | (char) currE;
                                            float adv = xAdvance(curr);
                                            change += adv + kerning.get(k2, 0) * scaleX;
                                            if (--leading < 0 && (e < appendTo.ellipsis.length())) {
                                                changeNext += xAdvance(currE) + kerning.get(k2e, 0) * scaleX;
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
                                    if (kerning == null) {
                                        for (int k = j + 1; k < earlier.glyphs.size; k++) {
                                            float adv = xAdvance(curr = earlier.glyphs.get(k));
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
                                            float adv = xAdvance(curr);
                                            change += adv + kerning.get(k2, 0) * scaleX;
                                            if (--leading < 0) {
                                                k3 = k3 << 16 | (char) curr;
                                                changeNext += adv + kerning.get(k3, 0) * scaleX;
                                                appendTo.add(curr);
                                            }
                                        }
                                    }
//                                    earlier.glyphs.truncate(j + 1);
                                    earlier.glyphs.truncate(j + 2);
                                    earlier.glyphs.set(j+1, '\n');
                                    later.width = changeNext;
                                    earlier.width -= change;
                                    break;
                                }
                            }
                        }
                    }
                }
            } else {
                char ch = text.charAt(i);
                if (isLowerCase(ch)) {
                    if ((capitalize && !previousWasLetter) || capsLock) {
                        ch = Character.toUpperCase(ch);
                    }
                    previousWasLetter = true;
                } else if (isUpperCase(ch)) {
                    if ((capitalize && previousWasLetter) || lowerCase) {
                        ch = Character.toLowerCase(ch);
                    }
                    previousWasLetter = true;
                } else {
                    previousWasLetter = false;
                }
                float w;
                if (kerning == null) {
                    w = (appendTo.peekLine().width += xAdvance(current | ch));
                } else {
                    kern = kern << 16 | (int) ((current | ch) & 0xFFFF);
                    w = (appendTo.peekLine().width += xAdvance(current | ch) + kerning.get(kern, 0) * scaleX);
                }
                appendTo.add(current | ch);
                if((targetWidth > 0 && w > targetWidth) || appendTo.atLimit) {
                    Line earlier = appendTo.peekLine();
                    Line later;
                    if(appendTo.lines.size >= appendTo.maxLines) {
                        later = null;
                    }
                    else {
                        later = Pools.obtain(Line.class);
                        later.height = earlier.height;
                        appendTo.lines.add(later);
                    }
                    if(later == null){
                        // here, the max lines have been reached, and an ellipsis may need to be added
                        // to the last line.
                        if(appendTo.ellipsis != null) {
                            for (int j = earlier.glyphs.size - 1; j >= 0; j--) {
                                int leading = 0;
                                long curr;
                                // remove a full word or other group of non-space characters.
                                while ((curr = earlier.glyphs.get(j)) >>> 32 == 0L || Arrays.binarySearch(spaceChars.items, 0, spaceChars.size, (char) curr) < 0 && j > 0) {
                                    ++leading;
                                    --j;
                                }
                                // remove the remaining space characters.
                                while ((curr = earlier.glyphs.get(j)) >>> 32 == 0L ||
                                        Arrays.binarySearch(spaceChars.items, 0, spaceChars.size, (char) curr) >= 0 && j > 0) {
                                    ++leading;
                                    --j;
                                }
                                float change = 0f, changeNext = 0f;
                                long currE;
                                if (kerning == null) {
                                    for (int k = j + 1, e = 0; k < earlier.glyphs.size; k++, e++) {
                                        change += xAdvance(earlier.glyphs.get(k));
                                        if ((e < appendTo.ellipsis.length())) {
                                            float adv = xAdvance(baseColor | appendTo.ellipsis.charAt(e));
                                            changeNext += adv;
                                        }
                                    }
                                } else {
                                    int k2 = ((int) earlier.glyphs.get(j) & 0xFFFF), k3 = -1;
                                    int k2e = appendTo.ellipsis.charAt(0) & 0xFFFF, k3e = -1;
                                    for (int k = j + 1, e = 0; k < earlier.glyphs.size; k++, e++) {
                                        curr = earlier.glyphs.get(k);
                                        k2 = k2 << 16 | (char) curr;
                                        float adv = xAdvance(curr);
                                        change += adv + kerning.get(k2, 0) * scaleX;
                                        if ((e < appendTo.ellipsis.length())) {
                                            currE = baseColor | appendTo.ellipsis.charAt(e);
                                            k2e = k2e << 16 | (char) currE;
                                            changeNext += xAdvance(currE) + kerning.get(k2e, 0) * scaleX;
//                                                appendTo.add(currE);
                                        }
                                    }
                                }
                                if (earlier.width + changeNext < appendTo.getTargetWidth()) {
                                    for (int e = 0; e < appendTo.ellipsis.length(); e++) {
                                        earlier.glyphs.add(baseColor | appendTo.ellipsis.charAt(e));
                                    }
                                    earlier.width = earlier.width + changeNext;
                                    return appendTo;
                                }
                                if (earlier.width - change + changeNext < appendTo.getTargetWidth()) {
                                    earlier.glyphs.truncate(j + 1);
                                    for (int e = 0; e < appendTo.ellipsis.length(); e++) {
                                        earlier.glyphs.add(baseColor | appendTo.ellipsis.charAt(e));
                                    }
                                    earlier.width = earlier.width - change + changeNext;
                                    return appendTo;
                                }
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
                                if (kerning == null) {
                                    for (int k = j + 1; k < earlier.glyphs.size; k++) {
                                        float adv = xAdvance(curr = earlier.glyphs.get(k));
                                        change += adv;
                                        if (--leading < 0) {
//                                            if((curr & 0xFFFFL) == 10L) {
//                                                Line line = Pools.obtain(Line.class);
//                                                line.height = earlier.height;
//                                                appendTo.lines.insert(ln + 1, line);
//                                            }
                                            appendTo.add(curr);
                                            changeNext += adv;
                                        }
                                    }
                                } else {
                                    int k2 = ((int) earlier.glyphs.get(j) & 0xFFFF), k3 = -1;
                                    for (int k = j + 1; k < earlier.glyphs.size; k++) {
                                        curr = earlier.glyphs.get(k);
                                        k2 = k2 << 16 | (char) curr;
                                        float adv = xAdvance(curr);
                                        change += adv + kerning.get(k2, 0) * scaleX;
                                        if (--leading < 0) {
                                            k3 = k3 << 16 | (char) curr;
                                            changeNext += adv + kerning.get(k3, 0) * scaleX;
//                                            if((curr & 0xFFFFL) == 10L) {
//                                                Line line = Pools.obtain(Line.class);
//                                                line.height = earlier.height;
//                                                appendTo.lines.insert(ln + 1, line);
//                                            }
                                            appendTo.add(curr);
                                        }
                                    }
                                }



                                earlier.glyphs.truncate(j+1);
                                earlier.glyphs.add('\n');
                                later.width = changeNext;
                                earlier.width -= change;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return appendTo;
    }

    public int wrap(Layout changing, float targetWidth) {
        if(changing.font == null || !changing.font.equals(this) || targetWidth <= 0f)
        {
            return 0;
        }
        int kern = -1, changed = 0;
        final long COLOR_MASK = 0xFFFFFFFF00000000L;
        long baseColor = Long.reverseBytes(NumberUtils.floatToIntColor(changing.getBaseColor())) & COLOR_MASK;
        for (int ln = 0; ln < changing.lines(); ln++) {
            Line earlier = changing.getLine(ln);
//            if(ln >= 7)
//                System.out.println(earlier.glyphs.size);
            float nextWidth = 0f;
            PER_GLYPH:
            for (int i = 0; i < earlier.glyphs.size; i++) {
                long current = earlier.glyphs.get(i);
                float w;
                if (kerning == null) {
                    w = (nextWidth += xAdvance(current));
                } else {
                    kern = kern << 16 | (int) ((current) & 0xFFFF);
                    w = (nextWidth += xAdvance(current) + kerning.get(kern, 0) * scaleX);
                }
                if (w > targetWidth || changing.atLimit) {
                    Line later;


                    if(ln + 1 >= changing.maxLines) {
                        changing.atLimit = true;
                        later = null;
//                        later = Pools.obtain(Line.class);
//                        later.height = earlier.height;
                    } else if(ln + 1 < changing.lines()) {
                        later = changing.getLine(ln + 1);
                    }
                    else{
                        Line line = Pools.obtain(Line.class);
                        line.height = earlier.height;
//                        earlier.glyphs.add('\n');
//                        changed++;
                        changing.lines.insert(ln + 1, line);
                        later = line;
                    }


                    if (later == null) {
                        // here, the max lines have been reached, and an ellipsis may need to be added
                        // to the last line.
                        if (changing.ellipsis != null) {
                            for (int j = earlier.glyphs.size - 1; j >= 0; j--) {
                                int leading = 0;
                                while (Arrays.binarySearch(spaceChars.items, 0, spaceChars.size, (char) earlier.glyphs.get(j)) < 0 && j > 0) {
                                    ++leading;
                                    --j;
                                }
                                while (Arrays.binarySearch(spaceChars.items, 0, spaceChars.size, (char) earlier.glyphs.get(j)) >= 0 && j > 0) {
                                    ++leading;
                                    --j;
                                }
                                float change = 0f, changeNext = 0f;
                                long currE, curr;
                                if (kerning == null) {
                                    for (int k = j + 1, e = 0; k < earlier.glyphs.size; k++, e++) {
                                        change += xAdvance(earlier.glyphs.get(k));
                                        if ((e < changing.ellipsis.length())) {
                                            float adv = xAdvance(baseColor | changing.ellipsis.charAt(e));
                                            changeNext += adv;
                                        }
                                    }
                                } else {
                                    int k2 = ((int) earlier.glyphs.get(j) & 0xFFFF), k3 = -1;
                                    int k2e = changing.ellipsis.charAt(0) & 0xFFFF, k3e = -1;
                                    for (int k = j + 1, e = 0; k < earlier.glyphs.size; k++, e++) {
                                        curr = earlier.glyphs.get(k);
                                        k2 = k2 << 16 | (char) curr;
                                        float adv = xAdvance(curr);
                                        change += adv + kerning.get(k2, 0) * scaleX;
                                        if ((e < changing.ellipsis.length())) {
                                            currE = baseColor | changing.ellipsis.charAt(e);
                                            k2e = k2e << 16 | (char) currE;
                                            changeNext += xAdvance(currE) + kerning.get(k2e, 0) * scaleX;
                                        }
                                    }
                                }
                                if (earlier.width + changeNext < targetWidth) {
                                    for (int e = 0; e < changing.ellipsis.length(); e++) {
                                        earlier.glyphs.add(baseColor | changing.ellipsis.charAt(e));
                                    }
                                    earlier.width = earlier.width + changeNext;
                                    return changed + changing.ellipsis.length();
                                }
                                if (earlier.width - change + changeNext < targetWidth) {
                                    changed += j + 1 - earlier.glyphs.size;
                                    earlier.glyphs.truncate(j + 1);
                                    for (int e = 0; e < changing.ellipsis.length(); e++) {
                                        earlier.glyphs.add(baseColor | changing.ellipsis.charAt(e));
                                    }
                                    earlier.width = earlier.width - change + changeNext;
                                    return changed + changing.ellipsis.length();
                                }
                            }
                        }
                    } else {
                        for (int j = earlier.glyphs.size - 2; j >= 0; j--) {
                            if (Arrays.binarySearch(breakChars.items, 0, breakChars.size, (char) earlier.glyphs.get(j)) >= 0) {
                                int leading = 0;
                                while (Arrays.binarySearch(spaceChars.items, 0, spaceChars.size, (char) earlier.glyphs.get(j)) >= 0) {
                                    ++leading;
                                    --j;
                                }
                                --leading;
                                ++j;
                                float change = 0f, changeNext = 0f;
                                long curr;
                                if (kerning == null) {
                                    for (int k = j + 1, idx = 0; k < earlier.glyphs.size; k++) {
                                        float adv = xAdvance(curr = earlier.glyphs.get(k));
                                        change += adv;
                                        if (--leading < 0) {
//                                            if((curr & 0xFFFFL) == 10L) {
//                                                Line line = Pools.obtain(Line.class);
//                                                line.height = earlier.height;
//                                                changing.lines.insert(ln + 1, line);
//                                            }
                                            changeNext += adv;
                                            break;
                                        }
                                        later.glyphs.insert(idx++, curr);
                                        changed++;
                                    }
                                } else {
                                    int k2 = ((int) earlier.glyphs.get(j) & 0xFFFF), k3 = -1;
                                    for (int k = j + 1, idx = 0; k < earlier.glyphs.size; k++) {
                                        curr = earlier.glyphs.get(k);
                                        k2 = k2 << 16 | (char) curr;
                                        float adv = xAdvance(curr);
                                        change += adv + kerning.get(k2, 0) * scaleX;
                                        if (--leading < 0) {
                                            k3 = k3 << 16 | (char) curr;
                                            changeNext += adv + kerning.get(k3, 0) * scaleX;
//                                            if((curr & 0xFFFFL) == 10L) {
//                                                Line line = Pools.obtain(Line.class);
//                                                line.height = earlier.height;
//                                                changing.lines.insert(ln + 1, line);
//                                            }
                                            break;
                                        }
                                        later.glyphs.insert(idx++, curr);
                                        changed++;
                                    }
                                }
                                for (int p = 0; p < earlier.glyphs.size - 1 - j; p++) {
                                    later.glyphs.insert(p, earlier.glyphs.get(j + 1 + p));
                                    changed++;
                                }
                                changed += j - earlier.glyphs.size;
                                earlier.glyphs.truncate(j);
                                if(ln + 1 < changing.maxLines)
                                {
                                    earlier.glyphs.add('\n');
                                    changed++;
                                }
//                                changing.add('\n');
                                later.width += changeNext;
                                earlier.width -= change;
                                break PER_GLYPH;
                            }
                        }
                    }
                }
            }
        }

        return changed;
    }

    /**
     * Releases all resources of this object.
     */
    @Override
    public void dispose() {
        Pools.free(tempLayout);
        if(shader != null)
            shader.dispose();
    }
}
