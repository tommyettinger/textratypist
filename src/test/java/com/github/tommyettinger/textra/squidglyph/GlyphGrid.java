/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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

package com.github.tommyettinger.textra.squidglyph;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.ObjectLongMap;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.textra.ColorLookup;
import com.github.tommyettinger.textra.Font;

/**
 * This is a modified copy of a class from SquidSquad's SquidGlyph module, to make testing (much) easier.
 * All SquidSquad-specific classes have been replaced, more or less, with libGDX ones. This probably isn't as
 * efficient because it might allocate a lot of GridPoint2 values.
 */
public class GlyphGrid extends Group {
    protected int gridWidth;
    protected int gridHeight;
    public ObjectLongMap<GridPoint2> map;
    public int[][] backgrounds = null;
    protected Font font;
    public Viewport viewport;
    public int startX, startY, endX, endY;
    private GridPoint2 query = new GridPoint2();

    /**
     * Constructs a bare-bones GlyphGrid with size 64x64. Does not set {@link #font}, you will have to set it later.
     */
    public GlyphGrid() {
        this(null, 64, 64, false);
    }

    /**
     * Constructs a 64x64 GlyphGrid with the specified Font. You probably want {@link #GlyphGrid(Font, int, int)} unless
     * your maps are always 64x64. This calls {@link #GlyphGrid(Font, int, int, boolean)} with
     * squareCenteredCells=false.
     *
     * @param font a Font that will be copied and used for the new GlyphGrid
     */
    public GlyphGrid(Font font) {
        this(font, 64, 64, false);
    }

    /**
     * Constructs a GlyphGrid with the specified size in cells wide and cells tall for its grid, using the specified
     * Font (which will be copied). This calls {@link #GlyphGrid(Font, int, int, boolean)} with
     * squareCenteredCells=false.
     *
     * @param font       a Font that will be copied and used for the new GlyphGrid
     * @param gridWidth  how many cells wide the grid should be
     * @param gridHeight how many cells tall the grid should be
     */
    public GlyphGrid(Font font, int gridWidth, int gridHeight) {
        this(font, gridWidth, gridHeight, false);
    }

    /**
     * Constructs a GlyphGrid with the specified size in cells wide and cells tall for its grid, using the specified
     * Font (which will be copied). If squareCenteredGlyphs is true, the Font copy this uses will be modified to have
     * extra space around glyphs so that they fit in square cells. For fonts that use gridGlyphs (the default behavior),
     * any box drawing characters will still take up the full cell, and will connect seamlessly.
     *
     * @param font                a Font that will be copied and used for the new GlyphGrid
     * @param gridWidth           how many cells wide the grid should be
     * @param gridHeight          how many cells tall the grid should be
     * @param squareCenteredCells if true, space will be added to make glyphs fit in square cells
     */
    public GlyphGrid(Font font, int gridWidth, int gridHeight, boolean squareCenteredCells) {
        super();
        setTransform(false);
        this.startX = 0;
        this.startY = 0;
        this.gridWidth = this.endX = gridWidth;
        this.gridHeight = this.endY = gridHeight;
        map = new ObjectLongMap<>(gridWidth * gridHeight);
        viewport = new StretchViewport(gridWidth, gridHeight);
        if (font != null) {
            setFont(new Font(font), squareCenteredCells);
        }
    }

    public Font getFont() {
        return font;
    }

    /**
     * Sets the Font this uses, but also configures the viewport to use the appropriate size cells, then scales the font
     * to size 1x1 (this makes some calculations much easier inside GlyphGrid). This is the same as calling
     * {@code setFont(font, true)}.
     *
     * @param font a Font that will be used directly (not copied) and used to calculate the viewport dimensions
     */
    public void setFont(Font font) {
        setFont(font, true);
    }

    /**
     * Sets the Font this uses, but also configures the viewport to use the appropriate size cells, then scales the font
     * to size 1x1 (this makes some calculations much easier inside GlyphGrid). This can add spacing to cells so that
     * they are always square, while keeping the aspect ratio of {@code font} as it was passed in. Use squareCenter=true
     * to enable this; note that it modifies the Font more deeply than normally.
     *
     * @param font         a Font that will be used directly (not copied) and used to calculate the viewport dimensions
     * @param squareCenter if true, spacing will be added to the sides of each glyph so that they fit in square cells
     */
    public void setFont(Font font, boolean squareCenter) {
        if (font == null) return;
        this.font = font;
        font.useIntegerPositions(false);
        if (squareCenter) {
            viewport.setScreenWidth((int) (gridWidth * font.cellWidth));
            viewport.setScreenHeight((int) (gridHeight * font.cellHeight));
            float larger = Math.max(font.cellWidth, font.cellHeight);
            font.scaleTo(font.cellWidth / larger, font.cellHeight / larger).fitCell(1f, 1f, true);
        } else {
            viewport.setScreenWidth((int) (gridWidth * font.cellWidth));
            viewport.setScreenHeight((int) (gridHeight * font.cellHeight));
            font.scaleTo(1f, 1f);
        }
    }


    /**
     * Gets how wide the grid is, measured in discrete cells.
     *
     * @return how many cells wide the grid is
     */
    public int getGridWidth() {
        return gridWidth;
    }

    /**
     * Gets how high the grid is, measured in discrete cells.
     *
     * @return how many cells high the grid is
     */
    public int getGridHeight() {
        return gridHeight;
    }

    /**
     * Places a character (optionally with style information) at the specified cell, using white foreground color.
     *
     * @param x         x position of the cell, measured in cells on the grid
     * @param y         y position of the cell, measured in cells on the grid
     * @param codepoint the character, with or without style information, to place
     */
    public void put(int x, int y, int codepoint) {
        map.put(new GridPoint2(x, y), (codepoint & 0xFFFFFFFFL) | 0xFFFFFFFE00000000L);
    }

    /**
     * Places a character (optionally with style information) at the specified cell, using the given foreground color.
     *
     * @param x         x position of the cell, measured in cells on the grid
     * @param y         y position of the cell, measured in cells on the grid
     * @param codepoint the character, with or without style information, to place
     * @param color     the RGBA8888 color to use for the character
     */
    public void put(int x, int y, int codepoint, int color) {
        map.put(new GridPoint2(x, y), (codepoint & 0xFFFFFFFFL) | (long) color << 32);
    }

    /**
     * Places a character (optionally with style information) at the specified cell, using the given foreground color.
     *
     * @param x          x position of the cell, measured in cells on the grid
     * @param y          y position of the cell, measured in cells on the grid
     * @param simpleChar the character, without style information, to place
     * @param color      the RGBA8888 color to use for the character
     */
    public void put(int x, int y, char simpleChar, int color) {
        map.put(new GridPoint2(x, y), (simpleChar) | (long) color << 32);
    }

    /**
     * Places a glyph (optionally with style information and/or color) at the specified cell.
     *
     * @param x     x position of the cell, measured in cells on the grid
     * @param y     y position of the cell, measured in cells on the grid
     * @param glyph the glyph to place, as produced by {@link Font#markupGlyph(char, String, ColorLookup)}
     */
    public void put(int x, int y, long glyph) {
        map.put(new GridPoint2(x, y), glyph);
    }

    /**
     * Places a glyph (optionally with style information and/or color) at the specified cell (given as a GridPoint2).
     * This put() method has the least overhead if you already have a GridPoint2 key and long glyph.
     *
     * @param fused a {@link GridPoint2} integer-based position
     * @param glyph the glyph to place, as produced by {@link Font#markupGlyph(char, String, ColorLookup)}
     */
    public void put(GridPoint2 fused, long glyph) {
        map.put(fused, glyph);
    }

    /**
     * Draws the entire GlyphGrid at its position in world units. Does no clipping.
     *
     * @param batch a SpriteBatch, usually; must at least be compatible with SpriteBatch's attributes
     */
    public void draw(Batch batch) {
        getStage().setViewport(viewport);
        font.enableShader(batch);
        batch.setPackedColor(Color.WHITE_FLOAT_BITS);
        float x = getX(), y = getY();
        if (backgrounds != null)
            font.drawBlocks(batch, backgrounds, x, y);
        y -= font.descent * font.scaleY * 2f;
        for(ObjectLongMap.Entry<GridPoint2> e : map.entries()) {
            font.drawGlyph(batch, e.value, x + e.key.x, y + e.key.y);
        }
        super.drawChildren(batch, 1f);
    }

    /**
     * Draws part of the GlyphGrid at its position in world units. Still iterates through all keys in order,
     * but only draws those that are visible in the given {@link Frustum} {@code limit}.
     *
     * @param batch a SpriteBatch, usually; must at least be compatible with SpriteBatch's attributes
     * @param limit a Frustum, usually obtained from {@link com.badlogic.gdx.graphics.Camera#frustum}, that delimits what will be rendered
     */
    public void draw(Batch batch, Frustum limit) {
        getStage().setViewport(viewport);
        font.enableShader(batch);
        batch.setPackedColor(Color.WHITE_FLOAT_BITS);
        float x = getX(), y = getY();
        if (backgrounds != null)
            font.drawBlocks(batch, backgrounds, x, y);
        float xPos, yPos, boundsWidth = 2f, boundsHeight = 2f;
        y -= font.descent * font.scaleY * 2f;
//        x += font.cellWidth * 0.5f;
        for(ObjectLongMap.Entry<GridPoint2> e : map.entries()) {
            xPos = x + e.key.x;
            yPos = y + e.key.y;
            if (limit.boundsInFrustum(xPos, yPos, 0, boundsWidth, boundsHeight, 1f))
                font.drawGlyph(batch, e.value, xPos, yPos);
        }
        super.drawChildren(batch, 1f);

    }

    /**
     * Draws part of the GlyphGrid at its position in world units. Only draws cells between startCellX
     * (inclusive) and endCellX (exclusive), and likewise for startCellY and endCellY, where those ints represent cell
     * positions and not screen or world positions. This only even considers keys that are in the given start-to-end
     * rectangle, and doesn't check keys or values outside it. This is probably the most efficient of the draw() methods
     * here, but requires you to know what the start and end bounds are. All of the start and end cell coordinates must
     * be non-negative.
     *
     * @param batch      a SpriteBatch, usually; must at least be compatible with SpriteBatch's attributes
     * @param startCellX the inclusive x of the lower-left corner, measured in cells, to start rendering at
     * @param startCellY the inclusive y of the lower-left corner, measured in cells, to start rendering at
     * @param endCellX   the exclusive x of the upper-right corner, measured in cells, to stop rendering at
     * @param endCellY   the exclusive y of the upper-right corner, measured in cells, to stop rendering at
     */
    public void draw(Batch batch, int startCellX, int startCellY, int endCellX, int endCellY) {
        getStage().setViewport(viewport);
        font.enableShader(batch);
        batch.setPackedColor(Color.WHITE_FLOAT_BITS);
        float x = getX(), y = getY();
        if (backgrounds != null)
            font.drawBlocks(batch, backgrounds, x, y);
        GridPoint2 pos = query;
        long glyph;
        y -= font.descent * font.scaleY * 2f;
//        x += font.cellWidth * 0.5f;
        for (int xx = startCellX; xx < endCellX; xx++) {
            for (int yy = startCellY; yy < endCellY; yy++) {
                pos.set(xx, yy);
                glyph = map.get(pos, 0L);
                if ((glyph & 0x000000FE00000000L) != 0L) // if pos was found and glyph is not transparent
                    font.drawGlyph(batch, glyph, x + xx * font.cellWidth, y + yy * font.cellHeight);
            }
        }
        super.drawChildren(batch, 1f);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.getColor().a *= parentAlpha;
        batch.setColor(batch.getColor());
        draw(batch, startX, startY, endX, endY);
    }

    /**
     * This should generally be called in the {@link com.badlogic.gdx.ApplicationListener#resize(int, int)} or
     * {@link com.badlogic.gdx.Screen#resize(int, int)} method when the screen size changes. This affects the viewport
     * only.
     *
     * @param screenWidth  the new screen width in pixels
     * @param screenHeight the new screen height in pixels
     */
    public void resize(int screenWidth, int screenHeight) {
        viewport.update(screenWidth, screenHeight, false);
        font.resizeDistanceField(screenWidth, screenHeight, viewport);
    }

    /**
     * Returns true if any children of this GlyphGrid currently have Actions, or false if none do.
     *
     * @return whether any children of this GlyphGrid currently have Actions
     */
    public boolean areChildrenActing() {
        SnapshotArray<Actor> children = getChildren();
        for (int i = 0, n = children.size; i < n; i++) {
            if (children.get(i).hasActions()) {
                return true;
            }
        }
        return false;
    }
}
