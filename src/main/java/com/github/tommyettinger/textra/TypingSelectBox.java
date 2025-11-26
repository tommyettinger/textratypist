package com.github.tommyettinger.textra;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

/** A select box (aka a drop-down list) allows a user to choose one of a number of values from a list. When inactive, the selected
 * value is displayed. When activated, it shows the list of values that may be selected.
 * <p>
 * {@link ChangeListener.ChangeEvent} is fired when the select box selection changes.
 * <p>
 * The preferred size of the select box is determined by the maximum text bounds of the items and the size of the
 * {@link Styles.SelectBoxStyle#background}.
 * @author mzechner
 * @author Nathan Sweet */
public class TypingSelectBox extends TextraSelectBox {
    public TypingSelectBox(Skin skin) {
        super(skin);
    }

    public TypingSelectBox(Skin skin, String styleName) {
        super(skin, styleName);
    }

    public TypingSelectBox(Styles.SelectBoxStyle style) {
        super(style);
    }

    @Override
    protected TextraLabel newLabel(String markupText, Font font, Color color) {
        TypingLabel label = new TypingLabel(markupText, font, color);
        // Enforces bottom alignment, and also disables top alignment to prevent top or center from being used.
        label.align = (label.align | Align.bottom) & ~Align.top;
        return label;
    }
}
