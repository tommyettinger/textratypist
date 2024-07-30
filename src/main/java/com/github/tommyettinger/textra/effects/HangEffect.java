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
 * Hangs the text in midair and suddenly drops it. Doesn't repeat itself.
 * <br>
 * Parameters: {@code distance;speed}
 * <br>
 * The {@code distance} is how many line-heights the glyphs should move up before dropping down; defaults to 1.0 .
 * The {@code speed} affects how fast the glyphs should stretch out; defaults to 1.0 .
 * <br>
 * Example usage:
 * <code>
 * {HANG=2.0;1.3}Each glyph here will hang higher and drop a little more quickly.{ENDHANG}
 * {HANG=-0.5;3.0}Each glyph here will pop up quickly from a short distance below, instead of dropping down.{ENDHANG}
 * </code>
 */
public class HangEffect extends Effect {
    private static final float DEFAULT_DISTANCE = 0.7f;
    private static final float DEFAULT_SPEED = 1.5f;

    private float distance = 1; // How much of their height they should move
    private float speed = 1; // How fast the glyphs should move

    private final IntFloatMap timePassedByGlyphIndex = new IntFloatMap();

    public HangEffect(TypingLabel label, String[] params) {
        super(label);

        // Distance
        if (params.length > 0) {
            this.distance = paramAsFloat(params[0], 1);
        }

        // Speed
        if (params.length > 1) {
            this.speed = paramAsFloat(params[1], 1);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate real intensity
        float realIntensity = speed * 1f * DEFAULT_SPEED;

        // Calculate progress
        float timePassed = timePassedByGlyphIndex.getAndIncrement(localIndex, 0, delta);
        float progress = MathUtils.clamp(timePassed / realIntensity, 0, 1);

        // Calculate offset
        float interpolation;
        float split = 0.7f;
        if (progress < split) {
            interpolation = Interpolation.pow3Out.apply(0, 1, progress / split);
        } else {
            interpolation = Interpolation.swing.apply(1, 0, (progress - split) / (1f - split));
        }
        float distanceFactor = Interpolation.linear.apply(1.0f, 1.5f, progress);
        float height = label.getLineHeight(globalIndex);
        float y = height * distance * distanceFactor * interpolation * DEFAULT_DISTANCE;

        // Calculate fadeout
        float fadeout = calculateFadeout();
        y *= fadeout;

        // Apply changes
        label.offsets.incr(globalIndex << 1 | 1, y);
    }

}
