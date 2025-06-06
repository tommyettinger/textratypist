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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class DialogTest extends ApplicationAdapter {
    ScreenViewport viewport;
    Stage stage;
    TextraWindow dialog;
    @Override
    public void create() {
        viewport = new ScreenViewport();
        viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),true);
        stage = new Stage(viewport);
        Font gentium = KnownFonts.getGentium();
        Styles.WindowStyle style = new Styles.WindowStyle();
        style.titleFont = gentium;
//        style.background = new TextureRegionDrawable(gentium.mapping.get(gentium.solidBlock)).tint(Color.CLEAR);
        style.background = new TextureRegionDrawable(gentium.mapping.get(gentium.solidBlock)).tint(Color.MAROON);
        dialog = new TextraWindow("SING ALONG!", style, gentium);
        stage.setDebugAll(true);
        Table contentTable = new Table();
        dialog.add(contentTable).expandY().row();
        Table buttonTable = new Table();
        dialog.add(buttonTable);

        dialog.clearListeners();
        TextraButton ok = new TextraButton("OK", new Styles.TextButtonStyle(), gentium);
        ok.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.addAction(Actions.fadeOut(1.5f));
            }
        });
//        TextraLabel tl = new TextraLabel(
//                "{GRADIENT=CYAN;WHITE;1;1}Come on... The Magical Mystery Tour!{ENDGRADIENT}\n" +
//                        "The magical mystery tour:\nIs coming\nTo take you away\nDying to take you away!\nTake you,\nToday...",
//                gentium);

        ok.setVisible(false);
        TypingLabel tl = new TypingLabel(
                "[%?blacken]{GRADIENT=CYAN;WHITE;1;1}Come on... The Magical Mystery Tour!{ENDGRADIENT}\n" +
                        "The magical mystery tour:\nIs coming\nTo take you away\nDying to take you away!\nTake you,\nToday...",
//                "{GRADIENT=CYAN;WHITE;1;1}Come on... The Magical Mystery Tour!{ENDGRADIENT}\n" +
//                        "The magical mystery tour:\nIs coming\nTo take you away\nDying to take you away!\nTake you,\nToday...",
                gentium);
        tl.setTypingListener(new TypingAdapter() {
            @Override
            public void end() {
                ok.setVisible(true);
                dialog.pack();
            }
        });
        tl.setWrap(true);

        // This was necessary so the Dialog knew what height the TypingLabel actually was.
        // It should be automatically called by any getPrefHeight() or getPrefWidth() calls if not parsed already; that
        // is, it will be automatically called when its size must actually be known.
//        tl.parseTokens();

        dialog.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                tl.skipToTheEnd();
                super.clicked(event, x, y);
            }
        });

        // This was necessary in versions 7.5 through 7.7, and shouldn't be needed later. It can't hurt, though.
        contentTable.clear();

        buttonTable.add(ok).width(240f);
        contentTable.add(tl).width(250f);
        dialog.setKeepWithinStage(true);
        stage.addActor(dialog);
        dialog.pack();

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);
        System.out.println(dialog);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.GRAY);

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
        config.setTitle("TT Dialog test");
        config.setWindowedMode(1000, 800);
        config.disableAudio(true);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new DialogTest(), config);
    }

}
