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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;

public class Issue23Test extends ApplicationAdapter {
    Stage stage;
    Font font;
    @Override
    public void create() {
        stage = new Stage();
        stage.setDebugAll(true);
        font = KnownFonts.getRobotoCondensed(Font.DistanceFieldType.MSDF).scaleHeightTo(32);
        TextureAtlas atlas = new TextureAtlas("controller_white.atlas");
        font.addAtlas(atlas, 0, 8, 0);
        font.addImage("\uE777", atlas.findRegion("white"));
        font.addImage(";", atlas.findRegion("white"));
        Styles.LabelStyle style = new Styles.LabelStyle(font, Color.WHITE);

        final String text = "Did YOU[+white] know orange, [+controller_B] it is my favorite color? Did YOU\uE777 know orange, it is my favorite color?";
        final TypingLabel label = new TypingLabel("[%50]" + text, style);
        label.setSize(900, 300);
        label.setPosition(100, 100);
        label.setWrap(true);
        label.setDebug(true);
        stage.addActor(label);

        final TypingLabel label2 = new TypingLabel(text, style);
        label2.setSize(900, 300);
        label2.setPosition(100, 400);
        label2.setWrap(true);
        label2.setDebug(true);
        stage.addActor(label2);

        final TypingLabel label3 = new TypingLabel("[%150]" + text, style);
        label3.setSize(900, 300);
        label3.setPosition(100, 700);
        label3.setWrap(true);
        label3.setDebug(true);
        stage.addActor(label3);
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
        new Lwjgl3Application(new Issue23Test(), config);
    }
}
