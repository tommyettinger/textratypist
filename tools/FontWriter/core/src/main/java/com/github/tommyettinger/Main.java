package com.github.tommyettinger;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.ByteArray;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

import java.io.*;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    static private final byte[] SIGNATURE = {(byte)137, 80, 78, 71, 13, 10, 26, 10};
    static private final int IHDR = 0x49484452, IDAT = 0x49444154, IEND = 0x49454E44,
            PLTE = 0x504C5445, TRNS = 0x74524E53;
    static private final byte COLOR_INDEXED = 3;
    static private final byte COMPRESSION_DEFLATE = 0;
    static private final byte INTERLACE_NONE = 0;
    static private final byte FILTER_NONE = 0;

    private final ChunkBuffer buffer = new ChunkBuffer(65536);
    private final Deflater deflater = new Deflater(0);
    private ByteArray curLineBytes;
    private ByteArray prevLineBytes;
    private int lastLineLen;
    private final String[] args;

    public Main(String[] args) {
        if(args == null || args.length == 0) {
            System.out.println("This tool needs some parameters!");
            System.exit(1);
        }
        this.args = args;

    }
    @Override
    public void create() {
        Gdx.files.local("fonts").mkdirs();
        Gdx.files.local("previews").mkdirs();
        String fontFileName = args[0], fontName = fontFileName.substring(Math.max(fontFileName.lastIndexOf('/'), fontFileName.lastIndexOf('\\')) + 1, fontFileName.lastIndexOf('.'));

        System.out.println("Generating structured JSON font and PNG using msdf-atlas-gen...");
        String cmd = "distbin/msdf-atlas-gen -font \"" + fontFileName + "\" -allglyphs" +
                " -type "+("standard".equals(args[1]) ? "softmask" : args[1])+" -imageout \"fonts/"+fontName+"-"+args[1]+".png\" -json \"fonts/"+fontName+"-"+args[1]+".json\" " +
                "-pxrange 8 -dimensions 2048 2048 -size " + args[2];
        ProcessBuilder builder =
                new ProcessBuilder(cmd.split(" "));
        builder.directory(new File(Gdx.files.getLocalStoragePath()));
        builder.inheritIO();
        try {
            int exitCode = builder.start().waitFor();
            if(exitCode != 0) {
                System.out.println("msdf-atlas-gen failed, returning exit code " + exitCode + "; terminating.");
                System.exit(exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Applying changes for improved TextraTypist usage...");
        process(Gdx.files.local("fonts/"+fontName+"-"+args[1]+".png"));

        System.out.println("Optimizing result with oxipng...");
        builder.command(("distbin/oxipng -o 6 -s \"fonts/"+fontName+"-"+args[1]+".png\"").split(" "));
        try {
            int exitCode = builder.start().waitFor();
            if(exitCode != 0) {
                System.out.println("oxipng failed, returning exit code " + exitCode + "; terminating.");
                System.exit(exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
        Gdx.app.exit();
    }

    private void process (FileHandle file) {
        if(!file.exists()) {
            System.out.println("The specified file " + file + " does not exist; skipping.");
            return;
        }
        Pixmap pm = new Pixmap(file);

        final int w = pm.getWidth(), h = pm.getHeight();
        for (int x = w - 3; x < w; x++) {
            for (int y = h - 3; y < h; y++) {
                int color = pm.getPixel(x, y);
                if (!((color & 0xFF) == 0 || (color >>> 8) == 0)) {
                    throw new GdxRuntimeException("Had a transparency problem with " + file.name());
                }
            }
        }
        pm.setColor(-1);
        pm.fillRectangle(w - 3, h - 3, 3, 3);
        if("msdf".equals(args[1])){
            PixmapIO.writePNG(file, pm, 0, false);
            return;
        }

        OutputStream output = file.write(false);
        try {
            DeflaterOutputStream deflaterOutput = new DeflaterOutputStream(buffer, deflater);
            DataOutputStream dataOutput = new DataOutputStream(output);
            try {
                dataOutput.write(SIGNATURE);

                buffer.writeInt(IHDR);
                buffer.writeInt(pm.getWidth());
                buffer.writeInt(pm.getHeight());
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

                int lineLen = pm.getWidth();
                byte[] curLine, prevLine;
                if (curLineBytes == null) {
                    curLine = (curLineBytes = new ByteArray(lineLen)).items;
                    prevLine = (prevLineBytes = new ByteArray(lineLen)).items;
                } else {
                    curLine = curLineBytes.ensureCapacity(lineLen);
                    prevLine = prevLineBytes.ensureCapacity(lineLen);
                    for (int i = 0, n = lastLineLen; i < n; i++) {
                        prevLine[i] = 0;
                    }
                }

                lastLineLen = lineLen;

                int color;
                boolean noWarningNeeded = false;
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        color = pm.getPixel(x, y);
                        // this block may need to be commented out if a font uses non-white grayscale colors.
                        if(noWarningNeeded || ((color & 255) != 0 && (color & 0xFFFFFF00) != 0xFFFFFF00)) {
                            System.out.println("PROBLEM WITH " + file);
                            System.out.printf("Problem color: 0x%08X\n", color);
                            System.out.println("Position: " + x + "," + y);
                            noWarningNeeded = true;
                        }
                        curLine[x] = (byte) (color & 255);
                    }

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