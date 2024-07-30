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
import com.github.tommyettinger.textra.TypingConfig;
import com.github.tommyettinger.textra.TypingLabel;

/**
 * Makes a span of text appear instantly, without the typing delay taking place per-glyph. Text after the span will be
 * set to a speed of {@link TypingConfig#DEFAULT_SPEED_PER_CHAR}. Doesn't change over time and doesn't have parameters.
 * <br>
 * Parameters: {@code } (this has no parameters)
 * <br>
 * <br>
 * Example usage:
 * <code>
 * {INSTANT}This text will show up all at once.{ENDINSTANT}
 * </code>
 */
public class InstantEffect extends Effect {
    public InstantEffect(TypingLabel label, String[] params) {
        super(label);
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        if(indexEnd >= 0)
        {
            label.setTextSpeed(TypingConfig.DEFAULT_SPEED_PER_CHAR);
        }
        else
            label.setTextSpeed(0f);
    }

}
