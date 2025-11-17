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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class WelcomeScreen extends ApplicationAdapter {

  private Stage stage;

  public WelcomeScreen() {
  }

  @Override
  public void create() {
    stage = new Stage(new ScreenViewport());

    BitmapFont bitmapFont = new BitmapFont();
    bitmapFont.setUseIntegerPositions(false);
    Label.LabelStyle style = new Label.LabelStyle(bitmapFont, Color.WHITE);

    Font font = new Font(bitmapFont, 0f, 2f, 0f, 0f);
    Styles.LabelStyle style2 = new Styles.LabelStyle(font, Color.RED);

    String text = (
        "To pay homage to the great GBTK and a celebration of reaching the tenth one, I have created this masterpiece to reflect upon the happy times we've had together.\n" +
            "\n" +
            "Prepare yourself for a sensuous journey, in which you will have to grab the games from the previous nine GBTKs and categorise them into the appropriate jam. Once all games have been categorised, you shall be automatically whisked away to view how well you did without a chance to rethink your final placement.\n" +
            "\n" +
            "I am legally required to inform you that this tribute has been linked with hearing loss, seizures, and birth defects."
    ).replace(" ", "  ");

    Label label = new Label(text, style);
    TypingLabel textraLabel = new TypingLabel(text, style2);

    Stack stack = new Stack(label, textraLabel);
    stack.setFillParent(true);
    stage.addActor(stack);
    label.setWrap(true);
    textraLabel.setWrap(true);
  }

  @Override
  public void render() {
    ScreenUtils.clear(Color.BLACK);
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
    config.setWindowedMode(600, 500);
    config.disableAudio(true);
    config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
    config.useVsync(true);
    new Lwjgl3Application(new WelcomeScreen(), config);
  }
}
