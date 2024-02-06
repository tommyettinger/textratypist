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
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class Issue16Test extends ApplicationAdapter {

  private Stage stage;
  private Skin skin;

  @Override
  public void create () {
    stage = new Stage(new ScreenViewport());

    skin = new Skin(Gdx.files.internal("uiskin.json"));

    // Currently (Feb 5, 2024), scaled BitmapFonts aren't loaded correctly by the Font constructor.
//    skin.getFont("default-font").getData().setScale(2);

    TextraLabel label = new TextraLabel("This is a test. This is a test. This is a test. This is a test. This is a test.", skin)
//    {
//      @Override
//      public void layout() {
//        float width = getWidth();
//        if (style != null && style.background != null) {
//          width = (width - (style.background.getLeftWidth() + style.background.getRightWidth()));
//        }
//        if (wrap && layout.getTargetWidth() != width) {
//          if(width != 0)
//            layout.setTargetWidth(width);
//          font.regenerateLayout(layout);
//          invalidateHierarchy(); // Uncomment this line to fix the issue.
//        }
//      }
//    }
    ;
    // Currently (Feb 5, 2024), you can scale the Font after creation using its APIs.
    label.font.scale(2,2);
    
    label.setWrap(true);
    label.setAlignment(Align.left);

    Table table = new Table(skin);
    table.setBackground("default-pane");
    table.add(label).prefWidth(300).row();

    Table wrappingTable = new Table();
    wrappingTable.add(table).row();
    wrappingTable.add().growY();

    Stack stack = new Stack(wrappingTable);
    stack.setFillParent(true);

    stage.addActor(stack);
  }

  @Override
  public void render () {
    ScreenUtils.clear(0, 0, 0, 1);
    stage.act();
    stage.draw();
  }

  @Override
  public void dispose () {
    stage.dispose();
    skin.dispose();
  }
  public static void main(String[] arg) {
    Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
    config.setTitle("TextraLabel Test");
    config.setWindowedMode(720, 405);
    config.setResizable(true);
    config.setForegroundFPS(0);
    config.useVsync(false);
    config.disableAudio(true);
    new Lwjgl3Application(new Issue16Test(), config);
  }

}