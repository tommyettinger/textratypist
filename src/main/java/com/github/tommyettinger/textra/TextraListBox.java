/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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
 ******************************************************************************/

package com.github.tommyettinger.textra;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ArraySelection;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Cullable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.*;
import com.github.tommyettinger.textra.Styles.ListStyle;
import regexodus.Matcher;
import regexodus.Pattern;
import regexodus.REFlags;

/**
 * A TextraListBox (based on {@link com.badlogic.gdx.scenes.scene2d.ui.List}) displays {@link TextraLabel}s and
 * highlights the currently selected item.
 * <p>
 * A {@link ChangeEvent} is fired when the list selection changes.
 * <p>
 * The preferred size of the list is determined by the text bounds of the items and the size of the {@link ListStyle#selection}.
 * @author mzechner
 * @author Nathan Sweet */
public class TextraListBox<T extends TextraLabel> extends Widget implements Cullable {
	protected ListStyle style;
	protected final Array<T> items = new Array<>();
	private ArraySelection<T> selection = new ArraySelection<>(items);
	private Rectangle cullingArea;
	private float prefWidth, prefHeight;
	private int alignment = Align.left;
	public int pressedIndex = -1, overIndex = -1;
	private final InputListener keyListener;
	public boolean typeToSelect;

	public TextraListBox(Skin skin) {
		this(skin.get(ListStyle.class));
	}

	public TextraListBox(Skin skin, String styleName) {
		this(skin.get(styleName, ListStyle.class));
	}

	public TextraListBox(ListStyle style) {
		selection.setActor(this);
		selection.setRequired(true);

		setStyle(style);
		setSize(getPrefWidth(), getPrefHeight());

		addListener(keyListener = new InputListener() {
			long typeTimeout;
			String prefix;

			public boolean keyDown (InputEvent event, int keycode) {
				if (items.isEmpty()) return false;
				int index;
				switch (keycode) {
					case Keys.A:
						if (UIUtils.ctrl() && selection.getMultiple()) {
							selection.clear();
							selection.addAll(items);
							return true;
						}
						break;
					case Keys.HOME:
						setSelectedIndex(0);
						return true;
					case Keys.END:
						setSelectedIndex(items.size - 1);
						return true;
					case Keys.DOWN:
						index = items.indexOf(getSelected(), false) + 1;
						if (index >= items.size) index = 0;
						setSelectedIndex(index);
						return true;
					case Keys.UP:
						index = items.indexOf(getSelected(), false) - 1;
						if (index < 0) index = items.size - 1;
						setSelectedIndex(index);
						return true;
					case Keys.ESCAPE:
						if (getStage() != null) getStage().setKeyboardFocus(null);
						return true;
				}
				return false;
			}

			public boolean keyTyped (InputEvent event, char character) {
				if (!typeToSelect) return false;
				long time = System.currentTimeMillis();
				if (time > typeTimeout) prefix = "^(?:(?:({=brace}[\\[\\{])[^\\]\\}]*{\\:brace})|\\p{G}+)*";
				typeTimeout = time + 300;
				if(character == '\\') prefix += "\\\\";
				else if(character == '?') prefix += "\\?";
				else if(character == '*') prefix += "\\*";
				else if(character == '+') prefix += "\\+";
				else if(character == '|') prefix += "\\|";
				else if(character == '(') prefix += "\\(";
				else if(character == ')') prefix += "\\)";
				else if(character == '[') prefix += "\\[";
				else if(character == ']') prefix += "\\]";
				else if(character == '{') prefix += "\\{";
				else if(character == '}') prefix += "\\}";
				else prefix += Character.toLowerCase(character);
				Matcher matcher = Pattern.compile(prefix, REFlags.IGNORE_CASE | REFlags.UNICODE).matcher();
				for (int i = 0, n = items.size; i < n; i++) {
					matcher.setTarget(items.get(i).storedText);
					if (matcher.matchesPrefix()) {
						setSelectedIndex(i);
						break;
					}
				}
				return false;
			}
		});

		addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (pointer != 0 || button != 0) return true;
				if (selection.isDisabled()) return true;
				if (getStage() != null) getStage().setKeyboardFocus(TextraListBox.this);
				if (items.size == 0) return true;
				int index = getItemIndexAt(y);
				if (index == -1) return true;
				selection.choose(items.get(index));
				pressedIndex = index;
				return true;
			}

			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				if (pointer != 0 || button != 0) return;
				pressedIndex = -1;
			}

			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				overIndex = getItemIndexAt(y);
			}

			public boolean mouseMoved (InputEvent event, float x, float y) {
				overIndex = getItemIndexAt(y);
				return false;
			}

			public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {
				if (pointer == 0) pressedIndex = -1;
				if (pointer == -1) overIndex = -1;
			}
		});
	}

	public void setStyle (ListStyle style) {
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.style = style;
		invalidateHierarchy();
	}

	/** Returns the list's style. Modifying the returned style may not have an effect until {@link #setStyle(ListStyle)} is
	 * called. */
	public ListStyle getStyle () {
		return style;
	}

	public void layout () {
		Drawable selectedDrawable = style.selection;

		prefHeight = selectedDrawable.getTopHeight() + selectedDrawable.getBottomHeight();

		prefWidth = 0;
		for (int i = 0; i < items.size; i++) {
			prefWidth = Math.max(items.get(i).getPrefWidth(), prefWidth);
			prefHeight += items.get(i).getPrefHeight();
		}
		prefWidth += selectedDrawable.getLeftWidth() + selectedDrawable.getRightWidth();

		Drawable background = style.background;
		if (background != null) {
			prefWidth = Math.max(prefWidth + background.getLeftWidth() + background.getRightWidth(), background.getMinWidth());
			prefHeight = Math.max(prefHeight + background.getTopHeight() + background.getBottomHeight(), background.getMinHeight());
		}
	}

	@Override
	public void act(float delta) {
		super.act(delta);
		for (int i = 0; i < items.size; i++) {
			items.get(i).act(delta);
		}
	}

	public void draw (Batch batch, float parentAlpha) {
		validate();

		drawBackground(batch, parentAlpha);

		Drawable selectedDrawable = style.selection;
		Color fontColorSelected = style.fontColorSelected;
		Color fontColorUnselected = style.fontColorUnselected;

		Color color = getColor();
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

		float x = getX(), y = getY(), width = getWidth();
		float itemY = getHeight();

		Drawable background = style.background;
		if (background != null) {
			float leftWidth = background.getLeftWidth();
			x += leftWidth;
			itemY -= background.getTopHeight();
			width -= leftWidth + background.getRightWidth();
		}

		float textOffsetX = selectedDrawable.getLeftWidth();
		float textOffsetY = selectedDrawable.getTopHeight();

		for (int i = 0; i < items.size; i++) {
			T item = items.get(i);
			if (cullingArea == null || (itemY - items.get(i).getPrefHeight() <= cullingArea.y + cullingArea.height && itemY >= cullingArea.y)) {
				item.setColor(fontColorUnselected.r, fontColorUnselected.g, fontColorUnselected.b, fontColorUnselected.a * parentAlpha);
				boolean selected = selection.contains(item);
				Drawable drawable = null;
				if (pressedIndex == i && style.down != null)
					drawable = style.down;
				else if (selected) {
					drawable = selectedDrawable;
					item.setColor(fontColorSelected.r, fontColorSelected.g, fontColorSelected.b, fontColorSelected.a * parentAlpha);
				} else if (overIndex == i && style.over != null) //
					drawable = style.over;
				drawSelection(batch, drawable, x, y + itemY - item.getPrefHeight(), width, items.get(i).getPrefHeight());
				item.setPosition(x + textOffsetX, y + itemY - textOffsetY - item.getPrefHeight() * 0.5f);
				item.draw(batch, 1f);
				if (selected) {
					item.setColor(fontColorUnselected.r, fontColorUnselected.g, fontColorUnselected.b,
							fontColorUnselected.a * parentAlpha);
				}
			} else if (itemY < cullingArea.y) {
				break;
			}
			itemY -= item.getPrefHeight();
		}
	}

	protected void drawSelection (Batch batch, @Null Drawable drawable, float x, float y, float width, float height) {
		if (drawable != null) drawable.draw(batch, x, y, width, height);
	}

	/** Called to draw the background. Default implementation draws the style background drawable. */
	protected void drawBackground (Batch batch, float parentAlpha) {
		if (style.background != null) {
			Color color = getColor();
			batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
			style.background.draw(batch, getX(), getY(), getWidth(), getHeight());
		}
	}

	public ArraySelection<T> getSelection () {
		return selection;
	}

	public void setSelection (ArraySelection<T> selection) {
		this.selection = selection;
	}

	/** Returns the first selected item, or null. */
	public @Null T getSelected () {
		return selection.first();
	}

	/** Sets the selection to only the passed item, if it is a possible choice.
	 * @param item May be null. */
	public void setSelected (@Null T item) {
		if (items.contains(item, false))
			selection.set(item);
		else if (selection.getRequired() && items.size > 0)
			selection.set(items.first());
		else
			selection.clear();
	}

	/** @return The index of the first selected item. The top item has an index of 0. Nothing selected has an index of -1. */
	public int getSelectedIndex () {
		ObjectSet<T> selected = selection.items();
		return selected.size == 0 ? -1 : items.indexOf(selected.first(), false);
	}

	/** Sets the selection to only the selected index.
	 * @param index -1 to clear the selection. */
	public void setSelectedIndex (int index) {
		if (index < -1 || index >= items.size)
			throw new IllegalArgumentException("index must be >= -1 and < " + items.size + ": " + index);
		if (index == -1) {
			selection.clear();
		} else {
			selection.set(items.get(index));
		}
	}

	/** @return May be null. */
	public T getOverItem () {
		return overIndex == -1 ? null : items.get(overIndex);
	}

	/** @return May be null. */
	public T getPressedItem () {
		return pressedIndex == -1 ? null : items.get(pressedIndex);
	}

	/** @return null if not over an item. */
	public @Null T getItemAt (float y) {
		int index = getItemIndexAt(y);
		if (index == -1) return null;
		return items.get(index);
	}

	/** @return -1 if not over an item. */
	public int getItemIndexAt (float y) {
		float height = getHeight();
		Drawable background = TextraListBox.this.style.background;
		if (background != null) {
			height -= background.getTopHeight() + background.getBottomHeight();
			y -= background.getBottomHeight();
		}
		float h = height - y;
		if(h < 0) return -1;
		for (int i = 0; i < items.size; i++) {
			T item = items.get(i);
			h -= item.getPrefHeight();
			if(h <= 0){
				return i;
			}
		}
		return -1;
	}

	@SafeVarargs
	public final void setItems(T... newItems) {
		if (newItems == null) throw new IllegalArgumentException("newItems cannot be null.");
		float oldPrefWidth = getPrefWidth(), oldPrefHeight = getPrefHeight();

		items.clear();
		items.addAll(newItems);
		overIndex = -1;
		pressedIndex = -1;
		selection.validate();

		invalidate();
		if (oldPrefWidth != getPrefWidth() || oldPrefHeight != getPrefHeight()) invalidateHierarchy();
	}

	/** Sets the items visible in the list, clearing the selection if it is no longer valid. If a selection is
	 * {@link ArraySelection#getRequired()}, the first item is selected. This can safely be called with a (modified) array returned
	 * from {@link #getItems()}. */
	public void setItems (Array<? extends T> newItems) {
		if (newItems == null) throw new IllegalArgumentException("newItems cannot be null.");
		float oldPrefWidth = getPrefWidth(), oldPrefHeight = getPrefHeight();

		if (newItems != items) {
			items.clear();
			items.addAll(newItems);
		}
		overIndex = -1;
		pressedIndex = -1;
		selection.validate();

		invalidate();
		if (oldPrefWidth != getPrefWidth() || oldPrefHeight != getPrefHeight()) invalidateHierarchy();
	}

	public void clearItems () {
		if (items.size == 0) return;
		items.clear();
		overIndex = -1;
		pressedIndex = -1;
		selection.clear();
		invalidateHierarchy();
	}

	/** Returns the internal items array. If modified, {@link #setItems(Array)} must be called to reflect the changes. */
	public Array<T> getItems () {
		return items;
	}

	public float getPrefWidth () {
		validate();
		return prefWidth;
	}

	public float getPrefHeight () {
		validate();
		return prefHeight;
	}

	public void setCullingArea (@Null Rectangle cullingArea) {
		this.cullingArea = cullingArea;
	}

	/** @return May be null.
	 * @see #setCullingArea(Rectangle) */
	public Rectangle getCullingArea () {
		return cullingArea;
	}

	/** Sets the horizontal alignment of the list items.
	 * @param alignment See {@link Align}. */
	public void setAlignment (int alignment) {
		this.alignment = alignment;
	}

	public int getAlignment () {
		return alignment;
	}

	public void setTypeToSelect (boolean typeToSelect) {
		this.typeToSelect = typeToSelect;
	}

	public InputListener getKeyListener () {
		return keyListener;
	}
}
