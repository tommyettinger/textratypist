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
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.TypingLabel;
import regexodus.Category;

/**
 * Permanently changes lower-case letters to squashed upper-case letters. Doesn't change over time.
 */
public class SmallCapsEffect extends Effect {
    public SmallCapsEffect(TypingLabel label, String[] params) {
        super(label);
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        char c = (char) glyph;
        if(Category.Ll.contains(c)){
            float mul = Font.extractScale(glyph) * 0.75f;
            label.setInWorkingLayout(globalIndex, Font.applyChar(Font.applyScale(glyph, mul), Category.caseUp(c)));
        }
    }

}
