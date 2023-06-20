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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.ray3k.stripe.FreeTypeSkin;

public class IncongruityTest extends ApplicationAdapter {
    Stage stage;

    @Override
    public void create() {
        stage = new Stage();
        Skin skin = new FreeTypeSkin(Gdx.files.internal("uiskin2.json"));
        Table root = new Table(skin);

        Font[] fonts = KnownFonts.getAllStandard();
        BitmapFont[] bitmapFonts = getFonts();
        Table labels = new Table();
        labels.defaults().pad(5);
        for (int i = 0; i < fonts.length; i++) {
            Font font = fonts[i];
//            Font font = fonts[i].setDescent(fonts[i].getDescent() * 2);
            labels.add(new Label(font.name, skin)).left();
            TypingLabel label = new TypingLabel("Dummy Text 123", skin, font);
            labels.add(label).expandX().left();
//            label.validate();
            Gdx.app.log("Font", font.name + ", " + label.getPrefWidth() + ", " + label.getPrefHeight() + ", " + font.scaleY);

            BitmapFont bf = bitmapFonts[i];
            if (bf != null) {
                Label.LabelStyle style = new Label.LabelStyle();
                style.font = bf;
                style.fontColor = Color.WHITE;
                Label bmLabel = new Label("Dummy Text 123", style);
                bmLabel.validate();
                float scaleY = label.getPrefHeight()/bmLabel.getPrefHeight();
                float scaleX = label.getPrefWidth()/bmLabel.getPrefWidth();
                Gdx.app.log("BMFont", font.name + ", " + bmLabel.getPrefWidth() + ", " + bmLabel.getPrefHeight()
                        + ", " + scaleX + ", " + scaleY);
                bmLabel.setFontScale(bf.getScaleX() * Math.min(scaleX, scaleY), bf.getScaleY() * Math.min(scaleX, scaleY));
                labels.add(bmLabel).expandX().left();
            } else {
                labels.add(new Label("MISSING!", skin)).expandX().left();
            }
            if((i & 1) == 1)
            labels.row();
        }
        root.setFillParent(true);
        root.add(labels);
        labels.debugAll();
        stage.addActor(root);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);

        stage.act();
        stage.draw();
    }

    private BitmapFont[] getFonts () {
        BitmapFont tall = getFont("AStarry");
        tall.getData().setScale(1, 2);
        // getAStarry(), getAStarry().scaleTo(8, 16).setName("A Starry Tall"), getBitter(), getCanada(),
        // getCascadiaMono(), getCaveat(), getCozette(), getGentium(), getGentiumUnItalic(), getHanazono(),
        // getIBM8x16(), getInconsolata(), getIosevka(), getIosevkaSlab(), getKingthingsFoundation(),
        // getKingthingsPetrock(), getLibertinusSerif(), getNowAlt(), getOpenSans(), getOxanium(), getQuanPixel(),
        // getRobotoCondensed(), getTangerine(), getYanoneKaffeesatz()
        return new BitmapFont[] {
                getFont("AStarry"),
                tall,
                getFont("Bitter"),
                getFont("Canada1500"),
                getFont("CascadiaMono"),
                getFont("Caveat"),
                getFont("Cozette"),
                getFont("Gentium"),
                getFont("GentiumUnItalic"),
                getFont("Hanazono"),
                // cant load this one, wrong format
//			getFont("IBM-8x16"),
                null,
                getFont("Inconsolata-LGC-Custom"),
                getFont("Iosevka"),
                getFont("Iosevka-Slab"),
                getFont("KingthingsFoundation"),
                getFont("KingthingsPetrock"),
                getFont("LibertinusSerif"),
                getFont("Now-Alt"),
                getFont("OpenSans"),
                getFont("Oxanium"),
                getFont("QuanPixel"),
                getFont("RobotoCondensed"),
                getFont("Tangerine"),
                getFont("YanoneKaffeesatz"),
        };
    }

    private BitmapFont getFont (String name) {
        BitmapFont bf = new BitmapFont(Gdx.files.internal(name + "-standard.fnt"));
        bf.setUseIntegerPositions(false);
        return bf;
    }
    @Override
    public void resize(int width, int height) {
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
        config.setWindowedMode(1350, 700);
        config.disableAudio(true);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new IncongruityTest(), config);
    }

}