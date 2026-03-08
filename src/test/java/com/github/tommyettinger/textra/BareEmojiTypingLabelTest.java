package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import regexodus.Replacer;

public class BareEmojiTypingLabelTest extends ApplicationAdapter {
    Skin        skin;
    Stage       stage;
    SpriteBatch batch;
    TypingLabel label;
    Font font;
    Replacer processor;

    @Override
    public void create() {
        font = KnownFonts.addEmoji(KnownFonts.getFont(KnownFonts.GENTIUM, Font.DistanceFieldType.MSDF));
        processor = EmojiProcessor.getReplacer(font);
        adjustTypingConfigs();

        batch = new SpriteBatch();
        skin = new FreeTypistSkin(Gdx.files.internal("uiskin.json"));
        stage = new Stage(new ExtendViewport(720, 405), batch);
        stage.setDebugAll(true);
        Gdx.input.setInputProcessor(stage);

        final Table table = new Table();
        stage.addActor(table);
        table.setFillParent(true);

        label = createTypingLabel();

        label.debug();
        label.setAlignment(Align.center);
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

    public TypingLabel createTypingLabel() {
        // Create label
        final TypingLabel label = new TypingLabel(
                processor.replace(
                        "I love Textra[+]Typist! 🎷{HEARTBEAT}😍{ENDHEARTBEAT}🎷\n"
                        + "But... {COLOR=#79c353ff}{SICK}U. Nitty{ENDSICK}{ENDCOLOR} doesn't. {CROWD}{SLIP}[#BB1100]💀[#55AA22FF]🤡[ ]\n"
                        + "That's OK, I don't like loot crates anyway. {CROWD}{SLIP}[#B10F]🎉[#5A2]🥳[ ]\n"
                        + "⚖️[~][_]⚖️[ ] testing: [_][~]\n"
                        + "[%25]go[%50]go[%75]go[red][%100]go[white][%125]go[%150]go[%175]go[%200]go[%225]go[%250]go![ ]")
                ,
                font, Color.LIGHT_GRAY);
        label.setAlignment(Align.center);
        label.debug();
        // Make the label wrap to new lines, respecting the table's layout.
        label.setWrap(true);
        return label;
    }

    public void update(float delta) {
        if(Gdx.input.isKeyJustPressed(Input.Keys.S) && !label.hasEnded())
            label.skipToTheEnd();
        label.font.strikeBreadth = MathUtils.sinDeg((TimeUtils.millis() & 0xFFFFFL) * 0.0625f) * 0.5f;
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
        font.resizeDistanceField(width, height, stage.getViewport());
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
        new Lwjgl3Application(new BareEmojiTypingLabelTest(), config);
    }
}
