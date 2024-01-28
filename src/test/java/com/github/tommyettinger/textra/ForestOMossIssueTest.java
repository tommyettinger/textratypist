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
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class ForestOMossIssueTest extends ApplicationAdapter {
    ScreenViewport viewport;
    Stage stage;
    TypingLabel typingLabel;
    TypingLabel typingLabel2;
    float angle;

    @Override
    public void create() {
        viewport = new ScreenViewport();
        viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),true);
        stage = new Stage(viewport);
        stage.setDebugAll(true);

        Font gentium = KnownFonts.getGentium();

//        Group group=new Group();
//        stage.addActor(group);

        Sprite bg = new Sprite(gentium.mapping.get(gentium.solidBlock));
        bg.getColor().set(Color.FOREST);

        typingLabel=new TypingLabel();
        typingLabel.setAlignment(Align.center);
        typingLabel.setFont(gentium);
        typingLabel.style.background = new SpriteDrawable(bg);
        typingLabel.setText("[WHITE][%?blacken]Roll a ball. When the ball gets into a slot, adjacent slots freeze and become unavailable.");
        typingLabel.setZIndex(0);
        typingLabel.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f + 30f);
        // Normally we could use the next line, but the label doesn't have its size set yet, because it hasn't
        // been added to the Stage yet.
//        typingLabel.setOrigin(typingLabel.getWidth()/2f, typingLabel.getHeight()/2f);
        // Instead, we can use the width and height of the layout object, which knows how big the label will be
        // once the typing effect finishes.
        typingLabel.setOrigin(typingLabel.layout.getWidth()/2f, typingLabel.layout.getHeight()/2f);

//        Container<TypingLabel> container=new Container<>(typingLabel);
//        container.setFillParent(true);
//        stage.addActor(container);

        typingLabel2=new TypingLabel();
        typingLabel2.setAlignment(Align.center);
        typingLabel2.setFont(gentium);
        typingLabel2.style.background = new SpriteDrawable(bg);
        typingLabel2.setText("[WHITE][%?blacken]Roll a ball. When the ball gets into a slot,\nadjacent slots freeze and become unavailable.");
        typingLabel2.setZIndex(0);
        typingLabel2.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f - 30f);
        typingLabel2.setOrigin(typingLabel2.layout.getWidth()/2f, typingLabel2.layout.getHeight()/2f);

        VerticalGroup group = new VerticalGroup();
        group.setFillParent(true);
        group.align(Align.center);
        group.addActor(typingLabel);
        group.addActor(typingLabel2);
        stage.addActor(group);

    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.DARK_GRAY);

        if(Gdx.input.isKeyPressed(Input.Keys.UP)) typingLabel.setRotation(angle += Gdx.graphics.getDeltaTime() * 5f);
        else if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) typingLabel.setRotation(angle -= Gdx.graphics.getDeltaTime() * 5f);

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
        config.setTitle("ForestOMoss Background Issue test");
        config.setWindowedMode(600, 480);
        config.disableAudio(true);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new ForestOMossIssueTest(), config);
    }

}