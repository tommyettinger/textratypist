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
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.ScreenUtils;

public class ColorWrappingTest extends ApplicationAdapter {
    Stage stage;
    Font font;
    @Override
    public void create() {
        stage = new Stage();
        stage.setDebugAll(true);
        String text = "Did you know orange is my favorite color? Did[+OK hand, medium-light skin tone] YOU know orange is my favorite color? Did YOU know orange is my favorite color?";

        font = KnownFonts.getDejaVuSans();
        KnownFonts.addEmoji(font);

        final TextraLabel label = new TextraLabel("[#ffff00ff][%50]" + text, font);
        label.setSize(300, 300);
        label.setPosition(100, 400);
        label.setWrap(true);
        label.setDebug(true);
        stage.addAction(
                Actions.sequence(
                        Actions.delay(2.0f),
                        Actions.run(() -> {
                            // change to same color it starts with, to see if color is the culprit
                            label.setText("[#ffff00ff][%50]" + text);
                        }),
                        Actions.delay(4.0f),
                        Actions.run(() -> {
                            // change to a different color
                            label.setText("[#ff0000ff][%50]" + text);
                        })
                )
        );
        stage.addActor(label);
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
