package com.github.tommyettinger.textra;

import com.badlogic.gdx.utils.LongArray;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

/**
 * One line of possibly-colorful, possibly-styled text, with a width and height set by
 * {@link Font#markup(String, Layout)} on Lines in a {@link Layout}. This stores each (colorful, styled) char as a
 * {@code long} in a libGDX {@link LongArray}. This is a Poolable class, and you can obtain a
 * Line with {@code Pools.obtain(Line.class)}, or just using a constructor.
 */
public class Line implements Pool.Poolable {

    private static final Pool<Line> pool = new Pool<Line>() {
        @Override
        protected Line newObject() {
            return new Line();
        }
    };
    static {
        Pools.set(Line.class, pool);
    }

    public final LongArray glyphs;
    public float width, height;

    public Line() {
        glyphs = new LongArray(16);
    }

    public Line(int capacity) {
        glyphs = new LongArray(capacity);
    }

    public Line size(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    /**
     * Resets the object for reuse. This clears {@link #glyphs}, rather than nulling it. The sizes are set to 0.
     */
    @Override
    public void reset() {
        glyphs.clear();
        width = 0;
        height = 0;
    }
}
