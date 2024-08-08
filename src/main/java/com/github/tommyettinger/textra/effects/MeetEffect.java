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
 * Moves the text from random starting points, easing glyphs into their final positions. Doesn't repeat itself.
 * This is similar to {@link SlideEffect} and {@link EaseEffect}, except this uses random starting points.
 * <br>
 * Parameters: {@code distance;speed;elastic;inside}
 * <br>
 * The {@code distance} is how many line-heights each glyph should start away from its destination; defaults to 2 .
 * The {@code speed} affects how fast the glyphs should slide in; defaults to 1.0 .
 * If {@code elastic} is true, the glyphs will wiggle into position; defaults to false, which uses linear movement.
 * If {@code inside} is true, glyphs will start a random amount less than {@code distance} to their destination; defaults to false.
 * <br>
 * Example usage:
 * <code>
 * {MEET=5;2.8;y;y}Each glyph here will wiggle into position from very randomly-scattered points, doing so quickly.{ENDMEET}
 * {MEET=-3;0.3}Each glyph here will slide from an equal distance and random angle, going into place very slowly.{ENDMEET}
 * </code>
 */
public class MeetEffect extends Effect {
    private static final float DEFAULT_DISTANCE = 1f;
    private static final float DEFAULT_SPEED = 1f;

    private float distance = 2; // How much of their height they should move
    private float speed = 1; // How fast the glyphs should move
    private boolean elastic = false; // True if the glyphs have an elastic movement
    private boolean inside = false; // True if the glyphs can be positioned inside the circle

    private final IntFloatMap timePassedByGlyphIndex = new IntFloatMap();

    public MeetEffect(TypingLabel label, String[] params) {
        super(label);

        // Distance
        if (params.length > 0) {
            this.distance = paramAsFloat(params[0], 2);
        }

        // Speed
        if (params.length > 1) {
            this.speed = paramAsFloat(params[1], 1);
        }

        // Elastic
        if (params.length > 2) {
            this.elastic = paramAsBoolean(params[2]);
        }
        // Inside
        if (params.length > 3) {
            this.inside = paramAsBoolean(params[3]);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate real speed
        float realSpeed = speed * (elastic ? 0.333f : 1f) * DEFAULT_SPEED;

        // Calculate progress
        float timePassed = timePassedByGlyphIndex.getAndIncrement(localIndex, 0, delta);
        float progress = MathUtils.clamp(timePassed * realSpeed, 0, 1);

        // Calculate offset
        Interpolation interpolation = elastic ? Interpolation.swingOut : Interpolation.sine;
        float interpolatedValue = interpolation.apply(1, 0, progress);
        int random = ((globalIndex ^ 0xDE82EF95) * 0xD1343 ^ 0xDE82EF95) * 0xD1343;
        float angle = (random >>> 9) * 0x1p-23f * MathUtils.PI2;
        float dist = label.getLineHeight(globalIndex) * distance * DEFAULT_DISTANCE *
                ((inside) ? (float) Math.sqrt((((random ^ 0xDE82EF95) * 0xD1343 ^ 0xDE82EF95) * 0xD1343 >>> 9) * 0x1p-23f) : 1f);
        float x = MathUtils.cos(angle) * dist;
        float y = MathUtils.sin(angle) * dist;

        // Apply changes
        label.offsets.incr(globalIndex << 1, x * interpolatedValue);
        label.offsets.incr(globalIndex << 1 | 1, y * interpolatedValue);
    }

}
