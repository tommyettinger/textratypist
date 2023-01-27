/*
 * Copyright (c) 2021-2023 See AUTHORS file.
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
 */

package com.github.tommyettinger.textra;

import com.badlogic.gdx.utils.LongArray;
import com.badlogic.gdx.utils.Pool;

/**
 * One line of possibly-colorful, possibly-styled text, with a width and height set by
 * {@link Font#markup(String, Layout)} on Lines in a {@link Layout}. This stores each (colorful, styled) char as a
 * {@code long} in a libGDX {@link LongArray}. This is a Poolable class, and you can obtain a
 * Line with {@code Line.POOL.obtain()}, or just using a constructor.
 */
public class Line implements Pool.Poolable {

    public static final Pool<Line> POOL = new Pool<Line>() {
        @Override
        protected Line newObject() {
            return new Line();
        }
    };

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

    public StringBuilder appendTo(StringBuilder sb) {
        sb.append("(\"");
        for (int i = 0, n = glyphs.size; i < n; i++) {
            sb.append((char) glyphs.get(i));
        }
        sb.append("\" w=").append(width).append(" h=").append(height).append(')');
        return sb;
    }

    public String toString() {
        return appendTo(new StringBuilder(glyphs.size + 20)).toString();
    }
}
