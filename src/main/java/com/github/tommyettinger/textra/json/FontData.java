package com.github.tommyettinger.textra.json;

import java.util.ArrayList;

public class FontData {
    public AtlasData atlas = null;
    public MetricsData metrics = null;
    public ArrayList<GlyphData> glyphs = null;
    public ArrayList<KerningData> kerning = null;

    public FontData() {
    }

    public FontData(AtlasData atlas, MetricsData metrics, ArrayList<GlyphData> glyphs, ArrayList<KerningData> kerning) {
        this.atlas = atlas;
        this.metrics = metrics;
        this.glyphs = glyphs;
        this.kerning = kerning;
    }
}
