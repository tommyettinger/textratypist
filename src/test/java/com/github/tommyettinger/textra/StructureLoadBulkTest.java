package com.github.tommyettinger.textra;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.compression.Lzma;
import com.github.tommyettinger.textra.json.FontData;

import java.io.BufferedInputStream;
import java.io.IOException;

// Checked with 2.6.0 .

/**
 * Tests loading all standard JSON Fonts using libGDX Json.
 * <br>
 * <pre>
 *   36575500 ns to load A-Starry
 *   32682700 ns to load Abyssinica-SIL
 *    9690800 ns to load Asul
 *    6882800 ns to load Aubrey
 *    9867000 ns to load Birdland-Aeroplane
 *   22376900 ns to load Bitter
 *   14584200 ns to load Bonheur-Royale
 *   33187100 ns to load Canada1500
 *   21244700 ns to load Cascadia-Mono
 *   13048500 ns to load Caveat
 *    5863000 ns to load Changa-One
 *    6682100 ns to load Cinzel
 *    4221700 ns to load Comic-Mono
 *   22033000 ns to load Computer-Says-No
 *    3473400 ns to load Courier-Prime
 *    4050600 ns to load Crete-Round
 *   26532200 ns to load DejaVu-Sans-Condensed
 *   15386900 ns to load DejaVu-Sans-Mono
 *   27996800 ns to load DejaVu-Sans
 *   14379000 ns to load DejaVu-Serif-Condensed
 *   15775200 ns to load DejaVu-Serif
 *    3926000 ns to load DINish
 *    3786100 ns to load DINish-Light
 *    3672400 ns to load DINish-Heavy
 *    5717500 ns to load DINish-Condensed
 *    5489700 ns to load DINish-Condensed-Light
 *    6288300 ns to load DINish-Condensed-Heavy
 *    5776700 ns to load DINish-Expanded
 *    5626800 ns to load DINish-Expanded-Light
 *    5700900 ns to load DINish-Expanded-Heavy
 *   17012400 ns to load Gentium
 *   17511800 ns to load Gentium-Un-Italic
 *    3775500 ns to load Geo
 *    3745900 ns to load Glacial-Indifference
 *  141678400 ns to load Go-Noto-Universal
 *    5921400 ns to load Google-Sans-Flex
 *    5681600 ns to load Google-Sans-Flex-Heavy
 *    5520300 ns to load Google-Sans-Flex-Light
 *    5695700 ns to load Grenze
 *    6830100 ns to load Inconsolata-LGC
 *    4531500 ns to load Indie-Flower
 *   17409100 ns to load Iosevka
 *   25102000 ns to load Iosevka-Charon
 *   17496000 ns to load Iosevka-Slab
 *    9490000 ns to load JetBrains-Mono
 *    4433700 ns to load Jim-Nightshade
 *    2391700 ns to load Kingthings-Foundation
 *    2480500 ns to load Kingthings-Petrock
 *    2565900 ns to load League-Gothic
 *    9971000 ns to load Libertinus-Serif
 *    8849600 ns to load Libertinus-Serif-Semibold
 *   29953600 ns to load Ma-Shan-Zheng
 *   82630000 ns to load Maple-Mono
 *    2020900 ns to load Molle
 *    3140600 ns to load Moon-Dance
 *    5416800 ns to load Nova-Mono
 *    2464900 ns to load Now-Alt
 *    3344600 ns to load Nugothic
 *    6227500 ns to load Open-Sans
 *    2004800 ns to load Ostrich-Black
 *    3782600 ns to load Overlock
 *    3884600 ns to load Overlock-Un-Italic
 *    7193100 ns to load Oxanium
 *    6556300 ns to load Pangolin
 *    4994700 ns to load Protest-Revolution
 *    4343500 ns to load Quantico
 *    6706600 ns to load Roboto-Condensed
 *    4601200 ns to load Sancreek
 *    4036400 ns to load Selawik
 *    4536100 ns to load Selawik-Bold
 *    4149600 ns to load Sour-Gummy
 *    3838100 ns to load Special-Elite
 *    1827400 ns to load Tangerine
 *    2640500 ns to load Tillana
 *    3255700 ns to load Yanone-Kaffeesatz
 *    1799100 ns to load Yataghan
 * Loading 76 Fonts with 130661 glyphs took 0.9224884000000001 seconds.
 * </pre>
 */
public class StructureLoadBulkTest {
    public static void main(String[] args) throws IOException {
        Gdx.files = new Lwjgl3Files();
        Array<String> names = KnownFonts.JSON_NAMES.orderedItems();
        StreamUtils.OptimizedByteArrayOutputStream baos = new StreamUtils.OptimizedByteArrayOutputStream(4096);
        LongArray times = new LongArray(100);
        long startTime = System.nanoTime();
        Json json = new Json(JsonWriter.OutputType.json);
        int totalGlyphs = 0;
        for (int i = 0, n = names.size; i < n; i++) {
            long innerTime = System.nanoTime();
            FileHandle jsonHandle = Gdx.files.internal(names.get(i) + "-standard.json.lzma");
            BufferedInputStream bais = jsonHandle.read(4096);
            baos.reset();
            Lzma.decompress(bais, baos);
            FontData fd = json.fromJson(FontData.class, baos.toString("UTF-8"));

            totalGlyphs += fd.glyphs.size();
            times.add(System.nanoTime() - innerTime);
        }
        startTime = System.nanoTime() - startTime;
        for (int i = 0, n = names.size; i < n; i++) {
            System.out.printf("%10d ns to load %s\n", times.get(i), names.get(i));
        }
        System.out.println("Loading " + names.size + " Fonts with " + totalGlyphs + " glyphs took " + (startTime * 1E-9) + " seconds.");
    }
}
