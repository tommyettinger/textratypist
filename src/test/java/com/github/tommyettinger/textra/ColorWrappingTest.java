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
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;

// Checked with 2.6.0 before release, something is wrong.

/**
 * Checks an issue where apparently emoji get removed during {@link TextraLabel#setText(String)}...
 */
public class ColorWrappingTest extends ApplicationAdapter {
    Stage stage;
    Font font;
    @Override
    public void create() {
        stage = new Stage();
        stage.setDebugAll(true);

        font = KnownFonts.getDejaVuSans().setInlineImageStretch(0.75f);
        KnownFonts.addEmoji(font, 0, 5, -8f);
        final String text = "Did you know orange is my favorite color? Did[+👌🏼] YOU know orange is my favorite color? Did YOU know orange is my favorite color?";

        String initTo = "[#ffff00ff][%50]" + text;
        System.out.println(initTo);
        final TextraLabel label = new TextraLabel(initTo, font);
        label.setSize(300, 300);
        label.setPosition(100, 400);
        label.setWrap(true);
        label.setDebug(true);
//        label.setText("[#ffff00ff][%50]" + text);
        System.out.println(label);
        stage.addAction(
                Actions.sequence(
                        Actions.delay(2.0f),
                        Actions.run(() -> {
                            // change to same color it starts with, to see if color is the culprit
                            String setTo = "[#ffff00ff][%50]" + text;
                            System.out.println(setTo);
                            label.setText(setTo);
                            System.out.println(label);
                        }),
                        Actions.delay(3.0f),
                        Actions.run(() -> {
                            // change to the default color, to see if color is the culprit
                            String setTo = "[%50]" + text;
                            System.out.println(setTo);
                            label.setText(setTo);
                            System.out.println(label);
                        }),
                        Actions.delay(4.0f),
                        Actions.run(() -> {
                            // change to a different color
                            String setTo = "[#ff0000ff][%50]" + text;
                            System.out.println(setTo);
                            label.setText(setTo);
                            System.out.println(label);
                        })
                )
        );
        Table table = new Table();
        table.add(label).size(300, 300);
        table.setFillParent(true);
        stage.addActor(table);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        font.resizeDistanceField(width, height, stage.getViewport());
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);
        stage.act();
        stage.draw();
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Multi-Line Scaling Test");
        config.setWindowedMode(1080, 960);
        config.setResizable(true);
        config.setForegroundFPS(0);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new ColorWrappingTest(), config);
    }
}
