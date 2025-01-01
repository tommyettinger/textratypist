package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.nio.ByteBuffer;

public class PreviewSimpleTest extends ApplicationAdapter {

    Font fnt;
    SpriteBatch batch;
    Viewport viewport;
    Layout layout = new Layout().setTargetWidth(1200);
    int idx;
    int limit;
    long startTime;
    String[] jsonFiles;
    Font[] all;

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
        config.setForegroundFPS(60);
        config.useVsync(true);
        new Lwjgl3Application(new PreviewSimpleTest(), config);
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
        jsonFiles = KnownFonts.JSON_NAMES.orderedItems().toArray(String.class);
        limit = jsonFiles.length + 1;
        all = new Font[limit * 2];
        int idx = 0;
        for (int i = 0; i < jsonFiles.length; i++) {
//            all[idx++] = KnownFonts.addEmoji(KnownFonts.getFont(jsonFiles[i], Font.DistanceFieldType.STANDARD)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
//            all[idx] = KnownFonts.addEmoji(KnownFonts.getFont(jsonFiles[i], Font.DistanceFieldType.MSDF)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
//            all[idx++] = KnownFonts.addEmoji(KnownFonts.getFont(jsonFiles[i], Font.DistanceFieldType.SDF)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
//            all[idx++] = KnownFonts.addEmoji(KnownFonts.getFont(jsonFiles[i], Font.DistanceFieldType.SDF_OUTLINE)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
            all[idx] = KnownFonts.addEmoji(KnownFonts.getFont(jsonFiles[i], Font.DistanceFieldType.SDF)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
            all[idx++ + limit] = KnownFonts.addEmoji(KnownFonts.getFont(jsonFiles[i], Font.DistanceFieldType.SDF_OUTLINE)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
        }
//        all[idx++] = KnownFonts.addEmoji(KnownFonts.getAStarryTall(Font.DistanceFieldType.STANDARD)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
//        all[idx] = KnownFonts.addEmoji(KnownFonts.getAStarryTall(Font.DistanceFieldType.MSDF)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
        all[idx] = KnownFonts.addEmoji(KnownFonts.getAStarryTall(Font.DistanceFieldType.SDF)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
        all[idx++ + limit] = KnownFonts.addEmoji(KnownFonts.getAStarryTall(Font.DistanceFieldType.SDF_OUTLINE)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
        limit += idx;
        idx = 0;

        fnt = all[idx];
        generateLayout();
    }

    public void generateLayout() {
        Color baseColor = fnt.getDistanceField() == Font.DistanceFieldType.SDF_OUTLINE ? Color.WHITE : Color.DARK_GRAY;
        viewport.update(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), true);
        fnt.resizeDistanceField(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), viewport);

        layout.setBaseColor(baseColor);
        layout.setMaxLines(20);
        layout.setEllipsis(" and so on and so forth...");
        fnt.markup(text + emojiSupport, layout.clear());

    }
    @Override
    public void render() {
        if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.MINUS)){
            idx = (idx + limit - 1) % limit;
            fnt = all[idx];
            generateLayout();
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.EQUALS)){
            idx = (idx + 1) % limit;
            fnt = all[idx];
            generateLayout();
        }
        ScreenUtils.clear(0.75f, 0.75f, 0.75f, 1f);
        float x = Gdx.graphics.getBackBufferWidth() * 0.5f;
        float y = (Gdx.graphics.getBackBufferHeight() + layout.getHeight()) * 0.5f;// - font.descent * font.scaleY;
        batch.begin();
        fnt.enableShader(batch);
        fnt.resizeDistanceField(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), viewport);
        fnt.drawGlyphs(batch, layout, x, y, Align.center);
        batch.end();

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        fnt.resizeDistanceField(width, height, viewport);
    }
}
