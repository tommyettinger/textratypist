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
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.tommyettinger.textra.utils.StringUtils;

import static com.badlogic.gdx.utils.Align.center;

public class Issue6Test extends ApplicationAdapter {
    ScreenViewport viewport;
    Stage stage;
    String text;
    TextraLabel textraLabel;
    TypingLabel typingLabel;
    RandomXS128 random;
    long ctr = 1;
    @Override
    public void create() {
        viewport = new ScreenViewport();
        viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),true);
        stage = new Stage(viewport);
        stage.setDebugAll(true);
        random = new RandomXS128(ctr);

        Font gentium = KnownFonts.getGentium();

        text =
                "[*]Локус[*] [*]контроля[*] - свойство " +
                "личности приписывать " +
                "свои неудачи и успехи " +
                "либо внешним факторам " +
                "(погода, везение, другие " +
                "люди, судьба-злодейка), " +
                "либо внутренним (я сам, " +
                "моё отношение, мои" +
                "действия)";
        typingLabel = new TypingLabel(
                text, new Label.LabelStyle(), gentium);
        typingLabel.setWrap(true);
        typingLabel.skipToTheEnd();
        typingLabel.setAlignment(center);
        typingLabel.setMaxLines(3);
        typingLabel.setEllipsis("...");
        textraLabel = new TextraLabel(
                "[RED]" + text, new Label.LabelStyle(), gentium);
        textraLabel.setWrap(true);
        textraLabel.skipToTheEnd();
        textraLabel.setAlignment(center);

        Stack stack = new Stack(textraLabel, typingLabel);
        stack.setFillParent(true);
        stage.addActor(stack);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.DARK_GRAY);

        stage.act();
        stage.draw();

        random.setSeed(++ctr);
        if ((ctr & 3) == 0) {
            System.out.println("typingLabel has " + typingLabel.getMaxLines() + " max lines and " + typingLabel.getEllipsis() + " ellipsis.");
            text = StringUtils.shuffleWords(text, random);
            typingLabel.setText(text);
            typingLabel.skipToTheEnd();
            textraLabel.setText("[RED]" + text);
        }
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
		config.setForegroundFPS(2);
//		config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new Issue6Test(), config);
    }

}