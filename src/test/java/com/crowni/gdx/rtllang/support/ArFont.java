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
    private final StringBuilder workingText = new StringBuilder(128), subtext = new StringBuilder(16);

    public String typing(char c) {
        if (c == '\b') // backspace
            popChar();
        else
            addChar(new ArGlyph(c, ArUtils.isArabicNonNumeric(c)));
        workingText.setLength(0);
        return reorder(workingText).toString();
    }

    public String getText(String given) {
        String[] split = given.split("\n");
        workingText.setLength(0);
        for(int ln = 0; ln < split.length; ln++) {
            String line = split[ln];
            for (int i = 0, n = line.length(); i < n; i++) {
                char c = line.charAt(i);
                addChar(new ArGlyph(c, ArUtils.isArabicNonNumeric(c)));
            }
            reorder(workingText);
            if(ln + 1 < split.length) workingText.append('\n');
            this.glyphs.clear();
        }
        return workingText.toString();
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
                glyphs.add(((ArGlyphComplex) aChar).getAfterGlyph());
            }
            filterLastChars(1);
        }
    }

    private static void appendReversed(StringBuilder mainText, CharSequence reversing) {
        for (int n = reversing.length(), i = n - 1; i >= 0; i--) {
            mainText.append(reversing.charAt(i));
        }
    }

    private StringBuilder reorder(StringBuilder text) {
        boolean inserting = true;
        subtext.setLength(0);
        for (int i = glyphs.size - 1; i >= 0; i--) {
            if (glyphs.get(i).isRTL()) {
                if (!inserting) {
                    inserting = true;
                    appendReversed(text, subtext);
                    subtext.setLength(0);
                }

                text.append(glyphs.get(i).getChar());
            } else {
                inserting = false;
                subtext.append(glyphs.get(i).getOriginalChar());
            }
        }

        appendReversed(text, subtext);

        return text;
    }

    private void filterLastChars(int i) {
        if (glyphs.size - i >= 0)
            filter(glyphs.size - i);
    }


    /**
     * Attempts to modify the glyph stored at {@code index} to match the correct presentation form for its neighboring
     * Arabic glyphs. This does nothing if the char at {@code index} is not a right-to-left char. This is permitted to
     * replace two ArGlyph items with one ArGlyphComplex item if this is at the end of the text, a LAM char precedes
     * the given index, and the glyph at index is an ALF char.
     * @param index index of the glyph that will be mutated in-place
     */
    private void filter(int index) {
        ArGlyph glyph = glyphs.get(index);
        if (!glyph.isRTL()) {
            return;
        }

        ArGlyph before = getPositionGlyph(index - 1);
        ArGlyph after = getPositionGlyph(index + 1);


        /* CASE 1 */
        if (before == null && after == null)
            glyph.setChar(glyph.getOriginalChar());


        /* CASE 2 */
        if (before == null && after != null)
            glyph.setChar(ArUtils.getStartChar(glyph.getOriginalChar()));


        /* CASE 3 */
        if (before != null && after == null)
            if (ArUtils.isALFChar(glyph.getOriginalChar()) && ArUtils.isLAMChar(before.getOriginalChar())) {
                addComplexChars(index, glyph);
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


    private void addComplexChars(int index, ArGlyph arGlyph) {
        ArGlyphComplex glyph = new ArGlyphComplex(ArUtils.getLAM_ALF(arGlyph.getOriginalChar()));
        glyph.setSimpleGlyphs(arGlyph, getPositionGlyph(index - 1));
        glyphs.pop();
        glyphs.pop();
        addChar(glyph);
    }

    /**
     * @param index index of current glyph
     * @return correct position of glyph
     */
    private ArGlyph getPositionGlyph(int index) {
        ArGlyph glyph = (index >= 0 && index < glyphs.size) ? glyphs.get(index) : null;
        return (glyph != null) ? (ArUtils.isInvalidChar(glyph.getOriginalChar()) ? null : glyph) : null;
    }

}

