package com.github.tommyettinger.textra;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.LongArray;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.compression.Lzma;
import com.github.tommyettinger.textra.json.FontData;
import org.apache.fory.json.ForyJson;

import java.io.BufferedInputStream;
import java.io.IOException;

// Checked with 2.6.0 .

/**
 * Tests loading all standard JSON Fonts using Fory-JSON.
 * <br>
 * <pre>
 *  126411700 ns to load A-Starry
 *   18679700 ns to load Abyssinica-SIL
 *    7666600 ns to load Asul
 *    6216900 ns to load Aubrey
 *    7418100 ns to load Birdland-Aeroplane
 *    9955800 ns to load Bitter
 *    8195500 ns to load Bonheur-Royale
 *    9824300 ns to load Canada1500
 *    9080400 ns to load Cascadia-Mono
 *    6116800 ns to load Caveat
 *    4195800 ns to load Changa-One
 *    4046900 ns to load Cinzel
 *    3765500 ns to load Comic-Mono
 *    9809400 ns to load Computer-Says-No
 *    4669600 ns to load Courier-Prime
 *    4299500 ns to load Crete-Round
 *   32705600 ns to load DejaVu-Sans-Condensed
 *   10595800 ns to load DejaVu-Sans-Mono
 *   17003500 ns to load DejaVu-Sans
 *   10506100 ns to load DejaVu-Serif-Condensed
 *   11448100 ns to load DejaVu-Serif
 *    2653900 ns to load DINish
 *    2869200 ns to load DINish-Light
 *    2495700 ns to load DINish-Heavy
 *    2545100 ns to load DINish-Condensed
 *    2447900 ns to load DINish-Condensed-Light
 *    3460400 ns to load DINish-Condensed-Heavy
 *    2715300 ns to load DINish-Expanded
 *    2490500 ns to load DINish-Expanded-Light
 *    2703400 ns to load DINish-Expanded-Heavy
 *    9239200 ns to load Gentium
 *    9619200 ns to load Gentium-Un-Italic
 *    1590400 ns to load Geo
 *    3854600 ns to load Glacial-Indifference
 *   63083500 ns to load Go-Noto-Universal
 *    5126300 ns to load Google-Sans-Flex
 *    4845400 ns to load Google-Sans-Flex-Heavy
 *    4826800 ns to load Google-Sans-Flex-Light
 *    4745300 ns to load Grenze
 *    6807400 ns to load Inconsolata-LGC
 *    6066200 ns to load Indie-Flower
 *   15729300 ns to load Iosevka
 *   10968300 ns to load Iosevka-Charon
 *    7842700 ns to load Iosevka-Slab
 *    5662100 ns to load JetBrains-Mono
 *    2429200 ns to load Jim-Nightshade
 *    1562200 ns to load Kingthings-Foundation
 *    2120100 ns to load Kingthings-Petrock
 *    3536800 ns to load League-Gothic
 *    7991800 ns to load Libertinus-Serif
 *    7140500 ns to load Libertinus-Serif-Semibold
 *   21846500 ns to load Ma-Shan-Zheng
 *   47394100 ns to load Maple-Mono
 *    7828500 ns to load Molle
 *    7415200 ns to load Moon-Dance
 *    6736700 ns to load Nova-Mono
 *    3926200 ns to load Now-Alt
 *    3847000 ns to load Nugothic
 *    6789900 ns to load Open-Sans
 *    4571900 ns to load Ostrich-Black
 *    3433100 ns to load Overlock
 *    2226900 ns to load Overlock-Un-Italic
 *    5662100 ns to load Oxanium
 *    4107100 ns to load Pangolin
 *    3268300 ns to load Protest-Revolution
 *    1620000 ns to load Quantico
 *    3501800 ns to load Roboto-Condensed
 *    4499200 ns to load Sancreek
 *   16173500 ns to load Selawik
 *   11975600 ns to load Selawik-Bold
 *    1903100 ns to load Sour-Gummy
 *    2405100 ns to load Special-Elite
 *    1991600 ns to load Tangerine
 *    2847300 ns to load Tillana
 *    2273100 ns to load Yanone-Kaffeesatz
 *    1561100 ns to load Yataghan
 * Loading 76 Fonts with 130661 glyphs took 0.8480226000000001 seconds.
 * </pre>>
 */
public class StructureForyBulkTest {
    public static void main(String[] args) throws IOException {
        Gdx.files = new Lwjgl3Files();
        Array<String> names = KnownFonts.JSON_NAMES.orderedItems();
        StreamUtils.OptimizedByteArrayOutputStream baos = new StreamUtils.OptimizedByteArrayOutputStream(4096);
        LongArray times = new LongArray(100);
        long startTime = System.nanoTime();
        final ForyJson json = ForyJson.builder().withFieldMode(true).build();
        int totalGlyphs = 0;
        for (int i = 0, n = names.size; i < n; i++) {
            long innerTime = System.nanoTime();
            FileHandle jsonHandle = Gdx.files.internal(names.get(i) + "-standard.json.lzma");
            BufferedInputStream bais = jsonHandle.read(4096);
            baos.reset();
            Lzma.decompress(bais, baos);
            FontData fd = json.fromJson(baos.toByteArray(), FontData.class);

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
