/*
 * Copyright (c) 2022 See AUTHORS file.
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

import java.util.Random;

public final class StringUtils {
    private StringUtils() {}

    public static String join(CharSequence delimiter, CharSequence... items) {
        if(items == null || items.length == 0)
            return "";
        StringBuilder sb = new StringBuilder();
        sb.append(items[0]);
        for (int i = 1; i < items.length; i++) {
            sb.append(delimiter).append(items[i]);
        }
        return sb.toString();
    }

    /**
     * Shuffles the words in {@code text} using {@link MathUtils#random}, joins them with a space as the delimiter, and
     * returns that String.
     * @param text a String containing typically many whitespace-separated words
     * @return text with its words shuffled randomly
     */
    public static String shuffleWords(String text) {
        return shuffleWords(text, MathUtils.random);
    }
    /**
     * Shuffles the words in {@code text} using the given Random generator, joins them with a space as the delimiter,
     * and returns that String. The generator can be seeded to get replicable results.
     * @param text a String containing typically many whitespace-separated words
     * @return text with its words shuffled randomly
     */
    public static String shuffleWords(String text, Random generator) {
        String[] items = text.split("\\s+");
        int length = items.length;
        for (int i = length - 1; i > 0; i--) {
            int ii = generator.nextInt(i + 1);
            String temp = items[i];
            items[i] = items[ii];
            items[ii] = temp;
        }
        return join(" ", items);
    }
}
