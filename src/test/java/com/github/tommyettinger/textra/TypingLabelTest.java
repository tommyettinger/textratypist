package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public class TypingLabelTest extends ApplicationAdapter {
    Skin        skin;
    Stage       stage;
    SpriteBatch batch;
    TypingLabel label;
    TypingLabel labelEvent;
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
        skin.getAtlas().getTextures().iterator().next().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        float scale = 1;
        skin.getFont("default-font").getData().setScale(scale);
        stage = new Stage(new ScreenViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        final Table table = new Table();
        stage.addActor(table);
        table.setFillParent(true);

        label = createTypingLabel();

        labelEvent = new TypingLabel("",
                new Font(new BitmapFont(Gdx.files.internal("OpenSans.fnt")), Font.DistanceFieldType.STANDARD,
                        0f, 0f, 0f, 0f).scale(0.5f, 0.5f).setTextureFilter());
        labelEvent.setAlignment(Align.left);
        labelEvent.pause();
        labelEvent.setVisible(false);

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
        table.add(labelEvent).colspan(5).align(Align.center);
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
        TypingConfig.GLOBAL_VARS.put("FIRE_WIND", "{FASTER}{GRADIENT=ORANGE;DB6600;-0.5;5}{SLOWER}{WIND=2;4;0.5;0.5}");
    }

    public TypingLabel createTypingLabel() {
        // Create text with tokens
        final StringBuilder text = new StringBuilder();
//        text.append("{WAIT=1}{SLOWER}{GRADIENT=FF70F1;FFC300;-0.5;5}Welcome,{WAIT} {VAR=title}!");
//        text.append("{WAIT=1}{SLOWER}{GRADIENT=FF70F1;FFC300;-0.5;5}{EASE=-8;2;1}Welcome,{WAIT} {VAR=title}!{ENDEASE}");
//        text.append("{FAST}\n\n");
//        text.append("{RESET}{HANG=0.7}This is a simple test{ENDHANG} to show you");
//        text.append("{GRADIENT=27C1F5;2776E7;-0.5;5} {JUMP}how to make dialogues {SLOW}fun again! {ENDJUMP}{WAIT}{ENDGRADIENT}\n");
//        text.append("{NORMAL}{CLEARCOLOR}{SICK} With this library{ENDSICK} you can control the flow of the text with");
//        text.append(" {BLINK=FF6BF3;FF0582;3}tokens{ENDBLINK},{WAIT=0.7}");
//        text.append("{SPEED=2.50}{COLOR=#84DD60} making the text go {SHAKE=1;1;3}really fast{ENDSHAKE}{WAIT=0.5} ");
//        text.append("{SPEED=0.25}{COLOR=#A6E22D}{WAVE=0.66}or extremely slow.{ENDWAVE}");
        text.append(" {RESET}You can also wait for a 1234567890{EASE=-15;2;1}second{ENDEASE}\n\n{WAIT=1} {EASE=15;8;1}{COLOR=#E6DB74}or two{CLEARCOLOR}{ENDEASE}{WAIT=2},");
//        text.append(" {RESET}You can also wait for a {EASE=-15;2;1}second{ENDEASE}\n\n{WAIT=1} {EASE=15;8;1}{COLOR=#E6DB74}or two{CLEARCOLOR}{ENDEASE}{WAIT=2},");
        text.append("{RAINBOW=1;1;0.7;0.5} just to catch an event in code{EVENT=example}!{WAIT} {ENDRAINBOW}");
//        text.append("{NORMAL}\n\n");
//        text.append("{VAR=FIRE_WIND}Imagine the possibilities! =D {RESET}\n");

        // Create label
        final TypingLabel label = new TypingLabel(text.toString(), skin);
        label.setDefaultToken("{EASE}{FADE=0;1;0.33}");
//        label.setDefaultToken("{EASE=15;8;1}");

        // Make the label wrap to new lines, respecting the table's layout.
        label.layout.maxLines = 15;
        label.layout.setTargetWidth(Gdx.graphics.getBackBufferWidth());

        // Set variable replacements for the {VAR} token
        label.setVariable("title", "curious human");

        // Set an event listener for when the {EVENT} token is reached and for the char progression ends.
        label.setTypingListener(new TypingAdapter() {
            @Override
            public void event(String event) {
                System.out.println("Event: " + event);

                labelEvent.restart("{FADE}{SLIDE=2;1;1}{FASTER}{COLOR=GRAY}Event:{WAIT=0.1}{COLOR=LIME} " + event);
                labelEvent.clearActions();
                labelEvent.addAction(
                    sequence(
                        visible(true),
                        alpha(0),
                        alpha(1, 0.25f, Interpolation.pow2In),
                        delay(0.5f),
                        alpha(0, 2f, Interpolation.pow2)
                    )
                );
            }

            @Override
            public void end() {
                System.out.println("End");
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
        new Lwjgl3Application(new TypingLabelTest(), config);
    }
}
