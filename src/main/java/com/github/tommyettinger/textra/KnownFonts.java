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

package com.github.tommyettinger.textra;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;

import static com.github.tommyettinger.textra.Font.DistanceFieldType.*;

/**
 * Preconfigured static {@link Font} instances, with any important metric adjustments already applied. This uses a
 * singleton to ensure each font exists at most once, and implements {@link LifecycleListener} to ensure that when the
 * disposal stage of the lifecycle is called, then all Font instances here will be disposed and assigned null. This may
 * do more regarding its LifecycleListener code in the future, if Android turns out to need more work.
 * <br>
 * Typical usage involves calling one of the static methods like {@link #getCozette()} or {@link #getGentiumSDF()} to get a
 * particular Font. This knows a fair amount of fonts, but it doesn't require the image assets for all of those to be
 * present in a game -- only the files mentioned in the documentation for a method are needed, and only if you call that
 * method. It's likely that many games would only use one Font, and so would generally only need a .fnt file, a
 * .png file, and some kind of license file. They could ignore all other assets required by other fonts. The files this
 * class needs are looked for in the assets root folder by default, but you can change the names or locations of asset
 * files with {@link #setAssetPrefix(String)}.
 * <br>
 * There's some documentation for every known Font, including a link to a preview image and a listing of all required
 * files to use a Font. The required files include any license you need to abide by; this doesn't necessarily belong in
 * the {@code assets} folder like the rest of the files! Most of these fonts are either licensed under the OFL
 * or some Creative Commons license; the CC ones typically require attribution, but none of the fonts restrict usage to
 * noncommercial projects, and all are free as in beer as well.
 * <br>
 * There are some special features in Font that are easier to use with parts of this class. {@link #getStandardFamily()}
 * pre-populates a FontFamily so you can switch between different fonts with the {@code [@Sans]} syntax.
 * {@link #addEmoji(Font)} adds all of Twitter's emoji from the <a href="https://github.com/twitter/twemoji">Twemoji</a>
 * project to a given font, which lets you enter emoji with the {@code [+man scientist, dark skin tone]} syntax or the
 * generally-easier {@code [+👨🏿‍🔬]} syntax. If you want to use names for emoji, you may want to consult "Twemoji.atlas"
 * for the exact names used; some names changed from the standard because of technical restrictions.
 */
public final class KnownFonts implements LifecycleListener {
    private static KnownFonts instance;

    private KnownFonts() {
        if (Gdx.app == null)
            throw new IllegalStateException("Gdx.app cannot be null; initialize KnownFonts in create() or later.");
        Gdx.app.addLifecycleListener(this);
    }

    private static void initialize() {
        if (instance == null)
            instance = new KnownFonts();
    }

    private String prefix = "";

    /**
     * Changes the String prepended to each filename this looks up. This should end in "/" if the files have the same
     * name but are in a subdirectory (don't use backslashes for paths; they aren't cross-platform compatible). It can
     * end without a "/" to prepend a String to the name of each file this looks up. The default prefix is "".
     * @param prefix the new prefix to use before each filename, such as a subdirectory ending in "/"
     */
    public static void setAssetPrefix(String prefix) {
        initialize();
        if(prefix != null)
            instance.prefix = prefix;
    }

    private Font astarry;

    /**
     * Returns a very large fixed-width Font already configured to use a square font with 45-degree angled sections,
     * based on the typeface used on the Atari ST console. This font only supports ASCII, but it supports all of it.
     * Caches the result for later calls. The font is "a-starry", based on "Atari ST (low-res)" by Damien Guard; it is
     * available under a CC-BY-SA-3.0 license, which requires attribution to Damien Guard (and technically Tommy
     * Ettinger, because he made changes in a-starry) if you use it.
     * <br>
     * Preview: <a href="https://i.imgur.com/UhwRrFc.png">Image link</a> (uses width=8, height=8)
     * <br>
     * This also looks good if you scale it so its height is twice its width. For small sizes, you should stick to
     * multiples of 8. This "A Starry Tall" version is present in {@link #getAll()} and {@link #getAllStandard()}.
     * <br>
     * Preview: <a href="https://i.imgur.com/gZNsszP.png">Image link</a> (uses width=8, height=16)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/AStarry-standard.fnt">AStarry-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/AStarry-standard.png">AStarry-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/AStarry-License.txt">AStarry-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font A Starry
     */
    public static Font getAStarry() {
        initialize();
        if (instance.astarry == null) {
            try {
                instance.astarry = new Font(instance.prefix + "AStarry-standard.fnt",
                        instance.prefix + "AStarry-standard.png", STANDARD, 12, 0, 0, -12, true)
                        .scaleTo(8, 8)
                        .setUnderlineMetrics(0.25f, 0f, 0f, 0.25f).setStrikethroughMetrics(0.25f, 0.125f, 0f, 0.25f)
                        .setInlineImageMetrics(0f, 6f, 0f)
                        .setTextureFilter().setName("A Starry");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.astarry != null)
            return new Font(instance.astarry);
        throw new RuntimeException("Assets for getAStarry() not found.");
    }

    private Font astarryMSDF;

    /**
     * Returns a Font already configured to use a square font with 45-degree angled sections, based on the
     * typeface used on the Atari ST console, that should scale cleanly to many sizes. This font only supports ASCII,
     * but it supports all of it. Caches the result for later calls. The font is "a-starry", based on "Atari ST
     * (low-res)" by Damien Guard; it is available under a CC-BY-SA-3.0 license, which requires attribution to Damien
     * Guard (and technically Tommy Ettinger, because he made changes in a-starry) if you use it. This uses the
     * Multi-channel Signed Distance Field (MSDF) technique as opposed to the normal Signed Distance Field technique,
     * which gives the rendered font sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * If you only need sizes in small integer multiples of 8 pixels, you might get sharper-looking results from
     * {@link #getAStarry()}.
     * <br>
     * Preview: <a href="https://i.imgur.com/RVUvzJi.png">Image link</a> (uses width=10, height=10, crispness=2.0)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/AStarry-msdf.fnt">AStarry-msdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/AStarry-msdf.png">AStarry-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/AStarry-License.txt">AStarry-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font A Starry using MSDF
     */
    public static Font getAStarryMSDF() {
        initialize();
        if (instance.astarryMSDF == null) {
            try {
                instance.astarryMSDF = new Font(instance.prefix + "AStarry-msdf.fnt",
                        instance.prefix + "AStarry-msdf.png", MSDF, 0, 0, 0, 0, true)
                        .setUnderlinePosition(0f, -0.2f).setStrikethroughPosition(0f, -0.2f)
                        .scaleTo(10, 10).setCrispness(2f).setName("A Starry (MSDF)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.astarryMSDF != null)
            return new Font(instance.astarryMSDF);
        throw new RuntimeException("Assets for getAStarryMSDF() not found.");
    }

    private Font bitter;

    /**
     * Returns a Font already configured to use a light-weight variable-width slab serif font with good Latin and
     * Cyrillic script support, that should scale pretty well from a height of about 160 down to a height of maybe 30.
     * Caches the result for later calls. The font used is Bitter, a free (OFL) typeface by <a href="https://github.com/solmatas/BitterPro">The Bitter Project</a>.
     * It supports quite a lot of Latin-based scripts and Cyrillic, but does not really cover Greek or any other
     * scripts. This font can look good at its natural size, which uses width roughly equal to height,
     * or squashed so height is slightly smaller. Bitter looks very similar to {@link #getGentium()}, except that Bitter
     * is quite a bit lighter, with thinner strokes and stylistic flourishes on some glyphs.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect. Unlike most other
     * fonts here, this does not use makeGridGlyphs, because it would make underline and strikethrough much thicker than
     * other strokes in the font. This does mean that strikethrough starts too far to the left, and extends too far to
     * the right, unfortunately, but its weight matches.
     * <br>
     * Preview: <a href="https://i.imgur.com/7LwKBTZ.png">Image link</a> (uses width=33, height=30)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Bitter-standard.fnt">Bitter-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Bitter-standard.png">Bitter-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Bitter-License.txt">Bitter-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Bitter-Light.ttf
     */
    public static Font getBitter() {
        initialize();
        if (instance.bitter == null) {
            try {
                instance.bitter = new Font(instance.prefix + "Bitter-standard.fnt",
                        instance.prefix + "Bitter-standard.png", STANDARD, 0, -20, 0, 0, true)
                        .setInlineImageMetrics(0f, 20f, 0f).setLineMetrics(0, -0.0625f, 0f, -0.5f).setDescent(-16f)
                        .scaleTo(33, 30).setTextureFilter().setName("Bitter");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.bitter != null)
            return new Font(instance.bitter);
        throw new RuntimeException("Assets for getBitter() not found.");
    }

    private Font canada;

    /**
     * Returns a Font already configured to use a very-legible variable-width font with strong support for Canadian
     * Aboriginal Syllabic, that should scale pretty well from a height of about 86 down to a height of maybe 30.
     * Caches the result for later calls. The font used is Canada1500, a free (public domain, via CC0) typeface by Ray
     * Larabie. It supports quite a lot of Latin-based scripts, Greek, Cyrillic, Canadian Aboriginal Syllabic, arrows,
     * many dingbats, and more. This font can look good at its natural size, which uses width roughly equal to height,
     * or narrowed down so width is smaller.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/5rg7O36.png">Image link</a> (uses width=30, height=35)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Canada1500-standard.fnt">Canada1500-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Canada1500-standard.png">Canada1500-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Canada1500-License.txt">Canada1500-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Canada1500.ttf
     */
    public static Font getCanada() {
        initialize();
        if (instance.canada == null) {
            try {
                instance.canada = new Font(instance.prefix + "Canada1500-standard.fnt",
                        instance.prefix + "Canada1500-standard.png",
                        STANDARD, 0, 8, 0, 0, true).setDescent(-13f)
                        .setInlineImageMetrics(0f, 12f, 4f).setLineMetrics(0f, -0.125f, 0f, -0.25f)
                        .scaleTo(30, 35).setTextureFilter().setName("Canada1500");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.canada != null)
            return new Font(instance.canada);
        throw new RuntimeException("Assets for getCanada() not found.");
    }

    private Font cascadiaMono;

    /**
     * Returns a Font already configured to use a quirky fixed-width font with good Unicode support
     * and a humanist style, that should scale well from a height of about 60 pixels to about 15 pixels.
     * Caches the result for later calls. The font used is Cascadia Code Mono, an open-source (SIL Open Font
     * License) typeface by Microsoft (see <a href="https://github.com/microsoft/cascadia-code">Microsoft's page</a>).
     * It supports a lot of glyphs, including most extended Latin, Greek, Braille, and Cyrillic.
     * This uses a fairly-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/AGsj34B.png">Image link</a> (uses width=10, height=20)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/CascadiaMono-standard.fnt">CascadiaMono-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/CascadiaMono-standard.png">CascadiaMono-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cascadia-License.txt">Cascadia-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Cascadia Code Mono
     */
    public static Font getCascadiaMono() {
        initialize();
        if (instance.cascadiaMono == null) {
            try {
                instance.cascadiaMono = new Font(instance.prefix + "CascadiaMono-standard.fnt",
                        instance.prefix + "CascadiaMono-standard.png", STANDARD, 0f, -4f, 0f, 0f, true)
                        .setTextureFilter().scaleTo(10, 20).setName("Cascadia Mono");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.cascadiaMono != null)
            return new Font(instance.cascadiaMono);
        throw new RuntimeException("Assets for getCascadiaMono() not found.");
    }

    private Font cascadiaMonoMSDF;

    /**
     * Returns a Font already configured to use a quirky fixed-width font with good Unicode support
     * and a humanist style, that should scale cleanly to even very large sizes (using an MSDF technique).
     * Caches the result for later calls. The font used is Cascadia Code Mono, an open-source (SIL Open Font
     * License) typeface by Microsoft (see <a href="https://github.com/microsoft/cascadia-code">Microsoft's page</a>).
     * It supports a lot of glyphs,
     * including most extended Latin, Greek, Braille, and Cyrillic. This uses the Multi-channel Signed Distance
     * Field (MSDF) technique as opposed to the normal Signed Distance Field technique, which gives the rendered font
     * sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * Preview: <a href="https://i.imgur.com/qeQJ9TI.png">Image link</a> (uses width=10, height=20)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/CascadiaMono-msdf.fnt">CascadiaMono-msdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/CascadiaMono-msdf.png">CascadiaMono-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cascadia-License.txt">Cascadia-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Cascadia Code Mono using MSDF
     */
    public static Font getCascadiaMonoMSDF() {
        initialize();
        if (instance.cascadiaMonoMSDF == null) {
            try {
                instance.cascadiaMonoMSDF = new Font(instance.prefix + "CascadiaMono-msdf.fnt",
                        instance.prefix + "CascadiaMono-msdf.png", MSDF, 0f, 0f, -4f, -4f, true)
                        .scaleTo(10, 20).setName("Cascadia Mono (MSDF)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.cascadiaMonoMSDF != null)
            return new Font(instance.cascadiaMonoMSDF);
        throw new RuntimeException("Assets for getCascadiaMonoMSDF() not found.");
    }

    private Font caveat;

    /**
     * Returns a Font already configured to use a variable-width handwriting font with support for extended Latin and
     * Cyrillic, that should scale pretty well from a height of about 160 down to a height of maybe 20. It will look
     * sharper and more aliased at smaller sizes, but should be relatively smooth at a height of 32 or so. This is a
     * sort of natural handwriting, as opposed to the formal script in {@link #getTangerine()}.
     * Caches the result for later calls. The font used is Caveat, a free (OFL) typeface designed by Pablo Impallari.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/gxDY0vs.png">Image link</a> (uses width=32, height=32)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Caveat-standard.fnt">Caveat-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Caveat-standard.png">Caveat-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Caveat-License.txt">Caveat-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Caveat.ttf
     */
    public static Font getCaveat() {
        initialize();
        if (instance.caveat == null) {
            try {
                instance.caveat = new Font(instance.prefix + "Caveat-standard.fnt",
                        instance.prefix + "Caveat-standard.png",
                        STANDARD, 0, 16, 0, 0, true)
                        .setDescent(-8f)
                        .setLineMetrics(0.1f, -0.25f, 0f, -0.4f)
                        .setInlineImageMetrics(0f, 44f, 0f)
                        .scaleTo(32, 32).setTextureFilter().setName("Caveat");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.caveat != null)
            return new Font(instance.caveat);
        throw new RuntimeException("Assets for getCaveat() not found.");
    }

    private Font cozette;

    /**
     * Returns a Font configured to use a cozy fixed-width bitmap font,
     * <a href="https://github.com/slavfox/Cozette">Cozette by slavfox</a>. Cozette has broad coverage of Unicode,
     * including Greek, Cyrillic, Braille, and tech-related icons. This does not scale well except to integer
     * multiples, but it should look very crisp at its default size of 7x13 pixels. This defaults to having
     * {@link Font#integerPosition} set to true, which helps keep it pixel-perfect if 1 world unit is 1 pixel, but can
     * cause major visual issues if 1 world unit corresponds to much more than 1 pixel.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/uAsZnzo.png">Image link</a> (uses width=7, height=13,
     * useIntegerPositions(true); this size is small enough to make the scaled text unreadable in some places)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cozette-standard.fnt">Cozette-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cozette-standard.png">Cozette-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cozette-License.txt">Cozette-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that represents the 7x13px font Cozette
     */
    public static Font getCozette() {
        initialize();
        if (instance.cozette == null) {
            try {
                instance.cozette = new Font(instance.prefix + "Cozette-standard.fnt",
                        instance.prefix + "Cozette-standard.png", STANDARD, 1.5f, 5f, 0, 0, false)
                        .useIntegerPositions(true)
                        .setDescent(-3f)
                        .setUnderlinePosition(0.5f, 0f)
                        .setStrikethroughPosition(0.5f, 0f)
                        .setInlineImageMetrics(-24f, -4f, 0f)
                        .setName("Cozette");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.cozette != null)
            return new Font(instance.cozette);
        throw new RuntimeException("Assets for getCozette() not found.");
    }

    private Font dejaVuSansMono;

    /**
     * A nice old standby font with very broad language support, DejaVu Sans Mono is fixed-width and can be clearly
     * readable but doesn't do anything unusual stylistically. It really does handle a lot of glyphs; not only does this
     * have practically all Latin glyphs in Unicode (enough to support everything from Icelandic to Vietnamese), it has
     * Greek (including Extended), Cyrillic (including some optional glyphs), IPA, Armenian (maybe the only font here to
     * do so), Georgian (which won't be treated correctly by some case-insensitive code, so it should only be used if
     * case doesn't matter), and Lao. It has full box drawing and Braille support, handles a wide variety of math
     * symbols, technical marks, and dingbats, etc. This uses the Multi-channel Signed Distance
     * Field (MSDF) technique as opposed to the normal Signed Distance Field technique, which gives the rendered font
     * sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * Preview: <a href="https://i.imgur.com/V0zYSkO.png">Image link</a> (uses width=9, height=20)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVuSansMono-msdf.fnt">DejaVuSansMono-msdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVuSansMono-msdf.png">DejaVuSansMono-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVuSansMono-License.txt">DejaVuSansMono-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font DejaVu Sans Mono using MSDF
     */
    public static Font getDejaVuSansMono() {
        initialize();
        if (instance.dejaVuSansMono == null) {
            try {
                instance.dejaVuSansMono = new Font(instance.prefix + "DejaVuSansMono-msdf.fnt",
                        instance.prefix + "DejaVuSansMono-msdf.png", MSDF, 0f, 0f, 0f, 0f, true)
                        .setDescent(-6f).scaleTo(9, 20)
                        .setLineMetrics(0f, 0f, 0f, -0.25f).setCrispness(2f).setName("DejaVu Sans Mono (MSDF)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.dejaVuSansMono != null)
            return new Font(instance.dejaVuSansMono);
        throw new RuntimeException("Assets for getDejaVuSansMono() not found.");
    }

    private Font gentium;

    /**
     * Returns a Font already configured to use a variable-width serif font with excellent Unicode support, that should
     * scale well from a height of about 132 down to a height of 34. Caches the result for later calls. The font used is
     * Gentium, an open-source (SIL Open Font License) typeface by SIL (see
     * <a href="https://software.sil.org/gentium/">SIL's page on Gentium here</a>). It supports a lot of glyphs,
     * including quite a bit of extended Latin, Greek, and Cyrillic, as well as some less-common glyphs from various
     * real languages. This does not use a distance field effect, as opposed to {@link #getGentiumSDF()}. You may want
     * to stick using just fonts that avoid distance fields if you have a family of fonts.
     * <br>
     * Preview: <a href="https://i.imgur.com/ZsWgxEp.png">Image link</a> (uses width=31, height=35)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-standard.fnt">Gentium-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-standard.png">Gentium-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-License.txt">Gentium-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Gentium.ttf
     */
    public static Font getGentium() {
        initialize();
        if (instance.gentium == null) {
            try {
                instance.gentium = new Font(instance.prefix + "Gentium-standard.fnt",
                        instance.prefix + "Gentium-standard.png", Font.DistanceFieldType.STANDARD, 0f, 10f, 0f, 0f, true)
                        .scaleTo(31, 35).setInlineImageMetrics(-4f, 24f, 0f).setLineMetrics(0f, -0.2f, 0f, -0.4f)
                        .setDescent(-9f)
                        .setTextureFilter().setName("Gentium");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.gentium != null)
            return new Font(instance.gentium);
        throw new RuntimeException("Assets for getGentium() not found.");
    }

    private Font gentiumMSDF;

    /**
     * Returns a Font already configured to use a variable-width serif font with excellent Unicode support, that should
     * scale cleanly to even very large sizes (using an MSDF technique). You usually will want to reduce the line height
     * of this Font after you scale it; using {@code KnownFonts.getGentium().scaleTo(50, 45).adjustLineHeight(0.625f)}
     * usually works. Caches the result for later calls. The font used is Gentium, an open-source (SIL Open Font
     * License) typeface by SIL (see <a href="https://software.sil.org/gentium/">SIL's page on Gentium here</a>). It
     * supports a lot of glyphs, including quite a
     * bit of extended Latin, Greek, and Cyrillic, as well as some less-common glyphs from various real languages. This
     * uses the Multi-channel Signed Distance Field (MSDF) technique as opposed to the normal Signed Distance Field
     * technique, which gives the rendered font sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * Preview: <a href="https://i.imgur.com/BkFUEue.png">Image link</a> (uses width=50, height=45,
     * adjustLineHeight(0.625f), setCrispness(3f))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-msdf.fnt">Gentium-msdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-msdf.png">Gentium-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-License.txt">Gentium-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Gentium.ttf using MSDF
     */
    public static Font getGentiumMSDF() {
        initialize();
        if (instance.gentiumMSDF == null) {
            try {
                instance.gentiumMSDF = new Font(instance.prefix + "Gentium-msdf.fnt",
                        instance.prefix + "Gentium-msdf.png", MSDF, 0f, 0f, 0f, 0f, true)
                        .scaleTo(50, 45).adjustLineHeight(0.625f).setCrispness(3f).setName("Gentium (MSDF)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.gentiumMSDF != null)
            return new Font(instance.gentiumMSDF);
        throw new RuntimeException("Assets for getGentiumMSDF() not found.");
    }

    private Font gentiumSDF;

    /**
     * Returns a Font already configured to use a variable-width serif font with excellent Unicode support, that should
     * scale cleanly to even very large sizes (using an SDF technique). You usually will want to reduce the line height
     * of this Font after you scale it; using {@code KnownFonts.getGentium().scaleTo(50, 45).adjustLineHeight(0.625f)}
     * usually works. Caches the result for later calls. The font used is Gentium, an open-source (SIL Open Font
     * License) typeface by SIL (see <a href="https://software.sil.org/gentium/">SIL's page on Gentium here</a>). It
     * supports a lot of glyphs, including quite a
     * bit of extended Latin, Greek, and Cyrillic, as well as some less-common glyphs from various real languages. This
     * uses the Signed Distance Field (SDF) technique, which may be slightly fuzzy when zoomed in heavily, but should be
     * crisp enough when zoomed out.
     * <br>
     * Preview: <a href="https://i.imgur.com/tbiLDL3.png">Image link</a> (uses width=50, height=45,
     * adjustLineHeight(0.625f), setCrispness(1.5f))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-sdf.fnt">Gentium-sdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-sdf.png">Gentium-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-License.txt">Gentium-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Gentium.ttf using SDF
     */
    public static Font getGentiumSDF() {
        initialize();
        if (instance.gentiumSDF == null) {
            try {
                instance.gentiumSDF = new Font(instance.prefix + "Gentium-sdf.fnt",
                        instance.prefix + "Gentium-sdf.png", SDF, 4f, 0f, 0f, 0f, true)
                        .scaleTo(50, 45).adjustLineHeight(0.625f)
                        .setLineMetrics(0.05f, 0.25f, 0f, -0.5f).setInlineImageMetrics(0f, -20f, 0f)
                    .setCrispness(1.5f).setName("Gentium (SDF)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.gentiumSDF != null)
            return new Font(instance.gentiumSDF);
        throw new RuntimeException("Assets for getGentiumSDF() not found.");
    }
    private Font gentiumUnItalic;

    /**
     * Returns a Font already configured to use a variable-width "italic-like" serif font with excellent Unicode
     * support, that should scale well from a height of about 97 down to a height of 30.
     * Caches the result for later calls. The font used is Gentium, an open-source (SIL Open Font License) typeface by
     * SIL (see <a href="https://software.sil.org/gentium/">SIL's page on Gentium here</a>), but this took Gentim Italic
     * and removed the 8-degree slant it had, so it looks like a regular face but with the different serif style and
     * "flow" of an italic font. It supports a lot of glyphs,
     * including quite a bit of extended Latin, Greek, and Cyrillic, as well as some less-common glyphs from various
     * real languages. This does not use a distance field effect. You may want
     * to stick using just fonts that avoid distance fields if you have a family of fonts.
     * <br>
     * Preview: <a href="">Image link</a> (uses width=31, height=35)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/GentiumUnItalic-standard.fnt">GentiumUnItalic-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/GentiumUnItalic-standard.png">GentiumUnItalic-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-License.txt">Gentium-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Gentium-Un-Italic.ttf
     */
    public static Font getGentiumUnItalic() {
        initialize();
        if (instance.gentiumUnItalic == null) {
            try {
                instance.gentiumUnItalic = new Font(instance.prefix + "GentiumUnItalic-standard.fnt",
                        instance.prefix + "GentiumUnItalic-standard.png", Font.DistanceFieldType.STANDARD, 0f, -4f, 0f, -12f, true)
                        .scaleTo(60, 36).setTextureFilter()
                        .setLineMetrics(0f, 0.15f, 0f, -0.3125f).setInlineImageMetrics(0f, -8f, 4f)
                        .setName("Gentium Un-Italic");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.gentiumUnItalic != null)
            return new Font(instance.gentiumUnItalic);
        throw new RuntimeException("Assets for getGentiumUnItalic() not found.");
    }


    private Font goNotoUniversalSDF;

    /**
     * Returns a Font already configured to use a variable-width sans-serif font with extreme pan-Unicode support, that
     * should scale cleanly to even very large sizes (using an SDF technique). Caches the result for later calls. The
     * font used is Go Noto Universal, an open-source (SIL Open Font License) typeface that modifies Noto Sans by Google
     * (see <a href="https://github.com/satbyy/go-noto-universal">Go Noto Universal's page is here</a>, and
     * <a href="https://notofonts.github.io/">Noto Fonts have a page here</a>). It supports... most glyphs, from all
     * languages, including essentially all extended Latin, Greek, Cyrillic, Chinese, Japanese, Korean, Armenian,
     * Ethiopic, Canadian Aboriginal scripts, Yi, Javanese... Pretty much every script is here, plus symbols for math,
     * music, and other usage. The baseline may be slightly uneven at larger sizes, but should even out when height is
     * less than 40 or so. This uses the Signed Distance Field (SDF) technique, which may be slightly fuzzy when zoomed
     * in heavily, but should be crisp enough when zoomed out. The texture this uses is larger than many of the others
     * here, at 4096x4096 pixels, but the file isn't too large; in fact, the 2048x2048 textures Gentium-msdf.png and
     * Twemoji.png are each larger than GoNotoUniversal-sdf.png . The .fnt has 24350 glyphs plus extensive kerning info,
     * though, so it is quite large.
     * <br>
     * A quirk of this particular .fnt file is that it uses features specific to TextraTypist; as far as I know, it
     * cannot be read by the libGDX BitmapFont class. These features are simply how it stores metric values -- as float,
     * rather than only as int. You should probably not try to load GoNotoUniversal-sdf.fnt with BitmapFont or
     * DistanceFieldFont in libGDX. Using floats is very helpful for the distance field effect; without them, most
     * glyphs would render slightly off from the intended position, due to rounding to an int instead of using a float.
     * <br>
     * Preview: <a href="https://i.imgur.com/Daco1P4.png">Image link</a> (uses width=65.25, height=51,
     * adjustLineHeight(0.625f), setCrispness(1.8f))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/GoNotoUniversal-sdf.fnt">GoNotoUniversal-sdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/GoNotoUniversal-sdf.png">GoNotoUniversal-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/GoNotoUniversal-License.txt">GoNotoUniversal-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font GoNotoCurrent.ttf using SDF
     */
    public static Font getGoNotoUniversalSDF() {
        initialize();
        if (instance.goNotoUniversalSDF == null) {
            try {
                instance.goNotoUniversalSDF = new Font(instance.prefix + "GoNotoUniversal-sdf.fnt",
                        instance.prefix + "GoNotoUniversal-sdf.png", SDF, 0f, 0f, 0f, 0f, true)
                        .scaleTo(65.25f, 51)
                        .adjustLineHeight(0.625f)
                        .setCrispness(1.8f)
                        .setLineMetrics(0f, 0.375f, 0f, -0.4f).setInlineImageMetrics(0f, -12f, 0f)
                        .setName("Go Noto Universal (SDF)");

//                System.out.println(instance.goNotoUniversalSDF.cellWidth);
//                System.out.println(instance.goNotoUniversalSDF.cellHeight);

                // Above are used to see how big the font actually is, since due to scaling we don't know.
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.goNotoUniversalSDF != null)
            return new Font(instance.goNotoUniversalSDF);
        throw new RuntimeException("Assets for getGoNotoUniversalSDF() not found.");
    }

    private Font hanazono;

    /**
     * Returns a Font already configured to use a variable-width, narrow font with nearly-complete CJK character
     * coverage, plus Latin, Greek, and Cyrillic, that should scale pretty well down, but not up.
     * Caches the result for later calls. The font used is Hanazono (HanMinA, specifically), a free (OFL) typeface.
     * This uses a somewhat-small standard bitmap font because of how many glyphs are present (over 34000); it might not
     * scale as well as other standard bitmap fonts here. You may want to consider {@link #getGoNotoUniversalSDF()} if
     * you can use an SDF font, since it scales up with higher quality.
     * Otherwise, this may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/l6hQtxT.png">Image link</a> (uses width=16, height=20)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Hanazono-standard.fnt">Hanazono-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Hanazono-standard.png">Hanazono-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Hanazono-License.txt">Hanazono-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font HanMinA.ttf
     */
    public static Font getHanazono() {
        initialize();
        if (instance.hanazono == null) {
            try {
                instance.hanazono = new Font(instance.prefix + "Hanazono-standard.fnt",
                        instance.prefix + "Hanazono-standard.png", STANDARD, 0, 0, 0, 0, false)
                        .setDescent(-6f).scaleTo(16, 20).setInlineImageMetrics(0, -4f, 0f)
                        .setTextureFilter().setName("Hanazono");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.hanazono != null)
            return new Font(instance.hanazono);
        throw new RuntimeException("Assets for getHanazono() not found.");
    }

    private Font ibm8x16;

    /**
     * Returns a Font configured to use a classic, nostalgic fixed-width bitmap font,
     * IBM 8x16 from the early, oft-beloved computer line. This font is notably loaded
     * from a SadConsole format file, which shouldn't affect how it looks (but in reality,
     * it might). This does not scale except to integer multiples, but it should look very
     * crisp at its default size of 8x16 pixels. This supports some extra characters, but
     * not at the typical Unicode codepoints.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * This does not include a license because the source, <a href="https://github.com/Thraka/SadConsole/tree/master/Fonts">SadConsole's fonts</a>,
     * did not include one. It is doubtful that IBM would have any issues with respectful use
     * of their signature font throughout the 1980s, but if the legality is concerning, you
     * can use {@link #getCozette()} or {@link #getQuanPixel()} for a different bitmap font. There
     * is also {@link #getAStarry()} for a non-pixel font styled after a font from the same era.
     * <br>
     * Preview: <a href="https://i.imgur.com/HYdpU9k.png">Image link</a> (uses width=8, height=16, done with
     * fitCell(8, 16, false))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/IBM-8x16-standard.font">IBM-8x16-standard.font</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/IBM-8x16-standard.png">IBM-8x16-standard.png</a></li>
     * </ul>
     *
     * @return the Font object that represents an 8x16 font included with early IBM computers
     */
    public static Font getIBM8x16() {
        initialize();
        if (instance.ibm8x16 == null) {
            try {
                instance.ibm8x16 = new Font(instance.prefix, "IBM-8x16-standard.font", true)
                        .setInlineImageMetrics(-24, 0, 0).fitCell(8, 16, false).setName("IBM 8x16").setDescent(-3f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.ibm8x16 != null)
            return new Font(instance.ibm8x16);
        throw new RuntimeException("Assets for getIBM8x16() not found.");
    }

    private Font inconsolata;

    /**
     * A customized version of Inconsolata LGC, a fixed-width geometric font that supports a large range of Latin,
     * Greek, and Cyrillic glyphs, plus box drawing and some dingbat characters (like zodiac signs). The original font
     * Inconsolata is by Raph Levien, and various other contributors added support for other languages. This does not
     * use a distance field effect, as opposed to {@link #getInconsolataMSDF()}.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/dnahJGX.png">Image link</a> (uses width=10, height=26)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-Custom-standard.fnt">Inconsolata-LGC-Custom-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-Custom-standard.png">Inconsolata-LGC-Custom-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-License.txt">Inconsolata-LGC-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Inconsolata LGC Custom
     */
    public static Font getInconsolata() {
        initialize();
        if (instance.inconsolata == null) {
            try {
                instance.inconsolata = new Font(instance.prefix + "Inconsolata-LGC-Custom-standard.fnt",
                        instance.prefix + "Inconsolata-LGC-Custom-standard.png", STANDARD, 1f, 6f, -4f, 0f, true)
                        .setLineMetrics(0f, 0f, 0f, -0.4f).setInlineImageMetrics(0f, 4f, 0f).setDescent(-21f)
                        .scaleTo(10, 26).setTextureFilter().setName("Inconsolata LGC");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.inconsolata != null)
            return new Font(instance.inconsolata);
        throw new RuntimeException("Assets for getInconsolata() not found.");
    }

    private Font inconsolataMSDF;

    /**
     * A customized version of Inconsolata LGC, a fixed-width geometric font that supports a large range of Latin,
     * Greek, and Cyrillic glyphs, plus box drawing and some dingbat characters (like zodiac signs). The original font
     * Inconsolata is by Raph Levien, and various other contributors added support for other languages. This uses the
     * Multi-channel Signed Distance Field (MSDF) technique as opposed to the normal Signed Distance Field technique,
     * which gives the rendered font sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * Preview: <a href="https://i.imgur.com/ot66v1S.png">Image link</a> (uses width=10, height=26)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-Custom-msdf.fnt">Inconsolata-LGC-Custom-msdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-Custom-msdf.png">Inconsolata-LGC-Custom-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-License.txt">Inconsolata-LGC-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Inconsolata LGC Custom using MSDF
     */
    public static Font getInconsolataMSDF() {
        initialize();
        if (instance.inconsolataMSDF == null) {
            try {
                instance.inconsolataMSDF = new Font(instance.prefix + "Inconsolata-LGC-Custom-msdf.fnt",
                        instance.prefix + "Inconsolata-LGC-Custom-msdf.png", MSDF, 1f, 1f, -8f, -8f, true)
                        .scaleTo(12, 26).setCrispness(1.2f).setName("Inconsolata LGC (MSDF)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.inconsolataMSDF != null)
            return new Font(instance.inconsolataMSDF);
        throw new RuntimeException("Assets for getInconsolataMSDF() not found.");
    }

    private Font iosevka;

    /**
     * Returns a Font already configured to use a highly-legible fixed-width font with good Unicode support
     * and a sans-serif geometric style. Does not use a distance field effect, and is sized best at 9x25 pixels.
     * Caches the result for later calls. The font used is Iosevka, an open-source (SIL Open Font License) typeface by
     * <a href="https://be5invis.github.io/Iosevka/">Belleve Invis</a>, and it uses several customizations
     * thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of extended Latin,
     * Greek, and Cyrillic.
     * This Font is already configured with {@link Font#fitCell(float, float, boolean)}, and repeated calls to fitCell()
     * have an unknown effect; you may want to stick to scaling this and not re-fitting if you encounter issues.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/Fbw7ZIx.png">Image link</a> (uses .scaleTo(10, 24).fitCell(10, 24, false))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-standard.fnt">Iosevka-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-standard.png">Iosevka-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.md">Iosevka-License.md</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Iosevka.ttf
     */
    public static Font getIosevka() {
        initialize();
        if (instance.iosevka == null) {
            try {
                instance.iosevka = new Font(instance.prefix + "Iosevka-standard.fnt",
                        instance.prefix + "Iosevka-standard.png", STANDARD, -2f, 12f, 0f, 0f, true)
                        .scaleTo(10, 24).fitCell(10, 24, false)
                        .setDescent(-10f).setLineMetrics(0f, -0.125f, 0f, -0.25f).setInlineImageMetrics(0f, 12f, 0f)
                        .setTextureFilter().setName("Iosevka");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.iosevka != null)
            return new Font(instance.iosevka);
        throw new RuntimeException("Assets for getIosevka() not found.");
    }

    private Font iosevkaMSDF;

    /**
     * Returns a Font already configured to use a highly-legible fixed-width font with good Unicode support
     * and a sans-serif geometric style, that should scale cleanly to even very large sizes (using an MSDF technique).
     * Caches the result for later calls. The font used is Iosevka, an open-source (SIL Open Font License) typeface by
     * <a href="https://be5invis.github.io/Iosevka/">Belleve Invis</a>, and it uses several customizations
     * thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of extended Latin,
     * Greek, and Cyrillic.
     * This Font is already configured with {@link Font#fitCell(float, float, boolean)}, and repeated calls to fitCell()
     * have an unknown effect; you may want to stick to scaling this and not re-fitting if you encounter issues.
     * This uses the Multi-channel Signed Distance Field (MSDF) technique as opposed to the normal Signed Distance Field
     * technique, which gives the rendered font sharper edges and precise corners instead of rounded tips on strokes.
     * However, using a distance field makes it effectively impossible to mix fonts using a FontFamily (any variation in
     * distance field settings would make some fonts in the family blurry and others too sharp).
     * <br>
     * Preview: <a href="https://i.imgur.com/Xbm1mjE.png">Image link</a> (uses
     * .setCrispness(0.75f).scaleTo(12, 26).fitCell(10, 25, false))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-msdf.fnt">Iosevka-msdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-msdf.png">Iosevka-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.md">Iosevka-License.md</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Iosevka.ttf using MSDF
     */
    public static Font getIosevkaMSDF() {
        initialize();
        if (instance.iosevkaMSDF == null) {
            try {
                // NOTE: If the .fnt file is changed, the manual adjustment to '_' (id=95) will be lost. yoffset was changed to 4.
                // This should be OK now that this uses the box-drawing underline.
                instance.iosevkaMSDF = new Font(instance.prefix + "Iosevka-msdf.fnt",
                        instance.prefix + "Iosevka-msdf.png", MSDF, 1f, 0f, 0f, 0f, true).setDescent(-12)
                        .setLineMetrics(0.25f, 0.125f, 0f, -0.4f)
                        .setCrispness(2.5f).scaleTo(12, 26).fitCell(10, 25, false)
                        .setName("Iosevka (MSDF)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.iosevkaMSDF != null)
            return new Font(instance.iosevkaMSDF);
        throw new RuntimeException("Assets for getIosevkaMSDF() not found.");
    }

    private Font iosevkaSDF;

    /**
     * Returns a Font already configured to use a highly-legible fixed-width font with good Unicode support
     * and a sans-serif geometric style, that should scale cleanly to fairly large sizes (using an SDF technique).
     * Caches the result for later calls. The font used is Iosevka, an open-source (SIL Open Font License) typeface by
     * <a href="https://be5invis.github.io/Iosevka/">Belleve Invis</a>, and it uses several customizations
     * thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of extended Latin,
     * Greek, and Cyrillic.
     * This Font is already configured with {@link Font#fitCell(float, float, boolean)}, and repeated calls to fitCell()
     * have an unknown effect; you may want to stick to scaling this and not re-fitting if you encounter issues.
     * This uses the Signed Distance Field (SDF) technique as opposed to the Multi-channel Signed Distance Field
     * technique that {@link #getIosevkaMSDF()} uses, which isn't as sharp at large sizes but can look a little better
     * at small sizes. However, using a distance field makes it effectively impossible to mix fonts using a FontFamily
     * (any variation in distance field settings would make some fonts in the family blurry and others too sharp).
     * <br>
     * Preview: <a href="https://i.imgur.com/0Z1vPlo.png">Image link</a> (uses
     * .setCrispness(0.75f).scaleTo(12, 26).fitCell(10, 25, false))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-sdf.fnt">Iosevka-sdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-sdf.png">Iosevka-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.md">Iosevka-License.md</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Iosevka.ttf using SDF
     */
    public static Font getIosevkaSDF() {
        initialize();
        if (instance.iosevkaSDF == null) {
            try {
                // NOTE: If the .fnt file is changed, the manual adjustment to '_' (id=95) will be lost. yoffset was changed to 4.
                // This should be OK now that this uses the box-drawing underline.
                instance.iosevkaSDF = new Font(instance.prefix + "Iosevka-sdf.fnt",
                        instance.prefix + "Iosevka-sdf.png", SDF, 2f, 0f, -2f, -2f, true)
                        .setLineMetrics(0.25f, -0.125f, 0f, -0.4f).setInlineImageMetrics(0f, 8f, 0f)
                        .setCrispness(0.75f).scaleTo(12, 26).fitCell(10, 25, false)
                        .setName("Iosevka (SDF)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.iosevkaSDF != null)
            return new Font(instance.iosevkaSDF);
        throw new RuntimeException("Assets for getIosevkaSDF() not found.");
    }

    private Font iosevkaSlab;

    /**
     * Returns a Font already configured to use a highly-legible fixed-width font with good Unicode support
     * and a slab-serif geometric style. Does not use a distance field effect, and is sized best at 9x25 pixels.
     * Caches the result for later calls. The font used is Iosevka with Slab style, an open-source (SIL Open Font
     * License) typeface by <a href="https://be5invis.github.io/Iosevka/">Belleve Invis</a>, and it uses several
     * customizations thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of
     * extended Latin, Greek, and Cyrillic.
     * This Font is already configured with {@link Font#fitCell(float, float, boolean)}, and repeated calls to fitCell()
     * have an unknown effect; you may want to stick to scaling this and not re-fitting if you encounter issues.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/1hvINjC.png">Image link</a> (uses .scaleTo(10, 24).fitCell(10, 24, false))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-standard.fnt">Iosevka-Slab-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-standard.png">Iosevka-Slab-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.md">Iosevka-License.md</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Iosevka-Slab.ttf
     */
    public static Font getIosevkaSlab() {
        initialize();
        if (instance.iosevkaSlab == null) {
            try {
                instance.iosevkaSlab = new Font(instance.prefix + "Iosevka-Slab-standard.fnt",
                        instance.prefix + "Iosevka-Slab-standard.png", STANDARD, 0f, 12f, 0f, 0f, true)
                        .scaleTo(10, 24).fitCell(10, 24, false)
                        .setDescent(-10f).setLineMetrics(0f, -0.125f, 0f, -0.25f).setInlineImageMetrics(0f, 12f, 0f)
                        .setTextureFilter().setName("Iosevka Slab");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.iosevkaSlab != null)
            return new Font(instance.iosevkaSlab);
        throw new RuntimeException("Assets for getIosevkaSlab() not found.");
    }

    private Font iosevkaSlabMSDF;

    /**
     * Returns a Font already configured to use a highly-legible fixed-width font with good Unicode support
     * and a slab-serif geometric style, that should scale cleanly to even very large sizes (using an MSDF technique).
     * Caches the result for later calls. The font used is Iosevka with Slab style, an open-source (SIL Open Font
     * License) typeface by <a href="https://be5invis.github.io/Iosevka/">Belleve Invis</a>, and it uses several
     * customizations thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of
     * extended Latin, Greek, and Cyrillic.
     * This Font is already configured with {@link Font#fitCell(float, float, boolean)}, and repeated calls to fitCell()
     * have an unknown effect; you may want to stick to scaling this and not re-fitting if you encounter issues.
     * This uses the Multi-channel Signed Distance Field (MSDF) technique as opposed to the normal Signed Distance Field
     * technique, which gives the rendered font sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * Preview: <a href="https://i.imgur.com/WGrr8el.png">Image link</a> (uses
     * .setCrispness(0.75f).scaleTo(12, 26).fitCell(10, 25, false))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-msdf.fnt">Iosevka-Slab-msdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-msdf.png">Iosevka-Slab-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.md">Iosevka-License.md</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Iosevka-Slab.ttf using MSDF
     */
    public static Font getIosevkaSlabMSDF() {
        initialize();
        if (instance.iosevkaSlabMSDF == null) {
            try {
                instance.iosevkaSlabMSDF = new Font(instance.prefix + "Iosevka-Slab-msdf.fnt",
                        instance.prefix + "Iosevka-Slab-msdf.png", MSDF, 1f, 0f, 0f, 0f, true).setDescent(-12)
                        .setLineMetrics(0.25f, 0.125f, 0f, -0.4f)
                        .setCrispness(2.25f).scaleTo(12, 26).fitCell(10, 25, false)
                        .setName("Iosevka Slab (MSDF)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.iosevkaSlabMSDF != null)
            return new Font(instance.iosevkaSlabMSDF);
        throw new RuntimeException("Assets for getIosevkaSlabMSDF() not found.");
    }

    private Font iosevkaSlabSDF;

    /**
     * Returns a Font already configured to use a highly-legible fixed-width font with good Unicode support
     * and a slab-serif geometric style, that should scale cleanly to fairly large sizes (using an SDF technique).
     * Caches the result for later calls. The font used is Iosevka with Slab style, an open-source (SIL Open Font
     * License) typeface by <a href="https://be5invis.github.io/Iosevka/">Belleve Invis</a>, and it uses several
     * customizations thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of
     * extended Latin, Greek, and Cyrillic.
     * This Font is already configured with {@link Font#fitCell(float, float, boolean)}, and repeated calls to fitCell()
     * have an unknown effect; you may want to stick to scaling this and not re-fitting if you encounter issues.
     * This uses the Signed Distance Field (SDF) technique as opposed to the Multi-channel Signed Distance Field
     * technique that {@link #getIosevkaSlabMSDF()} uses, which isn't as sharp at large sizes but can look a little
     * better at small sizes.
     * <br>
     * Preview: <a href="https://i.imgur.com/dCuWg8o.png">Image link</a> (uses
     * .setCrispness(0.75f).scaleTo(12, 26).fitCell(10, 25, false))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-sdf.fnt">Iosevka-Slab-sdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-sdf.png">Iosevka-Slab-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.md">Iosevka-License.md</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Iosevka-Slab.ttf using SDF
     */
    public static Font getIosevkaSlabSDF() {
        initialize();
        if (instance.iosevkaSlabSDF == null) {
            try {
                // NOTE: If the .fnt file is changed, the manual adjustment to '_' (id=95) will be lost. yoffset was changed to 4.
                // This might be OK now that this uses the box-drawing underline.
                instance.iosevkaSlabSDF = new Font(instance.prefix + "Iosevka-Slab-sdf.fnt",
                        instance.prefix + "Iosevka-Slab-sdf.png", SDF, 2f, 0f, -2f, -2f, true)
                        .setLineMetrics(0.25f, -0.125f, 0f, -0.4f).setInlineImageMetrics(0f, 8f, 0f)
                        .setCrispness(0.75f).scaleTo(12, 26).fitCell(10, 25, false)
                        .setName("Iosevka Slab (SDF)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.iosevkaSlabSDF != null)
            return new Font(instance.iosevkaSlabSDF);
        throw new RuntimeException("Assets for getIosevkaSlabSDF() not found.");
    }

    private Font kingthingsFoundation;

    /**
     * Returns a Font already configured to use a fairly-legible variable-width ornamental/medieval font, that should
     * scale pretty well from a height of about 90 down to a height of maybe 30.
     * Caches the result for later calls. The font used is Kingthings Foundation, a free (custom permissive license)
     * typeface; this has faux-bold applied already in order to make some ornamental curls visible at more sizes. You
     * can still apply bold again using markup. It supports only ASCII. You may want to also look at
     * {@link #getKingthingsPetrock() Kingthings Petrock}; where Petrock is less-ornamented, Foundation is heavily
     * ornamented, and Foundation may make sense for text associated with writers of high social status.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/ulyOx6Q.png">Image link</a> (uses scaleTo(23, 31))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/KingthingsFoundation-standard.fnt">KingthingsFoundation-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/KingthingsFoundation-standard.png">KingthingsFoundation-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-License.txt">Kingthings-License.txt</a></li>
     * </ul>
     * You may instead want the non-bold version, but this doesn't have a pre-made instance in KnownFonts:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/KingthingsFoundation-Light-standard.fnt">KingthingsFoundation-Light-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/KingthingsFoundation-Light-standard.png">KingthingsFoundation-Light-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-License.txt">Kingthings-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font KingthingsFoundation.ttf
     */
    public static Font getKingthingsFoundation() {
        initialize();
        if (instance.kingthingsFoundation == null) {
            try {
                instance.kingthingsFoundation = new Font(instance.prefix + "KingthingsFoundation-standard.fnt",
                        instance.prefix + "KingthingsFoundation-standard.png", STANDARD, 0, 40, 0, 25, true)
                        .setUnderlineMetrics(0f, -0.125f, 0.125f, -0.2f).setStrikethroughMetrics(0f, -0.125f, 0.125f, -0.2f)
                        .setInlineImageMetrics(0f, 44f, 0f).setDescent(-24f)
                        .scaleTo(23, 31).setTextureFilter().setName("Kingthings Foundation");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.kingthingsFoundation != null)
            return new Font(instance.kingthingsFoundation);
        throw new RuntimeException("Assets for getKingthingsFoundation() not found.");
    }

    private Font kingthingsPetrock;

    /**
     * Returns a Font already configured to use a clearly-legible variable-width medieval font, that should
     * scale pretty well from a height of about 90 down to a height of maybe 30.
     * Caches the result for later calls. The font used is Kingthings Petrock, a free (custom permissive license)
     * typeface; it has a visual style similar to one used by some popular classic rock bands. It supports only ASCII
     * and a small amount of extended Latin. Kingthings Petrock is similar to
     * {@link #getKingthingsFoundation() Kingthings Foundation}, but Petrock isn't as heavily-ornamented, and looks more
     * like "every-day usable" medieval or maybe Renaissance text.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/KnFQTxj.png">Image link</a> (uses scaleTo(25, 32))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/KingthingsPetrock-standard.fnt">KingthingsPetrock-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/KingthingsPetrock-standard.png">KingthingsPetrock-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-License.txt">Kingthings-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font KingthingsPetrock.ttf
     */
    public static Font getKingthingsPetrock() {
        initialize();
        if (instance.kingthingsPetrock == null) {
            try {
                instance.kingthingsPetrock = new Font(instance.prefix + "KingthingsPetrock-standard.fnt",
                        instance.prefix + "KingthingsPetrock-standard.png", STANDARD, 0, 8, 2, 0, true)
                        .setInlineImageMetrics(0f, 6f, 0f)
                        .scaleTo(25, 32).setTextureFilter().setName("Kingthings Petrock");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.kingthingsPetrock != null)
            return new Font(instance.kingthingsPetrock);
        throw new RuntimeException("Assets for getKingthingsPetrock() not found.");
    }

    private Font libertinusSerif;

    /**
     * Returns a Font already configured to use a variable-width serif font with good Unicode support, that should
     * scale cleanly to fairly large sizes or down to about 20 pixels.
     * Caches the result for later calls. The font used is Libertinus Serif, an open-source (SIL Open Font
     * License) typeface. It supports a lot of glyphs, including quite a bit of extended Latin, Greek, and Cyrillic.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect. Earlier versions of
     * this font used an MSDF effect, but that doesn't work well with kerning, so the spacing between letters was odd,
     * and in general the font just didn't look as good as the similar {@link #getGentiumSDF()} or even
     * {@link #getGentium()}. The MSDF files are still present in the same directory where they were, but they are no
     * longer used by TextraTypist.
     * <br>
     * Preview: <a href="https://i.imgur.com/E0dWFl5.png">Image link</a> (uses width=40, height=34)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/LibertinusSerif-standard.fnt">LibertinusSerif-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/LibertinusSerif-standard.png">LibertinusSerif-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/LibertinusSerif-License.txt">LibertinusSerif-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font LibertinusSerif.ttf
     */
    public static Font getLibertinusSerif() {
        initialize();
        if (instance.libertinusSerif == null) {
            try {
                instance.libertinusSerif = new Font(instance.prefix + "LibertinusSerif-standard.fnt",
                        instance.prefix + "LibertinusSerif-standard.png", STANDARD, 0, 6, 0, 0, true)
                        .setLineMetrics(0.05f, 0f, 0.0625f, -0.25f)
                        .scaleTo(40, 34).setTextureFilter().setName("Libertinus Serif");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.libertinusSerif != null)
            return new Font(instance.libertinusSerif);
        throw new RuntimeException("Assets for getLibertinusSerif() not found.");
    }

    private Font nowAlt;

    /**
     * Returns a Font already configured to use a variable-width geometric sans-serif font, that should
     * scale cleanly to fairly large sizes or down to about 20 pixels.
     * Caches the result for later calls. The font used is Now Alt, an open-source (SIL Open Font License) typeface by
     * Hanken Design Co. It has decent glyph coverage for most European languages, but doesn't fully support Greek or
     * Cyrillic. This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very
     * well. This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/PzquU2s.png">Image link</a> (uses width=28, height=30)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Now-Alt-standard.fnt">Now-Alt-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Now-Alt-standard.png">Now-Alt-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Now-Alt-License.txt">Now-Alt-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font NowAlt.otf
     */
    public static Font getNowAlt() {
        initialize();
        if (instance.nowAlt == null) {
            try {
                instance.nowAlt = new Font(instance.prefix + "Now-Alt-standard.fnt",
                        instance.prefix + "Now-Alt-standard.png", STANDARD, 0, 16, 0, 0, true)
                        .setDescent(-12f).setLineMetrics(0f, -0.1f, 0f, 0f).setInlineImageMetrics(0f, 16f, 0f)
                        .scaleTo(28, 30).setTextureFilter().setName("Now Alt");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.nowAlt != null)
            return new Font(instance.nowAlt);
        throw new RuntimeException("Assets for getNowAlt() not found.");
    }

    private Font openSans;

    /**
     * Returns a Font configured to use a clean variable-width font, Open Sans. It has good extended-Latin coverage, but
     * does not support Greek, Cyrillic, or other scripts. This makes an especially large font by default, but can be
     * scaled down nicely.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/0Dxednl.png">Image link</a> (uses
     * .scaleTo(20, 32).adjustLineHeight(0.875f))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenSans-standard.fnt">OpenSans-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenSans-standard.png">OpenSans-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenSans-License.txt">OpenSans-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that represents the variable-width font OpenSans
     */
    public static Font getOpenSans() {
        initialize();
        if (instance.openSans == null) {
            try {
                instance.openSans = new Font(instance.prefix + "OpenSans-standard.fnt",
                        instance.prefix + "OpenSans-standard.png", STANDARD, 0, 16, 0, 0, true).setDescent(-8f)
                        .setLineMetrics(0f, -0.125f, 0f, -0.4f).setInlineImageMetrics(0f, 4f, 4f)
                        .scaleTo(20, 28).setTextureFilter().setName("OpenSans");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.openSans != null)
            return new Font(instance.openSans);
        throw new RuntimeException("Assets for getOpenSans() not found.");
    }

    private Font oxanium;

    /**
     * Returns a Font already configured to use a variable-width "science-fiction/high-tech" font, that should
     * scale pretty well down, but not up.
     * Caches the result for later calls. The font used is Oxanium, a free (OFL) typeface. It supports a lot of Latin
     * and extended Latin, but not Greek or Cyrillic.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/SQLYHD0.png">Image link</a> (uses width=31, height=35)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Oxanium-standard.fnt">Oxanium-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Oxanium-standard.png">Oxanium-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Oxanium-License.txt">Oxanium-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Oxanium.ttf
     */
    public static Font getOxanium() {
        initialize();
        if (instance.oxanium == null) {
            try {
                instance.oxanium = new Font(instance.prefix + "Oxanium-standard.fnt",
                        instance.prefix + "Oxanium-standard.png", STANDARD, 0, 2, -4, 0, true).setDescent(-12f)
                        .setLineMetrics(0f, -0.125f, 0f, 0f).setInlineImageMetrics(0f, 12f, 0f)
                        .scaleTo(31, 35).setTextureFilter().setName("Oxanium");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.oxanium != null)
            return new Font(instance.oxanium);
        throw new RuntimeException("Assets for getOxanium() not found.");
    }

    private Font quanPixel;

    /**
     * Returns a Font configured to use a small variable-width bitmap font with extensive coverage of Asian scripts,
     * <a href="https://diaowinner.itch.io/galmuri-extended">QuanPixel</a>. QuanPixel has good coverage of Unicode,
     * including all of Greek, at least most of Cyrillic, a good amount of extended Latin, all of Katakana and Hiragana,
     * many Hangul syllables, and literally thousands of CJK ideograms. This does not scale well except to integer
     * multiples, but it should look very crisp at its default size of about 8 pixels tall with variable width. This
     * defaults to having {@link Font#integerPosition} set to true, which helps keep it pixel-perfect if 1 world unit is
     * 1 pixel, but can cause major visual issues if 1 world unit corresponds to much more than 1 pixel.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/1Z3PTBF.png">Image link</a> (uses width=(not set), height=8,
     * useIntegerPositions(true); this size is small enough to make the scaled text unreadable in some places)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/QuanPixel-standard.fnt">QuanPixel-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/QuanPixel-standard.png">QuanPixel-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/QuanPixel-License.txt">QuanPixel-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that represents the 8px tall font QuanPixel
     */
    public static Font getQuanPixel() {
        initialize();
        if (instance.quanPixel == null) {
            try {
                // Manual adjustment: id 95 ('_') had yoffset changed from 5, to 6.
                // This makes underlines use a different row than the bottom of letters.
                instance.quanPixel = new Font(instance.prefix + "QuanPixel-standard.fnt",
                        instance.prefix + "QuanPixel-standard.png", STANDARD, 0, 2, 0, 2, false)
                        .setLineMetrics(0.16f, -0.0625f, 0f, 0f).setInlineImageMetrics(-32f, -4f, 0f)
                        .useIntegerPositions(true).setDescent(-4f)
                        .setName("QuanPixel");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.quanPixel != null)
            return new Font(instance.quanPixel);
        throw new RuntimeException("Assets for getQuanPixel() not found.");
    }

    private Font robotoCondensed;

    /**
     * Returns a Font already configured to use a very-legible condensed variable-width font with excellent Unicode
     * support, that should scale pretty well from a height of about 62 down to a height of maybe 20.
     * Caches the result for later calls. The font used is Roboto Condensed, a free (Apache 2.0) typeface by Christian
     * Robertson. It supports Latin-based scripts almost entirely, plus Greek, (extended) Cyrillic, and more.
     * This font is meant to be condensed in its natural appearance, but can be scaled to be wider if desired.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/kLGnvyY.png">Image link</a> (uses width=21, height=30)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/RobotoCondensed-standard.fnt">RobotoCondensed-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/RobotoCondensed-standard.png">RobotoCondensed-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/RobotoCondensed-License.txt">RobotoCondensed-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font RobotoCondensed.ttf
     */
    public static Font getRobotoCondensed() {
        initialize();
        if (instance.robotoCondensed == null) {
            try {
                instance.robotoCondensed = new Font(instance.prefix + "RobotoCondensed-standard.fnt",
                        instance.prefix + "RobotoCondensed-standard.png", STANDARD, 0, 25, 0, 20, true)
                        .setDescent(-15f).setInlineImageMetrics(0f, 8f, 6f)
                        .setUnderlineMetrics(0f, 0f, 0f, -0.4f).setStrikethroughMetrics(0f, -0.0625f, 0f, -0.4f)
                        .scaleTo(20, 32).setTextureFilter().setName("Roboto Condensed");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.robotoCondensed != null)
            return new Font(instance.robotoCondensed);
        throw new RuntimeException("Assets for getRobotoCondensed() not found.");
    }

    private Font tangerine;

    /**
     * Returns a Font already configured to use a variable-width script font, that should
     * scale pretty well down, but not up.
     * Caches the result for later calls. The font used is Tangerine, a free (OFL) typeface. It supports Latin only,
     * with a little support for Western European languages, but not really anything else. It looks elegant, though.
     * This uses a very-large standard bitmap font, which lets it be scaled down OK but not scaled up very well.
     * Some sizes may look very sharply-aliased with this version of Tangerine, but {@link #getTangerineSDF()} doesn't
     * seem to have that problem.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/ULAM7bL.png">Image link</a> (uses width=48, height=32)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Tangerine-standard.fnt">Tangerine-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Tangerine-standard.png">Tangerine-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Tangerine-License.txt">Tangerine-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Tangerine.ttf
     */
    public static Font getTangerine() {
        initialize();
        if (instance.tangerine == null) {
            try {
                instance.tangerine = new Font(instance.prefix + "Tangerine-standard.fnt",
                        instance.prefix + "Tangerine-standard.png", STANDARD, 0f, 16f, 0f, 0f, true)
                        .setUnderlineMetrics(0f, 0.125f, 0f, -0.6f).setStrikethroughMetrics(0f, 0f, 0f, -0.6f)
                        .setInlineImageMetrics(4f, -12f, 0f)
                        .scaleTo(48, 32).setTextureFilter().setName("Tangerine");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.tangerine != null)
            return new Font(instance.tangerine);
        throw new RuntimeException("Assets for getTangerine() not found.");
    }

    private Font tangerineSDF;

    /**
     * Returns a Font already configured to use a variable-width script font, that should
     * scale pretty well down, but not up.
     * Caches the result for later calls. The font used is Tangerine, a free (OFL) typeface. It supports Latin only,
     * with a little support for Western European languages, but not really anything else. It looks elegant, though.
     * This uses the Signed Distance Field (SDF) technique, which may be slightly fuzzy when zoomed in heavily, but
     * should be crisp enough when zoomed out. If you need to mix in images such as with {@link #addEmoji(Font)}, you
     * may be better off with {@link #getTangerine()}, the standard-bitmap-font version.
     * <br>
     * Preview: <a href="https://i.imgur.com/6vMSsdO.png">Image link</a> (uses width=48, height=32, setCrispness(0.375f))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Tangerine-sdf.fnt">Tangerine-sdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Tangerine-sdf.png">Tangerine-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Tangerine-License.txt">Tangerine-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Tangerine.ttf using SDF
     */
    public static Font getTangerineSDF() {
        initialize();
        if (instance.tangerineSDF == null) {
            try {
                instance.tangerineSDF = new Font(instance.prefix + "Tangerine-sdf.fnt",
                        instance.prefix + "Tangerine-sdf.png", SDF, 0f, 28f, 0f, 0, false)
                        .setLineMetrics(0.0625f, 0f, 0f, 0f).setInlineImageMetrics(0f, 0f, 4f)
                        .scaleTo(48, 32).setCrispness(0.375f).setName("Tangerine (SDF)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.tangerineSDF != null)
            return new Font(instance.tangerineSDF);
        throw new RuntimeException("Assets for getTangerineSDF() not found.");
    }

    private Font kaffeesatz;

    /**
     * Returns a Font already configured to use a variable-width, narrow, humanist font, that should
     * scale pretty well down, but not up.
     * Caches the result for later calls. The font used is Yanone Kaffeesatz, a free (OFL) typeface. It supports a lot
     * of Latin, Cyrillic, and some extended Latin, but not Greek.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/lGbqBQA.png">Image link</a> (uses width=26, height=30)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/YanoneKaffeesatz-standard.fnt">YanoneKaffeesatz-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/YanoneKaffeesatz-standard.png">YanoneKaffeesatz-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/YanoneKaffeesatz-License.txt">YanoneKaffeesatz-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font YanoneKaffeesatz.ttf
     */
    public static Font getYanoneKaffeesatz() {
        initialize();
        if (instance.kaffeesatz == null) {
            try {
                instance.kaffeesatz = new Font(instance.prefix + "YanoneKaffeesatz-standard.fnt",
                        instance.prefix + "YanoneKaffeesatz-standard.png", STANDARD, 2f, 6f, 0f, 0, true)
                        .setDescent(-8f).setLineMetrics(0f, -0.2f, 0f, 0f).setInlineImageMetrics(0f, 16f, 4f)
                        .scaleTo(26, 30).setTextureFilter().setName("Yanone Kaffeesatz");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.kaffeesatz != null)
            return new Font(instance.kaffeesatz);
        throw new RuntimeException("Assets for getYanoneKaffeesatz() not found.");
    }

    private Font kaffeesatzMSDF;

    /**
     * Returns a Font already configured to use a variable-width, narrow, humanist font, that should
     * scale very well up or down, but isn't compatible with inline images such as {@link #addEmoji(Font) emoji}.
     * Caches the result for later calls. The font used is Yanone Kaffeesatz, a free (OFL) typeface. It supports a lot
     * of Latin, Cyrillic, and some extended Latin, but not Greek.
     * This uses the Multi-channel Signed Distance Field (MSDF) technique as opposed to the normal Signed Distance Field
     * technique, which gives the rendered font sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * Preview: <a href="https://i.imgur.com/YeXFVE9.png">Image link</a> (uses width=26, height=30, setCrispness(2.5f))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/YanoneKaffeesatz-msdf.fnt">YanoneKaffeesatz-msdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/YanoneKaffeesatz-msdf.png">YanoneKaffeesatz-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/YanoneKaffeesatz-License.txt">YanoneKaffeesatz-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font YanoneKaffeesatz.ttf using MSDF
     */
    public static Font getYanoneKaffeesatzMSDF() {
        initialize();
        if (instance.kaffeesatzMSDF == null) {
            try {
                instance.kaffeesatzMSDF = new Font(instance.prefix + "YanoneKaffeesatz-msdf.fnt",
                        instance.prefix + "YanoneKaffeesatz-msdf.png", MSDF, 0f, 20f, 0f, 0, true)
                        .scaleTo(26, 30).setCrispness(2.5f).setName("Yanone Kaffeesatz (MSDF)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.kaffeesatzMSDF != null)
            return new Font(instance.kaffeesatzMSDF);
        throw new RuntimeException("Assets for getYanoneKaffeesatzMSDF() not found.");
    }

    private Font yataghanMSDF;

    /**
     * Returns a Font already configured to use a variable-width, narrow, "dark fantasy" font, that should
     * scale very well up or down, but isn't compatible with inline images such as {@link #addEmoji(Font) emoji}.
     * Caches the result for later calls. The font used is Yataghan, a widely-distributed typeface. It supports ASCII
     * and some extended Latin, but not much else.
     * This uses the Multi-channel Signed Distance Field (MSDF) technique as opposed to the normal Signed Distance Field
     * technique, which gives the rendered font sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * I don't know who the original author of Yataghan was; if you are the original author and want attribution or want
     * this font removed, please post an issue on the tommyettinger/textratypist GitHub repo, or email tommyettinger.
     * <br>
     * Preview: <a href="https://i.imgur.com/YbrzKlL.png">Image link</a> (uses width=20, height=32)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yataghan-msdf.fnt">Yataghan-msdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yataghan-msdf.png">Yataghan-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yataghan-License.txt">Yataghan-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Yataghan.ttf using MSDF
     */
    public static Font getYataghanMSDF() {
        initialize();
        if (instance.yataghanMSDF == null) {
            try {
                instance.yataghanMSDF = new Font(instance.prefix + "Yataghan-msdf.fnt",
                        instance.prefix + "Yataghan-msdf.png", MSDF, 0f, 4f, 0f, 0f, true)
                        .setLineMetrics(0f, 0.125f, 0f, -0.4f)
                        .scaleTo(20, 32).setName("Yataghan (MSDF)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.yataghanMSDF != null)
            return new Font(instance.yataghanMSDF);
        throw new RuntimeException("Assets for getYataghanMSDF() not found.");
    }
    /**
     * Horribly duplicated because this is private in TextureAtlas.
     * This can be entirely replaced by Consumer from JDK 8 once RoboVM supports it.
     * @param <T> the type that this can parse
     */
    private interface Field<T> {
        void parse(T object);
    }

    /**
     * This is exactly like {@link TextureAtlas#TextureAtlas(FileHandle, FileHandle, boolean)}, except it jumps through
     * some hoops to ensure the atlas is loaded with UTF-8 encoding. Loading an atlas that uses Unicode names for its
     * TextureRegions can have those names be unusable if TextureAtlas' normal default platform encoding is used; this
     * primarily affects Java versions before 18 where the JVM flag {@code -Dfile.encoding=UTF-8} was missing when a JAR
     * is launched.
     * @param packFile the FileHandle for the atlas file
     * @param imagesDir the FileHandle for the folder that holds the images used by the atlas file
     * @param flip If true, all regions loaded will be flipped for use with a perspective where 0,0 is the upper left corner.
     * @return a new TextureAtlas loaded from the given files.
     */
    public static TextureAtlas loadUnicodeAtlas(FileHandle packFile, FileHandle imagesDir, boolean flip) {
        return new TextureAtlas(new TextureAtlas.TextureAtlasData(packFile, imagesDir, flip){
            private int readEntry (String[] entry, @Null String line) {
                if (line == null) return 0;
                line = line.trim();
                if (line.length() == 0) return 0;
                int colon = line.indexOf(':');
                if (colon == -1) return 0;
                entry[0] = line.substring(0, colon).trim();
                for (int i = 1, lastMatch = colon + 1;; i++) {
                    int comma = line.indexOf(',', lastMatch);
                    if (comma == -1) {
                        entry[i] = line.substring(lastMatch).trim();
                        return i;
                    }
                    entry[i] = line.substring(lastMatch, comma).trim();
                    lastMatch = comma + 1;
                    if (i == 4) return 4;
                }
            }

            public void load (FileHandle packFile, FileHandle imagesDir, boolean flip) {
                final String[] entry = new String[5];

                ObjectMap<String, Field<Page>> pageFields = new ObjectMap<>(15, 0.99f); // Size needed to avoid collisions.
                pageFields.put("size", new Field<Page>() {
                    public void parse (Page page) {
                        page.width = Integer.parseInt(entry[1]);
                        page.height = Integer.parseInt(entry[2]);
                    }
                });
                pageFields.put("format", new Field<Page>() {
                    public void parse (Page page) {
                        page.format = Pixmap.Format.valueOf(entry[1]);
                    }
                });
                pageFields.put("filter", new Field<Page>() {
                    public void parse (Page page) {
                        page.minFilter = Texture.TextureFilter.valueOf(entry[1]);
                        page.magFilter = Texture.TextureFilter.valueOf(entry[2]);
                        page.useMipMaps = page.minFilter.isMipMap();
                    }
                });
                pageFields.put("repeat", new Field<Page>() {
                    public void parse (Page page) {
                        if (entry[1].indexOf('x') != -1) page.uWrap = Texture.TextureWrap.Repeat;
                        if (entry[1].indexOf('y') != -1) page.vWrap = Texture.TextureWrap.Repeat;
                    }
                });
                pageFields.put("pma", new Field<Page>() {
                    public void parse (Page page) {
                        page.pma = entry[1].equals("true");
                    }
                });

                final boolean[] hasIndexes = {false};
                ObjectMap<String, Field<Region>> regionFields = new ObjectMap<>(127, 0.99f); // Size needed to avoid collisions.
                regionFields.put("xy", new Field<Region>() { // Deprecated, use bounds.
                    public void parse (Region region) {
                        region.left = Integer.parseInt(entry[1]);
                        region.top = Integer.parseInt(entry[2]);
                    }
                });
                regionFields.put("size", new Field<Region>() { // Deprecated, use bounds.
                    public void parse (Region region) {
                        region.width = Integer.parseInt(entry[1]);
                        region.height = Integer.parseInt(entry[2]);
                    }
                });
                regionFields.put("bounds", new Field<Region>() {
                    public void parse (Region region) {
                        region.left = Integer.parseInt(entry[1]);
                        region.top = Integer.parseInt(entry[2]);
                        region.width = Integer.parseInt(entry[3]);
                        region.height = Integer.parseInt(entry[4]);
                    }
                });
                regionFields.put("offset", new Field<Region>() { // Deprecated, use offsets.
                    public void parse (Region region) {
                        region.offsetX = Integer.parseInt(entry[1]);
                        region.offsetY = Integer.parseInt(entry[2]);
                    }
                });
                regionFields.put("orig", new Field<Region>() { // Deprecated, use offsets.
                    public void parse (Region region) {
                        region.originalWidth = Integer.parseInt(entry[1]);
                        region.originalHeight = Integer.parseInt(entry[2]);
                    }
                });
                regionFields.put("offsets", new Field<Region>() {
                    public void parse (Region region) {
                        region.offsetX = Integer.parseInt(entry[1]);
                        region.offsetY = Integer.parseInt(entry[2]);
                        region.originalWidth = Integer.parseInt(entry[3]);
                        region.originalHeight = Integer.parseInt(entry[4]);
                    }
                });
                regionFields.put("rotate", new Field<Region>() {
                    public void parse (Region region) {
                        String value = entry[1];
                        if (value.equals("true"))
                            region.degrees = 90;
                        else if (!value.equals("false")) //
                            region.degrees = Integer.parseInt(value);
                        region.rotate = region.degrees == 90;
                    }
                });
                regionFields.put("index", new Field<Region>() {
                    public void parse (Region region) {
                        region.index = Integer.parseInt(entry[1]);
                        if (region.index != -1) hasIndexes[0] = true;
                    }
                });

                BufferedReader reader = null;
                try {
                    // all this for just one line changed...
                    reader = new BufferedReader(new InputStreamReader(packFile.read(), "UTF-8"), 1024);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e); // This should never happen on a sane JVM.
                }
                try {
                    String line = reader.readLine();
                    // Ignore empty lines before first entry.
                    while (line != null && line.trim().length() == 0)
                        line = reader.readLine();
                    // Header entries.
                    while (true) {
                        if (line == null || line.trim().length() == 0) break;
                        if (readEntry(entry, line) == 0) break; // Silently ignore all header fields.
                        line = reader.readLine();
                    }
                    // Page and region entries.
                    Page page = null;
                    Array<String> names = null;
                    Array<int[]> values = null;
                    while (line != null) {
                        if (line.trim().length() == 0) {
                            page = null;
                            line = reader.readLine();
                        } else if (page == null) {
                            page = new Page();
                            page.textureFile = imagesDir.child(line);
                            while (readEntry(entry, line = reader.readLine()) != 0) {
                                Field<Page> field = pageFields.get(entry[0]);
                                if (field != null) field.parse(page); // Silently ignore unknown page fields.
                            }
                            getPages().add(page);
                        } else {
                            Region region = new Region();
                            region.page = page;
                            region.name = line.trim();
                            if (flip) region.flip = true;
                            while (true) {
                                int count = readEntry(entry, line = reader.readLine());
                                if (count == 0) break;
                                Field<Region> field = regionFields.get(entry[0]);
                                if (field != null)
                                    field.parse(region);
                                else {
                                    if (names == null) {
                                        names = new Array<>(8);
                                        values = new Array<>(8);
                                    }
                                    names.add(entry[0]);
                                    int[] entryValues = new int[count];
                                    for (int i = 0; i < count; i++) {
                                        try {
                                            entryValues[i] = Integer.parseInt(entry[i + 1]);
                                        } catch (NumberFormatException ignored) { // Silently ignore non-integer values.
                                        }
                                    }
                                    values.add(entryValues);
                                }
                            }
                            if (region.originalWidth == 0 && region.originalHeight == 0) {
                                region.originalWidth = region.width;
                                region.originalHeight = region.height;
                            }
                            if (names != null && names.size > 0) {
                                region.names = names.toArray(String.class);
                                region.values = values.toArray(int[].class);
                                names.clear();
                                values.clear();
                            }
                            getRegions().add(region);
                        }
                    }
                } catch (Exception ex) {
                    throw new GdxRuntimeException("Error reading texture atlas file: " + packFile, ex);
                } finally {
                    StreamUtils.closeQuietly(reader);
                }

                if (hasIndexes[0]) {
                    getRegions().sort(new Comparator<Region>() {
                        public int compare (Region region1, Region region2) {
                            return (region1.index & Integer.MAX_VALUE) - (region2.index & Integer.MAX_VALUE);
                        }
                    });
                }

                //// We would use this if we need each emoji before its written-name counterpart.
                //// We currently do not need this feature, but might in the future.
                //// This is slower than using the above block to only sort if there are indexes.
//                Comparator<Region> comp = new Comparator<Region>() {
//                    public int compare (Region region1, Region region2) {
//                        int pos = (region1.left << 15 ^ region1.top) - (region2.left << 15 ^ region2.top);
//                        pos = (pos >> 31 | -pos >>> 31) << 2;
//                        int idx = (region1.index & Integer.MAX_VALUE) - (region2.index & Integer.MAX_VALUE);
//                        idx = (idx >> 31 | -idx >>> 31) << 1;
//                        int name = region1.name.compareTo(region2.name);
//                        name = (name >> 31 | -name >>> 31) << 1;
//                        return pos + idx - name;
//                    }
//                };
//                getRegions().sort(comp);
            }
        });
    }

    private TextureAtlas twemoji;

    /**
     * Takes a Font and adds the Twemoji icon set to it, making the glyphs available using {@code [+name]} syntax.
     * You can use the name of an emoji, such as {@code [+clown face]}, or equivalently use the actual emoji, such as
     * {@code [+🤡]}, with the latter preferred because the names can be unwieldy or hard to get right. This caches the
     * Twemoji atlas for later calls. This tries to load the files "Twemoji.atlas" and "Twemoji.png" from the internal
     * storage first, and if that fails, it tries to load them from local storage in the current working directory.
     * <br>
     * You can add emoji to a font as inline images with KnownFonts.addEmoji(Font).
     * Emoji don't work at all with MSDF fonts, and don't support more than one color with SDF fonts, but work as intended
     * with "standard" fonts (without a distance field effect). They can scale reasonably well down, and less-reasonably well
     * up, but at typical text sizes (12-30 pixels in height) they tend to be legible. There are over 3000 emoji in the Twemoji
     * set, and they are accessible both by name, using the syntax <code>[+clown face]</code>, and by entering the actual
     * emoji, using the syntax <code>[+🤡]</code>. You can search for names in {@code Twemoji.atlas}, or use the emoji picker in
     * <a href="https://github.com/raeleus/skin-composer">Skin Composer</a> to navigate by category. You can also use the
     * emoji picker present in some OSes, such as how Win+. allows selecting an emoji on Windows 10 and up.
     * Programmatically, you can use {@link Font#nameLookup} to look up the internal {@code char} this uses for a given
     * name or emoji, and {@link Font#namesByCharCode} to go from such an internal code to an emoji (as UTF-8).
     * <br>
     * Preview: <a href="https://i.imgur.com/Mw0fWA7.png">Image link</a> (uses the font {@link #getYanoneKaffeesatz()})
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Twemoji.atlas">Twemoji.atlas</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Twemoji.png">Twemoji.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Twemoji-License.txt">Twemoji-License.txt</a></li>
     * </ul>
     *
     * @param changing a Font that will have over 3000 emoji added to it, with more aliases
     * @return {@code changing}, after the emoji atlas has been added
     */
    public static Font addEmoji(Font changing) {
        return addEmoji(changing, 0f, 0f, 0f);
    }
    /**
     * Takes a Font and adds the Twemoji icon set to it, making the glyphs available using {@code [+name]} syntax.
     * You can use the name of an emoji, such as {@code [+clown face]}, or equivalently use the actual emoji, such as
     * {@code [+🤡]}, with the latter preferred because the names can be unwieldy or hard to get right. This caches the
     * Twemoji atlas for later calls. This tries to load the files "Twemoji.atlas" and "Twemoji.png" from the internal
     * storage first, and if that fails, it tries to load them from local storage in the current working directory.
     * There are over 3000 emoji in the Twemoji set;
     * <a href="https://github.com/twitter/twemoji#attribution-requirements">it requires attribution to use</a>.
     * <br>
     * Emoji don't work at all with MSDF fonts, and don't support more than one color with SDF fonts, but work as
     * intended with "standard" fonts (without a distance field effect). They can scale reasonably well down, and
     * less-reasonably well up, but at typical text sizes (12-30 pixels in height) they tend to be legible.
     * You can search for names in {@code Twemoji.atlas}, or use the emoji picker in
     * <a href="https://github.com/raeleus/skin-composer">Skin Composer</a> to navigate by category. You can also use
     * the emoji picker present in some OSes, such as how Win+. allows selecting an emoji on Windows 10 and up.
     * Programmatically, you can use {@link Font#nameLookup} to look up the internal {@code char} this uses for a given
     * name or emoji, and {@link Font#namesByCharCode} to go from such an internal code to an emoji (as UTF-8).
     * <br>
     * This overload allows customizing the x/y offsets and x-advance for every emoji this puts in a Font.
     * <br>
     * Preview: <a href="https://i.imgur.com/Mw0fWA7.png">Image link</a> (uses the font {@link #getYanoneKaffeesatz()})
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Twemoji.atlas">Twemoji.atlas</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Twemoji.png">Twemoji.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Twemoji-License.txt">Twemoji-License.txt</a></li>
     * </ul>
     *
     * @param changing a Font that will have over 3000 emoji added to it, with more aliases
     * @param offsetXChange will be added to the {@link Font.GlyphRegion#offsetX} of each added glyph
     * @param offsetYChange will be added to the {@link Font.GlyphRegion#offsetY} of each added glyph
     * @param xAdvanceChange will be added to the {@link Font.GlyphRegion#xAdvance} of each added glyph
     * @return {@code changing}, after the emoji atlas has been added
     */
    public static Font addEmoji(Font changing, float offsetXChange, float offsetYChange, float xAdvanceChange) {
        return addEmoji(changing, "", "", offsetXChange, offsetYChange, xAdvanceChange);
    }
    /**
     * Takes a Font and adds the Twemoji icon set to it, making the glyphs available using {@code [+name]} syntax.
     * You can use the name of an emoji, such as {@code [+clown face]}, or equivalently use the actual emoji, such as
     * {@code [+🤡]}, with the latter preferred because the names can be unwieldy or hard to get right. This caches the
     * Twemoji atlas for later calls. This tries to load the files "Twemoji.atlas" and "Twemoji.png" from the internal
     * storage first, and if that fails, it tries to load them from local storage in the current working directory.
     * There are over 3000 emoji in the Twemoji set;
     * <a href="https://github.com/twitter/twemoji#attribution-requirements">it requires attribution to use</a>.
     * <br>
     * Emoji don't work at all with MSDF fonts, and don't support more than one color with SDF fonts, but work as
     * intended with "standard" fonts (without a distance field effect). They can scale reasonably well down, and
     * less-reasonably well up, but at typical text sizes (12-30 pixels in height) they tend to be legible.
     * You can search for names in {@code Twemoji.atlas}, or use the emoji picker in
     * <a href="https://github.com/raeleus/skin-composer">Skin Composer</a> to navigate by category. You can also use
     * the emoji picker present in some OSes, such as how Win+. allows selecting an emoji on Windows 10 and up.
     * Programmatically, you can use {@link Font#nameLookup} to look up the internal {@code char} this uses for a given
     * name or emoji, and {@link Font#namesByCharCode} to go from such an internal code to an emoji (as UTF-8).
     * <br>
     * This overload allows customizing the x/y offsets and x-advance for every emoji this puts in a Font. It also
     * allows specifying Strings to prepend before and append after each name in the font, including emoji names.
     * <br>
     * Preview: <a href="https://i.imgur.com/Mw0fWA7.png">Image link</a> (uses the font {@link #getYanoneKaffeesatz()})
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Twemoji.atlas">Twemoji.atlas</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Twemoji.png">Twemoji.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Twemoji-License.txt">Twemoji-License.txt</a></li>
     * </ul>
     *
     * @param changing a Font that will have over 3000 emoji added to it, with more aliases
     * @param prepend will be prepended before each name in the atlas; if null, will be treated as ""
     * @param append will be appended after each name in the atlas; if null, will be treated as ""
     * @param offsetXChange will be added to the {@link Font.GlyphRegion#offsetX} of each added glyph
     * @param offsetYChange will be added to the {@link Font.GlyphRegion#offsetY} of each added glyph
     * @param xAdvanceChange will be added to the {@link Font.GlyphRegion#xAdvance} of each added glyph
     * @return {@code changing}, after the emoji atlas has been added
     */
    public static Font addEmoji(Font changing, String prepend, String append, float offsetXChange, float offsetYChange, float xAdvanceChange) {
        initialize();
        if (instance.twemoji == null) {
            try {
                FileHandle atlas = Gdx.files.internal(instance.prefix + "Twemoji.atlas");
                if (!atlas.exists() && Gdx.files.isLocalStorageAvailable()) atlas = Gdx.files.local(instance.prefix + "Twemoji.atlas");
                if (Gdx.files.internal(instance.prefix + "Twemoji.png").exists())
                    instance.twemoji = loadUnicodeAtlas(atlas, atlas.parent(), false);
                else if (Gdx.files.isLocalStorageAvailable() && Gdx.files.local(instance.prefix + "Twemoji.png").exists())
                    instance.twemoji = loadUnicodeAtlas(atlas, atlas.parent(), false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.twemoji != null) {
            return changing.addAtlas(instance.twemoji, prepend, append,
                    offsetXChange, offsetYChange - changing.descent * changing.scaleY, xAdvanceChange);
        }
        throw new RuntimeException("Assets 'Twemoji.atlas' and 'Twemoji.png' not found.");
    }

    private TextureAtlas gameIcons;

    /**
     * Takes a Font and adds the <a href="https://game-icons.net/">Game-Icons.net</a> icon set to it, making the glyphs
     * available using {@code [+name]} syntax. Unlike the emoji used by {@link #addEmoji(Font)}, icons here are always.
     * retrieved by name, and names are always all-lower-case, separated by dashes ({@code '-'}). This caches the
     * Game-Icons.net atlas for later calls. This tries to load the files "Game-Icons.atlas" and "Game-Icons.png" from
     * the internal storage first, and if that fails, it tries to load them from local storage in the current working
     * directory. There are 4131 images in this edition of the Game-Icons.net icons (from December 20, 2022), and
     * <a href="https://game-icons.net/faq.html">it requires attribution to use</a>.
     * <br>
     * Although these icons might work with MSDF fonts, they should work with standard and SDF fonts. They can
     * scale reasonably well down, and less-reasonably well up, but at typical text sizes (12-30 pixels in height) they
     * tend to be legible. All icons use only the color white with various levels of transparency, so they can be
     * colored like normal text glyphs. You can search for names in {@code Game-Icons.atlas}.
     * Programmatically, you can use {@link Font#nameLookup} to look up the internal {@code char} this uses for a given
     * name, and {@link Font#namesByCharCode} to go from such an internal code to the corresponding name.
     * <br>
     * Preview: <a href="https://i.imgur.com/s47OVEU.gif">Animated Image link</a> (uses {@link #getNowAlt()})
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Game-Icons.atlas">Game-Icons.atlas</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Game-Icons.png">Game-Icons.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Game-Icons-License.txt">Game-Icons-License.txt</a></li>
     * </ul>
     *
     * @param changing a Font that will have over 4000 icons added to it
     * @return {@code changing}, after the icon atlas has been added
     */
    public static Font addGameIcons(Font changing) {
        return addGameIcons(changing, 0f, 0f, 0f);
    }
    /**
     * Takes a Font and adds the <a href="https://game-icons.net/">Game-Icons.net</a> icon set to it, making the glyphs
     * available using {@code [+name]} syntax. Unlike the emoji used by {@link #addEmoji(Font)}, icons here are always.
     * retrieved by name, and names are always all-lower-case, separated by dashes ({@code '-'}). This caches the
     * Game-Icons.net atlas for later calls. This tries to load the files "Game-Icons.atlas" and "Game-Icons.png" from
     * the internal storage first, and if that fails, it tries to load them from local storage in the current working
     * directory. There are 4131 images in this edition of the Game-Icons.net icons (from December 20, 2022), and
     * <a href="https://game-icons.net/faq.html">it requires attribution to use</a>.
     * <br>
     * Although these icons might work with MSDF fonts, they should work with standard and SDF fonts. They can
     * scale reasonably well down, and less-reasonably well up, but at typical text sizes (12-30 pixels in height) they
     * tend to be legible. All icons use only the color white with various levels of transparency, so they can be
     * colored like normal text glyphs. You can search for names in {@code Game-Icons.atlas}.
     * Programmatically, you can use {@link Font#nameLookup} to look up the internal {@code char} this uses for a given
     * name, and {@link Font#namesByCharCode} to go from such an internal code to the corresponding name.
     * <br>
     * This overload allows customizing the x/y offsets and x-advance for every icon this puts in a Font.
     * <br>
     * Preview: <a href="https://i.imgur.com/s47OVEU.gif">Animated Image link</a> (uses {@link #getNowAlt()})
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Game-Icons.atlas">Game-Icons.atlas</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Game-Icons.png">Game-Icons.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Game-Icons-License.txt">Game-Icons-License.txt</a></li>
     * </ul>
     *
     * @param changing a Font that will have over 4000 icons added to it
     * @param offsetXChange will be added to the {@link Font.GlyphRegion#offsetX} of each added glyph
     * @param offsetYChange will be added to the {@link Font.GlyphRegion#offsetY} of each added glyph
     * @param xAdvanceChange will be added to the {@link Font.GlyphRegion#xAdvance} of each added glyph
     * @return {@code changing}, after the icon atlas has been added
     */
    public static Font addGameIcons(Font changing, float offsetXChange, float offsetYChange, float xAdvanceChange) {
        return addGameIcons(changing, "", "", offsetXChange, offsetYChange, xAdvanceChange);
    }

    /**
     * Takes a Font and adds the <a href="https://game-icons.net/">Game-Icons.net</a> icon set to it, making the glyphs
     * available using {@code [+name]} syntax. Unlike the emoji used by {@link #addEmoji(Font)}, icons here are always.
     * retrieved by name, and names are always all-lower-case, separated by dashes ({@code '-'}). This caches the
     * Game-Icons.net atlas for later calls. This tries to load the files "Game-Icons.atlas" and "Game-Icons.png" from
     * the internal storage first, and if that fails, it tries to load them from local storage in the current working
     * directory. There are 4131 images in this edition of the Game-Icons.net icons (from December 20, 2022), and
     * <a href="https://game-icons.net/faq.html">it requires attribution to use</a>.
     * <br>
     * Although these icons might work with MSDF fonts, they should work with standard and SDF fonts. They can
     * scale reasonably well down, and less-reasonably well up, but at typical text sizes (12-30 pixels in height) they
     * tend to be legible. All icons use only the color white with various levels of transparency, so they can be
     * colored like normal text glyphs. You can search for names in {@code Game-Icons.atlas}.
     * Programmatically, you can use {@link Font#nameLookup} to look up the internal {@code char} this uses for a given
     * name, and {@link Font#namesByCharCode} to go from such an internal code to the corresponding name.
     * <br>
     * This overload allows customizing the x/y offsets and x-advance for every icon this puts in a Font. It also
     * allows specifying Strings to prepend before and append after each name in the font.
     * <br>
     * Preview: <a href="https://i.imgur.com/s47OVEU.gif">Animated Image link</a> (uses {@link #getNowAlt()})
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Game-Icons.atlas">Game-Icons.atlas</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Game-Icons.png">Game-Icons.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Game-Icons-License.txt">Game-Icons-License.txt</a></li>
     * </ul>
     *
     * @param changing a Font that will have over 4000 icons added to it
     * @param prepend will be prepended before each name in the atlas; if null, will be treated as ""
     * @param append will be appended after each name in the atlas; if null, will be treated as ""
     * @param offsetXChange will be added to the {@link Font.GlyphRegion#offsetX} of each added glyph
     * @param offsetYChange will be added to the {@link Font.GlyphRegion#offsetY} of each added glyph
     * @param xAdvanceChange will be added to the {@link Font.GlyphRegion#xAdvance} of each added glyph
     * @return {@code changing}, after the icon atlas has been added
     */
    public static Font addGameIcons(Font changing, String prepend, String append, float offsetXChange, float offsetYChange, float xAdvanceChange) {
        initialize();
        if (instance.gameIcons == null) {
            try {
                FileHandle atlas = Gdx.files.internal(instance.prefix + "Game-Icons.atlas");
                if (!atlas.exists() && Gdx.files.isLocalStorageAvailable()) atlas = Gdx.files.local(instance.prefix + "Game-Icons.atlas");
                if (Gdx.files.internal(instance.prefix + "Game-Icons.png").exists())
                    instance.gameIcons = new TextureAtlas(atlas, atlas.parent(), false);
                else if (Gdx.files.isLocalStorageAvailable() && Gdx.files.local(instance.prefix + "Game-Icons.png").exists())
                    instance.gameIcons = new TextureAtlas(atlas, atlas.parent(), false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.gameIcons != null) {
            return changing.addAtlas(instance.gameIcons, prepend, append,
                    offsetXChange, offsetYChange - changing.descent * changing.scaleY, xAdvanceChange);
        }
        throw new RuntimeException("Assets 'Game-Icons.atlas' and 'Game-Icons.png' not found.");
    }

    private Font gameIconsFont;

    /**
     * Gets a typically-square Font that is meant to be used in a FontFamily, allowing switching to a Font with the many
     * game-icons.net icons. The base Font this uses is {@link #getAStarry()}, because it is perfectly square by
     * default, and this needs all of AStarry's assets. It also needs the assets for {@link #addGameIcons(Font)} to be
     * present, since those will be available with this Font. The name this will use in a FontFamily is "Icons". You can
     * specify the width and height you want for the icons; typically they are the same, because the icons here are
     * square, and you probably want the height to match the line height for your main font. It isn't expected that
     * many users would want to use the non-icon glyphs in the font. The reason this is needed is that you can't fit
     * both the emoji from {@link #addEmoji(Font)} and the icons from {@link #addGameIcons(Font)} in one Font, but you
     * can swap between two different Fonts in a FontFamily, one with emoji and one with icons.
     * <br>
     * Preview: <a href="https://i.imgur.com/s47OVEU.gif">Animated Image link</a> (uses {@link #getNowAlt()})
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/AStarry-standard.fnt">AStarry-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/AStarry-standard.png">AStarry-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/AStarry-License.txt">AStarry-License.txt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Game-Icons.atlas">Game-Icons.atlas</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Game-Icons.png">Game-Icons.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Game-Icons-License.txt">Game-Icons-License.txt</a></li>
     * </ul>
     * @param width the width of a single glyph in the returned Font; usually matches the line-height of other text
     * @param height the height of a single glyph in the returned Font; usually matches the line-height of other text
     * @return a preconfigured Font using {@link #getAStarry()} and {@link #addGameIcons(Font)}, with name "Icons"
     */
    public static Font getGameIconsFont(float width, float height) {
        initialize();
        if (instance.gameIconsFont == null) {
            try {
                instance.gameIconsFont = KnownFonts.addGameIcons(KnownFonts.getAStarry().scaleTo(width, height).setName("Icons"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.gameIconsFont != null)
            return new Font(instance.gameIconsFont);
        throw new RuntimeException("Assets for getGameIconsFont() not found.");
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    /**
     * Returns a new array of Font instances, calling each getXyz() method in this class that returns any Font.
     * This will only function at all if all the assets (for every known Font) are present and load-able.
     * You should store the result of this method, rather than calling it often, because each call copies many Fonts.
     * @return a new array containing all Font instances this knows
     */
    public static Font[] getAll() {
        return new Font[]{getAStarry(), getAStarry().scaleTo(8, 16).setName("A Starry Tall"), getAStarryMSDF(),
                getBitter(), getCanada(), getCascadiaMono(), getCascadiaMonoMSDF(), getCaveat(), getCozette(),
                getDejaVuSansMono(), getGentium(), getGentiumMSDF(), getGentiumSDF(), getGentiumUnItalic(),
                getGoNotoUniversalSDF(), getHanazono(), getIBM8x16(), getInconsolata(), getInconsolataMSDF(),
                getIosevka(), getIosevkaMSDF(), getIosevkaSDF(),
                getIosevkaSlab(), getIosevkaSlabMSDF(), getIosevkaSlabSDF(),
                getKingthingsFoundation(), getKingthingsPetrock(), getLibertinusSerif(), getNowAlt(), getOpenSans(),
                getOxanium(), getQuanPixel(), getRobotoCondensed(), getTangerine(), getTangerineSDF(),
                getYanoneKaffeesatz(), getYanoneKaffeesatzMSDF(), getYataghanMSDF()};
    }

    /**
     * Returns a new array of Font instances, calling each getXyz() method in this class that returns any
     * non-distance-field Font.
     * This will only function at all if all the assets (for every known standard Font) are present and load-able.
     * You should store the result of this method, rather than calling it often, because each call copies many Fonts.
     * @return a new array containing all non-distance-field Font instances this knows
     */
    public static Font[] getAllStandard() {
        return new Font[]{getAStarry(), getAStarry().scaleTo(8, 16).setName("A Starry Tall"), getBitter(), getCanada(),
                getCascadiaMono(), getCaveat(), getCozette(), getGentium(), getGentiumUnItalic(), getHanazono(),
                getIBM8x16(), getInconsolata(), getIosevka(), getIosevkaSlab(), getKingthingsFoundation(),
                getKingthingsPetrock(), getLibertinusSerif(), getNowAlt(), getOpenSans(), getOxanium(), getQuanPixel(),
                getRobotoCondensed(), getTangerine(), getYanoneKaffeesatz()};
    }

    /**
     * Returns a Font ({@link #getGentium()}) with a FontFamily configured so that all non-distance-field Fonts can be
     * used with syntax like {@code [@Sans]}. The names this supports can be accessed with code using
     * {@code getStandardFamily().family.fontAliases.keys()}. These names so far are:
     * <ul>
     *     <li>{@code Serif}, which is {@link #getGentium()},</li>
     *     <li>{@code Sans}, which is {@link #getOpenSans()},</li>
     *     <li>{@code Mono}, which is {@link #getInconsolata()},</li>
     *     <li>{@code Condensed}, which is {@link #getRobotoCondensed()},</li>
     *     <li>{@code Humanist}, which is {@link #getYanoneKaffeesatz()},</li>
     *     <li>{@code Retro}, which is {@link #getIBM8x16()},</li>
     *     <li>{@code Slab}, which is {@link #getIosevkaSlab()},</li>
     *     <li>{@code Handwriting}, which is {@link #getCaveat()},</li>
     *     <li>{@code Canada}, which is {@link #getCanada()},</li>
     *     <li>{@code Cozette}, which is {@link #getCozette()},</li>
     *     <li>{@code Iosevka}, which is {@link #getIosevka()},</li>
     *     <li>{@code Medieval}, which is {@link #getKingthingsFoundation()},</li>
     *     <li>{@code Future}, which is {@link #getOxanium()},</li>
     *     <li>{@code Console}, which is {@link #getAStarry()}, and</li>
     *     <li>{@code Code}, which is {@link #getCascadiaMono()}.</li>
     *     <li>{@code Geometric}, which is {@link #getNowAlt()}.</li>
     * </ul>
     * You can also always use the full name of one of these fonts, which can be obtained using {@link Font#getName()}.
     * {@code Serif}, which is {@link #getGentium()}, will always be the default font used after a reset. For
     * backwards compatibility, {@code Bitter} is an alias for {@link #getGentium()} (not {@link #getBitter()}), because
     * Bitter and Gentium look very similar and because a slot was needed for {@code Handwriting}, which seemed useful
     * in more situations.
     * <br>
     * This will only function at all if all the assets (for every known standard Font) are present and load-able.
     * You should store the result of this method, rather than calling it often, because each call copies many Fonts.
     * @return a Font that can switch between 16 different Fonts in its FontFamily, to any of several Fonts this knows
     */
    public static Font getStandardFamily() {
        Font.FontFamily family = new Font.FontFamily(
                new String[]{"Serif", "Sans", "Mono", "Condensed", "Humanist",
                        "Retro", "Slab", "Handwriting", "Canada", "Cozette", "Iosevka",
                        "Medieval", "Future", "Console", "Code", "Geometric"},
                new Font[]{getGentium(), getOpenSans(), getInconsolata(), getRobotoCondensed(), getYanoneKaffeesatz(),
                        getIBM8x16(), getIosevkaSlab(), getCaveat(), getCanada(), getCozette(), getIosevka(),
                        getKingthingsFoundation(), getOxanium(), getAStarry().scale(2, 2), getCascadiaMono(), getNowAlt()});
        family.fontAliases.put("Bitter", 0); // for compatibility; Bitter and Gentium look nearly identical anyway...
        return family.connected[0].setFamily(family);
    }

    /**
     * Returns a new array of Font instances, calling each getXyz() method in this class that returns any SDF Font.
     * This will only function at all if all the assets (for every known SDF Font) are present and load-able.
     * You should store the result of this method, rather than calling it often, because each call copies 5 Fonts.
     * @return a new array containing all SDF Font instances this knows
     */
    public static Font[] getAllSDF() {
        return new Font[]{getGentiumSDF(), getGoNotoUniversalSDF(), getIosevkaSDF(), getIosevkaSlabSDF(),
                getTangerineSDF()};
    }

    /**
     * Returns a new array of Font instances, calling each getXyz() method in this class that returns any MSDF Font.
     * This will only function at all if all the assets (for every known MSDF Font) are present and load-able.
     * You should store the result of this method, rather than calling it often, because each call copies 9 Fonts.
     * @return a new array containing all MSDF Font instances this knows
     */
    public static Font[] getAllMSDF() {
        return new Font[]{getAStarryMSDF(), getCascadiaMonoMSDF(), getDejaVuSansMono(),
                getGentiumMSDF(), getInconsolataMSDF(), getIosevkaMSDF(),
                getIosevkaSlabMSDF(), getYanoneKaffeesatzMSDF(), getYataghanMSDF()};
    }

    @Override
    public void dispose() {

        if (astarry != null) {
            astarry.dispose();
            astarry = null;
        }
        if (astarryMSDF != null) {
            astarryMSDF.dispose();
            astarryMSDF = null;
        }
        if (bitter != null) {
            bitter.dispose();
            bitter = null;
        }
        if (canada != null) {
            canada.dispose();
            canada = null;
        }
        if (cascadiaMono != null) {
            cascadiaMono.dispose();
            cascadiaMono = null;
        }
        if (cascadiaMonoMSDF != null) {
            cascadiaMonoMSDF.dispose();
            cascadiaMonoMSDF = null;
        }
        if (caveat != null) {
            caveat.dispose();
            caveat = null;
        }
        if (cozette != null) {
            cozette.dispose();
            cozette = null;
        }
        if (dejaVuSansMono != null) {
            dejaVuSansMono.dispose();
            dejaVuSansMono = null;
        }
        if (gentium != null) {
            gentium.dispose();
            gentium = null;
        }
        if (gentiumMSDF != null) {
            gentiumMSDF.dispose();
            gentiumMSDF = null;
        }
        if (gentiumSDF != null) {
            gentiumSDF.dispose();
            gentiumSDF = null;
        }
        if (gentiumUnItalic != null) {
            gentiumUnItalic.dispose();
            gentiumUnItalic = null;
        }
        if (goNotoUniversalSDF != null) {
            goNotoUniversalSDF.dispose();
            goNotoUniversalSDF = null;
        }
        if(hanazono != null) {
            hanazono.dispose();
            hanazono = null;
        }
        if (ibm8x16 != null) {
            ibm8x16.dispose();
            ibm8x16 = null;
        }
        if (inconsolata != null) {
            inconsolata.dispose();
            inconsolata = null;
        }
        if (inconsolataMSDF != null) {
            inconsolataMSDF.dispose();
            inconsolataMSDF = null;
        }
        if (iosevka != null) {
            iosevka.dispose();
            iosevka = null;
        }
        if (iosevkaMSDF != null) {
            iosevkaMSDF.dispose();
            iosevkaMSDF = null;
        }
        if (iosevkaSDF != null) {
            iosevkaSDF.dispose();
            iosevkaSDF = null;
        }
        if (iosevkaSlab != null) {
            iosevkaSlab.dispose();
            iosevkaSlab = null;
        }
        if (iosevkaSlabMSDF != null) {
            iosevkaSlabMSDF.dispose();
            iosevkaSlabMSDF = null;
        }
        if (iosevkaSlabSDF != null) {
            iosevkaSlabSDF.dispose();
            iosevkaSlabSDF = null;
        }
        if (kingthingsFoundation != null) {
            kingthingsFoundation.dispose();
            kingthingsFoundation = null;
        }
        if (kingthingsPetrock != null) {
            kingthingsPetrock.dispose();
            kingthingsPetrock = null;
        }
        if (libertinusSerif != null) {
            libertinusSerif.dispose();
            libertinusSerif = null;
        }
        if (nowAlt != null) {
            nowAlt.dispose();
            nowAlt = null;
        }
        if (openSans != null) {
            openSans.dispose();
            openSans = null;
        }
        if (oxanium != null) {
            oxanium.dispose();
            oxanium = null;
        }
        if (quanPixel != null) {
            quanPixel.dispose();
            quanPixel = null;
        }
        if (robotoCondensed != null) {
            robotoCondensed.dispose();
            robotoCondensed = null;
        }
        if(tangerine != null) {
            tangerine.dispose();
            tangerine = null;
        }
        if(tangerineSDF != null) {
            tangerineSDF.dispose();
            tangerineSDF = null;
        }
        if (kaffeesatz != null) {
            kaffeesatz.dispose();
            kaffeesatz = null;
        }
        if (kaffeesatzMSDF != null) {
            kaffeesatzMSDF.dispose();
            kaffeesatzMSDF = null;
        }
        if (yataghanMSDF != null) {
            yataghanMSDF.dispose();
            yataghanMSDF = null;
        }
        if(twemoji != null) {
            twemoji.dispose();
            twemoji = null;
        }
        if(gameIcons != null) {
            gameIcons.dispose();
            gameIcons = null;
        }
    }
}