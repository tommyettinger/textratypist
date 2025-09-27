package com.github.tommyettinger.textra.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.compression.Lzma;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Simple static utilities to make using {@link Lzma the LZMA compression in libGDX} easier.
 */
public final class LzmaUtils {
    private LzmaUtils() {

    }

    /**
     * Given an {@code input} FileHandle compressed with {@link Lzma}, and an {@code output} FileHandle that will be
     * overwritten, decompresses input into output.
     * @param input the Lzma-compressed FileHandle to read; typically the file extension ends in ".lzma"
     * @param output the FileHandle to write to
     */
    public static void decompress(FileHandle input, FileHandle output) {
        try (BufferedInputStream is = input.read(4096); OutputStream os = output.write(false)) {
            Lzma.decompress(is, os);
        } catch (IOException e) {
            System.out.println("Decompression failed! " + input + " could not be decompressed to " + output +
                    " because of the IOException: " + e.getMessage());
        }
    }
}
