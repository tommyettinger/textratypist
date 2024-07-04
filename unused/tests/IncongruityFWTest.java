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
/*
For font: knownFonts/fontwriter/A-Starry-msdf.json
Constructor took 186 ms
addEmoji took 383 ms
BitmapFontSupport.loadStructuredJson took 103 ms
In total, knownFonts/fontwriter/A-Starry-msdf.json took 672 ms
For font: knownFonts/fontwriter/A-Starry-sdf.json
Constructor took 121 ms
addEmoji took 8 ms
BitmapFontSupport.loadStructuredJson took 84 ms
In total, knownFonts/fontwriter/A-Starry-sdf.json took 213 ms
For font: knownFonts/fontwriter/A-Starry-standard.json
Constructor took 94 ms
addEmoji took 10 ms
BitmapFontSupport.loadStructuredJson took 59 ms
In total, knownFonts/fontwriter/A-Starry-standard.json took 163 ms
For font: knownFonts/fontwriter/Bitter-msdf.json
Constructor took 286 ms
addEmoji took 9 ms
BitmapFontSupport.loadStructuredJson took 153 ms
In total, knownFonts/fontwriter/Bitter-msdf.json took 448 ms
For font: knownFonts/fontwriter/Bitter-sdf.json
Constructor took 180 ms
addEmoji took 12 ms
BitmapFontSupport.loadStructuredJson took 147 ms
In total, knownFonts/fontwriter/Bitter-sdf.json took 339 ms
For font: knownFonts/fontwriter/Bitter-standard.json
Constructor took 124 ms
addEmoji took 8 ms
BitmapFontSupport.loadStructuredJson took 119 ms
In total, knownFonts/fontwriter/Bitter-standard.json took 251 ms
For font: knownFonts/fontwriter/Canada1500-msdf.json
Constructor took 194 ms
addEmoji took 7 ms
BitmapFontSupport.loadStructuredJson took 98 ms
In total, knownFonts/fontwriter/Canada1500-msdf.json took 300 ms
For font: knownFonts/fontwriter/Canada1500-sdf.json
Constructor took 206 ms
addEmoji took 9 ms
BitmapFontSupport.loadStructuredJson took 155 ms
In total, knownFonts/fontwriter/Canada1500-sdf.json took 370 ms
For font: knownFonts/fontwriter/Canada1500-standard.json
Constructor took 150 ms
addEmoji took 7 ms
BitmapFontSupport.loadStructuredJson took 193 ms
In total, knownFonts/fontwriter/Canada1500-standard.json took 351 ms
For font: knownFonts/fontwriter/Cascadia-Mono-msdf.json
Constructor took 295 ms
addEmoji took 6 ms
BitmapFontSupport.loadStructuredJson took 110 ms
In total, knownFonts/fontwriter/Cascadia-Mono-msdf.json took 411 ms
For font: knownFonts/fontwriter/Cascadia-Mono-sdf.json
Constructor took 170 ms
addEmoji took 36 ms
BitmapFontSupport.loadStructuredJson took 200 ms
In total, knownFonts/fontwriter/Cascadia-Mono-sdf.json took 406 ms
For font: knownFonts/fontwriter/Cascadia-Mono-standard.json
Constructor took 245 ms
addEmoji took 21 ms
BitmapFontSupport.loadStructuredJson took 173 ms
In total, knownFonts/fontwriter/Cascadia-Mono-standard.json took 439 ms
For font: knownFonts/fontwriter/Caveat-msdf.json
Constructor took 200 ms
addEmoji took 33 ms
BitmapFontSupport.loadStructuredJson took 172 ms
In total, knownFonts/fontwriter/Caveat-msdf.json took 405 ms
For font: knownFonts/fontwriter/Caveat-sdf.json
Constructor took 187 ms
addEmoji took 6 ms
BitmapFontSupport.loadStructuredJson took 112 ms
In total, knownFonts/fontwriter/Caveat-sdf.json took 305 ms
For font: knownFonts/fontwriter/Caveat-standard.json
Constructor took 132 ms
addEmoji took 26 ms
BitmapFontSupport.loadStructuredJson took 212 ms
In total, knownFonts/fontwriter/Caveat-standard.json took 371 ms
For font: knownFonts/fontwriter/DejaVu-Sans-Condensed-msdf.json
Constructor took 332 ms
addEmoji took 4 ms
BitmapFontSupport.loadStructuredJson took 302 ms
In total, knownFonts/fontwriter/DejaVu-Sans-Condensed-msdf.json took 638 ms
For font: knownFonts/fontwriter/DejaVu-Sans-Condensed-sdf.json
Constructor took 174 ms
addEmoji took 2 ms
BitmapFontSupport.loadStructuredJson took 234 ms
In total, knownFonts/fontwriter/DejaVu-Sans-Condensed-sdf.json took 410 ms
For font: knownFonts/fontwriter/DejaVu-Sans-Condensed-standard.json
Constructor took 277 ms
addEmoji took 4 ms
BitmapFontSupport.loadStructuredJson took 424 ms
In total, knownFonts/fontwriter/DejaVu-Sans-Condensed-standard.json took 705 ms
For font: knownFonts/fontwriter/DejaVu-Sans-Mono-msdf.json
Constructor took 180 ms
addEmoji took 2 ms
BitmapFontSupport.loadStructuredJson took 149 ms
In total, knownFonts/fontwriter/DejaVu-Sans-Mono-msdf.json took 331 ms
For font: knownFonts/fontwriter/DejaVu-Sans-Mono-sdf.json
Constructor took 289 ms
addEmoji took 3 ms
BitmapFontSupport.loadStructuredJson took 167 ms
In total, knownFonts/fontwriter/DejaVu-Sans-Mono-sdf.json took 459 ms
For font: knownFonts/fontwriter/DejaVu-Sans-Mono-standard.json
Constructor took 222 ms
addEmoji took 4 ms
BitmapFontSupport.loadStructuredJson took 157 ms
In total, knownFonts/fontwriter/DejaVu-Sans-Mono-standard.json took 383 ms
For font: knownFonts/fontwriter/DejaVu-Sans-msdf.json
Constructor took 821 ms
addEmoji took 11 ms
BitmapFontSupport.loadStructuredJson took 344 ms
In total, knownFonts/fontwriter/DejaVu-Sans-msdf.json took 1177 ms
For font: knownFonts/fontwriter/DejaVu-Sans-sdf.json
Constructor took 287 ms
addEmoji took 6 ms
BitmapFontSupport.loadStructuredJson took 302 ms
In total, knownFonts/fontwriter/DejaVu-Sans-sdf.json took 596 ms
For font: knownFonts/fontwriter/DejaVu-Sans-standard.json
Constructor took 354 ms
addEmoji took 7 ms
BitmapFontSupport.loadStructuredJson took 256 ms
In total, knownFonts/fontwriter/DejaVu-Sans-standard.json took 617 ms
For font: knownFonts/fontwriter/Gentium-msdf.json
Constructor took 584 ms
addEmoji took 3 ms
BitmapFontSupport.loadStructuredJson took 155 ms
In total, knownFonts/fontwriter/Gentium-msdf.json took 742 ms
For font: knownFonts/fontwriter/Gentium-sdf.json
Constructor took 194 ms
addEmoji took 4 ms
BitmapFontSupport.loadStructuredJson took 89 ms
In total, knownFonts/fontwriter/Gentium-sdf.json took 287 ms
For font: knownFonts/fontwriter/Gentium-standard.json
Constructor took 91 ms
addEmoji took 5 ms
BitmapFontSupport.loadStructuredJson took 89 ms
In total, knownFonts/fontwriter/Gentium-standard.json took 186 ms
For font: knownFonts/fontwriter/Now-Alt-msdf.json
Constructor took 179 ms
addEmoji took 4 ms
BitmapFontSupport.loadStructuredJson took 87 ms
In total, knownFonts/fontwriter/Now-Alt-msdf.json took 270 ms
For font: knownFonts/fontwriter/Now-Alt-sdf.json
Constructor took 102 ms
addEmoji took 2 ms
BitmapFontSupport.loadStructuredJson took 43 ms
In total, knownFonts/fontwriter/Now-Alt-sdf.json took 148 ms
For font: knownFonts/fontwriter/Now-Alt-standard.json
Constructor took 32 ms
addEmoji took 1 ms
BitmapFontSupport.loadStructuredJson took 30 ms
In total, knownFonts/fontwriter/Now-Alt-standard.json took 64 ms
For font: knownFonts/fontwriter/Yanone-Kaffeesatz-msdf.json
Constructor took 310 ms
addEmoji took 4 ms
BitmapFontSupport.loadStructuredJson took 64 ms
In total, knownFonts/fontwriter/Yanone-Kaffeesatz-msdf.json took 378 ms
For font: knownFonts/fontwriter/Yanone-Kaffeesatz-sdf.json
Constructor took 201 ms
addEmoji took 3 ms
BitmapFontSupport.loadStructuredJson took 42 ms
In total, knownFonts/fontwriter/Yanone-Kaffeesatz-sdf.json took 247 ms
For font: knownFonts/fontwriter/Yanone-Kaffeesatz-standard.json
Constructor took 206 ms
addEmoji took 7 ms
BitmapFontSupport.loadStructuredJson took 86 ms
In total, knownFonts/fontwriter/Yanone-Kaffeesatz-standard.json took 299 ms
For font: knownFonts/fontwriter/Yataghan-msdf.json
Constructor took 793 ms
addEmoji took 12 ms
BitmapFontSupport.loadStructuredJson took 413 ms
In total, knownFonts/fontwriter/Yataghan-msdf.json took 1218 ms
For font: knownFonts/fontwriter/Yataghan-sdf.json
Constructor took 268 ms
addEmoji took 7 ms
BitmapFontSupport.loadStructuredJson took 94 ms
In total, knownFonts/fontwriter/Yataghan-sdf.json took 369 ms
For font: knownFonts/fontwriter/Yataghan-standard.json
Constructor took 74 ms
addEmoji took 7 ms
BitmapFontSupport.loadStructuredJson took 70 ms
In total, knownFonts/fontwriter/Yataghan-standard.json took 151 ms
 */
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
            System.out.println("For font: " + jsonFiles[i]);
            long startTime = System.currentTimeMillis(), currentTime = startTime, totalStartTime = startTime;
            fonts[i] = new Font(jsonFiles[i].path(), true);
            System.out.println("Constructor took " + (-(startTime = currentTime) + (currentTime = System.currentTimeMillis())) + " ms");
            KnownFonts.addEmoji(fonts[i]);
            System.out.println("addEmoji took " + (-(startTime = currentTime) + (currentTime = System.currentTimeMillis())) + " ms");
            fonts[i].scaleHeightTo(20.00f);
            if(fonts[i].distanceField == Font.DistanceFieldType.STANDARD)
                bitmapFonts[i] = BitmapFontSupport.loadStructuredJson(jsonFiles[i], jsonFiles[i].nameWithoutExtension() + ".png");
            else
                bitmapFonts[i] = BitmapFontSupport.loadStructuredJson(jsonFiles[i].sibling(jsonFiles[i].name().replaceAll("-[a-z]+\\.json", "-standard.json")), jsonFiles[i].name().replaceAll("-[a-z]+\\.json", "-standard.png"));
            System.out.println("BitmapFontSupport.loadStructuredJson took " + (-(startTime = currentTime) + (currentTime = System.currentTimeMillis())) + " ms");
            bitmapFonts[i].setUseIntegerPositions(false);
            bitmapFonts[i].getData().setScale(20.00f / bitmapFonts[i].getLineHeight());
            System.out.println("In total, " + jsonFiles[i] + " took " + (System.currentTimeMillis() - totalStartTime) + " ms");
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
        for(Font f : fonts)
            f.resizeDistanceField(width, height);
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