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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectIntMap;

/**
 * Combines 50 colors chosen to be generally distinct with the 34 colors libGDX defines in {@link Colors}. Some colors
 * here are duplicates, and there are also aliases to part of the group of 50 for convenience. All colors are defined as
 * RGBA8888 ints with extensive JavaDocs. The colors from libGDX are in {@code ALL_CAPS}, while the colors defined here
 * first are in {@code lowercase}.
 */
public final class Palette {
    /**
     * Not instantiable.
     */
    private Palette() {}
    /**
     * Stores alternative names for colors in {@link #NAMED}, like "grey" as an alias for {@link #GRAY} or "gold" as an
     * alias for {@link #saffron}. Currently, the list of aliases is as follows:
     * <ul>
     * <li>"grey" maps to {@link #gray},</li>
     * <li>"gold" maps to {@link #saffron},</li>
     * <li>"puce" maps to {@link #mauve},</li>
     * <li>"sand" maps to {@link #tan},</li>
     * <li>"skin" maps to {@link #peach},</li>
     * <li>"coral" maps to {@link #salmon},</li>
     * <li>"azure" maps to {@link #sky}, and</li>
     * <li>"ocean" maps to {@link #teal}, and</li>
     * <li>"sapphire" maps to {@link #cobalt}.</li>
     * </ul>
     * These aliases are duplicated in {@link #NAMES}. They are primarily there so blind attempts to name
     * a color might still work.
     */
    public static final ObjectIntMap<String> ALIASES = new ObjectIntMap<>(20);

    public static final ObjectIntMap<String> NAMED = new ObjectIntMap<>(84);
    public static final IntArray LIST = new IntArray(84);

    /**
     * This color constant "transparent" has RGBA8888 code {@code 00000000}, R 0.0, G 0.0, B 0.0, A 0.0, hue 0.0, saturation 0.0, and lightness 0.0.
     * <pre>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #000000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int transparent = 0x00000000;
    static { NAMED.put("transparent", 0x00000000); LIST.add(0x00000000); }

    /**
     * This color constant "black" has RGBA8888 code {@code 000000FF}, R 0.0, G 0.0, B 0.0, A 1.0, hue 0.0, saturation 0.0, and lightness 0.0.
     * <pre>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #000000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int black = 0x000000FF;
    static { NAMED.put("black", 0x000000FF); LIST.add(0x000000FF); }

    /**
     * This color constant "gray" has RGBA8888 code {@code 808080FF}, R 0.5019608, G 0.5019608, B 0.5019608, A 1.0, hue 0.0, saturation 0.0, and lightness 0.5019608.
     * <pre>
     * <font style='background-color: #808080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #808080; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #808080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #808080'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #808080'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #808080'>&nbsp;@&nbsp;</font><font style='background-color: #808080; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #808080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #808080; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int gray = 0x808080FF;
    static { NAMED.put("gray", 0x808080FF); LIST.add(0x808080FF); }

    /**
     * This color constant "silver" has RGBA8888 code {@code B6B6B6FF}, R 0.7137255, G 0.7137255, B 0.7137255, A 1.0, hue 0.0, saturation 0.0, and lightness 0.7137255.
     * <pre>
     * <font style='background-color: #B6B6B6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B6B6B6; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #B6B6B6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #B6B6B6'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #B6B6B6'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #B6B6B6'>&nbsp;@&nbsp;</font><font style='background-color: #B6B6B6; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #B6B6B6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B6B6B6; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int silver = 0xB6B6B6FF;
    static { NAMED.put("silver", 0xB6B6B6FF); LIST.add(0xB6B6B6FF); }

    /**
     * This color constant "white" has RGBA8888 code {@code FFFFFFFF}, R 1.0, G 1.0, B 1.0, A 1.0, hue 0.0, saturation 0.0, and lightness 1.0.
     * <pre>
     * <font style='background-color: #FFFFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFFFF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFFFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFFFFF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFFFFF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFFFFF'>&nbsp;@&nbsp;</font><font style='background-color: #FFFFFF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFFFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFFFF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int white = 0xFFFFFFFF;
    static { NAMED.put("white", 0xFFFFFFFF); LIST.add(0xFFFFFFFF); }

    /**
     * This color constant "red" has RGBA8888 code {@code FF0000FF}, R 1.0, G 0.0, B 0.0, A 1.0, hue 0.0, saturation 1.0, and lightness 0.5.
     * <pre>
     * <font style='background-color: #FF0000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF0000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF0000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FF0000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FF0000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FF0000'>&nbsp;@&nbsp;</font><font style='background-color: #FF0000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF0000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF0000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int red = 0xFF0000FF;
    static { NAMED.put("red", 0xFF0000FF); LIST.add(0xFF0000FF); }

    /**
     * This color constant "orange" has RGBA8888 code {@code FF7F00FF}, R 1.0, G 0.49803922, B 0.0, A 1.0, hue 0.08300654, saturation 1.0, and lightness 0.5.
     * <pre>
     * <font style='background-color: #FF7F00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF7F00; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF7F00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FF7F00'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FF7F00'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FF7F00'>&nbsp;@&nbsp;</font><font style='background-color: #FF7F00; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF7F00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF7F00; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int orange = 0xFF7F00FF;
    static { NAMED.put("orange", 0xFF7F00FF); LIST.add(0xFF7F00FF); }

    /**
     * This color constant "yellow" has RGBA8888 code {@code FFFF00FF}, R 1.0, G 1.0, B 0.0, A 1.0, hue 0.16666667, saturation 1.0, and lightness 0.5.
     * <pre>
     * <font style='background-color: #FFFF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFF00; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFFF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFFF00'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFFF00'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFFF00'>&nbsp;@&nbsp;</font><font style='background-color: #FFFF00; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFFF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFF00; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int yellow = 0xFFFF00FF;
    static { NAMED.put("yellow", 0xFFFF00FF); LIST.add(0xFFFF00FF); }

    /**
     * This color constant "green" has RGBA8888 code {@code 00FF00FF}, R 0.0, G 1.0, B 0.0, A 1.0, hue 0.33333334, saturation 1.0, and lightness 0.5.
     * <pre>
     * <font style='background-color: #00FF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FF00; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00FF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #00FF00'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #00FF00'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #00FF00'>&nbsp;@&nbsp;</font><font style='background-color: #00FF00; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00FF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FF00; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int green = 0x00FF00FF;
    static { NAMED.put("green", 0x00FF00FF); LIST.add(0x00FF00FF); }

    /**
     * This color constant "blue" has RGBA8888 code {@code 0000FFFF}, R 0.0, G 0.0, B 1.0, A 1.0, hue 0.6666667, saturation 1.0, and lightness 0.5.
     * <pre>
     * <font style='background-color: #0000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0000FF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #0000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #0000FF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #0000FF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #0000FF'>&nbsp;@&nbsp;</font><font style='background-color: #0000FF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #0000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0000FF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int blue = 0x0000FFFF;
    static { NAMED.put("blue", 0x0000FFFF); LIST.add(0x0000FFFF); }

    /**
     * This color constant "indigo" has RGBA8888 code {@code 520FE0FF}, R 0.32156864, G 0.05882353, B 0.8784314, A 1.0, hue 0.7200957, saturation 0.81960785, and lightness 0.46862745.
     * <pre>
     * <font style='background-color: #520FE0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #520FE0; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #520FE0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #520FE0'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #520FE0'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #520FE0'>&nbsp;@&nbsp;</font><font style='background-color: #520FE0; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #520FE0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #520FE0; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int indigo = 0x520FE0FF;
    static { NAMED.put("indigo", 0x520FE0FF); LIST.add(0x520FE0FF); }

    /**
     * This color constant "violet" has RGBA8888 code {@code 9040EFFF}, R 0.5647059, G 0.2509804, B 0.9372549, A 1.0, hue 0.74285716, saturation 0.6862745, and lightness 0.59411764.
     * <pre>
     * <font style='background-color: #9040EF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #9040EF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #9040EF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #9040EF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #9040EF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #9040EF'>&nbsp;@&nbsp;</font><font style='background-color: #9040EF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #9040EF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #9040EF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int violet = 0x9040EFFF;
    static { NAMED.put("violet", 0x9040EFFF); LIST.add(0x9040EFFF); }

    /**
     * This color constant "purple" has RGBA8888 code {@code C000FFFF}, R 0.7529412, G 0.0, B 1.0, A 1.0, hue 0.7921569, saturation 1.0, and lightness 0.5.
     * <pre>
     * <font style='background-color: #C000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #C000FF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #C000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #C000FF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #C000FF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #C000FF'>&nbsp;@&nbsp;</font><font style='background-color: #C000FF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #C000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #C000FF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int purple = 0xC000FFFF;
    static { NAMED.put("purple", 0xC000FFFF); LIST.add(0xC000FFFF); }

    /**
     * This color constant "brown" has RGBA8888 code {@code 8F573BFF}, R 0.56078434, G 0.34117648, B 0.23137255, A 1.0, hue 0.055555552, saturation 0.3294118, and lightness 0.39607844.
     * <pre>
     * <font style='background-color: #8F573B;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #8F573B; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #8F573B;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #8F573B'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #8F573B'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #8F573B'>&nbsp;@&nbsp;</font><font style='background-color: #8F573B; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #8F573B;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #8F573B; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int brown = 0x8F573BFF;
    static { NAMED.put("brown", 0x8F573BFF); LIST.add(0x8F573BFF); }

    /**
     * This color constant "pink" has RGBA8888 code {@code FFA0E0FF}, R 1.0, G 0.627451, B 0.8784314, A 1.0, hue 0.8877193, saturation 0.372549, and lightness 0.8137255.
     * <pre>
     * <font style='background-color: #FFA0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA0E0; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFA0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFA0E0'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFA0E0'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFA0E0'>&nbsp;@&nbsp;</font><font style='background-color: #FFA0E0; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFA0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA0E0; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int pink = 0xFFA0E0FF;
    static { NAMED.put("pink", 0xFFA0E0FF); LIST.add(0xFFA0E0FF); }

    /**
     * This color constant "magenta" has RGBA8888 code {@code F500F5FF}, R 0.9607843, G 0.0, B 0.9607843, A 1.0, hue 0.8333333, saturation 0.9607843, and lightness 0.48039216.
     * <pre>
     * <font style='background-color: #F500F5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #F500F5; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #F500F5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #F500F5'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #F500F5'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #F500F5'>&nbsp;@&nbsp;</font><font style='background-color: #F500F5; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #F500F5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #F500F5; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int magenta = 0xF500F5FF;
    static { NAMED.put("magenta", 0xF500F5FF); LIST.add(0xF500F5FF); }

    /**
     * This color constant "brick" has RGBA8888 code {@code D5524AFF}, R 0.8352941, G 0.32156864, B 0.2901961, A 1.0, hue 0.009592325, saturation 0.54509807, and lightness 0.5627451.
     * <pre>
     * <font style='background-color: #D5524A;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D5524A; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D5524A;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #D5524A'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #D5524A'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #D5524A'>&nbsp;@&nbsp;</font><font style='background-color: #D5524A; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D5524A;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D5524A; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int brick = 0xD5524AFF;
    static { NAMED.put("brick", 0xD5524AFF); LIST.add(0xD5524AFF); }

    /**
     * This color constant "ember" has RGBA8888 code {@code F55A32FF}, R 0.9607843, G 0.3529412, B 0.19607843, A 1.0, hue 0.034188036, saturation 0.7647059, and lightness 0.5784313.
     * <pre>
     * <font style='background-color: #F55A32;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #F55A32; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #F55A32;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #F55A32'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #F55A32'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #F55A32'>&nbsp;@&nbsp;</font><font style='background-color: #F55A32; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #F55A32;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #F55A32; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int ember = 0xF55A32FF;
    static { NAMED.put("ember", 0xF55A32FF); LIST.add(0xF55A32FF); }

    /**
     * This color constant "salmon" has RGBA8888 code {@code FF6262FF}, R 1.0, G 0.38431373, B 0.38431373, A 1.0, hue 0.0, saturation 0.6156863, and lightness 0.69215685.
     * <pre>
     * <font style='background-color: #FF6262;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF6262; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF6262;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FF6262'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FF6262'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FF6262'>&nbsp;@&nbsp;</font><font style='background-color: #FF6262; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF6262;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF6262; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int salmon = 0xFF6262FF;
    static { NAMED.put("salmon", 0xFF6262FF); LIST.add(0xFF6262FF); }

    /**
     * This color constant "chocolate" has RGBA8888 code {@code 683818FF}, R 0.40784314, G 0.21960784, B 0.09411765, A 1.0, hue 0.066666655, saturation 0.3137255, and lightness 0.25098038.
     * <pre>
     * <font style='background-color: #683818;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #683818; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #683818;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #683818'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #683818'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #683818'>&nbsp;@&nbsp;</font><font style='background-color: #683818; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #683818;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #683818; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int chocolate = 0x683818FF;
    static { NAMED.put("chocolate", 0x683818FF); LIST.add(0x683818FF); }

    /**
     * This color constant "tan" has RGBA8888 code {@code D2B48CFF}, R 0.8235294, G 0.7058824, B 0.54901963, A 1.0, hue 0.0952381, saturation 0.2745098, and lightness 0.6862745.
     * <pre>
     * <font style='background-color: #D2B48C;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2B48C; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D2B48C;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #D2B48C'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #D2B48C'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #D2B48C'>&nbsp;@&nbsp;</font><font style='background-color: #D2B48C; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D2B48C;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2B48C; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int tan = 0xD2B48CFF;
    static { NAMED.put("tan", 0xD2B48CFF); LIST.add(0xD2B48CFF); }

    /**
     * This color constant "bronze" has RGBA8888 code {@code CE8E31FF}, R 0.80784315, G 0.5568628, B 0.19215687, A 1.0, hue 0.09872612, saturation 0.6156863, and lightness 0.49999997.
     * <pre>
     * <font style='background-color: #CE8E31;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #CE8E31; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #CE8E31;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #CE8E31'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #CE8E31'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #CE8E31'>&nbsp;@&nbsp;</font><font style='background-color: #CE8E31; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #CE8E31;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #CE8E31; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int bronze = 0xCE8E31FF;
    static { NAMED.put("bronze", 0xCE8E31FF); LIST.add(0xCE8E31FF); }

    /**
     * This color constant "cinnamon" has RGBA8888 code {@code D2691DFF}, R 0.8235294, G 0.4117647, B 0.11372549, A 1.0, hue 0.06998159, saturation 0.70980394, and lightness 0.46862742.
     * <pre>
     * <font style='background-color: #D2691D;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2691D; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D2691D;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #D2691D'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #D2691D'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #D2691D'>&nbsp;@&nbsp;</font><font style='background-color: #D2691D; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D2691D;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2691D; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int cinnamon = 0xD2691DFF;
    static { NAMED.put("cinnamon", 0xD2691DFF); LIST.add(0xD2691DFF); }

    /**
     * This color constant "apricot" has RGBA8888 code {@code FFA828FF}, R 1.0, G 0.65882355, B 0.15686275, A 1.0, hue 0.09922481, saturation 0.84313726, and lightness 0.57843137.
     * <pre>
     * <font style='background-color: #FFA828;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA828; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFA828;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFA828'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFA828'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFA828'>&nbsp;@&nbsp;</font><font style='background-color: #FFA828; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFA828;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA828; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int apricot = 0xFFA828FF;
    static { NAMED.put("apricot", 0xFFA828FF); LIST.add(0xFFA828FF); }

    /**
     * This color constant "peach" has RGBA8888 code {@code FFBF81FF}, R 1.0, G 0.7490196, B 0.5058824, A 1.0, hue 0.08201058, saturation 0.49411762, and lightness 0.7529412.
     * <pre>
     * <font style='background-color: #FFBF81;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFBF81; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFBF81;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFBF81'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFBF81'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFBF81'>&nbsp;@&nbsp;</font><font style='background-color: #FFBF81; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFBF81;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFBF81; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int peach = 0xFFBF81FF;
    static { NAMED.put("peach", 0xFFBF81FF); LIST.add(0xFFBF81FF); }

    /**
     * This color constant "pear" has RGBA8888 code {@code D3E330FF}, R 0.827451, G 0.8901961, B 0.1882353, A 1.0, hue 0.18156426, saturation 0.7019608, and lightness 0.5392157.
     * <pre>
     * <font style='background-color: #D3E330;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D3E330; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D3E330;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #D3E330'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #D3E330'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #D3E330'>&nbsp;@&nbsp;</font><font style='background-color: #D3E330; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D3E330;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D3E330; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int pear = 0xD3E330FF;
    static { NAMED.put("pear", 0xD3E330FF); LIST.add(0xD3E330FF); }

    /**
     * This color constant "saffron" has RGBA8888 code {@code FFD510FF}, R 1.0, G 0.8352941, B 0.0627451, A 1.0, hue 0.13737796, saturation 0.9372549, and lightness 0.53137255.
     * <pre>
     * <font style='background-color: #FFD510;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFD510; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFD510;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFD510'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFD510'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFD510'>&nbsp;@&nbsp;</font><font style='background-color: #FFD510; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFD510;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFD510; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int saffron = 0xFFD510FF;
    static { NAMED.put("saffron", 0xFFD510FF); LIST.add(0xFFD510FF); }

    /**
     * This color constant "butter" has RGBA8888 code {@code FFF288FF}, R 1.0, G 0.9490196, B 0.53333336, A 1.0, hue 0.14845939, saturation 0.46666664, and lightness 0.76666665.
     * <pre>
     * <font style='background-color: #FFF288;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFF288; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFF288;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFF288'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFF288'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFF288'>&nbsp;@&nbsp;</font><font style='background-color: #FFF288; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFF288;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFF288; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int butter = 0xFFF288FF;
    static { NAMED.put("butter", 0xFFF288FF); LIST.add(0xFFF288FF); }

    /**
     * This color constant "chartreuse" has RGBA8888 code {@code C8FF41FF}, R 0.78431374, G 1.0, B 0.25490198, A 1.0, hue 0.21491227, saturation 0.745098, and lightness 0.627451.
     * <pre>
     * <font style='background-color: #C8FF41;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #C8FF41; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #C8FF41;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #C8FF41'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #C8FF41'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #C8FF41'>&nbsp;@&nbsp;</font><font style='background-color: #C8FF41; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #C8FF41;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #C8FF41; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int chartreuse = 0xC8FF41FF;
    static { NAMED.put("chartreuse", 0xC8FF41FF); LIST.add(0xC8FF41FF); }

    /**
     * This color constant "cactus" has RGBA8888 code {@code 30A000FF}, R 0.1882353, G 0.627451, B 0.0, A 1.0, hue 0.28333336, saturation 0.627451, and lightness 0.3137255.
     * <pre>
     * <font style='background-color: #30A000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #30A000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #30A000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #30A000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #30A000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #30A000'>&nbsp;@&nbsp;</font><font style='background-color: #30A000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #30A000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #30A000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int cactus = 0x30A000FF;
    static { NAMED.put("cactus", 0x30A000FF); LIST.add(0x30A000FF); }

    /**
     * This color constant "lime" has RGBA8888 code {@code 93D300FF}, R 0.5764706, G 0.827451, B 0.0, A 1.0, hue 0.21721959, saturation 0.827451, and lightness 0.4137255.
     * <pre>
     * <font style='background-color: #93D300;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #93D300; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #93D300;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #93D300'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #93D300'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #93D300'>&nbsp;@&nbsp;</font><font style='background-color: #93D300; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #93D300;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #93D300; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int lime = 0x93D300FF;
    static { NAMED.put("lime", 0x93D300FF); LIST.add(0x93D300FF); }

    /**
     * This color constant "olive" has RGBA8888 code {@code 818000FF}, R 0.5058824, G 0.5019608, B 0.0, A 1.0, hue 0.16537468, saturation 0.5058824, and lightness 0.2529412.
     * <pre>
     * <font style='background-color: #818000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #818000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #818000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #818000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #818000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #818000'>&nbsp;@&nbsp;</font><font style='background-color: #818000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #818000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #818000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int olive = 0x818000FF;
    static { NAMED.put("olive", 0x818000FF); LIST.add(0x818000FF); }

    /**
     * This color constant "fern" has RGBA8888 code {@code 4E7942FF}, R 0.30588236, G 0.4745098, B 0.25882354, A 1.0, hue 0.2969697, saturation 0.21568626, and lightness 0.36666664.
     * <pre>
     * <font style='background-color: #4E7942;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #4E7942; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #4E7942;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #4E7942'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #4E7942'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #4E7942'>&nbsp;@&nbsp;</font><font style='background-color: #4E7942; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #4E7942;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #4E7942; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int fern = 0x4E7942FF;
    static { NAMED.put("fern", 0x4E7942FF); LIST.add(0x4E7942FF); }

    /**
     * This color constant "moss" has RGBA8888 code {@code 204608FF}, R 0.1254902, G 0.27450982, B 0.03137255, A 1.0, hue 0.26881722, saturation 0.24313727, and lightness 0.15294118.
     * <pre>
     * <font style='background-color: #204608;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #204608; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #204608;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #204608'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #204608'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #204608'>&nbsp;@&nbsp;</font><font style='background-color: #204608; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #204608;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #204608; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int moss = 0x204608FF;
    static { NAMED.put("moss", 0x204608FF); LIST.add(0x204608FF); }

    /**
     * This color constant "celery" has RGBA8888 code {@code 7DFF73FF}, R 0.49019608, G 1.0, B 0.4509804, A 1.0, hue 0.32142857, saturation 0.5490196, and lightness 0.7254902.
     * <pre>
     * <font style='background-color: #7DFF73;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7DFF73; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #7DFF73;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #7DFF73'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #7DFF73'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #7DFF73'>&nbsp;@&nbsp;</font><font style='background-color: #7DFF73; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #7DFF73;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7DFF73; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int celery = 0x7DFF73FF;
    static { NAMED.put("celery", 0x7DFF73FF); LIST.add(0x7DFF73FF); }

    /**
     * This color constant "sage" has RGBA8888 code {@code ABE3C5FF}, R 0.67058825, G 0.8901961, B 0.77254903, A 1.0, hue 0.4107143, saturation 0.21960783, and lightness 0.78039217.
     * <pre>
     * <font style='background-color: #ABE3C5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ABE3C5; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #ABE3C5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #ABE3C5'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #ABE3C5'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #ABE3C5'>&nbsp;@&nbsp;</font><font style='background-color: #ABE3C5; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #ABE3C5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ABE3C5; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int sage = 0xABE3C5FF;
    static { NAMED.put("sage", 0xABE3C5FF); LIST.add(0xABE3C5FF); }

    /**
     * This color constant "jade" has RGBA8888 code {@code 3FBF3FFF}, R 0.24705882, G 0.7490196, B 0.24705882, A 1.0, hue 0.33333334, saturation 0.5019608, and lightness 0.49803922.
     * <pre>
     * <font style='background-color: #3FBF3F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3FBF3F; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #3FBF3F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #3FBF3F'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #3FBF3F'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #3FBF3F'>&nbsp;@&nbsp;</font><font style='background-color: #3FBF3F; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #3FBF3F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3FBF3F; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int jade = 0x3FBF3FFF;
    static { NAMED.put("jade", 0x3FBF3FFF); LIST.add(0x3FBF3FFF); }

    /**
     * This color constant "cyan" has RGBA8888 code {@code 00FFFFFF}, R 0.0, G 1.0, B 1.0, A 1.0, hue 0.5, saturation 1.0, and lightness 0.5.
     * <pre>
     * <font style='background-color: #00FFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FFFF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00FFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #00FFFF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #00FFFF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #00FFFF'>&nbsp;@&nbsp;</font><font style='background-color: #00FFFF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00FFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FFFF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int cyan = 0x00FFFFFF;
    static { NAMED.put("cyan", 0x00FFFFFF); LIST.add(0x00FFFFFF); }

    /**
     * This color constant "mint" has RGBA8888 code {@code 7FFFD4FF}, R 0.49803922, G 1.0, B 0.83137256, A 1.0, hue 0.44401044, saturation 0.50196075, and lightness 0.7490196.
     * <pre>
     * <font style='background-color: #7FFFD4;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7FFFD4; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #7FFFD4;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #7FFFD4'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #7FFFD4'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #7FFFD4'>&nbsp;@&nbsp;</font><font style='background-color: #7FFFD4; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #7FFFD4;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7FFFD4; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int mint = 0x7FFFD4FF;
    static { NAMED.put("mint", 0x7FFFD4FF); LIST.add(0x7FFFD4FF); }

    /**
     * This color constant "teal" has RGBA8888 code {@code 007F7FFF}, R 0.0, G 0.49803922, B 0.49803922, A 1.0, hue 0.5, saturation 0.49803922, and lightness 0.24901961.
     * <pre>
     * <font style='background-color: #007F7F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #007F7F; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #007F7F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #007F7F'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #007F7F'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #007F7F'>&nbsp;@&nbsp;</font><font style='background-color: #007F7F; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #007F7F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #007F7F; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int teal = 0x007F7FFF;
    static { NAMED.put("teal", 0x007F7FFF); LIST.add(0x007F7FFF); }

    /**
     * This color constant "turquoise" has RGBA8888 code {@code 2ED6C9FF}, R 0.18039216, G 0.8392157, B 0.7882353, A 1.0, hue 0.48710316, saturation 0.65882355, and lightness 0.5098039.
     * <pre>
     * <font style='background-color: #2ED6C9;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #2ED6C9; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #2ED6C9;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #2ED6C9'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #2ED6C9'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #2ED6C9'>&nbsp;@&nbsp;</font><font style='background-color: #2ED6C9; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #2ED6C9;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #2ED6C9; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int turquoise = 0x2ED6C9FF;
    static { NAMED.put("turquoise", 0x2ED6C9FF); LIST.add(0x2ED6C9FF); }

    /**
     * This color constant "sky" has RGBA8888 code {@code 10C0E0FF}, R 0.0627451, G 0.7529412, B 0.8784314, A 1.0, hue 0.5256411, saturation 0.8156863, and lightness 0.47058824.
     * <pre>
     * <font style='background-color: #10C0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #10C0E0; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #10C0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #10C0E0'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #10C0E0'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #10C0E0'>&nbsp;@&nbsp;</font><font style='background-color: #10C0E0; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #10C0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #10C0E0; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int sky = 0x10C0E0FF;
    static { NAMED.put("sky", 0x10C0E0FF); LIST.add(0x10C0E0FF); }

    /**
     * This color constant "cobalt" has RGBA8888 code {@code 0046ABFF}, R 0.0, G 0.27450982, B 0.67058825, A 1.0, hue 0.5984406, saturation 0.67058825, and lightness 0.33529413.
     * <pre>
     * <font style='background-color: #0046AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0046AB; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #0046AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #0046AB'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #0046AB'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #0046AB'>&nbsp;@&nbsp;</font><font style='background-color: #0046AB; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #0046AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0046AB; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int cobalt = 0x0046ABFF;
    static { NAMED.put("cobalt", 0x0046ABFF); LIST.add(0x0046ABFF); }

    /**
     * This color constant "denim" has RGBA8888 code {@code 3088B8FF}, R 0.1882353, G 0.53333336, B 0.72156864, A 1.0, hue 0.5588235, saturation 0.53333336, and lightness 0.45490196.
     * <pre>
     * <font style='background-color: #3088B8;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3088B8; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #3088B8;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #3088B8'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #3088B8'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #3088B8'>&nbsp;@&nbsp;</font><font style='background-color: #3088B8; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #3088B8;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3088B8; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int denim = 0x3088B8FF;
    static { NAMED.put("denim", 0x3088B8FF); LIST.add(0x3088B8FF); }

    /**
     * This color constant "navy" has RGBA8888 code {@code 000080FF}, R 0.0, G 0.0, B 0.5019608, A 1.0, hue 0.6666667, saturation 0.5019608, and lightness 0.2509804.
     * <pre>
     * <font style='background-color: #000080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000080; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000080'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #000080'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #000080'>&nbsp;@&nbsp;</font><font style='background-color: #000080; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000080; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int navy = 0x000080FF;
    static { NAMED.put("navy", 0x000080FF); LIST.add(0x000080FF); }

    /**
     * This color constant "lavender" has RGBA8888 code {@code B991FFFF}, R 0.7254902, G 0.5686275, B 1.0, A 1.0, hue 0.72727275, saturation 0.43137252, and lightness 0.78431374.
     * <pre>
     * <font style='background-color: #B991FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B991FF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #B991FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #B991FF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #B991FF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #B991FF'>&nbsp;@&nbsp;</font><font style='background-color: #B991FF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #B991FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B991FF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int lavender = 0xB991FFFF;
    static { NAMED.put("lavender", 0xB991FFFF); LIST.add(0xB991FFFF); }

    /**
     * This color constant "plum" has RGBA8888 code {@code BE0DC6FF}, R 0.74509805, G 0.050980393, B 0.7764706, A 1.0, hue 0.82612616, saturation 0.7254902, and lightness 0.4137255.
     * <pre>
     * <font style='background-color: #BE0DC6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #BE0DC6; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #BE0DC6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #BE0DC6'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #BE0DC6'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #BE0DC6'>&nbsp;@&nbsp;</font><font style='background-color: #BE0DC6; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #BE0DC6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #BE0DC6; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int plum = 0xBE0DC6FF;
    static { NAMED.put("plum", 0xBE0DC6FF); LIST.add(0xBE0DC6FF); }

    /**
     * This color constant "mauve" has RGBA8888 code {@code AB73ABFF}, R 0.67058825, G 0.4509804, B 0.67058825, A 1.0, hue 0.8333334, saturation 0.21960786, and lightness 0.56078434.
     * <pre>
     * <font style='background-color: #AB73AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #AB73AB; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #AB73AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #AB73AB'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #AB73AB'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #AB73AB'>&nbsp;@&nbsp;</font><font style='background-color: #AB73AB; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #AB73AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #AB73AB; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int mauve = 0xAB73ABFF;
    static { NAMED.put("mauve", 0xAB73ABFF); LIST.add(0xAB73ABFF); }

    /**
     * This color constant "rose" has RGBA8888 code {@code E61E78FF}, R 0.9019608, G 0.11764706, B 0.47058824, A 1.0, hue 0.925, saturation 0.78431374, and lightness 0.5098039.
     * <pre>
     * <font style='background-color: #E61E78;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #E61E78; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #E61E78;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #E61E78'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #E61E78'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #E61E78'>&nbsp;@&nbsp;</font><font style='background-color: #E61E78; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #E61E78;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #E61E78; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int rose = 0xE61E78FF;
    static { NAMED.put("rose", 0xE61E78FF); LIST.add(0xE61E78FF); }

    /**
     * This color constant "raspberry" has RGBA8888 code {@code 911437FF}, R 0.5686275, G 0.078431375, B 0.21568628, A 1.0, hue 0.9533333, saturation 0.4901961, and lightness 0.32352945.
     * <pre>
     * <font style='background-color: #911437;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #911437; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #911437;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #911437'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #911437'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #911437'>&nbsp;@&nbsp;</font><font style='background-color: #911437; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #911437;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #911437; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int raspberry = 0x911437FF;
    static { NAMED.put("raspberry", 0x911437FF); LIST.add(0x911437FF); }

    /**
     * This color constant "YELLOW" has RGBA8888 code {@code FFFF00FF}, R 1.0, G 1.0, B 0.0, A 1.0, hue 0.16666667, saturation 1.0, and lightness 0.5.
     * <pre>
     * <font style='background-color: #FFFF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFF00; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFFF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFFF00'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFFF00'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFFF00'>&nbsp;@&nbsp;</font><font style='background-color: #FFFF00; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFFF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFF00; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int YELLOW = 0xFFFF00FF;
    static { NAMED.put("YELLOW", 0xFFFF00FF); LIST.add(0xFFFF00FF); }

    /**
     * This color constant "BLUE" has RGBA8888 code {@code 0000FFFF}, R 0.0, G 0.0, B 1.0, A 1.0, hue 0.6666667, saturation 1.0, and lightness 0.5.
     * <pre>
     * <font style='background-color: #0000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0000FF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #0000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #0000FF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #0000FF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #0000FF'>&nbsp;@&nbsp;</font><font style='background-color: #0000FF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #0000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0000FF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BLUE = 0x0000FFFF;
    static { NAMED.put("BLUE", 0x0000FFFF); LIST.add(0x0000FFFF); }

    /**
     * This color constant "GOLD" has RGBA8888 code {@code FFD700FF}, R 1.0, G 0.84313726, B 0.0, A 1.0, hue 0.14052288, saturation 1.0, and lightness 0.5.
     * <pre>
     * <font style='background-color: #FFD700;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFD700; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFD700;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFD700'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFD700'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFD700'>&nbsp;@&nbsp;</font><font style='background-color: #FFD700; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFD700;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFD700; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int GOLD = 0xFFD700FF;
    static { NAMED.put("GOLD", 0xFFD700FF); LIST.add(0xFFD700FF); }

    /**
     * This color constant "GRAY" has RGBA8888 code {@code 7F7F7FFF}, R 0.49803922, G 0.49803922, B 0.49803922, A 1.0, hue 0.0, saturation 0.0, and lightness 0.49803922.
     * <pre>
     * <font style='background-color: #7F7F7F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7F7F7F; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #7F7F7F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #7F7F7F'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #7F7F7F'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #7F7F7F'>&nbsp;@&nbsp;</font><font style='background-color: #7F7F7F; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #7F7F7F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7F7F7F; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int GRAY = 0x7F7F7FFF;
    static { NAMED.put("GRAY", 0x7F7F7FFF); LIST.add(0x7F7F7FFF); }

    /**
     * This color constant "ORANGE" has RGBA8888 code {@code FFA500FF}, R 1.0, G 0.64705884, B 0.0, A 1.0, hue 0.10784314, saturation 1.0, and lightness 0.5.
     * <pre>
     * <font style='background-color: #FFA500;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA500; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFA500;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFA500'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFA500'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFA500'>&nbsp;@&nbsp;</font><font style='background-color: #FFA500; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFA500;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA500; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int ORANGE = 0xFFA500FF;
    static { NAMED.put("ORANGE", 0xFFA500FF); LIST.add(0xFFA500FF); }

    /**
     * This color constant "MAGENTA" has RGBA8888 code {@code FF00FFFF}, R 1.0, G 0.0, B 1.0, A 1.0, hue 0.8333333, saturation 1.0, and lightness 0.5.
     * <pre>
     * <font style='background-color: #FF00FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF00FF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF00FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FF00FF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FF00FF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FF00FF'>&nbsp;@&nbsp;</font><font style='background-color: #FF00FF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF00FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF00FF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int MAGENTA = 0xFF00FFFF;
    static { NAMED.put("MAGENTA", 0xFF00FFFF); LIST.add(0xFF00FFFF); }

    /**
     * This color constant "FIREBRICK" has RGBA8888 code {@code B22222FF}, R 0.69803923, G 0.13333334, B 0.13333334, A 1.0, hue 0.0, saturation 0.5647059, and lightness 0.41568628.
     * <pre>
     * <font style='background-color: #B22222;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B22222; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #B22222;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #B22222'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #B22222'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #B22222'>&nbsp;@&nbsp;</font><font style='background-color: #B22222; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #B22222;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B22222; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int FIREBRICK = 0xB22222FF;
    static { NAMED.put("FIREBRICK", 0xB22222FF); LIST.add(0xB22222FF); }

    /**
     * This color constant "SCARLET" has RGBA8888 code {@code FF341CFF}, R 1.0, G 0.20392157, B 0.10980392, A 1.0, hue 0.017621147, saturation 0.8901961, and lightness 0.55490196.
     * <pre>
     * <font style='background-color: #FF341C;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF341C; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF341C;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FF341C'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FF341C'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FF341C'>&nbsp;@&nbsp;</font><font style='background-color: #FF341C; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF341C;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF341C; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int SCARLET = 0xFF341CFF;
    static { NAMED.put("SCARLET", 0xFF341CFF); LIST.add(0xFF341CFF); }

    /**
     * This color constant "WHITE" has RGBA8888 code {@code FFFFFFFF}, R 1.0, G 1.0, B 1.0, A 1.0, hue 0.0, saturation 0.0, and lightness 1.0.
     * <pre>
     * <font style='background-color: #FFFFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFFFF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFFFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFFFFF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFFFFF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFFFFF'>&nbsp;@&nbsp;</font><font style='background-color: #FFFFFF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFFFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFFFF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int WHITE = 0xFFFFFFFF;
    static { NAMED.put("WHITE", 0xFFFFFFFF); LIST.add(0xFFFFFFFF); }

    /**
     * This color constant "SKY" has RGBA8888 code {@code 87CEEBFF}, R 0.5294118, G 0.80784315, B 0.92156863, A 1.0, hue 0.54833335, saturation 0.39215684, and lightness 0.7254902.
     * <pre>
     * <font style='background-color: #87CEEB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #87CEEB; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #87CEEB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #87CEEB'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #87CEEB'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #87CEEB'>&nbsp;@&nbsp;</font><font style='background-color: #87CEEB; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #87CEEB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #87CEEB; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int SKY = 0x87CEEBFF;
    static { NAMED.put("SKY", 0x87CEEBFF); LIST.add(0x87CEEBFF); }

    /**
     * This color constant "FOREST" has RGBA8888 code {@code 228B22FF}, R 0.13333334, G 0.54509807, B 0.13333334, A 1.0, hue 0.33333334, saturation 0.41176474, and lightness 0.33921573.
     * <pre>
     * <font style='background-color: #228B22;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #228B22; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #228B22;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #228B22'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #228B22'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #228B22'>&nbsp;@&nbsp;</font><font style='background-color: #228B22; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #228B22;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #228B22; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int FOREST = 0x228B22FF;
    static { NAMED.put("FOREST", 0x228B22FF); LIST.add(0x228B22FF); }

    /**
     * This color constant "GREEN" has RGBA8888 code {@code 00FF00FF}, R 0.0, G 1.0, B 0.0, A 1.0, hue 0.33333334, saturation 1.0, and lightness 0.5.
     * <pre>
     * <font style='background-color: #00FF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FF00; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00FF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #00FF00'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #00FF00'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #00FF00'>&nbsp;@&nbsp;</font><font style='background-color: #00FF00; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00FF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FF00; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int GREEN = 0x00FF00FF;
    static { NAMED.put("GREEN", 0x00FF00FF); LIST.add(0x00FF00FF); }

    /**
     * This color constant "CHARTREUSE" has RGBA8888 code {@code 7FFF00FF}, R 0.49803922, G 1.0, B 0.0, A 1.0, hue 0.2503268, saturation 1.0, and lightness 0.5.
     * <pre>
     * <font style='background-color: #7FFF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7FFF00; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #7FFF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #7FFF00'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #7FFF00'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #7FFF00'>&nbsp;@&nbsp;</font><font style='background-color: #7FFF00; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #7FFF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7FFF00; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CHARTREUSE = 0x7FFF00FF;
    static { NAMED.put("CHARTREUSE", 0x7FFF00FF); LIST.add(0x7FFF00FF); }

    /**
     * This color constant "MAROON" has RGBA8888 code {@code B03060FF}, R 0.6901961, G 0.1882353, B 0.3764706, A 1.0, hue 0.9375, saturation 0.5019608, and lightness 0.4392157.
     * <pre>
     * <font style='background-color: #B03060;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B03060; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #B03060;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #B03060'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #B03060'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #B03060'>&nbsp;@&nbsp;</font><font style='background-color: #B03060; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #B03060;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B03060; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int MAROON = 0xB03060FF;
    static { NAMED.put("MAROON", 0xB03060FF); LIST.add(0xB03060FF); }

    /**
     * This color constant "RED" has RGBA8888 code {@code FF0000FF}, R 1.0, G 0.0, B 0.0, A 1.0, hue 0.0, saturation 1.0, and lightness 0.5.
     * <pre>
     * <font style='background-color: #FF0000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF0000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF0000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FF0000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FF0000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FF0000'>&nbsp;@&nbsp;</font><font style='background-color: #FF0000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF0000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF0000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int RED = 0xFF0000FF;
    static { NAMED.put("RED", 0xFF0000FF); LIST.add(0xFF0000FF); }

    /**
     * This color constant "CYAN" has RGBA8888 code {@code 00FFFFFF}, R 0.0, G 1.0, B 1.0, A 1.0, hue 0.5, saturation 1.0, and lightness 0.5.
     * <pre>
     * <font style='background-color: #00FFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FFFF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00FFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #00FFFF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #00FFFF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #00FFFF'>&nbsp;@&nbsp;</font><font style='background-color: #00FFFF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00FFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FFFF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CYAN = 0x00FFFFFF;
    static { NAMED.put("CYAN", 0x00FFFFFF); LIST.add(0x00FFFFFF); }

    /**
     * This color constant "BLACK" has RGBA8888 code {@code 000000FF}, R 0.0, G 0.0, B 0.0, A 1.0, hue 0.0, saturation 0.0, and lightness 0.0.
     * <pre>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #000000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BLACK = 0x000000FF;
    static { NAMED.put("BLACK", 0x000000FF); LIST.add(0x000000FF); }

    /**
     * This color constant "VIOLET" has RGBA8888 code {@code EE82EEFF}, R 0.93333334, G 0.50980395, B 0.93333334, A 1.0, hue 0.8333333, saturation 0.4235294, and lightness 0.72156864.
     * <pre>
     * <font style='background-color: #EE82EE;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #EE82EE; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #EE82EE;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #EE82EE'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #EE82EE'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #EE82EE'>&nbsp;@&nbsp;</font><font style='background-color: #EE82EE; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #EE82EE;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #EE82EE; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int VIOLET = 0xEE82EEFF;
    static { NAMED.put("VIOLET", 0xEE82EEFF); LIST.add(0xEE82EEFF); }

    /**
     * This color constant "CORAL" has RGBA8888 code {@code FF7F50FF}, R 1.0, G 0.49803922, B 0.3137255, A 1.0, hue 0.0447619, saturation 0.6862745, and lightness 0.65686274.
     * <pre>
     * <font style='background-color: #FF7F50;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF7F50; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF7F50;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FF7F50'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FF7F50'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FF7F50'>&nbsp;@&nbsp;</font><font style='background-color: #FF7F50; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF7F50;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF7F50; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CORAL = 0xFF7F50FF;
    static { NAMED.put("CORAL", 0xFF7F50FF); LIST.add(0xFF7F50FF); }

    /**
     * This color constant "ROYAL" has RGBA8888 code {@code 4169E1FF}, R 0.25490198, G 0.4117647, B 0.88235295, A 1.0, hue 0.625, saturation 0.62745094, and lightness 0.5686275.
     * <pre>
     * <font style='background-color: #4169E1;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #4169E1; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #4169E1;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #4169E1'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #4169E1'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #4169E1'>&nbsp;@&nbsp;</font><font style='background-color: #4169E1; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #4169E1;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #4169E1; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int ROYAL = 0x4169E1FF;
    static { NAMED.put("ROYAL", 0x4169E1FF); LIST.add(0x4169E1FF); }

    /**
     * This color constant "LIME" has RGBA8888 code {@code 32CD32FF}, R 0.19607843, G 0.8039216, B 0.19607843, A 1.0, hue 0.33333334, saturation 0.60784316, and lightness 0.5.
     * <pre>
     * <font style='background-color: #32CD32;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #32CD32; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #32CD32;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #32CD32'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #32CD32'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #32CD32'>&nbsp;@&nbsp;</font><font style='background-color: #32CD32; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #32CD32;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #32CD32; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int LIME = 0x32CD32FF;
    static { NAMED.put("LIME", 0x32CD32FF); LIST.add(0x32CD32FF); }

    /**
     * This color constant "CLEAR" has RGBA8888 code {@code 00000000}, R 0.0, G 0.0, B 0.0, A 0.0, hue 0.0, saturation 0.0, and lightness 0.0.
     * <pre>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #000000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CLEAR = 0x00000000;
    static { NAMED.put("CLEAR", 0x00000000); LIST.add(0x00000000); }

    /**
     * This color constant "LIGHT_GRAY" has RGBA8888 code {@code BFBFBFFF}, R 0.7490196, G 0.7490196, B 0.7490196, A 1.0, hue 0.0, saturation 0.0, and lightness 0.7490196.
     * <pre>
     * <font style='background-color: #BFBFBF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #BFBFBF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #BFBFBF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #BFBFBF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #BFBFBF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #BFBFBF'>&nbsp;@&nbsp;</font><font style='background-color: #BFBFBF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #BFBFBF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #BFBFBF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int LIGHT_GRAY = 0xBFBFBFFF;
    static { NAMED.put("LIGHT_GRAY", 0xBFBFBFFF); LIST.add(0xBFBFBFFF); }

    /**
     * This color constant "NAVY" has RGBA8888 code {@code 00007FFF}, R 0.0, G 0.0, B 0.49803922, A 1.0, hue 0.6666667, saturation 0.49803922, and lightness 0.24901961.
     * <pre>
     * <font style='background-color: #00007F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00007F; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00007F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #00007F'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #00007F'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #00007F'>&nbsp;@&nbsp;</font><font style='background-color: #00007F; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00007F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00007F; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int NAVY = 0x00007FFF;
    static { NAMED.put("NAVY", 0x00007FFF); LIST.add(0x00007FFF); }

    /**
     * This color constant "BROWN" has RGBA8888 code {@code 8B4513FF}, R 0.54509807, G 0.27058825, B 0.07450981, A 1.0, hue 0.06944444, saturation 0.47058827, and lightness 0.30980393.
     * <pre>
     * <font style='background-color: #8B4513;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #8B4513; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #8B4513;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #8B4513'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #8B4513'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #8B4513'>&nbsp;@&nbsp;</font><font style='background-color: #8B4513; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #8B4513;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #8B4513; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BROWN = 0x8B4513FF;
    static { NAMED.put("BROWN", 0x8B4513FF); LIST.add(0x8B4513FF); }

    /**
     * This color constant "SALMON" has RGBA8888 code {@code FA8072FF}, R 0.98039216, G 0.5019608, B 0.44705883, A 1.0, hue 0.017156873, saturation 0.5333333, and lightness 0.7137255.
     * <pre>
     * <font style='background-color: #FA8072;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FA8072; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FA8072;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FA8072'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FA8072'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FA8072'>&nbsp;@&nbsp;</font><font style='background-color: #FA8072; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FA8072;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FA8072; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int SALMON = 0xFA8072FF;
    static { NAMED.put("SALMON", 0xFA8072FF); LIST.add(0xFA8072FF); }

    /**
     * This color constant "PURPLE" has RGBA8888 code {@code A020F0FF}, R 0.627451, G 0.1254902, B 0.9411765, A 1.0, hue 0.7692308, saturation 0.8156863, and lightness 0.53333336.
     * <pre>
     * <font style='background-color: #A020F0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #A020F0; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #A020F0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #A020F0'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #A020F0'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #A020F0'>&nbsp;@&nbsp;</font><font style='background-color: #A020F0; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #A020F0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #A020F0; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int PURPLE = 0xA020F0FF;
    static { NAMED.put("PURPLE", 0xA020F0FF); LIST.add(0xA020F0FF); }

    /**
     * This color constant "DARK_GRAY" has RGBA8888 code {@code 3F3F3FFF}, R 0.24705882, G 0.24705882, B 0.24705882, A 1.0, hue 0.0, saturation 0.0, and lightness 0.24705882.
     * <pre>
     * <font style='background-color: #3F3F3F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3F3F3F; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #3F3F3F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #3F3F3F'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #3F3F3F'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #3F3F3F'>&nbsp;@&nbsp;</font><font style='background-color: #3F3F3F; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #3F3F3F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3F3F3F; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int DARK_GRAY = 0x3F3F3FFF;
    static { NAMED.put("DARK_GRAY", 0x3F3F3FFF); LIST.add(0x3F3F3FFF); }

    /**
     * This color constant "SLATE" has RGBA8888 code {@code 708090FF}, R 0.4392157, G 0.5019608, B 0.5647059, A 1.0, hue 0.5833333, saturation 0.12549022, and lightness 0.5019608.
     * <pre>
     * <font style='background-color: #708090;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #708090; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #708090;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #708090'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #708090'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #708090'>&nbsp;@&nbsp;</font><font style='background-color: #708090; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #708090;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #708090; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int SLATE = 0x708090FF;
    static { NAMED.put("SLATE", 0x708090FF); LIST.add(0x708090FF); }

    /**
     * This color constant "TAN" has RGBA8888 code {@code D2B48CFF}, R 0.8235294, G 0.7058824, B 0.54901963, A 1.0, hue 0.0952381, saturation 0.2745098, and lightness 0.6862745.
     * <pre>
     * <font style='background-color: #D2B48C;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2B48C; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D2B48C;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #D2B48C'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #D2B48C'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #D2B48C'>&nbsp;@&nbsp;</font><font style='background-color: #D2B48C; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D2B48C;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2B48C; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int TAN = 0xD2B48CFF;
    static { NAMED.put("TAN", 0xD2B48CFF); LIST.add(0xD2B48CFF); }

    /**
     * This color constant "PINK" has RGBA8888 code {@code FF69B4FF}, R 1.0, G 0.4117647, B 0.7058824, A 1.0, hue 0.9166666, saturation 0.58823526, and lightness 0.7058824.
     * <pre>
     * <font style='background-color: #FF69B4;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF69B4; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF69B4;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FF69B4'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FF69B4'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FF69B4'>&nbsp;@&nbsp;</font><font style='background-color: #FF69B4; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF69B4;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF69B4; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int PINK = 0xFF69B4FF;
    static { NAMED.put("PINK", 0xFF69B4FF); LIST.add(0xFF69B4FF); }

    /**
     * This color constant "OLIVE" has RGBA8888 code {@code 6B8E23FF}, R 0.41960785, G 0.5568628, B 0.13725491, A 1.0, hue 0.22118384, saturation 0.41960788, and lightness 0.34705883.
     * <pre>
     * <font style='background-color: #6B8E23;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #6B8E23; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #6B8E23;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #6B8E23'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #6B8E23'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #6B8E23'>&nbsp;@&nbsp;</font><font style='background-color: #6B8E23; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #6B8E23;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #6B8E23; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int OLIVE = 0x6B8E23FF;
    static { NAMED.put("OLIVE", 0x6B8E23FF); LIST.add(0x6B8E23FF); }

    /**
     * This color constant "TEAL" has RGBA8888 code {@code 007F7FFF}, R 0.0, G 0.49803922, B 0.49803922, A 1.0, hue 0.5, saturation 0.49803922, and lightness 0.24901961.
     * <pre>
     * <font style='background-color: #007F7F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #007F7F; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #007F7F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #007F7F'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #007F7F'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #007F7F'>&nbsp;@&nbsp;</font><font style='background-color: #007F7F; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #007F7F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #007F7F; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int TEAL = 0x007F7FFF;
    static { NAMED.put("TEAL", 0x007F7FFF); LIST.add(0x007F7FFF); }

    /**
     * This color constant "GOLDENROD" has RGBA8888 code {@code DAA520FF}, R 0.85490197, G 0.64705884, B 0.1254902, A 1.0, hue 0.119175635, saturation 0.7294118, and lightness 0.49019608.
     * <pre>
     * <font style='background-color: #DAA520;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #DAA520; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #DAA520;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #DAA520'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #DAA520'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #DAA520'>&nbsp;@&nbsp;</font><font style='background-color: #DAA520; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #DAA520;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #DAA520; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int GOLDENROD = 0xDAA520FF;
    static { NAMED.put("GOLDENROD", 0xDAA520FF); LIST.add(0xDAA520FF); }

    /**
     * All names for colors in this palette, in alphabetical order. You can fetch the corresponding packed float color
     * by looking up a name in {@link #NAMED}. This includes aliases, since those can be looked up like other names.
     */
    public static final Array<String> NAMES;
    static {
        ALIASES.put("grey", gray);
        ALIASES.put("gold", saffron);
        ALIASES.put("puce", mauve);
        ALIASES.put("sand", tan);
        ALIASES.put("skin", peach); // Yes, I am aware that there is more than one skin color, but this can only map to one.
        ALIASES.put("coral", salmon);
        ALIASES.put("azure", sky);
        ALIASES.put("ocean", teal);
        ALIASES.put("sapphire", cobalt);
        NAMED.putAll(ALIASES);
        NAMES = NAMED.keys().toArray();
        NAMES.sort();
    }

    /**
     * Appends standard RGBA Color instances to the map in {@link Colors}, using the names in {@link #NAMES} (which
     * are either "lowercased" instead of "ALL_UPPER_CASE", or are already present in Colors). This doesn't need any
     * changes to be made to Colors in order for it to be compatible; just remember that the colors originally in Colors
     * use "UPPER_CASE" and these may also use "lowercase" with no spaces.
     */
    public static void appendToKnownColors(){
        for(ObjectIntMap.Entry<String> ent : NAMED) {
            Color editing = new Color();
            Color.rgba8888ToColor(editing, ent.value);
            Colors.put(ent.key, editing);
        }
    }

    /**
     * Modifies the Palette by adding a color with its name.
     * @param name the name of the color to add; should not be already present in {@link #NAMED}
     * @param rgba8888 an int color as RGBA8888 to associate with the given name
     * @return true if this entered a new color, or false if name was already present (so nothing was added).
     */
    public static boolean addColor(String name, int rgba8888) {
        if(NAMED.containsKey(name))
            return false;
        NAMED.put(name, rgba8888);
        LIST.add(rgba8888);
        NAMES.add(name);
        NAMES.sort();
        return true;
    }
}
