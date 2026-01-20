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

import com.badlogic.gdx.utils.CharArray;
import com.crowni.gdx.rtllang.support.ArCharCode.*;

/**
 * Created by Crowni on 10/5/2017.
 **/

public class ArUtils {
    private static final char EMPTY = 0;
    private static final IndividualChar[] chars;

    static {
        chars = IndividualChar.values();
    }

    public static boolean isInvalidChar(char c) {
        return getCharType(c) == EMPTY;
    }

    public static boolean isLTR(char c) {
        return Languages.inRange(Languages.ENGLISH, c) || Languages.inRange(Languages.ARABIC_NUMERIC, c) || Languages.inRange(Languages.EXTENDED_NUMERIC, c);
    }

    public static int getCharType(char c) {
        for (IndividualChar individualChar : chars)
            if (individualChar.getChar() == c)
                return individualChar.getType();
        return 0;
    }

    public static char getIndividualChar(char c) {
        return c;
    }

    public static char getStartChar(char c) {
        for (int i = 0; i < chars.length; i++)
            if (chars[i].getChar() == c) return StartChar.getChar(i);
        return c;
    }

    public static char getCenterChar(char c) {
        for (int i = 0; i < chars.length; i++)
            if (chars[i].getChar() == c) return CenterChar.getChar(i);
        return c;
    }

    public static char getEndChar(char c) {
        for (int i = 0; i < chars.length; i++)
            if (chars[i].getChar() == c) return EndChar.getChar(i);
        return c;
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

    public static CharArray getAllChars() {
        CharArray array = new CharArray();
        for (int i = 0; i < chars.length; i++)
            if (!array.contains(IndividualChar.getChar(i)))
                array.add(IndividualChar.getChar(i));

        for (int i = 0; i < chars.length; i++)
            if (!array.contains(StartChar.getChar(i)))
                array.add(StartChar.getChar(i));

        for (int i = 0; i < chars.length; i++)
            if (!array.contains(CenterChar.getChar(i)))
                array.add(CenterChar.getChar(i));

        for (int i = 0; i < chars.length; i++)
            if (!array.contains(EndChar.getChar(i)))
                array.add(EndChar.getChar(i));

        return array;
    }
}
