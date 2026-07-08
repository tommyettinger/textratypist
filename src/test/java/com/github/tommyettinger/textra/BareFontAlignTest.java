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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class BareFontAlignTest extends ApplicationAdapter {

    private SpriteBatch batch;
    private Layout gameOver;
    private ScreenViewport viewport;
    private Font font;

    @Override
    public void create () {

        batch = new SpriteBatch();
        viewport = new ScreenViewport();
        font = KnownFonts.getLanaPixel().scale(3);
        gameOver = new Layout(font);
        gameOver.setTargetWidth(Gdx.graphics.getWidth());
        font.markup("[RED]YOUR CRAWL IS OVER!\n" +
                "[GRAY]A monster sniffs your corpse and says,\n"+
                "[FOREST]'Ewww! Like, grody to the max! Gag me with a spoon...'\n" +
                "[GRAY]q to quit.\n[YELLOW]r to restart.", gameOver);
        font.regenerateLayout(gameOver);

    }

    @Override
    public void render () {
        ScreenUtils.clear(0.3f, 0.3f, 0.3f, 1.0f);
        viewport.apply(false);
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        float x = 0f, y = 0f;
        font.drawGlyphs(batch, gameOver, x, y + 2 * font.cellHeight, Align.center);
//            font.draw(batch, "[RED]YOUR CRAWL IS OVER!", x, y + 2 * font.getLineHeight(), wide, Align.center, true);
//            font.draw(batch, "[GRAY]A monster sniffs your corpse and says,", x, y + font.getLineHeight(), wide, Align.center, true);
//            font.draw(batch, "[FOREST]" + lang, x, y, wide, Align.center, true);
//            font.draw(batch, "[GRAY]q to quit.", x, y - 2 * font.getLineHeight(), wide, Align.center, true);
//            font.draw(batch, "[YELLOW]r to restart.", x, y - 4 * font.getLineHeight(), wide, Align.center, true);
        batch.end();

    }

    @Override
    public void dispose () {
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height, false);

    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("State Storage Test");
        config.setWindowedMode(720, 480);
        config.setResizable(true);
        config.setForegroundFPS(300);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new BareFontAlignTest(), config);
    }
}