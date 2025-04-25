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

import com.badlogic.gdx.math.MathUtils;
import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.TypingLabel;

/**
 * Makes the hovered-over glyph stretch on y, as well as some glyphs near it. Doesn't change over time, but does change
 * with pointer movement.
 * <br>
 * Parameters: {@code spread;stretchY}
 * <br>
 * The {@code spread} is a percentage from 0% to potentially much higher than 100%; it defaults to 500%.
 * The {@code stretchY} is a percentage from 0% (shrink to nothing) to 100% (no change) to higher, such as 200% to
 * double the height of affected glyphs.
 * <br>
 * Example usage:
 * <code>
 * {ATTENTION=1000;50}Roughly 10 glyphs in either direction will get squashed to a shorter height on mouse-over.{ENDATTENTION}
 * {ATTENTION=300;200}Roughly 3 glyphs in either direction will stretch to approach double-height on mouse-over.{ENDATTENTION}
 * </code>
 */
public class AttentionEffect extends Effect {
    private float spread = 5; // How many glyphs in either direction of the pointer to scale
    private float stretchY = 2; // How much of their height they should be expanded by on y, at most

    public AttentionEffect(TypingLabel label, String[] params) {
        super(label);
        label.trackingInput = true;

        // Spread
        if (params.length > 0) {
            this.spread = paramAsFloat(params[0], 100.0f) * 0.01f;
        }
        // Size Y
        if (params.length > 1) {
            this.stretchY = paramAsFloat(params[1], 100.0f) * 0.01f;
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        int distance = Math.abs(globalIndex - label.overIndex);
        if(label.overIndex >= 0 && distance <= spread){
            label.getSizing().incr(globalIndex << 1 | 1, (stretchY - 1f) * MathUtils.cosDeg((90f * distance) / spread));
        }
    }

}
