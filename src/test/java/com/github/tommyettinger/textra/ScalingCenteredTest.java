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

public class ScalingCenteredTest extends ApplicationAdapter {
    Skin        skin;
    Stage       stage;
    SpriteBatch batch;
    TypingLabel label;
    Font font;

    public static Font getMonogramFamilySized() {
        Font.FontFamily family = new Font.FontFamily(
                new String[]{"Regular", "Italic", "BigRegular", "BigItalic"},
                new Font[]{KnownFonts.getMonogram().scale(2), KnownFonts.getMonogramItalic().scale(2),
                        KnownFonts.getMonogram().scale(8).setName("Big Monogram"), KnownFonts.getMonogramItalic().scale(8).setName("Big Monogram Italic"), });
        family.fontAliases.put("r", 0); // regular
        family.fontAliases.put("i", 1); // italic
        family.fontAliases.put("br", 2); // big regular
        family.fontAliases.put("bi", 3); // big italic
        return family.connected[0].setFamily(family);
    }

    @Override
    public void create() {
        font = getMonogramFamilySized();

        adjustTypingConfigs();

        batch = new SpriteBatch();
        skin = new FWSkin(Gdx.files.internal("uiskin.json"));
        stage = new Stage(new ExtendViewport(720, 405), batch);
        stage.setDebugAll(true);
        Gdx.input.setInputProcessor(stage);

        final Table table = new Table();
        stage.addActor(table);
        table.setFillParent(true);

        // Scale with [%400] isn't considered somewhere for position.
//        label = createTypingLabel(400);
        label = createTypingLabel(100);

        table.pad(50f);
        table.add(label).colspan(5).growX();
        table.row();
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

    public TypingLabel createTypingLabel(int scale) {
        final TypingLabel label = new TypingLabel(
                "{EMERGE}[@r][GOLD]Y U NOT{ENDEMERGE}{WAIT=0.5}[white]\n" +
                        "{VAR=FIRE}WORK!?!?{VAR=ENDFIRE}\n" +
                "{EMERGE}[@br][GOLD]WAT Y U{ENDEMERGE}{WAIT=0.5}[white]\n" +
                        "{VAR=FIRE}WORK!?!?{VAR=ENDFIRE}",
                font);
        label.setAlignment(Align.center);
        label.debug();
        // Make the label wrap to new lines, respecting the table's layout.
        label.setWrap(true);
        label.setDefaultToken("{EASE=-0.5;0.25}[#]");
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
        new Lwjgl3Application(new ScalingCenteredTest(), config);
    }
}
