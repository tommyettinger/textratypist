package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class WrapEmojiEllipsisCrashTest extends ApplicationAdapter {

  private Font font;
  private Layout layout;
  private SpriteBatch batch;

  private ScreenViewport viewport;

  public WrapEmojiEllipsisCrashTest() {
  }

  @Override
  public void create() {
    BitmapFont bitmapFont = new BitmapFont(Gdx.files.internal("the_works/open_sans_big.fnt"));
    bitmapFont.setUseIntegerPositions(false);

    TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("the_works/crash.atlas"));

//    font = new Font(bitmapFont, 0, -22, 0, -16);
    font = new Font(Gdx.files.internal("the_works/open_sans_big.fnt"));
    font.setTextureFilter();
    font.addAtlas(atlas, 0, 6, 0);
    font.setInlineImageStretch(32 / font.cellHeight);
    font.scaleHeightTo(18f);

    layout = new Layout();
    layout.setFont(font);
    layout.setTargetWidth(115f);
    layout.setMaxLines(4);
    // this is definitely causing some mismatch with layout.advances ...
    layout.setEllipsis("...");

    font.markup("Testtt:\ntesting-test, W-i-n-n-e-r haha-hehe testing-t-t-t-test W-i-n haha-hehe", layout);

    batch = new SpriteBatch();

    viewport = new ScreenViewport();
    viewport.setUnitsPerPixel(0.45714286f);
  }

  @Override
  public void render() {
    ScreenUtils.clear(Color.GRAY);

    viewport.apply();
    batch.setProjectionMatrix(viewport.getCamera().combined);

    batch.begin();
    font.drawGlyphs(batch, layout, 0, 0);
    batch.end();
  }

  @Override
  public void dispose() {
    batch.dispose();
  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height, false);
  }

  public static void main(String[] args){
    Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
    config.setTitle("Font crash test");
    config.setWindowedMode(600, 500);
    config.disableAudio(true);
    config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
    config.useVsync(true);
    new Lwjgl3Application(new WrapEmojiEllipsisCrashTest(), config);
  }
}

