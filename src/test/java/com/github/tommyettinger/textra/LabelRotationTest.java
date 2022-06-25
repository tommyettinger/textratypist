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
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class LabelRotationTest extends ApplicationAdapter {
    SpriteBatch batch;
    FitViewport viewport;
    OrthographicCamera camera;
    Stage stage;
    TextraLabel label;
    TextraLabel label2;
    TextraLabel label3;
    TextureRegion texture;
    float rot = 0;
    Table table;
    ShapeRenderer sr;
    @Override
    public void create() {
        // Prepare your screen here.
        batch = new SpriteBatch();
        sr = new ShapeRenderer();
        camera = new OrthographicCamera(800,540);
        camera.position.set(960,540,1);
        viewport = new FitViewport(800,540,camera);
        viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),true);
        texture = new TextureRegion(new Texture(Gdx.files.internal("tilerb.png")));
        stage = new Stage(viewport);
        label = new TextraLabel();
        label.useIntegerPositions(false);
        label.setText("Test");
        label.setPosition(200,200);
        label.style = new Label.LabelStyle();
        label.style.background = new TextureRegionDrawable(texture);
        label.style.background.setTopHeight(5);
        label.style.background.setBottomHeight(5);
        label.style.background.setLeftWidth(5);
        label.style.background.setRightWidth(5);
        label.pack();
        label2 = new TextraLabel();
        label2.useIntegerPositions(false);
        label2.setText("Longer Test For science");
        label2.setPosition(400,200);
        label2.style = new Label.LabelStyle();
        label2.style.background = new TextureRegionDrawable(texture);
        label2.style.background.setTopHeight(15);
        label2.style.background.setBottomHeight(15);
        label2.style.background.setLeftWidth(15);
        label2.style.background.setRightWidth(15);
        label2.pack();
        label3 = new TextraLabel();
        label3.useIntegerPositions(false);
        label3.setText("Taller Test \n For science");
        label3.setPosition(600,300);
        label3.style = new Label.LabelStyle();
        label3.style.background = new TextureRegionDrawable(texture);
        label3.style.background.setTopHeight(50);
        label3.style.background.setBottomHeight(50);
        label3.style.background.setLeftWidth(50);
        label3.style.background.setRightWidth(50);
        label3.pack();
        table = new Table();
        table.setFillParent(true);
        table.add(label).row();
        table.add(label2).row();
        table.add(label3);
        table.pack();
        stage.addActor(table);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.DARK_GRAY);
        batch.setProjectionMatrix(camera.combined);
        label.setRotation(rot);
        label2.setRotation(rot);
        label3.setRotation(rot);
        rot++;
        stage.draw();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.circle(400,200,2);
        sr.end();
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
        config.setTitle("TextraLabel UI test");
        config.setWindowedMode(800, 540);
        config.disableAudio(true);
        ShaderProgram.prependVertexCode = "#version 110\n";
        ShaderProgram.prependFragmentCode = "#version 110\n";
//		config.enableGLDebugOutput(true, System.out);
//		config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new LabelRotationTest(), config);
    }

}