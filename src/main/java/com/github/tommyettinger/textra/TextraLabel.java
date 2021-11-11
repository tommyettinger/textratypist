/*
 * Copyright (c) 2021-2021 See AUTHORS file.
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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Pools;

/**
 * A scene2d.ui Widget that displays text using a {@link Font} rather than a libGDX BitmapFont. This supports being
 * laid out in a Table just like the typical Label.
 * <br>
 * ...Maybe not quite ready for prime-time yet. This could use some more features for ease-of-use.
 */
public class TextraLabel extends Widget {
    public Layout layout;
    public Font font;
    public int align = Align.left;
    public boolean wrap = false;
    public TextraLabel(){
        layout = Pools.obtain(Layout.class);
        font = new Font(new BitmapFont(), Font.DistanceFieldType.STANDARD, 0, 0, 0, 0);
    }
    public TextraLabel(String text, Skin skin) {
        this(text, skin.get(Label.LabelStyle.class));
    }

    public TextraLabel(String text, Skin skin, String styleName) {
        this(text, skin.get(styleName, Label.LabelStyle.class));
    }


    public TextraLabel(String text, Label.LabelStyle style) {
        font = new Font(style.font, Font.DistanceFieldType.STANDARD, 0, 0, 0, 0);
        layout = Pools.obtain(Layout.class);
        layout.setBaseColor(style.fontColor);
        font.markup(text, layout);
        setSize(layout.getWidth(), layout.getHeight());
    }
    public TextraLabel(String text, Font font) {
        this.font = font;
        layout = Pools.obtain(Layout.class);
        font.markup(text, layout);
        setSize(layout.getWidth(), layout.getHeight());
    }
    public TextraLabel(String text, Font font, Color color) {
        this.font = font;
        layout = Pools.obtain(Layout.class);
        layout.setBaseColor(color);
        font.markup(text, layout);
        setSize(layout.getWidth(), layout.getHeight());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        boolean resetShader = font.distanceField != Font.DistanceFieldType.STANDARD && batch.getShader() != font.shader;
        if(resetShader)
            font.enableShader(batch);
        batch.setColor(1f, 1f, 1f, parentAlpha);
        font.drawGlyphs(batch, layout, getX(align), getHeight() * 0.5f + getY(align) - font.cellHeight, align);
        if(resetShader)
            batch.setShader(null);
    }

    @Override
    public float getPrefWidth() {
        return wrap ? 0f : layout.getWidth();
    }

    @Override
    public float getPrefHeight() {
        return layout.getHeight() + font.cellHeight * 0.5f;
    }

    public boolean isWrap() {
        return wrap;
    }

    public void setWrap(boolean wrap) {
        if(this.wrap != (this.wrap = wrap))
            invalidateHierarchy();
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
    }

    @Override
    public void layout() {
        float width = getWidth();
        if (wrap && layout.getTargetWidth() != width) {
            layout.setTargetWidth(width);
            invalidateHierarchy();
            font.regenerateLayout(layout);
        }
    }

    /**
     * Gets the alignment for the text in this TextraLabel.
     * This is a constant in {@link Align}.
     * @see Align
     * @return the alignment used by this TextraLabel, as a constant from {@link Align}
     */
    public int getAlignment() {
        return align;
    }

    /**
     * Sets the alignment for the text in this TextraLabel.
     * @see Align
     * @param alignment a constant from {@link Align}
     */
    public void setAlignment (int alignment) {
        align = alignment;
    }

    /**
     * Changes the text in this TextraLabel to the given String, parsing any markup in it.
     * @param markupText a String that can contain Font markup
     */
    public void setText(String markupText) {
        font.markup(markupText, layout.clear());
    }
}
