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
 * {THINKING=5;2.8;1;y}Each glyph here will fade into position from very randomly-scattered points, drifting before doing so slowly.{ENDTHINKING}
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
    }
}
