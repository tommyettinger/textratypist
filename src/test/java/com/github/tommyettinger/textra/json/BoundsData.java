package com.github.tommyettinger.textra.json;

/**
 * A type with four edge distances, stored many times in a Structured JSON font.
 * <br>
 * This was part of an experiment to see if loading .json.lzma fonts using alternative JSON libraries (not libGDX Json)
 * could speed up loading times. In short, both Fory-JSON and JsonBiter had enough startup overhead doing dynamic
 * class building to overshadow the time needed to load a relatively-small .json.lzma file.
 * The experiment was stopped there.
 */
public class BoundsData {
    public float left = 0f, bottom = 0f, right = 0f, top = 0f;
    public BoundsData() {

    }

    public BoundsData(float left, float bottom, float right, float top){
        this.left = left;
        this.bottom = bottom;
        this.right = right;
        this.top = top;
    }
}
