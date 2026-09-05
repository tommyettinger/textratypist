package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.ArrayList;

public class SingleTypingLabelTest extends ApplicationAdapter {
    Skin        skin;
    Stage       stage;
    TypingLabel label;
    ArrayList<Font> fonts = new ArrayList<>();

    @Override
    public void create() {
        // TextureArrayCpuPolygonSpriteBatch is an alternative to SpriteBatch that does some things better.
        TextureArrayCpuPolygonSpriteBatch batch = new TextureArrayCpuPolygonSpriteBatch(1000);
        // When using a TextureArray batch, you need to call this line before using anything from KnownFonts.
        // Usually this line goes right after creating a TextureArrayCpuPolygonSpriteBatch, at the start of create() .
        TextureArrayShaders.initializeTextureArrayShaders();

        adjustTypingConfigs();

        skin = new FWSkin(Gdx.files.internal("uiskin.json"));
        stage = new Stage(new ExtendViewport(720, 405), batch);
//        stage.setDebugAll(true);
        Gdx.input.setInputProcessor(stage);

        final Table table = new Table();
        stage.addActor(table);
        table.setFillParent(true);

        for (int i = 0; i <= 10; i++) {

                Font font;
                font = KnownFonts.addEmoji(KnownFonts.getGentiumUnItalic(Font.DistanceFieldType.MSDF)).scaleHeightTo(32);
                font.topEdgeLightnessChange = i * 0.1f;
                font.bottomEdgeLightnessChange = 0f;
                fonts.add(font);
                label = createTypingLabel(font);
                label.setAlignment(Align.center);
                table.pad(20f);
                table.add(label).colspan(5).growX().row();
        }
        table.row().uniform().expand().growX().space(40).center();

        table.pack();
    }

    public void adjustTypingConfigs() {
        // Only allow two chars per frame
        TypingConfig.CHAR_LIMIT_PER_FRAME = 2;

        // Change color used by CLEARCOLOR token
        TypingConfig.DEFAULT_CLEAR_COLOR = Color.WHITE;

        // Create some global variables to handle style
        TypingConfig.GLOBAL_VARS.put("ICE_WIND", "{GRADIENT=88ccff;eef8ff;-0.5;5}{WIND=2;4;0.25;0.1}{JOLT=1;0.6;inf;0.1;;}");
    }

    public TypingLabel createTypingLabel(Font font) {
        final TypingLabel label = new TypingLabel(
                "I [/]love[] TextraTypist! [+😀]\n" +
                "But [*]U. Nitty[ ] doesn't. {METRONOME}[+☝️]{ENDMETRONOME}",
//                "[gold 3 orange][#][*]The Planet Жфюй![*][#][white] [+🚀]",
                font);
        label.setAlignment(Align.center);
        // Make the label wrap to new lines, respecting the table's layout.
        label.setWrap(true);
        label.setDefaultToken("{EASE}{FADE=0;1;0.33}");
        return label;
    }

    public void update(float delta) {
        stage.act(delta);
    }

    @Override
    public void render() {
        update(Gdx.graphics.getDeltaTime());

        ScreenUtils.clear(0.25f, 0.3f, 0.3f, 1);
        
        stage.draw();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        for (int i = 0; i < fonts.size(); i++) {
            fonts.get(i).resizeDistanceField(width, height, stage.getViewport());
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("TypingLabel Test");
        config.setWindowedMode(720, 405);
        config.setResizable(true);
        config.setForegroundFPS(0);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new SingleTypingLabelTest(), config);
    }
}
