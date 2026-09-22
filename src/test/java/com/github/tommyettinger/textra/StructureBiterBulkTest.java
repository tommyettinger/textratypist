package com.github.tommyettinger.textra;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.compression.Lzma;
import com.github.tommyettinger.jsonbiter.JsonIterator;
import com.github.tommyettinger.jsonbiter.spi.Config;
import com.github.tommyettinger.textra.json.FontData;

import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * <pre>
 *   56706800 ns to load A-Starry
 *   22366400 ns to load Abyssinica-SIL
 *    7421300 ns to load Asul
 *    8416100 ns to load Aubrey
 *    9400500 ns to load Birdland-Aeroplane
 *   14785200 ns to load Bitter
 *    8444800 ns to load Bonheur-Royale
 *   12539800 ns to load Canada1500
 *   19068900 ns to load Cascadia-Mono
 *   10477800 ns to load Caveat
 *    8743300 ns to load Changa-One
 *    7741500 ns to load Cinzel
 *    4205600 ns to load Comic-Mono
 *   22073900 ns to load Computer-Says-No
 *    5869600 ns to load Courier-Prime
 *    6698900 ns to load Crete-Round
 *   25863100 ns to load DejaVu-Sans-Condensed
 *    9983500 ns to load DejaVu-Sans-Mono
 *   15473200 ns to load DejaVu-Sans
 *   10437500 ns to load DejaVu-Serif-Condensed
 *   19574800 ns to load DejaVu-Serif
 *    4810400 ns to load DINish
 *    5289300 ns to load DINish-Light
 *    5067000 ns to load DINish-Heavy
 *    2543800 ns to load DINish-Condensed
 *    2229800 ns to load DINish-Condensed-Light
 *    2311200 ns to load DINish-Condensed-Heavy
 *    2289900 ns to load DINish-Expanded
 *    2279800 ns to load DINish-Expanded-Light
 *    2563700 ns to load DINish-Expanded-Heavy
 *    8870800 ns to load Gentium
 *   10975200 ns to load Gentium-Un-Italic
 *    3507000 ns to load Geo
 *    3241300 ns to load Glacial-Indifference
 * 1690136700 ns to load Go-Noto-Universal
 *    3631400 ns to load Google-Sans-Flex
 *    2928900 ns to load Google-Sans-Flex-Heavy
 *    2774300 ns to load Google-Sans-Flex-Light
 *    3027300 ns to load Grenze
 *    3505900 ns to load Inconsolata-LGC
 *    2351200 ns to load Indie-Flower
 *    9468200 ns to load Iosevka
 *   16457000 ns to load Iosevka-Charon
 *   10302400 ns to load Iosevka-Slab
 *    4609600 ns to load JetBrains-Mono
 *    2073000 ns to load Jim-Nightshade
 *    1162200 ns to load Kingthings-Foundation
 *    1589100 ns to load Kingthings-Petrock
 *    2199300 ns to load League-Gothic
 *    8157600 ns to load Libertinus-Serif
 *    9152300 ns to load Libertinus-Serif-Semibold
 *   28005500 ns to load Ma-Shan-Zheng
 *   64519800 ns to load Maple-Mono
 *    3769900 ns to load Molle
 *    3362900 ns to load Moon-Dance
 *    4893700 ns to load Nova-Mono
 *    2294500 ns to load Now-Alt
 *    1522700 ns to load Nugothic
 *    4471600 ns to load Open-Sans
 *    1431500 ns to load Ostrich-Black
 *    2486400 ns to load Overlock
 *    2143100 ns to load Overlock-Un-Italic
 *    4136600 ns to load Oxanium
 *    4237600 ns to load Pangolin
 *    3194000 ns to load Protest-Revolution
 *    2018200 ns to load Quantico
 *    4076700 ns to load Roboto-Condensed
 *    2486700 ns to load Sancreek
 *    2459000 ns to load Selawik
 *    2964700 ns to load Selawik-Bold
 *    2813100 ns to load Sour-Gummy
 *    2543500 ns to load Special-Elite
 *    1990900 ns to load Tangerine
 *    2644600 ns to load Tillana
 *    3062100 ns to load Yanone-Kaffeesatz
 *    2347300 ns to load Yataghan
 * Loading 76 Fonts with 130661 glyphs took 2.3048463000000003 seconds.
 * </pre>>
 */
public class StructureBiterBulkTest {
    public static void main(String[] args) throws IOException {
        Gdx.files = new Lwjgl3Files();
        Array<String> names = KnownFonts.JSON_NAMES.orderedItems();
        StreamUtils.OptimizedByteArrayOutputStream baos = new StreamUtils.OptimizedByteArrayOutputStream(4096);
        LongArray times = new LongArray(100);
        long startTime = System.nanoTime();
        Config cfg = new Config.Builder().omitDefaultValue(true).escapeUnicode(false).build();
        int totalGlyphs = 0;
        for (int i = 0, n = names.size; i < n; i++) {
            long innerTime = System.nanoTime();
            FileHandle jsonHandle = Gdx.files.internal(names.get(i) + "-standard.json.lzma");
            BufferedInputStream bais = jsonHandle.read(4096);
            baos.reset();
            Lzma.decompress(bais, baos);
            FontData fd = JsonIterator.deserialize(cfg, baos.toByteArray(), FontData.class);

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
