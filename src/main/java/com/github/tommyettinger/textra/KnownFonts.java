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

package com.github.tommyettinger.textra;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;

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
 * .png file, and some kind of license file. They could ignore all other assets required by other fonts.
 */
public final class KnownFonts implements LifecycleListener {
    private static KnownFonts instance;

    private KnownFonts() {
        if(Gdx.app == null)
            throw new IllegalStateException("Gdx.app cannot be null; initialize KnownFonts in create() or later.");
        Gdx.app.addLifecycleListener(this);
    }

    private static void initialize()
    {
        if(instance == null)
            instance = new KnownFonts();
    }

    private Font astarry;
    /**
     * Returns a Font already configured to use a square font with 45-degree angled sections, based on the
     * typeface used on the Atari ST console, that should scale cleanly to many sizes. This font only supports ASCII,
     * but it supports all of it. Caches the result for later calls. The font is "a-starry", based on "Atari ST
     * (low-res)" by Damien Guard; it is available under a CC-BY-SA-3.0 license, which requires attribution to Damien
     * Guard (and technically Tommy Ettinger, because he made changes in a-starry) if you use it. This uses the
     * Multi-channel Signed Distance Field (MSDF) technique as opposed to the normal Signed Distance Field technique,
     * which gives the rendered font sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * Preview: <a href="https://i.imgur.com/sOvax8i.png">Image link</a> (uses width=10, height=10)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/AStarry-msdf.fnt">AStarry-msdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/AStarry-msdf.png">AStarry-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/AStarry-license.txt">AStarry-license.txt</a></li>
     * </ul>
     * @return the Font object that can represent many sizes of the font A Starry using MSDF
     */
    public static Font getAStarry()
    {
        initialize();
        if(instance.astarry == null)
        {
            try {
                instance.astarry = new Font("AStarry-msdf.fnt", "AStarry-msdf.png", MSDF, 0, 0, 0, 0, false)
                        .scaleTo(10, 10).setCrispness(2f).setName("A Starry (MSDF)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.astarry != null)
            return new Font(instance.astarry);
        throw new RuntimeException("Assets for getAStarry() not found.");
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
     * Preview: <a href="https://i.imgur.com/F5YJPCE.png">Image link</a> (uses width=33, height=30, adjustLineHeight(1.225f))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Bitter-standard.fnt">Bitter-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Bitter-standard.png">Bitter-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Bitter-License.txt">Bitter-License.txt</a></li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Bitter-Light.ttf
     */
    public static Font getBitter()
    {
        initialize();
        if(instance.bitter == null)
        {
            try {
                instance.bitter = new Font("Bitter-standard.fnt", STANDARD, 0, 0, 0, 0, false)
                        .scaleTo(33, 30).adjustLineHeight(1.225f).setTextureFilter().setName("Bitter");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.bitter != null)
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
     * Preview: <a href="https://i.imgur.com/tEVafWO.png">Image link</a> (uses width=30, height=35)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Canada1500-standard.fnt">Canada1500-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Canada1500-standard.png">Canada1500-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Canada1500-License.txt">Canada1500-License.txt</a></li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Canada1500.ttf
     */
    public static Font getCanada()
    {
        initialize();
        if(instance.canada == null)
        {
            try {
                instance.canada = new Font("Canada1500-standard.fnt", STANDARD, 0, 0, 0, 0, true)
                        .scaleTo(30, 35).setTextureFilter().setName("Canada1500");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.canada != null)
            return new Font(instance.canada);
        throw new RuntimeException("Assets for getCanada() not found.");
    }

    private Font cascadiaMono;
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
     * Preview: <a href="https://i.imgur.com/rp6pgSa.png">Image link</a> (uses width=9, height=16)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/CascadiaMono-msdf.fnt">CascadiaMono-msdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/CascadiaMono-msdf.png">CascadiaMono-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cascadia-license.txt">Cascadia-license.txt</a></li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Cascadia Code Mono using MSDF
     */
    public static Font getCascadiaMono()
    {
        initialize();
        if(instance.cascadiaMono == null)
        {
            try {
                instance.cascadiaMono = new Font("CascadiaMono-msdf.fnt", "CascadiaMono-msdf.png", MSDF, 2f, 1f, 0f, 0f, true)
                        .scaleTo(9, 16).setName("Cascadia Mono (MSDF)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.cascadiaMono != null)
            return new Font(instance.cascadiaMono);
        throw new RuntimeException("Assets for getCascadiaMono() not found.");
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
     * Preview: <a href="https://i.imgur.com/1wBVayl.png">Image link</a> (uses width=7, height=13; this is small enough
     * to make the scaled text look bad in some places)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cozette-standard.fnt">Cozette-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cozette-standard.png">Cozette-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cozette-license.txt">Cozette-license.txt</a></li>
     * </ul>
     * @return the Font object that represents the 7x13px font Cozette
     */
    public static Font getCozette()
    {
        initialize();
        if(instance.cozette == null)
        {
            try {
                instance.cozette = new Font("Cozette-standard.fnt", "Cozette-standard.png", STANDARD, 0, 0, 0, 0, false)
                        .useIntegerPositions(true)
                        .setName("Cozette");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.cozette != null)
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
     * Preview: <a href="https://i.imgur.com/fmRBfsp.png">Image link</a> (uses width=9, height=20)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVuSansMono-msdf.fnt">DejaVuSansMono-msdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVuSansMono-msdf.png">DejaVuSansMono-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVuSansMono-License.txt">DejaVuSansMono-License.txt</a></li>
     * </ul>
     * @return the Font object that can represent many sizes of the font DejaVu Sans Mono using MSDF
     */
    public static Font getDejaVuSansMono()
    {
        initialize();
        if(instance.dejaVuSansMono == null)
        {
            try {
                instance.dejaVuSansMono = new Font("DejaVuSansMono-msdf.fnt", "DejaVuSansMono-msdf.png", MSDF, 1f, 4f, 0f, 0f, true)
                        .scaleTo(9, 20).setName("DejaVu Sans Mono (MSDF)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.dejaVuSansMono != null)
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
     * Preview: <a href="https://i.imgur.com/wZ9NhJ2.png">Image link</a> (uses width=31, height=35)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-standard.fnt">Gentium-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-standard.png">Gentium-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-license.txt">Gentium-license.txt</a></li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Gentium.ttf
     */
    public static Font getGentium()
    {
        initialize();
        if(instance.gentium == null)
        {
            try {
                instance.gentium = new Font("Gentium-standard.fnt", Font.DistanceFieldType.STANDARD, -1f, 0f, -2f, 0f, true)
                        .scaleTo(31, 35).setTextureFilter().setName("Gentium");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.gentium != null)
            return new Font(instance.gentium);
        throw new RuntimeException("Assets for getGentium() not found.");
    }

    private Font gentiumSDF;
    /**
     * Returns a Font already configured to use a variable-width serif font with excellent Unicode support, that should
     * scale cleanly to even very large sizes (using an SDF technique). You usually will want to reduce the line height
     * of this Font after you scale it; using {@code KnownFonts.getGentium().scaleTo(55, 45).adjustLineHeight(0.8f)}
     * usually works. Caches the result for later calls. The font used is Gentium, an open-source (SIL Open Font
     * License) typeface by SIL (see <a href="https://software.sil.org/gentium/">SIL's page on Gentium here</a>). It
     * supports a lot of glyphs, including quite a
     * bit of extended Latin, Greek, and Cyrillic, as well as some less-common glyphs from various real languages. This
     * uses the Signed Distance Field (SDF) technique, which may be slightly fuzzy when zoomed in heavily, but should be
     * crisp enough when zoomed out.
     * <br>
     * Preview: <a href="https://i.imgur.com/e0cIBe3.png">Image link</a> (uses width=60, height=45, adjustLineHeight(0.8f))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-sdf.fnt">Gentium-sdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-sdf.png">Gentium-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-license.txt">Gentium-license.txt</a></li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Gentium.ttf using SDF
     */
    public static Font getGentiumSDF()
    {
        initialize();
        if(instance.gentiumSDF == null)
        {
            try {
                instance.gentiumSDF = new Font("Gentium-sdf.fnt", "Gentium-sdf.png", SDF, 8f, 0f, 0f, 0f, true)
                        .scaleTo(60, 45).adjustLineHeight(0.8f).setCrispness(1.5f).setName("Gentium (SDF)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.gentiumSDF != null)
            return new Font(instance.gentiumSDF);
        throw new RuntimeException("Assets for getGentiumSDF() not found.");
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
     * can use {@link #getCozette()} for a different bitmap font.
     * <br>
     * Preview: <a href="https://i.imgur.com/SQLGfWt.png">Image link</a> (uses width=8, height=16)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/IBM-8x16-standard.font">IBM-8x16-standard.font</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/IBM-8x16-standard.png">IBM-8x16-standard.png</a></li>
     * </ul>
     * @return the Font object that represents an 8x16 font included with early IBM computers
     */
    public static Font getIBM8x16()
    {
        initialize();
        if(instance.ibm8x16 == null)
        {
            try {
                instance.ibm8x16 = new Font("IBM-8x16-standard.font", true)
                        .fitCell(8, 16, false).setName("IBM 8x16");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.ibm8x16 != null)
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
     * Preview: <a href="https://i.imgur.com/a7M3HB9.png">Image link</a> (uses width=10, height=26)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-Custom-standard.fnt">Inconsolata-LGC-Custom-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-Custom-standard.png">Inconsolata-LGC-Custom-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-License.txt">Inconsolata-LGC-License.txt</a></li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Inconsolata LGC Custom
     */
    public static Font getInconsolata()
    {
        initialize();
        if(instance.inconsolata == null)
        {
            try {
                instance.inconsolata = new Font("Inconsolata-LGC-Custom-standard.fnt", "Inconsolata-LGC-Custom-standard.png", STANDARD, 0f, 0f, -4f, 0f, true)
                        .scaleTo(10, 26).setTextureFilter().setName("Inconsolata LGC");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.inconsolata != null)
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
     * Preview: <a href="https://i.imgur.com/MUzaRxR.png">Image link</a> (uses width=10, height=26)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-Custom-msdf.fnt">Inconsolata-LGC-Custom-msdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-Custom-msdf.png">Inconsolata-LGC-Custom-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-License.txt">Inconsolata-LGC-License.txt</a></li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Inconsolata LGC Custom using MSDF
     */
    public static Font getInconsolataMSDF()
    {
        initialize();
        if(instance.inconsolataMSDF == null)
        {
            try {
                instance.inconsolataMSDF = new Font("Inconsolata-LGC-Custom-msdf.fnt", "Inconsolata-LGC-Custom-msdf.png", MSDF, 0f, 1f, -12f, -8f, true)
                        .scaleTo(10, 26).setName("Inconsolata LGC (MSDF)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.inconsolataMSDF != null)
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
     * Preview: <a href="https://i.imgur.com/R5Ztqym.png">Image link</a> (uses .scaleTo(12, 26).fitCell(10, 26, false).adjustLineHeight(0.9f))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-standard.fnt">Iosevka-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-standard.png">Iosevka-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.md">Iosevka-License.md</a></li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Iosevka.ttf
     */
    public static Font getIosevka()
    {
        initialize();
        if(instance.iosevka == null)
        {
            try {
                instance.iosevka = new Font("Iosevka-standard.fnt", "Iosevka-standard.png", STANDARD, -2f, 0f, 0f, 0f, true)
                        .scaleTo(12, 26).fitCell(10, 26, false).adjustLineHeight(0.9f).setTextureFilter().setName("Iosevka");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.iosevka != null)
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
     * Preview: <a href="https://i.imgur.com/3mH04Nn.png">Image link</a> (uses .scaleTo(12, 25).fitCell(9, 25, false))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-msdf.fnt">Iosevka-msdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-msdf.png">Iosevka-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.md">Iosevka-License.md</a></li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Iosevka.ttf using MSDF
     */
    public static Font getIosevkaMSDF()
    {
        initialize();
        if(instance.iosevkaMSDF == null)
        {
            try {
                // NOTE: If the .fnt file is changed, the manual adjustment to '_' (id=95) will be lost. yoffset was changed to 4.
                // This might be OK now that this uses the box-drawing underline.
                instance.iosevkaMSDF = new Font("Iosevka-msdf.fnt", "Iosevka-msdf.png", MSDF, 0f, 2f, 0f, 0f, true)
                        .setCrispness(0.75f).scaleTo(12, 25).fitCell(9, 25, false).setName("Iosevka (MSDF)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.iosevkaMSDF != null)
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
     * Preview: <a href="https://i.imgur.com/ZTWOnOg.png">Image link</a> (uses .scaleTo(12, 26).fitCell(10, 26, false))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-sdf.fnt">Iosevka-sdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-sdf.png">Iosevka-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.md">Iosevka-License.md</a></li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Iosevka.ttf using SDF
     */
    public static Font getIosevkaSDF()
    {
        initialize();
        if(instance.iosevkaSDF == null)
        {
            try {
                // NOTE: If the .fnt file is changed, the manual adjustment to '_' (id=95) will be lost. yoffset was changed to 4.
                // This might be OK now that this uses the box-drawing underline.
                instance.iosevkaSDF = new Font("Iosevka-sdf.fnt", "Iosevka-sdf.png", SDF, 0f, 0f, -2f, 0f, true)
                        .setCrispness(0.75f).scaleTo(12, 26).fitCell(10, 26, false).setName("Iosevka (SDF)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.iosevkaSDF != null)
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
     * Preview: <a href="https://i.imgur.com/vtlq3J7.png">Image link</a> (uses .scaleTo(12, 26).fitCell(10, 26, false).adjustLineHeight(0.9f))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-standard.fnt">Iosevka-Slab-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-standard.png">Iosevka-Slab-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.md">Iosevka-License.md</a></li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Iosevka-Slab.ttf
     */
    public static Font getIosevkaSlab()
    {
        initialize();
        if(instance.iosevkaSlab == null)
        {
            try {
                instance.iosevkaSlab = new Font("Iosevka-Slab-standard.fnt", "Iosevka-Slab-standard.png", STANDARD, 0f, 0f, 0f, 0f, true)
                        .scaleTo(12, 26).fitCell(10, 26, false).adjustLineHeight(0.9f).setTextureFilter().setName("Iosevka Slab");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.iosevkaSlab != null)
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
     * Preview: <a href="https://i.imgur.com/vJJ62uA.png">Image link</a> (uses .scaleTo(12, 25).fitCell(9, 25, false))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-msdf.fnt">Iosevka-Slab-msdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-msdf.png">Iosevka-Slab-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.md">Iosevka-License.md</a></li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Iosevka-Slab.ttf using MSDF
     */
    public static Font getIosevkaSlabMSDF()
    {
        initialize();
        if(instance.iosevkaSlabMSDF == null)
        {
            try {
                // NOTE: If the .fnt file is changed, the manual adjustment to '_' (id=95) will be lost. yoffset was changed to 4.
                // This might be OK now that this uses the box-drawing underline.
                instance.iosevkaSlabMSDF = new Font("Iosevka-Slab-msdf.fnt", "Iosevka-Slab-msdf.png", MSDF, 0f, 2f, 0f, 0f, true)
                        .setCrispness(0.75f).scaleTo(12, 25).fitCell(9, 25, false).setName("Iosevka Slab (MSDF)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.iosevkaSlabMSDF != null)
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
     * Preview: <a href="https://i.imgur.com/cl1OBdz.png">Image link</a> (uses scaleTo(12, 26).fitCell(10, 26, false))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-sdf.fnt">Iosevka-Slab-sdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-sdf.png">Iosevka-Slab-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.md">Iosevka-License.md</a></li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Iosevka-Slab.ttf using SDF
     */
    public static Font getIosevkaSlabSDF()
    {
        initialize();
        if(instance.iosevkaSlabSDF == null)
        {
            try {
                // NOTE: If the .fnt file is changed, the manual adjustment to '_' (id=95) will be lost. yoffset was changed to 4.
                // This might be OK now that this uses the box-drawing underline.
                instance.iosevkaSlabSDF = new Font("Iosevka-Slab-sdf.fnt", "Iosevka-Slab-sdf.png", SDF, 0f, 0f, -2f, 0f, true)
                        .setCrispness(0.75f).scaleTo(12, 26).fitCell(10, 26, false).setName("Iosevka Slab (SDF)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.iosevkaSlabSDF != null)
            return new Font(instance.iosevkaSlabSDF);
        throw new RuntimeException("Assets for getIosevkaSlabSDF() not found.");
    }

    private Font kingthingsFoundation;
    /**
     * Returns a Font already configured to use a fairly-legible variable-width ornamental/medieval font, that should
     * scale pretty well from a height of about 90 down to a height of maybe 30.
     * Caches the result for later calls. The font used is Kingthings Foundation, a free (custom permissive license)
     * typeface; this has faux-bold applied already in order to make some ornamental curls visible at more sizes. You
     * can still apply bold again using markup. It supports only ASCII.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/HvvPiHN.png">Image link</a> (uses width=25, height=30)
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
     * @return the Font object that can represent many sizes of the font KingthingsFoundation.ttf
     */
    public static Font getKingthingsFoundation()
    {
        initialize();
        if(instance.kingthingsFoundation == null)
        {
            try {
                instance.kingthingsFoundation = new Font("KingthingsFoundation-standard.fnt", STANDARD, 2, -6, -2f, 0, false)
                        .scaleTo(25, 30).setTextureFilter().setName("KingThings Foundation");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.kingthingsFoundation != null)
            return new Font(instance.kingthingsFoundation);
        throw new RuntimeException("Assets for getKingthingsFoundation() not found.");
    }

    private Font libertinusSerif;
    /**
     * Returns a Font already configured to use a variable-width serif font with good Unicode support, that should
     * scale cleanly to even very large sizes (using an MSDF technique).
     * Caches the result for later calls. The font used is Libertinus Serif, an open-source (SIL Open Font
     * License) typeface. It supports a lot of glyphs, including quite a bit of extended Latin, Greek, and Cyrillic.
     * This uses the Multi-channel Signed Distance Field (MSDF) technique, which should be very sharp. This probably
     * needs to be scaled so that it has much larger width than height; the default is 150x32.
     * <br>
     * Preview: <a href="https://i.imgur.com/Se6iRvR.png">Image link</a> (uses width=132, height=28)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/LibertinusSerif-Regular-msdf.fnt">LibertinusSerif-Regular-msdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/LibertinusSerif-Regular-msdf.png">LibertinusSerif-Regular-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/LibertinusSerif-License.txt">LibertinusSerif-License.txt</a></li>
     * </ul>
     * @return the Font object that can represent many sizes of the font LibertinusSerif.ttf using MSDF
     */
    public static Font getLibertinusSerif()
    {
        initialize();
        if(instance.libertinusSerif == null)
        {
            try {
                instance.libertinusSerif = new Font("LibertinusSerif-Regular-msdf.fnt", "LibertinusSerif-Regular-msdf.png", MSDF, -2, 0, -2, 0, true)
                        .scaleTo(132, 28).setCrispness(1f).setName("Libertinus Serif (MSDF)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.libertinusSerif != null)
            return new Font(instance.libertinusSerif);
        throw new RuntimeException("Assets for getLibertinusSerif() not found.");
    }

    private Font openSans;
    /**
     * Returns a Font configured to use a clean variable-width font, Open Sans. It has good extended-Latin coverage, but
     * does not support Greek, Cyrillic, or other scripts. This makes an especially large font by default, but can be
     * scaled down nicely.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/nhSyfaD.png">Image link</a> (uses width=20, height=28, adjustLineHeight(0.65f))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenSans-standard.fnt">OpenSans-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenSans-standard.png">OpenSans-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenSans-License.txt">OpenSans-License.txt</a></li>
     * </ul>
     * @return the Font object that represents the variable-width font OpenSans
     */
    public static Font getOpenSans()
    {
        initialize();
        if(instance.openSans == null)
        {
            try {
                instance.openSans = new Font("OpenSans-standard.fnt", "OpenSans-standard.png", STANDARD, 2, -4, 0, 4, true)
                        .scaleTo(20, 28).adjustLineHeight(0.65f).setTextureFilter().setName("OpenSans");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.openSans != null)
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
     * Preview: <a href="https://i.imgur.com/9jdAurl.png">Image link</a> (uses width=31, height=35)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Oxanium-standard.fnt">Oxanium-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Oxanium-standard.png">Oxanium-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Oxanium-License.txt">Oxanium-License.txt</a></li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Oxanium.ttf
     */
    public static Font getOxanium()
    {
        initialize();
        if(instance.oxanium == null)
        {
            try {
                instance.oxanium = new Font("Oxanium-standard.fnt", STANDARD, -12, -12, -4, 0, true)
                        .scaleTo(31, 35).setTextureFilter().setName("Oxanium");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.oxanium != null)
            return new Font(instance.oxanium);
        throw new RuntimeException("Assets for getOxanium() not found.");
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
     * Preview: <a href="https://i.imgur.com/Kh0eMdT.png">Image link</a> (uses width=25, height=35)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/RobotoCondensed-standard.fnt">RobotoCondensed-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/RobotoCondensed-standard.png">RobotoCondensed-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/RobotoCondensed-License.txt">RobotoCondensed-License.txt</a></li>
     * </ul>
     * @return the Font object that can represent many sizes of the font RobotoCondensed.ttf
     */
    public static Font getRobotoCondensed()
    {
        initialize();
        if(instance.robotoCondensed == null)
        {
            try {
                instance.robotoCondensed = new Font("RobotoCondensed-standard.fnt", STANDARD, 0, 12, 0, 0, false)
                        .scaleTo(25, 30).setTextureFilter().setName("Roboto Condensed");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.robotoCondensed != null)
            return new Font(instance.robotoCondensed);
        throw new RuntimeException("Assets for getRobotoCondensed() not found.");
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
     * Preview: <a href="https://i.imgur.com/E3rCelE.png">Image link</a> (uses width=30, height=35)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/YanoneKaffeesatz-standard.fnt">YanoneKaffeesatz-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/YanoneKaffeesatz-standard.png">YanoneKaffeesatz-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/YanoneKaffeesatz-License.txt">YanoneKaffeesatz-License.txt</a></li>
     * </ul>
     * @return the Font object that can represent many sizes of the font YanoneKaffeesatz.ttf
     */
    public static Font getYanoneKaffeesatz()
    {
        initialize();
        if(instance.kaffeesatz == null)
        {
            try {
                instance.kaffeesatz = new Font("YanoneKaffeesatz-standard.fnt", STANDARD, 2f, 0, 0f, 0, true)
                        .scaleTo(30, 35).setTextureFilter().setName("Yanone Kaffeesatz");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.kaffeesatz != null)
            return new Font(instance.kaffeesatz);
        throw new RuntimeException("Assets for getYanoneKaffeesatz() not found.");
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
     * @return a new array containing all Font instances this knows
     */
    public static Font[] getAll() {
        return new Font[]{getAStarry(), getBitter(), getCanada(), getCascadiaMono(), getCozette(), getDejaVuSansMono(), getGentium(),
                getGentiumSDF(), getIBM8x16(), getInconsolata(), getInconsolataMSDF(), getIosevka(), getIosevkaMSDF(),
                getIosevkaSDF(), getIosevkaSlab(), getIosevkaSlabMSDF(), getIosevkaSlabSDF(), getKingthingsFoundation(),
                getLibertinusSerif(), getOpenSans(), getOxanium(), getRobotoCondensed(), getYanoneKaffeesatz()};
    }
    /**
     * Returns a new array of Font instances, calling each getXyz() method in this class that returns any
     * non-distance-field Font.
     * This will only function at all if all the assets (for every known standard Font) are present and load-able.
     * @return a new array containing all non-distance-field Font instances this knows
     */
    public static Font[] getAllStandard() {
        return new Font[]{getBitter(), getCanada(), getCozette(), getGentium(), getIBM8x16(), getInconsolata(), getIosevka(),
                getIosevkaSlab(), getKingthingsFoundation(), getOpenSans(), getOxanium(), getRobotoCondensed(),
                getYanoneKaffeesatz()};
    }

    /**
     * Returns a Font ({@link #getGentium()}) with a FontFamily configured so that all non-distance-field Fonts can be
     * used with syntax like {@code [@Sans]}. The names this supports can be accessed with code using
     * {@code getStandardFamily().family.fontAliases.keys()}. These names so far are:
     * <ul>
     *     <li>Serif</li>
     *     <li>Sans</li>
     *     <li>Mono</li>
     *     <li>Condensed</li>
     *     <li>Humanist</li>
     *     <li>Retro</li>
     *     <li>Slab</li>
     *     <li>Bitter</li>
     *     <li>Canada</li>
     *     <li>Cozette</li>
     *     <li>Iosevka</li>
     *     <li>Medieval</li>
     *     <li>Future</li>
     * </ul>
     * You can also always use the full name of one of these fonts, which can be obtained using {@link Font#getName()}.
     * This will only function at all if all the assets (for every known standard Font) are present and load-able.
     *
     * @return a Font that can switch between 13 different Fonts in its FontFamily, to any non-distance-field Font this knows
     */
    public static Font getStandardFamily(){
        Font.FontFamily family = new Font.FontFamily(
                new String[]{"Serif", "Sans", "Mono", "Condensed", "Humanist",
                        "Retro", "Slab", "Bitter", "Canada", "Cozette", "Iosevka",
                        "Medieval", "Future"},
                new Font[]{getGentium(), getOpenSans(), getInconsolata(), getRobotoCondensed(), getYanoneKaffeesatz(),
                        getIBM8x16(), getIosevkaSlab(), getBitter(), getCanada(), getCozette(), getIosevka(),
                        getKingthingsFoundation(), getOxanium()});
        return family.connected[0].setFamily(family);
    }

    /**
     * Returns a new array of Font instances, calling each getXyz() method in this class that returns any SDF Font.
     * This will only function at all if all the assets (for every known SDF Font) are present and load-able.
     * @return a new array containing all SDF Font instances this knows
     */
    public static Font[] getAllSDF() {
        return new Font[]{getGentiumSDF(), getIosevkaSDF(), getIosevkaSlabSDF()};
    }

    /**
     * Returns a new array of Font instances, calling each getXyz() method in this class that returns any MSDF Font.
     * This will only function at all if all the assets (for every known MSDF Font) are present and load-able.
     * @return a new array containing all MSDF Font instances this knows
     */
    public static Font[] getAllMSDF() {
        return new Font[]{getAStarry(), getCascadiaMono(), getDejaVuSansMono(), getInconsolataMSDF(), getIosevkaMSDF(),
                getIosevkaSlabMSDF(), getLibertinusSerif()};
    }

    @Override
    public void dispose() {

        if (astarry != null) {
            astarry.dispose();
            astarry = null;
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
        if (gentiumSDF != null) {
            gentiumSDF.dispose();
            gentiumSDF = null;
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
        if (libertinusSerif != null) {
            libertinusSerif.dispose();
            libertinusSerif = null;
        }
        if (openSans != null) {
            openSans.dispose();
            openSans = null;
        }
        if (oxanium != null) {
            oxanium.dispose();
            oxanium = null;
        }
        if (robotoCondensed != null) {
            robotoCondensed.dispose();
            robotoCondensed = null;
        }
        if (kaffeesatz != null) {
            kaffeesatz.dispose();
            kaffeesatz = null;
        }
    }
}
