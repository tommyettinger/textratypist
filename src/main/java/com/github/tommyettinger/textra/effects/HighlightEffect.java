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

package com.github.tommyettinger.textra.effects;

import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.TypingLabel;
import com.github.tommyettinger.textra.utils.ColorUtils;

/**
 * Tints the single glyph (or affected text, if all is true) underneath the pointer/mouse in a rainbow pattern.
 * <br>
 * Parameters: {@code color;distance;frequency;saturation;lightness;all}
 * <br>
 * The {@code color} can be any named color, potentially with modifiers, or a hex color (optionally starting with #), or
 * "default" to use the existing default color of the label.
 * The {@code distance} rarely needs to be changed from 1, but it affects how much the position of the mouse in the
 * affected text changes the effect.
 * The {@code frequency} makes the effect faster when higher than 1, or slower when lower than 1.
 * The {@code saturation} affects the rainbow's "colorful-ness", with 1 making it maximally colorful and 0 making it
 * grayscale.
 * The {@code lightness} affects how light the rainbow will be, with 0.5 the default, 1 being all white, and 0 being all
 * black. Colors can appear the most saturated when lightness is 0.5.
 * The parameter {@code all} makes the whole span of text become affected when true, or individual glyphs be the only
 * things affected when false.
 * <br>
 * Example usage:
 * <code>
 * {HIGHLIGHT=default;1;1;1;0.5;true}This whole span of text will be highlighted vividly on mouse-over.{ENDHIGHLIGHT}
 * {HIGHLIGHT=default;1;0.4;0.6;0.7;false}Individual glyphs will be highlighted slowly in pastel colors on mouse-over.{ENDHIGHLIGHT}
 * </code>
 */
public class HighlightEffect extends Effect {
    private static final float DEFAULT_DISTANCE = 0.975f;
    private static final float DEFAULT_FREQUENCY = 2f;
    private static final int DEFAULT_COLOR = -2;

    private int baseColor = DEFAULT_COLOR;
    private float distance = 1; // How extensive the rainbow effect should be.
    private float frequency = 1; // How frequently the color pattern should move through the text.
    private float saturation = 1; // Color saturation, as by HSL
    private float lightness = 0.5f; // Color lightness, as by HSL
    private boolean all = false; // Whether this should rainbow-highlight the whole responsive area.

    public HighlightEffect(TypingLabel label, String[] params) {
        super(label);
        label.trackingInput = true;

        // Base color
        if (params.length > 0) {
            this.baseColor = paramAsColor(params[0]);
            if(this.baseColor == 256) this.baseColor = DEFAULT_COLOR;
        }

        // Distance
        if (params.length > 1) {
            this.distance = paramAsFloat(params[1], 1);
        }

        // Frequency
        if (params.length > 2) {
            this.frequency = paramAsFloat(params[2], 1);
        }

        // Saturation
        if (params.length > 3) {
            this.saturation = paramAsFloat(params[3], 1);
        }

        // Brightness
        if (params.length > 4) {
            this.lightness = paramAsFloat(params[4], 0.5f);
        }

        // All
        if (params.length > 5) {
            this.all = paramAsBoolean(params[5]);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        if(all) {
            if(label.overIndex < indexStart || label.overIndex > indexEnd) {
                label.setInWorkingLayout(globalIndex, (glyph & 0xFFFFFFFFL) | (long) baseColor << 32);
                return;
            }
        }
        else {
            if(label.overIndex != globalIndex) {
                label.setInWorkingLayout(globalIndex, (glyph & 0xFFFFFFFFL) | (long) baseColor << 32);
                return;
            }
        }
        // Calculate progress
        float distanceMod = (1f / distance) * (1f - DEFAULT_DISTANCE);
        float frequencyMod = (1f / frequency) * DEFAULT_FREQUENCY;
        float progress = calculateProgress(frequencyMod, distanceMod * localIndex, false);

        label.setInWorkingLayout(globalIndex, (glyph & 0xFFFFFFFFL) | (long) ColorUtils.hsl2rgb(progress, saturation, lightness, 1f) << 32);
    }

}
