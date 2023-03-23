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
import com.badlogic.gdx.utils.FloatArray;
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
    private static final float DEFAULT_POWER = 1f;

    private float distance = 1; // How much of their height they should start expanded by
    private float intensity = 1; // How fast the glyphs should move
    private float height = 1; // How high the glyphs should move above their starting position
    private float shakeDuration = 2; // How long the glyph should shake after it stops moving in, in seconds
    private float shakePower = 1; // How strong the shake effect should be

    private final FloatArray lastOffsets = new FloatArray();

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

        // Shake duration
        if (params.length > 3) {
            this.shakeDuration = paramAsFloat(params[3], 2.0f);
        }

        // Shake power
        if (params.length > 4) {
            this.shakePower = paramAsFloat(params[4], 1.0f);
        }

    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate real intensity
        float realIntensity = intensity * DEFAULT_INTENSITY;

        // Calculate progress
        float timePassed = timePassedByGlyphIndex.getAndIncrement(localIndex, 0, delta);
        float progress = MathUtils.clamp(timePassed / realIntensity, 0, 1);
        progress = (float) Math.sqrt(progress);
        float shakeProgress = progress >= 0.9f && shakeDuration != 0f ? MathUtils.clamp((timePassed / realIntensity - 1f) / shakeDuration, 0f, 1f) : 0f;

        if(shakeProgress == 0f) {

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
        else {
            // Make sure we can hold enough entries for the current index
            if (localIndex >= lastOffsets.size / 2) {
                lastOffsets.setSize(lastOffsets.size + 16);
            }

            // Get last offsets
            float lastX = lastOffsets.get(localIndex * 2);
            float lastY = lastOffsets.get(localIndex * 2 + 1);

            // Calculate new offsets
            float x = label.getLineHeight(globalIndex) * distance * MathUtils.random(-0.125f, 0.125f);
            float y = label.getLineHeight(globalIndex) * distance * MathUtils.random(-0.125f, 0.125f);

            // Apply intensity
            float normalIntensity = MathUtils.clamp(shakePower * DEFAULT_POWER, 0, 1);
            x = Interpolation.linear.apply(lastX, x, normalIntensity);
            y = Interpolation.linear.apply(lastY, y, normalIntensity);

            // Apply fadeout
            float fadeout = 1f - Interpolation.sineOut.apply(shakeProgress);
            x *= fadeout;
            y *= fadeout;
            x = MathUtils.round(x);
            y = MathUtils.round(y);

            // Store offsets for the next tick
            lastOffsets.set(localIndex * 2, x);
            lastOffsets.set(localIndex * 2 + 1, y);

            // Apply changes
            label.offsets.incr(globalIndex << 1, x);
            label.offsets.incr(globalIndex << 1 | 1, y);

        }
    }

}
