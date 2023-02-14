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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.colorful.oklab.ColorTools;
import com.github.tommyettinger.colorful.oklab.ColorfulBatch;
import com.github.tommyettinger.colorful.oklab.Palette;

import static com.badlogic.gdx.Gdx.input;
import static com.github.tommyettinger.textra.Font.DistanceFieldType.STANDARD;

public class ColorfulBatchFontTest extends ApplicationAdapter {
    public static final int SCREEN_WIDTH = 808;
    public static final int SCREEN_HEIGHT = 51 * 15;
    private ColorfulBatch batch;
    private Viewport screenView;
    private Font font;
    private Texture blank;
    private long lastProcessedTime = 0L;
    private int selectedIndex;
    private String selectedName;
    private float selected;

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Named Color Demo");
        config.setWindowedMode(SCREEN_WIDTH, SCREEN_HEIGHT);
        config.useVsync(true);
        config.disableAudio(true);

        final ColorfulBatchFontTest app = new ColorfulBatchFontTest();
        new Lwjgl3Application(app, config);
    }

    @Override
    public void create() {
        Pixmap b = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        b.drawPixel(0, 0, 0x808080FF);
        blank = new Texture(b);
        font = new Font("Cozette-standard.fnt",
                "Cozette-standard.png", STANDARD, 0, 2, 0, 0, false)
                .useIntegerPositions(true)
                .setName("Cozette");
        font.PACKED_BLACK = Palette.BLACK;
//        font.setColor(1f, 0.5f, 0.5f, 1f);
        batch = new ColorfulBatch(1000);
        screenView = new ScreenViewport();
        screenView.getCamera().position.set(SCREEN_WIDTH * 0.5f, SCREEN_HEIGHT * 0.5f, 0);
        screenView.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.enableBlending();

        for (int i = 0; i < Palette.NAMES_BY_HUE.size; i++) {
            String name = Palette.NAMES_BY_HUE.get(i);
            float color = Palette.NAMED.get(name, Palette.WHITE);
            if (ColorTools.alphaInt(color) == 0)
                Palette.NAMES_BY_HUE.removeIndex(i--);
        }
        selectedName = "Gray";
        selectedIndex = Palette.NAMES_BY_HUE.indexOf("Gray", false);
        selected = Palette.GRAY;
//        selectedName = Palette.NAMES_BY_HUE.first();
//        selectedIndex = 0;
//        selected = Palette.NAMED.get(selectedName, Palette.GRAY);
    }


    @Override
    public void render() {
        Gdx.gl.glClearColor(0.4f, 0.4f, 0.4f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        handleInput();
        batch.setProjectionMatrix(screenView.getCamera().combined);
        batch.setPackedColor(selected);
        batch.begin();
        batch.setTweak(ColorfulBatch.TWEAK_RESET);
        int i = -1;
        final float width = Gdx.graphics.getWidth() / 5f, height = Gdx.graphics.getHeight() / 51f;
        for (int y = 0; y < 51; y++) {
            for (int x = 0; x < 5; x++) {
                String name = Palette.NAMES_BY_HUE.get(++i);
                float color = Palette.NAMED.get(name, Palette.WHITE);
                batch.setPackedColor(color);
                batch.draw(blank, width * x, height * (50 - y), width, height);
            }
        }
        i = -1;
        for (int y = 0; y < 51; y++) {
            for (int x = 0; x < 5; x++) {
                if (++i == selectedIndex) {
                    // This is a total hack. The tweak multiplies L by 0.25 (since the values we give here are half of
                    // what actually gets used), and maximizes contrast. L has 0.5 added to it in the batch color. This
                    // together seems to make black, black, and white, white, at least for text. This is only needed
                    // because Oklab's ColorfulBatch can only raise or lower L by 0.5 at most, so white can only go down
                    // to 50% gray. I think if the font were 50% gray instead of white, this might work without hacks.
                    batch.setTweakedColor(1f, 0.5f, 0.5f, 1f, 0.125f, 0.5f, 0.5f, 1f);
//                    batch.setTweakedColor(Palette.GRAY, ColorfulBatch.TWEAK_RESET);
                    font.drawMarkupText(batch, "[%?blacken]" + Palette.NAMES_BY_HUE.get(i), width * x + 1f, height * (51 - y) - 17f);
                }
            }
        }
        batch.end();


    }

    @Override
    public void resize(int width, int height) {
        screenView.update(width, height);
        screenView.getCamera().position.set(width * 0.5f, height * 0.5f, 0f);
        screenView.getCamera().update();
    }

    public void handleInput() {
        if (input.isKeyPressed(Input.Keys.Q) || input.isKeyPressed(Input.Keys.ESCAPE)) //quit
            Gdx.app.exit();
        else if (TimeUtils.timeSinceMillis(lastProcessedTime) > 180) {
            lastProcessedTime = TimeUtils.millis();
            if (input.isKeyPressed(Input.Keys.RIGHT) || input.isKeyPressed(Input.Keys.DOWN)) {
                selectedIndex = (selectedIndex + 1) % Palette.NAMES_BY_HUE.size;
                selectedName = Palette.NAMES_BY_HUE.get(selectedIndex);
                selected = Palette.NAMED.get(selectedName, Palette.GRAY);
            } else if (input.isKeyPressed(Input.Keys.LEFT) || input.isKeyPressed(Input.Keys.UP)) {
                selectedIndex = (selectedIndex + Palette.NAMES_BY_HUE.size - 1) % Palette.NAMES_BY_HUE.size;
                selectedName = Palette.NAMES_BY_HUE.get(selectedIndex);
                selected = Palette.NAMED.get(selectedName, Palette.GRAY);
            } else if (input.isKeyJustPressed(Input.Keys.C)) // CHAOS!
            {
                selectedIndex = MathUtils.random(Palette.NAMES_BY_HUE.size);
                selectedName = Palette.NAMES_BY_HUE.get(selectedIndex);
                selected = Palette.NAMED.get(selectedName, Palette.GRAY);
            } else if (input.isKeyJustPressed(Input.Keys.P)) // print
            {
                System.out.println("Using color " + selectedName
                        + " with L=" + ColorTools.channelL(selected) + ",A=" + ColorTools.channelA(selected)
                        + ",B=" + ColorTools.channelB(selected) + ",alpha=1.0 ."
                );
            }
        }
    }
//
//    public static class ColorfulFont extends Font {
//        private final float[] vertices = new float[24];
//
//        public ColorfulFont(String fntName, String textureName, DistanceFieldType distanceField, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust, boolean makeGridGlyphs) {
//            super(fntName, textureName, distanceField, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
//        }
//
//        @Override
//        protected void drawVertices(Batch batch, Texture texture, float[] spriteVertices) {
//            for (int s = 0, v = 0; v < 24; ) {
//                vertices[v++] = spriteVertices[s++];
//                vertices[v++] = spriteVertices[s++];
//                vertices[v++] = spriteVertices[s++];
//                vertices[v++] = spriteVertices[s++];
//                vertices[v++] = spriteVertices[s++];
//                vertices[v++] = ColorfulBatch.TWEAK_RESET;
//            }
//            batch.draw(texture, vertices, 0, 24);
//        }
//    }
}
