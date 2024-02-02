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
     * An overly-permissive, but fast, way of looking up the numeric value of a hex digit provided as a char.
     * This does not use a table lookup. It will return garbage if not given a valid hex digit, but will not crash
     * or throw an Exception on any input. If you know the given digit is between 0 and 9 inclusive, this can also be
     * used to get the numeric value of that digit as decimal, rather than hexadecimal. You could instead use
     * {@code (c & 15)} or just {@code (c - '0')} in that case, though.
     * @param c a char that should really be a valid hex digit (matching the regex {@code [0-9A-Fa-f]})
     * @return the numeric value of the given digit char
     */
    public static int hexCode(final char c) {
        // this will be 0 when c is between 0-9, and 64 when c is any letter.
        final int h = (c & 64);
        // the bottom bits (going up to 15) are accurate for 0-9, but are 9 off for letters.
        // (64 >>> 3) is 8, and (64 >>> 6) is 1.
        return (c & 15) + (h >>> 3) + (h >>> 6);
    }

    /**
     * Converts the given number, which should be between 0 and 15 inclusive, to its corresponding hex digit as a char.
     * This does not use a table lookup. It will return garbage if not given a number in range, but will not crash
     * or throw an Exception on any input.
     * @param number an int that should really be between 0 (inclusive) and 15 (inclusive)
     * @return the hex digit that matches the given number
     */
    @SuppressWarnings("ShiftOutOfRange")
    public static char hexChar(final int number) {
        return (char)(number + 48 + (9 - number >>> -3));
    }

    /**
     * Converts the given number, which should be between 0 and 15 inclusive, to its corresponding hex digit as a char.
     * This does not use a table lookup. It will return garbage if not given a number in range, but will not crash
     * or throw an Exception on any input.
     * <br>
     * This overload only exists to ease conversion to hex digits when given a long input. Its body is the same as the
     * overload that takes an int, {@link #hexChar(int)}.
     * @param number an int that should really be between 0 (inclusive) and 15 (inclusive)
     * @return the hex digit that matches the given number
     */
    @SuppressWarnings("ShiftOutOfRange")
    public static char hexChar(final long number) {
        return (char)(number + 48 + (9 - number >>> -3));
    }

    /**
     * Appends the 8-digit unsigned hex format of {@code number} to the given StringBuilder. This always draws from
     * only the digits 0-9 and the capital letters A-F for its hex digits.
     * @param sb an existing StringBuilder to append to
     * @param number any int to write in hex format
     * @return sb, after modification, for chaining.
     */
    public static StringBuilder appendUnsignedHex(StringBuilder sb, int number) {
        for (int s = 28; s >= 0; s -= 4) {
            sb.append(hexChar(number >>> s & 15));
        }
        return sb;
    }

    /**
     * Appends the 16-digit unsigned hex format of {@code number} to the given StringBuilder. This always draws from
     * only the digits 0-9 and the capital letters A-F for its hex digits.
     * @param sb an existing StringBuilder to append to
     * @param number any long to write in hex format
     * @return sb, after modification, for chaining.
     */
    public static StringBuilder appendUnsignedHex(StringBuilder sb, long number) {
        for (int i = 60; i >= 0; i -= 4) {
            sb.append(hexChar(number >>> i & 15));
        }
        return sb;
    }

    /**
     * Allocates a new 8-char array filled with the unsigned hex format of {@code number}, and returns it.
     * @param number any int to write in hex format
     * @return a new char array holding the 8-character unsigned hex representation of {@code number}
     */
    public static char[] unsignedHexArray(int number) {
        final char[] chars = new char[8];
        for (int i = 0, s = 28; i < 8; i++, s -= 4) {
            chars[i] = hexChar(number >>> s & 15);
        }
        return chars;
    }

    /**
     * Allocates a new 16-char array filled with the unsigned hex format of {@code number}, and returns it.
     * @param number any long to write in hex format
     * @return a new char array holding the 16-character unsigned hex representation of {@code number}
     */
    public static char[] unsignedHexArray(long number) {
        final char[] chars = new char[16];
        for (int i = 0, s = 60; i < 16; i++, s -= 4) {
            chars[i] = hexChar(number >>> s & 15);
        }
        return chars;
    }

    /**
     * Allocates a new 8-char String filled with the unsigned hex format of {@code number}, and returns it.
     * @param number any int to write in hex format
     * @return a new String holding the 8-character unsigned hex representation of {@code number}
     */
    public static String unsignedHex(int number) {
        return String.valueOf(unsignedHexArray(number));
    }

    /**
     * Allocates a new 16-char String filled with the unsigned hex format of {@code number}, and returns it.
     * @param number any long to write in hex format
     * @return a new String holding the 16-character unsigned hex representation of {@code number}
     */
    public static String unsignedHex(long number) {
        return String.valueOf(unsignedHexArray(number));
    }

    /**
     * Reads in a CharSequence containing only decimal digits (only 0-9) with an optional sign at the start
     * and returns the long they represent, reading at most 19 characters (20 if there is a sign) and returning the
     * result if valid, or 0 if nothing could be read. The leading sign can be '+' or '-' if present. This can also
     * represent negative numbers as they are printed as unsigned longs. This means "18446744073709551615" would
     * return the long -1 when passed to this, though you could also simply use "-1" . If you use both '-' at the start
     * and have the number as greater than {@link Long#MAX_VALUE}, such as with "-18446744073709551615", then both
     * indicate a negative number, but the digits will be processed first (producing -1) and then the whole thing will
     * be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a decimal digit, or
     * stopping the parse process early if a non-0-9 char is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a CharSequence, such as a String, containing decimal digits with an optional sign
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this stops after 20 characters if end is too large)
     * @return the long that cs represents
     */
    public static long longFromDec(final CharSequence cs, final int start, int end) {
        int sign, h, lim;
        if (cs == null || start < 0 || end <= 0 || (end = Math.min(end, cs.length())) - start <= 0)
            return 0;
        char c = cs.charAt(start);
        if (c == '-') {
            sign = -1;
            h = 0;
            lim = 21;
        } else if (c == '+') {
            sign = 1;
            h = 0;
            lim = 21;
        } else {
            if (!(c >= '0' && c <= '9'))
                return 0;
            else {
                sign = 1;
                lim = 20;
            }
            h = (c - '0');
        }
        long data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            c = cs.charAt(i);
            if (!(c >= '0' && c <= '9'))
                return data * sign;
            data *= 10;
            data += (c - '0');
        }
        return data * sign;
    }


    /**
     * Reads in a CharSequence containing only decimal digits (only 0-9) with an optional sign at the start
     * and returns the int they represent, reading at most 10 characters (11 if there is a sign) and returning the
     * result if valid, or 0 if nothing could be read. The leading sign can be '+' or '-' if present. This can also
     * represent negative numbers as they are printed as unsigned integers. This means "4294967295" would return the int
     * -1 when passed to this, though you could also simply use "-1" . If you use both '-' at the start and have the
     * number as greater than {@link Integer#MAX_VALUE}, such as with "-4294967295", then both indicate a negative
     * number, but the digits will be processed first (producing -1) and then the whole thing will be multiplied by -1
     * to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a decimal digit, or
     * stopping the parse process early if a non-0-9 char is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a CharSequence, such as a String, containing decimal digits with an optional sign
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this stops after 10 characters if end is too large)
     * @return the int that cs represents
     */
    public static int intFromDec(final CharSequence cs, final int start, int end) {
        int sign, h, lim;
        if (cs == null || start < 0 || end <= 0 || (end = Math.min(end, cs.length())) - start <= 0)
            return 0;
        char c = cs.charAt(start);
        if (c == '-') {
            sign = -1;
            h = 0;
            lim = 11;
        } else if (c == '+') {
            sign = 1;
            h = 0;
            lim = 11;
        } else {
            if (!(c >= '0' && c <= '9'))
                return 0;
            else {
                sign = 1;
                lim = 10;
            }
            h = (c - '0');
        }
        int data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            c = cs.charAt(i);
            if (!(c >= '0' && c <= '9'))
                return data * sign;
            data *= 10;
            data += (c - '0');
        }
        return data * sign;
    }

    /**
     * Reads in a CharSequence containing only hex digits (only 0-9, a-f, and A-F) with an optional sign at the start
     * and returns the int they represent, reading at most 8 characters (9 if there is a sign) and returning the
     * result if valid, or 0 if nothing could be read. The leading sign can be '+' or '-' if present. This can also
     * represent negative numbers as they are printed by such methods as String.format() given %X in the formatting
     * string; that is, if the first char of an 8-char (or longer)
     * CharSequence is a hex digit 8 or higher, then the whole number represents a negative number, using two's
     * complement and so on. This means "FFFFFFFF" would return the int -1 when passed to this, though you
     * could also simply use "-1" . If you use both '-' at the start and have the most significant digit as 8 or higher,
     * such as with "-FFFFFFFF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a hex digit, or
     * stopping the parse process early if a non-hex-digit char is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and just doesn't fill the larger places.
     *
     * @param cs    a CharSequence, such as a String, containing hex digits with an optional sign (no 0x at the start)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this stops after 8 characters if end is too large)
     * @return the int that cs represents
     */
    public static int intFromHex(final CharSequence cs, final int start, int end) {
        int sign, h, lim;
        if (cs == null || start < 0 || end <= 0 || (end = Math.min(end, cs.length())) - start <= 0)
            return 0;
        char c = cs.charAt(start);
        if (c == '-') {
            sign = -1;
            h = 0;
            lim = 9;
        } else if (c == '+') {
            sign = 1;
            h = 0;
            lim = 9;
        } else {
            if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f')))
                return 0;
            else {
                sign = 1;
                lim = 8;
                h = hexCode(c);
            }
        }
        int data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            c = cs.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f')))
                return data * sign;
            data <<= 4;
            data |= hexCode(c);
        }
        return data * sign;
    }

    /**
     * Reads in a CharSequence containing only hex digits (only 0-9, a-f, and A-F) with an optional sign at the start
     * and returns the long they represent, reading at most 16 characters (17 if there is a sign) and returning the
     * result if valid, or 0 if nothing could be read. The leading sign can be '+' or '-' if present. This can also
     * represent negative numbers as they are printed by such methods as String.format() given %X in the formatting
     * string; that is, if the first char of a 16-char (or longer) CharSequence is a hex digit 8 or higher, then the
     * whole number represents a negative number, using two's complement and so on. This means "FFFFFFFFFFFFFFFF" would
     * return the long -1 when passed to this, though you could also simply use "-1" . If you use both '-' at the start
     * and have the most significant digit as 8 or higher, such as with "-FFFFFFFFFFFFFFFF", then both indicate a
     * negative number, but the digits will be processed first (producing -1) and then the whole thing will be
     * multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a hex digit, or
     * stopping the parse process early if a non-hex-digit char is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and just doesn't fill the larger places.
     *
     * @param cs    a CharSequence, such as a String, containing hex digits with an optional sign (no 0x at the start)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this stops after 8 characters if end is too large)
     * @return the long that cs represents
     */
    public static long longFromHex(final CharSequence cs, final int start, int end) {
        int sign, h, lim;
        if (cs == null || start < 0 || end <= 0 || (end = Math.min(end, cs.length())) - start <= 0)
            return 0;
        char c = cs.charAt(start);
        if (c == '-') {
            sign = -1;
            h = 0;
            lim = 17;
        } else if (c == '+') {
            sign = 1;
            h = 0;
            lim = 17;
        } else {
            if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f')))
                return 0;
            else {
                sign = 1;
                lim = 16;
                h = hexCode(c);
            }
        }
        long data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            c = cs.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f')))
                return data * sign;
            data <<= 4;
            data |= hexCode(c);
        }
        return data * sign;
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
        } else if (c < '0' || c > '9')
            return 0;
        else {
            h = hexCode(c);
            len = 1;
        }
        int data = h;
        for (int i = start + 1; i < end; i++) {
            c = cs.charAt(i);
            if(c == '.') {
                foundPoint = true;
                continue;
            }
            if (c < '0' || c > '9')
                return data * len / decimal;
            h = hexCode(c);
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
