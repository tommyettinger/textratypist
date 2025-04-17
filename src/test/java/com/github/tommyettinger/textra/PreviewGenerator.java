package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.nio.ByteBuffer;

public class PreviewGenerator extends ApplicationAdapter {

    Font fnt;
    SpriteBatch batch;
    Viewport viewport;
    Layout layout = new Layout().setTargetWidth(1200);
    long startTime;
    static final String text = "Fonts can be rendered normally,{CURLY BRACKETS ARE IGNORED} but using [[tags], you can..."
            + "\n[#E74200]...use CSS-style hex colors like [*]#E74200[*]..."
            + "\n[darker purple blue]...use color names or descriptions, like [/]darker purple blue[/]...[ ]"
            + "\n[_]...and use [!]effects[!][_]!"
            + "\nNormal, [*]bold[*], [/]oblique[/] (like italic), [*][/]bold oblique[ ],"
            + "\n[_]underline (even for multiple words)[_], [~]strikethrough (same)[ ],"
            + "\nscaling: [%50]very [%75]small [%100]to [%150]quite [%200]large[ ], notes: [.]sub-[.], [=]mid-[=], and [^]super-[^]script,"
            + "\ncapitalization changes: [;]Each cap, [,]All lower, [!]Caps lock[ ],"
            + "\n[%^small caps][*]Special[*][%] [%^whiten][/]Effects[/][%]: [%?shadow]drop shadow[%], [%?jostle]RaNsoM nOtE[%], [%?error]spell check[%]...",
//    distanceField = "\nWelcome to the [_][*][TEAL]Textra Zone[ ]!",
    emojiSupport = "\nPlus, there's [_][*][TEAL]emoji[ ] and more! [WHITE][+🥳] [+👍🏻] [+🤙🏼] [+👌🏽] [+🤘🏾] [+✌🏿]";

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Font Preview Generator");
        config.setWindowedMode(1200, 675);
        config.disableAudio(true);
        ShaderProgram.prependVertexCode = "#version 110\n";
        ShaderProgram.prependFragmentCode = "#version 110\n";
//        config.enableGLDebugOutput(true, System.out);
        config.setForegroundFPS(1);
        config.useVsync(true);
        new Lwjgl3Application(new PreviewGenerator(), config);
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        viewport = new StretchViewport(1200, 675);

        // investigating a GPU-related bug... seems fixed now, sometimes?
        // with useIntegerPositions(true), on some discrete GPUs this looks "wobbly," with an uneven baseline.
        // with useIntegerPositions(false), it seems fine?
//        Font[] fonts = {KnownFonts.getCozette().useIntegerPositions(true)};
//        Font[] fonts = {KnownFonts.getGentiumSDF()};
//        Font[] all = KnownFonts.getAll(), sdf = KnownFonts.getAllSDF();
//        Font[] all = new Font[3];
//        String[] fullFaceNames = new String[]{"Iosevka Slab"};
//        String[] fullFieldNames = new String[]{" (MSDF)", " (SDF)", ""};
//        for(String face : new String[]{"Iosevka-Slab"}){

//        String[] fullFaceNames = new String[]{"Gentium Un-Italic", "Iosevka", "Iosevka Slab", "Libertinus Serif"};
//        String[] fullFieldNames = new String[]{" (MSDF)", " (SDF)", ""};
//        Font[] sdf = new Font[fullFaceNames.length];
//        Font[] all = new Font[fullFaceNames.length * fullFieldNames.length + 1];
//        int faceIdx = 0;
//        int sdfIdx = 0;
//        for(String face : new String[]{"GentiumUnItalic", "Iosevka", "Iosevka-Slab", "LibertinusSerif"}){
//            int fieldIdx = 0;
//            for(String field : new String[]{"msdf", "sdf", "standard"}){
//                all[faceIdx * 3 + fieldIdx] = new Font("experimental/"+face+"-"+field+".json",
//                        new TextureRegion(new Texture("experimental/"+face+"-"+field+".png")), 0f, 0f, 0f, 0f, true, true);
////                        .scaleTo(10, 24f)
//
//                all[faceIdx * 3 + fieldIdx]
//                        .scaleHeightTo(24f)
//                        .setName(fullFaceNames[faceIdx] + fullFieldNames[fieldIdx]);
//                if(fieldIdx == 1)
//                    sdf[sdfIdx++] = new Font(all[faceIdx * 3 + fieldIdx]);
//                fieldIdx++;
//            }
//            faceIdx++;
//        }
//        all[all.length-1] = new Font("fontwriter/Yanone-Kaffeesatz-msdf.json",
//                new TextureRegion(new Texture("fontwriter/Yanone-Kaffeesatz-msdf.png")), 0, 0, 0, 0, true, true)
//                .setTextureFilter()
//                .setName("Yanone Kaffeesatz (MSDF)");
//        all[all.length-1].scaleTo(all[all.length-1].originalCellWidth*24f/all[all.length-1].originalCellHeight, 24f);

//        FileHandle[] jsonFiles = Gdx.files.local("src/test/resources/experimental").list(".json");
//        FileHandle[] sdfFiles = new FileHandle[0];

        // WHAT WE NORMALLY USE
        String[] jsonFiles = KnownFonts.JSON_NAMES.orderedItems().toArray(String.class);
        Font[] all = new Font[jsonFiles.length * 4 + 4 + 7];
        int idx = 0;
        for (int i = 0; i < jsonFiles.length; i++) {
            all[idx++] = KnownFonts.getFont(jsonFiles[i], Font.DistanceFieldType.STANDARD).scaleHeightTo(32f).useIntegerPositions(false).setName(jsonFiles[i] + Font.DistanceFieldType.STANDARD.filePart);
            all[idx++] = KnownFonts.getFont(jsonFiles[i], Font.DistanceFieldType.MSDF).scaleHeightTo(32f).useIntegerPositions(false).setName(jsonFiles[i] + Font.DistanceFieldType.MSDF.filePart);
            all[idx++] = KnownFonts.getFont(jsonFiles[i], Font.DistanceFieldType.SDF).scaleHeightTo(32f).useIntegerPositions(false).setName(jsonFiles[i] + Font.DistanceFieldType.SDF.filePart);
            all[idx++] = KnownFonts.getFont(jsonFiles[i], Font.DistanceFieldType.SDF_OUTLINE).scaleHeightTo(32f).useIntegerPositions(false).setName(jsonFiles[i] + "-sdf-outline");
        }
        all[idx++] = KnownFonts.getAStarryTall(Font.DistanceFieldType.STANDARD).scaleHeightTo(32f).useIntegerPositions(false).setName("A-Starry-Tall" + Font.DistanceFieldType.STANDARD.filePart);
        all[idx++] = KnownFonts.getAStarryTall(Font.DistanceFieldType.MSDF).scaleHeightTo(32f).useIntegerPositions(false).setName("A-Starry-Tall" + Font.DistanceFieldType.MSDF.filePart);
        all[idx++] = KnownFonts.getAStarryTall(Font.DistanceFieldType.SDF).scaleHeightTo(32f).useIntegerPositions(false).setName("A-Starry-Tall" + Font.DistanceFieldType.SDF.filePart);
        all[idx++] = KnownFonts.getAStarryTall(Font.DistanceFieldType.SDF_OUTLINE).scaleHeightTo(32f).useIntegerPositions(false).setName("A-Starry-Tall" + "-sdf-outline");

        all[idx++] = KnownFonts.getCordata16x26().setName(KnownFonts.CORDATA_16X26 + "-standard");
        all[idx++] = KnownFonts.getIBM8x16().setName(KnownFonts.IBM_8X16 + "-standard");
        all[idx++] = KnownFonts.getCozette().setName(KnownFonts.COZETTE + "-standard");
        all[idx++] = KnownFonts.getHanazono().setName(KnownFonts.HANAZONO + "-standard");
        all[idx++] = KnownFonts.getIBM8x16Sad().setName(KnownFonts.IBM_8X16_SAD + "-standard");
        all[idx++] = KnownFonts.getLanaPixel().setName(KnownFonts.LANAPIXEL + "-standard");
        all[idx++] = KnownFonts.getQuanPixel().setName(KnownFonts.QUANPIXEL + "-standard");
        // DEBUG
//        Font[] all = new Font[]{KnownFonts.getQuanPixel()};

        fnt = all[0];
//        fnt = fonts[fonts.length - 1];
        Gdx.files.local("out/").mkdirs();
        for (int i = 0; i < all.length; i++) {
            Font font = all[i];
            Color baseColor = font.getDistanceField() == Font.DistanceFieldType.SDF_OUTLINE ? Color.WHITE : Color.DARK_GRAY;
            KnownFonts.addEmoji(font);
            viewport.update(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), true);
            font.resizeDistanceField(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), viewport);
            layout.setBaseColor(baseColor);
            layout.setMaxLines(20);
            layout.setEllipsis(" and so on and so forth...");
//            font.markup("[%300][#44DD22]digital[%]\n[#66EE55]just numeric things \n"
//                    , layout);
            font.markup(text + emojiSupport, layout);
//        font.markup("I wanna thank you all for coming here tonight..."
//                + "\n[#22BB22FF]Hello, [~]World[~]Universe[.]$[=]$[^]$[^]!"
//                + "\nThe [RED]MAW[] of the [/][CYAN]wendigo[/] (wendigo)[] [*]appears[*]!"
//                + "\nThe [_][GRAY]BLADE[] of [*][/][YELLOW]DYNAST-KINGS[] strikes!"
//                + "\n[_][;]Each cap, [,]All lower, [!]Caps lock[], [?]Unknown[]?"
//                + "\n[GOLD]phi[] = (1 + 5[^]0.5[^]) * 0.5"
//                + "\n[ORANGE][*]Mister Bond[*]! This is my right-hand man, Jojo Jimjam."
//                + "\nPchnąć[] w tę łódź [TAN]jeża[] lub ośm skrzyń [PURPLE]fig[]."
//                , layout);
//        layout.clear();
//        font.markup("Good day to you all, sirs and madams!"
//                + "\n[*]Водяно́й[] — в славянской мифологии дух, обитающий в воде, хозяин вод[^][BLUE][[2][]."
//                + "\nВоплощение стихии воды как отрицательного и опасного начала[^][BLUE][[3][].", layout);

            ScreenUtils.clear(0.75f, 0.75f, 0.75f, 1f);
            float x = Gdx.graphics.getBackBufferWidth() * 0.5f;
            float y = (Gdx.graphics.getBackBufferHeight() + layout.getHeight()) * 0.5f;
            batch.begin();
            font.enableShader(batch);
            font.resizeDistanceField(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), viewport);
            font.drawGlyphs(batch, layout, x, y, Align.center);
            batch.end();

            // Modified Pixmap.createFromFrameBuffer() code that uses RGB instead of RGBA
            Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
            final Pixmap pm = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), Pixmap.Format.RGB888);
            ByteBuffer pixels = pm.getPixels();
            Gdx.gl.glReadPixels(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, pixels);
            // End Pixmap.createFromFrameBuffer() modified code

//            Pixmap pm = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
            PixmapIO.writePNG(Gdx.files.local("out/" + font.name + ".png"), pm, 2, true);

//            // DEBUG
//            System.out.println();
//            System.out.println(font.debugString());
        }
//        System.out.println(layout);
        startTime = TimeUtils.millis();
//        fnt.markup(text + emojiSupport, layout.clear());

        Gdx.app.exit();
    }

    @Override
    public void render() {
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        fnt.resizeDistanceField(width, height, viewport);
    }
}
