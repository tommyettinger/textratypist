package com.github.tommyettinger.textra.json;

/**
 * The type representing each glyph stored in a Structured JSON font.
 * <br>
 * This was part of an experiment to see if loading .json.lzma fonts using alternative JSON libraries (not libGDX Json)
 * could speed up loading times. In short, both Fory-JSON and JsonBiter had enough startup overhead doing dynamic
 * class building to overshadow the time needed to load a relatively-small .json.lzma file.
 * The experiment was stopped there.
 */
public class GlyphData {
    public int unicode = 0;
    public float advance = 0f;
    public BoundsData planeBounds = null;
    public BoundsData atlasBounds = null;

    public GlyphData() {
    }

    public GlyphData(int unicode, float advance, BoundsData planeBounds, BoundsData atlasBounds) {
        this.unicode = unicode;
        this.advance = advance;
        this.planeBounds = planeBounds;
        this.atlasBounds = atlasBounds;
    }
}
