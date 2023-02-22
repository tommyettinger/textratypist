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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import static com.badlogic.gdx.utils.Align.center;

public class Issue6Test extends ApplicationAdapter {
    ScreenViewport viewport;
    Stage stage;
    TextraLabel textraLabel;
    TypingLabel typingLabel;

    @Override
    public void create() {
        viewport = new ScreenViewport();
        viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),true);
        stage = new Stage(viewport);
//        stage.setDebugAll(true);

        Font gentium = KnownFonts.getGentium();

        String text = "Effluvium Spattering Towards Congressmen";
//                "[*]Локус контроля[*] - свойство " +
//                        "личности приписывать " +
//                        "свои неудачи и успехи " +
//                        "либо внешним факторам " +
//                        "(погода, везение, другие " +
//                        "люди, судьба-злодейка), " +
//                        "либо внутренним (я сам, " +
//                        "моё отношение, мои" +
//                        "действия)";
        typingLabel = new TypingLabel(
                text, new Label.LabelStyle(), gentium);
        typingLabel.setWrap(true);
        typingLabel.setWidth(25f);
        typingLabel.skipToTheEnd();
        typingLabel.setAlignment(center);
        typingLabel.debug();

        textraLabel = new TextraLabel(
                "[RED]" + text, new Label.LabelStyle(), gentium);
        textraLabel.setWrap(true);
        textraLabel.setWidth(25f);
        textraLabel.skipToTheEnd();
        textraLabel.setAlignment(center);
        Stack stack = new Stack(textraLabel, typingLabel);
//        Stack stack = new Stack(typingLabel);
        Table table = new Table();
        Cell<Actor> stackCell = table.add(stack);
        stackCell.width(25f);
        table.setFillParent(true);
        stage.addActor(table);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.DARK_GRAY);

        stage.act();
        stage.draw();
//        System.out.println(typingLabel.workingLayout.lines());
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
        new Lwjgl3Application(new Issue6Test(), config);
    }

}