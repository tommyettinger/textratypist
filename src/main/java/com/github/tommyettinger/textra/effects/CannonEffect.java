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
 * <br>
 * Parameters: {@code initialStretch;speed;height;shakeDuration;shakePower}
 * <br>
 * The {@code initialStretch} affects how big the glyphs should be while they are in the "foreground;" defaults to 1.0 .
 * The {@code speed} affects how quickly the glyphs enter their target positions; defaults to 1.0 .
 * The {@code height} is how many line-heights the glyphs should arc up and then down; defaults to 1.0 .
 * The {@code shakeDuration} is how many seconds the glyph should shake after it "hits" its target; defaults to 2.0 .
 * The {@code shakePower} affects how much each glyph should shake after it "hits" its target; defaults to 1.0
 * <br>
 * Example usage:
 * <code>
 * {CANNON=0.5;1.5;0.25;0.3;0.2}This text will start smaller, travel faster, arc less, and not shake much.{ENDCANNON}
 * {CANNON=2.5;0.75;1.5;1.7;2.1}This text will start larger, travel more slowly, arc more, shake longer, and shake much more.{ENDCANNON}
 * </code>
 */
public class CannonEffect extends Effect {
    private static final float DEFAULT_STRETCH = 3f;
    private static final float DEFAULT_INTENSITY = 0.9f;
    private static final float DEFAULT_HEIGHT = 2.5f;
    private static final float DEFAULT_POWER = 1f;

    private float initialStretch = 1; // How much of their height they should start expanded by
    private float speed = 1; // How fast the glyphs should move
    private float height = 1; // How high the glyphs should move above their starting position
    private float shakeDuration = 2; // How long the glyph should shake after it stops moving in, in seconds
    private float shakePower = 1; // How strong the shake effect should be

    private final FloatArray lastOffsets = new FloatArray();

    private final IntFloatMap timePassedByGlyphIndex = new IntFloatMap();

    public CannonEffect(TypingLabel label, String[] params) {
        super(label);

        // Initial Stretch
        if (params.length > 0) {
            this.initialStretch = paramAsFloat(params[0], 1.0f);
        }

        // Speed
        if (params.length > 1) {
            this.speed = paramAsFloat(params[1], 1.0f);
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
        float realIntensity = speed * DEFAULT_INTENSITY;

        // Calculate progress
        float timePassed = timePassedByGlyphIndex.getAndIncrement(localIndex, 0, delta);
        float progress = MathUtils.clamp(timePassed / realIntensity, 0, 1);
        progress = (float) Math.sqrt(progress);
        float shakeProgress = progress >= 0.9f && shakeDuration != 0f ? MathUtils.clamp((timePassed / realIntensity - 1f) / shakeDuration, 0f, 1f) : 0f;

        if(shakeProgress == 0f) {

            // Calculate offset
            Interpolation interpolation = Interpolation.sine;
            float interpolatedValue = interpolation.apply(initialStretch * DEFAULT_STRETCH,
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
            float x = label.getLineHeight(globalIndex) * initialStretch * MathUtils.random(-0.125f, 0.125f);
            float y = label.getLineHeight(globalIndex) * initialStretch * MathUtils.random(-0.125f, 0.125f);

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
