package com.github.tommyettinger.textra;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * <a href="https://github.com/tommyettinger/textratypist/issues/29">Issue 29 test</a>.
 */
public class Issue29Test extends ApplicationAdapter {

  private Stage stage;

  public Issue29Test() {
  }

  @Override
  public void create() {
      Gdx.app.setLogLevel(Application.LOG_DEBUG);
    stage = new Stage(new ScreenViewport());

//    Font font = new Font();
    Font font = KnownFonts.getDejaVuSans();
      font.omitCurlyBraces = true;

//    TypingLabel label = new TypingLabel("icula nisl a diam felada urna nec magna dapibus", font);
    // The initial bracket starts an effect, which gets fully ignored by Font. That also means it won't wrap.
//    TypingLabel label = new TypingLabel("{icula nisl a diam felada urna nec magna dapibus", font);
    // What I thought would work as an escape for curly brackets... doesn't work.
//    TypingLabel label = new TypingLabel("{{icula nisl a diam felada urna nec magna dapibus", font);
    // When an effect isn't recognized, the {icula} still appears in the text.
//    TypingLabel label = new TypingLabel("{icula} nisl a diam felada urna nec magna dapibus", font);
    // If an Effect is valid (in any case), it works with DejaVu Sans, and with `new Font()`. Markup can be ruled out.
//    TypingLabel label = new TypingLabel("{rainbow} nisl a diam felada urna nec magna dapibus", font);
    // Using TextraLabel does "correctly" omit the effect-like text if omitCurlyBraces is true.
    // If it is false, it still doesn't wrap correctly.
//    TextraLabel label = new TextraLabel("{icula }nisl a diam felada urna nec magna dapibus", font);
      // Escaping a single curly brace already works with TextraLabel...
//    TextraLabel label = new TextraLabel("{{icula nisl a diam felada urna nec magna dapibus", font);
      // But not TypingLabel!
//    TypingLabel label = new TypingLabel("{{icula nisl a diam felada urna nec magna dapibus", font);
    // Wrap is still broken even if TextraLabel correctly escapes a brace and (unescaped) curly braces are omitted.
//    TextraLabel label = new TextraLabel("{{icula nisl a diam felada urna nec magna dapibus", font);
      // Escaping a brace isn't detected by wrap logic; it still checks for a closing brace.
    TextraLabel label = new TextraLabel("{{icula nisl} a diam felada urna nec magna dapibus", font);
    label.setWrap(true);
    label.skipToTheEnd();

    Table table = new Table();
    table.add(label).prefWidth(300).row();
    table.pack();
    table.debugAll();

    stage.addActor(new Stack(table));
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
    config.setTitle("Label wrapping test");
    config.setWindowedMode(400, 300);
    config.disableAudio(true);
    config.setForegroundFPS(10);
    config.useVsync(true);
    new Lwjgl3Application(new Issue29Test(), config);
  }
}