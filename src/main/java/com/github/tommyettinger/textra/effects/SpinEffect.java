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

/** Rotates each glyph quickly and slows down as it approaches some count of rotations. Doesn't repeat itself. */
public class SpinEffect extends Effect {
    private static final float DEFAULT_INTENSITY = 1.0f;

    private float   intensity = 1; // How fast the glyphs should move
    private float   rotations = 1; // how many times the glyph should circle before stopping
    private boolean elastic   = false; // True if the glyphs have an elastic movement

    private final IntFloatMap timePassedByGlyphIndex = new IntFloatMap();

    public SpinEffect(TypingLabel label, String[] params) {
        super(label);

        // Distance
        if(params.length > 0) {
            this.intensity = paramAsFloat(params[0], 1);
        }

        //Intensity
        if(params.length > 1) {
            this.rotations = paramAsFloat(params[1], 1);
        }

        // Elastic
        if(params.length > 2) {
            this.elastic = paramAsBoolean(params[2]);
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
        Interpolation interpolation = elastic ? Interpolation.bounceOut : Interpolation.pow3Out;
        float interpolatedValue = interpolation.apply(progress) * 360.0f * rotations;

        label.rotations.incr(globalIndex, interpolatedValue);
    }

}
