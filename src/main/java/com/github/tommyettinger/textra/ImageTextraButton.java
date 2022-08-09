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
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton.ImageTextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.Scaling;

/**
 * A button with a child {@link Image} and {@link TextraLabel}.
 *
 * @author Nathan Sweet
 * @see ImageButton
 * @see TextraButton
 * @see Button
 */
public class ImageTextraButton extends Button {
    private final Image image;
    private TextraLabel label;
    private ImageTextButtonStyle style;

    public ImageTextraButton(@Null String text, Skin skin) {
        this(text, skin.get(ImageTextButtonStyle.class));
        setSkin(skin);
    }

    public ImageTextraButton(@Null String text, Skin skin, String styleName) {
        this(text, skin.get(styleName, ImageTextButtonStyle.class));
        setSkin(skin);
    }

    public ImageTextraButton(@Null String text, ImageTextButtonStyle style) {
        super(style);
        this.style = style;

        defaults().space(3);

        image = newImage();

        label = newLabel(text, new LabelStyle(style.font, style.fontColor));
        label.setAlignment(Align.center);

        add(image);
        add(label);

        setStyle(style);

        setSize(getPrefWidth(), getPrefHeight());
    }

    public ImageTextraButton(@Null String text, Skin skin, Font replacementFont) {
        this(text, skin.get(ImageTextButtonStyle.class), replacementFont);
        setSkin(skin);
    }

    public ImageTextraButton(@Null String text, Skin skin, String styleName, Font replacementFont) {
        this(text, skin.get(styleName, ImageTextButtonStyle.class), replacementFont);
        setSkin(skin);
    }

    public ImageTextraButton(@Null String text, ImageTextButtonStyle style, Font replacementFont) {
        super(style);
        this.style = style;

        defaults().space(3);

        image = newImage();

        label = newLabel(text, replacementFont, style.fontColor);
        label.setAlignment(Align.center);

        add(image);
        add(label);

        setStyle(style, replacementFont);

        setSize(getPrefWidth(), getPrefHeight());
    }

    protected Image newImage() {
        return new Image(null, Scaling.fit);
    }

    protected TextraLabel newLabel(String text, LabelStyle style) {
        return new TextraLabel(text, style);
    }

    protected TextraLabel newLabel(String text, Font font, Color color) {
        return new TextraLabel(text, font, color);
    }

    public void setStyle(ButtonStyle style) {
        if (!(style instanceof ImageTextButtonStyle))
            throw new IllegalArgumentException("style must be a ImageTextButtonStyle.");
        this.style = (ImageTextButtonStyle) style;
        super.setStyle(style);

        if (image != null) updateImage();

        if (label != null) {
            ImageTextButtonStyle textButtonStyle = (ImageTextButtonStyle) style;
            label.font = new Font(textButtonStyle.font);
            Color c = getFontColor();
            if(c != null) label.setColor(c);
        }
    }

    public void setStyle(ButtonStyle style, boolean makeGridGlyphs) {
        if (!(style instanceof ImageTextButtonStyle))
            throw new IllegalArgumentException("style must be a ImageTextButtonStyle.");
        this.style = (ImageTextButtonStyle) style;
        super.setStyle(style);

        if (image != null) updateImage();

        if (label != null) {
            ImageTextButtonStyle textButtonStyle = (ImageTextButtonStyle) style;
            label.font = new Font(textButtonStyle.font, Font.DistanceFieldType.STANDARD, 0, 0, 0, 0, makeGridGlyphs);
            Color c = getFontColor();
            if(c != null) label.setColor(c);
        }
    }

    public void setStyle(ButtonStyle style, Font font) {
        if (!(style instanceof ImageTextButtonStyle))
            throw new IllegalArgumentException("style must be a ImageTextButtonStyle.");
        this.style = (ImageTextButtonStyle) style;
        super.setStyle(style);

        if (image != null) updateImage();

        if (label != null) {
            label.font = font;
            Color c = getFontColor();
            if(c != null) label.setColor(c);
        }
    }

    public ImageTextButtonStyle getStyle() {
        return style;
    }

    /**
     * Returns the appropriate image drawable from the style based on the current button state.
     */
    protected @Null Drawable getImageDrawable() {
        if (isDisabled() && style.imageDisabled != null) return style.imageDisabled;
        if (isPressed()) {
            if (isChecked() && style.imageCheckedDown != null) return style.imageCheckedDown;
            if (style.imageDown != null) return style.imageDown;
        }
        if (isOver()) {
            if (isChecked()) {
                if (style.imageCheckedOver != null) return style.imageCheckedOver;
            } else {
                if (style.imageOver != null) return style.imageOver;
            }
        }
        if (isChecked()) {
            if (style.imageChecked != null) return style.imageChecked;
            if (isOver() && style.imageOver != null) return style.imageOver;
        }
        return style.imageUp;
    }

    /**
     * Sets the image drawable based on the current button state. The default implementation sets the image drawable using
     * {@link #getImageDrawable()}.
     */
    protected void updateImage() {
        image.setDrawable(getImageDrawable());
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
        updateImage();
        Color c = getFontColor();
        if(c != null) label.setColor(c);
        super.draw(batch, parentAlpha);
    }

    public Image getImage() {
        return image;
    }

    public Cell<?> getImageCell() {
        return getCell(image);
    }

    public void setLabel(TextraLabel label) {
        getLabelCell().setActor(label);
        this.label = label;
    }

    public TextraLabel getLabel() {
        return label;
    }

    public Cell<?> getLabelCell() {
        return getCell(label);
    }

    public void setText(CharSequence text) {
        label.setText(text.toString());
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
        return (className.indexOf('$') != -1 ? "ImageTextraButton " : "") + className + ": " + image.getDrawable() + " "
                + label.toString();
    }
    
    /**
     * Does nothing unless the label used here is a TypingLabel; then, this will skip text progression ahead.
     */
    public void skipToTheEnd() {
        label.skipToTheEnd();
    }
}
