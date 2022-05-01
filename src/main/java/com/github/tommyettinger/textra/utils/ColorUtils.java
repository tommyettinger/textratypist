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
     * @param h hue, from 0.0 to 1.0
     * @param s saturation, from 0.0 to 1.0
     * @param l lightness, from 0.0 to 1.0
     * @param a alpha, from 0.0 to 1.0
     * @return an RGBA8888-format int
     */
    public static int hsl2rgb(final float h, final float s, final float l, final float a){
        float x = Math.min(Math.max(Math.abs(h * 6f - 3f) - 1f, 0f), 1f);
        float y = h + (2f / 3f);
        float z = h + (1f / 3f);
        y -= (int)y;
        z -= (int)z;
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
     * @param r red, from 0.0 to 1.0
     * @param g green, from 0.0 to 1.0
     * @param b blue, from 0.0 to 1.0
     * @param a alpha, from 0.0 to 1.0
     * @return an "HSLA-format" int
     */
    public static int rgb2hsl(final float r, final float g, final float b, final float a) {
        float x, y, z, w;
        if(g < b) {
            x = b;
            y = g;
            z = -1f;
            w = 2f / 3f;
        }
        else {
            x = g;
            y = b;
            z = 0f;
            w = -1f / 3f;
        }
        if(r < x) {
            z = w;
            w = r;
        }
        else {
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
     * @param h hue, from 0.0 to 1.0
     * @param s saturation, from 0.0 to 1.0
     * @param b brightness, from 0.0 to 1.0
     * @param a alpha, from 0.0 to 1.0
     * @return an RGBA8888-format int
     */
    public static int hsb2rgb(final float h, final float s, final float b, final float a){
        float x = Math.min(Math.max(Math.abs(h * 6f - 3f) - 1f, 0f), 1f);
        float y = h + (2f / 3f);
        float z = h + (1f / 3f);
        y -= (int)y;
        z -= (int)z;
        y = Math.min(Math.max(Math.abs(y * 6f - 3f) - 1f, 0f), 1f);
        z = Math.min(Math.max(Math.abs(z * 6f - 3f) - 1f, 0f), 1f);
        return Color.rgba8888(b * MathUtils.lerp(1f, x, s), b * MathUtils.lerp(1f, y, s), b * MathUtils.lerp(1f, z, s), a);
    }

    /**
     * Converts the four RGBA components, each in the 0.0 to 1.0 range, to an int in HSBA/HSVA format (hue,
     * saturation, brightness/value, alpha). This format is exactly like RGBA8888 but treats what would normally be red
     * as hue, green as saturation, and blue as brightness/value; alpha is the same. HSV and HSB are synonyms; it makes
     * a little more sense to call the third channel brightness.
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
        if(c == 0) h = 0f;
        else if(v == r) h = ((g - b) / c) / 6f;
        else if(v == g) h = ((b - r) / c + 2f) / 6f;
        else            h = ((r - g) / c + 4f) / 6f;
        return Color.rgba8888(h, v == 0 ? 0f : c / v, v, a);
    }

    /**
     * Given a packed int color and a channel value from 0 to 3, gets the value of that channel as a float from 0.0f
     * to 1.0f . Channel 0 refers to R in RGBA8888 and H in {@link #rgb2hsl(float, float, float, float) HSLA} ints,
     * channel 1 refers to G or S, 2 refers to B or L, and 3 always refers to A.
     * @param color a packed int color in any 32-bit, 4-channel format
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
     * @param color a packed int color in any 32-bit, 4-channel format
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
                sR = (s & 0xFF), sG = (s >>> 8) & 0xFF, sB = (s >>> 16) & 0xFF, sA = s >>> 24 & 0xFF,
                eR = (e & 0xFF), eG = (e >>> 8) & 0xFF, eB = (e >>> 16) & 0xFF, eA = e >>> 24 & 0xFF;
        return (((int) (sR + change * (eR - sR)) & 0xFF)
                | (((int) (sG + change * (eG - sG)) & 0xFF) << 8)
                | (((int) (sB + change * (eB - sB)) & 0xFF) << 16)
                | (((int) (sA + change * (eA - sA)) & 0xFF) << 24));
    }

    /**
     * Given an RGBA8888 or HSLA color as an int, this multiplies the alpha of that color by multiplier and returns
     * another int color of the same format passed in. This clamps the alpha if it would go below 0 or above 255, and
     * leaves the RGB or HSL channels alone.
     * @param color an RGBA8888 or HSLA color
     * @param multiplier a multiplier to apply to color's alpha
     * @return another color of the same format as the one given, with alpha multiplied
     */
    public static int multiplyAlpha(int color, float multiplier){
        return (color & 0xFFFFFF00) | Math.min(Math.max((int)((color & 0xFF) * multiplier), 0), 255);
    }

    /**
     * Given any purely-non-null 2D int array representing RGBA or HSLA colors, this multiplies the alpha channel of
     * each color by multiplier, modifying the given array, and returns the changed array for chaining. This uses
     * {@link #multiplyAlpha(int, float)} internally, so its documentation applies.
     * @param colors a 2D int array of RGBA or HSLA colors, none of which can include null arrays
     * @param multiplier a multiplier to apply to each color's alpha
     * @return colors, after having each item's alpha multiplied
     */
    public static int[][] multiplyAllAlpha(int[][] colors, float multiplier){
        for (int x = 0; x < colors.length; x++) {
            for (int y = 0; y < colors[x].length; y++) {
                colors[x][y] = multiplyAlpha(colors[x][y], multiplier);
            }
        }
        return colors;
    }

}
