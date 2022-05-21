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

import com.badlogic.gdx.utils.TimeUtils;
import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.TypingLabel;
import com.github.tommyettinger.textra.utils.NoiseUtils;

/** Rotates each glyph slightly back and forth, each one independently. Distance is measured in degrees. */
public class CrowdEffect extends Effect {
    private static final float DEFAULT_DISTANCE  = 1f;
    private static final float DEFAULT_INTENSITY = 0.001f;

    private float distance  = 15; // How many degrees a glyph can rotate, clockwise or counterclockwise
    private float intensity = 1; // How fast the glyphs should move

    public CrowdEffect(TypingLabel label, String[] params) {
        super(label);

        // Distance
        if(params.length > 0) {
            this.distance = paramAsFloat(params[0], 15);
        }

        // Intensity
        if(params.length > 1) {
            this.intensity = paramAsFloat(params[1], 1);
        }

        // Duration
        if(params.length > 2) {
            this.duration = paramAsFloat(params[2], -1);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate offset
        float rot = NoiseUtils.octaveNoise1D((TimeUtils.millis() & 0xFFFFFF) * intensity * DEFAULT_INTENSITY, globalIndex) * distance * DEFAULT_DISTANCE;

        // Calculate fadeout
        float fadeout = calculateFadeout();
        rot *= fadeout;

        // Apply changes
        label.rotations.incr(globalIndex, rot);
    }

}
