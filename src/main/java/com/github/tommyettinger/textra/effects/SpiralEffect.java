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

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntFloatMap;
import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.TypingLabel;

/**
 * Moves the text in a round spiral from outwards-in, easing it into the final position. Doesn't repeat itself.
 */
public class SpiralEffect extends Effect {
    private static final float DEFAULT_DISTANCE = 1f;
    private static final float DEFAULT_INTENSITY = 0.75f;

    private float distance = 1; // How much of their height they should move
    private float intensity = 1; // How fast the glyphs should move
    private float rotations = 1; // how many times the glyph should circle before stopping

    private final IntFloatMap timePassedByGlyphIndex = new IntFloatMap();

    public SpiralEffect(TypingLabel label, String[] params) {
        super(label);

        // Distance
        if (params.length > 0) {
            this.distance = paramAsFloat(params[0], 1);
        }

        // Intensity
        if (params.length > 1) {
            this.intensity = 1f / paramAsFloat(params[1], 1);
        }

        // Rotations
        if (params.length > 2) {
            this.rotations = paramAsFloat(params[2], 1);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate real intensity
        float realIntensity = intensity * DEFAULT_INTENSITY;

        // Calculate progress
        float timePassed = timePassedByGlyphIndex.getAndIncrement(localIndex, 0, delta);
        float progress = MathUtils.clamp(timePassed / realIntensity, 0, 1);
        float spin = 360f * rotations * progress;
        // Calculate offset
        float lineHeight = label.getLineHeight(globalIndex);
        float x = lineHeight * distance * DEFAULT_DISTANCE * MathUtils.cosDeg(spin) * (1f - progress);
        float y = lineHeight * distance * DEFAULT_DISTANCE * MathUtils.sinDeg(spin) * (1f - progress);

        // Apply changes
        label.offsets.incr(globalIndex << 1, x);
        label.offsets.incr(globalIndex << 1 | 1, y);
    }

}
