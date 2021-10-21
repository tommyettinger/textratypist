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
