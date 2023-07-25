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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.tommyettinger.textra.utils.StringUtils;

import static com.badlogic.gdx.utils.Align.center;

public class SetTextTest extends ApplicationAdapter {
    ScreenViewport viewport;
    Stage stage;
    String text, textra;
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

        Font roboto = KnownFonts.getRobotoCondensed();

        text = "Satchmo is a {RAINBOW}cat{RESET}, who is extremely {SPEED=0.05}fat{NORMAL}; when he sits " +
                "{SHAKE}down{RESET}, throughout the town, we all {WAVE}think{RESET}, 'What was that? Did it happen " +
                "again (that [*]thunderous[*] din)? What could ever make, such a [_]powerful[_] quake, but " +
                "a cat with a [~][_]double[_][~] chin?'";
//                "[*]Локус[*] [*]контроля[*] - свойство " +
//                "личности приписывать " +
//                "свои неудачи и успехи " +
//                "либо внешним факторам " +
//                "(погода, везение, другие " +
//                "люди, судьба-злодейка), " +
//                "либо внутренним (я сам, " +
//                "моё отношение, мои" +
//                "действия)";
        textra = text.replaceAll("\\{[^}]*}", "");
        typingLabel = new TypingLabel(
                "", new Label.LabelStyle(), roboto);
        typingLabel.setWrap(true);
        typingLabel.setAlignment(center);
        typingLabel.setMaxLines(2);
        typingLabel.setEllipsis("...");
        typingLabel.setText(text);
        typingLabel.skipToTheEnd();
        textraLabel = new TextraLabel(
                "[RED]" + textra, new Label.LabelStyle(), roboto);
        textraLabel.setWrap(true);
        textraLabel.setAlignment(center);
        textraLabel.layout.setMaxLines(2);
        textraLabel.layout.setEllipsis("...");
        textraLabel.skipToTheEnd();
        Stack stack = new Stack(textraLabel, typingLabel);
        stack.setFillParent(true);
        stack.pack();
        stage.addActor(stack);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.DARK_GRAY);

        stage.act();
        stage.draw();
//        System.out.println("!!!  On frame #" + ctr);

        random.setSeed(++ctr);
        if ((ctr & 511) == 0) {
            System.out.println("typingLabel has " + typingLabel.getMaxLines() + " max lines and " + typingLabel.getEllipsis() + " ellipsis.");
            text = StringUtils.shuffleWords(text, random);
            textra = text.replaceAll("\\{[^}]*}", "");
//            typingLabel.activeEffects.clear();
//            typingLabel.tokenEntries.clear();
            typingLabel.setText(text); // broken regarding effects...
            typingLabel.parseTokens(); // this is needed when using setText().
//            typingLabel.restart(text); // this works on its own.
//            typingLabel.skipToTheEnd();
            textraLabel.setText("[RED]" + textra);
            System.out.println(typingLabel.layout);
            System.out.println(typingLabel);
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
		config.setForegroundFPS(60);
//		config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new SetTextTest(), config);
    }

}