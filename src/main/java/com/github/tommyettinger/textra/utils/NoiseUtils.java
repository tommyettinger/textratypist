package com.github.tommyettinger.textra.utils;

import com.badlogic.gdx.utils.NumberUtils;

/**
 * Some 1D noise methods to be used when an effect needs smooth but random changes.
 */
public class NoiseUtils {
    /**
     * Quilez' 1D noise, with some changes to work on the CPU. Takes a distance x and any int seed, and produces a
     * smoothly-changing value as x goes up or down and seed stays the same. Uses a quartic curve.
     * @param x should go up and/or down steadily and by small amounts (less than 1.0, certainly)
     * @param seed should stay the same for a given curve
     * @return a noise value between -1.0 and 1.0
     */
    public static float noise1D(float x, final int seed) {
        x += seed * 0x1p-24f;
        final int xFloor = x >= 0f ? (int) x : (int) x - 1,
                rise = 1 - ((x >= 0f ? (int) (x + x) : (int) (x + x) - 1) & 2);
        x -= xFloor;
        final float h = NumberUtils.intBitsToFloat((int)((seed + xFloor ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L >>> 41) | 0x42000000) - 48f;
        x *= x - 1f;
        return rise * x * x * h;
    }

    /**
     * Just gets two octaves of {@link #noise1D(float, int)}; still has a range of -1 to 1.
     * @param x should go up and/or down steadily and by small amounts (less than 1.0, certainly)
     * @param seed should stay the same for a given curve
     * @return a noise value between -1.0 and 1.0
     */
    public static float octaveNoise1D(float x, int seed){
        return (noise1D(x, seed) * 2f + noise1D(x * 1.9f, ~seed)) * 0.3333333333333333f;
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
//        return (noise1D(x, seed) * 2.0 + noise1D(x * 2.0, ~seed)) * 0.3333333333333333;
//    }
