package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class TypingLabelEventScheduling extends ApplicationAdapter {
    Skin        skin;
    Stage       stage;
    SpriteBatch batch;
    TypingLabel label;

    @Override
    public void create() {
        adjustTypingConfigs();

        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        float scale = 0.5f;
        skin.getFont("default-font").getData().setScale(scale);
        stage = new Stage(new ScreenViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        final Table table = new Table();
        stage.addActor(table);
        stage.setDebugAll(true);
        table.setFillParent(true);

        final long timeBase = System.currentTimeMillis();
        label = new TypingLabel("{RAINBOW}Heww{ENDRAINBOW}o, {VAR=name}...{WAIT=1}{EVENT=on}{WAIT=2}{EVENT=off}bai!{WAIT=3}", KnownFonts.getGentiumSDF());
        label.setTypingListener(new TypingAdapter(){
            @Override
            public void onChar(long ch) {
                System.out.println((System.currentTimeMillis() - timeBase) + " " + (char)ch);
            }

            @Override
            public void event(String event) {
                System.out.println((System.currentTimeMillis() - timeBase) + " " + event);
            }

            @Override
            public void end() {
                label.restart();
            }
        });
        label.setVariable("name", "waeweus, fwend of uwu intewwfacies");
        label.layout.setTargetWidth(180);
        table.pad(50f);
        table.add(label);//.colspan(5).growX();

        table.pack();
    }

    public void adjustTypingConfigs() {
        // Only allow two chars per frame
        TypingConfig.CHAR_LIMIT_PER_FRAME = 1;

        // Change color used by CLEARCOLOR token
        TypingConfig.DEFAULT_CLEAR_COLOR = Color.WHITE;

        // Create some global variables to handle style
        TypingConfig.GLOBAL_VARS.put("ICE_WIND", "{FASTER}{GRADIENT=88ccff;eef8ff;-0.5;5}{SLOWER}{WIND=2;4;0.25;0.1}");
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
        config.setTitle("TypingLabel Event Scheduling Test");
        config.setWindowedMode(720, 405);
        config.setResizable(false);
        config.setForegroundFPS(60);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new TypingLabelEventScheduling(), config);
    }
}
