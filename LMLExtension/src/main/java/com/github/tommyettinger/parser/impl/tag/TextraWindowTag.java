package com.github.tommyettinger.parser.impl.tag;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.tag.actor.WindowLmlTag;
import com.github.czyzby.lml.parser.impl.tag.builder.TextLmlActorBuilder;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.tommyettinger.parser.impl.tag.builder.TextraWindowLmlActorBuilder;
import com.github.tommyettinger.textra.TextraWindow;

public class TextraWindowTag extends WindowLmlTag {
    public TextraWindowTag(final LmlParser parser, final LmlTag parentTag, final StringBuilder rawTagData) {
        super(parser, parentTag, rawTagData);
    }
    
    @Override
    protected TextLmlActorBuilder getNewInstanceOfBuilder() {
        return new TextraWindowLmlActorBuilder();
    }
    
    @Override
    protected final Window getNewInstanceOfWindow(final TextLmlActorBuilder builder) {
        return getNewInstanceOfTextraWindow((TextraWindowLmlActorBuilder) builder);
    }
    
    /** @param builder contains data necessary to build {@link TextraWindow}.
     * @return a new instance of {@link TextraWindow}. */
    protected TextraWindow getNewInstanceOfTextraWindow(final TextraWindowLmlActorBuilder builder) {
        final TextraWindow window = new TextraWindow(builder.getText(), getSkin(builder), builder.getStyleName());
        window.setSkin(getSkin(builder));
        return window;
    }
}