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
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;

/**
 * A button with a child {@link TextraLabel} to display text.
 *
 * @author Nathan Sweet
 */
public class TextraButton extends Button {
    private TextraLabel label;
    private TextButtonStyle style;

    public TextraButton(@Null String text, Skin skin) {
        this(text, skin.get(TextButtonStyle.class));
        setSkin(skin);
    }

    public TextraButton(@Null String text, Skin skin, String styleName) {
        this(text, skin.get(styleName, TextButtonStyle.class));
        setSkin(skin);
    }

    public TextraButton(@Null String text, TextButtonStyle style) {
        this(text, style, new Font(style.font));
    }


    public TextraButton(@Null String text, Skin skin, Font replacementFont) {
        this(text, skin.get(TextButtonStyle.class), replacementFont);
        setSkin(skin);
    }

    public TextraButton(@Null String text, Skin skin, String styleName, Font replacementFont) {
        this(text, skin.get(styleName, TextButtonStyle.class), replacementFont);
        setSkin(skin);
    }

    public TextraButton(@Null String text, TextButtonStyle style, Font replacementFont) {
        super();
        setStyle(style, replacementFont);
        label = newLabel(text, replacementFont, style.fontColor);
        label.setAlignment(Align.center);
        add(label).expand().fill();
        setSize(getPrefWidth(), getPrefHeight());
    }

    protected TextraLabel newLabel(String text, LabelStyle style) {
        return new TextraLabel(text, style);
    }

    protected TextraLabel newLabel(String text, Font font, Color color) {
        return new TextraLabel(text, font, color);
    }

    public void setStyle(ButtonStyle style) {
        setStyle(style, false);
    }

    public void setStyle(ButtonStyle style, boolean makeGridGlyphs) {
        if (style == null) throw new NullPointerException("style cannot be null");
        if (!(style instanceof TextButtonStyle)) throw new IllegalArgumentException("style must be a TextButtonStyle.");
        this.style = (TextButtonStyle) style;
        super.setStyle(style);

        if (label != null) {
            TextButtonStyle textButtonStyle = (TextButtonStyle) style;
            label.setFont(new Font(textButtonStyle.font, Font.DistanceFieldType.STANDARD, 0, 0, 0, 0, makeGridGlyphs));
            if (textButtonStyle.fontColor != null) label.setColor(textButtonStyle.fontColor);
        }
    }

    public void setStyle(ButtonStyle style, Font font) {
        if (style == null) throw new NullPointerException("style cannot be null");
        if (!(style instanceof TextButtonStyle)) throw new IllegalArgumentException("style must be a TextButtonStyle.");
        this.style = (TextButtonStyle) style;
        super.setStyle(style);

        if (label != null) {
            TextButtonStyle textButtonStyle = (TextButtonStyle) style;
            label.setFont(font);
            if (textButtonStyle.fontColor != null) label.setColor(textButtonStyle.fontColor);
        }
    }

    public TextButtonStyle getStyle() {
        return style;
    }

    /**
     * Returns the appropriate label font color from the style based on the current button state.
     */
    protected @Null Color getFontColor() {
        if (isDisabled() && style.disabledFontColor != null) return style.disabledFontColor;
        if (isPressed()) {
            if (isChecked() && style.checkedDownFontColor != null) return style.checkedDownFontColor;
            if (style.downFontColor != null) return style.downFontColor;
        }
        if (isOver()) {
            if (isChecked()) {
                if (style.checkedOverFontColor != null) return style.checkedOverFontColor;
            } else {
                if (style.overFontColor != null) return style.overFontColor;
            }
        }
        boolean focused = hasKeyboardFocus();
        if (isChecked()) {
            if (focused && style.checkedFocusedFontColor != null) return style.checkedFocusedFontColor;
            if (style.checkedFontColor != null) return style.checkedFontColor;
            if (isOver() && style.overFontColor != null) return style.overFontColor;
        }
        if (focused && style.focusedFontColor != null) return style.focusedFontColor;
        return style.fontColor;
    }

    public void draw(Batch batch, float parentAlpha) {
        Color c = getFontColor();
        if(c != null) label.setColor(c);
        super.draw(batch, parentAlpha);
    }

    public void setTextraLabel(TextraLabel label) {
        if (label == null) throw new IllegalArgumentException("label cannot be null.");
        getTextraLabelCell().setActor(label);
        this.label = label;
    }

    public TextraLabel getTextraLabel() {
        return label;
    }

    public Cell<TextraLabel> getTextraLabelCell() {
        return getCell(label);
    }

    public TextraButton useIntegerPositions(boolean integer) {
        label.getFont().integerPosition = integer;
        return this;
    }

    public void setText(@Null String text) {
        label.setText(text);
    }

    public String getText() {
        return label.toString();
    }

    public String toString() {
        String name = getName();
        if (name != null) return name;
        String className = getClass().getName();
        int dotIndex = className.lastIndexOf('.');
        if (dotIndex != -1) className = className.substring(dotIndex + 1);
        return (className.indexOf('$') != -1 ? "TextraButton " : "") + className + ": " + label.toString();
    }

    /**
     * Does nothing unless the label used here is a TypingLabel; then, this will skip text progression ahead.
     */
    public void skipToTheEnd() {
        label.skipToTheEnd();
    }

}
