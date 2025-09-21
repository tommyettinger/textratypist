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
