/*
 * Copyright (c) 2021-2022 See AUTHORS file.
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
 */
public class SquashEffect extends Effect {
    private static final float DEFAULT_INTENSITY = 0.125f;

    private float intensity = 4f; // How fast the glyphs should move
    private boolean elastic = false; // True if the glyphs have an elastic movement

    private final IntFloatMap timePassedByGlyphIndex = new IntFloatMap();

    public SquashEffect(TypingLabel label, String[] params) {
        super(label);

        // Distance
        if (params.length > 0) {
            this.intensity = 1.0f / paramAsFloat(params[0], 0.25f);
        }

        // Elastic
        if (params.length > 1) {
            this.elastic = paramAsBoolean(params[1]);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate real intensity
        float realIntensity = intensity * (elastic ? 3f : 1f) * DEFAULT_INTENSITY;

        // Calculate progress
        float timePassed = timePassedByGlyphIndex.getAndIncrement(localIndex, 0, delta);
        float progress = MathUtils.clamp(timePassed / realIntensity, 0, 1);

        // Calculate offset
        if (progress < 0.4f) {
            float interpolatedValue = 1f - Interpolation.sine.apply(progress * 2.5f) * 0.5f;
            label.offsets.incr(globalIndex << 1, label.font.mapping.get((char) glyph, label.font.defaultValue).xAdvance * (0.125f - 0.125f * interpolatedValue));
            label.sizing.incr(globalIndex << 1, 1.0f - interpolatedValue);
            label.sizing.incr(globalIndex << 1 | 1, interpolatedValue - 1.0f);
        } else {
            Interpolation interpolation = elastic ? Interpolation.swingOut : Interpolation.sine;
            float interpolatedValue = interpolation.apply((progress - 0.4f) * 1.666f) * 0.5f + 0.5f;
            label.offsets.incr(globalIndex << 1, label.font.mapping.get((char) glyph, label.font.defaultValue).xAdvance * (0.125f - 0.125f * interpolatedValue));
            label.sizing.incr(globalIndex << 1, 1.0f - interpolatedValue);
            label.sizing.incr(globalIndex << 1 | 1, interpolatedValue - 1.0f);
        }
    }

    {
    }

}
