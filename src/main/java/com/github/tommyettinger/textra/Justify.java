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

    /**
     * Attempts to convert between a libGDX Justify enum and a TextraTypist Justify enum. Currently, TextraTypist
     * doesn't distinguish wrapped lines from unwrapped ones, though it could in the future. The libGDX constants
     * {@link com.badlogic.gdx.utils.Justify#WrappedLinesBySpace} is matched to {@link #SPACES_ON_PARAGRAPH} here, and
     * {@link com.badlogic.gdx.utils.Justify#WrappedLinesByGlyph} is matched to {@link #FULL_ON_PARAGRAPH}.
     *
     * @param gdx a Justify enum from recent libGDX versions
     * @return the closest TextraTypist Justify enum there is to the given libGDX Justify enum
     */
    public static Justify fromGdx(com.badlogic.gdx.utils.Justify gdx){
        switch (gdx){
            case AllLinesBySpace: return SPACES_ON_ALL_LINES;
            case AllLinesByGlyph: return FULL_ON_ALL_LINES;
            case WrappedLinesBySpace:
            case ParagraphBySpace: return SPACES_ON_PARAGRAPH;
            case WrappedLinesByGlyph:
            case ParagraphByGlyph: return FULL_ON_PARAGRAPH;
            default: return NONE;
        }
    }

    /**
     * Converts between a TextraTypist Justify enum and a libGDX Justify enum. This matches the five possible enum
     * constants here to direct analogs in libGDX, and cannot return
     * {@link com.badlogic.gdx.utils.Justify#WrappedLinesByGlyph} or
     * {@link com.badlogic.gdx.utils.Justify#WrappedLinesBySpace}.
     * 
     * @param textra a Justify enum from TextraTypist
     * @return the closest Justify enum from libGDX to the given TextraTypist Justify enum
     */
    public static com.badlogic.gdx.utils.Justify toGdx(Justify textra){
        switch (textra){
            case SPACES_ON_ALL_LINES: return com.badlogic.gdx.utils.Justify.AllLinesBySpace;
            case FULL_ON_ALL_LINES: return com.badlogic.gdx.utils.Justify.AllLinesByGlyph;
            case SPACES_ON_PARAGRAPH: return com.badlogic.gdx.utils.Justify.ParagraphBySpace;
            case FULL_ON_PARAGRAPH: return com.badlogic.gdx.utils.Justify.ParagraphByGlyph;
            default: return com.badlogic.gdx.utils.Justify.None;
        }
    }
}
