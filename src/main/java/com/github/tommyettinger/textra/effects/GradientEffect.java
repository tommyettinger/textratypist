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

package com.github.tommyettinger.textra.effects;

import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.TypingLabel;
import com.github.tommyettinger.textra.utils.ColorUtils;

/** Tints the text in a gradient pattern. */
public class GradientEffect extends Effect {
    private static final float DEFAULT_DISTANCE  = 0.975f;
    private static final float DEFAULT_FREQUENCY = 2f;

    private int color1      = 0xFFFFFFFF; // First color of the gradient, RGBA8888.
    private int color2      = 0xFFFFFFFF; // Second color of the gradient, RGBA8888.
    private float distance  = 1; // How extensive the rainbow effect should be.
    private float frequency = 1; // How frequently the color pattern should move through the text.

    public GradientEffect(TypingLabel label, String[] params) {
        super(label);

        // Color 1
        if(params.length > 0) {
            Integer c = paramAsColor(params[0]);
            if(c != null) this.color1 = c;
        }

        // Color 2
        if(params.length > 1) {
            Integer c = paramAsColor(params[1]);
            if(c != null) this.color2 = c;
        }

        // Distance
        if(params.length > 2) {
            this.distance = paramAsFloat(params[2], 1);
        }

        // Frequency
        if(params.length > 3) {
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
