package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Testing the effects of scaling on atlas images, and larger-than-normal atlas images.
 */
public class AtlasTest extends ApplicationAdapter {
    Stage stage;
    Font font;
    @Override
    public void create() {
        stage = new Stage();
        font = new Font(KnownFonts.GENTIUM_UN_ITALIC + "-msdf.dat",
                new TextureRegion(new Texture(KnownFonts.GENTIUM_UN_ITALIC + "-msdf.png")),
                -30f, 0f, 0f, 0f,
                true, true).scaleHeightTo(50f).addAtlas(new TextureAtlas("controller.atlas"), -90, 0, 0);
        // I load the left-trigger image so we know what width we will need to offset by. Most buttons are probably similar.
        Font.GlyphRegion lt = font.mapping.get(font.nameLookup.get("controller_LT", ' ')),
            space = font.mapping.get(' '),
            back = new Font.GlyphRegion(space), // I chose space because it's invisible.
            forward = new Font.GlyphRegion(space),
            nothing = new Font.GlyphRegion(space);
        // Because atlases are meant for emoji, which are square, the GlyphRegion height is also used as the width, but only for atlases.
        back.xAdvance = -lt.getRegionHeight();
        forward.xAdvance = lt.getRegionHeight();
        nothing.xAdvance = 0;
        // The \b is traditionally backspace, I think, or bell. Either way it isn't visible normally, or even used.
        font.mapping.put('\b', back);
        // Now you can place \b before any text, and it will move the cursor to the left by the same width as controller_LT.

        // This also maps tab to move the cursor forward by the exact width as controller_LT.
        font.mapping.put('\t', forward);
        font.mapping.put('\u2000', nothing);

        TypingLabel label = new TypingLabel(
//            "[+controller_LT]\n" +
//            "[+controller_RT]"
//            "(This has no \\b before the image!)\n" +
//            "Inline with text, no \\b!"
            "\u2000[+controller_A]\n" +
            "\u2000[+controller_B] (This has NO \\b before the image!)\n" +
            "Inline [+controller_LT][+controller_RT] with text, no \\b!"
            , font);
        label.setWrap(false);
        label.setWidth(400);
        label.setSize(400, 200);
        label.setAlignment(Align.top);

        Table table = new Table();
        table.setFillParent(true);
        table.add(label).center();
        stage.addActor(table);
        stage.setDebugAll(true);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);
        stage.act();
        stage.draw();
    }

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Atlas Image sizing test");
        config.setWindowedMode(1000, 600);
        config.disableAudio(true);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        config.useVsync(true);
        new Lwjgl3Application(new AtlasTest(), config);
    }

}
