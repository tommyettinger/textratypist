package com.github.tommyettinger.textra.json;

/**
 * The type storing metrics data for the whole font, stored once in a Structured JSON font.
 * <br>
 * This was part of an experiment to see if loading .json.lzma fonts using alternative JSON libraries (not libGDX Json)
 * could speed up loading times. In short, both Fory-JSON and JsonBiter had enough startup overhead doing dynamic
 * class building to overshadow the time needed to load a relatively-small .json.lzma file.
 * The experiment was stopped there.
 */
public class MetricsData {
    public float emSize = 1f;
    public float lineHeight = 1f;
    public float ascender = 0f;
    public float descender = 0f;
    public float underlineY = 0f;
    public float underlineThickness = 0f;

    public MetricsData() {
    }

    public MetricsData(float emSize, float lineHeight, float ascender, float descender, float underlineY, float underlineThickness) {
        this.emSize = emSize;
        this.lineHeight = lineHeight;
        this.ascender = ascender;
        this.descender = descender;
        this.underlineY = underlineY;
        this.underlineThickness = underlineThickness;
    }
}
