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
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Tooltip;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import com.github.tommyettinger.textra.Styles.TextTooltipStyle;

/**
 * A tooltip that shows a TextraLabel.
 *
 * @author Nathan Sweet
 */
public class TextraTooltip extends Tooltip<TextraLabel> {
    public TextraTooltip(@Null String text, Skin skin) {
        this(text, TooltipManager.getInstance(), skin.get(TextTooltipStyle.class));
    }

    public TextraTooltip(@Null String text, Skin skin, String styleName) {
        this(text, TooltipManager.getInstance(), skin.get(styleName, TextTooltipStyle.class));
    }

    public TextraTooltip(@Null String text, TextTooltipStyle style) {
        this(text, TooltipManager.getInstance(), style);
    }

    public TextraTooltip(@Null String text, TooltipManager manager, Skin skin) {
        this(text, manager, skin.get(TextTooltipStyle.class));
    }

    public TextraTooltip(@Null String text, TooltipManager manager, Skin skin, String styleName) {
        this(text, manager, skin.get(styleName, TextTooltipStyle.class));
    }

    public TextraTooltip(@Null String text, final TooltipManager manager, TextTooltipStyle style) {
        this(text, manager, style, style.label.font);
    }

    public TextraTooltip(@Null String text, Skin skin, Font replacementFont) {
        this(text, TooltipManager.getInstance(), skin.get(TextTooltipStyle.class), replacementFont);
    }

    public TextraTooltip(@Null String text, Skin skin, String styleName, Font replacementFont) {
        this(text, TooltipManager.getInstance(), skin.get(styleName, TextTooltipStyle.class), replacementFont);
    }

    public TextraTooltip(@Null String text, TextTooltipStyle style, Font replacementFont) {
        this(text, TooltipManager.getInstance(), style, replacementFont);
    }

    public TextraTooltip(@Null String text, TooltipManager manager, Skin skin, Font replacementFont) {
        this(text, manager, skin.get(TextTooltipStyle.class), replacementFont);
    }

    public TextraTooltip(@Null String text, TooltipManager manager, Skin skin, String styleName, Font replacementFont) {
        this(text, manager, skin.get(styleName, TextTooltipStyle.class), replacementFont);
    }

    public TextraTooltip(@Null String text, final TooltipManager manager, TextTooltipStyle style, Font replacementFont) {
        super(new TextraLabel(text, style.label, replacementFont), manager);

        getActor().setAlignment(Align.center);
        getActor().setWrap(true);
//        if (style.label.fontColor != null) getActor().setColor(style.label.fontColor);
//        label.layout.setTargetWidth(style.wrapWidth);
        getContainer().width(style.wrapWidth).background(style.background);
//        setStyle(style, replacementFont);
        System.out.println("Created a TextraTooltip with wrapWidth " + style.wrapWidth);
    }

    private static TextraLabel newLabel(String text, Styles.LabelStyle style) {
        return new TextraLabel(text, style);
    }

    private static TextraLabel newLabel(String text, Font font) {
        return new TextraLabel(text, font);
    }

    private static TextraLabel newLabel(String text, Font font, Color color) {
        return color == null ? new TextraLabel(text, font) : new TextraLabel(text, font, color);
    }

    public void setStyle(TextTooltipStyle style) {
        if (style == null) throw new NullPointerException("style cannot be null");
        Container<TextraLabel> container = getContainer();
        // we don't want to regenerate the layout yet, so the last parameter is false.
        getActor().setFont(style.label.font, false);
        // we set the target width first.
//        getActor().layout.targetWidth = style.wrapWidth;
        if (style.label.fontColor != null) getActor().setColor(style.label.fontColor);
        // and then we can regenerate the layout.
        getActor().getFont().regenerateLayout(getActor().layout);
//        getActor().getFont().calculateSize(getActor().layout);
        getActor().setSize(getActor().layout.getWidth(), getActor().layout.getHeight());
        container.setBackground(style.background);
        container.width(style.wrapWidth);
    }

    public void setStyle(TextTooltipStyle style, Font font) {
        if (style == null) throw new NullPointerException("style cannot be null");
        Container<TextraLabel> container = getContainer();
        getActor().setFont(font, false);
        getActor().layout.targetWidth = style.wrapWidth;
        if (style.label.fontColor != null) getActor().setColor(style.label.fontColor);
        font.regenerateLayout(getActor().layout);
//        font.calculateSize(getActor().layout);
        getActor().setSize(getActor().layout.getWidth(), getActor().layout.getHeight());
        container.setBackground(style.background);
        container.maxWidth(style.wrapWidth);
    }

    /**
     * Does nothing unless the label used here is a TypingLabel; then, this will skip text progression ahead.
     */
    public void skipToTheEnd() {
        getContainer().getActor().skipToTheEnd();
    }

}
