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
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ByteArray;
import com.badlogic.gdx.utils.ScreenUtils;
import com.github.tommyettinger.textra.utils.LZBCompression;

public class LZBTest extends ApplicationAdapter {
    Stage stage;
    Skin skin;
    long uncompressedStartTime = 0L;
    long compressedStartTime = 0L;
    String uncompressedText = "";
    String compressedText = "";
    @Override
    public void create() {
        FileHandle uncompressedFile = Gdx.files.local("knownFonts/fontwriter/DejaVu-Sans-Condensed-sdf.json");
        FileHandle compressedFile = Gdx.files.local("knownFonts/fontwriter/DejaVu-Sans-Condensed-sdf.lzb");
        if(!compressedFile.exists()){
            ByteArray ba = LZBCompression.compressToByteArray(uncompressedFile.readString("UTF8"));
            compressedFile.writeBytes(ba.items, 0, ba.size, false);
        }
        stage = new Stage();
        skin = new FreeTypistSkin(Gdx.files.internal("uiskin2.json"));
        Table table = new Table();
        table.setFillParent(true);
        TextraLabel uncompressedTime = new TextraLabel("?????????????????????", skin);
        TextraButton uncompressedButton = new TextraButton("Load Uncompressed", skin);
        uncompressedButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                uncompressedStartTime = System.currentTimeMillis();
                uncompressedText = uncompressedFile.readString("UTF8");
                String script = (System.currentTimeMillis() - uncompressedStartTime) + " ms";
                System.out.println(script);
                uncompressedTime.setText(script);
                uncompressedTime.layout();
            }
        });
        TextraLabel compressedTime = new TextraLabel("?????????????????????", skin);
        TextraButton compressedButton = new TextraButton("Load Compressed", skin);
        compressedButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                compressedStartTime = System.currentTimeMillis();
                compressedText = compressedFile.readString("UTF8");
                String script = (System.currentTimeMillis() - compressedStartTime) + " ms";
                System.out.println(script);
                compressedTime.setText(script);
                compressedTime.layout();
            }
        });
        table.add(uncompressedButton);
        table.add(compressedButton).row();
        table.add(uncompressedTime);
        table.add(compressedTime);
        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);
        stage.act();
        stage.draw();
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("LZB Test");
        config.setWindowedMode(720, 480);
        config.setResizable(true);
        config.setForegroundFPS(0);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new LZBTest(), config);
    }
}
