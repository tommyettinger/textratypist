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
 * Created by Crowni on 10/2/2017.
 **/

public class ArCharCode {
    public static final int X0 = 0;
    public static final int X2 = 2;
    public static final int X4 = 4;

    public enum IndividualChar {
        SPACE(32, X0),
        EMPTY(0, X0),

        E0(1776, X0),
        E1(1777, X0),
        E2(1778, X0),
        E3(1779, X0),
        E4(1780, X0),
        E5(1781, X0),
        E6(1782, X0),
        E7(1783, X0),
        E8(1784, X0),
        E9(1785, X0),

        A0(1632, X0),
        A1(1633, X0),
        A2(1634, X0),
        A3(1635, X0),
        A4(1636, X0),
        A5(1637, X0),
        A6(1638, X0),
        A7(1639, X0),
        A8(1640, X0),
        A9(1641, X0),

        COMMA(1548, X0),

        /************** X2 *************/
        ALF(1575, X2),
        ALF_MAD(1570, X2),
        ALF_HAMZA_UP(1571, X2),
        ALF_HAMZA_DOWN(1573, X2),
        ALF_MAQSORA(1609, X2),

        LAM_ALF(65275, X2),
        LAM_ALF_MAD(65269, X2),
        LAM_ALF_HAMZA_UP(65271, X2),
        LAM_ALF_HAMZA_DOWN(65273, X2),

        DAL(1583, X2),
        ZAL(1584, X2),

        RAA(1585, X2),
        ZAY(1586, X2),
        JEH(1688, X2),

        WAW(1608, X2),
        WAW_HAMZA(1572, X2),

        TAA_MARBOTA(1577, X2),


        /************** X4 *************/
        ALF_MAQSORA_HAMZA(1574, X4),

        BAA(1576, X4),
        PEH(1662, X4),
        TAA(1578, X4),
        THA(1579, X4),
        GEM(1580, X4),
        TCHEH(1670, X4),
        HAA(1581, X4),
        KHA(1582, X4),

        SEN(1587, X4),
        SHN(1588, X4),
        SAD(1589, X4),
        DAD(1590, X4),
        TTA(1591, X4),
        ZAA(1592, X4),
        AIN(1593, X4),
        GHN(1594, X4),
        FAA(1601, X4),
        QAF(1602, X4),
        KAF(1603, X4),
        KEHEH(1705, X4),
        GAF(1711, X4),

        LAM(1604, X4),
        MEM(1605, X4),
        NON(1606, X4),
        HHA(1607, X4),

        YAA(1610, X4),
        YEH(1740, X4),;

        public final int id;
        public final int type;

        private static final IndividualChar[] values = values();

        IndividualChar(int id, int type) {
            this.id = id;
            this.type = type;
        }

        public static char getChar(int index) {
            return values[index].getChar();
        }

        public char getChar() {
            return (char) id;
        }

        public int getType() {
            return type;
        }
    }

    public enum EndChar {
        SPACE(32, X0),
        EMPTY(0, X0),

        E0(1776, X0),
        E1(1777, X0),
        E2(1778, X0),
        E3(1779, X0),
        E4(1780, X0),
        E5(1781, X0),
        E6(1782, X0),
        E7(1783, X0),
        E8(1784, X0),
        E9(1785, X0),

        A0(1632, X0),
        A1(1633, X0),
        A2(1634, X0),
        A3(1635, X0),
        A4(1636, X0),
        A5(1637, X0),
        A6(1638, X0),
        A7(1639, X0),
        A8(1640, X0),
        A9(1641, X0),

        COMMA(1548, X0),

        /************** X2 *************/
        ALF(65166, X2),
        ALF_MAD(65154, X2),
        ALF_HAMZA_UP(65156, X2),
        ALF_HAMZA_DOWN(65160, X2),
        ALF_MAQSORA(65264, X2),

        LAM_ALF(65276, X2),
        LAM_ALF_MAD(65270, X2),
        LAM_ALF_HAMZA_UP(65272, X2),
        LAM_ALF_HAMZA_DOWN(65274, X2),

        DAL(65194, X2),
        ZAL(65196, X2),

        RAA(65198, X2),
        ZAY(65200, X2),
        JEH(64395, X2),

        WAW(65262, X2),
        WAW_HAMZA(65158, X2),

        TAA_MARBOTA(65172, X2),


        /************ 4X ************/
        ALF_MAQSORA_HAMZA(65162, X4),

        BAA(65168, X4),
        PEH(64343, X4),
        TAA(65174, X4),
        THA(65178, X4),
        GEM(65182, X4),
        TCHEH(64379, X4),
        HAA(65186, X4),
        KHA(65190, X4),

        SEN(65202, X4),
        SHN(65206, X4),
        SAD(65210, X4),
        DAD(65214, X4),
        TTA(65218, X4),
        ZAA(65222, X4),
        AIN(65226, X4),
        GHN(65230, X4),
        FAA(65234, X4),
        QAF(65238, X4),
        KAF(65242, X4),
        KEHEH(64399, X4),
        GAF(64403, X4),

        LAM(65246, X4),
        MEM(65250, X4),
        NON(65254, X4),
        HHA(65258, X4),

        YAA(65266, X4),
        YEH(64509, X4),;

        public final int id;
        public final int type;

        private static final EndChar[] values = values();

        EndChar(int id, int type) {
            this.id = id;
            this.type = type;
        }

        public static char getChar(int index) {
            return values[index].getChar();
        }

        public char getChar() {
            return (char) id;
        }

        public int getType() {
            return type;
        }
    }

    public enum StartChar {
        SPACE(32, X0),
        EMPTY(0, X0),

        E0(1776, X0),
        E1(1777, X0),
        E2(1778, X0),
        E3(1779, X0),
        E4(1780, X0),
        E5(1781, X0),
        E6(1782, X0),
        E7(1783, X0),
        E8(1784, X0),
        E9(1785, X0),

        A0(1632, X0),
        A1(1633, X0),
        A2(1634, X0),
        A3(1635, X0),
        A4(1636, X0),
        A5(1637, X0),
        A6(1638, X0),
        A7(1639, X0),
        A8(1640, X0),
        A9(1641, X0),

        COMMA(1548, X0),

        /************** X2 *************/
        ALF(1575, X2),
        ALF_MAD(1570, X2),
        ALF_HAMZA_UP(1571, X2),
        ALF_HAMZA_DOWN(1573, X2),
        ALF_MAQSORA(1609, X2),

        LAM_ALF(65275, X2),
        LAM_ALF_MAD(65269, X2),
        LAM_ALF_HAMZA_UP(65271, X2),
        LAM_ALF_HAMZA_DOWN(65273, X2),

        DAL(1583, X2),
        ZAL(1584, X2),

        RAA(1585, X2),
        ZAY(1586, X2),
        JEH(1688, X2),

        WAW(1608, X2),
        WAW_HAMZA(1572, X2),

        TAA_MARBOTA(1577, X2),

        /************* 4X **************/
        ALF_MAQSORA_HAMZA(65163, X4),

        BAA(65169, X4),
        PEH(64344, X4),
        TAA(65175, X4),
        THA(65179, X4),
        GEM(65183, X4),
        TCHEH(64380, X4),
        HAA(65187, X4),
        KHA(65191, X4),

        SEN(65203, X4),
        SHN(65207, X4),
        SAD(65211, X4),
        DAD(65215, X4),
        TTA(65219, X4),
        ZAA(65223, X4),
        AIN(65227, X4),
        GHN(65231, X4),
        FAA(65235, X4),
        QAF(65239, X4),
        KAF(65243, X4),
        KEHEH(64400, X4),
        GAF(64404, X4),

        LAM(65247, X4),
        MEM(65251, X4),
        NON(65255, X4),
        HHA(65259, X4),

        YAA(65267, X4),
        YEH(64510, X4),;

        public final int id;
        public final int type;

        private static final StartChar[] values = values();

        StartChar(int id, int type) {
            this.id = id;
            this.type = type;
        }

        public static char getChar(int index) {
            return values[index].getChar();
        }

        public char getChar() {
            return (char) id;
        }

        public int getType() {
            return type;
        }
    }

    public enum CenterChar {
        SPACE(32, X0),
        EMPTY(0, X0),

        E0(1776, X0),
        E1(1777, X0),
        E2(1778, X0),
        E3(1779, X0),
        E4(1780, X0),
        E5(1781, X0),
        E6(1782, X0),
        E7(1783, X0),
        E8(1784, X0),
        E9(1785, X0),

        A0(1632, X0),
        A1(1633, X0),
        A2(1634, X0),
        A3(1635, X0),
        A4(1636, X0),
        A5(1637, X0),
        A6(1638, X0),
        A7(1639, X0),
        A8(1640, X0),
        A9(1641, X0),

        COMMA(1548, X0),

        /************** X2 *************/
        ALF(65166, X2),
        ALF_MAD(1570, X2),
        ALF_HAMZA_UP(65156, X2),
        ALF_HAMZA_DOWN(65160, X2),
        ALF_MAQSORA(1609, X2),

        LAM_ALF(65275, X2),
        LAM_ALF_MAD(65269, X2),
        LAM_ALF_HAMZA_UP(65271, X2),
        LAM_ALF_HAMZA_DOWN(65273, X2),

        DAL(65194, X2),
        ZAL(65196, X2),

        RAA(65198, X2),
        ZAY(65200, X2),
        JEH(64395, X2),

        WAW(65262, X2),
        WAW_HAMZA(65158, X2),

        TAA_MARBOTA(65172, X2),

        /*********** 4X ************/
        ALF_MAQSORA_HAMZA(65164, X4),

        BAA(65170, X4),
        PEH(64345, X4),
        TAA(65176, X4),
        THA(65180, X4),
        GEM(65184, X4),
        TCHEH(64381, X4),
        HAA(65188, X4),
        KHA(65192, X4),

        SEN(65204, X4),
        SHN(65208, X4),
        SAD(65212, X4),
        DAD(65216, X4),
        TTA(65220, X4),
        ZAA(65224, X4),
        AIN(65228, X4),
        GHN(65232, X4),
        FAA(65236, X4),
        QAF(65240, X4),
        KAF(65244, X4),
        KEHEH(64401, X4),
        GAF(64405, X4),

        LAM(65248, X4),
        MEM(65252, X4),
        NON(65256, X4),
        HHA(65260, X4),

        YAA(65268, X4),
        YEH(64511, X4),;

        public final int id;
        public final int type;

        private static final CenterChar[] values = values();

        CenterChar(int id, int type) {
            this.id = id;
            this.type = type;
        }

        public static char getChar(int index) {
            return values[index].getChar();
        }

        public char getChar() {
            return (char) id;
        }

        public int getType() {
            return type;
        }
    }
}