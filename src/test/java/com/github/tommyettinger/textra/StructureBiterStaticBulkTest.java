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

// Checked with 2.6.0 .

/**
 * Tests loading all standard JSON Fonts using JsonBiter's static mode.
 * <br>
 * <pre>
 *   38043000 ns to load A-Starry
 *   18718300 ns to load Abyssinica-SIL
 *    6610600 ns to load Asul
 *    7410300 ns to load Aubrey
 *   10348400 ns to load Birdland-Aeroplane
 *   11099700 ns to load Bitter
 *    8188700 ns to load Bonheur-Royale
 *   10166200 ns to load Canada1500
 *   14389900 ns to load Cascadia-Mono
 *    9939900 ns to load Caveat
 *    5676300 ns to load Changa-One
 *    6217100 ns to load Cinzel
 *    4153100 ns to load Comic-Mono
 *   14549300 ns to load Computer-Says-No
 *    4719800 ns to load Courier-Prime
 *    4498600 ns to load Crete-Round
 *   21254600 ns to load DejaVu-Sans-Condensed
 *   14900200 ns to load DejaVu-Sans-Mono
 *   16736700 ns to load DejaVu-Sans
 *    9439100 ns to load DejaVu-Serif-Condensed
 *   11066000 ns to load DejaVu-Serif
 *    2806800 ns to load DINish
 *    3191000 ns to load DINish-Light
 *    3075200 ns to load DINish-Heavy
 *    2812900 ns to load DINish-Condensed
 *    2717000 ns to load DINish-Condensed-Light
 *    2853200 ns to load DINish-Condensed-Heavy
 *    2678100 ns to load DINish-Expanded
 *    2393600 ns to load DINish-Expanded-Light
 *    2628000 ns to load DINish-Expanded-Heavy
 *    8227900 ns to load Gentium
 *    9541200 ns to load Gentium-Un-Italic
 *    1457600 ns to load Geo
 *    1356700 ns to load Glacial-Indifference
 * 1650393400 ns to load Go-Noto-Universal
 *    3350400 ns to load Google-Sans-Flex
 *    3084700 ns to load Google-Sans-Flex-Heavy
 *    2718600 ns to load Google-Sans-Flex-Light
 *    3136700 ns to load Grenze
 *    3946900 ns to load Inconsolata-LGC
 *    2346900 ns to load Indie-Flower
 *    8901400 ns to load Iosevka
 *   14619100 ns to load Iosevka-Charon
 *    9854800 ns to load Iosevka-Slab
 *    4914800 ns to load JetBrains-Mono
 *    2400300 ns to load Jim-Nightshade
 *    1587400 ns to load Kingthings-Foundation
 *    2013400 ns to load Kingthings-Petrock
 *    3106100 ns to load League-Gothic
 *    8380700 ns to load Libertinus-Serif
 *    7074200 ns to load Libertinus-Serif-Semibold
 *   27839500 ns to load Ma-Shan-Zheng
 *   59781400 ns to load Maple-Mono
 *    2331100 ns to load Molle
 *    3193800 ns to load Moon-Dance
 *    4654300 ns to load Nova-Mono
 *    2594100 ns to load Now-Alt
 *    1412700 ns to load Nugothic
 *    5188800 ns to load Open-Sans
 *    3024800 ns to load Ostrich-Black
 *    4421300 ns to load Overlock
 *    3818000 ns to load Overlock-Un-Italic
 *    5445800 ns to load Oxanium
 *    5238000 ns to load Pangolin
 *    4888600 ns to load Protest-Revolution
 *    3400600 ns to load Quantico
 *    5723900 ns to load Roboto-Condensed
 *    3477000 ns to load Sancreek
 *    3111200 ns to load Selawik
 *    4408700 ns to load Selawik-Bold
 *    2130100 ns to load Sour-Gummy
 *    1738000 ns to load Special-Elite
 *    1441600 ns to load Tangerine
 *    2023300 ns to load Tillana
 *    2178400 ns to load Yanone-Kaffeesatz
 *    1655300 ns to load Yataghan
 * Loading 76 Fonts with 130661 glyphs took 2.2182071000000003 seconds.
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
