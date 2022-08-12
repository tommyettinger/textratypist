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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import static com.badlogic.gdx.utils.Align.*;

public class Issue6933Test extends ApplicationAdapter {
    FitViewport viewport;
    Stage stage;
    Label label;

    @Override
    public void create() {
        viewport = new FitViewport(600,480);
        viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),true);
        stage = new Stage(viewport);

        BitmapFont font = new BitmapFont();

        label = new Label("Checking for updates...", new Label.LabelStyle(font, Color.WHITE));
        label.setAlignment(center);
        label.setFillParent(true);
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
        new Lwjgl3Application(new Issue6933Test(), config);
    }

}