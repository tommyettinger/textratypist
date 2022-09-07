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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;

import static com.badlogic.gdx.utils.Align.topLeft;

public class AntiAliasingTest extends ApplicationAdapter {
    Stage stage;

    @Override
    public void create() {
        stage = new Stage();

        String content = "libGDX is a free and open-source game-development application framework written in the " +
                "Java programming language with some C and C++ components for performance dependent code.";

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("mo/Roboto-Light.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 24;
        BitmapFont light = generator.generateFont(parameter);
        light.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        generator.dispose();

        Label.LabelStyle lightRoboto = new Label.LabelStyle(light, Color.WHITE);
        lightRoboto.font.getData().setLineHeight(30);

        TextraLabel textra = new TextraLabel(content, lightRoboto);
        textra.setWrap(true);
        textra.setAlignment(topLeft);
        textra.getFont().adjustLineHeight(1.3f);
        textra.getFont().useIntegerPositions(true);

        Label label = new Label(content, lightRoboto);
        label.setAlignment(topLeft);
        label.setWrap(true);

        Table table = new Table();
        table.setFillParent(true);
        // trying to figure out what offsets might cause AA
//        table.padTop(0.35f).padLeft(0.35f);

        table.add(textra).width(Gdx.graphics.getWidth() - 40).top().padBottom(20).row();
        table.add(label).width(Gdx.graphics.getWidth() - 40).top().padBottom(20).row();

        stage.addActor(table);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1f);

        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
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
        config.setTitle("TextraLabel vs. Label test");
        config.setWindowedMode(501, 497);
        config.disableAudio(true);
		config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new AntiAliasingTest(), config);
    }

}