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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class DialogTest extends ApplicationAdapter {
    ScreenViewport viewport;
    Stage stage;
    TextraDialog dialog;
    @Override
    public void create() {
        viewport = new ScreenViewport();
        viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),true);
        stage = new Stage(viewport);
        Font gentium = KnownFonts.getGentium();
        Window.WindowStyle style = new Window.WindowStyle();
        style.background = new TextureRegionDrawable(gentium.mapping.get(gentium.solidBlock)).tint(Color.MAROON);
        dialog = new TextraDialog("", style, gentium);
        stage.setDebugAll(true);

        dialog.getButtonTable().clear();
        dialog.getContentTable().clear();
        dialog.clearListeners();
        TextraButton ok = new TextraButton("OK", new TextButton.TextButtonStyle(), gentium);
        ok.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                dialog.hide();
            }
        });
        ok.setVisible(false);
        TypingLabel L = new TypingLabel(
                "{GRADIENT=CYAN;WHITE;1;1}Come on... The Magical Mystery Tour!{ENDGRADIENT}\n" +
                        "The magical mystery tour:\nIs coming\nTo take you away\nDying to take you away!\nTake you,\nToday...",
                gentium);
        L.setWrap(true);
        L.setTypingListener(new TypingAdapter() {
            @Override
            public void end() {
                ok.setVisible(true);
            }
        });
        dialog.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                L.skipToTheEnd();
                super.clicked(event, x, y);
            }
        });
        dialog.getButtonTable().add(ok).width(240f);
        dialog.getContentTable().add(L).width(250f);
        dialog.setKeepWithinStage(true);
        dialog.show(stage);

        Table table = new Table();
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
        config.setTitle("TT Dialog test");
        config.setWindowedMode(1000, 800);
        config.disableAudio(true);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new DialogTest(), config);
    }

}
