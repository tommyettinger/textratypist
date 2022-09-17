/*
 * Copyright (c) 2021-2022 See AUTHORS file.
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
 * Tints the single glyph underneath the pointer/mouse in a rainbow pattern.
 */
public class HighlightEffect extends Effect {
    private static final float DEFAULT_DISTANCE = 0.975f;
    private static final float DEFAULT_FREQUENCY = 2f;
    private static final int DEFAULT_COLOR = -2;

    private int baseColor = DEFAULT_COLOR;
    private float distance = 1; // How extensive the rainbow effect should be.
    private float frequency = 1; // How frequently the color pattern should move through the text.
    private float saturation = 1; // Color saturation
    private float brightness = 0.5f; // Color brightness
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
            this.brightness = paramAsFloat(params[4], 0.5f);
        }

        // All
        if (params.length > 5) {
            this.all = paramAsBoolean(params[5]);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        if(all) {
            if(label.overIndex < indexStart || label.overIndex >= indexEnd) {
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

        label.setInWorkingLayout(globalIndex, (glyph & 0xFFFFFFFFL) | (long) ColorUtils.hsl2rgb(progress, saturation, brightness, 1f) << 32);
    }

}
