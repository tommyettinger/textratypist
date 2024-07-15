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

package com.github.tommyettinger.textra.squidglyph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.textra.ColorLookup;
import com.github.tommyettinger.textra.Font;

public class GlyphActor extends Actor {

    public long glyph;
    public Font font;

    public GlyphActor() {
        this(0xFFFFFFFE00000000L | '@', null);
    }

    public GlyphActor(long g, Font f) {
        glyph = g;
        font = f;
    }

    public GlyphActor(char c, Font f) {
        glyph = 0xFFFFFFFE00000000L | c;
        font = f;
    }

    public GlyphActor(char c, int color, Font f) {
        glyph = ((long) color << 32 & 0xFFFFFFFE00000000L) | c;
        font = f;
    }

    public GlyphActor(char c, String markup, Font f) {
        glyph = Font.markupGlyph(c, markup, ColorLookup.DESCRIPTIVE, f.family);
        font = f;
    }
    public GlyphActor(String markup, Font f) {
        glyph = f.markupGlyph(markup);
        font = f;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.getColor().set((int)(glyph >>> 32)).a *= parentAlpha;
        batch.setColor(batch.getColor());
        font.enableShader(batch);
        final Viewport viewport = getStage().getViewport();
        font.resizeDistanceField(viewport.getScreenWidth(), viewport.getScreenHeight(), viewport);
        font.drawGlyph(batch, glyph, getX()
//                + font.cellWidth * 0.5f
                , getY() - font.descent * font.scaleY * 2f
                , getRotation(), getScaleX(), getScaleY());//((char)glyph >= 0xE000 && (char)glyph < 0xF800 ? font.originalCellHeight * 0.5f : 0f)
    }

    public void setColor(int color) {
        super.getColor().set(color);
        super.setColor(super.getColor());
        glyph = (glyph & 0xFFFFFFFFL) | ((long) color << 32 & 0xFFFFFFFE00000000L);
    }

    @Override
    public void setColor(Color color) {
        super.setColor(color);
        glyph = (glyph & 0xFFFFFFFFL) | ((long) Color.rgba8888(color) << 32 & 0xFFFFFFFE00000000L);
    }

    @Override
    public void setColor(float r, float g, float b, float a) {
        super.setColor(r, g, b, a);
        glyph = (glyph & 0xFFFFFFFFL) | ((long) Color.rgba8888(super.getColor()) << 32 & 0xFFFFFFFE00000000L);
    }

    @Override
    public Color getColor() {
        Color.rgba8888ToColor(super.getColor(), (int) (glyph >>> 32));
        return super.getColor();
    }

    public char getChar() {
        return (char) glyph;
    }

    public void setChar(char c) {
        glyph = (glyph & 0xFFFFFFFFFFFF0000L) | c;
    }

    public void setLocation(GridPoint2 location) {
        setPosition(location.x, location.y);
    }
}
