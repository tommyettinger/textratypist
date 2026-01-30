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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class FreeTypeIncrementalTest extends ApplicationAdapter {
    FitViewport viewport;
    SpriteBatch batch;
    GlyphLayout layout;
    BitmapFont messageBoxFont, menuDesc, menuFont, menuLabel, titleLabel, battleLabel;

    @Override
    public void create() {
        viewport = new FitViewport(600,480);
        viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),true);
        batch = new SpriteBatch();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("NotoSansSC-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 42;
        parameter.incremental = true;
        messageBoxFont = generator.generateFont(parameter);


        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 42;
        parameter.incremental = true;
        menuDesc = generator.generateFont(parameter);

        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 42;
        parameter.incremental = true;
        menuFont = generator.generateFont(parameter);
        menuFont.getData().setLineHeight(50f);



        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 42;
        parameter.incremental = true;
        menuLabel = generator.generateFont(parameter);

        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 42;
        parameter.incremental = true;
        titleLabel = generator.generateFont(parameter);

        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 42;
        parameter.incremental = true;
        battleLabel = generator.generateFont(parameter);
        // When the next line is uncommented, you get this exception:
//        generator.dispose();
        // Exception in thread "main" com.badlogic.gdx.utils.GdxRuntimeException: Couldn't set size for font

        layout = new GlyphLayout();
        layout.setText(messageBoxFont, "新游戏");
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.DARK_GRAY);

        viewport.apply();
        batch.begin();
        messageBoxFont.draw(batch, layout, 100, 100);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
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
        config.setTitle("FreeType Incremental test");
        config.setWindowedMode(600, 480);
        config.disableAudio(true);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL32, 3, 2);
        new Lwjgl3Application(new FreeTypeIncrementalTest(), config);
    }

}