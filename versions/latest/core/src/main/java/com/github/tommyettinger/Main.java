package com.github.tommyettinger;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.tommyettinger.textra.*;

public class Main extends ApplicationAdapter {
    private Stage uiStage;
    Font font;

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_ERROR);
        uiStage = new Stage(new ScreenViewport());
        Table table = new Table();
        table.setFillParent(true);
        Styles.TextButtonStyle style = new Styles.TextButtonStyle();
        font = KnownFonts.getRobotoCondensed(Font.DistanceFieldType.MSDF);
        style.font = font;
        TextraButton textraButton = new TextraButton("EXIT THE APP!", style);
        textraButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        String text = "{IF=PRONOUNS;1=Funcionária do mês;2=Funcionário do mês;Funcionárie do mês}";
        TypingLabel label1 = new TypingLabel(text + " should be " + "Funcionária do mês", font); label1.setVariable("PRONOUNS", "1");
        TypingLabel label2 = new TypingLabel(text + " should be " + "Funcionário do mês", font); label2.setVariable("PRONOUNS", "2");
        TypingLabel label3 = new TypingLabel(text + " should be " + "Funcionárie do mês", font); label3.setVariable("PRONOUNS", "3");
        table.add(label1).row();
        table.add(label2).row();
        table.add(label3).row();
        table.add(textraButton);

        uiStage.addActor(table);

        Gdx.input.setInputProcessor(uiStage);
    }

    @Override
    public void resume() {
    }

    @Override
    public void render() {
        ScreenUtils.clear(0x7c/255f, 0x80/255f, 0x82/255f, 1f);
        uiStage.act();
        uiStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        uiStage.getViewport().update(width, height);
        font.resizeDistanceField(width, height, uiStage.getViewport());
    }

    @Override
    public void dispose() {
//        font.dispose();
        uiStage.dispose();
    }
}
