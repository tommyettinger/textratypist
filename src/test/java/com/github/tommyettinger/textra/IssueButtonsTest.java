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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import static com.badlogic.gdx.utils.Align.center;

public class IssueButtonsTest extends ApplicationAdapter {
    ScreenViewport viewport;
    Stage stage;
    TextButton test1;
    TextraButton test2;

    @Override
    public void create() {
        viewport = new ScreenViewport();
        viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),true);
        stage = new Stage(viewport);
//        stage.setDebugAll(true);

        Skin skin = new FWSkin(Gdx.files.internal("paafomir/Homestuck_Skin.json"));
        test1 = new TextButton("TextButton", skin);
        test2 = new TypingButton("TextraButton", skin);

        Table table = new Table();
        table.add(test1).row();
        table.add(test2);
        table.setFillParent(true);
        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.DARK_GRAY);

        stage.act();
        stage.draw();
//        System.out.println(typingLabel.workingLayout.lines());
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
        config.setTitle("TextraButton test");
        config.setWindowedMode(600, 480);
        config.disableAudio(true);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new IssueButtonsTest(), config);
    }

}