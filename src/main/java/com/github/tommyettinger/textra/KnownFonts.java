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
 * Typical usage involves calling one of the static methods like {@link #getCozette()} or {@link #getGentium()} to get a
 * particular Font. This knows a fair amount of fonts, but it doesn't require the image assets for all of those to be
 * present in a game -- only the files mentioned in the documentation for a method are needed, and only if you call that
 * method. It's likely that many games would only use one Font, and so would generally only need a .fnt file, a
 * .png file, and some kind of license file. They could ignore all other assets required by other fonts.
 */
public class KnownFonts implements LifecycleListener {
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

    private Font cozette;
    /**
     * Returns a Font configured to use a cozy fixed-width bitmap font,
     * <a href="https://github.com/slavfox/Cozette">Cozette by slavfox</a>. Cozette has broad coverage of Unicode,
     * including Greek, Cyrillic, Braille, and tech-related icons. This does not scale except to integer
     * multiples, but it should look very crisp at its default size of 7x13 pixels.
     * <br>
     *
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cozette.fnt</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cozette.png</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cozette-license.txt</li>
     * </ul>
     * @return the Font object that represents the 7x13px font Cozette
     */
    public static Font getCozette()
    {
        initialize();
        if(instance.cozette == null)
        {
            try {
                instance.cozette = new Font("Cozette.fnt", "Cozette.png", STANDARD, 1, 1, 0, -1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.cozette != null)
            return new Font(instance.cozette);
        throw new RuntimeException("Assets for getCozette() not found.");
    }

    private Font openSans;
    /**
     * Returns a Font configured to use a clean variable-width font, Open Sans. This makes an especially large
     * font by default, but can be scaled down nicely.
     * <br>
     *
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenSans.fnt</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenSans.png</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenSans-License.txt</li>
     * </ul>
     * @return the Font object that represents the variable-width font OpenSans
     */
    public static Font getOpenSans()
    {
        initialize();
        if(instance.openSans == null)
        {
            try {
                instance.openSans = new Font("OpenSans.fnt", "OpenSans.png", STANDARD, 2, 0, 0, 0);
                instance.openSans.setTextureFilter();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.openSans != null)
            return new Font(instance.openSans);
        throw new RuntimeException("Assets for getOpenSans() not found.");
    }

    private Font astarry;
    /**
     * Returns a Font already configured to use a square font with 45-degree angled sections, based on the
     * typeface used on the Atari ST console, that should scale cleanly to many sizes. This font only supports ASCII,
     * but it supports all of it. Caches the result for later calls. The font is "a-starry", based on "Atari ST
     * (low-res)" by Damien Guard; it is available under a CC-BY-SA-3.0 license, which requires attribution to Damien
     * Guard (and technically Tommy Ettinger, because he made changes in a-starry) if you use it.
     * <br>
     *
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/AStarry-msdf.fnt</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/AStarry-msdf.png</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/AStarry-license.txt</li>
     * </ul>
     * @return the Font object that can represent many sizes of the font A Starry using MSDF
     */
    public static Font getAStarry()
    {
        initialize();
        if(instance.astarry == null)
        {
            try {
                instance.astarry = new Font("AStarry-msdf.fnt", "AStarry-msdf.png", MSDF, 0, 1, 0, 0).scaleTo(10, 10);
                instance.astarry.distanceFieldCrispness = 1.5f;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.astarry != null)
            return new Font(instance.astarry);
        throw new RuntimeException("Assets for getAStarry() not found.");
    }

    private Font cascadiaMono;
    /**
     * Returns a Font already configured to use a quirky fixed-width font with good Unicode support
     * and a humanist style, that should scale cleanly to even very large sizes (using an MSDF technique).
     * Caches the result for later calls. The font used is Cascadia Code Mono, an open-source (SIL Open Font
     * License) typeface by Microsoft (see https://github.com/microsoft/cascadia-code ). It supports a lot of glyphs,
     * including most extended Latin (though it doesn't support a handful of chars used by FakeLanguageGen), Greek,
     * Braille, and Cyrillic, but also the necessary box drawing characters. This uses the Multi-channel Signed Distance
     * Field (MSDF) technique as opposed to the normal Signed Distance Field technique, which gives the rendered font
     * sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     *
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/CascadiaMono-msdf.fnt</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/CascadiaMono-msdf.png</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cascadia-license.txt</li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Cascadia Code Mono using MSDF
     */
    public static Font getCascadiaMono()
    {
        initialize();
        if(instance.cascadiaMono == null)
        {
            try {
                instance.cascadiaMono = new Font("CascadiaMono-msdf.fnt", "CascadiaMono-msdf.png", MSDF, 2f, 1f, -5.5f, -1.5f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.cascadiaMono != null)
            return new Font(instance.cascadiaMono);
        throw new RuntimeException("Assets for getCascadiaMono() not found.");
    }

    private Font dejaVuSansMono;
    /**
     * A nice old standby font with very broad language support, DejaVu Sans Mono is fixed-width and can be clearly
     * readable but doesn't do anything unusual stylistically. It really does handle a lot of glyphs; not only does this
     * have practically all Latin glyphs in Unicode (enough to support everything from Icelandic to Vietnamese), it has
     * Greek (including Extended), Cyrillic (including some optional glyphs), IPA, Armenian (maybe the only font here to
     * do so), Georgian (which won't be treated correctly by some case-insensitive code, so it should only be used if
     * case doesn't matter), and Lao. It has full box drawing and Braille support, handles a wide variety of math
     * symbols, technical marks, and dingbats, etc.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVuSansMono-msdf.fnt</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVuSansMono-msdf.png</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVuSansMono-License.txt</li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Inconsolata LGC using MSDF
     */
    public static Font getDejaVuSansMono()
    {
        initialize();
        if(instance.dejaVuSansMono == null)
        {
            try {
                instance.dejaVuSansMono = new Font("DejaVuSansMono-msdf.fnt", "DejaVuSansMono-msdf.png", MSDF, 1f, 4f, -1.5f, -4.5f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.dejaVuSansMono != null)
            return new Font(instance.dejaVuSansMono);
        throw new RuntimeException("Assets for getDejaVuSansMono() not found.");
    }


    private Font inconsolataLGC;
    /**
     * A customized version of Inconsolata LGC, a fixed-width geometric font that supports a large range of Latin,
     * Greek, and Cyrillic glyphs, plus box drawing and some dingbat characters (like zodiac signs). The original font
     * Inconsolata is by Raph Levien, and various other contributors added support for other languages.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-Custom-msdf.fnt</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-Custom-msdf.png</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-License.txt</li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Inconsolata LGC using MSDF
     */
    public static Font getInconsolataLGC()
    {
        initialize();
        if(instance.inconsolataLGC == null)
        {
            try {
                instance.inconsolataLGC = new Font("Inconsolata-LGC-Custom-msdf.fnt", "Inconsolata-LGC-Custom-msdf.png", MSDF, 5f, 1f, -10f, -8f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.inconsolataLGC != null)
            return new Font(instance.inconsolataLGC);
        throw new RuntimeException("Assets for getInconsolataLGC() not found.");
    }

    private Font iosevka;
    /**
     * Returns a Font already configured to use a highly-legible fixed-width font with good Unicode support
     * and a sans-serif geometric style, that should scale cleanly to even very large sizes (using an MSDF technique).
     * Caches the result for later calls. The font used is Iosevka, an open-source (SIL Open Font
     * License) typeface by Belleve Invis (see https://be5invis.github.io/Iosevka/ ), and it uses several customizations
     * thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of extended Latin,
     * Greek, and Cyrillic, but also the necessary box drawing characters. This uses the Multi-channel Signed Distance
     * Field (MSDF) technique as opposed to the normal Signed Distance Field technique, which gives the rendered font
     * sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * Preview: <a href="https://i.imgur.com/YlzFEVX.png">Image link</a>
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-msdf.fnt</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-msdf.png</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.md</li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Iosevka.ttf using MSDF
     */
    public static Font getIosevka()
    {
        initialize();
        if(instance.iosevka == null)
        {
            try {
                instance.iosevka = new Font("Iosevka-msdf.fnt", "Iosevka-msdf.png", MSDF, 3f, 6, -4f, -7);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.iosevka != null)
            return new Font(instance.iosevka);
        throw new RuntimeException("Assets for getIosevka() not found.");
    }

    private Font iosevkaSlab;
    /**
     * Returns a Font already configured to use a highly-legible fixed-width font with good Unicode support
     * and a slab-serif geometric style, that should scale cleanly to even very large sizes (using an MSDF technique).
     * Caches the result for later calls. The font used is Iosevka with Slab style, an open-source (SIL Open Font
     * License) typeface by Belleve Invis (see https://be5invis.github.io/Iosevka/ ), and it uses several customizations
     * thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of extended Latin,
     * Greek, and Cyrillic, but also the necessary box drawing characters. This uses the Multi-channel Signed Distance
     * Field (MSDF) technique as opposed to the normal Signed Distance Field technique, which gives the rendered font
     * sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * Preview: <a href="https://i.imgur.com/YlzFEVX.png">Image link</a>
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-msdf.fnt</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-msdf.png</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.md</li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Iosevka-Slab.ttf using MSDF
     */
    public static Font getIosevkaSlab()
    {
        initialize();
        if(instance.iosevkaSlab == null)
        {
            try {
                instance.iosevkaSlab = new Font("Iosevka-Slab-msdf.fnt", "Iosevka-Slab-msdf.png", MSDF, 3f, 6, -4f, -7);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.iosevkaSlab != null)
            return new Font(instance.iosevkaSlab);
        throw new RuntimeException("Assets for getIosevkaSlab() not found.");
    }

    private Font gentium;
    /**
     * Returns a Font already configured to use a variable-width serif font with excellent Unicode support, that should
     * scale cleanly to even very large sizes (using an SDF technique).
     * Caches the result for later calls. The font used is Gentium, an open-source (SIL Open Font
     * License) typeface by SIL (see https://software.sil.org/gentium/ ). It supports a lot of glyphs, including quite a
     * bit of extended Latin, Greek, and Cyrillic, as well as some less-common glyphs from various real languages. This
     * uses the Signed Distance Field (SDF) technique, which may be slightly fuzzy when zoomed in heavily, but should be
     * crisp enough when zoomed out.
     * <br>
     * Preview: <a href="https://i.imgur.com/JXGbHVf.png">Image link</a>
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-sdf.fnt</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-sdf.png</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-license.txt</li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Gentium.ttf using SDF
     */
    public static Font getGentium()
    {
        initialize();
        if(instance.gentium == null)
        {
            try {
                instance.gentium = new Font("Gentium-sdf.fnt", "Gentium-sdf.png", SDF, 0f, 5f, 0f, -5f).adjustLineHeight(0.8f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.gentium != null)
            return new Font(instance.gentium);
        throw new RuntimeException("Assets for getGentium() not found.");
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
     * Preview: <a href="https://i.imgur.com/nESDlFJ.png">Image link</a>
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/LibertinusSerif-Regular-msdf.fnt</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/LibertinusSerif-Regular-msdf.png</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/LibertinusSerif-License.txt</li>
     * </ul>
     * @return the Font object that can represent many sizes of the font LibertinusSerif.ttf using MSDF
     */
    public static Font getLibertinusSerif()
    {
        initialize();
        if(instance.libertinusSerif == null)
        {
            try {
                instance.libertinusSerif = new Font("LibertinusSerif-Regular-msdf.fnt", "LibertinusSerif-Regular-msdf.png", MSDF, 5, 2, -2, -2).scaleTo(150, 32);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.libertinusSerif != null)
            return new Font(instance.libertinusSerif);
        throw new RuntimeException("Assets for getLibertinusSerif() not found.");
    }

    private Font kingthingsFoundation;
    /**
     * Returns a Font already configured to use a fairly-legible variable-width ornamental/medieval font, that should
     * scale pretty well from a height of about 90 down to a height of maybe 30.
     * Caches the result for later calls. The font used is Kingthings Foundation, a free (custom permissive license)
     * typeface; this has faux-bold applied already in order to make some ornamental curls visible at more sizes. You
     * can still apply bold again using markup. It supports only ASCII.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * <br>
     * Preview: <a href="https://i.imgur.com/DwXRXd3.png">Image link</a> (uses width=45, height=60)
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/KingthingsFoundation-bold.fnt</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/KingthingsFoundation-bold.png</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-License.txt</li>
     * </ul>
     * You may instead want the non-bold version, but this doesn't have a pre-made instance in KnownFonts:
     * <ul>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/KingthingsFoundation-standard.fnt</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/KingthingsFoundation-standard.png</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-License.txt</li>
     * </ul>
     * @return the Font object that can represent many sizes of the font KingthingsFoundation.ttf
     */
    public static Font getKingthingsFoundation()
    {
        initialize();
        if(instance.kingthingsFoundation == null)
        {
            try {
                instance.kingthingsFoundation = new Font("KingthingsFoundation-bold.fnt", STANDARD, 2, 0, -2.5f, 0).setTextureFilter();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.kingthingsFoundation != null)
            return new Font(instance.kingthingsFoundation);
        throw new RuntimeException("Assets for getKingthingsFoundation() not found.");
    }

    private Font oxanium;
    /**
     * Returns a Font already configured to use a variable-width "science-fiction/high-tech" font, that should
     * scale pretty well down, but not up.
     * Caches the result for later calls. The font used is Oxanium, a free (OFL) typeface. It supports a lot of Latin
     * and extended Latin, but not Greek or Cyrillic.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * <br>
     * Preview: <a href="https://i.imgur.com/uQzCEo9.png">Image link</a> (uses width=40, height=50)
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Oxanium-standard.fnt</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Oxanium-standard.png</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Oxanium-License.txt</li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Oxanium.ttf
     */
    public static Font getOxanium()
    {
        initialize();
        if(instance.oxanium == null)
        {
            try {
                instance.oxanium = new Font("Oxanium-standard.fnt", STANDARD, 3, 0, -3, 0).setTextureFilter();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.oxanium != null)
            return new Font(instance.oxanium);
        throw new RuntimeException("Assets for getOxanium() not found.");
    }

    private Font kaffeesatz;
    /**
     * Returns a Font already configured to use a variable-width, narrow, humanist font, that should
     * scale pretty well down, but not up.
     * Caches the result for later calls. The font used is Yanone Kaffeesatz, a free (OFL) typeface. It supports a lot
     * of Latin, Cyrillic, and some extended Latin, but not Greek.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * <br>
     * Preview: <a href="https://i.imgur.com/qSdhTsw.png">Image link</a> (uses width=45, height=60)
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/YanoneKaffeesatz-standard.fnt</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/YanoneKaffeesatz-standard.png</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/YanoneKaffeesatz-License.txt</li>
     * </ul>
     * @return the Font object that can represent many sizes of the font YanoneKaffeesatz.ttf
     */
    public static Font getYanoneKaffeesatz()
    {
        initialize();
        if(instance.kaffeesatz == null)
        {
            try {
                instance.kaffeesatz = new Font("YanoneKaffeesatz-standard.fnt", STANDARD, 2f, 0, -2f, 0).setTextureFilter();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.kaffeesatz != null)
            return new Font(instance.kaffeesatz);
        throw new RuntimeException("Assets for getYanoneKaffeesatz() not found.");
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
     * <br>
     * Preview: <a href="https://i.imgur.com/KFUOFSz.png">Image link</a> (uses width=40, height=58)
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Canada1500-standard.fnt</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Canada1500-standard.png</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Canada1500-License.txt</li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Canada1500.ttf
     */
    public static Font getCanada()
    {
        initialize();
        if(instance.canada == null)
        {
            try {
                instance.canada = new Font("Canada1500-standard.fnt", STANDARD, 0, 0, 0, 0).setTextureFilter();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.canada != null)
            return new Font(instance.canada);
        throw new RuntimeException("Assets for getCanada() not found.");
    }

   private Font robotoCondensed;
    /**
     * Returns a Font already configured to use a very-legible condensed variable-width font with excellent Unicode
     * support, that should scale pretty well from a height of about 62 down to a height of maybe 20.
     * Caches the result for later calls. The font used is Roboto Condensed, a free (Apache 2.0) typeface by Christian
     * Robertson. It supports Latin-based scripts almost entirely, plus Greek, (extended) Cyrillic, and more.
     * This font is meant to be condensed in its natural appearance, but can be scaled to be wider if desired.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * <br>
     * Preview: <a href="https://i.imgur.com/ytyx61F.png">Image link</a> (uses width=40, height=58)
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/RobotoCondensed-standard.fnt</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/RobotoCondensed-standard.png</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/RobotoCondensed-License.txt</li>
     * </ul>
     * @return the Font object that can represent many sizes of the font RobotoCondensed.ttf
     */
    public static Font getRobotoCondensed()
    {
        initialize();
        if(instance.robotoCondensed == null)
        {
            try {
                instance.robotoCondensed = new Font("RobotoCondensed-standard.fnt", STANDARD, 0, 0, 0, 0).setTextureFilter(); 
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.robotoCondensed != null)
            return new Font(instance.robotoCondensed);
        throw new RuntimeException("Assets for getRobotoCondensed() not found.");
    }

    private Font ibm8x16;
    /**
     * Returns a Font configured to use a classic, nostalgic fixed-width bitmap font,
     * IBM 8x16 from the early, oft-beloved computer line. This font is notably loaded
     * from a SadConsole format file, which shouldn't affect how it looks (but in reality,
     * it might). This does not scale except to integer multiples, but it should look very
     * crisp at its default size of 8x16 pixels. This supports some extra characters, but
     * not at the typical Unicode codepoints.
     * <br>
     * This does not include a license because the source, <a href="https://github.com/Thraka/SadConsole/tree/master/Fonts">SadConsole's fonts</a>,
     * did not include one. It is doubtful that IBM would have any issues with respectful use
     * of their signature font throughout the 1980s, but if the legality is concerning, you
     * can use {@link #getCozette()} for a different bitmap font.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/IBM.font</li>
     *     <li>https://github.com/tommyettinger/textratypist/blob/main/knownFonts/IBM8x16.png</li>
     * </ul>
     * @return the Font object that represents an 8x16 font included with early IBM computers
     */
    public static Font getIBM8x16()
    {
        initialize();
        if(instance.ibm8x16 == null)
        {
            try {
                instance.ibm8x16 = new Font("IBM.font", true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.ibm8x16 != null)
            return new Font(instance.ibm8x16);
        throw new RuntimeException("Assets for getIBM8x16() not found.");
    }


    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

        if(cozette != null){
            cozette.dispose();
            cozette = null;
        }
        if(openSans != null){
            openSans.dispose();
            openSans = null;
        }
        if(astarry != null){
            astarry.dispose();
            astarry = null;
        }
        if(cascadiaMono != null){
            cascadiaMono.dispose();
            cascadiaMono = null;
        }
        if(dejaVuSansMono != null){
            dejaVuSansMono.dispose();
            dejaVuSansMono = null;
        }
        if(inconsolataLGC != null){
            inconsolataLGC.dispose();
            inconsolataLGC = null;
        }
        if(iosevka != null){
            iosevka.dispose();
            iosevka = null;
        }
        if(iosevkaSlab != null){
            iosevkaSlab.dispose();
            iosevkaSlab = null;
        }
        if(gentium != null){
            gentium.dispose();
            gentium = null;
        }
        if(libertinusSerif != null){
            libertinusSerif.dispose();
            libertinusSerif = null;
        }
        if(kingthingsFoundation != null){
            kingthingsFoundation.dispose();
            kingthingsFoundation = null;
        }
        if(oxanium != null){
            oxanium.dispose();
            oxanium = null;
        }
        if(kaffeesatz != null){
            kaffeesatz.dispose();
            kaffeesatz = null;
        }
        if(ibm8x16 != null){
            ibm8x16.dispose();
            ibm8x16 = null;
        }
    }
}
