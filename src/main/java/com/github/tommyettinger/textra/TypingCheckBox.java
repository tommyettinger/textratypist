/*
 * Copyright (c) 2022 See AUTHORS file.
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

package com.github.tommyettinger.textra;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
/**
 * A checkbox is a button that contains an image indicating the checked or unchecked state and a {@link TypingLabel}.
 *
 * @author Nathan Sweet
 */
public class TypingCheckBox extends TextraCheckBox {

    public TypingCheckBox(String text, Skin skin) {
        super(text, skin);
    }

    public TypingCheckBox(String text, Skin skin, String styleName) {
        super(text, skin, styleName);
    }

    public TypingCheckBox(String text, CheckBox.CheckBoxStyle style) {
        super(text, style);
    }

    public TypingCheckBox(String text, Skin skin, Font replacementFont) {
        super(text, skin, replacementFont);
    }

    public TypingCheckBox(String text, Skin skin, String styleName, Font replacementFont) {
        super(text, skin, styleName, replacementFont);
    }

    public TypingCheckBox(String text, CheckBox.CheckBoxStyle style, Font replacementFont) {
        super(text, style, replacementFont);
    }

    @Override
    protected TextraLabel newLabel(String text, Label.LabelStyle style) {
        return new TypingLabel(text, style);
    }

    @Override
    protected TextraLabel newLabel(String text, Font font, Color color) {
        return new TypingLabel(text, font, color);
    }
}
