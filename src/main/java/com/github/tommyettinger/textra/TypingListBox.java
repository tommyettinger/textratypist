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

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Cullable;
import com.github.tommyettinger.textra.Styles.ListStyle;

/**
 * A TypingListBox (based on {@link com.badlogic.gdx.scenes.scene2d.ui.List}) displays {@link TypingLabel}s and
 * highlights the currently selected item.
 * <br>
 * A {@link ChangeEvent} is fired when the list selection changes.
 * <br>
 * The preferred size of the list is determined by the text bounds of the items and the size of the {@link ListStyle#selection}.
 * <br>
 * The main reason to use this instead of a {@link TextraListBox} containing {@link TypingLabel}s is so that you can use
 * the extra APIs available for TypingLabel, instead of having to cast a TextraLabel to TypingLabel from TextraListBox.
 *
 * @author mzechner
 * @author Nathan Sweet */
public class TypingListBox<T extends TypingLabel> extends TextraListBox<T> implements Cullable {
	public TypingListBox(Skin skin) {
		this(skin.get(ListStyle.class));
	}

	public TypingListBox(Skin skin, String styleName) {
		this(skin.get(styleName, ListStyle.class));
	}

	public TypingListBox(ListStyle style) {
		super(style);
	}
}
