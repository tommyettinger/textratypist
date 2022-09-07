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

import com.badlogic.gdx.math.MathUtils;
import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.TypingLabel;

/**
 * Makes the text jumps and falls as if there was gravity.
 */
public class CarouselEffect extends Effect {
    private static final float DEFAULT_FREQUENCY = 0.5f;

    private float frequency = 1; // How frequently the wave pattern repeats

    public CarouselEffect(TypingLabel label, String[] params) {
        super(label);

        // Frequency
        if (params.length > 0) {
            this.frequency = paramAsFloat(params[0], 1.0f);
        }

        // Duration
        if (params.length > 1) {
            this.duration = paramAsFloat(params[1], -1);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate progress
        float progress = totalTime * frequency * 360.0f * DEFAULT_FREQUENCY;

        float s = MathUtils.sinDeg(progress);

        // Calculate fadeout
        float fadeout = calculateFadeout();
        s *= fadeout;

        Font font = label.getFont();

        // Apply changes
        label.sizing.incr(globalIndex << 1, s - 1.0f);
        label.offsets.incr(globalIndex << 1, font.mapping.get((char) glyph, font.defaultValue).xAdvance * (0.125f * s));
    }

}
