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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Tooltip;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import com.github.tommyettinger.textra.Styles.TextTooltipStyle;

/**
 * A tooltip that shows a TypingLabel.
 *
 * @author Nathan Sweet
 */
public class TypingTooltip extends Tooltip<TypingLabel> {
    public TypingTooltip(@Null String text, Skin skin) {
        this(text, TooltipManager.getInstance(), skin.get(TextTooltipStyle.class));
    }

    public TypingTooltip(@Null String text, Skin skin, String styleName) {
        this(text, TooltipManager.getInstance(), skin.get(styleName, TextTooltipStyle.class));
    }

    public TypingTooltip(@Null String text, TextTooltipStyle style) {
        this(text, TooltipManager.getInstance(), style);
    }

    public TypingTooltip(@Null String text, TooltipManager manager, Skin skin) {
        this(text, manager, skin.get(TextTooltipStyle.class));
    }

    public TypingTooltip(@Null String text, TooltipManager manager, Skin skin, String styleName) {
        this(text, manager, skin.get(styleName, TextTooltipStyle.class));
    }

    public TypingTooltip(@Null String text, final TooltipManager manager, TextTooltipStyle style) {
        super(null, manager);

        final TypingLabel label = newLabel(text, new Styles.LabelStyle(style.label));
        label.setAlignment(Align.center);
        label.setWrap(true);
        label.layout.setTargetWidth(style.wrapWidth);
        getContainer().setActor(label);
        getContainer().width(style.wrapWidth);
        setStyle(style);
        label.restart(text);
    }

    public TypingTooltip(@Null String text, Skin skin, Font replacementFont) {
        this(text, TooltipManager.getInstance(), skin.get(TextTooltipStyle.class), replacementFont);
    }

    public TypingTooltip(@Null String text, Skin skin, String styleName, Font replacementFont) {
        this(text, TooltipManager.getInstance(), skin.get(styleName, TextTooltipStyle.class), replacementFont);
    }

    public TypingTooltip(@Null String text, TextTooltipStyle style, Font replacementFont) {
        this(text, TooltipManager.getInstance(), style, replacementFont);
    }

    public TypingTooltip(@Null String text, TooltipManager manager, Skin skin, Font replacementFont) {
        this(text, manager, skin.get(TextTooltipStyle.class), replacementFont);
    }

    public TypingTooltip(@Null String text, TooltipManager manager, Skin skin, String styleName, Font replacementFont) {
        this(text, manager, skin.get(styleName, TextTooltipStyle.class), replacementFont);
    }

    public TypingTooltip(@Null String text, final TooltipManager manager, TextTooltipStyle style, Font replacementFont) {
        super(null, manager);

        final TypingLabel label = newLabel(text, replacementFont, style.label.fontColor);
        label.setAlignment(Align.center);
        label.setWrap(true);
        label.layout.setTargetWidth(style.wrapWidth);
        getContainer().setActor(label);
        getContainer().width(style.wrapWidth);
        setStyle(style, replacementFont);
        label.setText(text);
    }

    protected TypingLabel newLabel(String text, Styles.LabelStyle style) {
        return new TypingLabel(text, style);
    }

    protected TypingLabel newLabel(String text, Font font) {
        return new TypingLabel(text, font);
    }

    protected TypingLabel newLabel(String text, Font font, Color color) {
        return new TypingLabel(text, font, color);
    }

    public void setStyle(TextTooltipStyle style) {
        if (style == null) throw new NullPointerException("style cannot be null");
        if (style.label == null) throw new NullPointerException("style.label cannot be null");
        if (style.label.font == null) throw new NullPointerException("style.label.font cannot be null");
        setStyle(style, style.label.font);
    }

    public void setStyle(TextTooltipStyle style, Font font) {
        if (style == null) throw new NullPointerException("style cannot be null");
        Container<TypingLabel> container = getContainer();
        container.setBackground(style.background);
        container.maxWidth(style.wrapWidth);

        boolean wrap = style.wrapWidth != 0;
        container.fill(wrap);

        getActor().setFont(font, false);
        getActor().layout.targetWidth = style.wrapWidth;
        getActor().wrap = true;
        if (style.label.fontColor != null) getActor().setColor(style.label.fontColor);
        font.regenerateLayout(getActor().layout);
//        font.calculateSize(container.getActor().layout);
        getActor().setSize(getActor().layout.getWidth(), getActor().layout.getHeight());
    }

    @Override
    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        super.enter(event, x, y, pointer, fromActor);
        getContainer().getActor().restart();
        System.out.println("TypingTooltip has size " + getActor().getWidth() + "," + getActor().getHeight());
        System.out.println("Container has size " + getContainer().getWidth() + "," + getContainer().getHeight());
    }
}
