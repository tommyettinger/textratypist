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

//        Top of the outline is cut off. Font was generated with... not currently known, but
//        the outlines have pointy corners rather than rounded ones.
        // https://i.imgur.com/5julXro.png
//        BitmapFont font = new BitmapFont(Gdx.files.internal("openSans30.fnt"));
        // Newly added workaround for Hiero's lack of spec-conformance.
//        Font.setPaddingForBitmapFont(-1, -1, -1, -1);

//        No outlines are cut off. font was generated with AngelCode BMFont with outline=1 and padding=2,2,2,2 .
//        Outlines have rounded corners.
        // https://i.imgur.com/KBmOFPT.png
//        BitmapFont font = new BitmapFont(Gdx.files.internal("Open-Sans-Extra-Bold-standard.fnt"));

//        Larger font, outline works; should roughly match the size of openSans30.fnt. Made with AngelCode
//        BMFont again. The padding is 0,0,0,0 this time, outline is still 1. spacing is still 1.
        // https://i.imgur.com/gJE61BC.png
//        BitmapFont font = new BitmapFont(Gdx.files.internal("Open-Sans-ExtraBold-standard.fnt"));

//        FreeType works too with no padding changes.
        // https://i.imgur.com/4mbGVLo.png

        // You create a FreeTypeFontGenerator per TTF or OTF font file.
        FreeTypeFontGenerator ft = new FreeTypeFontGenerator(Gdx.files.internal("OpenSans-ExtraBold.ttf"));
        // You can create more than one FreeTypeFontParameter, each one for a different size or look of the same TTF.
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        // Using white is recommended because it can be tinted to any other color (tinting can't brighten a color).
        parameter.color = Color.WHITE;
        // Border configuration
        parameter.borderColor = Color.BLACK;
        parameter.borderWidth = 1f;
        parameter.borderStraight = true;
        parameter.renderCount = 4;
        // This is a file with every char we need in the font. This could be all basic ASCII chars, or many more.
        parameter.characters = Gdx.files.internal("OpenSans-ExtraBold.txt").readString("UTF-8");
        // sharpens edges a little.
        parameter.gamma = 2.2f;
        parameter.minFilter = Texture.TextureFilter.Nearest;
        parameter.magFilter = Texture.TextureFilter.Nearest;
        parameter.hinting = FreeTypeFontGenerator.Hinting.Medium;
        // improves space between letters sometimes.
        parameter.kerning = true;
        // This is what likely will need to change based on screen size.
        parameter.size = 30;
        // creates a normal BitmapFont based on the TTF we loaded when we created ft.
        BitmapFont font = ft.generateFont(parameter);
        // Only dispose the FreeTypeFontGenerator when you don't need to generate more BitmapFonts.
        // You might want to keep the FreeTypeFontGenerator around for your whole program lifecycle if
        // you need to generate new BitmapFonts when the window is resized.
        ft.dispose();


//        Top is still cut off.
//        Font was generated with Hiero, padding 2,2,2,2, spacing -4,-4
        // https://i.imgur.com/eCULmhC.png
//        BitmapFont font = new BitmapFont(Gdx.files.internal("openSans30pad2space-4.fnt"));

//        Every glyph's outline works.
//        This is the same font as directly above, but with its padding manually changed to 1,1,1,1
        // https://i.imgur.com/mB9CMHL.png
//        BitmapFont font = new BitmapFont(Gdx.files.internal("OpenSans30PadChangedSpace-4.fnt"));

//        Everything works here, too. Font was generated with Hiero, and was edited from openSans30.fnt
//        only by reducing padding to 0,0,0,0. If a font had padding 0,0,0,0 and an outline, it wouldn't be
//        generated correctly by Hiero, but would be correct if BMFont was generating it.
        // https://i.imgur.com/tEPTHZ1.png
//        BitmapFont font = new BitmapFont(Gdx.files.internal("openSans30Edit.fnt"));


//        font.setUseIntegerPositions(false);

        String text = "The quick brown fox jumps over the lazy dog.";
        TextraLabel textraLabel = new TextraLabel(text, new Font(font));
//        TextraLabel textraLabel = new TextraLabel(text, new Font(Gdx.files.internal("openSans30.fnt")));
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