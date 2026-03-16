package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class GameIconSDFTypingLabelTest extends ApplicationAdapter {
    Skin        skin;
    Stage       stage;
    SpriteBatch batch;
    TypingLabel label;

    @Override
    public void create() {
        adjustTypingConfigs();

        batch = new SpriteBatch();
        skin = new FWSkin(Gdx.files.internal("uiskin.json"));
//        skin.getAtlas().getTextures().iterator().next().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        skin.getFont("default-font");//.getData().setScale(0.5f);
        stage = new Stage(new StretchViewport(720, 405), batch);
//        stage.setDebugAll(true);
        Gdx.input.setInputProcessor(stage);

        final Table table = new Table();
        stage.addActor(table);
        table.setFillParent(true);

        label = createTypingLabel();

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

        TypingConfig.setDefaultInitialText("[#]{EASE}");

        // Create some global variables to handle style
        TypingConfig.GLOBAL_VARS.put("ICE_WIND", "{GRADIENT=88ccff;eef8ff;-0.5;5}{WIND=2;4;0.25;0.1}{JOLT=1;0.6;inf;0.1;;}");
    }

    public TypingLabel createTypingLabel() {
//        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("dawnlike/Dawnlike.atlas"), Gdx.files.internal("dawnlike"));
//        Font font = new Font("dawnlike/PlainAndSimplePlus-standard.fnt", atlas.findRegion("PlainAndSimplePlus-standard"), 0, 0, 2, 2);
//        font.addImage("😀", atlas.findRegion("love box")).addImage("💀", atlas.findRegion("hate box"));
//        font.scale(2, 2);
        Font font = KnownFonts.getNowAlt(Font.DistanceFieldType.SDF);
        font.addAtlas(new TextureAtlas(Gdx.files.internal("Game-Icons-sdf.atlas")), "", "", 0f, 0f, 0f);

        // Create label
        final TypingLabel label = new TypingLabel(
                "I love TextraTypist! [+keyboard] [gold][+saxophone]{HEARTBEAT}[deep richer pink][+heart-wings] OMG! [+heart-wings]{ENDHEARTBEAT}[rich apricot][+party-popper][white]\n"
                + "But...{SICK}[_][purple][+ophiuchus][white][_]. Nitty{ENDSICK} {CROWD}doesn't. [green][+vomiting] [#BB1100][+broken-skull][#55AA22][+clown]{ENDCROWD}{CLEARCOLOR}\n"
                + "\n{RAINBOW}[+pineapple][+raccoon-head][+pterodactylus][+raised-fist][+wolf-howl][+western-hat][%]\n"
                ,
                font);
        label.align = Align.topLeft;
//        label.debug();
        // Make the label wrap to new lines, respecting the table's layout.
        label.wrap = true;
        label.setWidth(Gdx.graphics.getBackBufferWidth() - 100);

//        label.setDefaultToken("{EASE}{FADE=0;1;0.33}");
//
//        // Set an event listener for when the {EVENT} token is reached and for the char progression ends.
//        label.setTypingListener(new TypingAdapter() {
//            @Override
//            public void event(String event) {
//                System.out.println("Event: " + event);
//            }
//
//            @Override
//            public void end() {
//                System.out.println(label.getIntermediateText());
//            }
//        });
//
//        // Finally parse tokens in the label text.
//        label.parseTokens();

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
        config.setForegroundFPS(60);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new GameIconSDFTypingLabelTest(), config);
    }
}
