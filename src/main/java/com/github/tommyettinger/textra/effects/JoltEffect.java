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
import com.badlogic.gdx.utils.TimeUtils;
import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.TypingLabel;

/**
 * Randomly selects and shakes individual characters in the text, changing their color while shaking.
 * <br>
 * Parameters: {@code shakeDistance;shakeSpeed;duration;likelihood;baseColor;joltColor}
 * <br>
 * The {@code shakeDistance} affects how far a shaking glyph should move from its original position; defaults to 1.0 .
 * The {@code shakeSpeed} affects how fast a shaking glyph should move; defaults to 1.0 .
 * The {@code duration} affects how long the effect will continue for, in seconds; defaults to positive infinity.
 * The {@code likelihood} affects how often a glyph is selected to be shaken; defaults to 0.05 .
 * The {@code baseColor} can be a named color or hex color, but if not a color this will only shake glyphs, not color them
 * while shaking. Defaults to {@code _} (not a color).
 * The {@code joltColor} can be a named color or hex color, and will be used as the color for shaking glyphs as long as
 * {@code baseColor} has an actual color value (not {@code _}). Defaults to {@code #FFFF88FF} (light yellow).
 * <br>
 * Example usage:
 * <code>
 * {JOLT=1;0.8;_;0.25;dddddd;fff0cc}This text will shake more slowly, and select glyphs to shake much more frequently;
 * it will start light gray and become pale yellow when shaking. The effect won't ever end.{ENDJOLT}
 * {JOLT=1;1;_;0.05;DARKEST RED;RED}This text will default to very dark red but will make individual characters pop out
 * in shaking bright red text. The effect won't ever end.{ENDJOLT}
 * </code>
 */
public class JoltEffect extends Effect {
    private static final float DEFAULT_DISTANCE = 0.12f;
    private static final float DEFAULT_SPEED = 0.5f;
    private static final float DEFAULT_LIKELIHOOD = 0.05f;

    private final FloatArray lastOffsets = new FloatArray();

    private float shakeDistance = 1; // How far the glyphs should move
    private float shakeSpeed = 1; // How fast the glyphs should move
    private float likelihood = DEFAULT_LIKELIHOOD; // How likely it is that each glyph will shake; repeatedly checked
    private int baseColor = 256; // The color to use when not jolting. If 256, then colors will not change
    private int joltColor = 0xFFFF88FF; // The color to use when jolting. If baseColor is 256, this will be ignored

    public JoltEffect(TypingLabel label, String[] params) {
        super(label);

        // Shake Distance
        if (params.length > 0) {
            this.shakeDistance = paramAsFloat(params[0], 1);
        }

        // Shake Speed
        if (params.length > 1) {
            this.shakeSpeed = paramAsFloat(params[1], 1);
        }

        // Duration
        if (params.length > 2) {
            this.duration = paramAsFloat(params[2], Float.POSITIVE_INFINITY);
        }

        // Likelihood
        if (params.length > 3) {
            this.likelihood = paramAsFloat(params[3], DEFAULT_LIKELIHOOD);
        }

        // Base Color
        if (params.length > 4) {
            this.baseColor = paramAsColor(params[4]);
        }

        // Actively Jolting Color
        if (params.length > 5) {
            int c = paramAsColor(params[5]);
            if (c != 256) this.joltColor = c;
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
        float x = 0f, y = 0f;
        if (likelihood > determineFloat((TimeUtils.millis() >>> 10) * globalIndex + localIndex)) {
            x = label.getLineHeight(globalIndex) * shakeDistance * MathUtils.random(-1f, 1f) * DEFAULT_DISTANCE;
            y = label.getLineHeight(globalIndex) * shakeDistance * MathUtils.random(-1f, 1f) * DEFAULT_DISTANCE;

            // Apply intensity
            float normalIntensity = MathUtils.clamp(shakeSpeed * DEFAULT_SPEED, 0, 1);
            x = Interpolation.linear.apply(lastX, x, normalIntensity);
            y = Interpolation.linear.apply(lastY, y, normalIntensity);

            // Apply fadeout
            float fadeout = calculateFadeout();
            x *= fadeout;
            y *= fadeout;
            x = MathUtils.round(x);
            y = MathUtils.round(y);
            if (fadeout > 0) {
                if(baseColor == 256)
                    label.setInWorkingLayout(globalIndex, glyph);
                else
                    label.setInWorkingLayout(globalIndex, (glyph & 0xFFFFFFFFL) | (long) joltColor << 32);
            }
        } else {
            if(baseColor == 256)
                label.setInWorkingLayout(globalIndex, glyph);
            else
                label.setInWorkingLayout(globalIndex, (glyph & 0xFFFFFFFFL) | (long) baseColor << 32);
        }
        // Store offsets for the next tick
        lastOffsets.set(localIndex * 2, x);
        lastOffsets.set(localIndex * 2 + 1, y);

        // Apply changes
        label.offsets.incr(globalIndex << 1, x);
        label.offsets.incr(globalIndex << 1 | 1, y);
    }

    private static float determineFloat(long state) {
        return ((((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) >>> 40) * 0x1p-24f;
    }

}
