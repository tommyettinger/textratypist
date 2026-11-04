package com.github.tommyettinger.textra;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Container;

/**
 * Acts exactly like a {@link Container} around a scene2d Actor, but enables transform by default rather than disabling
 * it. This allows methods like {@link #setRotation(float)} to work by default. This is meant to be used with a "Cpu"
 * Batch for optimal performance; typically this is {@link com.badlogic.gdx.graphics.g2d.CpuSpriteBatch} from libGDX or
 * {@link TextureArrayCpuPolygonSpriteBatch} from this library. Note that there can still be a significant performance
 * drop from transforming even one Group, even with a TextureArrayCpuPolygonSpriteBatch or CpuSpriteBatch.
 * <br>
 * This only exists to streamline how rotation works for widgets here. It is equivalent to a normal Container that
 * automatically calls {@link #setTransform(boolean)} with true as its parameter. You can use a Container and enable
 * transform yourself, if you want. While a class like {@link com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup} also
 * contains one or more Actors and has transform enabled by default, it also doesn't participate in layout like a widget
 * normally does, so it looks and acts odd.
 * <br>
 * You can also rotate individual glyphs generally without any problems; this is used by various TypingLabel Effects.
 * Rotating whole TypingLabels or TextraLabels also works, and is much faster than using a TransformContainer (or a
 * normal Container with transform set to true), but can look incorrect where underline or strikethrough are used. Box
 * drawing characters also tend to slide when rotating without a Container of some sort.
 *
 * @param <T> an Actor type that you want to be transform-able
 */
public class TransformContainer<T extends Actor> extends Container<T> {
    public TransformContainer() {
        super();
        setTransform(true);
    }

    public TransformContainer(T actor) {
        super(actor);
        setTransform(true);
    }
}
