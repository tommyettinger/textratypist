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
 * Moves the text horizontally easing it into the final position. Doesn't repeat itself.
 * This is similar to {@link EaseEffect}, except that this is horizontal.
 * <br>
 * Parameters: {@code distance;speed;elastic}
 * <br>
 * The {@code distance} is how many line-heights each glyph should move as it gets into position; defaults to 1.0 .
 * The {@code speed} affects how fast the glyphs should slide in; defaults to 1.0 .
 * If {@code elastic} is true, the glyphs will wiggle into position; defaults to false, which uses linear movement.
 * <br>
 * Example usage:
 * <code>
 * {EASE=5;2.8;y}Each glyph here will wiggle into position from very far to the right, doing so quickly.{ENDEASE}
 * {EASE=-2;0.3}Each glyph here will slide into place from the left, very slowly.{ENDEASE}
 * </code>
 */
public class SlideEffect extends Effect {
    private static final float DEFAULT_DISTANCE = 2f;
    private static final float DEFAULT_SPEED = 0.375f;

    private float distance = 1; // How much of their height they should move
    private float speed = 1; // How fast the glyphs should move
    private boolean elastic = false; // Whether the glyphs have an elastic movement

    private final IntFloatMap timePassedByGlyphIndex = new IntFloatMap();

    public SlideEffect(TypingLabel label, String[] params) {
        super(label);

        // Distance
        if (params.length > 0) {
            this.distance = paramAsFloat(params[0], 1);
        }

        // Speed
        if (params.length > 1) {
            this.speed = paramAsFloat(params[1], 1);
        }

        // Elastic
        if (params.length > 2) {
            this.elastic = paramAsBoolean(params[2]);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate real intensity
        float realIntensity = speed * (elastic ? 3f : 1f) * DEFAULT_SPEED;

        // Calculate progress
        float timePassed = timePassedByGlyphIndex.getAndIncrement(localIndex, 0, delta);
        float progress = MathUtils.clamp(timePassed / realIntensity, 0, 1);

        // Calculate offset
        Interpolation interpolation = elastic ? Interpolation.swingOut : Interpolation.sine;
        float interpolatedValue = interpolation.apply(1, 0, progress);
        float x = label.getLineHeight(globalIndex) * distance * interpolatedValue * DEFAULT_DISTANCE;

        // Apply changes
        label.offsets.incr(globalIndex << 1, x);
    }

}
