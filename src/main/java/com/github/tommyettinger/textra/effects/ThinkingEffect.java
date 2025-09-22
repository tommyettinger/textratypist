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
import com.badlogic.gdx.utils.IntFloatMap;
import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.TypingLabel;

/**
 * Positions each glyph at random starting points, then has each fade and reappear in the intended position after a
 * short span of time. Doesn't repeat itself.
 * This is similar to {@link MeetEffect}, but doesn't move points into their final position (they "blink" out and into
 * their final position, though not instantly).
 * <br>
 * Parameters: {@code distance;extent;drift;inside}
 * <br>
 * The {@code distance} is how many line-heights each glyph should start away from its destination; defaults to 2 .
 * The {@code extent} affects how long the animation should be extended by (not in any unit); defaults to 1.0 .
 * The {@code drift} is greater than 0, each glyph will move up to that distance (in line-heights) in a random direction before disappearing; defaults to 1.0.
 * If {@code inside} is true, glyphs will start a random amount less than {@code distance} to their destination; defaults to false.
 * <br>
 * Example usage:
 * <code>
 * {THINKING=5;10;1;y}Each glyph here will fade into position from very randomly-scattered points, drifting before doing so slowly.{ENDTHINKING}
 * {THINKING=-3;0.3;0}Each glyph here will start at an equal distance and random angle, not drifting and blinking into place very quickly.{ENDTHINKING}
 * </code>
 */
public class ThinkingEffect extends Effect {
    private static final float DEFAULT_DISTANCE = 1f;
    private static final float DEFAULT_DRIFT = 1f;

    private float distance = 2; // How much of their height they should move
    private float extent = 1; // Approximately how much the animation should be extended by (made slower)
    private float drift = 1; // how much a glyph can drift before disappearing
    private boolean inside = false; // True if the glyphs can be positioned inside the circle

    private final IntFloatMap timePassedByGlyphIndex = new IntFloatMap();

    public ThinkingEffect(TypingLabel label, String[] params) {
        super(label);

        // Distance
        if (params.length > 0) {
            this.distance = paramAsFloat(params[0], 2);
        }

        // Extent
        if (params.length > 1) {
            this.extent = paramAsFloat(params[1], 1);
        }

        // Drift
        if (params.length > 2) {
            this.drift = paramAsFloat(params[2], 1);
        }
        // Inside
        if (params.length > 3) {
            this.inside = paramAsBoolean(params[3]);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate real extent
        float realExtent = extent;

        // Calculate progress
        float timePassed = timePassedByGlyphIndex.getAndIncrement(localIndex, 0, delta);
        float progress = MathUtils.clamp(timePassed / realExtent, 0, 1);

        // Calculate offset
        Interpolation interpolation = Interpolation.linear;
        float interpolatedValue = interpolation.apply(1, 0, progress);
        // randomized angles
        int random = ((globalIndex ^ 0xDE82EF95) * 0xD1343 ^ 0xDE82EF95) * 0xD1343;
        float angle = (random >>> 9) * 0x1p-23f * MathUtils.PI2;
        float driftAngle = (random & 0x7FFFFF) * 0x1p-23f * MathUtils.PI2;
        // more random values, used for inside distance and drif amount
        int random2 = ((random ^ 0xDE82EF95) * 0xD1343 ^ 0xDE82EF95) * 0xD1343;

        // takes progress from 0 to 1 and raises it to a random power between 0 and 1, which makes lower values take
        // up randomly more of the used time
        float randomizedProgress = (float) Math.pow(progress, (((random2 ^ 0xDE82EF95) * 0xD1343 ^ 0xDE82EF95) * 0xD1343 >>> 9) * 0x1p-23f);
        // We use cos here because between 0 and PI, it goes from 1, to -1, to 1 again; while it's negative, the glyph
        // will have alpha 2f/255f .
        float alpha = Math.max(0f, MathUtils.cos(randomizedProgress * MathUtils.PI));

        float lineHeight = label.getLineHeight(globalIndex);
        // if the glyph has already gone past halfway through the blink, we move it to its final position
        float dist = (randomizedProgress > 0.5f)
                ? 0f
                : lineHeight * distance * DEFAULT_DISTANCE *
                ((inside) ? (float) Math.sqrt((random2 >>> 9) * 0x1p-23f) : 1f);
        // if the glyph has already gone past halfway through the blink, we don't drift it anymore
        float driftAmount = (randomizedProgress > 0.5f || drift == 0f)
                ? 0f
                : lineHeight * drift * DEFAULT_DRIFT * ((random2 & 0x7FFFFF) * 0x1p-23f) * interpolatedValue;
        // starting offsets plus drift offsets, with drift potentially changing
        float x = MathUtils.cos(angle) * dist + MathUtils.cos(driftAngle) * driftAmount;
        float y = MathUtils.sin(angle) * dist + MathUtils.sin(driftAngle) * driftAmount;

        // Apply position changes
        label.getOffsets().incr(globalIndex << 1, x);
        label.getOffsets().incr(globalIndex << 1 | 1, y);

        // handle the fade out and in; alpha doesn't go below 2/255, which is nearly invisible
        label.setInWorkingLayout(globalIndex,
                (glyph & 0xFFFFFF00FFFFFFFFL) | (long) MathUtils.lerp(glyph >>> 32 & 255,
                        2, alpha) << 32);
    }
}
