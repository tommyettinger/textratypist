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
import com.badlogic.gdx.graphics.GL20;
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
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class TextraUITest extends InputAdapter implements ApplicationListener {

//	TestWindow tw;
//	static class TestWindow extends Window
//	{
//		Table labelTable;
//		Table textraTable;
//		Table textraTopTable;
//		TextraLabel textraLabel;
//		TextraLabel textraTopLabel;
//		Label label;
//		public TestWindow(Skin skin)
//		{
//			super("", skin);
//
//			this.setResizable(true);
//
//			this.textraLabel = new TextraLabel("TextraLabel", skin);
//			this.textraLabel.setAlignment(Align.center);
//			this.textraTopLabel = new TextraLabel("TextraLabel Align Top", skin);
//			this.textraTopLabel.setAlignment(Align.top);
//			this.label = new Label("Label", skin);
//
//			this.textraTable = new Table();
//			this.textraTable.add(textraLabel);
//
//			this.textraTopTable = new Table();
//			this.textraTopTable.add(textraTopLabel);
//
//			this.labelTable = new Table();
//			this.labelTable.add(label);
//
//			add(this.textraTable).pad(50);
//			add(this.textraTopTable).pad(50);
//			add(this.labelTable).pad(50);
//
//			resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//		}
//
//		public void resize(float width, float height)
//		{
//			setSize(width / 2f, height / 3f);
//
//			label.layout();
//			float scale = height / 500f;
//			label.setFontScale(scale);
//			textraLabel.font.scale(
//					scale / this.textraLabel.font.scaleX,
//					scale / this.textraLabel.font.scaleY
//			);
//			textraTopLabel.font.scale(
//					scale / this.textraTopLabel.font.scaleX,
//					scale / this.textraTopLabel.font.scaleY
//			);
//			textraTopTable.top();
//			textraTopLabel.setAlignment(Align.top);
//			textraTopTable.getCell(textraTopLabel).top().align(Align.top);
//
//			invalidateHierarchy();
//			pack();
//		}
//	}

	String[] listEntries = {"This is a list entry1", "And another one1", "The meaning of life1", "Is hard to come by1",
		"This is a list entry2", "And another one2", "The meaning of life2", "Is hard to come by2", "This is a list entry3",
		"And another one3", "The meaning of life3", "Is hard to come by3", "This is a list entry4", "And another one4",
		"The meaning of life4", "Is hard to come by4", "This is a list entry5", "And another one5", "The meaning of life5",
		"Is hard to come by5"};

	Skin skin;
	Stage stage;
	Texture texture1;
	Texture texture2;
	TextraLabel fpsLabel;
	GLProfiler profiler;

	@Override
	public void create () {
		profiler = new GLProfiler(Gdx.graphics);
		profiler.enable();
		skin = new FreeTypistSkin(Gdx.files.internal("uiskin2.json"));
		texture1 = new Texture(Gdx.files.internal("badlogicsmall.jpg"));
		texture2 = new Texture(Gdx.files.internal("badlogic.jpg"));
		TextureRegion image = new TextureRegion(texture1);
		TextureRegion imageFlipped = new TextureRegion(image);
		imageFlipped.flip(true, true);
		TextureRegion image2 = new TextureRegion(texture2);

		final Font font = KnownFonts.getStandardFamily();
		for(Font f : font.family.connected) {
			if(f != null)
				KnownFonts.addEmoji(f);
		}

		// stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, new PolygonSpriteBatch());
		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(stage);

//		 stage.setDebugAll(true);

		Styles.ImageTextButtonStyle style = new Styles.ImageTextButtonStyle(skin.get(Styles.TextButtonStyle.class));
		style.imageUp = new TextureRegionDrawable(image);
		style.imageDown = new TextureRegionDrawable(imageFlipped);
		ImageTextraButton iconButton = new ImageTextraButton("[/]a e s t h e t i c", style, font);

		Button buttonMulti = new TextraButton("Multi\nLine\nToggle", skin, "toggle", font);
		Button imgButton = new Button(new Image(image), skin);
		Button imgToggleButton = new Button(new Image(image), skin, "toggle");

		TextraLabel myLabel = new TextraLabel("This is some text.", skin, font);

		Table t = new Table();
		t.row();
		t.add(myLabel);

		t.layout();

		final TextraCheckBox checkBox = new TextraCheckBox(" Continuous rendering[+saxophone][+clown face][+saxophone]", skin, font);
		checkBox.setChecked(true);
		final Slider slider = new Slider(0, 10, 1, false, skin);
		slider.setAnimateDuration(0.3f);
		TextField textfield = new TextField("", skin);
		textfield.setMessageText("Click here!");
		textfield.setAlignment(Align.center);
		final SelectBox<String> selectBox = new SelectBox<>(skin);
		selectBox.setAlignment(Align.right);
		selectBox.getList().setAlignment(Align.right);
		selectBox.getStyle().listStyle.selection.setRightWidth(10);
		selectBox.getStyle().listStyle.selection.setLeftWidth(20);
		selectBox.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				System.out.println(selectBox.getSelected());
			}
		});
		selectBox.setItems("Android1", "Windows1 long text in item", "Linux1", "OSX1", "Android2", "Windows2", "Linux2", "OSX2",
			"Android3", "Windows3", "Linux3", "OSX3", "Android4", "Windows4", "Linux4", "OSX4", "Android5", "Windows5", "Linux5",
			"OSX5", "Android6", "Windows6", "Linux6", "OSX6", "Android7", "Windows7", "Linux7", "OSX7");
		selectBox.setSelected("Linux6");
		Image imageActor = new Image(image2);
		ScrollPane scrollPane = new ScrollPane(imageActor);
		TextraListBox<TextraLabel> list = new TextraListBox<>(skin);
		TextraLabel[] entriesArray = new TextraLabel[listEntries.length];
		for (int i = 0; i < listEntries.length; i++) {
			entriesArray[i] = new TextraLabel(listEntries[i], skin);
		}
		list.setItems(entriesArray);
		list.getSelection().setMultiple(true);
		list.getSelection().setRequired(false);
		// list.getSelection().setToggle(true);
		ScrollPane scrollPane2 = new ScrollPane(list, skin);
		scrollPane2.setFlickScroll(false);
		TextraLabel minSizeLabel = new TextraLabel("[@Medieval]ginWidth cell", skin, font); // demos SplitPane respecting widget's minWidth
		Table rightSideTable = new Table(skin);
		rightSideTable.add(minSizeLabel).growX().row();
		rightSideTable.add(scrollPane2).grow();
		SplitPane splitPane = new SplitPane(scrollPane, rightSideTable, false, skin, "default-horizontal");
		fpsLabel = new TextraLabel("fps:", font);
		fpsLabel.setAlignment(Align.left);
		// configures an example of a TextField in password mode.
		final TextraLabel passwordLabel = new TextraLabel("[@Medieval]Textfield in [~]secure[ ] password mode: ", skin, font);
		final TextField passwordTextField = new TextField("", skin);
		passwordTextField.setMessageText("password");
		passwordTextField.setPasswordCharacter('*');
		passwordTextField.setPasswordMode(true);

		buttonMulti.addListener(new TextraTooltip(
			"This is a tooltip! [~]This is a tooltip! [_]This is a tooltip! [/]This is a tooltip![~] This is a tooltip![_] This is a tooltip!",
//			skin)); // this doesn't wrap or show a BG
			skin, font)); // this wraps correctly but still doesn't show a BG
		Table tooltipTable = new Table(skin);
		tooltipTable.pad(10).background("default-round");
		tooltipTable.add(new TextraButton("Fancy tooltip!", skin, font));
		imgButton.addListener(new Tooltip<>(tooltipTable));

		TextraWindow window = new TextraWindow("TextraWindow", skin, "default", new Font(font).scale(0.75f, 0.75f), false);
//		window.getTitleTable().debug();
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
		window.add(selectBox).maxWidth(100);
		window.add(textfield).minWidth(100).expandX().fillX().colspan(3);
		window.row();
		window.add(splitPane).fill().expand().colspan(4).maxHeight(200);
		window.row();
		window.add(passwordLabel).left().colspan(2);
		window.add(passwordTextField).minWidth(100).expandX().fillX().colspan(2);
		window.row();
		window.add(fpsLabel).left().colspan(4);
		window.pack();

		// stage.addActor(new Button("Behind Window", skin));
		stage.addActor(window);

//		tw = new TestWindow(skin);
//		stage.addActor(tw);

		textfield.setTextFieldListener(new TextFieldListener() {
			public void keyTyped (TextField textField, char key) {
				if (key == '\n') textField.getOnscreenKeyboard().show(false);
			}
		});

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
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		fpsLabel.getFont().markup("fps: " + Gdx.graphics.getFramesPerSecond() + "[^][SKY][[citation needed]", fpsLabel.layout.clear());
		fpsLabel.rotateBy(Gdx.graphics.getDeltaTime() * 25f);

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
//		tw.resize(width, height);
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
		config.setWindowedMode(760, 600);
		config.disableAudio(true);
		ShaderProgram.prependVertexCode = "#version 110\n";
		ShaderProgram.prependFragmentCode = "#version 110\n";
//		config.enableGLDebugOutput(true, System.out);
//		config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
		config.useVsync(true);
		config.setForegroundFPS(0);
		new Lwjgl3Application(new TextraUITest(), config);
	}

}
