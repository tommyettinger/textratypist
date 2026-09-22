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
import com.github.tommyettinger.textra.json.FontData;
import org.apache.fory.json.ForyJson;

import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * <pre>
 *  117733100 ns to load A-Starry
 *   19459000 ns to load Abyssinica-SIL
 *    6887500 ns to load Asul
 *    6266100 ns to load Aubrey
 *    7839800 ns to load Birdland-Aeroplane
 *   10288000 ns to load Bitter
 *    7442600 ns to load Bonheur-Royale
 *   16255800 ns to load Canada1500
 *    7180100 ns to load Cascadia-Mono
 *   10429500 ns to load Caveat
 *    2075100 ns to load Changa-One
 *    2525400 ns to load Cinzel
 *    1629500 ns to load Comic-Mono
 *    8339100 ns to load Computer-Says-No
 *    2148500 ns to load Courier-Prime
 *    2764200 ns to load Crete-Round
 *   19474700 ns to load DejaVu-Sans-Condensed
 *    9625200 ns to load DejaVu-Sans-Mono
 *   16011200 ns to load DejaVu-Sans
 *   12149600 ns to load DejaVu-Serif-Condensed
 *   12326800 ns to load DejaVu-Serif
 *    5044200 ns to load DINish
 *    4661400 ns to load DINish-Light
 *    4546100 ns to load DINish-Heavy
 *    4784000 ns to load DINish-Condensed
 *    4364600 ns to load DINish-Condensed-Light
 *    4953200 ns to load DINish-Condensed-Heavy
 *    4527600 ns to load DINish-Expanded
 *    4740200 ns to load DINish-Expanded-Light
 *    4636900 ns to load DINish-Expanded-Heavy
 *   12373300 ns to load Gentium
 *   10612000 ns to load Gentium-Un-Italic
 *    3773800 ns to load Geo
 *    3533100 ns to load Glacial-Indifference
 *   60832000 ns to load Go-Noto-Universal
 *    4656200 ns to load Google-Sans-Flex
 *    4337100 ns to load Google-Sans-Flex-Heavy
 *    8742200 ns to load Google-Sans-Flex-Light
 *    2726400 ns to load Grenze
 *    3004100 ns to load Inconsolata-LGC
 *    2174800 ns to load Indie-Flower
 *    7747700 ns to load Iosevka
 *   10270500 ns to load Iosevka-Charon
 *    7076500 ns to load Iosevka-Slab
 *    3611600 ns to load JetBrains-Mono
 *    2149400 ns to load Jim-Nightshade
 *    1297500 ns to load Kingthings-Foundation
 *    1822500 ns to load Kingthings-Petrock
 *    2260500 ns to load League-Gothic
 *    6404500 ns to load Libertinus-Serif
 *    5583500 ns to load Libertinus-Serif-Semibold
 *   19584700 ns to load Ma-Shan-Zheng
 *   50416100 ns to load Maple-Mono
 *    6509000 ns to load Molle
 *    7164100 ns to load Moon-Dance
 *    6763700 ns to load Nova-Mono
 *   10543600 ns to load Now-Alt
 *    3486500 ns to load Nugothic
 *    5884700 ns to load Open-Sans
 *    3478900 ns to load Ostrich-Black
 *    4911300 ns to load Overlock
 *    4353400 ns to load Overlock-Un-Italic
 *    3124200 ns to load Oxanium
 *    2718800 ns to load Pangolin
 *    7226100 ns to load Protest-Revolution
 *    1515700 ns to load Quantico
 *    2933700 ns to load Roboto-Condensed
 *    1870800 ns to load Sancreek
 *    1875700 ns to load Selawik
 *    1958500 ns to load Selawik-Bold
 *    1927000 ns to load Sour-Gummy
 *    2387100 ns to load Special-Elite
 *    1542300 ns to load Tangerine
 *    2247000 ns to load Tillana
 *    2275900 ns to load Yanone-Kaffeesatz
 *    1478000 ns to load Yataghan
 * Loading 76 Fonts with 130661 glyphs took 0.8080959000000001 seconds.
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
