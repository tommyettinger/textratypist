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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class StateTest extends ApplicationAdapter {

    private Stage stage;
    private Skin skin;

    @Override
    public void create () {
        stage = new Stage(new ScreenViewport());
        stage.setDebugAll(true);
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        StringBuilder text = new StringBuilder();

        String wikipedia = "[(previous)][ ][SKY][^][[citation needed][ previous]";
//        text.append("[brown][/]CAT!").append(wikipedia).append(" [/]Kitty!");
        text.append("Satchmo is a [brown]cat[ ], who is extremely [*]fat[*]").append(wikipedia).append("; when he sits " +
                "[_]down[_], throughout the town, [/]we").append(wikipedia).append(" all think[/], 'What was that? Did it happen " +
                "again (that [*]thunderous[*] din)? What could ever make, such a [_]powerful[_] quake, but " +
                "a [brown]cat[ ] with a [~][_]double[_][~] chin?'");
//// Does work!
//        TextraLabel label = new TextraLabel(text.toString(), skin);
//        label.setWrap(true);

//// Also works!
        TypingLabel label = new TypingLabel(text.toString(), skin);
        label.setWrap(true);
        label.setSelectable(true);
        label.setTypingListener(new TypingAdapter(){
            @Override
            public void event(String event) {
                if("*SELECTED".equals(event)){
                    System.out.println(label.getSelectedText());
                }
            }
        });

        Table table = new Table();
        table.add(label).prefWidth(100).row();

        Stack stack = new Stack(table);
        stack.setFillParent(true);

        stage.addActor(stack);
    }

    @Override
    public void render () {
        ScreenUtils.clear(0, 0, 0, 1);
        stage.act();
        stage.draw();
    }

    @Override
    public void dispose () {
        stage.dispose();
        skin.dispose();
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("State Storage Test");
        config.setWindowedMode(720, 480);
        config.setResizable(true);
        config.setForegroundFPS(0);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new StateTest(), config);
    }
}