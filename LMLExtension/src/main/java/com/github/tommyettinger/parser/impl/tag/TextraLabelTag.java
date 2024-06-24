package com.github.tommyettinger.parser.impl.tag;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.tag.actor.LabelLmlTag;
import com.github.czyzby.lml.parser.impl.tag.builder.TextLmlActorBuilder;
import com.github.czyzby.lml.parser.tag.LmlActorBuilder;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.tommyettinger.textra.TypingLabel;

public class TextraLabelTag extends LabelLmlTag {
    public TextraLabelTag(final LmlParser parser, final LmlTag parentTag, final StringBuilder rawTagData) {
        super(parser, parentTag, rawTagData);
    }
    
    @Override
    protected Actor getNewInstanceOfActor(final LmlActorBuilder builder) {
        final TextLmlActorBuilder textBuilder = (TextLmlActorBuilder) builder;
        return new TypingLabel(textBuilder.getText(), getSkin(builder), builder.getStyleName());
    }
}
