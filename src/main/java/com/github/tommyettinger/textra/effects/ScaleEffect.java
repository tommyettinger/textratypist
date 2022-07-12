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

import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.TypingLabel;

/**
 * Permanently sets the size of text. Doesn't change over time.
 */
public class ScaleEffect extends Effect {
    private float sizeX = 1; // How much of their width they should start expanded by on x
    private float sizeY = 2; // How much of their height they should start expanded by on y

    public ScaleEffect(TypingLabel label, String[] params) {
        super(label);

        // Size X (and Y)
        if (params.length > 0) {
            this.sizeX = paramAsFloat(params[0], 100.0f) * 0.01f;
            this.sizeY = paramAsFloat(params[0], 100.0f) * 0.01f;
        }
        // Size Y
        if (params.length > 1) {
            this.sizeY = paramAsFloat(params[1], 100.0f) * 0.01f;
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        label.sizing.incr(globalIndex << 1, sizeX - 1f);
        label.sizing.incr(globalIndex << 1 | 1, sizeY - 1f);
    }

}
