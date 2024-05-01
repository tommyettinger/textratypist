/*******************************************************************************
 * Copyright 2023 See AUTHORS file.
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
 ******************************************************************************/

package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.ray3k.stripe.FreeTypeSkin;

public class ScrollPaneUITest extends InputAdapter implements ApplicationListener {
	Skin skin;
	Stage stage;

	@Override
	public void create () {
		skin = new FreeTypeFWSkin(Gdx.files.internal("uiskin2.json"));
		final Font font = KnownFonts.getStandardFamily();
		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(stage);

		Table root = new Table();
		root.setFillParent(true);
		stage.addActor(root);
		
		TypingLabel typingLabel = new TypingLabel("{HIGHLIGHT}This is a test.\n" +
				"Well, what in {TRIGGER=NAME}Raeleus'{ENDTRIGGER} name are we waiting for?\n" +
				"Let's go!{ENDHIGHLIGHT}", font);
		typingLabel.setSelectable(true);
		typingLabel.setTypingListener(new TypingAdapter(){
			@Override
			public void event(String event) {
				System.out.println(typingLabel.getSelectedText());
			}
		});
//		root.add(typingLabel);
		ScrollPane scrollPane = new ScrollPane(typingLabel);
		root.add(scrollPane);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void dispose () {
		stage.dispose();
		skin.dispose();
	}

	public static void main(String[] args){
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("TypingLabel UI test");
		config.setWindowedMode(760, 640);
		config.disableAudio(true);
		config.useVsync(true);
		config.setForegroundFPS(0);
		new Lwjgl3Application(new ScrollPaneUITest(), config);
	}
}
