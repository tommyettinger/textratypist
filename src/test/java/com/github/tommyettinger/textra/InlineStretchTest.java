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

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class InlineStretchTest extends ApplicationAdapter {

  private Font font;
  private Stage stage;

  public InlineStretchTest() {
  }

  @Override
  public void create() {
    stage = new Stage(new ScreenViewport());
    font = KnownFonts.getGentiumUnItalic();
    KnownFonts.addEmoji(font);
    Font font2 = new Font(font);
    font.setInlineImageStretch(0.5f);
    TypingLabel label  = new TypingLabel("[+🤡] This is a [+😄]test message.", font);
    TypingLabel label2 = new TypingLabel("[+🤡] This is a [+😄]test message.", font2);
    label.setPosition(50f, 100f);
    label2.setPosition(50f, 200f);
    label.pack();
    label2.pack();
    label.debug();
    label2.debug();
    stage.addActor(label);
    stage.addActor(label2);
  }

  @Override
  public void render() {
    ScreenUtils.clear(Color.BLACK);
    font.setInlineImageStretch(MathUtils.sinDeg((System.currentTimeMillis() & 0xFFFFFL) * 0.1f) * 0.5f + 1.0f);
    stage.act();
    stage.getViewport().apply(true);
    stage.draw();

  }

  @Override
  public void resize(int width, int height) {
    stage.getViewport().update(width, height);
  }

  public static void main(String[] args){
    Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
    config.setTitle("TextraLabel vs. Label test");
    config.setWindowedMode(400, 300);
    config.disableAudio(true);
    config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
    config.useVsync(true);
    new Lwjgl3Application(new InlineStretchTest(), config);
  }
}
