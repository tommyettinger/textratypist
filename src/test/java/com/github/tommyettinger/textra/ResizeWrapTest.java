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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Thanks to Discord user mas omenos for contributing this test. It shows a challenging issue that should be mostly
 * fixed now: Changing the Font on a TextraLabel didn't automatically update the sizing, wrapping, and layout.
 */
public class ResizeWrapTest extends ApplicationAdapter {

    private Label labScale;
    private Label labFont;
    private TypingLabel typingLabelScaleFont;
    private TypingLabel typingLabelChangeFont;
    private BitmapFont font28;
    private BitmapFont font20;
    private Label.LabelStyle style28;
    private Label.LabelStyle style20;
    private Table outer;
    private Stage stage;

    public float getWidth() {
        return Gdx.graphics.getWidth();
    }
    public void create(){
        stage = new Stage();
        outer = new Table();
        outer.setWidth(getWidth() - 40);

        generateFontAndStyle();

        labScale = new Label("FONT SCALE", style20);
        labScale.setAlignment(Align.right);
        labScale.addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                super.touchDown(event, x, y, pointer, button);
                changeFontScale(button == Input.Buttons.LEFT);
                return true;
            }
        });

        labFont = new Label("CHANGE to Bigger FONT", style20);
        labFont.setAlignment(Align.right);
        labFont.addListener(new ClickListener(){
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                changeFont();
            }
        });
        typingLabelScaleFont = new TypingLabel("libGDX is a free and open-source game-development application framework written in the Java programming language.\n\ntextraLabelScale.font.scale(1.5f, 1.5f);\n" +
                "textraLabelScale.pack();", style20);
//        textraLabelScaleFont = new TypingLabel("Hero ice-clone Hero ice-clone Hero ice-clone Hero ice-clone Hero ice-clone Hero ice-clone Hero ice-clone"
//                "{SHAKE}libGDX is a free and open-source game-development application framework written in the Java programming language."
//                +"\n\ntextraLabelScale.font.scale(1.5f, 1.5f);\n" +
//                "textraLabelScale.pack();"
//                , style20);
        typingLabelScaleFont.setWrap(true);

        typingLabelChangeFont = new TypingLabel("libGDX is a free and open-source game-development application framework written in the Java programming language.\n\nFont font = new Font(font28);\n" +
                "textraLabelFont.font = font;\ntextraLabelFont.pack();", style20);
        typingLabelChangeFont.setWrap(true);

        outer.add(labScale).right().width(getWidth() - 40).padBottom(20).row();
        outer.add(typingLabelScaleFont).width(getWidth() - 40).padBottom(80).row();
        outer.add(labFont).right().width(getWidth() - 40).padBottom(20).row();
        outer.add(typingLabelChangeFont).width(getWidth() - 40).padBottom(20).row();

        outer.padTop(20);
        outer.top();
        outer.debugAll();
        outer.setFillParent(true);
        stage.addActor(outer);
        Gdx.input.setInputProcessor(stage);
    }

    public void changeFontScale(boolean up){
        if(up)
            typingLabelScaleFont.getFont().scale(1.25f, 1.25f);
        else
            typingLabelScaleFont.getFont().scale(0.8f, 0.8f);
//         used to reproduce a word-wrap and line-break bug with TypingLabel.offsets , which is used by effects.
//        typingLabelScaleFont.setText("{SICK}Hero ice-clone Hero ice-clone Hero ice-clone Hero ice-clone Hero ice-clone Hero ice-clone Hero ice-clone");
        System.out.println("changeFontScale to " + typingLabelScaleFont.getFont().scaleY);
        typingLabelScaleFont.pack();
    }

    public void changeFont(){
        System.out.println("changeFont");
        Font font = new Font(font28);
        typingLabelChangeFont.setFont(font);
        typingLabelChangeFont.pack();

        // debugging info.
//        float widest = textraLabelChangeFont.layout.getWidth();
//        System.out.println("Layout thinks it is " + widest + " wide.");
//        System.out.println("The label is " + outer.getWidth() + " wide.");
    }

    public void dispose() {
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, false);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);
        stage.act();
        stage.draw();
    }

    public void generateFontAndStyle(){
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("mo/Roboto-Light.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 28;
        font28 = generator.generateFont(parameter); // font size 12 pixels
        font28.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        generator.dispose(); // don't forget to dispose to avoid memory leaks!

        generator = new FreeTypeFontGenerator(Gdx.files.internal("mo/Roboto-Light.ttf"));
        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 20;
        font20 = generator.generateFont(parameter); // font size 12 pixels
        font20.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        generator.dispose(); // don't forget to dispose to avoid memory leaks!

        style28 = new Label.LabelStyle();
        style28.font = font28;
        style28.fontColor = Color.WHITE;

        style20 = new Label.LabelStyle();
        style20.font = font20;
        style20.fontColor = Color.WHITE;
    }
    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("TextraLabel Resizing and Font Change Test");
        config.setWindowedMode(800, 800);
        config.disableAudio(true);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new ResizeWrapTest(), config);
    }


}