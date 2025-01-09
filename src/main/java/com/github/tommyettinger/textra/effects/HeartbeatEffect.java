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
 * Makes the text expand and shrink like a beating heart.
 * <br>
 * Parameters: {@code expansion;frequency;duration}
 * <br>
 * The {@code expansion} is how much the text should expand by, relative to its original size, at most. Defaults to 1.0 .
 * The {@code frequency} is how quickly the "heart" "beats;" defaults to 1.0, and doesn't use any exact unit.
 * The {@code duration} is how many seconds the beating-heart should go on, or {@code _} to repeat forever; defaults to
 * positive infinity.
 * <br>
 * Example usage:
 * <code>
 * {HEARTBEAT=0.75;0.5;_}Each glyph here will slowly pulse by a smaller amount forever.{ENDHEARTBEAT}
 * {HEARTBEAT=2.5;2;5}Each glyph here will pulse quickly by a large amount for 5 seconds total.{ENDHEARTBEAT}
 * </code>
 */
public class HeartbeatEffect extends Effect {
    private static final float DEFAULT_FREQUENCY = 1f;
    private static final float DEFAULT_EXPANSION = 0.5f;

    private float expansion = 1; // How much of their size they should expand by
    private float frequency = 1; // How frequently the heartbeat repeats

    public HeartbeatEffect(TypingLabel label, String[] params) {
        super(label);

        // Expansion
        if (params.length > 0) {
            this.expansion = paramAsFloat(params[0], 1);
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
        float x = expansion * Math.max(-0.125f, Math.max(c * c * c, s * s * s)) * DEFAULT_EXPANSION;
//        float y = distance * Math.max(-0.125f, s * s * s) * DEFAULT_DISTANCE;

        // Calculate fadeout
        float fadeout = calculateFadeout();
        x *= fadeout;
//        y *= fadeout;

        // Apply changes
        label.sizing.incr(globalIndex << 1, x);
        label.sizing.incr(globalIndex << 1 | 1, x);
        float lineHeight = label.getLineHeight(globalIndex);
//        label.offsets.incr(globalIndex << 1, label.font.mapping.get((char)glyph, label.font.defaultValue).xAdvance * (-0.25f * x));
//        label.offsets.incr(globalIndex << 1 | 1, lineHeight * (-0.5f * x));
        float xOff = lineHeight * -0.25f * x;
        label.offsets.incr(globalIndex << 1, xOff);
        label.offsets.incr(globalIndex << 1 | 1, xOff);
    }

}
