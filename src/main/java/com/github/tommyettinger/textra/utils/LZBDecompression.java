/*
 * Copyright (c) 2020-2024 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.tommyettinger.textra.utils;

import com.badlogic.gdx.utils.ByteArray;

import java.util.ArrayList;

/**
 * Decompresses byte arrays to Strings using a type of LZ-compression.
 * This is the counterpart to {@link LZBCompression}.
 * <br>
 * This is loosely based on LZ-String.
 * The LZ-String algorithm was formulated by <a href="https://github.com/pieroxy/lz-string">pieroxy</a>.
 * This was loosely based on a port/optimization attempt on another port (to Java),
 * <a href="https://github.com/rufushuang/lz-string4java">LZString4Java By Rufus Huang</a>.
 */
public final class LZBDecompression {
    private LZBDecompression() {
    }
    /**
     * Decompresses a byte array compressed with LZB, getting the original
     * String back that was given to a compression method.
     * @param compressedBytes a byte array compressed with LZB
     * @return the String that was originally given to be compressed
     */
    public static String decompressFromBytes(byte[] compressedBytes) {
        return decompressFromBytes(compressedBytes, 0, compressedBytes.length);
    }

    /**
     * Decompresses a libGDX ByteArray compressed with LZB, getting the original
     * String back that was given to a compression method.
     * @param compressedBytes a libGDX ByteArray compressed with LZB
     * @return the String that was originally given to be compressed
     */
    public static String decompressFromByteArray(ByteArray compressedBytes) {
        return decompressFromBytes(compressedBytes.items, 0, compressedBytes.size);
    }
    /**
     * Decompresses a byte array compressed with LZB, getting the original
     * String back that was given to a compression method.
     * @param compressedBytes a byte array compressed with LZB
     * @param offset where to start reading in compressedBytes
     * @param length how many bytes to read from compressedBytes
     * @return the String that was originally given to be compressed
     */
    public static String decompressFromBytes(byte[] compressedBytes, int offset, int length) {
        if(compressedBytes == null)
            return null;
        if(length <= 0)
            return "";
        final int resetValue = 128;
        ArrayList<String> dictionary = new ArrayList<>(256);
        int enlargeIn = 4, dictSize = 4, numBits = 3, position = resetValue, index = offset+1, resb, maxpower, power;
        String entry, w, c;
        StringBuilder res = new StringBuilder(length);
        char bits;
        int val = compressedBytes[offset];

        for (char i = 0; i < 3; i++) {
            dictionary.add(String.valueOf(i));
        }

        bits = 0;
        maxpower = 2;
        power = 0;
        while (power != maxpower) {
            resb = val & position;
            position >>>= 1;
            if (position == 0) {
                position = resetValue;
                val = compressedBytes[index++];
            }
            bits |= (resb != 0 ? 1 << power : 0);
            power++;
        }

        switch (bits) {
            case 0:
                bits = 0;
                maxpower = 8;
                power = 0;
                while (power != maxpower) {
                    resb = val & position;
                    position >>>= 1;
                    if (position == 0) {
                        position = resetValue;
                        val = compressedBytes[index++];
                    }
                    bits |= (resb != 0 ? 1 << power : 0);
                    power++;
                }
                c = String.valueOf(bits);
                break;
            case 1:
                bits = 0;
                maxpower = 16;
                power = 0;
                while (power != maxpower) {
                    resb = val & position;
                    position >>>= 1;
                    if (position == 0) {
                        position = resetValue;
                        val = compressedBytes[index++];
                    }
                    bits |= (resb != 0 ? 1 << power : 0);
                    power++;
                }
                c = String.valueOf(bits);
                break;
            default:
                return "";
        }
        dictionary.add(c);
        w = c;
        res.append(w);
        while (true) {
            if (index - offset > length) {
                return "";
            }
            int cc = 0;
            maxpower = numBits;
            power = 0;
            while (power != maxpower) {
                resb = val & position;
                position >>>= 1;
                if (position == 0) {
                    position = resetValue;
                    val = compressedBytes[index++];
                }
                cc |= (resb != 0 ? 1 << power : 0);
                power++;
            }
            switch (cc) {
                case 0:
                    bits = 0;
                    maxpower = 8;
                    power = 0;
                    while (power != maxpower) {
                        resb = val & position;
                        position >>>= 1;
                        if (position == 0) {
                            position = resetValue;
                            val = compressedBytes[index++];
                        }
                        bits |= (resb != 0 ? 1 << power : 0);
                        power++;
                    }

                    dictionary.add(String.valueOf(bits));
                    cc = dictSize++;
                    enlargeIn--;
                    break;
                case 1:
                    bits = 0;
                    maxpower = 16;
                    power = 0;
                    while (power != maxpower) {
                        resb = val & position;
                        position >>>= 1;
                        if (position == 0) {
                            position = resetValue;
                            val = compressedBytes[index++];
                        }
                        bits |= (resb != 0 ? 1 << power : 0);
                        power++;
                    }
                    dictionary.add(String.valueOf(bits));
                    cc = dictSize++;
                    enlargeIn--;
                    break;
                case 2:
                    return res.toString();
            }

            if (enlargeIn == 0) {
                enlargeIn = 1 << numBits;
                numBits++;
            }

            if (cc < dictionary.size() && dictionary.get(cc) != null) {
                entry = dictionary.get(cc);
            } else {
                if (cc == dictSize) {
                    entry = w + w.charAt(0);
                } else {
                    return "";
                }
            }
            res.append(entry);

            // Add w+entry[0] to the dictionary.
            dictionary.add(w + entry.charAt(0));
            dictSize++;
            enlargeIn--;

            w = entry;

            if (enlargeIn == 0) {
                enlargeIn = 1 << numBits;
                numBits++;
            }

        }
    }
}
