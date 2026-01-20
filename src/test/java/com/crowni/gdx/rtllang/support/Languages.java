package com.crowni.gdx.rtllang.support;

/**
 * Created by Crowni on 10/11/2017.
 **/

public enum Languages {

    ENGLISH(32, 160),
    ARABIC_NUMERIC(1632, 1641),
    EXTENDED_NUMERIC(1776, 1785);

    private final int from;
    private final int to;

    Languages(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public static boolean inRange(Languages languages, char c) {
        return languages.from <= c && c <= languages.to;
    }
}
