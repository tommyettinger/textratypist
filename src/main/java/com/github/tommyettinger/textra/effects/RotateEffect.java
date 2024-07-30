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

import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.TypingLabel;

/**
 * Permanently rotates text in-place for each cell. Doesn't change over time.
 * <br>
 * Parameters: {@code rotation}
 * <br>
 * The {@code rotation} is measured in degrees counterclockwise, and defaults to 90 .
 * <br>
 * Example usage:
 * <code>
 * {ROTATE=30}Rotates characters 30 degrees counterclockwise, which would "undo" italic effects, mostly.{ENDROTATE}
 * {ROTATE=180}Rotates characters 180 degrees counterclockwise, which flips them upside-down.{ENDROTATE}
 * </code>
 */
public class RotateEffect extends Effect {
    private float rotation = 90; // how many degrees to rotate each glyph, counter-clockwise

    public RotateEffect(TypingLabel label, String[] params) {
        super(label);

        // Size X (and Y)
        if (params.length > 0) {
            this.rotation = paramAsFloat(params[0], 90.0f);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        label.rotations.incr(globalIndex, rotation);
    }

}
