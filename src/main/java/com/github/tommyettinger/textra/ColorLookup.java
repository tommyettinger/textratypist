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

package com.github.tommyettinger.textra;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.github.tommyettinger.textra.utils.ColorUtils;

/**
 * Allows looking up an RGBA8888 int color given a String key, returning either the color or 256 if none was found.
 * This is an extension point for games and libraries that may want their own way of looking up colors. This can be
 * treated as a functional interface in Java 8 and higher.
 * <br>
 * The default here is typically {@link #DESCRIPTIVE}, which allows using multiple color names, plus adjectives. There
 * is also {@link #INSTANCE}, which is older and only looks up one color name at a time from {@link Colors} in libGDX.
 */
public interface ColorLookup {
    /**
     * The default ColorLookup, this simply looks up {@code key} in {@link Colors}. It returns 256 (fully transparent,
     * extremely dark blue) if no Color exists by that exact name (case-sensitive), or returning the RGBA8888 value
     * of the color otherwise. All color names are {@code ALL_CAPS} in libGDX's Colors collection by default.
     * This can also be accessed with {@link GdxColorLookup#INSTANCE}.
     */
    GdxColorLookup INSTANCE = GdxColorLookup.INSTANCE;

    /**
     * An alternative ColorLookup, this parses a description such as "peach red" or "DARK DULLEST GREEN" using
     * {@link ColorUtils#describe(String)} (See its docs for more information). The colors available are in
     * {@link com.github.tommyettinger.textra.utils.Palette}; there are adjectives that modify lightness and saturation
     * (see {@link ColorUtils#describe(String)}), and you can specify multiple colors to mix them, with or without
     * weights per-color. Case is effectively ignored for adjectives, but in some cases it can matter for color names --
     * ALL_CAPS names are ones from the libGDX class {@link Colors}, while lowercase ones are defined by this library.
     */
    ColorLookup DESCRIPTIVE = new ColorLookup() {
        @Override
        public int getRgba(String description) {
            return ColorUtils.describe(description);
        }
    };

    /**
     * Uses {@code key} to look up an RGBA8888 color, and returns that color as an int if one was found, or returns
     * 256 if none was found. 256 is used because it is different from the more commonly-used 0 for fully-transparent,
     * while still being easy to remember and very rare to ever want (it is fully transparent, very dark blue). This
     * library will never call this method with a null key, and in most cases you can safely assume key is non-null.
     *
     * @param key the String key to use to look up or build a color; should not be null.
     * @return an RGBA8888 color; if 256, this can be considered to not know how to look up the given key.
     */
    int getRgba(String key);

    /**
     * The default ColorLookup, this simply looks up {@code key} in {@link Colors}, returning 256 (fully transparent,
     * extremely dark blue) if no Color exists by that exact name (case-sensitive), or returning the RGBA8888 value
     * of the color otherwise. All color names are {@code ALL_CAPS} in libGDX's Colors collection by default.
     */
    class GdxColorLookup implements ColorLookup {
        /**
         * The only way to access a GdxColorLookup. Since this class has no state and exists only to implement an
         * interface, it is fine that this is static.
         */
        public static final GdxColorLookup INSTANCE = new GdxColorLookup();

        private GdxColorLookup() {
        }

        @Override
        public int getRgba(String key) {
            Color c = Colors.get(key);
            return c == null ? 256 : Color.rgba8888(c);
        }
    }
}
