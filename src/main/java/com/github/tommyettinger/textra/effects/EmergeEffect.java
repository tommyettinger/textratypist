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
 * Stretches the text vertically from the baseline up to its full height. Doesn't repeat itself.
 * <br>
 * Parameters: {@code speed;elastic}
 * <br>
 * The {@code speed} affects how fast the glyphs should stretch out; defaults to 1.0 .
 * If {@code elastic} is true, the glyphs will wiggle to their full size; defaults to false, which uses linear movement.
 * <br>
 * Example usage:
 * <code>
 * {EMERGE=2.8;y}Each glyph here will wiggle into its full size quickly.{ENDEMERGE}
 * {EMERGE=0.3}Each glyph here will very slowly stretch to its full size.{ENDEMERGE}
 * </code>
 */
public class EmergeEffect extends Effect {
    private static final float DEFAULT_SPEED = 0.125f;

    private float speed = 4f; // How fast the glyphs should move
    private boolean elastic = false; // True if the glyphs have an elastic movement

    private final IntFloatMap timePassedByGlyphIndex = new IntFloatMap();

    public EmergeEffect(TypingLabel label, String[] params) {
        super(label);

        // Speed
        if (params.length > 0) {
            this.speed = 1.0f / paramAsFloat(params[0], 0.25f);
        }

        // Elastic
        if (params.length > 1) {
            this.elastic = paramAsBoolean(params[1]);
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
        float interpolatedValue = interpolation.apply(progress) - 1f;

        label.sizing.incr(globalIndex << 1 | 1, interpolatedValue);
        label.offsets.incr(globalIndex << 1 | 1, interpolatedValue * 0.5f * label.getLineHeight(globalIndex));
    }

}
