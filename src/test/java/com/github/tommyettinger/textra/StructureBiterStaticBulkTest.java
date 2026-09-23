package com.github.tommyettinger.textra;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.LongArray;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.compression.Lzma;
import com.github.tommyettinger.jsonbiter.JsonIterator;
import com.github.tommyettinger.jsonbiter.spi.Config;
import com.github.tommyettinger.textra.json.FontConfig;
import com.github.tommyettinger.textra.json.FontData;

import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * <pre>
 *   32995800 ns to load A-Starry
 *   19348300 ns to load Abyssinica-SIL
 *    6817000 ns to load Asul
 *    6463600 ns to load Aubrey
 *    9278000 ns to load Birdland-Aeroplane
 *   11633900 ns to load Bitter
 *    8598400 ns to load Bonheur-Royale
 *   15078100 ns to load Canada1500
 *   13963900 ns to load Cascadia-Mono
 *   10425600 ns to load Caveat
 *    5368800 ns to load Changa-One
 *    5028100 ns to load Cinzel
 *    3687200 ns to load Comic-Mono
 *   10436400 ns to load Computer-Says-No
 *    4815400 ns to load Courier-Prime
 *    4137700 ns to load Crete-Round
 *   21071300 ns to load DejaVu-Sans-Condensed
 *   13537500 ns to load DejaVu-Sans-Mono
 *   15613700 ns to load DejaVu-Sans
 *    9441300 ns to load DejaVu-Serif-Condensed
 *   11819700 ns to load DejaVu-Serif
 *    2899500 ns to load DINish
 *    3534600 ns to load DINish-Light
 *    3006500 ns to load DINish-Heavy
 *    2904300 ns to load DINish-Condensed
 *    2855200 ns to load DINish-Condensed-Light
 *    2864400 ns to load DINish-Condensed-Heavy
 *    2691900 ns to load DINish-Expanded
 *    3812700 ns to load DINish-Expanded-Light
 *    2554800 ns to load DINish-Expanded-Heavy
 *    8432400 ns to load Gentium
 *    8455700 ns to load Gentium-Un-Italic
 *    1487400 ns to load Geo
 *    1421900 ns to load Glacial-Indifference
 * 1557003800 ns to load Go-Noto-Universal
 *    2962400 ns to load Google-Sans-Flex
 *    2808000 ns to load Google-Sans-Flex-Heavy
 *    2721000 ns to load Google-Sans-Flex-Light
 *    3470800 ns to load Grenze
 *    3961000 ns to load Inconsolata-LGC
 *    2339000 ns to load Indie-Flower
 *    8816100 ns to load Iosevka
 *   14088500 ns to load Iosevka-Charon
 *    9340400 ns to load Iosevka-Slab
 *    4424900 ns to load JetBrains-Mono
 *    3051000 ns to load Jim-Nightshade
 *    1625100 ns to load Kingthings-Foundation
 *    2456100 ns to load Kingthings-Petrock
 *    2714600 ns to load League-Gothic
 *    8218100 ns to load Libertinus-Serif
 *    7568400 ns to load Libertinus-Serif-Semibold
 *   25913300 ns to load Ma-Shan-Zheng
 *   57747800 ns to load Maple-Mono
 *    2209800 ns to load Molle
 *    2954700 ns to load Moon-Dance
 *    4791100 ns to load Nova-Mono
 *    2382800 ns to load Now-Alt
 *    1448600 ns to load Nugothic
 *    3882700 ns to load Open-Sans
 *    1379400 ns to load Ostrich-Black
 *    2443900 ns to load Overlock
 *    3601900 ns to load Overlock-Un-Italic
 *    3193000 ns to load Oxanium
 *    2817300 ns to load Pangolin
 *    2284700 ns to load Protest-Revolution
 *    1402100 ns to load Quantico
 *    3230500 ns to load Roboto-Condensed
 *    2535100 ns to load Sancreek
 *    2166400 ns to load Selawik
 *    2149100 ns to load Selawik-Bold
 *    2296200 ns to load Sour-Gummy
 *    2270600 ns to load Special-Elite
 *    2101600 ns to load Tangerine
 *    2797200 ns to load Tillana
 *    3482500 ns to load Yanone-Kaffeesatz
 *    2114700 ns to load Yataghan
 * Loading 76 Fonts with 130661 glyphs took 2.0924349 seconds.
 * </pre>
 */
public class StructureBiterStaticBulkTest {
    public static void main(String[] args) throws IOException {
        Gdx.files = new Lwjgl3Files();
        Array<String> names = KnownFonts.JSON_NAMES.orderedItems();
        StreamUtils.OptimizedByteArrayOutputStream baos = new StreamUtils.OptimizedByteArrayOutputStream(4096);
        LongArray times = new LongArray(100);
        long startTime = System.nanoTime();
        new FontConfig().setup();
        int totalGlyphs = 0;
        for (int i = 0, n = names.size; i < n; i++) {
            long innerTime = System.nanoTime();
            FileHandle jsonHandle = Gdx.files.internal(names.get(i) + "-standard.json.lzma");
            BufferedInputStream bais = jsonHandle.read(4096);
            baos.reset();
            Lzma.decompress(bais, baos);
            FontData fd = JsonIterator.deserialize(baos.toByteArray(), FontData.class);

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
