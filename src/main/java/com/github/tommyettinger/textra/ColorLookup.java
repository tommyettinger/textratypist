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

package com.github.tommyettinger.textra;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;

/**
 * Allows looking up an RGBA8888 Integer color given a String key, returning either the color or null if none was found.
 * This is an extension point for games and libraries that may want their own way of looking up colors.
 */
public interface ColorLookup {
    /**
     * Uses {@code key} to look up an RGBA8888 color, and returns that color as an Integer if one was found, or returns
     * null if none was found. This library will never call this method with a null key, and in most cases you can
     * safely assume key is non-null.
     * @param key the String key to use to look up or build a color; should not be null.
     * @return an RGBA8888 color that may be null.
     */
    Integer getRgba(String key);

    /**
     * The default ColorLookup, this simply looks up {@code key} in {@link Colors}, returning null if no Color exists by
     * that exact name (case-sensitive), or returning the RGBA8888 value of the color otherwise.
     */
    class GdxColorLookup implements ColorLookup{
        /**
         * The only way to access a GdxColorLookup. Since this class has no state and exists only to implement an
         * interface, it is fine that this is static.
         */
        public static final GdxColorLookup INSTANCE = new GdxColorLookup();
        private GdxColorLookup(){}
        @Override
        public Integer getRgba(String key) {
            Color c = Colors.get(key);
            return c == null ? null : Color.rgba8888(c);
        }
    }
}
