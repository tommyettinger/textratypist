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
import com.github.tommyettinger.textra.utils.NoiseUtils;

/**
 * Tints the text in an irregular, flowing color pattern that defaults to using sea green through deep blue, but can be
 * changed to other colors. This can also make a decent fire effect, or various other natural swirling effects.
 * <br>
 * Parameters: {@code distance;frequency;hue;saturation;brightness}
 * <br>
 * The {@code distance} rarely needs to be changed from 1, but it affects how much the position of the glyph in the
 * affected text changes the effect.
 * The {@code frequency} affects how fast the effect should change; defaults to 0.25 .
 * The {@code hue} is the middle hue of the colors this uses; it can go up or down by 0.15, wrapping around at 1.0 . Defaults to 0.5 .
 * The {@code saturation} is the saturation of all colors this will use; doesn't vary. Defaults to 0.8 .
 * The {@code lightness} is the middle lightness of the colors this uses; it can go up or down by 0.15, clamping at 0.0 and 1.0 . Defaults to 0.25 .
 * <br>
 * Example usage:
 * <code>
 * {OCEAN=1;0.25;0.5;0.8;0.25}This text will slowly shift between darker and lighter blue and blue-green.{ENDGRADIENT}
 * {OCEAN=0.7;1.25;0.11;1.0;0.65}This text will burn with the cleansing power of fire!{ENDGRADIENT}
 * </code>
 */
public class OceanEffect extends Effect {
    private static final float DEFAULT_DISTANCE = 0.975f;
    private static final float DEFAULT_FREQUENCY = 2f;

    private float distance = 1; // How extensive the color change effect should be.
    private float frequency = 0.25f; // How frequently the color pattern should move through the text.
    private float hue = 0.5f; // Color hue; this is the middle of the hue range, and it can go up by 0.15 or down by 0.15 at most
    private float saturation = 0.8f; // Color saturation
    private float lightness = 0.25f; // Color lightness as per HSL; this is the middle of the brightness range, and it can go up by 0.15 or down by 0.15 at most

    public OceanEffect(TypingLabel label, String[] params) {
        super(label);

        // Distance
        if (params.length > 0) {
            this.distance = paramAsFloat(params[0], 1);
        }

        // Frequency
        if (params.length > 1) {
            this.frequency = paramAsFloat(params[1], 0.25f);
        }

        // Middle hue
        if (params.length > 2) {
            this.hue = paramAsFloat(params[2], 0.5f);
        }

        // Saturation
        if (params.length > 3) {
            this.saturation = paramAsFloat(params[3], 0.8f);
        }

        // Lightness
        if (params.length > 4) {
            this.lightness = paramAsFloat(params[4], 0.25f);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate progress
        float distanceMod = (1f / distance) * (1f - DEFAULT_DISTANCE);
        float frequencyMod = (1f / frequency) * DEFAULT_FREQUENCY;
        float progress = calculateProgress(frequencyMod, distanceMod * localIndex, false);

        label.setInWorkingLayout(globalIndex, (glyph & 0xFFFFFFFFL) |
                (long) ColorUtils.hsl2rgb(NoiseUtils.octaveNoise1D(progress * 5f, 12345) * 0.15f + hue, saturation,
                        0.15f - Math.abs(NoiseUtils.noise1D(progress * 3f + progress * progress, -123456789)) * 0.3f + lightness, 1f) << 32);
    }

}
