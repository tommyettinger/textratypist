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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class WrappingTest extends ApplicationAdapter {
    Stage stage;
    Font font;
    @Override
    public void create() {
        stage = new Stage();
        font = KnownFonts.getRobotoCondensed();
        Array<TypingLabel> labels = new Array<>(new TypingLabel[]{
                new TypingLabel("", font)
        });
        TypingLabel label = labels.get(0);
        label.setWrap(true);
        label.setWidth(200);
        label.setSize(200, 400);
        label.setAlignment(Align.top);
        label.setText("{RAINBOW}Texttexttextte xtext text t-e-x-t text text text text\ntexttexttextte xtext space \n text t-e-x-t two newlines\n\ntexttexttextte xtext text t-e-x-t{ENDRAINBOW}");
        label.setTypingListener(new TypingAdapter(){
            @Override
            public void end() {
                System.out.println(label.workingLayout);
            }
        });

        BitmapFont bmfont = BitmapFontSupport.loadStructuredJson(Gdx.files.internal("Roboto-Condensed-standard.dat"), "Roboto-Condensed-standard.png");
        // new BitmapFont(Gdx.files.internal("RobotoCondensed-standard.fnt"));
        bmfont.getData().markupEnabled = true;
        Label s2dLabel = new Label("I [SKY]can [ROYAL]do[] it!", new Label.LabelStyle(bmfont, Color.WHITE));
        s2dLabel.setAlignment(Align.bottom);
        Table table = new Table();
        table.setFillParent(true);
        table.add(label).row();
        table.add(s2dLabel).bottom();
        stage.addActor(table);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);
        stage.act();
        stage.draw();
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Wrapping Test");
        config.setWindowedMode(720, 480);
        config.setResizable(true);
        config.setForegroundFPS(0);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new WrappingTest(), config);
    }
}
