/*
 * Copyright (c) 2024 See AUTHORS file.
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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class Issue7508TextraTest extends ApplicationAdapter {

  private Stage stage;

  public Issue7508TextraTest() {
  }

  @Override
  public void create() {

    Gdx.app.setLogLevel(Application.LOG_DEBUG);
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < 1000000; i++) {
      stringBuilder.append("aaaaaaaaa\n");
    }

    String text = stringBuilder.toString();
    Gdx.app.log("MEMORY", "text size: " + text.length() + " bytes"); // size is 10 MB

    stage = new Stage(new ScreenViewport());

    // Old uses 1615733248 for both Java and Native heap
    // New uses  266092552 for both Java and Native heap
//    FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("OpenSans-standard.ttf"));
//    FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
//    parameter.size = 16;
//    BitmapFont bmFont = generator.generateFont(parameter);
//    generator.dispose();

    // Old uses 1623910080 for both Java and Native heap
    // New uses  266370560 for both Java and Native heap
    BitmapFont bmFont = new BitmapFont();

    Font font16 = new Font(bmFont);

    Styles.LabelStyle labelStyle = new Styles.LabelStyle(font16, Color.WHITE);
    TextraLabel label = new TextraLabel(text, labelStyle);

    stage.addActor(label);
  }

  @Override
  public void render() {
    ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
    stage.act();
    stage.getViewport().apply(true);
    stage.draw();
    Gdx.app.log("MEMORY", "Java heap: " + Gdx.app.getJavaHeap() + ", Native heap: " + Gdx.app.getNativeHeap());
  }

  @Override
  public void dispose() {
    stage.dispose();
  }

  @Override
  public void resize(int width, int height) {
    stage.getViewport().update(width, height);
  }

  public static void main(String[] args){
    Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
    config.setTitle("Large Label memory usage test");
    config.setWindowedMode(660, 500);
    config.disableAudio(true);
    config.setForegroundFPS(1);
    config.useVsync(true);
    new Lwjgl3Application(new Issue7508TextraTest(), config);
  }
}
