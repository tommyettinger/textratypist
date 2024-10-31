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
        font = KnownFonts.addEmoji(KnownFonts.getRobotoCondensed(), -8, 8, 8);
        TypingLabel label = new TypingLabel(
            "[+🤠][+🥾]A Cowboy strolled on in\n" +
            "[+💖][+🏩]to his favorite den of sin.\n" +
            "[+🥔][+🤿]He'd got his chips, and went all in,\n" +
            "[+♠️][+🏆]he drew an ace, and got his win.", font);
        label.setWrap(false);
        label.setWidth(400);
        label.setSize(400, 200);
        label.setAlignment(Align.top);

        Table table = new Table();
        table.setFillParent(true);
        table.add(label).center();
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
