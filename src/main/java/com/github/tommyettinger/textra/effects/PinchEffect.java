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
import com.badlogic.gdx.utils.TimeUtils;
import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.TypingLabel;

/**
 * Randomly selects and squeezes individual characters in the text, reducing their width and increasing their height.
 * <br>
 * Parameters: {@code strength;duration;likelihood;elastic}
 * <br>
 * The {@code strength} affects how much a selected glyph should be scaled in and up, as a fraction between 0 and 1; defaults to 0.5 .
 * The {@code duration} affects how long the effect will continue for, in seconds; defaults to positive infinity.
 * The {@code likelihood} affects how often a glyph is selected to be squeezed; defaults to 0.05 .
 * If {@code elastic} is true, the glyphs will wiggle to their squashed and then full size; defaults to false, which uses linear movement.
 * <br>
 * Example usage:
 * <code>
 * {PINCH=0.8;_;0.25;f}This text will select glyphs to squeeze much more frequently, and squeeze them more than usual;
 * it won't be elastic. The effect won't ever end.{ENDPINCH}
 * {PINCH=0.3;5;0.03;t}This text will select glyphs to squeeze infrequently, and squeeze less than usual; it will be
 * elastic at the end of each pinch. The effect will end after 5 seconds.{ENDPINCH}
 * </code>
 */
public class PinchEffect extends Effect {
    private static final float DEFAULT_STRENGTH = 0.5f;
    private static final float DEFAULT_LIKELIHOOD = 0.05f;

    private float strength = DEFAULT_STRENGTH; // How far the glyphs should move
    private float likelihood = DEFAULT_LIKELIHOOD; // How likely it is that each glyph will shake; checked 1/second
    private boolean elastic = false; // True if the glyphs have an elastic movement

    public PinchEffect(TypingLabel label, String[] params) {
        super(label);

        // Squeeze strength
        if (params.length > 0) {
            this.strength = paramAsFloat(params[0], 0.5f);
        }

        // Duration
        if (params.length > 1) {
            this.duration = paramAsFloat(params[2], Float.POSITIVE_INFINITY);
        }

        // Likelihood
        if (params.length > 3) {
            this.likelihood = paramAsFloat(params[3], DEFAULT_LIKELIHOOD);
        }

        // Base Color
        if (params.length > 4) {
            this.elastic = paramAsBoolean(params[4]);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate scales
        long time = TimeUtils.millis();
        if (likelihood > determineFloat((time >>> 10) * globalIndex + localIndex)) {
            float lineHeight = label.getLineHeight(globalIndex);
            float progress = (time & 1023) * 0x1p-10f;
            // Apply fadeout
            float fadeout = calculateFadeout() * strength;

            // Calculate offset
            if (progress < 0.4f) {
                float interpolatedValue = 1f - Interpolation.sine.apply(progress * 2.5f) * fadeout;
                float xOff = lineHeight * (-0.25f * (1.0f - interpolatedValue));
                label.getOffsets().incr(globalIndex << 1, xOff);
                label.getOffsets().incr(globalIndex << 1 | 1, (interpolatedValue - 1f) * 0.5f * lineHeight);
                label.getSizing().incr(globalIndex << 1, interpolatedValue - 1f);
                label.getSizing().incr(globalIndex << 1 | 1, 1.0f - interpolatedValue);
            } else {
                Interpolation interpolation = elastic ? Interpolation.swingOut : Interpolation.sine;
                float interpolatedValue = interpolation.apply((progress - 0.4f) * 1.666f) * fadeout + 1f - fadeout;
                float xOff = lineHeight * (-0.25f * (1.0f - interpolatedValue));
                label.getOffsets().incr(globalIndex << 1, xOff);
                label.getOffsets().incr(globalIndex << 1 | 1, (interpolatedValue - 1f) * 0.5f * lineHeight);
                label.getSizing().incr(globalIndex << 1, interpolatedValue - 1f);
                label.getSizing().incr(globalIndex << 1 | 1, 1.0f - interpolatedValue);
            }
        }
    }

    private static float determineFloat(long state) {
        return ((((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) >>> 40) * 0x1p-24f;
    }

}
