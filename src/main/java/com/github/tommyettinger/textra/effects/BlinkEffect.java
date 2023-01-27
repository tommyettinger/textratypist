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

import com.badlogic.gdx.math.MathUtils;
import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.TypingLabel;
import com.github.tommyettinger.textra.utils.ColorUtils;

/**
 * Blinks the entire text in two different colors at once, without interpolation.
 */
public class BlinkEffect extends Effect {
    private static final float DEFAULT_FREQUENCY = 1f;

    private int color1 = 256; // First color of the effect.
    private int color2 = 256; // Second color of the effect.
    private float alpha1 = 1f; // First alpha of the effect, in case a color isn't provided.
    private float alpha2 = 0f; // Second alpha of the effect, in case a color isn't provided.
    private float frequency = 1; // How frequently the color pattern should move through the text.
    private float threshold = 0.5f; // Point to switch colors.

    public BlinkEffect(TypingLabel label, String[] params) {
        super(label);

        // Color 1 or Alpha 1
        if (params.length > 0) {
            this.color1 = paramAsColor(params[0]);
            if (this.color1 == 256) {
                alpha1 = paramAsFloat(params[0], 0);
            }
        }

        // Color 2 or Alpha 2
        if (params.length > 1) {
            this.color2 = paramAsColor(params[1]);
            if (this.color2 == 256) {
                alpha2 = paramAsFloat(params[1], 1);
            }
        }

        // Frequency
        if (params.length > 2) {
            this.frequency = paramAsFloat(params[2], 1);
        }

        // Threshold
        if (params.length > 3) {
            this.threshold = paramAsFloat(params[3], 0.5f);
        }

        // Validate parameters
        this.threshold = MathUtils.clamp(this.threshold, 0, 1);
        this.alpha1 = MathUtils.clamp(this.alpha1, 0f, 1f);
        this.alpha2 = MathUtils.clamp(this.alpha2, 0f, 1f);
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate progress
        float frequencyMod = (1f / frequency) * DEFAULT_FREQUENCY;
        float progress = calculateProgress(frequencyMod);

        // Calculate and assign color
        if(progress <= threshold){
            if(color1 == 256)
                label.setInWorkingLayout(globalIndex,
                        (glyph & 0xFFFFFF00FFFFFFFFL) | (long) (alpha1 * 255) << 32);
            else
                label.setInWorkingLayout(globalIndex, (glyph & 0xFFFFFFFFL) | (long) color1 << 32);
        }
        else {
            if(color1 == 256)
                label.setInWorkingLayout(globalIndex,
                        (glyph & 0xFFFFFF00FFFFFFFFL) | (long) (alpha2 * 255) << 32);
            else
                label.setInWorkingLayout(globalIndex, (glyph & 0xFFFFFFFFL) | (long) color2 << 32);
        }
    }

}
