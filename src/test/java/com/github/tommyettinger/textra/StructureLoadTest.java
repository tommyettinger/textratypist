package com.github.tommyettinger.textra;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.compression.Lzma;
import com.github.tommyettinger.textra.json.FontData;

import java.io.BufferedInputStream;
import java.io.IOException;

public class StructureLoadTest {
    public static void main(String[] args) throws IOException {
        Gdx.files = new Lwjgl3Files();

        FontData fd;
//        FileHandle jsonHandle = Gdx.files.internal("Birdland-Aeroplane-standard.json.lzma");
        FileHandle jsonHandle = Gdx.files.internal("Crete-Round-sdf.json.lzma");
        BufferedInputStream bais = jsonHandle.read(4096);
        StreamUtils.OptimizedByteArrayOutputStream baos = new StreamUtils.OptimizedByteArrayOutputStream(4096);
        Lzma.decompress(bais, baos);
        Json json = new Json(JsonWriter.OutputType.json);
        fd = json.fromJson(FontData.class, baos.toString("UTF-8"));

        System.out.println(fd.atlas.type);
        System.out.println(fd.metrics.lineHeight);
        System.out.println(fd.glyphs.size());
        System.out.println(fd.kerning != null);

    }
}
