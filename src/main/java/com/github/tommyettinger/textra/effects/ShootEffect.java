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
import com.github.tommyettinger.textra.utils.NoiseUtils;

/**
 * Moves an underline or strikethrough line through part of the affected text repeatedly.
 * <br>
 * Parameters: {@code underline;strikethrough;distance;frequency}
 * <br>
 * The {@code underline} should be true if an underline should be drawn; defaults to true.
 * The {@code strikethrough} should be true if a strikethrough line should be drawn; defaults to false.
 * The {@code distance} affects how long the line(s) should be; it should be between 0 and 1, and defaults to 0.3 .
 * The {@code frequency} affects how fast the effect should move, and may be negative to reverse the direction; defaults to 1.0 .
 * <br>
 * Example usage:
 * <code>
 * {SHOOT=f;t;0.4;2}This text will draw a longer line than usual as strikethrough only, and will move more quickly.{ENDSHOOT}
 * {SHOOT=t;f;0.1;0.5}This text will draw a shorter line as underline only, and will move more slowly.{ENDSHOOT}
 * </code>
 */
public class ShootEffect extends Effect {
    private static final float DEFAULT_FREQUENCY = 3f;

    private boolean underline = true; // whether an underline should be drawn
    private boolean strikethrough = false; // whether an underline should be drawn
    private float distance = 0.3f; // How long each line should be
    private float frequency = 1; // How frequently the line should move through the text.

    public ShootEffect(TypingLabel label, String[] params) {
        super(label);

        // Underline
        if (params.length > 0) {
            underline = paramAsBoolean(params[0]);
        }

        // Strikethrough
        if (params.length > 1) {
            strikethrough = paramAsBoolean(params[1]);
        }

        // Distance
        if (params.length > 2) {
            this.distance = paramAsFloat(params[2], 0.3f);
        }

        // Frequency
        if (params.length > 3) {
            this.frequency = paramAsFloat(params[3], 1);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate lines
        float s = NoiseUtils.triangleWave((totalTime - localIndex * 0.03f) * frequency * DEFAULT_FREQUENCY);
        if(s > 1f - distance)
            label.setInWorkingLayout(globalIndex, glyph | (underline ? Font.UNDERLINE : 0L) | (strikethrough ? Font.STRIKETHROUGH : 0L));
        else
            label.setInWorkingLayout(globalIndex, glyph & (underline ? ~Font.UNDERLINE : -1L) & (strikethrough ? ~Font.STRIKETHROUGH : -1L));
    }

}
