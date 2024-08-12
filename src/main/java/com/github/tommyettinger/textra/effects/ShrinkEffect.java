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
 * Starts the text large and shrinks into the final position/size. Doesn't repeat itself.
 * <br>
 * Parameters: {@code expansion;speed;elastic}
 * <br>
 * The {@code expansion} is how many line-heights each glyph should be stretched out by at the start; defaults to 1.0 .
 * The {@code speed} affects how fast the glyphs should shrink; defaults to 1.0 .
 * If {@code elastic} is true, the glyphs will wiggle into position; defaults to false, which uses linear movement.
 * <br>
 * Example usage:
 * <code>
 * {SHRINK=5;2.8;y}Each glyph here will shrink "bouncily" into position from a very large size, doing so quickly.{ENDSHRINK}
 * {SHRINK=1.5;0.3}Each glyph here will shrink into position from a somewhat-large size, and very slowly.{ENDSHRINK}
 * </code>
 */
public class ShrinkEffect extends Effect {
    private static final float DEFAULT_EXPANSION = 3f;
    private static final float DEFAULT_SPEED = 0.15f;

    private float expansion = 1; // How much of their height they should start expanded by
    private float speed = 1; // How fast the glyphs should move
    private boolean elastic = false; // True if the glyphs have an elastic movement

    private final IntFloatMap timePassedByGlyphIndex = new IntFloatMap();

    public ShrinkEffect(TypingLabel label, String[] params) {
        super(label);

        // Expansion
        if (params.length > 0) {
            this.expansion = paramAsFloat(params[0], 1.0f);
        }

        // Speed
        if (params.length > 1) {
            this.speed = paramAsFloat(params[1], 1.0f);
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
        float interpolatedValue = interpolation.apply(expansion * DEFAULT_EXPANSION,
                0f, progress);

        label.sizing.incr(globalIndex << 1, interpolatedValue);
        label.sizing.incr(globalIndex << 1 | 1, interpolatedValue);
    }

}
