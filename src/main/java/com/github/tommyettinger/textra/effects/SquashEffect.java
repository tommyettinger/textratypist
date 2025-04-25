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
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.TypingLabel;

/**
 * Shrinks the text vertically toward the baseline while stretching a little outward horizontally, then returns to
 * normal. Doesn't repeat itself.
 * <br>
 * Parameters: {@code speed;elastic}
 * <br>
 * The {@code speed} affects how fast the glyphs should be squashed down and out; defaults to 0.25 .
 * If {@code elastic} is true, the glyphs will wiggle to their squashed and then full size; defaults to false, which uses linear movement.
 * <br>
 * Example usage:
 * <code>
 * {SQUASH=2.8;y}Each glyph here will wiggle "bouncily" into a squashed size and back again quickly.{ENDSQUASH}
 * {SQUASH=0.3}Each glyph here will very slowly be crushed down to a squashed size and back again.{ENDSQUASH}
 * </code>
 */
public class SquashEffect extends Effect {
    private static final float DEFAULT_SPEED = 0.125f;

    private float speed = 4f; // How fast the glyphs should move
    private boolean elastic = false; // True if the glyphs have an elastic movement

    private final IntFloatMap timePassedByGlyphIndex = new IntFloatMap();

    public SquashEffect(TypingLabel label, String[] params) {
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
        // Calculate real speed
        float realSpeed = speed * (elastic ? 3f : 1f) * DEFAULT_SPEED;

        // Calculate progress
        float timePassed = timePassedByGlyphIndex.getAndIncrement(localIndex, 0, delta);
        float progress = MathUtils.clamp(timePassed / realSpeed, 0, 1);

        Font font = label.getFont();
        float lineHeight = label.getLineHeight(globalIndex);

        // Calculate offset
        if (progress < 0.4f) {
            float interpolatedValue = 1f - Interpolation.sine.apply(progress * 2.5f) * 0.5f;
            float xOff = lineHeight * (-0.25f * (1.0f - interpolatedValue));
            label.getOffsets().incr(globalIndex << 1, xOff);
            label.getOffsets().incr(globalIndex << 1 | 1, (interpolatedValue - 1f) * 0.5f * lineHeight);
            label.getSizing().incr(globalIndex << 1, 1.0f - interpolatedValue);
            label.getSizing().incr(globalIndex << 1 | 1, interpolatedValue - 1f);
        } else {
            Interpolation interpolation = elastic ? Interpolation.swingOut : Interpolation.sine;
            float interpolatedValue = interpolation.apply((progress - 0.4f) * 1.666f) * 0.5f + 0.5f;
            float xOff = lineHeight * (-0.25f * (1.0f - interpolatedValue));
            label.getOffsets().incr(globalIndex << 1, xOff);
            label.getOffsets().incr(globalIndex << 1 | 1, (interpolatedValue - 1f) * 0.5f * lineHeight);
            label.getSizing().incr(globalIndex << 1, 1.0f - interpolatedValue);
            label.getSizing().incr(globalIndex << 1 | 1, interpolatedValue - 1f);
        }
    }

}
