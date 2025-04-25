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

import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.TypingLabel;
import com.github.tommyettinger.textra.utils.NoiseUtils;

/**
 * Moves the text as if it is being blown around by wind.
 * <br>
 * Parameters: {@code distanceX;distanceY;spacing;intensity;duration}
 * <br>
 * The {@code distanceX} is how many line-heights each glyph should move left and right by; defaults to 1.0 .
 * The {@code distanceX} is how many line-heights each glyph should move up and down by; defaults to 1.0 .
 * The {@code spacing} affects how much space there should be between stronger gusts; defaults to 1.0 .
 * The {@code intensity} is how strongly the wind should appear to push on each glyph; defaults to 1.0 .
 * The {@code duration} is how many seconds the wind should go on, or {@code _} to repeat forever; defaults to
 * positive infinity.
 * <br>
 * Example usage:
 * <code>
 * {WIND=2;4;0.5;0.5;_}Glyphs here will move more vertically than horizontally, with little spacing and lower intensity; the wind will go on forever.{ENDWIND}
 * {WIND=3;0.5;2.5;1.5;10}Glyphs here will move much more horizontally than vertically, with more spacing and stronger intensity; the wind will go on for 10 seconds.{ENDWIND}
 * </code>
 */
public class WindEffect extends Effect {
    private static final float DEFAULT_SPACING = 10f;
    private static final float DEFAULT_DISTANCE = 0.33f;
    private static final float DEFAULT_INTENSITY = 0.375f;
    private static final float DISTANCE_X_RATIO = 1.5f;
    private static final float DISTANCE_Y_RATIO = 1.0f;
    private static final float IDEAL_DELTA = 60f;

    private float noiseCursorX = 0;
    private float noiseCursorY = 0;

    private float distanceX = 1; // How much of their line height glyphs should move in the X axis
    private float distanceY = 1; // How much of their line height glyphs should move in the Y axis
    private float spacing = 1; // How much space there should be between gusts
    private float intensity = 1; // How strong the wind should be

    public WindEffect(TypingLabel label, String[] params) {
        super(label);

        // Distance X
        if (params.length > 0) {
            this.distanceX = paramAsFloat(params[0], 1);
        }

        // Distance Y
        if (params.length > 1) {
            this.distanceY = paramAsFloat(params[1], 1);
        }

        // Spacing
        if (params.length > 2) {
            this.spacing = paramAsFloat(params[2], 1);
        }

        // Intensity
        if (params.length > 3) {
            this.intensity = paramAsFloat(params[3], 1);
        }

        // Duration
        if (params.length > 4) {
            this.duration = paramAsFloat(params[4], Float.POSITIVE_INFINITY);
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // Update noise cursor
        float changeAmount = 0.15f * intensity * DEFAULT_INTENSITY * delta * IDEAL_DELTA;
        noiseCursorX += changeAmount;
        noiseCursorY += changeAmount;
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate progress
        float progressModifier = DEFAULT_INTENSITY / intensity;
        float normalSpacing = DEFAULT_SPACING / spacing;
        float progressOffset = localIndex / normalSpacing;
        float progress = calculateProgress(progressModifier, progressOffset);

        // Calculate noise
        float indexOffset = localIndex * 0.05f * spacing;
        float noiseX = NoiseUtils.octaveNoise1D(noiseCursorX + indexOffset, 123);
        float noiseY = NoiseUtils.octaveNoise1D(noiseCursorY + indexOffset, -4321);

        // Calculate offset
        float lineHeight = label.getLineHeight(globalIndex);
        float x = lineHeight * noiseX * progress * distanceX * DISTANCE_X_RATIO * DEFAULT_DISTANCE;
        float y = lineHeight * noiseY * progress * distanceY * DISTANCE_Y_RATIO * DEFAULT_DISTANCE;

        // Calculate fadeout
        float fadeout = calculateFadeout();
        x *= fadeout;
        y *= fadeout;

        // Add flag effect to X offset
        x = Math.abs(x) * -Math.signum(distanceX);

        // Apply changes
        label.getOffsets().incr(globalIndex << 1, x);
        label.getOffsets().incr(globalIndex << 1 | 1, y);
    }
}
