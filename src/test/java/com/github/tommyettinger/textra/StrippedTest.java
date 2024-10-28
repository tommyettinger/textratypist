package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class StrippedTest extends ApplicationAdapter {
    Stage stage;
    Font font;
    @Override
    public void create() {
        stage = new Stage();
        FWSkin skin = new FWSkin(Gdx.files.internal("stripped/stripped.json"), new TextureAtlas("stripped/stripped.atlas"));
//        FWSkin skin = new FWSkin(Gdx.files.internal("changa.json"), new TextureAtlas("changa.atlas"));
        BitmapFont bmfont = skin.getFont("one-50");
//        BitmapFont bmfont = new BitmapFont(Gdx.files.internal("one-50.fnt"));
        bmfont.getData().markupEnabled = true;
        font = skin.get("one-50", Font.class);
//        font = new Font(bmfont);
//        font = new Font("one-50.fnt");
        KnownFonts.addEmoji(font);
        TypingLabel label = new TypingLabel("{RAINBOW}Ads! [~]Ads![~] [_]Ads![_] [~][_]Ads![_][~] [+🐷]\nAds Challenge! [+🏆]", new Styles.LabelStyle(font, null));
        label.setWrap(false);
        label.setAlignment(Align.top);

        Table table = new Table();
        table.setFillParent(true);
        table.add(label).row();

//        bmfont.getData().markupEnabled = false;
//        Label s2dLabel = new Label("I can do it!", new Label.LabelStyle(bmfont, Color.WHITE));
        Label s2dLabel = new Label("[GOLD]Ads! Ads! Ads! Ads! PIG\nAds Challenge! WIN", new Label.LabelStyle(bmfont, Color.WHITE));
//        Label s2dLabel = new Label("I [SKY]can [ROYAL]do[] it!", new Label.LabelStyle(bmfont, Color.WHITE));
        s2dLabel.setAlignment(Align.bottom);
        table.add(s2dLabel).bottom();

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
        config.setTitle("Whitespace stripping test");
        config.setWindowedMode(800, 497);
        config.disableAudio(true);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate+1);
        config.useVsync(true);
        new Lwjgl3Application(new StrippedTest(), config);
    }
}
