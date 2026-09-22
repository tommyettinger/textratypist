package com.github.tommyettinger.textra;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.compression.Lzma;
import com.github.tommyettinger.textra.json.FontData;

import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * Loading 76 Fonts with 130661 glyphs took 0.9248839000000001 seconds.
 */
public class StructureLoadBulkTest {
    public static void main(String[] args) throws IOException {
        Gdx.files = new Lwjgl3Files();
        Array<String> names = KnownFonts.JSON_NAMES.orderedItems();
        StreamUtils.OptimizedByteArrayOutputStream baos = new StreamUtils.OptimizedByteArrayOutputStream(4096);
        long startTime = System.nanoTime();
        Json json = new Json(JsonWriter.OutputType.json);
        int totalGlyphs = 0;
        for (int i = 0, n = names.size; i < n; i++) {
            FileHandle jsonHandle = Gdx.files.internal(names.get(i) + "-standard.json.lzma");
            BufferedInputStream bais = jsonHandle.read(4096);
            baos.reset();
            Lzma.decompress(bais, baos);
            FontData fd = json.fromJson(FontData.class, baos.toString("UTF-8"));

            totalGlyphs += fd.glyphs.size();
        }

        System.out.println("Loading " + names.size + " Fonts with " + totalGlyphs + " glyphs took " + (System.nanoTime() - startTime) * 1E-9 + " seconds.");
    }
}
