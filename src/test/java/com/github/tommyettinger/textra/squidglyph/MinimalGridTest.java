package com.github.tommyettinger.textra.squidglyph;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.KnownFonts;
import com.github.tommyettinger.textra.TypingLabel;

import java.util.ArrayDeque;

import static com.badlogic.gdx.Gdx.input;
import static com.badlogic.gdx.Input.Keys.*;

public class MinimalGridTest extends ApplicationAdapter {

    private Stage stage;
    private GlyphGrid gg;
    private char[][] bare, dungeon;
    private GlyphActor emojiGlyph;
    private GlyphActor emojiGlyph2;
    private GlyphActor atGlyph;
    private GlyphActor atGlyph2;
    private GlyphActor usedGlyph;
    private GlyphActor usedGlyph2;

    private Stage screenStage;
    private Font varWidthFont;
    private ArrayDeque<Container<TypingLabel>> messages = new ArrayDeque<>(30);
    private ArrayDeque<String> markupMessages = new ArrayDeque<>(30);
    private Table messageGroup;
    private Table root;

    private static final int GRID_WIDTH = 40;
    private static final int GRID_HEIGHT = 25;
    private static final int CELL_WIDTH = 32;
    private static final int CELL_HEIGHT = 32;

    private long timeUntilMessage = 1000, startTime = TimeUtils.millis();

//    private static final String VALID_CHARS = "abcdefghijklmnopqrstuvwxyz" + BlockUtils.ALL_BLOCK_CHARS;

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Traditional Roguelike Map Demo");
        config.setWindowedMode(GRID_WIDTH * CELL_WIDTH, GRID_HEIGHT * CELL_HEIGHT);
        config.disableAudio(true);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new MinimalGridTest(), config);
    }

    @Override
    public void create() {
        stage = new Stage();
        screenStage = new Stage();
//        Font aStarry = KnownFonts.addEmoji(KnownFonts.getGentiumUnItalic().scaleTo(46f, 25f));
        // OK, this is a total mess.
        // Here, we sort-of duplicate KnownFonts.getIosevkaSlab(), but change the size, offsetY, and descent.
        // Having descent = 0 is normally incorrect, but seems to work well with GlyphGrid for some reason.
//        Font aStarry = KnownFonts.addEmoji(new Font("Iosevka-Slab-standard.fnt",
//                "Iosevka-Slab-standard.png", STANDARD, 0f, 0f, 0f, 0f, true) // offsetY changed
//                .scaleTo(16, 28).fitCell(16, 28, false)
//                .setDescent(0f) // changed a lot
//                .setLineMetrics(-0.125f, -0.125f, 0f, -0.25f).setInlineImageMetrics(-8f, 24f, 0f)
//                .setTextureFilter().setName("Iosevka Slab"));

        Font aStarry = KnownFonts.addEmoji(KnownFonts.getIosevka(Font.DistanceFieldType.MSDF));
        Font grenze = KnownFonts.addEmoji(KnownFonts.getGrenze(Font.DistanceFieldType.SDF_OUTLINE));

//        varWidthFont = KnownFonts.getGentium();
        varWidthFont = KnownFonts.getGentiumUnItalic();
        varWidthFont.scaleHeightTo(30f);
//        aStarry.adjustCellWidth(0.5f);
//        aStarry.originalCellHeight *= 0.5f;
//        aStarry.cellHeight *= 0.5f;
//        aStarry.descent *= 0.5f;
//        aStarry.fitCell(32, 32, true);
        gg = new GlyphGrid(aStarry, GRID_WIDTH, GRID_HEIGHT, true);

        float larger = Math.max(grenze.cellWidth, grenze.cellHeight);
        grenze.scaleTo(grenze.cellWidth / larger, grenze.cellHeight / larger).fitCell(1f, 1f, true);


        //use Ă to test glyph height
        emojiGlyph = new GlyphActor("[_][~][+😁]", gg.font);
        atGlyph = new GlyphActor("[red orange][~][_]@", gg.font);
        usedGlyph = atGlyph;
        gg.addActor(usedGlyph);
        emojiGlyph2 = new GlyphActor("[_][~][+😁]", grenze);
        atGlyph2 = new GlyphActor("[red orange][~][_]@", grenze);
        usedGlyph2 = atGlyph2;
        gg.addActor(usedGlyph2);

        input.setInputProcessor(new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode){
                    case ESCAPE:
                    case Q:
                        Gdx.app.exit();
                        break;
                    case E:
                        if(usedGlyph != emojiGlyph) {
                            emojiGlyph.setPosition(atGlyph.getX(), atGlyph.getY());
                            gg.removeActor(usedGlyph);
                            usedGlyph = emojiGlyph;
                            gg.addActor(usedGlyph);

                            emojiGlyph2.setPosition(atGlyph2.getX(), atGlyph2.getY());
                            gg.removeActor(usedGlyph2);
                            usedGlyph2 = emojiGlyph2;
                            gg.addActor(usedGlyph2);
                        }
                        break;
                    case NUM_2:
                    case AT:
                        if(usedGlyph != atGlyph) {
                            atGlyph.setPosition(emojiGlyph.getX(), emojiGlyph.getY());
                            gg.removeActor(usedGlyph);
                            usedGlyph = atGlyph;
                            gg.addActor(usedGlyph);

                            atGlyph2.setPosition(emojiGlyph2.getX(), emojiGlyph2.getY());
                            gg.removeActor(usedGlyph2);
                            usedGlyph2 = atGlyph2;
                            gg.addActor(usedGlyph2);
                        }
                        break;
                    case R:
                        regenerate();
                        break;
                    default: return false;
                }
                return true;
            }
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                return false;
            }
        });

        messageGroup = new Table().background(new TextureRegionDrawable(varWidthFont.mapping.get(varWidthFont.solidBlock)).tint(new Color(0.1f, 0.1f, 0.1f, 0.5f)));
        messageGroup.left();

        root = new Table();
        root.setFillParent(true);
        Table nest = new Table();
        nest.add(messageGroup).size(screenStage.getWidth() * 0.8f, 8 * varWidthFont.cellHeight);
        root.add(nest).bottom().expand().padBottom(25f);
        root.pack();
        screenStage.addActor(root);

        regenerate();
        stage.addActor(gg);

        markupMessages.add("[%?blacken]Grumbles Sludgenugget got {VAR=FIRE}{CANNON}obliterated!{RESET}");
        markupMessages.add("[%?blacken]Crammage Cribbage-Babbage got {VAR=FIRE}{CANNON}obliterated!{RESET}");
        markupMessages.add("[%?blacken]Hawke 'The Sock' Locke got {VAR=SPUTTERINGFIRE}obliterated!{RESET}");
        markupMessages.add("[%?blacken]Hyperdeath Slaykiller got {VAR=ZOMBIE}zombified!{RESET}");
        markupMessages.add("[%?blacken][*]WELCOME[*] to your [/]DOOM[/]!");
    }

    public void move(int x, int y){
        // this prevents movements from restarting while a slide is already in progress.
        if(usedGlyph.hasActions()) return;

        x = Math.round(usedGlyph.getX() + x);
        y = Math.round(usedGlyph.getY() + y);
        if(x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT && bare[x][y] == '.') {
            usedGlyph.addAction(Actions.moveTo(x, y, 0.2f));
        }
        else {
                usedGlyph.addAction(Actions.rotateBy(360f, 1f));
        }
        int hash = NumberUtils.floatToRawIntBits(x);
        hash = (hash ^ hash >>> 19) * 0x9BA55;
        hash += NumberUtils.floatToRawIntBits(y);
        hash = (hash ^ hash >>> 19) * 0x9BA55;
        hash = (hash ^ hash >>> 19);
        x = Math.round(usedGlyph2.getX() + (hash & 2) - 1);
        y = Math.round(usedGlyph2.getY() + (hash >>> 2 & 2) - 1);
        if(x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT && bare[x][y] == '.') {
            usedGlyph2.addAction(Actions.moveTo(x, y, 0.2f));
        }
        else {
                usedGlyph2.addAction(Actions.rotateBy(360f, 1f));
        }
    }

    public void regenerate(){
        //[SEED] Initial seed is -2701817898995387683
        dungeon = new char[][]{
                "└│││││││┌ └│││││││├││┌   ".toCharArray(),
                "─.......┘│┐.......─..─   ".toCharArray(),
                "─...............││┐..─   ".toCharArray(),
                "─....................─   ".toCharArray(),
                "─.└│..│┌.............─   ".toCharArray(),
                "─.─....─......└┌....└┐   ".toCharArray(),
                "─.─....─....└│┐─....─    ".toCharArray(),
                "─.─....┘│├││┐  ─....─    ".toCharArray(),
                "┴│┐......─     ─..└│┐    ".toCharArray(),
                "─........─ └│││┐..─      ".toCharArray(),
                "─........─└┐......┘┌     ".toCharArray(),
                "┘├│││││││┤┐........─     ".toCharArray(),
                "└┐.................─     ".toCharArray(),
                "─..................┘││││┌".toCharArray(),
                "─...└│...└││││││┌.......─".toCharArray(),
                "─...─...└┐└││││┌─.......─".toCharArray(),
                "─......└┐ ─....─┘││││├││┬".toCharArray(),
                "─.....└┐  ─....┘│││┌ ─..─".toCharArray(),
                "┘┌...└┐   ─........─ ─..─".toCharArray(),
                " ─..│┤││┌ ─........─ ─..─".toCharArray(),
                " ┘┌.....─ ─........─└┐..─".toCharArray(),
                "└│┬.....┘┌─........┘┐...─".toCharArray(),
                "─.─......┘┐.............─".toCharArray(),
                "─.┘┌....................─".toCharArray(),
                "─..┘┌..........─....─...─".toCharArray(),
                "─...┘┌.........─....┴┌..─".toCharArray(),
                "─....┘│││...........─┘┌.─".toCharArray(),
                "─...................─ ┘│┐".toCharArray(),
                "─..................└┐    ".toCharArray(),
                "┘│││├│..└││┌..─..└│┐     ".toCharArray(),
                "└│││┐..│┤│┌─..┴││┤│┌  └│┌".toCharArray(),
                "─.........──..─....─ └┐.─".toCharArray(),
                "─.........──..─....─└┐..─".toCharArray(),
                "┴│││┌..─..──..─....──...─".toCharArray(),
                "─...─..─..──..─....──...─".toCharArray(),
                "─...─..─..──..─....┘┐...─".toCharArray(),
                "─...┘││┐..──............─".toCharArray(),
                "─.........──............─".toCharArray(),
                "─.........──.......└┌...─".toCharArray(),
                "┘│││││││││┐┘│││││││┐┘│││┐".toCharArray(),
        };
        bare = new char[][]{
                "#########################".toCharArray(),
                "#.......###.......#..####".toCharArray(),
                "#...............###..####".toCharArray(),
                "#....................####".toCharArray(),
                "#.##..##.............####".toCharArray(),
                "#.#....#......##....#####".toCharArray(),
                "#.#....#....####....#####".toCharArray(),
                "#.#....#########....#####".toCharArray(),
                "###......#######..#######".toCharArray(),
                "#........#######..#######".toCharArray(),
                "#........###......#######".toCharArray(),
                "###########........######".toCharArray(),
                "##.................######".toCharArray(),
                "#..................######".toCharArray(),
                "#...##...########.......#".toCharArray(),
                "#...#...#########.......#".toCharArray(),
                "#......####....##########".toCharArray(),
                "#.....#####....#######..#".toCharArray(),
                "##...######........###..#".toCharArray(),
                "##..#######........###..#".toCharArray(),
                "###.....###........###..#".toCharArray(),
                "###.....###........##...#".toCharArray(),
                "#.#......##.............#".toCharArray(),
                "#.##....................#".toCharArray(),
                "#..##..........#....#...#".toCharArray(),
                "#...##.........#....##..#".toCharArray(),
                "#....####...........###.#".toCharArray(),
                "#...................#####".toCharArray(),
                "#..................######".toCharArray(),
                "######..####..#..########".toCharArray(),
                "#####..#####..###########".toCharArray(),
                "#.........##..#....####.#".toCharArray(),
                "#.........##..#....###..#".toCharArray(),
                "#####..#..##..#....##...#".toCharArray(),
                "#...#..#..##..#....##...#".toCharArray(),
                "#...#..#..##..#....##...#".toCharArray(),
                "#...####..##............#".toCharArray(),
                "#.........##............#".toCharArray(),
                "#.........##.......##...#".toCharArray(),
                "#########################".toCharArray(),
        };

//        dungeonProcessor.setPlaceGrid(dungeon = LineTools.hashesToLines(dungeonProcessor.generate(), true));
//        System.out.println("new char[][]{");
//        for (int x = 0; x < dungeon.length; x++) {
//            System.out.print('"');
//            System.out.print(dungeon[x]);
//            System.out.println("\".toCharArray(),");
//        }
//        System.out.println("};");
//        bare = dungeonProcessor.getBarePlaceGrid();
//        System.out.println("new char[][]{");
//        for (int x = 0; x < bare.length; x++) {
//            System.out.print('"');
//            System.out.print(bare[x]);
//            System.out.println("\".toCharArray(),");
//        }
//        System.out.println("};");
        usedGlyph.setPosition(1, 1);
        usedGlyph2.setPosition(2, 1);
        gg.backgrounds = new int[GRID_WIDTH][GRID_HEIGHT];
        gg.map.clear();
    }

    public void recolor(){
//        int idx = 0;
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                char c = dungeon[x][y];
                switch (c){
                    case ' ':
                        gg.backgrounds[x][y] = 0;
                        break;
                    case '.':
                        gg.backgrounds[x][y] = 0x808080FF |
                                (int)((y + ((x + y) * (x + y + 1) >> 1)) * 0x9E3779B97F4A7C15L >>> 57)
                                        * 0x01010100;
                        gg.put(x, y, '.', 0x444444FF);
//                        gg.put(x, y, VALID_CHARS.charAt(idx++ * idx % VALID_CHARS.length()), 0x444444FF);
                        break;
                    default:
                        gg.backgrounds[x][y] = 0x808080FF |
                                (int)((y + ((x + y) * (x + y + 1) >> 1)) * 0x9E3779B97F4A7C15L >>> 57)
                                        * 0x01010100;
                        gg.put(x, y, c, 0x444444FF);
                }
            }
        }
    }

    /**
     * Supports WASD, vi-keys (hjklyubn), arrow keys, and numpad for movement, plus '.' or numpad 5 to stay still.
     */
    public void handleHeldKeys() {
        if(input.isKeyPressed(A) || input.isKeyPressed(H) || input.isKeyPressed(LEFT) || input.isKeyPressed(NUMPAD_4))
            move(-1, 0);
        else if(input.isKeyPressed(S) || input.isKeyPressed(J) || input.isKeyPressed(DOWN) || input.isKeyPressed(NUMPAD_2))
            move(0, -1);
        else if(input.isKeyPressed(W) || input.isKeyPressed(K) || input.isKeyPressed(UP) || input.isKeyPressed(NUMPAD_8))
            move(0, 1);
        else if(input.isKeyPressed(D) || input.isKeyPressed(L) || input.isKeyPressed(RIGHT) || input.isKeyPressed(NUMPAD_6))
            move(1, 0);
        else if(input.isKeyPressed(Y) || input.isKeyPressed(NUMPAD_7))
            move(-1, 1);
        else if(input.isKeyPressed(U) || input.isKeyPressed(NUMPAD_9))
            move(1, 1);
        else if(input.isKeyPressed(B) || input.isKeyPressed(NUMPAD_1))
            move(-1, -1);
        else if(input.isKeyPressed(N) || input.isKeyPressed(NUMPAD_3))
            move(1, -1);
        else if(input.isKeyPressed(PERIOD) || input.isKeyPressed(NUMPAD_5) || input.isKeyPressed(NUMPAD_DOT))
            move(0, 0);
    }
    public void message(String markupString) {
        TypingLabel label = null;
        Container<TypingLabel> con = null;
        float tall = 0;
        for(Container<TypingLabel> c : messages){
            tall += c.getHeight();
        }
        while(tall > 8 * varWidthFont.cellHeight){
            con = messages.removeFirst();
            label = con.getActor();
            messageGroup.removeActor(con);
            tall = 0;
            for(Container<TypingLabel> c : messages){
                tall += c.getHeight();
            }
        }
        messageGroup.pack();

        if(label == null)
        {
            label = new TypingLabel("", varWidthFont);
            label.setWrap(true);
            label.setMaxLines(1);
            label.setEllipsis("...");
            label.restart(markupString);
        }
        else {
            label.setSize(0f, 0f);
            label.restart(markupString);
        }
        if(con == null)
        {
            con = new Container<>(label);
        }
        con.prefWidth(screenStage.getWidth() * 0.8f);
        label.debug();
        label.setAlignment(Align.bottomLeft);
        messages.addLast(con);
        messageGroup.add(con).row();
        root.pack();
    }

    public void processQueue() {
        if(markupMessages.isEmpty() || TimeUtils.timeSinceMillis(startTime) < timeUntilMessage){
            return;
        }
        message(markupMessages.pollFirst());
        startTime = TimeUtils.millis();
    }

    @Override
    public void render() {
        gg.font.boxDrawingBreadth = 1.5f + MathUtils.sinDeg(TimeUtils.millis() >>> 1 & 0xFFFFFL) * 0.75f;
        processQueue();
        recolor();
        handleHeldKeys();
        ScreenUtils.clear(Color.BLACK);
        Camera camera = gg.viewport.getCamera();
        camera.position.set(gg.gridWidth * 0.5f, gg.gridHeight * 0.5f, 0f);
        camera.update();
        stage.act();
        stage.draw();
        screenStage.act();
        screenStage.draw();

        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        gg.resize(width, height);
        atGlyph2.font.resizeDistanceField(width, height);
        screenStage.getViewport().update(width, height, true);
    }
}
