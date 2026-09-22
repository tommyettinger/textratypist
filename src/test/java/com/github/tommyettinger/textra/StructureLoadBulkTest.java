package com.github.tommyettinger.textra;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.compression.Lzma;
import com.github.tommyettinger.textra.json.FontData;

import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * <pre>
 *   34246500 ns to load A-Starry
 *   32206600 ns to load Abyssinica-SIL
 *   10495200 ns to load Asul
 *    8079200 ns to load Aubrey
 *   12657500 ns to load Birdland-Aeroplane
 *   19891800 ns to load Bitter
 *   12871700 ns to load Bonheur-Royale
 *   22878800 ns to load Canada1500
 *   30156400 ns to load Cascadia-Mono
 *   18125400 ns to load Caveat
 *    7999800 ns to load Changa-One
 *   10224500 ns to load Cinzel
 *    4368000 ns to load Comic-Mono
 *   26918500 ns to load Computer-Says-No
 *    5347500 ns to load Courier-Prime
 *    5203000 ns to load Crete-Round
 *   36115900 ns to load DejaVu-Sans-Condensed
 *   14231100 ns to load DejaVu-Sans-Mono
 *   26760600 ns to load DejaVu-Sans
 *   15492000 ns to load DejaVu-Serif-Condensed
 *   18049700 ns to load DejaVu-Serif
 *    5573900 ns to load DINish
 *    4159700 ns to load DINish-Light
 *    3625500 ns to load DINish-Heavy
 *    6000200 ns to load DINish-Condensed
 *    5907100 ns to load DINish-Condensed-Light
 *    5970700 ns to load DINish-Condensed-Heavy
 *    6241100 ns to load DINish-Expanded
 *    6419600 ns to load DINish-Expanded-Light
 *    6584800 ns to load DINish-Expanded-Heavy
 *   18430700 ns to load Gentium
 *   17742700 ns to load Gentium-Un-Italic
 *    3703400 ns to load Geo
 *    3960100 ns to load Glacial-Indifference
 *  131872600 ns to load Go-Noto-Universal
 *    5504900 ns to load Google-Sans-Flex
 *    5778000 ns to load Google-Sans-Flex-Heavy
 *    5839600 ns to load Google-Sans-Flex-Light
 *    5816300 ns to load Grenze
 *    7050200 ns to load Inconsolata-LGC
 *    4953700 ns to load Indie-Flower
 *   16878900 ns to load Iosevka
 *   25273300 ns to load Iosevka-Charon
 *   18222400 ns to load Iosevka-Slab
 *    8827700 ns to load JetBrains-Mono
 *    4006000 ns to load Jim-Nightshade
 *    1337800 ns to load Kingthings-Foundation
 *    2085800 ns to load Kingthings-Petrock
 *    2525400 ns to load League-Gothic
 *    9871200 ns to load Libertinus-Serif
 *    8712000 ns to load Libertinus-Serif-Semibold
 *   30205000 ns to load Ma-Shan-Zheng
 *   84209300 ns to load Maple-Mono
 *    2379600 ns to load Molle
 *    3426200 ns to load Moon-Dance
 *    5468200 ns to load Nova-Mono
 *    2145000 ns to load Now-Alt
 *    3497300 ns to load Nugothic
 *    6334000 ns to load Open-Sans
 *    1686700 ns to load Ostrich-Black
 *    3519300 ns to load Overlock
 *    3519200 ns to load Overlock-Un-Italic
 *    7312800 ns to load Oxanium
 *    6015100 ns to load Pangolin
 *    5163100 ns to load Protest-Revolution
 *    3832100 ns to load Quantico
 *    6423500 ns to load Roboto-Condensed
 *    4406000 ns to load Sancreek
 *    4432200 ns to load Selawik
 *    4133300 ns to load Selawik-Bold
 *    3986800 ns to load Sour-Gummy
 *    3634700 ns to load Special-Elite
 *    1882900 ns to load Tangerine
 *    2642900 ns to load Tillana
 *    2980300 ns to load Yanone-Kaffeesatz
 *    1886200 ns to load Yataghan
 * Loading 76 Fonts with 130661 glyphs took 0.9432142000000001 seconds.
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
