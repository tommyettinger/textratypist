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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.nio.ByteBuffer;

public class DigitalLogoGenerator extends ApplicationAdapter {

    private static final String LOGO_NAME = "digital";
    private static final int WIDTH = 300;
    private static final int HEIGHT = 100;

    SpriteBatch batch;
    Viewport viewport;
    Layout layout = new Layout().setTargetWidth(WIDTH);

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Logo Generator");
        config.setWindowedMode(WIDTH, HEIGHT);
        config.disableAudio(true);
        ShaderProgram.prependVertexCode = "#version 110\n";
        ShaderProgram.prependFragmentCode = "#version 110\n";
        config.setForegroundFPS(1);
        config.useVsync(true);
        new Lwjgl3Application(new DigitalLogoGenerator(), config);
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        viewport = new StretchViewport(WIDTH, HEIGHT);

        Font font = KnownFonts.getAStarry(Font.DistanceFieldType.SDF).scale(0.5f);

        font.PACKED_WHITE = Color.toFloatBits(0.45f, 1f, 0.35f, 1f);

        final String text = "[%300][#55EE44][?neon]digital[%][?]\njust numeric things\n";

        Gdx.files.local("out/").mkdirs();
        Color baseColor = font.getDistanceField() == Font.DistanceFieldType.SDF_OUTLINE ? Color.WHITE : Color.DARK_GRAY;
        viewport.update(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), true);
        font.resizeDistanceField(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), viewport);
        layout.setBaseColor(baseColor);
        layout.setMaxLines(20);
        font.markup(text, layout);
        ScreenUtils.clear(0.18f, 0.27f, 0.15f, 1f);
        float x = Gdx.graphics.getBackBufferWidth() * 0.5f;
        float y = (layout.getHeight()) * 0.8f;
        batch.begin();
        font.enableShader(batch);
        font.resizeDistanceField(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), viewport);
        font.drawGlyphs(batch, layout, x, y, Align.center);
        batch.end();

        // Modified Pixmap.createFromFrameBuffer() code that uses RGB instead of RGBA
        Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
        final Pixmap pm = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), Pixmap.Format.RGB888);
        ByteBuffer pixels = pm.getPixels();
        Gdx.gl.glReadPixels(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, pixels);
        // End Pixmap.createFromFrameBuffer() modified code

//        Pixmap pm = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        PixmapIO.writePNG(Gdx.files.local("out/logos/" + LOGO_NAME + ".png"), pm, 2, true);
        Gdx.app.exit();
    }

    @Override
    public void render() {
    }
}
