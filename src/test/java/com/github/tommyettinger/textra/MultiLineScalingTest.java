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
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;

public class MultiLineScalingTest extends ApplicationAdapter {
    Stage stage;
    Font font;
    @Override
    public void create() {
        stage = new Stage();
        stage.setDebugAll(true);
        font = KnownFonts.getRobotoCondensed(Font.DistanceFieldType.MSDF).scaleHeightTo(32);

        Styles.LabelStyle style = new Styles.LabelStyle(font, Color.WHITE);

        String text = "Hello, world! Did you know...\n\norange is my favorite color?";

        final TextraLabel testLabel1 = new TextraLabel("[%50]" + text, style);
        testLabel1.setWrap(true);
        testLabel1.setAlignment(Align.left);
        Container<TextraLabel> c1 = new Container<>(testLabel1).width(200);
        c1.setPosition(140, 200, Align.left);
        stage.addActor(c1);

        final TextraLabel testLabel2 = new TextraLabel(text, style);
        testLabel2.setWrap(true);
        testLabel2.setAlignment(Align.left);
        Container<TextraLabel> c2 = new Container<>(testLabel2).width(200);
        c2.setPosition(10 + c1.getX() + 200, 200, Align.left);
        stage.addActor(c2);

        final TextraLabel testLabel3 = new TextraLabel("{SCALE=150%}" + text, style);
        testLabel3.setWrap(true);
        testLabel3.setAlignment(Align.left);
        Container<TextraLabel> c3 = new Container<>(testLabel3).width(200);
        c3.setPosition(10 + c2.getX() + 200, 200, Align.left);
        stage.addActor(c3);
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
        config.setWindowedMode(720, 480);
        config.setResizable(true);
        config.setForegroundFPS(0);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new MultiLineScalingTest(), config);
    }
}
