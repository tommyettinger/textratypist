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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.ray3k.stripe.FreeTypeSkin;

public class TableWrapTest extends ApplicationAdapter {
    Stage stage;

    @Override
    public void create() {
        stage = new Stage();
        Skin skin = new FreeTypistSkin(Gdx.files.internal("uiskinOS.json"));
        Table root = new Table(skin);
        root.setSize(780, 600);

        final Font font = new Font(skin.getFont("outline-font"), 0, -7, 0, 2);
        final Table labels = new Table();
        labels.defaults().pad(5);
        labels.defaults().width(540).growY();
        boolean wr = true;
        final TextraButton button = new TextraButton("Text Size", skin, font);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                final float change = 0.7f + MathUtils.random(0.65f);
                // Changing the Font's scale doesn't automatically resize widgets.
                font.scale(change, change);
                // So we can resize any widgets that are children of "labels" here.
                for(Actor a : labels.getChildren()) {
                    // Note that Button and TextraButton don't extend Widget, but they do implement Layout.
                    if(a instanceof Layout) {
                        // ...Yeah, I guess pack() is the one we need.
                        ((Layout)a).pack();
                    }
                }
                button.pack();
            }
        });
        labels.add(button).height(50).colspan(3).top().row();

        TextraLabel tl = new TypingLabel("lib[RED]GDX[ ] is a free, open-source, warm and toasty game-development application framework " +
                "written in the Java programming language.", skin, font);
        tl.setWrap(wr);
        tl.setAlignment(Align.center);
        labels.add(tl).center().height(400).row();

//        labels.add(new TypingLabel("lib[RED]GDX[] is a free and open-source game-development application framework " +
//                "written in the Java programming language", skin, font).setWrap(wr)).left().height(400).row();


//        labels.add(new TextraLabel("Company", skin, font).setWrap(wr)).left();
//        labels.add(new TextraLabel("Contact", skin, font).setWrap(wr)).left();
//        labels.add(new TextraLabel("Country", skin, font).setWrap(wr)).left().row();
//        labels.add(new TextraLabel("Hapsburg Wursthaus", skin, font).setWrap(wr)).left();
//        labels.add(new TextraLabel("Johannes Durst", skin, font).setWrap(wr)).left();
//        labels.add(new TextraLabel("Germany", skin, font).setWrap(wr)).left().row();
//        labels.add(new TextraLabel("Centro Comercial Liberdad", skin, font).setWrap(wr)).left();
//        labels.add(new TextraLabel("Guadalupe Vasquez", skin, font).setWrap(wr)).left();
//        labels.add(new TextraLabel("Mexico", skin, font).setWrap(wr)).left().row();
        root.setFillParent(true);
        root.add(labels);
        labels.debugAll();
        stage.addActor(root);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);

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
        config.setTitle("TextraLabel Table Wrap test");
        config.setWindowedMode(780, 600);
        config.disableAudio(true);
		config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new TableWrapTest(), config);
    }

}