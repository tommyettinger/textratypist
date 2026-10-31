package com.github.tommyettinger.textra.json;

/**
 * Data about the atlas stored once in a Structured JSON font.
 * <br>
 * This was part of an experiment to see if loading .json.lzma fonts using alternative JSON libraries (not libGDX Json)
 * could speed up loading times. In short, both Fory-JSON and JsonBiter had enough startup overhead doing dynamic
 * class building to overshadow the time needed to load a relatively-small .json.lzma file.
 * The experiment was stopped there.
 */
public class AtlasData {
    public String type = "softmask";
    public float distanceRange = 0f;
    public float distanceRangeMiddle = 0f;
    public float size = 0f;
    public float width = 0f;
    public float height = 0f;
    public String yOrigin = "bottom";

    public AtlasData() {
    }

    public AtlasData(String type, float distanceRange, float distanceRangeMiddle, float size, float width, float height, String yOrigin) {
        this.type = type;
        this.distanceRange = distanceRange;
        this.distanceRangeMiddle = distanceRangeMiddle;
        this.size = size;
        this.width = width;
        this.height = height;
        this.yOrigin = yOrigin;
    }

    public AtlasData(float size, float width, float height) {
        this.size = size;
        this.width = width;
        this.height = height;
    }
}
