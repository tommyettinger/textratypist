/*
 * Copyright (c) 2022 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class IncongruityTest extends ApplicationAdapter {
    Stage stage;

    @Override
    public void create() {
        stage = new Stage(new ScreenViewport());
        Skin skin = new FreeTypistSkin(Gdx.files.internal("uiskin2.json"));
        Table root = new Table(skin);


        Font[] fonts = KnownFonts.getAllStandard();
        fonts = new Font[KnownFonts.STANDARD_NAMES.size - 1];
        for (int i = 0; i < fonts.length; i++) {
            fonts[i] = KnownFonts.getFont(KnownFonts.STANDARD_NAMES.orderedItems().get(i));
        }
        BitmapFont[] bitmapFonts = getFonts();
        Table labels = new Table();
        labels.defaults().pad(5);
        for (int i = 0; i < fonts.length; i++) {
            Font font = fonts[i];
            labels.add(new Label(font.name, skin)).left();
            TypingLabel label = new TypingLabel("Lorem ipsum etc. 123, TT", skin, font);
//            label.align = Align.bottom;
            labels.add(label).expandX().left();
//            label.validate();
            Gdx.app.log("Font", font.name + (font.isMono ? " (MONO)" : "") + ", " + label.getPrefWidth() + ", " + label.getPrefHeight() + ", " + font.scaleY);

            BitmapFont bf = bitmapFonts[i];
            if (bf != null) {
                Label.LabelStyle style = new Label.LabelStyle();
                style.font = bf;
                style.fontColor = Color.WHITE;
                Label bmLabel = new Label("Lorem ipsum etc. 123, BM", style);
                bmLabel.validate();
                float scaleX = fonts[i].scaleX;//label.getPrefWidth()/bmLabel.getPrefWidth();
                float scaleY = fonts[i].scaleY;//label.getPrefHeight()/bmLabel.getPrefHeight();
                bmLabel.setFontScale(bf.getScaleX() * Math.min(scaleX, scaleY), bf.getScaleY() * Math.min(scaleX, scaleY));
                Gdx.app.log("BMFont", font.name + ", " + bmLabel.getPrefWidth() + ", " + bmLabel.getPrefHeight()
                        + ", " + scaleX + ", " + scaleY);
                labels.add(bmLabel).expandX().left();
            } else {
                labels.add(new Label("MISSING!", skin)).expandX().left();
            }
            labels.row();
        }
        root.setFillParent(true);
        ScrollPane pane = new ScrollPane(labels);
        root.add(pane);
        labels.pack();
        labels.debugAll();
        stage.addActor(root);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);
        stage.getViewport().apply();
        stage.act();
        stage.draw();
    }

    private BitmapFont[] getFonts () {
        BitmapFont[] fonts = new BitmapFont[KnownFonts.STANDARD_NAMES.size - 1];
        for (int i = 0; i < fonts.length; i++) {
            fonts[i] = getFont(KnownFonts.STANDARD_NAMES.orderedItems().get(i));
        }

        return fonts;
    }

    private BitmapFont getFont (String name) {
//        BitmapFont bf = new BitmapFont(Gdx.files.internal(name + "-standard.fnt"));
        BitmapFont bf = KnownFonts.getBitmapFont(name);
        bf.getData().setScale(1);
        bf.setUseIntegerPositions(false);
        return bf;
    }
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
    }
    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("TextraLabel Incongruity test");
        config.setWindowedMode(1800, 900);
        config.disableAudio(true);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new IncongruityTest(), config);
    }

}