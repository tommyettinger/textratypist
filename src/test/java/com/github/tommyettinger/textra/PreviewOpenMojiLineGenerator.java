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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.textra.utils.ColorUtils;
import com.github.tommyettinger.textra.utils.Palette;
import com.github.tommyettinger.textra.utils.StringUtils;

import java.nio.ByteBuffer;

public class PreviewOpenMojiLineGenerator extends ApplicationAdapter {

    Font font;
    SpriteBatch batch;
    Viewport viewport;
    Layout layout = new Layout().setTargetWidth(1200);
    float x, y;
    long startTime;

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Emoji Preview Generator");
        config.setWindowedMode(1200, 675);
        config.disableAudio(true);
        ShaderProgram.prependVertexCode = "#version 110\n";
        ShaderProgram.prependFragmentCode = "#version 110\n";
//        config.enableGLDebugOutput(true, System.out);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new PreviewOpenMojiLineGenerator(), config);
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        viewport = new StretchViewport(1200, 600);

        Gdx.files.local("out/").mkdirs();
//        font = KnownFonts.addOpenMoji(KnownFonts.getInconsolata().scaleTo(32, 32), false, -12f, -6f, 0f);
        font = KnownFonts.addOpenMoji(KnownFonts.getNowAlt(), false, 0f, 0f, 0f).fitCell(32, 32, true);
        layout.setBaseColor(Color.DARK_GRAY);
        StringBuilder sb = new StringBuilder(4000);
        sb.append("[%?whiten]");
        RandomXS128 random = new RandomXS128(1, 42);
        font.mapping.remove('[');
        font.mapping.remove(']');
        font.mapping.remove('{');
        font.mapping.remove('}');
        font.mapping.remove('\n');
        font.mapping.remove('\r');
        font.mapping.remove('\t');
        font.mapping.remove(' ');
        IntArray keys = font.mapping.keys().toArray();
        int ks = keys.size, ps = Palette.LIST.size;
        for (int y = 0; y < 18; y++) {
            for (int x = 0; x < 36; x++) {
//                sb.append("[richmost darker ").append(Palette.NAMES.get(random.nextInt(ps))).append(']');
                StringUtils.appendUnsignedHex(sb.append("[#"), ColorUtils.darken(Palette.LIST.get(random.nextInt(ps)), 0.25f)).append(']');
                sb.append((char)keys.get(random.nextInt(ks)));
            }
            sb.append('\n');
        }
        font.markup(sb.toString(), layout);
        font.calculateSize(layout);
        System.out.println(sb);

        ScreenUtils.clear(0.75f, 0.75f, 0.75f, 1f);
        x = Gdx.graphics.getBackBufferWidth() * 0.5f;
        y = Gdx.graphics.getBackBufferHeight() - font.cellHeight * 3.5f;
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

        PixmapIO.writePNG(Gdx.files.local("out/OpenMojiPreview.png"), pm, 2, true);
//        Gdx.app.exit();
        startTime = TimeUtils.millis();
    }

    @Override
    public void render() {
        float bright = MathUtils.sin(TimeUtils.timeSinceMillis(startTime) * 3E-3f) * 0.25f + 0.4f;
        ScreenUtils.clear(bright, bright, bright, 1f);
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
    }
}
