package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.github.tommyettinger.anim8.AnimatedGif;
import com.github.tommyettinger.anim8.Dithered;
import com.github.tommyettinger.anim8.QualityPalette;

import java.nio.ByteBuffer;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public class AnimatedPreviewGenerator extends ApplicationAdapter {
    public static final int FRAMERATE = 30;
    Skin        skin;
    Stage       stage;
    SpriteBatch batch;
    TypingLabel label;
    TypingLabel labelEvent;
    AnimatedGif gif;
    Array<Pixmap> pms = new Array<>(Pixmap.class);

    @Override
    public void create() {
        gif = new AnimatedGif();

        adjustTypingConfigs();

        batch = new SpriteBatch();
        skin = new FWSkin(Gdx.files.internal("uiskin.json"));
        stage = new Stage(new StretchViewport(720, 480), batch);
        Gdx.input.setInputProcessor(stage);

        final Table table = new Table();

        stage.addActor(table);
        table.setFillParent(true);

        label = createTypingLabel();

        labelEvent = new TypingLabel("", KnownFonts.getOpenSans());
        labelEvent.setAlignment(Align.left);
        labelEvent.pause();
        labelEvent.setVisible(false);

        table.pad(20f);
        table.add(label).colspan(5).growX();
        table.row();
        table.add(labelEvent).colspan(5).align(Align.center);

        table.pack();
    }

    public void adjustTypingConfigs() {
        // Only allow two chars per frame
        TypingConfig.CHAR_LIMIT_PER_FRAME = 2;

        TypingConfig.DEFAULT_SPEED_PER_CHAR = 0.0666f;

        // Change color used by CLEARCOLOR token
        TypingConfig.DEFAULT_CLEAR_COLOR = Color.WHITE;

        // Create some global variables to handle style
        TypingConfig.GLOBAL_VARS.put("FIRE_WIND", "{GRADIENT=ORANGE;DB6600;-0.5;5}{WIND=2;4;0.5;0.5}");
    }

    public TypingLabel createTypingLabel() {
        final StringBuilder text = new StringBuilder();
        text.append("{SLOWER}{GRADIENT=FF70F1;light exciting pink orange with ignored words;-0.5;5}{EASE=-8;2;1}{SHRINK=2;5}[@Medieval]Welcome,{ENDSHRINK}[%] [@]{WAIT}");
        text.append("{SPIRAL=2;0.5;-2.5}{STYLE=/}{STYLE=;}[%^SHADOW]{VAR=title}[%]{STYLE=;}{STYLE=/}{ENDSPIRAL}![ ] ");
        text.append("{TRIGGER=lightest violet}[lightest violet][+🤔][ ]{WAIT=0.8}");
        text.append("{NORMAL}\n\n");
        text.append("{RESET}[@Sans]{MEET}This is{ENDMEET} a [*][MAROON][%?SHINY]simple[WHITE][*] [%?blacken]test[%][@]{ENDATTENTION} to {SPIN}show you{ENDSPIN}");
        text.append("{GRADIENT=27C1F5;2776E7;-0.5;5} {CROWD=20;1;forever}how to make dialogues{ENDCROWD} {JUMP}{SLOW}[*][/]fun[/][*] again! ");
        text.append("{ENDGRADIENT}[+🥳]{ENDJUMP}{WAIT}\n");
        text.append("{NORMAL}{CLEARCOLOR}{JOLT=1;0.8;inf;0.25;dddddd;fff0cc}With this library{ENDJOLT} [LIGHTER RICH gold]you[WHITE] ");
        text.append("can {SQUASH}{SIZE=150%}[_]control[_]{ENDSQUASH} {SIZE=%75}the{SIZE=150%} flow[^][SKY] [[citation needed][ ] ");
        text.append("of {SLOWER}{ZIPPER}the text{ENDZIPPER}{NORMAL} with {BLINK=FF6BF3;FF0582;3}tokens{ENDBLINK},{WAIT=0.7}");
        text.append("{SPEED=2.50}{COLOR=lighter dull GREEN} making the text go {SHAKE=1.1;0.6;inf}[@Future]really fast[@]{ENDSHAKE}{WAIT=0.5} ");
        text.append("{SPEED=0.25}{COLOR=jade fern}{WAVE=0.66;1;0.5;∞}[@Mono] or extremely slow.[@]{ENDWAVE}");
        text.append("{RESET} You {HEARTBEAT}[darker red]can also wait[#FFFFFF]{ENDHEARTBEAT} for a {EASE=-15;2;1}[black][%?whiten]second[ ]{ENDEASE}{WAIT=1} ");
        text.append("{EASE=15;8;1}{COLOR=#E6DB74}or two{CLEARCOLOR}{ENDEASE}{WAIT=2}, ");
        text.append("[%?Error]jussst[%][.][red][@Canada] spelling[ ] to [%?WARN]catching[%][.][#FFD510FF][@Canada] grammar[ ] an ");
        text.append("[@Console][;]event[;][@] in [%?note]code[%][.][#3088B8FF][@Canada] cool[ ]{EVENT=example}!{WAIT} ");
        text.append("{NORMAL}\n\n");
        text.append("[lighter blue violet]{CAROUSEL}Imagine{ENDCAROUSEL}[] the [rich green]{SLAM}[~]bugs[][]!{ENDSLAM} I mean, {RAINBOW=1;1;0.7}possibilities{ENDRAINBOW}! ");
        text.append("{SPEED=0.1}{CANNON}[+🔥][+😁][+👏]{WAIT=2} {RESET}");

//        Font font = KnownFonts.addEmoji(KnownFonts.getStandardFamily());
        Font font = KnownFonts.addNotoEmoji(KnownFonts.getStandardFamily());
        final TypingLabel label = new TypingLabel(text.toString(), font);
        label.setAlignment(Align.left);
        label.setDefaultToken("{EASE}{FADE=0;1;0.33}");

        // Make the label wrap to new lines, respecting the table's layout.
        label.layout.maxLines = 15;
//        label.layout.setTargetWidth(620);

        // Set variable replacements for the {VAR} token
        label.setVariable("title", "curious typist");

        // Set an event listener for when the {EVENT} token is reached and for the char progression ends.
        label.setTypingListener(new TypingAdapter() {
            @Override
            public void event(String event) {
                System.out.println("Event: " + event);

                if("example".equals(event)) {
                    labelEvent.restart("{FADE}{SLIDE=2;1;1}{FASTER}{COLOR=GRAY}Event:{WAIT=0.1}{COLOR=LIME} " + event);
                    labelEvent.clearActions();
                    labelEvent.addAction(
                            sequence(
                                    visible(true),
                                    alpha(0),
                                    alpha(1, 0.25f, Interpolation.pow2In),
                                    delay(0.5f),
                                    alpha(0, 2f, Interpolation.pow2)
                            )
                    );
                }
            }

            @Override
            public void end() {
                QualityPalette pal = new QualityPalette();
                pal.analyze(pms);
                gif.setPalette(pal);
                gif.setDitherAlgorithm(Dithered.DitherAlgorithm.NONE);
                gif.setDitherStrength(0.5f);
                gif.write(Gdx.files.local("preview.gif"), pms, FRAMERATE);
                Gdx.app.exit();
            }
        });

        label.setWrap(true);

        return label;
    }

    public void update(float delta) {
        stage.act(delta);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.4f, 0.4f, 0.4f, 1);

        update(1f/FRAMERATE);

        stage.draw();
        // Modified Pixmap.createFromFrameBuffer() code that uses RGB instead of RGBA
        Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
        final Pixmap pm = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), Pixmap.Format.RGB888);
        ByteBuffer pixels = pm.getPixels();
        Gdx.gl.glReadPixels(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, pixels);
        // End Pixmap.createFromFrameBuffer() modified code

        pms.add(pm);
//        pms.add(Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
    }

    @Override
    public void resize(int width, int height) {
//        label.getFont().resizeDistanceField(width, height);
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
        config.setWindowedMode(720, 480);
        config.setResizable(true);
        config.setForegroundFPS(FRAMERATE);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new AnimatedPreviewGenerator(), config);
    }
}
