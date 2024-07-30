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
import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.TypingLabel;

/**
 * Makes the text jumps and falls as if there was gravity.
 * <br>
 * Parameters: {@code jumpHeight;frequency;speed;duration}
 * <br>
 * The {@code jumpHeight} is how many line-heights each glyph should move up (and back down) by; defaults to 1.0 .
 * The {@code frequency} is how often the glyphs should jump, in a wave; defaults to 1.0 .
 * The {@code speed} is how quickly each jumping glyph should move; defaults to 1.0 .
 * The {@code duration} is how many seconds the jumping should go on, or {@code _} to repeat forever; defaults to
 * positive infinity.
 * <br>
 * Example usage:
 * <code>
 * {JUMP=0.5;1.5;0.8;_}Each glyph here will jump a little and with slower movement, but more often; the jumps will go on forever.{ENDJUMP}
 * {JUMP=2.5;0.25;1.0;5}Each glyph here will jump very high, infrequently, at normal speed, for 5 seconds total.{ENDJUMP}
 * </code>
 */
public class JumpEffect extends Effect {
    private static final float DEFAULT_FREQUENCY = 50f;
    private static final float DEFAULT_JUMP_HEIGHT = 1.33f;
    private static final float DEFAULT_SPEED = 1f;

    private float jumpHeight = 1; // How much of their height they should move
    private float frequency = 1; // How frequently the wave pattern repeats
    private float speed = 1; // How fast the glyphs should move

    public JumpEffect(TypingLabel label, String[] params) {
        super(label);

        // Jump Height
        if (params.length > 0) {
            this.jumpHeight = paramAsFloat(params[0], 1);
        }

        // Frequency
        if (params.length > 1) {
            this.frequency = paramAsFloat(params[1], 1);
        }

        // Speed
        if (params.length > 2) {
            this.speed = paramAsFloat(params[2], 1);
        }

        // Duration
        if (params.length > 3) {
            this.duration = paramAsFloat(params[3], Float.POSITIVE_INFINITY);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate progress
        float progressModifier = (1f / speed) * DEFAULT_SPEED;
        float normalFrequency = (1f / frequency) * DEFAULT_FREQUENCY;
        float progressOffset = localIndex / normalFrequency;
        float progress = calculateProgress(progressModifier, -progressOffset, false);

        // Calculate offset
        float interpolation;
        float split = 0.2f;
        if (progress < split) {
            interpolation = Interpolation.pow2Out.apply(0, 1, progress / split);
        } else {
            interpolation = Interpolation.bounceOut.apply(1, 0, (progress - split) / (1f - split));
        }
        float y = label.getLineHeight(globalIndex) * jumpHeight * interpolation * DEFAULT_JUMP_HEIGHT;

        // Calculate fadeout
        float fadeout = calculateFadeout();
        y *= fadeout;

        // Apply changes
        label.offsets.incr(globalIndex << 1 | 1, y);
    }

}
