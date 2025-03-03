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
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.tommyettinger.textra.utils.Palette;

public class ForestOMossIssueTest extends ApplicationAdapter {
    ScreenViewport viewport;
    Stage stage;
    Font font;
    TypingLabel typingLabel;
    TypingLabel typingLabel2;
    float angle;
    Layout markup;

    @Override
    public void create() {
        viewport = new ScreenViewport();
        viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),true);
        stage = new Stage(viewport);
        stage.setDebugAll(true);

        // These fonts aren't in the GitHub repo. To reproduce the bug, I'm using local copies of this
        // font but not publishing them. I can also reproduce with Cozette and QuanPixel.
//        font = new Font("moss/Quicksand_Medium.fnt", "moss/Quicksand_Medium.png");

//        font = KnownFonts.getCozette(); // works with integer positions false only
//        font = KnownFonts.getCozette().setOutlineStrength(2f); // same as the current default
        font = KnownFonts.getQuanPixel(); // works with integer positions false only
//        font = KnownFonts.getQuanPixel().setOutlineStrength(2f); // same as the current default
//        font = KnownFonts.getLanaPixel(); // doesn't care about integer positions, both work
//        font = KnownFonts.getIBM8x16(); // lines were too thick
//        font = KnownFonts.getIBM8x16().setOutlineStrength(1.6f); // why 1.6??? it works regardless of int position.
//        font = KnownFonts.getIBM8x16Sad(); // char-to-char spacing is bizarre.
//        font = KnownFonts.getCordata16x26(); // works by default now... needed outlineStrength 0.8f
//        font = KnownFonts.getHanazono();

        // toggle between the next two lines to make the outline partly disappear or reappear.
//        font.useIntegerPositions(false); // currently does not have bug, for Container. It still does for Group.
//        font.useIntegerPositions(true); // sometimes still has outline bug, at least for QuanPixel.

        Sprite bg = new Sprite(font.mapping.get(font.solidBlock));
        bg.getColor().set(Color.FOREST);

        font.PACKED_WHITE = NumberUtils.intBitsToFloat(Integer.reverseBytes(Palette.SALMON) & 0x44FFFFFF);

        font.markup("[#FFFFFF44][%?whiten]Jump around. When the ball gets into a slot,\njoined slots freeze and become unavailable.", markup = new Layout(font));

        typingLabel=new TypingLabel();
        typingLabel.setAlignment(Align.center);
        typingLabel.setFont(font);
        typingLabel.style.background = new SpriteDrawable(bg).tint(Color.FOREST);
        typingLabel.setText("[WHITE][%?blacken]Roll a ball. When the ball gets into a slot, adjacent slots freeze and become unavailable.");
        typingLabel.setZIndex(0);
        typingLabel.setPosition(MathUtils.round((Gdx.graphics.getWidth() - typingLabel.layout.getWidth()) / 2f), MathUtils.round(Gdx.graphics.getHeight() / 2f + 30f));
        // Normally we could use the next line, but the label doesn't have its size set yet, because it hasn't
        // been added to the Stage yet.
//        typingLabel.setOrigin(typingLabel.getWidth()/2f, typingLabel.getHeight()/2f);
        // Instead, we can use the width and height of the layout object, which knows how big the label will be
        // once the typing effect finishes.
        typingLabel.setOrigin(typingLabel.layout.getWidth() / 2f, typingLabel.layout.getHeight() / 2f);
        typingLabel.setSize(typingLabel.layout.getWidth(), typingLabel.layout.getHeight());
        typingLabel.layout();

//        Container<TypingLabel> container=new Container<>(typingLabel);
//        container.setFillParent(true);
//        stage.addActor(container);

        typingLabel2=new TypingLabel();
        typingLabel2.setAlignment(Align.center);
        typingLabel2.setFont(font);
        typingLabel2.style.background = new SpriteDrawable(bg).tint(Color.FOREST);
        typingLabel2.setText("[WHITE][%?blacken]Jump around. When the ball gets into a slot,\njoined slots freeze and become unavailable.");
        typingLabel2.setZIndex(0);
        typingLabel.setPosition(MathUtils.round((Gdx.graphics.getWidth() - typingLabel.layout.getWidth()) / 2f), MathUtils.round(Gdx.graphics.getHeight() / 2f + 30f));
//        typingLabel2.setOrigin(typingLabel2.layout.getWidth() / 2f, typingLabel2.layout.getHeight() / 2f);
        typingLabel2.setSize(typingLabel2.layout.getWidth(), typingLabel2.layout.getHeight());
        typingLabel2.layout();

//        Group group = new Group();
//        group.setFillParent(true);
//        group.align(Align.center);
//        group.addActor(typingLabel);
//        group.addActor(typingLabel2);
//        stage.addActor(typingLabel2);

        Container<TypingLabel> container=new Container<>(typingLabel2);
        container.setFillParent(true);
        stage.addActor(container);

//        viewport.getCamera().position.set(typingLabel2.getX(), typingLabel2.getY(), 0);

    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.DARK_GRAY);

//        if(Gdx.input.isKeyJustPressed(Input.Keys.UP)) typingLabel2.setRotation(MathUtils.round(angle += 45f));
//        else if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) typingLabel2.setRotation(MathUtils.round(angle -= 45f));
//
        if(Gdx.input.isKeyPressed(Input.Keys.UP)) typingLabel2.setRotation(MathUtils.round(angle += Gdx.graphics.getDeltaTime() * 25f));
        else if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) typingLabel2.setRotation(MathUtils.round(angle -= Gdx.graphics.getDeltaTime() * 25f));

        viewport.apply(true);
        stage.act();
        Camera camera = viewport.getCamera();
        camera.update();

        Batch batch = stage.getBatch();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        float x = Gdx.graphics.getWidth() / 2;
        float y = Gdx.graphics.getHeight() / 2;

        stage.getRoot().draw(batch, 1);

//        font.drawGlyphs(stage.getBatch(), markup,
//                x, y,
//                Align.center, angle, 0, 0);

        batch.end();

//        System.out.println(typingLabel.workingLayout.lines());
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
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
        config.setWindowedMode(601, 481);
        config.disableAudio(true);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new ForestOMossIssueTest(), config);
    }

}