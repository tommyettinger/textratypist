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
 * changed to other colors.
 */
public class OceanEffect extends Effect {
    private static final float DEFAULT_DISTANCE = 0.975f;
    private static final float DEFAULT_FREQUENCY = 2f;

    private float distance = 1; // How extensive the rainbow effect should be.
    private float frequency = 0.25f; // How frequently the color pattern should move through the text.
    private float hue = 0.5f; // Color hue; this is the middle of the hue range, and it can go up or down up to 0.15 out of 1.0
    private float saturation = 0.8f; // Color saturation
    private float brightness = 0.25f; // Color brightness; this is the middle of the brightness range, and it can go up or down up to 0.15 out of 1.0

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

        // Brightness
        if (params.length > 4) {
            this.brightness = paramAsFloat(params[4], 0.25f);
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
                        0.15f - Math.abs(NoiseUtils.noise1D(progress * 3f + progress * progress, -123456789)) * 0.3f + brightness, 1f) << 32);
    }

}
