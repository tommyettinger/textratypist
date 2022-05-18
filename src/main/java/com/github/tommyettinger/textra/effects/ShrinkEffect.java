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

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntFloatMap;
import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.TypingLabel;
import com.github.tommyettinger.textra.utils.ColorUtils;

/** Shrinks each glyph from large at the start to normal-sized at the end. Doesn't repeat itself. */
public class ShrinkEffect extends Effect {
    private float initial      = 250; // Initial size as a percentage.
    private float fadeDuration = 1; // Duration of the shrink effect

    private IntFloatMap timePassedByGlyphIndex = new IntFloatMap();

    public ShrinkEffect(TypingLabel label, String[] params) {
        super(label);

        // Initial size
        if(params.length > 0) {
            this.initial = paramAsFloat(params[0], 250);
        }

        // Duration
        if(params.length > 1) {
            this.fadeDuration = paramAsFloat(params[1], 1);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate progress
        float timePassed = timePassedByGlyphIndex.getAndIncrement(localIndex, 0, delta);
        float progress = MathUtils.clamp(timePassed / fadeDuration, 0, 1);
        label.setInWorkingLayout(globalIndex,
                    (glyph & 0xFFFFFFFFFF0FFFFFL) | (long) ((int)((MathUtils.lerp(initial, 100, progress) - 24f) / 25f) & 15) << 20);
    }

}
