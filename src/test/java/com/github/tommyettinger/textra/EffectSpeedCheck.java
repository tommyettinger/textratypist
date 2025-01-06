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
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 */
public class EffectSpeedCheck extends ApplicationAdapter {
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
                KnownFonts.getAStarryTall(Font.DistanceFieldType.MSDF).scaleHeightTo(20);
//                KnownFonts.getStandardFamily();
//                KnownFonts.getGentiumSDF().scale(1.1f, 1.1f).multiplyCrispness(1.3f);
        // KNOWN EFFECTS THAT NEED TO USE EXTENT:
        // CANNON, EASE, HANG, SHRINK, SLAM, SLIDE, SPIN, ZIPPER (already uses it)
        // NEW EFFECTS THAT SHOULD HAVE CODE CHANGED TO MATCH:
        // MEET (should use extent)
        String text =
//                "CANNON=1;10         {CANNON=1;10}This effect has a high value!{RESET}\n" +
//                "CANNON=1;0.1        {CANNON=1;0.1}This effect has a low value!{RESET}\n" +
//                "CROWD=15;10         {CROWD=15;10}This effect has a high value!{RESET}\n" +
//                "CROWD=15;0.1        {CROWD=15;0.1}This effect has a low value!{RESET}\n" +
//                "EASE=2;10           {EASE=2;10}This effect has a high value!{RESET}\n" +
//                "EASE=2;0.1          {EASE=2;0.1}This effect has a low value!{RESET}\n" +
//                "EMERGE=10           {EMERGE=10}This effect has a high value!{RESET}\n" +
//                "EMERGE=0.1          {EMERGE=0.1}This effect has a low value!{RESET}\n" +
//                "HANG=1;10           {HANG=1;10}This effect has a high value!{RESET}\n" +
//                "HANG=1;0.1          {HANG=1;0.1}This effect has a low value!{RESET}\n" +
//                "JOLT=1;10           {JOLT=1;10}This effect has a high value!{RESET}\n" +
//                "JOLT=1;0.1          {JOLT=1;0.1}This effect has a low value!{RESET}\n" +
//                "JUMP=1;1;10         {JUMP=1;1;10}This effect has a high value!{RESET}\n" +
//                "JUMP=1;1;0.1        {JUMP=1;1;0.1}This effect has a low value!{RESET}\n" +
//                "MEET=2;10           {MEET=2;10}This effect has a high value!{RESET}\n" +
//                "MEET=2;0.1          {MEET=2;0.1}This effect has a low value!{RESET}\n" +
//                "SHAKE=1;10          {SHAKE=1;10}This effect has a high value!{RESET}\n" +
//                "SHAKE=1;0.1         {SHAKE=1;0.1}This effect has a low value!{RESET}\n" +
//                "SHRINK=1;10         {SHRINK=1;10}This effect has a high value!{RESET}\n" +
//                "SHRINK=1;0.1        {SHRINK=1;0.1}This effect has a low value!{RESET}\n" +
//                "SICK=1;10           {SICK=1;10}This effect has a high value!{RESET}\n" +
//                "SICK=1;0.1          {SICK=1;0.1}This effect has a low value!{RESET}\n" +
//                "SLAM=0.3;10         {SLAM=0.3;10}This effect has a high value!{RESET}\n" +
//                "SLAM=0.3;0.1        {SLAM=0.3;0.1}This effect has a low value!{RESET}\n" +
//                "SLIDE=1;10          {SLIDE=1;10}This effect has a high value!{RESET}\n" +
//                "SLIDE=1;0.1         {SLIDE=1;0.1}This effect has a low value!{RESET}\n" +
//                "SPIN=10;5           {SPIN=10;5}This effect has a high value!{RESET}\n" +
//                "SPIN=0.1;5          {SPIN=0.1;5}This effect has a low value!{RESET}\n" +

                "SPIRAL=1;10         {SPIRAL=1;10}This effect has a high value!{RESET}\n" +
                "SPIRAL=1;0.1        {SPIRAL=1;0.1}This effect has a low value!{RESET}\n" +
                "SPUTTER=0.3;0.3;10  {SPUTTER=0.3;0.3;10}This effect has a high value!{RESET}\n" +
                "SPUTTER=0.3;0.3;0.1 {SPUTTER=0.3;0.3;0.1}This effect has a low value!{RESET}\n" +
                "SQUASH=10           {SQUASH=10}This effect has a high value!{RESET}\n" +
                "SQUASH=0.1          {SQUASH=0.1}This effect has a low value!{RESET}\n" +
                "WAVE=1;1;10         {WAVE=1;1;10}This effect has a high value!{RESET}\n" +
                "WAVE=1;1;0.1        {WAVE=1;1;0.1}This effect has a low value!{RESET}\n" +
                "ZIPPER=2;10         {ZIPPER=2;10}This effect has a high value!{RESET}\n" +
                "ZIPPER=2;0.1        {ZIPPER=2;0.1}This effect has a low value!{RESET}\n" +
                        "{WAIT=5}That's all, folks!"
                ;
        StringBuilder sb = new StringBuilder("{SPEED=SLOWER}").append(text);

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
                label.restart();
            }
        });

        return label;
    }

    public void update(float delta) {
        stage.act(delta);
    }

    @Override
    public void render() {
        update(Gdx.graphics.getDeltaTime());

        ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1);
        

        stage.draw();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        label.font.resizeDistanceField(width, height, stage.getViewport());
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
        new Lwjgl3Application(new EffectSpeedCheck(), config);
    }
}
