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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.compression.Lzma;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.textra.utils.*;
import regexodus.Category;
import regexodus.Pattern;
import regexodus.Replacer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.IdentityHashMap;

/**
 * A replacement for libGDX's BitmapFont class, supporting additional markup to allow styling text with various effects.
 * This includes the commonly-requested "faux bold" and oblique mode using one font image; you don't need a bold and
 * italic/oblique image separate from the book face. This also supports underline, strikethrough, subscript/superscript
 * (and "midscript," for a height between the two), color markup, scale/size markup, and the option to switch to other
 * Fonts from a family of several. You can add images from a {@link TextureAtlas} to any Font, which is commonly used
 * with emoji (see {@link KnownFonts#addEmoji(Font)}, which calls {@link #addAtlas(TextureAtlas)} here).
 * <br>
 * A Font represents either one size of a "standard" bitmap font (which can be drawn scaled up or down), or many sizes
 * of a distance field font (using either the commonly-used SDF format or newer MSDF format). The same class is used for
 * standard, SDF, and MSDF fonts, but you call {@link #enableShader(Batch)} before rendering with SDF or MSDF fonts, and
 * can switch back to a normal SpriteBatch shader with {@code batch.setShader(null);}. The {@link TextraLabel} and
 * {@link TypingLabel} classes handle the calls to enableShader() for you. You don't have to use SDF or MSDF
 * fonts, but they can scale more cleanly. MSDF looks the sharpest in general, but uses larger-sized PNG files, while
 * SDF typically looks the same as MSDF at all but the largest sizes, but enabled either the standard smooth lines or an
 * {@link DistanceFieldType#SDF_OUTLINE outlined mode} to be used. "Standard" fonts look the best when mixing different
 * Fonts in one piece of text. Standard fonts usually need the smallest PNG files, SDF in second, and MSDF thge largest.
 * <br>
 * You can generate SDF fonts with Hiero or
 * <a href="https://github.com/libgdx/libgdx/wiki/Distance-field-fonts#using-distance-fields-for-arbitrary-images">a related tool</a>
 * that is part of libGDX; MSDF fonts need a different tool, like
 * <a href="https://github.com/maltaisn/msdf-gdx-gen">maltaisn's msdf-gdx-gen</a> (decent for fonts with few chars) or
 * <a href="https://github.com/tommyettinger/fontwriter">fontwriter</a> (recommended, but Windows-only). Using
 * fontwriter means you get Structured JSON Font files instead of AngelCode BMFont files; these can be loaded with
 * {@link #Font(String, TextureRegion, float, float, float, float, boolean, boolean)}. Structured JSON Fonts can be SDF,
 * MSDF, standard (no distance field), or some other kinds of font; the information about this is stored in the font.
 * You can also load a libGDX BitmapFont with a Structured JSON Font using the {@link BitmapFontSupport} class.
 * Structured JSON files are often compressed because they have a repetitive structure that shrinks greatly when
 * compressed. The earliest method for this produced ".dat" fonts using {@link LZBCompression}, but better compression
 * is now available using the libGDX built-in {@link Lzma} code (usually in the form of ".json.lzma" files). A handy
 * quirk of these files is that some general-use decompression programs, like 7-zip, can turn a ".json.lzma" file into
 * its decompressed, readable ".json" format just by extracting that ".lzma" file as if it is an archive.
 * If you want a .fnt file instead for an MSDF font, msdf-gdx-gen does work, but has an uneven baseline if you put too
 * many chars in a font, or use too small of a texture size.
 * <br>
 * This interacts with the {@link Layout} class, with a Layout referencing a Font, and various methods in Font taking
 * a Layout. You usually want to have a Layout for any text you draw repeatedly, and draw that Layout each frame with
 * {@link #drawGlyphs(Batch, Layout, float, float, int)} or a similar method.
 * <br>
 * The {@link TypingLabel} class has its own markup that generally has an equivalent for all the markup options here.
 * This class has some special behavior for both the square-bracket markup used here and the curly-bracket markup used
 * by TypingLabel, even if the markup isn't actually used in a TypingLabel. In particular, anything in curly brackets is
 * ignored and left alone when markup() is called, and is not considered part of the size of the Layout. Anything inside
 * single curly braces is not rendered here, though it may be interpreted by TypingLabel if you use a Layout this
 * produces there. You should escape both square brackets with <code>[[</code> and curly braces with <code>{{</code> if
 * you intend them to appear. You can change the behavior of curly braces by setting {@link #omitCurlyBraces} to false;
 * this will make curly braces and their contents parse as normal text (the default setting is true). If curly braces
 * have a special meaning in some other markup you use, like I18N bundles, you can change a curly-brace tag such as
 * <code>{RESET}</code> to <code>[-RESET]</code>, with the {@code -} at the start making it ignored by markup here but
 * usable to TypingLabel.
 * <br>
 * Most things this can draw can be drawn with a rotation, and usually an origin can be specified (where it makes
 * sense). The rotation can't be configured from markup, but the widgets that understand this class, like
 * {@link TextraLabel} and {@link TypingLabel}, can have their rotation set using the standard scene2d.ui method
 * {@link com.badlogic.gdx.scenes.scene2d.Actor#setRotation(float)}, and then they will request the correct rotation
 * from this class. This is different from {@link com.badlogic.gdx.scenes.scene2d.ui.Label}, which ignores rotation!
 * <br>
 * There are some features here that cannot be used purely from markup, such as per-character rotation and smooth
 * scaling, but these can be used by TypingLabel and its Effect assortment.
 *
 * @see #markup(String, Layout) The markup() method's documentation covers all the markup tags.
 */
public class Font implements Disposable {
    /**
     * Given a partial filename (which can have an internal path), this checks the range of possible file
     * extensions that TextraTypist understands for fonts, and returns {@code jsonName} with one of the five possible
     * extensions added if a file by that name exists as an internal asset. This tries the extensions {@code .ubj.lzma},
     * {@code .json.lzma}, {@code .ubj}, {@code .dat}, and then {@code .json}, in order. If no such file exists, this
     * throws a {@link GdxRuntimeException}.
     * @param jsonName a partial filename that can contain an internal path but does not contain an extension
     * @return a complete filename with an extension, for a file that does exist
     * @throws GdxRuntimeException if no file was found with an appropriate extension
     */
    public static String getJsonExtension(String jsonName) {
        if ((Gdx.files.internal(jsonName + ".ubj.lzma")).exists())
            return jsonName + ".ubj.lzma";
        if ((Gdx.files.internal(jsonName + ".json.lzma")).exists())
            return jsonName + ".json.lzma";
        if ((Gdx.files.internal(jsonName + ".ubj")).exists())
            return jsonName + ".ubj";
        if ((Gdx.files.internal(jsonName + ".dat")).exists())
            return jsonName + ".dat";
        if ((Gdx.files.internal(jsonName + ".json")).exists())
            return jsonName + ".json";
        throw new GdxRuntimeException("No file was found with an appropriate extension appended to " + jsonName);
    }

    /**
     * Describes the region of a glyph in a larger TextureRegion, carrying a little more info about the offsets that
     * apply to where the glyph is rendered.
     */
    public static class GlyphRegion extends TextureRegion {
        /**
         * The offset from the left of the original image to the left of the packed image, after whitespace was removed
         * for packing.
         */
        public float offsetX;

        /**
         * The offset from the bottom of the original image to the bottom of the packed image, after whitespace was
         * removed for packing.
         */
        public float offsetY;

        /**
         * How far to move the "cursor" to the right after drawing this GlyphRegion. Uses the same unit as
         * {@link #offsetX}.
         */
        public float xAdvance;

        /**
         * Creates a GlyphRegion from a parent TextureRegion (typically from an atlas). The resulting GlyphRegion will
         * have 0 offsetX, 0 offsetY, and xAdvance equal to {@link TextureRegion#getRegionWidth()}.
         *
         * @param textureRegion a TextureRegion to draw for this GlyphRegion, typically from a TextureAtlas
         */
        public GlyphRegion(TextureRegion textureRegion) {
            this(textureRegion, 0f, 0f, textureRegion.getRegionWidth());
        }

        /**
         * Creates a GlyphRegion from a parent TextureAtlas.AtlasRegion (almost always from an atlas). The resulting
         * GlyphRegion will have the same offsetX and offsetY as atlasRegion, and xAdvance equal to
         * {@link TextureAtlas.AtlasRegion#originalWidth}.
         *
         * @param atlasRegion a TextureAtlas.AtlasRegion to draw for this GlyphRegion, typically from a TextureAtlas
         */
        public GlyphRegion(TextureAtlas.AtlasRegion atlasRegion) {
            this(atlasRegion, atlasRegion.offsetX, atlasRegion.offsetY, atlasRegion.originalWidth);
        }

        /**
         * Creates a GlyphRegion from a parent TextureRegion (typically from an atlas), along with any offsets to use
         * for its x and y coordinates, and the amount of horizontal space to move over when this is drawn.
         *
         * @param textureRegion a TextureRegion to draw for this GlyphRegion, typically from a TextureAtlas
         * @param offsetX how many pixels to shift over the TextureRegion when drawn; positive is to the right
         * @param offsetY how many pixels to shift over the TextureRegion when drawn; positive is upwards
         * @param xAdvance how much horizontal space the GlyphRegion should use up when drawn
         */
        public GlyphRegion(TextureRegion textureRegion, float offsetX, float offsetY, float xAdvance) {
            super();
            if(textureRegion.getTexture() != null)
                setRegion(textureRegion);
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.xAdvance = xAdvance;
        }

        /**
         * Creates a GlyphRegion from a parent TextureRegion (typically from an atlas), along with the lower-left x and
         * y coordinates, the width, and the height of the GlyphRegion.
         *
         * @param textureRegion a TextureRegion, typically from a TextureAtlas
         * @param x             the x-coordinate of the left side of the texture, in pixels
         * @param y             the y-coordinate of the lower side of the texture, in pixels
         * @param width         the width of the GlyphRegion, in pixels
         * @param height        the height of the GlyphRegion, in pixels
         */
        public GlyphRegion(TextureRegion textureRegion, float x, float y, float width, float height) {
            super();
            if(textureRegion.getTexture() != null)
                setRegion(textureRegion, Math.round(x), Math.round(y), Math.round(width), Math.round(height));
            offsetX = 0f;
            offsetY = 0f;
            xAdvance = width;
        }

        /**
         * Copies another GlyphRegion.
         *
         * @param other the other GlyphRegion to copy
         */
        public GlyphRegion(GlyphRegion other) {
            super();
            if(other.getTexture() != null)
                setRegion(other);
            offsetX = other.offsetX;
            offsetY = other.offsetY;
            xAdvance = other.xAdvance;
        }

        /**
         * Flips the region, adjusting the offset so the image appears to be flipped as if no whitespace has been
         * removed for packing.
         *
         * @param x true if this should flip x to be -x
         * @param y true if this should flip y to be -y
         */
        @Override
        public void flip(boolean x, boolean y) {
            super.flip(x, y);
            if (x) {
                offsetX = -offsetX;
                xAdvance = -xAdvance;
            }
            if (y) offsetY = -offsetY;
        }

        public float getMaxDimension() {
            return Math.max(getRegionWidth(), getRegionHeight());
        }
    }

    /**
     * Describes the region of a glyph in a hypothetical larger Texture that most likely does not exist here.
     * This is meant to replace {@link TextureRegion} when {@link #canUseTextures} is false. It knows the x,y position
     * that a normal TextureRegion would start drawing with, as well as the width and height of that region, but it
     * cannot generally draw anything.
     */
    public static class TexturelessRegion extends TextureRegion {
        public int x, y, width, height;

        public TexturelessRegion() {
            super();
        }

        public TexturelessRegion(Texture texture) {
            super();
            setRegion(0, 0, 0, 0);
        }

        public TexturelessRegion(Texture texture, int width, int height) {
            super();
            setRegion(0, 0, width, height);
        }

        public TexturelessRegion(Texture texture, int x, int y, int width, int height) {
            super();
            setRegion(x, y, width, height);
        }

        public TexturelessRegion(Texture texture, float u, float v, float u2, float v2) {
            this(texture);
        }

        public TexturelessRegion(TextureRegion region) {
            super(region);
        }

        public TexturelessRegion(TextureRegion region, int x, int y, int width, int height) {
            super();
            setRegion(x, y, width, height);
        }

        @Override
        public void setRegion(Texture texture) {
        }

        @Override
        public void setRegion(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        @Override
        public void setRegion(float u, float v, float u2, float v2) {
        }

        @Override
        public void setRegion(TextureRegion region) {
        }

        @Override
        public void setRegion(TextureRegion region, int x, int y, int width, int height) {
            this.setRegion(x, y, width, height);
        }

        @Override
        public int getRegionX() {
            return this.x;
        }

        @Override
        public void setRegionX(int x) {
            this.x = x;
        }

        @Override
        public int getRegionY() {
            return y;
        }

        @Override
        public void setRegionY(int y) {
            this.y = y;
        }

        @Override
        public int getRegionWidth() {
            return width;
        }

        @Override
        public void setRegionWidth(int width) {
            this.width = width;
        }

        @Override
        public int getRegionHeight() {
            return height;
        }

        @Override
        public void setRegionHeight(int height) {
            this.height = height;
        }

        @Override
        public void flip(boolean x, boolean y) {
            if(x) width = -width;
            if(y) height = -height;
        }

        @Override
        public boolean isFlipX() {
            return width < 0;
        }

        @Override
        public boolean isFlipY() {
            return height < 0;
        }
    }

    public static class TexturelessAtlasRegion extends TextureAtlas.AtlasRegion {
        public int x, y, width, height;

        protected TexturelessAtlasRegion(Texture texture, int x, int y, int width, int height) throws NullPointerException {
            super(null, x, y, width, height);
        }

        public static TexturelessAtlasRegion make(int x, int y, int width, int height){
            TexturelessAtlasRegion tar = null;
            try {
                tar = new TexturelessAtlasRegion(null, x, y, width, height);
            } catch (NullPointerException ignored) {
            }
            tar.setRegion(x, y, width, height);

            return tar;
        }

        @Override
        public void setRegion(Texture texture) {
        }

        @Override
        public void setRegion(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        @Override
        public void setRegion(float u, float v, float u2, float v2) {
        }

        @Override
        public void setRegion(TextureRegion region) {
        }

        @Override
        public void setRegion(TextureRegion region, int x, int y, int width, int height) {
            this.setRegion(x, y, width, height);
        }

        @Override
        public int getRegionX() {
            return this.x;
        }

        @Override
        public void setRegionX(int x) {
            this.x = x;
        }

        @Override
        public int getRegionY() {
            return y;
        }

        @Override
        public void setRegionY(int y) {
            this.y = y;
        }

        @Override
        public int getRegionWidth() {
            return width;
        }

        @Override
        public void setRegionWidth(int width) {
            this.width = width;
        }

        @Override
        public int getRegionHeight() {
            return height;
        }

        @Override
        public void setRegionHeight(int height) {
            this.height = height;
        }

        @Override
        public void flip(boolean x, boolean y) {
            if(x) width = -width;
            if(y) height = -height;
        }

        @Override
        public boolean isFlipX() {
            return width < 0;
        }

        @Override
        public boolean isFlipY() {
            return height < 0;
        }

    }

    /**
     * Holds up to 16 Font values, accessible by index or by name, that markup can switch between while rendering.
     * This uses the [@Name] syntax. It is suggested that multiple Font objects share the same FontFamily so users can
     * have the same names mean the same fonts reliably.
     */
    public static class FontFamily {
        /**
         * Stores this Font and up to 15 other connected Fonts that can be switched between using [@Name] syntax.
         * If an item is null and this tries to switch to it, the font does not change.
         */
        public final Font[] connected = new Font[16];

        /**
         * Stores the names of Fonts (or aliases for those Fonts) as keys, mapped to ints between 0 and 15 inclusive.
         * The int values that this map keeps are stored in long glyphs and looked up as indices in {@link #connected}.
         * This map is case-insensitive when comparing keys or getting their values.
         */
        public final CaseInsensitiveIntMap fontAliases = new CaseInsensitiveIntMap(48);

        /**
         * Creates a FontFamily that only allows staying on the same font, unless later configured otherwise.
         */
        public FontFamily() {
        }

        /**
         * Creates a FontFamily given an array of Font values, using the {@link Font#name} of each Font as its alias.
         * This allows switching to different fonts using the [@Name] syntax. This also registers aliases for the
         * Strings "0" through up to "15" to refer to the Font values with the same indices (it can register fewer
         * aliases than up to "15" if there are fewer than 16 Fonts). You should avoid using more than 16 fonts here.
         *
         * @param fonts a non-null array of Font values that should each have their name set (as by {@link #setName(String)}
         */
        public FontFamily(Font[] fonts) {
            this(fonts, 0, fonts.length);
        }

        /**
         * Creates a FontFamily given an array of Font values that and offset/length values for those arrays (allowing
         * {@link Array} to sometimes be used to get the items for fonts). This uses the {@link Font#name} of each Font
         * as its alias. This allows switching to different fonts using the [@Name] syntax. This registers aliases for
         * the Strings "0" through up to "15" to refer to the Font values with the same indices (it can register fewer
         * aliases than up to "15" if there are fewer than 16 Fonts). You should avoid using more than 16 fonts here.
         *
         * @param fonts  an array of Font values that should have the same length as aliases (no more than 16)
         * @param offset where to start accessing fonts, as a non-negative index
         * @param length how many items to use from fonts, if that many are provided
         */
        public FontFamily(Font[] fonts, int offset, int length) {
            if (fonts == null || fonts.length == 0) return;
            for (int i = offset, a = 0; i < length && i < fonts.length; i++, a++) {
                if (fonts[i] == null) continue;
                connected[a & 15] = fonts[i];
                if (fonts[i].name != null)
                    fontAliases.put(fonts[i].name, a & 15);
                fontAliases.put(String.valueOf(a & 15), a & 15);
            }
        }

        /**
         * Creates a FontFamily given an array of String names and a (almost-always same-sized) array of Font values
         * that those names will refer to. This allows switching to different fonts using the [@Name] syntax. This
         * registers aliases for the Strings "0" through up to "15" to refer to the Font values with the same indices
         * (it can register fewer aliases than up to "15" if there are fewer than 16 Fonts). It also registers the
         * {@link Font#name} of each Font as an alias. You should avoid using more than 16 fonts here. You should avoid
         * using more than 16 fonts with this.
         *
         * @param aliases a non-null array of up to 16 String names to use for fonts (individual items may be null)
         * @param fonts   a non-null array of Font values that should have the same length as aliases (no more than 16)
         */
        public FontFamily(String[] aliases, Font[] fonts) {
            this(aliases, fonts, 0, Math.min(aliases.length, fonts.length));
        }

        /**
         * Creates a FontFamily given an array of String names, a (almost-always same-sized) array of Font values that
         * those names will refer to, and offset/length values for those arrays (allowing {@link Array} to sometimes be
         * used to get the items for aliases and fonts). This allows switching to different fonts using the [@Name]
         * syntax. This registers aliases for the Strings "0" through up to "15" to refer to the Font values with the
         * same indices (it can register fewer aliases than up to "15" if there are fewer than 16 Fonts). It also
         * registers the {@link Font#name} of each Font as an alias. You should avoid using more than 16 fonts here.
         *
         * @param aliases an array of up to 16 String names to use for fonts (individual items may be null)
         * @param fonts   an array of Font values that should have the same length as aliases (no more than 16)
         * @param offset  where to start accessing aliases and fonts, as a non-negative index
         * @param length  how many items to use from aliases and fonts, if that many are provided
         */
        public FontFamily(String[] aliases, Font[] fonts, int offset, int length) {
            if (aliases == null || fonts == null || (aliases.length & fonts.length) == 0) return;
            for (int i = offset, a = 0; i < length && i < aliases.length && i < fonts.length; i++, a++) {
                if (fonts[i] == null) continue;
                connected[a & 15] = fonts[i];
                fontAliases.put(aliases[i], a & 15);
                if (fonts[i].name != null)
                    fontAliases.put(fonts[i].name, a & 15);
                fontAliases.put(String.valueOf(a & 15), a & 15);
            }
        }

        /**
         * Constructs a FontFamily given an OrderedMap of String keys (names of Fonts) to Font values (the Fonts that
         * can be switched between). This registers the Strings "0" up to "15" to be aliases for the Fonts with those
         * indices in the map. It also registers the {@link Font#name} of each Font as an alias. This only uses up to
         * the first 16 keys of map.
         *
         * @param map an OrderedMap of String keys to Font values
         */
        public FontFamily(OrderedMap<String, Font> map) {
            Array<String> ks = map.orderedKeys();
            for (int i = 0; i < map.size && i < 16; i++) {
                String name = ks.get(i);
                if ((connected[i] = map.get(name)) == null) continue;
                fontAliases.put(name, i);
                if(connected[i].name != null)
                    fontAliases.put(connected[i].name, i);
                fontAliases.put(String.valueOf(i), i);
            }
        }

        /**
         * Constructs a FontFamily given a Skin that defines one or more BitmapFont items. The name in the Skin file
         * for each font is one way you will be able to access fonts from this family. You can also use the
         * {@link BitmapFontData#name} of a BitmapFont in the Skin as an alias, or the essentially-randomly-chosen
         * index of the BitmapFont in the order this encountered it. Note that this constructor does not handle distance
         * field fonts (it treats all fonts as {@link DistanceFieldType#STANDARD}). But, if you use a {@link FWSkin}
         * instead of a Skin, it will try to load Fonts instead of BitmapFonts, and Font supports distance fields.
         *
         * @param skin a non-null Skin that defines one or more BitmapFont items
         */
        public FontFamily(Skin skin) {
            initBitmapFonts(skin);
        }

        private void initBitmapFonts(Skin skin) {
            ObjectMap<String, BitmapFont> map = skin.getAll(BitmapFont.class);
            Array<String> keys = map.keys().toArray();
            for (int i = 0; i < map.size && i < 16; i++) {
                String name = keys.get(i);
                BitmapFont bmf = map.get(name);
                if(bmf == null) continue;
                Font font = new Font(bmf);
                font.name = name;
                font.family = this;
                connected[i] = font;
                fontAliases.put(name, i);
                if(bmf.getData().name != null)
                    fontAliases.put(bmf.getData().name, i);
                fontAliases.put(String.valueOf(i), i);
            }
        }


        /**
         * Constructs a FontFamily given a FWSkin that defines one or more Font items. The name in the Skin file
         * for each font is one way you will be able to access fonts from this family. You can also use the
         * {@link Font#name} of a Font in the Skin as an alias, or the essentially-randomly-chosen
         * index of the Font in the order this encountered it. Note that if you store an FWSkin in a {@link Skin}
         * variable, this constructor will not be used, instead choosing {@link #FontFamily(Skin)}, and that constructor
         * doesn't handle any distance field fonts correctly.
         * @param skin a non-null FWSkin or subclass that defines one or more Font items
         */
        public FontFamily(FWSkin skin) {
            ObjectMap<String, Font> map = skin.getAll(Font.class);
            if(map.isEmpty()) {
                initBitmapFonts(skin);
                return;
            }
            Array<String> keys = map.keys().toArray();
            for (int i = 0; i < map.size && i < 16; i++) {
                String name = keys.get(i);
                Font font = map.get(name);
                if(font == null) continue;
                font.name = name;
                font.family = this;
                connected[i] = font;
                fontAliases.put(name, i);
                if(font.name != null)
                    fontAliases.put(font.name, i);
                fontAliases.put(String.valueOf(i), i);
            }
        }

        /**
         * Copy constructor for another FontFamily. Font items in {@link #connected} will not be copied, and the same
         * references will be used.
         *
         * @param other another, non-null, FontFamily to copy into this.
         */
        public FontFamily(FontFamily other) {
            System.arraycopy(other.connected, 0, connected, 0, 16);
            fontAliases.putAll(other.fontAliases);
        }

        /**
         * Gets the corresponding Font for a name/alias, or null if it was not found.
         *
         * @param name a name or alias for a font, such as "Gentium" or "2"
         * @return the Font corresponding to the given name, or null if not found
         */
        public Font get(String name) {
            if (name == null) return null;
            return connected[fontAliases.get(name, 0) & 15];
        }

        /**
         * Calls {@link Font#resizeDistanceField(float, float)} on each Font in this FontFamily.
         * @param width  the new window width; usually a parameter in {@link com.badlogic.gdx.ApplicationListener#resize(int, int)}
         * @param height the new window height; usually a parameter in {@link com.badlogic.gdx.ApplicationListener#resize(int, int)}
         */
        public void resizeDistanceFields(float width, float height) {
            for(Font f : connected){
                if(f != null) f.resizeDistanceField(width, height);
            }
        }

        /**
         * Calls {@link Font#resizeDistanceField(float, float, Viewport)} on each Font in this FontFamily.
         * @param width  the new window width; usually a parameter in {@link com.badlogic.gdx.ApplicationListener#resize(int, int)}
         * @param height the new window height; usually a parameter in {@link com.badlogic.gdx.ApplicationListener#resize(int, int)}
         * @param viewport the current Viewport, after it has been updated using {@link Viewport#update(int, int)}
         */
        public void resizeDistanceFields(float width, float height, Viewport viewport) {
            for(Font f : connected){
                if(f != null) f.resizeDistanceField(width, height, viewport);
            }
        }
    }

    /**
     * Defines what types of distance field font this can use and render.
     * <ul>
     * <li>STANDARD has no distance field.</li>
     * <li>SDF is the signed distance field technique Hiero is compatible with, and uses only an alpha channel.</li>
     * <li>MSDF is the multi-channel signed distance field technique, which is sharper but uses the RGB channels.</li>
     * <li>SDF_OUTLINE can use the same font files as SDF, but draws them with a black outline.</li>
     * </ul>
     */
    public enum DistanceFieldType {
        /**
         * Used by normal fonts with no distance field effect.
         * If the font has a large image that is downscaled, you may want to call {@link #setTextureFilter()}.
         * You can optionally set a custom ShaderProgram this can use by setting {@link #shader}.
         */
        STANDARD("-standard", ""),
        /**
         * Used by Signed Distance Field fonts that are compatible with
         * {@link com.badlogic.gdx.graphics.g2d.DistanceFieldFont}, and may be created by Hiero with its Distance Field
         * effect, <a href="https://github.com/tommyettinger/fontwriter">fontwriter</a> or the tool it uses and builds
         * upon, <a href="https://github.com/Chlumsky/msdf-atlas-gen">msdf-atlas-gen</a>. You may want to set the
         * {@link #distanceFieldCrispness} field to a
         * higher or lower value depending on how thick the stroke is in the font; this can take experimentation,
         * but the default is 1 and higher values have sharper edges (risking getting pixelated if too high).
         */
        SDF("-sdf", " (SDF)"),
        /**
         * Used by Multi-channel Signed Distance Field fonts, which are typically created by fontwriter or
         * msdf-atlas-gen and can sometimes be more crisp than SDF fonts MSDF produces hard corners where the corners
         * were hard in the original font. Creating MSDF fonts can be done with
         * <a href="https://github.com/tommyettinger/fontwriter">fontwriter</a> or the tool it uses and builds upon,
         * <a href="https://github.com/Chlumsky/msdf-atlas-gen">msdf-atlas-gen</a>. You may want to set the
         * {@link #distanceFieldCrispness} field to a higher or lower value than its default of 1 based on preference,
         * with higher values making more crisp (possibly aliased) fonts and lower values making softer (possibly
         * blurry) fonts.
         */
        MSDF("-msdf", " (MSDF)"),
        /**
         * Very similar to {@link #SDF}, except that this draws a moderately-thick black outline around all SDF glyphs.
         * It won't necessarily draw the outline correctly for inline images without a distance field effect.
         * The outline will respect the transparency of the glyph (unlike {@link #BLACK_OUTLINE} mode).
         * This can use the same font files as {@link #SDF}; just set {@link Font#distanceField} to SDF_OUTLINE.
         */
        SDF_OUTLINE("-sdf", " (SDF Outline)");

        public static final DistanceFieldType[] ALL = values();

        /**
         * The part of a filename before the extension that is used to look up fonts with this DistanceFieldType.
         * Always one of "-standard", "-sdf", or "-msdf".
         */
        public final String filePart;
        /**
         * The part of the typical name of a Font that says what distance field that Font has, if any.
         */
        public final String namePart;

        DistanceFieldType(String filePart, String namePart) {
            this.filePart = filePart;
            this.namePart = namePart;
        }
    }

    //// members section

    /**
     * If true, any Font created will be able to instantiate Texture objects, query their dimensions, and perform other
     * operations that typically require an OpenGL context. If false, all operations that would use a Texture are either
     * avoided or can be expected to fail (such as trying to {@link #drawGlyph(Batch, long, float, float)}). Even if
     * this is false, you can still change {@link Layout} instances with {@link #markup(String, Layout)}, since those
     * don't require any Texture to function.
     * <br>
     * You should set this to false before creating any Font if you are using a headless backend.
     */
    public static boolean canUseTextures = true;
    /**
     * Determines whether this Font can share the same reference for its data structure members (if true), or if copies
     * should be made when using {@link #Font(Font)} (if false). This defaults to false. You should generally set this
     * by using {@link #setSharing(boolean)} rather than manually changing this field.
     * <br>
     * It can be a good idea to set this to {@code true} using {@link #setSharing(boolean)} once you are done calling
     * the methods {@link #addAtlas(TextureAtlas)}, {@link #addImage(String, TextureRegion)},
     * {@link #addSpacingGlyph(char, float)}, and {@link #fitCell(float, float, boolean)}. Copying a large
     * {@link #mapping}, and the rest of the data associated with it, can take a little time, but setting sharing to
     * true can reduce the amount of time spent loading.
     */
    public boolean sharing = false;

    /**
     * Maps char keys (stored as ints) to their corresponding {@link GlyphRegion} values. You can add arbitrary images
     * to this mapping if you create appropriate GlyphRegion values (as with
     * {@link GlyphRegion#GlyphRegion(TextureRegion, float, float, float, float)}), though they must map to a char.
     * If {@link #sharing} is true, then this reference may be shared with one or more other Fonts, and changes to the
     * mapping will affect all Fonts that share that mapping. {@link #addAtlas(TextureAtlas)},
     * {@link #addImage(String, TextureRegion)}, {@link #addSpacingGlyph(char, float)}, and
     * {@link #fitCell(float, float, boolean)} all modify this field, and it can also be modified directly.
     */
    public IntMap<GlyphRegion> mapping;

    /**
     * Optional; maps the names of TextureRegions to the indices they use in {@link #mapping}, and usually assigned by
     * {@link #addAtlas(TextureAtlas)}. The keys in this map are case-insensitive.
     * If {@link #sharing} is true, then this reference may be shared with one or more other Fonts, and changes to this
     * variable will affect all Fonts that share references with this Font. {@link #addAtlas(TextureAtlas)} modifies
     * this field, and it can also be modified directly.
     */
    public CaseInsensitiveIntMap nameLookup;

    /**
     * Optional; a reversed form of {@link #nameLookup} that allows you to get the printable name for a given char code.
     * This is usually assigned by {@link #addAtlas(TextureAtlas)}.
     * <br>
     * If multiple names are registered for the same char, the first one registered takes priority, unless the second
     * name starts with an "astral plane" char such as an emoji. In the
     * common use case of {@link KnownFonts#addEmoji(Font)}, this means the printable names are all emoji.
     * <br>
     * If {@link #sharing} is true, then this reference may be shared with one or more other Fonts, and changes to this
     * variable will affect all Fonts that share references with this Font. {@link #addAtlas(TextureAtlas)} modifies
     * this field, and it can also be modified directly.
     */
    public IntMap<String> namesByCharCode;
    /**
     * Which GlyphRegion to display if a char isn't found in {@link #mapping}. May be null to show a space by default.
     */
    public GlyphRegion defaultValue;
    /**
     * The larger TextureRegions that {@link GlyphRegion} images are pulled from; these could be whole Textures or be
     * drawn from a TextureAtlas that the font shares with other images.
     */
    public Array<TextureRegion> parents;
    protected DistanceFieldType distanceField = DistanceFieldType.STANDARD;

    /**
     * Effectively used to attach a Float value to each Batch that might be used to draw a Font, where the Float is the
     * current {@code u_smoothing} uniform value used by that Batch.
     */
    private static final ObjectFloatMap<Batch> smoothingValues = new ObjectFloatMap<>(8);

    /**
     * The last Texture drawn, which may be null if nothing has drawn before. This is used to tell when to enable or
     * disable distance field shaders because some other Texture is being drawn. It's pretty much a hack.
     * <br>
     * It is a bad practice, but this is a static mutable field. This should be OK in this case because this only has
     * any meaning when the Font is being drawn by OpenGL, which is a single-threaded API. All Font instances share this
     * because that is necessary to allow switching between fonts to make sense.
     */
    @SuppressWarnings("GDXJavaStaticResource")
    private static Texture latestTexture = null;

    /**
     * If true, this is a fixed-width (monospace) font; if false, this is probably a variable-width font. This affects
     * some rendering decisions Font makes, such as whether subscript chars should take up half-width (for variable
     * fonts) or full-width (for monospace).
     */
    public boolean isMono;
    /**
     * Unlikely to be used externally, this is one way of storing the kerning information that some fonts have. Kerning
     * can improve the appearance of variable-width fonts, and is always null for monospace fonts. This uses a
     * combination of two chars as a key (the earlier char is in the upper 16 bits, and the later char is in the lower
     * 16 bits). Each such combination that has a special kerning value (not the default 0) has a float associated with
     * it, which applies to the x-position of the later char.
     * <br>
     * If {@link #sharing} is true, then this reference may be shared with one or more other Fonts, and changes to this
     * variable will affect all Fonts that share references with this Font. While kerning is usually only assigned when
     * a Font is loaded from a file or a BitmapFont, it can also be modified directly.
     */
    public IntFloatMap kerning;
    /**
     * When {@link #distanceField} is {@link DistanceFieldType#SDF}, {@link DistanceFieldType#MSDF}, or
     * {@link DistanceFieldType#SDF_OUTLINE}, this determines how much the edges of the glyphs should be aliased sharply
     * (higher values) or anti-aliased softly (lower values). The default value is 1. This is set internally by
     * {@link #resizeDistanceField(float, float)} using {@link #distanceFieldCrispness} as a multiplier; when you want to
     * have a change to crispness persist, use that other field.
     */
    public float actualCrispness = 1f;

    /**
     * When {@link #distanceField} is {@link DistanceFieldType#SDF}, {@link DistanceFieldType#MSDF}, or
     * {@link DistanceFieldType#SDF_OUTLINE}, this determines how much the edges of the glyphs should be aliased sharply
     * (higher values) or anti-aliased softly (lower values). The default value is 1. This is used as a persistent
     * multiplier that can be configured per-font, whereas {@link #actualCrispness} is the working value that changes
     * often but is influenced by this one. This variable is used by {@link #resizeDistanceField(float, float)} to affect
     * the working crispness value.
     */
    public float distanceFieldCrispness = 1f;

    /**
     * Only actually refers to a "cell" when {@link #isMono} is true; otherwise refers to the largest width of any
     * glyph in the font, after scaling.
     */
    public float cellWidth = 1f;
    /**
     * Refers to the largest height of any glyph in the font, after scaling.
     */
    public float cellHeight = 1f;
    /**
     * Only actually refers to a "cell" when {@link #isMono} is true; otherwise refers to the largest width of any
     * glyph in the font, before any scaling.
     */
    public float originalCellWidth = 1f;
    /**
     * Refers to the largest height of any glyph in the font, before any scaling.
     */
    public float originalCellHeight = 1f;
    /**
     * Scale multiplier for width.
     */
    public float scaleX = 1f;
    /**
     * Scale multiplier for height.
     */
    public float scaleY = 1f;

    /**
     * How far the unscaled font descends below the baseline, typically as a negative number (not always).
     */
    public float descent = 0f;

    /**
     * A char that will be used to draw solid blocks with {@link #drawBlocks(Batch, int[][], float, float)}, and to draw
     * box-drawing/block-element characters if {@code makeGridGlyphs} is true in the constructor. The glyph
     * that corresponds to this char should be a 1x1 pixel block of solid white pixels in most cases. Because there is
     * already a solid block in Unicode, this
     * defaults to that Unicode glyph, at u2588 . There is also a test in TextraTypist, BlockStamper, that can place a
     * tiny solid block in the lower-right corner and use that for this purpose. You can check if a .fnt file has a
     * solid block present by searching for {@code char id=9608} (9608 is the decimal way to write 0x2588).
     */
    public char solidBlock = '█'; // in decimal, this is 9608, in hex, u2588

    /**
     * If non-null, may contain connected Font values and names/aliases to look them up with using [@Name] syntax.
     */
    public FontFamily family;

    /**
     * Determines how colors are looked up by name; defaults to using {@link ColorUtils#describe(String)}.
     */
    public ColorLookup colorLookup = ColorLookup.DESCRIPTIVE;

    /*
     * If true, this will always use integers for x and y position (rounding), which can help some fonts look more
     * clear. However, if your world units are measured so that one world unit covers several pixels, then having this
     * enabled can cause bizarre-looking visual glitches involving stretched or disappearing glyphs. This defaults to
     * false, unlike what libGDX BitmapFont defaults to.
     */
    /**
     * By default, this doesn't do anything; subclasses can override {@link #handleIntegerPosition(float)} to try to do
     * something different with it. BitmapFont in libGDX defaults to having integer positions enabled, and there they
     * actually do something (lock the font positions to integer world units). When world units aren't equivalent to
     * on-screen pixels, BitmapFont's behavior leads to severe glitches in font appearance, so usually we aren't missing
     * much by not using this behavior.
     */
    public boolean integerPosition = false;

    /**
     * A multiplier that applies to the horizontal movement associated with oblique text (which is similar to italic).
     * The default is 1.0f, which is fairly strong for some fonts, and some styles look better with a smaller value.
     * A value of 0.0f will make oblique text look like regular text. A negative value will tip the angle backwards, so
     * the top will be to the left of the bottom.
     */
    public float obliqueStrength = 1f;

    /**
     * A multiplier that applies to the distance bold text will stretch away from the original glyph outline. The bold
     * effect is achieved here by drawing the same GlyphRegion multiple times, separated to the left and right by a
     * small distance. By reducing boldStrength to between 0.0 and 1.0, you can reduce the weight of bold text, but
     * increasing boldStrength does not usually work well.
     */
    public float boldStrength = 1f;

    /**
     * A multiplier that applies to the distance the "blacken" and "whiten" outlines will stretch away from the original
     * glyph edge. The outline effects are achieved here by drawing the same GlyphRegion multiple times, in a grid of
     * duplicate draws each a small distance apart. By reducing outlineStrength to between 0.0 and 1.0, you can reduce
     * the thickness of outlines at large font sizes, or by increasing outlineStrength above 1.0, you can make the
     * outlines more visible with smaller font sizes.
     */
    public float outlineStrength = 1f;

    /**
     * When {@code makeGridGlyphs} is passed as true to a constructor here, box drawing and other block elements will be
     * drawn using a solid block GlyphRegion that is stretched and moved to form various lines and blocks. Setting this
     * field to something other than 1 affects how wide the lines are for box drawing characters only; this is acts as a
     * multiplier on the original width of a line. Normal box drawing lines such as those in {@code ┌} are 0.1f of a
     * cell across. Thick lines such as those in {@code ┏} are 0.2f of a cell across. Double lines such as those in
     * {@code ╔} are two normal lines, 0.1f of a cell apart; double lines are not affected by this field.
     */
    public float boxDrawingBreadth = 1f;

    /**
     * The offset added to the position of the drop shadow when {@code [%?shadow]} mode is enabled. This defaults to
     * {@code (1.0f, -2.0f)}, which makes the shadow cast down and slightly to the right, and can be set in-place using
     * {@link Vector2#set(float, float)}. The shadow is currently not sized differently from the text that casts it.
     * The color of the shadow is determined by {@link #PACKED_SHADOW_COLOR}, a packed float color obtainable via
     * {@link Color#toFloatBits(float, float, float, float)}, typically with fractional alpha.
     */
    public final Vector2 dropShadowOffset = new Vector2(1f, -2f);

    /**
     * A multiplier that applies to the alpha of the surrounding, typically colorful ring around text using NEON or HALO
     * mode. The NEON and HALO modes each draw a glowing area by repeatedly drawing an offset version of the text with a
     * low alpha before drawing the non-offset text in white (for NEON) or black (for HALO).
     * <br>
     * By changing glowStrength to between 0.0 and 1.0, you can make the glow effect for both NEON and HALO fade out, or
     * by raising it above 1.0, you can make it stronger. This may work well with {@link NoiseUtils#noise1D(float, int)}
     * to determine the strength, adding 1f to that noise result to keep glowStrength in the 0f to 2f range while
     * semi-randomly changing it over time (if the seconds elapsed are given as x).
     */
    public float glowStrength = 1f;

    /**
     * The name of the Font, for display purposes. This is not necessarily the same as the name of the font used in any
     * particular {@link FontFamily}.
     */
    public String name = "Unnamed Font";

    /**
     * A white square Texture, typically 3x3, that can be used as a backup in case a Font doesn't have a solid block
     * character available to it already (such as for many Fonts created from BitmapFonts). This will be null unless it
     * becomes needed, and will be disposed if this Font is.
     */
    public Texture whiteBlock = null;

    /**
     * Bit flag for bold mode, as a long.
     */
    public static final long BOLD = 1L << 30;
    /**
     * Bit flag for oblique mode, as a long.
     */
    public static final long OBLIQUE = 1L << 29;
    /**
     * Bit flag for underline mode, as a long.
     */
    public static final long UNDERLINE = 1L << 28;
    /**
     * Bit flag for strikethrough mode, as a long.
     */
    public static final long STRIKETHROUGH = 1L << 27;
    /**
     * Bit flag for subscript mode, as a long.
     */
    public static final long SUBSCRIPT = 1L << 25;
    /**
     * Bit flag for midscript mode, as a long.
     */
    public static final long MIDSCRIPT = 2L << 25;
    /**
     * Two-bit flag for superscript mode, as a long.
     * This can also be checked to see if it is non-zero, which it will be if any of
     * {@link #SUBSCRIPT}, {@link #MIDSCRIPT}, or SUPERSCRIPT are enabled.
     */
    public static final long SUPERSCRIPT = 3L << 25;
    /**
     * Bit flag for alternate mode, as a long.
     * The behavior of the scale bits changes if alternate mode is enabled.
     */
    //FIXME: Alternate modes no longer replace scale!
    public static final long ALTERNATE = 1L << 24;
    /**
     * Bit flag for matching alternate modes, as a long.
     * If a glyph is masked with this ({@code (glyph & ALTERNATE_MODES_MASK)}), and that equals one of the alternate
     * mode bit flags exactly ({@link #SMALL_CAPS}, {@link #WHITE_OUTLINE}, {@link #DROP_SHADOW}, and so on), then
     * that mode is enabled.
     */
    public static final long ALTERNATE_MODES_MASK = 15L << 20;
    /**
     * Bit flag for black outline, as a long.
     * This can be enabled at the same time as alternate modes, and some alternate modes will both enable this and
     * affect its color temporarily.
     * This can be configured to use a different color in place of black by changing {@link #PACKED_BLACK}.
     */
    public static final long BLACK_OUTLINE = 1L << 24;
    /**
     * Bit flag for white outline mode, as a long.
     * If the Font being outlined uses some other color in its texture, this will draw the outline in the color used by
     * the outer edge of each glyph drawn. For images in an atlas, like {@link KnownFonts#addEmoji(Font)}, this will
     * typically color the outline in the same color used by the edge of the image. This can be avoided by tinting the
     * glyph with a darker color and still using this white outline.
     * This can be configured to use a different color in place of white by changing {@link #PACKED_WHITE}.
     */
    public static final long WHITE_OUTLINE = 15L << 20;
    /**
     * Bit flag for yellow outline mode, as a long.
     * This changes the color used for the outline when {@link #BLACK_OUTLINE} is enabled.
     */
    public static final long YELLOW_OUTLINE = 14L << 20;
    /**
     * Bit flag for red outline mode, as a long.
     * This changes the color used for the outline when {@link #BLACK_OUTLINE} is enabled.
     */
    public static final long RED_OUTLINE = 13L << 20;
    /**
     * Bit flag for blue outline mode, as a long.
     * This changes the color used for the outline when {@link #BLACK_OUTLINE} is enabled.
     */
    public static final long BLUE_OUTLINE = 12L << 20;
    /**
     * Bit flag for small caps mode, as a long. This draws capital letters normally, but makes lower-case letters
     * render as reduced-height versions of their capitalized forms.
     */
    public static final long SMALL_CAPS = 1L << 20;
    /**
     * Bit flag for "jostle" mode, as a long. This moves characters around in a stationary random pattern, but keeps
     * their existing metrics (width and height).
     */
    public static final long JOSTLE = 2L << 20;
    /**
     * Bit flag for shiny mode, as a long. This draws the glyph once in white and up one pixel earlier, then draws
     * over it with the normal glyph in its normal color.
     * If {@link #PACKED_WHITE} has changed, this will use its changes for the shiny part of the glyph.
     */
    public static final long SHINY = 3L << 20;
    /**
     * Bit flag for neon mode, as a long. This draws the glyph multiple times with offsets in all directions, in its
     * normal color but at reduced alpha, then draws over those with the normal glyph in white. Using a white text color
     * will mostly make the NEON text look blurry, so using a bright, saturated color for the text color is suggested.
     * If {@link #PACKED_WHITE} has changed, this will use its changes for the white center of the glyph.
     */
    public static final long NEON = 4L << 20;
    /**
     * Bit flag for halo mode, as a long. This draws the glyph multiple times with offsets in all directions, drawing
     * with a low-alpha edit of the normal text color, then draws over those with the normal glyph with
     * {@link #PACKED_HALO_COLOR} as its actual text color. The text color is typically light or bright when HALO mode
     * is in use, to contrast best with the dark text color.
     * This can be configured to use a different color in place of black text by changing {@link #PACKED_HALO_COLOR}.
     */
    public static final long HALO = 5L << 20;
    /**
     * Bit flag for drop shadow mode, as a long. This draws the glyph in dark gray with low alpha and offset to the
     * lower right, then draws over that with the normal glyph in its normal color.
     * This can be configured to use a different color in place of gray by changing {@link #PACKED_SHADOW_COLOR}.
     */
    public static final long DROP_SHADOW = 6L << 20;
    /**
     * Bit flag for error mode, shown as a red wiggly-underline, as a long.
     * This can be configured to use a different color in place of red by changing {@link #PACKED_ERROR_COLOR}.
     */
    public static final long ERROR = 7L << 20;
    /**
     * Bit flag for context mode, shown as a green series of diagonal tick marks with large gaps, as a long.
     * This can be configured to use a different color in place of green by changing {@link #PACKED_CONTEXT_COLOR}.
     */
    public static final long CONTEXT = 8L << 20;
    /**
     * Bit flag for warning mode, shown as a yellow barred-underline, as a long.
     * This can be configured to use a different color in place of yellow by changing {@link #PACKED_WARN_COLOR}.
     */
    public static final long WARN = 9L << 20;
    /**
     * Bit flag for suggest mode, shown as a gray series of right angles with small gaps, as a long.
     * This can be configured to use a different color in place of gray by changing {@link #PACKED_SUGGEST_COLOR}.
     */
    public static final long SUGGEST = 10L << 20;
    /**
     * Bit flag for note mode, shown as a blue wavy-underline, as a long.
     * This can be configured to use a different color in place of blue by changing {@link #PACKED_NOTE_COLOR}.
     */
    public static final long NOTE = 11L << 20;

    /**
     * The color black, as a packed float using the default RGBA color space.
     * This can be edited for Fonts that either use a different color space,
     * or want to use a different color in place of black for effects like {@link #BLACK_OUTLINE}.
     */
    public float PACKED_BLACK = NumberUtils.intBitsToFloat(0xFE000000);
    /**
     * The color white, as a packed float using the default RGBA color space.
     * This can be edited for Fonts that either use a different color space,
     * or want to use a different color in place of white for effects like {@link #WHITE_OUTLINE} and {@link #SHINY}.
     */
    public float PACKED_WHITE = Color.WHITE_FLOAT_BITS;
    /**
     * The color red, as a packed float using the default RGBA color space.
     * This can be edited for Fonts that either use a different color space,
     * or want to use a different color in place of red for effects like {@link #RED_OUTLINE}.
     */
    public float PACKED_RED = NumberUtils.intBitsToFloat(0xFE0000FF);
    /**
     * The color yellow, as a packed float using the default RGBA color space.
     * This can be edited for Fonts that either use a different color space,
     * or want to use a different color in place of yellow for effects like {@link #YELLOW_OUTLINE}.
     */
    public float PACKED_YELLOW = NumberUtils.intBitsToFloat(0xFE00FFFF);
    /**
     * The color blue, as a packed float using the default RGBA color space.
     * This can be edited for Fonts that either use a different color space,
     * or want to use a different color in place of blue for effects like {@link #BLUE_OUTLINE}.
     */
    public float PACKED_BLUE = NumberUtils.intBitsToFloat(0xFEFF0000);
    /**
     * The color to use for {@link #ERROR}'s underline, as a packed float using the default RGBA color space.
     * Defaults to red.
     * This can be edited for Fonts that either use a different color space,
     * or want to use a different color in place of red for {@link #ERROR}.
     * In RGBA8888 format, this is the color {@code 0xFF0000FF}.
     * You can generate packed float colors using {@link Color#toFloatBits} or {@link NumberUtils#intToFloatColor(int)},
     * among other methods. Make sure that the order the method expects RGBA channels is what you provide.
     */
    public float PACKED_ERROR_COLOR = NumberUtils.intBitsToFloat(0xFE0000FF); // ABGR red
    /**
     * The color to use for {@link #WARN}'s underline, as a packed float using the default RGBA color space.
     * Defaults to yellow or gold.
     * This can be edited for Fonts that either use a different color space,
     * or want to use a different color in place of yellow for {@link #WARN}.
     * In RGBA8888 format, this is the color {@code 0xFFD510FF}.
     * You can generate packed float colors using {@link Color#toFloatBits} or {@link NumberUtils#intToFloatColor(int)},
     * among other methods. Make sure that the order the method expects RGBA channels is what you provide.
     */
    public float PACKED_WARN_COLOR = NumberUtils.intBitsToFloat(0xFE10D5FF); // ABGR yellow
    /**
     * The color to use for {@link #NOTE}'s underline, as a packed float using the default RGBA color space.
     * Defaults to blue.
     * This can be edited for Fonts that either use a different color space,
     * or want to use a different color in place of blue for {@link #NOTE}.
     * In RGBA8888 format, this is the color {@code 0x3088B8FF}.
     * You can generate packed float colors using {@link Color#toFloatBits} or {@link NumberUtils#intToFloatColor(int)},
     * among other methods. Make sure that the order the method expects RGBA channels is what you provide.
     */
    public float PACKED_NOTE_COLOR = NumberUtils.intBitsToFloat(0xFEB88830); // ABGR blue
    /**
     * The color to use for {@link #CONTEXT}'s underline, as a packed float using the default RGBA color space.
     * Defaults to forest green.
     * This can be edited for Fonts that either use a different color space,
     * or want to use a different color in place of green for {@link #CONTEXT}.
     * In RGBA8888 format, this is the color {@code 0x228B22FF}.
     * You can generate packed float colors using {@link Color#toFloatBits} or {@link NumberUtils#intToFloatColor(int)},
     * among other methods. Make sure that the order the method expects RGBA channels is what you provide.
     */
    public float PACKED_CONTEXT_COLOR = NumberUtils.intBitsToFloat(0xFE228B22); // ABGR green
    /**
     * The color to use for {@link #SUGGEST}'s underline, as a packed float using the default RGBA color space.
     * Defaults to light gray.
     * This can be edited for Fonts that either use a different color space,
     * or want to use a different color in place of gray for {@link #SUGGEST}.
     * In RGBA8888 format, this is the color {@code 0x999999FF}.
     * You can generate packed float colors using {@link Color#toFloatBits} or {@link NumberUtils#intToFloatColor(int)},
     * among other methods. Make sure that the order the method expects RGBA channels is what you provide.
     */
    public float PACKED_SUGGEST_COLOR = NumberUtils.intBitsToFloat(0xFE999999); // ABGR gray

    /**
     * The color to use for the text when {@link #HALO} mode is used, as a packed float using the default RGBA color
     * space. Defaults to opaque black. The normal text color is faded to a reduced alpha and used as the color for
     * the surrounding "halo" or "aura" effect, which is usually a light or bright color.
     * This can be edited for Fonts that either use a different color space,
     * or want to use a different color in place of opaque black for {@link #HALO} text.
     * In RGBA8888 format, this is the color {@code 0x000000FF}.
     * You can generate packed float colors using {@link Color#toFloatBits} or {@link NumberUtils#intToFloatColor(int)},
     * among other methods. Make sure that the order the method expects RGBA channels is what you provide.
     */
    public float PACKED_HALO_COLOR = NumberUtils.intBitsToFloat(0xFE000000); // opaque black

    /**
     * The color to use for {@link #DROP_SHADOW}, as a packed float using the default RGBA color space.
     * Defaults to 13% lightness, 50% alpha gray.
     * This can be edited for Fonts that either use a different color space,
     * or want to use a different color in place of half-transparent dark gray for {@link #DROP_SHADOW}.
     * In RGBA8888 format, this is the color {@code 0x2121217E}.
     * You can generate packed float colors using {@link Color#toFloatBits} or {@link NumberUtils#intToFloatColor(int)},
     * among other methods. Make sure that the order the method expects RGBA channels is what you provide.
     */
    public float PACKED_SHADOW_COLOR = NumberUtils.intBitsToFloat(0x7E212121); // half-transparent dark gray

    //Color.toFloatBits(0.1333f, 0.1333f, 0.1333f, 0.5f);

    /**
     * The x-adjustment this Font was initialized with, or 0 if there was none given.
     * This is not meant to affect appearance after a Font has been constructed, but is meant to allow
     * some introspection into what values a Font was given at construction-time.
     */
    public float xAdjust;
    /**
     * The y-adjustment this Font was initialized with, or 0 if there was none given.
     * This is not meant to affect appearance after a Font has been constructed, but is meant to allow
     * some introspection into what values a Font was given at construction-time.
     */
    public float yAdjust;
    /**
     * The width-adjustment this Font was initialized with, or 0 if there was none given.
     * This is not meant to affect appearance after a Font has been constructed, but is meant to allow
     * some introspection into what values a Font was given at construction-time.
     */
    public float widthAdjust;
    /**
     * The height-adjustment this Font was initialized with, or 0 if there was none given.
     * This is not meant to affect appearance after a Font has been constructed, but is meant to allow
     * some introspection into what values a Font was given at construction-time.
     */
    public float heightAdjust;

    /**
     * Precise adjustment for the underline's x-position, affecting the left side of the underline.
     * Normally, because underlines continue into any underline for the next glyph, decreasing
     * underX should be accompanied by increasing {@link #underLength} by a similar amount.
     * <br>
     * This is a "Zen" metric, which means it is measured in fractions of
     * {@link #cellWidth} or {@link #cellHeight} (as appropriate), and only affects one value.
     */
    public float underX;
    /**
     * Precise adjustment for the underline's y-position, affecting the bottom side of the underline.
     * <br>
     * This is a "Zen" metric, which means it is measured in fractions of
     * {@link #cellWidth} or {@link #cellHeight} (as appropriate), and only affects one value.
     */
    public float underY;
    /**
     * Precise adjustment for the underline's x-size, affecting the extra underline drawn to the right
     * of the underline. Normally, because underlines continue into any underline for the next glyph,
     * decreasing {@link #underX} should be accompanied by increasing underLength by a similar amount.
     * <br>
     * This is a "Zen" metric, which means it is measured in fractions of
     * {@link #cellWidth} or {@link #cellHeight} (as appropriate), and only affects one value.
     */
    public float underLength;
    /**
     * Precise adjustment for the underline's y-size, affecting how thick the underline is from bottom
     * to top.
     * <br>
     * This is a "Zen" metric, which means it is measured in fractions of
     * {@link #cellWidth} or {@link #cellHeight} (as appropriate), and only affects one value.
     */
    public float underBreadth;
    /**
     * Precise adjustment for the strikethrough's x-position, affecting the left side of the strikethrough.
     * Normally, because strikethrough continues into any strikethrough for the next glyph, decreasing
     * strikeX should be accompanied by increasing {@link #strikeLength} by a similar amount.
     * <br>
     * This is a "Zen" metric, which means it is measured in fractions of
     * {@link #cellWidth} or {@link #cellHeight} (as appropriate), and only affects one value.
     */
    public float strikeX;
    /**
     * Precise adjustment for the strikethrough's y-position, affecting the bottom side of the strikethrough.
     * <br>
     * This is a "Zen" metric, which means it is measured in fractions of
     * {@link #cellWidth} or {@link #cellHeight} (as appropriate), and only affects one value.
     */
    public float strikeY;
    /**
     * Precise adjustment for the strikethrough's x-size, affecting the extra strikethrough drawn to the right
     * of the strikethrough. Normally, because strikethrough continues into any strikethrough for the next glyph,
     * decreasing {@link #strikeX} should be accompanied by increasing strikeLength by a similar amount.
     * <br>
     * This is a "Zen" metric, which means it is measured in fractions of
     * {@link #cellWidth} or {@link #cellHeight} (as appropriate), and only affects one value.
     */
    public float strikeLength;
    /**
     * Precise adjustment for the strikethrough's y-size, affecting how thick the strikethrough is from bottom
     * to top.
     * <br>
     * This is a "Zen" metric, which means it is measured in fractions of
     * {@link #cellWidth} or {@link #cellHeight} (as appropriate), and only affects one value.
     */
    public float strikeBreadth;
    /**
     * Precise adjustment for the x-position of "fancy lines" such as error, warning, and note effects, affecting the
     * left side of the line.
     * <br>
     * This is a "Zen" metric, which means it is measured in fractions of
     * {@link #cellWidth} or {@link #cellHeight} (as appropriate), and only affects one value.
     */
    public float fancyX;
    /**
     * Precise adjustment for the y-position of "fancy lines" such as error, warning, and note effects, affecting the
     * bottom side of the line.
     * <br>
     * This is a "Zen" metric, which means it is measured in fractions of
     * {@link #cellWidth} or {@link #cellHeight} (as appropriate), and only affects one value.
     */
    public float fancyY;

    /**
     * An adjustment added to the {@link GlyphRegion#offsetX} of any inline images added with
     * {@link #addAtlas(TextureAtlas, String, String, float, float, float)} (or its overloads).
     * This is meant as a guess for how square inline images, such as {@link KnownFonts#addEmoji(Font) emoji}, may need
     * to be moved around to fit correctly on a line. If you have multiple atlases added to one font, you should
     * probably use the {@link #addAtlas(TextureAtlas, float, float, float)} overload that allows adding additional
     * adjustments if one atlas isn't quite right.
     * <br>
     * Changing offsetXChange with a positive value moves all GlyphRegions to the right.
     */
    public float inlineImageOffsetX = 0f;

    /**
     * An adjustment added to the {@link GlyphRegion#offsetY} of any inline images added with
     * {@link #addAtlas(TextureAtlas, String, String, float, float, float)} (or its overloads).
     * This is meant as a guess for how square inline images, such as {@link KnownFonts#addEmoji(Font) emoji}, may need
     * to be moved around to fit correctly on a line. If you have multiple atlases added to one font, you should
     * probably use the {@link #addAtlas(TextureAtlas, float, float, float)} overload that allows adding additional
     * adjustments if one atlas isn't quite right.
     * <br>
     * Changing offsetYChange with a positive value moves all inline images up (this is possibly unexpected).
     */
    public float inlineImageOffsetY = 0f;

    /**
     * An adjustment added to the {@link GlyphRegion#xAdvance} of any inline images added with
     * {@link #addAtlas(TextureAtlas, String, String, float, float, float)} (or its overloads).
     * This is meant as a guess for how square inline images, such as {@link KnownFonts#addEmoji(Font) emoji}, may need
     * to be moved around to fit correctly on a line. If you have multiple atlases added to one font, you should
     * probably use the {@link #addAtlas(TextureAtlas, float, float, float)} overload that allows adding additional
     * adjustments if one atlas isn't quite right.
     * <br>
     * Changing xAdvanceChange with a positive value will make all inline images take up more space to the right.
     */
    public float inlineImageXAdvance = 0f;

    /**
     * A multiplier applied to the sizes of any inline images (such as emoji) added with
     * {@link #addAtlas(TextureAtlas, String, String, float, float, float)} (or its overloads), at draw-time.
     * This defaults to 1.0f, and typically doesn't change much because it applies to the inline images after they have
     * already been scaled to fit in approximately one line-height. This means 0.5f will make inline images quite small,
     * and anything greater than 1.0f will probably make inline images taller than one line of text.
     */
    public float inlineImageStretch = 1f;

    /**
     * If true (the default), any text inside matching curly braces, plus the curly braces themselves, will be ignored
     * during rendering and not displayed. If false, curly braces and their contents are treated as normal text.
     * Note that regardless of this setting, you can use the <code>[-TAG]</code> syntax instead of <code>{TAG}</code>
     * to produce a tag that TypingLabel can read, even though Font itself wouldn't do anything with TAG there.
     * <br>
     * This is meant to allow TextraTypist to interoperate with the I18N Bundle system widely used for localizing apps
     * and games. Bundles use curly braces for their own purposes, but don't interfere with square ones. You can set
     * this variable to false and use {@code [-RAINBOW]} instead of <code>{RAINBOW}</code> (or any other effect), which
     * should behave the same in an I18N bundle as not in one.
     * <br>
     * The reason for the name of this field is that the text inside curly braces really is omitted from any
     * rendering by default, but it is still present in the Layout, taking up no space visually. Font code that draws
     * a Layout won't do anything with omitted data, nor will TextraLabel, but TypingLabel will.
     */
    public boolean omitCurlyBraces = true;

    /**
     * If true (the default), square bracket markup functions as documented in {@link #markup(String, Layout)}. If
     * false, square brackets and their contents are treated as normal text.
     */
    public boolean enableSquareBrackets = true;

    protected final transient float[] vertices = new float[20];
    protected final transient Layout tempLayout = new Layout();
    protected final transient LongArray glyphBuffer = new LongArray(128);
    protected final transient LongArray historyBuffer = new LongArray(64);
    protected final transient ObjectLongMap<String> labeledStates = new ObjectLongMap<>(16);
    protected final ObjectLongMap<String> storedStates = new ObjectLongMap<>(16);
    /**
     * Must be in lexicographic order because we use {@link Arrays#binarySearch(char[], int, int, char)} to
     * verify if a char is present.
     */
    protected final CharArray breakChars = CharArray.with(
            '\t',    // horizontal tab
            '\r',    // carriage return (used like a space)
            ' ',     // space
            '-',     // ASCII hyphen-minus
            '\u00AD',// soft hyphen
            '\u2000',// Unicode space
            '\u2001',// Unicode space
            '\u2002',// Unicode space
            '\u2003',// Unicode space
            '\u2004',// Unicode space
            '\u2005',// Unicode space
            '\u2006',// Unicode space
            '\u2008',// Unicode space
            '\u2009',// Unicode space
            '\u200A',// Unicode space (hair-width)
            '\u200B',// Unicode space (zero-width)
            '\u2010',// hyphen (not minus)
            '\u2012',// figure dash
            '\u2013',// en dash
            '\u2014',// em dash
            '\u2027' // hyphenation point
    );

    /**
     * Must be in lexicographic order because we use {@link Arrays#binarySearch(char[], int, int, char)} to
     * verify if a char is present.
     */
    private final CharArray spaceChars = CharArray.with(
            '\t',    // horizontal tab
            '\r',    // carriage return (used like a space)
            ' ',     // space
            '\u2000',// Unicode space
            '\u2001',// Unicode space
            '\u2002',// Unicode space
            '\u2003',// Unicode space
            '\u2004',// Unicode space
            '\u2005',// Unicode space
            '\u2006',// Unicode space
            '\u2008',// Unicode space
            '\u2009',// Unicode space
            '\u200A',// Unicode space (hair-width)
            '\u200B' // Unicode space (zero-width)
    );

    /**
     * A RegExodus Replacer that replaces any CJK ideographic characters with themselves plus a zero-width space after.
     * Meant to be used via {@link #insertZeroWidthSpacesInCJK(CharSequence)}, but can be used on its own.
     */
    public static final Replacer CJK_SPACE_INSERTER = new Replacer(Pattern.compile("([\u2E80-\u303F\u31C0-\u31EF\u3200-\u9FFF\uF900-\uFAFF\uFE30-\uFE4F])"), "$1\u200B");

    /**
     * Inserts a zero-width space character (Unicode U+200B) after every CJK ideographic character in text. This is
     * meant to create word breaks where there might otherwise be none, to allow breaking lines up how at least written
     * Chinese expects them to be broken up. This assumes there won't be any ideograms in color markup or effects, and
     * won't add zero-width spaces to Latin, Greek, Cyrillic, or other non-CJK languages.
     * <br>
     * The zero-width space character is automatically added to every Font that defines a normal space character
     * (U+0020), and is literally a modified version of space that has zero width. This definition is allowed to
     * overwrite an existing zero-width space character, but both should be invisible and have zero width (plus, not
     * many fonts have a zero-width space without this modification).
     *
     * @param text a CharSequence such as a String, typically containing CJK ideograms
     * @return a String where every CJK char has a zero-width space (U+200B) appended after it
     */
    public static String insertZeroWidthSpacesInCJK(CharSequence text) {
        return CJK_SPACE_INSERTER.replace(text);
    }

    /**
     * The standard libGDX vertex shader source, which is also used by the SDF and MSDF shaders.
     */
    public static final String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
            + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
            + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n"
            + "uniform mat4 u_projTrans;\n"
            + "varying vec4 v_color;\n"
            + "varying vec2 v_texCoords;\n"
            + "\n"
            + "void main() {\n"
            + "	v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
            + "	v_color.a = v_color.a * (255.0/254.0);\n"
            + "	v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n"
            + "	gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
            + "}\n";
    /**
     * A modified version of the fragment shader for SDF fonts from
     * {@link com.badlogic.gdx.graphics.g2d.DistanceFieldFont}, now supporting RGB colors other than white (but with
     * limited support for partial transparency). This is automatically used when {@link #enableShader(Batch)} is
     * called and the {@link #distanceField} is {@link DistanceFieldType#SDF}. This shader can be used with inline
     * images, but won't behave 100% correctly if they use partial transparency, such as to anti-alias edges.
     */
    public static final String sdfFragmentShader =
            "#ifdef GL_ES\n"
                    + "	precision mediump float;\n"
                    + "	precision mediump int;\n"
                    + "#endif\n"
                    + "\n"
                    + "uniform sampler2D u_texture;\n"
                    + "uniform float u_smoothing;\n"
                    + "varying vec4 v_color;\n"
                    + "varying vec2 v_texCoords;\n"
                    + "\n"
                    + "void main() {\n"
                    + "	if (u_smoothing > 0.0) {\n"
                    + "		float smoothing = 0.25 / u_smoothing;\n"
                    + "		vec4 color = texture2D(u_texture, v_texCoords);\n"
                    + "		float alpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, color.a);\n"
                    + "		gl_FragColor = vec4(v_color.rgb * color.rgb, alpha * v_color.a);\n"
                    + "  } else {\n"
                    + "	    gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n"
                    + "  }\n"
                    + "}\n";
    /**
     * A modified version of the fragment shader for SDF fonts from
     * <a href="https://jvm-gaming.org/t/solved-signed-distance-field-fonts-look-crappy-at-small-pt-sizes/49617/8">this JVM-Gaming forum post</a>,
     * now supporting RGB colors other than white (but with
     * limited support for partial transparency). This is automatically used when {@link #enableShader(Batch)} is
     * called, the {@link #distanceField} is {@link DistanceFieldType#SDF}, and the application is running on desktop
     * OpenGL (so it has derivative functions available). This shader can be used with inline
     * images, but won't behave 100% correctly if they use partial transparency, such as to anti-alias edges.
     */
    public static final String sdfFragmentShaderDesktopOpenGL =
            "#ifdef GL_ES\n"
                    + "	precision mediump float;\n"
                    + "	precision mediump int;\n"
                    + "#endif\n"
                    + "\n"
                    + "uniform sampler2D u_texture;\n"
                    + "uniform float u_smoothing; /* Unused except to enable or disable the shader. */\n"
                    + "varying vec4 v_color;\n"
                    + "varying vec2 v_texCoords;\n"
                    + "\n"
                    + "void main() {\n"
                    + "	if (u_smoothing > 0.0) {\n"
                    + "		vec4 color = texture2D(u_texture, v_texCoords);\n"
                    + "     //float smoothing = fwidth(color.a);\n"
                    + "     float smoothing = 0.7 * length(vec2(dFdx(color.a), dFdy(color.a)));\n"
                    + "		float alpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, color.a);\n"
                    + "		gl_FragColor = vec4(v_color.rgb * color.rgb, alpha * v_color.a);\n"
                    + "  } else {\n"
                    + "	    gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n"
                    + "  }\n"
                    + "}\n";
    /**
     * A modified version of the fragment shader for SDF fonts from
     * {@link com.badlogic.gdx.graphics.g2d.DistanceFieldFont}, this draws a moderately thick black outline around
     * each glyph, with the outline respecting the transparency of the glyph (unlike {@link #BLACK_OUTLINE} mode).
     * This also supports RGB colors other than white (but with limited support for partial transparency).
     * This is automatically used when {@link #enableShader(Batch)} is called and the {@link #distanceField} is
     * {@link DistanceFieldType#SDF_OUTLINE}. This shader can be used with inline images, but won't behave 100%
     * correctly if they use partial transparency, such as to anti-alias edges.
     */
    public static final String sdfBlackOutlineFragmentShader =
            "#ifdef GL_ES\n" +
                    "precision mediump float;\n" +
                    "#endif\n" +
                    "uniform sampler2D u_texture;\n" +
                    "uniform float u_smoothing;\n" +
                    "varying vec4 v_color;\n" +
                    "varying vec2 v_texCoords;\n" +
                    "const float closeness = 0.015625; // Between 0 and 0.5, 0 = thick outline, 0.5 = no outline\n" +
                    "void main() {\n" +
                    "  if (u_smoothing > 0.0) {\n" +
                    "    vec4 image = texture2D(u_texture, v_texCoords);\n" +
                    "    float smoothing = 0.5 / u_smoothing;\n" +
                    "    float outlineFactor = smoothstep(0.5 - smoothing, 0.5 + smoothing, image.a);\n" +
                    "    vec3 color = image.rgb * v_color.rgb * outlineFactor;\n" +
                    "    float alpha = smoothstep(closeness, closeness + smoothing, image.a);\n" +
                    "    gl_FragColor = vec4(color, v_color.a * alpha);\n" +
                    "  } else {\n" +
                    "    gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" +
                    "  }\n" +
                    "}";

    /**
     * A modified version of the fragment shader for SDF fonts from
     * {@link com.badlogic.gdx.graphics.g2d.DistanceFieldFont}, this draws a moderately thick black outline around
     * each glyph, with the outline respecting the transparency of the glyph (unlike {@link #BLACK_OUTLINE} mode).
     * This also supports RGB colors other than white (but with limited support for partial transparency).
     * This is automatically used when {@link #enableShader(Batch)} is called and the {@link #distanceField} is
     * {@link DistanceFieldType#SDF_OUTLINE}. This shader can be used with inline images, but won't behave 100%
     * correctly if they use partial transparency, such as to anti-alias edges.
     */
    public static final String sdfBlackOutlineFragmentShaderDesktopOpenGL =
            "#ifdef GL_ES\n" +
                    "precision mediump float;\n" +
                    "#endif\n" +
                    "uniform sampler2D u_texture;\n" +
                    "uniform float u_smoothing;\n" +
                    "varying vec4 v_color;\n" +
                    "varying vec2 v_texCoords;\n" +
                    "const float closeness = 0.015625; // Between 0 and 0.5, 0 = thick outline, 0.5 = no outline\n" +
                    "void main() {\n" +
                    "  if (u_smoothing > 0.0) {\n" +
                    "    vec4 image = texture2D(u_texture, v_texCoords);\n" +
                    "    float smoothing = 0.7 * length(vec2(dFdx(image.a), dFdy(image.a)));\n" +
                    "    float outlineFactor = smoothstep(0.5 - smoothing, 0.5 + smoothing, image.a);\n" +
                    "    vec3 color = image.rgb * v_color.rgb * outlineFactor;\n" +
                    "    float alpha = smoothstep(closeness, closeness + smoothing, image.a);\n" +
                    "    gl_FragColor = vec4(color, v_color.a * alpha);\n" +
                    "  } else {\n" +
                    "    gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" +
                    "  }\n" +
                    "}";

    /**
     * Fragment shader source meant for MSDF fonts. This is automatically used when {@link #enableShader(Batch)} is
     * called and the {@link #distanceField} is {@link DistanceFieldType#MSDF}. This shader will almost always fail to
     * work correctly with inline images.
     * <br>
     * This is mostly derived from
     * <a href="https://github.com/maltaisn/msdf-gdx/blob/v0.2.1/lib/src/main/resources/font.frag">msdf-gdx</a>, which
     * is Apache 2.0-licensed.
     */
    public static final String msdfFragmentShader =
            "#ifdef GL_ES\n" +
                    "precision mediump float;\n" +
                    "#endif\n" +
                    "#if __VERSION__ >= 130\n" +
                    "#define TEXTURE texture\n" +
                    "#else\n" +
                    "#define TEXTURE texture2D\n" +
                    "#endif\n" +
                    "uniform sampler2D u_texture;\n" +
                    "varying vec4 v_color;\n" +
                    "varying vec2 v_texCoords;\n" +
                    "uniform float u_smoothing;\n" +
//                    "uniform float u_weight;\n" +
                    "float median(float r, float g, float b) {\n" +
                    "    return max(min(r, g), min(max(r, g), b));\n" +
                    "}\n" +
                    "void main() {\n" +
                    "  if (u_smoothing > 0.0) {\n" +
                    "    vec4 msdf = TEXTURE(u_texture, v_texCoords);\n" +
                    "    float distance = u_smoothing * (median(msdf.r, msdf.g, msdf.b) - 0.5);\n" +
                    "    float glyphAlpha = clamp(distance + 0.5, 0.0, 1.0);\n" +
                    "    gl_FragColor = vec4(v_color.rgb, glyphAlpha * v_color.a);\n" +
                    "  } else {\n" +
                    "    gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" +
                    "  }\n" +
                    "}";
//            "#ifdef GL_ES\n"
//            + "	precision mediump float;\n"
//            + "#endif\n"
//            + "\n"
//            + "uniform sampler2D u_texture;\n"
//            + "uniform float u_smoothing;\n"
//            + "varying vec4 v_color;\n"
//            + "varying vec2 v_texCoords;\n"
//            + "\n"
//            + "void main() {\n"
//            + "  vec3 sdf = texture2D(u_texture, v_texCoords).rgb;\n"
//            + "  gl_FragColor = vec4(v_color.rgb, clamp((max(min(sdf.r, sdf.g), min(max(sdf.r, sdf.g), sdf.b)) - 0.5) * u_smoothing + 0.5, 0.0, 1.0) * v_color.a);\n"
//            + "}\n";

    /**
     * The ShaderProgram used to render this font, as used by {@link #enableShader(Batch)}.
     * If this is null, the font will be rendered with the Batch's default shader.
     * It may be set to a custom ShaderProgram if {@link #distanceField} is set to {@link DistanceFieldType#MSDF}
     * or {@link DistanceFieldType#SDF}. It can be set to a user-defined ShaderProgram; if it is meant to render
     * MSDF or SDF fonts, then the ShaderProgram should have a {@code uniform float u_smoothing;} that will be
     * set by {@link #enableShader(Batch)}. Values passed to u_smoothing can vary a lot, depending on how the
     * font was initially created, its current scale, and its {@link #actualCrispness} field. You can
     * also use a user-defined ShaderProgram with a font using {@link DistanceFieldType#STANDARD}, which may be
     * easier and can use any uniforms you normally could with a ShaderProgram, since enableShader() won't
     * change any of the uniforms.
     */
    public ShaderProgram shader = null;

    //// font parsing section

    /**
     * Gets the ColorLookup this uses to look up colors by name.
     *
     * @return a ColorLookup implementation
     */
    public ColorLookup getColorLookup() {
        return colorLookup;
    }

    /**
     * Unlikely to be used in most games (meant more for other libraries), this allows changing how colors are looked up
     * by name (or built) given a {@link ColorLookup} interface implementation.
     *
     * @param lookup a non-null ColorLookup
     * @return this, for chaining
     */
    public Font setColorLookup(ColorLookup lookup) {
        if (lookup != null)
            colorLookup = lookup;
        return this;
    }

    //// constructor section

    /**
     * Constructs a Font from a newly-created default BitmapFont, as by {@link BitmapFont#BitmapFont()}, passing it to
     * {@link #Font(BitmapFont, float, float, float, float)}. This means this constructor will always produce a Font
     * based on 15-point Liberation Sans, with a shadow effect applied to all chars. The shadow may cause some effects,
     * such as bold and strikethrough, to look incorrect, so this is mostly useful for testing. Note, you can add a
     * shadow effect to any of the fonts in {@link KnownFonts} by using the {@code [%?shadow]} mode, so you don't
     * typically need or want the shadow to be applied to the font beforehand.
     * <br>
     * This makes small adjustments to the vertical metrics of the default BitmapFont when copied into this Font. These
     * should make a BitmapFont created with no constructor parameters and a Font constructed in the same way position
     * themselves nearly-identically when placed over each other or compared side-by-side. Wrapping may behave
     * differently between BitmapFont and Font, both because of different line break behavior on hyphens and because
     * Font may wrap slightly sooner than BitmapFont if they both are on the threshold of filling a line entirely.
     */
    public Font() {
        this(new BitmapFont(), 0f, 2f, 0f, 0f);
    }

    /**
     * Constructs a Font by reading in the given .fnt file and loading any images it specifies. Tries an internal
     * handle. Does not use a distance field effect.
     *
     * @param fntName the file path and name to a .fnt file this will load
     */
    public Font(String fntName) {
        this(fntName, DistanceFieldType.STANDARD, 0f, 0f, 0f, 0f);
    }

    /**
     * Constructs a Font by reading in the given .fnt file and loading any images it specifies. Tries an internal
     * handle. Uses the specified distance field effect.
     *
     * @param fntName       the file path and name to a .fnt file this will load
     * @param distanceField determines how edges are drawn; if unsure, you should use {@link DistanceFieldType#STANDARD}
     */
    public Font(String fntName, DistanceFieldType distanceField) {
        this(fntName, distanceField, 0f, 0f, 0f, 0f);
    }

    /**
     * Constructs a Font by reading in the given .fnt file and the given Texture by filename. Tries an internal
     * handle. Does not use a distance field effect.
     *
     * @param fntName the file path and name to a .fnt file this will load
     */
    public Font(String fntName, String textureName) {
        this(fntName, textureName, DistanceFieldType.STANDARD, 0f, 0f, 0f, 0f);
    }

    /**
     * Constructs a Font by reading in the given .fnt file and the given Texture by filename. Tries an internal
     * handle. Uses the specified distance field effect.
     *
     * @param fntName       the file path and name to a .fnt file this will load
     * @param distanceField determines how edges are drawn; if unsure, you should use {@link DistanceFieldType#STANDARD}
     */
    public Font(String fntName, String textureName, DistanceFieldType distanceField) {
        this(fntName, textureName, distanceField, 0f, 0f, 0f, 0f);
    }

    /**
     * Copy constructor; does not copy the font's {@link #shader} or {@link #colorLookup}, if it has them (it uses the
     * same reference for the new Font), but will fully copy everything else.
     *
     * @param toCopy another Font to copy
     */
    public Font(Font toCopy) {
        this.distanceField = toCopy.distanceField;
        isMono = toCopy.isMono;
        actualCrispness = toCopy.actualCrispness;
        distanceFieldCrispness = toCopy.distanceFieldCrispness;
        cellWidth = toCopy.cellWidth;
        cellHeight = toCopy.cellHeight;
        scaleX = toCopy.scaleX;
        scaleY = toCopy.scaleY;
        originalCellWidth = toCopy.originalCellWidth;
        originalCellHeight = toCopy.originalCellHeight;
        descent = toCopy.descent;

        boldStrength = toCopy.boldStrength;
        obliqueStrength = toCopy.obliqueStrength;
        outlineStrength = toCopy.outlineStrength;
        underX = toCopy.underX;
        underY = toCopy.underY;
        underLength = toCopy.underLength;
        underBreadth = toCopy.underBreadth;
        strikeX = toCopy.strikeX;
        strikeY = toCopy.strikeY;
        strikeLength = toCopy.strikeLength;
        strikeBreadth = toCopy.strikeBreadth;
        fancyX = toCopy.fancyX;
        fancyY = toCopy.fancyY;
        boxDrawingBreadth = toCopy.boxDrawingBreadth;

        xAdjust =      toCopy.xAdjust;
        yAdjust =      toCopy.yAdjust;
        widthAdjust =  toCopy.widthAdjust;
        heightAdjust = toCopy.heightAdjust;
        inlineImageOffsetX = toCopy.inlineImageOffsetX;
        inlineImageOffsetY = toCopy.inlineImageOffsetY;
        inlineImageXAdvance = toCopy.inlineImageXAdvance;
        inlineImageStretch = toCopy.inlineImageStretch;

        parents = new Array<>(toCopy.parents);
        mapping = new IntMap<>(toCopy.mapping.size);
        if(toCopy.sharing){
            sharing = true;
            mapping = toCopy.mapping;
            nameLookup = toCopy.nameLookup;
            namesByCharCode = toCopy.namesByCharCode;
            kerning = toCopy.kerning;
        } else {
            for (IntMap.Entry<GlyphRegion> e : toCopy.mapping) {
                if (e.value == null) continue;
                mapping.put(e.key, new GlyphRegion(e.value));
            }
            if(toCopy.nameLookup != null)
                nameLookup = new CaseInsensitiveIntMap(toCopy.nameLookup);
            if(toCopy.namesByCharCode != null)
                namesByCharCode = new IntMap<>(toCopy.namesByCharCode);
            kerning = toCopy.kerning == null ? null : new IntFloatMap(toCopy.kerning);
        }
        defaultValue = toCopy.defaultValue;
        solidBlock = toCopy.solidBlock;
        name = toCopy.name;
        integerPosition = toCopy.integerPosition;
        omitCurlyBraces = toCopy.omitCurlyBraces;
        enableSquareBrackets = toCopy.enableSquareBrackets;
        whiteBlock = toCopy.whiteBlock;

        storedStates.putAll(toCopy.storedStates);

        if (toCopy.family != null) {
            family = new FontFamily(toCopy.family);
            Font[] connected = family.connected;
            for (int i = 0; i < connected.length; i++) {
                Font f = connected[i];
                if (f == toCopy) {
                    connected[i] = this;
                    break;
                }
            }
            if (toCopy.name != null) {
                int connect = family.fontAliases.remove(toCopy.name, -1);
                if(connect != -1 && name != null) family.fontAliases.put(name, connect);
            }
        }

        PACKED_BLACK = toCopy.PACKED_BLACK;
        PACKED_WHITE = toCopy.PACKED_WHITE;
        PACKED_ERROR_COLOR = toCopy.PACKED_ERROR_COLOR;
        PACKED_WARN_COLOR = toCopy.PACKED_WARN_COLOR;
        PACKED_NOTE_COLOR = toCopy.PACKED_NOTE_COLOR;
        PACKED_SHADOW_COLOR = toCopy.PACKED_SHADOW_COLOR;

        // shader, colorLookup, and whiteBlock are not copied, because there isn't much point in having different copies
        // of a ShaderProgram, stateless ColorLookup, or always-identical Texture. They are referenced directly.
        if (toCopy.shader != null)
            shader = toCopy.shader;
        if (toCopy.colorLookup != null)
            colorLookup = toCopy.colorLookup;
    }

    /**
     * Constructs a new Font by reading in a .fnt file with the given name (an internal handle is tried) and loading any
     * images specified in that file. No distance field effect is used.
     * This allows globally adjusting the x and y positions of glyphs in the font, as well as
     * globally adjusting the horizontal and vertical space glyphs take up. Changing these adjustments by small values
     * can drastically improve the appearance of text, but has to be manually edited; every font is quite different.
     * If you want to add empty space around each character, you can add approximately the normal
     * {@link #originalCellWidth} to widthAdjust and about half that to xAdjust; this can be used to make the glyphs fit
     * in square cells.
     *
     * @param fntName      the path and filename of a .fnt file this will load as an internal asset
     * @param xAdjust      how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust      how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust  how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust how many pixels to add to the used height of each character, using more above
     */
    public Font(String fntName,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this(fntName, DistanceFieldType.STANDARD, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    /**
     * Constructs a new Font by reading in a .fnt file with the given name (an internal handle is tried)
     * and loading any images specified in that file. The specified distance field effect is used.
     * This allows globally adjusting the x and y positions of glyphs in the font, as well as
     * globally adjusting the horizontal and vertical space glyphs take up. Changing these adjustments by small values
     * can drastically improve the appearance of text, but has to be manually edited; every font is quite different.
     * If you want to add empty space around each character, you can add approximately the normal
     * {@link #originalCellWidth} to widthAdjust and about half that to xAdjust; this can be used to make the glyphs fit
     * in square cells.
     *
     * @param fntName       the path and filename of a .fnt file this will load as an internal asset
     * @param distanceField determines how edges are drawn; if unsure, you should use {@link DistanceFieldType#STANDARD}
     * @param xAdjust       how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust       how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust   how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust  how many pixels to add to the used height of each character, using more above
     */
    public Font(String fntName, DistanceFieldType distanceField,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this(fntName, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, false);
    }

    /**
     * Constructs a new Font by reading in a .fnt file with the given name (an internal handle is tried)
     * and loading any images specified in that file. The specified distance field effect is used.
     * This allows globally adjusting the x and y positions of glyphs in the font, as well as
     * globally adjusting the horizontal and vertical space glyphs take up. Changing these adjustments by small values
     * can drastically improve the appearance of text, but has to be manually edited; every font is quite different.
     * If you want to add empty space around each character, you can add approximately the normal
     * {@link #originalCellWidth} to widthAdjust and about half that to xAdjust; this can be used to make the glyphs fit
     * in square cells.
     *
     * @param fntName        the path and filename of a .fnt file this will load as an internal asset
     * @param distanceField  determines how edges are drawn; if unsure, you should use {@link DistanceFieldType#STANDARD}
     * @param xAdjust        how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust        how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust    how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust   how many pixels to add to the used height of each character, using more above
     * @param makeGridGlyphs true if this should use its own way of rendering box-drawing/block-element glyphs, ignoring any in the font file
     */
    public Font(String fntName, DistanceFieldType distanceField,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs) {
        this.setDistanceField(distanceField);
        FileHandle fntHandle;
        if ((fntHandle = Gdx.files.internal(fntName)).exists()) {
            loadFNT(fntHandle, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
        } else {
            throw new RuntimeException("Missing font file: " + fntName);
        }
    }

    /**
     * Constructs a new Font by reading in a .fnt file from the given FileHandle and loading any images specified in
     * that file. The specified distance field effect is used.
     * This allows globally adjusting the x and y positions of glyphs in the font, as well as
     * globally adjusting the horizontal and vertical space glyphs take up. Changing these adjustments by small values
     * can drastically improve the appearance of text, but has to be manually edited; every font is quite different.
     * If you want to add empty space around each character, you can add approximately the normal
     * {@link #originalCellWidth} to widthAdjust and about half that to xAdjust; this can be used to make the glyphs fit
     * in square cells.
     *
     * @param fntHandle      the FileHandle holding the path to a .fnt file
     * @param distanceField  determines how edges are drawn; if unsure, you should use {@link DistanceFieldType#STANDARD}
     * @param xAdjust        how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust        how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust    how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust   how many pixels to add to the used height of each character, using more above
     * @param makeGridGlyphs true if this should use its own way of rendering box-drawing/block-element glyphs, ignoring any in the font file
     */
    public Font(FileHandle fntHandle, DistanceFieldType distanceField,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs) {
        this.setDistanceField(distanceField);
        if (fntHandle.exists()) {
            loadFNT(fntHandle, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
        } else {
            throw new RuntimeException("Missing font file: " + fntHandle.name());
        }
    }

    /**
     * Constructs a new Font by reading in a Texture from the given named path (internal is tried),
     * and no distance field effect.
     * This allows globally adjusting the x and y positions of glyphs in the font, as well as
     * globally adjusting the horizontal and vertical space glyphs take up. Changing these adjustments by small values
     * can drastically improve the appearance of text, but has to be manually edited; every font is quite different.
     * If you want to add empty space around each character, you can add approximately the normal
     * {@link #originalCellWidth} to widthAdjust and about half that to xAdjust; this can be used to make the glyphs fit
     * in square cells.
     *
     * @param fntName      the path and filename of a .fnt file this will load as an internal asset
     * @param textureName  the path and filename of a texture file this will load as an internal asset
     * @param xAdjust      how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust      how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust  how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust how many pixels to add to the used height of each character, using more above
     */
    public Font(String fntName, String textureName,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this(fntName, textureName, DistanceFieldType.STANDARD, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    /**
     * Constructs a new Font by reading in a Texture from the given named path (internal is tried),
     * and the specified distance field effect.
     * This allows globally adjusting the x and y positions of glyphs in the font, as well as
     * globally adjusting the horizontal and vertical space glyphs take up. Changing these adjustments by small values
     * can drastically improve the appearance of text, but has to be manually edited; every font is quite different.
     * If you want to add empty space around each character, you can add approximately the normal
     * {@link #originalCellWidth} to widthAdjust and about half that to xAdjust; this can be used to make the glyphs fit
     * in square cells.
     *
     * @param fntName       the path and filename of a .fnt file this will load as an internal asset
     * @param textureName   the path and filename of a texture file this will load as an internal asset
     * @param distanceField determines how edges are drawn; if unsure, you should use {@link DistanceFieldType#STANDARD}
     * @param xAdjust       how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust       how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust   how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust  how many pixels to add to the used height of each character, using more above
     */
    public Font(String fntName, String textureName, DistanceFieldType distanceField,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this(fntName, textureName, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, false);
    }

    /**
     * Constructs a new Font by reading in a Texture from the given named path (internal is tried),
     * and the specified distance field effect.
     * This allows globally adjusting the x and y positions of glyphs in the font, as well as
     * globally adjusting the horizontal and vertical space glyphs take up. Changing these adjustments by small values
     * can drastically improve the appearance of text, but has to be manually edited; every font is quite different.
     * If you want to add empty space around each character, you can add approximately the normal
     * {@link #originalCellWidth} to widthAdjust and about half that to xAdjust; this can be used to make the glyphs fit
     * in square cells.
     *
     * @param fntName        the path and filename of a .fnt file this will load as an internal asset
     * @param textureName    the path and filename of a texture file this will load as an internal asset
     * @param distanceField  determines how edges are drawn; if unsure, you should use {@link DistanceFieldType#STANDARD}
     * @param xAdjust        how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust        how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust    how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust   how many pixels to add to the used height of each character, using more above
     * @param makeGridGlyphs true if this should use its own way of rendering box-drawing/block-element glyphs, ignoring any in the font file
     */
    public Font(String fntName, String textureName, DistanceFieldType distanceField,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs) {
        this.setDistanceField(distanceField);
        FileHandle textureHandle;
        if(textureName == null) {
            parents = Array.of(true, 1, TextureRegion.class);
            parents.add(new TexturelessRegion());
        } else if ((textureHandle = Gdx.files.internal(textureName)).exists()) {
            parents = Array.with(new TextureRegion(new Texture(textureHandle)));
            if (distanceField != DistanceFieldType.STANDARD) {
                parents.first().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            }
        } else {
            throw new RuntimeException("Missing texture file: " + textureName);
        }
        FileHandle fntHandle;
        if ((fntHandle = Gdx.files.internal(fntName)).exists()) {
            loadFNT(fntHandle, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
        } else {
            throw new RuntimeException("Missing font file: " + fntName);
        }
    }

    /**
     * Constructs a font using the given TextureRegion that holds all of its glyphs, with no distance field effect.
     * This allows globally adjusting the x and y positions of glyphs in the font, as well as
     * globally adjusting the horizontal and vertical space glyphs take up. Changing these adjustments by small values
     * can drastically improve the appearance of text, but has to be manually edited; every font is quite different.
     * If you want to add empty space around each character, you can add approximately the normal
     * {@link #originalCellWidth} to widthAdjust and about half that to xAdjust; this can be used to make the glyphs fit
     * in square cells.
     *
     * @param fntName       the path and filename of a .fnt file this will load as an internal asset
     * @param textureRegion an existing TextureRegion, typically inside a larger TextureAtlas
     * @param xAdjust       how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust       how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust   how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust  how many pixels to add to the used height of each character, using more above
     */
    public Font(String fntName, TextureRegion textureRegion,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this(fntName, textureRegion, DistanceFieldType.STANDARD, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    /**
     * Constructs a font based off of an AngelCode BMFont .fnt file and the given TextureRegion that holds all of its
     * glyphs, with the specified distance field effect.
     * This allows globally adjusting the x and y positions of glyphs in the font, as well as
     * globally adjusting the horizontal and vertical space glyphs take up. Changing these adjustments by small values
     * can drastically improve the appearance of text, but has to be manually edited; every font is quite different.
     * If you want to add empty space around each character, you can add approximately the normal
     * {@link #originalCellWidth} to widthAdjust and about half that to xAdjust; this can be used to make the glyphs fit
     * in square cells.
     *
     * @param fntName       the path and filename of a .fnt file this will load as an internal asset
     * @param textureRegion an existing TextureRegion, typically inside a larger TextureAtlas
     * @param distanceField determines how edges are drawn; if unsure, you should use {@link DistanceFieldType#STANDARD}
     * @param xAdjust       how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust       how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust   how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust  how many pixels to add to the used height of each character, using more above
     */
    public Font(String fntName, TextureRegion textureRegion, DistanceFieldType distanceField,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this(fntName, textureRegion, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, false);
    }

    /**
     * Constructs a font based off of an AngelCode BMFont .fnt file and the given TextureRegion that holds all of its
     * glyphs, with the specified distance field effect.
     * This allows globally adjusting the x and y positions of glyphs in the font, as well as
     * globally adjusting the horizontal and vertical space glyphs take up. Changing these adjustments by small values
     * can drastically improve the appearance of text, but has to be manually edited; every font is quite different.
     * If you want to add empty space around each character, you can add approximately the normal
     * {@link #originalCellWidth} to widthAdjust and about half that to xAdjust; this can be used to make the glyphs fit
     * in square cells.
     *
     * @param fntName        the path and filename of a .fnt file this will load as an internal asset
     * @param textureRegion  an existing TextureRegion, typically inside a larger TextureAtlas
     * @param distanceField  determines how edges are drawn; if unsure, you should use {@link DistanceFieldType#STANDARD}
     * @param xAdjust        how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust        how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust    how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust   how many pixels to add to the used height of each character, using more above
     * @param makeGridGlyphs true if this should use its own way of rendering box-drawing/block-element glyphs, ignoring any in the font file
     */
    public Font(String fntName, TextureRegion textureRegion, DistanceFieldType distanceField,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs) {
        this.setDistanceField(distanceField);
        this.parents = Array.with(textureRegion);
        if (textureRegion.getTexture() != null && distanceField != DistanceFieldType.STANDARD) {
            textureRegion.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        FileHandle fntHandle;
        if ((fntHandle = Gdx.files.internal(fntName)).exists()) {
            loadFNT(fntHandle, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
        } else {
            throw new RuntimeException("Missing font file: " + fntName);
        }
    }

    /**
     * Constructs a font based off of an AngelCode BMFont .fnt file and the given TextureRegion that holds all of its
     * glyphs, with the specified distance field effect.
     * This allows globally adjusting the x and y positions of glyphs in the font, as well as
     * globally adjusting the horizontal and vertical space glyphs take up. Changing these adjustments by small values
     * can drastically improve the appearance of text, but has to be manually edited; every font is quite different.
     * If you want to add empty space around each character, you can add approximately the normal
     * {@link #originalCellWidth} to widthAdjust and about half that to xAdjust; this can be used to make the glyphs fit
     * in square cells.
     *
     * @param fntHandle      the FileHandle holding the path to a .fnt file
     * @param textureRegion  an existing TextureRegion, typically inside a larger TextureAtlas
     * @param distanceField  determines how edges are drawn; if unsure, you should use {@link DistanceFieldType#STANDARD}
     * @param xAdjust        how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust        how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust    how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust   how many pixels to add to the used height of each character, using more above
     * @param makeGridGlyphs true if this should use its own way of rendering box-drawing/block-element glyphs, ignoring any in the font file
     */
    public Font(FileHandle fntHandle, TextureRegion textureRegion, DistanceFieldType distanceField,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs) {
        this.setDistanceField(distanceField);
        this.parents = Array.with(textureRegion);
        if (textureRegion.getTexture() != null && distanceField != DistanceFieldType.STANDARD) {
            textureRegion.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        if (fntHandle.exists()) {
            loadFNT(fntHandle, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
        } else {
            throw new RuntimeException("Missing font file: " + fntHandle.name());
        }
    }

    /**
     * Constructs a font based off of an AngelCode BMFont .fnt file and the given TextureRegion Array, with no distance
     * field effect. This allows globally adjusting the x and y positions of glyphs in the font, as well as
     * globally adjusting the horizontal and vertical space glyphs take up. Changing these adjustments by small values
     * can drastically improve the appearance of text, but has to be manually edited; every font is quite different.
     * If you want to add empty space around each character, you can add approximately the normal
     * {@link #originalCellWidth} to widthAdjust and about half that to xAdjust; this can be used to make the glyphs fit
     * in square cells.
     *
     * @param fntName        the path and filename of a .fnt file this will load as an internal asset
     * @param textureRegions an Array of TextureRegions that will be used in order as the .fnt file uses more pages
     * @param xAdjust        how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust        how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust    how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust   how many pixels to add to the used height of each character, using more above
     */
    public Font(String fntName, Array<TextureRegion> textureRegions,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this(fntName, textureRegions, DistanceFieldType.STANDARD, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    /**
     * Constructs a font based off of an AngelCode BMFont .fnt file, with the given TextureRegion Array and specified
     * distance field effect. This allows globally adjusting the x and y positions of glyphs in the font, as well as
     * globally adjusting the horizontal and vertical space glyphs take up. Changing these adjustments by small values
     * can drastically improve the appearance of text, but has to be manually edited; every font is quite different.
     * If you want to add empty space around each character, you can add approximately the normal
     * {@link #originalCellWidth} to widthAdjust and about half that to xAdjust; this can be used to make the glyphs fit
     * in square cells.
     *
     * @param fntName        the path and filename of a .fnt file this will load as an internal asset
     * @param textureRegions an Array of TextureRegions that will be used in order as the .fnt file uses more pages
     * @param distanceField  determines how edges are drawn; if unsure, you should use {@link DistanceFieldType#STANDARD}
     * @param xAdjust        how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust        how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust    how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust   how many pixels to add to the used height of each character, using more above
     */
    public Font(String fntName, Array<TextureRegion> textureRegions, DistanceFieldType distanceField,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this(fntName, textureRegions, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, false);
    }

    /**
     * Constructs a font based off of an AngelCode BMFont .fnt file, with the given TextureRegion Array and specified
     * distance field effect. This allows globally adjusting the x and y positions of glyphs in the font, as well as
     * globally adjusting the horizontal and vertical space glyphs take up. Changing these adjustments by small values
     * can drastically improve the appearance of text, but has to be manually edited; every font is quite different.
     * If you want to add empty space around each character, you can add approximately the normal
     * {@link #originalCellWidth} to widthAdjust and about half that to xAdjust; this can be used to make the glyphs fit
     * in square cells.
     *
     * @param fntName        the path and filename of a .fnt file this will load as an internal asset
     * @param textureRegions an Array of TextureRegions that will be used in order as the .fnt file uses more pages
     * @param distanceField  determines how edges are drawn; if unsure, you should use {@link DistanceFieldType#STANDARD}
     * @param xAdjust        how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust        how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust    how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust   how many pixels to add to the used height of each character, using more above
     * @param makeGridGlyphs true if this should use its own way of rendering box-drawing/block-element glyphs, ignoring any in the font file
     */
    public Font(String fntName, Array<TextureRegion> textureRegions, DistanceFieldType distanceField,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs) {
        this.setDistanceField(distanceField);
        this.parents = textureRegions;
        if (distanceField != DistanceFieldType.STANDARD && textureRegions != null) {
            for (TextureRegion parent : textureRegions)
                if(parent.getTexture() != null)
                    parent.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        FileHandle fntHandle;
        if ((fntHandle = Gdx.files.internal(fntName)).exists()) {
            loadFNT(fntHandle, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
        } else {
            throw new RuntimeException("Missing font file: " + fntName);
        }
    }

    /**
     * Constructs a font based off of an AngelCode BMFont .fnt file, with the given TextureRegion Array and specified
     * distance field effect. This allows globally adjusting the x and y positions of glyphs in the font, as well as
     * globally adjusting the horizontal and vertical space glyphs take up. Changing these adjustments by small values
     * can drastically improve the appearance of text, but has to be manually edited; every font is quite different.
     * If you want to add empty space around each character, you can add approximately the normal
     * {@link #originalCellWidth} to widthAdjust and about half that to xAdjust; this can be used to make the glyphs fit
     * in square cells.
     *
     * @param fntHandle      the FileHandle holding the path to a .fnt file
     * @param textureRegions an Array of TextureRegions that will be used in order as the .fnt file uses more pages
     * @param distanceField  determines how edges are drawn; if unsure, you should use {@link DistanceFieldType#STANDARD}
     * @param xAdjust        how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust        how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust    how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust   how many pixels to add to the used height of each character, using more above
     * @param makeGridGlyphs true if this should use its own way of rendering box-drawing/block-element glyphs, ignoring any in the font file
     */
    public Font(FileHandle fntHandle, Array<TextureRegion> textureRegions, DistanceFieldType distanceField,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs) {
        this.setDistanceField(distanceField);
        this.parents = textureRegions;
        if (distanceField != DistanceFieldType.STANDARD && textureRegions != null) {
            for (TextureRegion parent : textureRegions)
                if(parent.getTexture() != null)
                    parent.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        if (fntHandle.exists()) {
            loadFNT(fntHandle, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
        } else {
            throw new RuntimeException("Missing font file: " + fntHandle.name());
        }
    }

    /**
     * Constructs a new Font from the existing BitmapFont, using its same Textures and TextureRegions for glyphs, and
     * without a distance field effect or any adjustments to position except for a y offset equal to
     * {@link BitmapFont#getDescent()}.
     *
     * @param bmFont an existing BitmapFont that will be copied in almost every way this can
     */
    public Font(BitmapFont bmFont) {
        this(bmFont, DistanceFieldType.STANDARD, 0f, 0, 0f, 0f, false);
    }

    /**
     * Constructs a new Font from the existing BitmapFont, using its same Textures and TextureRegions for glyphs, and
     * without a distance field effect. Adds a value to {@code yAdjust} equal to {@link BitmapFont#getDescent()}.
     *
     * @param bmFont       an existing BitmapFont that will be copied in almost every way this can
     * @param xAdjust      how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust      how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust  how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust how many pixels to add to the used height of each character, using more above
     */
    public Font(BitmapFont bmFont,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this(bmFont, DistanceFieldType.STANDARD, xAdjust, yAdjust, widthAdjust, heightAdjust, false);
    }

    /**
     * Constructs a new Font from the existing BitmapFont, using its same Textures and TextureRegions for glyphs, and
     * with the specified distance field effect. Adds a value to {@code yAdjust} equal to
     * {@link BitmapFont#getDescent()}.
     *
     * @param bmFont        an existing BitmapFont that will be copied in almost every way this can
     * @param distanceField determines how edges are drawn; if unsure, you should use {@link DistanceFieldType#STANDARD}
     * @param xAdjust       how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust       how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust   how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust  how many pixels to add to the used height of each character, using more above
     */
    public Font(BitmapFont bmFont, DistanceFieldType distanceField,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this(bmFont, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, false);
    }

    /**
     * Constructs a new Font from the existing BitmapFont, using its same Textures and TextureRegions for glyphs, and
     * with the specified distance field effect. Adds a value to {@code yAdjust} equal to
     * {@link BitmapFont#getDescent()}.
     *
     * @param bmFont         an existing BitmapFont that will be copied in almost every way this can
     * @param distanceField  determines how edges are drawn; if unsure, you should use {@link DistanceFieldType#STANDARD}
     * @param xAdjust        how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust        how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust    how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust   how many pixels to add to the used height of each character, using more above
     * @param makeGridGlyphs true if this should use its own way of rendering box-drawing/block-element glyphs, ignoring any in the font file
     */
    public Font(BitmapFont bmFont, DistanceFieldType distanceField,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs) {
        this.setDistanceField(distanceField);
        this.parents = bmFont.getRegions();
        if (distanceField != DistanceFieldType.STANDARD && parents != null) {
            for (TextureRegion parent : parents) {
                if(parent.getTexture() != null)
                    parent.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            }
        }
        float offsetY = 0f;
        if((parents != null) && parents.notEmpty()) {
            TextureRegion parent = parents.first();
            if(parent instanceof TextureAtlas.AtlasRegion){
                offsetY = ((TextureAtlas.AtlasRegion) parent).originalHeight
                        - ((TextureAtlas.AtlasRegion) parent).packedHeight
                        - ((TextureAtlas.AtlasRegion) parent).offsetY;
            }
        }
        BitmapFontData data = bmFont.getData();
        mapping = new IntMap<>(128);
        float minWidth = Float.MAX_VALUE;

        this.xAdjust = xAdjust;
        this.yAdjust = yAdjust;
        this.widthAdjust = widthAdjust;
        this.heightAdjust = heightAdjust;

        float size = data.lineHeight * 0.8f;
        descent = size * -0.25f;//metrics.getFloat("descender", -0.25f);
        originalCellHeight = cellHeight = heightAdjust - descent + size;// * metrics.getFloat("lineHeight", 1f);

//        descent = data.capHeight + data.ascent - data.lineHeight;
//        cellHeight = heightAdjust + data.lineHeight - descent * 0.25f;

        // Needed to make emoji and other texture regions appear at a reasonable height on the line.
        // Also moves the descender so that it isn't below the baseline, which causes issues.
//        yAdjust += data.xHeight - data.ascent + descent;
//        yAdjust += data.xHeight - data.ascent;
//        yAdjust = (int)(yAdjust + data.capHeight - data.lineHeight - data.descent);
//        yAdjust -= descent;
//        yAdjust += 2;
//        yAdjust += descent + bmFont.getLineHeight() * 0.5f;

        // SHOULD USE metrics ?
        underY = 0.05f;//0.5f * metrics.getFloat("underlineY", -0.1f);
        // SHOULD USE metrics ?
        strikeY = 0.15f;//metrics.getFloat("strikeY", 0f);
        strikeBreadth = underBreadth = -0.375f;
        if(makeGridGlyphs){
            underLength = strikeLength = 0.05f;
            underX = strikeX = -0.05f;
        } else {
            underLength = strikeLength = 0.0f;
            underX = strikeX = 0.0f;
        }

        fancyY = 2f;

//        fancyY += 0.5f;

        for (BitmapFont.Glyph[] page : data.glyphs) {
            if (page == null) continue;
            for (BitmapFont.Glyph glyph : page) {
                if (glyph != null) {
                    float x = glyph.srcX + data.padLeft, y = glyph.srcY + data.padTop, w = glyph.width - data.padLeft, h = glyph.height - data.padTop, a = glyph.xadvance,
                            yOffset = glyph.yoffset;

                    // More of this may need to be copied in from BitmapFontData.setGlyphRegion() .
                    // It will probably also need to be modified, likely to reverse directions.
                    if (offsetY > 0) {
                        y -= offsetY;
                        y = Math.max(0, y);
                        float y2 = y + h - offsetY, regionHeight = bmFont.getRegion(glyph.page).getRegionHeight();
                        if (y2 > regionHeight) {
                            float amount = y2 - regionHeight;
                            h -= amount;
                            yOffset += amount;
                        }
                    }

                    if (glyph.id != 9608) // full block
                        minWidth = Math.min(minWidth, a);
                    cellWidth = Math.max(a, cellWidth);
//                    cellHeight = Math.max(h + heightAdjust, cellHeight);

                    GlyphRegion gr = new GlyphRegion(bmFont.getRegion(glyph.page), x, y, w, h);
                    if (glyph.id == 10) { // newline
                        a = 0;
                        gr.offsetX = 0;
                    } else if (makeGridGlyphs && BlockUtils.isBlockGlyph(glyph.id)) {
                        gr.offsetX = Float.NaN;
                    } else {
                        gr.offsetX = glyph.xoffset + xAdjust;
                    }
                    gr.offsetY = yAdjust - h - yOffset;
                    gr.xAdvance = a + widthAdjust;
                    mapping.put(glyph.id & 0xFFFF, gr);
                    if (glyph.kerning != null) {
                        if (kerning == null) kerning = new IntFloatMap(128);
                        for (int b = 0; b < glyph.kerning.length; b++) {
                            byte[] kern = glyph.kerning[b];
                            if (kern != null) {
                                int k;
                                for (int i = 0; i < 512; i++) {
                                    k = kern[i];
                                    if (k != 0) {
                                        kerning.put(glyph.id << 16 | (b << 9 | i), k);
                                    }
                                    if ((b << 9 | i) == '[') {
                                        kerning.put(glyph.id << 16 | 2, k);
                                    }
                                }
                            }
                        }
                    }
                    if ((glyph.id & 0xFFFF) == '[') {
                        mapping.put(2, gr);
                        if (glyph.kerning != null) {
                            for (int b = 0; b < glyph.kerning.length; b++) {
                                byte[] kern = glyph.kerning[b];
                                if (kern != null) {
                                    int k;
                                    for (int i = 0; i < 512; i++) {
                                        k = kern[i];
                                        if (k != 0) {
                                            kerning.put(2 << 16 | (b << 9 | i), k);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!mapping.containsKey(' ')) {
            Gdx.app.error("FONT", "Font is missing a space character! Aborting!");
            throw new RuntimeException("Cannot create a font withoit a space character.");
        }
        else {
            mapping.put('\r', mapping.get(' '));
            // U+200B is the zero-width space
            mapping.put('\u200B', new Font.GlyphRegion(mapping.get(' '), 0f, 0f, 0f));
        }
        // Newlines should be equivalent to zero-width spaces in terms of rendering.
        if (mapping.containsKey('\n')) {
            GlyphRegion gr = mapping.get('\n');
            gr.setRegionWidth(0);
            gr.setRegionHeight(0);
            gr.xAdvance = 0;
        } else {
            GlyphRegion newline = new GlyphRegion(mapping.get('\u200B'));
            newline.setRegionWidth(0);
            newline.setRegionHeight(0);
            newline.xAdvance = 0;
            mapping.put('\n', newline);
        }
        solidBlock = '█';

        if (makeGridGlyphs) {
            GlyphRegion block;
            Pixmap temp = new Pixmap(3, 3, Pixmap.Format.RGBA8888);
            temp.setColor(Color.WHITE);
            temp.fill();
            // This is only OK because a BitmapFont requires a Texture anyway, so the headless case is impossible.
            whiteBlock = new Texture(3, 3, Pixmap.Format.RGBA8888);
            whiteBlock.draw(temp, 0, 0);
            mapping.put(solidBlock, block = new GlyphRegion(new TextureRegion(whiteBlock, 1, 1, 1, 1)));
            temp.dispose();
            for (int i = 0x2500; i < 0x2500 + BlockUtils.BOX_DRAWING.length; i++) {
                if (BlockUtils.isBlockGlyph(i)) {
                    mapping.put(i, new GlyphRegion(block, Float.NaN, cellHeight, cellWidth));
                }
            }
        }
        defaultValue = mapping.get(data.missingGlyph == null ? ' ' : data.missingGlyph.id, mapping.get(' ', mapping.values().next()));
        originalCellWidth = cellWidth;
        originalCellHeight = cellHeight;
        isMono = minWidth == cellWidth && kerning == null;
        integerPosition = bmFont.usesIntegerPositions();

        inlineImageOffsetX = 0f;
        inlineImageOffsetY = 0f;
        inlineImageXAdvance = 0f;
        inlineImageStretch = 1f;

        scale(bmFont.getScaleX(), bmFont.getScaleY());
    }

    /**
     * Constructs a new Font by reading in a SadConsole .font file with the given name (an internal handle is tried)
     * and loading any images specified in that file. This never uses a distance field
     * effect, and always tries to load one image by the path specified in the .font file.
     *
     * @param prefix                a String to prepend to any filenames looked up for this Font (typically a .font file and a .png file)
     * @param fontName              the path and filename of a .font file this will load as an internal asset
     * @param ignoredSadConsoleFlag the value is ignored here; the presence of this parameter says to load a SadConsole .font file
     */
    public Font(String prefix, String fontName, boolean ignoredSadConsoleFlag) {
        this.setDistanceField(DistanceFieldType.STANDARD);
        loadSad(prefix == null ? "" : prefix, fontName);
    }

    /**
     * The gritty parsing code that pulls relevant info from an AngelCode BMFont .fnt file and uses it to assemble the
     * many {@link GlyphRegion}s this has for each glyph.
     *
     * @param fntHandle      the FileHandle holding the path to a .fnt file
     * @param xAdjust        added to the x-position for each glyph in the font
     * @param yAdjust        added to the y-position for each glyph in the font
     * @param widthAdjust    added to the glyph width for each glyph in the font
     * @param heightAdjust   added to the glyph height for each glyph in the font
     * @param makeGridGlyphs true if this should use its own way of rendering box-drawing/block-element glyphs, ignoring any in the font file
     */
    protected void loadFNT(FileHandle fntHandle, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs) {
        String fnt = fntHandle.readString("UTF-8");
        this.xAdjust = xAdjust;
        this.yAdjust = yAdjust;
        this.widthAdjust = widthAdjust;
        this.heightAdjust = heightAdjust;
        int idx;
        idx = StringUtils.indexAfter(fnt, "padding=", 0);
        int padTop = StringUtils.intFromDec(fnt, idx, idx = StringUtils.indexAfter(fnt, ",", idx+1));
        int padRight = StringUtils.intFromDec(fnt, idx, idx = StringUtils.indexAfter(fnt, ",", idx+1));
        int padBottom = StringUtils.intFromDec(fnt, idx, idx = StringUtils.indexAfter(fnt, ",", idx+1));
        int padLeft = StringUtils.intFromDec(fnt, idx, idx = StringUtils.indexAfter(fnt, "lineHeight=", idx+1));

        float rawLineHeight = StringUtils.floatFromDec(fnt, idx, idx = StringUtils.indexAfter(fnt, "base=", idx));
        float baseline = StringUtils.floatFromDec(fnt, idx, idx = StringUtils.indexAfter(fnt, "pages=", idx));
//        descent = baseline - rawLineHeight;
        descent = 0;

        // The SDF and MSDF fonts have essentially garbage for baseline, since Glamer can't accurately guess it.
        // For standard fonts, we incorporate the descender into yAdjust, which seems to be reliable.
//        if(distanceField == DistanceFieldType.STANDARD)
//            yAdjust += descent;
        int pages = StringUtils.intFromDec(fnt, idx, idx = StringUtils.indexAfter(fnt, "\npage id=", idx));
        if (parents == null || parents.size < pages) {
            if (parents == null) parents = new Array<>(true, pages, TextureRegion.class);
            else parents.clear();
            FileHandle textureHandle;
            for (int i = 0; i < pages; i++) {
                String textureName = fnt.substring(idx = StringUtils.indexAfter(fnt, "file=\"", idx), idx = fnt.indexOf('"', idx));
                if(!canUseTextures){
                    parents = Array.of(true, 1, TextureRegion.class);
                    parents.add(new TexturelessRegion());
                } else if ((textureHandle = Gdx.files.internal(textureName)).exists()) {
                    parents.add(new TextureRegion(new Texture(textureHandle)));
                    if (distanceField != DistanceFieldType.STANDARD)
                        parents.peek().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                } else {
                    throw new RuntimeException("Missing texture file: " + textureName);
                }

            }
        }
        int size = StringUtils.intFromDec(fnt, idx = StringUtils.indexAfter(fnt, "\nchars count=", idx), idx = StringUtils.indexAfter(fnt, "\nchar id=", idx));
        mapping = new IntMap<>(size);
        float minWidth = Integer.MAX_VALUE;
        for (int i = 0; i < size; i++) {
            if (idx == fnt.length())
                break;
            int c =    StringUtils.intFromDec(fnt, idx, idx = StringUtils.indexAfter(fnt, " x=", idx));
            float x =  StringUtils.floatFromDec(fnt, idx, idx = StringUtils.indexAfter(fnt, " y=", idx));
            float y =  StringUtils.floatFromDec(fnt, idx, idx = StringUtils.indexAfter(fnt, " width=", idx));
            float w =  StringUtils.floatFromDec(fnt, idx, idx = StringUtils.indexAfter(fnt, " height=", idx));
            float h =  StringUtils.floatFromDec(fnt, idx, idx = StringUtils.indexAfter(fnt, " xoffset=", idx));
            float xo = StringUtils.floatFromDec(fnt, idx, idx = StringUtils.indexAfter(fnt, " yoffset=", idx));
            float yo = StringUtils.floatFromDec(fnt, idx, idx = StringUtils.indexAfter(fnt, " xadvance=", idx));
            float a =  StringUtils.floatFromDec(fnt, idx, idx = StringUtils.indexAfter(fnt, " page=", idx));
            int p =    StringUtils.intFromDec(fnt, idx, idx = StringUtils.indexAfter(fnt, "\nchar id=", idx));

//            x += xAdjust;
//            y += yAdjust;

//            a += widthAdjust;
//            h += heightAdjust;

            if (c != 9608) // full block
                minWidth = Math.min(minWidth, a + widthAdjust);
            GlyphRegion gr = new GlyphRegion(parents.get(p), x, y, w, h);
            if (c == 10) {
                a = 0;
                gr.offsetX = 0;
            } else if (makeGridGlyphs && BlockUtils.isBlockGlyph(c)) {
                gr.offsetX = Float.NaN;
            } else
                gr.offsetX = xo + xAdjust;
            gr.offsetY = yo + yAdjust;
            gr.xAdvance = a + widthAdjust;

            cellWidth = Math.max(a + widthAdjust, cellWidth);
            cellHeight = Math.max(h + heightAdjust, cellHeight);
            if (w * h > 1) {
                descent = Math.min(baseline - h - yo, descent);
//                if(descent != (descent = Math.min(baseline - h - yo, descent)))
//                    chosenDescender = c;
            }
            mapping.put(c, gr);
            if (c == '[') {
                mapping.put(2, gr);
            }
        }
        // TODO: Do we want this?
//        descent += padBottom;

        idx = StringUtils.indexAfter(fnt, "\nkernings count=", 0);
        if (idx < fnt.length()) {
            int kernings = StringUtils.intFromDec(fnt, idx, idx = StringUtils.indexAfter(fnt, "\nkerning first=", idx));
            if(kernings >= 1) {
                kerning = new IntFloatMap(kernings);
                for (int i = 0; i < kernings; i++) {
                    int first = StringUtils.intFromDec(fnt, idx, idx = StringUtils.indexAfter(fnt, " second=", idx));
                    int second = StringUtils.intFromDec(fnt, idx, idx = StringUtils.indexAfter(fnt, " amount=", idx));
                    float amount = StringUtils.floatFromDec(fnt, idx, idx = StringUtils.indexAfter(fnt, "\nkerning first=", idx));
                    kerning.put(first << 16 | second, amount);
                    if (first == '[') {
                        kerning.put(2 << 16 | second, amount);
                    }
                    if (second == '[') {
                        kerning.put(first << 16 | 2, amount);
                    }
                }
            }
        }
        if (!mapping.containsKey(' ')) {
            Gdx.app.error("FONT", "Font is missing a space character! Aborting!");
            throw new RuntimeException("Cannot create a font withoit a space character.");
        }
        else {
            mapping.put('\r', mapping.get(' '));
            // U+200B is the zero-width space
            mapping.put('\u200B', new Font.GlyphRegion(mapping.get(' '), 0f, 0f, 0f));
        }
        // Newlines should be equivalent to zero-width spaces in terms of rendering.
        if (mapping.containsKey('\n')) {
            GlyphRegion gr = mapping.get('\n');
            gr.setRegionWidth(0);
            gr.setRegionHeight(0);
            gr.xAdvance = 0f;
        } else {
            GlyphRegion newline = new GlyphRegion(mapping.get('\u200B'));
            newline.setRegionWidth(0);
            newline.setRegionHeight(0);
            newline.xAdvance = 0;
            mapping.put('\n', newline);
        }
        solidBlock =
                mapping.containsKey(9608) ? '\u2588' : '\uFFFF';
        if (makeGridGlyphs) {
            GlyphRegion block = mapping.get(solidBlock, null);
            if (block == null && canUseTextures) {
                Pixmap temp = new Pixmap(3, 3, Pixmap.Format.RGBA8888);
                temp.setColor(Color.WHITE);
                temp.fill();
                whiteBlock = new Texture(3, 3, Pixmap.Format.RGBA8888);
                whiteBlock.draw(temp, 0, 0);
                solidBlock = '\u2588';
                mapping.put(solidBlock, block = new GlyphRegion(new TextureRegion(whiteBlock, 1, 1, 1, 1)));
                temp.dispose();
            }
            for (int i = 0x2500; i < 0x2500 + BlockUtils.BOX_DRAWING.length; i++) {
                if(BlockUtils.isBlockGlyph(i)) {
                    GlyphRegion gr = new GlyphRegion(block);
                    gr.offsetX = Float.NaN;
                    gr.xAdvance = cellWidth;
                    gr.offsetY = cellHeight;
                    mapping.put(i, gr);
                }
            }
        } else if(!canUseTextures) {
            solidBlock = '\u2588';
            mapping.put(solidBlock, new GlyphRegion(new TexturelessRegion()));
        } else if (!mapping.containsKey(solidBlock)) {
            Pixmap temp = new Pixmap(3, 3, Pixmap.Format.RGBA8888);
            temp.setColor(Color.WHITE);
            temp.fill();
            whiteBlock = new Texture(3, 3, Pixmap.Format.RGBA8888);
            whiteBlock.draw(temp, 0, 0);
            solidBlock = '\u2588';
            mapping.put(solidBlock, new GlyphRegion(new TextureRegion(whiteBlock, 1, 1, 1, 1)));
            temp.dispose();
        }
        defaultValue = mapping.get(' ', mapping.get(0));
        originalCellWidth = cellWidth;
        originalCellHeight = cellHeight -= descent * 0.25f;
        isMono = minWidth == cellWidth && kerning == null;

//        underY = 0.05f;
//        strikeY = 0.15f;
//        strikeBreadth = underBreadth = -0.375f;
//        if(makeGridGlyphs){
//            underLength = strikeLength = 0.05f;
//            underX = strikeX = -0.05f;
//        } else {
//            underLength = strikeLength = 0.0f;
//            underX = strikeX = 0.0f;
//        }
//        fancyY = 2f;

        inlineImageOffsetX = 0f;
        inlineImageOffsetY = 0f;
        inlineImageXAdvance = 0f;
        inlineImageStretch = 1f;
    }

    /**
     * The parsing code that pulls relevant info from a SadConsole .font configuration file and uses it to assemble the
     * many {@link GlyphRegion}s this has for each glyph.
     *
     * @param fntName the name of a font file this will load from an internal file handle
     */
    protected void loadSad(String prefix, String fntName) {
        FileHandle fntHandle;
        JsonValue fnt;
        JsonReader reader = new JsonReader();
        if ((fntHandle = Gdx.files.internal(prefix + fntName)).exists()) {
            fnt = reader.parse(fntHandle);
        } else {
            throw new RuntimeException("Missing font file: " + prefix + fntName);
        }
        int pages = 1;
        TextureRegion parent;
        if (parents == null || parents.size == 0) {
            if (parents == null) parents = new Array<>(true, pages, TextureRegion.class);
            FileHandle textureHandle;
            String textureName = fnt.getString("FilePath");
            if (!canUseTextures) {
                parents.add(parent = new TextureRegion(null, 1, 1));
            } else if ((textureHandle = Gdx.files.internal(prefix + textureName)).exists()) {
                parents.add(parent = new TextureRegion(new Texture(textureHandle)));
            } else {
                throw new RuntimeException("Missing texture file: " + prefix + textureName);
            }
        } else parent = parents.first();

        int columns = fnt.getInt("Columns");
        int padding = fnt.getInt("GlyphPadding");
        cellHeight = fnt.getInt("GlyphHeight");
        cellWidth = fnt.getInt("GlyphWidth");
        descent = Math.round(cellHeight * -0.25f);
        int rows = (parent.getRegionHeight() - padding) / ((int) cellHeight + padding);
        int size = rows * columns;
        mapping = new IntMap<>(size + 1);
        for (int y = 0, c = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++, c++) {
                GlyphRegion gr = new GlyphRegion(parent, x * ((int) cellWidth + padding) + padding, y * ((int) cellHeight + padding) + padding, (int) cellWidth, (int) cellHeight);
                gr.offsetX = 0;
                gr.offsetY = descent;
                if (c == 10) {
                    gr.xAdvance = 0;
                } else {
                    gr.xAdvance = cellWidth;
                }
                mapping.put(c, gr);
                if (c == '[') {
                    if (mapping.containsKey(2))
                        mapping.put(size, mapping.get(2));
                    mapping.put(2, gr);
                }
            }
        }
        solidBlock = (char) fnt.getInt("SolidGlyphIndex");
        if (!mapping.containsKey(' ')) {
            Gdx.app.error("FONT", "Font is missing a space character! Aborting!");
            throw new RuntimeException("Cannot create a font withoit a space character.");
        }
        else {
            mapping.put('\r', mapping.get(' '));
            // U+200B is the zero-width space
            mapping.put('\u200B', new Font.GlyphRegion(mapping.get(' '), 0f, 0f, 0f));
        }
        // Newlines should be equivalent to zero-width spaces in terms of rendering.
        if (mapping.containsKey('\n')) {
            GlyphRegion gr = mapping.get('\n');
            gr.setRegionWidth(0);
            gr.setRegionHeight(0);
            gr.xAdvance = 0f;
        } else {
            GlyphRegion newline = new GlyphRegion(mapping.get('\u200B'));
            newline.setRegionWidth(0);
            newline.setRegionHeight(0);
            newline.xAdvance = 0;
            mapping.put('\n', newline);
        }
        defaultValue = mapping.get(' ', mapping.get(0));
        originalCellWidth = this.cellWidth;
        originalCellHeight = this.cellHeight;
        integerPosition = true;
        isMono = true;

        underY += 0.375f;
        strikeY += 0.375f;

        inlineImageOffsetX = 0f;
        inlineImageOffsetY = 0f;
        inlineImageXAdvance = 0f;
        inlineImageStretch = 1f;
    }

    /**
     * Creates a Font from a "Structured JSON" file produced by Chlumsky's msdf-atlas-gen tool, and uses it to assemble
     * the many {@link GlyphRegion}s this has for each glyph. Reads the distance field type from the JSON. This always
     * tries to load a PNG file with the same filename as (and different extension from) the JSON file, trying an
     * internal file path. No matter what extension the JSON font uses, this will be able to load it
     * (so this can load compressed .dat fonts as well as .json, .json.lzma, .ubj, and .ubj.lzma).
     * This always makes grid glyphs.
     * <br>
     * <a href="https://github.com/Chlumsky/msdf-atlas-gen">The msdf-atlas-gen tool</a> can actually produce MSDF, SDF,
     * PSDF, and "soft masked" (standard) fonts. It can't produce AngelCode BMFont .fnt files, but the JSON it can
     * produce is fairly full-featured. Its handling of metrics seems significantly better than the tools that work with
     * AngelCode BMFont .fnt files, so all but a few fonts in {@link KnownFonts} use JSON. A specialized version
     * of msdf-atlas-gen that only writes structured JSON files (and optimizes the files at the right size) is available
     * at <a href="https://github.com/tommyettinger/fontwriter">fontwriter</a> (Windows-only, for now).
     *
     * @param jsonName the name of a structured JSON font file this will load from an internal file handle
     * @param ignoredStructuredJsonFlag only present to distinguish this from other constructors; ignored
     */
    public Font(String jsonName, boolean ignoredStructuredJsonFlag) {
        this(jsonName, !canUseTextures
                ? new TexturelessRegion((TextureRegion) null, 0, 0, 2048, 2048)
                : new TextureRegion(new Texture(Gdx.files.internal(jsonName.replaceFirst("\\..+$", ".png"))
        )), 0f, 0f, 0f, 0f, true, ignoredStructuredJsonFlag);
    }

    /**
     * Creates a Font from a "Structured JSON" file produced by Chlumsky's msdf-atlas-gen tool, and uses it to assemble
     * the many {@link GlyphRegion}s this has for each glyph. Reads the distance field type from the JSON.
     * <br>
     * <a href="https://github.com/Chlumsky/msdf-atlas-gen">The msdf-atlas-gen tool</a> can actually produce MSDF, SDF,
     * PSDF, and "soft masked" (standard) fonts. It can't produce AngelCode BMFont .fnt files, but the JSON it can
     * produce is fairly full-featured. Its handling of metrics seems significantly better than the tools that work with
     * AngelCode BMFont .fnt files, all but a few fonts in {@link KnownFonts} use JSON. A specialized version
     * of msdf-atlas-gen that only writes structured JSON files (and optimizes the files at the right size) is available
     * at <a href="https://github.com/tommyettinger/fontwriter">fontwriter</a> (Windows-only, for now).
     *
     * @param jsonName the name of a structured JSON font file this will load from an internal file handle
     * @param textureRegion a non-null TextureRegion, often taking up all of a Texture, that stores the images of the glyphs
     * @param makeGridGlyphs true if this should use its own way of rendering box-drawing/block-element glyphs, ignoring any in the font file
     * @param ignoredStructuredJsonFlag only present to distinguish this from other constructors; ignored
     */
    public Font(String jsonName, TextureRegion textureRegion,
                boolean makeGridGlyphs,
                boolean ignoredStructuredJsonFlag) {
        this(jsonName, textureRegion, 0f, 0f, 0f, 0f, makeGridGlyphs, ignoredStructuredJsonFlag);
    }

    /**
     * Creates a Font from a "Structured JSON" file produced by Chlumsky's msdf-atlas-gen tool, and uses it to assemble
     * the many {@link GlyphRegion}s this has for each glyph. Reads the distance field type from the JSON.
     * <br>
     * <a href="https://github.com/Chlumsky/msdf-atlas-gen">The msdf-atlas-gen tool</a> can actually produce MSDF, SDF,
     * PSDF, and "soft masked" (standard) fonts. It can't produce AngelCode BMFont .fnt files, but the JSON it can
     * produce is fairly full-featured. Its handling of metrics seems significantly better than the tools that work with
     * AngelCode BMFont .fnt files, all but a few fonts in {@link KnownFonts} use JSON. A specialized version
     * of msdf-atlas-gen that only writes structured JSON files (and optimizes the files at the right size) is available
     * at <a href="https://github.com/tommyettinger/fontwriter">fontwriter</a> (Windows-only, for now).
     *
     * @param jsonName the name of a structured JSON font file this will load from an internal file handle
     * @param textureRegion  a non-null TextureRegion, often taking up all of a Texture, that stores the images of the glyphs
     * @param xAdjust        how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust        how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust    how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust   how many pixels to add to the used height of each character, using more above
     * @param makeGridGlyphs true if this should use its own way of rendering box-drawing/block-element glyphs, ignoring any in the font file
     * @param ignoredStructuredJsonFlag only present to distinguish this from other constructors; ignored
     */
    public Font(String jsonName, TextureRegion textureRegion,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs,
                boolean ignoredStructuredJsonFlag) {
        FileHandle fntHandle = Gdx.files.internal(jsonName);
        if (fntHandle.exists()) {
            loadJSON(fntHandle, textureRegion, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
        } else {
            throw new RuntimeException("Missing font file: " + jsonName);
        }
    }

    /**
     * Creates a Font from a "Structured JSON" file produced by Chlumsky's msdf-atlas-gen tool, and uses it to assemble
     * the many {@link GlyphRegion}s this has for each glyph. Reads the distance field type from the JSON.
     * <br>
     * <a href="https://github.com/Chlumsky/msdf-atlas-gen">The msdf-atlas-gen tool</a> can actually produce MSDF, SDF,
     * PSDF, and "soft masked" (standard) fonts. It can't produce AngelCode BMFont .fnt files, but the JSON it can
     * produce is fairly full-featured. Its handling of metrics seems significantly better than the tools that work with
     * AngelCode BMFont .fnt files, all but a few fonts in {@link KnownFonts} use JSON. A specialized version
     * of msdf-atlas-gen that only writes structured JSON files (and optimizes the files at the right size) is available
     * at <a href="https://github.com/tommyettinger/fontwriter">fontwriter</a> (Windows-only, for now).
     *
     * @param jsonHandle     the FileHandle of a structured JSON font file
     * @param textureRegion  a non-null TextureRegion, often taking up all of a Texture, that stores the images of the glyphs
     * @param ignoredStructuredJsonFlag only present to distinguish this from other constructors; ignored
     */
    public Font(FileHandle jsonHandle, TextureRegion textureRegion, boolean ignoredStructuredJsonFlag) {
        if (jsonHandle.exists()) {
            loadJSON(jsonHandle, textureRegion, 0f, 0f, 0f, 0f, true);
        } else {
            throw new RuntimeException("Missing font file: " + jsonHandle);
        }
    }

    /**
     * Creates a Font from a "Structured JSON" file produced by Chlumsky's msdf-atlas-gen tool, and uses it to assemble
     * the many {@link GlyphRegion}s this has for each glyph. Reads the distance field type from the JSON.
     * <br>
     * <a href="https://github.com/Chlumsky/msdf-atlas-gen">The msdf-atlas-gen tool</a> can actually produce MSDF, SDF,
     * PSDF, and "soft masked" (standard) fonts. It can't produce AngelCode BMFont .fnt files, but the JSON it can
     * produce is fairly full-featured. Its handling of metrics seems significantly better than the tools that work with
     * AngelCode BMFont .fnt files, all but a few fonts in {@link KnownFonts} use JSON. A specialized version
     * of msdf-atlas-gen that only writes structured JSON files (and optimizes the files at the right size) is available
     * at <a href="https://github.com/tommyettinger/fontwriter">fontwriter</a> (Windows-only, for now).
     *
     * @param jsonHandle     the FileHandle of a structured JSON font file
     * @param textureRegion  a non-null TextureRegion, often taking up all of a Texture, that stores the images of the glyphs
     * @param xAdjust        how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust        how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust    how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust   how many pixels to add to the used height of each character, using more above
     * @param makeGridGlyphs true if this should use its own way of rendering box-drawing/block-element glyphs, ignoring any in the font file
     * @param ignoredStructuredJsonFlag only present to distinguish this from other constructors; ignored
     */
    public Font(FileHandle jsonHandle, TextureRegion textureRegion,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs,
                boolean ignoredStructuredJsonFlag) {
        if (jsonHandle.exists()) {
            loadJSON(jsonHandle, textureRegion, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
        } else {
            throw new RuntimeException("Missing font file: " + jsonHandle);
        }
    }

    /**
     * The bulk of the loading code for Structured JSON fonts.
     *
     * @param jsonHandle     the FileHandle of a structured JSON font file
     * @param textureRegion  a non-null TextureRegion, often taking up all of a Texture, that stores the images of the glyphs
     * @param xAdjust        how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust        how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust    how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust   how many pixels to add to the used height of each character, using more above
     * @param makeGridGlyphs true if this should use its own way of rendering box-drawing/block-element glyphs, ignoring any in the font file
     */
    protected void loadJSON(FileHandle jsonHandle, TextureRegion textureRegion,
                            float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs)
    {
        this.parents = Array.with(textureRegion);
        this.xAdjust = xAdjust;
        this.yAdjust = yAdjust;
        this.widthAdjust = widthAdjust;
        this.heightAdjust = heightAdjust;

        JsonValue fnt;
        if("lzma".equalsIgnoreCase(jsonHandle.extension())) {
            BufferedInputStream bais = jsonHandle.read(4096);
            StreamUtils.OptimizedByteArrayOutputStream baos = new StreamUtils.OptimizedByteArrayOutputStream(4096);
            try {
                Lzma.decompress(bais, baos);
                if (jsonHandle.name().length() > 10 && ".json.lzma".equalsIgnoreCase(jsonHandle.name().substring(jsonHandle.name().length() - 10)))
                    fnt = new JsonReader().parse(baos.toString("UTF-8"));
                else if (jsonHandle.name().length() > 9 && ".ubj.lzma".equalsIgnoreCase(jsonHandle.name().substring(jsonHandle.name().length() - 9))) {
                    StreamUtils.closeQuietly(bais);
                    bais = new BufferedInputStream(new ByteArrayInputStream(baos.toByteArray()));
                    fnt = new UBJsonReader().parse(bais);
                } else {
                    throw new UnsupportedOperationException("Unsupported file type inside compressed file: " + jsonHandle.path());
                }
            } catch (IOException e) {
                throw new RuntimeException("Error reading compressed file: " + jsonHandle.path() + "\n" + e);
            } finally {
                StreamUtils.closeQuietly(bais);
                StreamUtils.closeQuietly(baos);
            }
        } else if (jsonHandle.nameWithoutExtension().endsWith(".ubj")) {
            fnt = new UBJsonReader().parse(jsonHandle.read());
        } else if("dat".equalsIgnoreCase(jsonHandle.extension())) {
            fnt = new JsonReader().parse(LZBDecompression.decompressFromBytes(jsonHandle.readBytes()));
        } else {
            fnt = new JsonReader().parse(jsonHandle);
        }

        name = jsonHandle.name().substring(0, jsonHandle.name().indexOf('.'));

        JsonValue atlas = fnt.get("atlas");
        String dfType = atlas.getString("type", "");
        if("msdf".equals(dfType) || "mtsdf".equals(dfType)) {
            this.setDistanceField(DistanceFieldType.MSDF);
            setCrispness(atlas.getFloat("distanceRange", 8f) * 0.2f); // maybe we don't need to read this?
        }
        else if("sdf".equals(dfType) || "psdf".equals(dfType)) {
            this.setDistanceField(DistanceFieldType.SDF);
            setCrispness(atlas.getFloat("distanceRange", 8f) * 0.2f);
//            setCrispness(0.03125f * atlas.getFloat("distanceRange", 4f)); // maybe we don't need to read this?
        }
        else // softmask, hardmask
            this.setDistanceField(DistanceFieldType.STANDARD);

        float size = atlas.getFloat("size", 16f);

//        JsonValue metrics = fnt.get("metrics");

        // SHOULD USE metrics ?
//        size *= metrics.getFloat("emSize", 1f);

//        float ascender = metrics.getFloat("ascender", 0.8f);
        // SHOULD USE metrics (both lines)?
        descent = size * -0.25f;//metrics.getFloat("descender", -0.25f);
        originalCellHeight = cellHeight = heightAdjust - descent + size;// * metrics.getFloat("lineHeight", 1f);

        // SHOULD USE metrics ?
        underY = 0.05f;//0.5f * metrics.getFloat("underlineY", -0.1f);
        // SHOULD USE metrics ?
        strikeY = 0.15f;//metrics.getFloat("strikeY", 0f);
        strikeBreadth = underBreadth = -0.375f;
        if(makeGridGlyphs){
            underLength = strikeLength = 0.0f;
            underX = strikeX = -0.3f;
        } else {
            underLength = strikeLength = 0.0f;
            underX = strikeX = 0.0f;
        }
        fancyY = 2f;

        JsonValue glyphs = fnt.get("glyphs"), planeBounds, atlasBounds;
        int count = glyphs.size;

        mapping = new IntMap<>(count + 1);
        float minWidth = Integer.MAX_VALUE;
        for (JsonValue.JsonIterator it = glyphs.iterator(); it.hasNext(); ) {
            JsonValue current = it.next();
            int c =    current.getInt("unicode", 65535);
            float a =  current.getFloat("advance", 1f) * size;
            planeBounds = current.get("planeBounds");
            atlasBounds = current.get("atlasBounds");
            float x, y, w, h, xo, yo;
            if(atlasBounds != null) {
                x = atlasBounds.getFloat("left", 0f);
                w = atlasBounds.getFloat("right", 0f) - x;
                y = textureRegion.getRegionHeight() - atlasBounds.getFloat("top", 0f);
                h = textureRegion.getRegionHeight() - atlasBounds.getFloat("bottom", 0f) - y;
            } else {
                x = y = w = h = 0f;
            }
            if(planeBounds != null) {
                xo = planeBounds.getFloat("left", 0f) * size;
                yo = size - planeBounds.getFloat("top", 0f) * size;
            } else {
                xo = yo = 0f;
            }

            if (c != 9608) // full block
                minWidth = Math.min(minWidth, a + widthAdjust);
            GlyphRegion gr = new GlyphRegion(textureRegion, x, y, w, h);
            if (c == 10) {
                a = 0;
                gr.offsetX = 0;
            } else if (makeGridGlyphs && BlockUtils.isBlockGlyph(c)) {
                gr.offsetX = Float.NaN;
            } else
                gr.offsetX = xo + xAdjust;
            gr.offsetY = yo + yAdjust;
            gr.xAdvance = a + widthAdjust;

            cellWidth = Math.max(a + widthAdjust, cellWidth);
            mapping.put(c, gr);
            if (c == '[') {
                mapping.put(2, gr);
            }
        }

        JsonValue kern = fnt.get("kerning");
        if(kern == null || kern.isEmpty())
            kerning = null;
        else {
            kerning = new IntFloatMap(kern.size);
            for (JsonValue.JsonIterator it = kern.iterator(); it.hasNext(); ) {
                JsonValue current = it.next();
                int first = current.getInt("unicode1", 65535);
                int second = current.getInt("unicode2", 65535);
                float amount = current.getFloat("advance", 0f);
                kerning.put(first << 16 | second, amount);
                if (first == '[') {
                    kerning.put(2 << 16 | second, amount);
                }
                if (second == '[') {
                    kerning.put(first << 16 | 2, amount);
                }

            }
        }
        if (!mapping.containsKey(' ')) {
            Gdx.app.error("FONT", "Font is missing a space character! Aborting!");
            throw new RuntimeException("Cannot create a font withoit a space character.");
        }
        else {
            mapping.put('\r', mapping.get(' '));
            // U+200B is the zero-width space
            mapping.put('\u200B', new Font.GlyphRegion(mapping.get(' '), 0f, 0f, 0f));
        }
        // Newlines should be equivalent to zero-width spaces in terms of rendering.
        if (mapping.containsKey('\n')) {
            GlyphRegion gr = mapping.get('\n');
            gr.setRegionWidth(0);
            gr.setRegionHeight(0);
            gr.xAdvance = 0f;
        } else {
            GlyphRegion newline = new GlyphRegion(mapping.get('\u200B'));
            newline.setRegionWidth(0);
            newline.setRegionHeight(0);
            newline.xAdvance = 0;
            mapping.put('\n', newline);
        }
        solidBlock = '█';
        if (makeGridGlyphs) {
            GlyphRegion block = new GlyphRegion(canUseTextures
                    ? new TextureRegion(textureRegion, textureRegion.getRegionWidth() - 2, textureRegion.getRegionHeight() - 2, 1, 1)
                    : new TexturelessRegion(textureRegion, textureRegion.getRegionWidth() - 2, textureRegion.getRegionHeight() - 2, 1, 1)
                    , 0, cellHeight, cellWidth);
            mapping.put(solidBlock, block);
            for (int i = 0x2500; i < 0x2500 + BlockUtils.BOX_DRAWING.length; i++) {
                if (BlockUtils.isBlockGlyph(i)) {
                    mapping.put(i, new GlyphRegion(block, Float.NaN, cellHeight, cellWidth));
                }
            }
        } else if (!mapping.containsKey(solidBlock)) {
            mapping.put(solidBlock, new GlyphRegion(canUseTextures
                    ? new TextureRegion(textureRegion, textureRegion.getRegionWidth() - 2, textureRegion.getRegionHeight() - 2, 1, 1)
                    : new TexturelessRegion(textureRegion, textureRegion.getRegionWidth() - 2, textureRegion.getRegionHeight() - 2, 1, 1)
                    , 0, cellHeight, cellWidth));
        }
        defaultValue = mapping.get(' ', mapping.values().next());
        originalCellWidth = cellWidth;
        originalCellHeight = cellHeight;

//        if (distanceField != DistanceFieldType.STANDARD) {
//            textureRegion.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
//        }

        // This should be the default for Structured JSON fonts because they (so far) are always large.
        if(textureRegion.getTexture() != null)
            textureRegion.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        isMono = minWidth == cellWidth && kerning == null;
        integerPosition = false;

//        underY =  -descent / originalCellHeight;
//        strikeY = -descent / originalCellHeight;

//        underY -=  descent / size;
//        strikeY -= descent / size;

        inlineImageOffsetX = 0f;
        inlineImageOffsetY = 0f;
        inlineImageXAdvance = 0f;
        inlineImageStretch = 1f;
    }

    //// usage section

    /**
     * Configures whether some data structures should be shared when this Font is copied via {@link #Font(Font)}.
     * If {@code share} is true, this simply sets a boolean and that will reduce the amount of work done in the copy
     * constructor, at the cost of preventing unique configuration per Font in some fields. If {@code share} is false,
     * and this is currently already set to share those data structures, this will stop sharing them by creating copies
     * of {@link #mapping}, {@link #namesByCharCode}, {@link #nameLookup}, and {@link #kerning}, and using those copies
     * from this point onward. When sharing is disabled, the copy constructor will do a similar copy for the new Font.
     * <br>
     * While you can still set {@link #sharing} by the field directly, doing so is discouraged unless you find yourself
     * needing to manually handle references for any reason.
     *
     * @param share whether some data structures should share references with copies of this Font
     * @return this, for chaining
     */
    public Font setSharing(boolean share) {
        if(sharing && !share) {
            IntMap<GlyphRegion> sharedMapping = mapping;
            mapping = new IntMap<>(sharedMapping.size);
            for (IntMap.Entry<GlyphRegion> e : sharedMapping) {
                if (e.value == null) continue;
                mapping.put(e.key, new GlyphRegion(e.value));
            }
            if(nameLookup != null)
                nameLookup = new CaseInsensitiveIntMap(nameLookup);
            if(namesByCharCode != null)
                namesByCharCode = new IntMap<>(namesByCharCode);
            kerning = kerning == null ? null : new IntFloatMap(kerning);
        }
        sharing = share;
        return this;
    }
    /**
     * A {@link DistanceFieldType} that should be {@link DistanceFieldType#STANDARD} for most fonts, and can be
     * {@link DistanceFieldType#SDF}, {@link DistanceFieldType#MSDF}, or {@link DistanceFieldType#SDF_OUTLINE} if you
     * know you have a font made to be used with one of those rendering techniques. See {@link #distanceFieldCrispness}
     * for one way to configure SDF and MSDF fonts, and {@link #resizeDistanceField(float, float)} for an important method
     * to handle window-resizing correctly.
     */
    public DistanceFieldType getDistanceField() {
        return distanceField;
    }

    /**
     * Sets this Font's distance field type; this should be {@link DistanceFieldType#STANDARD} for most fonts (meaning
     * it is a normal bitmap font with no distance field effect), but specially-made fonts can use
     * {@link DistanceFieldType#SDF} or {@link DistanceFieldType#MSDF} to get
     * smoother scaling and/or sharper edges. Fonts compatible with SDF can use {@link DistanceFieldType#SDF_OUTLINE} to
     * show a black outline around all glyphs in the font.
     * @param distanceField a DistanceFieldType enum constant; if null, this will be treated as {@link DistanceFieldType#STANDARD}
     * @return this, for chaining
     */
    public Font setDistanceField(DistanceFieldType distanceField) {
        if(!canUseTextures)
            return this;
        this.distanceField = distanceField == null ? DistanceFieldType.STANDARD : distanceField;
        if (this.distanceField == DistanceFieldType.MSDF) {
            shader = new ShaderProgram(vertexShader, msdfFragmentShader);
            if (!shader.isCompiled())
                Gdx.app.error("textratypist", "MSDF shader failed to compile: " + shader.getLog());
        } else if (this.distanceField == DistanceFieldType.SDF) {
            shader = new ShaderProgram(vertexShader,
                    Gdx.app.getType() == Application.ApplicationType.Desktop
                            ? sdfFragmentShaderDesktopOpenGL
                            : sdfFragmentShader);
            if (!shader.isCompiled())
                Gdx.app.error("textratypist", "SDF shader failed to compile: " + shader.getLog());
        } else if (this.distanceField == DistanceFieldType.SDF_OUTLINE) {
            shader = new ShaderProgram(vertexShader,
                    Gdx.app.getType() == Application.ApplicationType.Desktop
                    ? sdfBlackOutlineFragmentShaderDesktopOpenGL
                    : sdfBlackOutlineFragmentShader);
            if (!shader.isCompiled())
                Gdx.app.error("textratypist", "SDF_OUTLINE shader failed to compile: " + shader.getLog());
        } else shader = null;
        return this;
    }

    /**
     * Assembles two chars into a kerning pair that can be looked up as a key in {@link #kerning}. This is unlikely to
     * be used by most user code, but can be useful for anything that's digging deeply into the internals here.
     * If you give such a pair to {@code kerning}'s {@link IntFloatMap#get(int, float)} method, you'll get the amount of
     * extra space (in the same unit the font uses) this will insert between {@code first} and {@code second}.
     *
     * @param first  the first char
     * @param second the second char
     * @return a kerning pair that can be looked up in {@link #kerning}
     */
    public int kerningPair(char first, char second) {
        return first << 16 | (second & 0xFFFF);
    }

    /**
     * Scales the font by the given multiplier, applying it both horizontally and vertically.
     *
     * @param both how much to multiply the width and height of each glyph by
     * @return this Font, for chaining
     */
    public Font scale(float both) {
        scaleX *= both;
        scaleY *= both;
        cellWidth *= both;
        cellHeight *= both;
        return this;
    }

    /**
     * Scales the font by the given horizontal and vertical multipliers.
     *
     * @param horizontal how much to multiply the width of each glyph by
     * @param vertical   how much to multiply the height of each glyph by
     * @return this Font, for chaining
     */
    public Font scale(float horizontal, float vertical) {
        scaleX *= horizontal;
        scaleY *= vertical;
        cellWidth *= horizontal;
        cellHeight *= vertical;
        return this;
    }

    /**
     * Scales the font so that it will have the given width and height.
     *
     * @param width  the target width of the font, in world units
     * @param height the target height of the font, in world units
     * @return this Font, for chaining
     */
    public Font scaleTo(float width, float height) {
        scaleX = width / originalCellWidth;
        scaleY = height / originalCellHeight;
        cellWidth = width;
        cellHeight = height;
        return this;
    }

    /**
     * Scales the font so that it will have the given height, keeping the current aspect ratio.
     *
     * @param height the target height of the font, in world units
     * @return this Font, for chaining
     */
    public Font scaleHeightTo(float height) {
        return scaleTo(cellWidth * height / cellHeight, height);
    }

    /**
     * Multiplies the line height by {@code multiplier} without changing the size of any characters.
     * This can cut off the tops of letters if the multiplier is too small.
     *
     * @param multiplier will be applied to {@link #cellHeight} and {@link #originalCellHeight}
     * @return this Font, for chaining
     */
    public Font adjustLineHeight(float multiplier) {
        cellHeight *= multiplier;
        originalCellHeight *= multiplier;
        descent *= multiplier; // I'm not sure if this would help or not.
//        underY *= multiplier; strikeY *= multiplier; // Very unsure about this.
        return this;
    }

    /**
     * Multiplies the width used by each glyph in a monospaced font by {@code multiplier} without changing the size of
     * any characters.
     *
     * @param multiplier will be applied to {@link #cellWidth} and {@link #originalCellWidth}
     * @return this Font, for chaining
     */
    public Font adjustCellWidth(float multiplier) {
        cellWidth *= multiplier;
        originalCellWidth *= multiplier;
//        underX *= multiplier; strikeX *= multiplier; // Unsure about this.
        return this;
    }

    /**
     * Fits all chars into cells width by height in size, and optionally centers them in their cells.
     * This sets {@link #isMono} to true, and {@link #kerning} to null.
     * If you call {@link #scaleTo(float, float)} after this, you will need to call fitCell() again to update cell size.
     * <br>
     * If {@link #sharing} is true, then calling this makes the same changes to Fonts that share references with this.
     *
     * @param width  the target width of a cell, in world units
     * @param height the target height of a cell, in world units
     * @param center if true, this will center every glyph in its cell
     * @return this Font, for chaining
     */
    public Font fitCell(float width, float height, boolean center) {
//        float hRatio = width / cellWidth;
//        float vRatio = height / cellHeight;
//        underX *= hRatio; strikeX *= hRatio;
//        underY *= vRatio; strikeY *= vRatio;
        cellWidth = width;
        cellHeight = height;
        float wsx = width / scaleX;
        IntMap.Entries<GlyphRegion> vs = mapping.entries();
        if (center) {
            while (vs.hasNext) {
                IntMap.Entry<GlyphRegion> ent = vs.next();
                GlyphRegion g = ent.value;
                if(ent.key >= 0xE000 && ent.key < 0xF800) {
//                    g.offsetY -= descent;
                } else{
                    g.offsetX += (wsx - g.xAdvance) * 0.5f;
                    g.xAdvance = wsx;
                }
            }
        } else {
            while (vs.hasNext) {
                IntMap.Entry<GlyphRegion> ent = vs.next();
                if(ent.key >= 0xE000 && ent.key < 0xF800) {
//                    ent.value.offsetY -= descent;
                } else{
                    ent.value.xAdvance = wsx;
                }
            }
        }
        isMono = true;
        kerning = null;
        return this;
    }

    public float getUnderlineX() {
        return underX;
    }

    public float getUnderlineY() {
        return underY;
    }

    public Font setUnderlinePosition(float underX, float underY) {
        this.underX = underX;
        this.underY = underY;
        return this;
    }
    
    public Font setUnderlineMetrics(float underX, float underY, float underLength, float underBreadth) {
        this.underX = underX;
        this.underY = underY;
        this.underLength = underLength;
        this.underBreadth = underBreadth;
        return this;
    }

    public float getStrikethroughX() {
        return strikeX;
    }

    public float getStrikethroughY() {
        return strikeY;
    }

    public Font setStrikethroughPosition(float strikeX, float strikeY) {
        this.strikeX = strikeX;
        this.strikeY = strikeY;
        return this;
    }
    
    public Font setStrikethroughMetrics(float strikeX, float strikeY, float strikeLength, float strikeBreadth) {
        this.strikeX = strikeX;
        this.strikeY = strikeY;
        this.strikeLength = strikeLength;
        this.strikeBreadth = strikeBreadth;
        return this;
    }

    /**
     * Sets both the underline and strikethrough metric adjustments with the same values, as if you called both
     * {@link #setUnderlineMetrics(float, float, float, float)} and
     * {@link #setStrikethroughMetrics(float, float, float, float)} with identical parameters.
     * This does not affect "fancy lines" (the zigzag lines produced by error, warning, and note effects).
     * <br>
     * This affects "Zen" metrics, which means it is measured in fractions of
     * {@link #cellWidth} or {@link #cellHeight} (as appropriate), and each metric only affects one value (even though
     * this sets two metrics for each parameter). For length and breadth, you should consider the value as adding or
     * subtracting a fraction of the original length of a line across a cell or of the breadth of the line itself.
     * @param x adjustment for the underline and strikethrough x-position, affecting the left side of each line
     * @param y adjustment for the underline and strikethrough y-position, affecting the bottom side of each line
     * @param length adjustment for the underline and strikethrough x-size, affecting the extra part drawn to the right of each line
     * @param breadth adjustment for the underline and strikethrough y-size, affecting how thick each line is from bottom to top
     * @return this, for chaining
     */
    public Font setLineMetrics(float x, float y, float length, float breadth) {
        this.underX = x;
        this.underY = y;
        this.underLength = length;
        this.underBreadth = breadth;
        this.strikeX = x;
        this.strikeY = y;
        this.strikeLength = length;
        this.strikeBreadth = breadth;
        return this;
    }

    public float getFancyLineX() {
        return fancyX;
    }

    public float getFancyLineY() {
        return fancyY;
    }

    /**
     * Sets both the "fancy line" metric adjustments for position, only changing {@link #fancyX} and {@link #fancyY}.
     * "Fancy lines" are the zigzag lines produced by error, warning, and note effects. This does not change the normal
     * underline or the strikethrough styles.
     * <br>
     * This affects "Zen" metrics, which means it is measured in fractions of
     * {@link #cellWidth} or {@link #cellHeight} (as appropriate), and each metric only affects one value (even though
     * this sets two metrics for each parameter).
     * @param x adjustment for the "fancy line" x-position, affecting the left side of each line
     * @param y adjustment for the "fancy line" y-position, affecting the bottom side of each line
     * @return this, for chaining
     */
    public Font setFancyLinePosition(float x, float y) {
        this.fancyX = x;
        this.fancyY = y;
        return this;
    }

    public float getInlineImageOffsetX() {
        return inlineImageOffsetX;
    }

    public float getInlineImageOffsetY() {
        return inlineImageOffsetY;
    }

    public float getInlineImageXAdvance() {
        return inlineImageXAdvance;
    }

    public float getInlineImageStretch() {
        return inlineImageStretch;
    }

    public Font setInlineImageStretch(float inlineImageStretch) {
        this.inlineImageStretch = inlineImageStretch;
        return this;
    }

    /**
     * Sets the adjustments added to the metric for inline images added with {@link #addAtlas(TextureAtlas)} (or its
     * overloads).
     * <br>
     * Changing offsetX with a positive value moves all GlyphRegions to the right.
     * Changing offsetY with a positive value moves all GlyphRegions up (this is possibly unexpected).
     * Changing xAdvanceChange with a positive value will put more space between each GlyphRegion and the glyph after it.
     *
     * @param offsetX will be added to the {@link GlyphRegion#offsetX} of each added glyph; positive change moves a GlyphRegion to the right
     * @param offsetY will be added to the {@link GlyphRegion#offsetY} of each added glyph; positive change moves a GlyphRegion up
     * @param xAdvance will be added to the {@link GlyphRegion#xAdvance} of each added glyph; positive values make images push later glyphs away more
     * @return this Font, for chaining
     */
    public Font setInlineImageMetrics(float offsetX, float offsetY, float xAdvance) {
        inlineImageOffsetX = offsetX;
        inlineImageOffsetY = offsetY;
        inlineImageXAdvance = xAdvance;
        return this;
    }

    //TODO: check these methods (including above) for what xAdvance does
    /**
     * Sets the adjustments added to the metric for inline images added with {@link #addAtlas(TextureAtlas)} (or its
     * overloads).
     * <br>
     * Changing offsetX with a positive value moves all GlyphRegions to the right.
     * Changing offsetY with a positive value moves all GlyphRegions up (this is possibly unexpected).
     * Changing xAdvanceChange with a positive value will put more space between each GlyphRegion and the glyph after it.
     * Changing stretch with a value higher than 1 will enlarge all inline images, and lower than 1 will shrink them.
     *
     * @param offsetX will be added to the {@link GlyphRegion#offsetX} of each added glyph; positive change moves a GlyphRegion to the right
     * @param offsetY will be added to the {@link GlyphRegion#offsetY} of each added glyph; positive change moves a GlyphRegion up
     * @param xAdvance will be added to the {@link GlyphRegion#xAdvance} of each added glyph; positive values make images push later glyphs away more
     * @param stretch affects the size at which inline images are drawn, but does not change individual glyphs; this is a size multiplier with values higher than 1 making glyphs larger
     * @return this Font, for chaining
     */
    public Font setInlineImageMetrics(float offsetX, float offsetY, float xAdvance, float stretch) {
        inlineImageOffsetX = offsetX;
        inlineImageOffsetY = offsetY;
        inlineImageXAdvance = xAdvance;
        inlineImageStretch = stretch;
        return this;
    }

    /**
     * Calls {@link #setTextureFilter(Texture.TextureFilter, Texture.TextureFilter)} with
     * {@link Texture.TextureFilter#Linear} for both min and mag filters.
     * This is the most common usage for setting the texture filters, and is appropriate when you have
     * a large TextureRegion holding the font and you normally downscale it. This is automatically done
     * for {@link DistanceFieldType#SDF}, {@link DistanceFieldType#MSDF}, and {@link DistanceFieldType#SDF_OUTLINE}
     * fonts, but you may also want
     * to use it for {@link DistanceFieldType#STANDARD} fonts when downscaling (they can look terrible
     * if the default {@link Texture.TextureFilter#Nearest} filter is used).
     * Note that this sets the filter on every Texture that holds a TextureRegion used by the font, so
     * it may affect the filter on other parts of an atlas.
     *
     * @return this, for chaining
     */
    public Font setTextureFilter() {
        return setTextureFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    /**
     * Sets the texture filters on each Texture that holds a TextureRegion used by the font to the given
     * {@code minFilter} and {@code magFilter}. You may want to use this to set a font using
     * {@link DistanceFieldType#STANDARD} to use a better TextureFilter for smooth downscaling, like
     * {@link Texture.TextureFilter#MipMapLinearLinear} or just
     * {@link Texture.TextureFilter#Linear}. You might, for some reason, want to
     * set a font using {@link DistanceFieldType#SDF}, {@link DistanceFieldType#MSDF}, or
     * {@link DistanceFieldType#SDF_OUTLINE} to use TextureFilters
     * other than its default {@link Texture.TextureFilter#Linear}.
     * Note that this may affect the filter on other parts of an atlas.
     *
     * @return this, for chaining
     */
    public Font setTextureFilter(Texture.TextureFilter minFilter, Texture.TextureFilter magFilter) {

        if(canUseTextures) {
            for (TextureRegion parent : parents) {
                parent.getTexture().setFilter(minFilter, magFilter);
            }
        }
        return this;
    }
    /**
     * A no-op unless this is a subclass that overrides {@link Font#handleIntegerPosition(float)}.
     * @param integer usually ignored
     * @return this for chaining
     */
    public Font useIntegerPositions(boolean integer) {
        integerPosition = integer;
        return this;
    }

    public float getDescent() {
        return descent;
    }

    public Font setDescent(float descent) {
        this.descent = descent;
        return this;
    }

    public String getName() {
        return name;
    }

    public Font setName(String name) {
        this.name = name;
        return this;
    }

    public float getObliqueStrength() {
        return obliqueStrength;
    }

    public Font setObliqueStrength(float obliqueStrength) {
        this.obliqueStrength = obliqueStrength;
        return this;
    }

    public float getBoldStrength() {
        return boldStrength;
    }

    public Font setBoldStrength(float boldStrength) {
        this.boldStrength = boldStrength;
        return this;
    }

    public float getOutlineStrength() {
        return outlineStrength;
    }

    public Font setOutlineStrength(float outlineStrength) {
        this.outlineStrength = outlineStrength;
        return this;
    }

    /**
     * When {@code makeGridGlyphs} is passed as true to a constructor here, box drawing and other block elements will be
     * drawn using a solid block GlyphRegion that is stretched and moved to form various lines and blocks. Setting this
     * field to something other than 1 affects how wide the lines are for box drawing characters only; this is acts as a
     * multiplier on the original width of a line. Normal box drawing lines such as those in {@code ┌} are 0.1f of a
     * cell across. Thick lines such as those in {@code ┏} are 0.2f of a cell across. Double lines such as those in
     * {@code ╔} are two normal lines, 0.1f of a cell apart; double lines are not affected by this field.
     * @return the current box drawing breadth multiplier, which defaults to 1
     */
    public float getBoxDrawingBreadth() {
        return boxDrawingBreadth;
    }

    /**
     * When {@code makeGridGlyphs} is passed as true to a constructor here, box drawing and other block elements will be
     * drawn using a solid block GlyphRegion that is stretched and moved to form various lines and blocks. Setting this
     * field to something other than 1 affects how wide the lines are for box drawing characters only; this is acts as a
     * multiplier on the original width of a line. Normal box drawing lines such as those in {@code ┌} are 0.1f of a
     * cell across. Thick lines such as those in {@code ┏} are 0.2f of a cell across. Double lines such as those in
     * {@code ╔} are two normal lines, 0.1f of a cell apart; double lines are not affected by this field.
     * @param boxDrawingBreadth the new float value to use for the box drawing breadth multiplier
     * @return this, for chaining
     */
    public Font setBoxDrawingBreadth(float boxDrawingBreadth) {
        this.boxDrawingBreadth = boxDrawingBreadth;
        return this;
    }

    /**
     * Gets the FontFamily this can use to switch fonts using [@Name] syntax. If the family is null, only the current
     * Font will be used.
     * @return the current FontFamily this Font uses, or null if this does not have one
     */
    public FontFamily getFamily() {
        return family;
    }

    /**
     * Sets the FontFamily this can use to switch fonts using [@Name] syntax. If family is null, only the current Font
     * will be used.
     *
     * @param family a {@link FontFamily} that may be null or shared with other Fonts.
     * @return this, for chaining
     */
    public Font setFamily(FontFamily family) {
        this.family = family;
        return this;
    }

    /**
     * Gets the "crispness" multiplier for distance field fonts (SDF and MSDF). There is no default value, because this
     * depends on how an individual distance field font was created. Typical values range from 1.5 to 4.5 . Lower values
     * look softer and fuzzier, while higher values look sharper and possibly more jagged. This is used as a persistent
     * multiplier that can be configured per-font, whereas {@link #actualCrispness} is the working value that changes
     * often but is influenced by this one. This variable is used by {@link #resizeDistanceField(float, float)} to affect
     * the working crispness value.
     *
     * @return the current crispness multiplier, as a float
     */
    public float getCrispness() {
        return distanceFieldCrispness;
    }

    /**
     * Sets the "crispness" multiplier for distance field fonts (SDF and MSDF). There is no default value, because this
     * depends on how an individual distance field font was created. Typical values range from 1.5 to 4.5 . Lower values
     * look softer and fuzzier, while higher values look sharper and possibly more jagged. This is used as a persistent
     * multiplier that can be configured per-font, whereas {@link #actualCrispness} is the working value that changes
     * often but is influenced by this one. This variable is used by {@link #resizeDistanceField(float, float)} to affect
     * the working crispness value. <em>Changing the crispness does nothing unless you also resize the distance field,
     * for each font you are rendering.</em> So make sure to call {@link #resizeDistanceField(float, float)} in resize() !
     *
     * @param crispness a float multiplier to be applied to the working crispness; often between 1.5 and 4.5
     * @return this Font, for chaining
     */
    public Font setCrispness(float crispness) {
        distanceFieldCrispness = crispness;
        return this;
    }

    /**
     * Takes the "crispness" multiplier for distance field fonts (SDF and MSDF) and multiplies it by another multiplier.
     * There is no default value for crispness, because this depends on how an individual distance field font was
     * created. Typical values range from 1.5 to 4.5 . Lower values look softer and fuzzier, while higher values look
     * sharper and possibly more jagged. This affects a persistent multiplier that can be configured per-font,
     * whereas {@link #actualCrispness} is the working value that changes often but is influenced by this one. The
     * variable this affects is used by {@link #resizeDistanceField(float, float)} to affect the working crispness value.
     * <em>Changing the crispness does nothing unless you also resize the distance field, for each font you are
     * rendering.</em> So make sure to call {@link #resizeDistanceField(float, float)} in resize() !
     *
     * @param multiplier a float multiplier to be applied to the working crispness multiplier
     * @return this Font, for chaining
     */
    public Font multiplyCrispness(float multiplier) {
        distanceFieldCrispness *= multiplier;
        return this;
    }

    /**
     * Makes this Font "learn" a new mapping from a char (typically an emoji in a String for {@code character}) to a
     * TextureRegion, allowing any offsets on x or y to be specified as well as the amount of horizontal space the
     * resulting GlyphRegion should use. The most common way to call this uses a String containing one emoji character,
     * because those are relatively easy to enter with a clear result. Because most emoji are technically more than one
     * Java {@code char}, we only use the last char in {@code character}, which usually is a value that only overlaps
     * with a private-use area character (and most of those are unused). Some emoji glyphs require more characters than
     * normal, such as any with human skin tones. These won't be handled well... You may want to use the
     * {@code [+scientist, dark skin tone]} or {@code [+🧑🏿‍🔬]} syntax for multipart emoji when you actually have an
     * atlas full of emoji to draw from, such as the one used by {@link KnownFonts#addEmoji(Font)}.
     * <br>
     * If {@link #sharing} is true, then calling this makes the same changes to Fonts that share references with this.
     *
     * @param character a String containing at least one character; only the last char (not codepoint) will be used
     * @param region the TextureRegion to associate with the given character
     * @param offsetX the x offset to position the drawn TextureRegion at, with positive offset moving right
     * @param offsetY the y offset to position the drawn TextureRegion at, with positive offset moving up
     * @param xAdvance how much horizontal space the GlyphRegion should take up
     * @return this Font, for chaining
     */
    public Font addImage(String character, TextureRegion region, float offsetX, float offsetY, float xAdvance) {
        if(character != null && !character.isEmpty())
            mapping.put(character.charAt(character.length() - 1), new GlyphRegion(region, offsetX, offsetY, xAdvance));
        return this;
    }

    /**
     * Makes this Font "learn" a new mapping from a char (typically an emoji in a String for {@code character}) to a
     * TextureRegion. The GlyphRegion that will be placed into {@code mapping} will have 0 for its offsetX and offsetY,
     * and its xAdvance will be the same as region's {@link TextureRegion#getRegionWidth()}. The most common way to call
     * this uses a String containing one emoji character, because those are relatively easy to enter with a clear
     * result. Because most emoji are technically more than one Java {@code char}, we only use the last char in
     * {@code character}, which usually is a value that only overlaps with a private-use area character (and most of
     * those are unused). Some emoji glyphs require more characters than normal, such as any with human skin tones.
     * These won't be handled well... You may want to use the {@code [+scientist, dark skin tone]} or {@code [+🧑🏿‍🔬]}
     * syntax for multipart emoji when you actually have an atlas full of emoji to draw from, such as the one used by
     * {@link KnownFonts#addEmoji(Font)}.
     * <br>
     * If {@link #sharing} is true, then calling this makes the same changes to Fonts that share references with this.
     *
     * @param character a String containing at least one character; only the last char (not codepoint) will be used
     * @param region the TextureRegion to associate with the given character
     * @return this Font, for chaining
     */
    public Font addImage(String character, TextureRegion region) {
        if(character != null && !character.isEmpty())
            mapping.put(character.charAt(character.length() - 1), new GlyphRegion(region));
        return this;
    }

    /**
     * Adds all items in {@code atlas} to the private use area of {@link #mapping}, and stores their names, so they can
     * be looked up with {@code [+saxophone]} syntax (which is often the same as the {@code [+🎷]} syntax). The names
     * of TextureRegions in the atlas are treated as case-insensitive, like some file systems.
     * <a href="https://github.com/tommyettinger/twemoji-atlas/">There are possible emoji atlases here.</a>
     * This may be useful if you have your own atlas, but for Twemoji in particular, you can use
     * {@link KnownFonts#addEmoji(Font)} and the Twemoji files in the knownFonts folder.
     * <br>
     * If {@link #sharing} is true, then calling this makes the same changes to Fonts that share references with this.
     *
     * @param atlas a TextureAtlas that shouldn't have more than 6144 names; all of it will be used
     * @return this Font, for chaining
     */
    public Font addAtlas(TextureAtlas atlas) {
        return addAtlas(atlas, "", "", 0, 0, 0);
    }
    /**
     * Adds all items in {@code atlas} to the private use area of {@link #mapping}, and stores their names, so they can
     * be looked up with {@code [+saxophone]} syntax (which is often the same as the {@code [+🎷]} syntax). The names
     * of TextureRegions in the atlas are treated as case-insensitive, like some file systems.
     * <a href="https://github.com/tommyettinger/twemoji-atlas/">There are possible emoji atlases here.</a>
     * This may be useful if you have your own atlas, but for Twemoji in particular, you can use
     * {@link KnownFonts#addEmoji(Font)} and the Twemoji files in the knownFonts folder. This overload allows specifying
     * adjustments to the font-like properties of each GlyphRegion added, which may be useful if images from a
     * particular atlas show up with an incorrect position or have the wrong spacing.
     * <br>
     * Changing offsetXChange with a positive value moves all GlyphRegions to the right.
     * Changing offsetYChange with a positive value moves all GlyphRegions up (this is possibly unexpected).
     * Changing xAdvanceChange with a positive value will put more space between each GlyphRegion and the glyph after it.
     * Each of the metric changes has a variable from this Font added to it; {@link #inlineImageOffsetX},
     * {@link #inlineImageOffsetY}, and {@link #inlineImageXAdvance} all are added in here. You can change
     * {@link #inlineImageStretch} at any time; this multiplies the size each inline image is drawn at (after the image
     * is scaled to try to fit a line's height).
     * <br>
     * If {@link #sharing} is true, then calling this makes the same changes to Fonts that share references with this.
     *
     * @param atlas a TextureAtlas that shouldn't have more than 6144 names; all of it will be used
     * @param offsetXChange will be added to the {@link GlyphRegion#offsetX} of each added glyph; positive change moves a GlyphRegion to the right
     * @param offsetYChange will be added to the {@link GlyphRegion#offsetY} of each added glyph; positive change moves a GlyphRegion up
     * @param xAdvanceChange will be added to the {@link GlyphRegion#xAdvance} of each added glyph; positive values make images push later glyphs away more
     * @return this Font, for chaining
     */
    public Font addAtlas(TextureAtlas atlas, float offsetXChange, float offsetYChange, float xAdvanceChange) {
        return addAtlas(atlas, "", "", offsetXChange, offsetYChange, xAdvanceChange);
    }
    /**
     * Adds all items in {@code atlas} to the private use area of {@link #mapping}, and stores their names, so they can
     * be looked up with {@code [+saxophone]} syntax (which is often the same as the {@code [+🎷]} syntax). The names
     * of TextureRegions in the atlas are treated as case-insensitive, like some file systems.
     * <a href="https://github.com/tommyettinger/twemoji-atlas/">There are possible emoji atlases here.</a>
     * This may be useful if you have your own atlas, but for Twemoji in particular, you can use
     * {@link KnownFonts#addEmoji(Font)} and the Twemoji files in the knownFonts folder. This overload allows specifying
     * adjustments to the font-like properties of each GlyphRegion added, which may be useful if images from a
     * particular atlas show up with an incorrect position or have the wrong spacing. It also allows specifying a String
     * to prepend and to append that will be prepended and appended to each name, respectively. Either or both of the
     * Strings to prepend and append may be empty (or equivalently here, null).
     * <br>
     * Changing offsetXChange with a positive value moves all GlyphRegions to the right.
     * Changing offsetYChange with a positive value moves all GlyphRegions up (this is possibly unexpected).
     * Changing xAdvanceChange with a positive value will put more space between each GlyphRegion and the glyph after it.
     * Each of the metric changes has a variable from this Font added to it; {@link #inlineImageOffsetX},
     * {@link #inlineImageOffsetY}, and {@link #inlineImageXAdvance} all are added in here. You can change
     * {@link #inlineImageStretch} at any time; this multiplies the size each inline image is drawn at (after the image
     * is scaled to try to fit a line's height).
     * <br>
     * If {@link #sharing} is true, then calling this makes the same changes to Fonts that share references with this.
     *
     * @param atlas a TextureAtlas that shouldn't have more than 6144 names; all of it will be used
     * @param prepend will be prepended before each name in the atlas; if null, will be treated as ""
     * @param append will be appended after each name in the atlas; if null, will be treated as ""
     * @param offsetXChange will be added to the {@link GlyphRegion#offsetX} of each added glyph; positive change moves a GlyphRegion to the right
     * @param offsetYChange will be added to the {@link GlyphRegion#offsetY} of each added glyph; positive change moves a GlyphRegion up
     * @param xAdvanceChange will be added to the {@link GlyphRegion#xAdvance} of each added glyph; positive values make images push later glyphs away more
     * @return this Font, for chaining
     */
    public Font addAtlas(TextureAtlas atlas, String prepend, String append, float offsetXChange, float offsetYChange, float xAdvanceChange) {
        Array<TextureAtlas.AtlasRegion> regions = atlas.getRegions();
        if(nameLookup == null)
            nameLookup = new CaseInsensitiveIntMap(regions.size, 0.5f);
        else
            nameLookup.ensureCapacity(regions.size);
        if(namesByCharCode == null)
            namesByCharCode = new IntMap<>(regions.size >> 1, 0.5f);
        else
            namesByCharCode.ensureCapacity(regions.size >> 1);
        if(prepend == null) prepend = "";
        if(append == null) append = "";

        offsetXChange += inlineImageOffsetX;
        offsetYChange += inlineImageOffsetY;
        xAdvanceChange += inlineImageXAdvance;

        int start = 0xE000 + namesByCharCode.size;

        TextureAtlas.AtlasRegion previous = regions.first();
        GlyphRegion gr = new GlyphRegion(previous,
                previous.offsetX + offsetXChange, previous.offsetY - offsetYChange, previous.originalWidth + xAdvanceChange);

        mapping.put(start, gr);
        String name = prepend + previous.name + append;
        nameLookup.put(name, start);
        namesByCharCode.put(start, name);
        for (int i = start, a = 1; i < 0xF800 && a < regions.size; a++) {
            TextureAtlas.AtlasRegion region = regions.get(a);
            if (previous.getRegionX() == region.getRegionX() && previous.getRegionY() == region.getRegionY()) {
                name = prepend + region.name + append;
                nameLookup.put(name, i);
                char f = previous.name.charAt(0);
                // If the previous name didn't start with an emoji char, use this name. This means if there is only one
                // name that refers to a region, that name will be used in namesByCharCode, but if there is more than
                // one sequential name, then names that start with emoji take priority.
                // This uses a pretty simple check, but it works even for unusual emoji like ‼, which starts with
                // U+203C, or ✌🏿, which starts with U+270C . It does, however, fail for some natural-language characters
                // (such as all Chinese characters, which this identifies as emoji). That shouldn't come up often, since
                // this requires both later-in-Unicode names and earlier-in-Unicode names to refer to the same region.
                if(f < 0x2000)
                    namesByCharCode.put(i, name);
            } else {
                ++i;
                previous = region;
                gr = new GlyphRegion(region,
                        region.offsetX + offsetXChange, region.offsetY - offsetYChange, region.originalWidth + xAdvanceChange);
                mapping.put(i, gr);
                name = prepend + region.name + append;
                nameLookup.put(name, i);
                namesByCharCode.put(i, name);
            }
        }
        return this;
    }

    /**
     * Gets the char that might be associated with {@code name} in at Atlas added to this (see
     * {@link #addAtlas(TextureAtlas)}, or returns the int -1 if the name could not be found. This will only return a
     * negative result if the name was not found. This can be useful to look up complex names, such as emoji entered by
     * a user, and get the char that can be used to render such an emoji. The name is treated as case-insensitive. If
     * you are certain a name is present, you can cast the result immediately to a char to use it normally.
     * @param name a name from a TextureAtlas added to this Font, looked up as case-insensitive
     * @return the char that the given name is associated with, as an int in the char range if found, or -1 otherwise
     */
    public int atlasLookup(String name) {
        if(nameLookup == null) return -1;
        return nameLookup.get(name, -1);
    }

    /**
     * Adds a new glyph to this Font that has no visible representation (like space) but advances the cursor by a
     * specified amount, either positive to move right or negative to move left. This can be useful to manually move
     * problematic elements in a user interface, to add a fixed-width character for something like tab, or to make a
     * new character that moves back by an exact amount. Suggestions for representations are {@code '\b'}, {@code '\t'},
     * or the whitespace Unicode chars between {@code 0x2000} and {@code 0x200A}, inclusive. Suggestions for
     * representations to <em>avoid</em> are any existing characters in the Font, carriage return, newline, and any of
     * the possibly-whitespace characters between {@code 0x200B} and {@code 0x200D}, inclusive.
     * <br>
     * The representation must be {@code (representation < 0xE000 || representation >= 0xF800)}; if it is in that range
     * starting with {@code 0xE000}, then it would be considered an atlas image and the spacing calculations would be
     * all wrong. This Font must also have an existing space character, {@code ' '}.
     * <br>
     * If {@link #sharing} is true, then calling this makes the same changes to Fonts that share references with this.
     *
     * @param representation a char that can be entered to move the cursor by a specific amount
     * @param advance how much to advance the cursor by, in unscaled font units; positive for right or negative for left
     * @return this Font, for chaining
     */
    public Font addSpacingGlyph(char representation, float advance) {
        GlyphRegion space = mapping.get(' ', null);
        if(space == null || (representation >= 0xE000 && representation < 0xF800)) return this;
        GlyphRegion next = new GlyphRegion(space);
        next.xAdvance = advance;
        mapping.put(representation, next);
        return this;
    }

    /**
     * Must be called before drawing anything with an SDF or MSDF font; does not need to be called for other fonts
     * unless you are mixing them with SDF/MSDF fonts or other shaders. This also resets the Batch color to white, in
     * case it had been left with a different setting before. If this Font is not an SDF or MSDF font, then this resets
     * batch's shader to the default (using {@code batch.setShader(null)}).
     * <br>
     * This is called automatically for {@link TextraLabel} and {@link TypingLabel} if it hasn't been called already.
     * You may still want to call this automatically for those cases if you have multiple such Labels that use the same
     * Font; in that case, you can draw several Labels without ending the current batch. You do need to set the shader
     * back to whatever you use for other items before you draw those, typically with {@code batch.setShader(null);} .
     * <br>
     * Like all distance field code here, this is dependent on calling {@link #resizeDistanceField(float, float)} in your
     * {@link com.badlogic.gdx.ApplicationListener#resize(int, int)} method, since that allows the distance field to be
     * scaled correctly to the screen. Without calling resizeDistanceField(), distance field fonts will look terrible.
     *
     * @param batch the Batch to instruct to use the appropriate shader for this font; should usually be a SpriteBatch
     */
    public void enableShader(Batch batch) {
        if (batch.getShader() != shader) {
            if (distanceField == DistanceFieldType.MSDF) {
                batch.setShader(shader);
//                float smoothing = 0.4f * actualCrispness * Math.max(cellHeight, cellWidth);
                float smoothing = 8f * actualCrispness * Math.max(cellHeight / originalCellHeight, cellWidth / originalCellWidth);
                batch.flush();
                shader.setUniformf("u_smoothing", smoothing);
                smoothingValues.put(batch, smoothing);
            } else if (distanceField == DistanceFieldType.SDF || distanceField == DistanceFieldType.SDF_OUTLINE) {
                batch.setShader(shader);
//                if(Gdx.app.getType() != Application.ApplicationType.Desktop || smoothingValues.get(batch) == null || smoothingValues.get(batch) <= 0) {
                    float smoothing = 4f * actualCrispness * Math.max(cellHeight / originalCellHeight, cellWidth / originalCellWidth);
                    batch.flush();
                    shader.setUniformf("u_smoothing", smoothing);
                    smoothingValues.put(batch, smoothing);
//                }
            } else {
                batch.setShader(null);
                smoothingValues.put(batch, 0f);
            }
        }
    }

    /**
     * If a distance field font needs to be drawn with a different size, different crispness, or a different Texture
     * altogether (such as to draw an icon or emoji), you can call this just before you start drawing distance field
     * text with this Font. You should only call this when you just changed something that affects how distance fields
     * are drawn, which primarily means {@link #actualCrispness}, {@link #cellHeight}, {@link #originalCellHeight},
     * {@link #cellWidth}, or {@link #originalCellWidth}, and also includes Texture changes to non-distance-field
     * Textures. This should not be called immediately after {@link #enableShader(Batch)}, since they do similar things.
     * This does nothing if this font is not a distance field font. It also does nothing if batch's shader is different
     * from the shader this Font uses. Otherwise, this flushes batch and sets a uniform ("u_smoothing") to a value
     * calculated with the ratio of current cell size to original (unscaled) cell size.
     * <br>
     * Typically, if you call {@link #pauseDistanceFieldShader(Batch)}, you call this method later to resume drawing
     * with a distance field. This is usually done automatically by {@link TextraLabel} and {@link TypingLabel}.
     * <br>
     * Like all distance field code here, this is dependent on calling {@link #resizeDistanceField(float, float)} in your
     * {@link com.badlogic.gdx.ApplicationListener#resize(int, int)} method, since that allows the distance field to be
     * scaled correctly to the screen. Without calling resizeDistanceField(), distance field fonts will look terrible.
     *
     * @param batch a Batch that should be running (between {@link Batch#begin()} and {@link Batch#end()})
     */
    public void resumeDistanceFieldShader(Batch batch) {
        if (batch.getShader() == shader) {
            if (distanceField == DistanceFieldType.MSDF) {
//                float smoothing = 0.4f * actualCrispness * Math.max(cellHeight, cellWidth);
                float smoothing = 8f * actualCrispness * Math.max(cellHeight / originalCellHeight, cellWidth / originalCellWidth);
                batch.flush();
                shader.setUniformf("u_smoothing", smoothing);
                smoothingValues.put(batch, smoothing);
            } else if (distanceField == DistanceFieldType.SDF || distanceField == DistanceFieldType.SDF_OUTLINE) {
//                if(Gdx.app.getType() != Application.ApplicationType.Desktop || smoothingValues.get(batch) == null || smoothingValues.get(batch) <= 0) {
                    float smoothing = 4f * actualCrispness * Math.max(cellHeight / originalCellHeight, cellWidth / originalCellWidth);
                    batch.flush();
                    shader.setUniformf("u_smoothing", smoothing);
                    smoothingValues.put(batch, smoothing);
//                }
            }
        } else if(shader == null) {
            batch.flush();
            if(batch.getShader().hasUniform("u_smoothing"))
                batch.getShader().setUniformf("u_smoothing", 0f);
            smoothingValues.put(batch, 0f);
        } else {
            batch.setShader(shader);
            if (distanceField == DistanceFieldType.MSDF) {
                float smoothing = 0.4f * actualCrispness * Math.max(cellHeight, cellWidth);
                batch.flush();
                shader.setUniformf("u_smoothing", smoothing);
                smoothingValues.put(batch, smoothing);
            } else if (distanceField == DistanceFieldType.SDF || distanceField == DistanceFieldType.SDF_OUTLINE) {
//                if(Gdx.app.getType() != Application.ApplicationType.Desktop || smoothingValues.get(batch) == null || smoothingValues.get(batch) <= 0) {
                    float smoothing = 0.2f * actualCrispness * Math.max(cellHeight, cellWidth);
                    batch.flush();
                    shader.setUniformf("u_smoothing", smoothing);
                    smoothingValues.put(batch, smoothing);
//                }
            }
        }
    }

    /**
     * If a distance field font needs to have its distance field effect disabled temporarily (such as to draw an icon
     * or emoji), you can call this just before you start drawing the non-distance-field images. You should only call
     * this just before drawing from a non-distance-field Texture. This flushes batch and sets a uniform ("u_smoothing")
     * to 0, unless that uniform was already 0 (then this doesn't flush, or do anything).
     * <br>
     * You can resume using the distance field by calling {@link #resumeDistanceFieldShader(Batch)}. This is usually
     * done automatically by {@link TextraLabel} and {@link TypingLabel}.
     * <br>
     * Like all distance field code here, this is dependent on calling {@link #resizeDistanceField(float, float)} in your
     * {@link com.badlogic.gdx.ApplicationListener#resize(int, int)} method, since that allows the distance field to be
     * scaled correctly to the screen. Without calling resizeDistanceField(), distance field fonts will look terrible.
     *
     * @param batch a Batch that should be running (between {@link Batch#begin()} and {@link Batch#end()})
     */
    public void pauseDistanceFieldShader(Batch batch) {
        if(batch.getShader() == shader){// && distanceField != DistanceFieldType.STANDARD) {
            float smoothing = smoothingValues.get(batch, 0f);
            if(smoothing == 0f) return;
            batch.flush();
            shader.setUniformf("u_smoothing", 0f);
            smoothingValues.put(batch, 0f);
        }
    }

    /**
     * Draws the specified text at the given x,y position (in world space) with a white foreground.
     *
     * @param batch typically a SpriteBatch
     * @param text  typically a String, but this can also be a StringBuilder or some custom class
     * @param x     the x position in world space to start drawing the text at (lower left corner)
     * @param y     the y position in world space to start drawing the text at (lower left corner)
     */
    public void drawText(Batch batch, CharSequence text, float x, float y) {
        drawText(batch, text, x, y, -2);
    }

    /**
     * Draws the specified text at the given x,y position (in world space) with the given foreground color.
     *
     * @param batch typically a SpriteBatch
     * @param text  typically a String, but this can also be a StringBuilder or some custom class
     * @param x     the x position in world space to start drawing the text at (lower left corner)
     * @param y     the y position in world space to start drawing the text at (lower left corner)
     * @param color an int color; typically this is RGBA, but custom shaders or Batches can use other kinds of color
     */
    public void drawText(Batch batch, CharSequence text, float x, float y, int color) {
        batch.setPackedColor(NumberUtils.intToFloatColor(Integer.reverseBytes(color)));
        GlyphRegion current;
        for (int i = 0, n = text.length(); i < n; i++) {
            batch.draw(current = mapping.get(text.charAt(i)), x + current.offsetX * scaleX, y + current.offsetY * scaleY,
                    current.getRegionWidth() * scaleX, current.getRegionHeight() * scaleY);
            x += current.getRegionWidth() * scaleX;
        }
    }

    /**
     * Draws a grid made of rectangular blocks of int colors (typically RGBA) at the given x,y position in world space.
     * This is only useful for monospace fonts. This assumes there is a full-block character at {@link #solidBlock}, so
     * you should have set that field; most fonts in {@link KnownFonts} have a solid block char set already.
     * The {@code colors} parameter should be a rectangular 2D array, and because any colors that are the default int
     * value {@code 0} will be treated as transparent RGBA values, if a value is not assigned to a slot in the array
     * then nothing will be drawn there. The 2D array is treated as [x][y] indexed here. This is usually called before
     * other methods that draw foreground text.
     * <br>
     * Internally, this uses {@link #drawVertices(Batch, Texture, float[])} to draw each rectangle with minimal
     * overhead, and this also means it is unaffected by the batch color unless drawVertices was overridden. If you want
     * to alter the colors using a shader, the shader will receive each color in {@code colors} as its {@code a_color}
     * attribute, the same as if it was passed via the batch color.
     * <br>
     * If you want to change the alpha of the colors array, you can use
     * {@link ColorUtils#multiplyAllAlpha(int[][], float)}.
     *
     * @param batch  typically a SpriteBatch
     * @param colors a 2D rectangular array of int colors (typically RGBA)
     * @param x      the x position in world space to draw the text at (lower left corner)
     * @param y      the y position in world space to draw the text at (lower left corner)
     */
    public void drawBlocks(Batch batch, int[][] colors, float x, float y) {
        drawBlocks(batch, solidBlock, colors, x, y);
    }

    /**
     * Draws a grid made of rectangular blocks of int colors (typically RGBA) at the given x,y position in world space.
     * This is only useful for monospace fonts.
     * The {@code blockChar} should visually be represented by a very large block, occupying all of a monospaced cell.
     * The {@code colors} parameter should be a rectangular 2D array, and because any colors that are the default int
     * value {@code 0} will be treated as transparent RGBA values, if a value is not assigned to a slot in the array
     * then nothing will be drawn there. The 2D array is treated as [x][y] indexed here. This is usually called before
     * other methods that draw foreground text.
     * <br>
     * Internally, this uses {@link #drawVertices(Batch, Texture, float[])} to draw each rectangle with minimal
     * overhead, and this also means it is unaffected by the batch color unless drawVertices was overridden. If you want
     * to alter the colors using a shader, the shader will receive each color in {@code colors} as its {@code a_color}
     * attribute, the same as if it was passed via the batch color.
     * <br>
     * If you want to change the alpha of the colors array, you can use
     * {@link ColorUtils#multiplyAllAlpha(int[][], float)}.
     *
     * @param batch     typically a SpriteBatch
     * @param blockChar a char that renders as a full block, occupying an entire monospaced cell with a color
     * @param colors    a 2D rectangular array of int colors (typically RGBA)
     * @param x         the x position in world space to draw the text at (lower left corner)
     * @param y         the y position in world space to draw the text at (lower left corner)
     */
    public void drawBlocks(Batch batch, char blockChar, int[][] colors, float x, float y) {
        final TextureRegion block = mapping.get(blockChar);
        if (block == null) return;
        final Texture parent = block.getTexture();
        final float ipw = 1.0f / parent.getWidth();
        final float iph = 1.0f / parent.getHeight();
        final float u = block.getU(),
                v = block.getV(),
                u2 = block.getU() + ipw,
                v2 = block.getV() + iph;
//        final float u = block.getU() + (block.getU2() - block.getU()) * 0.25f,
//                v = block.getV() + (block.getV2() - block.getV()) * 0.25f,
//                u2 = block.getU2() - (block.getU2() - block.getU()) * 0.25f,
//                v2 = block.getV2() - (block.getV2() - block.getV()) * 0.25f;

        x += 0x1p-8f; // not sure exactly why this is needed, but bad line artifacts show up otherwise...
        y += 0x1p-8f;
        vertices[0] = x;
        vertices[1] = y;
        //vertices[2] = color;
        vertices[3] = u;
        vertices[4] = v;

        vertices[5] = x;
        vertices[6] = y + cellHeight;
        //vertices[7] = color;
        vertices[8] = u;
        vertices[9] = v2;

        vertices[10] = x + cellWidth;
        vertices[11] = y + cellHeight;
        //vertices[12] = color;
        vertices[13] = u2;
        vertices[14] = v2;

        vertices[15] = x + cellWidth;
        vertices[16] = y;
        //vertices[17] = color;
        vertices[18] = u2;
        vertices[19] = v;
        for (int xi = 0, xn = colors.length, yn = colors[0].length; xi < xn; xi++) {
            for (int yi = 0; yi < yn; yi++) {
                if ((colors[xi][yi] & 254) != 0) {
                    vertices[2] = vertices[7] = vertices[12] = vertices[17] =
                            NumberUtils.intBitsToFloat(Integer.reverseBytes(colors[xi][yi] & -2));
                    drawVertices(batch, parent, vertices);
                }
                vertices[1] = vertices[16] += cellHeight;
                vertices[6] = vertices[11] += cellHeight;
            }
            vertices[0] = vertices[5] += cellWidth;
            vertices[10] = vertices[15] += cellWidth;
            vertices[1] = vertices[16] = y;
            vertices[6] = vertices[11] = y + cellHeight;
        }
    }

    /**
     * An internal method that draws blocks in a sequence specified by a {@code float[]}, with the block usually
     * {@link #solidBlock} (but not always). This is somewhat complicated; the sequence is typically drawn directly from
     * {@link BlockUtils}. Draws {@code block} at the given width and height, in the given packed color, rotating by the
     * specified amount in degrees.
     * @param batch    typically a SpriteBatch
     * @param sequence a sequence of instructions in groups of 4: starting x, starting y, width to draw, height to draw
     * @param block    the TextureRegion to use as a block for drawing; usually {@link #solidBlock}
     * @param color    the color as a packed float
     * @param x        the x position to draw at
     * @param y        the y position to draw at
     * @param width    the width of one cell for the purposes of sequence instructions
     * @param height   the height of one cell for the purposes of sequence instructions
     * @param rotation the rotation in degrees to use for the cell of blocks, with the origin in the center of the cell
     */
    protected void drawBlockSequence(Batch batch, float[] sequence, TextureRegion block, float color, float x, float y,
                                     float width, float height, float rotation) {
        drawBlockSequence(batch, sequence, block, color, x, y, width, height, rotation, 1f);
    }
    /**
     * An internal method that draws blocks in a sequence specified by a {@code float[]}, with the block usually
     * {@link #solidBlock} (but not always). This is somewhat complicated; the sequence is typically drawn directly from
     * {@link BlockUtils}. Draws {@code block} at the given width and height, in the given packed color, rotating by the
     * specified amount in degrees. This overload also allows specifying a breadth multiplier for box drawing character
     * lines, which can help match the aesthetic of a given font. The breadth multiplier only affects box drawing
     * characters, but not any with double lines, nor does it affect block elements.
     * @param batch    typically a SpriteBatch
     * @param sequence a sequence of instructions in groups of 4: starting x, starting y, width to draw, height to draw
     * @param block    the TextureRegion to use as a block for drawing; usually {@link #solidBlock}
     * @param color    the color as a packed float
     * @param x        the x position to draw at
     * @param y        the y position to draw at
     * @param width    the width of one cell for the purposes of sequence instructions
     * @param height   the height of one cell for the purposes of sequence instructions
     * @param rotation the rotation in degrees to use for the cell of blocks, with the origin in the center of the cell
     * @param breadth  a multiplier applied only to the size of box-drawing characters going across the line(s); breadth changes are not performed if this is 1
     */
    protected void drawBlockSequence(Batch batch, float[] sequence, TextureRegion block, float color, float x, float y,
                                     float width, float height, float rotation, float breadth) {
        final Texture parent = block.getTexture();
        final float ipw = 1f / parent.getWidth();
        final float iph = 1f / parent.getHeight();
        final float halfWidth = width * 0.5f;
        final float halfHeight = height * 0.5f;
        final float u = block.getU(),
                v = block.getV(),
                u2 = u + ipw,
                v2 = v - iph;
        final float sn = MathUtils.sinDeg(rotation);
        final float cs = MathUtils.cosDeg(rotation);

        float startX, startY, sizeX, sizeY, adjustment = 0f;
        if(sequence.length == 8 && breadth != 1f && sequence[0] == 0 && sequence[5] == 0) {
            // lousy right angles where both the left side and the bottom side are connected.
            adjustment = (BlockUtils.THIN_ACROSS) - BlockUtils.THIN_ACROSS * breadth;
        }
        for (int b = 0; b < sequence.length; b += 4) {
            startX = (sequence[b]);
            startY = (sequence[b + 1]);
            sizeX =  (sequence[b + 2]);
            sizeY =  (sequence[b + 3]);

            if(breadth != 1f){
                float thinAcross = BlockUtils.THIN_ACROSS * breadth;
                float wideAcross = BlockUtils.WIDE_ACROSS * breadth;

                if(sizeX == BlockUtils.THIN_ACROSS) sizeX = thinAcross;
                else if(sizeX == BlockUtils.WIDE_ACROSS) sizeX = wideAcross;
                else if(startX == 0f) {
                    if (sizeX == BlockUtils.THIN_OVER) sizeX -= thinAcross * 0.5f + adjustment;
                    else if (sizeX == BlockUtils.WIDE_OVER) sizeX -= wideAcross * 0.5f + adjustment;
                } else if(startX > 0f) {
                    if (sizeX == BlockUtils.THIN_OVER) sizeX += thinAcross * 0.5f;
                    else if (sizeX == BlockUtils.WIDE_OVER) sizeX += wideAcross * 0.5f;
                }

                if(sizeY == BlockUtils.THIN_ACROSS) sizeY = thinAcross;
                else if(sizeY == BlockUtils.WIDE_ACROSS) sizeY = wideAcross;
                else if(startY == 0f) {
                    if (sizeY == BlockUtils.THIN_OVER) sizeY -= thinAcross * 0.5f + adjustment;
                    else if (sizeY == BlockUtils.WIDE_OVER) sizeY -= wideAcross * 0.5f + adjustment;
                } else if(startY > 0f) {
                    if (sizeY == BlockUtils.THIN_OVER) sizeY += thinAcross * 0.5f;
                    else if (sizeY == BlockUtils.WIDE_OVER) sizeY += wideAcross * 0.5f;
                }

                if(startX == BlockUtils.THIN_START) startX -= thinAcross * 0.5f;
                else if(startX == BlockUtils.WIDE_START) startX -= wideAcross * 0.5f;
                if(startY == BlockUtils.THIN_START) startY -= thinAcross * 0.5f;
                else if(startY == BlockUtils.WIDE_START) startY -= wideAcross * 0.5f;
            }
            startX = startX * width - halfWidth;
            startY = startY * height - halfHeight;
            sizeX *= width;
            sizeY *= height;
            float p0x = startX;
            float p0y = startY + sizeY;
            float p1x = startX;
            float p1y = startY;
            float p2x = startX + sizeX;
            float p2y = startY;

            vertices[15] = /* handleIntegerPosition */((vertices[0] = /* handleIntegerPosition */(x + cs * p0x - sn * p0y)) - (vertices[5] = /* handleIntegerPosition */(x + cs * p1x - sn * p1y)) + (vertices[10] = /* handleIntegerPosition */(x + cs * p2x - sn * p2y)));
            vertices[16] = /* handleIntegerPosition */((vertices[1] = /* handleIntegerPosition */(y + sn * p0x + cs * p0y)) - (vertices[6] = /* handleIntegerPosition */(y + sn * p1x + cs * p1y)) + (vertices[11] = /* handleIntegerPosition */(y + sn * p2x + cs * p2y)));


            vertices[2] = color;
            vertices[3] = u;
            vertices[4] = v;

            vertices[7] = color;
            vertices[8] = u;
            vertices[9] = v2;

            vertices[12] = color;
            vertices[13] = u2;
            vertices[14] = v2;

            vertices[17] = color;
            vertices[18] = u2;
            vertices[19] = v;

            drawVertices(batch, parent, vertices);
        }
    }

    /**
     * An internal method that draws blocks in a sequence specified by a {@code mode}, with the block always
     * {@link #solidBlock}. Draws the solidBlock at a very small size (determined by {@code xPx} and {@code yPx}, which
     * are usually sized to one pixel each), repeating in a pattern to fill the given width and part of the given
     * height, in the given packed color, rotating by the specified amount in degrees.
     * @param batch    typically a SpriteBatch
     * @param mode     currently must be {@link #ERROR}, {@link #CONTEXT}, {@link #WARN}, {{@link #SUGGEST}, or
     * {@link #NOTE}, determining the pattern
     * @param x        the x position to draw at
     * @param y        the y position to draw at
     * @param width    the width of one cell in world units
     * @param xPx      the width of one pixel, approximately, in world units
     * @param yPx      the height of one pixel, approximately, in world units
     * @param rotation the rotation in degrees to use for the cell of blocks, with the origin in the center of the cell
     */
    protected void drawFancyLine(Batch batch, long mode, float x, float y, float width,
                                 float xPx, float yPx, float rotation) {
        final TextureRegion block = mapping.get(solidBlock);
        final Texture parent = block.getTexture();
        final float ipw = 1f / parent.getWidth();
        final float iph = 1f / parent.getHeight();
        final float u = block.getU(),
                v = block.getV(),
                u2 = u + ipw,
                v2 = v + iph;
        final float sn = MathUtils.sinDeg(rotation);
        final float cs = MathUtils.cosDeg(rotation);
        float color;
        if(mode == ERROR)
            color = PACKED_ERROR_COLOR; // red, 0xFF0000FF
        else if(mode == CONTEXT)
            color = PACKED_CONTEXT_COLOR; // forest green, 0x228B22FF
        else if(mode == WARN)
            color = PACKED_WARN_COLOR; // gold/saffron/yellow, 0xFFD510FF
        else if(mode == SUGGEST)
            color = PACKED_SUGGEST_COLOR; // light gray/silver, 0x999999FF
        else// if(mode == NOTE)
            color = PACKED_NOTE_COLOR; // cyan/denim, 0x3088B8FF
        color = ColorUtils.multiplyAlpha(color, batch.getColor().a);
        int index = 0;
        for (float startX = 0f, shiftY = 0f; startX <= width; startX += xPx, index++) {
            float p0x;
            float p0y;
            float p1x;
            float p1y;
            float p2x;
            float p2y;
            float shiftX = startX;
            if(mode == ERROR) {
                shiftY = (index & 1) * yPx;
            } else if(mode == CONTEXT) {
                shiftX -= (index & 2) * xPx;
                shiftY = -(index & 1) * yPx;
            } else if(mode == WARN) {
                shiftX += (~index & 1) * xPx;
                shiftY = (~index & 1) * yPx;
            } else if(mode == SUGGEST) {
                shiftX -= (index & index >>> 1 & 1) * xPx;
                shiftY = -(index & index >>> 1 & 1) * yPx;
            } else {
                shiftY = (index >>> 1 & 1) * yPx;
            }
            p0x = shiftX;
            p0y = shiftY + yPx;
            p1x = shiftX;
            p1y = shiftY;
            p2x = shiftX + xPx;
            p2y = shiftY;
//            else {
//                long time = TimeUtils.millis() >>> 5 & 0x7FFFFFL;
//                shiftX = NoiseUtils.octaveNoise1D(time * 0x1p-4f, index);
//                shiftY = NoiseUtils.octaveNoise1D((time + 0x9E3779 & 0x7FFFFFL) * 0x1p-4f, ~index);
//                p0x = shiftX;
//                p0y = shiftY + yPx;
//                p1x = shiftX;
//                p1y = shiftY;
//                p2x = shiftX + xPx;
//                p2y = shiftY;
//            }


            vertices[15] = /* handleIntegerPosition */((vertices[0] = /* handleIntegerPosition */(x + cs * p0x - sn * p0y)) - (vertices[5] = /* handleIntegerPosition */(x + cs * p1x - sn * p1y)) + (vertices[10] = /* handleIntegerPosition */(x + cs * p2x - sn * p2y)));
            vertices[16] = /* handleIntegerPosition */((vertices[1] = /* handleIntegerPosition */(y + sn * p0x + cs * p0y)) - (vertices[6] = /* handleIntegerPosition */(y + sn * p1x + cs * p1y)) + (vertices[11] = /* handleIntegerPosition */(y + sn * p2x + cs * p2y)));


            vertices[2] = color;
            vertices[3] = u;
            vertices[4] = v;

            vertices[7] = color;
            vertices[8] = u;
            vertices[9] = v2;

            vertices[12] = color;
            vertices[13] = u2;
            vertices[14] = v2;

            vertices[17] = color;
            vertices[18] = u2;
            vertices[19] = v;

            drawVertices(batch, parent, vertices);
        }
    }

    /**
     * Draws the specified text at the given x,y position (in world space), parsing an extension of libGDX markup
     * and using it to determine color, size, position, shape, strikethrough, underline, case, and scale of the given
     * CharSequence. The text drawn will start as white, with the normal size as by {@link #cellWidth} and
     * {@link #cellHeight}, normal case, and without bold, italic, superscript, subscript, strikethrough, or
     * underline. Markup starts with {@code [}; the next non-letter character determines what that piece of markup
     * toggles. Markup this knows:
     * <ul>
     *     <li>{@code []} undoes the most-recently-applied format change.</li>
     *     <li>{@code [ ]} clears all markup to the initial state without any applied.</li>
     *     <li>{@code [(label)]} temporarily stores the current formatting state as {@code label}.</li>
     *     <li>{@code [ label]} re-applies the formatting state stored as {@code label}, if there is one.</li>
     *     <li>{@code [[} escapes a literal left bracket, producing it without changing state.</li>
     *     <li>{@code [+name]}, where name is the name of a TextureRegion from an atlas added to this Font with
     *     {@link #addAtlas(TextureAtlas)}, produces the corresponding TextureRegion (scaled when drawn) without
     *     changing state. If no atlas has been added, this emits undefined character(s) instead.</li>
     *     <li>{@code [*]} toggles bold mode.</li>
     *     <li>{@code [/]} toggles italic (technically, oblique) mode.</li>
     *     <li>{@code [^]} toggles superscript mode (and turns off subscript or midscript mode).</li>
     *     <li>{@code [=]} toggles midscript mode (and turns off superscript or subscript mode).</li>
     *     <li>{@code [.]} toggles subscript mode (and turns off superscript or midscript mode).</li>
     *     <li>{@code [_]} toggles underline mode.</li>
     *     <li>{@code [~]} toggles strikethrough mode.</li>
     *     <li>{@code [!]} toggles all upper case mode.</li>
     *     <li>{@code [,]} toggles all lower case mode.</li>
     *     <li>{@code [;]} toggles capitalize each word mode.</li>
     *     <li>{@code [%P]}, where P is any decimal number, changes the scale to that percentage.
     *     {@code [%100]} is the initial value and sets to 100% scale, as in "not scaled at all."
     *     Note that this is not restricted to any increments; even {@code [%0.0123]} is technically valid.</li>
     *     <li>{@code [%]}, with no number just after it, resets scale to 100%.</li>
     *     <li>{@code [?MODE]}, where MODE can be (case-insensitive) one of "black outline", "white outline",
     *     "red outline", "blue outline", "yellow outline", "shiny", "drop shadow"/"shadow", "neon", "halo",
     *     "error", "warn", "note", "context", "suggest", "jostle", or "small caps",
     *     will enable that alternate mode. If MODE is empty or not recognized, this will disable any current active
     *     mode. If any mode is non-empty and recognized, it will be enabled and replace any current active mode.
     *     Disabling a mode should always use the empty string for MODE. The "black outline" mode isn't actually a mode,
     *     and internally enables the same feature as {@code [#]}; it is still recognized for compatibility. Note that
     *     this means it can be enabled at the same time as any other mode, and that it will not be disabled by
     *     {@code [?]}, though it can be toggled off or back on by {@code [#]} (which is the preferred way to use the
     *     outline). Other "outline" modes also enable the outline if it isn't active, but change its color. The outline
     *     can always be toggled on or off, regardless of color, by {@code [#]}.</li>
     *     <li>{@code [%?MODE]} is a synonym for the preceding {@code [?MODE]} markup for compatibility.</li>
     *     <li>{@code [%^MODE]} is a synonym for the preceding {@code [?MODE]} markup for compatibility.</li>
     *     <li>{@code [?]}, {@code [%?]} and {@code [%^]} are equivalent ways to disable a special mode, if it is active.</li>
     *     <li>{@code [@Name]}, where Name is a key in family, changes the current Font used for rendering to the Font
     *     in this.family by that name. This is ignored if family is null.</li>
     *     <li>{@code [@]}, with no text just after it, resets the font to this one (which should be item 0 in family,
     *     if family is non-null).</li>
     *     <li>{@code [#]} toggles a black outline around text. This can have its color changed by some special modes.</li>
     *     <li>{@code [#HHHHHHHH]}, where HHHHHHHH is a hex RGB888 or RGBA8888 int color, changes the color.</li>
     *     <li>{@code [#HHH]} is also a valid way to set the color using CSS-style 3 hex digits, each of which is
     *     effectively duplicated to make a 6-digit RGB888 code. {@code [#HHHH]} is a way to do this with alpha.</li>
     *     <li>{@code [COLORNAME]}, where "COLORNAME" is a color name or description that will be looked up in
     *     {@link #getColorLookup()}, changes the color. By default, this can receive ALL_CAPS names from {@link Colors}
     *     in libGDX, any names from {@link com.github.tommyettinger.textra.utils.Palette}, or mixes of one or
     *     more color names with adjectives like "dark". The name can optionally be preceded by {@code |}, which allows
     *     looking up colors with names that contain punctuation. This doesn't do much if using the default ColorLookup,
     *     {@link ColorLookup#DESCRIPTIVE}, because it only evaluates ASCII letters and numbers, and treats everything
     *     else as a separator.</li>
     * </ul>
     * <br>
     * Parsing markup for a full screen every frame typically isn't necessary, and you may want to store the most recent
     * glyphs by calling {@link #markup(String, Layout)} and render its result with
     * {@link #drawGlyphs(Batch, Layout, float, float)} every frame.
     *
     * @param batch typically a SpriteBatch
     * @param text  typically a String with markup, but this can also be a StringBuilder or some custom class
     * @param x     the x position in world space to start drawing the text at (lower left corner)
     * @param y     the y position in world space to start drawing the text at (lower left corner)
     * @return the number of glyphs drawn
     */
    public int drawMarkupText(Batch batch, String text, float x, float y) {
        Layout layout = tempLayout;
        layout.clear();
        markup(text, tempLayout);
        drawGlyphs(batch, tempLayout, x, y);
        return tempLayout.countGlyphs();
    }

    /**
     * Draws the specified Layout of glyphs with a Batch at a given x, y position, drawing the full layout.
     *
     * @param batch  typically a SpriteBatch
     * @param layout typically returned as part of {@link #markup(String, Layout)}
     * @param x      the x position in world space to start drawing the glyph at (lower left corner)
     * @param y      the y position in world space to start drawing the glyph at (lower left corner)
     * @return the total distance in world units all drawn Lines use up from left to right
     */
    public float drawGlyphs(Batch batch, Layout layout, float x, float y) {
        return drawGlyphs(batch, layout, x, y, Align.left);
    }

    /**
     * Draws the specified Layout of glyphs with a Batch at a given x, y position, using {@code align} to
     * determine how to position the text. Typically, align is {@link Align#left}, {@link Align#center}, or
     * {@link Align#right}, which make the given x,y point refer to the lower-left corner, center-bottom edge point, or
     * lower-right corner, respectively.
     *
     * @param batch  typically a SpriteBatch
     * @param layout typically returned by {@link #markup(String, Layout)}
     * @param x      the x position in world space to start drawing the glyph at (where this is depends on align)
     * @param y      the y position in world space to start drawing the glyph at (where this is depends on align)
     * @param align  an {@link Align} constant; if {@link Align#left}, x and y refer to the lower left corner
     * @return the total distance in world units all drawn Lines use up from left to right
     */
    public float drawGlyphs(Batch batch, Layout layout, float x, float y, int align) {
        return drawGlyphs(batch, layout, x, y, align, 0,
                0, 0
//                x + (Align.isRight(align) ? glyphs.getWidth() : Align.isCenterHorizontal(align) ? glyphs.getWidth() * 0.5f : 0f),
//                y + (Align.isTop(align) ? glyphs.getHeight() : Align.isCenterVertical(align) ? glyphs.getHeight() * 0.5f : 0f)
        );
    }

    /**
     * Draws the specified Layout of glyphs with a Batch at a given x, y position, rotated using degrees around the
     * given origin point, using {@code align} to determine how to position the text. Typically, align is
     * {@link Align#left}, {@link Align#center}, or {@link Align#right}, but it can have a vertical component as well.
     *
     * @param batch    typically a SpriteBatch
     * @param layout   typically returned by {@link #markup(String, Layout)}
     * @param x        the x position in world space to start drawing the glyph at (where this is depends on align)
     * @param y        the y position in world space to start drawing the glyph at (where this is depends on align)
     * @param align    an {@link Align} constant; if {@link Align#left}, x and y refer to the left edge of the first Line
     * @param rotation measured in degrees counterclockwise, typically 0-360, and applied to the whole Layout
     * @param originX the x position in world space of the point to rotate around
     * @param originY the y position in world space of the point to rotate around
     * @return the total distance in world units all drawn Lines use up from lines along the given rotation
     */
    public float drawGlyphs(Batch batch, Layout layout, float x, float y, int align, float rotation, float originX, float originY) {
        if(layout == null || layout.countGlyphs() == 0) return 0f;
        float drawn = 0;
        float sn = MathUtils.sinDeg(rotation);
        float cs = MathUtils.cosDeg(rotation);
        final int lines = layout.lines();
        float baseX = x, baseY = y;
        int a = 0;

        for (int ln = 0; ln < lines; ln++) {
            Line line = layout.getLine(ln);
            baseX += sn * line.height;
            baseY -= cs * line.height;

            x = baseX;
            y = baseY;

            final float worldOriginX = x + originX;
            final float worldOriginY = y + originY;
            float fx = -originX;
            float fy = -originY;
            x = cs * fx - sn * fy + worldOriginX;
            y = sn * fx + cs * fy + worldOriginY;

            if (Align.isCenterHorizontal(align)) {
                x -= cs * (line.width * 0.5f);
                y -= sn * (line.width * 0.5f);
            } else if (Align.isRight(align)) {
                x -= cs * line.width;
                y -= sn * line.width;
            }
//            x -= sn * (0.5f * line.height);
//            y += cs * (0.5f * line.height);

            int kern = -1;
            long glyph;
            float single, xChange = 0f, yChange = 0f;

            boolean curly = false, initial = true;
            for (int i = 0, n = line.glyphs.size; i < n; i++) {
                glyph = line.glyphs.get(i);
                char ch = (char) glyph;
                float advance = layout.advances.get(a);
                float sizingX = layout.sizing.get(a << 1);
                float sizingY = layout.sizing.get(a << 1 | 1);
                a++;
                if(omitCurlyBraces) {
                    if (curly) {
                        if (ch == '}') {
                            curly = false;
                            continue;
                        } else if (ch == '{')
                            curly = false;
                        else continue;
                    } else if (ch == '{') {
                        curly = true;
                        continue;
                    }
                }
                Font font = null;
                if (family != null) font = family.connected[(int) (glyph >>> 16 & 15)];
                if (font == null) font = this;

                if (font.kerning != null) {
                    kern = kern << 16 | (int) (glyph & 0xFFFF);
                    float amt = font.kerning.get(kern, 0)
                            * font.scaleX * advance;
                    xChange += cs * amt;
                    yChange += sn * amt;
                }
                if(initial){

                    xChange -= font.cellWidth * 0.5f;
                    yChange += font.cellHeight * 0.5f;

                    xChange += cs * font.cellWidth * 0.5f;
                    yChange += sn * font.cellWidth * 0.5f;

                    xChange += sn * font.descent * font.scaleY * 0.5f;
                    yChange -= cs * font.descent * font.scaleY * 0.5f;

                    xChange -= sn * line.height * 0.5f;
                    yChange += cs * line.height * 0.5f;

                    final Font.GlyphRegion reg = font.mapping.get((int) (glyph & 0xFFFF));
                    if(!isMono && reg != null && !(ch >= '\uE000' && ch < '\uF800')) {
                        float ox = reg.offsetX;
                        if (Float.isNaN(ox)) ox = 0f;
                        else
                            ox *= font.scaleX * advance;
                        if (ox < 0) {
                            xChange -= cs * ox;
                            yChange -= sn * ox;
                        }
                    }
                    initial = false;
                }
                single = drawGlyph(batch, glyph, x + xChange, y + yChange, rotation, sizingX, sizingY, 0, advance);
                xChange += cs * single;
                yChange += sn * single;
                drawn += single;
            }

        }
        return drawn;
    }

    /**
     * Gets the distance to advance the cursor after drawing {@code glyph}, scaled by {@code scale} as if drawing.
     * This handles monospaced fonts correctly and ensures that for variable-width fonts, subscript, midscript, and
     * superscript halve the advance amount. This does not consider kerning, if the font has it. If the glyph is fully
     * transparent, this does not draw it at all, and treats its x advance as 0. This version of xAdvance does not
     * read the scale information from glyph, and instead takes it from the scale parameter. This takes a Font to allow
     * for families to swap out the current font for a different one.
     *
     * @param font  the Font object to use to measure
     * @param scale the scale to draw the glyph at, usually {@link #scaleX} and possibly adjusted by per-glyph scaling
     * @param glyph a long encoding the color, style information, and char of a glyph, as from a {@link Line}
     * @return the (possibly non-integer) amount to advance the cursor when you draw the given glyph, not counting kerning
     */
    public static float xAdvance(Font font, float scale, long glyph) {
        if (glyph >>> 32 == 0L) return 0;
        char ch = (char) glyph;
        if((glyph & SMALL_CAPS) == SMALL_CAPS) ch = Category.caseUp(ch);
        GlyphRegion tr = font.mapping.get(ch);
        if (tr == null) return 0f;
        float changedW = tr.xAdvance * scale;
        if (!font.isMono) {
            if ((glyph & SUPERSCRIPT) != 0L) {
                changedW *= 0.5f;
            }
        }
        return changedW;
    }

    /**
     * Given a Layout that uses this Font, this will recalculate the width and height of each Line in layout, changing
     * the values in layout if they are incorrect. This returns the total width of the measured Layout. Most usage will
     * not necessarily need the return value; either this is called to fix incorrect size information on a Layout, or
     * the Layout this modifies will be queried for its {@link Layout#getWidth()} and/or {@link Layout#getHeight()}.
     * @param layout a Layout object that may have the width and height of its lines modified (its content won't change)
     * @return the total width of the measured Layout, as a float
     */
    public float calculateSize(Layout layout) {
        float w = 0f;
        float currentHeight = 0f;
        int a = 0;
        for (int ln = 0; ln < layout.lines(); ln++) {
            float drawn = 0f;
            float scaleX;
            float advance, sizingY;
            Line line = layout.getLine(ln);
            LongArray glyphs = line.glyphs;
            boolean curly = false, initial = true;
            int kern = -1;
            float amt;
            line.height = currentHeight;
            for (int i = 0, n = glyphs.size; i < n; i++) {
                long glyph = glyphs.get(i);
                char ch = (char) glyph;
                advance = layout.advances.get(a);
                a++;
                if((glyph & SMALL_CAPS) == SMALL_CAPS) ch = Category.caseUp(ch);
                if(omitCurlyBraces) {
                    if (curly) {
                        if (ch == '}') {
                            curly = false;
                            continue;
                        } else if (ch == '{')
                            curly = false;
                        else continue;
                    } else if (ch == '{') {
                        curly = true;
                        continue;
                    }
                }
                Font font = null;
                if (family != null) font = family.connected[(int) (glyph >>> 16 & 15)];
                if (font == null) font = this;
                GlyphRegion tr = font.mapping.get(ch);
                if (tr == null) continue;
                if (font.kerning != null) {
                    kern = kern << 16 | ch;
                    if(ch >= 0xE000 && ch < 0xF800)
                        scaleX = (font.isMono ? 1f : advance) * font.cellHeight / tr.getMaxDimension() * font.inlineImageStretch;
                    else
                        scaleX = font.scaleX * (font.isMono ? 1f : advance) * (1f + 0.5f * (-(glyph & SUPERSCRIPT) >> 63));
                    line.height = Math.max(line.height, (currentHeight = font.cellHeight * advance));
                    amt = font.kerning.get(kern, 0) * scaleX;
                    float changedW = tr.xAdvance * scaleX;
                    if(Float.isNaN(tr.offsetX))
                        changedW = font.cellWidth * advance;
                    else if(initial && !font.isMono /* && !(ch >= '\uE000' && ch < '\uF800') */ ){
                        float ox = tr.offsetX * scaleX;
                        if(ox < 0) changedW -= ox;
                    }
                    initial = false;
                    drawn += changedW + amt;
                } else {
                    line.height = Math.max(line.height, (currentHeight = font.cellHeight * advance));
                    if(ch >= 0xE000 && ch < 0xF800)
                        scaleX = (font.isMono ? 1f : advance) * font.cellHeight / tr.getMaxDimension() * font.inlineImageStretch;
                    else
                        scaleX = font.scaleX * (font.isMono ? 1f : advance) * ((glyph & SUPERSCRIPT) != 0L && !font.isMono ? 0.5f : 1.0f);
                    float changedW = tr.xAdvance * scaleX;
                    if(Float.isNaN(tr.offsetX))
                        changedW = font.cellWidth * advance;
                    else if(initial && !font.isMono /* && !(ch >= '\uE000' && ch < '\uF800') */ ){
                        float ox = tr.offsetX * scaleX;
                        if(ox < 0) changedW -= ox;
                    }
                    initial = false;
                    drawn += changedW;
                }
            }
            line.width = drawn;
            w = Math.max(w, drawn);
        }
        return layout.getWidth();
    }

    /**
     * Uses the given Layout's {@link Layout#getJustification()} setting to justify all applicable Lines.
     * @param layout a Layout that will be modified if its {@link Layout#getJustification()} was set
     * @return {@code layout}, for chaining
     */
    public float justify(Layout layout) {
        if(layout.justification == Justify.NONE) return layout.getWidth();
        int lineCount = layout.lines();
        PER_LINE:
        for (int l = 0; l < lineCount; l++) {
            Line currentLine = layout.getLine(l);
            if(layout.justification.ignoreLastLine && (l + 1 == lineCount || (currentLine.glyphs.isEmpty() || (char)currentLine.glyphs.peek() == '\n'))) {
                continue;
            }
            int start = layout.countGlyphsBeforeLine(l);
            if(layout.justification.affectAllGlyphs) {
                Font font = null;
                int lastIndex = currentLine.glyphs.size - 1;
                long glyph = currentLine.glyphs.get(lastIndex);
                while ((char)glyph == '\n' || (char)glyph == ' ') {
                    --lastIndex;
                    if(lastIndex < 0) continue PER_LINE;
                    glyph = currentLine.glyphs.get(lastIndex);
                }
                if (family != null) font = family.connected[(int) (glyph >>> 16 & 15)];
                if (font == null) font = this;
                float lastAdvance = xAdvance(font, font.scaleX * layout.advances.get(start + lastIndex), glyph);
                float multiplier = (layout.targetWidth - lastAdvance) / (currentLine.width - lastAdvance);
                for (int g = 0, n = lastIndex; g < n; g++) {
                    layout.advances.mul(start + g, multiplier);
                }
//                System.out.println("last used char is " + (char)currentLine.glyphs.get(lastIndex) + " with advance " + layout.advances.get(start + lastIndex));
//                System.out.println("last real char is " + (char)currentLine.glyphs.get(currentLine.glyphs.size - 1) + " with advance " + layout.advances.get(start + currentLine.glyphs.size - 1));

//                for (int g = lastIndex + 1, n = currentLine.glyphs.size; g < n; g++) {0
//                    layout.advances.mul(start + g, 0f);
//                }
                if(currentLine.glyphs.size > 1) currentLine.width = layout.targetWidth;
            }
            else if(layout.justification.affectSpaces) {
                float sumWidth = 0f;
                for (int g = 0, n = currentLine.glyphs.size - 1; g < n; g++) {
                    long glyph = currentLine.glyphs.get(g);
                    char ch = (char) glyph;
                    if (ch == ' ') {
                        Font font = null;
                        if (family != null) font = family.connected[(int) (glyph >>> 16 & 15)];
                        if (font == null) font = this;
                        GlyphRegion tr = font.mapping.get(ch);
                        if (tr == null) continue; // if space cannot be rendered, don't use it!
                        float advance = xAdvance(font, font.scaleX * layout.advances.get(start + g), glyph);
                        sumWidth += advance;
                    }
                }
                if (!MathUtils.isZero(sumWidth) && currentLine.glyphs.size > 1) {
                    int lastIndex = currentLine.glyphs.size - 1;
                    long glyph = currentLine.glyphs.get(lastIndex);
                    while ((char)glyph == '\n' || (char)glyph == ' ') {
                        --lastIndex;
                        if(lastIndex < 0) continue PER_LINE;
                        glyph = currentLine.glyphs.get(lastIndex);
                    }
                    float multiplier = (layout.targetWidth - currentLine.width + sumWidth) / (sumWidth);
                    for (int g = 0, n = lastIndex; g < n; g++) {
                        if ((char) currentLine.glyphs.get(g) == ' ')
                            layout.advances.mul(start + g, multiplier);
                    }
                    currentLine.width = layout.targetWidth;
                }
            }
        }
        return layout.getWidth();
    }

    /**
     * Not meant for general use; calculates the x-positions before every glyph in {@code layout}, including invisible
     * ones. Appends to {@code advances} with the position values, which can go up and down as lines change.
     * @param layout will have each line processed and appended to advances
     * @param advances will have the positions of each glyph in line appended to it
     * @return the highest x-position after the last glyph on any line
     */
    public float calculateXAdvances(Layout layout, FloatArray advances) {
        float max = -1e30f;
        int a = 0;
        for (int ln = 0, len = layout.lines(); ln < len; ln++) {
            Line line = layout.getLine(ln);
            float scaleX;
            float scale;
            LongArray glyphs = line.glyphs;
            advances.ensureCapacity(line.glyphs.size + 1);
            boolean curly = false, initial = true;
            int kern = -1;
            float amt, total = 0f;
            line.height = 0f;
            for (int i = 0, n = glyphs.size; i < n; i++) {
                long glyph = glyphs.get(i);
                char ch = (char) glyph;
                float advance = layout.advances.get(a++);
                if(omitCurlyBraces) {
                    if (curly) {
                        if (ch == '}') {
                            curly = false;
                            advances.add(0f);
                            continue;
                        } else if (ch == '{')
                            curly = false;
                        else {
                            advances.add(0f);
                            continue;
                        }
                    } else if (ch == '{') {
                        curly = true;
                        advances.add(0f);
                        continue;
                    }
                }
                Font font = null;
                if (family != null) font = family.connected[(int) (glyph >>> 16 & 15)];
                if (font == null) font = this;
                GlyphRegion tr = font.mapping.get(ch);
                if (tr == null) {
                    advances.add(0f);
                    continue;
                }
                if (font.kerning != null) {
                    kern = kern << 16 | ch;
                    scale = advance;
                    if((char)glyph >= 0xE000 && (char)glyph < 0xF800)
                        scaleX = scale * font.cellHeight / tr.getMaxDimension() * font.inlineImageStretch;
                    else
                        scaleX = font.scaleX * scale * (1f + 0.5f * (-(glyph & SUPERSCRIPT) >> 63));
                    line.height = Math.max(line.height, (font.cellHeight /* - font.descent * font.scaleY */) * scale);
                    amt = font.kerning.get(kern, 0) * scaleX;
                    float changedW = xAdvance(font, scaleX, glyph);
                    if(initial) {
                        if (!(ch >= '\uE000' && ch < '\uF800')) {
                            float ox = font.mapping.get((int) (glyph & 0xFFFF), font.defaultValue).offsetX
                                    * scaleX;
                            if (ox < 0) changedW -= ox;
                        }
                        initial = false;
                    }
                    advances.add(total);
                    total += changedW + amt;
                } else {
                    scale = advance;
                    line.height = Math.max(line.height, (font.cellHeight /* - font.descent * font.scaleY */) * scale);
                    if((char)glyph >= 0xE000 && (char)glyph < 0xF800){
                        scaleX = scale * font.cellHeight / tr.getMaxDimension() * font.inlineImageStretch;
                    }
                    else
                        scaleX = font.scaleX * scale * ((glyph & SUPERSCRIPT) != 0L && !font.isMono ? 0.5f : 1.0f);
                    float changedW = xAdvance(font, scaleX, glyph);
                    if (font.isMono)
                        changedW += tr.offsetX * scaleX;
                    else if(initial){
                        if(!(ch >= '\uE000' && ch < '\uF800')) {
                            float ox = font.mapping.get((int) (glyph & 0xFFFF), font.defaultValue).offsetX
                                    * scaleX;
                            if (ox < 0) changedW -= ox;
                        }
                        initial = false;
                    }
                    advances.add(total);
                    total += changedW;
                }
            }
            max = Math.max(max, total);
        }
        return max;
    }

    /*
     * If {@link #integerPosition} is true, this returns {@code p} rounded to the nearest int; otherwise this just
     * returns p unchanged.
     * @param p a float that could be rounded
     * @return either p rounded to the nearest int or p unchanged, depending on {@link #integerPosition}
     */

    /**
     * Currently, this is only an extension point for code that wants to ensure integer positions; it does nothing on
     * its own other than return its argument unchanged. See {@link #integerPosition} for more.
     * @param p a float that could be rounded (it will not be unless this is overridden)
     * @return unless overridden, p without changes
     */
    protected float handleIntegerPosition(float p) {
        return p;//integerPosition ? MathUtils.round(p) : p;
    }

    /**
     * Draws the specified glyph with a Batch at the given x, y position. The glyph contains multiple types of data all
     * packed into one {@code long}: the bottom 16 bits store a {@code char}, the roughly 16 bits above that store
     * formatting (bold, underline, superscript, etc.), and the remaining upper 32 bits store color as RGBA.
     *
     * @param batch typically a SpriteBatch
     * @param glyph a long storing a char, format, and color; typically part of a longer formatted text as a LongArray
     * @param x     the x position in world space to start drawing the glyph at (lower left corner)
     * @param y     the y position in world space to start drawing the glyph at (lower left corner)
     * @return the distance in world units the drawn glyph uses up for width, as in a line of text
     */
    public float drawGlyph(Batch batch, long glyph, float x, float y) {
        return drawGlyph(batch, glyph, x, y, 0f, 1f, 1f, 0, 1f);
    }

    /**
     * Draws the specified glyph with a Batch at the given x, y position and with the specified counterclockwise
     * rotation, measured in degrees. The glyph contains multiple types of data all packed into one {@code long}:
     * the bottom 16 bits store a {@code char}, the roughly 16 bits above that store formatting (bold, underline,
     * superscript, etc.), and the remaining upper 32 bits store color as RGBA. Rotation is not stored in the long
     * glyph; it may change frequently or as part of an animation.
     *
     * @param batch    typically a SpriteBatch
     * @param glyph    a long storing a char, format, and color; typically part of a longer formatted text as a LongList
     * @param x        the x position in world space to start drawing the glyph at (lower left corner)
     * @param y        the y position in world space to start drawing the glyph at (lower left corner)
     * @param rotation what angle to rotate the glyph, measured in degrees counterclockwise
     * @return the distance in world units the drawn glyph uses up for width, as in a line of text along the given rotation
     */
    public float drawGlyph(Batch batch, long glyph, float x, float y, float rotation) {
        return drawGlyph(batch, glyph, x, y, rotation, 1f, 1f, 0, 1f);
    }

    /**
     * Draws the specified glyph with a Batch at the given x, y position, with the specified counterclockwise
     * rotation, measured in degrees, and with the specified x and y sizing/scaling, which are meant to be treated
     * independently of the incremental scales in a glyph, and can be smooth. The glyph contains multiple types of data
     * all packed into one {@code long}: the bottom 16 bits store a {@code char}, the roughly 16 bits above that store
     * formatting (bold, underline, superscript, etc.), and the remaining upper 32 bits store color as RGBA. Rotation is
     * not stored in the long glyph; it may change frequently or as part of an animation. Sizing isn't part of the glyph
     * either, and is meant to be handled by Effects in TypingLabel, and does not affect the text metrics.
     *
     * @param batch    typically a SpriteBatch
     * @param glyph    a long storing a char, format, and color; typically part of a longer formatted text as a LongList
     * @param x        the x position in world space to start drawing the glyph at (lower left corner)
     * @param y        the y position in world space to start drawing the glyph at (lower left corner)
     * @param rotation what angle to rotate the glyph, measured in degrees counterclockwise
     * @param sizingX  the multiple for the glyph to be stretched on x, where 1 is "no change"; does not affect metrics
     * @param sizingY  the multiple for the glyph to be stretched on y, where 1 is "no change"; does not affect metrics
     * @return the distance in world units the drawn glyph uses up for width, as in a line of text along the given rotation
     */
    public float drawGlyph(Batch batch, long glyph, float x, float y, float rotation, float sizingX, float sizingY) {
        return drawGlyph(batch, glyph, x, y, rotation, sizingX, sizingY, 0, sizingX);
    }
    /**
     * Draws the specified glyph with a Batch at the given x, y position, with the specified counterclockwise
     * rotation, measured in degrees, and with the specified x and y sizing/scaling, which are meant to be treated
     * independently of the incremental scales in a glyph, and can be smooth. The glyph contains multiple types of data
     * all packed into one {@code long}: the bottom 16 bits store a {@code char}, the roughly 16 bits above that store
     * formatting (bold, underline, superscript, etc.), and the remaining upper 32 bits store color as RGBA. Rotation is
     * not stored in the long glyph; it may change frequently or as part of an animation. Sizing isn't part of the glyph
     * either, and is meant to be handled by Effects in TypingLabel, and does not affect the text metrics.
     *
     * @param batch    typically a SpriteBatch
     * @param glyph    a long storing a char, format, and color; typically part of a longer formatted text as a LongList
     * @param x        the x position in world space to start drawing the glyph at (lower left corner)
     * @param y        the y position in world space to start drawing the glyph at (lower left corner)
     * @param rotation what angle to rotate the glyph, measured in degrees counterclockwise
     * @param sizingX  the multiple for the glyph to be stretched on x, where 1 is "no change"; does not affect metrics
     * @param sizingY  the multiple for the glyph to be stretched on y, where 1 is "no change"; does not affect metrics
     * @param backgroundColor an RGBA8888 color to use for a block background behind the glyph; won't be drawn if 0
     * @param advanceMultiplier the multiplier applied to the amount of space the glyph will occupy on a line, where 1 is "no change"
     * @return the distance in world units the drawn glyph uses up for width, as in a line of text along the given rotation
     */
    public float drawGlyph(Batch batch, long glyph, float x, float y, float rotation, float sizingX, float sizingY, int backgroundColor, float advanceMultiplier) {
        final float sin = MathUtils.sinDeg(rotation);
        final float cos = MathUtils.cosDeg(rotation);

        Font font = null;
        if (family != null) font = family.connected[(int) (glyph >>> 16 & 15)];
        if (font == null) font = this;
        char c = (char) glyph;
        boolean squashed = false, jostled = false;
        if((glyph & ALTERNATE_MODES_MASK) == SMALL_CAPS) {
            squashed = (c != (c = Category.caseUp(c)));
            glyph = (glyph & 0xFFFFFFFFFFFF0000L) | c;
        } else {
            jostled = (glyph & ALTERNATE_MODES_MASK) == JOSTLE;
        }

        GlyphRegion tr = font.mapping.get(c);
        if (tr == null) return 0f;


        if(latestTexture != (latestTexture = tr.getTexture())) {
            boolean located = false;
            for (int p = 0; p < font.parents.size; p++) {
                if (font.parents.get(p).getTexture() == latestTexture) {
                    font.resumeDistanceFieldShader(batch);
                    located = true;
                    break;
                }
            }
            if (!located)
                font.pauseDistanceFieldShader(batch);
        }

        if(squashed) {
            sizingY *= 0.7f;
        }


        float batchAlpha = batch.getColor().a;
        float batchAlpha2 = batchAlpha * batchAlpha;
        float batchAlpha3 = batchAlpha2 * batchAlpha;
        float batchAlpha1_5 = (float)Math.sqrt(batchAlpha3);

        float color = NumberUtils.intBitsToFloat(
                  (int) ((((glyph & BOLD) != 0L) ? batchAlpha1_5 : batchAlpha) * (glyph >>> 33 & 127)) << 25
                | (int)(batch.getColor().r * (glyph >>> 56))
                | (int)(batch.getColor().g * (glyph >>> 48 & 0xFF)) << 8
                | (int)(batch.getColor().b * (glyph >>> 40 & 0xFF)) << 16);
        float secondaryColor;
        if((glyph & ALTERNATE_MODES_MASK) == HALO) {
            secondaryColor = color;
            color = PACKED_HALO_COLOR;
        }
        else if((glyph & ALTERNATE_MODES_MASK) == NEON) {
            secondaryColor = color;
            color = PACKED_WHITE;
        } else if((glyph & ALTERNATE_MODES_MASK) == BLUE_OUTLINE) {
            secondaryColor = PACKED_BLUE;
        } else if((glyph & ALTERNATE_MODES_MASK) == RED_OUTLINE) {
            secondaryColor = PACKED_RED;
        } else if((glyph & ALTERNATE_MODES_MASK) == YELLOW_OUTLINE) {
            secondaryColor = PACKED_YELLOW;
        } else if((glyph & ALTERNATE_MODES_MASK) == WHITE_OUTLINE) {
            secondaryColor = PACKED_WHITE;
        } else {
            secondaryColor = PACKED_BLACK;
        }
        float scale = 1f;
        float scaleX, fsx, osx;
        float scaleY, fsy, osy;
        if(c >= 0xE000 && c < 0xF800){
            fsx = font.cellHeight / tr.getMaxDimension() * font.inlineImageStretch;
            fsy = fsx;
            scaleX = scaleY = scale * fsx;
        }
        else
        {
            scaleX = (fsx = font.scaleX) * scale;
            scaleY = (fsy = font.scaleY) * scale;
//            y -= descent * scaleY;
        }
//        osx = font.scaleX * scale;
//        osy = font.scaleY;
        osx = fsx * (scale + 1f) * 0.5f;
        osy = fsy * (scale + 1f) * 0.5f;
        float centerX = tr.xAdvance * scaleX * 0.5f;
        float centerY = font.originalCellHeight * scaleY * 0.5f;

        float oCenterX = tr.xAdvance * osx * 0.5f;
        float oCenterY = font.originalCellHeight * osy * 0.5f;

        float scaleCorrection = font.descent * font.scaleY * 2f;// - font.descent * osy;
        y += scaleCorrection * scale;

        float ox = x, oy = y;

        float ix = font.handleIntegerPosition(x + centerX);
        float iy = font.handleIntegerPosition(y + centerY);
        // The shifts here represent how far the position was moved by handling the integer position, if that was done.
        float xShift = (x + centerX) - (ix);
        float yShift = (y + centerY) - (iy);
        // This moves the center to match the movement from integer position.
        x = font.handleIntegerPosition(ix - xShift);
        y = font.handleIntegerPosition(iy - yShift);
        centerX -= xShift * 0.5f;
        centerY -= yShift * 0.5f;

        // when offsetX is NaN, that indicates a box drawing character that we draw ourselves.
        if (Float.isNaN(tr.offsetX)) {
            if(backgroundColor != 0) {
                drawBlockSequence(batch, BlockUtils.BOX_DRAWING[0x88], font.mapping.get(solidBlock, tr),
                        NumberUtils.intToFloatColor(Integer.reverseBytes(backgroundColor)),
                        x,
                        y,// - font.descent * scaleY - font.cellHeight * scale * sizingY * 0.5f,
                        font.cellWidth * sizingX, font.cellHeight * scale * sizingY, rotation);
            }
            float[] boxes = BlockUtils.BOX_DRAWING[c - 0x2500];
            drawBlockSequence(batch, boxes, font.mapping.get(solidBlock, tr), color,
                    x, y,// - font.descent * scaleY - font.cellHeight * scale * sizingY * 0.5f,
                    font.cellWidth * sizingX, font.cellHeight * scale * sizingY, rotation,
                    c < 0x2580 ? boxDrawingBreadth : 1f);
            return font.cellWidth;
        }
        x += font.cellWidth * 0.5f;

        Texture tex = tr.getTexture();
        float scaledHeight = font.cellHeight * scale * sizingY;
        float x0 = 0f;
        float x1 = 0f;
        float x2 = 0f;
        float y0 = 0f;
        float y1 = 0f;
        float y2 = 0f;
        final float iw = 1f / tex.getWidth();
        float w = tr.getRegionWidth() * scaleX * sizingX;
        float xAdvance = tr.xAdvance;
        float changedW = xAdvance * scaleX * advanceMultiplier;

        //        float xc = ((tr.getRegionWidth() + tr.offsetX) * fsx - font.cellWidth) * scale * sizingX;
        //// This rotates around the center, but fails with box drawing. Underlines are also off, unless adjusted.
//        float xc = (font.cellWidth * 0.5f - (tr.getRegionWidth() + tr.offsetX) * fsx) * scale * sizingX;
        //// This works(*) with box-drawing chars, but rotates around halfway up the left edge, not the center.
        //// It does have the same sliding issue as the other methods so far.
//        float xc = (font.cellWidth * -0.5f) * sizingX;// + (tr.offsetX * scaleX * sizingX);
        float xc = (tr.offsetX * scaleX * sizingX) - cos * centerX - font.cellWidth * 0.5f;

        float trrh = tr.getRegionHeight();
        float yt = ((font.originalCellHeight - (trrh + tr.offsetY) + font.descent) * sizingY - font.descent) * scaleY + sin * centerX - centerY;

// The next line makes small-caps letters go part-way up in the line.
        if(squashed) yt -= font.descent * scaleY * sizingY * (3f/7f);

        float h = trrh * scaleY * sizingY;

        // These may need to be changed to use some other way of getting a screen pixel's size in world units.
        // They might actually be 1.5 or 2 pixels; it's hard to tell when a texture with alpha is drawn over an area.

        // This is used in 1.0.0, and it might be more correct... I'm not sure.
        float xPx = 2f / (Gdx.graphics.getBackBufferWidth()  * batch.getProjectionMatrix().val[0]);
        float yPx = 2f / (Gdx.graphics.getBackBufferHeight() * batch.getProjectionMatrix().val[5]);

        // This uses height to calculate both, so that the outline has even thickness.
        float xOutline = (outlineStrength * cellHeight / 32f);
        float yOutline = (outlineStrength * cellHeight / 32f);
//        float xOutline = (outlineStrength * xPx);
//        float yOutline = (outlineStrength * yPx);

        float u, v, u2, v2;
        u = tr.getU();
        v = tr.getV();
        u2 = tr.getU2();
        v2 = tr.getV2();

        if (c >= 0xE000 && c < 0xF800) {
            // for inline images, this does two things.
            // it moves the changes from the inline image's offsetX and offsetY from the
            // rotating xc and yt variables, to the position-only x and y variables.
            // it also offsets x by a half-cell to the right, and moves the origin for y.

            float stretchShift = (trrh * font.inlineImageStretch - trrh) * scaleX * sizingX * 0.5f;

            float xch = tr.offsetX * scaleX * sizingX;
            xc -= xch + stretchShift;
            x  += xch + stretchShift;

            float ych = tr.offsetY * scaleY * sizingY;
            yt = (sin * scaledHeight - scaledHeight) * 0.5f - ych - stretchShift;

// The next line makes small-caps letters go part-way up in the line.
            if(squashed) yt -= font.descent * font.scaleY * scale * sizingY * (3f/7f);
            y = oy + (scaledHeight * 0.5f) - ych;// + font.descent * font.scaleY * scale * sizingY;
        }

        // leaving this in commented because it can be useful to quickly get info on a particular char
//        if(c == 57863) // floppy disk
//            System.out.println("floppy disk: " + yt + ", font.cellHeight: " + font.cellHeight + ", trrh: " + trrh + ", tr.offsetY: " + tr.offsetY + ", fsy: "+ fsy + ", scale: " + scale + ", sizingY: " + sizingY + ", descent: " + font.descent);

        if ((glyph & OBLIQUE) != 0L) {
            final float amount = h * obliqueStrength * 0.2f;
            x0 += amount;
            x1 -= amount;
            x2 -= amount;
        }
        final long script = (glyph & SUPERSCRIPT);
        if (script == SUPERSCRIPT) {
            w *= 0.5f;
            h *= 0.5f;
            yt = yt * 0.625f;

            y1 += scaledHeight * 0.375f;
            y2 += scaledHeight * 0.375f;
            y0 += scaledHeight * 0.375f;
            if (!font.isMono)
                changedW *= 0.5f;
        } else if (script == SUBSCRIPT) {
            w *= 0.5f;
            h *= 0.5f;
            yt = yt * 0.625f;
            y1 -= scaledHeight * 0.375f;
            y2 -= scaledHeight * 0.375f;
            y0 -= scaledHeight * 0.375f;
            if (!font.isMono)
                changedW *= 0.5f;
        } else if (script == MIDSCRIPT) {
            w *= 0.5f;
            h *= 0.5f;
            yt = yt * 0.625f;
//            y0 += scaledHeight * 0.125f;
//            y1 += scaledHeight * 0.125f;
//            y2 += scaledHeight * 0.125f;
            if (!font.isMono)
                changedW *= 0.5f;
        }

        if(backgroundColor != 0) {
            drawBlockSequence(batch, BlockUtils.BOX_DRAWING[0x88], font.mapping.get(font.solidBlock, tr),
                    NumberUtils.intToFloatColor(Integer.reverseBytes(backgroundColor)),
                    x - font.cellWidth * scale * 0.5f,// - (xAdvance * scaleX * (sizingX - 0.5f) + tr.offsetX * scaleX) * 0.5f,
                    y + font.descent * scaleY * sizingY,// - (font.cellHeight * scale + font.descent * osy) * 0.5f * sizingY,
                    xAdvance * scaleX * sizingX + 5f, (font.cellHeight * scale) * sizingY, rotation);
        }
        if (jostled) {
            int code = NumberUtils.floatToIntBits(x * 1.8191725133961645f + y * 1.6710436067037893f + c * 1.5497004779019703f) & 0xFFFFFF;
            xc += code % 5 - 2f;
            yt += (code >>> 6) % 5 - 2f;
//            int code = (NumberUtils.floatToIntBits(x + y) >>> 16 ^ c);
//            drawBlockSequence(batch, BlockUtils.BOX_DRAWING[(code % 0x6D)],
//                    font.mapping.get(solidBlock, tr), color,
//                    x - xAdvance * scaleX * (sizingX - 1.0f) + atlasOffX - tr.offsetX * scaleX + (code & 7) - 4.5f,
//                    y + font.descent * scaleY * sizingY + atlasOffY + (code >>> 3 & 7) - 3.5f,
//                    xAdvance * scaleX * sizingX, (cellHeight * scale - font.descent * scaleY) * sizingY, rotation);
//            code ^= code * code | 1;
//            code ^= code >> 16;
//            drawBlockSequence(batch, BlockUtils.BOX_DRAWING[(code % 0x6D)],
//                    font.mapping.get(solidBlock, tr), color,
//                    x - xAdvance * scaleX * (sizingX - 1.0f) + atlasOffX - tr.offsetX * scaleX + (code & 3) - 3f,
//                    y + font.descent * scaleY * sizingY + atlasOffY + (code >>> 2 & 15) - 8f,
//                    xAdvance * scaleX * sizingX, (cellHeight * scale - font.descent * scaleY) * sizingY, rotation);
//            return changedW;
        }

        float p0x = xc + x0;
        float p0y = yt + y0 + h;
        float p1x = xc + x1;
        float p1y = yt + y1;
        float p2x = xc + x2 + w;
        float p2y = yt + y2;

        vertices[3] = u;
        vertices[4] = v;

        vertices[8] = u;
        vertices[9] = v2;

        vertices[13] = u2;
        vertices[14] = v2;

        vertices[18] = u2;
        vertices[19] = v;

        if((glyph & ALTERNATE_MODES_MASK) == DROP_SHADOW) {
//            float shadow = Color.toFloatBits(0.1333f, 0.1333f, 0.1333f, 0.5f);// (dark transparent gray, as batch alpha is lowered, this gets more transparent)
            float shadow = ColorUtils.multiplyAlpha(PACKED_SHADOW_COLOR, batchAlpha1_5);// (dark transparent gray, as batch alpha is lowered, this gets more transparent)
            vertices[2] = shadow;
            vertices[7] = shadow;
            vertices[12] = shadow;
            vertices[17] = shadow;

            vertices[15] = ((vertices[0] = (x + cos * p0x - sin * p0y + dropShadowOffset.x)) - (vertices[5] = (x + cos * p1x - sin * p1y + dropShadowOffset.x)) + (vertices[10] = (x + cos * p2x - sin * p2y + dropShadowOffset.x)));
            vertices[16] = ((vertices[1] = (y + sin * p0x + cos * p0y + dropShadowOffset.y)) - (vertices[6] = (y + sin * p1x + cos * p1y + dropShadowOffset.y)) + (vertices[11] = (y + sin * p2x + cos * p2y + dropShadowOffset.y)));

            drawVertices(batch, tex, vertices);
        }
        else if((glyph & BLACK_OUTLINE) == BLACK_OUTLINE && (((glyph & ALTERNATE_MODES_MASK) != HALO) && ((glyph & ALTERNATE_MODES_MASK) != NEON))) {
            int widthAdj = ((glyph & BOLD) != 0L) ? 2 : 1;
            float outline = ColorUtils.multiplyAlpha(secondaryColor,
                    widthAdj == 1 ? batchAlpha1_5 : batchAlpha2);
            vertices[2] = outline;
            vertices[7] = outline;
            vertices[12] = outline;
            vertices[17] = outline;
            for (int xi = -widthAdj; xi <= widthAdj; xi++) {
                float xa = xi * xOutline;
                if(widthAdj == 2 && (xi > 0 || boldStrength > 1f)) xa *= boldStrength;
                for (int yi = -1; yi <= 1; yi++) {
                    if(xi == 0 && yi == 0) continue;
                    float ya = yi * yOutline;
//                    vertices[15] = ((vertices[0] = (x + cos * p0x - sin * p0y + xa)) - (vertices[5] = (x + cos * p1x - sin * p1y + xa)) + (vertices[10] = (x + cos * p2x - sin * p2y + xa)));
//                    vertices[16] = ((vertices[1] = (y + sin * p0x + cos * p0y + ya)) - (vertices[6] = (y + sin * p1x + cos * p1y + ya)) + (vertices[11] = (y + sin * p2x + cos * p2y + ya)));
                    vertices[15] = (vertices[0] = font.handleIntegerPosition(x + cos * p0x - sin * p0y + xa)) - (vertices[5] = font.handleIntegerPosition(x + cos * p1x - sin * p1y + xa)) + (vertices[10] = font.handleIntegerPosition(x + cos * p2x - sin * p2y + xa));
                    vertices[16] = (vertices[1] = font.handleIntegerPosition(y + sin * p0x + cos * p0y + ya)) - (vertices[6] = font.handleIntegerPosition(y + sin * p1x + cos * p1y + ya)) + (vertices[11] = font.handleIntegerPosition(y + sin * p2x + cos * p2y + ya));

                    drawVertices(batch, tex, vertices);
                }
            }
        }
        else if(((glyph & ALTERNATE_MODES_MASK) == HALO) || ((glyph & ALTERNATE_MODES_MASK) == NEON)) {
            int widthAdj = ((glyph & BOLD) != 0L) ? 5 : 3;
            float outline = ColorUtils.multiplyAlpha(secondaryColor,
                    widthAdj == 3 ? batchAlpha * 0.2f * glowStrength : batchAlpha * 0.1f * glowStrength);
            vertices[2] = outline;
            vertices[7] = outline;
            vertices[12] = outline;
            vertices[17] = outline;
            for (int xi = -widthAdj; xi <= widthAdj; xi++) {
                float xa = xi * xOutline;
                if(widthAdj == 5 && (xi > 0 || boldStrength > 1f)) xa *= boldStrength;
                for (int yi = -3; yi <= 3; yi++) {
                    if((xi == 0 && yi == 0) || (Math.abs(yi) + Math.abs(xi) > widthAdj + 1)) continue;
                    float ya = yi * yOutline;
//                    vertices[15] = ((vertices[0] = (x + cos * p0x - sin * p0y + xa)) - (vertices[5] = (x + cos * p1x - sin * p1y + xa)) + (vertices[10] = (x + cos * p2x - sin * p2y + xa)));
//                    vertices[16] = ((vertices[1] = (y + sin * p0x + cos * p0y + ya)) - (vertices[6] = (y + sin * p1x + cos * p1y + ya)) + (vertices[11] = (y + sin * p2x + cos * p2y + ya)));
                    vertices[15] = (vertices[0] = font.handleIntegerPosition(x + cos * p0x - sin * p0y + xa)) - (vertices[5] = font.handleIntegerPosition(x + cos * p1x - sin * p1y + xa)) + (vertices[10] = font.handleIntegerPosition(x + cos * p2x - sin * p2y + xa));
                    vertices[16] = (vertices[1] = font.handleIntegerPosition(y + sin * p0x + cos * p0y + ya)) - (vertices[6] = font.handleIntegerPosition(y + sin * p1x + cos * p1y + ya)) + (vertices[11] = font.handleIntegerPosition(y + sin * p2x + cos * p2y + ya));

                    drawVertices(batch, tex, vertices);
                }
            }
        }
        else if((glyph & ALTERNATE_MODES_MASK) == SHINY) {
            int widthAdj = ((glyph & BOLD) != 0L) ? 1 : 0;
            float shine = ColorUtils.multiplyAlpha(PACKED_WHITE, widthAdj == 0 ? batchAlpha1_5 : batchAlpha2);
            vertices[2] = shine;
            vertices[7] = shine;
            vertices[12] = shine;
            vertices[17] = shine;
            for (int xi = -widthAdj; xi <= widthAdj; xi++) {
                float xa = xi * xOutline;
                if(widthAdj == 1 && (xi > 0 || boldStrength >= 1f)) xa *= boldStrength;
                float ya = 1.5f * yOutline;
                vertices[15] = ((vertices[0] = (x + cos * p0x - sin * p0y + xa)) - (vertices[5] = (x + cos * p1x - sin * p1y + xa)) + (vertices[10] = (x + cos * p2x - sin * p2y + xa)));
                vertices[16] = ((vertices[1] = (y + sin * p0x + cos * p0y + ya)) - (vertices[6] = (y + sin * p1x + cos * p1y + ya)) + (vertices[11] = (y + sin * p2x + cos * p2y + ya)));

                drawVertices(batch, tex, vertices);
            }
        }

        // actually draw the main glyph
        vertices[2] = color;
        vertices[7] = color;
        vertices[12] = color;
        vertices[17] = color;

//        vertices[15] = ((vertices[0] = (x + cos * p0x - sin * p0y)) - (vertices[5] = (x + cos * p1x - sin * p1y)) + (vertices[10] = (x + cos * p2x - sin * p2y)));
//        vertices[16] = ((vertices[1] = (y + sin * p0x + cos * p0y)) - (vertices[6] = (y + sin * p1x + cos * p1y)) + (vertices[11] = (y + sin * p2x + cos * p2y)));
        vertices[15] = (vertices[0] = font.handleIntegerPosition(x + cos * p0x - sin * p0y)) - (vertices[5] = font.handleIntegerPosition(x + cos * p1x - sin * p1y)) + (vertices[10] = font.handleIntegerPosition(x + cos * p2x - sin * p2y));
        vertices[16] = (vertices[1] = font.handleIntegerPosition(y + sin * p0x + cos * p0y)) - (vertices[6] = font.handleIntegerPosition(y + sin * p1x + cos * p1y)) + (vertices[11] = font.handleIntegerPosition(y + sin * p2x + cos * p2y));

        drawVertices(batch, tex, vertices);

        // This is the "emergency debug code" to get as much info as possible about a glyph when it prints.
//        if(c == '@' || (c >= 0xE000 && c < 0xF800)) {
//            System.out.println("With font " + font.name + ", drawing glyph " + namesByCharCode.get(c, "") +
//                    ", it has v0: " + vertices[0] + ", v1: " + vertices[1] +
//                    ", x: " + x + ", y: " + y + ", p0x: " + p0x + ", p0y: " + p0y +
//                    ", h: " + h + ", xc: " + xc + ", yt: " + yt +
//                    ", font.descent: " + font.descent + ", osy: " + osy +
//                    ", tr.offsetX: " + tr.offsetX + ", tr.offsetY: " + tr.offsetY + ", tr.xAdvance: " + tr.xAdvance +
//                    ", xShift: " + xShift + ", yShift: " + yShift + ", fsx: " + fsx + ", fsy: " + fsy +
//                    ", cellHeight: " + cellHeight + ", originalCellHeight: " + originalCellHeight + ", scaledHeight: " + scaledHeight
//            );
//        }

        if ((glyph & BOLD) != 0L) {
            final float old0 = p0x;
            final float old1 = p1x;
            final float old2 = p2x;
            float leftStrength = (this.boldStrength >= 1f) ? this.boldStrength : 0f;
            float rightStrength = (this.boldStrength >= 0f) ? 1f : 0f;
            if (rightStrength != 0f) {
                p0x = old0 + rightStrength;
                p1x = old1 + rightStrength;
                p2x = old2 + rightStrength;
                vertices[15] = ((vertices[0] = (x + cos * p0x - sin * p0y)) - (vertices[5] = (x + cos * p1x - sin * p1y)) + (vertices[10] = (x + cos * p2x - sin * p2y)));
                vertices[16] = ((vertices[1] = (y + sin * p0x + cos * p0y)) - (vertices[6] = (y + sin * p1x + cos * p1y)) + (vertices[11] = (y + sin * p2x + cos * p2y)));
                drawVertices(batch, tex, vertices);
                p0x = old0 + rightStrength * 0.5f;
                p1x = old1 + rightStrength * 0.5f;
                p2x = old2 + rightStrength * 0.5f;
                vertices[15] = ((vertices[0] = (x + cos * p0x - sin * p0y)) - (vertices[5] = (x + cos * p1x - sin * p1y)) + (vertices[10] = (x + cos * p2x - sin * p2y)));
                vertices[16] = ((vertices[1] = (y + sin * p0x + cos * p0y)) - (vertices[6] = (y + sin * p1x + cos * p1y)) + (vertices[11] = (y + sin * p2x + cos * p2y)));
                drawVertices(batch, tex, vertices);
            }
            if (leftStrength != 0f) {
                p0x = old0 - leftStrength;
                p1x = old1 - leftStrength;
                p2x = old2 - leftStrength;
                vertices[15] = ((vertices[0] = (x + cos * p0x - sin * p0y)) - (vertices[5] = (x + cos * p1x - sin * p1y)) + (vertices[10] = (x + cos * p2x - sin * p2y)));
                vertices[16] = ((vertices[1] = (y + sin * p0x + cos * p0y)) - (vertices[6] = (y + sin * p1x + cos * p1y)) + (vertices[11] = (y + sin * p2x + cos * p2y)));
                drawVertices(batch, tex, vertices);
                p0x = old0 - leftStrength * 0.5f;
                p1x = old1 - leftStrength * 0.5f;
                p2x = old2 - leftStrength * 0.5f;
                vertices[15] = ((vertices[0] = (x + cos * p0x - sin * p0y)) - (vertices[5] = (x + cos * p1x - sin * p1y)) + (vertices[10] = (x + cos * p2x - sin * p2y)));
                vertices[16] = ((vertices[1] = (y + sin * p0x + cos * p0y)) - (vertices[6] = (y + sin * p1x + cos * p1y)) + (vertices[11] = (y + sin * p2x + cos * p2y)));
                drawVertices(batch, tex, vertices);
            }
        }

        // this changes scaleCorrection from one that uses fsx to one that uses font.scaleY.
        // fsx and font.scaleY are equivalent for most glyphs, but not for inline images.

        oy = y + (scaleCorrection * sizingY + scaledHeight) * -0.5f + centerY * 0.25f;
        oy += scaleCorrection;// - font.descent * osy;
        if ((glyph & UNDERLINE) != 0L && (c < 0xE000 || c >= 0xF800)) {
            ix = font.handleIntegerPosition(ox + oCenterX);
            iy = font.handleIntegerPosition(oy + oCenterY);
            xShift = (ox + oCenterX) - (ix);
            yShift = (oy + oCenterY) - (iy);
            x = font.handleIntegerPosition(ix + xShift);
            y = font.handleIntegerPosition(iy + yShift);
            centerX = oCenterX + xShift * 0.5f;
            centerY = oCenterY + yShift * 0.5f;
            x += font.cellWidth * 0.5f;

            GlyphRegion under = font.mapping.get(0x2500);

            if (under != null && Float.isNaN(under.offsetX)) {
                p0x = (0.125f * font.cellWidth + xAdvance * font.underX * font.scaleX) * sizingX - 0.625f * font.cellWidth;
                p0y = 0.5f * centerY - font.cellHeight - (((font.underY - 0.5f) * font.cellHeight - font.descent * font.scaleY) * sizingY + font.descent * font.scaleY);
                p0x += centerX - cos * centerX + xPx * 4f;
                p0y += sin * centerX;
                drawBlockSequence(batch, BlockUtils.BOX_DRAWING[0], font.mapping.get(font.solidBlock, tr), color,
                        x + (cos * p0x - sin * p0y), y + (sin * p0x + cos * p0y),
                        (xAdvance * (font.underLength + 1.5f) * sizingX - xAdvance * (font.underLength + 0.25f)) * scaleX + xPx,
                        font.cellHeight * sizingY * (1f + font.underBreadth), rotation);
            } else {
                under = font.mapping.get('_');
                if (under != null) {
                    trrh = under.getRegionHeight();
                    h = trrh * osy * sizingY + cellHeight * font.underBreadth * sizingY;
                    yt = (centerY - (trrh + under.offsetY) * font.scaleY) * sizingY
                            + cellHeight * font.underY * sizingY;
                    if(squashed) yt -= font.descent * scaleY * sizingY * (3f/7f);
                    final float underU = (under.getU() + under.getU2()) * 0.5f - iw,
                            underV = under.getV(),
                            underU2 = underU + iw,
                            underV2 = under.getV2();
                    xc = -0.5f * font.cellWidth + changedW * font.underX - scale * fsx;
                    x0 = -2 * xPx;
                    float addW = xPx * 2;
                    x0 += xPx + centerX - cos * centerX;
                    yt += sin * centerX;
                    vertices[2] = color;
                    vertices[3] = underU;
                    vertices[4] = underV;

                    vertices[7] = color;
                    vertices[8] = underU;
                    vertices[9] = underV2;

                    vertices[12] = color;
                    vertices[13] = underU2;
                    vertices[14] = underV2;

                    vertices[17] = color;
                    vertices[18] = underU2;
                    vertices[19] = underV;

                    p0x = xc + x0 - addW;
                    p0y = yt + y0 + h;//yu + hu;
                    p1x = xc + x0 - addW;
                    p1y = yt + y1;//yu;
                    p2x = xc + x0 + changedW * (font.underLength + 1f) + addW;
                    p2y = yt + y2;//yu;
                    vertices[15] = (vertices[0] = x + cos * p0x - sin * p0y) - (vertices[5] = x + cos * p1x - sin * p1y) + (vertices[10] = x + cos * p2x - sin * p2y);
                    vertices[16] = (vertices[1] = y + sin * p0x + cos * p0y) - (vertices[6] = y + sin * p1x + cos * p1y) + (vertices[11] = y + sin * p2x + cos * p2y);

                    drawVertices(batch, under.getTexture(), vertices);
                }
            }
        }
        if ((glyph & STRIKETHROUGH) != 0L && (c < 0xE000 || c >= 0xF800)) {
            ix = font.handleIntegerPosition(ox + oCenterX);
            iy = font.handleIntegerPosition(oy + oCenterY);
            xShift = (ox + oCenterX) - (ix);
            yShift = (oy + oCenterY) - (iy);
            x = font.handleIntegerPosition(ix + xShift);
            y = font.handleIntegerPosition(iy + yShift);
            centerX = oCenterX + xShift * 0.5f;
            centerY = oCenterY + yShift * 0.5f;
            x += font.cellWidth * 0.5f;

            GlyphRegion dash = font.mapping.get(0x2500);
            if (dash != null && Float.isNaN(dash.offsetX)) {
                p0x = (0.125f * font.cellWidth + xAdvance * font.strikeX * font.scaleX) * sizingX - 0.625f * font.cellWidth;
                p0y = centerY * 0.65f - font.cellHeight - (((font.strikeY - 0.85f) * font.cellHeight - font.descent * font.scaleY) * sizingY + font.descent * font.scaleY);
                p0x += centerX - cos * centerX + xPx * 4f;
                p0y += sin * centerX;

                drawBlockSequence(batch, BlockUtils.BOX_DRAWING[0], font.mapping.get(font.solidBlock, tr), color,
                        x + cos * p0x - sin * p0y, y + (sin * p0x + cos * p0y),
                        (xAdvance * (font.strikeLength + 1.5f) * sizingX - xAdvance * (font.strikeLength + 0.25f)) * scaleX + xPx,
                        (1f + font.strikeBreadth) * font.cellHeight * sizingY, rotation);
            } else {
                dash = font.mapping.get('-');
                if (dash != null) {
                    trrh = dash.getRegionHeight();
                    h = trrh * osy * sizingY * (1f + font.strikeBreadth);

                    yt = (centerY - (trrh + dash.offsetY) * font.scaleY) * scale * sizingY
                            + font.cellHeight * font.strikeY * scale * sizingY;
                    if(squashed) yt -= font.descent * scaleY * sizingY * (3f/7f);

                    final float dashU = (dash.getU() + dash.getU2()) * 0.5f - iw,
                            dashV = dash.getV(),
                            dashU2 = dashU + iw,
                            dashV2 = dash.getV2();
                    xc = -cellWidth * 0.5f + changedW * font.strikeX - scale * fsx;
                    x0 = -2 * xPx;
                    float addW = xPx * 2;
                    x0 += xPx + centerX - cos * centerX;
                    yt += sin * centerX;

                    vertices[2] = color;
                    vertices[3] = dashU;
                    vertices[4] = dashV;

                    vertices[7] = color;
                    vertices[8] = dashU;
                    vertices[9] = dashV2;

                    vertices[12] = color;
                    vertices[13] = dashU2;
                    vertices[14] = dashV2;

                    vertices[17] = color;
                    vertices[18] = dashU2;
                    vertices[19] = dashV;

                    p0x = xc + x0 - addW;
                    p0y = yt + y0 + h;//yd + hd;
                    p1x = xc + x0 - addW;
                    p1y = yt + y1;//yd;
                    p2x = xc + x0 + changedW * (font.strikeLength + 1f) + addW;
                    p2y = yt + y2;//yd;
                    vertices[15] = (vertices[0] = x + cos * p0x - sin * p0y) - (vertices[5] = x + cos * p1x - sin * p1y) + (vertices[10] = x + cos * p2x - sin * p2y);
                    vertices[16] = (vertices[1] = y + sin * p0x + cos * p0y) - (vertices[6] = y + sin * p1x + cos * p1y) + (vertices[11] = y + sin * p2x + cos * p2y);
//                    vertices[15] = (vertices[0] = handleIntegerPosition(x + cos * p0x - sin * p0y)) - (vertices[5] = handleIntegerPosition(x + cos * p1x - sin * p1y)) + (vertices[10] = handleIntegerPosition(x + cos * p2x - sin * p2y));
//                    vertices[16] = (vertices[1] = handleIntegerPosition(y + sin * p0x + cos * p0y)) - (vertices[6] = handleIntegerPosition(y + sin * p1x + cos * p1y)) + (vertices[11] = handleIntegerPosition(y + sin * p2x + cos * p2y));

                    drawVertices(batch, dash.getTexture(), vertices);
                }
            }
        }
        // checks for fancy underline modes
        if((glyph & ALTERNATE_MODES_MASK) >= ERROR && (glyph & ALTERNATE_MODES_MASK) <= NOTE && (c < 0xE000 || c >= 0xF800)) {
            ix = font.handleIntegerPosition(ox + oCenterX);
            iy = font.handleIntegerPosition(oy + oCenterY);
            xShift = (ox + oCenterX) - (ix);
            yShift = (oy + oCenterY) - (iy);
            x = font.handleIntegerPosition(ix + xShift);
            y = font.handleIntegerPosition(iy + yShift);
            centerX = oCenterX + xShift * 0.5f;
            centerY = oCenterY + yShift * 0.5f;
//            x += cellWidth * 0.5f;
//            if (c >= 0xE000 && c < 0xF800) {
//                x += (changedW * 0.25f);
//                y -= scaledHeight * 0.5f;
//            }

            p0x = -cos * centerX + changedW * (font.fancyX);
            p0y = (font.descent * font.scaleY * 0.5f) * (scale * sizingY - font.fancyY) - centerY * 2f + centerY * advanceMultiplier + sin * centerX;

//            p0x = -cellWidth + xAdvance * font.underX * scaleX;
//            p0y = ((font.underY - 0.75f) * font.cellHeight) * scale * sizingY + centerY;
//            if (c >= 0xE000 && c < 0xF800)
//            {
//                p0x -= changedW * 0.25f - xPx * 2f;
//                p0y -= scaledHeight * 0.5f;
//            }
//            else
//            {
//                p0x += xPx + centerX - cos * centerX;
//                p0y += sin * centerX;
//            }

            drawFancyLine(batch, (glyph & ALTERNATE_MODES_MASK),
                    x + (cos * p0x - sin * p0y), y + (sin * p0x + cos * p0y),
                    changedW * (1f + underLength), xPx, yPx, rotation);

        }
        return changedW;
    }

    /**
     * Reads markup from text, along with the chars to receive markup, processes it, and appends into appendTo, which is
     * a {@link Layout} holding one or more {@link Line}s. This parses an extension of libGDX markup and uses it to
     * determine color, size, position, shape, strikethrough, underline, case, and scale of the given CharSequence.
     * It also reads typing markup, for effects, but passes it through without changing it and without considering it
     * for line wrapping or text position.
     * <br>
     * The text drawn will start in {@code appendTo}'s {@link Layout#baseColor}, which is usually white, with the normal
     * size as determined by the font's metrics and scale ({@link #scaleX} and {@link #scaleY}), normal case, and
     * without bold, italic, superscript, subscript, strikethrough, or underline. Markup starts with {@code [}; the next
     * character determines what that piece of markup toggles. Markup this knows:
     * <ul>
     *     <li>{@code []} undoes the most-recently-applied format change.</li>
     *     <li>{@code [ ]} clears all markup to the initial state without any applied.</li>
     *     <li>{@code [(label)]} temporarily stores the current formatting state as {@code label}.</li>
     *     <li>{@code [ label]} re-applies the formatting state stored as {@code label}, if there is one.</li>
     *     <li>{@code [[} escapes a literal left bracket, producing it without changing state.</li>
     *     <li>{@code [+name]}, where name is the name of a TextureRegion from an atlas added to this Font with
     *     {@link #addAtlas(TextureAtlas)}, produces the corresponding TextureRegion (scaled when drawn) without
     *     changing state. If no atlas has been added, this emits undefined character(s) instead.</li>
     *     <li>{@code [*]} toggles bold mode.</li>
     *     <li>{@code [/]} toggles italic (technically, oblique) mode.</li>
     *     <li>{@code [^]} toggles superscript mode (and turns off subscript or midscript mode).</li>
     *     <li>{@code [=]} toggles midscript mode (and turns off superscript or subscript mode).</li>
     *     <li>{@code [.]} toggles subscript mode (and turns off superscript or midscript mode).</li>
     *     <li>{@code [_]} toggles underline mode.</li>
     *     <li>{@code [~]} toggles strikethrough mode.</li>
     *     <li>{@code [!]} toggles all upper case mode.</li>
     *     <li>{@code [,]} toggles all lower case mode.</li>
     *     <li>{@code [;]} toggles capitalize each word mode.</li>
     *     <li>{@code [%P]}, where P is any decimal number, changes the scale to that percentage.
     *     {@code [%100]} is the initial value and sets to 100% scale, as in "not scaled at all."
     *     Note that this is not restricted to any increments; even {@code [%0.0123]} is technically valid.</li>
     *     <li>{@code [%]}, with no number just after it, resets scale to 100%.</li>
     *     <li>{@code [?MODE]}, where MODE can be (case-insensitive) one of "black outline", "white outline",
     *     "red outline", "blue outline", "yellow outline", "shiny", "drop shadow"/"shadow", "neon", "halo",
     *     "error", "warn", "note", "context", "suggest", "jostle", or "small caps",
     *     will enable that alternate mode. If MODE is empty or not recognized, this will disable any current active
     *     mode. If any mode is non-empty and recognized, it will be enabled and replace any current active mode.
     *     Disabling a mode should always use the empty string for MODE. The "black outline" mode isn't actually a mode,
     *     and internally enables the same feature as {@code [#]}; it is still recognized for compatibility. Note that
     *     this means it can be enabled at the same time as any other mode, and that it will not be disabled by
     *     {@code [?]}, though it can be toggled off or back on by {@code [#]} (which is the preferred way to use the
     *     outline). Other "outline" modes also enable the outline if it isn't active, but change its color. The outline
     *     can always be toggled on or off, regardless of color, by {@code [#]}.</li>
     *     <li>{@code [%?MODE]} is a synonym for the preceding {@code [?MODE]} markup for compatibility.</li>
     *     <li>{@code [%^MODE]} is a synonym for the preceding {@code [?MODE]} markup for compatibility.</li>
     *     <li>{@code [?]}, {@code [%?]} and {@code [%^]} are equivalent ways to disable a special mode, if it is active.</li>
     *     <li>{@code [@Name]}, where Name is a key in family, changes the current Font used for rendering to the Font
     *     in this.family by that name. This is ignored if family is null.</li>
     *     <li>{@code [@]}, with no text just after it, resets the font to this one (which should be item 0 in family,
     *     if family is non-null).</li>
     *     <li>{@code [#]} toggles a black outline around text. This can have its color changed by some special modes.</li>
     *     <li>{@code [#HHHHHHHH]}, where HHHHHHHH is a hex RGB888 or RGBA8888 int color, changes the color.</li>
     *     <li>{@code [#HHH]} is also a valid way to set the color using CSS-style 3 hex digits, each of which is
     *     effectively duplicated to make a 6-digit RGB888 code. {@code [#HHHH]} is a way to do this with alpha.</li>
     *     <li>{@code [COLORNAME]}, where "COLORNAME" is a color name or description that will be looked up in
     *     {@link #getColorLookup()}, changes the color. By default, this can receive ALL_CAPS names from {@link Colors}
     *     in libGDX, any names from {@link com.github.tommyettinger.textra.utils.Palette}, or mixes of one or
     *     more color names with adjectives like "dark". The name can optionally be preceded by {@code |}, which allows
     *     looking up colors with names that contain punctuation. This doesn't do much if using the default ColorLookup,
     *     {@link ColorLookup#DESCRIPTIVE}, because it only evaluates ASCII letters and numbers, and treats everything
     *     else as a separator.</li>
     * </ul>
     * You can render {@code appendTo} using {@link #drawGlyphs(Batch, Layout, float, float)}.
     *
     * @param text     text, typically with square-bracket markup
     * @param appendTo a Layout that stores one or more Line objects, carrying color, style, chars, and size
     * @return appendTo, for chaining
     */
    public Layout markup(String text, Layout appendTo) {
        boolean capitalize = false, previousWasLetter = false,
                capsLock = false, lowerCase = false, initial = true;
        int c, fontIndex = -1;
        float scale = 1f, rotation = 0f;
        Font font = this;
        float sclX;
        final long COLOR_MASK = 0xFFFFFFFF00000000L;
        long baseColor = Long.reverseBytes(NumberUtils.floatToIntBits(appendTo.getBaseColor())) & 0xFFFFFFFE00000000L;
        long color = baseColor;
        long current = color;
        if (appendTo.font == null || !appendTo.font.equals(this)) {
            appendTo.clear();
            appendTo.font(this);
        }
        appendTo.peekLine().height = 0;
        float targetWidth = appendTo.getTargetWidth();
        int kern = -1;
        historyBuffer.clear();
        labeledStates.clear();
        labeledStates.putAll(storedStates);

        for (int i = 0, n = text.length(); i < n; i++) {
            sclX = font.scaleX * scale;

            //// CURLY BRACKETS

            if (omitCurlyBraces && text.charAt(i) == '{' && i + 1 < n && text.charAt(i + 1) != '{') {
                int start = i;
                int sizeChange = -1, fontChange = -1, innerSquareStart = -1, innerSquareEnd = -1;
                int end = text.indexOf('}', i);
                if (end == -1) end = text.length();
                int eq = end;
                for (; i < n && i <= end; i++) {
                    c = text.charAt(i);
                    if (enableSquareBrackets && c == '[' && i + 1 < end && text.charAt(i + 1) == '+') innerSquareStart = i;
                    else if (innerSquareStart == -1) appendTo.add(current | c, scale, scale, 0f, 0f, rotation);
                    if (enableSquareBrackets && c == ']') {
                        innerSquareEnd = i;
                        if (innerSquareStart != -1 && font.nameLookup != null) {
                            int len = innerSquareEnd - innerSquareStart;
                            if (len >= 2) {
                                c = font.nameLookup.get(StringUtils.safeSubstring(text, innerSquareStart + 2, innerSquareEnd), '+');
                                innerSquareStart = -1;
                                appendTo.add(current | c, scale, scale, 0f, 0f, rotation);
                            }
                        }
                    }
                    // meaningful chars:
                    // []{}@%?^=.
                    if (c == '@') fontChange = i;
                    else if (c == '%') sizeChange = i;
                    else if (c == '?') sizeChange = -1;
                    else if (c == '^') sizeChange = -1;
                    else if (c == '=') eq = Math.min(eq, i);
                }
                char after = eq + 1 >= end ? '\u0000' : text.charAt(eq + 1);
                if (start + 1 == end || "RESET".equalsIgnoreCase(StringUtils.safeSubstring(text, start + 1, end))) {
                    historyBuffer.add(current);
                    scale = 1f;
                    font = this;
                    fontIndex = 0;
                    current &= ~SUPERSCRIPT;
                } else if (after == '^' || after == '=' || after == '.') {
                    switch (after) {
                        case '^':
                            if ((current & SUPERSCRIPT) == SUPERSCRIPT)
                                current &= ~SUPERSCRIPT;
                            else
                                current |= SUPERSCRIPT;
                            break;
                        case '.':
                            if ((current & SUPERSCRIPT) == SUBSCRIPT)
                                current &= ~SUBSCRIPT;
                            else
                                current = (current & ~SUPERSCRIPT) | SUBSCRIPT;
                            break;
                        case '=':
                            if ((current & SUPERSCRIPT) == MIDSCRIPT)
                                current &= ~MIDSCRIPT;
                            else
                                current = (current & ~SUPERSCRIPT) | MIDSCRIPT;
                            break;
                    }
                } else if (fontChange >= 0 && family != null) {
                    fontIndex = family.fontAliases.get(StringUtils.safeSubstring(text, fontChange + 1, end), -1);
                    if (fontIndex == -1) {
                        font = this;
                        fontIndex = 0;
                    } else {
                        font = family.connected[fontIndex];
                        if (font == null) {
                            font = this;
                            fontIndex = 0;
                        }
                    }
                } else if (sizeChange >= 0) {
                    if (sizeChange + 1 == end) {
                        if (eq + 1 == sizeChange) {
                            scale = 1f;
                        } else {
                            scale = StringUtils.floatFromDec(text, eq + 1, sizeChange) * 0.01f;
                        }
                    } else {
                        scale = StringUtils.floatFromDec(text, sizeChange + 1, end) * 0.01f;
                    }
                }
                long next = (current & 0xFFFFFFFFFFF0FFFFL) | (fontIndex & 15) << 16;
                if(current != next) historyBuffer.add(current);
                current = next;
                i--;
            } else if (enableSquareBrackets && text.charAt(i) == '[') {

                //// SQUARE BRACKET MARKUP
                c = '[';
                if (++i < n && (c = text.charAt(i)) != '[' && c != '+') {
                    if (c == ']') {
                        if(historyBuffer.isEmpty()) {
                            color = baseColor;
                            current = color & ~SUPERSCRIPT;
                            font = this;
                            capitalize = false;
                            capsLock = false;
                            lowerCase = false;
                        } else {
                            current = historyBuffer.pop();
                            if (family == null) {
                                font = this;
                                fontIndex = 0;
                            }
                            else {
                                fontIndex = (int) ((current & 0xF0000L) >>> 16);
                                font = family.connected[fontIndex & 15];
                                if (font == null) font = this;
                            }
                        }
                        continue;
                    }
                    int len = text.indexOf(']', i) - i;
                    if (len < 0) break;
                    if(!(len == 1 && c == ' '))
                        historyBuffer.add(current);
                    switch (c) {
                        case '*':
                            current ^= BOLD;
                            break;
                        case '/':
                            current ^= OBLIQUE;
                            break;
                        case '^':
                            if ((current & SUPERSCRIPT) == SUPERSCRIPT)
                                current &= ~SUPERSCRIPT;
                            else
                                current |= SUPERSCRIPT;
                            break;
                        case '.':
                            if ((current & SUPERSCRIPT) == SUBSCRIPT)
                                current &= ~SUBSCRIPT;
                            else
                                current = (current & ~SUPERSCRIPT) | SUBSCRIPT;
                            break;
                        case '=':
                            if ((current & SUPERSCRIPT) == MIDSCRIPT)
                                current &= ~MIDSCRIPT;
                            else
                                current = (current & ~SUPERSCRIPT) | MIDSCRIPT;
                            break;
                        case '_':
                            current ^= UNDERLINE;
                            break;
                        case '~':
                            current ^= STRIKETHROUGH;
                            break;
                        case ';':
                            capitalize = !capitalize;
                            capsLock = false;
                            lowerCase = false;
                            break;
                        case '!':
                            capsLock = !capsLock;
                            capitalize = false;
                            lowerCase = false;
                            break;
                        case ',':
                            lowerCase = !lowerCase;
                            capitalize = false;
                            capsLock = false;
                            break;
                        case '%':
                            if (len >= 2) {
                                // alternate mode, takes [%?mode name]
                                // "black outline", "white outline", "red outline", "blue outline", "yellow outline",
                                // "shiny", "drop shadow"/"shadow", "neon", "halo",
                                // "error", "warn", "note", "context", "suggest",
                                // "jostle", "small caps"
                                if (text.charAt(i + 1) == '?' || text.charAt(i + 1) == '^') {
                                    long modes = 0L;
                                    if(len >= 5) {
                                        char ch = Category.caseUp(text.charAt(i+2));
                                        if(ch == 'B') {
                                            modes |= BLACK_OUTLINE;
                                            if(Category.caseUp(text.charAt(i+4)) == 'U')
                                                modes |= BLUE_OUTLINE;
                                        } else if(ch == 'W') {
                                            if(Category.caseUp(text.charAt(i+3)) == 'H') {
                                                modes |= WHITE_OUTLINE | BLACK_OUTLINE;
                                            }
                                            else {
                                                modes |= WARN;
                                            }
                                        } else if(ch == 'S') {
                                            if(Category.caseUp(text.charAt(i+3)) == 'U') {
                                                modes |= SUGGEST;
                                            }
                                            else if(Category.caseUp(text.charAt(i+3)) == 'M') {
                                                modes |= SMALL_CAPS;
                                            }
                                            else if(Category.caseUp(text.charAt(i+4)) == 'I') {
                                                modes |= SHINY;
                                            }
                                            else if(Category.caseUp(text.charAt(i+3)) == 'H'){
                                                modes |= DROP_SHADOW;
                                            }
                                            // unrecognized falls back to small caps or jostle
                                        } else if(ch == 'C'){
                                            modes |= CONTEXT;
                                        } else if(ch == 'D'){
                                            modes |= DROP_SHADOW;
                                        } else if(ch == 'E'){
                                            modes |= ERROR;
                                        } else if(ch == 'H'){
                                            modes |= HALO;
                                        } else if(ch == 'J'){
                                            modes |= JOSTLE;
                                        } else if(ch == 'N'){
                                            if(Category.caseUp(text.charAt(i+3)) == 'O')
                                                modes |= NOTE;
                                            else if(Category.caseUp(text.charAt(i+3)) == 'E')
                                                modes |= NEON;
                                        } else if(ch == 'R') {
                                            modes |= RED_OUTLINE | BLACK_OUTLINE;
                                        } else if(ch == 'Y') {
                                            modes |= YELLOW_OUTLINE | BLACK_OUTLINE;
                                        }
                                    }
                                    // unrecognized falls back to no mode
                                    current = ((current & 0xFFFFFFFFFF0FFFFFL) ^ modes);
                                } else {
                                    scale = StringUtils.floatFromDec(text, i + 1, i + len) * 0.01f;
                                }
                            }
                            else {
//                                current = (current & 0xFFFFFFFFFE0FFFFFL);
                                scale = 1f;
                            }
                            break;
                        case '?':
                            if(len > 1) {
                            /*
                               alternate mode, takes [?mode name]
                               "black outline", "white outline", "red outline", "blue outline", "yellow outline",
                               "shiny", "drop shadow"/"shadow", "neon", "halo",
                               "error", "warn", "note", "context", "suggest",
                               "jostle", "small caps"
                                */
                                long modes = 0L;
                                if (len >= 4) {
                                    char ch = Category.caseUp(text.charAt(i + 1));
                                    if (ch == 'B') {
                                        modes |= BLACK_OUTLINE;
                                        if (Category.caseUp(text.charAt(i + 3)) == 'U')
                                            modes |= BLUE_OUTLINE;
                                    } else if (ch == 'W') {
                                        if (Category.caseUp(text.charAt(i + 2)) == 'H') {
                                            modes |= WHITE_OUTLINE | BLACK_OUTLINE;
                                        } else {
                                            modes |= WARN;
                                        }
                                    } else if (ch == 'S') {
                                        if (Category.caseUp(text.charAt(i + 2)) == 'U') {
                                            modes |= SUGGEST;
                                        } else if (Category.caseUp(text.charAt(i + 2)) == 'M') {
                                            modes |= SMALL_CAPS;
                                        } else if (Category.caseUp(text.charAt(i + 3)) == 'I') {
                                            modes |= SHINY;
                                        } else if (Category.caseUp(text.charAt(i + 2)) == 'H') {
                                            modes |= DROP_SHADOW;
                                        }
                                        // unrecognized falls back to small caps or jostle
                                    } else if (ch == 'C') {
                                        modes |= CONTEXT;
                                    } else if (ch == 'D') {
                                        modes |= DROP_SHADOW;
                                    } else if (ch == 'E') {
                                        modes |= ERROR;
                                    } else if (ch == 'H') {
                                        modes |= HALO;
                                    } else if (ch == 'J') {
                                        modes |= JOSTLE;
                                    } else if (ch == 'N') {
                                        if (Category.caseUp(text.charAt(i + 2)) == 'O')
                                            modes |= NOTE;
                                        else if (Category.caseUp(text.charAt(i + 2)) == 'E')
                                            modes |= NEON;
                                    } else if (ch == 'R') {
                                        modes |= RED_OUTLINE | BLACK_OUTLINE;
                                    } else if (ch == 'Y') {
                                        modes |= YELLOW_OUTLINE | BLACK_OUTLINE;
                                    }
                                }
                                // unrecognized falls back to no mode
                                current = ((current & 0xFFFFFFFFFF0FFFFFL) ^ modes);
                            } else {
                                current &= 0xFFFFFFFFFF0FFFFFL;
                            }
                            break;
                        case '#':
                            if (len >= 4 && len < 7) {
                                color = StringUtils.longFromHex(text, i + 1, i + 4);
                                color =
                                (color << 52 & 0xF000000000000000L) | (color << 48 & 0x0F00000000000000L) |
                                (color << 48 & 0x00F0000000000000L) | (color << 44 & 0x000F000000000000L) |
                                (color << 44 & 0x0000F00000000000L) | (color << 40 & 0x00000F0000000000L) |
                                0x000000FE00000000L;
                            }
                            else if (len >= 7 && len < 9)
                                color = StringUtils.longFromHex(text, i + 1, i + 7) << 40 | 0x000000FE00000000L;
                            else if (len >= 9)
                                color = StringUtils.longFromHex(text, i + 1, i + 9) << 32 & 0xFFFFFFFE00000000L;
                            else if(len == 1){
                                // [#] is used to toggle the outline.
                                current ^= BLACK_OUTLINE;
                            } else
                                // any other length, such as [#0] or [#12] is invalid and resets the color.
                                // this doesn't care what the other chars are, so [##] is a good way to reset the color.
                                color = baseColor;
                            current = (current & ~COLOR_MASK) | color;
                            break;
                        case '@':
                            if (family == null) {
                                font = this;
                                fontIndex = 0;
                                break;
                            }
                            fontIndex = family.fontAliases.get(StringUtils.safeSubstring(text, i + 1, i + len), 0);
                            current = (current & 0xFFFFFFFFFFF0FFFFL) | (fontIndex & 15L) << 16;
                            font = family.connected[fontIndex & 15];
                            if (font == null) font = this;
                            break;
                        case '(':
                            // record labeled state
                            // the left parenthesis "must" be matched by a right parenthesis at the end.
                            // (but really, the last char before the closing right square bracket is just ignored.)
                            if(len - 2 > 0)
                                labeledStates.put(StringUtils.safeSubstring(text, i + 1, i + len - 1), (current & 0xFFFFFFFFFFFF0000L));
                            break;
                        case '|':
                            // attempt to look up a known Color name with a ColorLookup
                            int lookupColor = colorLookup.getRgba(text, i + 1, i + len) & 0xFFFFFFFE;
                            if (lookupColor == 256) color = baseColor;
                            else color = (long) lookupColor << 32;
                            current = (current & ~COLOR_MASK) | color;
                            break;
                        case ' ':
                            capitalize = false;
                            capsLock = false;
                            lowerCase = false;
                            if(len > 1) {
                                // jump to labeled state
                                current = labeledStates.get(StringUtils.safeSubstring(text, i + 1, i + len), current);
                                if(family != null){
                                    font = family.connected[(int)(current >>> 16 & 15)];
                                    if(font == null) font = this;
                                }
                            } else {
                                // reset current state
                                color = baseColor;
                                current = color & ~SUPERSCRIPT;
                                scale = 1f;
                                font = this;
                            }
                            break;
                        default:
                            // attempt to look up a known Color name with a ColorLookup
                            int gdxColor = colorLookup.getRgba(text, i, i + len) & 0xFFFFFFFE;
                            if (gdxColor == 256) color = baseColor;
                            else color = (long) gdxColor << 32;
                            current = (current & ~COLOR_MASK) | color;
                    }
                    i += len;
                }

                //// ESCAPED SQUARE BRACKET AND TEXTURE REGION RENDERING

                else {
                    float w;
                    if(c == '+' && font.nameLookup != null) {
                        int len = text.indexOf(']', i) - i;
                        if (len >= 0) {
                            c = font.nameLookup.get(StringUtils.safeSubstring(text, i + 1, i + len), '+');
                            i += len;
                            sclX = scale * font.cellHeight / font.mapping.get(c, font.defaultValue).getMaxDimension() * font.inlineImageStretch;
                        }
                    }
                    if (font.kerning == null) {
                        w = (appendTo.peekLine().width += xAdvance(font, sclX, current | c));
                        if(initial && !isMono && !(c >= '\uE000' && c < '\uF800')){
                            float ox = font.mapping.get(c, font.defaultValue).offsetX;
                            if(Float.isNaN(ox)) ox = 0;
                            else ox *= sclX;
                            if(ox < 0) w = (appendTo.peekLine().width -= ox);
                        }
                        initial = false;
                    } else {
                        kern = kern << 16 | c;
                        w = (appendTo.peekLine().width += xAdvance(font, sclX, current | c) + font.kerning.get(kern, 0) * sclX * (1f + 0.5f * (-(current & SUPERSCRIPT) >> 63)));
                        if(initial && !isMono && !(c >= '\uE000' && c < '\uF800')){
                            float ox = font.mapping.get(c, font.defaultValue).offsetX;
                            if(Float.isNaN(ox)) ox = 0;
                            else ox *= sclX;
                            ox *= (1f + 0.5f * (-(current & SUPERSCRIPT) >> 63));
                            if (ox < 0) w = (appendTo.peekLine().width -= ox);
                        }
                        initial = false;
                    }
                    if(c == '[')
                        appendTo.add(current | 2, scale, scale, 0f, 0f, rotation);
                    else
                        appendTo.add(current | c, scale, scale, 0f, 0f, rotation);

                    if (targetWidth > 0 && w > targetWidth) {
                        Line earlier = appendTo.peekLine();
                        Line later = appendTo.pushLine();
                        if (later == null) {
                            if(handleEllipsis(appendTo)) {
//                                justify(appendTo);
                                return appendTo;
                            }
                        } else {
                            for (int j = earlier.glyphs.size - 2; j >= 0; j--) {
                                long curr;
                                if ((curr = earlier.glyphs.get(j)) >>> 32 == 0L ||
                                        Arrays.binarySearch(breakChars.items, 0, breakChars.size, (char) curr) >= 0) {
                                    int leading = 0;
                                    boolean hyphenated = true;
                                    while (j > 0 && ((curr = earlier.glyphs.get(j)) >>> 32 == 0L ||
                                            Arrays.binarySearch(spaceChars.items, 0, spaceChars.size, (char) curr) >= 0)) {
                                        ++leading;
                                        --j;
                                        hyphenated = false;
                                    }
                                    glyphBuffer.clear();
                                    float change = 0f, changeNext = 0f;
                                    if (font.kerning == null) {

                                        // NO KERNING

                                        boolean curly = false;
                                        for (int k = j + 1; k < earlier.glyphs.size; k++) {
                                            curr = earlier.glyphs.get(k);
                                            if(omitCurlyBraces) {
                                                if (curly) {
                                                    glyphBuffer.add(curr);
                                                    if ((char) curr == '{') {
                                                        curly = false;
                                                    } else if ((char) curr == '}') {
                                                        curly = false;
                                                        continue;
                                                    } else continue;
                                                }
                                            }
                                            if ((char) curr == '{') {
                                                glyphBuffer.add(curr);
                                                curly = omitCurlyBraces;
                                                continue;
                                            }

                                            float adv = xAdvance(font, sclX, curr);
                                            change += adv;
                                            if (--leading < 0) {
                                                glyphBuffer.add(curr);
                                                changeNext += adv;
                                                if(glyphBuffer.size == 1){
                                                    if(!isMono && !(c >= '\uE000' && c < '\uF800')) {
                                                        float ox = font.mapping.get((char) curr, font.defaultValue).offsetX;
                                                        if (Float.isNaN(ox)) ox = 0;
                                                        else ox *= sclX;
                                                        ox *= (1f + 0.5f * (-(current & SUPERSCRIPT) >> 63));
                                                        if (ox < 0) changeNext -= ox;
                                                    }
                                                    initial = false;
                                                }
                                            }
                                        }
                                    } else {

                                        // YES KERNING

                                        int k2 = (char) earlier.glyphs.get(j), k3 = -1;
                                        boolean curly = false;
                                        for (int k = j + 1; k < earlier.glyphs.size; k++) {
                                            curr = earlier.glyphs.get(k);
                                            if(omitCurlyBraces) {
                                                if (curly) {
                                                    glyphBuffer.add(curr);
                                                    if ((char) curr == '{') {
                                                        curly = false;
                                                    } else if ((char) curr == '}') {
                                                        curly = false;
                                                        continue;
                                                    } else continue;
                                                }
                                            }
                                            if ((char) curr == '{') {
                                                glyphBuffer.add(curr);
                                                curly = omitCurlyBraces;
                                                continue;
                                            }
                                            k2 = k2 << 16 | (char) curr;
                                            float adv = xAdvance(font, sclX, curr);
                                            change += adv + font.kerning.get(k2, 0) * sclX * (1f + 0.5f * (-(curr & SUPERSCRIPT) >> 63));
                                            if (--leading < 0) {
                                                k3 = k3 << 16 | (char) curr;
                                                changeNext += adv + font.kerning.get(k3, 0) * sclX * (1f + 0.5f * (-(curr & SUPERSCRIPT) >> 63));
                                                glyphBuffer.add(curr);
                                                if(glyphBuffer.size == 1){
                                                    if(!isMono && !(c >= '\uE000' && c < '\uF800')) {
                                                        float ox = font.mapping.get((char) curr, font.defaultValue).offsetX;
                                                        if (Float.isNaN(ox)) ox = 0;
                                                        else ox *= sclX;
                                                        ox *= (1f + 0.5f * (-(current & SUPERSCRIPT) >> 63));
                                                        if (ox < 0) changeNext -= ox;
                                                    }
                                                    initial = false;
                                                }
                                            }
                                        }
                                    }
                                    if (earlier.width - change > targetWidth)
                                        continue;
                                    earlier.glyphs.truncate(j + 1);
                                    if(!hyphenated)
                                        earlier.glyphs.add(applyChar(earlier.glyphs.isEmpty() ? 0L : earlier.glyphs.peek(), ' '));
                                    later.width = changeNext;
                                    earlier.width -= change;
                                    later.glyphs.addAll(glyphBuffer);
                                    later.height = Math.max(later.height, font.cellHeight * scale);
                                    break;
                                }
                            }
                            if(later.glyphs.isEmpty()){
                                appendTo.lines.pop();
                            }
                        }
                    } else {
                        appendTo.peekLine().height = Math.max(appendTo.peekLine().height, font.cellHeight * scale);
                    }
                }
            } else {

                //// VISIBLE CHAR RENDERING

                char ch = text.charAt(i), showCh;
                if (StringUtils.isLowerCase(ch)) {
                    if ((capitalize && !previousWasLetter) || capsLock) {
                        ch = Category.caseUp(ch);
                    }
                    previousWasLetter = true;
                } else if (StringUtils.isUpperCase(ch)) {
                    if ((capitalize && previousWasLetter) || lowerCase) {
                        ch = Category.caseDown(ch);
                    }
                    previousWasLetter = true;
                } else {
                    previousWasLetter = false;
                }
                showCh = (current & SMALL_CAPS) == SMALL_CAPS ? Category.caseUp(ch) : ch;
                if(ch >= 0xE000 && ch < 0xF800){
                    sclX = scale * font.cellHeight / font.mapping.get(ch, font.defaultValue).getMaxDimension() * font.inlineImageStretch;
                }
                float w;
                if (font.kerning == null) {
                    w = (appendTo.peekLine().width += xAdvance(font, sclX, current | showCh));
                } else {
                    kern = kern << 16 | showCh;
                    w = (appendTo.peekLine().width += xAdvance(font, sclX, current | showCh) + font.kerning.get(kern, 0) * sclX * (1f + 0.5f * (-((current | showCh) & SUPERSCRIPT) >> 63)));
                }
                if(initial && !isMono && !(showCh >= '\uE000' && showCh < '\uF800')) {
                    float ox = font.mapping.get(showCh, font.defaultValue).offsetX;
                    if (Float.isNaN(ox)) ox = 0;
                    else ox *= sclX;
                    ox *= (1f + 0.5f * (-(current & SUPERSCRIPT) >> 63));
                    if (ox < 0) w = (appendTo.peekLine().width -= ox);
                }
                initial = false;
                if (ch == '\n')
                {
                    appendTo.peekLine().height = Math.max(appendTo.peekLine().height, font.cellHeight * scale);
                    initial = true;
                }
                appendTo.add(current | ch, scale, scale, 0f, 0f, rotation);
                if ((targetWidth > 0 && w > targetWidth) || appendTo.atLimit) {
                    Line earlier = appendTo.peekLine();
                    Line later;
                    if (appendTo.lines.size >= appendTo.maxLines) {
                        later = null;
                    } else {
                        later = new Line();
                        later.height = 0;
                        appendTo.lines.add(later);
                        initial = true;
                    }
                    if (later == null) {
                        if(handleEllipsis(appendTo)) {
//                            justify(appendTo);
                            return appendTo;
                        }
                    } else {

                        //// WRAP VISIBLE

                        for (int j = earlier.glyphs.size - 2; j >= 0; j--) {
                            long curr;
                            if ((curr = earlier.glyphs.get(j)) >>> 32 == 0L ||
                                    Arrays.binarySearch(breakChars.items, 0, breakChars.size, (char) curr) >= 0) {
                                int leading = 0;
                                boolean hyphenated = true;
                                while (j > 0 && ((curr = earlier.glyphs.get(j)) >>> 32 == 0L ||
                                        Arrays.binarySearch(spaceChars.items, 0, spaceChars.size, (char) curr) >= 0)) {
                                    ++leading;
                                    --j;
                                    hyphenated = false;
                                }
                                glyphBuffer.clear();
                                float change = 0f, changeNext = 0f;
                                if (font.kerning == null) {

                                    // NO KERNING

                                    boolean curly = false;
                                    for (int k = j + 1; k < earlier.glyphs.size; k++) {
                                        curr = earlier.glyphs.get(k);
                                        showCh = (curr & SMALL_CAPS) == SMALL_CAPS ? Category.caseUp((char)curr) : (char)curr;
                                        if(omitCurlyBraces) {
                                            if (curly) {
                                                glyphBuffer.add(curr);
                                                if ((char) curr == '{') {
                                                    curly = false;
                                                } else if ((char) curr == '}') {
                                                    curly = false;
                                                    continue;
                                                } else continue;
                                            }
                                        }
                                        if (showCh == '{') {
                                            glyphBuffer.add(curr);
                                            curly = omitCurlyBraces;
                                            continue;
                                        }

                                        float adv = xAdvance(font, sclX, curr);
                                        change += adv;
                                        if (--leading < 0) {
                                            glyphBuffer.add(curr);
                                            changeNext += adv;
                                            if(glyphBuffer.size == 1){
                                                if(!isMono && !(showCh >= '\uE000' && showCh < '\uF800')) {
                                                    float ox = font.mapping.get(showCh, font.defaultValue).offsetX;
                                                    if (Float.isNaN(ox)) ox = 0;
                                                    else ox *= sclX * (1f + 0.5f * (-(current & SUPERSCRIPT) >> 63));
                                                    if (ox < 0) changeNext -= ox;
                                                }
                                                initial = false;
                                            }

                                        }
                                    }
                                } else {

                                    // YES KERNING

                                    int k2 = (char) earlier.glyphs.get(j);
                                    kern = -1;
                                    boolean curly = false;
                                    for (int k = j + 1; k < earlier.glyphs.size; k++) {
                                        curr = earlier.glyphs.get(k);
                                        showCh = (curr & SMALL_CAPS) == SMALL_CAPS ? Category.caseUp((char)curr) : (char)curr;
                                        if(omitCurlyBraces){
                                            if (curly) {
                                                glyphBuffer.add(curr);
                                                if ((char) curr == '{') {
                                                    curly = false;
                                                } else if ((char) curr == '}') {
                                                    curly = false;
                                                    continue;
                                                } else continue;
                                            }
                                        }
                                        if (showCh == '{') {
                                            glyphBuffer.add(curr);
                                            curly = omitCurlyBraces;
                                            continue;
                                        }
                                        k2 = k2 << 16 | showCh;
                                        float adv = xAdvance(font, sclX, curr);
                                        change += adv + font.kerning.get(k2, 0) * sclX * (isMono || (curr & SUPERSCRIPT) == 0L ? 1f : 0.5f);
                                        if (--leading < 0) {
                                            kern = kern << 16 | showCh;
                                            changeNext += adv + font.kerning.get(kern, 0) * sclX * (isMono || (curr & SUPERSCRIPT) == 0L ? 1f : 0.5f);
                                            glyphBuffer.add(curr);
                                            if(glyphBuffer.size == 1){
                                                if(!isMono && !(showCh >= '\uE000' && showCh < '\uF800')) {
                                                    float ox = font.mapping.get(showCh, font.defaultValue).offsetX;
                                                    if (Float.isNaN(ox)) ox = 0;
                                                    else ox *= sclX * (1f + 0.5f * (-(current & SUPERSCRIPT) >> 63));
                                                    if (ox < 0) changeNext -= ox;
                                                }
                                                initial = false;
                                            }
                                        }
                                    }
                                }
                                if (earlier.width - change > targetWidth)
                                    continue;
                                earlier.glyphs.truncate(j + 1);
                                if(!hyphenated)
                                    earlier.glyphs.add(applyChar(earlier.glyphs.isEmpty() ? 0L : earlier.glyphs.peek(), ' '));
                                later.width = changeNext;
                                earlier.width -= change;
                                later.glyphs.addAll(glyphBuffer);
                                later.height = Math.max(later.height, font.cellHeight * scale);
                                break;
                            }
                        }
                        if(later.glyphs.isEmpty()){
                            appendTo.lines.pop();
                        }
                    }
                } else {
                    appendTo.peekLine().height = Math.max(appendTo.peekLine().height, font.cellHeight * scale);
                }
            }
        }
//        justify(appendTo);
        return appendTo;
    }

    protected boolean handleEllipsis(Layout appendTo) {
        Font font = null;
        Line earlier = appendTo.peekLine();
        //// ELLIPSIS FOR VISIBLE

        // here, the max lines have been reached, and an ellipsis may need to be added
        // to the last line.
        String ellipsis = (appendTo.ellipsis == null) ? "" : appendTo.ellipsis;
        for (int j = earlier.glyphs.size - 2; j >= 0; j--) {
            long curr;
            if ((curr = earlier.glyphs.get(j)) >>> 32 == 0L ||
                    Arrays.binarySearch(breakChars.items, 0, breakChars.size, (char) curr) >= 0) {
                while (j > 0 && ((curr = earlier.glyphs.get(j)) >>> 32 == 0L ||
                        Arrays.binarySearch(spaceChars.items, 0, spaceChars.size, (char) curr) >= 0)) {
                    --j;
                }
                if (family != null) font = family.connected[(int) (curr >>> 16 & 15)];
                if (font == null) font = this;

                float change = 0f;
                if (font.kerning == null) {

                    // NO KERNING

                    boolean curly = false;
                    for (int k = j + 1; k < earlier.glyphs.size; k++) {
                        curr = earlier.glyphs.get(k);
                        if (family != null) font = family.connected[(int) (curr >>> 16 & 15)];
                        if (font == null) font = this;

                        if(omitCurlyBraces){
                            if (curly) {
                                if ((char) curr == '{') {
                                    curly = false;
                                } else if ((char) curr == '}') {
                                    curly = false;
                                    continue;
                                } else continue;
                            }
                        }
                        if ((char) curr == '{') {
                            curly = omitCurlyBraces;
                            continue;
                        }

                        float adv = xAdvance(font, scaleX, curr);
                        change += adv;
                    }
                    for (int e = 0; e < ellipsis.length(); e++) {
                        // 0xFFFFFFFF81FF0000L masks to include everything but style and char
                        curr = (curr & 0xFFFFFFFF81FF0000L) | ellipsis.charAt(e);
                        float adv = xAdvance(font, scaleX, curr);
                        change -= adv;
                    }
                } else {

                    // YES KERNING

                    int k2 = (char) earlier.glyphs.get(j);
                    boolean curly = false;
                    for (int k = j + 1; k < earlier.glyphs.size; k++) {
                        curr = earlier.glyphs.get(k);
                        if (family != null) font = family.connected[(int) (curr >>> 16 & 15)];
                        if (font == null) font = this;
                        if(omitCurlyBraces) {
                            if (curly) {
                                if ((char) curr == '{') {
                                    curly = false;
                                } else if ((char) curr == '}') {
                                    curly = false;
                                    continue;
                                } else continue;
                            }
                        }
                        if ((char) curr == '{') {
                            curly = omitCurlyBraces;
                            continue;
                        }
                        k2 = k2 << 16 | (char) curr;
                        float adv = xAdvance(font, scaleX, curr);
                        change += adv + font.kerning.get(k2, 0) * scaleX * (isMono || (curr & SUPERSCRIPT) == 0L ? 1f : 0.5f);
                    }
                    for (int e = 0; e < ellipsis.length(); e++) {
                        // 0xFFFFFFFF81FF0000L masks to include everything but style and char
                        curr = (curr & 0xFFFFFFFF81FF0000L) | ellipsis.charAt(e);
                        k2 = k2 << 16 | (char) curr;
                        float adv = xAdvance(font, scaleX, curr);
                        change -= adv + font.kerning.get(k2, 0) * scaleX * (isMono || (curr & SUPERSCRIPT) == 0L ? 1f : 0.5f);
                    }
                }
                if (earlier.width - change > appendTo.targetWidth)
                    continue;
                earlier.glyphs.truncate(j + 1);
                for (int e = 0; e < ellipsis.length(); e++) {
                    // 0xFFFFFFFF81FF0000L masks to include everything but style and char
                    earlier.glyphs.add((curr & 0xFFFFFFFF81FF0000L) | ellipsis.charAt(e));
                }
                earlier.width -= change;
                return true;
            }
        }
        return false;
    }

    /**
     * Reads markup from {@code markup}, processes it, and applies it to the given char {@code chr}; returns a long
     * in the format used for styled glyphs here. This parses an extension of libGDX markup and uses it to determine
     * color, size, position, shape, strikethrough, underline, case, and scale of the given char.
     * The char drawn will start in white, with the normal size as determined by the font's metrics and scale
     * ({@link #scaleX} and {@link #scaleY}), normal case, and without bold, italic, superscript, subscript,
     * strikethrough, or underline. Markup starts with {@code [}; the next character determines what that piece of
     * markup toggles. Markup this knows:
     * <ul>
     *     <li>{@code []} undoes the most-recently-applied format change.</li>
     *     <li>{@code [ ]} clears all markup to the initial state without any applied.</li>
     *     <li>{@code [*]} toggles bold mode.</li>
     *     <li>{@code [/]} toggles italic (technically, oblique) mode.</li>
     *     <li>{@code [^]} toggles superscript mode (and turns off subscript or midscript mode).</li>
     *     <li>{@code [=]} toggles midscript mode (and turns off superscript or subscript mode).</li>
     *     <li>{@code [.]} toggles subscript mode (and turns off superscript or midscript mode).</li>
     *     <li>{@code [_]} toggles underline mode.</li>
     *     <li>{@code [~]} toggles strikethrough mode.</li>
     *     <li>{@code [!]} toggles all upper case mode.</li>
     *     <li>{@code [,]} toggles all lower case mode.</li>
     *     <li>{@code [;]} toggles capitalize each word mode (this is the same as upper case mode here).</li>
     *     <li>{@code [%P]}, where P is any decimal number, changes the scale to that percentage.
     *     {@code [%100]} is the initial value and sets to 100% scale, as in "not scaled at all."
     *     Note that this is not restricted to any increments; even {@code [%0.0123]} is technically valid.</li>
     *     <li>{@code [%]}, with no number just after it, resets scale to 100%.</li>
     *     <li>{@code [?MODE]}, where MODE can be (case-insensitive) one of "black outline", "white outline",
     *     "red outline", "blue outline", "yellow outline", "shiny", "drop shadow"/"shadow", "neon", "halo",
     *     "error", "warn", "note", "context", "suggest", "jostle", or "small caps",
     *     will enable that alternate mode. If MODE is empty or not recognized, this will disable any current active
     *     mode. If any mode is non-empty and recognized, it will be enabled and replace any current active mode.
     *     Disabling a mode should always use the empty string for MODE. The "black outline" mode isn't actually a mode,
     *     and internally enables the same feature as {@code [#]}; it is still recognized for compatibility. Note that
     *     this means it can be enabled at the same time as any other mode, and that it will not be disabled by
     *     {@code [?]}, though it can be toggled off or back on by {@code [#]} (which is the preferred way to use the
     *     outline). Other "outline" modes also enable the outline if it isn't active, but change its color. The outline
     *     can always be toggled on or off, regardless of color, by {@code [#]}.</li>
     *     <li>{@code [%?MODE]} is a synonym for the preceding {@code [?MODE]} markup for compatibility.</li>
     *     <li>{@code [%^MODE]} is a synonym for the preceding {@code [?MODE]} markup for compatibility.</li>
     *     <li>{@code [?]}, {@code [%?]} and {@code [%^]} are equivalent ways to disable a special mode, if it is active.</li>
     *     <li>{@code [@Name]}, where Name is a key in family, changes the current Font used for rendering to the Font
     *     in this.family by that name. This is ignored if family is null.</li>
     *     <li>{@code [@]}, with no text just after it, resets the font to this one (which should be item 0 in family,
     *     if family is non-null).</li>
     *     <li>{@code [#]} toggles a black outline around text. This can have its color changed by some special modes.</li>
     *     <li>{@code [#HHHHHHHH]}, where HHHHHHHH is a hex RGB888 or RGBA8888 int color, changes the color.</li>
     *     <li>{@code [#HHH]} is also a valid way to set the color using CSS-style 3 hex digits, each of which is
     *     effectively duplicated to make a 6-digit RGB888 code. {@code [#HHHH]} is a way to do this with alpha.</li>
     *     <li>{@code [COLORNAME]}, where "COLORNAME" is a color name or description that will be looked up in
     *     {@link #getColorLookup()}, changes the color. By default, this can receive ALL_CAPS names from {@link Colors}
     *     in libGDX, any names from {@link com.github.tommyettinger.textra.utils.Palette}, or mixes of one or
     *     more color names with adjectives like "dark". The name can optionally be preceded by {@code |}, which allows
     *     looking up colors with names that contain punctuation. This doesn't do much if using the default ColorLookup,
     *     {@link ColorLookup#DESCRIPTIVE}, because it only evaluates ASCII letters and numbers, and treats everything
     *     else as a separator.</li>
     * </ul>
     * This does not automatically understand the {@code [+🔬]} syntax; you can use {@link #atlasLookup(String)} to get
     * the internal character code that refers to an atlas glyph such as an emoji, or you can just use
     * {@link #markupGlyph(String)} with that plus-sign syntax to enter the character.
     * <br>
     * You can render the result using {@link #drawGlyph(Batch, long, float, float)}. It is recommended that you avoid
     * calling this method every frame, because the color lookups usually allocate some memory, and because this can
     * usually be stored for later without needing repeated computation.
     * <br>
     * This is equivalent to calling {@link #markupGlyph(String)} using a placeholder character (or none) and
     * changing the returned glyph to use {@code chr} using {@link #applyChar(long, char)}.
     *
     * @param chr    a single char to apply markup to; may also be a character code from {@link #atlasLookup(String)}
     * @param markup a String containing only markup syntax, like "[*][_][RED]" for bold underline in red
     * @return a long that encodes the given char with the specified markup
     */
    public long markupGlyph(int chr, String markup) {
        return (markupGlyph(markup + (char)chr) & 0xFFFFFFFFFFFF0000L) | (char)chr;
    }

    /**
     * Reads markup from {@code markup} and processes it until it has a single complete glyph; returns that glyph as a
     * long in the format used for styled glyphs here. This parses an extension of libGDX markup and uses it to
     * determine color, size, position, shape, strikethrough, underline, case, and scale of the given char.
     * This overload works even if the glyph is from an atlas (see {@link #addAtlas(TextureAtlas)}, as long as the atlas
     * was added to this Font. As such, this can be useful to get an emoji or similar character with markup, using the
     * {@code [+👸🏽]} syntax to produce the one char.
     * <br>
     * The char drawn will start in white, with the normal size as determined by the font's metrics and scale
     * ({@link #scaleX} and {@link #scaleY}), normal case, and without bold, italic, superscript, subscript,
     * strikethrough, or underline. Markup starts with {@code [}; the next character determines what that piece of
     * markup toggles. Markup this knows:
     * <ul>
     *     <li>{@code []} undoes the most-recently-applied format change.</li>
     *     <li>{@code [ ]} clears all markup to the initial state without any applied.</li>
     *     <li>{@code [*]} toggles bold mode.</li>
     *     <li>{@code [/]} toggles italic (technically, oblique) mode.</li>
     *     <li>{@code [^]} toggles superscript mode (and turns off subscript or midscript mode).</li>
     *     <li>{@code [=]} toggles midscript mode (and turns off superscript or subscript mode).</li>
     *     <li>{@code [.]} toggles subscript mode (and turns off superscript or midscript mode).</li>
     *     <li>{@code [_]} toggles underline mode.</li>
     *     <li>{@code [~]} toggles strikethrough mode.</li>
     *     <li>{@code [!]} toggles all upper case mode.</li>
     *     <li>{@code [,]} toggles all lower case mode.</li>
     *     <li>{@code [;]} toggles capitalize each word mode (this is the same as upper case mode here).</li>
     *     <li>{@code [%P]}, where P is any decimal number, changes the scale to that percentage.
     *     {@code [%100]} is the initial value and sets to 100% scale, as in "not scaled at all."
     *     Note that this is not restricted to any increments; even {@code [%0.0123]} is technically valid.</li>
     *     <li>{@code [%]}, with no number just after it, resets scale to 100%.</li>
     *     <li>{@code [?MODE]}, where MODE can be (case-insensitive) one of "black outline", "white outline",
     *     "red outline", "blue outline", "yellow outline", "shiny", "drop shadow"/"shadow", "neon", "halo",
     *     "error", "warn", "note", "context", "suggest", "jostle", or "small caps",
     *     will enable that alternate mode. If MODE is empty or not recognized, this will disable any current active
     *     mode. If any mode is non-empty and recognized, it will be enabled and replace any current active mode.
     *     Disabling a mode should always use the empty string for MODE. The "black outline" mode isn't actually a mode,
     *     and internally enables the same feature as {@code [#]}; it is still recognized for compatibility. Note that
     *     this means it can be enabled at the same time as any other mode, and that it will not be disabled by
     *     {@code [?]}, though it can be toggled off or back on by {@code [#]} (which is the preferred way to use the
     *     outline). Other "outline" modes also enable the outline if it isn't active, but change its color. The outline
     *     can always be toggled on or off, regardless of color, by {@code [#]}.</li>
     *     <li>{@code [%?MODE]} is a synonym for the preceding {@code [?MODE]} markup for compatibility.</li>
     *     <li>{@code [%^MODE]} is a synonym for the preceding {@code [?MODE]} markup for compatibility.</li>
     *     <li>{@code [?]}, {@code [%?]} and {@code [%^]} are equivalent ways to disable a special mode, if it is active.</li>
     *     <li>{@code [@Name]}, where Name is a key in family, changes the current Font used for rendering to the Font
     *     in this.family by that name. This is ignored if family is null.</li>
     *     <li>{@code [@]}, with no text just after it, resets the font to this one (which should be item 0 in family,
     *     if family is non-null).</li>
     *     <li>{@code [#]} toggles a black outline around text. This can have its color changed by some special modes.</li>
     *     <li>{@code [#HHHHHHHH]}, where HHHHHHHH is a hex RGB888 or RGBA8888 int color, changes the color.</li>
     *     <li>{@code [#HHH]} is also a valid way to set the color using CSS-style 3 hex digits, each of which is
     *     effectively duplicated to make a 6-digit RGB888 code. {@code [#HHHH]} is a way to do this with alpha.</li>
     *     <li>{@code [COLORNAME]}, where "COLORNAME" is a color name or description that will be looked up in
     *     {@link #getColorLookup()}, changes the color. By default, this can receive ALL_CAPS names from {@link Colors}
     *     in libGDX, any names from {@link com.github.tommyettinger.textra.utils.Palette}, or mixes of one or
     *     more color names with adjectives like "dark". The name can optionally be preceded by {@code |}, which allows
     *     looking up colors with names that contain punctuation. This doesn't do much if using the default ColorLookup,
     *     {@link ColorLookup#DESCRIPTIVE}, because it only evaluates ASCII letters and numbers, and treats everything
     *     else as a separator.</li>
     * </ul>
     * You can render the result using {@link #drawGlyph(Batch, long, float, float)}. It is recommended that you avoid
     * calling this method every frame, because the color lookups usually allocate some memory, and because this can
     * usually be stored for later without needing repeated computation.
     * <br>
     * This is not static; it can depend on the current Font's FontFamily, ColorLookup, and any atlases added to it.
     *
     * @param markup      a String containing markup syntax and one char, like "[*][RED]G" for a bold, red 'G'
     * @return a long that encodes the given char with the specified markup
     */
    public long markupGlyph(String markup) {
        boolean capitalize = false,
                capsLock = false, lowerCase = false;
        int c, scale = 3, fontIndex = -1;
        final long COLOR_MASK = 0xFFFFFFFF00000000L;
        long baseColor = 0xFFFFFFFE00000000L;
        long color = baseColor;
        long current = color;
        Font font = this;
        for (int i = 0, n = markup.length(); i <= n; i++) {
            if(i == n) return current | ' ';
            //// CURLY BRACKETS
            if (omitCurlyBraces && markup.charAt(i) == '{' && i + 1 < n && markup.charAt(i + 1) != '{') {
                int start = i;
                int sizeChange = -1, fontChange = -1, innerSquareStart = -1, innerSquareEnd = -1;
                int end = markup.indexOf('}', i);
                if (end == -1) end = markup.length();
                int eq = end;
                for (; i < n && i <= end; i++) {
                    c = markup.charAt(i);

                    if (enableSquareBrackets && c == '[' && i + 1 < end && markup.charAt(i+1) == '+') innerSquareStart = i;
                    else if(innerSquareStart == -1) current = (current | c);
                    if (enableSquareBrackets && c == ']') {
                        innerSquareEnd = i;
                        if(innerSquareStart != -1 && font.nameLookup != null) {
                            int len = innerSquareEnd - innerSquareStart;
                            if (len >= 2) {
                                c = font.nameLookup.get(StringUtils.safeSubstring(markup, innerSquareStart + 2, innerSquareEnd), '+');
                                innerSquareStart = -1;
                                current = (current | c);
                            }
                        }
                    }

                    if (c == '@') fontChange = i;
                    else if (c == '%') sizeChange = i;
                    else if (c == '?') sizeChange = -1;
                    else if (c == '^') sizeChange = -1;
                    else if (c == '=') eq = Math.min(eq, i);
                }
                char after = eq + 1 >= end ? '\u0000' : markup.charAt(eq + 1);
                if (start + 1 == end || "RESET".equalsIgnoreCase(StringUtils.safeSubstring(markup, start + 1, end))) {
                    scale = 3;
                    fontIndex = 0;
                    current &= ~SUPERSCRIPT;
                } else if (after == '^' || after == '=' || after == '.') {
                    switch (after) {
                        case '^':
                            if ((current & SUPERSCRIPT) == SUPERSCRIPT)
                                current &= ~SUPERSCRIPT;
                            else
                                current |= SUPERSCRIPT;
                            break;
                        case '.':
                            if ((current & SUPERSCRIPT) == SUBSCRIPT)
                                current &= ~SUBSCRIPT;
                            else
                                current = (current & ~SUPERSCRIPT) | SUBSCRIPT;
                            break;
                        case '=':
                            if ((current & SUPERSCRIPT) == MIDSCRIPT)
                                current &= ~MIDSCRIPT;
                            else
                                current = (current & ~SUPERSCRIPT) | MIDSCRIPT;
                            break;
                    }
                } else if (fontChange >= 0 && family != null) {
                    fontIndex = family.fontAliases.get(StringUtils.safeSubstring(markup, fontChange + 1, end), -1);
                    if (fontIndex == -1) {
                        fontIndex = 0;
                    } else {
                        if (family.connected[fontIndex] == null) {
                            fontIndex = 0;
                        } else {
                            font = family.connected[fontIndex];
                        }
                    }
                } else if (sizeChange >= 0) {
                    if (sizeChange + 1 == end) {
                        if (eq + 1 == sizeChange) {
                            scale = 3;
                        } else {
                            scale = ((StringUtils.intFromDec(markup, eq + 1, sizeChange) - 24) / 25) & 15;
                        }
                    } else {
                        scale = ((StringUtils.intFromDec(markup, sizeChange + 1, end) - 24) / 25) & 15;
                    }
                }
                current = (current & 0xFFFFFFFFFF00FFFFL) | (scale - 3 & 15) << 20 | (fontIndex & 15) << 16;
                i--;
            } else if (enableSquareBrackets && markup.charAt(i) == '[') {

                //// SQUARE BRACKET MARKUP
                c = '[';
                if (++i < n && (c = markup.charAt(i)) != '[' && c != '+') {
                    if (c == ']') {
                        color = baseColor;
                        current = color & ~SUPERSCRIPT;
                        scale = 3;
                        capitalize = false;
                        capsLock = false;
                        lowerCase = false;
                        continue;
                    }
                    int len = markup.indexOf(']', i) - i;
                    if (len < 0) break;
                    switch (c) {
                        case '*':
                            current ^= BOLD;
                            break;
                        case '/':
                            current ^= OBLIQUE;
                            break;
                        case '^':
                            if ((current & SUPERSCRIPT) == SUPERSCRIPT)
                                current &= ~SUPERSCRIPT;
                            else
                                current |= SUPERSCRIPT;
                            break;
                        case '.':
                            if ((current & SUPERSCRIPT) == SUBSCRIPT)
                                current &= ~SUBSCRIPT;
                            else
                                current = (current & ~SUPERSCRIPT) | SUBSCRIPT;
                            break;
                        case '=':
                            if ((current & SUPERSCRIPT) == MIDSCRIPT)
                                current &= ~MIDSCRIPT;
                            else
                                current = (current & ~SUPERSCRIPT) | MIDSCRIPT;
                            break;
                        case '_':
                            current ^= UNDERLINE;
                            break;
                        case '~':
                            current ^= STRIKETHROUGH;
                            break;
                        case ';':
                            capitalize = !capitalize;
                            capsLock = false;
                            lowerCase = false;
                            break;
                        case '!':
                            capsLock = !capsLock;
                            capitalize = false;
                            lowerCase = false;
                            break;
                        case ',':
                            lowerCase = !lowerCase;
                            capitalize = false;
                            capsLock = false;
                            break;
                        case '%':
                            if (len >= 2) {
                                // alternate mode, takes [%?mode name]
                                // "black outline", "white outline", "red outline", "blue outline", "yellow outline",
                                // "shiny", "drop shadow"/"shadow", "neon", "halo",
                                // "error", "warn", "note", "context", "suggest",
                                // "jostle", "small caps"
                                if (markup.charAt(i + 1) == '?' || markup.charAt(i + 1) == '^') {
                                    long modes = 0L;
                                    if(len >= 5) {
                                        char ch = Category.caseUp(markup.charAt(i+2));
                                        if(ch == 'B') {
                                            modes |= BLACK_OUTLINE;
                                            if(Category.caseUp(markup.charAt(i+4)) == 'U')
                                                modes |= BLUE_OUTLINE;
                                        } else if(ch == 'W') {
                                            if(Category.caseUp(markup.charAt(i+3)) == 'H') {
                                                modes |= WHITE_OUTLINE | BLACK_OUTLINE;
                                            }
                                            else {
                                                modes |= WARN;
                                            }
                                        } else if(ch == 'S') {
                                            if(Category.caseUp(markup.charAt(i+3)) == 'U') {
                                                modes |= SUGGEST;
                                            }
                                            else if(Category.caseUp(markup.charAt(i+3)) == 'M') {
                                                modes |= SMALL_CAPS;
                                            }
                                            else if(Category.caseUp(markup.charAt(i+4)) == 'I') {
                                                modes |= SHINY;
                                            }
                                            else if(Category.caseUp(markup.charAt(i+3)) == 'H'){
                                                modes |= DROP_SHADOW;
                                            }
                                            // unrecognized falls back to small caps or jostle
                                        } else if(ch == 'C'){
                                            modes |= CONTEXT;
                                        } else if(ch == 'D'){
                                            modes |= DROP_SHADOW;
                                        } else if(ch == 'E'){
                                            modes |= ERROR;
                                        } else if(ch == 'H'){
                                            modes |= HALO;
                                        } else if(ch == 'J'){
                                            modes |= JOSTLE;
                                        } else if(ch == 'N'){
                                            if(Category.caseUp(markup.charAt(i+3)) == 'O')
                                                modes |= NOTE;
                                            else if(Category.caseUp(markup.charAt(i+3)) == 'E')
                                                modes |= NEON;
                                        } else if(ch == 'R') {
                                            modes |= RED_OUTLINE | BLACK_OUTLINE;
                                        } else if(ch == 'Y') {
                                            modes |= YELLOW_OUTLINE | BLACK_OUTLINE;
                                        }
                                    }
                                    // unrecognized falls back to no mode
                                    current = ((current & 0xFFFFFFFFFF0FFFFFL) ^ modes);
                                }
                            }
                            break;
                        case '?':
                            if(len > 1) {
                            /*
                               alternate mode, takes [?mode name]
                               "black outline", "white outline", "red outline", "blue outline", "yellow outline",
                               "shiny", "drop shadow"/"shadow", "neon", "halo",
                               "error", "warn", "note", "context", "suggest",
                               "jostle", "small caps"
                                */
                                long modes = 0L;
                                if (len >= 4) {
                                    char ch = Category.caseUp(markup.charAt(i + 1));
                                    if (ch == 'B') {
                                        modes |= BLACK_OUTLINE;
                                        if (Category.caseUp(markup.charAt(i + 3)) == 'U')
                                            modes |= BLUE_OUTLINE;
                                    } else if (ch == 'W') {
                                        if (Category.caseUp(markup.charAt(i + 2)) == 'H') {
                                            modes |= WHITE_OUTLINE | BLACK_OUTLINE;
                                        } else {
                                            modes |= WARN;
                                        }
                                    } else if (ch == 'S') {
                                        if (Category.caseUp(markup.charAt(i + 2)) == 'U') {
                                            modes |= SUGGEST;
                                        } else if (Category.caseUp(markup.charAt(i + 2)) == 'M') {
                                            modes |= SMALL_CAPS;
                                        } else if (Category.caseUp(markup.charAt(i + 3)) == 'I') {
                                            modes |= SHINY;
                                        } else if (Category.caseUp(markup.charAt(i + 2)) == 'H') {
                                            modes |= DROP_SHADOW;
                                        }
                                        // unrecognized falls back to small caps or jostle
                                    } else if (ch == 'C') {
                                        modes |= CONTEXT;
                                    } else if (ch == 'D') {
                                        modes |= DROP_SHADOW;
                                    } else if (ch == 'E') {
                                        modes |= ERROR;
                                    } else if (ch == 'H') {
                                        modes |= HALO;
                                    } else if (ch == 'J') {
                                        modes |= JOSTLE;
                                    } else if (ch == 'N') {
                                        if (Category.caseUp(markup.charAt(i + 2)) == 'O')
                                            modes |= NOTE;
                                        else if (Category.caseUp(markup.charAt(i + 2)) == 'E')
                                            modes |= NEON;
                                    } else if (ch == 'R') {
                                        modes |= RED_OUTLINE | BLACK_OUTLINE;
                                    } else if (ch == 'Y') {
                                        modes |= YELLOW_OUTLINE | BLACK_OUTLINE;
                                    }
                                }
                                // unrecognized falls back to no mode
                                current = ((current & 0xFFFFFFFFFF0FFFFFL) ^ modes);
                            } else {
                                current &= 0xFFFFFFFFFF0FFFFFL;
                            }
                            break;
                        case '#':
                            if (len >= 4 && len < 7) {
                                color = StringUtils.longFromHex(markup, i + 1, i + 4);
                                color =
                                        (color << 52 & 0xF000000000000000L) | (color << 48 & 0x0F00000000000000L) |
                                        (color << 48 & 0x00F0000000000000L) | (color << 44 & 0x000F000000000000L) |
                                        (color << 44 & 0x0000F00000000000L) | (color << 40 & 0x00000F0000000000L) |
                                        0x000000FE00000000L;
                            }
                            else if (len >= 7 && len < 9)
                                color = StringUtils.longFromHex(markup, i + 1, i + 7) << 40 | 0x000000FE00000000L;
                            else if (len >= 9)
                                color = StringUtils.longFromHex(markup, i + 1, i + 9) << 32 & 0xFFFFFFFE00000000L;
                            else if(len == 1){
                                // [#] is used to toggle the outline.
                                current ^= BLACK_OUTLINE;
                            } else
                                // any other length, such as [#0] or [#12] is invalid and resets the color.
                                // this doesn't care what the other chars are, so [##] is a good way to reset the color.
                                color = baseColor;
                            current = (current & ~COLOR_MASK) | color;
                            break;
                        case '@':
                            if (family == null) {
                                fontIndex = 0;
                                break;
                            }
                            fontIndex = family.fontAliases.get(StringUtils.safeSubstring(markup, i + 1, i + len), 0);
                            font = family.connected[fontIndex];
                            current = (current & 0xFFFFFFFFFFF0FFFFL) | (fontIndex & 15L) << 16;
                            break;
                        case '|':
                            // attempt to look up a known Color name with a ColorLookup
                            int lookupColor = colorLookup.getRgba(markup, i + 1, i + len) & 0xFFFFFFFE;
                            if (lookupColor == 256) color = baseColor;
                            else color = (long) lookupColor << 32;
                            current = (current & ~COLOR_MASK) | color;
                            break;
                        default:
                            // attempt to look up a known Color name with a ColorLookup
                            int gdxColor = colorLookup.getRgba(markup, i, i + len) & 0xFFFFFFFE;
                            if (gdxColor == 256) color = baseColor;
                            else color = (long) gdxColor << 32;
                            current = (current & ~COLOR_MASK) | color;
                    }
                    i += len;
                }

                //// ESCAPED SQUARE BRACKET AND TEXTURE REGION RENDERING

                else {
                    float w;
                    if(c == '+' && font.nameLookup != null) {
                        int len = markup.indexOf(']', i) - i;
                        if (len >= 0) {
                            c = font.nameLookup.get(StringUtils.safeSubstring(markup, i + 1, i + len), '+');
                        }
                    }
                    if(c == '[')
                        return (current | 2);
                    else
                        return (current | c);
                }
            } else {

                //// VISIBLE CHAR RENDERING

                char ch = markup.charAt(i);
                if (StringUtils.isLowerCase(ch)) {
                    if ((capitalize) || capsLock) {
                        ch = Category.caseUp(ch);
                    }
                } else if (StringUtils.isUpperCase(ch)) {
                    if (lowerCase) {
                        ch = Category.caseDown(ch);
                    }
                }
                return (current | ch);
            }
        }
        return current | ' ';
    }

    /**
     * Reads markup from {@code markup}, processes it, and applies it to the given char {@code chr}; returns a long
     * in the format used for styled glyphs here. This parses an extension of libGDX markup and uses it to determine
     * color, size, position, shape, strikethrough, underline, case, and scale of the given char.
     * The char drawn will start in white, with the normal size as determined by the font's metrics and scale
     * ({@link #scaleX} and {@link #scaleY}), normal case, and without bold, italic, superscript, subscript,
     * strikethrough, or underline. Markup starts with {@code [}; the next character determines what that piece of
     * markup toggles. Markup this knows:
     * <ul>
     *     <li>{@code []} undoes the most-recently-applied format change.</li>
     *     <li>{@code [ ]} clears all markup to the initial state without any applied.</li>
     *     <li>{@code [*]} toggles bold mode.</li>
     *     <li>{@code [/]} toggles italic (technically, oblique) mode.</li>
     *     <li>{@code [^]} toggles superscript mode (and turns off subscript or midscript mode).</li>
     *     <li>{@code [=]} toggles midscript mode (and turns off superscript or subscript mode).</li>
     *     <li>{@code [.]} toggles subscript mode (and turns off superscript or midscript mode).</li>
     *     <li>{@code [_]} toggles underline mode.</li>
     *     <li>{@code [~]} toggles strikethrough mode.</li>
     *     <li>{@code [!]} toggles all upper case mode.</li>
     *     <li>{@code [,]} toggles all lower case mode.</li>
     *     <li>{@code [;]} toggles capitalize each word mode (this is the same as upper case mode here).</li>
     *     <li>{@code [%P]}, where P is any decimal number, changes the scale to that percentage.
     *     {@code [%100]} is the initial value and sets to 100% scale, as in "not scaled at all."
     *     Note that this is not restricted to any increments; even {@code [%0.0123]} is technically valid.</li>
     *     <li>{@code [%]}, with no number just after it, resets scale to 100%.</li>
     *     <li>{@code [?MODE]}, where MODE can be (case-insensitive) one of "black outline", "white outline",
     *     "red outline", "blue outline", "yellow outline", "shiny", "drop shadow"/"shadow", "neon", "halo",
     *     "error", "warn", "note", "context", "suggest", "jostle", or "small caps",
     *     will enable that alternate mode. If MODE is empty or not recognized, this will disable any current active
     *     mode. If any mode is non-empty and recognized, it will be enabled and replace any current active mode.
     *     Disabling a mode should always use the empty string for MODE. The "black outline" mode isn't actually a mode,
     *     and internally enables the same feature as {@code [#]}; it is still recognized for compatibility. Note that
     *     this means it can be enabled at the same time as any other mode, and that it will not be disabled by
     *     {@code [?]}, though it can be toggled off or back on by {@code [#]} (which is the preferred way to use the
     *     outline). Other "outline" modes also enable the outline if it isn't active, but change its color. The outline
     *     can always be toggled on or off, regardless of color, by {@code [#]}.</li>
     *     <li>{@code [%?MODE]} is a synonym for the preceding {@code [?MODE]} markup for compatibility.</li>
     *     <li>{@code [%^MODE]} is a synonym for the preceding {@code [?MODE]} markup for compatibility.</li>
     *     <li>{@code [?]}, {@code [%?]} and {@code [%^]} are equivalent ways to disable a special mode, if it is active.</li>
     *     <li>{@code [@Name]}, where Name is a key in family, changes the current Font used for rendering to the Font
     *     in this.family by that name. This is ignored if family is null.</li>
     *     <li>{@code [@]}, with no text just after it, resets the font to this one (which should be item 0 in family,
     *     if family is non-null).</li>
     *     <li>{@code [#]} toggles a black outline around text. This can have its color changed by some special modes.</li>
     *     <li>{@code [#HHHHHHHH]}, where HHHHHHHH is a hex RGB888 or RGBA8888 int color, changes the color.</li>
     *     <li>{@code [#HHH]} is also a valid way to set the color using CSS-style 3 hex digits, each of which is
     *     effectively duplicated to make a 6-digit RGB888 code. {@code [#HHHH]} is a way to do this with alpha.</li>
     *     <li>{@code [COLORNAME]}, where "COLORNAME" is a color name or description that will be looked up in
     *     {@link #getColorLookup()}, changes the color. By default, this can receive ALL_CAPS names from {@link Colors}
     *     in libGDX, any names from {@link com.github.tommyettinger.textra.utils.Palette}, or mixes of one or
     *     more color names with adjectives like "dark". The name can optionally be preceded by {@code |}, which allows
     *     looking up colors with names that contain punctuation. This doesn't do much if using the default ColorLookup,
     *     {@link ColorLookup#DESCRIPTIVE}, because it only evaluates ASCII letters and numbers, and treats everything
     *     else as a separator.</li>
     * </ul>
     * You can render the result using {@link #drawGlyph(Batch, long, float, float)}. It is recommended that you avoid
     * calling this method every frame, because the color lookups usually allocate some memory, and because this can
     * usually be stored for later without needing repeated computation.
     * <br>
     * This takes a ColorLookup so that it can look up colors given a name or description; if you don't know what to
     * use, then {@link ColorLookup#DESCRIPTIVE} is the default elsewhere. Because this is static, it does
     * not need a Font to be involved.
     *
     * @param chr         a single char to apply markup to
     * @param markup      a String containing only markup syntax, like "[*][_][RED]" for bold underline in red
     * @param colorLookup a ColorLookup (often a method reference or {@link ColorLookup#DESCRIPTIVE}) to get
     *                    colors from textual names or descriptions
     * @return a long that encodes the given char with the specified markup
     */
    public static long markupGlyph(char chr, String markup, ColorLookup colorLookup) {
        return markupGlyph(chr, markup, colorLookup, null);
    }

    /**
     * Reads markup from {@code markup}, processes it, and applies it to the given char {@code chr}; returns a long
     * in the format used for styled glyphs here. This parses an extension of libGDX markup and uses it to determine
     * color, size, position, shape, strikethrough, underline, case, and scale of the given char.
     * The char drawn will start in white, with the normal size as determined by the font's metrics and scale
     * ({@link #scaleX} and {@link #scaleY}), normal case, and without bold, italic, superscript, subscript,
     * strikethrough, or underline. Markup starts with {@code [}; the next character determines what that piece of
     * markup toggles. Markup this knows:
     * <ul>
     *     <li>{@code []} undoes the most-recently-applied format change.</li>
     *     <li>{@code [ ]} clears all markup to the initial state without any applied.</li>
     *     <li>{@code [*]} toggles bold mode.</li>
     *     <li>{@code [/]} toggles italic (technically, oblique) mode.</li>
     *     <li>{@code [^]} toggles superscript mode (and turns off subscript or midscript mode).</li>
     *     <li>{@code [=]} toggles midscript mode (and turns off superscript or subscript mode).</li>
     *     <li>{@code [.]} toggles subscript mode (and turns off superscript or midscript mode).</li>
     *     <li>{@code [_]} toggles underline mode.</li>
     *     <li>{@code [~]} toggles strikethrough mode.</li>
     *     <li>{@code [!]} toggles all upper case mode.</li>
     *     <li>{@code [,]} toggles all lower case mode.</li>
     *     <li>{@code [;]} toggles capitalize each word mode (this is the same as upper case mode here).</li>
     *     <li>{@code [%P]}, where P is any decimal number, changes the scale to that percentage.
     *     {@code [%100]} is the initial value and sets to 100% scale, as in "not scaled at all."
     *     Note that this is not restricted to any increments; even {@code [%0.0123]} is technically valid.</li>
     *     <li>{@code [%]}, with no number just after it, resets scale to 100%.</li>
     *     <li>{@code [?MODE]}, where MODE can be (case-insensitive) one of "black outline", "white outline",
     *     "red outline", "blue outline", "yellow outline", "shiny", "drop shadow"/"shadow", "neon", "halo",
     *     "error", "warn", "note", "context", "suggest", "jostle", or "small caps",
     *     will enable that alternate mode. If MODE is empty or not recognized, this will disable any current active
     *     mode. If any mode is non-empty and recognized, it will be enabled and replace any current active mode.
     *     Disabling a mode should always use the empty string for MODE. The "black outline" mode isn't actually a mode,
     *     and internally enables the same feature as {@code [#]}; it is still recognized for compatibility. Note that
     *     this means it can be enabled at the same time as any other mode, and that it will not be disabled by
     *     {@code [?]}, though it can be toggled off or back on by {@code [#]} (which is the preferred way to use the
     *     outline). Other "outline" modes also enable the outline if it isn't active, but change its color. The outline
     *     can always be toggled on or off, regardless of color, by {@code [#]}.</li>
     *     <li>{@code [%?MODE]} is a synonym for the preceding {@code [?MODE]} markup for compatibility.</li>
     *     <li>{@code [%^MODE]} is a synonym for the preceding {@code [?MODE]} markup for compatibility.</li>
     *     <li>{@code [?]}, {@code [%?]} and {@code [%^]} are equivalent ways to disable a special mode, if it is active.</li>
     *     <li>{@code [@Name]}, where Name is a key in family, changes the current Font used for rendering to the Font
     *     in this.family by that name. This is ignored if family is null.</li>
     *     <li>{@code [@]}, with no text just after it, resets the font to this one (which should be item 0 in family,
     *     if family is non-null).</li>
     *     <li>{@code [#]} toggles a black outline around text. This can have its color changed by some special modes.</li>
     *     <li>{@code [#HHHHHHHH]}, where HHHHHHHH is a hex RGB888 or RGBA8888 int color, changes the color.</li>
     *     <li>{@code [#HHH]} is also a valid way to set the color using CSS-style 3 hex digits, each of which is
     *     effectively duplicated to make a 6-digit RGB888 code. {@code [#HHHH]} is a way to do this with alpha.</li>
     *     <li>{@code [COLORNAME]}, where "COLORNAME" is a color name or description that will be looked up in
     *     {@link #getColorLookup()}, changes the color. By default, this can receive ALL_CAPS names from {@link Colors}
     *     in libGDX, any names from {@link com.github.tommyettinger.textra.utils.Palette}, or mixes of one or
     *     more color names with adjectives like "dark". The name can optionally be preceded by {@code |}, which allows
     *     looking up colors with names that contain punctuation. This doesn't do much if using the default ColorLookup,
     *     {@link ColorLookup#DESCRIPTIVE}, because it only evaluates ASCII letters and numbers, and treats everything
     *     else as a separator.</li>
     * </ul>
     * You can render the result using {@link #drawGlyph(Batch, long, float, float)}. It is recommended that you avoid
     * calling this method every frame, because the color lookups usually allocate some memory, and because this can
     * usually be stored for later without needing repeated computation.
     * <br>
     * This takes a ColorLookup so that it can look up colors given a name or description; if you don't know what to
     * use, then {@link ColorLookup#DESCRIPTIVE} is the default elsewhere. Because this is static, it does
     * not need a Font to be involved.
     *
     * @param chr         a single char to apply markup to
     * @param markup      a String containing only markup syntax, like "[*][_][RED]" for bold underline in red
     * @param colorLookup a ColorLookup (often a method reference or {@link ColorLookup#DESCRIPTIVE}) to get
     *                    colors from textual names or descriptions
     * @return a long that encodes the given char with the specified markup
     */
    public static long markupGlyph(char chr, String markup, ColorLookup colorLookup, FontFamily family) {
        boolean capsLock = false, lowerCase = false;
        int c;
        final long COLOR_MASK = 0xFFFFFFFF00000000L;
        long baseColor = 0xFFFFFFFE00000000L | chr;
        long color = baseColor;
        long current = color;
        for (int i = 0, n = markup.length(); i < n; i++) {
            if (markup.charAt(i) == '[') {
                if (++i < n && (c = markup.charAt(i)) != '[') {
                    if (c == ']') {
                        color = baseColor;
                        current = color;
                        capsLock = false;
                        lowerCase = false;
                        continue;
                    }
                    int len = markup.indexOf(']', i) - i;
                    if (len < 0) break;
                    switch (c) {
                        case '*':
                            current ^= BOLD;
                            break;
                        case '/':
                            current ^= OBLIQUE;
                            break;
                        case '^':
                            if ((current & SUPERSCRIPT) == SUPERSCRIPT)
                                current &= ~SUPERSCRIPT;
                            else
                                current |= SUPERSCRIPT;
                            break;
                        case '.':
                            if ((current & SUPERSCRIPT) == SUBSCRIPT)
                                current &= ~SUBSCRIPT;
                            else
                                current = (current & ~SUPERSCRIPT) | SUBSCRIPT;
                            break;
                        case '=':
                            if ((current & SUPERSCRIPT) == MIDSCRIPT)
                                current &= ~MIDSCRIPT;
                            else
                                current = (current & ~SUPERSCRIPT) | MIDSCRIPT;
                            break;
                        case '_':
                            current ^= UNDERLINE;
                            break;
                        case '~':
                            current ^= STRIKETHROUGH;
                            break;
                        case ';':
                        case '!':
                            capsLock = !capsLock;
                            lowerCase = false;
                            break;
                        case ',':
                            lowerCase = !lowerCase;
                            capsLock = false;
                            break;
                        case '%':
                            if (len >= 2) {
                                // alternate mode, takes [%?mode name]
                                // "black outline", "white outline", "red outline", "blue outline", "yellow outline",
                                // "shiny", "drop shadow"/"shadow", "neon", "halo",
                                // "error", "warn", "note", "context", "suggest",
                                // "jostle", "small caps"
                                if (markup.charAt(i + 1) == '?' || markup.charAt(i + 1) == '^') {
                                    long modes = 0L;
                                    if(len >= 5) {
                                        char ch = Category.caseUp(markup.charAt(i+2));
                                        if(ch == 'B') {
                                            modes |= BLACK_OUTLINE;
                                            if(Category.caseUp(markup.charAt(i+4)) == 'U')
                                                modes |= BLUE_OUTLINE;
                                        } else if(ch == 'W') {
                                            if(Category.caseUp(markup.charAt(i+3)) == 'H') {
                                                modes |= WHITE_OUTLINE | BLACK_OUTLINE;
                                            }
                                            else {
                                                modes |= WARN;
                                            }
                                        } else if(ch == 'S') {
                                            if(Category.caseUp(markup.charAt(i+3)) == 'U') {
                                                modes |= SUGGEST;
                                            }
                                            else if(Category.caseUp(markup.charAt(i+3)) == 'M') {
                                                modes |= SMALL_CAPS;
                                            }
                                            else if(Category.caseUp(markup.charAt(i+4)) == 'I') {
                                                modes |= SHINY;
                                            }
                                            else if(Category.caseUp(markup.charAt(i+3)) == 'H'){
                                                modes |= DROP_SHADOW;
                                            }
                                            // unrecognized falls back to small caps or jostle
                                        } else if(ch == 'C'){
                                            modes |= CONTEXT;
                                        } else if(ch == 'D'){
                                            modes |= DROP_SHADOW;
                                        } else if(ch == 'E'){
                                            modes |= ERROR;
                                        } else if(ch == 'H'){
                                            modes |= HALO;
                                        } else if(ch == 'J'){
                                            modes |= JOSTLE;
                                        } else if(ch == 'N'){
                                            if(Category.caseUp(markup.charAt(i+3)) == 'O')
                                                modes |= NOTE;
                                            else if(Category.caseUp(markup.charAt(i+3)) == 'E')
                                                modes |= NEON;
                                        } else if(ch == 'R') {
                                            modes |= RED_OUTLINE | BLACK_OUTLINE;
                                        } else if(ch == 'Y') {
                                            modes |= YELLOW_OUTLINE | BLACK_OUTLINE;
                                        }
                                    }
                                    // unrecognized falls back to no mode
                                    current = ((current & 0xFFFFFFFFFF0FFFFFL) ^ modes);
                                }
                            }
                            break;
                        case '?':
                            if(len > 1) {
                            /*
                               alternate mode, takes [?mode name]
                               "black outline", "white outline", "red outline", "blue outline", "yellow outline",
                               "shiny", "drop shadow"/"shadow", "neon", "halo",
                               "error", "warn", "note", "context", "suggest",
                               "jostle", "small caps"
                                */
                                long modes = 0L;
                                if (len >= 4) {
                                    char ch = Category.caseUp(markup.charAt(i + 1));
                                    if (ch == 'B') {
                                        modes |= BLACK_OUTLINE;
                                        if (Category.caseUp(markup.charAt(i + 3)) == 'U')
                                            modes |= BLUE_OUTLINE;
                                    } else if (ch == 'W') {
                                        if (Category.caseUp(markup.charAt(i + 2)) == 'H') {
                                            modes |= WHITE_OUTLINE | BLACK_OUTLINE;
                                        } else {
                                            modes |= WARN;
                                        }
                                    } else if (ch == 'S') {
                                        if (Category.caseUp(markup.charAt(i + 2)) == 'U') {
                                            modes |= SUGGEST;
                                        } else if (Category.caseUp(markup.charAt(i + 2)) == 'M') {
                                            modes |= SMALL_CAPS;
                                        } else if (Category.caseUp(markup.charAt(i + 3)) == 'I') {
                                            modes |= SHINY;
                                        } else if (Category.caseUp(markup.charAt(i + 2)) == 'H') {
                                            modes |= DROP_SHADOW;
                                        }
                                        // unrecognized falls back to small caps or jostle
                                    } else if (ch == 'C') {
                                        modes |= CONTEXT;
                                    } else if (ch == 'D') {
                                        modes |= DROP_SHADOW;
                                    } else if (ch == 'E') {
                                        modes |= ERROR;
                                    } else if (ch == 'H') {
                                        modes |= HALO;
                                    } else if (ch == 'J') {
                                        modes |= JOSTLE;
                                    } else if (ch == 'N') {
                                        if (Category.caseUp(markup.charAt(i + 2)) == 'O')
                                            modes |= NOTE;
                                        else if (Category.caseUp(markup.charAt(i + 2)) == 'E')
                                            modes |= NEON;
                                    } else if (ch == 'R') {
                                        modes |= RED_OUTLINE | BLACK_OUTLINE;
                                    } else if (ch == 'Y') {
                                        modes |= YELLOW_OUTLINE | BLACK_OUTLINE;
                                    }
                                }
                                // unrecognized falls back to no mode
                                current = ((current & 0xFFFFFFFFFF0FFFFFL) ^ modes);
                            } else {
                                current &= 0xFFFFFFFFFF0FFFFFL;
                            }
                            break;
                        case '#':
                            if (len >= 4 && len < 7) {
                                color = StringUtils.longFromHex(markup, i + 1, i + 4);
                                color =
                                    (color << 52 & 0xF000000000000000L) | (color << 48 & 0x0F00000000000000L) |
                                    (color << 48 & 0x00F0000000000000L) | (color << 44 & 0x000F000000000000L) |
                                    (color << 44 & 0x0000F00000000000L) | (color << 40 & 0x00000F0000000000L) |
                                    0x000000FE00000000L;
                            }
                            else if (len >= 7 && len < 9)
                                color = StringUtils.longFromHex(markup, i + 1, i + 7) << 40 | 0x000000FE00000000L;
                            else if (len >= 9)
                                color = StringUtils.longFromHex(markup, i + 1, i + 9) << 32 & 0xFFFFFFFE00000000L;
                            else if(len == 1){
                                // [#] is used to toggle the outline.
                                current ^= BLACK_OUTLINE;
                            } else
                                // any other length, such as [#0] or [#12] is invalid and resets the color.
                                // this doesn't care what the other chars are, so [##] is a good way to reset the color.
                                color = baseColor;
                            current = (current & ~COLOR_MASK) | color;
                            break;
                        case '@':
                            if (family == null) {
                                break;
                            }
                            int fontIndex = family.fontAliases.get(StringUtils.safeSubstring(markup, i + 1, i + len), 0);
                            current = (current & 0xFFFFFFFFFFF0FFFFL) | (fontIndex & 15L) << 16;
                            break;
                        case '|':
                            // attempt to look up a known Color name with a ColorLookup
                            int lookupColor = colorLookup.getRgba(markup, i + 1, i + len) & 0xFFFFFFFE;
                            if (lookupColor == 256) color = baseColor;
                            else color = (long) lookupColor << 32;
                            current = (current & ~COLOR_MASK) | color;
                            break;
                        default:
                            // attempt to look up a known Color name with a ColorLookup
                            int gdxColor = colorLookup.getRgba(markup, i, i + len) & 0xFFFFFFFE;
                            if (gdxColor == 256) color = baseColor;
                            else color = (long) gdxColor << 32;
                            current = (current & ~COLOR_MASK) | color;
                    }
                }
            }
        }
        return current;
    }

    /**
     * When the {@link Layout#getTargetWidth() targetWidth} of a Layout changes, you can use this to cause the text to
     * be placed according to the new width, and wrap if needed. This doesn't allocate as much as
     * {@link #markup(String, Layout)}, if at all, but may eat up newlines if called repeatedly.
     * @param changing a Layout that will be modified in-place
     * @return {@code changing}, after modifications
     */
    public Layout regenerateLayout(Layout changing) {
        if (changing.font == null) {
            return changing;
        }
        if(!changing.font.equals(this)){
            changing.font = this;
        }
        Font font = null;
        float scaleX;
        float targetWidth = changing.getTargetWidth();
        int oldLength = changing.lines.size;
        Line firstLine = changing.getLine(0);
        for (int i = 1; i < oldLength; i++) {
            firstLine.glyphs.addAll(changing.getLine(i).glyphs);
        }
        changing.lines.truncate(1);
        boolean curly = false;
        for (int ln = 0; ln < changing.lines(); ln++) {
            Line line = changing.getLine(ln);
            line.height = 0;
            int a = changing.countGlyphsBeforeLine(ln);
            System.out.println("On line " + ln + ", advances.size = " + changing.advances.size + " and layout size = " + changing.countGlyphs());
            float drawn = 0f, visibleWidth = 0f;
            int cutoff, breakPoint = -2, spacingPoint = -2;
            LongArray glyphs = line.glyphs;
            int kern = -1;
            float amt;
            for (int i = 0, n = glyphs.size; i < n; i++) {
                long glyph = glyphs.get(i);
                char ch = (char) glyph;
                float advance = changing.advances.get(a);
                float sizingY = changing.sizing.get(a << 1 | 1);
//                float advance = Math.min(changing.advances.get(a), changing.sizing.get(a << 1 | 1));
                a++;
                if(ch == '{' && !curly) curly = true;
                if((glyph & SMALL_CAPS) == SMALL_CAPS) ch = Category.caseUp(ch);
                if (family != null) font = family.connected[(int) (glyph >>> 16 & 15)];
                if (font == null) font = this;

                if (font.kerning == null) {

                    //// no kerning

                    line.height = Math.max(line.height, font.cellHeight * sizingY);
                    if(ch >= 0xE000 && ch < 0xF800)
                        scaleX = (font.isMono ? 1f : advance) * font.cellHeight / font.mapping.get(ch, font.defaultValue).getMaxDimension() * font.inlineImageStretch;
                    else
                        scaleX = font.scaleX * (font.isMono ? 1f : advance);

                    GlyphRegion tr = font.mapping.get(ch);
                    if (tr == null) continue;
                    float changedW = xAdvance(font, scaleX, glyph);
                    if(i == 0 && !isMono && !(ch >= '\uE000' && ch < '\uF800')){
                        float ox = tr.offsetX;
                        if(Float.isNaN(ox)) ox = 0;
                        else ox *= scaleX * (1f + 0.5f * (-(glyph & SUPERSCRIPT) >> 63));
                        if(ox < 0) changedW -= ox;
                    }
                    // if inside curly braces, set width to 0.
                    if(curly) {
                        changedW = 0;
                        if(ch == '}') curly = false;
                    }


                    if (breakPoint >= 0 && visibleWidth + (breakPoint == spacingPoint ? 0 : changedW) > (targetWidth + 1f)) {
                        cutoff = breakPoint + 1;
                        Line next;
                        if (changing.lines() == ln + 1) {
                            next = changing.pushLineBare();
                        } else
                            next = changing.getLine(ln + 1);
                        if (next == null) {
                            glyphs.truncate(cutoff);
                            if(handleEllipsis(changing)) {
                                calculateSize(changing);
                                return changing;
                            }
                            break;
                        }
                        next.height = Math.max(next.height, font.cellHeight * sizingY);

                        int nextSize = next.glyphs.size;
                        long[] arr = next.glyphs.setSize(nextSize + glyphs.size - cutoff);
                        System.arraycopy(arr, 0, arr, glyphs.size - cutoff, nextSize);
                        System.arraycopy(glyphs.items, cutoff, arr, 0, glyphs.size - cutoff);
                        glyphs.truncate(cutoff);
                        break;
                    }
                    if (ch == '\n') {
                        Line next = changing.pushLineBare();
                        if (next == null) {
                            if(handleEllipsis(changing)) {
                                calculateSize(changing);
                                return changing;
                            }
                            break;
                        }
                        next.height = Math.max(next.height, font.cellHeight * sizingY);

                        long[] arr = next.glyphs.setSize(glyphs.size - i - 1);
                        System.arraycopy(glyphs.items, i + 1, arr, 0, glyphs.size - i - 1);
                        glyphs.truncate(i);
                        glyphs.add(applyChar(glyphs.isEmpty() ? 0L : glyphs.peek(), '\n'));
                        break;
                    }
                    if (glyph >>> 32 == 0L) {
                        breakPoint = i;
                        spacingPoint = i;
                        visibleWidth -= changedW;
                    } else if (Arrays.binarySearch(breakChars.items, 0, breakChars.size, (char) glyph) >= 0) {
                        breakPoint = i;
                        if (Arrays.binarySearch(spaceChars.items, 0, spaceChars.size, (char) glyph) >= 0) {
                            spacingPoint = i;
                            visibleWidth -= changedW;
                        } else {
                            visibleWidth = drawn;
                        }
                    }
                    else {
                        visibleWidth = drawn;
                    }
                    drawn += changedW;
                    visibleWidth += changedW;
                } else {

                    //// font has kerning

                    line.height = Math.max(line.height, font.cellHeight * sizingY);
                    if(ch >= 0xE000 && ch < 0xF800)
                        scaleX = (font.isMono ? 1f : advance) * font.cellHeight / font.mapping.get(ch, font.defaultValue).getMaxDimension() * font.inlineImageStretch;
                    else
                        scaleX = font.scaleX * (font.isMono ? 1f : advance);
                    kern = kern << 16 | ch;
                    amt = font.kerning.get(kern, 0) * scaleX;
                    GlyphRegion tr = font.mapping.get(ch);
                    if (tr == null) continue;
                    float changedW = xAdvance(font, scaleX, glyph);
                    if(i == 0 && !isMono && !(ch >= '\uE000' && ch < '\uF800')){
                        float ox = tr.offsetX;
                        if(Float.isNaN(ox)) ox = 0;
                        else ox *= scaleX * (1f + 0.5f * (-(glyph & SUPERSCRIPT) >> 63));
                        if(ox < 0) changedW -= ox;
                    }
                    // if inside curly braces, set width to 0.
                    if(curly) {
                        changedW = 0;
                        if(ch == '}') curly = false;
                    }
                    if (breakPoint >= 0 && visibleWidth + (breakPoint == spacingPoint ? 0 : changedW + amt) > (targetWidth + 1f)) {
                        cutoff = breakPoint + 1;
                        Line next;
                        if (changing.lines() == ln + 1) {
                            next = changing.pushLineBare();
                        } else
                            next = changing.getLine(ln + 1);
                        if (next == null) {
                            glyphs.truncate(cutoff);
                            if(handleEllipsis(changing)) {
                                calculateSize(changing);
                                return changing;
                            }
                            break;
                        }
                        next.height = Math.max(next.height, font.cellHeight * sizingY);

                        int nextSize = next.glyphs.size;
                        long[] arr = next.glyphs.setSize(nextSize + glyphs.size - cutoff);
                        System.arraycopy(arr, 0, arr, glyphs.size - cutoff, nextSize);
                        System.arraycopy(glyphs.items, cutoff, arr, 0, glyphs.size - cutoff);
                        glyphs.truncate(cutoff);
                        break;
                    }
                    if (ch == '\n') {
                        Line next = changing.pushLineBare();
                        if (next == null) {
                            if(handleEllipsis(changing)) {
                                calculateSize(changing);
                                return changing;
                            }
                            break;
                        }
                        next.height = Math.max(next.height, font.cellHeight * sizingY);

                        long[] arr = next.glyphs.setSize(glyphs.size - i - 1);
                        System.arraycopy(glyphs.items, i + 1, arr, 0, glyphs.size - i - 1);
                        glyphs.truncate(i);
                        if(glyphs.isEmpty())
                            glyphs.add('\n');
                        else {
                            glyphs.add(applyChar(glyphs.peek(), '\n'));
                        }
                        break;
                    }
                    if (glyph >>> 32 == 0L) {
                        breakPoint = i;
                        spacingPoint = i;
                        visibleWidth -= changedW + amt;
                    }
                    else if (Arrays.binarySearch(breakChars.items, 0, breakChars.size, (char) glyph) >= 0) {
                        breakPoint = i;
                        if (Arrays.binarySearch(spaceChars.items, 0, spaceChars.size, (char) glyph) >= 0) {
                            spacingPoint = i;
                            visibleWidth -= changedW + amt;
                        }
                        else {
                            visibleWidth = drawn;
                        }
                    }
                    else {
                        visibleWidth = drawn;
                    }
                    drawn += changedW + amt;
                    visibleWidth += changedW + amt;
                }
            }
            System.out.println("Line " + ln + " has height " + line.height + ", with " + line.glyphs.size + " glyphs.");
        }
        calculateSize(changing);
        for (int ln = 0; ln < changing.lines(); ln++) {
            System.out.println("Line " + ln + " has height " + changing.getLine(ln).height);
        }
        return changing;
    }

    /**
     * Evaluates {@code markup} to get a formatting state and stores it for later usage with {@code "[ name]"} syntax
     * and the given {@code name}. Where {@code "[ ]"} will reset state to its starting value, {@code "[ name]"} will
     * restore a previously saved state. States can be stored with this method (which will associate the named state
     * with this Font, and will be copied with it), or temporarily stored in a markup String using {@code "[(name)]"}.
     * @param name a non-null String to associate a formatting state with
     * @param markup markup (with or without a char in it) that will be applied when this formatting state is used
     */
    public void storeState(String name, String markup) {
        storedStates.put(name, markupGlyph('\u0000', markup));
    }

    /**
     * Evaluates {@code markup} to get a formatting state and stores it for later usage with {@code "[ name]"} syntax
     * and the given {@code name}. Where {@code "[ ]"} will reset state to its starting value, {@code "[ name]"} will
     * restore a previously saved state. States can be stored with this method (which will associate the named state
     * with this Font, and will be copied with it), or temporarily stored in a markup String using {@code "[(name)]"}.
     * @param name a non-null String to associate a formatting state with
     * @param formatted any (ignored) character that will have its formatting stored for later usage
     */
    public void storeState(String name, long formatted) {
        storedStates.put(name, formatted & 0xFFFFFFFFFFFF0000L);
    }

    /**
     * Looks up the given name in the stored states and returns the associated formatting state, or {@code fallback} if
     * the name was not present. A suggestion for {@code fallback} is to use {@code 'N'}, since a valid formatting state
     * won't have any character data, and {@code 'N'} only has character data.
     * @param name a non-null String to look up in the stored states
     * @param fallback a formatting state or character to return if the given name is not found (often {@code 'N'}
     * @return the formatting state associated with name (if found), or {@code fallback} (if not found)
     */
    public long getStoredState(String name, long fallback) {
        return storedStates.get(name, fallback);
    }

    /**
     * Removes the formatting state with the given name if it is present, or does nothing if the name is not present.
     * @param name the name of the formatting state to remove
     */
    public void removeStoredState(String name) {
        storedStates.remove(name, 0L);
    }

    /**
     * Important; must be called in {@link com.badlogic.gdx.ApplicationListener#resize(int, int)} on each
     * SDF, MSDF, or SDF_OUTLINE font you have currently rendering! This allows the distance field to appear
     * as correct and crisp-outlined as it should be; without this, the distance field will probably not look
     * very sharp at all. This doesn't need to be called every frame, only in resize().
     * <br>
     * Given the new width and height for a window, this attempts to adjust the {@link #actualCrispness} of an
     * SDF/MSDF/SDF_OUTLINE font so that it will display cleanly at a different size. This uses this font's
     * {@link #distanceFieldCrispness} as a multiplier applied after calculating the initial crispness.
     * <br>
     * If you use a viewport that significantly zooms in or out, you should multiply width by
     * {@code viewport.getScreenWidth() / viewport.getWorldWidth()}, and similarly multiply height by the corresponding
     * screen height divided by world height. This can avoid the distance fields looking extremely blurry or boxy when
     * one world unit covers many pixels, or too aliased and jagged in the opposite case.Typically, in your
     * {@link com.badlogic.gdx.ApplicationListener#resize(int, int)} method, you call {@link Viewport#update(int, int)}
     * (or the overload that can center the camera, if needed), and then call this or another method that resizes
     * distance fields on each Font you are currently using.
     * <br>
     * If you load all or most of your Font instances via a {@link FWSkin}, you can use
     * {@link FWSkin#resizeDistanceFields(float, float)} to resize all Fonts loaded by the Skin at once.
     * You can also resize all distance fields in a FontFamily using
     * {@link FontFamily#resizeDistanceFields(float, float)}.
     *
     * @param width  the new window width; usually a parameter in {@link com.badlogic.gdx.ApplicationListener#resize(int, int)}
     * @param height the new window height; usually a parameter in {@link com.badlogic.gdx.ApplicationListener#resize(int, int)}
     */
    public void resizeDistanceField(float width, float height) {
        if (distanceField != DistanceFieldType.STANDARD) {
            if (Gdx.graphics.getBackBufferWidth() == 0 || Gdx.graphics.getBackBufferHeight() == 0) {
                actualCrispness = distanceFieldCrispness;
            } else {
                actualCrispness = distanceFieldCrispness *
                                  Math.max(width / Gdx.graphics.getBackBufferWidth(),
                                          height / Gdx.graphics.getBackBufferHeight());
            }
        }
    }

    /**
     * Important; must be called in {@link com.badlogic.gdx.ApplicationListener#resize(int, int)} on each
     * SDF, MSDF, or SDF_OUTLINE font you have currently rendering! This allows the distance field to appear
     * as correct and crisp-outlined as it should be; without this, the distance field will probably not look
     * very sharp at all. This doesn't need to be called every frame, only in resize(). This overload also uses
     * a Viewport to better configure heavily-zoomed Fonts. It does not change the Viewport.
     * <br>
     * Given the new width and height for a window, this attempts to adjust the {@link #actualCrispness} of an
     * SDF/MSDF/SDF_OUTLINE font so that it will display cleanly at a different size. This uses this font's
     * {@link #distanceFieldCrispness} as a multiplier applied after calculating the initial crispness.
     * <br>
     * This handles scaling based on the given Viewport as well as based on the given width and height. This means
     * you do not need to consider the manual changes suggested for the other overload,
     * {@link #resizeDistanceField(float, float)}. The given Viewport must have been updated with the proper width
     * and height for your application window before passing it here. Typically, in your
     * {@link com.badlogic.gdx.ApplicationListener#resize(int, int)} method, you call {@link Viewport#update(int, int)}
     * (or the overload that can center the camera, if needed), and then call this or another method that resizes
     * distance fields on each Font you are currently using.
     * <br>
     * If you load all or most of your Font instances via a {@link FWSkin}, you can use
     * {@link FWSkin#resizeDistanceFields(float, float, Viewport)} to resize all Fonts loaded by the Skin at once.
     * You can also resize all distance fields in a FontFamily using
     * {@link FontFamily#resizeDistanceFields(float, float, Viewport)}.
     *
     * @param width  the new window width; usually a parameter in {@link com.badlogic.gdx.ApplicationListener#resize(int, int)}
     * @param height the new window height; usually a parameter in {@link com.badlogic.gdx.ApplicationListener#resize(int, int)}
     * @param viewport the current Viewport, after it has been updated using {@link Viewport#update(int, int)}
     */
    public void resizeDistanceField(float width, float height, Viewport viewport) {
        if (distanceField != DistanceFieldType.STANDARD) {
            if (Gdx.graphics.getBackBufferWidth() == 0 || Gdx.graphics.getBackBufferHeight() == 0) {
                actualCrispness = distanceFieldCrispness;
            } else {
                actualCrispness = distanceFieldCrispness *
                                  Math.max(width * viewport.getScreenWidth() / (viewport.getWorldWidth() * Gdx.graphics.getBackBufferWidth()),
                                          height * viewport.getScreenHeight() / (viewport.getWorldHeight() * Gdx.graphics.getBackBufferHeight()));
            }
        }
    }

    /**
     * Given a glyph as a long, this returns the RGBA8888 color it uses.
     *
     * @param glyph a glyph as a long, as used by {@link Layout} and {@link Line}
     * @return the int color used by the given glyph, as RGBA8888
     */
    public static int extractColor(long glyph) {
        return (int) (glyph >>> 32);
    }

    /**
     * Replaces the section of glyph that stores its color with the given RGBA8888 int color.
     *
     * @param glyph a glyph as a long, as used by {@link Layout} and {@link Line}
     * @param color the int color to use, as an RGBA8888 int
     * @return another long glyph that uses the specified color
     */
    public static long applyColor(long glyph, int color) {
        return (glyph & 0xFFFFFFFFL) | ((long) color << 32 & 0xFFFFFFFE00000000L);
    }

    /**
     * Given a glyph as a long, this returns the style bits it uses. You can cross-reference these with
     * {@link #BOLD}, {@link #OBLIQUE}, {@link #UNDERLINE}, {@link #STRIKETHROUGH}, {@link #SUBSCRIPT},
     * {@link #MIDSCRIPT}, {@link #SUPERSCRIPT}, and {@link #BLACK_OUTLINE}.
     *
     * @param glyph a glyph as a long, as used by {@link Layout} and {@link Line}
     * @return the style bits used by the given glyph
     */
    public static long extractStyle(long glyph) {
        return glyph & 0x7F000000L;
    }

    /**
     * Replaces the section of glyph that stores its style with the given long bits.You can get the bit constants with
     * {@link #BOLD}, {@link #OBLIQUE}, {@link #UNDERLINE}, {@link #STRIKETHROUGH}, {@link #SUBSCRIPT},
     * {@link #MIDSCRIPT}, {@link #SUPERSCRIPT}, and {@link #BLACK_OUTLINE}. Because only a small section is used from
     * style, you can pass an existing styled glyph as the second parameter to copy its style information into glyph.
     *
     * @param glyph a glyph as a long, as used by {@link Layout} and {@link Line}
     * @param style the long style bits to use, which should usually be bits from the aforementioned constants
     * @return another long glyph that uses the specified style
     */
    public static long applyStyle(long glyph, long style) {
        return (glyph & 0xFFFFFFFF80FFFFFFL) | (style & 0x7F000000L);
    }

    /**
     * This no longer does anything because scale is no longer stored inside each glyph.
     * This allows scales to smoothly change from 0.001f to 123456.789f, or whatever the user requests.
     * The scale is now stored in {@link Layout#sizing} and a similar value (that changes less often when
     * TypingLabel effects are in use) in {@link Layout#advances}. These both store multipliers to the
     * GlyphRegion's already-stored-per-region metrics.
     * @param glyph ignored
     * @return 1.0f, always; you need to get the scale from a Layout directly
     * @deprecated use the {@link Layout#sizing} or {@link Layout#advances} fields in a Layout instead
     */
    @Deprecated
    public static float extractScale(long glyph) {
        return 1f;
    }

    /**
     * This no longer does anything because scale is no longer stored inside each glyph, and isn't an int.
     * This allows scales to smoothly change from 0.001f to 123456.789f, or whatever the user requests.
     * The scale is now stored in {@link Layout#sizing} and a similar value (that changes less often when
     * TypingLabel effects are in use) in {@link Layout#advances}. These both store multipliers to the
     * GlyphRegion's already-stored-per-region metrics.
     *
     * @param glyph ignored
     * @return 4, always; you need to get the scale from a Layout directly
     * @deprecated use the {@link Layout#sizing} or {@link Layout#advances} fields in a Layout instead
     */
    @Deprecated
    public static int extractIntScale(long glyph) {
        return 4;
    }
    /**
     * This no longer does anything because scale is no longer stored inside each glyph; this returns
     * {@code glyph} without changes.
     * The scale is now stored in {@link Layout#sizing} and a similar value (that changes less often when
     * TypingLabel effects are in use) in {@link Layout#advances}. These both store multipliers to the
     * GlyphRegion's already-stored-per-region metrics.
     *
     * @param glyph a glyph as a long, as used by {@link Layout} and {@link Line}
     * @param scale ignored
     * @return glyph, without changes
     * @deprecated scaling is now stored in both the {@link Layout#sizing}and {@link Layout#advances} fields in a Layout
     */
    @Deprecated
    public static long applyScale(long glyph, float scale) {
        return glyph;
//        return (glyph & 0xFFFFFFFFFF0FFFFFL) | ((long) Math.floor(scale * 4.0 - 4.0) & 15L) << 20;
    }


    /**
     * Given a glyph as a long, this returns the bit flags for the current mode, if alternate mode is enabled. Note that
     * while BLACK_OUTLINE can be enabled like a mode, it is considered a style, so it can overlap with any mode, and
     * can be checked with {@link #extractStyle(long)}. To check whether a given mode is present, use one of:
     * <ul>
     *     <li>{@code (extractMode(glyph) & ALTERNATE_MODES_MASK) == WHITE_OUTLINE}</li>
     *     <li>{@code (extractMode(glyph) & ALTERNATE_MODES_MASK) == YELLOW_OUTLINE}</li>
     *     <li>{@code (extractMode(glyph) & ALTERNATE_MODES_MASK) == RED_OUTLINE}</li>
     *     <li>{@code (extractMode(glyph) & ALTERNATE_MODES_MASK) == BLUE_OUTLINE}</li>
     *     <li>{@code (extractMode(glyph) & ALTERNATE_MODES_MASK) == DROP_SHADOW}</li>
     *     <li>{@code (extractMode(glyph) & ALTERNATE_MODES_MASK) == SHINY}</li>
     *     <li>{@code (extractMode(glyph) & ALTERNATE_MODES_MASK) == NEON}</li>
     *     <li>{@code (extractMode(glyph) & ALTERNATE_MODES_MASK) == HALO}</li>
     *     <li>{@code (extractMode(glyph) & ALTERNATE_MODES_MASK) == ERROR}</li>
     *     <li>{@code (extractMode(glyph) & ALTERNATE_MODES_MASK) == WARN}</li>
     *     <li>{@code (extractMode(glyph) & ALTERNATE_MODES_MASK) == NOTE}</li>
     *     <li>{@code (extractMode(glyph) & ALTERNATE_MODES_MASK) == CONTEXT}</li>
     *     <li>{@code (extractMode(glyph) & ALTERNATE_MODES_MASK) == SUGGEST}</li>
     *     <li>{@code (extractMode(glyph) & ALTERNATE_MODES_MASK) == SMALL_CAPS}</li>
     *     <li>{@code (extractMode(glyph) & ALTERNATE_MODES_MASK) == JOSTLE}</li>
     * </ul>
     * The last constant of each line is the mode that line checks for. Each constant is defined in Font.
     * If no alternate mode is not enabled, this returns 0.
     *
     * @param glyph a glyph as a long, as used by {@link Layout} and {@link Line}
     * @return the bit flags for the current alternate mode, if enabled; see docs
     */
    public static long extractMode(long glyph) {
        return (glyph & ALTERNATE_MODES_MASK);
    }

    /**
     * Replaces the section of glyph that stores its alternate mode (which is the same section that stores its scale)
     * with the given bit flags representing a mode (or lack of one). These bit flags are generally obtained using
     * {@link #extractMode(long)}, though you could acquire or create them in any number of ways. If any of the outline
     * color modes are applied using this, that doesn't mean an outline is enabled, just that if an outline is drawn,
     * it will use a different color. You can make an outline present with {@link #applyStyle(long, long)} and
     * {@link #BLACK_OUTLINE}; if the mode {@link #RED_OUTLINE} is also present, the outline will be drawn in red.
     *
     * @param glyph a glyph as a long, as used by {@link Layout} and {@link Line}
     * @param modeFlags bit flags typically obtained from {@link #extractMode(long)}
     * @return another long glyph that uses the specified mode
     */
    public static long applyMode(long glyph, long modeFlags) {
        return (glyph & 0xFFFFFFFFFF0FFFFFL) | (0xF00000L & modeFlags);
    }

    /**
     * Given a glyph as a long, this returns the char it displays. This automatically corrects the placeholder char
     * u0002 to the glyph it displays as, {@code '['}.
     *
     * @param glyph a glyph as a long, as used by {@link Layout} and {@link Line}
     * @return the char used by the given glyph
     */
    public static char extractChar(long glyph) {
        final char c = (char) glyph;
        return c == 2 ? '[' : c;
    }

    /**
     * Replaces the section of glyph that stores its char with the given other char. You can enter the character
     * {@code '['} by using the char {@code (char)2}, or possibly by using an actual {@code '['} char. This last option
     * is untested and unrecommended, but may have uses if you are willing to dig deep into the internals here.
     *
     * @param glyph a glyph as a long, as used by {@link Layout} and {@link Line}
     * @param c     the char to use
     * @return another long glyph that uses the specified char
     */
    public static long applyChar(long glyph, char c) {
        return (glyph & 0xFFFFFFFFFFFF0000L) | c;
    }

    /**
     * Releases all resources of this object.
     */
    @Override
    public void dispose() {
        if (shader != null)
            shader.dispose();
        if(whiteBlock != null)
            whiteBlock.dispose();
    }

    /**
     * Clears {@link TypingConfig#GLOBAL_VARS} and calls {@link TypingConfig#initializeGlobalVars()}. This can be useful
     * if you target Android and you use Activity.finish(), or some other way of ending an app that does not clear
     * static values. Consider calling this if you encounter different (buggy) behavior on the second launch of an
     * Android app vs. the first launch. It is not needed on desktop JVMs or GWT. This only could be needed if you add
     * different items to {@link TypingConfig#GLOBAL_VARS} on different runs of your Android app.
     */
    public static void clearStatic() {
        TypingConfig.GLOBAL_VARS.clear();
        TypingConfig.initializeGlobalVars();
    }


    @Override
    public String toString() {
        return "Font '" + name + "' at scale " + scaleX + " by " + scaleY;
    }

    public String debugString() {
        return "Font{" +
                "distanceField=" + distanceField +
                ", isMono=" + isMono +
                ", kerning=" + (kerning == null ? "null" : kerning.size + " pairs") +
                ", actualCrispness=" + actualCrispness +
                ", distanceFieldCrispness=" + distanceFieldCrispness +
                ", cellWidth=" + cellWidth +
                ", cellHeight=" + cellHeight +
                ", originalCellWidth=" + originalCellWidth +
                ", originalCellHeight=" + originalCellHeight +
                ", scaleX=" + scaleX +
                ", scaleY=" + scaleY +
                ", descent=" + descent +
                ", solidBlock=" + solidBlock +
                ", family=" + family +
                ", integerPosition=" + integerPosition +
                ", obliqueStrength=" + obliqueStrength +
                ", boldStrength=" + boldStrength +
                ", outlineStrength=" + outlineStrength +
                ", name='" + name + '\'' +
                ", PACKED_BLACK=" + PACKED_BLACK +
                ", PACKED_WHITE=" + PACKED_WHITE +
                ", PACKED_ERROR_COLOR=" + PACKED_ERROR_COLOR +
                ", PACKED_WARN_COLOR=" + PACKED_WARN_COLOR +
                ", PACKED_NOTE_COLOR=" + PACKED_NOTE_COLOR +
                ", xAdjust=" + xAdjust +
                ", yAdjust=" + yAdjust +
                ", widthAdjust=" + widthAdjust +
                ", heightAdjust=" + heightAdjust +
                ", underX=" + underX +
                ", underY=" + underY +
                ", underLength=" + underLength +
                ", underBreadth=" + underBreadth +
                ", strikeX=" + strikeX +
                ", strikeY=" + strikeY +
                ", strikeLength=" + strikeLength +
                ", strikeBreadth=" + strikeBreadth +
                ", fancyX=" + fancyX +
                ", fancyY=" + fancyY +
                ", inlineImageOffsetX=" + inlineImageOffsetX +
                ", inlineImageOffsetY=" + inlineImageOffsetY +
                ", inlineImageXAdvance=" + inlineImageXAdvance +
                ", inlineImageStretch=" + inlineImageStretch +
                '}';
    }

    /**
     * Given a 20-item float array (almost always {@link #vertices} in this class) and a Texture to draw (part of), this
     * draws some part of the Texture using the given Batch. This is used internally to wrap around calls to
     * {@link Batch#draw(Texture, float[], int, int)}.
     * <br>
     * This is an extension point so Batch implementations that use more attributes than SpriteBatch can still make use
     * of Font. If not overridden, this will act exactly like calling {@code batch.draw(texture, vertices, 0, 20);}.
     * <br>
     * The format this should generally expect for {@code vertices} is the same as what a single Sprite would draw with
     * SpriteBatch. There are 4 sections in each array, each with 5 floats corresponding to one vertex on a quad. Font
     * only ever assigns one color (as a packed float, such as from {@link Color#toFloatBits()}) to all four vertices,
     * but overriding classes don't necessarily need to do this. Where x and y define the lower-left vertex position,
     * width and height are the axis-aligned dimensions of the quad, color is a packed float, and u/v/u2/v2 are the UV
     * coordinates to use from {@code texture}, the format looks like:
     * <pre>
     *         vertices[0] = x;
     *         vertices[1] = y;
     *         vertices[2] = color;
     *         vertices[3] = u;
     *         vertices[4] = v;
     *
     *         vertices[5] = x;
     *         vertices[6] = y + height;
     *         vertices[7] = color;
     *         vertices[8] = u;
     *         vertices[9] = v2;
     *
     *         vertices[10] = x + width;
     *         vertices[11] = y + height;
     *         vertices[12] = color;
     *         vertices[13] = u2;
     *         vertices[14] = v2;
     *
     *         vertices[15] = x + width;
     *         vertices[16] = y;
     *         vertices[17] = color;
     *         vertices[18] = u2;
     *         vertices[19] = v;
     * </pre>
     * <br>
     * When a custom Font overrides this to handle a Batch with one extra attribute per-vertex, the custom Font should
     * have a 24-item float array and copy data from {@code vertices} to its own 24-item float array, then pass that
     * larger array to {@link Batch#draw(Texture, float[], int, int)}.
     *
     * @param batch a Batch, which should be a SpriteBatch (or a compatible Batch) unless this was overridden
     * @param texture a Texture to draw (part of)
     * @param vertices a 20-item float array organized into 5-float sections per-vertex
     */
    protected void drawVertices(Batch batch, Texture texture, float[] vertices) {
        batch.draw(texture, vertices, 0, 20);
    }
}