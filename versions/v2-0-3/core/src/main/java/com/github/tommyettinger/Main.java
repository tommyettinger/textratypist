package com.github.tommyettinger;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
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
        font = new Font("unicode/LanaPixel.fnt", "unicode/LanaPixel.png");

        Array<TextraLabel> labels = new Array<>(new TextraLabel[]{
                new TextraLabel("[%99][RED][%?whiten]＊[WHITE]", new Styles.LabelStyle(font, null))
        });
        TextraLabel label = labels.get(0);
        label.setWrap(true);
        label.setAlignment(Align.top);

        Table table = new Table();
        table.setFillParent(true);
        table.add(label).row();

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
