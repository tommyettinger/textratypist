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

/**
 * Created by Crowni on 10/5/2017.
 **/

public class ArGlyph {

    protected final char originalChar;
    protected final int type;
    protected char modifiedChar;
    private final boolean rtl;

    public ArGlyph(char c, boolean rtl) {
        this.originalChar = c;
        this.rtl = rtl;
        type = ArUtils.getCharType(c);
    }

    public char getOriginalChar() {
        return originalChar;
    }

    public char getChar() {
        return modifiedChar;
    }

    public boolean isRTL() {
        return rtl;
    }

    public void setChar(char c) {
        this.modifiedChar = c;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.valueOf(rtl && modifiedChar != 0 ? modifiedChar : originalChar);
    }
}
