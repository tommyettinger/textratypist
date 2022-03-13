/*
 * Copyright (c) 2022 See AUTHORS file.
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

package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.ByteArray;
import com.badlogic.gdx.utils.StreamUtils;

import java.io.*;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * This is a somewhat strange runnable. Its purpose is to take images with up to 256 levels of transparency, but only
 * one color used (here, white), and correct the fully transparent color from 0x000000 to match the 0xFFFFFF used
 * everywhere else in the image. What this is useful for, is to make linear filtering look (much) better when used
 * against a light-colored background. It has no real effect if drawing black text, but with white or brightly-colored
 * text on almost any background, it can be very noticeable, especially with bold text.
 * <br>
 * This copies a lot of code from anim8-gdx, which in turn copied a lot from PixmapIO in libGDX.
 */
public class TransparencyProcessor extends ApplicationAdapter {
    static private final byte[] SIGNATURE = {(byte)137, 80, 78, 71, 13, 10, 26, 10};
    static private final int IHDR = 0x49484452, IDAT = 0x49444154, IEND = 0x49454E44,
            PLTE = 0x504C5445, TRNS = 0x74524E53,
            acTL = 0x6163544C, fcTL = 0x6663544C, fdAT = 0x66644154;
    static private final byte COLOR_INDEXED = 3;
    static private final byte COMPRESSION_DEFLATE = 0;
    static private final byte INTERLACE_NONE = 0;
    static private final byte FILTER_NONE = 0;
//    static private final byte FILTER_PAETH = 4;

    private final ChunkBuffer buffer;
    private final Deflater deflater;
    private ByteArray curLineBytes;
    private ByteArray prevLineBytes;
    private boolean flipY = false;
    private int lastLineLen;

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("textramode block stamper tool");
        config.setWindowedMode(800, 400);
        config.disableAudio(true);
        ShaderProgram.prependVertexCode = "#version 110\n";
        ShaderProgram.prependFragmentCode = "#version 110\n";
        config.useVsync(true);
        new Lwjgl3Application(new TransparencyProcessor(), config);
    }


    public TransparencyProcessor() {
        buffer = new ChunkBuffer(65536);
        deflater = new Deflater();
    }

    @Override
    public void create() {
        FileHandle[] files = Gdx.files.local("knownFonts").list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".png") && !name.endsWith("-msdf.png");
            }
        });
        for (FileHandle file : files)
            rewrite(file);
        Gdx.app.exit();
    }

    private void rewrite (FileHandle file) {
        Pixmap pixmap = new Pixmap(file);
        OutputStream output = file.write(false);
        try {
            DeflaterOutputStream deflaterOutput = new DeflaterOutputStream(buffer, deflater);
            DataOutputStream dataOutput = new DataOutputStream(output);
            try {
                dataOutput.write(SIGNATURE);

                buffer.writeInt(IHDR);
                buffer.writeInt(pixmap.getWidth());
                buffer.writeInt(pixmap.getHeight());
                buffer.writeByte(8); // 8 bits per component.
                buffer.writeByte(COLOR_INDEXED);
                buffer.writeByte(COMPRESSION_DEFLATE);
                buffer.writeByte(FILTER_NONE);
                buffer.writeByte(INTERLACE_NONE);
                buffer.endChunk(dataOutput);

                buffer.writeInt(PLTE);
                for (int i = 0; i < 256; i++) {
                    buffer.write(255);
                    buffer.write(255);
                    buffer.write(255);
                }
                buffer.endChunk(dataOutput);

                buffer.writeInt(TRNS);

                for (int i = 0; i < 256; i++) {
                    buffer.write(i);
                }

                buffer.endChunk(dataOutput);
                buffer.writeInt(IDAT);
                deflater.reset();

                int lineLen = pixmap.getWidth();
//        byte[] lineOut, curLine, prevLine;
                byte[] curLine, prevLine;
                if (curLineBytes == null) {
//            lineOut = (lineOutBytes = new ByteArray(lineLen)).items;
                    curLine = (curLineBytes = new ByteArray(lineLen)).items;
                    prevLine = (prevLineBytes = new ByteArray(lineLen)).items;
                } else {
//            lineOut = lineOutBytes.ensureCapacity(lineLen);
                    curLine = curLineBytes.ensureCapacity(lineLen);
                    prevLine = prevLineBytes.ensureCapacity(lineLen);
                    for (int i = 0, n = lastLineLen; i < n; i++) {
                        prevLine[i] = 0;
                    }
                }

                lastLineLen = lineLen;

                int color;
                final int w = pixmap.getWidth(), h = pixmap.getHeight();
                for (int y = 0; y < h; y++) {
                    int py = flipY ? (h - y - 1) : y;
                    for (int px = 0; px < w; px++) {
                        color = pixmap.getPixel(px, py);
                        if((color & 255) != 0 && (color & 0xFFFFFF00) != 0xFFFFFF00) {
                            System.out.println("PROBLEM WITH " + file);
                            return;
                        }
                        curLine[px] = (byte) (color & 255);
                    }

//            lineOut[0] = (byte)(curLine[0] - prevLine[0]);
//
//            //Paeth
//            for (int x = 1; x < lineLen; x++) {
//                int a = curLine[x - 1] & 0xff;
//                int b = prevLine[x] & 0xff;
//                int c = prevLine[x - 1] & 0xff;
//                int p = a + b - c;
//                int pa = p - a;
//                if (pa < 0) pa = -pa;
//                int pb = p - b;
//                if (pb < 0) pb = -pb;
//                int pc = p - c;
//                if (pc < 0) pc = -pc;
//                if (pa <= pb && pa <= pc)
//                    c = a;
//                else if (pb <= pc)
//                    c = b;
//                lineOut[x] = (byte)(curLine[x] - c);
//            }
//
//            deflaterOutput.write(FILTER_PAETH);
//            deflaterOutput.write(lineOut, 0, lineLen);

                    deflaterOutput.write(FILTER_NONE);
                    deflaterOutput.write(curLine, 0, lineLen);

                    byte[] temp = curLine;
                    curLine = prevLine;
                    prevLine = temp;
                }
                deflaterOutput.finish();
                buffer.endChunk(dataOutput);

                buffer.writeInt(IEND);
                buffer.endChunk(dataOutput);

                output.flush();
            } catch (IOException e) {
                Gdx.app.error("transparency", e.getMessage());
            }
        } finally {
            StreamUtils.closeQuietly(output);
        }
    }

    static class ChunkBuffer extends DataOutputStream {
        final ByteArrayOutputStream buffer;
        final CRC32 crc;

        ChunkBuffer(int initialSize) {
            this(new ByteArrayOutputStream(initialSize), new CRC32());
        }

        private ChunkBuffer(ByteArrayOutputStream buffer, CRC32 crc) {
            super(new CheckedOutputStream(buffer, crc));
            this.buffer = buffer;
            this.crc = crc;
        }

        public void endChunk(DataOutputStream target) throws IOException {
            flush();
            target.writeInt(buffer.size() - 4);
            buffer.writeTo(target);
            target.writeInt((int) crc.getValue());
            buffer.reset();
            crc.reset();
        }
    }

}
