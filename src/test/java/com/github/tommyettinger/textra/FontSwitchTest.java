package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

public class FontSwitchTest extends ApplicationAdapter {

    Font font;
    SpriteBatch batch;
    Layout layout = new Layout().setTargetWidth(780);
    Array<String> colorNames;
    long startTime;

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Multiple Font test");
        config.setWindowedMode(800, 400);
        config.disableAudio(true);
        ShaderProgram.prependVertexCode = "#version 110\n";
        ShaderProgram.prependFragmentCode = "#version 110\n";
        config.enableGLDebugOutput(true, System.out);
        config.useVsync(true);
        new Lwjgl3Application(new FontSwitchTest(), config);
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        colorNames = Colors.getColors().keys().toArray();
        Font.FontFamily family = new Font.FontFamily(
                new String[]{
                        "Gentium", "OpenSans", "Cozette", "Kingthings",
                },
                new Font[]{
                        KnownFonts.getGentium().scaleHeightTo(35),
                        KnownFonts.getOpenSans().scaleHeightTo(35).adjustLineHeight(0.9f),
                        KnownFonts.getCozette().scale(2, 2).adjustLineHeight(0.8f),
                        KnownFonts.getKingthingsFoundation(Font.DistanceFieldType.SDF).scaleHeightTo(34).adjustLineHeight(0.875f),
                });
        font = family.connected[0].setFamily(family);
        layout.setBaseColor(Color.DARK_GRAY);
        layout.setMaxLines(20);
        layout.setEllipsis("...");
        font.markup("@ Fonts can be rendered normally, but using [@Cozette][[tags][@], you can..."
                + "\n[#E74200]...use [@OpenSans]CSS-style[@] hex colors like [*]#E74200[*]..."
                + "\n[FOREST]...use [@OpenSans]named[@] colors from the Colors class, like [/]FOREST[/]...[ ]"
                + "\n...[_]and[_] use [@Kingthings]effects[@]!"
                + "\nNormal, [*]bold[*], [/]oblique[/] (like italic), [*][/]bold oblique[ ],"
                + "\n[_]underline (even for multiple words)[_], [~]strikethrough (same)[ ],"
                + "\nscaling: [%50]very [%75]small [%100]to [%150]quite [%200]large[ ], notes: [.]sub-[.], [=]mid-[=], and [^]super-[^]script,"
                + "\ncapitalization changes: [;]Each cap, [,]All lower, [!]Caps lock[ ],"
                + "\nUnicode support: Pchnąć w tę łódź [BROWN]jeża[ ] lub ośm skrzyń [PURPLE]fig[ ]."
                + "\nWelcome to the [_][*][TEAL]Textra Zone[ ]!"
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
        ScreenUtils.clear(0.8f, 0.8f, 0.8f, 1);
        
        layout.getLine(0).glyphs.set(0, font.markupGlyph('@', "[" + colorNames.get((int)(TimeUtils.timeSinceMillis(startTime) >>> 8) % colorNames.size) + "]"));
        float x = 400, y = layout.getHeight();
        batch.begin();
        font.family.connected[3].enableShader(batch);
        font.drawGlyphs(batch, layout, x, y, Align.center);
        batch.end();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }

    @Override
    public void resize(int width, int height) {
        font.family.connected[3].resizeDistanceField(width, height);
    }
}
