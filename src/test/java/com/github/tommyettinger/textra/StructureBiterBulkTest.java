package com.github.tommyettinger.textra;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.compression.Lzma;
import com.github.tommyettinger.jsonbiter.JsonIterator;
import com.github.tommyettinger.jsonbiter.spi.Config;
import com.github.tommyettinger.textra.json.FontData;

import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * Loading 76 Fonts with 130661 glyphs took 2.3100583 seconds.
 */
public class StructureBiterBulkTest {
    public static void main(String[] args) throws IOException {
        Gdx.files = new Lwjgl3Files();
        Array<String> names = KnownFonts.JSON_NAMES.orderedItems();
        StreamUtils.OptimizedByteArrayOutputStream baos = new StreamUtils.OptimizedByteArrayOutputStream(4096);
        long startTime = System.nanoTime();
        Config cfg = new Config.Builder().omitDefaultValue(true).escapeUnicode(false).build();
        int totalGlyphs = 0;
        for (int i = 0, n = names.size; i < n; i++) {
            FileHandle jsonHandle = Gdx.files.internal(names.get(i) + "-standard.json.lzma");
            BufferedInputStream bais = jsonHandle.read(4096);
            baos.reset();
            Lzma.decompress(bais, baos);
            FontData fd = JsonIterator.deserialize(cfg, baos.toByteArray(), FontData.class);

            totalGlyphs += fd.glyphs.size();
        }

        System.out.println("Loading " + names.size + " Fonts with " + totalGlyphs + " glyphs took " + (System.nanoTime() - startTime) * 1E-9 + " seconds.");
    }
}
