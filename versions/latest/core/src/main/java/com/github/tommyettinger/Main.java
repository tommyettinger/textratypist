package com.github.tommyettinger;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.github.tommyettinger.textra.*;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
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
        font = new Font(bmfont);
//        font = new Font("one-50.fnt");
        Array<TypingLabel> labels = new Array<>(new TypingLabel[]{
                new TypingLabel("{RAINBOW}Ads Challenge...{ENDRAINBOW}", new Styles.LabelStyle(bmfont, null))
        });
        TypingLabel label = labels.get(0);
        label.setWrap(true);
        label.setAlignment(Align.top);

        Table table = new Table();
        table.setFillParent(true);
        table.add(label).size(200, 100).row();

//        bmfont.getData().markupEnabled = false;
//        Label s2dLabel = new Label("I can do it!", new Label.LabelStyle(bmfont, Color.WHITE));
        Label s2dLabel = new Label("[GOLD]Ads Challenge!", new Label.LabelStyle(bmfont, Color.WHITE));
//        Label s2dLabel = new Label("I [SKY]can [ROYAL]do[] it!", new Label.LabelStyle(bmfont, Color.WHITE));
        s2dLabel.setAlignment(Align.bottom);
        table.add(s2dLabel).bottom();

        stage.addActor(table);
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
}
