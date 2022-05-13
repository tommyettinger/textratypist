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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip.TextTooltipStyle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;

/** A tooltip that shows a TextraLabel.
 * @author Nathan Sweet */
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
		super(null, manager);

		final TextraLabel label = newLabel(text, style.label);
		label.setAlignment(Align.center);
		label.setWrap(true);
		getContainer().setActor(label);
		getContainer().width(label.layout.getWidth());

		setStyle(style);
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
		super(null, manager);

		final TextraLabel label = newLabel(text, replacementFont, style.label.fontColor);
		label.setAlignment(Align.center);
		label.setWrap(true);
		getContainer().setActor(label);
		getContainer().width(label.layout.getWidth());
		setStyle(style, replacementFont);
	}

	protected TextraLabel newLabel (String text, LabelStyle style) {
		return new TextraLabel(text, style);
	}

	protected TextraLabel newLabel (String text, Font font) {
		return new TextraLabel(text, font);
	}

	protected TextraLabel newLabel (String text, Font font, Color color) {
		return color == null ? new TextraLabel(text, font) : new TextraLabel(text, font, color);
	}

	public void setStyle (TextTooltipStyle style) {
		setStyle(style, false);
	}

	public void setStyle (TextTooltipStyle style, boolean makeGridGlyphs) {
		if (style == null) throw new NullPointerException("style cannot be null");
		Container<TextraLabel> container = getContainer();
		container.getActor().font = new Font(style.label.font, Font.DistanceFieldType.STANDARD, 0, 0, 0, 0, makeGridGlyphs);
		container.getActor().layout.targetWidth = style.wrapWidth;
		if(style.label.fontColor != null) container.getActor().setColor(style.label.fontColor);
		container.getActor().font.regenerateLayout(container.getActor().layout);
		container.getActor().font.calculateSize(container.getActor().layout);
		container.getActor().setWidth(container.getActor().layout.getWidth());
		container.setBackground(style.background);
		container.maxWidth(style.wrapWidth);
	}

	public void setStyle (TextTooltipStyle style, Font font) {
		if (style == null) throw new NullPointerException("style cannot be null");
		Container<TextraLabel> container = getContainer();
		container.getActor().font = font;
		container.getActor().layout.targetWidth = style.wrapWidth;
		if(style.label.fontColor != null) container.getActor().setColor(style.label.fontColor);
		font.regenerateLayout(container.getActor().layout);
		font.calculateSize(container.getActor().layout);
		container.getActor().setWidth(container.getActor().layout.getWidth());
		container.setBackground(style.background);
		container.maxWidth(style.wrapWidth);
	}
}
