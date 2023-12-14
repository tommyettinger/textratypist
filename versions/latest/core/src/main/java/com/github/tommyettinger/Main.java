package com.github.tommyettinger;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.KnownFonts;
import com.github.tommyettinger.textra.TypingLabel;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    Stage stage;
    Font font;
    @Override
    public void create() {
        stage = new Stage();
        font = KnownFonts.getRobotoCondensed();
        Array<TypingLabel> labels = new Array<>(new TypingLabel[]{
                new TypingLabel("{RAINBOW}Texttexttextte xtext text t e x t text text text text{ENDRAINBOW}", font)
        });
        TypingLabel label = labels.get(0);
        label.setWrap(true);
        label.setAlignment(Align.top);

        Table table = new Table();
        table.setFillParent(true);
        table.add(label).size(200, 100).row();

        BitmapFont bmfont = new BitmapFont(Gdx.files.internal("RobotoCondensed-standard.fnt"));
        bmfont.getData().markupEnabled = false;
        Label s2dLabel = new Label("I can do it!", new Label.LabelStyle(bmfont, Color.WHITE));
//        bmfont.getData().markupEnabled = true;
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