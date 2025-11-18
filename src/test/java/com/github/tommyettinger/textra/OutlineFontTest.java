package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
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
        // https://i.imgur.com/5julXro.png
//        BitmapFont font = new BitmapFont(Gdx.files.internal("openSans30.fnt"));

//        no outlines are cut off. font was generated with AngelCode BMFont with outline=1 and padding=2,2,2,2 .
//        outlines have rounded corners.
        // https://i.imgur.com/KBmOFPT.png
//        BitmapFont font = new BitmapFont(Gdx.files.internal("Open-Sans-Extra-Bold-standard.fnt"));

//        larger font, outline works; should roughly match the size of openSans30.fnt. Made with AngelCode
//        BMFont again. padding is 0,0,0,0 this time, outline is still 1. spacing is still 1.
        // https://i.imgur.com/gJE61BC.png
//        BitmapFont font = new BitmapFont(Gdx.files.internal("Open-Sans-ExtraBold-standard.fnt"));

//        FreeType works too with no padding changes.
        // https://i.imgur.com/4mbGVLo.png
        FreeTypeFontGenerator ft = new FreeTypeFontGenerator(Gdx.files.internal("OpenSans-ExtraBold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.borderColor = Color.BLACK;
        parameter.color = Color.WHITE;
        parameter.borderWidth = 1f;
        parameter.borderStraight = true;
        parameter.renderCount = 4;
        parameter.characters = Gdx.files.internal("OpenSans-ExtraBold.txt").readString("UTF-8");
        parameter.gamma = 2.2f;
        parameter.hinting = FreeTypeFontGenerator.Hinting.Medium;
        parameter.kerning = true;
        parameter.size = 30;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        BitmapFont font = ft.generateFont(parameter);

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