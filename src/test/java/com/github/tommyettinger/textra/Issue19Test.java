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
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;

public class Issue19Test extends ApplicationAdapter {
    Stage stage;
    Font font;
    @Override
    public void create() {
        stage = new Stage();
        stage.setDebugAll(true);
        font = KnownFonts.getRobotoCondensed(Font.DistanceFieldType.MSDF).scaleHeightTo(32);

        Styles.LabelStyle style = new Styles.LabelStyle(font, Color.WHITE);

        String text = "Hello, world! Did you know orange is my favorite color?";
        {
            final TextraLabel testLabel1 = new TextraLabel(text, style);
            testLabel1.setPosition(10, 400);
            testLabel1.setWrap(true);
            testLabel1.setSize(250, 100);

            final TextraLabel testLabel2 = new TextraLabel(text, style) {
                private boolean prefSizeInvalid = true;

                @Override
                public void invalidate() {
                    super.invalidate();
                    prefSizeInvalid = true;
                }

                @Override
                public float getPrefWidth() {
                    if (wrap)
                        return 0f;
                    if (prefSizeInvalid) {
                        validate();
                    }
                    return super.getPrefWidth();
                }

                @Override
                public float getPrefHeight() {
                    if (prefSizeInvalid) {
                        validate();
                    }
                    return super.getPrefHeight();
                }

                @Override
                public void validate() {
                    prefSizeInvalid = false;
                    super.validate();
                }
            };
            testLabel2.setPosition(260, 400);
            testLabel2.setWrap(true);
            testLabel2.setSize(250, 100);

            stage.addActor(testLabel1);
            stage.addActor(testLabel2);

            Gdx.app.log(":UNCHANGED:", "TextraLabel :"
                    + "\nlayout ratio before getting size: " + testLabel1.layout.getWidth() + "/" + testLabel1.layout.getHeight()
                    + "\nwidth/height ratio              : " + testLabel1.getWidth() + "/" + testLabel1.getHeight()
                    + "\npref size ratio                 : " + testLabel1.getPrefWidth() + "/" + testLabel1.getPrefHeight()
                    + "\nlayout ratio after getting size : " + testLabel1.layout.getWidth() + "/" + testLabel1.layout.getHeight()
            );

            Gdx.app.log("::CHANGED::", "TextraLabel2:"
                    + "\nlayout ratio before getting size: " + testLabel2.layout.getWidth() + "/" + testLabel2.layout.getHeight()
                    + "\nwidth/height ratio              : " + testLabel2.getWidth() + "/" + testLabel2.getHeight()
                    + "\npref size ratio                 : " + testLabel2.getPrefWidth() + "/" + testLabel2.getPrefHeight()
                    + "\nlayout ratio after getting size : " + testLabel2.layout.getWidth() + "/" + testLabel2.layout.getHeight()
            );
        }
        {
            final TypingLabel testLabel1 = new TypingLabel(text, style);
            testLabel1.setPosition(10, 200);
            testLabel1.setWrap(true);
            testLabel1.setSize(250, 100);

            final TypingLabel testLabel2 = new TypingLabel(text, style) {
                private boolean prefSizeInvalid = true;

                @Override
                public void invalidate() {
                    super.invalidate();
                    prefSizeInvalid = true;
                }

                @Override
                public float getPrefWidth() {
                    if (wrap)
                        return 0f;
                    if (prefSizeInvalid) {
                        validate();
                    }
                    return super.getPrefWidth();
                }

                @Override
                public float getPrefHeight() {
                    if (prefSizeInvalid) {
                        validate();
                    }
                    return super.getPrefHeight();
                }

                @Override
                public void validate() {
                    prefSizeInvalid = false;
                    super.validate();
                }
            };
            testLabel2.setPosition(260, 200);
            testLabel2.setWrap(true);
            testLabel2.setSize(250, 100);

            stage.addActor(testLabel1);
            stage.addActor(testLabel2);

            Gdx.app.log(":UNCHANGED:", "TypingLabel :"
                    + "\nlayout ratio before getting size: " + testLabel1.workingLayout.getWidth() + "/" + testLabel1.workingLayout.getHeight()
                    + "\nwidth/height ratio              : " + testLabel1.getWidth() + "/" + testLabel1.getHeight()
                    + "\npref size ratio                 : " + testLabel1.getPrefWidth() + "/" + testLabel1.getPrefHeight()
                    + "\nlayout ratio after getting size : " + testLabel1.workingLayout.getWidth() + "/" + testLabel1.workingLayout.getHeight()
            );

            Gdx.app.log("::CHANGED::", "TypingLabel2:"
                    + "\nlayout ratio before getting size: " + testLabel2.workingLayout.getWidth() + "/" + testLabel2.workingLayout.getHeight()
                    + "\nwidth/height ratio              : " + testLabel2.getWidth() + "/" + testLabel2.getHeight()
                    + "\npref size ratio                 : " + testLabel2.getPrefWidth() + "/" + testLabel2.getPrefHeight()
                    + "\nlayout ratio after getting size : " + testLabel2.workingLayout.getWidth() + "/" + testLabel2.workingLayout.getHeight()
            );
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        font.resizeDistanceField(width, height, stage.getViewport());
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);
        stage.act();
        stage.draw();
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Multi-Line Scaling Test");
        config.setWindowedMode(720, 600);
        config.setResizable(true);
        config.setForegroundFPS(0);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new Issue19Test(), config);
    }
}
