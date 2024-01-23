/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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
import regexodus.Category;

import java.util.Random;

public final class StringUtils {
    private static final int[] hexCodes = new int[]
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                    0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1,
                    -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                    -1, 10, 11, 12, 13, 14, 15};

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
     *
     * @param cs    a CharSequence, such as a String, containing only hex digits with an optional sign (no 0x at the start)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this stops after 16 characters if end is too large)
     * @return the long that cs represents
     */
    public static long longFromHex(final CharSequence cs, final int start, int end) {
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
     * Reads in a CharSequence containing only hex digits (only 0-9, a-f, and A-F) with an optional sign at the start
     * and returns the long they represent, reading at most 16 characters (17 if there is a sign) and returning the
     * result if valid, or 0 if nothing could be read. The leading sign can be '+' or '-' if present. This can also
     * represent negative numbers as they are printed by such methods as String.format given %X in the formatting
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
     *
     * @param cs    a CharSequence, such as a String, containing only hex digits with an optional sign (no 0x at the start)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this stops after 16 characters if end is too large)
     * @return the long that cs represents
     */
    public static int intFromHex(final CharSequence cs, final int start, int end) {
        int len, h, lim = 8;
        if (cs == null || start < 0 || end <= 0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if (c == '-') {
            len = -1;
            h = 0;
            lim = 9;
        } else if (c == '+') {
            len = 1;
            h = 0;
            lim = 9;
        } else if (c > 102 || (h = hexCodes[c]) < 0)
            return 0;
        else {
            len = 1;
        }
        int data = h;
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
     *
     * @param cs    a CharSequence, such as a String, containing only digits 0-9 with an optional sign
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this will stop early if it encounters any invalid char, or 10 digits have been read, not including sign)
     * @return the int that cs represents
     */
    public static int intFromDec(final CharSequence cs, final int start, int end) {
        int len, h, lim = 10;
        if (cs == null || start < 0 || end <= 0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if (c == '-') {
            len = -1;
            lim = 11;
            h = 0;
        } else if (c == '+') {
            len = 1;
            lim = 11;
            h = 0;
        } else if (c > 102 || (h = hexCodes[c]) < 0 || h > 9)
            return 0;
        else {
            len = 1;
        }
        int data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if ((c = cs.charAt(i)) > 102 || (h = hexCodes[c]) < 0 || h > 9)
                return data * len;
            data = data * 10 + h;
        }
        return data * len;
    }

    /**
     * Reads in a CharSequence containing only decimal digits (0-9) with an optional sign at the start and an optional
     * decimal point anywhere in the CharSequence, and returns the float they represent, reading until it encounters the
     * end of the sequence or any invalid char, then returning the result if valid, or 0 if nothing could be read.
     * The leading sign can be '+' or '-' if present.
     * <br>
     * This is somewhat similar to the JDK's {@link Float#parseFloat(String)} method, but this also supports
     * CharSequence data instead of just String data, and allows specifying a start and end, but doesn't support
     * scientific notation or hexadecimal float notation. This doesn't throw on invalid input, either, instead returning
     * 0 if the first char is not a decimal digit, or stopping the parse process early if a non-decimal-digit char is
     * read before end is reached. If the parse is stopped early, this behaves as you would expect for a number with
     * fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a CharSequence, such as a String, containing digits 0-9 with an optional sign and decimal point
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this will stop early if it encounters any invalid char)
     * @return the float that cs represents
     */
    public static float floatFromDec(final CharSequence cs, final int start, int end) {
        int len, h;
        float decimal = 1f;
        boolean foundPoint = false;
        if (cs == null || start < 0 || end <= 0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if (c == '-') {
            len = -1;
            h = 0;
        } else if (c == '+') {
            len = 1;
            h = 0;
        } else if (c > 102 || (h = hexCodes[c]) < 0 || h > 9)
            return 0;
        else {
            len = 1;
        }
        int data = h;
        for (int i = start + 1; i < end; i++) {
            c = cs.charAt(i);
            if(c == '.') {
                foundPoint = true;
                continue;
            }
            if (c > 102 || (h = hexCodes[c]) < 0 || h > 9)
                return data * len / decimal;
            if(foundPoint){
                decimal *= 10f;
            }
            data = data * 10 + h;
        }
        return data * len / decimal;
    }

    /**
     * Returns the next index just after the end of {@code search} starting at {@code from} in {@code text}.
     * If {@code search} cannot be found at or after {@code from}, this returns {@code text.length()}.
     * <br>
     * This is used heavily by the code that loads FNT files, but isn't used much elsewhere.
     *
     * @param text the String to look inside
     * @param search the String to look for in text
     * @param from the index to start searching in text
     * @return the index in text that is just after the next occurrence of search, starting at from
     */
    public static int indexAfter(String text, String search, int from) {
        return ((from = text.indexOf(search, from)) < 0 ? text.length() : from + search.length());
    }

    /**
     * Like {@link String#substring(int, int)} but returns "" instead of throwing any sort of Exception.
     *
     * @param source     the String to get a substring from
     * @param beginIndex the first index, inclusive; will be treated as 0 if negative
     * @param endIndex   the index after the last character (exclusive); if negative this will be source.length()
     * @return the substring of source between beginIndex and endIndex, or "" if any parameters are null/invalid
     */
    public static String safeSubstring(String source, int beginIndex, int endIndex) {
        if (source == null || source.isEmpty()) return "";
        if (beginIndex < 0) beginIndex = 0;
        if (endIndex < 0 || endIndex > source.length()) endIndex = source.length();
        if (beginIndex >= endIndex) return "";
        return source.substring(beginIndex, endIndex);
    }

    /**
     * Returns true if {@code c} is a lower-case letter, or false otherwise.
     * Similar to {@link Character#isLowerCase(char)}, but should actually work on GWT.
     *
     * @param c a char to check
     * @return true if c is a lower-case letter, or false otherwise.
     */
    public static boolean isLowerCase(char c) {
        return Category.Ll.contains(c);
    }

    /**
     * Returns true if {@code c} is an upper-case letter, or false otherwise.
     * Similar to {@link Character#isUpperCase(char)}, but should actually work on GWT.
     *
     * @param c a char to check
     * @return true if c is an upper-case letter, or false otherwise.
     */
    public static boolean isUpperCase(char c) {
        return Category.Lu.contains(c);
    }
}
