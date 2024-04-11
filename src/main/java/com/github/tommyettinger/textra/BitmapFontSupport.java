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
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import static com.badlogic.gdx.math.MathUtils.round;

public class BitmapFontSupport {
    public static class JsonFontData extends BitmapFont.BitmapFontData {
        public String path = null;

        public JsonFontData() {
            super();
        }

        public JsonFontData(FileHandle jsonFont) {
            super();
            load(jsonFont, false);
        }

        public JsonFontData(FileHandle jsonFont, String imagePath) {
            super();
            path = imagePath;
            load(jsonFont, false);
        }

        @Override
        public void load(FileHandle jsonFont, boolean flip) {
            if (imagePaths != null) throw new IllegalStateException("Already loaded.");
            try {
                name = jsonFont.nameWithoutExtension();


                JsonValue fnt;
                JsonReader reader = new JsonReader();
                if (jsonFont.exists()) {
                    fnt = reader.parse(jsonFont);
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

                float baseLine = round(-descent);
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
                    float x, y, w, h, xo, yo;
                    if (atlasBounds != null) {
                        glyph.srcX = (int) (x = atlasBounds.getFloat("left", 0f));
                        glyph.width = (int) (w = atlasBounds.getFloat("right", 0f) - x);
                        glyph.srcY = (int) (y = height - atlasBounds.getFloat("top", 0f));
                        glyph.height = (int) (h = height - atlasBounds.getFloat("bottom", 0f) - y);
                    } else {
                        x = y = w = h = 0f;
                    }
                    if (planeBounds != null) {
                        glyph.xoffset = round(planeBounds.getFloat("left", 0f) * size);
                        glyph.yoffset = round(size - planeBounds.getFloat("top", 0f) * size);
                    } else {
                        xo = yo = 0f;
                    }

//                    if (glyph.width > 0 && glyph.height > 0) descent = Math.min(baseLine + glyph.yoffset, descent);
                }

                JsonValue kern = fnt.get("kerning");
                if (kern != null && !kern.isEmpty()) {
                    for (JsonValue.JsonIterator it = kern.iterator(); it.hasNext(); ) {
                        JsonValue current = it.next();
                        int first = current.getInt("unicode1", 65535);
                        int second = current.getInt("unicode2", 65535);
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

                ascent = baseLine - capHeight;
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
}
