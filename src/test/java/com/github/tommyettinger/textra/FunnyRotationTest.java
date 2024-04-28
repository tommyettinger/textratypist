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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import static com.badlogic.gdx.utils.Align.*;

public class FunnyRotationTest extends ApplicationAdapter {
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
        camera = new OrthographicCamera(1000,540);
        camera.position.set(960,540,1);
        viewport = new FitViewport(1000,540,camera);
        viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),true);
        texture = new TextureRegion(new Texture(Gdx.files.internal("tilerb.png")));
        stage = new Stage(viewport);
        Skin skin = new Skin(Gdx.files.internal("shadeui/uiskin.json"));

        Font tFont = new Font("RaeleusScriptius-standard.fnt", 0, 8, 0, 0);
        tFont.setLineMetrics(-0.25f, -0.1f, 0f, 0f);
//        BitmapFont font = new BitmapFont();
//        Font tFont = new Font(font);
        textraLabel1 = new TextraLabel("Test", skin.get("subtitle", Styles.LabelStyle.class), tFont);
        textraLabel1.useIntegerPositions(false);
        textraLabel1.setPosition(200,200);
//        textraLabel1.style = new Styles.LabelStyle();
//        textraLabel1.style.background = new TextureRegionDrawable(texture);
//        textraLabel1.style.background.setTopHeight(20);
//        textraLabel1.style.background.setBottomHeight(20);
//        textraLabel1.style.background.setLeftWidth(10);
//        textraLabel1.style.background.setRightWidth(10);
        textraLabel1.pack();
        textraLabel1.setOrigin(center);
        textraLabel2 = new TextraLabel("Check check, [_]one two[_], [~]one two[~]...", skin.get("subtitle", Styles.LabelStyle.class), tFont);
        textraLabel2.useIntegerPositions(false);
        textraLabel2.setPosition(400,200);
//        textraLabel2.style = new Styles.LabelStyle();
//        textraLabel2.style.background = new TextureRegionDrawable(texture);
//        textraLabel2.style.background.setTopHeight(15);
//        textraLabel2.style.background.setBottomHeight(15);
//        textraLabel2.style.background.setLeftWidth(25);
//        textraLabel2.style.background.setRightWidth(25);
        textraLabel2.pack();
        textraLabel2.setOrigin(center);
        textraLabel3 = new TextraLabel("We're no strangers to love...\nYou [_]know[_] the [_]rules[_],\nand [~]so do I[~]!",
                skin.get("subtitle", Styles.LabelStyle.class), tFont);
        textraLabel3.useIntegerPositions(false);
        textraLabel3.setPosition(600,300);
//        textraLabel3.style = new Styles.LabelStyle();
//        textraLabel3.style.background = new TextureRegionDrawable(texture);
//        textraLabel3.style.background.setTopHeight(50);
//        textraLabel3.style.background.setBottomHeight(50);
//        textraLabel3.style.background.setLeftWidth(50);
//        textraLabel3.style.background.setRightWidth(50);
        textraLabel3.pack();
        textraLabel3.setOrigin(center);

        typingLabel1 = new TypingLabel("Test", skin.get("subtitle", Styles.LabelStyle.class), tFont);
        typingLabel1.useIntegerPositions(false);
        typingLabel1.setText("Test");
        typingLabel1.setPosition(200,200);
//        typingLabel1.style = new Styles.LabelStyle();
//        typingLabel1.style.background = new TextureRegionDrawable(texture);
//        typingLabel1.style.background.setTopHeight(20);
//        typingLabel1.style.background.setBottomHeight(20);
//        typingLabel1.style.background.setLeftWidth(10);
//        typingLabel1.style.background.setRightWidth(10);
        typingLabel1.pack();
        typingLabel1.setOrigin(center);
        typingLabel2 = new TypingLabel("Test", skin.get("subtitle", Styles.LabelStyle.class), tFont);
        typingLabel2.useIntegerPositions(false);
        typingLabel2.setText("Check check, [_]one two[_], [~]one two[~]...");
        typingLabel2.setPosition(400,200);
//        typingLabel2.style = new Styles.LabelStyle();
//        typingLabel2.style.background = new TextureRegionDrawable(texture);
//        typingLabel2.style.background.setTopHeight(15);
//        typingLabel2.style.background.setBottomHeight(15);
//        typingLabel2.style.background.setLeftWidth(25);
//        typingLabel2.style.background.setRightWidth(25);
        typingLabel2.pack();
        typingLabel2.setOrigin(center);
        typingLabel3 = new TypingLabel("Test", skin.get("subtitle", Styles.LabelStyle.class), tFont);
        typingLabel3.useIntegerPositions(false);
        typingLabel3.setText("We're no strangers to love...\nYou [_]know[_] the [_]rules[_],\nand [~]so do I[~]!");
        typingLabel3.setPosition(600,300);
//        typingLabel3.style = new Styles.LabelStyle();
//        typingLabel3.style.background = new TextureRegionDrawable(texture);
//        typingLabel3.style.background.setTopHeight(50);
//        typingLabel3.style.background.setBottomHeight(50);
//        typingLabel3.style.background.setLeftWidth(50);
//        typingLabel3.style.background.setRightWidth(50);
        typingLabel3.pack();
        typingLabel3.setOrigin(center);

        table = new Table();
        table.setFillParent(true);
        table.defaults().padBottom(100).padTop(40);
        table.top();
//        table.add(textraLabel1).row();
//        table.add(textraLabel2).row();
//        table.add(textraLabel3).row();
//        table.add(textraLabel1);
        table.add(typingLabel1).row();
//        table.add(textraLabel2);
        table.add(typingLabel2).row();
//        table.add(textraLabel3);
//        table.add(typingLabel3).row();
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
        rot = (rot + Gdx.graphics.getDeltaTime() * 25f) % 3240.0f;
        int alignment = center;
        Gdx.graphics.setTitle(Align.toString(alignment));

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
        config.setTitle("TextraLabel UI test");
        config.setWindowedMode(1000, 540);
        config.disableAudio(true);
        ShaderProgram.prependVertexCode = "#version 110\n";
        ShaderProgram.prependFragmentCode = "#version 110\n";
//		config.enableGLDebugOutput(true, System.out);
		config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
//        config.setForegroundFPS(60);
        new Lwjgl3Application(new FunnyRotationTest(), config);
    }

}