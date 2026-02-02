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

package com.github.tommyettinger;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.tommyettinger.freetypist.FreeTypistSkin;
import com.github.tommyettinger.textra.*;

public class StressTest extends ApplicationAdapter {
    Stage stage;

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_INFO);
        long startMillis = TimeUtils.millis();
        stage = new Stage();
        FreeTypistSkin skin = new FreeTypistSkin(Gdx.files.internal("uiskin.json"));
        Table root = new Table(skin);

        Table labels = new Table();
        labels.defaults().pad(5);
        for (int i = 0; i < 300; i++) {
            // intentionally doing the wrong thing!
            TypingLabel label = new TypingLabel("Lorem ipsum etc. " + i, new Styles.LabelStyle(skin.get(Label.LabelStyle.class)));
            // this would be better, but makes very little difference
//            TypingLabel label = new TypingLabel("Lorem ipsum etc. " + i, skin);
            labels.add(label).expandX().left();
            // wrong thing again
            TypingLabel label2 = new TypingLabel("Lorem ipsum etc. " + ++i, new Styles.LabelStyle(skin.get(Label.LabelStyle.class)));
            // this would be better
//            TypingLabel label2 = new TypingLabel("Lorem ipsum etc. " + ++i, skin);
            labels.add(label2).expandX().center();
            // wrong thing again
            TypingLabel label3 = new TypingLabel("Lorem ipsum etc. " + ++i, new Styles.LabelStyle(skin.get(Label.LabelStyle.class)));
            // this would be better
//            TypingLabel label3 = new TypingLabel("Lorem ipsum etc. " + ++i, skin);
            labels.add(label3).expandX().right();
            labels.row();
        }
        root.setFillParent(true);
        ScrollPane pane = new ScrollPane(labels);
        root.add(pane);
        labels.pack();
        labels.debugAll();
        stage.addActor(root);
        Gdx.input.setInputProcessor(stage);
        Gdx.app.log("TIMING", "create() took   : " + TimeUtils.timeSinceMillis(startMillis) + " ms");
        Gdx.app.log("HEAP",   "native heap used: " + Gdx.app.getNativeHeap());
        Gdx.app.log("HEAP",   "java heap used  : " + Gdx.app.getJavaHeap());
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);

        stage.act();
        stage.draw();
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
}
