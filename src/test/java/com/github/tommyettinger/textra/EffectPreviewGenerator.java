package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.tommyettinger.anim8.AnimatedGif;
import com.github.tommyettinger.anim8.Dithered;
import com.github.tommyettinger.anim8.QualityPalette;

import java.nio.ByteBuffer;

/**
 */
public class EffectPreviewGenerator extends ApplicationAdapter {
    public static final int FRAMERATE = 60;
    Skin        skin;
    Stage       stage;
    SpriteBatch batch;
    TypingLabel label;
    AnimatedGif gif;
    Array<Pixmap> pms = new Array<>(Pixmap[]::new);
    Array<String> starts;
    Array<String> ends;
    boolean keepGoing = true;

    @Override
    public void create() {
        Gdx.files.local("out/effects").mkdirs();
        gif = new AnimatedGif();
        starts = TypingConfig.EFFECT_START_TOKENS.orderedKeys();
        starts.sort();
        ends = TypingConfig.EFFECT_END_TOKENS.orderedKeys();
        ends.sort();

        adjustTypingConfigs();

        batch = new SpriteBatch();

        stage = new Stage(new ScreenViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        for (int index = 0, n = starts.size; index < n; index++) {

            stage.clear();
            final Table table = new Table();
            stage.addActor(table);

            table.setFillParent(true);
            label = createTypingLabel(index);

            table.pad(20f);
            table.add(label).colspan(5).growX();
            table.row();

            table.pack();
            keepGoing = true;
            while (keepGoing){
                render();
            }
            for (int i = 0; i < pms.size; i++) {
                pms.get(i).dispose();
            }
            pms.clear();

        }
        Gdx.app.exit();
    }

    public void adjustTypingConfigs() {
        // Only allow two chars per frame
        TypingConfig.CHAR_LIMIT_PER_FRAME = 2;

        // Change color used by CLEARCOLOR token
        TypingConfig.DEFAULT_CLEAR_COLOR = Color.WHITE;
    }

    public TypingLabel createTypingLabel(int index) {
//        Font.FontFamily family = new Font.FontFamily(
//                new String[]{
//                        "Serif", "Sans", "Mono", "Medieval", "Future", "Humanist"
//                },
//                new Font[]{
//                        KnownFonts.getGentium().scaleTo(36, 39),
//                        KnownFonts.getOpenSans().scaleTo(26, 39).adjustLineHeight(0.9f),
//                        KnownFonts.getInconsolata().scaleTo(17, 39).adjustLineHeight(0.9375f),
//                        KnownFonts.getKingthingsFoundation().scaleTo(39, 39),
//                        KnownFonts.getOxanium().scaleTo(36, 39).adjustLineHeight(1.05f),
//                        KnownFonts.getYanoneKaffeesatz().scaleTo(36, 39).adjustLineHeight(0.85f)
//                });
//        Font font = family.connected[0].setFamily(family);
        Font font =
                KnownFonts.getGentiumSDF().scale(1.1f, 1.1f);
//                KnownFonts.getStandardFamily();
//                KnownFonts.getGentiumSDF().scale(1.1f, 1.1f).multiplyCrispness(1.3f);
        String text = '{' + starts.get(index) + '}' +
                starts.get(index).toLowerCase() +
                '{' + ends.get(index) + "}{WAIT=3}";

        final TypingLabel label = new TypingLabel(
                text,
                font);
//        label.setDefaultToken("{EASE}{FADE=0;1;0.33}");

        label.selectable = true;
        label.align = Align.topLeft;

        // Make the label wrap to new lines, respecting the table's layout.
        label.wrap = true;
        label.layout.maxLines = 15;
        label.layout.setTargetWidth(Gdx.graphics.getBackBufferWidth() - 100);

        // Set an event listener for when the {EVENT} token is reached and for the char progression ends.
        label.setTypingListener(new TypingAdapter() {
            @Override
            public void end() {
                QualityPalette pal = new QualityPalette();
                pal.analyze(pms);
                gif.setPalette(pal);
                gif.setDitherAlgorithm(Dithered.DitherAlgorithm.BAYDIENT);
                gif.setDitherStrength(0.2f);
                gif.write(Gdx.files.local("out/effects/"+label+".gif"), pms, FRAMERATE);
                keepGoing = false;
            }
        });

        return label;
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);

        stage.act(1f/FRAMERATE);

        stage.draw();
        // Modified Pixmap.createFromFrameBuffer() code that uses RGB instead of RGBA
        Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
        final Pixmap pm = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), Pixmap.Format.RGB888);
        ByteBuffer pixels = pm.getPixels();
        Gdx.gl.glReadPixels(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, pixels);
        // End Pixmap.createFromFrameBuffer() modified code

        pms.add(pm);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        label.font.resizeDistanceField(width, height, stage.getViewport());
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("TypingLabel Test");
        config.setWindowedMode(200, 150);
        config.setResizable(false);
        config.setForegroundFPS(60);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new EffectPreviewGenerator(), config);
    }
}
