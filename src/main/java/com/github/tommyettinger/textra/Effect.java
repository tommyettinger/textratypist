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

package com.github.tommyettinger.textra;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;

/**
 * Abstract text effect.
 */
public abstract class Effect {
    private static final float FADEOUT_SPLIT = 0.25f;
    protected final TypingLabel label;
    public int indexStart = -1;
    public int indexEnd = -1;
    public float duration = Float.POSITIVE_INFINITY;
    protected float totalTime;

    public Effect(TypingLabel label) {
        this.label = label;
    }

    public void update(float delta) {
        totalTime += delta;
    }

    /**
     * Applies the effect to the given glyph.
     */
    public final void apply(long glyph, int glyphIndex, float delta) {
        int localIndex = glyphIndex - indexStart;
        onApply(glyph, localIndex, glyphIndex, delta);
    }

    /**
     * Called when this effect should be applied to the given glyph.
     */
    protected abstract void onApply(long glyph, int localIndex, int globalIndex, float delta);

    /**
     * Returns whether this effect is finished and should be removed. Note that effects are infinite by default.
     */
    public boolean isFinished() {
        return duration < 0 || totalTime > duration;
    }

    /**
     * Calculates the fadeout of this effect, if any. Only considers the second half of the duration.
     */
    protected float calculateFadeout() {
        if (duration < 0 || duration == Float.POSITIVE_INFINITY) return 1;

        // Calculate raw progress
        float progress = MathUtils.clamp(totalTime / duration, 0, 1);

        // If progress is before the split point, return a full factor
        if (progress < FADEOUT_SPLIT) return 1;

        // Otherwise calculate from the split point
        return Interpolation.smooth.apply(1, 0, (progress - FADEOUT_SPLIT) / (1f - FADEOUT_SPLIT));
    }

    /**
     * Calculates a linear progress dividing the total time by the given modifier. Returns a value between 0 and 1 that
     * loops in a ping-pong mode.
     */
    protected float calculateProgress(float modifier) {
        return calculateProgress(modifier, 0, true);
    }

    /**
     * Calculates a linear progress dividing the total time by the given modifier. Returns a value between 0 and 1 that
     * loops in a ping-pong mode.
     */
    protected float calculateProgress(float modifier, float offset) {
        return calculateProgress(modifier, offset, true);
    }

    /**
     * Calculates a linear progress dividing the total time by the given modifier. Returns a value between 0 and 1.
     */
    protected float calculateProgress(float modifier, float offset, boolean pingpong) {
        float progress = totalTime / modifier + offset;
        while (progress < 0.0f) {
            progress += 2.0f;
        }
        if (pingpong) {
            progress %= 2f;
            if (progress > 1.0f) progress = 1f - (progress - 1f);
        } else {
            progress %= 1.0f;
        }
        progress = MathUtils.clamp(progress, 0, 1);
        return progress;
    }

    /**
     * Returns a float value parsed from the given String, or the default value if the string couldn't be parsed.
     */
    protected float paramAsFloat(String str, float defaultValue) {
        return Parser.stringToFloat(str, defaultValue);
    }

    /**
     * Returns a boolean value parsed from the given String, or the default value if the string couldn't be parsed.
     */
    protected boolean paramAsBoolean(String str) {
        return Parser.stringToBoolean(str);
    }

    /**
     * Parses a color from the given string. Returns 256 if the color couldn't be parsed.
     */
    protected int paramAsColor(String str) {
        return Parser.stringToColor(label, str);
    }

}
