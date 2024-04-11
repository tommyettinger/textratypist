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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class BitmapFontSupport {
    public static class JsonFontData extends BitmapFont.BitmapFontData {
        public String path = null;

        public JsonFontData(){
            super();
        }
        public JsonFontData(FileHandle jsonFont){
            super();
            load(jsonFont, false);
        }
        public JsonFontData(FileHandle jsonFont, String imagePath){
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
//            String dfType = atlas.getString("type", "");
//            if("msdf".equals(dfType) || "mtsdf".equals(dfType) || "sdf".equals(dfType) || "psdf".equals(dfType)) {
//                throw new RuntimeException("Distance field fonts cannot be loaded; use a 'standard' font.");
//            }

            float size = atlas.getFloat("size", 16f);

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

                float baseLine = MathUtils.round(-descent);
                descent += padBottom;

                if(path != null)
                    imagePaths = new String[]{jsonFont.sibling(path).path().replaceAll("\\\\", "/")};

//                while (true) {
//                    line = reader.readLine();
//                    if (line == null) break; // EOF
//                    if (line.startsWith("kernings ")) break; // Starting kernings block.
//                    if (line.startsWith("metrics ")) break; // Starting metrics block.
//                    if (!line.startsWith("char ")) continue;
//
//                    BitmapFont.Glyph glyph = new BitmapFont.Glyph();
//
//                    StringTokenizer tokens = new StringTokenizer(line, " =");
//                    tokens.nextToken();
//                    tokens.nextToken();
//                    int ch = Integer.parseInt(tokens.nextToken());
//                    if (ch <= 0)
//                        missingGlyph = glyph;
//                    else if (ch <= Character.MAX_VALUE)
//                        setGlyph(ch, glyph);
//                    else
//                        continue;
//                    glyph.id = ch;
//                    tokens.nextToken();
//                    glyph.srcX = Integer.parseInt(tokens.nextToken());
//                    tokens.nextToken();
//                    glyph.srcY = Integer.parseInt(tokens.nextToken());
//                    tokens.nextToken();
//                    glyph.width = Integer.parseInt(tokens.nextToken());
//                    tokens.nextToken();
//                    glyph.height = Integer.parseInt(tokens.nextToken());
//                    tokens.nextToken();
//                    glyph.xoffset = Integer.parseInt(tokens.nextToken());
//                    tokens.nextToken();
//                    if (flip)
//                        glyph.yoffset = Integer.parseInt(tokens.nextToken());
//                    else
//                        glyph.yoffset = -(glyph.height + Integer.parseInt(tokens.nextToken()));
//                    tokens.nextToken();
//                    glyph.xadvance = Integer.parseInt(tokens.nextToken());
//
//                    // Check for page safely, it could be omitted or invalid.
//                    if (tokens.hasMoreTokens()) tokens.nextToken();
//                    if (tokens.hasMoreTokens()) {
//                        try {
//                            glyph.page = Integer.parseInt(tokens.nextToken());
//                        } catch (NumberFormatException ignored) {
//                        }
//                    }
//
//                    if (glyph.width > 0 && glyph.height > 0) descent = Math.min(baseLine + glyph.yoffset, descent);
//                }

//                while (true) {
//                    line = reader.readLine();
//                    if (line == null) break;
//                    if (!line.startsWith("kerning ")) break;
//
//                    StringTokenizer tokens = new StringTokenizer(line, " =");
//                    tokens.nextToken();
//                    tokens.nextToken();
//                    int first = Integer.parseInt(tokens.nextToken());
//                    tokens.nextToken();
//                    int second = Integer.parseInt(tokens.nextToken());
//                    if (first < 0 || first > Character.MAX_VALUE || second < 0 || second > Character.MAX_VALUE) continue;
//                    BitmapFont.Glyph glyph = getGlyph((char)first);
//                    tokens.nextToken();
//                    int amount = Integer.parseInt(tokens.nextToken());
//                    if (glyph != null) { // Kernings may exist for glyph pairs not contained in the font.
//                        glyph.setKerning(second, amount);
//                    }
//                }

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
                    spaceGlyph.width = (int)(padLeft + spaceGlyph.xadvance + padRight);
                    spaceGlyph.xoffset = (int)-padLeft;
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
