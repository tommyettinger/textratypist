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
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;

public class IncongruityFWTest extends ApplicationAdapter {
    Stage stage;
    Font[] fonts;
    @Override
    public void create() {
        stage = new Stage();
        Skin skin = new FreeTypistSkin(Gdx.files.internal("uiskin2.json"));
        Table root = new Table(skin);

        FileHandle[] jsonFiles = Gdx.files.local("knownFonts/fontwriter").list(".json");
        fonts = new Font[jsonFiles.length];
        BitmapFont[] bitmapFonts = new BitmapFont[jsonFiles.length];
        for (int i = 0; i < jsonFiles.length; i++) {
            fonts[i] = KnownFonts.addEmoji(new Font(jsonFiles[i].path(), true));
            fonts[i].scaleHeightTo(20.00f);
            if(fonts[i].distanceField == Font.DistanceFieldType.STANDARD)
                bitmapFonts[i] = BitmapFontSupport.loadStructuredJson(jsonFiles[i], jsonFiles[i].nameWithoutExtension() + ".png");
            else
                bitmapFonts[i] = BitmapFontSupport.loadStructuredJson(jsonFiles[i].sibling(jsonFiles[i].name().replaceAll("-[a-z]+\\.json", "-standard.json")), jsonFiles[i].name().replaceAll("-[a-z]+\\.json", "-standard.png"));
            bitmapFonts[i].setUseIntegerPositions(false);
            bitmapFonts[i].getData().setScale(20.00f / bitmapFonts[i].getLineHeight());
        }
        fonts[0].scale(0.5f, 1f);
        fonts[1].scale(0.5f, 1f);
        fonts[2].scale(0.5f, 1f);
        bitmapFonts[0].getData().setScale(0.5f * bitmapFonts[0].getScaleX(), bitmapFonts[0].getScaleY());
        bitmapFonts[1].getData().setScale(0.5f * bitmapFonts[1].getScaleX(), bitmapFonts[1].getScaleY());
        bitmapFonts[2].getData().setScale(0.5f * bitmapFonts[2].getScaleX(), bitmapFonts[2].getScaleY());
        Table labels = new Table();
        labels.defaults().pad(1);
        for (int i = 0; i < fonts.length; i++) {
            Font font = fonts[i];
//            Font font = fonts[i].setDescent(fonts[i].getDescent() * 2);
            labels.add(new Label(font.name, skin)).left();
            TypingLabel label = new TypingLabel("Emoji Text? 3, 2, 1, [+🎉]! [/]hooray...[/]", skin, font);
//            label.align = Align.bottom;
            label.setDefaultToken("{EASE}{FADE=0;1;0.33}");
            labels.add(label).expandX().left();
//            label.validate();
            Gdx.app.log("Font", font.name + (font.isMono ? " (MONO)" : "") + ", " + label.getPrefWidth() + ", " + label.getPrefHeight() + ", " + font.scaleY);

            BitmapFont bf = bitmapFonts[i];
            if (bf != null) {
                Label.LabelStyle style = new Label.LabelStyle();
                style.font = bf;
                style.fontColor = Color.WHITE;
                Label bmLabel = new Label("Emoji Text? 3, 2, 1... nooo... ;(", style);
                bmLabel.validate();
                float scaleY = label.getPrefHeight()/bmLabel.getPrefHeight();
                float scaleX = label.getPrefWidth()/bmLabel.getPrefWidth();
                bmLabel.setFontScale(bf.getScaleX() * Math.min(scaleX, scaleY), bf.getScaleY() * Math.min(scaleX, scaleY));
                Gdx.app.log("BMFont", font.name + ", " + bmLabel.getPrefWidth() + ", " + bmLabel.getPrefHeight()
                        + ", " + scaleX + ", " + scaleY);
                labels.add(bmLabel).expandX().left();
            } else {
                labels.add(new Label("MISSING!", skin)).expandX().left();
            }
//            if((i & 1) == 1)
                labels.row();
        }
        root.setFillParent(true);
        root.add(labels);
        root.pack();
        labels.debugAll();
        stage.addActor(root);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);

        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        for(Font f : fonts) f.resizeDistanceField(width, height);
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
        config.setTitle("TextraLabel Incongruity test");
        config.setWindowedMode(1300, 950);
        config.disableAudio(true);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new IncongruityFWTest(), config);
    }

}