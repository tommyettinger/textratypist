package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.github.tommyettinger.textra.utils.ColorUtils;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.visible;

public class TypingLabelTest extends ApplicationAdapter {
    Skin        skin;
    Stage       stage;
    SpriteBatch batch;
    TypingLabel label;
    TypingLabel labelEvent;
    TextButton  buttonPause;
    TextButton  buttonResume;
    TextButton  buttonRestart;
    TextButton  buttonRebuild;
    TextButton  buttonSkip;
    Color flashColor = new Color(1, 1, 0.6f, 1f);

    @Override
    public void create() {
        adjustTypingConfigs();

        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("uiskin.json"));
//        skin.getAtlas().getTextures().iterator().next().setFilter(TextureFilter.Linear, TextureFilter.Linear);
        BitmapFont bmf = skin.getFont("default-font");
        bmf.getData().setScale(0.5f);
        bmf.setUseIntegerPositions(false);
        stage = new Stage(new StretchViewport(720, 405), batch);
        Gdx.input.setInputProcessor(stage);

        final Table table = new Table();

//        table.debugAll();

        stage.addActor(table);
        table.setFillParent(true);

        label = createTypingLabel();

        labelEvent = new TypingLabel("",
                new Font(new BitmapFont(Gdx.files.internal("OpenSans-standard.fnt")), Font.DistanceFieldType.STANDARD,
                        0f, 0f, 0f, 0f).scale(0.5f, 0.5f).setTextureFilter());
        labelEvent.setAlignment(Align.left);
        labelEvent.pause();
        labelEvent.setVisible(false);

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
                labelEvent.setVisible(false);
                labelEvent.restart();
                labelEvent.pause();
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
                labelEvent.setVisible(false);
                labelEvent.restart();
                labelEvent.pause();
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
        table.add(labelEvent).colspan(5).align(Align.center);
        table.row().uniform().expand().growX().space(40).align(Align.bottom);
        table.add(buttonPause, buttonResume, buttonRestart, buttonSkip, buttonRebuild);

        table.pack();
    }

    public void adjustTypingConfigs() {
        // Only allow two chars per frame
        TypingConfig.CHAR_LIMIT_PER_FRAME = 2;

        // Change color used by CLEARCOLOR token
        TypingConfig.DEFAULT_CLEAR_COLOR = Color.WHITE;

        // Create some global variables to handle style
        TypingConfig.GLOBAL_VARS.put("FIRE_WIND", "{FASTER}{GRADIENT=ORANGE;DB6600;-0.5;5}{SLOWER}{WIND=2;4;0.5;0.5}");
    }

    public TypingLabel createTypingLabel() {
        // Create text with tokens
//        final StringBuilder text = new StringBuilder();
//        text.append("Cool{STYLE=^}{COLOR=SKY}[[citation needed]{STYLE=^}{COLOR=WHITE}.");

//        final StringBuilder text = new StringBuilder();
//        text.append(" {RESET}You can also wait for a {EASE=-15;2;1}second{ENDEASE}\n\n{WAIT=1} {EASE=15;8;1}{COLOR=#E6DB74}or two{CLEARCOLOR}{ENDEASE}{WAIT=2},");
//        text.append("{RAINBOW=1;1;0.7;0.5} just to catch an event in code{EVENT=example}!{WAIT} {ENDRAINBOW}");

//        final StringBuilder text = new StringBuilder();
//        text.append("Welcome, curious human!");
//        text.append("\n\n");
//        text.append("This is a simple test to show you ");
//        text.append("how to make dialogues fun again!\n");
//        text.append("With this library you can control the flow of the text with ");
//        text.append("tokens, making the text go\nreally fast ");
//        text.append("or extremely slow. ");
//        text.append("You can also wait for a second or two, ");
//        text.append("just to catch an\nevent in code!\n\n");
//        text.append("Imagine the possibilities! =D");

//        final StringBuilder text = new StringBuilder("Fonts can be rendered normally, but using {{TAGS}, you can..."
//                + "\n{COLOR=#E74200}...use CSS-style hex colors like #E74200..."
//                + "\n{COLOR=FOREST}...use named colors from the Colors class, like FOREST...{RESET}"
//                + "\n...and use {STYLE=!}effects{STYLE=!}!"
//                + "\nNormal, {STYLE=*}bold{STYLE=*}, {STYLE=/}oblique{STYLE=/} (like italic), {STYLE=*}{STYLE=/}bold oblique{RESET},"
//                + "\n{STYLE=_}underline (even for multiple words){STYLE=_}, {STYLE=~}strikethrough (same){RESET},"
//                + "\nvarious heights: {STYLE=.}sub-{STYLE=.}, {STYLE==}mid-{STYLE==}, and {STYLE=^}super-{STYLE=^}script,"
//                + "\ncapitalization changes: {STYLE=;}each cap, {STYLE=,}All Lower, {STYLE=!}Caps lock{RESET},"
//                + "\nUnicode support: Pchnąć w tę łódź {COLOR=BROWN}jeża{RESET} lub ośm skrzyń {COLOR=PURPLE}fig{RESET}."
//                + "\nWELCOME {STYLE=/}TO THE {STYLE=*}{COLOR=GREEN}JUNGLE{RESET}.");

//        final StringBuilder text = new StringBuilder("Fonts can be rendered normally, but using [[TAGS], you can..."
//                + "\n[#E74200]...use CSS-style hex colors like #E74200..."
//                + "\n[FOREST]...use named colors from the Colors class, like FOREST...[]"
//                + "\n...and use [!]effects[!]!"
//                + "\nNormal, [*]bold[*], [/]oblique[/] (like italic), [*][/]bold oblique[],"
//                + "\n[_]underline (even for multiple words)[_], [~]strikethrough (same)[],"
//                + "\nvarious heights: [.]sub-[.], [=]mid-[=], and [^]super-[^]script,"
//                + "\ncapitalization changes: [;]Each cap, [,]All lower, [!]Caps lock[],"
//                + "\nUnicode support: Pchnąć w tę łódź [BROWN]jeża[] lub ośm skrzyń [PURPLE]fig[]."
//                + "\nWELCOME [/]TO THE [*][GREEN]JUNGLE[].");

//        final StringBuilder text = new StringBuilder();
//        text.append("Welcome, curious human!\n");
//        text.append("This is a simple test to show you ");
//        text.append("how to make dialogues fun again! ");
//        text.append("With this library you can control the flow of the text with ");
//        text.append("tokens, making the text go really fast ");
//        text.append("or extremely slow. ");
//        text.append("You can also wait for a second or two, ");
//        text.append("just to catch an event in code!\n\n");
//        text.append("Imagine the possibilities! =D");

        final StringBuilder text = new StringBuilder();
        text.append("{SLOWER}{GRADIENT=FF70F1;light exciting pink orange of justice;-0.5;5}{EASE=-8;2;1}{SHRINK=2;5}[%125][@Medieval]Welcome,[%]{ENDSHRINK}[@] {WAIT}{SPIRAL=2;0.5;-2.5}{STYLE=/}{STYLE=;}{VAR=title}{STYLE=;}{STYLE=/}{ENDSPIRAL}![] {TRIGGER=lightest violet}[lightest violet][_][+🤔][]{WAIT=0.8}");
        text.append("{FAST}\n\n");
        text.append("{RESET}[@Sans]{ATTENTION}This is a [*][YELLOW]simple[WHITE][*] test[@]{ENDATTENTION} to {SPIN}show you{ENDSPIN}");
        text.append("{GRADIENT=27C1F5;2776E7;-0.5;5} {CROWD=20;1;forever}how to make dialogues{ENDCROWD} {JUMP}{SLOW}[*][/]fun[/][*] again! {ENDGRADIENT}[+🥳]{ENDJUMP}{WAIT}\n");
        text.append("{NORMAL}{CLEARCOLOR}{JOLT=1;0.8;inf;0.25;dddddd;fff0cc}With this library{ENDJOLT} [LIGHTER RICH gold]you[WHITE] can {SQUASH}{SIZE=150%}[_]control[_]{ENDSQUASH} {SIZE=%75}the{SIZE=150%} flow[^][SKY] [[citation needed][] of the text with");
        text.append(" {BLINK=FF6BF3;FF0582;3}tokens{ENDBLINK},{WAIT=0.7}");
        text.append("{SPEED=2.50}{COLOR=lighter dull GREEN} making the text go {SHAKE=1.1;0.6;inf}[@Future]really fast[@]{ENDSHAKE}{WAIT=0.5} ");
        text.append("{SPEED=0.25}{COLOR=jade fern}{WAVE=0.66;1;0.5;∞}[@Mono] or extremely slow.[@]{ENDWAVE}");
        text.append("{RESET} You {HEARTBEAT}[darker red]can also wait[#FFFFFF]{ENDHEARTBEAT} for a {EASE=-15;2;1}second{ENDEASE}{WAIT=1} {EASE=15;8;1}{COLOR=#E6DB74}or two{CLEARCOLOR}{ENDEASE}{WAIT=2},");
        text.append("{RAINBOW=1;1;0.7} just to catch an event in code{EVENT=example}!{WAIT} {ENDRAINBOW}");
        text.append("{NORMAL}\n\n");
        text.append("{VAR=FIRE_WIND}Imagine the {STYLE=STRIKE}bugs{STYLE=STRIKE} possibilities! {ENDGRADIENT}[+🔥][+😁][+👏] {RESET}");

//        text.append("{SLOWER}{GRADIENT=FF70F1;FFC300;-0.5;5}{EASE=-8;2;1}{SHRINK=2;5}[%125][@Medieval]Welcome,[%]{ENDSHRINK}[@] {WAIT}{SPIRAL=2;0.5;-2.5}{STYLE=/}{STYLE=;}{VAR=title}{STYLE=;}{STYLE=/}{ENDSPIRAL}![] [+🤔]{ENDEASE}{WAIT=0.8}");
//        text.append("{FAST}\n\n");
//        text.append("{RESET}{HANG=0.7}[@Sans]This is a [*][YELLOW]simple[WHITE][*] test[@]{ENDHANG} to {SPIN}show you{ENDSPIN}");
//        text.append("{GRADIENT=27C1F5;2776E7;-0.5;5} {CROWD}how to make dialogues{ENDCROWD} {JUMP}{SLOW}[*][/]fun[/][*] again! {ENDGRADIENT}[+🥳]{ENDJUMP}{WAIT}\n");
//        text.append("{NORMAL}{CLEARCOLOR}{JOLT=1;0.8;inf;0.25;dddddd;fff0cc}With this library{ENDJOLT} you can {SQUASH}{SIZE=150%}[_]control[_]{ENDSQUASH} {SIZE=%75}the{SIZE=150%} flow[^][SKY] [[citation needed][] of the text with");
//        text.append(" {BLINK=FF6BF3;FF0582;3}tokens{ENDBLINK},{WAIT=0.7}");
//        text.append("{SPEED=2.50}{COLOR=#84DD60} making the text go {SHAKE=1;1;3}[@Future]really fast[@]{ENDSHAKE}{WAIT=0.5} ");
//        text.append("{SPEED=0.25}{COLOR=#A6E22D}{WAVE=0.66}[@Mono] or extremely slow.[@]{ENDWAVE}");
//        text.append("{RESET} You {HEARTBEAT}[#AA0000]can also wait[#FFFFFF]{ENDHEARTBEAT} for a {EASE=-15;2;1}second{ENDEASE}{WAIT=1} {EASE=15;8;1}{COLOR=#E6DB74}or two{CLEARCOLOR}{ENDEASE}{WAIT=2},");
//        text.append("{RAINBOW=1;1;0.7} just to catch an event in code{EVENT=example}!{WAIT} {ENDRAINBOW}");
//        text.append("{NORMAL}\n\n");
//        text.append("{VAR=FIRE_WIND}Imagine the {STYLE=STRIKE}bugs{STYLE=STRIKE} possibilities! {ENDGRADIENT}[+🔥][+😁][+👏] {RESET}");

//        Array<String> names = KnownFonts.getStandardFamily().family.fontAliases.keys().toArray();
//        names.sort();
//        for(String name : names)
//            System.out.println(name);

        // Create label
//        Font font = KnownFonts.getGentiumSDF().scaleTo(36, 36).adjustLineHeight(0.8125f);
        Font.FontFamily family = new Font.FontFamily(
                new String[]{
                        "Serif", "Sans", "Mono", "Medieval", "Future", "Cozette"
                },
                new Font[]{
                        KnownFonts.addEmoji(KnownFonts.getGentium()).scaleTo(32, 35).scale(0.75f, 0.75f),
                        KnownFonts.addEmoji(KnownFonts.getOpenSans()).scaleTo(23, 35).scale(0.75f, 0.75f).adjustLineHeight(0.85f),
                        KnownFonts.addEmoji(KnownFonts.getInconsolata()).scaleTo(15, 35).scale(0.75f, 0.75f).adjustLineHeight(0.9375f),
                        KnownFonts.addEmoji(KnownFonts.getKingthingsFoundation()).scaleTo(35, 35).scale(0.75f, 0.75f).adjustLineHeight(0.9f),
                        KnownFonts.addEmoji(KnownFonts.getOxanium()).scaleTo(32, 35).scale(0.75f, 0.75f).adjustLineHeight(1.05f),
                        KnownFonts.addEmoji(KnownFonts.getCozette())
                });
        Font font = family.connected[0].setFamily(family);
        final TypingLabel label = new TypingLabel(text.toString(), font);
        label.setAlignment(Align.left);
        label.setDefaultToken("{EASE}{FADE=0;1;0.33}");

        // Make the label wrap to new lines, respecting the table's layout.
        label.layout.maxLines = 15;
//        label.layout.setTargetWidth(620);

        // Set variable replacements for the {VAR} token
        label.setVariable("title", "curious human");

        label.selectable = true;
        label.trackingInput = true;

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
                } else if("*SELECTED".equals(event)) {
                    System.out.println("Selection start: " + label.selectionStart + " Selection end: " + label.selectionEnd);
                    if(label.copySelectedText())
                        System.out.println(label.getSelectedText());
                    else
                        System.out.println("Nothing was copied.");
                } else {
                    Color.rgba8888ToColor(flashColor, ColorUtils.describe(event));
                    ScreenUtils.clear(flashColor);
                }
            }

            @Override
            public void end() {
                System.out.println("End");
                label.skipToTheEnd(false, false);
            }
        });

        label.setWrap(true);

        // Finally parse tokens in the label text.
        label.parseTokens();

        return label;
    }

    public void update(float delta) {
        stage.act(delta);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update(Gdx.graphics.getDeltaTime());

        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        label.getFont().resizeDistanceField(width, height);
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
        new Lwjgl3Application(new TypingLabelTest(), config);
    }
}
