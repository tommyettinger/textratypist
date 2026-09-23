package com.github.tommyettinger.textra;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.compression.Lzma;
import com.github.tommyettinger.textra.json.FontData;
import org.apache.fory.json.ForyJson;

import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * Specific JSON loading took 0.2912185 seconds.
 * Loading Maple-Mono-standard.json.lzma took 0.3545185 seconds.
 */
public class StructureForyTest {
    public static void main(String[] args) throws IOException {
        Gdx.files = new Lwjgl3Files();
        long startTime = System.nanoTime();

//        FileHandle jsonHandle = Gdx.files.internal("Birdland-Aeroplane-standard.json.lzma");
        FileHandle jsonHandle = Gdx.files.internal("Maple-Mono-standard.json.lzma");
        BufferedInputStream bais = jsonHandle.read(4096);
        StreamUtils.OptimizedByteArrayOutputStream baos = new StreamUtils.OptimizedByteArrayOutputStream(4096);
        Lzma.decompress(bais, baos);
        long innerTime = System.nanoTime();
        final ForyJson json = ForyJson.builder().withFieldMode(true).build();
        FontData fd = json.fromJson(baos.toByteArray(), FontData.class);

        System.out.println("Specific JSON loading took " + (System.nanoTime() - innerTime) * 1E-9 + " seconds.");
        System.out.println("Loading " + jsonHandle.name() + " took " + (System.nanoTime() - startTime) * 1E-9 + " seconds.");
        System.out.println();
        System.out.println(fd.atlas.type);
        System.out.println(fd.metrics.lineHeight);
        System.out.println(fd.glyphs.size());
        System.out.println(fd.kerning != null);

    }
}
