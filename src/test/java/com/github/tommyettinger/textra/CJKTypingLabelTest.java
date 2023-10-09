package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
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

public class CJKTypingLabelTest extends ApplicationAdapter {
    Skin        skin;
    Stage       stage;
    SpriteBatch batch;
    TypingLabel label;
    TextButton  buttonPause;
    TextButton  buttonResume;
    TextButton  buttonRestart;
    TextButton  buttonRebuild;
    TextButton  buttonSkip;
    int adj = 0;

    @Override
    public void create() {
        adjustTypingConfigs();

        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("uiskin.json"));
//        skin.getAtlas().getTextures().iterator().next().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        skin.getFont("default-font");//.getData().setScale(0.5f);
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
//                label.skipToTheEnd();
                Cell<TypingLabel> labelCell = table.getCell(label);
                table.pack();
//                System.out.println("Label height: " + labelCell.getActorHeight()
//                        + ", cell max height: " + labelCell.getMaxHeight()
//                        + ", cell pref height: " + labelCell.getPrefHeight());

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
//                label.skipToTheEnd();
                table.pack();
//                System.out.println("Label height: " + labelCell.getActorHeight()
//                        + ", cell max height: " + labelCell.getMaxHeight()
//                        + ", cell pref height: " + labelCell.getPrefHeight());
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


    protected BitmapFont newBitmapFont() {
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 15;

        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("sc/NotoSansCJKsc-Regular.otf"));
        parameter.incremental = true;
        parameter.characters = "howdY" + "汉仪拜基火云体简 汉仪拜基火云体繁 汉仪拜基火云体W\n" +
                "汉仪报宋简 汉仪报宋繁\n" +
                "汉仪碑刻黑简 汉仪碑刻黑繁 汉仪碑刻黑W";

        FreeTypeFontGenerator.setMaxTextureSize(128);
        FreeTypeFontGenerator.FreeTypeBitmapFontData data = new FreeTypeFontGenerator.FreeTypeBitmapFontData() {
            public int getWrapIndex(Array<BitmapFont.Glyph> glyphs, int start) {
                int i = start - 1;
                for (; i >= 1; i--) {
                    int startChar = glyphs.get(i).id;
                    if (!legalAtStart(startChar)) continue;
                    int endChar = glyphs.get(i - 1).id;
                    if (!legalAtEnd(endChar)) continue;
                    // Don't wrap between ASCII chars.
                    if (startChar < 127 && endChar < 127 && !Character.isWhitespace(startChar)) continue;
                    return i;
                }
                return start;
            }

            private boolean legalAtStart(int ch) {
                switch (ch) {
                    case '!':
                    case '%':
                    case ')':
                    case ',':
                    case '.':
                    case ':':
                    case ';':
                    case '>':
                    case '?':
                    case ']':
                    case '}':
                    case '¢':
                    case '¨':
                    case '°':
                    case '·':
                    case 'ˇ':
                    case 'ˉ':
                    case '―':
                    case '‖':
                    case '’':
                    case '”':
                    case '„':
                    case '‟':
                    case '†':
                    case '‡':
                    case '›':
                    case '℃':
                    case '∶':
                    case '、':
                    case '。':
                    case '〃':
                    case '〆':
                    case '〈':
                    case '《':
                    case '「':
                    case '『':
                    case '〕':
                    case '〗':
                    case '〞':
                    case '﹘':
                    case '﹚':
                    case '﹜':
                    case '！':
                    case '＂':
                    case '％':
                    case '＇':
                    case '）':
                    case '，':
                    case '．':
                    case '：':
                    case '；':
                    case '？':
                    case '］':
                    case '｀':
                    case '｜':
                    case '｝':
                    case '～':
                        return false;
                    default:
                        return true;
                }
            }

            private boolean legalAtEnd(int ch) {
                switch (ch) {
                    case '$':
                    case '(':
                    case '*':
                    case ',':
                    case '£':
                    case '¥':
                    case '·':
                    case '‘':
                    case '“':
                    case '〈':
                    case '《':
                    case '「':
                    case '『':
                    case '【':
                    case '〔':
                    case '〖':
                    case '〝':
                    case '﹗':
                    case '﹙':
                    case '﹛':
                    case '＄':
                    case '（':
                    case '．':
                    case '［':
                    case '｛':
                    case '￡':
                    case '￥':
                        return false;
                    default:
                        return true;
                }
            }
        };

        // By default, latin chars are used for x and cap height, causing some fonts to display non-latin chars out of bounds.
        data.xChars = new char[]{'动'};
        data.capChars = new char[]{'动'};

        return fontGenerator.generateFont(parameter, data);
    }

    public TypingLabel newTypingLabel(String text) {
        Font.FontFamily family = new Font.FontFamily(
                new String[]{
                        "HYYiHeXianJingW"
                },
                new Font[]{
                        new Font(newBitmapFont()),
                });
        Font font = family.connected[0].setFamily(family);
        final TypingLabel label = new TypingLabel(text, font);

        label.parseTokens();

        return label;
    }

    public TypingLabel createTypingLabel() {
        Font font = KnownFonts.getStandardFamily();
        for(Font f : font.family.connected) {
            if(f != null)
                KnownFonts.addEmoji(f);
        }

        final TypingLabel label = newTypingLabel("{SPIRAL=2;0.5;-2.5}{STYLE=/}[%^SHADOW]汉仪拜基火云体简[%]{STYLE=/}{ENDSPIRAL} " +
                "汉仪拜基火云体繁 汉仪拜基火云体W\n" +
                "汉仪报宋简 [RED]汉仪报宋繁[]\n" +
                "[GREEN]汉仪碑刻黑简[] 汉仪碑刻黑繁 汉仪碑刻黑W");
        label.setDefaultToken("{EASE}{FADE=0;1;0.33}{SLOW}");
        label.align = Align.topLeft;

        // Make the label wrap to new lines, respecting the table's layout.
        label.setWrap(true);
        label.layout.maxLines = 15;

        // Finally parse tokens in the label text.
//        label.parseTokens();

        return label;
    }

    public void update(float delta) {
        stage.act(delta);
    }

    @Override
    public void render() {
        update(Gdx.graphics.getDeltaTime());

        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);
//        if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT))
//        {
//            adj = adj + 15 & 15;
//            System.out.println("Adjusting " + label.font.family.connected[adj].name);
//        }
//        else if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT))
//        {
//            adj = adj + 1 & 15;
//            System.out.println("Adjusting " + label.font.family.connected[adj].name);
//        }
//        if(Gdx.input.isKeyJustPressed(Input.Keys.UP))
//            System.out.println(label.font.family.connected[adj].setDescent(label.font.family.connected[adj].descent + 1).descent + " " + label.font.family.connected[adj].name);
//        else if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN))
//            System.out.println(label.font.family.connected[adj].setDescent(label.font.family.connected[adj].descent - 1).descent + " " + label.font.family.connected[adj].name);
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
        config.setTitle("TypingLabel CJK Test");
        config.setWindowedMode(720, 405);
        config.setResizable(false);
        config.setForegroundFPS(60);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new CJKTypingLabelTest(), config);
    }
}
