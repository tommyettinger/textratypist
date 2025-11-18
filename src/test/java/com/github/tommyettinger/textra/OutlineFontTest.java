package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Tests the behavior of a Font loaded from a BitmapFont with an outline.
 * <br>
 * This is from <a href="https://github.com/tommyettinger/textratypist/pull/34">PR 34</a> by lucas-viva.
 */
public class OutlineFontTest extends ApplicationAdapter {

    private Stage stage;

    public OutlineFontTest() {
    }

    @Override
    public void create() {
        stage = new Stage(new ScreenViewport());

//        top of the outline is cut off. font was generated with... not currently known, but
//        the outlines have pointy corners rather than rounded ones.
//        BitmapFont font = new BitmapFont(Gdx.files.internal("openSans30.fnt"));

//        no outlines are cut off. font was generated with AngelCode BMFont with outline=1 and padding=2,2,2,2 .
//        outlines have rounded corners.
        BitmapFont font = new BitmapFont(Gdx.files.internal("Open-Sans-Extra-Bold-standard.fnt"));
        font.setUseIntegerPositions(false);

        String text = "The quick brown fox jumps over the lazy dog.";
        TextraLabel textraLabel = new TextraLabel(text, new Font(font));
        textraLabel.setWrap(true);

        Stack stack = new Stack(textraLabel);
        stack.setFillParent(true);
        stage.addActor(stack);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.WHITE);
        stage.act();
        stage.getViewport().apply(true);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("TextraLabel outline font with padding test");
        config.setWindowedMode(600, 500);
        config.disableAudio(true);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new OutlineFontTest(), config);
    }
}