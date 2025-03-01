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
    Font msdf;
    @Override
    public void create() {
        stage = new Stage();
        msdf = KnownFonts.getRobotoCondensed(Font.DistanceFieldType.MSDF).scaleHeightTo(30).multiplyCrispness(0.75f);

        String content = "libGDX is a free and open-source game-development application framework written in the " +
                "Java programming language with some C and C++ components for performance dependent code.";

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("mo/Roboto-Condensed.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 24;
        parameter.hinting = FreeTypeFontGenerator.Hinting.Medium;
        BitmapFont light = generator.generateFont(parameter);
        light.getData().breakChars = new char[]{'-'};
//        light.setUseIntegerPositions(false);
        light.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        generator.dispose();

        Label.LabelStyle lightRoboto = new Label.LabelStyle(light, Color.WHITE);
        lightRoboto.font.getData().setLineHeight(30);

        TextraLabel textra = new TextraLabel("Stretched TextraLabel:\n" + content, new Styles.LabelStyle(lightRoboto));
        textra.setWrap(true);
        textra.setAlignment(topLeft);
        textra.getFont().adjustLineHeight(1.2f);
        textra.getFont().useIntegerPositions(true);

        Label label = new Label("Label:\n" + content, lightRoboto);
        label.setAlignment(topLeft);
        label.setWrap(true);

        TextraLabel textra2 = new TextraLabel("TextraLabel:\n" + content, new Styles.LabelStyle(msdf, Color.WHITE));
        textra2.setWrap(true);
        textra2.setAlignment(topLeft);
        msdf.useIntegerPositions(true);

        Table table = new Table();
        table.setFillParent(true);
        // trying to figure out what offsets might cause AA
//        table.padTop(0.35f).padLeft(0.35f);

        table.add(textra).width(Gdx.graphics.getWidth() * 0.3f - 40).top().pad(20);
        table.add(label).width(Gdx.graphics.getWidth() * 0.3f - 40).top().pad(20);
        table.add(textra2).width(Gdx.graphics.getWidth()  * 0.3f - 40).top().pad(20);

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
        msdf.resizeDistanceField(width, height, stage.getViewport());
    }

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("TextraLabel vs. Label test");
        config.setWindowedMode(909, 497);
        config.disableAudio(true);
		config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new AntiAliasingTest(), config);
    }

}