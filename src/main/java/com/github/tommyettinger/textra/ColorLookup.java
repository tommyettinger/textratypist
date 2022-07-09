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
 * Allows looking up an RGBA8888 int color given a String key, returning either the color or 256 if none was found.
 * This is an extension point for games and libraries that may want their own way of looking up colors. This can be
 * treated as a functional interface in Java 8 and higher.
 * <br>
 * You can see how this is used in <a href="https://github.com/yellowstonegames/SquidSquad">SquidSquad</a>, which uses
 * it in its squidglyph module to look up colors by potentially complex descriptive terms.
 */
public interface ColorLookup {
    ColorLookup INSTANCE = new ColorLookup() {};
    /**
     * Uses {@code key} to look up an RGBA8888 color, and returns that color as an int if one was found, or returns
     * 256 if none was found. 256 is used because it is different from the more commonly-used 0 for fully-transparent,
     * while still being easy to remember and very rare to ever want (it is fully transparent, very dark blue). This
     * library will never call this method with a null key, and in most cases you can safely assume key is non-null.
     *
     * @param key the String key to use to look up or build a color; should not be null.
     * @return an RGBA8888 color; if 256, this can be considered to not know how to look up the given key.
     */
    default int getRgba(String key) {
        Color c = Colors.get(key);
        return c == null ? 256 : Color.rgba8888(c);
    }

}
