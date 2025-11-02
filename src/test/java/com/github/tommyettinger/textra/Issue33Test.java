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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

public class Issue33Test extends ApplicationAdapter {
    Stage stage;

    private static final String longText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed non risus. Suspendisse lectus tortor, dignissim sit";

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_INFO);
        long startMillis = TimeUtils.millis();
        stage = new Stage();
        Skin skin = new FreeTypistSkin(Gdx.files.internal("uiskin3.json"));
        Table root = new Table(skin);

        Table labels = new Table();
        labels.defaults().pad(5);
        for (int i = 0; i < 100; i++) {
            // intentionally doing the wrong thing!
//            TextraLabel label = new TextraLabel(longText, new Styles.LabelStyle(skin.get(Label.LabelStyle.class)));
            // this would be better, but makes very little difference
            TextraLabel label = new TextraLabel(longText, skin);
            label.setWrap(true);
            labels.add(label).width(800).top();
            labels.row();
        }
        root.setFillParent(true);
        labels.pack();
        ScrollPane pane = new ScrollPane(labels);
        root.add(pane);
        labels.debugAll();
        stage.addActor(root);
        Gdx.input.setInputProcessor(stage);
        Gdx.app.log("TIMING", "create() took   : " + TimeUtils.timeSinceMillis(startMillis) + " ms");
        Gdx.app.log("HEAP",   "native heap used: " + Gdx.app.getNativeHeap());
        Gdx.app.log("HEAP",   "java heap used  : " + Gdx.app.getJavaHeap());
        Gdx.app.log("HEIGHT", "label height    : " + labels.getCells().first().getActorHeight());
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
    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("TextraLabel Wrap test");
        config.setWindowedMode(1800, 900);
        config.disableAudio(true);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new Issue33Test(), config);
    }
}
