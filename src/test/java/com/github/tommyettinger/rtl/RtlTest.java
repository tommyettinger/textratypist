/*
 * *****************************************************************************
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
package com.github.tommyettinger.rtl;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.CharArray;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.crowni.gdx.rtllang.support.ArFont;
import com.crowni.gdx.rtllang.support.ArUtils;

/**
 * Created by Crowni on 9/14/2017.
 **/
public class RtlTest extends ApplicationAdapter {

    public static final int WIDTH = 1024;
    public static final int HEIGHT = 534;

    private static final String TAG = "RtlTest";

    private static final String ARABIC_LANGUAGE = "اللغة العربية Arabic Languages ";
    private static final String INSERT_YOUR_NAME = "أدخل إسمك Insert Your Name: ";
    private static final String PERSIAN_LANGUAGE = "این متن جهت تست گچپژ می‌باشد.";
    private static final String PERSIAN_COMPLEX_TEXT = "اتوبوس ۱۲ نفره Bus است!";

    private final ArFont arFont = new ArFont();

    private Stage stage;

    @Override
    public void create() {
        stage = new Stage(new ExtendViewport(WIDTH, HEIGHT));

        Image image = new Image(new Texture("wallpaper.png"));
        image.setFillParent(true);
        stage.addActor(image);

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("NotoSansArabic-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
//        CharArray arChars = ArUtils.getAllChars();
//        System.out.print("\"");
//        for (int i = 0, n = arChars.size; i < n; i++) {
//            int c = arChars.get(i) & 0xFFFF;
//            System.out.printf("\\u%04X", c);
//        }
//        System.out.println("\"");
        parameter.characters += ArUtils.getAllChars();
        parameter.size = 40;
        parameter.color = Color.GOLD;
//        parameter.borderColor = Color.GOLD;
//        parameter.borderWidth = 1.5f;
        parameter.shadowColor = Color.DARK_GRAY;
        parameter.shadowOffsetX = 2;
        parameter.shadowOffsetY = 2;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;

        BitmapFont freeTypeFont = generator.generateFont(parameter);
        Label label = new Label(arFont.getText(ARABIC_LANGUAGE), new Label.LabelStyle(freeTypeFont, Color.WHITE));
        label.layout();
        label.setAlignment(Align.center);
        label.setPosition(512f, 350f, Align.center);
        stage.addActor(label);

        TextArea.TextFieldStyle style = new TextField.TextFieldStyle();
        style.font = freeTypeFont;
        style.fontColor = Color.WHITE;

        TextField textField = new TextField(arFont.getText(INSERT_YOUR_NAME), style);
        textField.setAlignment(Align.right);
        textField.setSize(WIDTH, 100f);
        textField.setPosition(WIDTH - 10f, HEIGHT * 0.5f - 45f, Align.right);
        stage.addActor(textField);

        textField.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                String text = arFont.typing(c);
                textField.setText(text);
            }
        });

        /*
        * Persian test text
         */
        generator = new FreeTypeFontGenerator(Gdx.files.internal("NotoSansArabic-Regular.ttf"));

        BitmapFont freeTypeFont2 = generator.generateFont(parameter);
        Label persianLabel = new Label(arFont.getText(PERSIAN_LANGUAGE) + "\n" + arFont.getText(PERSIAN_COMPLEX_TEXT), new Label.LabelStyle(freeTypeFont2, Color.WHITE));
        persianLabel.layout();
        persianLabel.setAlignment(Align.center);
        persianLabel.setPosition(512f, 440f, Align.center);
        stage.addActor(persianLabel);

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.CLEAR);
        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
    }

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("RTL Text Test");
        config.setWindowedMode(WIDTH, HEIGHT);
        config.disableAudio(true);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new RtlTest(), config);
    }

}
