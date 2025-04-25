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

import com.badlogic.gdx.utils.TimeUtils;
import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.TypingLabel;
import com.github.tommyettinger.textra.utils.NoiseUtils;

/**
 * Rotates each glyph slightly back and forth, each one independently and randomly based on the current time.
 * <br>
 * Parameters: {@code rotationAmount;speed;duration}
 * <br>
 * The {@code rotationAmount} is how many degrees a glyph is allowed to rotate clockwise or counterclockwise; defaults
 * to 15 degrees.
 * The {@code speed} affects how fast the glyphs should rotate; defaults to 1.0 .
 * The {@code duration} is how many seconds the effect should repeat, or {@code _} to repeat forever; defaults to
 * positive infinity.
 * <br>
 * Example usage:
 * <code>
 * {CROWD=50;0.8;_}Each glyph here will rotate a lot, but slowly, and will do so forever.{ENDCROWD}
 * {CROWD=10;4;5}Each glyph here will rotate a little, but quickly, for 5 seconds total.{ENDCROWD}
 * </code>
 */
public class CrowdEffect extends Effect {
    private static final float DEFAULT_ROTATION_STRENGTH = 1f;
    private static final float DEFAULT_SPEED = 0.001f;

    private float rotationAmount = 15; // How many degrees a glyph can rotate, clockwise or counterclockwise
    private float speed = 1; // How fast the glyphs should move

    public CrowdEffect(TypingLabel label, String[] params) {
        super(label);

        // Rotation Amount
        if (params.length > 0) {
            this.rotationAmount = paramAsFloat(params[0], 15);
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
        // Calculate offset
        float rot = NoiseUtils.octaveNoise1D((TimeUtils.millis() & 0xFFFFFF) * speed * DEFAULT_SPEED + globalIndex * 0.42f, globalIndex) * rotationAmount * DEFAULT_ROTATION_STRENGTH;

        // Calculate fadeout
        float fadeout = calculateFadeout();
        rot *= fadeout;

        // Apply changes
        label.getRotations().incr(globalIndex, rot);
    }

}
