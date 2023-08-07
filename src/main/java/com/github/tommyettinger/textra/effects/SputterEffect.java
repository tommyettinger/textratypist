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

import com.badlogic.gdx.utils.TimeUtils;
import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.TypingLabel;
import com.github.tommyettinger.textra.utils.NoiseUtils;

/**
 * Scales each glyph randomly, with each one scaled independently.
 */
public class SputterEffect extends Effect {
    private static final float DEFAULT_WIDEN = 5;
    private static final float DEFAULT_HEIGHTEN = 5;
    private static final float DEFAULT_INTENSITY = 0.001f;

    private float widen = 0.25f; // What fraction of the original width a glyph is allows to stretch or shrink by
    private float heighten = 0.25f; // What fraction of the original height a glyph is allows to stretch or shrink by
    private float intensity = 1; // How fast the glyphs should move

    public SputterEffect(TypingLabel label, String[] params) {
        super(label);

        // x size change
        if (params.length > 0) {
            this.widen = paramAsFloat(params[0], 0.25f);
        }

        // y size change
        if (params.length > 1) {
            this.heighten = paramAsFloat(params[1], 0.25f);
        }

        // Intensity
        if (params.length > 2) {
            this.intensity = paramAsFloat(params[2], 1);
        }

        // Duration
        if (params.length > 3) {
            this.duration = paramAsFloat(params[3], Float.POSITIVE_INFINITY);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate offset

        // horizontal
        float h = NoiseUtils.octaveNoise1D((TimeUtils.millis() & 0xFFFFFF) * intensity * DEFAULT_INTENSITY + globalIndex * 0.1f, globalIndex);
        // vertical
        float v = NoiseUtils.octaveNoise1D((TimeUtils.millis() & 0xFFFFFF) * intensity * DEFAULT_INTENSITY + globalIndex * 0.1f, ~globalIndex);

        float hSharp = h * h * h * widen * DEFAULT_WIDEN - v * 0.25f;
        float vSharp = v * v * v * heighten * DEFAULT_HEIGHTEN - h * 0.25f;

        // Calculate fadeout
        float fadeout = calculateFadeout();
        hSharp *= fadeout;
        vSharp *= fadeout;

        // Apply changes
        label.sizing.incr(globalIndex << 1, hSharp);
        label.sizing.incr(globalIndex << 1 | 1, vSharp);

    }

}
