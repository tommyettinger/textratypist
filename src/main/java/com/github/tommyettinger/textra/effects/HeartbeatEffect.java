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

/**
 * Makes the text jumps and falls as if there was gravity.
 */
public class HeartbeatEffect extends Effect {
    private static final float DEFAULT_FREQUENCY = 1f;
    private static final float DEFAULT_DISTANCE = 0.5f;

    private float distance = 1; // How much of their height they should move
    private float frequency = 1; // How frequently the wave pattern repeats

    public HeartbeatEffect(TypingLabel label, String[] params) {
        super(label);

        // Distance
        if (params.length > 0) {
            this.distance = paramAsFloat(params[0], 1);
        }

        // Frequency
        if (params.length > 1) {
            this.frequency = paramAsFloat(params[1], 1);
        }

        // Duration
        if (params.length > 2) {
            this.duration = paramAsFloat(params[2], Float.POSITIVE_INFINITY);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate progress
        float progress = totalTime * frequency * 360.0f * DEFAULT_FREQUENCY;

        float c = MathUtils.cosDeg(progress), s = MathUtils.sinDeg(progress);
        float x = distance * Math.max(-0.125f, Math.max(c * c * c, s * s * s)) * DEFAULT_DISTANCE;
//        float y = distance * Math.max(-0.125f, s * s * s) * DEFAULT_DISTANCE;

        // Calculate fadeout
        float fadeout = calculateFadeout();
        x *= fadeout;
//        y *= fadeout;

        // Apply changes
        label.sizing.incr(globalIndex << 1, x);
        label.sizing.incr(globalIndex << 1 | 1, x);
//        float lineHeight = label.getLineHeight(globalIndex);
//        label.offsets.incr(globalIndex << 1, label.font.mapping.get((char)glyph, label.font.defaultValue).xAdvance * (-0.25f * x));
//        label.offsets.incr(globalIndex << 1 | 1, lineHeight * (-0.5f * x));
    }

}
