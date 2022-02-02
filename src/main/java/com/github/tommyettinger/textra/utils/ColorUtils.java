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
 * Just has methods to convert from HSL colors to RGB and back again, for hue-changing effects mainly.
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
    public static float rgb2hsl(final float r, final float g, final float b, final float a) {
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

}
