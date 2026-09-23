package com.github.tommyettinger.textra.json;

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
