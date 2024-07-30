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
 * Tints the text in a gradient pattern; never ends.
 * <br>
 * Parameters: {@code color1;color2;distance;frequency}
 * <br>
 * The {@code color1} can be a named color or hex color; defaults to {@code #FFFFFFFF} (white).
 * The {@code color2} should be a named color or hex color; defaults to {@code #888888FF} (medium gray).
 * The {@code distance} rarely needs to be changed from 1, but it affects how much the position of the glyph in the
 * affected text changes the effect.
 * The {@code frequency} affects how fast the effect should change; defaults to 1.0 .
 * <br>
 * Example usage:
 * <code>
 * {GRADIENT=RED;LIGHT BLUE;1.0;0.3}This text will blend slowly between from red to light blue.{ENDGRADIENT}
 * {GRADIENT=#111111FF;#EEEEEEFF;1.0;3.0}This text will blend quickly between from dark gray and light gray.{ENDGRADIENT}
 * </code>
 */
public class GradientEffect extends Effect {
    private static final float DEFAULT_DISTANCE = 0.975f;
    private static final float DEFAULT_FREQUENCY = 2f;

    private int color1 = 0xFFFFFFFF; // First color of the gradient, RGBA8888.
    private int color2 = 0x888888FF; // Second color of the gradient, RGBA8888.
    private float distance = 1; // How extensive the gradient effect should be.
    private float frequency = 1; // How frequently the color pattern should move through the text.

    public GradientEffect(TypingLabel label, String[] params) {
        super(label);

        // Color 1
        if (params.length > 0) {
            int c = paramAsColor(params[0]);
            if (c != 256) this.color1 = c;
        }

        // Color 2
        if (params.length > 1) {
            int c = paramAsColor(params[1]);
            if (c != 256) this.color2 = c;
        }

        // Distance
        if (params.length > 2) {
            this.distance = paramAsFloat(params[2], 1);
        }

        // Frequency
        if (params.length > 3) {
            this.frequency = paramAsFloat(params[3], 1);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate progress
        float distanceMod = (1f / distance) * (1f - DEFAULT_DISTANCE);
        float frequencyMod = (1f / frequency) * DEFAULT_FREQUENCY;
        float progress = calculateProgress(frequencyMod, distanceMod * localIndex, true);

        // Calculate color
        label.setInWorkingLayout(globalIndex,
                (glyph & 0xFFFFFFFFL) | (long) ColorUtils.lerpColors(this.color1, this.color2, progress) << 32);
    }

}
