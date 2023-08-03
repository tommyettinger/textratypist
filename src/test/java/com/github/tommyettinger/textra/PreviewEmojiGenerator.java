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
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.nio.ByteBuffer;

public class PreviewEmojiGenerator extends ApplicationAdapter {

    Font font;
    SpriteBatch batch;
    Viewport viewport;
    Layout layout = new Layout().setTargetWidth(1200);
    float x, y;

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Font Preview Generator");
        config.setWindowedMode(1200, 675);
        config.disableAudio(true);
        ShaderProgram.prependVertexCode = "#version 110\n";
        ShaderProgram.prependFragmentCode = "#version 110\n";
//        config.enableGLDebugOutput(true, System.out);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new PreviewEmojiGenerator(), config);
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        viewport = new StretchViewport(1200, 600);

        Gdx.files.local("out/").mkdirs();
        font = KnownFonts.addEmoji(KnownFonts.getAStarry().scale(3, 3), 0f, 0f, 4f);
        layout.setBaseColor(Color.WHITE);
        StringBuilder sb = new StringBuilder(4000);
        sb.append("[%?blacken]");
        RandomXS128 random = new RandomXS128(23, 42);
        IntArray keys = font.mapping.keys().toArray();
        int ks = keys.size;
        for (int y = 0; y < 24; y++) {
            for (int x = 0; x < 49; x++) {
                sb.append((char)keys.get(random.nextInt(ks)));
            }
            sb.append('\n');
        }//
        font.markup(sb.toString(), layout);

        ScreenUtils.clear(0.75f, 0.75f, 0.75f, 1f);
        x = Gdx.graphics.getBackBufferWidth() * 0.5f;
        y = (Gdx.graphics.getBackBufferHeight() + layout.getHeight() - font.cellHeight * 4f) * 0.5f + font.descent * font.scaleY;
        batch.begin();
        font.enableShader(batch);
        font.drawGlyphs(batch, layout, x, y, Align.center);
        batch.end();//

        // Modified Pixmap.createFromFrameBuffer() code that uses RGB instead of RGBA
        Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
        final Pixmap pm = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), Pixmap.Format.RGB888);
        ByteBuffer pixels = pm.getPixels();
        Gdx.gl.glReadPixels(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, pixels);
        // End Pixmap.createFromFrameBuffer() modified code

        PixmapIO.writePNG(Gdx.files.local("out/EmojiPreview.png"), pm, 2, true);
//        Gdx.app.exit();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.75f, 0.75f, 0.75f, 1f);
        viewport.apply(true);
        batch.begin();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        font.enableShader(batch);
        font.drawGlyphs(batch, layout, x, y, Align.center);
        batch.end();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }
}
