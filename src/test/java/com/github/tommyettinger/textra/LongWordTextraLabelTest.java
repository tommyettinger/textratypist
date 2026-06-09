package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class LongWordTextraLabelTest extends ApplicationAdapter {
    Stage       stage;
    SpriteBatch batch;
    TextraLabel label;
    Layout secondLayout;
    Font font;

    @Override
    public void create() {
        adjustTypingConfigs();
        font = KnownFonts.addEmoji(KnownFonts.getFont(KnownFonts.GENTIUM, Font.DistanceFieldType.MSDF));

        batch = new SpriteBatch();
        stage = new Stage(new StretchViewport(920, 405), batch);
//        stage.setDebugAll(true);
        Gdx.input.setInputProcessor(stage);

        final Table table = new Table();
        stage.addActor(table);
//        table.setFillParent(true);
        table.setSize(720, 400);
        table.setPosition(50, 150);

        label = createTextraLabel();
        label.debug();
        label.setAlignment(Align.center);
        table.pad(50f);
        table.add(label).width(720);
        table.row();
        table.row().uniform().expand().growX().space(40).center();

        table.pack();
//        secondLayout = new Layout(font);
//        secondLayout.setTargetWidth(720);
//        font.markup(
//                  "I love Textra[+]Typist!\n"
//                + "*[/]Plays a [#B10F]romantic[] [/][+🎷][/] solo[/]*\n"
//                + "ButThisIsAnExtremelyVeryVeryVeryVeryLongWordThatInterferesWithWrappingSanelyAndAllPrettyLike!\n"
//                + "Maybe this works? [+party popper] [*]Whee[]!\n"
//                , secondLayout);
    }

    public void adjustTypingConfigs() {
        // Only allow two chars per frame
        TypingConfig.CHAR_LIMIT_PER_FRAME = 2;

        // Change color used by CLEARCOLOR token
        TypingConfig.DEFAULT_CLEAR_COLOR = Color.WHITE;

        // Create some global variables to handle style
        TypingConfig.GLOBAL_VARS.put("ICE_WIND", "{GRADIENT=88ccff;eef8ff;-0.5;5}{WIND=2;4;0.25;0.1}{JOLT=1;0.6;inf;0.1;;}");
    }

    public TextraLabel createTextraLabel() {
        final TextraLabel label = new TextraLabel(
                        "I love Textra[+]Typist!\n*[/]Plays a [#B10F]romantic[] [/][+🎷][/] solo[/]*\n"
                        + "ButThisIsAnExtremelyVeryVeryVeryVeryLongWordThatInterferesWithWrappingSanelyAndCleanly!\n"
                        + "Maybe this works? [+party popper] [*]Whee[]!"
                ,
                font);
        label.setAlignment(Align.center);
        label.debug();
        // Make the label wrap to new lines, respecting the table's layout.
        label.setWrap(true);

        return label;
    }

    public void update(float delta) {
        stage.act(delta);
    }

    @Override
    public void render() {
        update(Gdx.graphics.getDeltaTime());

        ScreenUtils.clear(0.25f, 0.3f, 0.3f, 1);

        stage.act();
        stage.draw();

//        stage.getBatch().begin();
//        font.drawGlyphs(stage.getBatch(),
//                secondLayout,
//                Gdx.graphics.getBackBufferWidth() * 0.5f, 190f, Align.center
//        );
//        stage.getBatch().end();
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
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("TextraLabel Test");
        config.setWindowedMode(920, 405);
        config.setResizable(true);
        config.setForegroundFPS(0);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new LongWordTextraLabelTest(), config);
    }
}
