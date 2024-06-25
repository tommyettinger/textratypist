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
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.TypingLabel;

/**
 * Enables style properties for the single glyph (or affected text, if all is true) underneath the pointer/mouse, and
 * disables them when not underneath.
 */
public class StylistEffect extends Effect {
    private long effects = 0L;//Font.BOLD | Font.OBLIQUE | Font.UNDERLINE | Font.STRIKETHROUGH | Font.SUPERSCRIPT;
    private boolean all = false; // Whether this should stylize the whole responsive area.

    public StylistEffect(TypingLabel label, String[] params) {
        super(label);
        label.trackingInput = true;

        // Bold
        if (params.length > 0) {
            if(paramAsBoolean(params[0]))
                effects |= Font.BOLD;
        }

        // Oblique
        if (params.length > 1) {
            if(paramAsBoolean(params[1]))
                effects |= Font.OBLIQUE;
        }

        // Underline
        if (params.length > 2) {
            if(paramAsBoolean(params[2]))
                effects |= Font.UNDERLINE;
        }

        // Strikethrough
        if (params.length > 3) {
            if(paramAsBoolean(params[3]))
                effects |= Font.STRIKETHROUGH;
        }

        // Script
        if (params.length > 4) {
            effects |= ((long)paramAsFloat(params[4], 0) & 3L) << 25;
        }

        // All
        if (params.length > 5) {
            this.all = paramAsBoolean(params[5]);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        if(all) {
            if(label.overIndex < indexStart || label.overIndex > indexEnd) {
                label.setInWorkingLayout(globalIndex, (glyph & ~effects));
                return;
            }
        }
        else {
            if(label.overIndex != globalIndex) {
                label.setInWorkingLayout(globalIndex, (glyph & ~effects));
                return;
            }
        }
        // Calculate progress
        label.setInWorkingLayout(globalIndex, (glyph & ~effects) | effects);
    }

}
