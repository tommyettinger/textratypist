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

package com.github.tommyettinger.textra.utils;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.NumberUtils;

import static regexodus.Compatibility.imul;

/**
 * Some 1D noise methods to be used when an effect needs smooth but random changes.
 */
public class NoiseUtils {
    /**
     * Quilez' 1D noise, with some changes to work on the CPU. Takes a distance x and any int seed, and produces a
     * smoothly-changing value as x goes up or down and seed stays the same. Uses a quartic curve. You will often want
     * to prefer {@link #octaveNoise1D(float, int)}, which uses this method, instead of directly calling noise1D(),
     * because octaveNoise1D() looks much more natural.
     * <br>
     * The distance ({@code x}) should be between -8192 and 1073733631 for this to return correct results.
     * Because floats incur precision loss earlier than 1073733631, the actual upper bound is lower. The limit of
     * -8192 comes from how this uses {@link MathUtils#floor(float)} internally on {@code x + x}.
     * <br>
     * Credit to Inigo Quilez, who posted <a href="https://www.shadertoy.com/view/3sd3Rs">this ShaderToy</a> in 2019
     * under the MIT license. There are changes here in the way it uses a seed and gets the {@code h} value, but the
     * core idea is the same, using a symmetrical curve to minimize the number of hashes needed per point.
     *
     * @param x    should go up and/or down steadily and by small amounts (less than 1.0, certainly)
     * @param seed should stay the same for a given curve
     * @return a noise value between -1.0 and 1.0
     */
    public static float noise1D(float x, final int seed) {
        x += seed * 0x1p-24f;
        final int xFloor = MathUtils.floor(x),
                rise = 1 - (MathUtils.floor(x + x) & 2);
        x -= xFloor;
        // gets a random float between -16 and 16. Magic.
        final float h = NumberUtils.intBitsToFloat((int) ((seed + (xFloor ^ 0x9E3779B97F4A7C15L)) * 0xD1B54A32D192ED03L >>> 41) | 0x42000000) - 48f;
        x *= x - 1f;
        return rise * x * x * h;
    }

    /**
     * A more natural 1D noise that uses two octaves of {@link #noise1D(float, int)}; still has a range of -1 to 1.
     * <br>
     * Because this uses a higher frequency for one octave of noise1D(), the limit on x is more restricting; x should be
     * no less than -4096 here, though there may be precision quirks for other inputs near that range, and excessively
     * large inputs will naturally lose some precision due to how floats work.
     *
     * @param x    should go up and/or down steadily and by small amounts (less than 1.0, certainly)
     * @param seed should stay the same for a given curve
     * @return a noise value between -1.0 and 1.0
     */
    public static float octaveNoise1D(float x, int seed) {
        return noise1D(x, seed) * 0.6666667f + noise1D(x * 1.9f, ~seed) * 0.33333333f;
    }

    /**
     * Sway smoothly using bicubic interpolation between 4 points (the two integers before x and the two after).
     * This pretty much never produces steep changes between peaks and valleys; this may make it more useful for things
     * like generating terrain that can be walked across in a side-scrolling game.
     * <br>
     * This should produce extreme values rarely compared to {@link #noise1D(float, int)}, but can produce flatter high
     * or low results for a longer span of time (plateaus, for instance).
     *
     * @param x a distance traveled; should change by less than 1 between calls, and should be less than about 10000
     * @param seed any int
     * @return a smoothly-interpolated swaying value between -1 and 1, both exclusive
     */
    public static float bicubicNoise1D(float x, int seed)
    {
        // int fast floor, from libGDX; 16384 is 2 to the 14, or 0x1p14, or 0x4000
        final int floor = ((int)(x + 16384.0) - 16384);
        // fancy XOR-rotate-rotate is a way to mix bits both up and down without multiplication.
        seed = (seed ^ (seed << 11 | seed >>> 21) ^ (seed << 25 | seed >>> 7)) + floor;
        // we use a different technique here, relative to other wobble methods.
        // to avoid frequent multiplication and replace it with addition by constants, we track 3 variables, each of
        // which updates with a different large, negative int increment. when we want to get a result, we just XOR
        // m, n, and o, and use mainly the upper bits (by multiplying by a tiny fraction).
        // imul is from the Compatibility class from RegExodus, which ensures imul uses the Math.imul() method on GWT.
        // if you don't ever target GWT, you could replace the next 3 lines with normal multiplications on seed.
        final int m = imul(seed, 0xD1B54A33);
        final int n = imul(seed, 0xABC98383);
        final int o = imul(seed, 0x8CB92BA7);

        final float a = (m ^ n ^ o);
        final float b = (m + 0xD1B54A33 ^ n + 0xABC98383 ^ o + 0x8CB92BA7);
        final float c = (m + 0xA36A9466 ^ n + 0x57930716 ^ o + 0x1972574E);
        final float d = (m + 0x751FDE99 ^ n + 0x035C8A99 ^ o + 0xA62B82F5);

        // get the fractional part of x.
        x -= floor;
        // this is bicubic interpolation, inlined
        final float p = (d - c) - (a - b);
        // 3.1044084E-10f , or 0x1.555555p-32f , is just inside {@code -2f/3f/Integer.MIN_VALUE} .
        // it gets us about as close as we can go to 1.0 .
        return (x * (x * x * p + x * (a - b - p) + c - a) + b) * 3.1044084E-10f;
    }

    /**
     * A more natural 1D noise that uses two octaves of {@link #bicubicNoise1D(float, int)}; has a range of -1 to 1.
     * <br>
     * Because this uses a higher frequency for one octave of bicubicNoise1D(), the limit on x is more restricting; x
     * should be no less than -4096 here, though there may be precision quirks for other inputs near that range, and
     * excessively large inputs will naturally lose some precision due to how floats work.
     *
     * @param x    should go up and/or down steadily and by small amounts (less than 1.0, certainly)
     * @param seed should stay the same for a given curve
     * @return a noise value between -1.0 and 1.0
     */
    public static float octaveBicubicNoise1D(float x, int seed) {
        return bicubicNoise1D(x, seed) * 0.6666667f + bicubicNoise1D(x * 1.9f, ~seed) * 0.33333333f;
    }

}

//// double versions
//    /**
//     * Quilez' 1D noise, with some changes to work on the CPU. Takes a distance x and any int seed, and produces a
//     * smoothly-changing value as x goes up or down and seed stays the same. Uses a quartic curve.
//     * @param x should go up and/or down steadily and by small amounts (less than 1.0, certainly)
//     * @param seed should stay the same for a given curve
//     * @return a noise value between -1.0 and 1.0
//     */
//    public static double noise1D(double x, final int seed) {
//        x += seed * 0x1p-24;
//        final long xFloor = x >= 0.0 ? (long) x : (long) x - 1L,
//                rise = 1L - ((x >= 0.0 ? (long) (x + x) : (long) (x + x) - 1L) & 2L);
//        x -= xFloor;
//        final double h = NumberUtils.longBitsToDouble((seed + xFloor ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L >>> 12 | 0x4040000000000000L) - 48.0;
//        x *= x - 1.0;
//        return rise * x * x * h;
//    }
//
//    /**
//     * Just gets two octaves of {@link #noise1D(double, int)}; still has a range of -1 to 1.
//     * @param x should go up and/or down steadily and by small amounts (less than 1.0, certainly)
//     * @param seed should stay the same for a given curve
//     * @return a noise value between -1.0 and 1.0
//     */
//    public static double octaveNoise1D(double x, int seed){
//        return noise1D(x, seed) * 0.6666666666666666 + noise1D(x * 1.9, ~seed) * 0.3333333333333333;
//    }
