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

public class ArGlyphComplex extends ArGlyph {

    private Array<ArGlyph> simpleGlyphs;

    // originalChar is a complex
    public ArGlyphComplex(char complexChar) {
        super(complexChar, true);
        simpleGlyphs = new Array<ArGlyph>();
    }

    public Array<ArGlyph> getSimpleChars() {
        return simpleGlyphs;
    }

    public ArGlyph getElementChar(int i) {
        return simpleGlyphs.get(i);
    }

    public char getComplexChar() {
        return modifiedChar;
    }

    public void setComplexChar(char c) {
        this.modifiedChar = c;
    }

    public int getType() {
        return type;
    }

    public void setSimpleGlyphs(ArGlyph... glyphs) {
        if (simpleGlyphs.size == 0)
            simpleGlyphs.addAll(glyphs);
    }

    public void setSimpleGlyphs(Array<ArGlyph> glyphs) {
        this.simpleGlyphs = glyphs;
    }
}
