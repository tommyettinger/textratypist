/*
 * Copyright (c) 2024 See AUTHORS file.
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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;

import static com.badlogic.gdx.math.MathUtils.round;

/**
 * A utility class for loading {@link BitmapFont} instances from Structured JSON files (which use .json or .dat).
 * {@link Font} instances can already be loaded using
 * {@link Font#Font(FileHandle, TextureRegion, boolean) some of the constructors there}.
 */
public class BitmapFontSupport {

    /**
     * Creates a BitmapFont by loading it from a Structured JSON Font, which is typically a .json file produced by
     * <a href="https://github.com/tommyettinger/fontwriter">FontWriter</a> or a related tool. This overload takes
     * a TextureRegion for the image the JSON needs; this region is often part of an atlas.
     * @param jsonFont a FileHandle with the path to a Structured JSON Font (typically a .json file)
     * @param region a TextureRegion, often part of a shared atlas, holding the image the JSON needs
     * @return a new BitmapFont loaded from {@code jsonFont}
     */
    public static BitmapFont loadStructuredJson(FileHandle jsonFont, TextureRegion region) {
        JsonFontData data = new JsonFontData(jsonFont);
        return new BitmapFont(data, region, false);
    }

    /**
     * Creates a BitmapFont by loading it from a Structured JSON Font, which is typically a .json file produced by
     * <a href="https://github.com/tommyettinger/fontwriter">FontWriter</a> or a related tool. This overload takes
     * a TextureRegion for the image the JSON needs; this region is often part of an atlas.
     * @param jsonFont a FileHandle with the path to a Structured JSON Font (typically a .json file)
     * @param region a TextureRegion, often part of a shared atlas, holding the image the JSON needs
     * @param flip true if this BitmapFont has been flipped for use with a y-down coordinate system
     * @return a new BitmapFont loaded from {@code jsonFont}
     */
    public static BitmapFont loadStructuredJson(FileHandle jsonFont, TextureRegion region, boolean flip) {
        JsonFontData data = new JsonFontData(jsonFont, null, flip);
        return new BitmapFont(data, region, false);
    }

    /**
     * Creates a BitmapFont by loading it from a Structured JSON Font, which is typically a .json file produced by
     * <a href="https://github.com/tommyettinger/fontwriter">FontWriter</a> or a related tool. This overload takes
     * a relative path (from {@code jsonFont}) to the necessary image file, with the path as a String.
     * @param jsonFont a FileHandle with the path to a Structured JSON Font (typically a .json file)
     * @param imagePath a String holding the relative path from {@code jsonFont} to the image file the JSON needs
     * @return a new BitmapFont loaded from {@code jsonFont}
     */
    public static BitmapFont loadStructuredJson(FileHandle jsonFont, String imagePath) {
        JsonFontData data = new JsonFontData(jsonFont, imagePath);
        return new BitmapFont(data, (TextureRegion) null, false);
    }

    /**
     * Creates a BitmapFont by loading it from a Structured JSON Font, which is typically a .json file produced by
     * <a href="https://github.com/tommyettinger/fontwriter">FontWriter</a> or a related tool. This overload takes
     * a relative path (from {@code jsonFont}) to the necessary image file, with the path as a String.
     * @param jsonFont a FileHandle with the path to a Structured JSON Font (typically a .json file)
     * @param imagePath a String holding the relative path from {@code jsonFont} to the image file the JSON needs
     * @param flip true if this BitmapFont has been flipped for use with a y-down coordinate system
     * @return a new BitmapFont loaded from {@code jsonFont}
     */
    public static BitmapFont loadStructuredJson(FileHandle jsonFont, String imagePath, boolean flip) {
        JsonFontData data = new JsonFontData(jsonFont, imagePath, flip);
        return new BitmapFont(data, (TextureRegion) null, false);
    }

    /**
     * Mainly for internal use; allows loading BitmapFontData from a Structured JSON Font instead of a .fnt file.
     */
    public static class JsonFontData extends BitmapFont.BitmapFontData {
        public String path = null;

        public JsonFontData() {
            super();
        }

        public JsonFontData(FileHandle jsonFont) {
            this(jsonFont, null);
        }

        public JsonFontData(FileHandle jsonFont, String imagePath) {
            this(jsonFont, imagePath, false);
        }

        public JsonFontData(FileHandle jsonFont, String imagePath, boolean flip) {
            super();
            path = imagePath;
            this.flipped = flip;
            load(jsonFont, flip);
        }

        @Override
        public void load(FileHandle jsonFont, boolean flip) {
            if (imagePaths != null) throw new IllegalStateException("Already loaded.");
            try {
                name = jsonFont.nameWithoutExtension();

                JsonValue fnt;
                JsonReader reader = new JsonReader();
                if (jsonFont.exists()) {
                    if("json".equalsIgnoreCase(jsonFont.extension())) {
                        fnt = reader.parse(jsonFont);
                    } else if("dat".equalsIgnoreCase(jsonFont.extension())) {
                        fnt = reader.parse(decompressFromBytes(jsonFont.readBytes()));
                    } else {
                        throw new RuntimeException("Not a .json or .dat font file: " + jsonFont);
                    }
                } else {
                    throw new RuntimeException("Missing font file: " + jsonFont);
                }
                if (fnt.isEmpty()) throw new GdxRuntimeException("File is empty.");

                JsonValue atlas = fnt.get("atlas");
//                String dfType = atlas.getString("type", "");
//                if("msdf".equals(dfType) || "mtsdf".equals(dfType) || "sdf".equals(dfType) || "psdf".equals(dfType)) {
//                    throw new RuntimeException("Distance field fonts cannot be loaded; use a 'standard' font.");
//                }

                float size = atlas.getFloat("size", 16f);
                int width = atlas.getInt("width", 2048);
                int height = atlas.getInt("height", 2048);

                padTop = 1;
                padRight = 1;
                padBottom = 1;
                padLeft = 1;
                float padY = padTop + padBottom;

                JsonValue metrics = fnt.get("metrics");

                size *= metrics.getFloat("emSize", 1f);
                lineHeight = size * atlas.getFloat("lineHeight", 1f);
//                ascent = atlas.getFloat("ascender", 0.8f);
                descent = size * atlas.getFloat("descender", -0.25f);

                descent += padBottom;

                if (path != null)
                    imagePaths = new String[]{jsonFont.sibling(path).path().replaceAll("\\\\", "/")};

                JsonValue glyphs = fnt.get("glyphs"), planeBounds, atlasBounds;
                int count = glyphs.size;

                for (JsonValue.JsonIterator it = glyphs.iterator(); it.hasNext(); ) {
                    JsonValue current = it.next();
                    BitmapFont.Glyph glyph = new BitmapFont.Glyph();
                    int ch = current.getInt("unicode", -1);
                    if (ch <= 0)
                        missingGlyph = glyph;
                    else if (ch <= Character.MAX_VALUE)
                        setGlyph(ch, glyph);
                    else
                        continue;
                    glyph.id = ch;
                    glyph.xadvance = round(current.getFloat("advance", 1f) * size);
                    planeBounds = current.get("planeBounds");
                    atlasBounds = current.get("atlasBounds");
                    if (atlasBounds != null) {
                        float x, y;
                        glyph.srcX = (int) (x = atlasBounds.getFloat("left", 0f));
                        glyph.width = (int) (atlasBounds.getFloat("right", 0f) - x);
                        glyph.srcY = (int) (y = height - atlasBounds.getFloat("top", 0f));
                        glyph.height = (int) (height - atlasBounds.getFloat("bottom", 0f) - y);
                    } else {
                        glyph.srcX = glyph.srcY = glyph.width = glyph.height = 0;
                    }
                    if (planeBounds != null) {
                        glyph.xoffset = round(planeBounds.getFloat("left", 0f) * size);
                        glyph.yoffset = flip
                                ? round(-size - planeBounds.getFloat("top", 0f) * size)
                                :  round(size + planeBounds.getFloat("bottom", 0f) * size);
                    } else {
                        glyph.xoffset = glyph.yoffset = 0;
                    }
                }

                JsonValue kern = fnt.get("kerning");
                if (kern != null && !kern.isEmpty()) {
                    for (JsonValue.JsonIterator it = kern.iterator(); it.hasNext(); ) {
                        JsonValue current = it.next();
                        int first = current.getInt("unicode1", -1);
                        int second = current.getInt("unicode2", -1);
                        if (first < 0 || first > Character.MAX_VALUE || second < 0 || second > Character.MAX_VALUE)
                            continue;
                        BitmapFont.Glyph glyph = getGlyph((char) first);
                        float amount = current.getFloat("advance", 0f);
                        if (glyph != null)
                            glyph.setKerning(second, round(amount));
                    }
                }

                BitmapFont.Glyph spaceGlyph = getGlyph(' ');
                if (spaceGlyph == null) {
                    spaceGlyph = new BitmapFont.Glyph();
                    spaceGlyph.id = ' ';
                    BitmapFont.Glyph xadvanceGlyph = getGlyph('l');
                    if (xadvanceGlyph == null) xadvanceGlyph = getFirstGlyph();
                    spaceGlyph.xadvance = xadvanceGlyph.xadvance;
                    setGlyph(' ', spaceGlyph);
                }
                if (spaceGlyph.width == 0) {
                    spaceGlyph.width = (int) (padLeft + spaceGlyph.xadvance + padRight);
                    spaceGlyph.xoffset = (int) -padLeft;
                }
                spaceXadvance = spaceGlyph.xadvance;

                BitmapFont.Glyph xGlyph = null;
                for (char xChar : xChars) {
                    xGlyph = getGlyph(xChar);
                    if (xGlyph != null) break;
                }
                if (xGlyph == null) xGlyph = getFirstGlyph();
                xHeight = xGlyph.height - padY;

                BitmapFont.Glyph capGlyph = null;
                for (char capChar : capChars) {
                    capGlyph = getGlyph(capChar);
                    if (capGlyph != null) break;
                }
                if (capGlyph == null) {
                    for (BitmapFont.Glyph[] page : this.glyphs) {
                        if (page == null) continue;
                        for (BitmapFont.Glyph glyph : page) {
                            if (glyph == null || glyph.height == 0 || glyph.width == 0) continue;
                            capHeight = Math.max(capHeight, glyph.height);
                        }
                    }
                } else
                    capHeight = capGlyph.height;
                capHeight -= padY;

                ascent = lineHeight - capHeight;
                down = -lineHeight;
                if (flip) {
                    ascent = -ascent;
                    down = -down;
                }

            } catch (Exception ex) {
                throw new GdxRuntimeException("Error loading font file: " + jsonFont, ex);
            }
        }
    }
    /**
     * Decompresses a byte array compressed with LZB, getting the original
     * String back that was given to a compression method.
     * <br>
     * This is private because a preferred version is present in
     * {@code com.github.tommyettinger.textra.utils.LZBDecompression}; this method is only present here to
     * make copying this class easier on its own.
     * @param compressedBytes a byte array compressed with LZB
     * @return the String that was originally given to be compressed
     */
    private static String decompressFromBytes(byte[] compressedBytes) {
        if(compressedBytes == null)
            return null;
        final int length = compressedBytes.length;
        if(length == 0)
            return "";
        final int resetValue = 128;
        ArrayList<String> dictionary = new ArrayList<>(256);
        int enlargeIn = 4, dictSize = 4, numBits = 3, position = resetValue, index = 1, resb, maxpower, power;
        String entry, w, c;
        StringBuilder res = new StringBuilder(length);
        char bits;
        int val = compressedBytes[0];

        for (char i = 0; i < 3; i++) {
            dictionary.add(String.valueOf(i));
        }

        bits = 0;
        maxpower = 2;
        power = 0;
        while (power != maxpower) {
            resb = val & position;
            position >>>= 1;
            if (position == 0) {
                position = resetValue;
                val = compressedBytes[index++];
            }
            bits |= (resb != 0 ? 1 : 0) << power++;
        }

        switch (bits) {
            case 0:
                bits = 0;
                maxpower = 8;
                power = 0;
                while (power != maxpower) {
                    resb = val & position;
                    position >>>= 1;
                    if (position == 0) {
                        position = resetValue;
                        val = compressedBytes[index++];
                    }
                    bits |= (resb != 0 ? 1 : 0) << power++;
                }
                c = String.valueOf(bits);
                break;
            case 1:
                bits = 0;
                maxpower = 16;
                power = 0;
                while (power != maxpower) {
                    resb = val & position;
                    position >>>= 1;
                    if (position == 0) {
                        position = resetValue;
                        val = compressedBytes[index++];
                    }
                    bits |= (resb != 0 ? 1 : 0) << power++;
                }
                c = String.valueOf(bits);
                break;
            default:
                return "";
        }
        dictionary.add(c);
        w = c;
        res.append(w);
        while (true) {
            if (index > length) {
                return "";
            }
            int cc = 0;
            maxpower = numBits;
            power = 0;
            while (power != maxpower) {
                resb = val & position;
                position >>>= 1;
                if (position == 0) {
                    position = resetValue;
                    val = compressedBytes[index++];
                }
                cc |= (resb != 0 ? 1 : 0) << power++;
            }
            switch (cc) {
                case 0:
                    bits = 0;
                    maxpower = 8;
                    power = 0;
                    while (power != maxpower) {
                        resb = val & position;
                        position >>>= 1;
                        if (position == 0) {
                            position = resetValue;
                            val = compressedBytes[index++];
                        }
                        bits |= (resb != 0 ? 1 : 0) << power++;
                    }

                    dictionary.add(String.valueOf(bits));
                    cc = dictSize++;
                    enlargeIn--;
                    break;
                case 1:
                    bits = 0;
                    maxpower = 16;
                    power = 0;
                    while (power != maxpower) {
                        resb = val & position;
                        position >>>= 1;
                        if (position == 0) {
                            position = resetValue;
                            val = compressedBytes[index++];
                        }
                        bits |= (resb != 0 ? 1 : 0) << power++;
                    }
                    dictionary.add(String.valueOf(bits));
                    cc = dictSize++;
                    enlargeIn--;
                    break;
                case 2:
                    return res.toString();
            }

            if (enlargeIn == 0) {
                enlargeIn = 1 << numBits;
                numBits++;
            }

            if (cc < dictionary.size() && dictionary.get(cc) != null) {
                entry = dictionary.get(cc);
            } else {
                if (cc == dictSize) {
                    entry = w + w.charAt(0);
                } else {
                    return "";
                }
            }
            res.append(entry);

            // Add w+entry[0] to the dictionary.
            dictionary.add(w + entry.charAt(0));
            dictSize++;
            enlargeIn--;

            w = entry;

            if (enlargeIn == 0) {
                enlargeIn = 1 << numBits;
                numBits++;
            }

        }
    }

}
