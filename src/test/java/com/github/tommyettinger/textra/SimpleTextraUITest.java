/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton.ImageTextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.ray3k.stripe.FreeTypeSkin;

public class SimpleTextraUITest extends InputAdapter implements ApplicationListener {
	Skin skin;
	Stage stage;
	Texture texture1;
	Texture texture2;
	GLProfiler profiler;

	@Override
	public void create () {
		profiler = new GLProfiler(Gdx.graphics);
		profiler.enable();
		skin = new FreeTypeSkin(Gdx.files.internal("uiskin2.json"));
		texture1 = new Texture(Gdx.files.internal("badlogicsmall.jpg"));
		texture2 = new Texture(Gdx.files.internal("badlogic.jpg"));
		TextureRegion image = new TextureRegion(texture1);
		TextureRegion imageFlipped = new TextureRegion(image);
		imageFlipped.flip(true, true);
		final Font font = new Font(skin.getFont("outline-font"), 0f, 15f, 0f, 10f);
		font.family = new Font.FontFamily(KnownFonts.getStandardFamily().family);
		font.family.connected[11] =
				KnownFonts.getYanoneKaffeesatz();
		font.family.connected[11].scale(2, 2);
		font.family.connected[0] = font;

		for(Font f : font.family.connected) {
			if(f != null)
				KnownFonts.addEmoji(f);
		}

		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(stage);

//		 stage.setDebugAll(true);

		ImageTextButtonStyle style = new ImageTextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
		style.imageUp = new TextureRegionDrawable(image);
		style.imageDown = new TextureRegionDrawable(imageFlipped);
		ImageTextraButton iconButton = new ImageTextraButton("jóÓetc[_]Ójóetc[_]cjóÓet", style, font);
//		ImageTextraButton iconButton = new ImageTextraButton("[/]a e s t h e t i c", style, font);

		Button buttonMulti = new TextraButton("jóÓetc\nÓjóetc\ncjóÓet", skin, "toggle", font);
//		Button buttonMulti = new TextraButton("Multi\nLine\nToggle", skin, "toggle", font);
		Button imgButton = new Button(new Image(image), skin);
		Button imgToggleButton = new Button(new Image(image), skin, "toggle");

		final TextraCheckBox checkBox = new TextraCheckBox(" Continuous rendering[+saxophone][+clown face][+saxophone]", skin, font);
		checkBox.setChecked(true);
		final Slider slider = new Slider(0, 10, 1, false, skin);
		slider.setAnimateDuration(0.3f);
		TextraLabel minSizeLabel = new TextraLabel("[@Medieval]ginWidth cell", skin, font);
		Table rightSideTable = new Table(skin);
		rightSideTable.add(minSizeLabel).growX().row();

		buttonMulti.addListener(new TextraTooltip(
			"This is a tooltip! [~]This is a tooltip! [_]This is a tooltip! [/]This is a tooltip![~] This is a tooltip![_] This is a tooltip!",
//			skin)); // this doesn't wrap or show a BG
			skin, font)); // this wraps correctly but still doesn't show a BG
		Table tooltipTable = new Table(skin);
		tooltipTable.pad(10).background("default-round");
		tooltipTable.add(new TextraButton("Fancy tooltip!", skin, font));
		imgButton.addListener(new Tooltip<>(tooltipTable));

		// window.debug();
		TextraWindow window = new TextraWindow("TextraWindow", skin, "default", new Font(font), true);
		window.getTitleTable().add(new TextraButton("X", skin, window.font)).height(window.getPadTop());
		window.setPosition(0, 0);
		window.defaults().spaceBottom(10);
		window.row().fill().expandX();
		window.add(iconButton);
		window.add(buttonMulti);
		window.add(imgButton);
		window.add(imgToggleButton);
		window.row();
		window.add(checkBox);
		window.add(slider).minWidth(100).fillX().colspan(3);
		window.row();
		window.add(rightSideTable).fill().expand().colspan(4).maxHeight(200);
		window.pack();

		// stage.addActor(new Button("Behind Window", skin));
		stage.addActor(window);

		slider.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				Gdx.app.log("UITest", "slider: " + slider.getValue());
			}
		});

		iconButton.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				new TextraDialog("Some TextraDialog", skin, "dialog", font) {
					protected void result (Object object) {
						System.out.println("Chosen: " + object);
					}
				}.text("Are you enjoying this demo?").button("Yes", true).button("No", false).key(Keys.ENTER, true)
					.key(Keys.ESCAPE, false).show(stage);
			}
		});

		checkBox.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				Gdx.graphics.setContinuousRendering(checkBox.isChecked());
			}
		});
	}

	@Override
	public void render () {
		profiler.reset();
		ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1);

		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
		if(Gdx.input.isKeyJustPressed(Keys.SPACE))
			System.out.printf("Calls: %d, draw calls: %d, shader switches: %d, texture bindings: %d\n",
					profiler.getCalls(), profiler.getDrawCalls(),
					profiler.getShaderSwitches(), profiler.getTextureBindings());
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
		texture1.dispose();
		texture2.dispose();
	}

	public static void main(String[] args){
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("TextraLabel UI test");
		config.setWindowedMode(900, 700);
		config.disableAudio(true);
		ShaderProgram.prependVertexCode = "#version 110\n";
		ShaderProgram.prependFragmentCode = "#version 110\n";
//		config.enableGLDebugOutput(true, System.out);
//		config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
		config.useVsync(true);
		config.setForegroundFPS(0);
		new Lwjgl3Application(new SimpleTextraUITest(), config);
	}

}
