package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.nio.ByteBuffer;

public class DebugPreviewGenerator extends ApplicationAdapter {

    Font font;
    SpriteBatch batch;
    Viewport viewport;
    Layout layout = new Layout().setTargetWidth(800);
    long startTime;
    String distanceField = "\nNo emoji here!";
    String emojiSupport = "\nEmoji! [WHITE][+🥳] [+👍🏻] [+🤙🏼] [+👌🏽] [+🤘🏾] [+✌🏿] [_][+🥰] hm[~]m... [+🤯][ ]";
/*
AStarry-standard.fnt has descent: -12
AStarry-msdf.fnt has descent: -94
Bitter-standard.fnt has descent: -38
Canada1500-standard.fnt has descent: -15
CascadiaMono-msdf.fnt has descent: -10
Cozette-standard.fnt has descent: -3
DejaVuSansMono-msdf.fnt has descent: 3
Gentium-standard.fnt has descent: -31
Gentium-sdf.fnt has descent: -18
Hanazono-standard.fnt has descent: -5
Inconsolata-LGC-Custom-standard.fnt has descent: -18
Inconsolata-LGC-Custom-msdf.fnt has descent: -10
Iosevka-standard.fnt has descent: -17
Iosevka-msdf.fnt has descent: 5
Iosevka-sdf.fnt has descent: 0
Iosevka-Slab-standard.fnt has descent: -17
Iosevka-Slab-msdf.fnt has descent: 5
Iosevka-Slab-sdf.fnt has descent: 0
KingthingsFoundation-standard.fnt has descent: -40
LibertinusSerif-Regular-msdf.fnt has descent: 17
OpenSans-standard.fnt has descent: -11
Oxanium-standard.fnt has descent: -20
RobotoCondensed-standard.fnt has descent: -13
YanoneKaffeesatz-standard.fnt has descent: -19

*/
    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Font Preview Generator");
        config.setWindowedMode(800, 450);
        config.disableAudio(true);
        ShaderProgram.prependVertexCode = "#version 110\n";
        ShaderProgram.prependFragmentCode = "#version 110\n";
//        config.enableGLDebugOutput(true, System.out);
        config.setForegroundFPS(10);
        config.useVsync(true);
        new Lwjgl3Application(new DebugPreviewGenerator(), config);
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        viewport = new StretchViewport(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());

        // investigating a GPU-related bug... seems fixed now, sometimes?
        // with useIntegerPositions(true), on some discrete GPUs this looks "wobbly," with an uneven baseline.
        // with useIntegerPositions(false), it seems fine?
//        Font[] fonts = {KnownFonts.getCozette().useIntegerPositions(true)};
//        Font[] fonts = {KnownFonts.getLanaPixel().useIntegerPositions(true)//.setName("LanaInteger")
//                , KnownFonts.getLanaPixel().useIntegerPositions(false)//.setName("LanaNot")
//        };
        Font[] fonts = KnownFonts.getAll();
//        fnt = fonts[fonts.length - 1];
        Gdx.files.local("out/").mkdirs();
        int index = 0;
        Font fnt = fonts[index];
        for (int i = 0; i < fonts.length; i++) {
            font = fonts[i];
//            if(!font.integerPosition) continue;
            font.setFamily(new Font.FontFamily(new String[]{"Main", "G"}, new Font[]{font, KnownFonts.getGentium().scale(font.cellHeight / 35f, font.cellHeight / 35f)}));
            KnownFonts.addEmoji(font);
            font.resizeDistanceField(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), viewport);
            layout.setBaseColor(Color.DARK_GRAY);
            layout.setMaxLines(20);
            layout.targetWidth = Gdx.graphics.getWidth() * 0.95f;
            layout.setEllipsis(" and so on and so forth...");
//            font.markup("[%300][#44DD22]digital[%]\n[#66EE55]just numeric things \n"
//                    , layout);
            font.markup("[_]Font[ ] [~]name[ ] ([%?error]error[%] [%?warn]warn[%] [%?note]note[%] [*]bold[ ] [/]oblique[ ]): " + font.name +
                    (font.getDistanceField() != Font.DistanceFieldType.MSDF ?
                            ",\n[@Main]Do I... [@G]Do I..." +
                            ",\n[@Main]line up... [@G]line up..." +
                            ",\n[@Main]with Gentium? [@G]with Gentium?[@Main]" : "") +
                    ",\noriginalCellWidth: " + font.originalCellWidth +
                    ", originalCellHeight: " + font.originalCellHeight +
                    ",\ncellWidth: " + font.cellWidth +
                    ", cellHeight: " + font.cellHeight +
                    ",\nscaleX: " + font.scaleX +
                    ", scaleY: " + font.scaleY +
                    ",\nintegerPosition: " + font.integerPosition +
                    ", descent: " + font.descent +
                    ",\nxAdjust: " + font.xAdjust +
                    ", yAdjust: " + font.yAdjust +
                    ",\nwidthAdjust: " + font.widthAdjust +
                    ", heightAdjust: " + font.heightAdjust +
                    "\n" +
                    (font.getDistanceField() != Font.DistanceFieldType.MSDF ? emojiSupport : distanceField), layout);
//            System.out.println(layout);

            ScreenUtils.clear(0.75f, 0.75f, 0.75f, 1f);
//            ScreenUtils.clear(0.3f, 0.3f, 0.3f, 1f);
//        layout.getLine(0).glyphs.set(0, font.markupGlyph('@', "[" + colorNames.get((int)(TimeUtils.timeSinceMillis(startTime) >>> 8) % colorNames.size) + "]"));
            float x = 400, y = (Gdx.graphics.getBackBufferHeight() + layout.getHeight()) * 0.5f - font.descent * font.scaleY;
            batch.begin();
            font.enableShader(batch);
            font.drawGlyphs(batch, layout, x, y, Align.center);
            batch.end();

            // Modified Pixmap.createFromFrameBuffer() code that uses RGB instead of RGBA
            Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
            final Pixmap pm = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), Pixmap.Format.RGB888);
            ByteBuffer pixels = pm.getPixels();
            Gdx.gl.glReadPixels(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, pixels);
            // End Pixmap.createFromFrameBuffer() modified code

//            Pixmap pm = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
            PixmapIO.writePNG(Gdx.files.local("out/debug/debug" + (index++) + font.name + ".png"), pm, 6, true);
        }
        font = fnt;
//        System.out.println(layout);
        startTime = TimeUtils.millis();
        font.markup("[_]Font[ ] [~]name[ ] ([%?error]error[%] [%?warn]warn[%] [%?note]note[%] [*]bold[ ] [/]oblique[ ]):  " + font.name +
                ",\noriginalCellWidth: " + font.originalCellWidth +
                ", originalCellHeight: " + font.originalCellHeight +
                ",\ncellWidth: " + font.cellWidth +
                ", cellHeight: " + font.cellHeight +
                ",\nscaleX: " + font.scaleX +
                ", scaleY: " + font.scaleY +
                ",\nintegerPosition: " + font.integerPosition +
                ", descent: " + font.descent +
                ",\nxAdjust: " + font.xAdjust +
                ", yAdjust: " + font.yAdjust +
                ",\nwidthAdjust: " + font.widthAdjust +
                ", heightAdjust: " + font.heightAdjust +
                "\n" +
                (font.getDistanceField() != Font.DistanceFieldType.MSDF ? emojiSupport : distanceField), layout.clear());
//        System.out.println(layout);

        Gdx.app.exit();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.75f, 0.75f, 0.75f, 1f);
//        ScreenUtils.clear(0.3f, 0.3f, 0.3f, 1f);
//        layout.getLine(0).glyphs.set(0, font.markupGlyph('@', "[" + colorNames.get((int)(TimeUtils.timeSinceMillis(startTime) >>> 8) % colorNames.size) + "]"));
        float x = 400, y = (450 + layout.getHeight()) * 0.5f - font.descent * font.scaleY;
        viewport.apply();
        batch.begin();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        font.enableShader(batch);
        font.drawGlyphs(batch, layout, x, y, Align.center);
        batch.end();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        font.resizeDistanceField(width, height, viewport);
    }
}
