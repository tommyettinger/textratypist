package com.crowni.gdx.rtllang.support;

/**
 * Created by Crowni on 10/11/2017.
 **/

public enum ArRanges {
/*
Arabic (0600–06FF, 256 characters)
Arabic Supplement (0750–077F, 48 characters)
Arabic Extended-B (0870–089F, 43 characters)
Arabic Extended-A (08A0–08FF, 96 characters)
Arabic Presentation Forms-A (FB50–FDFF, 656 characters)
Arabic Presentation Forms-B (FE70–FEFF, 141 characters)
*/
    ARABIC_A(0x0600, 0x065F),
//	ARABIC_NUMERIC(0x0660, 0x0669), // LTR
    ARABIC_B(0x066A, 0x06EF),
//    EXTENDED_NUMERIC(0x06F0, 0x06F9), // LTR
    ARABIC_C(0x06FA, 0x06FF),
    ARABIC_SUPPLEMENT(0x0750, 0x077F),
    ARABIC_EXTENDED(0x0870, 0x08FF),
    ARABIC_PRESENTATION_A(0xFB50, 0xFDFF),
    ARABIC_PRESENTATION_B(0xFE70, 0xFEFF),;

    private final int from;
    private final int to;

    ArRanges(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public static boolean inRange(ArRanges languages, char c) {
        return languages.from <= c && c <= languages.to;
    }
}
