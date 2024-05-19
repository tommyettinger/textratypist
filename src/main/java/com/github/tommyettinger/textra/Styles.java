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
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Null;

public final class Styles {
    private Styles() {
    }


    /**
     * The style for a {@link TextraLabel} or {@link TypingLabel}.
     */
    public static class LabelStyle {
        public Font font;
        public @Null Color fontColor;
        public @Null Drawable background;

        public LabelStyle() {
        }

        public LabelStyle(Font font, @Null Color fontColor) {
            this.font = font;
            this.fontColor = fontColor;
        }

        public LabelStyle(BitmapFont font, @Null Color fontColor) {
            this.font = new Font(font);
            this.fontColor = fontColor;
        }

        public LabelStyle(LabelStyle style) {
            font = style.font;
            if (style.fontColor != null) fontColor = new Color(style.fontColor);
            background = style.background;
        }

        public LabelStyle(Label.LabelStyle style) {
            font = new Font(style.font);
            if (style.fontColor != null) fontColor = new Color(style.fontColor);
            background = style.background;
        }
    }

    /**
     * The style for a text button, see {@link TextraButton} or {@link TypingButton}.
     */
    static public class TextButtonStyle extends Button.ButtonStyle {
        public @Null Font font;
        public @Null Color fontColor;
        public @Null Color downFontColor;
        public @Null Color overFontColor;
        public @Null Color focusedFontColor;
        public @Null Color disabledFontColor;
        public @Null Color checkedFontColor;
        public @Null Color checkedDownFontColor;
        public @Null Color checkedOverFontColor;
        public @Null Color checkedFocusedFontColor;

        public TextButtonStyle() {
        }

        public TextButtonStyle(@Null Drawable up, @Null Drawable down, @Null Drawable checked, @Null Font font) {
            super(up, down, checked);
            this.font = font;
        }

        public TextButtonStyle(@Null Drawable up, @Null Drawable down, @Null Drawable checked, @Null BitmapFont font) {
            super(up, down, checked);
            this.font = new Font(font);
        }

        public TextButtonStyle(TextButtonStyle style) {
            super(style);
            font = style.font;

            if (style.fontColor != null) fontColor = new Color(style.fontColor);
            if (style.downFontColor != null) downFontColor = new Color(style.downFontColor);
            if (style.overFontColor != null) overFontColor = new Color(style.overFontColor);
            if (style.focusedFontColor != null) focusedFontColor = new Color(style.focusedFontColor);
            if (style.disabledFontColor != null) disabledFontColor = new Color(style.disabledFontColor);

            if (style.checkedFontColor != null) checkedFontColor = new Color(style.checkedFontColor);
            if (style.checkedDownFontColor != null) checkedDownFontColor = new Color(style.checkedDownFontColor);
            if (style.checkedOverFontColor != null) checkedOverFontColor = new Color(style.checkedOverFontColor);
            if (style.checkedFocusedFontColor != null)
                checkedFocusedFontColor = new Color(style.checkedFocusedFontColor);
        }

        public TextButtonStyle(TextButton.TextButtonStyle style) {
            super(style);
            font = new Font(style.font);

            if (style.fontColor != null) fontColor = new Color(style.fontColor);
            if (style.downFontColor != null) downFontColor = new Color(style.downFontColor);
            if (style.overFontColor != null) overFontColor = new Color(style.overFontColor);
            if (style.focusedFontColor != null) focusedFontColor = new Color(style.focusedFontColor);
            if (style.disabledFontColor != null) disabledFontColor = new Color(style.disabledFontColor);

            if (style.checkedFontColor != null) checkedFontColor = new Color(style.checkedFontColor);
            if (style.checkedDownFontColor != null) checkedDownFontColor = new Color(style.checkedDownFontColor);
            if (style.checkedOverFontColor != null) checkedOverFontColor = new Color(style.checkedOverFontColor);
            if (style.checkedFocusedFontColor != null)
                checkedFocusedFontColor = new Color(style.checkedFocusedFontColor);
        }
    }

    /**
     * The style for a select box, see {@link TextraCheckBox} or {@link TypingCheckBox}.
     */
    static public class CheckBoxStyle extends TextButtonStyle {
        public Drawable checkboxOn;
        public Drawable checkboxOff;
        public @Null Drawable checkboxOnOver;
        public @Null Drawable checkboxOver;
        public @Null Drawable checkboxOnDisabled;
        public @Null Drawable checkboxOffDisabled;

        public CheckBoxStyle() {
        }

        public CheckBoxStyle(Drawable checkboxOff, Drawable checkboxOn, Font font, @Null Color fontColor) {
            this.checkboxOff = checkboxOff;
            this.checkboxOn = checkboxOn;
            this.font = font;
            this.fontColor = fontColor;
        }

        public CheckBoxStyle(Drawable checkboxOff, Drawable checkboxOn, BitmapFont font, @Null Color fontColor) {
            this.checkboxOff = checkboxOff;
            this.checkboxOn = checkboxOn;
            this.font = new Font(font);
            this.fontColor = fontColor;
        }

        public CheckBoxStyle(CheckBoxStyle style) {
            super(style);
            checkboxOff = style.checkboxOff;
            checkboxOn = style.checkboxOn;

            checkboxOnOver = style.checkboxOnOver;
            checkboxOver = style.checkboxOver;
            checkboxOnDisabled = style.checkboxOnDisabled;
            checkboxOffDisabled = style.checkboxOffDisabled;
        }

        public CheckBoxStyle(CheckBox.CheckBoxStyle style) {
            super(style);
            checkboxOff = style.checkboxOff;
            checkboxOn = style.checkboxOn;

            checkboxOnOver = style.checkboxOnOver;
            checkboxOver = style.checkboxOver;
            checkboxOnDisabled = style.checkboxOnDisabled;
            checkboxOffDisabled = style.checkboxOffDisabled;
        }
    }
}
