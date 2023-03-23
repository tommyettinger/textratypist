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
 * Starts the text large and shrinks into the final position/size, arcing up and then ending moving down.
 * Doesn't repeat itself.
 */
public class CannonEffect extends Effect {
    private static final float DEFAULT_DISTANCE = 3f;
    private static final float DEFAULT_INTENSITY = 0.9f;
    private static final float DEFAULT_HEIGHT = 2.5f;

    private float distance = 1; // How much of their height they should start expanded by
    private float intensity = 1; // How fast the glyphs should move
    private float height = 1; // How high the glyphs should move above their starting position

    private final IntFloatMap timePassedByGlyphIndex = new IntFloatMap();

    public CannonEffect(TypingLabel label, String[] params) {
        super(label);

        // Distance
        if (params.length > 0) {
            this.distance = paramAsFloat(params[0], 1.0f);
        }

        //Intensity
        if (params.length > 1) {
            this.intensity = paramAsFloat(params[1], 1.0f);
        }

        // Height
        if (params.length > 2) {
            this.height = paramAsFloat(params[2], 1.0f);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate real intensity
        float realIntensity = intensity * DEFAULT_INTENSITY;

        // Calculate progress
        float timePassed = timePassedByGlyphIndex.getAndIncrement(localIndex, 0, delta);
        float progress = MathUtils.clamp(timePassed / realIntensity, 0, 1);

        progress = 1f - (1f - progress) * (1f - progress);

        // Calculate offset
        Interpolation interpolation = Interpolation.sine;
        float interpolatedValue = interpolation.apply(distance * DEFAULT_DISTANCE,
                0f, progress);
        float arcHeight = MathUtils.sin(MathUtils.PI * progress) * label.getLineHeight(globalIndex) * height * DEFAULT_HEIGHT;

        label.sizing.incr(globalIndex << 1, interpolatedValue);
        label.sizing.incr(globalIndex << 1 | 1, interpolatedValue);

        // Apply changes
        label.offsets.incr(globalIndex << 1 | 1, arcHeight);

    }

}
