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
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Null;

/**
 * An outer class that holds all styles for TextraTypist widgets. These are each named to match a scene2d.ui style,
 * such as {@link Styles.LabelStyle} matching {@link Label.LabelStyle}. These styles are typically loaded from a skin
 * JSON file using {@link FWSkin} or one of its subclasses, but can also be created on their own. When reading in a
 * skin JSON file, no changes need to be made for TextraTypist styles if using FWSkin to load in the JSON.
 */
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

        public LabelStyle(Font font, @Null Color fontColor, @Null Drawable background) {
            this.font = font;
            this.fontColor = fontColor;
            this.background = background;
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
    public static class TextButtonStyle extends Button.ButtonStyle {
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
     * The style for an image text button, see {@link ImageTextraButton}.
     */
    public static class ImageTextButtonStyle extends TextButtonStyle {
        public @Null Drawable imageUp, imageDown, imageOver, imageDisabled;
        public @Null Drawable imageChecked, imageCheckedDown, imageCheckedOver;

        public ImageTextButtonStyle() {
        }

        public ImageTextButtonStyle(@Null Drawable up, @Null Drawable down, @Null Drawable checked, Font font) {
            super(up, down, checked, font);
        }

        public ImageTextButtonStyle(@Null Drawable up, @Null Drawable down, @Null Drawable checked, BitmapFont font) {
            super(up, down, checked, font);
        }

        public ImageTextButtonStyle(ImageTextButtonStyle style) {
            super(style);
            imageUp = style.imageUp;
            imageDown = style.imageDown;
            imageOver = style.imageOver;
            imageDisabled = style.imageDisabled;

            imageChecked = style.imageChecked;
            imageCheckedDown = style.imageCheckedDown;
            imageCheckedOver = style.imageCheckedOver;
        }

        public ImageTextButtonStyle(ImageTextButton.ImageTextButtonStyle style) {
            super(style);
            imageUp = style.imageUp;
            imageDown = style.imageDown;
            imageOver = style.imageOver;
            imageDisabled = style.imageDisabled;

            imageChecked = style.imageChecked;
            imageCheckedDown = style.imageCheckedDown;
            imageCheckedOver = style.imageCheckedOver;
        }

        public ImageTextButtonStyle(TextButtonStyle style) {
            super(style);
        }

        public ImageTextButtonStyle(TextButton.TextButtonStyle style) {
            super(style);
        }
    }

    /**
     * The style for a select box, see {@link TextraCheckBox} or {@link TypingCheckBox}.
     */
    public static class CheckBoxStyle extends TextButtonStyle {
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

    /**
     * The style for a window, see {@link TextraWindow} or {@link TypingWindow}.
     */
    public static class WindowStyle {
        public @Null Drawable background;
        public Font titleFont;
        public @Null Color titleFontColor = new Color(1, 1, 1, 1);
        public @Null Drawable stageBackground;

        public WindowStyle() {
        }

        public WindowStyle(Font titleFont, Color titleFontColor, @Null Drawable background) {
            this.titleFont = titleFont;
            this.titleFontColor.set(titleFontColor);
            this.background = background;
        }

        public WindowStyle(Font titleFont, Color titleFontColor, @Null Drawable background, @Null Drawable stageBackground) {
            this.titleFont = titleFont;
            this.titleFontColor.set(titleFontColor);
            this.background = background;
            this.stageBackground = stageBackground;
        }

        public WindowStyle(BitmapFont titleFont, Color titleFontColor, @Null Drawable background) {
            this.titleFont = new Font(titleFont);
            this.titleFontColor.set(titleFontColor);
            this.background = background;
        }

        public WindowStyle(WindowStyle style) {
            titleFont = style.titleFont;
            if (style.titleFontColor != null) titleFontColor = new Color(style.titleFontColor);
            background = style.background;
            stageBackground = style.stageBackground;
        }

        public WindowStyle(Window.WindowStyle style) {
            titleFont = new Font(style.titleFont);
            if (style.titleFontColor != null) titleFontColor = new Color(style.titleFontColor);
            background = style.background;
            stageBackground = style.stageBackground;
        }
    }

    /**
     * The style for a ListBox, see {@link TextraListBox}.
     */
    public static class ListStyle {
        public Font font;
        public Color fontColorSelected = new Color(1, 1, 1, 1);
        public Color fontColorUnselected = new Color(1, 1, 1, 1);
        public Drawable selection;
        public @Null Drawable down, over, background;

        public ListStyle() {
        }

        public ListStyle(Font font, Color fontColorSelected, Color fontColorUnselected, Drawable selection) {
            this.font = font;
            this.fontColorSelected.set(fontColorSelected);
            this.fontColorUnselected.set(fontColorUnselected);
            this.selection = selection;
        }

        public ListStyle(BitmapFont font, Color fontColorSelected, Color fontColorUnselected, Drawable selection) {
            this.font = new Font(font);
            this.fontColorSelected.set(fontColorSelected);
            this.fontColorUnselected.set(fontColorUnselected);
            this.selection = selection;
        }

        public ListStyle(Font font, Color fontColorSelected, Color fontColorUnselected, Drawable selection,
                         @Null Drawable down, @Null Drawable over, @Null Drawable background) {
            this.font = font;
            this.fontColorSelected.set(fontColorSelected);
            this.fontColorUnselected.set(fontColorUnselected);
            this.selection = selection;
            this.down = down;
            this.over = over;
            this.background = background;
        }

        public ListStyle(ListStyle style) {
            font = style.font;
            fontColorSelected.set(style.fontColorSelected);
            fontColorUnselected.set(style.fontColorUnselected);
            selection = style.selection;

            down = style.down;
            over = style.over;
            background = style.background;
        }

        public ListStyle(List.ListStyle style) {
            font = new Font(style.font);
            fontColorSelected.set(style.fontColorSelected);
            fontColorUnselected.set(style.fontColorUnselected);
            selection = style.selection;

            down = style.down;
            over = style.over;
            background = style.background;
        }
    }

    /**
     * The style for a select box, see {@code TextraSelectBox}.
     */
    public static class SelectBoxStyle {
        public Font font;
        public Color fontColor = new Color(1, 1, 1, 1);
        public @Null Color overFontColor, disabledFontColor;
        public @Null Drawable background;
        public ScrollPane.ScrollPaneStyle scrollStyle;
        public ListStyle listStyle;
        public @Null Drawable backgroundOver, backgroundOpen, backgroundDisabled;

        public SelectBoxStyle() {
        }

        public SelectBoxStyle(Font font, Color fontColor, @Null Drawable background, ScrollPane.ScrollPaneStyle scrollStyle,
                              ListStyle listStyle) {
            this.font = font;
            this.fontColor.set(fontColor);
            this.background = background;
            this.scrollStyle = scrollStyle;
            this.listStyle = listStyle;
        }

        public SelectBoxStyle(BitmapFont font, Color fontColor, @Null Drawable background, ScrollPane.ScrollPaneStyle scrollStyle,
                              List.ListStyle listStyle) {
            this.font = new Font(font);
            this.fontColor.set(fontColor);
            this.background = background;
            this.scrollStyle = scrollStyle;
            this.listStyle = new ListStyle(listStyle);
        }

        public SelectBoxStyle(SelectBoxStyle style) {
            font = new Font(style.font);
            fontColor.set(style.fontColor);

            if (style.overFontColor != null) overFontColor = new Color(style.overFontColor);
            if (style.disabledFontColor != null) disabledFontColor = new Color(style.disabledFontColor);

            background = style.background;
            scrollStyle = new ScrollPane.ScrollPaneStyle(style.scrollStyle);
            listStyle = new ListStyle(style.listStyle);

            backgroundOver = style.backgroundOver;
            backgroundOpen = style.backgroundOpen;
            backgroundDisabled = style.backgroundDisabled;
        }

        public SelectBoxStyle(SelectBox.SelectBoxStyle style) {
            font = new Font(style.font);
            fontColor.set(style.fontColor);

            if (style.overFontColor != null) overFontColor = new Color(style.overFontColor);
            if (style.disabledFontColor != null) disabledFontColor = new Color(style.disabledFontColor);

            background = style.background;
            scrollStyle = new ScrollPane.ScrollPaneStyle(style.scrollStyle);
            listStyle = new ListStyle(style.listStyle);

            backgroundOver = style.backgroundOver;
            backgroundOpen = style.backgroundOpen;
            backgroundDisabled = style.backgroundDisabled;
        }
    }

    /**
     * The style for a text tooltip, see {@link TextraTooltip} or {@link TypingTooltip}.
     */
    public static class TextTooltipStyle {
        public LabelStyle label;
        public @Null Drawable background;
        /**
         * 0 means don't wrap.
         */
        public float wrapWidth;

        public TextTooltipStyle() {
        }

        public TextTooltipStyle(LabelStyle label, @Null Drawable background) {
            this.label = label;
            this.background = background;
        }

        public TextTooltipStyle(Label.LabelStyle label, @Null Drawable background) {
            this.label = new LabelStyle(label);
            this.background = background;
        }

        public TextTooltipStyle(TextTooltipStyle style) {
            label = new LabelStyle(style.label);
            background = style.background;
            wrapWidth = style.wrapWidth;
        }

        public TextTooltipStyle(TextTooltip.TextTooltipStyle style) {
            label = new LabelStyle(style.label);
            background = style.background;
            wrapWidth = style.wrapWidth;
        }
    }

    /**
     * The style for a text field, see {@link TextraField}.
     */
    public static class TextFieldStyle {
        public Font font;
        public Color fontColor;
        public @Null Color focusedFontColor, disabledFontColor;
        public @Null Drawable background, focusedBackground, disabledBackground, cursor, selection;
//        public @Null Font messageFont;
        public @Null Color messageFontColor;

        public TextFieldStyle() {
        }

        public TextFieldStyle(Font font, Color fontColor, @Null Drawable cursor, @Null Drawable selection,
                              @Null Drawable background) {
            this.font = new Font(font);
            this.fontColor = fontColor;
            this.cursor = cursor;
            this.selection = selection;
            this.background = background;
        }


        public TextFieldStyle(BitmapFont font, Color fontColor, @Null Drawable cursor, @Null Drawable selection,
                              @Null Drawable background) {
            this.font = new Font(font);
            this.fontColor = fontColor;
            this.cursor = cursor;
            this.selection = selection;
            this.background = background;
        }

        public TextFieldStyle(TextFieldStyle style) {
            font = new Font(style.font);
            if (style.fontColor != null) fontColor = new Color(style.fontColor);
            if (style.focusedFontColor != null) focusedFontColor = new Color(style.focusedFontColor);
            if (style.disabledFontColor != null) disabledFontColor = new Color(style.disabledFontColor);

            background = style.background;
            focusedBackground = style.focusedBackground;
            disabledBackground = style.disabledBackground;
            cursor = style.cursor;
            selection = style.selection;

//            messageFont = style.messageFont;
            if (style.messageFontColor != null) messageFontColor = new Color(style.messageFontColor);
        }

        public TextFieldStyle(TextField.TextFieldStyle style) {
            font = new Font(style.font);
            if (style.fontColor != null) fontColor = new Color(style.fontColor);
            if (style.focusedFontColor != null) focusedFontColor = new Color(style.focusedFontColor);
            if (style.disabledFontColor != null) disabledFontColor = new Color(style.disabledFontColor);

            background = style.background;
            focusedBackground = style.focusedBackground;
            disabledBackground = style.disabledBackground;
            cursor = style.cursor;
            selection = style.selection;

//            if (style.messageFont != null)
//                messageFont = style.font == style.messageFont ? font : new Font(style.messageFont);
            if (style.messageFontColor != null) messageFontColor = new Color(style.messageFontColor);
        }
    }
}
