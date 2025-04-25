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
 * Moves the text vertically in a sine wave pattern.
 * <br>
 * Parameters: {@code distance;frequency;speed;duration}
 * <br>
 * The {@code distance} is how many line-heights each glyph should move up and down by; defaults to 1.0 .
 * The {@code frequency} is how often the glyphs should rise and fall, in a wave; defaults to 1.0 .
 * The {@code speed} is how quickly each glyph should move; defaults to 1.0 .
 * The {@code duration} is how many seconds the wave should go on, or {@code _} to repeat forever; defaults to
 * positive infinity.
 * <br>
 * Example usage:
 * <code>
 * {WAVE=0.5;1.5;0.8;_}Each glyph here will rise/fall a little and with slower movement, but more often; the wave will go on forever.{ENDWAVE}
 * {WAVE=2.5;0.25;1.0;5}Each glyph here will rise/fall a lot, infrequently, at normal speed, for 5 seconds total.{ENDWAVE}
 * </code>
 */
public class WaveEffect extends Effect {
    private static final float DEFAULT_FREQUENCY = 15f;
    private static final float DEFAULT_DISTANCE = 0.33f;
    private static final float DEFAULT_SPEED = 0.5f;

    private float distance = 1; // How much of their height they should move
    private float frequency = 1; // How frequently the wave pattern repeats
    private float speed = 1; // How fast the glyphs should move

    public WaveEffect(TypingLabel label, String[] params) {
        super(label);

        // Distance
        if (params.length > 0) {
            this.distance = paramAsFloat(params[0], 1);
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
        float progress = calculateProgress(progressModifier, progressOffset);

        // Calculate offset
        float y = label.getLineHeight(globalIndex) * distance * Interpolation.sine.apply(-1, 1, progress) * DEFAULT_DISTANCE;

        // Calculate fadeout
        float fadeout = calculateFadeout();
        y *= fadeout;

        // Apply changes
        label.getOffsets().incr(globalIndex << 1 | 1, y);
    }

}
