package com.github.tommyettinger.textra;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Null;

public class TextraArea2 extends TextraField {

    public TextraArea2(@Null String text, Skin skin) {
        this(text, skin.get(Styles.TextFieldStyle.class));
    }

    public TextraArea2(@Null String text, Skin skin, Font replacementFont) {
        this(text, skin.get(Styles.TextFieldStyle.class), replacementFont);
    }

    public TextraArea2(@Null String text, Skin skin, String styleName) {
        this(text, skin.get(styleName, Styles.TextFieldStyle.class));
    }

    public TextraArea2(String text, Styles.TextFieldStyle style) {
        super(text, style);
        label.setWrap(true);
        label.workingLayout.targetWidth = 50;
        label.setMaxLines(5);
        label.setSize(getPrefWidth(), getPrefHeight());
    }

    public TextraArea2(String text, Styles.TextFieldStyle style, Font replacementFont) {
        super(text, style, replacementFont);
        label.setWrap(true);
        label.workingLayout.targetWidth = 50;
        label.setMaxLines(5);
        label.setSize(getPrefWidth(), getPrefHeight());
    }

}
