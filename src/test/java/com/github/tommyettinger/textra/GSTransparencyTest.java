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
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import static com.badlogic.gdx.utils.Align.*;

public class GSTransparencyTest  extends ApplicationAdapter {
    SpriteBatch batch;
    FitViewport viewport;
    OrthographicCamera camera;
    Stage stage;
    TextraLabel textraLabel1;
    TextureRegion texture;
    Table table;
    Texture bg;

    static final int[] aligns = {left, topLeft, top, topRight, right, bottomRight, bottom, bottomLeft, center};

    @Override
    public void create() {
        // Prepare your screen here.
        bg = new Texture("gs/TitleBackground.png");
        batch = new SpriteBatch();
        camera = new OrthographicCamera(1000,540);
        camera.position.set(960,540,1);
        viewport = new FitViewport(1000,540,camera);
        viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),true);
        Skin skin = new Skin(Gdx.files.internal("gs/alphaSkin.json"),
                new TextureAtlas(Gdx.files.internal("gs/alphaSkin.atlas"), Gdx.files.internal("gs/")));
        stage = new Stage(viewport);
        Font tFont = KnownFonts.getGentium();
        textraLabel1 = new TextraLabel("[*][#ECF0DC]Quests!\nGardens!\n\nHarvest Some Turnips!", skin, tFont);
        textraLabel1.setAlignment(center);
        textraLabel1.useIntegerPositions(false);
        textraLabel1.setPosition(200,200);
        textraLabel1.style = new Label.LabelStyle();
        textraLabel1.style.background = skin.getDrawable("tablet_icon");
        textraLabel1.pack();
        textraLabel1.setOrigin(center);

        table = new Table();
        table.setFillParent(true);
        table.top();
        table.add(textraLabel1);
        table.pack();
        stage.addActor(table);
    }

    @Override
    public void render() {
        textraLabel1.font.boldStrength = 1f + (float) Math.tanh(Gdx.input.getX() / (Gdx.graphics.getWidth() * 0.2) - 2.5);
        ScreenUtils.clear(Color.DARK_GRAY);
        batch.setProjectionMatrix(camera.combined);

        stage.act();
        Camera camera = stage.getViewport().getCamera();
        camera.update();

        Batch batch = stage.getBatch();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(bg, 0, 0, bg.getWidth() * 2, bg.getHeight() * 2);
        stage.getRoot().draw(batch, 1);
        batch.end();

//        sr.begin(ShapeRenderer.ShapeType.Filled);
//        sr.circle(400,200,2);
//        sr.end();
    }

    @Override
    public void resize(int width, int height) {
        // Resize your screen here. The parameters represent the new window size.
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
        config.setTitle("TextraTypist Partial Transparency test");
        config.setWindowedMode(1000, 540);
        config.disableAudio(true);
        ShaderProgram.prependVertexCode = "#version 110\n";
        ShaderProgram.prependFragmentCode = "#version 110\n";
//		config.enableGLDebugOutput(true, System.out);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
//        config.setForegroundFPS(60);
        new Lwjgl3Application(new GSTransparencyTest(), config);
    }

}
