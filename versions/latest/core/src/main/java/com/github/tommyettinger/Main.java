package com.github.tommyettinger;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.KnownFonts;
import com.github.tommyettinger.textra.Styles;
import com.github.tommyettinger.textra.TextraButton;

public class Main extends ApplicationAdapter {
    private Stage uiStage;
    Font font;

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_ERROR);
        uiStage = new Stage(new ScreenViewport());
        Styles.TextButtonStyle style = new Styles.TextButtonStyle();
        font = KnownFonts.getRobotoCondensed(Font.DistanceFieldType.MSDF);
        font.scale(4.0f);
        style.font = font;
        TextraButton textraButton = new TextraButton("EXIT THE APP!", style);
        uiStage.addActor(textraButton);
        textraButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
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
