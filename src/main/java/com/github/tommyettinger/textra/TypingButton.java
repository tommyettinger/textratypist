/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.github.tommyettinger.textra.Styles.TextButtonStyle;

/**
 * A button with a child {@link TypingLabel} to display text.
 */
public class TypingButton extends TextraButton {

    public TypingButton(String text, Skin skin) {
        super(text, skin);
    }

    public TypingButton(String text, Skin skin, String styleName) {
        super(text, skin, styleName);
    }

    public TypingButton(String text, TextButtonStyle style) {
        super(text, style);
    }

    public TypingButton(String text, Skin skin, Font replacementFont) {
        super(text, skin, replacementFont);
    }

    public TypingButton(String text, Skin skin, String styleName, Font replacementFont) {
        super(text, skin, styleName, replacementFont);
    }

    public TypingButton(String text, TextButtonStyle style, Font replacementFont) {
        super(text, style, replacementFont);
    }

    @Override
    protected TypingLabel newLabel(String text, Styles.LabelStyle style) {
        return new TypingLabel(text, style);
    }

    @Override
    protected TypingLabel newLabel(String text, Font font, Color color) {
        return new TypingLabel(text, font, color);
    }
}
