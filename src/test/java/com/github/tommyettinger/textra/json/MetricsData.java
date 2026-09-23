package com.github.tommyettinger.textra.json;

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
