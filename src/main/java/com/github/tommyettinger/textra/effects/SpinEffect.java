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
 * Rotates each glyph quickly and slows down as it approaches some count of rotations. Doesn't repeat itself.
 * <br>
 * Parameters: {@code extent;rotations;elastic}
 * <br>
 * The {@code extent} affects how long the animation should be extended by (not in any unit); defaults to 1.0 .
 * The {@code rotations} affects how many times each glyph should fully rotate before stopping; defaults to 1.0 . Negative values rotate the same amount as positive, but do so clockwise.
 * If {@code elastic} is true, the glyphs will wiggle into their final rotation; defaults to false, which uses linear movement.
 * <br>
 * Example usage:
 * <code>
 * {SPIN=5;2;y}Each glyph here will wiggle-spin very quickly twice.{ENDSPIN}
 * {SPIN=0.4;6}Each glyph here will spin slowly six times.{ENDSPIN}
 * </code>
 */
public class SpinEffect extends Effect {
    private static final float DEFAULT_EXTENT = 1.0f;

    private float extent = 1; // How fast the glyphs should spin
    private float rotations = 1; // how many times the glyph should rotate fully before stopping
    private boolean elastic = false; // True if the glyphs have an elastic movement

    private final IntFloatMap timePassedByGlyphIndex = new IntFloatMap();

    public SpinEffect(TypingLabel label, String[] params) {
        super(label);

        // Extent
        if (params.length > 0) {
            this.extent = paramAsFloat(params[0], 1);
        }

        //Rotations
        if (params.length > 1) {
            this.rotations = paramAsFloat(params[1], 1);
        }

        // Elastic
        if (params.length > 2) {
            this.elastic = paramAsBoolean(params[2]);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate real extent
        float realExtent = extent * (elastic ? 3f : 1f) * DEFAULT_EXTENT;

        // Calculate progress
        float timePassed = timePassedByGlyphIndex.getAndIncrement(localIndex, 0, delta);
        float progress = MathUtils.clamp(timePassed / realExtent, 0, 1);

        // Calculate offset
        Interpolation interpolation = elastic ? Interpolation.bounceOut : Interpolation.pow3Out;
        float interpolatedValue = interpolation.apply(progress) * 360.0f * rotations;

        label.rotations.incr(globalIndex, interpolatedValue);
    }

}
