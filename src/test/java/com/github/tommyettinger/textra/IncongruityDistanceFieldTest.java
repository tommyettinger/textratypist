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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;

public class IncongruityDistanceFieldTest extends ApplicationAdapter {
    Stage stage;
    Font[] sdfFonts;
    Font[] msdfFonts;

    @Override
    public void create() {
        stage = new Stage();
        Skin skin = new FreeTypistSkin(Gdx.files.internal("uiskin2.json"));
        Table root = new Table(skin);

        sdfFonts = KnownFonts.getAllSDF();
        msdfFonts = KnownFonts.getAllMSDF();

        Table labels = new Table();
        labels.defaults().pad(5);
        for (int i = 0; i < sdfFonts.length; i++) {
            Font font = sdfFonts[i];
            labels.add(new Label(font.name, skin)).left();
            TypingLabel label = new TypingLabel("Dummy Text 123", skin, font);
            labels.add(label).expandX().left();
//            label.validate();
            labels.row();
        }
        for (int i = 0; i < msdfFonts.length; i++) {
            Font font = msdfFonts[i];
            labels.add(new Label(font.name, skin)).left();
            TypingLabel label = new TypingLabel("Dummy Text 123", skin, font);
            labels.add(label).expandX().left();
//            label.validate();
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
//        System.out.println("A Starry (MSDF) uses shader " + msdfFonts[0].shader);
//        System.out.println("Cascadia Mono (MSDF) uses shader " + msdfFonts[1].shader);
//        System.out.println("Iosevka (MSDF) uses shader " + msdfFonts[6].shader);
    }

    @Override
    public void resize(int width, int height) {
        for(Font f : sdfFonts)
            f.resizeDistanceField(width, height);
        for(Font f : msdfFonts)
            f.resizeDistanceField(width, height);
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
        config.setTitle("TextraLabel Incongruity (Distance Field) test");
        config.setWindowedMode(800, 900);
        config.disableAudio(true);
		config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new IncongruityDistanceFieldTest(), config);
    }

}