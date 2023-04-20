package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.nio.ByteBuffer;

public class PreviewGenerator extends ApplicationAdapter {

    Font fnt;
    SpriteBatch batch;
    Viewport viewport;
    Layout layout = new Layout().setTargetWidth(1200);
    Array<String> colorNames;
    long startTime;
    static final String text = "Fonts can be rendered normally,{CURLY BRACKETS ARE IGNORED} but using [[tags], you can..."
            + "\n[#E74200]...use CSS-style hex colors like [*]#E74200[*]..."
            + "\n[darker purple blue]...use color names or descriptions, like [/]darker purple blue[/]...[]"
            + "\n[_]...and use [!]effects[!][_]!"
            + "\nNormal, [*]bold[*], [/]oblique[/] (like italic), [*][/]bold oblique[],"
            + "\n[_]underline (even for multiple words)[_], [~]strikethrough (same)[],"
            + "\nscaling: [%50]very [%75]small [%100]to [%150]quite [%200]large[], notes: [.]sub-[.], [=]mid-[=], and [^]super-[^]script,"
            + "\ncapitalization changes: [;]Each cap, [,]All lower, [!]Caps lock[],"
            + "\n[%^small caps][*]Special[*] [%^whiten][/]Effects[/][%]: [%?shadow]drop shadow[%], [%?jostle]RaNsoM nOtE[%], [%?error]spell check[%],",
    distanceField = "\nWelcome to the [_][*][TEAL]Textra Zone[]!",
    emojiSupport = "\nPlus, there's [_][*][TEAL]emoji[] and more! [WHITE][+🥳] [+👍🏻] [+🤙🏼] [+👌🏽] [+🤘🏾] [+✌🏿]";

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Font Preview Generator");
        config.setWindowedMode(1200, 675);
        config.disableAudio(true);
        ShaderProgram.prependVertexCode = "#version 110\n";
        ShaderProgram.prependFragmentCode = "#version 110\n";
        config.enableGLDebugOutput(true, System.out);
        config.setForegroundFPS(1);
        config.useVsync(true);
        new Lwjgl3Application(new PreviewGenerator(), config);
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        viewport = new StretchViewport(1200, 675);
        colorNames = Colors.getColors().keys().toArray();

        // investigating a GPU-related bug... seems fixed now, sometimes?
        // with useIntegerPositions(true), on some discrete GPUs this looks "wobbly," with an uneven baseline.
        // with useIntegerPositions(false), it seems fine?
//        Font[] fonts = {KnownFonts.getCozette().useIntegerPositions(true)};
//        Font[] fonts = {KnownFonts.getGentiumSDF()};
        Font[] fonts = KnownFonts.getAll();
        fnt = fonts[13];
//        fnt = fonts[fonts.length - 1];
        Gdx.files.local("out/").mkdirs();
        int index = 0;
        for (int i = 0; i < fonts.length; i++) {
            Font font = fonts[i];
            if(!font.integerPosition)
                font.scale(1.5f, 1.5f);
//        Font font = fnt = fonts[0]; {
            KnownFonts.addEmoji(font);
            font.resizeDistanceField(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
//        font = new Font(new BitmapFont(Gdx.files.internal("OpenSans-standard.fnt")), Font.DistanceFieldType.STANDARD, 0f, 0f, 0f, 0f)
//                .scale(0.5f, 0.5f).setTextureFilter();
//        font = new Font(new BitmapFont(Gdx.files.internal("Gentium.fnt")), Font.DistanceFieldType.STANDARD, -1f, 0f, -4.5f, 0f)
//                .scale(0.42f, 0.42f).setTextureFilter();
//      font = new Font("Gentium.fnt", Font.DistanceFieldType.STANDARD, -1f, 0f, -4.5f, 0f).scale(0.35f, 0.35f)
//          .setTextureFilter().adjustLineHeight(0.8f);
//        font = new Font("LibertinusSerif.fnt",
//                new TextureRegion(new Texture(Gdx.files.internal("LibertinusSerif.png"), true)), Font.DistanceFieldType.STANDARD, 0, 0, 0, 0)
//        .scale(0.25f, 0.25f).setTextureFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.MipMapLinearNearest);
//        font = KnownFonts.getLibertinusSerif().scaleTo(165, 40);
//        font = KnownFonts.getCozette().scale(2, 2);
//        font = KnownFonts.getGentium().scaleTo(31, 35);
//        font = KnownFonts.getGentiumSDF().scaleTo(60, 45).adjustLineHeight(0.8f);
//        font = KnownFonts.getAStarry();
//        font = KnownFonts.getIosevkaSlab().scaleTo(12, 28);
//        font = KnownFonts.getInconsolataLGC().scaleTo(12, 40);
//        font = KnownFonts.getIosevka().scaleTo(10, 30);
//        font = KnownFonts.getIosevkaSlab().scaleTo(10, 30);
//        font = KnownFonts.getIosevkaSDF().scaleTo(10, 25);
//        font = KnownFonts.getIosevkaSlabSDF().scaleTo(10, 25);
//        font = KnownFonts.getIosevkaMSDF().scaleTo(10, 25);
//        font = KnownFonts.getIosevkaSlabMSDF().scaleTo(10, 25);
//        font = KnownFonts.getCozette().scaleTo(7, 13);
//        font = KnownFonts.getOpenSans().scaleTo(25, 35);
//        font = KnownFonts.getAStarry().scaleTo(18, 18);
//        font = KnownFonts.getCascadiaMono().scaleTo(10, 20);
//        font = new Font("Iosevka-sdf.fnt", "Iosevka-sdf.png", Font.DistanceFieldType.SDF, 0, 0, 0, 0).scaleTo(12f, 24f);
//        font = KnownFonts.getIBM8x16();
//        font = new Font("Iosevka-Slab-msdf.fnt", "Iosevka-Slab-msdf.png", MSDF, 3f, 6, 16f, -7).scaleTo(16, 16);
            layout.setBaseColor(Color.DARK_GRAY);
            layout.setMaxLines(20);
            layout.setEllipsis(" and so on and so forth...");
//            font.markup("[%300][#44DD22]digital[%]\n[#66EE55]just numeric things \n"
//                    , layout);
            font.markup(text + (font.distanceField != Font.DistanceFieldType.MSDF ? emojiSupport : distanceField), layout);
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
            float y = (Gdx.graphics.getBackBufferHeight() + layout.getHeight()) * 0.5f - font.descent * font.scaleY;
            batch.begin();
            font.enableShader(batch);
            font.drawGlyphs(batch, layout, x, y, Align.center);
            batch.end();

            // Modified Pixmap.createFromFrameBuffer() code that uses RGB instead of RGBA
            Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
            final Pixmap pm = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), Pixmap.Format.RGB888);
            ByteBuffer pixels = pm.getPixels();
            Gdx.gl.glReadPixels(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, pixels);
            // End Pixmap.createFromFrameBuffer() modified code

//            Pixmap pm = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
            PixmapIO.writePNG(Gdx.files.local("out/image" + (index++) + ".png"), pm, 6, true);
        }
//        System.out.println(layout);
        startTime = TimeUtils.millis();
        fnt.markup(text + (fnt.distanceField != Font.DistanceFieldType.MSDF ? emojiSupport : distanceField), layout.clear());

        Gdx.app.exit();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.75f, 0.75f, 0.75f, 1f);
//        ScreenUtils.clear(0.3f, 0.3f, 0.3f, 1f);
//        layout.getLine(0).glyphs.set(0, font.markupGlyph('@', "[" + colorNames.get((int)(TimeUtils.timeSinceMillis(startTime) >>> 8) % colorNames.size) + "]"));
        float x = Gdx.graphics.getBackBufferWidth() * 0.5f;
        float y = (Gdx.graphics.getBackBufferHeight() + layout.getHeight()) * 0.5f - fnt.descent * fnt.scaleY;
        viewport.apply();
        batch.begin();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        fnt.enableShader(batch);
        fnt.drawGlyphs(batch, layout, x, y, Align.center);
        batch.end();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        fnt.resizeDistanceField(width, height);
    }
}
