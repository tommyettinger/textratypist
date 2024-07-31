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
import com.badlogic.gdx.utils.IntArray;
import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.TypingLabel;

/**
 * Drips the text down and back up from its normal position in a random pattern.
 * This can work well with {@link CrowdEffect} to incorporate random rotation.
 * <br>
 * Parameters: {@code distance;speed;duration}
 * <br>
 * The {@code distance} is how many line-heights each glyph should move down, at most; defaults to 1.0 .
 * The {@code speed} is how quickly each glyph should move; defaults to 1.0 .
 * The {@code duration} is how many seconds the dripping should go on, or {@code _} to repeat forever; defaults to
 * positive infinity.
 * <br>
 * Example usage:
 * <code>
 * {SICK=0.5;0.8;_}Each glyph here will shake a little and with slower movement; the shaking will go on forever.{ENDSICK}
 * {SICK=2.5;1.0;5}Each glyph here will shake a lot, at normal speed, for 5 seconds total.{ENDSICK}
 * </code>
 */
public class SickEffect extends Effect {
    private static final float DEFAULT_FREQUENCY = 50f;
    private static final float DEFAULT_DISTANCE = .125f;
    private static final float DEFAULT_SPEED = 1f;

    public float distance = 1; // How far the glyphs should move
    public float speed = 1; // How fast the glyphs should move

    private final IntArray indices = new IntArray();

    public SickEffect(TypingLabel label, String[] params) {
        super(label);

        // Distance
        if (params.length > 0) {
            this.distance = paramAsFloat(params[0], 1);
        }

        // Speed
        if (params.length > 1) {
            this.speed = paramAsFloat(params[1], 1);
        }

        // Duration
        if (params.length > 2) {
            this.duration = paramAsFloat(params[2], Float.POSITIVE_INFINITY);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate progress
        float progressModifier = (1f / speed) * DEFAULT_SPEED;
        float progressOffset = localIndex / DEFAULT_FREQUENCY;
        float progress = calculateProgress(progressModifier, -progressOffset, false);

        if (progress < .01f && Math.random() > .25f && !indices.contains(localIndex))
            indices.add(localIndex);
        if (progress > .95f)
            indices.removeValue(localIndex);

        if (!indices.contains(localIndex) &&
                !indices.contains(localIndex - 1) &&
                !indices.contains(localIndex - 2) &&
                !indices.contains(localIndex + 2) &&
                !indices.contains(localIndex + 1))
            return;

        // Calculate offset
        float interpolation;
        float split = 0.5f;
        if (progress < split) {
            interpolation = Interpolation.pow2Out.apply(0, 1, progress / split);
        } else {
            interpolation = Interpolation.pow2In.apply(1, 0, (progress - split) / (1f - split));
        }
        float y = label.getLineHeight(globalIndex) * distance * interpolation * DEFAULT_DISTANCE;

        if (indices.contains(localIndex))
            y *= 2.15f;
        if (indices.contains(localIndex - 1) || indices.contains(localIndex + 1))
            y *= 1.35f;

        // Calculate fadeout
        float fadeout = calculateFadeout();
        y *= fadeout;

        // Apply changes
        label.offsets.incr(globalIndex << 1 | 1, -y);
    }

}
