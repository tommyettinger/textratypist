package com.github.tommyettinger.textra;

/**
 * Determines line justification behavior in a Layout.
 * Besides {@link #NONE}, each constant applies justification to some glyphs on a typical {@link Line}.
 * <br>
 * The simplest behavior is that of {@link #COMPLETE_ALL_GLYPHS}; it adds extra space to every glyph so the text fills
 * all the way to the right edge. {@link #PARAGRAPH_ALL_GLYPHS} acts the same except on the last Line (or only Line)
 * of a paragraph; it will not justify the last line at all.
 * <br>
 * Other constants can cause Lines to justify only spaces, or spaces more than other glyphs while still adding to all.
 * {@link #COMPLETE_ONLY_SPACES} and {@link #PARAGRAPH_ONLY_SPACES} only increase the width of the ASCII space character
 * {@code ' '}, or decimal 32, hex 0020, and so on. "COMPLETE" affects all Lines, while "PARAGRAPH" doesn't affect the
 * last (or only) Line. {@link #COMPLETE_BONUS_SPACES} and {@link #PARAGRAPH_BONUS_SPACES} affect all characters, but
 * treat spaces as "counting for double" and add more space to them.
 *
 */
public enum Justify {
    /**
     * No justification will be applied; the x-advances of glyphs will not be changed.
     */
    NONE(false, false, false),
    /**
     * Adds extra x-advance to every space (the char {@code ' '}) so the text fills all the way to the right edge.
     */
    COMPLETE_ONLY_SPACES(false, true, false),
    /**
     * Adds extra x-advance to every glyph so the text fills all the way to the right edge.
     */
    COMPLETE_ALL_GLYPHS(false, false, true),
    /**
     * Adds extra x-advance to every glyph, with more applied to every space (the char {@code ' '}), so the text fills
     * all the way to the right edge.
     */
    COMPLETE_BONUS_SPACES(false, true, true),
    /**
     * Adds extra x-advance to every space (the char {@code ' '}) so the text fills all the way to the right edge,
     * except for the last Line (or only Line) of a paragraph.
     */
    PARAGRAPH_ONLY_SPACES(true, true, false),
    /**
     * Adds extra space to every glyph so the text fills all the way to the right edge, except for the last Line (or
     * only Line) of a paragraph.
     */
    PARAGRAPH_ALL_GLYPHS(true, false, true),
    /**
     * Adds extra x-advance to every glyph, with more applied to every space (the char {@code ' '}), so the text fills
     * all the way to the right edge, except for the last Line (or only Line) of a paragraph.
     */
    PARAGRAPH_BONUS_SPACES(true, true, true),
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
