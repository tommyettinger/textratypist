package com.github.tommyettinger.textra.json;

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
