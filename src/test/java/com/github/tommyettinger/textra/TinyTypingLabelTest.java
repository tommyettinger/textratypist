package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

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

    @Override
    public void create() {
        adjustTypingConfigs();

        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("uiskin.json"));
//        skin.getAtlas().getTextures().iterator().next().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        float scale = 0.5f;
        skin.getFont("default-font").getData().setScale(scale);
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
                label.skipToTheEnd();
                Cell<TypingLabel> labelCell = table.getCell(label);
                System.out.println("Label height: " + labelCell.getActorHeight()
                        + ", cell max height: " + labelCell.getMaxHeight()
                        + ", cell pref height: " + labelCell.getPrefHeight());

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
                label.skipToTheEnd();
                System.out.println("Label height: " + labelCell.getActorHeight()
                        + ", cell max height: " + labelCell.getMaxHeight()
                        + ", cell pref height: " + labelCell.getPrefHeight());
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
        TypingConfig.GLOBAL_VARS.put("ICE_WIND", "{FASTER}{GRADIENT=88ccff;eef8ff;-0.5;5}{SLOWER}{WIND=2;4;0.25;0.1}");
    }

    public TypingLabel createTypingLabel() {
        Font.FontFamily family = new Font.FontFamily(
                new String[]{
                        "Serif", "Sans", "Mono", "Medieval", "Future", "Humanist"
                },
                new Font[]{
                        KnownFonts.getGentium().scaleTo(32, 35)/*.scale(1.15f, 1.15f)*/,
                        KnownFonts.getOpenSans().scaleTo(23, 35)/*.scale(1.15f, 1.15f)*/.adjustLineHeight(0.9f),
                        KnownFonts.getInconsolata().scaleTo(15, 35)/*.scale(1.15f, 1.15f)*/.adjustLineHeight(1.1f),
                        KnownFonts.getKingthingsFoundation().scaleTo(35, 35)/*.scale(1.15f, 1.15f)*/,
                        KnownFonts.getOxanium().scaleTo(32, 35)/*.scale(1.15f, 1.15f)*/.adjustLineHeight(1.05f),
                        KnownFonts.getYanoneKaffeesatz().scaleTo(32, 35)/*.scale(1.15f, 1.15f)*/.adjustLineHeight(0.85f)
                });
        Font font = family.connected[0].setFamily(family);

//        Font font = new Font(KnownFonts.getOpenSans().scale(0.5f, 0.5f).setTextureFilter());
        // Create label
//        final TypingLabel label = new TypingLabel("WELCOME {STYLE=OBLIQUE}TO THE {STYLE=bold}{COLOR=11bb00}JUNGLE{RESET}, WE'VE GOT A MAN, A PLAN, A CANAL: PANAMA!",

        // There's a strange bug that can happen with the WIND effect, but it only seems to happen when the affected
        // text wraps across lines... Not totally sure what's happening.
        // OK, it is definitely not something that requires different fonts to trigger. Specific widths cause line
        // wrapping to somehow break the ENDWIND token (or RESET).
        final TypingLabel label = new TypingLabel(
//                "Behold, the [/Terror{RESET}-[*]Bunny[*]!",
//                "{SHAKE=1,1,2}[@Medieval]Behold{RESET}, the [/]Terror{RESET}-{GRADIENT=WHITE;RED}Bunny!",
//                "{BLINK=ff0000ff;00ff27ff;1.0;0.5}redtogreen", // used to check unclosed effects with incomplete parameters
//                "A {Bunny!", // used to check unclosed curly braces

//                "[#8fc60cff][@OpenSans][%75]{spin}Lorem ipsum dolor sit amet, consectetur adipiscing elit.[]",

                "{JOLT=1;1.2;inf;0.3;dull lavender;light butter}There's a [/][@Medieval]STORM{RESET} on [@Future]the way[@], " +
                "she's{WIND=3;2;0.2;0.2} blowin' on down{RESET}, " +
                "whippin' her way through the [*]{FONT=Sans}whole dang[@][*] town! " +
                "Sure as [/]I reckon[], if we [@Mono]meet our {HANG}fate{RESET}, " +
                "this [light grey black][%150]storm[] will be there on clouds{SPIN=2;1;false}[%75] one{CLEARSIZE}{ENDSPIN} through {SPIN=1;8;false}[%200]eight[%]{ENDSPIN}!\n" +
                "{SCALE=60.0}Uh-[_]huh[_].{ENDSCALE}{SCALE=120.0} All [~]right[~].{ENDSCALE}",

//                "{JOLT=1;1.2;inf;0.3;9944aa;fff0cc}There's a [/]STORM[/]{ENDJOLT} on the way, " +
//                "she's {WIND=3;2;0.2;0.2}blowin' on down{ENDWIND}, " +
//                "whippin' her way through the [*]whole dang[*] town! " +
//                "Sure as [/]I reckon[], if we meet our {HANG}fate{RESET}, " +
//                "this [%150]storm[%] will be there on clouds [%75]one[%] through [%200]eight[%]!",
//
//                "[@Gentium]{JOLT=1;1.2;inf;0.3;9944aa;fff0cc}There's a [/]STORM{RESET}[@Gentium] on the way, " +
//                "she's {WIND=3;2;0.2;0.2} blowin' on down{ENDWIND}, " +
//                "whippin' her way through the [*]whole dang[*] town! " +
//                "Sure as [/]I reckon[][@Gentium], if we meet our {HANG}fate{RESET}, " +
//                "this [%150]storm[%] will be there on clouds [%75]one[%] through [%200]eight[%]!",

//        final TypingLabel label = new TypingLabel("[/][*][GREEN]JUNGLE[*][WHITE] TO THE[/] WELCOME!",
//        final TypingLabel label = new TypingLabel("WELCOME [/]TO THE [*][GREEN]JUNGLE[]!",
                font);
//        final TypingLabel label = new TypingLabel("WELCOME [/]TO THE [*][GREEN]JUNGLE[]!", skin);
//        final TypingLabel label = new TypingLabel("{WAIT=1}{SLOWER}Welcome, {VAR=title}!", skin);
        label.setDefaultToken("{EASE}{FADE=0;1;0.33}");
        label.align = Align.topLeft;

        // Make the label wrap to new lines, respecting the table's layout.
        label.setWrap(true);
        label.layout.maxLines = 15;
//        label.layout.setTargetWidth(Gdx.graphics.getBackBufferWidth() - 100);

        // Set variable replacements for the {VAR} token
        label.setVariable("title", "curious human");

        // Set an event listener for when the {EVENT} token is reached and for the char progression ends.
        label.setTypingListener(new TypingAdapter() {
            @Override
            public void event(String event) {
                System.out.println("Event: " + event);
            }

            @Override
            public void end() {
                System.out.println(label.getIntermediateText());
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

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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
