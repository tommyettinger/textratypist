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
    Styles.LabelStyle label1Style;
    Styles.LabelStyle label2Style;
    Styles.LabelStyle label3Style;
    TextureRegion texture;
    float rot = 360 * 5;
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
//        BitmapFont font = new BitmapFont();
//        BitmapFont font = new BitmapFont(Gdx.files.internal("GoNotoUniversal-standard.fnt"), Gdx.files.internal("GoNotoUniversal-standard.png"), false);
        BitmapFont font = BitmapFontSupport.loadStructuredJson(Gdx.files.internal("experimental/GoNotoUniversal-standard.json"), "GoNotoUniversal-standard.png");
        Font tFont = new Font("experimental/GoNotoUniversal-standard.json", new TextureRegion(new Texture("experimental/GoNotoUniversal-standard.png")), 0, 0, 0, 0, false, true);
        Font tFont2 = new Font(font);
        tFont.scale(0.5f, 0.5f);
        tFont2.scale(0.5f, 0.5f);
        tFont.useIntegerPositions(false);
        tFont2.useIntegerPositions(false);

        Styles.LabelStyle style = new Styles.LabelStyle();
        style.font = tFont;
        style.font.setTextureFilter();
        style.font.useIntegerPositions(false);
        style.background = new TextureRegionDrawable(texture);
        style.background.setTopHeight(15);
        style.background.setBottomHeight(15);
        style.background.setLeftWidth(25);
        style.background.setRightWidth(25);

        font.getData().scale(-0.5f);
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        labelStyle.background = new TextureRegionDrawable(texture);
        labelStyle.background.setTopHeight(15);
        labelStyle.background.setBottomHeight(15);
        labelStyle.background.setLeftWidth(25);
        labelStyle.background.setRightWidth(25);

        textraLabel1 = new TextraLabel("สวัสดีครับ", tFont);
//        textraLabel1 = new TextraLabel("Test", tFont);
        textraLabel1.setPosition(200,200);
        textraLabel1.style = style;
        textraLabel1.pack();
        textraLabel1.setOrigin(center);
        textraLabel2 = new TextraLabel("Check check, [_]one two[_], [~]one two[~]...", tFont);
        textraLabel2.setPosition(400,200);
        textraLabel2.style = style;
        textraLabel2.pack();
        textraLabel2.setOrigin(center);
        textraLabel3 = new TextraLabel("We're no strangers to love...\nYou [_]know[_] the [_]rules[_],\nand [~]so do I[~]!", tFont);
        textraLabel3.setPosition(600,300);
        textraLabel3.style = style;
        textraLabel3.pack();
        textraLabel3.setOrigin(center);

        typingLabel1 = new TypingLabel("สวัสดีครับ", tFont2);
//        typingLabel1 = new TypingLabel("Test", tFont);
//        typingLabel1.setText("Test");
        typingLabel1.setText("สวัสดีครับ");
        typingLabel1.setPosition(200,200);
        typingLabel1.style = style;
        typingLabel1.pack();
        typingLabel1.setOrigin(center);
        typingLabel2 = new TypingLabel("Test", tFont2);
        typingLabel2.setText("Check check, [_]one two[_], [~]one two[~]...");
        typingLabel2.setPosition(400,200);
        typingLabel2.style = style;
        typingLabel2.pack();
        typingLabel2.setOrigin(center);
        typingLabel3 = new TypingLabel("Test", tFont2);
        typingLabel3.setText("We're no strangers to love...\nYou [_]know[_] the [_]rules[_],\nand [~]so do I[~]!");
        typingLabel3.setPosition(600,300);
        typingLabel3.style = style;
        typingLabel3.pack();
        typingLabel3.setOrigin(center);

        label1 = new Label("สวัสดีครับ", labelStyle);
//        label1 = new Label("Test", labelStyle);
        label1.setPosition(200,200);
        label1.pack();
        label2 = new Label("Check check, one two, one two...", labelStyle);
        label2.setPosition(400,200);
        label2.setOrigin(top);
        label2.pack();
        label3 = new Label("We're no strangers to love...\nYou know the rules,\nand so do I!", labelStyle);
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
        rot = (rot + Gdx.graphics.getDeltaTime() * 40f) % 3240.0f;
        int alignment = aligns[8 - ((int)rot / 360) % 9];
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
        new Lwjgl3Application(new LabelRotationTest(), config);
    }

}