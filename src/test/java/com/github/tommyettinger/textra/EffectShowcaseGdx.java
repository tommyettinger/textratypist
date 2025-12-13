package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
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

/**
 */
public class EffectShowcaseGdx extends ApplicationAdapter {
    Skin        skin;
    Stage       stage;
    SpriteBatch batch;
    TypingLabel label;
    TextButton  buttonPause;
    TextButton  buttonResume;
    TextButton  buttonRestart;
    TextButton  buttonRebuild;
    TextButton  buttonSkip;

    @Override
    public void create() {
        adjustTypingConfigs();

        batch = new SpriteBatch();
        skin = new FWSkin(Gdx.files.internal("uiskin.json"));
//        skin.getFont("default-font");//.getData().setScale(0.5f);
        stage = new Stage(new ScreenViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        final Table table = new Table();
        stage.addActor(table);
        table.setFillParent(true);

        label = createTypingLabel();

        buttonPause = new TextButton("Pause", skin);
        buttonPause.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                label.pause();
            }
        });

        buttonResume = new TextButton("Resume", skin);
        buttonResume.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                label.resume();
            }
        });

        buttonRestart = new TextButton("Restart", skin);
        buttonRestart.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                label.restart();
            }
        });

        buttonRebuild = new TextButton("Rebuild", skin);
        buttonRebuild.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                adjustTypingConfigs();
                Cell<TypingLabel> labelCell = table.getCell(label);
                label = createTypingLabel();
                labelCell.setActor(label);
            }
        });

        buttonSkip = new TextButton("Skip", skin);
        buttonSkip.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                label.skipToTheEnd();
            }
        });

        table.pad(50f);
        table.add(label).colspan(5).growX();
        table.row();
        table.row().uniform().expand().growX().space(40).center();
        table.add(buttonPause, buttonResume, buttonRestart, buttonSkip, buttonRebuild);

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

    public TypingLabel createTypingLabel() {
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
                KnownFonts.getAStarryTall();
//                KnownFonts.getStandardFamily();
//                KnownFonts.getGentiumSDF().scale(1.1f, 1.1f).multiplyCrispness(1.3f);
//        String text = " [BLACK]lib[#e74a45]GDX   " +
//                "{EASE}[BLACK]lib[#e74a45]GDX{ENDEASE}   " +
//                "[BLACK]lib[#e74a45]GDX   " +
//                "{SPIN}[BLACK]lib[#e74a45]GDX{ENDSPIN}   " +
//                "[BLACK]lib[#e74a45]GDX   " +
//                "{FADE}[BLACK]lib[#e74a45]GDX{ENDFADE}   " +
//                "[BLACK]lib[#e74a45]GDX   " +
//                "{WAVE}[BLACK]lib[#e74a45]GDX{ENDWAVE}   " +
//                "[BLACK]lib[#e74a45]GDX   " +
//                "{SHRINK}[BLACK]lib[#e74a45]GDX{ENDSHRINK}   " +
//                "[BLACK]lib[#e74a45]GDX   " +
//                "{BLINK}[BLACK]lib[#e74a45]GDX{ENDBLINK}   " +
//                "[BLACK]lib[#e74a45]GDX   " +
//                "{EMERGE}[BLACK]lib[#e74a45]GDX{ENDEMERGE}   " +
//                "[BLACK]lib[#e74a45]GDX   " +
//                "{SPIRAL}[BLACK]lib[#e74a45]GDX{ENDSPIRAL}   " +
//                "[BLACK]lib[#e74a45]GDX  ";
//        StringBuilder sb = new StringBuilder("{SPEED=SLOWER}").append(text).append(text).append(text).append(text);

        StringBuilder sb = new StringBuilder(256).append("{SPEED=SLOWER}");
        Array<String> starts = TypingConfig.EFFECT_START_TOKENS.orderedKeys();
        starts.sort();
        Array<String> ends = TypingConfig.EFFECT_END_TOKENS.orderedKeys();
        ends.sort();
        for (int i = 0, n = starts.size; i < n; i++) {
            sb.append('{').append(starts.get(i)).append('}').append("[BLACK]lib[#e74a45]GDX")
                    .append(" with ").append(starts.get(i))
                    //FIXME: use this to test if the multiple-space wrapping bug is fixed
//                    .append(" with ").append(starts.get(i)).append(' ')
                    .append('{').append(ends.get(i)).append("} {WAIT=0.8}");
        }

        final TypingLabel label = new TypingLabel(sb.toString(), font);

//        final TypingLabel label = new TypingLabel("", font);
//        label.setText(sb.toString());

        label.selectable = true;
        label.align = Align.topLeft;

        // Make the label wrap to new lines, respecting the table's layout.
        label.wrap = true;
        label.setMaxLines(20);
        label.layout.setTargetWidth(Gdx.graphics.getBackBufferWidth() - 100);

        // Set an event listener for when the {EVENT} token is reached and for the char progression ends.
        label.setTypingListener(new TypingAdapter() {
            @Override
            public void event(String event) {
                System.out.println("Event: " + event);
            }

            @Override
            public void end() {
                System.out.println(label);
            }
        });

        return label;
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.9f, 0.9f, 0.9f, 1);

        stage.act();
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
        config.setWindowedMode(720, 600);
        config.setResizable(false);
        config.setForegroundFPS(60);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new EffectShowcaseGdx(), config);
    }
}
