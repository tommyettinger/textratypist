package com.github.tommyettinger.textra.json;

/**
 * The type representing an individual kerning pair stored in a Structured JSON font.
 * <br>
 * This was part of an experiment to see if loading .json.lzma fonts using alternative JSON libraries (not libGDX Json)
 * could speed up loading times. In short, both Fory-JSON and JsonBiter had enough startup overhead doing dynamic
 * class building to overshadow the time needed to load a relatively-small .json.lzma file.
 * The experiment was stopped there.
 */

public class KerningData {
    public int unicode1 = 0;
    public int unicode2 = 1;
    public float advance = 0f;

    public KerningData() {
    }

    public KerningData(int unicode1, int unicode2, float advance) {
        this.unicode1 = unicode1;
        this.unicode2 = unicode2;
        this.advance = advance;
    }
}
