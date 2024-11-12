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
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.lang.management.ManagementFactory;
/*
[MEMORY] text size: 10000000 bytes
[MEMORY] ThreadMXBean reports font16 uses
1565240 bytes.
[MEMORY] ThreadMXBean reports label uses
284326488 bytes.
[MEMORY] Java heap:
1594963440, Native heap: 1594963440
 */
public class Issue7508Test extends ApplicationAdapter {
  private static final long OFFSET = measureInternal(() -> { });

  /**
   * @return amount of memory allocated while executing provided {@link Runnable}
   */
  private static long measureInternal(final Runnable x) {
    final long now = getCurrentThreadAllocatedBytes();
    x.run();
    return getCurrentThreadAllocatedBytes() - now;
  }

  public static long measure(final Runnable x)
  {
    System.gc();
    final long mi = measureInternal(x);
    return mi - OFFSET;
  }

  @SuppressWarnings("restriction")
  private static long getCurrentThreadAllocatedBytes() {
    return ((com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean())
            .getThreadAllocatedBytes(Thread.currentThread().getId());
  }

  private Stage stage;

  public Issue7508Test() {
  }

  @Override
  public void create() {

    Gdx.app.setLogLevel(Application.LOG_DEBUG);
    StringBuilder stringBuilder = new StringBuilder(10_000_000);
    for (int i = 0; i < 1000000; i++) {
      stringBuilder.append("aaaaaaaaa\n");
    }

    String text = stringBuilder.toString();
    Gdx.app.log("MEMORY", "text size: " + text.length() + " bytes"); // size is 10 MB

    stage = new Stage(new ScreenViewport());

    long memory;
    final BitmapFont[] font16 = new BitmapFont[1];

    // Uses 1615733248 for both Java and Native heap
    // ThreadMXBean reports 284068616 bytes used.
    memory = measure(() -> {
              FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("OpenSans-standard.ttf"));
              FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
              parameter.size = 16;
              font16[0] = generator.generateFont(parameter);
              generator.dispose();
    });
    // Uses 1623910080 for both Java and Native heap
    // ThreadMXBean reports 284068160 bytes used.
//    memory = measure(() -> {
//      font16[0] = new BitmapFont();
//    });

    Gdx.app.log("MEMORY", "ThreadMXBean reports font16 uses\n" + memory + " bytes.");

    if(font16[0] == null) {
      font16[0] = new BitmapFont();
    }

    Label.LabelStyle labelStyle = new Label.LabelStyle(font16[0], Color.WHITE);
    final Label[] label = new Label[1];
    memory = measure(() -> {
      label[0] = new Label(text, labelStyle);
    });
    Gdx.app.log("MEMORY", "ThreadMXBean reports label uses\n" + memory + " bytes.");

    if(label[0] == null)
       label[0] = new Label("", labelStyle);
    stage.addActor(label[0]);
    stage.act(0.5f);
  }

  @Override
  public void render() {
    ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
    stage.act();
    stage.getViewport().apply(true);
    stage.draw();
    Gdx.app.log("MEMORY", "Java heap:\n" + Gdx.app.getJavaHeap() + ", Native heap: " + Gdx.app.getNativeHeap());
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
    new Lwjgl3Application(new Issue7508Test(), config);
  }
}
