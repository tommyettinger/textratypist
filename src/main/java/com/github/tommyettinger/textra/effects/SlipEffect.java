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
 * Adds a small value to each glyph's x-offset, each one independently and randomly based on the current time.
 * <br>
 * Parameters: {@code distance;speed;duration}
 * <br>
 * The {@code distance} determines how far a glyph can be offset from its original position, in viewport units;
 * defaults to 5.
 * The {@code speed} affects how fast the glyphs should change position; defaults to 1.0 .
 * The {@code duration} is how many seconds the effect should repeat, or {@code _} to repeat forever; defaults to
 * positive infinity.
 * <br>
 * Example usage:
 * <code>
 * {SLIP=5;0.8;_}Each glyph here will slip a lot, but slowly, and will do so forever.{ENDSLIP}
 * {SLIP=0.25;4;5}Each glyph here will slip a little, but quickly, for 5 seconds total.{ENDSLIP}
 * </code>
 */
public class SlipEffect extends Effect {
    private static final float DEFAULT_DISTANCE = 0.5f;
    private static final float DEFAULT_SPEED = 0.001f;

    private float distance = 5f; // How far a glyph can be offset on x from its original position, in viewport units
    private float speed = 1; // How fast the glyphs should move

    public SlipEffect(TypingLabel label, String[] params) {
        super(label);

        // Expansion Amount
        if (params.length > 0) {
            this.distance = paramAsFloat(params[0], 5f);
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
        float slip = (1f + NoiseUtils.octaveNoise1D((TimeUtils.millis() & 0xFFFFFF) * speed * DEFAULT_SPEED + globalIndex * 0.2357f, 0x12345678)) * distance * DEFAULT_DISTANCE;

        // Calculate fadeout
        float fadeout = calculateFadeout();
        slip *= fadeout;

        // Apply changes
        label.getOffsets().incr(globalIndex << 1, slip);
    }

}
