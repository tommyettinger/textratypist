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

// Checked with 2.6.0 .

/**
 * Tests loading all standard JSON Fonts using JsonBiter's dynamic mode.
 * <br>
 * <pre>
 *   72026500 ns to load A-Starry
 *   23569000 ns to load Abyssinica-SIL
 *    8154400 ns to load Asul
 *    7242700 ns to load Aubrey
 *   10926800 ns to load Birdland-Aeroplane
 *   14761000 ns to load Bitter
 *    9822900 ns to load Bonheur-Royale
 *   13060100 ns to load Canada1500
 *   16043900 ns to load Cascadia-Mono
 *   10671500 ns to load Caveat
 *    6392500 ns to load Changa-One
 *    6583200 ns to load Cinzel
 *    4803600 ns to load Comic-Mono
 *   17442500 ns to load Computer-Says-No
 *    5745700 ns to load Courier-Prime
 *    6169800 ns to load Crete-Round
 *   25476800 ns to load DejaVu-Sans-Condensed
 *   10289500 ns to load DejaVu-Sans-Mono
 *   16753900 ns to load DejaVu-Sans
 *   10364300 ns to load DejaVu-Serif-Condensed
 *   20145200 ns to load DejaVu-Serif
 *    4784800 ns to load DINish
 *    4156900 ns to load DINish-Light
 *    3540800 ns to load DINish-Heavy
 *    4017400 ns to load DINish-Condensed
 *    2319100 ns to load DINish-Condensed-Light
 *    2462000 ns to load DINish-Condensed-Heavy
 *    2451200 ns to load DINish-Expanded
 *    2388800 ns to load DINish-Expanded-Light
 *    2649700 ns to load DINish-Expanded-Heavy
 *    9081600 ns to load Gentium
 *    9899600 ns to load Gentium-Un-Italic
 *    1293100 ns to load Geo
 *    3313900 ns to load Glacial-Indifference
 * 1676120900 ns to load Go-Noto-Universal
 *    3482500 ns to load Google-Sans-Flex
 *    2542200 ns to load Google-Sans-Flex-Heavy
 *    2982000 ns to load Google-Sans-Flex-Light
 *    2691600 ns to load Grenze
 *    3125300 ns to load Inconsolata-LGC
 *    2064000 ns to load Indie-Flower
 *   10227400 ns to load Iosevka
 *   16142000 ns to load Iosevka-Charon
 *   10152500 ns to load Iosevka-Slab
 *    5301700 ns to load JetBrains-Mono
 *    2728400 ns to load Jim-Nightshade
 *    1896900 ns to load Kingthings-Foundation
 *    2228000 ns to load Kingthings-Petrock
 *    2856900 ns to load League-Gothic
 *    9245800 ns to load Libertinus-Serif
 *    8885200 ns to load Libertinus-Serif-Semibold
 *   28502100 ns to load Ma-Shan-Zheng
 *   66446400 ns to load Maple-Mono
 *    2367100 ns to load Molle
 *    3836300 ns to load Moon-Dance
 *    5169600 ns to load Nova-Mono
 *    2338900 ns to load Now-Alt
 *    1710400 ns to load Nugothic
 *    4575700 ns to load Open-Sans
 *    1565800 ns to load Ostrich-Black
 *    2518700 ns to load Overlock
 *    2489500 ns to load Overlock-Un-Italic
 *    4867100 ns to load Oxanium
 *    4283400 ns to load Pangolin
 *    3178400 ns to load Protest-Revolution
 *    2006000 ns to load Quantico
 *    4505200 ns to load Roboto-Condensed
 *    2776600 ns to load Sancreek
 *    2631600 ns to load Selawik
 *    2319000 ns to load Selawik-Bold
 *    2551400 ns to load Sour-Gummy
 *    2676700 ns to load Special-Elite
 *    2155900 ns to load Tangerine
 *    2993700 ns to load Tillana
 *    3300000 ns to load Yanone-Kaffeesatz
 *    2125200 ns to load Yataghan
 * Loading 76 Fonts with 130661 glyphs took 2.3075328 seconds.
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
