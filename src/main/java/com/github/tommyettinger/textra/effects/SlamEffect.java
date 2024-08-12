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
import com.badlogic.gdx.utils.IntFloatMap;
import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.TypingLabel;

/**
 * Starts the text with a y-offset, holds there for a short time, drops quickly into the final position/size, and may
 * optionally shake after dropping in. Doesn't repeat itself.
 * <br>
 * Parameters: {@code initialStretch;extent;height;shakeDuration;shakePower}
 * <br>
 * The {@code hangTime} is how many seconds the glyph should stay elevated before dropping; defaults to 0.25 .
 * The {@code extent} affects how long the animation should be extended by (not in any unit); defaults to 1.0 .
 * The {@code height} is how many line-heights the glyphs should start above the destination; defaults to 1.0 .
 * The {@code shakeDuration} is how many seconds the glyph should shake after it reaches its target; defaults to 2.0 .
 * The {@code shakePower} affects how much each glyph should shake after it reaches its target; defaults to 1.0 .
 * <br>
 * Example usage:
 * <code>
 * {SLAM=0.5;2.0;2.5;1.5;2.0}This text will hang briefly, travel faster, start higher, and shake longer, and shake a lot.{ENDSLAM}
 * {SLAM=2.5;0.75;-0.5;0.7;0.9}This text will hang longer, travel more slowly, start a little below the destination, shake less time, and shake less.{ENDSLAM}
 * </code>
 */
public class SlamEffect extends Effect {
    private static final float DEFAULT_HANG_TIME = 1f;
    private static final float DEFAULT_EXTENT = 1.5f;
    private static final float DEFAULT_HEIGHT = 1f;
    private static final float DEFAULT_POWER = 1f;

    private float hangTime = 0.25f; // How long they should stay elevated before dropping, in seconds
    private float extent = 1; // Approximately how much the animation should be extended by (made slower)
    private float height = 1; // How high the glyphs should start above their target position
    private float shakeDuration = 2; // How long the glyph should shake after it stops moving in, in seconds
    private float shakePower = 1; // How strong the shake effect should be

    private final FloatArray lastOffsets = new FloatArray();

    private final IntFloatMap timePassedByGlyphIndex = new IntFloatMap();

    public SlamEffect(TypingLabel label, String[] params) {
        super(label);

        // Hang Time
        if (params.length > 0) {
            this.hangTime = paramAsFloat(params[0], 0.25f);
        }

        // Extent
        if (params.length > 1) {
            this.extent = paramAsFloat(params[1], 1.0f);
        }

        // Height
        if (params.length > 2) {
            this.height = paramAsFloat(params[2], 1.0f);
        }

        // Shake duration
        if (params.length > 3) {
            this.shakeDuration = paramAsFloat(params[3], 2.0f);
        }

        // Shake power
        if (params.length > 4) {
            this.shakePower = paramAsFloat(params[4], 1.0f);
        }

    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate real extent
        float realExtent = extent * DEFAULT_EXTENT;

        // Calculate progress
        float timePassed = timePassedByGlyphIndex.getAndIncrement(localIndex, -hangTime * DEFAULT_HANG_TIME + 1f, delta);
        float progress = MathUtils.clamp(timePassed / realExtent, 0, 1);
        progress *= progress * progress;
        float shakeProgress = progress >= 0.9f && shakeDuration != 0f ? MathUtils.clamp((timePassed / realExtent - 1f) / shakeDuration, 0f, 1f) : 0f;

        if(shakeProgress == 0f) {

            // Calculate offset
            Interpolation interpolation = Interpolation.exp10In;
            float yMove = interpolation.apply(label.getLineHeight(globalIndex) * height * DEFAULT_HEIGHT, 0, progress * progress);

            // Apply changes
            label.offsets.incr(globalIndex << 1 | 1, yMove);
        }
        else {
            // Make sure we can hold enough entries for the current index
            if (localIndex >= lastOffsets.size / 2) {
                lastOffsets.setSize(lastOffsets.size + 16);
            }

            // Get last offsets
            float lastX = lastOffsets.get(localIndex * 2);
            float lastY = lastOffsets.get(localIndex * 2 + 1);

            // Calculate new offsets
            float x = label.getLineHeight(globalIndex) * MathUtils.random(-0.125f, 0.125f);
            float y = label.getLineHeight(globalIndex) * MathUtils.random(-0.125f, 0.125f);

            // Apply intensity
            float normalIntensity = MathUtils.clamp(shakePower * DEFAULT_POWER, 0, 1);
            x = Interpolation.linear.apply(lastX, x, normalIntensity);
            y = Interpolation.linear.apply(lastY, y, normalIntensity);

            // Apply fadeout
            float fadeout = 1f - Interpolation.sineOut.apply(shakeProgress);
            x *= fadeout;
            y *= fadeout;
            x = MathUtils.round(x);
            y = MathUtils.round(y);

            // Store offsets for the next tick
            lastOffsets.set(localIndex * 2, x);
            lastOffsets.set(localIndex * 2 + 1, y);

            // Apply changes
            label.offsets.incr(globalIndex << 1, x);
            label.offsets.incr(globalIndex << 1 | 1, y);

        }
    }

}
