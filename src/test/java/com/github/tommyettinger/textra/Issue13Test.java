/*
 * Copyright (c) 2023 See AUTHORS file.
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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class Issue13Test  extends ApplicationAdapter {

    private Stage stage;
    private FWSkin skin;

    @Override
    public void create () {
        stage = new Stage(new ScreenViewport());
//        stage.setDebugAll(true);
        skin = new FWSkin(Gdx.files.internal("uiskin.json"));

        int fitOneLineCount = 25;
        int extraSpaces = 10;

        StringBuilder text = new StringBuilder();
//        for (int count = 0; count < fitOneLineCount + extraSpaces; count++) {
//            text.append(' ');
//        }
//        text.append("OK.");

        // This seems to work here.
        // https://i.imgur.com/LFYLAPc.png
        text.append("this is a normal text test test        test!");
        // It works for TextraLabel and TypingLabel, in the same way.
        TextraLabel label;
//        if("TEXTRA".equals("TYPING")) {
        if("TEXTRA".equals("TEXTRA")) {
            label = new TextraLabel(text.toString(), skin);
            label.setWrap(true);
            // Runs in the next render thread so the layout is ready.
            Gdx.app.postRunnable(() -> System.out.println("Height: " + label.getHeight()));
        } else {
            label = new TypingLabel(text.toString(), skin);
            label.setWrap(true);
            // Runs in the next render thread so the layout is ready.
            Gdx.app.postRunnable(() -> System.out.println("Height: " + label.getHeight()));
        }
        Table table = new Table();
        table.debug();
        table.add(label).prefWidth(100).row();
        // pack() sets the actual size to the preferred size and then validates the sizing.
        // without calling pack(), the actual height will be reported as one line's worth,
        // but after calling pack(), it should match the actual size.
//        table.pack();
        Stack stack = new Stack(table);
        stack.setFillParent(true);
        stage.addActor(stack);
    }

    @Override
    public void render () {
        ScreenUtils.clear(0, 0, 0, 1);
        stage.act();
        stage.draw();
    }

    @Override
    public void dispose () {
        stage.dispose();
        skin.dispose();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
        skin.resizeDistanceFields(width, height, stage.getViewport());
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Space Wrapping Test");
        config.setWindowedMode(720, 480);
        config.setResizable(true);
        config.setForegroundFPS(0);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new Issue13Test(), config);
    }
}