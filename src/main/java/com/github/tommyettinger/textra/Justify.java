package com.github.tommyettinger.textra;

/**
 * Determines line justification behavior in a Layout.
 * Besides {@link #NONE}, which makes no changes, each constant applies justification to some glyphs on a typical
 * {@link Line}.
 * <br>
 * The simplest behavior is that of {@link #FULL_ON_ALL_LINES}; it adds extra space to every glyph so the text fills
 * all the way to the right edge. {@link #FULL_ON_PARAGRAPH} acts the same except on the last Line (or only Line)
 * of a paragraph; it will not justify the last line at all.
 * <br>
 * The other constants cause Lines to justify only spaces. {@link #SPACES_ON_ALL_LINES} and {@link #SPACES_ON_PARAGRAPH}
 * only increase the width of the ASCII space character {@code ' '}, or decimal 32, hex 0020, and so on. "ALL_LINES"
 * affects all Lines, while "PARAGRAPH" doesn't affect the last (or only) Line.
 */
public enum Justify {
    /**
     * No justification will be applied; the x-advances of glyphs will not be changed.
     */
    NONE(false, false, false),
    /**
     * Adds extra x-advance to every space (the char {@code ' '}) so the text fills all the way to the right edge.
     */
    SPACES_ON_ALL_LINES(false, true, false),
    /**
     * Adds extra x-advance to every glyph so the text fills all the way to the right edge.
     */
    FULL_ON_ALL_LINES(false, false, true),
    /**
     * Adds extra x-advance to every space (the char {@code ' '}) so the text fills all the way to the right edge,
     * except for the last Line (or only Line) of a paragraph.
     */
    SPACES_ON_PARAGRAPH(true, true, false),
    /**
     * Adds extra space to every glyph so the text fills all the way to the right edge, except for the last Line (or
     * only Line) of a paragraph.
     */
    FULL_ON_PARAGRAPH(true, false, true),
    ;
    public final boolean ignoreLastLine;
    public final boolean affectSpaces;
    public final boolean affectAllGlyphs;
    Justify(boolean last, boolean spaces, boolean all){
        ignoreLastLine = last;
        affectSpaces = spaces;
        affectAllGlyphs = all;
    }
}
