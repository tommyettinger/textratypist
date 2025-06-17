package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.textra.utils.Palette;

import java.nio.ByteBuffer;

public class PreviewMaterialDesignGenerator extends ApplicationAdapter {

    public static final int SCREEN_WIDTH = 1200, SCREEN_HEIGHT = 600;
    Font font;
    SpriteBatch batch;
    Viewport viewport;
    Layout layout = new Layout().setTargetWidth(1200);
    float x, y;

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Material Design Preview Generator");
        config.setWindowedMode(SCREEN_WIDTH, SCREEN_HEIGHT);
        config.disableAudio(true);
        ShaderProgram.prependVertexCode = "#version 110\n";
        ShaderProgram.prependFragmentCode = "#version 110\n";
//        config.enableGLDebugOutput(true, System.out);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new PreviewMaterialDesignGenerator(), config);
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        viewport = new StretchViewport(SCREEN_WIDTH, SCREEN_HEIGHT);

        Gdx.files.local("out/").mkdirs();
        font = KnownFonts.addMaterialDesignIcons(KnownFonts.getNowAlt(Font.DistanceFieldType.MSDF).scaleHeightTo(20).fitCell(24, 24, true), 0f, 0f, 0f);
        layout.setBaseColor(Color.WHITE);
        StringBuilder sb = new StringBuilder(4000);
//        sb.append("[%?blacken]");
        RandomXS128 random = new RandomXS128(23, 42);
        IntArray keys = font.mapping.keys().toArray();
        int ps = Palette.NAMES.size;
        Array<String> names = font.namesByCharCode.values().toArray();
        int ns = names.size;
        for (int y = 0; y < 24; y++) {
            for (int x = 0; x < 49; x++) {
                sb.append("[richmost white ").append(Palette.NAMES.get(random.nextInt(ps))).append("][+").append(names.get(random.nextInt(ns))).append(']');
            }
            sb.append('\n');
        }
        font.markup(sb.toString(), layout);
        font.calculateSize(layout);
        System.out.println(sb);

        ScreenUtils.clear(0.15f, 0.15f, 0.15f, 1f);
        x = Gdx.graphics.getBackBufferWidth() * 0.5f + 12f;
        y = Gdx.graphics.getBackBufferHeight() - font.cellHeight;
        viewport.update(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), true);
        font.resizeDistanceField(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), viewport);
        batch.begin();
        font.enableShader(batch);
        font.drawGlyphs(batch, layout, x, y, Align.top);
        batch.end();//

        // Modified Pixmap.createFromFrameBuffer() code that uses RGB instead of RGBA
        Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
        final Pixmap pm = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), Pixmap.Format.RGB888);
        ByteBuffer pixels = pm.getPixels();
        Gdx.gl.glReadPixels(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, pixels);
        // End Pixmap.createFromFrameBuffer() modified code

        PixmapIO.writePNG(Gdx.files.local("out/MaterialDesignPreview.png"), pm, 2, true);
//        Gdx.app.exit();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.15f, 1f);
        viewport.apply(true);
        batch.begin();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        font.enableShader(batch);
        font.drawGlyphs(batch, layout, x, y, Align.top);
        batch.end();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        font.resizeDistanceField(width, height, viewport);
    }
}
