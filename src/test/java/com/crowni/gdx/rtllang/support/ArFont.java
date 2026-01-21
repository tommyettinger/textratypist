/*
 * **************************************************************************
 * Copyright 2017 See AUTHORS file.
 *
 * Licensed under the Apache License,Version2.0(the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,software
 * distributed under the License is distributed on an"AS IS"BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *************************************************************************
 *
 */

package com.crowni.gdx.rtllang.support;

import com.badlogic.gdx.utils.Array;

/**
 * Created by Crowni on 10/5/2017.
 **/
public class ArFont {
    private final Array<ArGlyph> glyphs = new Array<>();

    public String typing(char c) {
        if (c == '\b') // backspace
            popChar();
        else
            addChar(new ArGlyph(c, ArUtils.isArabicNonNumeric(c)));
        return reorder(new StringBuilder()).toString();
    }

    public String getText(String given) {
        String[] split = given.split("\n");
        StringBuilder text = new StringBuilder(given.length());
        for(int ln = 0; ln < split.length; ln++) {
            String line = split[ln];
            for (int i = 0, n = line.length(); i < n; i++) {
                char c = line.charAt(i);
                addChar(new ArGlyph(c, ArUtils.isArabicNonNumeric(c)));
            }
            reorder(text);
            if(ln + 1 < split.length) text.append('\n');
            this.glyphs.clear();
        }
        return text.toString();
    }

    private void addChar(ArGlyph glyph) {
        glyphs.add(glyph);
        filterLastChars(1);
        filterLastChars(2);
    }

    private void popChar() {
        if (glyphs.size > 0) {
            ArGlyph aChar = glyphs.pop();
            if (aChar instanceof ArGlyphComplex) {
                Array<ArGlyph> glyphComplex = ((ArGlyphComplex) aChar).getSimpleChars();
                for (int i = glyphComplex.size - 1; i >= 0; i--)
                    glyphs.add(glyphComplex.get(i));
                glyphs.pop();
            }
            filterLastChars(1);
        }
    }

    private StringBuilder reorder(StringBuilder text) {
        boolean inserting = true;
        StringBuilder subtext = new StringBuilder();
        for (int i = glyphs.size - 1; i >= 0; i--) {
            if (glyphs.get(i).isRTL()) {
                if (!inserting) {
                    inserting = true;
                    text.append(subtext);
                    subtext.setLength(0);
                }

                text.append(glyphs.get(i).getChar());
            } else {
                inserting = false;
                subtext.insert(0, glyphs.get(i).getOriginalChar());
            }
        }

        text.append(subtext);

        return text;
    }

    /**
     * @param glyph will be mutated in-place
     */
    private void filter(ArGlyph glyph) {
        if (!glyph.isRTL()) {
            return;
        }

        ArGlyph before = getPositionGlyph(glyph, -1);
        ArGlyph after = getPositionGlyph(glyph, +1);


        /* CASE 1 */
        if (before == null && after == null)
            glyph.setChar(glyph.getOriginalChar());


        /* CASE 2 */
        if (before == null && after != null)
            glyph.setChar(ArUtils.getStartChar(glyph.getOriginalChar()));


        /* CASE 3 */
        if (before != null && after == null)
            if (ArUtils.isALFChar(glyph.getOriginalChar()) && ArUtils.isLAMChar(before.getOriginalChar())) {
                addComplexChars(glyph);
            } else {
                if (before.getType() == ArCharCode.X2)
                    glyph.setChar(ArUtils.getIndividualChar(glyph.getOriginalChar()));
                else
                    glyph.setChar(ArUtils.getEndChar(glyph.getOriginalChar()));
            }


        /* CASE 4 */
        if (before != null && after != null)
            if (glyph.getType() == ArCharCode.X4) {
                if (before.getType() == ArCharCode.X2)
                    glyph.setChar(ArUtils.getStartChar(glyph.getOriginalChar()));
                else
                    glyph.setChar(ArUtils.getCenterChar(glyph.getOriginalChar()));
            } else {
                if (before.getType() == ArCharCode.X2)
                    glyph.setChar(ArUtils.getIndividualChar(glyph.getOriginalChar()));
                else
                    glyph.setChar(ArUtils.getEndChar(glyph.getOriginalChar()));
            }

    }

    /**
     * @param arGlyph current glyph
     * @param pos     value is always either -1 or 1: -1 is before arGlyph or +1 is after arGlyph
     * @return correct position of glyph
     */
    private ArGlyph getPositionGlyph(ArGlyph arGlyph, int pos) {
        int i = glyphs.lastIndexOf(arGlyph, true) + pos;
        ArGlyph glyph = (pos > 0 ? i < glyphs.size : i > -1) ? glyphs.get(i) : null;
        return glyph != null ? ArUtils.isInvalidChar(glyph.getOriginalChar()) ? null : glyph : null;
    }

    private void addComplexChars(ArGlyph arGlyph) {
        ArGlyphComplex glyph = new ArGlyphComplex(ArUtils.getLAM_ALF(arGlyph.getOriginalChar()));
        glyph.setSimpleGlyphs(arGlyph, getPositionGlyph(arGlyph, -1));
        for (int i = 0; i < glyph.getSimpleChars().size; i++) glyphs.pop();
        addChar(glyph);
    }

    private void filterLastChars(int i) {
        if (glyphs.size - i >= 0)
            filter(glyphs.get(glyphs.size - i));
    }
}

