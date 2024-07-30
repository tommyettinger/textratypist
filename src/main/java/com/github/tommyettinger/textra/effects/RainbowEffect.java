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
 * Tints the text in a rainbow pattern.
 * <br>
 * Parameters: {@code distance;frequency;saturation;lightness}
 * <br>
 * The {@code distance} rarely needs to be changed from 1, but it affects how much the position of the mouse in the
 * affected text changes the effect.
 * The {@code frequency} makes the effect faster when higher than 1, or slower when lower than 1; defaults to 1.0 .
 * The {@code saturation} affects the rainbow's "colorful-ness", with 1 making it maximally colorful and 0 making it
 * grayscale; defaults to 1.0 .
 * The {@code lightness} affects how light the rainbow will be, with 0.5 the default, 1 being all white, and 0 being all
 * black. Colors can appear the most saturated when lightness is 0.5.
 * <br>
 * Example usage:
 * <code>
 * {RAINBOW=1;1;1;0.5}This span of text will use a vividly-saturated rainbow.{ENDRAINBOW}
 * {RAINBOW=1;0.4;0.6;0.7}This span of text will use a slower-changing pastel rainbow.{ENDRAINBOW}
 * </code>
 */
public class RainbowEffect extends Effect {
    private static final float DEFAULT_DISTANCE = 0.975f;
    private static final float DEFAULT_FREQUENCY = 2f;

    private float distance = 1; // How extensive the rainbow effect should be.
    private float frequency = 1; // How frequently the color pattern should move through the text.
    private float saturation = 1; // Color saturation
    private float lightness = 0.5f; // Color lightness

    public RainbowEffect(TypingLabel label, String[] params) {
        super(label);

        // Distance
        if (params.length > 0) {
            this.distance = paramAsFloat(params[0], 1);
        }

        // Frequency
        if (params.length > 1) {
            this.frequency = paramAsFloat(params[1], 1);
        }

        // Saturation
        if (params.length > 2) {
            this.saturation = paramAsFloat(params[2], 1);
        }

        // Lightness
        if (params.length > 3) {
            this.lightness = paramAsFloat(params[3], 0.5f);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate progress
        float distanceMod = (1f / distance) * (1f - DEFAULT_DISTANCE);
        float frequencyMod = (1f / frequency) * DEFAULT_FREQUENCY;
        float progress = calculateProgress(frequencyMod, distanceMod * localIndex, false);

        label.setInWorkingLayout(globalIndex, (glyph & 0xFFFFFFFFL) | (long) ColorUtils.hsl2rgb(progress, saturation, lightness, 1f) << 32);
    }

}
