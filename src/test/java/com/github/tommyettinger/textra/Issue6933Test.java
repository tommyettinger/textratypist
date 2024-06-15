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
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Issue6933Test extends ApplicationAdapter {
    FitViewport viewport;
    Stage stage;
    Label label;

    @Override
    public void create() {
        viewport = new FitViewport(600,480);
        viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),true);
        stage = new Stage(viewport);

//        BitmapFont font = new BitmapFont();
//        label = new Label("Checking for updates...", new Styles.LabelStyle(font, Color.WHITE));
//        label.setAlignment(Align.center);
//        label.setFillParent(true);
//        stage.addActor(label);

//        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("Montserrat-Regular.ttf"));
//        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
//        parameter.size = 40; // important! set this to the size you will display at!!!
//
//        // this section just ensures every printable character in ASCII will be in the font.
//        StringBuilder sb = new StringBuilder(128).append(" \r\n\t");
//        for (int i = 32; i < 127; i++) {
//            sb.append((char) i);
//        }
//        // You can also give this any String that has all the characters you want to use.
//        parameter.characters = sb.toString();
//
//        //FreeTypeFontGenerator.setMaxTextureSize(2048); // only needed for large font sizes or many glyphs
//        BitmapFont bitmapFont = fontGenerator.generateFont(parameter);
//
//        Styles.LabelStyle labelStyle = new Styles.LabelStyle(bitmapFont, Color.WHITE);
//        this.label = new Label("FPS: " + Gdx.graphics.getFramesPerSecond(), labelStyle);
//        label.setSize(100f, 60f);
//        label.setAlignment(Align.left, Align.bottom);
//        label.setPosition(100f, 150f);
//        stage.addActor(label);

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("TiroDevanagariHindi-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
//        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "उच्चस्कोर";
        parameter.incremental = true;

        parameter.size = 40;

        BitmapFont bitmapFont = generator.generateFont(parameter);
        Label.LabelStyle labelStyle = new Label.LabelStyle(bitmapFont, Color.WHITE);
        this.label = new Label("उच्च स्कोर", labelStyle); // Devanagari glyphs do not combine.
        label.setSize(100f, 60f);
        label.setAlignment(Align.left, Align.bottom);
        label.setPosition(100f, 150f);
        stage.addActor(label);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.DARK_GRAY);

        stage.act();
        stage.draw();
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
        config.setTitle("TextraLabel UI test");
        config.setWindowedMode(600, 480);
        config.disableAudio(true);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL32, 3, 2);
        new Lwjgl3Application(new Issue6933Test(), config);
    }

}