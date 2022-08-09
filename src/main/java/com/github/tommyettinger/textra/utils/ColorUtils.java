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

package com.github.tommyettinger.textra.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntArray;

import static com.github.tommyettinger.textra.utils.Palette.NAMED;

/**
 * A few static methods for commonly-used color handling tasks.
 * This has methods to convert from HSLA colors to RGBA and back again, for hue-changing effects mainly.
 * It also has {@link #lerpColors(int, int, float)} to blend RGBA colors, and {@link #multiplyAlpha(int, float)} to
 * alter only the alpha channel on an RGBA or HSLA int color.
 */
public class ColorUtils {
    /**
     * Converts the four HSLA components, each in the 0.0 to 1.0 range, to an int in RGBA8888 format.
     * I brought this over from colorful-gdx's FloatColors class. I can't recall where I got the original HSL(A) code
     * from, but there's a strong chance it was written by cypherdare/cyphercove for their color space comparison.
     *
     * @param h hue, from 0.0 to 1.0
     * @param s saturation, from 0.0 to 1.0
     * @param l lightness, from 0.0 to 1.0
     * @param a alpha, from 0.0 to 1.0
     * @return an RGBA8888-format int
     */
    public static int hsl2rgb(final float h, final float s, final float l, final float a) {
        float x = Math.min(Math.max(Math.abs(h * 6f - 3f) - 1f, 0f), 1f);
        float y = h + (2f / 3f);
        float z = h + (1f / 3f);
        y -= (int) y;
        z -= (int) z;
        y = Math.min(Math.max(Math.abs(y * 6f - 3f) - 1f, 0f), 1f);
        z = Math.min(Math.max(Math.abs(z * 6f - 3f) - 1f, 0f), 1f);
        float v = (l + s * Math.min(l, 1f - l));
        float d = 2f * (1f - l / (v + 1e-10f));
        return Color.rgba8888(v * MathUtils.lerp(1f, x, d), v * MathUtils.lerp(1f, y, d), v * MathUtils.lerp(1f, z, d), a);
    }

    /**
     * Converts the four RGBA components, each in the 0.0 to 1.0 range, to an int in HSLA format (hue,
     * saturation, lightness, alpha). This format is exactly like RGBA8888 but treats what would normally be red as hue,
     * green as saturation, and blue as lightness; alpha is the same.
     *
     * @param r red, from 0.0 to 1.0
     * @param g green, from 0.0 to 1.0
     * @param b blue, from 0.0 to 1.0
     * @param a alpha, from 0.0 to 1.0
     * @return an "HSLA-format" int
     */
    public static int rgb2hsl(final float r, final float g, final float b, final float a) {
        float x, y, z, w;
        if (g < b) {
            x = b;
            y = g;
            z = -1f;
            w = 2f / 3f;
        } else {
            x = g;
            y = b;
            z = 0f;
            w = -1f / 3f;
        }
        if (r < x) {
            z = w;
            w = r;
        } else {
            w = x;
            x = r;
        }
        float d = x - Math.min(w, y);
        float l = x * (1f - 0.5f * d / (x + 1e-10f));
        return Color.rgba8888(Math.abs(z + (w - y) / (6f * d + 1e-10f)), (x - l) / (Math.min(l, 1f - l) + 1e-10f), l, a);
    }

    /**
     * Converts the four HSBA/HSVA components, each in the 0.0 to 1.0 range, to an int in RGBA8888 format.
     * I brought this over from colorful-gdx's FloatColors class. I can't recall where I got the original HSL(A) code
     * from, but there's a strong chance it was written by cypherdare/cyphercove for their color space comparison.
     * HSV and HSB are synonyms; it makes a little more sense to call the third channel brightness.
     *
     * @param h hue, from 0.0 to 1.0
     * @param s saturation, from 0.0 to 1.0
     * @param b brightness, from 0.0 to 1.0
     * @param a alpha, from 0.0 to 1.0
     * @return an RGBA8888-format int
     */
    public static int hsb2rgb(final float h, final float s, final float b, final float a) {
        float x = Math.min(Math.max(Math.abs(h * 6f - 3f) - 1f, 0f), 1f);
        float y = h + (2f / 3f);
        float z = h + (1f / 3f);
        y -= (int) y;
        z -= (int) z;
        y = Math.min(Math.max(Math.abs(y * 6f - 3f) - 1f, 0f), 1f);
        z = Math.min(Math.max(Math.abs(z * 6f - 3f) - 1f, 0f), 1f);
        return Color.rgba8888(b * MathUtils.lerp(1f, x, s), b * MathUtils.lerp(1f, y, s), b * MathUtils.lerp(1f, z, s), a);
    }

    /**
     * Converts the four RGBA components, each in the 0.0 to 1.0 range, to an int in HSBA/HSVA format (hue,
     * saturation, brightness/value, alpha). This format is exactly like RGBA8888 but treats what would normally be red
     * as hue, green as saturation, and blue as brightness/value; alpha is the same. HSV and HSB are synonyms; it makes
     * a little more sense to call the third channel brightness.
     *
     * @param r red, from 0.0 to 1.0
     * @param g green, from 0.0 to 1.0
     * @param b blue, from 0.0 to 1.0
     * @param a alpha, from 0.0 to 1.0
     * @return an "HSBA/HSVA-format" int
     */
    public static int rgb2hsb(final float r, final float g, final float b, final float a) {
        float v = Math.max(Math.max(r, g), b);
        float n = Math.min(Math.min(r, g), b);
        float c = v - n;
        float h;
        if (c == 0) h = 0f;
        else if (v == r) h = ((g - b) / c) / 6f;
        else if (v == g) h = ((b - r) / c + 2f) / 6f;
        else h = ((r - g) / c + 4f) / 6f;
        return Color.rgba8888(h, v == 0 ? 0f : c / v, v, a);
    }

    /**
     * Given a packed int color and a channel value from 0 to 3, gets the value of that channel as a float from 0.0f
     * to 1.0f . Channel 0 refers to R in RGBA8888 and H in {@link #rgb2hsl(float, float, float, float) HSLA} ints,
     * channel 1 refers to G or S, 2 refers to B or L, and 3 always refers to A.
     *
     * @param color   a packed int color in any 32-bit, 4-channel format
     * @param channel which channel to access, as an index from 0 to 3 inclusive
     * @return the non-packed float value of the requested channel, from 0.0f to 1.0f inclusive
     */
    public static float channel(int color, int channel) {
        return (color >>> 24 - ((channel & 3) << 3) & 255) / 255f;
    }

    /**
     * Given a packed int color and a channel value from 0 to 3, gets the value of that channel as an int from 0 to
     * 255 . Channel 0 refers to R in RGBA8888 and H in {@link #rgb2hsl(float, float, float, float) HSLA} ints,
     * channel 1 refers to G or S, 2 refers to B or L, and 3 always refers to A.
     *
     * @param color   a packed int color in any 32-bit, 4-channel format
     * @param channel which channel to access, as an index from 0 to 3 inclusive
     * @return the int value of the requested channel, from 0 to 255 inclusive
     */
    public static float channelInt(int color, int channel) {
        return (color >>> 24 - ((channel & 3) << 3) & 255);
    }

    /**
     * Interpolates from the RGBA8888 int color start towards end by change. Both start and end should be RGBA8888
     * ints, and change can be between 0f (keep start) and 1f (only use end). This is a good way to reduce allocations
     * of temporary Colors.
     *
     * @param s      the starting color as a packed int
     * @param e      the end/target color as a packed int
     * @param change how much to go from start toward end, as a float between 0 and 1; higher means closer to end
     * @return an RGBA8888 int that represents a color between start and end
     */
    public static int lerpColors(final int s, final int e, final float change) {
        final int
                sR = (s & 0xFF), sG = (s >>> 8) & 0xFF, sB = (s >>> 16) & 0xFF, sA = s >>> 25 & 0x7F,
                eR = (e & 0xFF), eG = (e >>> 8) & 0xFF, eB = (e >>> 16) & 0xFF, eA = e >>> 25 & 0x7F;
        return (((int) (sR + change * (eR - sR)) & 0xFF)
                | (((int) (sG + change * (eG - sG)) & 0xFF) << 8)
                | (((int) (sB + change * (eB - sB)) & 0xFF) << 16)
                | (((int) (sA + change * (eA - sA)) & 0x7F) << 25));
    }

    /**
     * Given several colors, this gets an even mix of all colors in equal measure.
     * If {@code colors} is null or has no items, this returns 256 (a transparent placeholder used by
     * {@link com.github.tommyettinger.textra.ColorLookup} for "no color found").
     * This is mostly useful in conjunction with {@link com.badlogic.gdx.utils.IntArray}, using its {@code items}
     * for colors, typically 0 for offset, and its {@code size} for size.
     * @param colors an array of RGBA8888 int colors; all should use the same color space
     * @param offset the index of the first item in {@code colors} to use
     * @param size how many items from {@code colors} to use
     * @return an even mix of all colors given, as an RGBA8888 int color
     */
    public static int mix(int[] colors, int offset, int size) {
        if(colors == null || colors.length < offset + size || offset < 0 || size <= 0)
            return 256; // transparent super-dark-blue, used to indicate "not found"
        int result = colors[offset];
        for (int i = offset + 1, o = offset + size, denom = 2; i < o; i++, denom++) {
            result = lerpColors(result, colors[i], 1f / denom);
        }
        return result;
    }

    /**
     * Interpolates from the int color start towards white by change. While change should be between 0f (return
     * start as-is) and 1f (return white), start should be an RGBA8888 color.
     * This is a good way to reduce allocations of temporary Colors, and is a little more efficient and clear than
     * using {@link #lerpColors(int, int, float)} to lerp towards
     * white. Unlike {@link #lerpColors(int, int, float)}, this keeps the alpha of start as-is.
     * @see #darken(int, float) the counterpart method that darkens an int color
     * @param start the starting color as an RGBA8888 int
     * @param change how much to go from start toward white, as a float between 0 and 1; higher means closer to white
     * @return an RGBA8888 int that represents a color between start and white
     */
    public static int lighten(final int start, final float change) {
        final int r = start >>> 24, g = start >>> 16 & 0xFF, b = start >>> 8 & 0xFF,
                a = start & 0x000000FE;
        return  ((int) (r + (0xFF - r) * change) & 0xFF) << 24 |
                ((int) (g + (0xFF - g) * change) & 0xFF) << 16 |
                ((int) (b + (0xFF - b) * change) & 0xFF) << 8 |
                a;
    }

    /**
     * Interpolates from the int color start towards black by change. While change should be between 0f (return
     * start as-is) and 1f (return black), start should be an RGBA8888 color.
     * This is a good way to reduce allocations of temporary Colors, and is a little more efficient and clear than
     * using {@link #lerpColors(int, int, float)} to lerp towards
     * black. Unlike {@link #lerpColors(int, int, float)}, this keeps the alpha of start as-is.
     * @see #lighten(int, float) the counterpart method that lightens an int color
     * @param start the starting color as an RGBA8888 int
     * @param change how much to go from start toward black, as a float between 0 and 1; higher means closer to black
     * @return an RGBA8888 int that represents a color between start and black
     */
    public static int darken(final int start, final float change) {
        final int r = start >>> 24, g = start >>> 16 & 0xFF, b = start >>> 8 & 0xFF,
                a = start & 0x000000FE;
        final float ch = 1f - change;
        return  ((int) (r * ch) & 0xFF) << 24 |
                ((int) (g * ch) & 0xFF) << 16 |
                ((int) (b * ch) & 0xFF) << 8 |
                a;
    }

    /**
     * Brings the chromatic components of {@code start} closer to grayscale by {@code change} (desaturating them). While
     * change should be between 0f (return start as-is) and 1f (return fully gray), start should be an RGBA8888 int
     * color. This leaves alpha alone.
     * <br>
     * <a href="http://www.graficaobscura.com/matrix/index.html">The algorithm used is from here</a>.
     * @see #enrich(int, float) the counterpart method that makes an int color more saturated
     * @param start the starting color as an RGBA8888 int
     * @param change how much to change start to a desaturated color, as a float between 0 and 1; higher means a less saturated result
     * @return an RGBA8888 int that represents a color between start and a desaturated color
     */
    public static int dullen(final int start, final float change) {
        final float rc = 0.32627f, gc = 0.3678f, bc = 0.30593001f;
        final int r = start >>> 24, g = start >>> 16 & 0xFF, b = start >>> 8 & 0xFF,
                a = start & 0x000000FE;
        final float ch = 1f - change, rw = change * rc, gw = change * gc, bw = change * bc;
        return  (int) Math.min(Math.max(r * (rw+ch) + g * rw + b * rw, 0), 255) << 24 |
                (int) Math.min(Math.max(r * gw + g * (gw+ch) + b * gw, 0), 255) << 16 |
                (int) Math.min(Math.max(r * bw + g * bw + b * (bw+ch), 0), 255) << 8  |
                a;
    }

    /**
     * Pushes the chromatic components of {@code start} away from grayscale by change (saturating them). While change
     * should be between 0f (return start as-is) and 1f (return maximally saturated), start should be an RGBA8888 int
     * color.
     * <br>
     * <a href="http://www.graficaobscura.com/matrix/index.html">The algorithm used is from here</a>.
     * @see #dullen(int, float) the counterpart method that makes an int color less saturated
     * @param start the starting color as an RGBA8888 int
     * @param change how much to change start to a saturated color, as a float between 0 and 1; higher means a more saturated result
     * @return an RGBA8888 int that represents a color between start and a saturated color
     */
    public static int enrich(final int start, final float change) {
        final float rc = -0.32627f, gc = -0.3678f, bc = -0.30593001f;
        final int r = start >>> 24, g = start >>> 16 & 0xFF, b = start >>> 8 & 0xFF,
                a = start & 0x000000FE;
        final float ch = 1f + change, rw = change * rc, gw = change * gc, bw = change * bc;
        return  (int) Math.min(Math.max(r * (rw+ch) + g * rw + b * rw, 0), 255) << 24 |
                (int) Math.min(Math.max(r * gw + g * (gw+ch) + b * gw, 0), 255) << 16 |
                (int) Math.min(Math.max(r * bw + g * bw + b * (bw+ch), 0), 255) << 8  |
                a;
    }

    /**
     * Gets an "offset color" for the original {@code color} where high red, green, or blue channels become low values
     * in that same channel, and vice versa, then blends the original with that offset, using more of the offset if
     * {@code power} is higher (closer to 1.0f). It is usually fine for {@code power} to be 0.5f .
     * @param color the original color as an RGBA8888 int
     * @param power between 0.0f and 1.0f, this is how heavily the offset color should factor in to the result
     * @return a mix between {@code color} and its offset, with higher {@code power} using more of the offset
     */
    public static int offset(final int color, float power) {
        return lerpColors(color, color ^ 0x80808000, power);
    }

    /**
     * Given an RGBA8888 or HSLA color as an int, this multiplies the alpha of that color by multiplier and returns
     * another int color of the same format passed in. This clamps the alpha if it would go below 0 or above 255, and
     * leaves the RGB or HSL channels alone.
     *
     * @param color      an RGBA8888 or HSLA color
     * @param multiplier a multiplier to apply to color's alpha
     * @return another color of the same format as the one given, with alpha multiplied
     */
    public static int multiplyAlpha(int color, float multiplier) {
        return (color & 0xFFFFFF00) | Math.min(Math.max((int) ((color & 0xFF) * multiplier), 0), 255);
    }

    /**
     * Given any purely-non-null 2D int array representing RGBA or HSLA colors, this multiplies the alpha channel of
     * each color by multiplier, modifying the given array, and returns the changed array for chaining. This uses
     * {@link #multiplyAlpha(int, float)} internally, so its documentation applies.
     *
     * @param colors     a 2D int array of RGBA or HSLA colors, none of which can include null arrays
     * @param multiplier a multiplier to apply to each color's alpha
     * @return colors, after having each item's alpha multiplied
     */
    public static int[][] multiplyAllAlpha(int[][] colors, float multiplier) {
        for (int x = 0; x < colors.length; x++) {
            for (int y = 0; y < colors[x].length; y++) {
                colors[x][y] = multiplyAlpha(colors[x][y], multiplier);
            }
        }
        return colors;
    }

    private static final IntArray mixing = new IntArray(4);

    /**
     * Parses a color description and returns the approximate color it describes, as an RGBA8888 int color.
     * Color descriptions consist of one or more alphabetical words, separated by non-alphabetical characters (typically
     * spaces and/or hyphens, though the underscore is treated as a letter). Any word that is the name of a color in
     * {@link Palette} will be looked up in {@link Palette#NAMED} and tracked; if there is more than one of these color
     * name words, the colors will be mixed using {@link #mix(int[], int, int)}, or if there is just one color name
     * word, then the corresponding color will be used. The special adjectives "light" and "dark" change the lightness
     * of the described color; likewise, "rich" and "dull" change the saturation (how different the color is from
     * grayscale). All of these adjectives can have "-er" or "-est" appended to make their effect twice or three times
     * as strong. Technically, the chars appended to an adjective don't matter, only their count, so "lightaa" is the
     * same as "lighter" and "richcat" is the same as "richest". There's an unofficial fourth level as well, used when
     * any 4 characters are appended to an adjective (as in "darkmost"); it has four times the effect of the original
     * adjective. If a color name or adjective is invalid, it is not considered; if the description is empty or fully
     * invalid, this returns the RGBA8888 int value 256 (used as a placeholder by
     * {@link com.github.tommyettinger.textra.ColorLookup}).
     * <br>
     * Examples of valid descriptions include "blue", "dark green", "DULLER RED", "peach pink", "indigo purple mauve",
     * "lightest, richer apricot-olive", and "LIGHTMOST rich MAROON indigo".
     * @param description a color description, as a lower-case String matching the above format
     * @return an RGBA8888 int color as described
     */
    public static int describe(final String description) {
        float lightness = 0f, saturation = 0f;
        final String[] terms = description.split("[^a-zA-Z_]+");
        mixing.clear();
        for(String term : terms) {
            if (term == null || term.isEmpty()) continue;
            final int len = term.length();
            switch (term.charAt(0)) {
                case 'L':
                case 'l':
                    if (len > 2 && (term.charAt(2) == 'g' || term.charAt(2) == 'G')) {
                        switch (len) {
                            case 9:
                                lightness += 0.20f;
                            case 8:
                                lightness += 0.20f;
                            case 7:
                                lightness += 0.20f;
                            case 5:
                                lightness += 0.20f;
                                break;
                        }
                    } else {
                        mixing.add(NAMED.get(term, 256));
                    }
                    break;
                case 'R':
                case 'r':
                    if (len > 1 && (term.charAt(1) == 'i' || term.charAt(1) == 'I')) {
                        switch (len) {
                            case 8:
                                saturation += 0.200f;
                            case 7:
                                saturation += 0.200f;
                            case 6:
                                saturation += 0.200f;
                            case 4:
                                saturation += 0.200f;
                                break;
                        }
                    } else {
                        mixing.add(NAMED.get(term, 256));
                    }
                    break;
                case 'D':
                case 'd':
                    if (len > 1 && (term.charAt(1) == 'a' || term.charAt(1) == 'A')) {
                        switch (len) {
                            case 8:
                                lightness -= 0.20f;
                            case 7:
                                lightness -= 0.20f;
                            case 6:
                                lightness -= 0.20f;
                            case 4:
                                lightness -= 0.20f;
                                break;
                        }
                    } else if (len > 1 && (term.charAt(1) == 'u' || term.charAt(1) == 'U')) {
                        switch (len) {
                            case 8:
                                saturation -= 0.200f;
                            case 7:
                                saturation -= 0.200f;
                            case 6:
                                saturation -= 0.200f;
                            case 4:
                                saturation -= 0.200f;
                                break;
                        }
                    } else {
                        mixing.add(NAMED.get(term, 256));
                    }
                    break;
                default:
                    mixing.add(NAMED.get(term, 256));
                    break;
            }
        }

        int result = mix(mixing.items, 0, mixing.size);
        if(result == 256) return result;

        if(lightness > 0) result = lighten(result, lightness);
        else if(lightness < 0) result = darken(result, -lightness);

        if(saturation > 0) result = enrich(result, saturation);
        else if(saturation < 0) result = dullen(result, -saturation);

        return result;
    }
}
