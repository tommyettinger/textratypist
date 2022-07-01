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
import com.badlogic.gdx.Input;
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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import static com.badlogic.gdx.utils.Align.*;

public class LabelRotationTest extends ApplicationAdapter {
    SpriteBatch batch;
    FitViewport viewport;
    OrthographicCamera camera;
    Stage stage;
    TextraLabel textraLabel1;
    TextraLabel textraLabel2;
    TextraLabel textraLabel3;
    TypingLabel typingLabel1;
    TypingLabel typingLabel2;
    TypingLabel typingLabel3;
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

    static final int[] aligns = {left, topLeft, top, topRight, right, bottomRight, bottom, bottomLeft, center};

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
        textraLabel1.style.background.setTopHeight(20);
        textraLabel1.style.background.setBottomHeight(20);
        textraLabel1.style.background.setLeftWidth(10);
        textraLabel1.style.background.setRightWidth(10);
        textraLabel1.pack();
        textraLabel1.setOrigin(center);
        textraLabel2 = new TextraLabel();
        textraLabel2.useIntegerPositions(false);
        textraLabel2.setText("Check check, one two, one two...");
        textraLabel2.setPosition(400,200);
        textraLabel2.style = new Label.LabelStyle();
        textraLabel2.style.background = new TextureRegionDrawable(texture);
        textraLabel2.style.background.setTopHeight(15);
        textraLabel2.style.background.setBottomHeight(15);
        textraLabel2.style.background.setLeftWidth(25);
        textraLabel2.style.background.setRightWidth(25);
        textraLabel2.pack();
        textraLabel2.setOrigin(center);
        textraLabel3 = new TextraLabel();
        textraLabel3.useIntegerPositions(false);
        textraLabel3.setText("We're no strangers to love...\nYou know the rules,\nand so do I!");
        textraLabel3.setPosition(600,300);
        textraLabel3.style = new Label.LabelStyle();
        textraLabel3.style.background = new TextureRegionDrawable(texture);
        textraLabel3.style.background.setTopHeight(50);
        textraLabel3.style.background.setBottomHeight(50);
        textraLabel3.style.background.setLeftWidth(50);
        textraLabel3.style.background.setRightWidth(50);
        textraLabel3.pack();
        textraLabel3.setOrigin(center);

        typingLabel1 = new TypingLabel();
        typingLabel1.useIntegerPositions(false);
        typingLabel1.setText("Test");
        typingLabel1.setPosition(200,200);
        typingLabel1.style = new Label.LabelStyle();
        typingLabel1.style.background = new TextureRegionDrawable(texture);
        typingLabel1.style.background.setTopHeight(20);
        typingLabel1.style.background.setBottomHeight(20);
        typingLabel1.style.background.setLeftWidth(10);
        typingLabel1.style.background.setRightWidth(10);
        typingLabel1.pack();
        typingLabel2 = new TypingLabel();
        typingLabel2.useIntegerPositions(false);
        typingLabel2.setText("Check check, one two, one two...");
        typingLabel2.setPosition(400,200);
        typingLabel2.style = new Label.LabelStyle();
        typingLabel2.style.background = new TextureRegionDrawable(texture);
        typingLabel2.style.background.setTopHeight(15);
        typingLabel2.style.background.setBottomHeight(15);
        typingLabel2.style.background.setLeftWidth(25);
        typingLabel2.style.background.setRightWidth(25);
        typingLabel2.setOrigin(top);
        typingLabel2.pack();
        typingLabel3 = new TypingLabel();
        typingLabel3.useIntegerPositions(false);
        typingLabel3.setText("We're no strangers to love...\nYou know the rules,\nand so do I!");
        typingLabel3.setPosition(600,300);
        typingLabel3.style = new Label.LabelStyle();
        typingLabel3.style.background = new TextureRegionDrawable(texture);
        typingLabel3.style.background.setTopHeight(50);
        typingLabel3.style.background.setBottomHeight(50);
        typingLabel3.style.background.setLeftWidth(50);
        typingLabel3.style.background.setRightWidth(50);
        typingLabel3.pack();

        label1Style = new Label.LabelStyle(font, Color.RED);
        label1Style.font.setUseIntegerPositions(false);
        label1Style.background = new TextureRegionDrawable(texture);
        label1Style.background.setTopHeight(20);
        label1Style.background.setBottomHeight(20);
        label1Style.background.setLeftWidth(10);
        label1Style.background.setRightWidth(10);
        label1 = new Label("Test", label1Style);
        label1.setPosition(200,200);
        label1.pack();
        label2Style = new Label.LabelStyle(font, Color.RED);
        label2Style.font.setUseIntegerPositions(false);
        label2Style.background = new TextureRegionDrawable(texture);
        label2Style.background.setTopHeight(15);
        label2Style.background.setBottomHeight(15);
        label2Style.background.setLeftWidth(25);
        label2Style.background.setRightWidth(25);
        label2 = new Label("Check check, one two, one two...", label2Style);
        label2.setPosition(400,200);
        label2.setOrigin(top);
        label2.pack();
        label3Style = new Label.LabelStyle(font, Color.RED);
        label3Style.font.setUseIntegerPositions(false);
        label3Style.background = new TextureRegionDrawable(texture);
        label3Style.background.setTopHeight(50);
        label3Style.background.setBottomHeight(50);
        label3Style.background.setLeftWidth(50);
        label3Style.background.setRightWidth(50);
        label3 = new Label("We're no strangers to love...\nYou know the rules,\nand so do I!", label3Style);
        label3.setPosition(600,300);
        label3.pack();

        table = new Table();
        table.setFillParent(true);
        table.top();
        table.add(label1);
        table.add(textraLabel1);
        table.add(typingLabel1).row();
        table.add(label2);
        table.add(textraLabel2);
        table.add(typingLabel2).row();
        table.add(label3);
        table.add(textraLabel3);
        table.add(typingLabel3).row();
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
        typingLabel1.setRotation(rot);
        typingLabel2.setRotation(rot);
        typingLabel3.setRotation(rot);
        rot += Gdx.graphics.getDeltaTime() * 80f;
        int alignment = aligns[((int)rot / 360) % 9];
        Gdx.graphics.setTitle(Align.toString(alignment));

        label1.setAlignment(alignment);
        label2.setAlignment(alignment);
        label3.setAlignment(alignment);
        textraLabel1.setAlignment(alignment);
        textraLabel2.setAlignment(alignment);
        textraLabel3.setAlignment(alignment);
        typingLabel1.setAlignment(alignment);
        typingLabel2.setAlignment(alignment);
        typingLabel3.setAlignment(alignment);

        if(Gdx.input.isKeyJustPressed(Input.Keys.PRINT_SCREEN)) {
            typingLabel1.restart();
            typingLabel2.restart();
            typingLabel3.restart();
        }
        stage.act();
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