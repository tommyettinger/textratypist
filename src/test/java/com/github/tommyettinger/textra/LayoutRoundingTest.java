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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Tests <a href="https://github.com/libgdx/libgdx/pull/7276">libGDX PR 7276</a>, which
 * relates to how widgets size themselves and their containing layouts. This currently
 * just tests a Label in a Container; I need to make sure the ellipsis works correctly
 * in TextraLabel before I can test it here.
 */
public class LayoutRoundingTest extends ApplicationAdapter {
	Skin skin;
	Stage stage;
	SpriteBatch batch;

	public void create () {
		batch = new SpriteBatch();
		skin = new Skin(Gdx.files.internal("uiskin.json"));
		stage = new Stage(new ScreenViewport());

		skin.getFont("default-font").getData().setScale(1.23f);

		Label label = new Label("This is some text.", skin);
		label.setEllipsis(true); // So we can easily see when the label doesn't fit.
		System.out.println(label.getPrefWidth()); // 127 normally * 1.23 font scale = 156.20999

		Container<Label> container = new Container<>(label);
		container.setPosition(100, 100);
		container.pack();
		stage.addActor(container);
	}

	public void render () {
		ScreenUtils.clear(0, 0, 0, 1);
		stage.draw();
	}

	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
	}

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("TextraLabel vs. Label test");
		config.setWindowedMode(640, 480);
		config.disableAudio(true);
		config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new LayoutRoundingTest(), config);
    }
}
