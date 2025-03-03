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

package com.github.tommyettinger;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.tommyettinger.textra.*;

public class TypingUITest extends InputAdapter implements ApplicationListener {
	String[] listEntries = {"This is a list entry1", "And another one1", "The meaning of life1", "Is hard to come by1",
		"This is a list entry2", "And another one2", "The meaning of life2", "Is hard to come by2", "This is a list entry3",
		"And another one3", "The meaning of life3", "Is hard to come by3", "This is a list entry4", "And another one4",
		"The meaning of life4", "Is hard to come by4", "This is a list entry5", "And another one5", "The meaning of life5",
		"Is hard to come by5"};

	Skin skin;
	Font font;
	TypingWindow window;
	Stage stage;
	Texture texture1;
	Texture texture2;
	TypingLabel fpsLabel;
//	GLProfiler profiler;

    public static Font getStandardFamily() {
        Font.FontFamily family = new Font.FontFamily(
            new String[]{"Serif", "Humanist", "Medieval", },
            new Font[]{KnownFonts.getGentium(), KnownFonts.getYanoneKaffeesatz(),
                KnownFonts.getKingthingsFoundation(),});
        return family.connected[0].setFamily(family);
    }

    @Override
	public void create () {
//		profiler = new GLProfiler(Gdx.graphics);
//		profiler.enable();
		skin = new FWSkin(Gdx.files.internal("uiskin.json"));
		texture1 = new Texture(Gdx.files.internal("badlogicsmall.jpg"));
		texture2 = new Texture(Gdx.files.internal("badlogic.jpg"));
		TextureRegion image = new TextureRegion(texture1);
		TextureRegion imageFlipped = new TextureRegion(image);
		imageFlipped.flip(true, true);
		TextureRegion image2 = new TextureRegion(texture2);
		final Font.FontFamily family = getStandardFamily().family;
		family.connected[2] =
				KnownFonts.getYanoneKaffeesatz();
		family.connected[0] = KnownFonts.getNowAlt().scaleHeightTo(30);
		font = family.connected[0];
		font.family = family;
		for(Font f : font.family.connected) {
			if(f != null)
				KnownFonts.addEmoji(f);
		}
		stage = new Stage(new ScreenViewport(), new SpriteBatch(1000));
//		stage = new Stage(new ScreenViewport(), new TextureArraySpriteBatch(1000));
//		stage = new Stage(new ScreenViewport(), new ArrayTextureSpriteBatch(1000, 2048, 2048, 16, GL30.GL_NEAREST, GL30.GL_LINEAR_MIPMAP_LINEAR));
		Gdx.input.setInputProcessor(stage);

//		stage.setDebugAll(true);

		Styles.ImageTextButtonStyle style = new Styles.ImageTextButtonStyle(skin.get(Styles.TextButtonStyle.class));
		style.imageUp = new TextureRegionDrawable(image);
		style.imageDown = new TextureRegionDrawable(imageFlipped);
		ImageTypingButton iconButton = new ImageTypingButton("jóÓetcjóÓetcjóÓetc", style, font);
//		ImageTextraButton iconButton = new ImageTextraButton("[/]a e s t h e t i c", style, font);

//		TypingButton buttonMulti = new TypingButton("", skin, "toggle", font);
//		TypingLabel fancyLabel = new TypingLabel("", font){
//			@Override
//			public void act(float delta) {
//				if(System.currentTimeMillis() % 5000 > 4940) {
//					restart("[DARK-bright-raspberry-magenta-rose-red-ember]So juicy and red! You could probably collect them on a mountain..");
//				}
//				super.act(delta);
//			}
//		};
//		fancyLabel.debug();
//		buttonMulti.setTextraLabel(fancyLabel);
//		fancyLabel.setWrap(true);
//		buttonMulti.getTextraLabelCell().width(403);
		TypingButton buttonMulti = new TypingButton("jóÓetc\nÓjóetc\ncjóÓet", skin, "toggle", font);
//		Button buttonMulti = new TextraButton("Multi\nLine\nToggle", skin, "toggle");
		Button imgButton = new Button(new Image(image), skin);
		Button imgToggleButton = new Button(new Image(image), skin, "toggle");

		final TypingCheckBox checkBox = new TypingCheckBox(" Continuous rendering[+saxophone][+clown face][+saxophone]", skin, font);
		checkBox.setChecked(true);
		final Slider slider = new Slider(0, 10, 1, false, skin);
		slider.setAnimateDuration(0.3f);
		TextraField textfield = new TextraField("", skin, font);
		textfield.setMessageText("Click here!");
		textfield.setAlignment(Align.center);
		final TypingSelectBox selectBox = new TypingSelectBox(skin);
		selectBox.setAlignment(Align.right);
		selectBox.getList().setAlignment(Align.right);
		selectBox.getStyle().font = font;
		selectBox.getStyle().listStyle.selection.setRightWidth(10);
		selectBox.getStyle().listStyle.selection.setLeftWidth(20);
		selectBox.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				System.out.println(selectBox.getSelected());
			}
		});
		String[] items = {"[+🤖]Android1", "[+🪟]Windows1 long text in item", "[+🐧]Linux1", "[+🍎]macOS1", "[+🤖]Android2", "[+🪟]Windows2", "[+🐧]Linux2", "[+🍎]macOS2",
				"[+🤖]Android3", "[+🪟]Windows3", "[+🐧]Linux3", "[+🍎]macOS3", "[+🤖]Android4", "[+🪟]Windows4", "[+🐧]Linux4", "[+🍎]macOS4", "[+🤖]Android5", "[+🪟]Windows5", "[+🐧]Linux5",
				"[+🍎]macOS5", "[+🤖]Android6", "[+🪟]Windows6", "[+🐧]Linux6", "[+🍎]macOS6", "[+🤖]Android7", "[+🪟]Windows7", "[+🐧]Linux7", "[+🍎]macOS7"};
		selectBox.setItemTexts(items);
		selectBox.setSelectedIndex(20);
		Image imageActor = new Image(image2);
		ScrollPane scrollPane = new ScrollPane(imageActor);
		TypingListBox<TypingLabel> list = new TypingListBox<>(skin);
		TypingLabel[] listEntryLabels = new TypingLabel[listEntries.length];
		for (int i = 0; i < listEntries.length; i++) {
			listEntryLabels[i] = new TypingLabel(listEntries[i], font);
		}
		list.setItems(listEntryLabels);
		list.getSelection().setMultiple(true);
		list.getSelection().setRequired(false);
		// list.getSelection().setToggle(true);
		ScrollPane scrollPane2 = new ScrollPane(list, skin);
		scrollPane2.setFlickScroll(false);
		TypingLabel minSizeLabel = new TypingLabel("[@Medieval]ginWidth cell", skin, font); // demos SplitPane respecting widget's minWidth
		Table rightSideTable = new Table(skin);
		rightSideTable.add(minSizeLabel).growX().row();
		rightSideTable.add(scrollPane2).grow();
		SplitPane splitPane = new SplitPane(scrollPane, rightSideTable, false, skin, "default-horizontal");
		fpsLabel = new TypingLabel("fps: 0    [^][SKY][[citation needed]", skin, font);
		fpsLabel.setAlignment(Align.center);
		// configures an example of a TextField in password mode.
		final TypingLabel passwordLabel = new TypingLabel("[@Medieval]Textfield in [~]secure[ ] password mode: ", skin, font);
		final TextField passwordTextField = new TextField("", skin);
		passwordTextField.setMessageText("password");
		passwordTextField.setPasswordCharacter('*');
		passwordTextField.setPasswordMode(true);

		buttonMulti.addListener(new TypingTooltip(
			"This is a tooltip! [_]This is a tooltip! [~]This is a tooltip! [/]This is a tooltip! [_]This is a tooltip! [~]This is a tooltip!",
			skin, font));
		Table tooltipTable = new Table(skin);
		tooltipTable.pad(10).background("default-round");
		tooltipTable.add(new TextraButton("Fancy tooltip!", skin, font));
		imgButton.addListener(new Tooltip<>(tooltipTable));

		// window.debug();
		window = new TypingWindow("TypingWindow", skin, "default", new Font(font).scaleHeightTo(16), false);
		window.getTitleTable().add(new TextraButton("X", skin, window.getTitleLabel().getFont())).height(window.getPadTop());
		window.getTitleTable().add(new TypingButton("X", skin, window.getTitleLabel().getFont())).height(window.getPadTop());
		window.getTitleTable().add(new TextButton("X", skin)).height(window.getPadTop());
		window.setPosition(0, 0);
		window.defaults().spaceBottom(10);
		window.row().fill().expandX();
		window.add(iconButton);

		window.add(buttonMulti);
//		window.add(fancyLabel).width(403);

		window.add(imgButton);
		window.add(imgToggleButton);
		window.row();
		window.add(checkBox);
		window.add(slider).minWidth(100).fillX().colspan(3);
		window.row();
		window.add(selectBox).maxWidth(300);
		window.add(textfield).minWidth(100).expandX().fillX().colspan(3);
		window.row();
		window.add(splitPane).fill().expand().colspan(4).maxHeight(200);
		window.row();
		window.add(passwordLabel).align(Align.topLeft).colspan(2);
		window.add(passwordTextField).minWidth(100).expandX().fillX().colspan(2);
		window.row();
		window.add(fpsLabel).align(Align.topLeft).colspan(4);
		window.pack();

		// stage.addActor(new Button("Behind Window", skin));
		stage.addActor(window);

		textfield.setTextFieldListener(new TextraField.TextFieldListener() {
			public void keyTyped (TextraField textField, char key) {
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
//		profiler.reset();
		ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1);
		String s = String.valueOf(Gdx.graphics.getFramesPerSecond());
		int i;
		for (i = 0; i < s.length() && i < 5; i++) {
			fpsLabel.setInWorkingLayout(5+i, s.charAt(i) | 0xFFFFFFFF00000000L);
		}
		for (; i < 5; i++) {
			fpsLabel.setInWorkingLayout(5+i, 0L);
		}
		fpsLabel.setRotation(20f + 20f * MathUtils.sinDeg((TimeUtils.millis() & 0xFFFFFL) * 0.1f));
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
//		if(Gdx.input.isKeyJustPressed(Keys.SPACE) && profiler.isEnabled())
//			System.out.println("Calls: "+ profiler.getCalls() +
//                ", draw calls: "+ profiler.getDrawCalls() +
//                ", shader switches: "+ profiler.getShaderSwitches() +
//                ", texture bindings: "+ profiler.getTextureBindings());
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
		font.resizeDistanceField(width, height, stage.getViewport());
		window.getTitleLabel().getFont().resizeDistanceField(width, height, stage.getViewport());
	}

	@Override
	public void dispose () {
		stage.dispose();
		skin.dispose();
		texture1.dispose();
		texture2.dispose();
	}
}
