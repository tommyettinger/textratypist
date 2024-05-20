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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

/**
 * A table that can be dragged and act as a modal window. The top padding is used as the window's title height. The
 * title uses a {@link TypingLabel} and will by default draw gradually.
 * <p>
 * The preferred size of a window is the preferred size of the title text and the children as laid out by the table. After adding
 * children to the window, it can be convenient to call {@link #pack()} to size the window to the size of the children.
 *
 * @author Nathan Sweet
 */
public class TypingWindow extends TextraWindow {
    public TypingWindow(String title, Skin skin) {
        super(title, skin);
    }

    public TypingWindow(String title, Skin skin, String styleName) {
        super(title, skin, styleName);
    }

    public TypingWindow(String title, Styles.WindowStyle style) {
        super(title, style);
    }

    public TypingWindow(String title, Styles.WindowStyle style, boolean makeGridGlyphs) {
        super(title, style, makeGridGlyphs);
    }

    public TypingWindow(String title, Skin skin, Font replacementFont) {
        super(title, skin, replacementFont);
    }

    public TypingWindow(String title, Skin skin, String styleName, Font replacementFont) {
        super(title, skin, styleName, replacementFont);
    }

    public TypingWindow(String title, Skin skin, String styleName, Font replacementFont, boolean scaleTitleFont) {
        super(title, skin, styleName, replacementFont, scaleTitleFont);
    }

    public TypingWindow(String title, Styles.WindowStyle style, Font replacementFont) {
        super(title, style, replacementFont);
    }

    public TypingWindow(String title, Styles.WindowStyle style, Font replacementFont, boolean scaleTitleFont) {
        super(title, style, replacementFont, scaleTitleFont);
    }

    @Override
    protected TextraLabel newLabel(String text, Styles.LabelStyle style) {
        return new TypingLabel(text, style);
    }

    @Override
    protected TextraLabel newLabel(String text, Font font, Color color) {
        return new TypingLabel(text, font, color);
    }
}
