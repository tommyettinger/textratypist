package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class FontTest extends ApplicationAdapter {

    Font font;
    SpriteBatch batch;
    Layout layout = new Layout().setTargetWidth(750);
    Array<String> colorNames;
    long startTime;

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("textramode Font test");
        config.setWindowedMode(800, 400);
        config.disableAudio(true);
        ShaderProgram.prependVertexCode = "#version 150\n";
        ShaderProgram.prependFragmentCode = "#version 150\n";
        config.enableGLDebugOutput(true, System.out);
        config.useVsync(true);
        new Lwjgl3Application(new FontTest(), config);
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        colorNames = Colors.getColors().keys().toArray();
//        font = new Font(new BitmapFont(Gdx.files.internal("OpenSans.fnt")), Font.DistanceFieldType.STANDARD, 0f, 0f, 0f, 0f)
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
//        font = KnownFonts.getGentium().scaleTo(55, 40).adjustLineHeight(0.8f);
//        font = KnownFonts.getAStarry();
        font = KnownFonts.getIosevkaSlab().scaleTo(12, 24);
//        font = KnownFonts.getInconsolataLGC().scaleTo(12, 40);
//        font = KnownFonts.getIosevka().scaleTo(12, 40);
//        font = new Font("Iosevka-distance.fnt", "Iosevka-distance.png", Font.DistanceFieldType.SDF, 0, 0, 0, 0).scaleTo(12f, 24f);
//        font = KnownFonts.getIBM8x16();
//        font = new Font("Iosevka-Slab-msdf.fnt", "Iosevka-Slab-msdf.png", MSDF, 3f, 6, 16f, -7).scaleTo(16, 16);
        layout.setBaseColor(Color.DARK_GRAY);
        layout.setMaxLines(20);
        layout.setEllipsis("...");
        font.markup("@ Fonts can be rendered normally, but using [[tags], you can..."
                + "\n[#E74200]...use CSS-style hex colors like [*]#E74200[*]..."
                + "\n[FOREST]...use named colors from the Colors class, like [/]FOREST[/]...[]"
                + "\n[_]...and use [!]effects[!][_]!"
                + "\nNormal, [*]bold[*], [/]oblique[/] (like italic), [*][/]bold oblique[],"
                + "\n[_]underline (even for multiple words)[_], [~]strikethrough (same)[],"
                + "\nscaling: [%50]very [%75]small [%100]to [%150]quite [%200]large[], notes: [.]sub-[.], [=]mid-[=], and [^]super-[^]script,"
                + "\ncapitalization changes: [;]Each cap, [,]All lower, [!]Caps lock[],"
                + "\nUnicode support: Pchnąć w tę łódź [BROWN]jeża[] lub ośm skrzyń [PURPLE]fig[]."
                + "\nWelcome to the [_][*][TEAL]Textra Zone[]!"
//                + "\nВоплощение стихии воды как отрицательного[^][BLUE][[3][]..."
                , layout);
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

//        for(Line line : layout.lines)
//            font.calculateSize(line);
        System.out.println(layout);
        startTime = TimeUtils.millis();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.8f, 0.8f, 0.8f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        layout.getLine(0).glyphs.set(0, font.markupGlyph('@', "[" + colorNames.get((int)(TimeUtils.timeSinceMillis(startTime) >>> 8) % colorNames.size) + "]"));
        float x = 400, y = layout.getHeight();
        batch.begin();
        font.enableShader(batch);
        font.drawGlyphs(batch, layout, x, y, Align.center);
        batch.end();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }

    @Override
    public void resize(int width, int height) {
        font.resizeDistanceField(width, height);
    }
}
