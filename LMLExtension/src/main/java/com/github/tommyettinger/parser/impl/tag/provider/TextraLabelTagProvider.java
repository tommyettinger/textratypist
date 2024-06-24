package com.github.tommyettinger.parser.impl.tag.provider;

import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.parser.tag.LmlTagProvider;
import com.github.tommyettinger.parser.impl.tag.TextraLabelTag;

public class TextraLabelTagProvider implements LmlTagProvider {
    @Override
    public LmlTag create(final LmlParser parser, final LmlTag parentTag, final StringBuilder rawTagData) {
        return new TextraLabelTag(parser, parentTag, rawTagData);
    }
}
