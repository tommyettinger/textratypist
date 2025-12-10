package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GradientTest extends ApplicationAdapter {

  private Font font;
  private Stage stage;

  public GradientTest() {
  }

  @Override
  public void create() {
    font = new Font();
    font.omitCurlyBraces = false;

    TypingLabel label = new TypingLabel("testing {GRADIENT}testing{ENDGRADIENT} testing", font);
    label.skipToTheEnd();

    Table table = new Table();
    table.add(label);

    Stack stack = new Stack(table);
    stack.setFillParent(true);

    stage = new Stage(new ScreenViewport());
    stage.addActor(stack);
  }

  @Override
  public void render() {
    ScreenUtils.clear(Color.BLACK);

    stage.act();
    stage.draw();
  }

  @Override
  public void dispose() {
    stage.dispose();
    font.dispose();
  }

  @Override
  public void resize(int width, int height) {
    stage.getViewport().update(width, height, false);
  }

  public static void main(String[] args){
    Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
    config.setTitle("Gradient with omitCurlyBraces=false test");
    config.setWindowedMode(600, 500);
    config.disableAudio(true);
    config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
    config.useVsync(true);
    new Lwjgl3Application(new GradientTest(), config);
  }
}
