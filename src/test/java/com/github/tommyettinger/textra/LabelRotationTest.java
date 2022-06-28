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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
    TextraLabel textraLabel1;
    TextraLabel textraLabel2;
    TextraLabel textraLabel3;
    Label label1;
    Label label2;
    Label label3;
    Label.LabelStyle label1Style;
    Label.LabelStyle label2Style;
    Label.LabelStyle label3Style;
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
        BitmapFont font = new BitmapFont();
        textraLabel1 = new TextraLabel();
        textraLabel1.useIntegerPositions(false);
        textraLabel1.setText("Test");
        textraLabel1.setPosition(200,200);
        textraLabel1.style = new Label.LabelStyle();
        textraLabel1.style.background = new TextureRegionDrawable(texture);
        textraLabel1.style.background.setTopHeight(5);
        textraLabel1.style.background.setBottomHeight(5);
        textraLabel1.style.background.setLeftWidth(5);
        textraLabel1.style.background.setRightWidth(5);
        textraLabel1.pack();
        textraLabel2 = new TextraLabel();
        textraLabel2.useIntegerPositions(false);
        textraLabel2.setText("Longer Test For science");
        textraLabel2.setPosition(400,200);
        textraLabel2.style = new Label.LabelStyle();
        textraLabel2.style.background = new TextureRegionDrawable(texture);
        textraLabel2.style.background.setTopHeight(15);
        textraLabel2.style.background.setBottomHeight(15);
        textraLabel2.style.background.setLeftWidth(15);
        textraLabel2.style.background.setRightWidth(15);
        textraLabel2.pack();
        textraLabel3 = new TextraLabel();
        textraLabel3.useIntegerPositions(false);
        textraLabel3.setText("Taller Test \n For science");
        textraLabel3.setPosition(600,300);
        textraLabel3.style = new Label.LabelStyle();
        textraLabel3.style.background = new TextureRegionDrawable(texture);
        textraLabel3.style.background.setTopHeight(50);
        textraLabel3.style.background.setBottomHeight(50);
        textraLabel3.style.background.setLeftWidth(50);
        textraLabel3.style.background.setRightWidth(50);
        textraLabel3.pack();

        label1Style = new Label.LabelStyle(font, Color.RED);
        label1Style.font.setUseIntegerPositions(false);
        label1Style.background = new TextureRegionDrawable(texture);
        label1Style.background.setTopHeight(5);
        label1Style.background.setBottomHeight(5);
        label1Style.background.setLeftWidth(5);
        label1Style.background.setRightWidth(5);
        label1 = new Label("Test", label1Style);
        label1.setPosition(200,200);
        label1.pack();
        label2Style = new Label.LabelStyle(font, Color.RED);
        label2Style.font.setUseIntegerPositions(false);
        label2Style.background = new TextureRegionDrawable(texture);
        label2Style.background.setTopHeight(15);
        label2Style.background.setBottomHeight(15);
        label2Style.background.setLeftWidth(15);
        label2Style.background.setRightWidth(15);
        label2 = new Label("Longer Test For science", label2Style);
        label2.setPosition(400,200);
        label2.pack();
        label3Style = new Label.LabelStyle(font, Color.RED);
        label3Style.font.setUseIntegerPositions(false);
        label3Style.background = new TextureRegionDrawable(texture);
        label3Style.background.setTopHeight(50);
        label3Style.background.setBottomHeight(50);
        label3Style.background.setLeftWidth(50);
        label3Style.background.setRightWidth(50);
        label3 = new Label("Taller Test \n For science", label3Style);
        label3.setPosition(600,300);
        label3.pack();

        table = new Table();
        table.setFillParent(true);
        table.add(label1);
        table.add(textraLabel1).row();
        table.add(label2);
        table.add(textraLabel2).row();
        table.add(label3);
        table.add(textraLabel3);
        table.pack();
        stage.addActor(table);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.DARK_GRAY);
        batch.setProjectionMatrix(camera.combined);
        textraLabel1.setRotation(rot);
        textraLabel2.setRotation(rot);
        textraLabel3.setRotation(rot);
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