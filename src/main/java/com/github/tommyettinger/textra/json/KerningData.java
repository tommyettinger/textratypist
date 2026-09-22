package com.github.tommyettinger.textra.json;

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
