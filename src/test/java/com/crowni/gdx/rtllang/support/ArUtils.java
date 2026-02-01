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

import com.badlogic.gdx.utils.IntIntMap;
import com.crowni.gdx.rtllang.support.ArCharCode.*;

/**
 * Created by Crowni on 10/5/2017.
 **/

public class ArUtils {
    private static final int EMPTY = 0;
    private static final IndividualChar[] chars;

    static {
        chars = IndividualChar.values();
    }

    /**
     * A String containing all Arabic-script characters this handles, plus the ASCII space.
     * This can be useful to append to {@code FreeTypeFontGenerator.FreeTypeFontParameter.characters} to ensure every
     * char this may need is present in a generated BitmapFont.
     */
    public static final String ALL_CHARS = "\u0020\u06F0\u06F1\u06F2\u06F3\u06F4\u06F5\u06F6\u06F7\u06F8\u06F9\u0660\u0661\u0662\u0663\u0664\u0665\u0666\u0667\u0668\u0669\u060C\u0627\u0622\u0623\u0625\u0649\uFEFB\uFEF5\uFEF7\uFEF9\u062F\u0630\u0631\u0632\u0698\u0648\u0624\u0629\u0626\u0628\u067E\u062A\u062B\u062C\u0686\u062D\u062E\u0633\u0634\u0635\u0636\u0637\u0638\u0639\u063A\u0641\u0642\u0643\u06A9\u06AF\u0644\u0645\u0646\u0647\u064A\u06CC\uFE8B\uFE91\uFB58\uFE97\uFE9B\uFE9F\uFB7C\uFEA3\uFEA7\uFEB3\uFEB7\uFEBB\uFEBF\uFEC3\uFEC7\uFECB\uFECF\uFED3\uFED7\uFEDB\uFB90\uFB94\uFEDF\uFEE3\uFEE7\uFEEB\uFEF3\uFBFE\uFE8E\uFE84\uFE88\uFEAA\uFEAC\uFEAE\uFEB0\uFB8B\uFEEE\uFE86\uFE94\uFE8C\uFE92\uFB59\uFE98\uFE9C\uFEA0\uFB7D\uFEA4\uFEA8\uFEB4\uFEB8\uFEBC\uFEC0\uFEC4\uFEC8\uFECC\uFED0\uFED4\uFED8\uFEDC\uFB91\uFB95\uFEE0\uFEE4\uFEE8\uFEEC\uFEF4\uFBFF\uFE82\uFEF0\uFEFC\uFEF6\uFEF8\uFEFA\uFE8A\uFE90\uFB57\uFE96\uFE9A\uFE9E\uFB7B\uFEA2\uFEA6\uFEB2\uFEB6\uFEBA\uFEBE\uFEC2\uFEC6\uFECA\uFECE\uFED2\uFED6\uFEDA\uFB8F\uFB93\uFEDE\uFEE2\uFEE6\uFEEA\uFEF2\uFBFD";

    private static final IntIntMap types = new IntIntMap(chars.length);
    private static final IntIntMap startChars = new IntIntMap(chars.length);
    private static final IntIntMap centerChars = new IntIntMap(chars.length);
    private static final IntIntMap endChars = new IntIntMap(chars.length);

    static {
        for (int i = 0, n = chars.length; i < n; i++) {
            int id = chars[i].id;
            types.put(id, chars[i].type);
            startChars.put(id, StartChar.getChar(i));
            centerChars.put(id, CenterChar.getChar(i));
            endChars.put(id, EndChar.getChar(i));
        }
    }

    public static boolean isInvalidChar(char c) {
        return getCharType(c) == EMPTY;
    }

    /**
     * Checks if a char uses the Arabic script, and isn't a number. Numbers are written left-to-right in the Arabic
     * script, but everything else is written right-to-left.
     *
     * @param c the char to evaluate
     * @return if the given char is an Arabic-script glyph that isn't a number
     */
    public static boolean isArabicNonNumeric(char c) {
        return ArRanges.inRange(ArRanges.ARABIC_A, c)
                || ArRanges.inRange(ArRanges.ARABIC_B, c)
                || ArRanges.inRange(ArRanges.ARABIC_C, c)
                || ArRanges.inRange(ArRanges.ARABIC_SUPPLEMENT, c)
                || ArRanges.inRange(ArRanges.ARABIC_EXTENDED, c)
                || ArRanges.inRange(ArRanges.ARABIC_PRESENTATION_A, c)
                || ArRanges.inRange(ArRanges.ARABIC_PRESENTATION_B, c)
                ;
    }

    public static int getCharType(char c) {
        return types.get(c, 0);
    }

    public static char getIndividualChar(char c) {
        return c;
    }

    public static char getStartChar(char c) {
        return (char)startChars.get(c, c);
    }

    public static char getCenterChar(char c) {
        return (char)centerChars.get(c, c);
    }

    public static char getEndChar(char c) {
        return (char)endChars.get(c, c);
    }

    /*************** SPECIAL CASE *****************/
    public static char getLAM_ALF(char alf) {
        switch (alf) {
            case 1575:
                return IndividualChar.LAM_ALF.getChar();
            case 1570:
                return IndividualChar.LAM_ALF_MAD.getChar();
            case 1571:
                return IndividualChar.LAM_ALF_HAMZA_UP.getChar();
            case 1573:
                return IndividualChar.LAM_ALF_HAMZA_DOWN.getChar();
        }
        return alf;
    }

    public static boolean isALFChar(char c) {
        return
                c == IndividualChar.ALF.getChar()
                        || c == IndividualChar.ALF_HAMZA_UP.getChar()
                        || c == IndividualChar.ALF_HAMZA_DOWN.getChar()
                        || c == IndividualChar.ALF_MAD.getChar()
                ;
    }

    public static boolean isLAMChar(char c) {
        return c == IndividualChar.LAM.getChar();
    }

    public static boolean isComplexChar(ArGlyph glyph) {
        return glyph instanceof ArGlyphComplex;
    }

    public static String getAllChars() {
        return ALL_CHARS;
    }
}
