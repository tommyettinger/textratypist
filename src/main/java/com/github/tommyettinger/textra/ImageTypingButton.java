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
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.github.tommyettinger.textra.Styles.ImageTextButtonStyle;


/**
 * A button with a child {@link Image} and {@link TypingLabel}.
 *
 * @author Nathan Sweet
 * @see ImageButton
 * @see TypingButton
 * @see Button
 */
public class ImageTypingButton extends ImageTextraButton {

    public ImageTypingButton(String text, Skin skin) {
        super(text, skin);
    }

    public ImageTypingButton(String text, Skin skin, String styleName) {
        super(text, skin, styleName);
    }

    public ImageTypingButton(String text, ImageTextButtonStyle style) {
        super(text, style);
    }

    public ImageTypingButton(String text, Skin skin, Font replacementFont) {
        super(text, skin, replacementFont);
    }

    public ImageTypingButton(String text, Skin skin, String styleName, Font replacementFont) {
        super(text, skin, styleName, replacementFont);
    }

    public ImageTypingButton(String text, ImageTextButtonStyle style, Font replacementFont) {
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
