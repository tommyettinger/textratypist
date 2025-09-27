package com.github.tommyettinger.textra;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.files.FileHandle;
import com.github.tommyettinger.textra.utils.LzmaUtils;

import java.util.Arrays;

public class LzmaTest {

    public static void main(String[] args) {
        Gdx.files = new Lwjgl3Files();

        FileHandle inCompressed = Gdx.files.local("src/test/resources/experimental/Grenze-standard.json.lzma");
        FileHandle inUncompressed = Gdx.files.local("src/test/resources/experimental/Grenze-standard.json");
        if(!inCompressed.exists() || !inUncompressed.exists()) {
            System.out.println("Folders are wrong! Check working directory: " + Gdx.files.local("").file().getAbsolutePath());
            return;
        }
        Gdx.files.local("src/test/resources/experimental/copies/").mkdirs();
        FileHandle outCompressed = Gdx.files.local("src/test/resources/experimental/copies/Grenze-standard.json.lzma");
        FileHandle outUncompressed = Gdx.files.local("src/test/resources/experimental/copies/Grenze-standard.json");

        LzmaUtils.compress(inUncompressed, outCompressed);
        LzmaUtils.decompress(inCompressed, outUncompressed);

        System.out.println("Compressed files are equal? " + Arrays.equals(inCompressed.readBytes(), outCompressed.readBytes()));
        System.out.println("Uncompressed files are equal? " + inUncompressed.readString().equals(outUncompressed.readString()));

        outUncompressed.delete();
        outCompressed.delete();
    }
}
