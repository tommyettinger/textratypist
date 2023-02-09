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

import java.nio.ByteBuffer;

public class PreviewGenerator extends ApplicationAdapter {

    Font fnt;
    SpriteBatch batch;
    Layout layout = new Layout().setTargetWidth(800);
    Array<String> colorNames;
    long startTime;
    static final String text = "Fonts can be rendered normally,{CURLY BRACKETS ARE IGNORED} but using [[tags], you can..."
            + "\n[#E74200]...use CSS-style hex colors like [*]#E74200[*]..."
            + "\n[darker purple blue]...use color names or descriptions, like [/]darker purple blue[/]...[]"
            + "\n[_]...and use [!]effects[!][_]!"
            + "\nNormal, [*]bold[*], [/]oblique[/] (like italic), [*][/]bold oblique[],"
            + "\n[_]underline (even for multiple words)[_], [~]strikethrough (same)[],"
            + "\nscaling: [%50]very [%75]small [%100]to [%150]quite [%200]large[], notes: [.]sub-[.], [=]mid-[=], and [^]super-[^]script,"
            + "\n\ncapitalization changes: [;]Each cap, [,]All lower, [!]Caps lock[],"
            + "\nUnicode support: Pchnąć w tę łódź [BROWN]jeża[] lub ośm skrzyń [PURPLE]fig[].",
    distanceField = "\nWelcome to the [_][*][TEAL]Textra Zone[]!",
    emojiSupport = "\nPlus, there's [_][*][TEAL]emoji[] and more! [WHITE][+🥳] [+👍🏻] [+🤙🏼] [+👌🏽] [+🤘🏾] [+✌🏿]";
/*
AStarry-standard.fnt has descent: -12
AStarry-msdf.fnt has descent: -94
Bitter-standard.fnt has descent: -38
Canada1500-standard.fnt has descent: -15
CascadiaMono-msdf.fnt has descent: -10
Cozette-standard.fnt has descent: -3
DejaVuSansMono-msdf.fnt has descent: 3
Gentium-standard.fnt has descent: -31
Gentium-sdf.fnt has descent: -18
Hanazono-standard.fnt has descent: -5
Inconsolata-LGC-Custom-standard.fnt has descent: -18
Inconsolata-LGC-Custom-msdf.fnt has descent: -10
Iosevka-standard.fnt has descent: -17
Iosevka-msdf.fnt has descent: 5
Iosevka-sdf.fnt has descent: 0
Iosevka-Slab-standard.fnt has descent: -17
Iosevka-Slab-msdf.fnt has descent: 5
Iosevka-Slab-sdf.fnt has descent: 0
KingthingsFoundation-standard.fnt has descent: -40
LibertinusSerif-Regular-msdf.fnt has descent: 17
OpenSans-standard.fnt has descent: -11
Oxanium-standard.fnt has descent: -20
RobotoCondensed-standard.fnt has descent: -13
YanoneKaffeesatz-standard.fnt has descent: -19

*/
    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Font Preview Generator");
        config.setWindowedMode(800, 450);
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
        colorNames = Colors.getColors().keys().toArray();

        // investigating a GPU-related bug... seems fixed now, sometimes?
        // with useIntegerPositions(true), on some discrete GPUs this looks "wobbly," with an uneven baseline.
        // with useIntegerPositions(false), it seems fine?
//        Font[] fonts = {KnownFonts.getCozette().useIntegerPositions(true)};
//        Font[] fonts = {KnownFonts.getGentiumSDF()};
        Font[] fonts = KnownFonts.getAll();
//        fnt = fonts[2];
        fnt = fonts[fonts.length - 1];
        Gdx.files.local("out/").mkdirs();
        int index = 0;
        for (int i = 0; i < fonts.length; i++) {
            Font font = fonts[i];
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
            font.markup(text + (font.distanceField == Font.DistanceFieldType.STANDARD ? emojiSupport : distanceField), layout);
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
//            ScreenUtils.clear(0.3f, 0.3f, 0.3f, 1f);
//        layout.getLine(0).glyphs.set(0, font.markupGlyph('@', "[" + colorNames.get((int)(TimeUtils.timeSinceMillis(startTime) >>> 8) % colorNames.size) + "]"));
            float x = 400, y = (Gdx.graphics.getBackBufferHeight() + layout.getHeight()) * 0.5f - font.descent * font.scaleY;
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
        fnt.markup(text + (fnt.distanceField == Font.DistanceFieldType.STANDARD ? emojiSupport : distanceField), layout.clear());

//        Gdx.app.exit();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.75f, 0.75f, 0.75f, 1f);
//        ScreenUtils.clear(0.3f, 0.3f, 0.3f, 1f);
//        layout.getLine(0).glyphs.set(0, font.markupGlyph('@', "[" + colorNames.get((int)(TimeUtils.timeSinceMillis(startTime) >>> 8) % colorNames.size) + "]"));
        float x = 400, y = (Gdx.graphics.getBackBufferHeight() + layout.getHeight()) * 0.5f - fnt.descent * fnt.scaleY;
        batch.begin();
        fnt.enableShader(batch);
        fnt.drawGlyphs(batch, layout, x, y, Align.center);
        batch.end();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }

    @Override
    public void resize(int width, int height) {
        fnt.resizeDistanceField(width, height);
    }
}
