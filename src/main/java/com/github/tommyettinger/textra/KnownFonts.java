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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.*;
import com.github.tommyettinger.textra.Font.DistanceFieldType;

import java.io.BufferedReader;
import java.util.Comparator;

import static com.github.tommyettinger.textra.Font.DistanceFieldType.*;

/**
 * Preconfigured static {@link Font} instances, with any important metric adjustments already applied. This uses a
 * singleton to ensure each font exists at most once, and implements {@link LifecycleListener} to ensure that when the
 * disposal stage of the lifecycle is called, then all Font instances here will be disposed and assigned null. This may
 * do more regarding its LifecycleListener code in the future, if Android turns out to need more work.
 * <br>
 * Typical usage involves calling one of the static methods like {@link #getCozette()} or {@link #getGentiumSDF()} to
 * get a particular Font. You can also have some added flexibility by instead calling
 * {@link #getFont(String, DistanceFieldType)} and passing it one of the constants in this class, such as
 * {@link #GENTIUM} or {@link #INCONSOLATA_LGC}, with a {@link DistanceFieldType} of your choice (such as
 * {@link DistanceFieldType#STANDARD}, which is also used if you don't pass a DistanceFieldType, or
 * {@link DistanceFieldType#SDF_OUTLINE}, which is primarily accessible by this technique). This knows a fair amount of
 * fonts, but it doesn't require the image assets for all of those to be present in a game -- only the files mentioned
 * in the documentation for a method are needed, and only if you call that method. It's likely that many games would
 * only use one Font, and so would generally only need a .fnt file, a .png file, and some kind of license file. They
 * could ignore all other assets required by other fonts. The files this class needs are looked for in the assets root
 * folder by default, but you can change the names or locations of asset files with {@link #setAssetPrefix(String)}.
 * <br>
 * There's some documentation for every known Font, including a link to a preview image and a listing of all required
 * files to use a Font. The required files include any license you need to abide by; this doesn't necessarily belong in
 * the {@code assets} folder like the rest of the files! Most of these fonts are either licensed under the OFL
 * or some Creative Commons license; the CC ones typically require attribution, but none of the fonts restrict usage to
 * noncommercial projects, and all are free as in beer as well. Please take care to attribute the authors of fonts you
 * use! Good fonts are not easy to make.
 * <br>
 * There are some special features in Font that are easier to use with parts of this class. {@link #getStandardFamily()}
 * pre-populates a FontFamily so you can switch between different fonts with the {@code [@Sans]} syntax.
 * {@link #addEmoji(Font)} adds all of Twitter's emoji from the <a href="https://github.com/twitter/twemoji">Twemoji</a>
 * project to a given font, which lets you enter emoji with the {@code [+man scientist, dark skin tone]} syntax or the
 * generally-easier {@code [+👨🏿‍🔬]} syntax. If you want to use names for emoji, you may want to consult "Twemoji.atlas"
 * for the exact names used; some names changed from the standard because of technical restrictions. Other types of
 * emoji are also available; {@link #addNotoEmoji(Font)} uses Noto Color Emoji instead of Twemoji, which may look better
 * with some art styles (especially more detailed ones), while {@link #addOpenMoji(Font, boolean)} adds either color or
 * monochrome (white lines only) emoji from the OpenMoji set, which has a more minimalist style. You can also add
 * the icons from <a href="https://game-icons.net">game-icons.net</a> using {@link #addGameIcons(Font)}. There is a
 * <a href="https://tommyettinger.github.io/twemoji-atlas/">preview site for Twemoji, with names</a>, another
 * <a href="https://tommyettinger.github.io/noto-emoji-atlas/">preview site for Noto Emoji, with names</a>, another
 * <a href="https://tommyettinger.github.io/openmoji-atlas/">preview site for OpenMoji, with names</a>, and another
 * <a href="https://tommyettinger.github.io/game-icons-net-atlas/">preview site for the game icons</a>. Note that the
 * names are different for Noto Emoji and the other emoji, but you can use the {@code [+👩🏽‍🚀]} syntax with a literal emoji
 * char for any of them.
 */
@SuppressWarnings("CallToPrintStackTrace")
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

    private final ObjectMap<String, Font> loaded = new ObjectMap<>(32);

    /** Base name for a fixed-width octagonal font. */
    public static final String A_STARRY = "A-Starry";
    /** Base name for a variable-width "sloppy" or "grungy" display font. */
    public static final String BIRDLAND_AEROPLANE = "Birdland-Aeroplane";
    /** Base name for a variable-width serif font. */
    public static final String BITTER = "Bitter";
    /** Base name for a variable-width sans font. */
    public static final String CANADA1500 = "Canada1500";
    /** Base name for a fixed-width programming font. */
    public static final String CASCADIA_MONO = "Cascadia-Mono";
    /** Base name for a variable-width handwriting font. */
    public static final String CAVEAT = "Caveat";
    /** Base name for a fixed-width octagonal font. */
    public static final String COMPUTER_SAYS_NO = "Computer-Says-No";
    /** Base name for a variable-width narrow sans font. */
    public static final String DEJAVU_SANS_CONDENSED = "DejaVu-Sans-Condensed";
    /** Base name for a fixed-width programming font. */
    public static final String DEJAVU_SANS_MONO = "DejaVu-Sans-Mono";
    /** Base name for a variable-width sans font. */
    public static final String DEJAVU_SANS = "DejaVu-Sans";
    /** Base name for a variable-width narrow serif font. */
    public static final String DEJAVU_SERIF_CONDENSED = "DejaVu-Serif-Condensed";
    /** Base name for a variable-width serif font. */
    public static final String DEJAVU_SERIF = "DejaVu-Serif";
    /** Base name for a variable-width Unicode-heavy serif font. */
    public static final String GENTIUM = "Gentium";
    /** Base name for a variable-width Unicode-heavy "swashbuckling" serif font. */
    public static final String GENTIUM_UN_ITALIC = "Gentium-Un-Italic";
    /** Base name for a variable-width geometric font. */
    public static final String GLACIAL_INDIFFERENCE = "Glacial-Indifference";
    /** Base name for a variable-width Unicode-heavy sans font. */
    public static final String GO_NOTO_UNIVERSAL = "Go-Noto-Universal";
    /** Base name for a variable-width heavy-weight serif font. */
    public static final String GRENZE = "Grenze";
    /** Base name for a fixed-width geometric/programming font. */
    public static final String INCONSOLATA_LGC = "Inconsolata-LGC";
    /** Base name for a fixed-width Unicode-heavy sans font. */
    public static final String IOSEVKA = "Iosevka";
    /** Base name for a fixed-width Unicode-heavy slab-serif font. */
    public static final String IOSEVKA_SLAB = "Iosevka-Slab";
    /** Base name for a variable-width ornate medieval font. */
    public static final String KINGTHINGS_FOUNDATION = "Kingthings-Foundation";
    /** Base name for a variable-width legible medieval font. */
    public static final String KINGTHINGS_PETROCK = "Kingthings-Petrock";
    /** Base name for a variable-width medium-weight serif font. */
    public static final String LIBERTINUS_SERIF = "Libertinus-Serif";
    /** Base name for a variable-width heavy-weight serif font. */
    public static final String LIBERTINUS_SERIF_SEMIBOLD = "Libertinus-Serif-Semibold";
    /** Base name for a variable-width geometric font. */
    public static final String NOW_ALT = "Now-Alt";
    /** Base name for a variable-width sans font. */
    public static final String OPEN_SANS = "Open-Sans";
    /** Base name for a variable-width geometric sans font. */
    public static final String OSTRICH_BLACK = "Ostrich-Black";
    /** Base name for a variable-width "flowy" sans font. */
    public static final String OVERLOCK = "Overlock";
    /** Base name for a variable-width "especially flowy" sans font. */
    public static final String OVERLOCK_UN_ITALIC = "Overlock-Un-Italic";
    /** Base name for a variable-width sci-fi font. */
    public static final String OXANIUM = "Oxanium";
    /** Base name for a variable-width narrow sans font. */
    public static final String ROBOTO_CONDENSED = "Roboto-Condensed";
    /** Base name for a variable-width sans-serif font. */
    public static final String SELAWIK = "Selawik";
    /** Base name for a variable-width script font. */
    public static final String TANGERINE = "Tangerine";
    /** Base name for a variable-width humanist sans font. */
    public static final String YANONE_KAFFEESATZ = "Yanone-Kaffeesatz";
    /** Base name for a variable-width "dark fantasy" font. */
    public static final String YATAGHAN = "Yataghan";

    /** Base name for a fixed-width pixel font. */
    public static final String COZETTE = "Cozette";
    /** Base name for a fixed-width CJK-heavy serif font. */
    public static final String HANAZONO = "Hanazono";
    /** Base name for a variable-width Unicode-heavy pixel font. */
    public static final String LANAPIXEL = "LanaPixel";
    /** Base name for a tiny variable-width Unicode-heavy pixel font. */
    public static final String QUANPIXEL = "QuanPixel";

    /** Base name for a fixed-width "traditional" pixel font. */
    public static final String IBM_8X16 = "IBM-8x16";

    public static final OrderedSet<String> JSON_NAMES = OrderedSet.with(
            A_STARRY, BIRDLAND_AEROPLANE, BITTER, CANADA1500, CASCADIA_MONO, CAVEAT, COMPUTER_SAYS_NO,
            DEJAVU_SANS_CONDENSED, DEJAVU_SANS_MONO, DEJAVU_SANS, DEJAVU_SERIF_CONDENSED, DEJAVU_SERIF,
            GENTIUM, GENTIUM_UN_ITALIC, GLACIAL_INDIFFERENCE, GO_NOTO_UNIVERSAL, GRENZE, INCONSOLATA_LGC,
            IOSEVKA, IOSEVKA_SLAB, KINGTHINGS_FOUNDATION, KINGTHINGS_PETROCK,
            LIBERTINUS_SERIF, LIBERTINUS_SERIF_SEMIBOLD, NOW_ALT, OPEN_SANS, OVERLOCK, OVERLOCK_UN_ITALIC,
            OSTRICH_BLACK, OXANIUM, ROBOTO_CONDENSED, SELAWIK, TANGERINE, YANONE_KAFFEESATZ,
            YATAGHAN);

    public static final OrderedSet<String> FNT_NAMES = OrderedSet.with(COZETTE, HANAZONO, LANAPIXEL, QUANPIXEL);

    public static final OrderedSet<String> SAD_NAMES = OrderedSet.with(IBM_8X16);

    public static final OrderedSet<String> STANDARD_NAMES = new OrderedSet<>(JSON_NAMES.size + FNT_NAMES.size + SAD_NAMES.size);
    public static final OrderedSet<String> SDF_NAMES = new OrderedSet<>(JSON_NAMES);
    public static final OrderedSet<String> MSDF_NAMES = new OrderedSet<>(JSON_NAMES);

    static {
        STANDARD_NAMES.addAll(JSON_NAMES);
        STANDARD_NAMES.addAll(FNT_NAMES);
        STANDARD_NAMES.addAll(SAD_NAMES);
    }

    /**
     * A general way to get a copied Font from the known set of fonts, this takes a String name (which can be from
     * {@link #JSON_NAMES}, {@link #FNT_NAMES}, or {@link #SAD_NAMES}, or more likely from a constant such as
     * {@link #OPEN_SANS}) and treats it as using no distance field effect ({@link DistanceFieldType#STANDARD}). It
     * looks up the appropriate file name, respecting asset prefix (see {@link #setAssetPrefix(String)}), creates the
     * Font if necessary, then returns a copy of it.
     * <br>
     * If a more specialized method modifies a Font in the {@link #loaded} cache when it runs, its effects will not
     * necessarily be shown here.
     *
     * @param baseName typically a constant such as {@link #OPEN_SANS} or {@link #LIBERTINUS_SERIF}
     * @return a copy of the Font with the given name
     */
    public static Font getFont(final String baseName) {
        return getFont(baseName, STANDARD);
    }

    /**
     * A general way to get a copied Font from the known set of fonts, this takes a String name (which can be from
     * {@link #JSON_NAMES}, {@link #FNT_NAMES}, or {@link #SAD_NAMES}, or more likely from a constant such as
     * {@link #OPEN_SANS}) and a DistanceFieldType (which is usually {@link DistanceFieldType#STANDARD}, but could also
     * be {@link DistanceFieldType#SDF}, {@link DistanceFieldType#MSDF}, or even {@link DistanceFieldType#SDF_OUTLINE}).
     * It looks up the appropriate file name, respecting asset prefix (see {@link #setAssetPrefix(String)}), creates the
     * Font if necessary, then returns a copy of it.
     * <br>
     * If a more specialized method modifies a Font in the {@link #loaded} cache when it runs, its effects will not
     * necessarily be shown here.
     *
     * @param baseName typically a constant such as {@link #OPEN_SANS} or {@link #LIBERTINUS_SERIF}
     * @param distanceField a DistanceFieldType, usually {@link DistanceFieldType#STANDARD}
     * @return a copy of the Font with the given name
     */
    public static Font getFont(final String baseName, DistanceFieldType distanceField) {
        if(baseName == null)
            throw new RuntimeException("Font name cannot be null.");
        if(distanceField == null) distanceField = STANDARD;
        initialize();
        String rootName = baseName + distanceField.filePart;
        Font known = instance.loaded.get(rootName);
        if(known == null){
            if(JSON_NAMES.contains(baseName))
                known = new Font(instance.prefix + rootName + ".dat", true).scaleHeightTo(32);
            else if(FNT_NAMES.contains(baseName))
                known = new Font(instance.prefix + rootName + ".fnt", distanceField);
            else if(distanceField == STANDARD && SAD_NAMES.contains(baseName))
                known = new Font(instance.prefix, rootName + ".font", true);
            else
                throw new RuntimeException("Unknown font name/distance field: " + baseName + "/" + distanceField.name());
            instance.loaded.put(rootName, known);
        }
        return new Font(known).setName(baseName + distanceField.namePart).setDistanceField(distanceField);
    }

    /**
     * A general way to get a copied BitmapFont from the known set of fonts, this takes a String name (which can be from
     * {@link #JSON_NAMES} or {@link #FNT_NAMES}, but is more likely from a constant such as {@link #OPEN_SANS}).
     * It looks up the appropriate file name, respecting asset prefix (see {@link #setAssetPrefix(String)}), creates a
     * new instance of that BitmapFont, and returns it. This works even for fonts that don't have known .fnt files, but
     * do have known .dat files (which is the case for most fonts here). Like {@link #getFont(String)}, this scales anu
     * returned font that was loaded from a .dat file to have a height of 32 units, so that fonts can be mixed without
     * too much effort. This doesn't scale .fnt fonts because (here) those are often pixel fonts that don't scale well.
     * For .dat fonts, it also sets the (first) Texture the BitmapFont uses to use linear min-filtering, and sets
     * {@link BitmapFont#setUseIntegerPositions(boolean)} to false, since these are generally expected for the .dat
     * fonts here.
     *
     * @param baseName typically a constant such as {@link #OPEN_SANS} or {@link #LIBERTINUS_SERIF}
     * @return a copy of the Font with the given name
     */
    public static BitmapFont getBitmapFont(final String baseName) {
        if (baseName == null)
            throw new RuntimeException("BitmapFont name cannot be null.");
        initialize();
        String rootName = baseName + STANDARD.filePart;
        BitmapFont known;
        FileHandle fh;
        if (JSON_NAMES.contains(baseName)) {
            known = BitmapFontSupport.loadStructuredJson(
                    (fh = Gdx.files.local(instance.prefix + rootName + ".dat")).exists()
                            ? fh
                            : Gdx.files.internal(instance.prefix + rootName + ".dat"), rootName + ".png");
            known.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Nearest);
            known.setUseIntegerPositions(false);
            known.getData().setScale(32f / (known.getData().lineHeight - known.getDescent()));
        }
        else if (FNT_NAMES.contains(baseName))
            known = BitmapFontSupport.loadStructuredJson(
                    (fh = Gdx.files.local(instance.prefix + rootName + ".fnt")).exists()
                            ? fh
                            : Gdx.files.internal(instance.prefix + rootName + ".fnt"), rootName + ".png");
        else
            throw new RuntimeException("Unknown BitmapFont name: " + baseName);
        known.getData().name = baseName + STANDARD.namePart;
        return known;
    }

    /**
     * Loads a font by name but does not copy it, typically so it can be modified. This takes a String name (which can
     * be from {@link #JSON_NAMES} or more likely from a constant such as {@link #OPEN_SANS}) and a DistanceFieldType
     * (which is usually {@link DistanceFieldType#STANDARD}, but could also be {@link DistanceFieldType#SDF},
     * {@link DistanceFieldType#MSDF}, or even  {@link DistanceFieldType#SDF_OUTLINE}). It looks up the appropriate file
     * name, respecting asset prefix (see {@link #setAssetPrefix(String)}), creates the Font if necessary, then returns
     * the same Font stored in {@link #loaded}. This does not set the {@link Font#name} or {@link Font#distanceField} on
     * the returned Font.
     *
     * @param baseName typically a constant such as {@link #OPEN_SANS} or {@link #LIBERTINUS_SERIF}
     * @param distanceField a DistanceFieldType, usually {@link DistanceFieldType#STANDARD}
     * @return the cached Font with the given name; this does not set the name or DistanceFieldType on the returned Font
     */
    private static Font loadFont(final String baseName, DistanceFieldType distanceField) {
        if(baseName == null)
            throw new RuntimeException("Font name cannot be null.");
        if(distanceField == null) distanceField = STANDARD;
        initialize();
        String rootName = baseName + distanceField.filePart;
        Font known = instance.loaded.get(rootName);
        if(known == null){
            if(JSON_NAMES.contains(baseName))
                known = new Font(instance.prefix + rootName + ".dat", true).scaleHeightTo(32);
            else if(FNT_NAMES.contains(baseName))
                known = new Font(instance.prefix + rootName + ".fnt", distanceField);
            else if(distanceField == STANDARD && SAD_NAMES.contains(baseName))
                known = new Font(instance.prefix, rootName + ".font", true);
            else
                throw new RuntimeException("Unknown font name/distance field: " + baseName + "/" + distanceField.name());
            instance.loaded.put(rootName, known);
        }
        return known;
    }

    /**
     * Returns a very large fixed-width Font already configured to use a square font with 45-degree angled sections,
     * based on the typeface used on the Atari ST console. This font only supports ASCII, but it supports all of it.
     * Caches the result for later calls. The font is "a-starry", based on "Atari ST (low-res)" by Damien Guard; it is
     * available under a CC-BY-SA-3.0 license, which requires attribution to Damien Guard (and technically Tommy
     * Ettinger, because he made changes in a-starry) if you use it.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.A_STARRY, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/A-Starry-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-standard.dat">A-Starry-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-standard.png">A-Starry-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-License.txt">A-Starry-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font A Starry
     */
    public static Font getAStarry() {
        return getFont(A_STARRY, STANDARD);
    }

    /**
     * Returns a very large fixed-width Font already configured to use a square font with 45-degree angled sections,
     * based on the typeface used on the Atari ST console. Uses the given distance field type. This font only supports
     * ASCII, but it supports all of it.
     * Caches the result for later calls. The font is "a-starry", based on "Atari ST (low-res)" by Damien Guard; it is
     * available under a CC-BY-SA-3.0 license, which requires attribution to Damien Guard (and technically Tommy
     * Ettinger, because he made changes in a-starry) if you use it.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/A-Starry-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-standard.dat">A-Starry-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-standard.png">A-Starry-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-License.txt">A-Starry-License.txt</a></li>
     * </ul>
     * <br>or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-sdf.dat">A-Starry-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-sdf.png">A-Starry-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-License.txt">A-Starry-License.txt</a></li>
     * </ul>
     *<br>or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-msdf.dat">A-Starry-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-msdf.png">A-Starry-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-License.txt">A-Starry-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font A Starry using the given DistanceFieldType
     */
    public static Font getAStarry(DistanceFieldType dft) {
        return getFont(A_STARRY, dft);
    }
    
    /**
     * Returns a very large fixed-width Font already configured to use a square font with 45-degree angled sections,
     * based on the typeface used on the Atari ST console.
     * This uses the MSDF distance field effect.
     * This font only supports ASCII, but it supports all of it.
     * Caches the result for later calls. The font is "a-starry", based on "Atari ST (low-res)" by Damien Guard; it is
     * available under a CC-BY-SA-3.0 license, which requires attribution to Damien Guard (and technically Tommy
     * Ettinger, because he made changes in a-starry) if you use it.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.A_STARRY, Font.DistanceFieldType.MSDF)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/A-Starry-msdf.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-msdf.dat">A-Starry-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-msdf.png">A-Starry-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-License.txt">A-Starry-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font A Starry using MSDF
     */
    public static Font getAStarryMSDF() {
        return getFont(A_STARRY, MSDF);
    }

    /**
     * Returns a very large fixed-width Font already configured to use a tall font with angled sections,
     * based on the typeface used on the Atari ST console. This font only supports ASCII, but it supports all of it.
     * Caches the result for later calls. The font is "a-starry", based on "Atari ST (low-res)" by Damien Guard; it is
     * available under a CC-BY-SA-3.0 license, which requires attribution to Damien Guard (and technically Tommy
     * Ettinger, because he made changes in a-starry) if you use it. This is an extended-height version of a-starry,
     * making it half the width relative to its height, instead of having equal width and height.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/textratypist/previews/A%20Starry%20Tall.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-standard.dat">A-Starry-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-standard.png">A-Starry-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-License.txt">A-Starry-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font A Starry, with half width
     */
    public static Font getAStarryTall() {
        return getAStarryTall(STANDARD);
    }

    /**
     * Returns a very large fixed-width Font already configured to use a tall font with angled sections,
     * based on the typeface used on the Atari ST console. Uses the given distance field type. This font only supports
     * ASCII, but it supports all of it.
     * Caches the result for later calls. The font is "a-starry", based on "Atari ST (low-res)" by Damien Guard; it is
     * available under a CC-BY-SA-3.0 license, which requires attribution to Damien Guard (and technically Tommy
     * Ettinger, because he made changes in a-starry) if you use it. This is an extended-height version of a-starry,
     * making it half the width relative to its height, instead of having equal width and height.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/textratypist/previews/A%20Starry%20Tall.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-standard.dat">A-Starry-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-standard.png">A-Starry-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-License.txt">A-Starry-License.txt</a></li>
     * </ul>
     * <br>or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-sdf.dat">A-Starry-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-sdf.png">A-Starry-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-License.txt">A-Starry-License.txt</a></li>
     * </ul>
     *<br>or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-msdf.dat">A-Starry-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-msdf.png">A-Starry-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-License.txt">A-Starry-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font A Starry, with half width, using the given DistanceFieldType
     */
    public static Font getAStarryTall(DistanceFieldType dft) {
        return getFont(A_STARRY, dft).scale(0.5f, 1f).setName(A_STARRY + "-Tall" + dft.namePart);
    }

    /**
     * Returns a Font already configured to use a variable-width "sloppy" or "grungy" display font, that should scale
     * pretty well from a height of about 160 down to a height of maybe 40. This font only covers ASCII and some (mostly
     * Western European) other languages that use the Latin script. Caches the result for later calls. The font used is
     * Birdland Aeroplane, a public domain typeface by
     * <a href="https://typodermicfonts.com/public-domain/">Ray Larabie of Typodermic Fonts</a>.
     * This font generally looks better if you use SDF or MSDF, especially at small font sizes. There's just too much
     * resizing this has to do with STANDARD mode to look good unless displayed at a rather large size. You can use SDF
     * with {@code KnownFonts.getFont(KnownFonts.BIRDLAND_AEROPLANE, Font.DistanceFieldType.SDF)}, or MSDF by changing
     * SDF to... MSDF. They should look similar in most cases.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.BIRDLAND_AEROPLANE, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Birdland-Aeroplane-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Birdland-Aeroplane-standard.dat">Birdland-Aeroplane-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Birdland-Aeroplane-standard.png">Birdland-Aeroplane-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Birdland-Aeroplane-License.txt">Birdland-Aeroplane-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Birdland Aeroplane
     */
    public static Font getBirdlandAeroplane() {
        return getFont(BIRDLAND_AEROPLANE, STANDARD);
    }

    /**
     * Returns a Font already configured to use a variable-width "sloppy" or "grungy" display font, that should scale
     * pretty well from a height of about 160 down to a height of maybe 40, or a little smaller if using SDF or MSDF.
     * Uses the given distance field type.
     * This font only covers ASCII and some (mostly Western European) other languages that use the Latin script. Caches
     * the result for later calls. The font used is Birdland Aeroplane, a public domain typeface by
     * <a href="https://typodermicfonts.com/public-domain/">Ray Larabie of Typodermic Fonts</a>.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Birdland-Aeroplane-sdf.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Birdland-Aeroplane-standard.dat">Birdland-Aeroplane-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Birdland-Aeroplane-standard.png">Birdland-Aeroplane-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Birdland-Aeroplane-License.txt">Birdland-Aeroplane-License.txt</a></li>
     * </ul>
     * <br>or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Birdland-Aeroplane-sdf.dat">Birdland-Aeroplane-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Birdland-Aeroplane-sdf.png">Birdland-Aeroplane-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Birdland-Aeroplane-License.txt">Birdland-Aeroplane-License.txt</a></li>
     * </ul>
     *<br>or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Birdland-Aeroplane-msdf.dat">Birdland-Aeroplane-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Birdland-Aeroplane-msdf.png">Birdland-Aeroplane-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Birdland-Aeroplane-License.txt">Birdland-Aeroplane-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font Birdland Aeroplane using the given DistanceFieldType
     */
    public static Font getBirdlandAeroplane(DistanceFieldType dft) {
        return getFont(BIRDLAND_AEROPLANE, dft);
    }

    /**
     * Returns a Font already configured to use a light-weight variable-width slab serif font with good Latin and
     * Cyrillic script support, that should scale pretty well from a height of about 160 down to a height of maybe 30.
     * Caches the result for later calls. The font used is Bitter, a free (OFL) typeface by <a href="https://github.com/solmatas/BitterPro">The Bitter Project</a>.
     * It supports quite a lot of Latin-based scripts and Cyrillic, but does not really cover Greek or any other
     * scripts. This font can look good at its natural size, which uses width roughly equal to height,
     * or squashed so height is slightly smaller. Bitter looks very similar to {@link #getGentium()}, except that Bitter
     * is quite a bit lighter, with thinner strokes and stylistic flourishes on some glyphs.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.BITTER, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Bitter-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Bitter-standard.dat">Bitter-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Bitter-standard.png">Bitter-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Bitter-License.txt">Bitter-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Bitter Light
     */
    public static Font getBitter() {
        return getFont(BITTER, STANDARD);
    }

    /**
     * Returns a Font already configured to use a light-weight variable-width slab serif font with good Latin and
     * Cyrillic script support. Uses the given distance field type.
     * Caches the result for later calls. The font used is Bitter, a free (OFL) typeface by <a href="https://github.com/solmatas/BitterPro">The Bitter Project</a>.
     * It supports quite a lot of Latin-based scripts and Cyrillic, but does not really cover Greek or any other
     * scripts. This font can look good at its natural size, which uses width roughly equal to height,
     * or squashed so height is slightly smaller. Bitter looks very similar to {@link #getGentium()}, except that Bitter
     * is quite a bit lighter, with thinner strokes and stylistic flourishes on some glyphs.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Bitter-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Bitter-standard.dat">Bitter-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Bitter-standard.png">Bitter-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Bitter-License.txt">Bitter-License.txt</a></li>
     * </ul>
     * <br>or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Bitter-sdf.dat">Bitter-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Bitter-sdf.png">Bitter-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Bitter-License.txt">Bitter-License.txt</a></li>
     * </ul>
     *<br>or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Bitter-msdf.dat">Bitter-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Bitter-msdf.png">Bitter-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Bitter-License.txt">Bitter-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font Bitter Light using the given DistanceFieldType
     */
    public static Font getBitter(DistanceFieldType dft) {
        return getFont(BITTER, dft);
    }

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
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.CANADA1500, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Canada1500-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Canada1500-standard.dat">Canada1500-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Canada1500-standard.png">Canada1500-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Canada1500-License.txt">Canada1500-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Canada1500
     */
    public static Font getCanada() {
        return getFont(CANADA1500, STANDARD);
    }

    /**
     * Returns a Font already configured to use a very-legible variable-width font with strong support for Canadian
     * Aboriginal Syllabic. Uses the given distance field type.
     * Caches the result for later calls. The font used is Canada1500, a free (public domain, via CC0) typeface by Ray
     * Larabie. It supports quite a lot of Latin-based scripts, Greek, Cyrillic, Canadian Aboriginal Syllabic, arrows,
     * many dingbats, and more. This font can look good at its natural size, which uses width roughly equal to height,
     * or narrowed down so width is smaller.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Canada1500-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Canada1500-standard.dat">Canada1500-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Canada1500-standard.png">Canada1500-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Canada1500-License.txt">Canada1500-License.txt</a></li>
     * </ul>
     * <br>or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Canada1500-sdf.dat">Canada1500-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Canada1500-sdf.png">Canada1500-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Canada1500-License.txt">Canada1500-License.txt</a></li>
     * </ul>
     *<br>or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Canada1500-msdf.dat">Canada1500-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Canada1500-msdf.png">Canada1500-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Canada1500-License.txt">Canada1500-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font Canada1500 using the given DistanceFieldType
     */
    public static Font getCanada(DistanceFieldType dft) {
        return getFont(CANADA1500, dft);
    }

    /**
     * Returns a Font already configured to use a quirky fixed-width font with good Unicode support
     * and a humanist style, that should scale well from a height of about 60 pixels to about 15 pixels.
     * Caches the result for later calls. The font used is Cascadia Code Mono, an open-source (SIL Open Font
     * License) typeface by Microsoft (see <a href="https://github.com/microsoft/cascadia-code">Microsoft's page</a>).
     * It supports a lot of glyphs, including most extended Latin, Greek, Braille, and Cyrillic.
     * This uses a fairly-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.CASCADIA_MONO, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Cascadia-Mono-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cascadia-Mono-standard.dat">Cascadia-Mono-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cascadia-Mono-standard.png">Cascadia-Mono-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cascadia-License.txt">Cascadia-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Cascadia Code Mono
     */
    public static Font getCascadiaMono() {
        return getFont(CASCADIA_MONO, STANDARD);
    }

    /**
     * Returns a Font already configured to use a quirky fixed-width font with good Unicode support
     * and a humanist style. Uses the given distance field type.
     * Caches the result for later calls. The font used is Cascadia Code Mono, an open-source (SIL Open Font
     * License) typeface by Microsoft (see <a href="https://github.com/microsoft/cascadia-code">Microsoft's page</a>).
     * It supports a lot of glyphs, including most extended Latin, Greek, Braille, and Cyrillic.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Cascadia-Mono-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cascadia-Mono-standard.dat">Cascadia-Mono-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cascadia-Mono-standard.png">Cascadia-Mono-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cascadia-License.txt">Cascadia-License.txt</a></li>
     * </ul>
     * <br>or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cascadia-Mono-sdf.dat">Cascadia-Mono-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cascadia-Mono-sdf.png">Cascadia-Mono-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cascadia-License.txt">Cascadia-License.txt</a></li>
     * </ul>
     * <br>or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cascadia-Mono-msdf.dat">Cascadia-Mono-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cascadia-Mono-msdf.png">Cascadia-Mono-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cascadia-License.txt">Cascadia-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font Cascadia Code Mono using the given DistanceFieldType
     */
    public static Font getCascadiaMono(DistanceFieldType dft) {
        return getFont(CASCADIA_MONO, dft);
    }

    /**
     * Returns a Font already configured to use a quirky fixed-width font with good Unicode support
     * and a humanist style.
     * This uses the MSDF distance field effect.
     * Caches the result for later calls. The font used is Cascadia Code Mono, an open-source (SIL Open Font
     * License) typeface by Microsoft (see <a href="https://github.com/microsoft/cascadia-code">Microsoft's page</a>).
     * It supports a lot of glyphs,
     * including most extended Latin, Greek, Braille, and Cyrillic.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.CASCADIA_MONO, Font.DistanceFieldType.MSDF)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Cascadia-Mono-msdf.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cascadia-Mono-msdf.dat">Cascadia-Mono-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cascadia-Mono-msdf.png">Cascadia-Mono-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cascadia-License.txt">Cascadia-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Cascadia Code Mono using MSDF
     */
    public static Font getCascadiaMonoMSDF() {
        return getFont(CASCADIA_MONO, MSDF);
    }

    /**
     * Returns a Font already configured to use a variable-width handwriting font with support for extended Latin and
     * Cyrillic, that should scale pretty well from a height of about 160 down to a height of maybe 20. It will look
     * sharper and more aliased at smaller sizes, but should be relatively smooth at a height of 32 or so. This is a
     * sort of natural handwriting, as opposed to the formal script in {@link #getTangerine()}.
     * Caches the result for later calls. The font used is Caveat, a free (OFL) typeface designed by Pablo Impallari.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.CAVEAT, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Caveat-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Caveat-standard.dat">Caveat-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Caveat-standard.png">Caveat-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Caveat-License.txt">Caveat-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Caveat
     */
    public static Font getCaveat() {
        return getFont(CAVEAT, STANDARD);
    }

    /**
     * Returns a Font already configured to use a variable-width handwriting font with support for extended Latin and
     * Cyrillic. Uses the given distance field type. This is a
     * sort of natural handwriting, as opposed to the formal script in {@link #getTangerine()}.
     * Caches the result for later calls. The font used is Caveat, a free (OFL) typeface designed by Pablo Impallari.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Caveat-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Caveat-standard.dat">Caveat-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Caveat-standard.png">Caveat-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Caveat-License.txt">Caveat-License.txt</a></li>
     * </ul>
     * <br>or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Caveat-sdf.dat">Caveat-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Caveat-sdf.png">Caveat-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Caveat-License.txt">Caveat-License.txt</a></li>
     * </ul>
     * <br>or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Caveat-msdf.dat">Caveat-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Caveat-msdf.png">Caveat-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Caveat-License.txt">Caveat-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font Caveat using the given DistanceFieldType
     */
    public static Font getCaveat(DistanceFieldType dft) {
        return getFont(CAVEAT, dft);
    }

    /**
     * Returns a Font already configured to use a fixed-width, octagonal, unicode-heavy font. Caches the result for
     * later calls. The font used is Computer Says No, a free (CC-BY-SA) typeface designed by Christian Munk.
     * It supports Latin (including a very large span of extended Latin), Greek, and Cyrillic, Cherokee, Armenian, IPA
     * symbols (and probably more phonetic symbols), Runic, etc.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very
     * well. This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.COMPUTER_SAYS_NO, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Computer-Says-No-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Computer-Says-No-standard.dat">Computer-Says-No-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Computer-Says-No-standard.png">Computer-Says-No-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Computer-Says-No-License.txt">Computer-Says-No-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Computer Says No
     */
    public static Font getComputerSaysNo() {
        return getFont(COMPUTER_SAYS_NO, STANDARD);
    }

    /**
     * Returns a Font already configured to use a fixed-width, octagonal, unicode-heavy font. Caches the result for
     * later calls. Uses the given distance field type.
     * The font used is Computer Says No, a free (CC-BY-SA) typeface designed by Christian Munk.
     * It supports Latin (including a very large span of extended Latin), Greek, and Cyrillic, Cherokee, Armenian, IPA
     * symbols (and probably more phonetic symbols), Runic, etc.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Computer-Says-No-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Computer-Says-No-standard.dat">Computer-Says-No-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Computer-Says-No-standard.png">Computer-Says-No-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Computer-Says-No-License.txt">Computer-Says-No-License.txt</a></li>
     * </ul>
     * <br>or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Computer-Says-No-sdf.dat">Computer-Says-No-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Computer-Says-No-sdf.png">Computer-Says-No-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Computer-Says-No-License.txt">Computer-Says-No-License.txt</a></li>
     * </ul>
     * <br>or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Computer-Says-No-msdf.dat">Computer-Says-No-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Computer-Says-No-msdf.png">Computer-Says-No-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Computer-Says-No-License.txt">Computer-Says-No-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font Computer Says No using the given DistanceFieldType
     */
    public static Font getComputerSaysNo(DistanceFieldType dft) {
        return getFont(COMPUTER_SAYS_NO, dft);
    }
    
    /**
     * Returns a Font configured to use a cozy fixed-width bitmap font,
     * <a href="https://github.com/slavfox/Cozette">Cozette by slavfox</a>. Cozette has broad coverage of Unicode,
     * including Greek, Cyrillic, Braille, and tech-related icons. This does not scale well except to integer
     * multiples, but it should look very crisp at its default size of 6x17 pixels. This defaults to having
     * {@link Font#integerPosition} set to true, which currently does nothing (the code that enforces integer positions
     * seems to ruin the appearance of any font that uses it, so that code isn't ever used now).
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://tommyettinger.github.io/textratypist/previews/Cozette.png">Image link</a> (uses width=6, height=17; this size is small
     * enough to make the scaled text unreadable in some places)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cozette-standard.fnt">Cozette-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cozette-standard.png">Cozette-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cozette-License.txt">Cozette-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that represents the 6x17px font Cozette
     */
    public static Font getCozette() {
        initialize();
        final String baseName = COZETTE;
        final DistanceFieldType distanceField = STANDARD;
        String rootName = baseName + distanceField.filePart;
        Font found = instance.loaded.get(rootName);
        if(found == null){
            found = new Font(instance.prefix + rootName + ".fnt", distanceField, 1, 5, 0, 0, false);
            found
                    .useIntegerPositions(true)
                    .setDescent(-3f)
                    .setUnderlinePosition(0f, -0.125f)
                    .setStrikethroughPosition(0f, 0f)
                    .setInlineImageMetrics(-32f, 4f, 8f)
                    .setName(baseName + STANDARD.namePart);
            ;
            instance.loaded.put(rootName, found);
        }
        return new Font(found);
    }

    /**
     * Returns a Font already configured to use a variable-width sans-serif font with excellent Unicode support.
     * Caches the result for later calls. The font used is
     * DejaVu Sans, an open-source typeface included in many Linux distros. It supports a lot of glyphs,
     * including quite a bit of extended Latin, Greek, and Cyrillic, as well as some less-common glyphs from various
     * real languages. This does not use a distance field effect, as opposed to {@link #getDejaVuSansMono()}.
     * You may want to stick using just fonts that avoid distance fields if you have a family of fonts.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.DEJAVU_SANS, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/DejaVu-Sans-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Sans-standard.dat">DejaVu-Sans-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Sans-standard.png">DejaVu-Sans-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-License.txt">DejaVu-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font DejaVuSans
     */
    public static Font getDejaVuSans() {
        return getFont(DEJAVU_SANS, STANDARD);
    }

    /**
     * Returns a Font already configured to use a variable-width sans-serif font with excellent Unicode support.
     * Uses the given distance field type. Caches the result for later calls. The font used is
     * DejaVu Sans, an open-source typeface included in many Linux distros. It supports a lot of glyphs,
     * including quite a bit of extended Latin, Greek, and Cyrillic, as well as some less-common glyphs from various
     * real languages.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/DejaVu-Sans-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Sans-standard.dat">DejaVu-Sans-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Sans-standard.png">DejaVu-Sans-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-License.txt">DejaVu-License.txt</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Sans-msdf.dat">DejaVu-Sans-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Sans-msdf.png">DejaVu-Sans-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-License.txt">DejaVu-License.txt</a></li>
     * </ul>
     * or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Sans-sdf.dat">DejaVu-Sans-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Sans-sdf.png">DejaVu-Sans-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-License.txt">DejaVu-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font DejaVu Sans using the given DistanceFieldType
     */
    public static Font getDejaVuSans(DistanceFieldType dft) {
        return getFont(DEJAVU_SANS, dft);
    }

    /**
     * Returns a Font already configured to use a variable-width narrow sans-serif font with excellent Unicode support.
     * Caches the result for later calls. The font used is
     * DejaVu Sans Condensed, an open-source typeface included in many Linux distros. It supports a lot of glyphs,
     * including quite a bit of extended Latin, Greek, and Cyrillic, as well as some less-common glyphs from various
     * real languages. This does not use a distance field effect, as opposed to {@link #getDejaVuSansMono()}.
     * You may want to stick using just fonts that avoid distance fields if you have a family of fonts.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.DEJAVU_SANS_CONDENSED, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/DejaVu-Sans-Condensed-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Sans-Condensed-standard.dat">DejaVu-Sans-Condensed-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Sans-Condensed-standard.png">DejaVu-Sans-Condensed-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-License.txt">DejaVu-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font DejaVu Sans Condensed
     */
    public static Font getDejaVuSansCondensed() {
        return getFont(DEJAVU_SANS_CONDENSED, STANDARD);
    }

    /**
     * Returns a Font already configured to use a variable-width narrow sans-serif font with excellent Unicode support.
     * Uses the given distance field type. Caches the result for later calls. The font used is
     * DejaVu Sans Condensed, an open-source typeface included in many Linux distros. It supports a lot of glyphs,
     * including quite a bit of extended Latin, Greek, and Cyrillic, as well as some less-common glyphs from various
     * real languages.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/DejaVu-Sans-Condensed-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Sans-Condensed-standard.dat">DejaVu-Sans-Condensed-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Sans-Condensed-standard.png">DejaVu-Sans-Condensed-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-License.txt">DejaVu-License.txt</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Sans-Condensed-msdf.dat">DejaVu-Sans-Condensed-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Sans-Condensed-msdf.png">DejaVu-Sans-Condensed-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-License.txt">DejaVu-License.txt</a></li>
     * </ul>
     * or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Sans-Condensed-sdf.dat">DejaVu-Sans-Condensed-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Sans-Condensed-sdf.png">DejaVu-Sans-Condensed-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-License.txt">DejaVu-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font DejaVu Sans Condensed using the given DistanceFieldType
     */
    public static Font getDejaVuSansCondensed(DistanceFieldType dft) {
        return getFont(DEJAVU_SANS_CONDENSED, dft);
    }

    /**
     * A nice old standby font with very broad language support, DejaVu Sans Mono is fixed-width and can be clearly
     * readable but doesn't do anything unusual stylistically. It really does handle a lot of glyphs; not only does this
     * have practically all Latin glyphs in Unicode (enough to support everything from Icelandic to Vietnamese), it has
     * Greek (including Extended), Cyrillic (including some optional glyphs), IPA, Armenian (maybe the only font here to
     * do so), Georgian (which won't be treated correctly by some case-insensitive code, so it should only be used if
     * case doesn't matter), and Lao. It has full box drawing and Braille support, handles a wide variety of math
     * symbols, technical marks, and dingbats, etc.
     * This is an open-source typeface included in many Linux distros.
     * This uses the Multi-channel Signed Distance
     * Field (MSDF) technique as opposed to the normal Signed Distance Field technique, which gives the rendered font
     * sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * Note that the name here doesn't include "MSDF" for historical reasons. This omission is part of why using
     * {@link #getFont(String, DistanceFieldType)} is preferred.
     * <br>
     * The crispness is likely too high in this version. You can call
     * {@code KnownFonts.getDejaVuSansMono().setCrispness(0.5f)} if you want significantly smoother edges.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.DEJAVU_SANS_MONO, Font.DistanceFieldType.MSDF)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/DejaVu-Sans-Mono-msdf.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Sans-Mono-msdf.dat">DejaVu-Sans-Mono-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Sans-Mono-msdf.png">DejaVu-Sans-Mono-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-License.txt">DejaVu-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font DejaVu Sans Mono using MSDF
     */
    public static Font getDejaVuSansMono() {
        return getFont(DEJAVU_SANS_MONO, MSDF);
    }

    /**
     * A nice old standby font with very broad language support, DejaVu Sans Mono is fixed-width and can be clearly
     * readable but doesn't do anything unusual stylistically. Uses the given distance field type. It really does handle
     * a lot of glyphs; not only does this
     * have practically all Latin glyphs in Unicode (enough to support everything from Icelandic to Vietnamese), it has
     * Greek (including Extended), Cyrillic (including some optional glyphs), IPA, Armenian (maybe the only font here to
     * do so), Georgian (which won't be treated correctly by some case-insensitive code, so it should only be used if
     * case doesn't matter), and Lao. It has full box drawing and Braille support, handles a wide variety of math
     * symbols, technical marks, and dingbats, etc.
     * This is an open-source typeface included in many Linux distros.
     * <br>
     * The crispness for the MSDF version is likely too high in this version. You can call
     * {@code KnownFonts.getDejaVuSansMono(Font.DistanceFieldType.MSDF).setCrispness(0.5f)} if you want significantly
     * smoother edges.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/DejaVu-Sans-Mono-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Sans-Mono-standard.dat">DejaVu-Sans-Mono-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Sans-Mono-standard.png">DejaVu-Sans-Mono-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-License.txt">DejaVu-License.txt</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Sans-Mono-sdf.dat">DejaVu-Sans-Mono-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Sans-Mono-sdf.png">DejaVu-Sans-Mono-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-License.txt">DejaVu-License.txt</a></li>
     * </ul>
     * or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Sans-Mono-msdf.dat">DejaVu-Sans-Mono-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Sans-Mono-msdf.png">DejaVu-Sans-Mono-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-License.txt">DejaVu-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font DejaVu Sans Mono using the given DistanceFieldType
     */
    public static Font getDejaVuSansMono(DistanceFieldType dft) {
        return getFont(DEJAVU_SANS_MONO, dft);
    }

    /**
     * Returns a Font already configured to use a variable-width serif font with excellent Unicode support.
     * Caches the result for later calls. The font used is
     * DejaVu Serif, an open-source typeface included in many Linux distros. It supports a lot of glyphs,
     * including quite a bit of extended Latin, Greek, and Cyrillic, as well as some less-common glyphs from various
     * real languages. This does not use a distance field effect, as opposed to {@link #getDejaVuSansMono()}.
     * You may want to stick using just fonts that avoid distance fields if you have a family of fonts.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.DEJAVU_SERIF, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/DejaVu-Serif-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Serif-standard.dat">DejaVu-Serif-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Serif-standard.png">DejaVu-Serif-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-License.txt">DejaVu-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font DejaVu Serif
     */
    public static Font getDejaVuSerif() {
        return getFont(DEJAVU_SERIF, STANDARD);
    }

    /**
     * Returns a Font already configured to use a variable-width serif font with excellent Unicode support.
     * Uses the given distance field type. Caches the result for later calls. The font used is
     * DejaVu Serif, an open-source typeface included in many Linux distros. It supports a lot of glyphs,
     * including quite a bit of extended Latin, Greek, and Cyrillic, as well as some less-common glyphs from various
     * real languages.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/DejaVu-Serif-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Serif-standard.dat">DejaVu-Serif-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Serif-standard.png">DejaVu-Serif-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-License.txt">DejaVu-License.txt</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Serif-msdf.dat">DejaVu-Serif-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Serif-msdf.png">DejaVu-Serif-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-License.txt">DejaVu-License.txt</a></li>
     * </ul>
     * or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Serif-sdf.dat">DejaVu-Serif-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Serif-sdf.png">DejaVu-Serif-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-License.txt">DejaVu-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font DejaVu Serif using the given DistanceFieldType
     */
    public static Font getDejaVuSerif(DistanceFieldType dft) {
        return getFont(DEJAVU_SERIF, dft);
    }

    /**
     * Returns a Font already configured to use a variable-width narrow serif font with excellent Unicode support.
     * Caches the result for later calls. The font used is
     * DejaVu Serif Condensed, an open-source typeface included in many Linux distros. It supports a lot of glyphs,
     * including quite a bit of extended Latin, Greek, and Cyrillic, as well as some less-common glyphs from various
     * real languages. This does not use a distance field effect, as opposed to {@link #getDejaVuSansMono()}.
     * You may want to stick using just fonts that avoid distance fields if you have a family of fonts.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.DEJAVU_SERIF_CONDENSED, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/DejaVu-Serif-Condensed-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Serif-Condensed-standard.dat">DejaVu-Serif-Condensed-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Serif-Condensed-standard.png">DejaVu-Serif-Condensed-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-License.txt">DejaVu-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font DejaVu Serif Condensed
     */
    public static Font getDejaVuSerifCondensed() {
        return getFont(DEJAVU_SERIF_CONDENSED, STANDARD);
    }

    /**
     * Returns a Font already configured to use a variable-width narrow serif font with excellent Unicode support.
     * Uses the given distance field type. Caches the result for later calls. The font used is
     * DejaVu Serif Condensed, an open-source typeface included in many Linux distros. It supports a lot of glyphs,
     * including quite a bit of extended Latin, Greek, and Cyrillic, as well as some less-common glyphs from various
     * real languages.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/DejaVu-Serif-Condensed-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Serif-Condensed-standard.dat">DejaVu-Serif-Condensed-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Serif-Condensed-standard.png">DejaVu-Serif-Condensed-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-License.txt">DejaVu-License.txt</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Serif-Condensed-msdf.dat">DejaVu-Serif-Condensed-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Serif-Condensed-msdf.png">DejaVu-Serif-Condensed-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-License.txt">DejaVu-License.txt</a></li>
     * </ul>
     * or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Serif-Condensed-sdf.dat">DejaVu-Serif-Condensed-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-Serif-Condensed-sdf.png">DejaVu-Serif-Condensed-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVu-License.txt">DejaVu-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font DejaVu Serif Condensed using the given DistanceFieldType
     */
    public static Font getDejaVuSerifCondensed(DistanceFieldType dft) {
        return getFont(DEJAVU_SERIF_CONDENSED, dft);
    }

    /**
     * Returns a Font already configured to use a variable-width serif font with excellent Unicode support, that should
     * scale well from a height of about 132 down to a height of 24. Caches the result for later calls. The font used is
     * Gentium, an open-source (SIL Open Font License) typeface by SIL (see
     * <a href="https://software.sil.org/gentium/">SIL's page on Gentium here</a>). It supports a lot of glyphs,
     * including quite a bit of extended Latin, Greek, and Cyrillic, as well as some less-common glyphs from various
     * real languages. This does not use a distance field effect, as opposed to {@link #getGentiumSDF()} or
     * {@link #getGentiumMSDF()}. You may want to stick using just fonts that avoid distance fields if you have a family
     * of fonts.
     * You can also use {@link #getStandardFamily()} to obtain a variant on this Font that has a FontFamily already.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.GENTIUM, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Gentium-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-standard.dat">Gentium-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-standard.png">Gentium-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-License.txt">Gentium-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Gentium
     */
    public static Font getGentium() {
        return getFont(GENTIUM, STANDARD);
    }

    /**
     * Returns a Font already configured to use a variable-width serif font with excellent Unicode support, that should
     * scale cleanly to even very large sizes (using an MSDF technique). Caches the result for later calls.
     * The font used is Gentium, an open-source (SIL Open Font
     * License) typeface by SIL (see <a href="https://software.sil.org/gentium/">SIL's page on Gentium here</a>). It
     * supports a lot of glyphs, including quite a
     * bit of extended Latin, Greek, and Cyrillic, as well as some less-common glyphs from various real languages. This
     * uses the Multi-channel Signed Distance Field (MSDF) technique as opposed to the normal Signed Distance Field
     * technique, which gives the rendered font sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.GENTIUM, Font.DistanceFieldType.MSDF)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Gentium-msdf.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-msdf.dat">Gentium-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-msdf.png">Gentium-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-License.txt">Gentium-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Gentium using MSDF
     */
    public static Font getGentiumMSDF() {
        return getFont(GENTIUM, MSDF);
    }

    /**
     * Returns a Font already configured to use a variable-width serif font with excellent Unicode support, that should
     * scale cleanly to even very large sizes (using an SDF technique).
     * Caches the result for later calls. The font used is Gentium, an open-source (SIL Open Font
     * License) typeface by SIL (see <a href="https://software.sil.org/gentium/">SIL's page on Gentium here</a>). It
     * supports a lot of glyphs, including quite a
     * bit of extended Latin, Greek, and Cyrillic, as well as some less-common glyphs from various real languages. This
     * uses the Signed Distance Field (SDF) technique, which may be slightly fuzzy when zoomed in heavily, but should be
     * crisp enough when zoomed out.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.GENTIUM, Font.DistanceFieldType.SDF)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Gentium-sdf.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-sdf.dat">Gentium-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-sdf.png">Gentium-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-License.txt">Gentium-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Gentium using SDF
     */
    public static Font getGentiumSDF() {
        return getFont(GENTIUM, SDF);
    }

    /**
     * Returns a Font already configured to use a variable-width serif font with excellent Unicode support.
     * Uses the given distance field type.
     * Caches the result for later calls. The font used is Gentium, an open-source (OFL) typeface by SIL (see
     * <a href="https://software.sil.org/gentium/">SIL's page on Gentium here</a>). It supports a lot of glyphs,
     * including quite a bit of extended Latin, Greek, and Cyrillic, as well as some less-common glyphs from various
     * real languages.
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-standard.dat">Gentium-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-standard.png">Gentium-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-License.txt">Gentium-License.txt</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-sdf.dat">Gentium-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-sdf.png">Gentium-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-License.txt">Gentium-License.txt</a></li>
     * </ul>
     * or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-msdf.dat">Gentium-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-msdf.png">Gentium-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-License.txt">Gentium-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font Gentium using the given DistanceFieldType
     */
    public static Font getGentium(DistanceFieldType dft) {
        return getFont(GENTIUM, dft);
    }

    /**
     * Returns a Font already configured to use a variable-width "italic-like" serif font with excellent Unicode
     * support, that should scale well from a height of about 97 down to a height of 30.
     * Caches the result for later calls. The font used is Gentium, an open-source (OFL) typeface by
     * SIL (see <a href="https://software.sil.org/gentium/">SIL's page on Gentium here</a>), but this took Gentium
     * Italic and removed the 8-degree slant it had, so it looks like a regular face but with the different serif style
     * and the "flow" of an italic font. This helps it look closer to carefully-hand-written text mixed with a serif
     * typeface, and may fit well as a main-text font for medieval or Renaissance-period games while something like
     * {@link #getKingthingsFoundation()} is used for titles or headers. It supports a lot of glyphs, including quite a
     * bit of extended Latin, Greek, and Cyrillic, as well as some less-common glyphs from various real languages. Even
     * though glyphs are not especially wide here, this Font does need to be configured with a much larger width than
     * height to be readable. This does not use a distance field effect. You may want to stick using just fonts that
     * avoid distance fields if you have a family of fonts.
     * <br>
     * Thanks to Siavash Ranbar, who came up with the idea to take an italic version of a serif font and remove its
     * slant, keeping the different flow from a simple oblique font.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.GENTIUM_UN_ITALIC, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Gentium-Un-Italic-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-Un-Italic-standard.dat">Gentium-Un-Italic-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-Un-Italic-standard.png">Gentium-Un-Italic-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-License.txt">Gentium-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Gentium Un-Italic
     */
    public static Font getGentiumUnItalic() {
        return getFont(GENTIUM_UN_ITALIC, STANDARD);
    }

    /**
     * Returns a Font already configured to use a variable-width "italic-like" serif font with excellent Unicode
     * support, that should scale well from a height of about 97 down to a height of 30. Uses the given distance field
     * type. Caches the result for later calls. The font used is Gentium, an open-source (OFL) typeface by
     * SIL (see <a href="https://software.sil.org/gentium/">SIL's page on Gentium here</a>), but this took Gentium
     * Italic and removed the 8-degree slant it had, so it looks like a regular face but with the different serif style
     * and the "flow" of an italic font. This helps it look closer to carefully-hand-written text mixed with a serif
     * typeface, and may fit well as a main-text font for medieval or Renaissance-period games while something like
     * {@link #getKingthingsFoundation()} is used for titles or headers. It supports a lot of glyphs, including quite a
     * bit of extended Latin, Greek, and Cyrillic, as well as some less-common glyphs from various real languages. Even
     * though glyphs are not especially wide here, this Font does need to be configured with a much larger width than
     * height to be readable. This does not use a distance field effect. You may want to stick using just fonts that
     * avoid distance fields if you have a family of fonts.
     * <br>
     * Thanks to Siavash Ranbar, who came up with the idea to take an italic version of a serif font and remove its
     * slant, keeping the different flow from a simple oblique font.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Gentium-Un-Italic-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-Un-Italic-standard.dat">Gentium-Un-Italic-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-Un-Italic-standard.png">Gentium-Un-Italic-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-License.txt">Gentium-License.txt</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-Un-Italic-sdf.dat">Gentium-Un-Italic-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-Un-Italic-sdf.png">Gentium-Un-Italic-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-License.txt">Gentium-License.txt</a></li>
     * </ul>
     * or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-Un-Italic-msdf.dat">Gentium-Un-Italic-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-Un-Italic-msdf.png">Gentium-Un-Italic-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-License.txt">Gentium-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font Gentium Un-Italic using the given DistanceFieldType
     */
    public static Font getGentiumUnItalic(DistanceFieldType dft) {
        return getFont(GENTIUM_UN_ITALIC, dft);
    }

    /**
     * Returns a Font already configured to use a variable-width geometric sans-serif font. This looks similar, but
     * not identical, to {@link #getNowAlt()}. In particular, this font has a much lighter weight.
     * Caches the result for later calls. The font used is Glacial Indifference, an open-source (SIL Open Font License)
     * typeface. This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very
     * well. This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.GLACIAL_INDIFFERENCE, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Glacial-Indifference-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Glacial-Indifference-standard.dat">Glacial-Indifference-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Glacial-Indifference-standard.png">Glacial-Indifference-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Glacial-Indifference-License.txt">Glacial-Indifference-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Glacial Indifference
     */
    public static Font getGlacialIndifference() {
        return getFont(GLACIAL_INDIFFERENCE, STANDARD);
    }

    /**
     * Returns a Font already configured to use a variable-width geometric sans-serif font.
     * Uses the given distance field type. This looks similar, but
     * not identical, to {@link #getNowAlt()}. In particular, this font has a much lighter weight.
     * Caches the result for later calls. The font used is Glacial Indifference, an open-source (SIL Open Font License)
     * typeface.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Glacial-Indifference-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Glacial-Indifference-standard.dat">Glacial-Indifference-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Glacial-Indifference-standard.png">Glacial-Indifference-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Glacial-Indifference-License.txt">Glacial-Indifference-License.txt</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Glacial-Indifference-msdf.dat">Glacial-Indifference-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Glacial-Indifference-msdf.png">Glacial-Indifference-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Glacial-Indifference-License.txt">Glacial-Indifference-License.txt</a></li>
     * </ul>
     * or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Glacial-Indifference-sdf.dat">Glacial-Indifference-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Glacial-Indifference-sdf.png">Glacial-Indifference-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Glacial-Indifference-License.txt">Glacial-Indifference-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font Glacial Indifference using the given DistanceFieldType
     */
    public static Font getGlacialIndifference(DistanceFieldType dft) {
        return getFont(GLACIAL_INDIFFERENCE, dft);
    }

    /**
     * Returns a Font already configured to use a variable-width sans-serif font with extreme pan-Unicode support, that
     * should scale cleanly to medium-small sizes (but not large sizes). Caches the result for later calls. The
     * font used is Go Noto Universal, an open-source (SIL Open Font License) typeface that modifies Noto Sans by Google
     * (see <a href="https://github.com/satbyy/go-noto-universal">Go Noto Universal's page is here</a>, and
     * <a href="https://notofonts.github.io/">Noto Fonts have a page here</a>). It supports... most glyphs, from many
     * languages, including essentially all extended Latin, Greek, Cyrillic, Chinese, Japanese, Armenian, Ethiopic,
     * Cherokee, Javanese... Most scripts are here, though not Hangul (used for Korean). This also has symbols for math,
     * music, and other usage. The texture this uses is larger than many of the others here, at 4096x4096 pixels, but
     * the file isn't too large; in fact, the 2048x2048 textures Gentium-msdf.png and Twemoji.png are each larger than
     * Go-Noto-Universal-standard.png . The .dat has 21274 glyphs plus extensive kerning info, though, so it is large.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.GO_NOTO_UNIVERSAL, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Go-Noto-Universal-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Go-Noto-Universal-standard.dat">Go-Noto-Universal-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Go-Noto-Universal-standard.png">Go-Noto-Universal-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Go-Noto-Universal-License.txt">Go-Noto-Universal-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Go Noto Current
     */
    public static Font getGoNotoUniversal() {
        return getFont(GO_NOTO_UNIVERSAL, STANDARD);
    }

    /**
     * Returns a Font already configured to use a variable-width sans-serif font with extreme pan-Unicode support, that
     * should scale cleanly to even very large sizes (using an SDF technique). Caches the result for later calls. The
     * font used is Go Noto Universal, an open-source (SIL Open Font License) typeface that modifies Noto Sans by Google
     * (see <a href="https://github.com/satbyy/go-noto-universal">Go Noto Universal's page is here</a>, and
     * <a href="https://notofonts.github.io/">Noto Fonts have a page here</a>). It supports... most glyphs, from many
     * languages, including essentially all extended Latin, Greek, Cyrillic, Chinese, Japanese, Armenian, Ethiopic,
     * Cherokee, Javanese... Most scripts are here, though not Hangul (used for Korean). This also has symbols for math,
     * music, and other usage. The baseline may be slightly uneven at larger sizes, but should even out when height is
     * less than 40 or so. This uses the Signed Distance Field (SDF) technique, which may be slightly fuzzy when zoomed
     * in heavily, but should be crisp enough when zoomed out. The texture this uses is larger than many of the others
     * here, at 4096x4096 pixels, but the file isn't too large; in fact, the 2048x2048 textures Gentium-msdf.png and
     * Twemoji.png are each larger than GoNotoUniversal-sdf.png . The .fnt has 24350 glyphs plus extensive kerning info,
     * though, so it is quite large.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.GO_NOTO_UNIVERSAL, Font.DistanceFieldType.SDF)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Go-Noto-Universal-sdf.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Go-Noto-Universal-sdf.dat">Go-Noto-Universal-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Go-Noto-Universal-sdf.png">Go-Noto-Universal-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Go-Noto-Universal-License.txt">Go-Noto-Universal-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Go Noto Current using SDF
     */
    public static Font getGoNotoUniversalSDF() {
        return getFont(GO_NOTO_UNIVERSAL, SDF);
    }

    /**
     * Returns a Font already configured to use a variable-width sans-serif font with extreme pan-Unicode support.
     * Uses the given distance field type. The font used is Go Noto Universal, an open-source (SIL Open Font License)
     * typeface that modifies Noto Sans by Google
     * (see <a href="https://github.com/satbyy/go-noto-universal">Go Noto Universal's page is here</a>, and
     * <a href="https://notofonts.github.io/">Noto Fonts have a page here</a>). It supports... most glyphs, from many
     * languages, including essentially all extended Latin, Greek, Cyrillic, Chinese, Japanese, Armenian, Ethiopic,
     * Cherokee, Javanese... Most scripts are here, though not Hangul (used for Korean). This also has symbols for math,
     * music, and other usage. The texture this uses is larger than many of the others here, at 4096x4096 pixels, but
     * the file isn't too large; in fact, the 2048x2048 textures Gentium-msdf.png and Twemoji.png are each larger than
     * Go-Noto-Universal-standard.png . The .dat has 21274 glyphs plus extensive kerning info, though, so it is large.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Go-Noto-Universal-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Go-Noto-Universal-standard.dat">Go-Noto-Universal-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Go-Noto-Universal-standard.png">Go-Noto-Universal-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Go-Noto-Universal-License.txt">Go-Noto-Universal-License.txt</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Go-Noto-Universal-sdf.dat">Go-Noto-Universal-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Go-Noto-Universal-sdf.png">Go-Noto-Universal-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Go-Noto-Universal-License.txt">Go-Noto-Universal-License.txt</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Go-Noto-Universal-msdf.dat">Go-Noto-Universal-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Go-Noto-Universal-msdf.png">Go-Noto-Universal-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Go-Noto-Universal-License.txt">Go-Noto-Universal-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font Go Noto Current using the given DistanceFieldType
     */
    public static Font getGoNotoUniversal(DistanceFieldType dft) {
        return getFont(GO_NOTO_UNIVERSAL, dft);
    }

    /**
     * Returns a Font already configured to use a variable-width, heavy-weight, legible gothic font.
     * Caches the result for later calls. The font used is Grenze, a free (OFL) typeface designed by the Manuale
     * Project. This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very
     * well. This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.GRENZE, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Grenze-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Grenze-standard.dat">Grenze-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Grenze-standard.png">Grenze-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Grenze-License.txt">Grenze-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Grenze
     */
    public static Font getGrenze() {
        return getFont(GRENZE, STANDARD);
    }

    /**
     * Returns a Font already configured to use a variable-width, heavy-weight, legible gothic font.
     * Uses the given distance field type. Caches the result for later calls. The font used is Grenze, a free
     * (OFL) typeface designed by the Manuale Project.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Grenze-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Grenze-standard.dat">Grenze-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Grenze-standard.png">Grenze-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Grenze-License.txt">Grenze-License.txt</a></li>
     * </ul>
     * <br>or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Grenze-sdf.dat">Grenze-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Grenze-sdf.png">Grenze-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Grenze-License.txt">Grenze-License.txt</a></li>
     * </ul>
     * <br>or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Grenze-msdf.dat">Grenze-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Grenze-msdf.png">Grenze-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Grenze-License.txt">Grenze-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font Grenze using the given DistanceFieldType
     */
    public static Font getGrenze(DistanceFieldType dft) {
        return getFont(GRENZE, dft);
    }

    /**
     * Returns a Font already configured to use a variable-width, narrow font with nearly-complete CJK character
     * coverage, plus Latin, Greek, and Cyrillic, that should scale pretty well down, but not up.
     * Caches the result for later calls. The font used is Hanazono (HanMinA, specifically), a free (OFL) typeface.
     * This uses a somewhat-small standard bitmap font because of how many glyphs are present (over 34000); it might not
     * scale as well as other standard bitmap fonts here. You may want to consider {@link #getGoNotoUniversalSDF()} if
     * you can use an SDF font, since it scales up with higher quality.
     * Otherwise, this may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://tommyettinger.github.io/textratypist/previews/Hanazono.png">Image link</a> (uses width=16, height=20)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Hanazono-standard.fnt">Hanazono-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Hanazono-standard.png">Hanazono-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Hanazono-License.txt">Hanazono-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font HanMinA
     */
    public static Font getHanazono() {
        initialize();
        final String baseName = HANAZONO;
        final DistanceFieldType distanceField = STANDARD;
        String rootName = baseName + distanceField.filePart;
        Font found = instance.loaded.get(rootName);
        if(found == null){
            found = new Font(instance.prefix + rootName + ".fnt", distanceField, 1, 5, 0, 0, false);
            found
                    .setDescent(-6f).scaleTo(16, 20).setFancyLinePosition(-0.5f, 0.125f)
                    .setLineMetrics(-0.25f, 0f, 0f, -0.5f).setInlineImageMetrics(-16f, -4f, 0f)
                    .setTextureFilter()
                    .setName(baseName + distanceField.namePart);
            ;
            instance.loaded.put(rootName, found);
        }
        return new Font(found);
    }

    /**
     * Returns a Font configured to use a classic, nostalgic fixed-width bitmap font,
     * IBM 8x16 from the early, oft-beloved computer line. This font is notably loaded
     * from a SadConsole format file, which shouldn't affect how it looks (but in reality,
     * it might). This does not scale except to integer multiples, but it should look very
     * crisp at its default size of 8x16 pixels. This supports some extra characters, but
     * not at the typical Unicode codepoints. This defaults to having
     * {@link Font#integerPosition} set to true, which currently does nothing (the code that enforces integer positions
     * seems to ruin the appearance of any font that uses it, so that code isn't ever used now).
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * This does not include a license because the source, <a href="https://github.com/Thraka/SadConsole/tree/master/Fonts">SadConsole's fonts</a>,
     * did not include one. It is doubtful that IBM would have any issues with respectful use
     * of their signature font throughout the 1980s, but if the legality is concerning, you
     * can use {@link #getCozette()} or {@link #getQuanPixel()} for a different bitmap font. There
     * is also {@link #getAStarry()} for a non-pixel font styled after a font from the same era.
     * <br>
     * Preview: <a href="https://tommyettinger.github.io/textratypist/previews/IBM%208x16.png">Image link</a> (uses width=8, height=16, done with
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
        final String baseName = IBM_8X16;
        final DistanceFieldType distanceField = STANDARD;
        String rootName = baseName + distanceField.filePart;
        Font found = instance.loaded.get(rootName);
        if(found == null){
            found = new Font(instance.prefix, rootName + ".font", true);
            found
                    .setBoldStrength(0.5f).setLineMetrics(-0.25f, 0f, 0f, 0f)
                    .setInlineImageMetrics(-40, 0, 0).fitCell(8, 16, false).setDescent(-6f)
                    .setName(baseName + distanceField.namePart);
            ;
            instance.loaded.put(rootName, found);
        }
        return new Font(found);
    }

    /**
     * A customized version of Inconsolata LGC, a fixed-width geometric font that supports a large range of Latin,
     * Greek, and Cyrillic glyphs, plus box drawing and some dingbat characters (like zodiac signs). The original font
     * Inconsolata is by Raph Levien, and various other contributors added support for other languages. This does not
     * use a distance field effect, as opposed to {@link #getInconsolataMSDF()}.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.INCONSOLATA_LGC, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Inconsolata-LGC-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-standard.dat">Inconsolata-LGC-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-standard.png">Inconsolata-LGC-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-License.txt">Inconsolata-LGC-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Inconsolata LGC Custom
     */
    public static Font getInconsolata() {
        return getFont(INCONSOLATA_LGC, STANDARD);
    }

    /**
     * A customized version of Inconsolata LGC, a fixed-width geometric font that supports a large range of Latin,
     * Greek, and Cyrillic glyphs, plus box drawing and some dingbat characters (like zodiac signs). The original font
     * Inconsolata is by Raph Levien, and various other contributors added support for other languages. This uses the
     * Multi-channel Signed Distance Field (MSDF) technique as opposed to the normal Signed Distance Field technique,
     * which gives the rendered font sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.INCONSOLATA_LGC, Font.DistanceFieldType.MSDF)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Inconsolata-LGC-msdf.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-msdf.dat">Inconsolata-LGC-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-msdf.png">Inconsolata-LGC-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-License.txt">Inconsolata-LGC-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Inconsolata LGC Custom using MSDF
     */
    public static Font getInconsolataMSDF() {
        return getFont(INCONSOLATA_LGC, MSDF);
    }

    /**
     * A customized version of Inconsolata LGC, a fixed-width geometric font that supports a large range of Latin,
     * Greek, and Cyrillic glyphs, plus box drawing and some dingbat characters (like zodiac signs).
     * Uses the given distance field type. The original font
     * Inconsolata is by Raph Levien, and various other contributors added support for other languages.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Inconsolata-LGC-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-standard.dat">Inconsolata-LGC-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-standard.png">Inconsolata-LGC-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-License.txt">Inconsolata-LGC-License.txt</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-sdf.dat">Inconsolata-LGC-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-sdf.png">Inconsolata-LGC-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-License.txt">Inconsolata-LGC-License.txt</a></li>
     * </ul>
     * or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-msdf.dat">Inconsolata-LGC-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-msdf.png">Inconsolata-LGC-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-License.txt">Inconsolata-LGC-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font Inconsolata LGC Custom using the given DistanceFieldType
     */
    public static Font getInconsolata(DistanceFieldType dft) {
        return getFont(INCONSOLATA_LGC, dft);
    }

    /**
     * Returns a Font already configured to use a highly-legible fixed-width font with good Unicode support
     * and a sans-serif geometric style. Does not use a distance field effect.
     * Caches the result for later calls. The font used is Iosevka, an open-source (SIL Open Font License) typeface by
     * <a href="https://be5invis.github.io/Iosevka/">Belleve Invis</a>, and it uses several customizations
     * thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of extended Latin,
     * Greek, and Cyrillic.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.IOSEVKA, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Iosevka-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-standard.dat">Iosevka-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-standard.png">Iosevka-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.txt">Iosevka-License.md</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Iosevka
     */
    public static Font getIosevka() {
        return getFont(IOSEVKA, STANDARD);
    }

    /**
     * Returns a Font already configured to use a highly-legible fixed-width font with good Unicode support
     * and a sans-serif geometric style, that should scale cleanly to even very large sizes (using an MSDF technique).
     * Caches the result for later calls. The font used is Iosevka, an open-source (SIL Open Font License) typeface by
     * <a href="https://be5invis.github.io/Iosevka/">Belleve Invis</a>, and it uses several customizations
     * thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of extended Latin,
     * Greek, and Cyrillic.
     * This uses the Multi-channel Signed Distance Field (MSDF) technique as opposed to the normal Signed Distance Field
     * technique, which gives the rendered font sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.IOSEVKA, Font.DistanceFieldType.MSDF)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Iosevka-msdf.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-msdf.dat">Iosevka-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-msdf.png">Iosevka-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.txt">Iosevka-License.md</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Iosevka using MSDF
     */
    public static Font getIosevkaMSDF() {
        return getFont(IOSEVKA, MSDF);
    }

    /**
     * Returns a Font already configured to use a highly-legible fixed-width font with good Unicode support
     * and a sans-serif geometric style, that should scale cleanly to fairly large sizes (using an SDF technique).
     * Caches the result for later calls. The font used is Iosevka, an open-source (SIL Open Font License) typeface by
     * <a href="https://be5invis.github.io/Iosevka/">Belleve Invis</a>, and it uses several customizations
     * thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of extended Latin,
     * Greek, and Cyrillic.
     * This uses the Signed Distance Field (SDF) technique as opposed to the Multi-channel Signed Distance Field
     * technique that {@link #getIosevkaMSDF()} uses, which isn't as sharp at large sizes but can look a little better
     * at small sizes.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.IOSEVKA, Font.DistanceFieldType.SDF)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Iosevka-sdf.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-sdf.dat">Iosevka-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-sdf.png">Iosevka-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.txt">Iosevka-License.md</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Iosevka using SDF
     */
    public static Font getIosevkaSDF() {
        return getFont(IOSEVKA, SDF);
    }

    /**
     * Returns a Font already configured to use a highly-legible fixed-width font with good Unicode support
     * and a sans-serif geometric style. Uses the given distance field type.
     * Caches the result for later calls. The font used is Iosevka, an open-source (SIL Open Font License) typeface by
     * <a href="https://be5invis.github.io/Iosevka/">Belleve Invis</a>, and it uses several customizations
     * thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of extended Latin,
     * Greek, and Cyrillic.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Iosevka-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-standard.dat">Iosevka-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-standard.png">Iosevka-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.txt">Iosevka-License.md</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-msdf.dat">Iosevka-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-msdf.png">Iosevka-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.txt">Iosevka-License.md</a></li>
     * </ul>
     * or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-sdf.dat">Iosevka-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-sdf.png">Iosevka-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.txt">Iosevka-License.md</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font Iosevka using the given DistanceFieldType
     */
    public static Font getIosevka(DistanceFieldType dft) {
        return getFont(IOSEVKA, dft);
    }

    /**
     * Returns a Font already configured to use a highly-legible fixed-width font with good Unicode support
     * and a slab-serif geometric style. Does not use a distance field effect.
     * Caches the result for later calls. The font used is Iosevka with Slab style, an open-source (SIL Open Font
     * License) typeface by <a href="https://be5invis.github.io/Iosevka/">Belleve Invis</a>, and it uses several
     * customizations thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of
     * extended Latin, Greek, and Cyrillic.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.IOSEVKA_SLAB, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Iosevka-Slab-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-standard.dat">Iosevka-Slab-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-standard.png">Iosevka-Slab-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.txt">Iosevka-License.md</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Iosevka Slab
     */
    public static Font getIosevkaSlab() {
        return getFont(IOSEVKA_SLAB, STANDARD);
    }

    /**
     * Returns a Font already configured to use a highly-legible fixed-width font with good Unicode support
     * and a slab-serif geometric style, that should scale cleanly to even very large sizes (using an MSDF technique).
     * Caches the result for later calls. The font used is Iosevka with Slab style, an open-source (SIL Open Font
     * License) typeface by <a href="https://be5invis.github.io/Iosevka/">Belleve Invis</a>, and it uses several
     * customizations thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of
     * extended Latin, Greek, and Cyrillic.
     * This uses the Multi-channel Signed Distance Field (MSDF) technique as opposed to the normal Signed Distance Field
     * technique, which gives the rendered font sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.IOSEVKA_SLAB, Font.DistanceFieldType.MSDF)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Iosevka-Slab-msdf.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-msdf.dat">Iosevka-Slab-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-msdf.png">Iosevka-Slab-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.txt">Iosevka-License.md</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Iosevka Slab using MSDF
     */
    public static Font getIosevkaSlabMSDF() {
        return getFont(IOSEVKA_SLAB, MSDF);
    }

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
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.IOSEVKA_SLAB, Font.DistanceFieldType.SDF)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Iosevka-Slab-msdf.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-sdf.dat">Iosevka-Slab-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-sdf.png">Iosevka-Slab-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.txt">Iosevka-License.md</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Iosevka Slab using SDF
     */
    public static Font getIosevkaSlabSDF() {
        return getFont(IOSEVKA_SLAB, SDF);
    }

    /**
     * Returns a Font already configured to use a highly-legible fixed-width font with good Unicode support
     * and a sans-serif geometric style. Uses the given distance field type.
     * Caches the result for later calls. The font used is Iosevka, an open-source (SIL Open Font License) typeface by
     * <a href="https://be5invis.github.io/Iosevka/">Belleve Invis</a>, and it uses several customizations
     * thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of extended Latin,
     * Greek, and Cyrillic.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Iosevka-Slab-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-standard.dat">Iosevka-Slab-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-standard.png">Iosevka-Slab-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.txt">Iosevka-License.md</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-msdf.dat">Iosevka-Slab-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-msdf.png">Iosevka-Slab-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.txt">Iosevka-License.md</a></li>
     * </ul>
     * or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-sdf.dat">Iosevka-Slab-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-sdf.png">Iosevka-Slab-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.txt">Iosevka-License.md</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font Iosevka Slab using the given DistanceFieldType
     */
    public static Font getIosevkaSlab(DistanceFieldType dft) {
        return getFont(IOSEVKA_SLAB, dft);
    }

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
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.KINGTHINGS_FOUNDATION, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Kingthings-Foundation-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-Foundation-standard.dat">Kingthings-Foundation-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-Foundation-standard.png">Kingthings-Foundation-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-License.txt">Kingthings-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Kingthings Foundation
     */
    public static Font getKingthingsFoundation() {
        return getFont(KINGTHINGS_FOUNDATION, STANDARD);
    }

    /**
     * Returns a Font already configured to use a fairly-legible variable-width ornamental/medieval font,
     * using the given distance field type.
     * Caches the result for later calls. The font used is Kingthings Foundation, a free (custom permissive license)
     * typeface; this has faux-bold applied already in order to make some ornamental curls visible at more sizes. You
     * can still apply bold again using markup. It supports only ASCII. You may want to also look at
     * {@link #getKingthingsPetrock() Kingthings Petrock}; where Petrock is less-ornamented, Foundation is heavily
     * ornamented, and Foundation may make sense for text associated with writers of high social status.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Kingthings-Foundation-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-Foundation-standard.dat">Kingthings-Foundation-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-Foundation-standard.png">Kingthings-Foundation-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-License.txt">Kingthings-License.txt</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-Foundation-msdf.dat">Kingthings-Foundation-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-Foundation-msdf.png">Kingthings-Foundation-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-License.txt">Kingthings-License.txt</a></li>
     * </ul>
     * or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-Foundation-sdf.dat">Kingthings-Foundation-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-Foundation-sdf.png">Kingthings-Foundation-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-License.txt">Kingthings-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font Kingthings Foundation using the given DistanceFieldType
     */
    public static Font getKingthingsFoundation(DistanceFieldType dft) {
        return getFont(KINGTHINGS_FOUNDATION, dft);
    }

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
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.KINGTHINGS_PETROCK, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Kingthings-Petrock-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-Petrock-standard.dat">Kingthings-Petrock-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-Petrock-standard.png">Kingthings-Petrock-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-License.txt">Kingthings-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Kingthings Petrock
     */
    public static Font getKingthingsPetrock() {
        return getFont(KINGTHINGS_PETROCK, STANDARD);
    }

    /**
     * Returns a Font already configured to use a clearly-legible variable-width medieval font,
     * using the given distance field type.
     * Caches the result for later calls. The font used is Kingthings Petrock, a free (custom permissive license)
     * typeface; it has a visual style similar to one used by some popular classic rock bands. It supports only ASCII
     * and a small amount of extended Latin. Kingthings Petrock is similar to
     * {@link #getKingthingsFoundation() Kingthings Foundation}, but Petrock isn't as heavily-ornamented, and looks more
     * like "every-day usable" medieval or maybe Renaissance text.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Kingthings-Petrock-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-Petrock-standard.dat">Kingthings-Petrock-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-Petrock-standard.png">Kingthings-Petrock-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-License.txt">Kingthings-License.txt</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-Petrock-msdf.dat">Kingthings-Petrock-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-Petrock-msdf.png">Kingthings-Petrock-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-License.txt">Kingthings-License.txt</a></li>
     * </ul>
     * or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-Petrock-sdf.dat">Kingthings-Petrock-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-Petrock-sdf.png">Kingthings-Petrock-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-License.txt">Kingthings-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font Kingthings Petrock using the given DistanceFieldType
     */
    public static Font getKingthingsPetrock(DistanceFieldType dft) {
        return getFont(KINGTHINGS_PETROCK, dft);
    }

    /**
     * Returns a Font already configured to use a variable-width pixel font with excellent Unicode support, that
     * probably should only be used at integer multiples of its normal size.
     * Caches the result for later calls. The font used is LanaPixel, an open-source (dual-licensed under the SIL Open
     * Font License and Creative Commons Attribution License) typeface. It supports an incredible amount of glyphs,
     * and is meant to allow localizing to just about any widely-used language.
     * This uses a tiny standard bitmap font, and it can only be used as-is or scaled up by integer multiples.
     * This defaults to having {@link Font#integerPosition} set to true, which currently does nothing (the code that
     * enforces integer positions seems to ruin the appearance of any font that uses it, so that code isn't ever used
     * now). This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://tommyettinger.github.io/textratypist/previews/LanaPixel.png">Image link</a> (uses width=20, height=15)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/LanaPixel-standard.fnt">LanaPixel-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/LanaPixel-standard.png">LanaPixel-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/LanaPixel-CCBYLicense.txt">LanaPixel-CCBYLicense.txt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/LanaPixel-OpenFontLicense.txt">LanaPixel-OpenFontLicense.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font LanaPixel
     */
    public static Font getLanaPixel() {
        initialize();
        final String baseName = LANAPIXEL;
        final DistanceFieldType distanceField = STANDARD;
        String rootName = baseName + distanceField.filePart;
        Font found = instance.loaded.get(rootName);
        if(found == null){
            found = new Font(instance.prefix + rootName + ".fnt", distanceField, 0, 0, 0, 0, false);
            found
                    .setInlineImageMetrics(-64, 0, 16).setFancyLinePosition(0f, 0.5f)
                    .useIntegerPositions(true).setBoldStrength(0.5f).setLineMetrics(0f, -0.0625f, 0f, 0f)
                    .setName(baseName + distanceField.namePart);
            instance.loaded.put(rootName, found);
        }
        return new Font(found);
    }

    /**
     * Returns a Font already configured to use a variable-width serif font with good Unicode support, that should
     * scale cleanly to fairly large sizes or down to about 20 pixels.
     * Caches the result for later calls. The font used is Libertinus Serif, an open-source (SIL Open Font
     * License) typeface. It supports a lot of glyphs, including quite a bit of extended Latin, Greek, and Cyrillic.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.LIBERTINUS_SERIF, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Libertinus-Serif-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Libertinus-Serif-standard.dat">Libertinus-Serif-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Libertinus-Serif-standard.png">Libertinus-Serif-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Libertinus-Serif-License.txt">Libertinus-Serif-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Libertinus Serif
     */
    public static Font getLibertinusSerif() {
        return getFont(LIBERTINUS_SERIF, STANDARD);
    }

    /**
     * Returns a Font already configured to use a variable-width serif font with good Unicode support,
     * using the given distance field type.
     * Caches the result for later calls. The font used is Libertinus Serif, an open-source (SIL Open Font
     * License) typeface. It supports a lot of glyphs, including quite a bit of extended Latin, Greek, and Cyrillic.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Libertinus-Serif-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Libertinus-Serif-standard.dat">Libertinus-Serif-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Libertinus-Serif-standard.png">Libertinus-Serif-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Libertinus-Serif-License.txt">Libertinus-Serif-License.txt</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Libertinus-Serif-msdf.dat">Libertinus-Serif-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Libertinus-Serif-msdf.png">Libertinus-Serif-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Libertinus-Serif-License.txt">Libertinus-Serif-License.txt</a></li>
     * </ul>
     * or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Libertinus-Serif-sdf.dat">Libertinus-Serif-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Libertinus-Serif-sdf.png">Libertinus-Serif-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Libertinus-Serif-License.txt">Libertinus-Serif-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font Libertinus Serif using the given DistanceFieldType
     */
    public static Font getLibertinusSerif(DistanceFieldType dft) {
        return getFont(LIBERTINUS_SERIF, dft);
    }


    /**
     * Returns a Font already configured to use a variable-width heavy-weight serif font with good Unicode support, that
     * should scale cleanly to fairly large sizes or down to about 20 pixels.
     * Caches the result for later calls. The font used is Libertinus Serif Semibold, an open-source (SIL Open Font
     * License) typeface. It supports a lot of glyphs, including quite a bit of extended Latin, Greek, and Cyrillic.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.LIBERTINUS_SERIF_SEMIBOLD, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Libertinus-Serif-Semibold-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Libertinus-Serif-Semibold-standard.dat">Libertinus-Serif-Semibold-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Libertinus-Serif-Semibold-standard.png">Libertinus-Serif-Semibold-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Libertinus-Serif-License.txt">Libertinus-Serif-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Libertinus Serif Semibold
     */
    public static Font getLibertinusSerifSemibold() {
        return getFont(LIBERTINUS_SERIF_SEMIBOLD, STANDARD);
    }

    /**
     * Returns a Font already configured to use a variable-width heavy-weight serif font with good Unicode support,
     * using the given distance field type.
     * Caches the result for later calls. The font used is Libertinus Serif Semibold, an open-source (SIL Open Font
     * License) typeface. It supports a lot of glyphs, including quite a bit of extended Latin, Greek, and Cyrillic.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Libertinus-Serif-Semibold-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Libertinus-Serif-Semibold-standard.dat">Libertinus-Serif-Semibold-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Libertinus-Serif-Semibold-standard.png">Libertinus-Serif-Semibold-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Libertinus-Serif-License.txt">Libertinus-Serif-License.txt</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Libertinus-Serif-Semibold-msdf.dat">Libertinus-Serif-Semibold-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Libertinus-Serif-Semibold-msdf.png">Libertinus-Serif-Semibold-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Libertinus-Serif-License.txt">Libertinus-Serif-License.txt</a></li>
     * </ul>
     * or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Libertinus-Serif-Semibold-sdf.dat">Libertinus-Serif-Semibold-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Libertinus-Serif-Semibold-sdf.png">Libertinus-Serif-Semibold-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Libertinus-Serif-License.txt">Libertinus-Serif-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font Libertinus Serif Semibold using the given DistanceFieldType
     */
    public static Font getLibertinusSerifSemibold(DistanceFieldType dft) {
        return getFont(LIBERTINUS_SERIF_SEMIBOLD, dft);
    }

    /**
     * Returns a Font already configured to use a variable-width geometric sans-serif font, that should
     * scale cleanly to fairly large sizes or down to about 20 pixels.
     * Caches the result for later calls. The font used is Now Alt, an open-source (SIL Open Font License) typeface by
     * Hanken Design Co. It has decent glyph coverage for most European languages, but doesn't fully support Greek or
     * Cyrillic. This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very
     * well. This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.NOW_ALT, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Now-Alt-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Now-Alt-standard.dat">Now-Alt-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Now-Alt-standard.png">Now-Alt-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Now-Alt-License.txt">Now-Alt-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font NowAlt
     */
    public static Font getNowAlt() {
        return getFont(NOW_ALT, STANDARD);
    }

    /**
     * Returns a Font already configured to use a variable-width geometric sans-serif font,
     * using the given distance field type.
     * Caches the result for later calls. The font used is Now Alt, an open-source (SIL Open Font License) typeface by
     * Hanken Design Co. It has decent glyph coverage for most European languages, but doesn't fully support Greek or
     * Cyrillic.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Now-Alt-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Now-Alt-standard.dat">Now-Alt-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Now-Alt-standard.png">Now-Alt-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Now-Alt-License.txt">Now-Alt-License.txt</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Now-Alt-msdf.dat">Now-Alt-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Now-Alt-msdf.png">Now-Alt-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Now-Alt-License.txt">Now-Alt-License.txt</a></li>
     * </ul>
     * or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Now-Alt-sdf.dat">Now-Alt-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Now-Alt-sdf.png">Now-Alt-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Now-Alt-License.txt">Now-Alt-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font NowAlt using the given DistanceFieldType
     */
    public static Font getNowAlt(DistanceFieldType dft) {
        return getFont(NOW_ALT, dft);
    }

    /**
     * Returns a Font configured to use a clean variable-width font, Open Sans. It has good extended-Latin coverage, but
     * does not support Greek, Cyrillic, or other scripts. This makes an especially large font by default, but can be
     * scaled down nicely.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.OPEN_SANS, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Open-Sans-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Open-Sans-standard.dat">Open-Sans-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Open-Sans-standard.png">Open-Sans-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Open-Sans-License.txt">Open-Sans-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that represents the variable-width font OpenSans
     */
    public static Font getOpenSans() {
        return getFont(OPEN_SANS, STANDARD);
    }

    /**
     * Returns a Font configured to use a clean variable-width font, Open Sans,
     * using the given distance field type. It has good extended-Latin coverage, but
     * does not support Greek, Cyrillic, or other scripts. This makes an especially large font by default, but can be
     * scaled down nicely.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Open-Sans-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Open-Sans-standard.dat">Open-Sans-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Open-Sans-standard.png">Open-Sans-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Open-Sans-License.txt">Open-Sans-License.txt</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Open-Sans-msdf.dat">Open-Sans-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Open-Sans-msdf.png">Open-Sans-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Open-Sans-License.txt">Open-Sans-License.txt</a></li>
     * </ul>
     * or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Open-Sans-sdf.dat">Open-Sans-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Open-Sans-sdf.png">Open-Sans-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Open-Sans-License.txt">Open-Sans-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the variable-width font OpenSans using the given DistanceFieldType
     */
    public static Font getOpenSans(DistanceFieldType dft) {
        return getFont(OPEN_SANS, dft);
    }

    /**
     * Returns a Font configured to use an ALL-CAPS, variable-width, tall, very-heavy-weight sans-serif font, Ostrich
     * Black, without using a distance field effect. It is OFL-licensed and was made by Tyler Fink.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.OSTRICH_BLACK, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Ostrich-Black-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Ostrich-Black-standard.dat">Ostrich-Black-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Ostrich-Black-standard.png">Ostrich-Black-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Ostrich-License.md">Ostrich-License.md</a></li>
     * </ul>
     *
     * @return the Font object that represents the variable-width font Ostrich Black
     */
    public static Font getOstrichBlack() {
        return getFont(OSTRICH_BLACK, STANDARD);
    }

    /**
     * Returns a Font configured to use an ALL-CAPS, variable-width, tall, very-heavy-weight sans-serif font, Ostrich
     * Black, using the given distance field type. It is OFL-licensed and was made by Tyler Fink.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Ostrich-Black-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Ostrich-Black-standard.dat">Ostrich-Black-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Ostrich-Black-standard.png">Ostrich-Black-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Ostrich-License.md">Ostrich-License.md</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Ostrich-Black-msdf.dat">Ostrich-Black-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Ostrich-Black-msdf.png">Ostrich-Black-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Ostrich-License.md">Ostrich-License.md</a></li>
     * </ul>
     * or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Ostrich-Black-sdf.dat">Ostrich-Black-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Ostrich-Black-sdf.png">Ostrich-Black-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Ostrich-License.md">Ostrich-License.md</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the variable-width font Ostrich Black using the given DistanceFieldType
     */
    public static Font getOstrichBlack(DistanceFieldType dft) {
        return getFont(OSTRICH_BLACK, dft);
    }

    /**
     * Returns a Font already configured to use a variable-width "flowy" sans-serif font, that should
     * scale pretty well down, but not up.
     * Caches the result for later calls. The font used is Overlock, a free (OFL) typeface by Dario Manuel Muhafara.
     * It supports a little more than ASCII and Latin-1, but not much Greek and no Cyrillic.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.OVERLOCK, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Overlock-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Overlock-standard.dat">Overlock-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Overlock-standard.png">Overlock-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Overlock-License.txt">Overlock-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Overlock
     */
    public static Font getOverlock() {
        return getFont(OVERLOCK, STANDARD);
    }

    /**
     * Returns a Font already configured to use a variable-width "flowy" sans-serif font,
     * using the given distance field type.
     * Caches the result for later calls. The font used is Overlock, a free (OFL) typeface by Dario Manuel Muhafara.
     * It supports a little more than ASCII and Latin-1, but not much Greek and no Cyrillic.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Overlock-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Overlock-standard.dat">Overlock-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Overlock-standard.png">Overlock-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Overlock-License.txt">Overlock-License.txt</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Overlock-msdf.dat">Overlock-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Overlock-msdf.png">Overlock-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Overlock-License.txt">Overlock-License.txt</a></li>
     * </ul>
     * or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Overlock-sdf.dat">Overlock-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Overlock-sdf.png">Overlock-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Overlock-License.txt">Overlock-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font Overlock using the given DistanceFieldType
     */
    public static Font getOverlock(DistanceFieldType dft) {
        return getFont(OVERLOCK, dft);
    }

    /**
     * Returns a Font already configured to use a variable-width "italic-like" and "especially flowy" sans font.
     * Caches the result for later calls. The font used is Overlock, a free (OFL) typeface by Dario Manuel Muhafara, but
     * this took Overlock Italic and removed the 7.4-degree slant it had, so it looks like a regular face but with the
     * different curvature and the "flow" of an italic font. This "un-italic" effect can be useful for making text look
     * more human-written, and this font looks somewhat similar to a lighter-weight {@link #getGentiumUnItalic()}.
     * It supports a little more than ASCII and Latin-1, but not much Greek and no Cyrillic.
     * This does not use a distance field effect. The SDF and MSDF distance field effects are likely to look much better
     * than this at normal font sizes; for example, you can use {@link #getOverlockUnItalic(DistanceFieldType)} with
     * {@link DistanceFieldType#MSDF} as the argument to use MSDF.
     * <br>
     * Thanks to Siavash Ranbar, who came up with the idea to take an italic version of a font and remove its
     * slant, keeping the different flow from a simple oblique font.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.OVERLOCK_UN_ITALIC, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Overlock-Un-Italic-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Overlock-Un-Italic-standard.dat">Overlock-Un-Italic-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Overlock-Un-Italic-standard.png">Overlock-Un-Italic-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Overlock-License.txt">Overlock-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Overlock Un-Italic
     */
    public static Font getOverlockUnItalic() {
        return getFont(OVERLOCK_UN_ITALIC, STANDARD);
    }

    /**
     * Returns a Font already configured to use a variable-width "italic-like" and "especially flowy" sans font.
     * Uses the given distance field type. Caches the result for later calls. The font used is Overlock, a free (OFL)
     * typeface by Dario Manuel Muhafara, but this took Overlock Italic and removed the 7.4-degree slant it had, so it
     * looks like a regular face but with the different curvature and the "flow" of an italic font. This "un-italic"
     * effect can be useful for making text look more human-written, and this font looks somewhat similar to a
     * lighter-weight {@link #getGentiumUnItalic()}.
     * It supports a little more than ASCII and Latin-1, but not much Greek and no Cyrillic.
     * The SDF and MSDF distance field effects are likely to look much better than Standard at normal font sizes.
     * <br>
     * Thanks to Siavash Ranbar, who came up with the idea to take an italic version of a font and remove its
     * slant, keeping the different flow from a simple oblique font.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Overlock-Un-Italic-msdf.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Overlock-Un-Italic-standard.dat">Overlock-Un-Italic-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Overlock-Un-Italic-standard.png">Overlock-Un-Italic-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Overlock-License.txt">Overlock-License.txt</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Overlock-Un-Italic-sdf.dat">Overlock-Un-Italic-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Overlock-Un-Italic-sdf.png">Overlock-Un-Italic-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Overlock-License.txt">Overlock-License.txt</a></li>
     * </ul>
     * or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Overlock-Un-Italic-msdf.dat">Overlock-Un-Italic-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Overlock-Un-Italic-msdf.png">Overlock-Un-Italic-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Overlock-License.txt">Overlock-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font Overlock Italic, with slant removed, using the given DistanceFieldType
     */
    public static Font getOverlockUnItalic(DistanceFieldType dft) {
        return getFont(OVERLOCK_UN_ITALIC, dft);
    }

    /**
     * Returns a Font already configured to use a variable-width "science-fiction/high-tech" font, that should
     * scale pretty well down, but not up.
     * Caches the result for later calls. The font used is Oxanium, a free (OFL) typeface. It supports a lot of Latin
     * and extended Latin, but not Greek or Cyrillic.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.OXANIUM, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Oxanium-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Oxanium-standard.dat">Oxanium-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Oxanium-standard.png">Oxanium-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Oxanium-License.txt">Oxanium-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Oxanium
     */
    public static Font getOxanium() {
        return getFont(OXANIUM, STANDARD);
    }

    /**
     * Returns a Font already configured to use a variable-width "science-fiction/high-tech" font,
     * using the given distance field type.
     * Caches the result for later calls. The font used is Oxanium, a free (OFL) typeface. It supports a lot of Latin
     * and extended Latin, but not Greek or Cyrillic.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Oxanium-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Oxanium-standard.dat">Oxanium-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Oxanium-standard.png">Oxanium-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Oxanium-License.txt">Oxanium-License.txt</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Oxanium-msdf.dat">Oxanium-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Oxanium-msdf.png">Oxanium-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Oxanium-License.txt">Oxanium-License.txt</a></li>
     * </ul>
     * or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Oxanium-sdf.dat">Oxanium-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Oxanium-sdf.png">Oxanium-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Oxanium-License.txt">Oxanium-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font Oxanium using the given DistanceFieldType
     */
    public static Font getOxanium(DistanceFieldType dft) {
        return getFont(OXANIUM, dft);
    }

    /**
     * Returns a Font configured to use a small variable-width bitmap font with extensive coverage of Asian scripts,
     * <a href="https://diaowinner.itch.io/galmuri-extended">QuanPixel</a>. QuanPixel has good coverage of Unicode,
     * including all of Greek, at least most of Cyrillic, a good amount of extended Latin, all of Katakana and Hiragana,
     * many Hangul syllables, and literally thousands of CJK ideograms. This does not scale well except to integer
     * multiples, but it should look very crisp at its default size of about 8 pixels tall with variable width. This
     * defaults to having {@link Font#integerPosition} set to true, which currently does nothing (the code that enforces
     * integer positions seems to ruin the appearance of any font that uses it, so that code isn't ever used now).
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://tommyettinger.github.io/textratypist/previews/QuanPixel.png">Image link</a> (uses width=12, height=12; this size is small
     * enough to make the scaled text unreadable in some places)
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
        final String baseName = QUANPIXEL;
        final DistanceFieldType distanceField = STANDARD;
        String rootName = baseName + distanceField.filePart;
        Font found = instance.loaded.get(rootName);
        if(found == null){
            found = new Font(instance.prefix + rootName + ".fnt", distanceField, 0, 2, 0, 2, false);
            found
                    .setLineMetrics(0.0625f, -0.0625f, -0.25f, 0f).setInlineImageMetrics(-40f, -4f, 0f)
                    .setFancyLinePosition(0f, 0.375f).useIntegerPositions(true).setDescent(-4f)
                    .setBoldStrength(0.5f)
                    .setName(baseName + distanceField.namePart);
            ;
            instance.loaded.put(rootName, found);
        }
        return new Font(found);
    }

    /**
     * Returns a Font already configured to use a very-legible condensed variable-width font with excellent Unicode
     * support, that should scale pretty well from a height of about 62 down to a height of maybe 20.
     * Caches the result for later calls. The font used is Roboto Condensed, a free (Apache 2.0) typeface by Christian
     * Robertson. It supports Latin-based scripts almost entirely, plus Greek, (extended) Cyrillic, and more.
     * This font is meant to be condensed in its natural appearance, but can be scaled to be wider if desired.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.ROBOTO_CONDENSED, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Roboto-Condensed-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Roboto-Condensed-standard.dat">Roboto-Condensed-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Roboto-Condensed-standard.png">Roboto-Condensed-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Roboto-Condensed-License.txt">Roboto-Condensed-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Roboto Condensed
     */
    public static Font getRobotoCondensed() {
        return getFont(ROBOTO_CONDENSED, STANDARD);
    }

    /**
     * Returns a Font already configured to use a very-legible condensed variable-width font with excellent Unicode
     * support, using the given distance field type.
     * Caches the result for later calls. The font used is Roboto Condensed, a free (Apache 2.0) typeface by Christian
     * Robertson. It supports Latin-based scripts almost entirely, plus Greek, (extended) Cyrillic, and more.
     * This font is meant to be condensed in its natural appearance, but can be scaled to be wider if desired.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Roboto-Condensed-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Roboto-Condensed-standard.dat">Roboto-Condensed-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Roboto-Condensed-standard.png">Roboto-Condensed-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Roboto-Condensed-License.txt">Roboto-Condensed-License.txt</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Roboto-Condensed-msdf.dat">Roboto-Condensed-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Roboto-Condensed-msdf.png">Roboto-Condensed-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Roboto-Condensed-License.txt">Roboto-Condensed-License.txt</a></li>
     * </ul>
     * or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Roboto-Condensed-sdf.dat">Roboto-Condensed-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Roboto-Condensed-sdf.png">Roboto-Condensed-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Roboto-Condensed-License.txt">Roboto-Condensed-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font Roboto Condensed using the given DistanceFieldType
     */
    public static Font getRobotoCondensed(DistanceFieldType dft) {
        return getFont(ROBOTO_CONDENSED, dft);
    }


    /**
     * Returns a Font already configured to use a variable-width humanist sans-serif font, that should
     * scale pretty well down, but not up. This font is metric-compatible with Segoe UI.
     * Caches the result for later calls. The font used is Selawik, a free (OFL) typeface released by Microsoft.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.SELAWIK, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Selawik-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Selawik-standard.dat">Selawik-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Selawik-standard.png">Selawik-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Selawik-License.txt">Selawik-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Selawik
     */
    public static Font getSelawik() {
        return getFont(SELAWIK, STANDARD);
    }

    /**
     * Returns a Font already configured to use a variable-width humanist sans-serif font,
     * using the given distance field type. This font is metric-compatible with Segoe UI.
     * Caches the result for later calls. The font used is Selawik, a free (OFL) typeface released by Microsoft.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Selawik-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Selawik-standard.dat">Selawik-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Selawik-standard.png">Selawik-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Selawik-License.txt">Selawik-License.txt</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Selawik-msdf.dat">Selawik-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Selawik-msdf.png">Selawik-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Selawik-License.txt">Selawik-License.txt</a></li>
     * </ul>
     * or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Selawik-sdf.dat">Selawik-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Selawik-sdf.png">Selawik-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Selawik-License.txt">Selawik-License.txt</a></li>
     * </ul>
     *
     * @param dft which distance field type to use, such as {@link DistanceFieldType#STANDARD} or {@link DistanceFieldType#SDF}
     * @return the Font object that can represent many sizes of the font Selawik using the given DistanceFieldType
     */
    public static Font getSelawik(DistanceFieldType dft) {
        return getFont(SELAWIK, dft);
    }

    /**
     * Returns a Font already configured to use a variable-width script font, that should
     * scale pretty well down, but not up.
     * Caches the result for later calls. The font used is Tangerine, a free (OFL) typeface. It supports Latin only,
     * with a little support for Western European languages, but not really anything else. It looks elegant, though.
     * This uses a very-large standard bitmap font, which lets it be scaled down OK but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.TANGERINE, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Tangerine-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Tangerine-standard.dat">Tangerine-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Tangerine-standard.png">Tangerine-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Tangerine-License.txt">Tangerine-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Tangerine
     */
    public static Font getTangerine() {
        return getFont(TANGERINE, STANDARD);
    }

    /**
     * Returns a Font already configured to use a variable-width script font, that should scale to many sizes using
     * an SDF effect.
     * Caches the result for later calls. The font used is Tangerine, a free (OFL) typeface. It supports Latin only,
     * with a little support for Western European languages, but not really anything else. It looks elegant, though.
     * This uses the Signed Distance Field (SDF) technique, which may be slightly fuzzy when zoomed in heavily, but
     * should be crisp enough when zoomed out.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.TANGERINE, Font.DistanceFieldType.SDF)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Tangerine-sdf.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Tangerine-sdf.dat">Tangerine-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Tangerine-sdf.png">Tangerine-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Tangerine-License.txt">Tangerine-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Tangerine using SDF
     */
    public static Font getTangerineSDF() {
        return getFont(TANGERINE, SDF);
    }

    /**
     * Returns a Font already configured to use a variable-width script font,
     * using the given distance field type.
     * Caches the result for later calls. The font used is Tangerine, a free (OFL) typeface. It supports Latin only,
     * with a little support for Western European languages, but not really anything else. It looks elegant, though.
     * Using MSDF is currently recommended.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Tangerine-msdf.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Tangerine-standard.dat">Tangerine-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Tangerine-standard.png">Tangerine-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Tangerine-License.txt">Tangerine-License.txt</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Tangerine-msdf.dat">Tangerine-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Tangerine-msdf.png">Tangerine-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Tangerine-License.txt">Tangerine-License.txt</a></li>
     * </ul>
     * or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Tangerine-sdf.dat">Tangerine-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Tangerine-sdf.png">Tangerine-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Tangerine-License.txt">Tangerine-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Tangerine using the given DistanceFieldType
     */
    public static Font getTangerine(DistanceFieldType dft) {
        return getFont(TANGERINE, dft);
    }

    /**
     * Returns a Font already configured to use a variable-width, narrow, humanist font, that should
     * scale pretty well down, but not up.
     * Caches the result for later calls. The font used is Yanone Kaffeesatz, a free (OFL) typeface. It supports a lot
     * of Latin, Cyrillic, and some extended Latin, but not Greek.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.YANONE_KAFFEESATZ, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Yanone-Kaffeesatz-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yanone-Kaffeesatz-standard.dat">Yanone-Kaffeesatz-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yanone-Kaffeesatz-standard.png">Yanone-Kaffeesatz-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yanone-Kaffeesatz-License.txt">Yanone-Kaffeesatz-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Yanone Kaffeesatz
     */
    public static Font getYanoneKaffeesatz() {
        return getFont(YANONE_KAFFEESATZ, STANDARD);
    }

    /**
     * Returns a Font already configured to use a variable-width, narrow, humanist font, that should scale
     * to many sizes using an MSDF effect.
     * Caches the result for later calls. The font used is Yanone Kaffeesatz, a free (OFL) typeface. It supports a lot
     * of Latin, Cyrillic, and some extended Latin, but not Greek.
     * This uses the Multi-channel Signed Distance Field (MSDF) technique as opposed to the normal Signed Distance Field
     * technique, which gives the rendered font sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.YANONE_KAFFEESATZ, Font.DistanceFieldType.MSDF)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Yanone-Kaffeesatz-msdf.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yanone-Kaffeesatz-msdf.dat">Yanone-Kaffeesatz-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yanone-Kaffeesatz-msdf.png">Yanone-Kaffeesatz-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yanone-Kaffeesatz-License.txt">Yanone-Kaffeesatz-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Yanone Kaffeesatz using MSDF
     */
    public static Font getYanoneKaffeesatzMSDF() {
        return getFont(YANONE_KAFFEESATZ, MSDF);
    }

    /**
     * Returns a Font already configured to use a variable-width, narrow, humanist font,
     * using the given distance field type.
     * Caches the result for later calls. The font used is Yanone Kaffeesatz, a free (OFL) typeface. It supports a lot
     * of Latin, Cyrillic, and some extended Latin, but not Greek.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Yanone-Kaffeesatz-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yanone-Kaffeesatz-standard.dat">Yanone-Kaffeesatz-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yanone-Kaffeesatz-standard.png">Yanone-Kaffeesatz-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yanone-Kaffeesatz-License.txt">Yanone-Kaffeesatz-License.txt</a></li>
     * </ul>
     * or,
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yanone-Kaffeesatz-msdf.dat">Yanone-Kaffeesatz-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yanone-Kaffeesatz-msdf.png">Yanone-Kaffeesatz-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yanone-Kaffeesatz-License.txt">Yanone-Kaffeesatz-License.txt</a></li>
     * </ul>
     * or
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yanone-Kaffeesatz-sdf.dat">Yanone-Kaffeesatz-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yanone-Kaffeesatz-sdf.png">Yanone-Kaffeesatz-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yanone-Kaffeesatz-License.txt">Yanone-Kaffeesatz-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Yanone Kaffeesatz using the given DistanceFieldType
     */
    public static Font getYanoneKaffeesatz(DistanceFieldType dft) {
        return getFont(YANONE_KAFFEESATZ, dft);
    }

    /**
     * Returns a Font already configured to use a variable-width, narrow, "dark fantasy" font.
     * Caches the result for later calls. The font used is Yataghan, a widely-distributed typeface. It supports ASCII
     * and some extended Latin, but not much else.
     * <br>
     * I don't know who the original author of Yataghan was; if you are the original author and want attribution or want
     * this font removed, please post an issue on the tommyettinger/textratypist GitHub repo, or email tommyettinger.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.YATAGHAN, Font.DistanceFieldType.STANDARD)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Yataghan-standard.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yataghan-standard.dat">Yataghan-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yataghan-standard.png">Yataghan-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yataghan-License.txt">Yataghan-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Yataghan
     */
    public static Font getYataghan() {
        return getFont(YATAGHAN, STANDARD);
    }

    /**
     * Returns a Font already configured to use a variable-width, narrow, "dark fantasy" font,
     * that can scale to many sizes using an MSDF effect.
     * Caches the result for later calls. The font used is Yataghan, a widely-distributed typeface. It supports ASCII
     * and some extended Latin, but not much else.
     * This uses the Multi-channel Signed Distance Field (MSDF) technique as opposed to the normal Signed Distance Field
     * technique, which gives the rendered font sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * I don't know who the original author of Yataghan was; if you are the original author and want attribution or want
     * this font removed, please post an issue on the tommyettinger/textratypist GitHub repo, or email tommyettinger.
     * <br>
     * This returns the same thing as {@code KnownFonts.getFont(KnownFonts.YATAGHAN, Font.DistanceFieldType.MSDF)};
     * using {@link #getFont(String, DistanceFieldType)} is preferred in new code unless a font needs special support.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Yataghan-msdf.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yataghan-msdf.dat">Yataghan-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yataghan-msdf.png">Yataghan-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yataghan-License.txt">Yataghan-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Yataghan using MSDF
     */
    public static Font getYataghanMSDF() {
        return getFont(YATAGHAN, MSDF);
    }

    /**
     * Returns a Font already configured to use a variable-width, narrow, "dark fantasy" font,
     * using the given distance field type.
     * Caches the result for later calls. The font used is Yataghan, a widely-distributed typeface. It supports ASCII
     * and some extended Latin, but not much else.
     * <br>
     * I don't know who the original author of Yataghan was; if you are the original author and want attribution or want
     * this font removed, please post an issue on the tommyettinger/textratypist GitHub repo, or email tommyettinger.
     * <br>
     * Preview: <img src="https://tommyettinger.github.io/fontwriter/knownFonts/previews/Yataghan-msdf.png" alt="Image preview" width="1200" height="675" />
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yataghan-standard.dat">Yataghan-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yataghan-standard.png">Yataghan-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yataghan-License.txt">Yataghan-License.txt</a></li>
     * </ul>
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yataghan-msdf.dat">Yataghan-msdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yataghan-msdf.png">Yataghan-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yataghan-License.txt">Yataghan-License.txt</a></li>
     * </ul>
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yataghan-sdf.dat">Yataghan-sdf.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yataghan-sdf.png">Yataghan-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Yataghan-License.txt">Yataghan-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Yataghan using the given DistanceFieldType
     */
    public static Font getYataghan(DistanceFieldType dft) {
        return getFont(YATAGHAN, dft);
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
                if (line.isEmpty()) return 0;
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

                BufferedReader reader;
                try {
                    // all this for just one line changed...
                    reader = new BufferedReader(packFile.reader("UTF-8"), 1024);
                } catch (IllegalArgumentException e) {
                    throw new GdxRuntimeException(e); // This should never happen on a sane JVM.
                }
                try {
                    String line = reader.readLine();
                    // Ignore empty lines before first entry.
                    while (line != null && line.trim().isEmpty())
                        line = reader.readLine();
                    // Header entries.
                    while (true) {
                        if (line == null || line.trim().isEmpty()) break;
                        if (readEntry(entry, line) == 0) break; // Silently ignore all header fields.
                        line = reader.readLine();
                    }
                    // Page and region entries.
                    Page page = null;
                    Array<String> names = null;
                    Array<int[]> values = null;
                    while (line != null) {
                        if (line.trim().isEmpty()) {
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
//                        name = (name >> 31 | -name >>> 31);
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
     * There are over 3000 emoji in the Twemoji set;
     * <a href="https://github.com/twitter/twemoji#attribution-requirements">it requires attribution to use</a>.
     * <br>
     * You can add emoji to a font as inline images with {@code KnownFonts.addEmoji(myFont)}.
     * Since TextraTypist 1.0.0, emoji display correctly with standard, SDF, and MSDF fonts, though they always look how
     * they do with standard fonts and don't use any distance field themselves. They can scale reasonably well down, and
     * less-reasonably well up, but at typical text sizes (12-30 pixels in height) they tend to be legible.
     * You can search for names in {@code Twemoji.atlas}, or use the emoji picker in
     * <a href="https://github.com/raeleus/skin-composer">Skin Composer</a> to navigate by category. You can also use
     * the emoji picker present in some OSes, such as how Win+. allows selecting an emoji on Windows 10 and up.
     * Programmatically, you can use {@link Font#nameLookup} to look up the internal {@code char} this uses for a given
     * name or emoji, and {@link Font#namesByCharCode} to go from such an internal code to an emoji (as UTF-8).
     * <br>
     * Note that there isn't enough available space in a Font to add both emoji with this and icons with
     * {@link #addGameIcons(Font)}. You can, however, make two copies of a Font, add emoji to one and icons to the
     * other, and put both in a FontFamily, so you can access both atlases in the same block of text.
     * <br>
     * Preview: <a href="https://tommyettinger.github.io/textratypist/previews/EmojiPreview.png">Image link</a> (uses
     * the font {@link #getAStarry()} and {@code [%?blacken]} mode)
     * <br>
     * You can see all emoji and the names they use
     * <a href="https://tommyettinger.github.io/twemoji-atlas/">at this GitHub Pages site</a>.
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
     * Since TextraTypist 1.0.0, emoji display correctly with standard, SDF, and MSDF fonts, though they always look how
     * they do with standard fonts and don't use any distance field themselves. They can scale reasonably well down, and
     * less-reasonably well up, but at typical text sizes (12-30 pixels in height) they tend to be legible.
     * You can search for names in {@code Twemoji.atlas}, or use the emoji picker in
     * <a href="https://github.com/raeleus/skin-composer">Skin Composer</a> to navigate by category. You can also use
     * the emoji picker present in some OSes, such as how Win+. allows selecting an emoji on Windows 10 and up.
     * Programmatically, you can use {@link Font#nameLookup} to look up the internal {@code char} this uses for a given
     * name or emoji, and {@link Font#namesByCharCode} to go from such an internal code to an emoji (as UTF-8).
     * <br>
     * Note that there isn't enough available space in a Font to add both emoji with this and icons with
     * {@link #addGameIcons(Font)}. You can, however, make two copies of a Font, add emoji to one and icons to the
     * other, and put both in a FontFamily, so you can access both atlases in the same block of text.
     * <br>
     * This overload allows customizing the x/y offsets and x-advance for every emoji this puts in a Font.
     * <br>
     * Preview: <a href="https://tommyettinger.github.io/textratypist/previews/EmojiPreview.png">Image link</a> (uses
     * the font {@link #getAStarry()} and {@code [%?blacken]} mode)
     * <br>
     * You can see all emoji and the names they use
     * <a href="https://tommyettinger.github.io/twemoji-atlas/">at this GitHub Pages site</a>.
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Twemoji.atlas">Twemoji.atlas</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Twemoji.png">Twemoji.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Twemoji-License.txt">Twemoji-License.txt</a></li>
     * </ul>
     *
     * @param changing a Font that will have over 3000 emoji added to it, with more aliases
     * @param offsetXChange will be added to the {@link Font.GlyphRegion#offsetX} of each added glyph; in practice, positive values push emoji to the right
     * @param offsetYChange will be added to the {@link Font.GlyphRegion#offsetY} of each added glyph; in practice, positive values push emoji down
     * @param xAdvanceChange will be added to the {@link Font.GlyphRegion#xAdvance} of each added glyph; in practice, positive values shrink emoji
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
     * Since TextraTypist 1.0.0, emoji display correctly with standard, SDF, and MSDF fonts, though they always look how
     * they do with standard fonts and don't use any distance field themselves. They can scale reasonably well down, and
     * less-reasonably well up, but at typical text sizes (12-30 pixels in height) they tend to be legible.
     * You can search for names in {@code Twemoji.atlas}, or use the emoji picker in
     * <a href="https://github.com/raeleus/skin-composer">Skin Composer</a> to navigate by category. You can also use
     * the emoji picker present in some OSes, such as how Win+. allows selecting an emoji on Windows 10 and up.
     * Programmatically, you can use {@link Font#nameLookup} to look up the internal {@code char} this uses for a given
     * name or emoji, and {@link Font#namesByCharCode} to go from such an internal code to an emoji (as UTF-8).
     * <br>
     * Note that there isn't enough available space in a Font to add both emoji with this and icons with
     * {@link #addGameIcons(Font)}. You can, however, make two copies of a Font, add emoji to one and icons to the
     * other, and put both in a FontFamily, so you can access both atlases in the same block of text.
     * <br>
     * This overload allows customizing the x/y offsets and x-advance for every emoji this puts in a Font. It also
     * allows specifying Strings to prepend before and append after each name in the font, including emoji names.
     * <br>
     * Preview: <a href="https://tommyettinger.github.io/textratypist/previews/EmojiPreview.png">Image link</a> (uses
     * the font {@link #getAStarry()} and {@code [%?blacken]} mode)
     * <br>
     * You can see all emoji and the names they use
     * <a href="https://tommyettinger.github.io/twemoji-atlas/">at this GitHub Pages site</a>.
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
     * @param offsetXChange will be added to the {@link Font.GlyphRegion#offsetX} of each added glyph; in practice, positive values push emoji to the right
     * @param offsetYChange will be added to the {@link Font.GlyphRegion#offsetY} of each added glyph; in practice, positive values push emoji down
     * @param xAdvanceChange will be added to the {@link Font.GlyphRegion#xAdvance} of each added glyph; in practice, positive values shrink emoji
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
                    offsetXChange, offsetYChange, xAdvanceChange);
        }
        throw new RuntimeException("Assets 'Twemoji.atlas' and 'Twemoji.png' not found.");
    }

    private TextureAtlas openMojiColor, openMojiWhite;

    /**
     * Takes a Font and adds the OpenMoji icon set to it, making the glyphs available using {@code [+name]} syntax.
     * You can use the name of an emoji, such as {@code [+clown face]}, or equivalently use the actual emoji, such as
     * {@code [+🤡]}, with the latter preferred because the names can be unwieldy or hard to get right. This caches the
     * OpenMoji atlas for later calls. There are two variants on the OpenMoji set here; one is in full color, and the
     * other is line art with white lines (This version can be drawn with a color set using markup or the Batch). This
     * tries to load the files "OpenMoji-color.atlas" and "OpenMoji-color.png" (if loading the full-color set) or
     * "OpenMoji-white.atlas" and "OpenMoji-white.png" (if loading the line art set) from the internal
     * storage first, and if that fails, it tries to load them from local storage in the current working directory.
     * OpenMoji is licensed under Creative Commons-Attribution-Share-Alike, so make sure you attribute
     * the <a href="https://openmoji.org/">OpenMoji project</a>.
     * <br>
     * This set of emoji has a different style than the <a href="https://github.com/jdecked/twemoji/tree/v15.0.3">Twemoji</a>
     * used by {@link #addEmoji(Font)}, with more flat areas of one color, frequent appearances of lines only partly
     * covering the inner color, and very consistent patterns for things like the poses of people in emojis. There's
     * also the white-line-only version you can use here, which has no equivalent in Twemoji. OpenMoji probably don't
     * look quite as good at very small sizes when compared to Twemoji, though.
     * <br>
     * You can add OpenMoji emoji to a font as inline images with {@code KnownFonts.addOpenMoji(myFont, boolean)}.
     * Since TextraTypist 1.0.0, emoji display correctly with standard, SDF, and MSDF fonts, though they always look how
     * they do with standard fonts and don't use any distance field themselves. They can scale reasonably well down, and
     * less-reasonably well up, but at typical text sizes (12-30 pixels in height) they tend to be legible.
     * You can search for names in {@code OpenMoji-color.atlas}, or use the emoji picker in
     * <a href="https://github.com/raeleus/skin-composer">Skin Composer</a> to navigate by category. You can also use
     * the emoji picker present in some OSes, such as how Win+. allows selecting an emoji on Windows 10 and up.
     * Programmatically, you can use {@link Font#nameLookup} to look up the internal {@code char} this uses for a given
     * name or emoji, and {@link Font#namesByCharCode} to go from such an internal code to an emoji (as UTF-8).
     * <br>
     * Note that there isn't enough available space in a Font to add both emoji with this and icons with
     * {@link #addGameIcons(Font)}. You can, however, make two copies of a Font, add emoji to one and icons to the
     * other, and put both in a FontFamily, so you can access both atlases in the same block of text.
     * <br>
     * Preview: <a href="https://tommyettinger.github.io/textratypist/previews/OpenMojiPreview.png">Image link</a> (uses
     * white lines, the font {@link #getInconsolata()}, and {@code [%?whiten]} mode)
     * <br>
     * You can see all emoji and the names they use
     * <a href="https://tommyettinger.github.io/openmoji-atlas/">at this GitHub Pages site</a>.
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenMoji-color.atlas">OpenMoji-color.atlas</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenMoji-color.png">OpenMoji-color.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenMoji-License.txt">OpenMoji-License.txt</a></li>
     *     <li>OR, if {@code color} is false,</li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenMoji-white.atlas">OpenMoji-white.atlas</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenMoji-white.png">OpenMoji-white.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenMoji-License.txt">OpenMoji-License.txt</a></li>
     * </ul>
     *
     * @param changing a Font that will have over 3700 emoji added to it, with more aliases
     * @return {@code changing}, after the emoji atlas has been added
     */
    public static Font addOpenMoji(Font changing, boolean color) {
        return addOpenMoji(changing, color, 0f, 0f, 0f);
    }
    /**
     * Takes a Font and adds the OpenMoji icon set to it, making the glyphs available using {@code [+name]} syntax.
     * You can use the name of an emoji, such as {@code [+clown face]}, or equivalently use the actual emoji, such as
     * {@code [+🤡]}, with the latter preferred because the names can be unwieldy or hard to get right. This caches the
     * OpenMoji atlas for later calls. There are two variants on the OpenMoji set here; one is in full color, and the
     * other is line art with white lines (This version can be drawn with a color set using markup or the Batch). This
     * tries to load the files "OpenMoji-color.atlas" and "OpenMoji-color.png" (if loading the full-color set) or
     * "OpenMoji-white.atlas" and "OpenMoji-white.png" (if loading the line art set) from the internal
     * storage first, and if that fails, it tries to load them from local storage in the current working directory.
     * OpenMoji is licensed under Creative Commons-Attribution-Share-Alike, so make sure you attribute
     * the <a href="https://openmoji.org/">OpenMoji project</a>.
     * <br>
     * This set of emoji has a different style than the <a href="https://github.com/jdecked/twemoji/tree/v15.0.3">Twemoji</a>
     * used by {@link #addEmoji(Font)}, with more flat areas of one color, frequent appearances of lines only partly
     * covering the inner color, and very consistent patterns for things like the poses of people in emojis. There's
     * also the white-line-only version you can use here, which has no equivalent in Twemoji. OpenMoji probably don't
     * look quite as good at very small sizes when compared to Twemoji, though.
     * <br>
     * Since TextraTypist 1.0.0, emoji display correctly with standard, SDF, and MSDF fonts, though they always look how
     * they do with standard fonts and don't use any distance field themselves. They can scale reasonably well down, and
     * less-reasonably well up, but at typical text sizes (12-30 pixels in height) they tend to be legible.
     * You can search for names in {@code OpenMoji-color.atlas}, or use the emoji picker in
     * <a href="https://github.com/raeleus/skin-composer">Skin Composer</a> to navigate by category. You can also use
     * the emoji picker present in some OSes, such as how Win+. allows selecting an emoji on Windows 10 and up.
     * Programmatically, you can use {@link Font#nameLookup} to look up the internal {@code char} this uses for a given
     * name or emoji, and {@link Font#namesByCharCode} to go from such an internal code to an emoji (as UTF-8).
     * <br>
     * Note that there isn't enough available space in a Font to add both emoji with this and icons with
     * {@link #addGameIcons(Font)}. You can, however, make two copies of a Font, add emoji to one and icons to the
     * other, and put both in a FontFamily, so you can access both atlases in the same block of text.
     * <br>
     * This overload allows customizing the x/y offsets and x-advance for every emoji this puts in a Font.
     * <br>
     * Preview: <a href="https://tommyettinger.github.io/textratypist/previews/OpenMojiPreview.png">Image link</a> (uses
     * white lines, the font {@link #getInconsolata()}, and {@code [%?whiten]} mode)
     * <br>
     * You can see all emoji and the names they use
     * <a href="https://tommyettinger.github.io/openmoji-atlas/">at this GitHub Pages site</a>.
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenMoji-color.atlas">OpenMoji-color.atlas</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenMoji-color.png">OpenMoji-color.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenMoji-License.txt">OpenMoji-License.txt</a></li>
     *     <li>OR, if {@code color} is false,</li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenMoji-white.atlas">OpenMoji-white.atlas</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenMoji-white.png">OpenMoji-white.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenMoji-License.txt">OpenMoji-License.txt</a></li>
     * </ul>
     *
     * @param changing a Font that will have over 3700 emoji added to it, with more aliases
     * @param offsetXChange will be added to the {@link Font.GlyphRegion#offsetX} of each added glyph; in practice, positive values push emoji to the right
     * @param offsetYChange will be added to the {@link Font.GlyphRegion#offsetY} of each added glyph; in practice, positive values push emoji down
     * @param xAdvanceChange will be added to the {@link Font.GlyphRegion#xAdvance} of each added glyph; in practice, positive values shrink emoji
     * @return {@code changing}, after the emoji atlas has been added
     */
    public static Font addOpenMoji(Font changing, boolean color, float offsetXChange, float offsetYChange, float xAdvanceChange) {
        return addOpenMoji(changing, color, "", "", offsetXChange, offsetYChange, xAdvanceChange);
    }
    /**
     * Takes a Font and adds the OpenMoji icon set to it, making the glyphs available using {@code [+name]} syntax.
     * You can use the name of an emoji, such as {@code [+clown face]}, or equivalently use the actual emoji, such as
     * {@code [+🤡]}, with the latter preferred because the names can be unwieldy or hard to get right. This caches the
     * OpenMoji atlas for later calls. This tries to load the files "OpenMoji.atlas" and "OpenMoji.png" from the internal
     * storage first, and if that fails, it tries to load them from local storage in the current working directory.
     * There are two variants on the OpenMoji set here; one is in full color, and the
     * other is line art with white lines (This version can be drawn with a color set using markup or the Batch). This
     * tries to load the files "OpenMoji-color.atlas" and "OpenMoji-color.png" (if loading the full-color set) or
     * "OpenMoji-white.atlas" and "OpenMoji-white.png" (if loading the line art set) from the internal
     * storage first, and if that fails, it tries to load them from local storage in the current working directory.
     * OpenMoji is licensed under Creative Commons-Attribution-Share-Alike, so make sure you attribute
     * the <a href="https://openmoji.org/">OpenMoji project</a>.
     * <br>
     * This set of emoji has a different style than the <a href="https://github.com/jdecked/twemoji/tree/v15.0.3">Twemoji</a>
     * used by {@link #addEmoji(Font)}, with more flat areas of one color, frequent appearances of lines only partly
     * covering the inner color, and very consistent patterns for things like the poses of people in emojis. There's
     * also the white-line-only version you can use here, which has no equivalent in Twemoji. OpenMoji probably don't
     * look quite as good at very small sizes when compared to Twemoji, though.
     * <br>
     * Since TextraTypist 1.0.0, emoji display correctly with standard, SDF, and MSDF fonts, though they always look how
     * they do with standard fonts and don't use any distance field themselves. They can scale reasonably well down, and
     * less-reasonably well up, but at typical text sizes (12-30 pixels in height) they tend to be legible.
     * You can search for names in {@code OpenMoji-color.atlas}, or use the emoji picker in
     * <a href="https://github.com/raeleus/skin-composer">Skin Composer</a> to navigate by category. You can also use
     * the emoji picker present in some OSes, such as how Win+. allows selecting an emoji on Windows 10 and up.
     * Programmatically, you can use {@link Font#nameLookup} to look up the internal {@code char} this uses for a given
     * name or emoji, and {@link Font#namesByCharCode} to go from such an internal code to an emoji (as UTF-8).
     * <br>
     * Note that there isn't enough available space in a Font to add both emoji with this and icons with
     * {@link #addGameIcons(Font)}. You can, however, make two copies of a Font, add emoji to one and icons to the
     * other, and put both in a FontFamily, so you can access both atlases in the same block of text.
     * <br>
     * This overload allows customizing the x/y offsets and x-advance for every emoji this puts in a Font. It also
     * allows specifying Strings to prepend before and append after each name in the font, including emoji names.
     * <br>
     * Preview: <a href="https://tommyettinger.github.io/textratypist/previews/OpenMojiPreview.png">Image link</a> (uses
     * white lines, the font {@link #getInconsolata()}, and {@code [%?whiten]} mode)
     * <br>
     * You can see all emoji and the names they use
     * <a href="https://tommyettinger.github.io/openmoji-atlas/">at this GitHub Pages site</a>.
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenMoji-color.atlas">OpenMoji-color.atlas</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenMoji-color.png">OpenMoji-color.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenMoji-License.txt">OpenMoji-License.txt</a></li>
     *     <li>OR, if {@code color} is false,</li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenMoji-white.atlas">OpenMoji-white.atlas</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenMoji-white.png">OpenMoji-white.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenMoji-License.txt">OpenMoji-License.txt</a></li>
     * </ul>
     *
     * @param changing a Font that will have over 3700 emoji added to it, with more aliases
     * @param prepend will be prepended before each name in the atlas; if null, will be treated as ""
     * @param append will be appended after each name in the atlas; if null, will be treated as ""
     * @param offsetXChange will be added to the {@link Font.GlyphRegion#offsetX} of each added glyph; in practice, positive values push emoji to the right
     * @param offsetYChange will be added to the {@link Font.GlyphRegion#offsetY} of each added glyph; in practice, positive values push emoji down
     * @param xAdvanceChange will be added to the {@link Font.GlyphRegion#xAdvance} of each added glyph; in practice, positive values shrink emoji
     * @return {@code changing}, after the emoji atlas has been added
     */
    public static Font addOpenMoji(Font changing, boolean color, String prepend, String append, float offsetXChange, float offsetYChange, float xAdvanceChange) {
        initialize();
        if(color) {
            String baseName = "OpenMoji-color";
            if (instance.openMojiColor == null) {
                try {
                    FileHandle atlas = Gdx.files.internal(instance.prefix + baseName + ".atlas");
                    if (!atlas.exists() && Gdx.files.isLocalStorageAvailable())
                        atlas = Gdx.files.local(instance.prefix + baseName + ".atlas");
                    if (Gdx.files.internal(instance.prefix + baseName + ".png").exists())
                        instance.openMojiColor = loadUnicodeAtlas(atlas, atlas.parent(), false);
                    else if (Gdx.files.isLocalStorageAvailable() && Gdx.files.local(instance.prefix + baseName + ".png").exists())
                        instance.openMojiColor = loadUnicodeAtlas(atlas, atlas.parent(), false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (instance.openMojiColor != null) {
                return changing.addAtlas(instance.openMojiColor, prepend, append,
                        offsetXChange, offsetYChange, xAdvanceChange);
            }
            throw new RuntimeException("Assets '"+baseName+".atlas' and '"+baseName+".png' not found.");
        }
        else {
            String baseName = "OpenMoji-white";
            if (instance.openMojiWhite == null) {
                try {
                    FileHandle atlas = Gdx.files.internal(instance.prefix + baseName + ".atlas");
                    if (!atlas.exists() && Gdx.files.isLocalStorageAvailable())
                        atlas = Gdx.files.local(instance.prefix + baseName + ".atlas");
                    if (Gdx.files.internal(instance.prefix + baseName + ".png").exists())
                        instance.openMojiWhite = loadUnicodeAtlas(atlas, atlas.parent(), false);
                    else if (Gdx.files.isLocalStorageAvailable() && Gdx.files.local(instance.prefix + baseName + ".png").exists())
                        instance.openMojiWhite = loadUnicodeAtlas(atlas, atlas.parent(), false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (instance.openMojiWhite != null) {
                return changing.addAtlas(instance.openMojiWhite, prepend, append,
                        offsetXChange, offsetYChange, xAdvanceChange);
            }
            throw new RuntimeException("Assets '"+baseName+".atlas' and '"+baseName+".png' not found.");
        }
    }
    private TextureAtlas notoEmoji;

    /**
     * Takes a Font and adds the Noto Color Emoji icon set to it, making the glyphs available using {@code [+name]} syntax.
     * You can use the name of an emoji, such as {@code [+clown]}, or equivalently use the actual emoji, such as
     * {@code [+🤡]}, with the latter preferred because the names can be unwieldy or hard to get right. This caches the
     * Noto Color Emoji atlas for later calls. This tries to load the files "Noto-Emoji.atlas" and "Noto-Emoji.png" from
     * the internal storage first using the configured {@link #setAssetPrefix(String) path prefix}, and if that fails,
     * it tries to load them from local storage in the current working directory, also with that path prefix.
     * There are over 3000 emoji in the
     * <a href="https://fonts.google.com/noto/specimen/Noto+Color+Emoji">Noto Color Emoji</a> set; it is licensed as
     * OFL 1.1. There are many additional names (called "shortcodes" by some sources) for various emoji; this uses the
     * <a href="{@literal https://emojibase.dev/shortcodes?filter=&shortcodePresets=emojibase&skinTones=true&genders=true}">EmojiBase</a>
     * set of shortcodes for Unicode 15.1, which is a different set from what services like Slack and Discord use. The
     * names are also different here from the names in {@link #addEmoji(Font)}; these names use underscores to separate
     * words, and don't use commas or other normal-sentence punctuation. Skin tones are available for compatible emoji,
     * and the names for these contain "tone1" for the lightest skin tone through "tone5" for the darkest. Because
     * shortcodes are different for Noto Emoji and the other emoji here (Twemoji and OpenMoji, which use a slightly
     * older version of the standard for emoji), using the syntax with a single emoji, {@code [+👷🏻]}, is preferred.
     * <br>
     * Since TextraTypist 1.0.0, emoji display correctly with standard, SDF, and MSDF fonts, though they always look how
     * they do with standard fonts and don't use any distance field themselves. They can scale reasonably well down, and
     * less-reasonably well up, but at typical text sizes (12-30 pixels in height) they tend to be legible.
     * You can search for names in {@code Noto-Emoji.atlas}, or use the emoji picker in
     * <a href="https://github.com/raeleus/skin-composer">Skin Composer</a> to navigate by category. You can also use
     * the emoji picker present in some OSes, such as how Win+. allows selecting an emoji on Windows 10 and up.
     * Programmatically, you can use {@link Font#nameLookup} to look up the internal {@code char} this uses for a given
     * name or emoji, and {@link Font#namesByCharCode} to go from such an internal code to an emoji (as UTF-8).
     * <br>
     * Note that there isn't enough available space in a Font to add both emoji with this and icons with
     * {@link #addGameIcons(Font)}. You can, however, make two copies of a Font, add emoji to one and icons to the
     * other, and put both in a FontFamily, so you can access both atlases in the same block of text.
     * <br>
     * You can see all emoji and the names they use
     * <a href="https://tommyettinger.github.io/noto-emoji-atlas/">at this GitHub Pages site</a>.
     * <br>
     * Preview: <a href="https://tommyettinger.github.io/textratypist/previews/NotoEmojiPreview.png">Image link</a>
     * (uses the font {@link #getAStarry()} and {@code [%?blacken]} mode)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Noto-Emoji.atlas">Noto-Emoji.atlas</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Noto-Emoji.png">Noto-Emoji.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Noto-Emoji-License.txt">Noto-Emoji-License.txt</a></li>
     * </ul>
     *
     * @param changing a Font that will have over 3000 emoji added to it, with more aliases
     * @return {@code changing}, after the emoji atlas has been added
     */
    public static Font addNotoEmoji(Font changing) {
        return addNotoEmoji(changing, 0f, 0f, 0f);
    }

    /**
     * Takes a Font and adds the Noto Color Emoji icon set to it, making the glyphs available using {@code [+name]} syntax.
     * You can use the name of an emoji, such as {@code [+clown]}, or equivalently use the actual emoji, such as
     * {@code [+🤡]}, with the latter preferred because the names can be unwieldy or hard to get right. This caches the
     * Noto Color Emoji atlas for later calls. This tries to load the files "Noto-Emoji.atlas" and "Noto-Emoji.png" from
     * the internal storage first using the configured {@link #setAssetPrefix(String) path prefix}, and if that fails,
     * it tries to load them from local storage in the current working directory, also with that path prefix.
     * There are over 3000 emoji in the
     * <a href="https://fonts.google.com/noto/specimen/Noto+Color+Emoji">Noto Color Emoji</a> set; it is licensed as
     * OFL 1.1. There are many additional names (called "shortcodes" by some sources) for various emoji; this uses the
     * <a href="{@literal https://emojibase.dev/shortcodes?filter=&shortcodePresets=emojibase&skinTones=true&genders=true}">EmojiBase</a>
     * set of shortcodes for Unicode 15.1, which is a different set from what services like Slack and Discord use. The
     * names are also different here from the names in {@link #addEmoji(Font)}; these names use underscores to separate
     * words, and don't use commas or other normal-sentence punctuation. Skin tones are available for compatible emoji,
     * and the names for these contain "tone1" for the lightest skin tone through "tone5" for the darkest. Because
     * shortcodes are different for Noto Emoji and the other emoji here (Twemoji and OpenMoji, which use a slightly
     * older version of the standard for emoji), using the syntax with a single emoji, {@code [+👷🏻]}, is preferred.
     * <br>
     * Since TextraTypist 1.0.0, emoji display correctly with standard, SDF, and MSDF fonts, though they always look how
     * they do with standard fonts and don't use any distance field themselves. They can scale reasonably well down, and
     * less-reasonably well up, but at typical text sizes (12-30 pixels in height) they tend to be legible.
     * You can search for names in {@code Noto-Emoji.atlas}, or use the emoji picker in
     * <a href="https://github.com/raeleus/skin-composer">Skin Composer</a> to navigate by category. You can also use
     * the emoji picker present in some OSes, such as how Win+. allows selecting an emoji on Windows 10 and up.
     * Programmatically, you can use {@link Font#nameLookup} to look up the internal {@code char} this uses for a given
     * name or emoji, and {@link Font#namesByCharCode} to go from such an internal code to an emoji (as UTF-8).
     * <br>
     * Note that there isn't enough available space in a Font to add both emoji with this and icons with
     * {@link #addGameIcons(Font)}. You can, however, make two copies of a Font, add emoji to one and icons to the
     * other, and put both in a FontFamily, so you can access both atlases in the same block of text.
     * <br>
     * This overload allows customizing the x/y offsets and x-advance for every emoji this puts in a Font.
     * <br>
     * You can see all emoji and the names they use
     * <a href="https://tommyettinger.github.io/noto-emoji-atlas/">at this GitHub Pages site</a>.
     * <br>
     * Preview: <a href="https://tommyettinger.github.io/textratypist/previews/NotoEmojiPreview.png">Image link</a>
     * (uses the font {@link #getAStarry()} and {@code [%?blacken]} mode)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Noto-Emoji.atlas">Noto-Emoji.atlas</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Noto-Emoji.png">Noto-Emoji.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Noto-Emoji-License.txt">Noto-Emoji-License.txt</a></li>
     * </ul>
     *
     * @param changing a Font that will have over 3000 emoji added to it, with more aliases
     * @param offsetXChange will be added to the {@link Font.GlyphRegion#offsetX} of each added glyph; in practice, positive values push emoji to the right
     * @param offsetYChange will be added to the {@link Font.GlyphRegion#offsetY} of each added glyph; in practice, positive values push emoji down
     * @param xAdvanceChange will be added to the {@link Font.GlyphRegion#xAdvance} of each added glyph; in practice, positive values shrink emoji
     * @return {@code changing}, after the emoji atlas has been added
     */
    public static Font addNotoEmoji(Font changing, float offsetXChange, float offsetYChange, float xAdvanceChange) {
        return addNotoEmoji(changing, "", "", offsetXChange, offsetYChange, xAdvanceChange);
    }
    /**
     * Takes a Font and adds the Noto Color Emoji icon set to it, making the glyphs available using {@code [+name]} syntax.
     * You can use the name of an emoji, such as {@code [+clown]}, or equivalently use the actual emoji, such as
     * {@code [+🤡]}, with the latter preferred because the names can be unwieldy or hard to get right. This caches the
     * Noto Color Emoji atlas for later calls. This tries to load the files "Noto-Emoji.atlas" and "Noto-Emoji.png" from
     * the internal storage first using the configured {@link #setAssetPrefix(String) path prefix}, and if that fails,
     * it tries to load them from local storage in the current working directory, also with that path prefix.
     * There are over 3000 emoji in the
     * <a href="https://fonts.google.com/noto/specimen/Noto+Color+Emoji">Noto Color Emoji</a> set; it is licensed as
     * OFL 1.1. There are many additional names (called "shortcodes" by some sources) for various emoji; this uses the
     * <a href="{@literal https://emojibase.dev/shortcodes?filter=&shortcodePresets=emojibase&skinTones=true&genders=true}">EmojiBase</a>
     * set of shortcodes for Unicode 15.1, which is a different set from what services like Slack and Discord use. The
     * names are also different here from the names in {@link #addEmoji(Font)}; these names use underscores to separate
     * words, and don't use commas or other normal-sentence punctuation. Skin tones are available for compatible emoji,
     * and the names for these contain "tone1" for the lightest skin tone through "tone5" for the darkest. Because
     * shortcodes are different for Noto Emoji and the other emoji here (Twemoji and OpenMoji, which use a slightly
     * older version of the standard for emoji), using the syntax with a single emoji, {@code [+👷🏻]}, is preferred.
     * <br>
     * Since TextraTypist 1.0.0, emoji display correctly with standard, SDF, and MSDF fonts, though they always look how
     * they do with standard fonts and don't use any distance field themselves. They can scale reasonably well down, and
     * less-reasonably well up, but at typical text sizes (12-30 pixels in height) they tend to be legible.
     * You can search for names in {@code Noto-Emoji.atlas}, or use the emoji picker in
     * <a href="https://github.com/raeleus/skin-composer">Skin Composer</a> to navigate by category. You can also use
     * the emoji picker present in some OSes, such as how Win+. allows selecting an emoji on Windows 10 and up.
     * Programmatically, you can use {@link Font#nameLookup} to look up the internal {@code char} this uses for a given
     * name or emoji, and {@link Font#namesByCharCode} to go from such an internal code to an emoji (as UTF-8).
     * <br>
     * Note that there isn't enough available space in a Font to add both emoji with this and icons with
     * {@link #addGameIcons(Font)}. You can, however, make two copies of a Font, add emoji to one and icons to the
     * other, and put both in a FontFamily, so you can access both atlases in the same block of text.
     * <br>
     * This overload allows customizing the x/y offsets and x-advance for every emoji this puts in a Font. It also
     * allows specifying Strings to prepend before and append after each name in the font, including emoji names.
     * <br>
     * You can see all emoji and the names they use
     * <a href="https://tommyettinger.github.io/noto-emoji-atlas/">at this GitHub Pages site</a>.
     * <br>
     * Preview: <a href="https://tommyettinger.github.io/textratypist/previews/NotoEmojiPreview.png">Image link</a>
     * (uses the font {@link #getAStarry()} and {@code [%?blacken]} mode)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Noto-Emoji.atlas">Noto-Emoji.atlas</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Noto-Emoji.png">Noto-Emoji.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Noto-Emoji-License.txt">Noto-Emoji-License.txt</a></li>
     * </ul>
     *
     * @param changing a Font that will have over 3000 emoji added to it, with more aliases
     * @param prepend will be prepended before each name in the atlas; if null, will be treated as ""
     * @param append will be appended after each name in the atlas; if null, will be treated as ""
     * @param offsetXChange will be added to the {@link Font.GlyphRegion#offsetX} of each added glyph; in practice, positive values push emoji to the right
     * @param offsetYChange will be added to the {@link Font.GlyphRegion#offsetY} of each added glyph; in practice, positive values push emoji down
     * @param xAdvanceChange will be added to the {@link Font.GlyphRegion#xAdvance} of each added glyph; in practice, positive values shrink emoji
     * @return {@code changing}, after the emoji atlas has been added
     */
    public static Font addNotoEmoji(Font changing, String prepend, String append, float offsetXChange, float offsetYChange, float xAdvanceChange) {
        initialize();
        if (instance.notoEmoji == null) {
            try {
                FileHandle atlas = Gdx.files.internal(instance.prefix + "Noto-Emoji.atlas");
                if (!atlas.exists() && Gdx.files.isLocalStorageAvailable()) atlas = Gdx.files.local(instance.prefix + "Noto-Emoji.atlas");
                if (Gdx.files.internal(instance.prefix + "Noto-Emoji.png").exists())
                    instance.notoEmoji = loadUnicodeAtlas(atlas, atlas.parent(), false);
                else if (Gdx.files.isLocalStorageAvailable() && Gdx.files.local(instance.prefix + "Noto-Emoji.png").exists())
                    instance.notoEmoji = loadUnicodeAtlas(atlas, atlas.parent(), false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance.notoEmoji != null) {
            return changing.addAtlas(instance.notoEmoji, prepend, append,
                    offsetXChange, offsetYChange, xAdvanceChange);
        }
        throw new RuntimeException("Assets 'Noto-Emoji.atlas' and 'Noto-Emoji.png' not found.");
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
     * Since TextraTypist 1.0.0, icons display correctly with standard, SDF, and MSDF fonts, though they always look how
     * they do with standard fonts and don't use any distance field themselves. They can scale reasonably well down, and
     * less-reasonably well up, but at typical text sizes (12-30 pixels in height) they tend to be legible.
     * All icons use only the color white with various levels of transparency, so they can be
     * colored like normal text glyphs. You can search for names in {@code Game-Icons.atlas}.
     * Programmatically, you can use {@link Font#nameLookup} to look up the internal {@code char} this uses for a given
     * name, and {@link Font#namesByCharCode} to go from such an internal code to the corresponding name.
     * <br>
     * Note that there isn't enough available space in a Font to add both emoji with {@link #addEmoji(Font)} and icons
     * with this. You can, however, make two copies of a Font, add emoji to one and icons to the other, and put both in
     * a FontFamily, so you can access both atlases in the same block of text.
     * <br>
     * Preview: <a href="https://tommyettinger.github.io/textratypist/previews/GameIconsPreview.png">Image link</a> (uses
     * the font {@link #getNowAlt()} and {@code [%?blacken]} mode)
     * <br>
     * You can see all icons and the names they use
     * <a href="https://tommyettinger.github.io/game-icons-net-atlas/">at this GitHub Pages site</a>.
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
     * Since TextraTypist 1.0.0, icons display correctly with standard, SDF, and MSDF fonts, though they always look how
     * they do with standard fonts and don't use any distance field themselves. They can scale reasonably well down, and
     * less-reasonably well up, but at typical text sizes (12-30 pixels in height) they tend to be legible.
     * All icons use only the color white with various levels of transparency, so they can be
     * colored like normal text glyphs. You can search for names in {@code Game-Icons.atlas}.
     * Programmatically, you can use {@link Font#nameLookup} to look up the internal {@code char} this uses for a given
     * name, and {@link Font#namesByCharCode} to go from such an internal code to the corresponding name.
     * <br>
     * Note that there isn't enough available space in a Font to add both emoji with {@link #addEmoji(Font)} and icons
     * with this. You can, however, make two copies of a Font, add emoji to one and icons to the other, and put both in
     * a FontFamily, so you can access both atlases in the same block of text.
     * <br>
     * This overload allows customizing the x/y offsets and x-advance for every icon this puts in a Font.
     * <br>
     * Preview: <a href="https://tommyettinger.github.io/textratypist/previews/GameIconsPreview.png">Image link</a> (uses
     * the font {@link #getNowAlt()} and {@code [%?blacken]} mode)
     * <br>
     * You can see all icons and the names they use
     * <a href="https://tommyettinger.github.io/game-icons-net-atlas/">at this GitHub Pages site</a>.
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Game-Icons.atlas">Game-Icons.atlas</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Game-Icons.png">Game-Icons.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Game-Icons-License.txt">Game-Icons-License.txt</a></li>
     * </ul>
     *
     * @param changing a Font that will have over 4000 icons added to it
     * @param offsetXChange will be added to the {@link Font.GlyphRegion#offsetX} of each added glyph; in practice, positive values push icons to the right
     * @param offsetYChange will be added to the {@link Font.GlyphRegion#offsetY} of each added glyph; in practice, positive values push icons down
     * @param xAdvanceChange will be added to the {@link Font.GlyphRegion#xAdvance} of each added glyph; in practice, positive values shrink icons
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
     * Since TextraTypist 1.0.0, icons display correctly with standard, SDF, and MSDF fonts, though they always look how
     * they do with standard fonts and don't use any distance field themselves. They can scale reasonably well down, and
     * less-reasonably well up, but at typical text sizes (12-30 pixels in height) they tend to be legible.
     * All icons use only the color white with various levels of transparency, so they can be
     * colored like normal text glyphs. You can search for names in {@code Game-Icons.atlas}.
     * Programmatically, you can use {@link Font#nameLookup} to look up the internal {@code char} this uses for a given
     * name, and {@link Font#namesByCharCode} to go from such an internal code to the corresponding name.
     * <br>
     * Note that there isn't enough available space in a Font to add both emoji with {@link #addEmoji(Font)} and icons
     * with this. You can, however, make two copies of a Font, add emoji to one and icons to the other, and put both in
     * a FontFamily, so you can access both atlases in the same block of text.
     * <br>
     * This overload allows customizing the x/y offsets and x-advance for every icon this puts in a Font. It also
     * allows specifying Strings to prepend before and append after each name in the font.
     * <br>
     * Preview: <a href="https://tommyettinger.github.io/textratypist/previews/GameIconsPreview.png">Image link</a> (uses
     * the font {@link #getNowAlt()} and {@code [%?blacken]} mode)
     * <br>
     * You can see all icons and the names they use
     * <a href="https://tommyettinger.github.io/game-icons-net-atlas/">at this GitHub Pages site</a>.
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
     * @param offsetXChange will be added to the {@link Font.GlyphRegion#offsetX} of each added glyph; in practice, positive values push icons to the right
     * @param offsetYChange will be added to the {@link Font.GlyphRegion#offsetY} of each added glyph; in practice, positive values push icons down
     * @param xAdvanceChange will be added to the {@link Font.GlyphRegion#xAdvance} of each added glyph; in practice, positive values shrink icons
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
                    offsetXChange - 20f, offsetYChange, xAdvanceChange);
        }
        throw new RuntimeException("Assets 'Game-Icons.atlas' and 'Game-Icons.png' not found.");
    }

    private Font gameIconsFont;

    /**
     * Gets a typically-square Font that is meant to be used in a FontFamily, allowing switching to a Font with the many
     * game-icons.net icons. The base Font this uses is {@link #getAStarry()}, because it is perfectly square by
     * default, and this needs all of A-Starry's assets. It also needs the assets for {@link #addGameIcons(Font)} to be
     * present, since those will be available with this Font. The name this will use in a FontFamily is "Icons". You can
     * specify the width and height you want for the icons; typically they are the same, because the icons here are
     * square, and you probably want the height to match the line height for your main font. It isn't expected that
     * many users would want to use the non-icon glyphs in the font. The reason this is needed is that you can't fit
     * both the emoji from {@link #addEmoji(Font)} and the icons from {@link #addGameIcons(Font)} in one Font, but you
     * can swap between two different Fonts in a FontFamily, one with emoji and one with icons.
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-standard.dat">A-Starry-standard.dat</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-standard.png">A-Starry-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/A-Starry-License.txt">A-Starry-License.txt</a></li>
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
     * This returns a mix of standard, SDF, and MSDF fonts; some can be scaled by fractional metrics, and some are pixel
     * fonts that should generally be scaled only by integers.
     * @return a new array containing all Font instances this knows
     */
    public static Font[] getAll() {
        return new Font[]{getAStarry(), getAStarryMSDF(), getAStarryTall(), getBirdlandAeroplane(), getBitter(),
                getCanada(), getCascadiaMono(), getCascadiaMonoMSDF(), getCaveat(), getComputerSaysNo(), getCozette(),
                getDejaVuSans(), getDejaVuSansCondensed(), getDejaVuSansMono(), getDejaVuSerif(),
                getDejaVuSerifCondensed(), getGentium(), getGentiumMSDF(), getGentiumSDF(), getGentiumUnItalic(),
                getGlacialIndifference(), getGoNotoUniversal(), getGoNotoUniversalSDF(), getGrenze(), getHanazono(),
                getIBM8x16(), getInconsolata(), getInconsolataMSDF(), getIosevka(), getIosevkaMSDF(), getIosevkaSDF(),
                getIosevkaSlab(), getIosevkaSlabMSDF(), getIosevkaSlabSDF(), getKingthingsFoundation(),
                getKingthingsPetrock(), getLanaPixel(), getLibertinusSerif(), getLibertinusSerifSemibold(),
                getNowAlt(), getOpenSans(), getOstrichBlack(), getOverlock(), getOverlockUnItalic(), getOxanium(),
                getQuanPixel(), getRobotoCondensed(), getSelawik(), getTangerine(), getTangerineSDF(),
                getYanoneKaffeesatz(), getYanoneKaffeesatzMSDF(), getYataghan(), getYataghanMSDF()};
    }

    /**
     * Returns a new array of Font instances, calling {@link #getFont(String, DistanceFieldType)} on every font name
     * in {@link #STANDARD_NAMES} with {@link DistanceFieldType#STANDARD} as the distance field type (meaning no
     * distance field effect will be used). This uses the more specific configuration in methods like
     * {@link #getCozette()} for any .fnt or .font fonts it has to load, as well as for {@link #getAStarryTall()}.
     * <br>
     * This will only function at all if all the assets (for every known standard Font) are present and load-able.
     * You should store the result of this method, rather than calling it often, because each call copies many Fonts.
     * @return a new array containing all non-distance-field Font instances this knows
     */
    public static Font[] getAllStandard() {
        Font[] found = new Font[STANDARD_NAMES.size+1];
        int i = 0;
        // Structured JSON format
        for(String name : JSON_NAMES){
            found[i++] = getFont(name, STANDARD);
        }
        // special JSON config
        found[i++] = getAStarryTall();
        // AngelCode BMFont format
        found[i++] = getCozette();
        found[i++] = getHanazono();
        found[i++] = getLanaPixel();
        found[i++] = getQuanPixel();
        // SadConsole format
        found[i++] = getIBM8x16();
        return found;
    }

    /**
     * Returns a Font ({@link #getGentium()}) with a FontFamily configured so that 15 non-distance-field Fonts can be
     * used with syntax like {@code [@Sans]}. The names this supports can be accessed with code using
     * {@code getStandardFamily().family.fontAliases.keys()}. These names are:
     * <ul>
     *     <li>{@code Serif}, which is {@link #getGentium()},</li>
     *     <li>{@code Sans}, which is {@link #getOpenSans()},</li>
     *     <li>{@code Mono}, which is {@link #getInconsolata()},</li>
     *     <li>{@code Condensed}, which is {@link #getRobotoCondensed()},</li>
     *     <li>{@code Humanist}, which is {@link #getYanoneKaffeesatz()},</li>
     *     <li>{@code Retro}, which is {@link #getIBM8x16()},</li>
     *     <li>{@code Slab}, which is {@link #getIosevkaSlab()},</li>
     *     <li>{@code Handwriting}, which is {@link #getCaveat()},</li>
     *     <li>{@code Dark}, which is {@link #getGrenze()},</li>
     *     <li>{@code Cozette}, which is {@link #getCozette()},</li>
     *     <li>{@code Iosevka}, which is {@link #getIosevka()},</li>
     *     <li>{@code Medieval}, which is {@link #getKingthingsFoundation()},</li>
     *     <li>{@code Future}, which is {@link #getOxanium()},</li>
     *     <li>{@code Console}, which is {@link #getAStarryTall()},</li>
     *     <li>{@code Code}, which is {@link #getCascadiaMono()}, and</li>
     *     <li>{@code Geometric}, which is {@link #getNowAlt()}.</li>
     * </ul>
     * You can also always use the full name of one of these fonts, which can be obtained using {@link Font#getName()}.
     * {@code Serif}, which is {@link #getGentium()}, will always be the default font used after a reset. For
     * backwards compatibility, {@code Bitter} is an alias for {@link #getGentium()} (not {@link #getBitter()}), because
     * Bitter and Gentium look very similar and because a slot was needed. Similarly, {@code Canada} is an alias for
     * {@link #getNowAlt()} (not {@link #getCanada()}), because they're both geometric heavier-weight typefaces, and
     * as before, a slot was needed.
     * <br>
     * This will only function at all if all the assets (for every listed Font) are present and load-able.
     * You should store the result of this method, rather than calling it often, because each call copies many Fonts.
     * @return a Font that can switch between 16 different Fonts in its FontFamily, to any of several Fonts this knows
     */
    public static Font getStandardFamily() {
        Font.FontFamily family = new Font.FontFamily(
                new String[]{"Serif", "Sans", "Mono", "Condensed", "Humanist",
                        "Retro", "Slab", "Handwriting", "Dark", "Cozette", "Iosevka",
                        "Medieval", "Future", "Console", "Code", "Geometric"},
                new Font[]{getGentium(), getOpenSans(), getInconsolata(), getRobotoCondensed(), getYanoneKaffeesatz(),
                        getIBM8x16(), getIosevkaSlab(), getCaveat(), getGrenze(), getCozette(), getIosevka(),
                        getKingthingsFoundation(), getOxanium(), getAStarryTall(), getCascadiaMono(), getNowAlt()});
        family.fontAliases.put("Bitter", 0); // for compatibility; Bitter and Gentium look nearly identical anyway...
        family.fontAliases.put("Canada", 15); // Canada1500 is... sort-of close... to Now Alt...
        return family.connected[0].setFamily(family);
    }


    /**
     * Returns a Font ({@link #getGentium()}) with a FontFamily configured so that 15 Fonts with the given
     * {@link DistanceFieldType} can be used with syntax like {@code [@Sans]}. The names this supports can be accessed
     * with code using {@code getFamily(dft).family.fontAliases.keys()}. These names are:
     * <ul>
     *     <li>{@code Serif}, which is {@link #getGentium(DistanceFieldType)},</li>
     *     <li>{@code Sans}, which is {@link #getOpenSans(DistanceFieldType)},</li>
     *     <li>{@code Mono}, which is {@link #getInconsolata(DistanceFieldType)},</li>
     *     <li>{@code Condensed}, which is {@link #getRobotoCondensed(DistanceFieldType)},</li>
     *     <li>{@code Humanist}, which is {@link #getYanoneKaffeesatz(DistanceFieldType)},</li>
     *     <li>{@code Fantasy}, which is {@link #getGentiumUnItalic(DistanceFieldType)},</li>
     *     <li>{@code Slab}, which is {@link #getIosevkaSlab(DistanceFieldType)},</li>
     *     <li>{@code Handwriting}, which is {@link #getCaveat(DistanceFieldType)},</li>
     *     <li>{@code Dark}, which is {@link #getGrenze(DistanceFieldType)},</li>
     *     <li>{@code Script}, which is {@link #getTangerine(DistanceFieldType)},</li>
     *     <li>{@code Iosevka}, which is {@link #getIosevka(DistanceFieldType)},</li>
     *     <li>{@code Medieval}, which is {@link #getKingthingsFoundation(DistanceFieldType)},</li>
     *     <li>{@code Future}, which is {@link #getOxanium(DistanceFieldType)},</li>
     *     <li>{@code Console}, which is {@link #getAStarryTall(DistanceFieldType)},</li>
     *     <li>{@code Code}, which is {@link #getCascadiaMono(DistanceFieldType)}, and</li>
     *     <li>{@code Geometric}, which is {@link #getNowAlt(DistanceFieldType)}.</li>
     * </ul>
     * You can also always use the full name of one of these fonts, which can be obtained using {@link Font#getName()}.
     * {@code Serif}, which is {@link #getGentium(DistanceFieldType)}, will always be the default font used after a reset. For
     * backwards compatibility, {@code Bitter} is an alias for {@link #getGentium(DistanceFieldType)} (not {@link #getBitter(DistanceFieldType)}), because
     * Bitter and Gentium look very similar and because a slot was needed. Similarly, {@code Canada} is an alias for
     * {@link #getNowAlt(DistanceFieldType)} (not {@link #getCanada(DistanceFieldType)}), because they're both geometric heavier-weight typefaces, and
     * as before, a slot was needed. Both "Retro" and "Cozette" are aliases for {@link #getAStarryTall(DistanceFieldType)}, because those aliases
     * have a meaning in {@link #getStandardFamily()}, and A Starry Tall is suitable for similar tasks.
     * <br>
     * This will only function at all if all the assets (for every listed Font, with the given distance field type) are present and load-able.
     * You should store the result of this method, rather than calling it often, because each call copies many Fonts.
     * @return a Font that can switch between 16 different Fonts in its FontFamily, to any of several Fonts this knows
     */
    public static Font getFamily(DistanceFieldType dft) {
        Font.FontFamily family = new Font.FontFamily(
                new String[]{"Serif", "Sans", "Mono", "Condensed", "Humanist",
                        "Fantasy", "Slab", "Handwriting", "Dark", "Script", "Iosevka",
                        "Medieval", "Future", "Console", "Code", "Geometric"},
                new Font[]{getGentium(dft), getOpenSans(dft), getInconsolata(dft), getRobotoCondensed(dft), getYanoneKaffeesatz(dft),
                        getGentiumUnItalic(dft), getIosevkaSlab(dft), getCaveat(dft), getGrenze(dft), getTangerine(dft), getIosevka(dft),
                        getKingthingsFoundation(dft), getOxanium(dft), getAStarryTall(dft), getCascadiaMono(dft), getNowAlt(dft)});
        family.fontAliases.put("Bitter", 0); // for compatibility; Bitter and Gentium look nearly identical anyway...
        family.fontAliases.put("Canada", 15); // Canada1500 is... sort-of close... to Now Alt...
        family.fontAliases.put("Retro", 13); // A Starry Tall is similar to IBM 8x16.
        family.fontAliases.put("Cozette", 13); // A Starry Tall is similar to Cozette.
        return family.connected[0].setFamily(family);
    }

    /**
     * Returns a new array of Font instances, calling {@link #getFont(String, DistanceFieldType)} on every font name
     * in {@link #STANDARD_NAMES} with {@link DistanceFieldType#SDF} as the distance field type. This uses the more
     * specific configuration in {@link #getAStarryTall(DistanceFieldType)}.
     * <br>
     * This will only function at all if all the assets (for every known SDF Font) are present and load-able.
     * You should store the result of this method, rather than calling it often, because each call copies many Fonts.
     * @return a new array containing all SDF Font instances this knows
     */
    public static Font[] getAllSDF() {
        Font[] found = new Font[SDF_NAMES.size+1];
        int i = 0;
        // Structured JSON format
        for(String name : SDF_NAMES){
            found[i++] = getFont(name, SDF);
        }
        // special JSON config
        found[i++] = getAStarryTall(SDF);
        return found;
    }

    /**
     * Returns a new array of Font instances, calling {@link #getFont(String, DistanceFieldType)} on every font name
     * in {@link #STANDARD_NAMES} with {@link DistanceFieldType#MSDF} as the distance field type. This uses the more
     * specific configuration in {@link #getAStarryTall(DistanceFieldType)}.
     * <br>
     * This will only function at all if all the assets (for every known MSDF Font) are present and load-able.
     * You should store the result of this method, rather than calling it often, because each call copies many Fonts.
     * @return a new array containing all MSDF Font instances this knows
     */
    public static Font[] getAllMSDF() {
        Font[] found = new Font[MSDF_NAMES.size+1];
        int i = 0;
        // Structured JSON format
        for(String name : MSDF_NAMES){
            found[i++] = getFont(name, MSDF);
        }
        // special JSON config
        found[i++] = getAStarryTall(MSDF);
        return found;
    }

    @Override
    public void dispose() {
        for(Font f : loaded.values()){
            f.dispose();
        }
        loaded.clear();

        if(twemoji != null) {
            twemoji.dispose();
            twemoji = null;
        }
        if(gameIcons != null) {
            gameIcons.dispose();
            gameIcons = null;
        }
        if(openMojiColor != null) {
            openMojiColor.dispose();
            openMojiColor = null;
        }
        if(openMojiWhite != null) {
            openMojiWhite.dispose();
            openMojiWhite = null;
        }
        if(notoEmoji != null) {
            notoEmoji.dispose();
            notoEmoji = null;
        }
        if(gameIconsFont != null) {
            gameIconsFont.dispose();
            gameIconsFont = null;
        }
    }
}