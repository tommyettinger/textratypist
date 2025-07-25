package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.tommyettinger.textra.utils.NoiseUtils;

public class TinyTypingLabelTest extends ApplicationAdapter {
    Skin        skin;
    Stage       stage;
    SpriteBatch batch;
    TypingLabel label;
    TextButton  buttonPause;
    TextButton  buttonResume;
    TextButton  buttonRestart;
    TextButton  buttonRebuild;
    TextButton  buttonSkip;
    int adj = 0;
    long startTime;

    @Override
    public void create() {
        startTime = TimeUtils.millis();
        adjustTypingConfigs();

        batch = new SpriteBatch();
        skin = new FWSkin(Gdx.files.internal("uiskin.json"));
//        skin.getAtlas().getTextures().iterator().next().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        //skin.getFont("default-font");//.getData().setScale(0.5f);
        stage = new Stage(new ScreenViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        final Table table = new Table();
        stage.addActor(table);
        table.setFillParent(true);

        label = createTypingLabel();

        buttonPause = new TextButton("Pause", skin);
        buttonPause.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                label.pause();
            }
        });

        buttonResume = new TextButton("Resume", skin);
        buttonResume.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                label.resume();
            }
        });

        buttonRestart = new TextButton("Restart", skin);
        buttonRestart.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                label.restart();
//                label.skipToTheEnd();
                Cell<TypingLabel> labelCell = table.getCell(label);
                table.pack();
//                System.out.println("Label height: " + labelCell.getActorHeight()
//                        + ", cell max height: " + labelCell.getMaxHeight()
//                        + ", cell pref height: " + labelCell.getPrefHeight());

            }
        });

        buttonRebuild = new TextButton("Rebuild", skin);
        buttonRebuild.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                adjustTypingConfigs();
                Cell<TypingLabel> labelCell = table.getCell(label);
                label = createTypingLabel();
                labelCell.setActor(label);
//                label.skipToTheEnd();
                table.pack();
//                System.out.println("Label height: " + labelCell.getActorHeight()
//                        + ", cell max height: " + labelCell.getMaxHeight()
//                        + ", cell pref height: " + labelCell.getPrefHeight());
            }
        });

        buttonSkip = new TextButton("Skip", skin);
        buttonSkip.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                label.skipToTheEnd();
            }
        });

        table.pad(50f);
        table.add(label).colspan(5).growX();
        table.row();
        table.row().uniform().expand().growX().space(40).center();
        table.add(buttonPause, buttonResume, buttonRestart, buttonSkip, buttonRebuild);

        table.pack();
    }

    public void adjustTypingConfigs() {
        // Only allow two chars per frame
        TypingConfig.CHAR_LIMIT_PER_FRAME = 2;

        // Change color used by CLEARCOLOR token
        TypingConfig.DEFAULT_CLEAR_COLOR = Color.WHITE;

        // Create some global variables to handle style
        TypingConfig.GLOBAL_VARS.put("ICE_WIND", "{GRADIENT=88ccff;eef8ff;-0.5;5}{WIND=2;4;0.25;0.1}{JOLT=1;0.6;inf;0.1;;}");

        TypingConfig.GLOBAL_VARS.put("SHOWIFA", "{IF=A;show=;=[ (FormatA)][#00ff0000]}");
        TypingConfig.GLOBAL_VARS.put("ENDSHOWIFA", "{IF=A;show=;=[ FormatA]}");
        TypingConfig.GLOBAL_VARS.put("SHOWIFNOTA", "{IF=A;show=[ (FormatNotA)][#ff000000];=}");
        TypingConfig.GLOBAL_VARS.put("ENDSHOWIFNOTA", "{IF=A;show=[ FormatNotA];=}");

    }

    public TypingLabel createTypingLabel() {
//        Font.FontFamily family = new Font.FontFamily(
//                new String[]{
//                        "Serif", "Sans", "Mono", "Medieval", "Future", "Humanist"
//                },
//                new Font[]{
//                        KnownFonts.getGentium().scaleTo(32, 35)/*.scale(1.15f, 1.15f)*/,
//                        KnownFonts.getOpenSans().scaleTo(23, 35)/*.scale(1.15f, 1.15f)*/.adjustLineHeight(0.9f),
//                        KnownFonts.getInconsolata().scaleTo(15, 35)/*.scale(1.15f, 1.15f)*/.adjustLineHeight(1.1f),
//                        KnownFonts.getKingthingsFoundation().scaleTo(35, 35)/*.scale(1.15f, 1.15f)*/,
//                        KnownFonts.getOxanium().scaleTo(32, 35)/*.scale(1.15f, 1.15f)*/.adjustLineHeight(1.05f),
//                        KnownFonts.getYanoneKaffeesatz().scaleTo(32, 35)/*.scale(1.15f, 1.15f)*/.adjustLineHeight(0.85f)
//                });
//        Font font = family.connected[0].setFamily(family);
        Font font = KnownFonts.getStandardFamily();
        for(Font f : font.family.connected) {
            if (f != null) {
                KnownFonts.addEmoji(f);
            }
        }

//        Font font = new Font(KnownFonts.getOpenSans().scale(0.5f, 0.5f).setTextureFilter());
        // Create label
//        final TypingLabel label = new TypingLabel("WELCOME {STYLE=OBLIQUE}TO THE {STYLE=bold}{COLOR=11bb00}JUNGLE{RESET}, WE'VE GOT A MAN, A PLAN, A CANAL: PANAMA!",

        // There's a strange bug that can happen with the WIND effect, but it only seems to happen when the affected
        // text wraps across lines... Not totally sure what's happening.
        // OK, it is definitely not something that requires different fonts to trigger. Specific widths cause line
        // wrapping to somehow break the ENDWIND token (or RESET).
        final TypingLabel label = new TypingLabel(
//                // correct
//                "{WAVE=1;1;10}Everybody do the thing!{RESET} (fast)\n" +
//                "{WAVE=1;1;0.1}Everybody do the thing!{RESET} (slow)",
//                // correct (but fast isn't visible)
//                "{SQUASH=10}Everybody do the thing!{RESET} (fast)\n" +
//                "{SQUASH=0.1}Everybody do the thing!{RESET} (slow)",
//                // correct
//                "{SPUTTER=0.25;0.25;10}Everybody do the thing!{RESET} (fast)\n" +
//                "{SPUTTER=0.25;0.25;0.1}Everybody do the thing!{RESET} (slow)",

//                "she's[red]{JOLT=1;0.6;inf;0.7;;} blowin' on down{RESET}, ",
//                "Behold, the [/Terror{RESET}-[*]Bunny[*]!",
//                "{SHAKE=1,1,2}[@Medieval]Behold{RESET}, the [/]Terror{RESET}-{GRADIENT=WHITE;RED}Bunny!",
//                "{BLINK=ff0000ff;00ff27ff;1.0;0.5}redtogreen", // used to check unclosed effects with incomplete parameters
//                "A {Bunny!", // used to check unclosed curly braces

//                "[#8fc60cff][@OpenSans][%75]{spin}Lorem ipsum dolor sit amet, consectetur adipiscing elit.[]",
//"Serif", "Sans", "Mono", "Condensed", "Humanist",
//                        "Retro", "Slab", "Handwriting", "Canada", "Cozette", "Iosevka",
//                        "Medieval", "Future", "Console", "Code"

//                "{VAR=SHOWIFA}A is enabled! Press A on your controller to confirm!{VAR=ENDSHOWIFA}{VAR=SHOWIFNOTA}A is disabled! Click to confirm!{VAR=ENDSHOWIFNOTA}\n" +

//                "{IF=controller;yes=A is enabled! Press A on your controller to confirm!;=A is disabled! Click to confirm!}\n",
/*                        +

                "{SLAM}There's a [/][@Medieval]STORM{RESET} on {MEET=2;1;n;y}[@Future][GREEN]the way[][][-ENDMEET], " +
                "{INSTANT}she's{VAR=SHIVERINGBLIZZARD} blowin' on down{ENDINSTANT}{RESET}, " +
                "[@Handwriting]whippin'[] her [@Slab]way[] through the [*]{FONT=Sans}whole dang[][] town! " +
                "{ZIPPER}[@Iosevka]Sure[@] as [/]I reckon{ENDZIPPER}[ ], if we [@Mono]meet our [@Cozette]{HANG}fate[@]{RESET}, " +
                "this [light grey black][%125]storm[ ] will be [@Canada]there[@] on clouds{SPIN=2;1;false}[%75] one{CLEARSIZE}{ENDSPIN} through {SPIN=1;8;false}[%150]eight[%]{ENDSPIN}! " +
                "Should a [@Retro]young[@] {IF=gender;m=lad;f=lass;t='un;e=[+🧒]} go [@Code]out[@], in the [@Humanist]wind[@] and [@Geometric]the {SHAKE=;;2}thunder{ENDSHAKE}[@], " +
                "if {IF=gender;m=he makes;f=she makes;t=they make;e=[+🧒] makes} it [@Condensed]back[@], it [@Console]will[@] be a [;][%^]true wonder[%][;]!",
*/
//                "Welcome back {VAR=check case}, how are you doing?\nI hear those controllers are making a comeback...",
                "MODES! Error: [?error]oh no[?], Warn: [?warn]uh oh[?], Note: [?note]huh[?], Context: [?context]interesting[?], Suggest: [?suggest]neat[?]\n" +
//                "OUTLINES! [?blacken]Black[#], [?whiten]White[#], [?bluen]Blue[#], [?redden]Red[#], [?yellowen]Yellow[#]\n" +
                "RADIANT! [@Geometric][RED][?neon]Hot Food! [GREEN]Low Prices! [YELLOW][?halo]Believe it![ ]\n"
                ,
                font);
        label.setDefaultToken("{EASE}{FADE=0;1;0.33}{SLOWER}[%130]");
        label.align = Align.topLeft;
        TypingConfig.GLOBAL_VARS.put("CHECK CASE", "Bob"); //all caps works
//        TypingConfig.GLOBAL_VARS.put("player", "Bob"); //not working
        label.setVariable("player", "Bob"); //working
        label.parseTokens();
//        label.skipToTheEnd();

        // Make the label wrap to new lines, respecting the table's layout.
        label.setWrap(true);
        label.layout.maxLines = 15;
//        label.layout.setTargetWidth(Gdx.graphics.getBackBufferWidth() - 100);

        // Set variable replacements for the {VAR} and {IF} tokens; needs label.parseTokens() to be called later.
        label.setVariable("gender", "f");
        label.setVariable("lad", "'un");
        label.setVariable("he makes", "they make");
        label.setVariable("A", "show");
        label.setVariable("controller", "yes");

        // Set an event listener for when the {EVENT} token is reached and for the char progression ends.
        label.setTypingListener(new TypingAdapter() {
            @Override
            public void event(String event) {
                System.out.println("Event: " + event);
            }

            @Override
            public void end() {
//                label.setVariable("A", "hide");

                label.parseTokens();
//                System.out.println(label);
                if(label.length() < 150)
                    label.appendText("\n{IF=controller;yes=A is enabled! Press A on your controller to confirm!;=A is disabled! Click to confirm!}");
                label.setVariable("controller", "no");
            }
        });

        // Finally parse tokens in the label text.
        label.parseTokens();

        return label;
    }

    public void update(float delta) {
        stage.act(delta);
    }

    @Override
    public void render() {
        update(Gdx.graphics.getDeltaTime());
        label.font.family.get("Geometric").glowStrength = (NoiseUtils.octaveNoise1D(TimeUtils.timeSinceMillis(startTime) * 0.002f, 12345) + 1f) * 0.8f;
        ScreenUtils.clear(0.3f, 0.3f, 0.3f, 1);
        if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT))
        {
            adj = adj + 15 & 15;
            System.out.println("Adjusting " + label.font.family.connected[adj].name);
        }
        else if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT))
        {
            adj = adj + 1 & 15;
            System.out.println("Adjusting " + label.font.family.connected[adj].name);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP))
            System.out.println(label.font.family.connected[adj].setDescent(label.font.family.connected[adj].descent + 1).descent + " " + label.font.family.connected[adj].name);
        else if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN))
            System.out.println(label.font.family.connected[adj].setDescent(label.font.family.connected[adj].descent - 1).descent + " " + label.font.family.connected[adj].name);
        stage.draw();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("TypingLabel Test");
        config.setWindowedMode(720, 405);
        config.setResizable(false);
        config.setForegroundFPS(60);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new TinyTypingLabelTest(), config);
    }
}
