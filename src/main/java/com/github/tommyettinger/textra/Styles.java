/*
 * Copyright (c) 2024 See AUTHORS file.
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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Null;

public final class Styles {
    private Styles() {}


    /**
     * The style for a {@link TextraLabel} or {@link TypingLabel}.
     */
    public static class LabelStyle {
        public Font font;
        public @Null Color fontColor;
        public @Null Drawable background;

        public LabelStyle () {
        }

        public LabelStyle (Font font, @Null Color fontColor) {
            this.font = font;
            this.fontColor = fontColor;
        }

        public LabelStyle (BitmapFont font, @Null Color fontColor) {
            this.font = new Font(font);
            this.fontColor = fontColor;
        }

        public LabelStyle (LabelStyle style) {
            font = style.font;
            if (style.fontColor != null) fontColor = new Color(style.fontColor);
            background = style.background;
        }

        public LabelStyle (Label.LabelStyle style) {
            font = new Font(style.font);
            if (style.fontColor != null) fontColor = new Color(style.fontColor);
            background = style.background;
        }
    }

}
