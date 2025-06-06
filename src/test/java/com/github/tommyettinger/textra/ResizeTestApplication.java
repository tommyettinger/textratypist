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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class ResizeTestApplication extends ApplicationAdapter {

    private SpriteBatch batch;
    private Texture backgroundImage;

    private Stage ui;
    private Table background;

    TextraLabel itemDesc;
    Font nowAlt;

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Resizable Label Test");
//        configuration.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL30, 3, 0);

        configuration.setResizable(true);
        configuration.setWindowedMode(1280, 720);
        configuration.useVsync(true);

        configuration.disableAudio(true);

        new Lwjgl3Application(new ResizeTestApplication(), configuration);
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        ui = new Stage(new ScreenViewport());
//        ui = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        Gdx.input.setInputProcessor(ui);

        //KnownFonts.setAssetPrefix("knownFonts/");
        nowAlt = KnownFonts.getNowAlt(Font.DistanceFieldType.SDF);

        backgroundImage = new Texture(Gdx.files.internal("Among_the_Sierra_Nevada_by_Albert_Bierstadt.jpg"));

        createUI();
    }

    public void createUI() {
        background = new Table();
        background.background(new TextureRegionDrawable(new TextureRegion(backgroundImage)));
        background.setFillParent(true);
        ui.addActor(background);

        TextraLabel instructions = new TextraLabel("Try with/without maximizing the window.\n\nPress [*]G[*] to refresh /*Typing*/TextraLabel's text ", nowAlt);
        background.add(instructions).top().left().row();

        Table description = new Table();

        itemDesc = new /*Typing*/TextraLabel("", nowAlt);
        itemDesc.setWrap(true);
        itemDesc.setDebug(true);
        description.add(itemDesc).padTop(20).growX().left().row();
        description.add(new Table()).expandY().row();

        background.add(new /*Typing*/TextraLabel("|", nowAlt) {

            boolean first = false;

            @Override
            public void act(float delta) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
                    float time = 0.15f;
                    addAction(
                            Actions.sequence(
                                    Actions.parallel(
                                            Actions.moveBy(150, 150, time),
                                            Actions.fadeOut(time)
                                    ),
                                    Actions.run(() -> {
                                        if (!first) {
                                            setText("[%150][*]Bestiary");
                                            first = true;
                                        } else {
                                            first = false;
                                            setText("[%150][*]Inventory");
                                        }
                                        skipToTheEnd();
                                    }),
                                    Actions.parallel(
                                            Actions.moveBy(-150, -150, time),
                                            Actions.fadeIn(time)
                                    )
                            )
                    );
                }
                super.act(delta);
            }
        }).row();

        background.add(description).width(Value.percentWidth(0.3f, background));
    }

    @Override
    public void dispose() {
        Gdx.input.setInputProcessor(null);
        ui.dispose();
        batch.dispose();
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.WHITE);
//        batch.begin();
//        batch.setColor(Color.GRAY);
//        batch.draw(backgroundImage, 0,0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//        batch.setColor(Color.WHITE);
//        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            itemDesc.setText("[DARK-bright-raspberry-magenta-rose-red-ember]"
                    + "So juicy and red! You could probably collect them on a mountain..");
            System.out.println(itemDesc.layout);
            System.out.println("" + itemDesc.getWidth());
        }
        ui.getViewport().apply(true);
        ui.act();
        ui.draw();
    }

    @Override
    public void resize(int width, int height) {
        // initial, has an issue
//        ui = new Stage(new FitViewport(width, height));
//        ui.addActor(background);
        // fixed, maybe, for FitViewport, but not ScreenViewport...
        ui.getViewport().update(width, height, true);
        nowAlt.resizeDistanceField(width, height, ui.getViewport());
    }
}