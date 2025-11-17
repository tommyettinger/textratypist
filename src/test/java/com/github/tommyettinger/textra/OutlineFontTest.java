package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class OutlineFontTest extends ApplicationAdapter {

  private Stage stage;

  public OutlineFontTest() {
  }

  @Override
  public void create() {
    stage = new Stage(new ScreenViewport());

    BitmapFont font = new BitmapFont(Gdx.files.classpath("openSans30.fnt"));
    font.setUseIntegerPositions(false);

    String text = "The quick brown fox jumps over the lazy dog.";
    TypingLabel textraLabel = new TypingLabel(text, new Font(font));
    textraLabel.setWrap(true);

    Stack stack = new Stack(textraLabel);
    stack.setFillParent(true);
    stage.addActor(stack);
  }

  @Override
  public void render() {
    ScreenUtils.clear(Color.WHITE);
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
    config.setTitle("TextraLabel outline font with padding test");
    config.setWindowedMode(600, 500);
    config.disableAudio(true);
    config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
    config.useVsync(true);
    new Lwjgl3Application(new OutlineFontTest(), config);
  }
}
