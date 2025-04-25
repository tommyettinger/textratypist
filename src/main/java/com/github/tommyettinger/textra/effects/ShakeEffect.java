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

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.FloatArray;
import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.TypingLabel;

/**
 * Shakes the text in a random pattern.
 * <br>
 * Parameters: {@code distance;speed;duration}
 * <br>
 * The {@code distance} is how many line-heights each glyph should move at most in any direction; defaults to 1.0 .
 * The {@code speed} is how quickly each glyph should move; defaults to 1.0 .
 * The {@code duration} is how many seconds the shaking should go on, or {@code _} to repeat forever; defaults to
 * positive infinity.
 * <br>
 * Example usage:
 * <code>
 * {SHAKE=0.5;0.8;_}Each glyph here will shake a little and with slower movement; the shaking will go on forever.{ENDSHAKE}
 * {SHAKE=2.5;1.0;5}Each glyph here will shake a lot, at normal speed, for 5 seconds total.{ENDSHAKE}
 * </code>
 */
public class ShakeEffect extends Effect {
    private static final float DEFAULT_DISTANCE = 0.12f;
    private static final float DEFAULT_SPEED = 0.5f;

    private final FloatArray lastOffsets = new FloatArray();

    private float distance = 1; // How far the glyphs should move
    private float speed = 1; // How fast the glyphs should move

    public ShakeEffect(TypingLabel label, String[] params) {
        super(label);

        // Distance
        if (params.length > 0) {
            this.distance = paramAsFloat(params[0], 1);
        }

        // Speed
        if (params.length > 1) {
            this.speed = paramAsFloat(params[1], 1);
        }

        // Duration
        if (params.length > 2) {
            this.duration = paramAsFloat(params[2], Float.POSITIVE_INFINITY);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Make sure we can hold enough entries for the current index
        if (localIndex >= lastOffsets.size / 2) {
            lastOffsets.setSize(lastOffsets.size + 16);
        }

        // Get last offsets
        float lastX = lastOffsets.get(localIndex * 2);
        float lastY = lastOffsets.get(localIndex * 2 + 1);

        // Calculate new offsets
        float x = label.getLineHeight(globalIndex) * distance * MathUtils.random(-1f, 1f) * DEFAULT_DISTANCE;
        float y = label.getLineHeight(globalIndex) * distance * MathUtils.random(-1f, 1f) * DEFAULT_DISTANCE;

        // Apply speed
        float normalSpeed = MathUtils.clamp(speed * DEFAULT_SPEED, 0, 1);
        x = Interpolation.linear.apply(lastX, x, normalSpeed);
        y = Interpolation.linear.apply(lastY, y, normalSpeed);

        // Apply fadeout
        float fadeout = calculateFadeout();
        x *= fadeout;
        y *= fadeout;
        x = MathUtils.round(x);
        y = MathUtils.round(y);

        // Store offsets for the next tick
        lastOffsets.set(localIndex * 2, x);
        lastOffsets.set(localIndex * 2 + 1, y);

        // Apply changes
        label.getOffsets().incr(globalIndex << 1, x);
        label.getOffsets().incr(globalIndex << 1 | 1, y);
    }

}
