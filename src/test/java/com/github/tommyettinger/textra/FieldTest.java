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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import static com.badlogic.gdx.utils.Align.center;
import static com.badlogic.gdx.utils.Align.left;

public class FieldTest extends ApplicationAdapter {
    ScreenViewport viewport;
    Stage stage;
    TextraField field;

    @Override
    public void create() {
        viewport = new ScreenViewport();
        viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),true);
        stage = new Stage(viewport);
        stage.setDebugAll(true);

//        BitmapFont bmp = new BitmapFont(Gdx.files.internal("knownFonts/Gentium-standard.fnt"));
//        bmp.getData().setScale(0.5f);
//        bmp.setUseIntegerPositions(false);
//        bmp.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
//        Font font = new Font(bmp, 0f, 10f, 0f, 0f);

        Font font = new Font("RaeleusScriptius-standard.fnt", Font.DistanceFieldType.STANDARD, 0, 8, 0, 0);
//        Font font = KnownFonts.getAStarry().scale(2, 3).useIntegerPositions(true);
        System.out.println("descent: "+font.descent + ", lineHeight: " + font.cellHeight);
        String text = "22";
//                "[*]Локус контроля[*] - свойство " +
//                "личности приписывать " +
//                "свои неудачи и успехи " +
//                "либо внешним факторам " +
//                "(погода, везение, другие " +
//                "люди, судьба-злодейка), " +
//                "либо внутренним (я сам, " +
//                "моё отношение, мои" +
//                "действия)";
        Font.GlyphRegion solid = font.mapping.get(font.solidBlock);
        Drawable pipe = new TextureRegionDrawable(solid),
                selection = new TextureRegionDrawable(solid).tint(Color.GRAY),
                background = new TextureRegionDrawable(solid).tint(Color.NAVY);
        pipe.setMinWidth(2);
        pipe.setMinHeight(font.cellHeight);
        selection.setMinWidth(font.cellWidth);
        selection.setMinHeight(font.cellHeight);
        background.setMinHeight(1);
        background.setMinWidth(1);
        field = new TextraField(text, new TextField.TextFieldStyle(null, Color.WHITE.cpy(), pipe,
                selection, background), font);
        field.setWidth(500);
        field.setPasswordMode(false);
        field.setHeight(font.cellHeight);
        field.setAlignment(left);

        Table table = new Table();
        table.setFillParent(true);
        table.add(field).width(500);
        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.DARK_GRAY);

        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
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
        config.setTitle("TextraLabel UI test");
        config.setWindowedMode(600, 480);
        config.disableAudio(true);
		config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new FieldTest(), config);
    }

}