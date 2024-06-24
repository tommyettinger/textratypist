package com.github.tommyettinger.parser.impl;

import com.github.czyzby.lml.parser.impl.DefaultLmlSyntax;
import com.github.tommyettinger.parser.impl.tag.provider.TextraLabelTagProvider;

/** Copy-pasted nearly verbatim from <a href="https://github.com/crashinvaders/gdx-lml/blob/master/lml-vis/src/main/java/com/github/czyzby/lml/vis/parser/impl/VisLmlSyntax.java">here</a>.<br>
 * <br>
 * Replaces regular Scene2D actor tags with TextraTyping widgets. Supports the same core syntax - most tags from original LML
 * are either pointing to the same widgets or to VisUI equivalents, and all the attributes and macros you know from LML
 * are also supported. This syntax, however, adds extra tags and attributes of unique TextraTyping actors, that are simply
 * absent in regular Scene2D. See {@link #registerActorTags()} method source for all registered actor tags. Macro tags
 * are unchanged.
 *
 * @author MJ
 * @author Kotcrab
 * @author Blurple */
public class TextraLmlSyntax extends DefaultLmlSyntax {
    
    @Override
    protected void registerActorTags() {
        addTagProvider(new TextraLabelTagProvider(), "textra-label");
    }
    
    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        registerTextraAttributes();
    }
    
    /** Registers attributes of TextraTyping-specific actors. */
    protected void registerTextraAttributes() {
    }
}
