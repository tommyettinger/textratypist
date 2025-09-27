package com.github.tommyettinger.fontwriter;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.compression.Lzma;

import java.io.BufferedInputStream;
import java.io.OutputStream;

/**
 * Simple static utilities to make using {@link Lzma the LZMA compression in libGDX} easier.
 * This simply handles opening and closing compressed and decompressed files without needing to catch IOException.
 * If something goes wrong, this will throw an unchecked exception, a GdxRuntimeException, with more information than
 * the original IOException would have.
 */
public final class LzmaUtils {
    private LzmaUtils() {

    }

    /**
     * Given an {@code input} FileHandle compressed with {@link Lzma}, and an {@code output} FileHandle that will be
     * overwritten, decompresses input into output.
     * @param input the Lzma-compressed FileHandle to read; typically the file extension ends in ".lzma"
     * @param output the FileHandle to write to; will be overwritten
     */
    public static void decompress(FileHandle input, FileHandle output) {
        try (BufferedInputStream is = input.read(4096); OutputStream os = output.write(false)) {
            Lzma.decompress(is, os);
        } catch (Exception e) {
            System.out.println("Decompression failed! " + input + " could not be decompressed to " + output +
                    " because of the IOException: " + e.getMessage());
            throw new GdxRuntimeException("Decompression failed! " + input + " could not be decompressed to " + output,
                    e);
        }
    }

    /**
     * Given an {@code input} FileHandle and an {@code output} FileHandle that will be overwritten, compresses input
     * using {@link Lzma} and writes the result into output.
     * @param input the FileHandle to read; will not be modified
     * @param output the FileHandle to write Lzma-compressed output to; typically the file extension ends in ".lzma"
     */
    public static void compress(FileHandle input, FileHandle output) {
        try (BufferedInputStream is = input.read(4096); OutputStream os = output.write(false)) {
            Lzma.compress(is, os);
        } catch (Exception e) {
            System.out.println("Compression failed! " + input + " could not be compressed to " + output +
                    " because of the IOException: " + e.getMessage());
            throw new GdxRuntimeException("Compression failed! " + input + " could not be compressed to " + output, e);
        }
    }
}
