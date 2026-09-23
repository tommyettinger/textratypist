package com.github.tommyettinger.textra.json;

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
