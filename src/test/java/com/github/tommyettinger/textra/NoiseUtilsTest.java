package com.github.tommyettinger.textra;

import com.github.tommyettinger.textra.utils.NoiseUtils;

public class NoiseUtilsTest {
    public static void main(String[] args) {
        int seed = 11;
        System.out.println("octaveNoise1D():");
        // when x is exactly a large even integer, the result seems to be 0 very often...
        System.out.printf("x=%f, seed=%010d: %f\n", 1100.0f, seed, NoiseUtils.octaveNoise1D(1100.0f, seed));
        System.out.printf("x=%f, seed=%010d: %f\n", 1100.1f, seed, NoiseUtils.octaveNoise1D(1100.1f, seed));
        System.out.printf("x=%f, seed=%010d: %f\n", 1100.2f, seed, NoiseUtils.octaveNoise1D(1100.2f, seed));
        System.out.printf("x=%f, seed=%010d: %f\n", 1100.3f, seed, NoiseUtils.octaveNoise1D(1100.3f, seed));
        System.out.printf("x=%f, seed=%010d: %f\n", 1100.4f, seed, NoiseUtils.octaveNoise1D(1100.4f, seed));
        System.out.printf("x=%f, seed=%010d: %f\n", 1100.5f, seed, NoiseUtils.octaveNoise1D(1100.5f, seed));
        System.out.printf("x=%f, seed=%010d: %f\n", 1100.6f, seed, NoiseUtils.octaveNoise1D(1100.6f, seed));
        System.out.printf("x=%f, seed=%010d: %f\n", 1100.7f, seed, NoiseUtils.octaveNoise1D(1100.7f, seed));
        System.out.printf("x=%f, seed=%010d: %f\n", 1100.8f, seed, NoiseUtils.octaveNoise1D(1100.8f, seed));
        System.out.printf("x=%f, seed=%010d: %f\n", 1100.9f, seed, NoiseUtils.octaveNoise1D(1100.9f, seed));

        System.out.printf("x=%f, seed=%010d: %f\n", 1101.0f, seed, NoiseUtils.octaveNoise1D(1101.0f, seed));
        System.out.printf("x=%f, seed=%010d: %f\n", 1101.1f, seed, NoiseUtils.octaveNoise1D(1101.1f, seed));
        System.out.printf("x=%f, seed=%010d: %f\n", 1101.2f, seed, NoiseUtils.octaveNoise1D(1101.2f, seed));
        System.out.printf("x=%f, seed=%010d: %f\n", 1101.3f, seed, NoiseUtils.octaveNoise1D(1101.3f, seed));


        System.out.println("octaveBicubicNoise1D():");
        System.out.printf("x=%f, seed=%010d: %f\n", 1100.0f, seed, NoiseUtils.octaveBicubicNoise1D(1100.0f, seed));
        System.out.printf("x=%f, seed=%010d: %f\n", 1100.1f, seed, NoiseUtils.octaveBicubicNoise1D(1100.1f, seed));
        System.out.printf("x=%f, seed=%010d: %f\n", 1100.2f, seed, NoiseUtils.octaveBicubicNoise1D(1100.2f, seed));
        System.out.printf("x=%f, seed=%010d: %f\n", 1100.3f, seed, NoiseUtils.octaveBicubicNoise1D(1100.3f, seed));
        System.out.printf("x=%f, seed=%010d: %f\n", 1100.4f, seed, NoiseUtils.octaveBicubicNoise1D(1100.4f, seed));
        System.out.printf("x=%f, seed=%010d: %f\n", 1100.5f, seed, NoiseUtils.octaveBicubicNoise1D(1100.5f, seed));
        System.out.printf("x=%f, seed=%010d: %f\n", 1100.6f, seed, NoiseUtils.octaveBicubicNoise1D(1100.6f, seed));
        System.out.printf("x=%f, seed=%010d: %f\n", 1100.7f, seed, NoiseUtils.octaveBicubicNoise1D(1100.7f, seed));
        System.out.printf("x=%f, seed=%010d: %f\n", 1100.8f, seed, NoiseUtils.octaveBicubicNoise1D(1100.8f, seed));
        System.out.printf("x=%f, seed=%010d: %f\n", 1100.9f, seed, NoiseUtils.octaveBicubicNoise1D(1100.9f, seed));

        System.out.printf("x=%f, seed=%010d: %f\n", 1101.0f, seed, NoiseUtils.octaveBicubicNoise1D(1101.0f, seed));
        System.out.printf("x=%f, seed=%010d: %f\n", 1101.1f, seed, NoiseUtils.octaveBicubicNoise1D(1101.1f, seed));
        System.out.printf("x=%f, seed=%010d: %f\n", 1101.2f, seed, NoiseUtils.octaveBicubicNoise1D(1101.2f, seed));
        System.out.printf("x=%f, seed=%010d: %f\n", 1101.3f, seed, NoiseUtils.octaveBicubicNoise1D(1101.3f, seed));

    }
}
