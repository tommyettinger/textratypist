package com.github.tommyettinger.textra.json;

import java.util.ArrayList;

/**
 * The outermost type stored in a Structured JSON font.
 * <br>
 * This was part of an experiment to see if loading .json.lzma fonts using alternative JSON libraries (not libGDX Json)
 * could speed up loading times. In short, both Fory-JSON and JsonBiter had enough startup overhead doing dynamic
 * class building to overshadow the time needed to load a relatively-small .json.lzma file.
 * The experiment was stopped there.
 */
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
