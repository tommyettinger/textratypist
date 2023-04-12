package com.github.tommyettinger.textra.squidglyph;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.ScreenUtils;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.KnownFonts;

import static com.badlogic.gdx.Gdx.input;
import static com.badlogic.gdx.Input.Keys.*;

public class MinimalGridTest extends ApplicationAdapter {

    private Stage stage;
    private GlyphGrid gg;
    private char[][] bare, dungeon;
    private GlyphActor playerGlyph;

    private static final int GRID_WIDTH = 40;
    private static final int GRID_HEIGHT = 25;
    private static final int CELL_WIDTH = 32;
    private static final int CELL_HEIGHT = 32;

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Traditional Roguelike Map Demo");
        config.setWindowedMode(GRID_WIDTH * CELL_WIDTH, GRID_HEIGHT * CELL_HEIGHT);
        config.disableAudio(true);
        config.setForegroundFPS(360); // shouldn't need to be any faster
        config.useVsync(true);
        new Lwjgl3Application(new MinimalGridTest(), config);
    }

    @Override
    public void create() {
        stage = new Stage();
        Font font = KnownFonts.getInconsolata().scaleTo(15f, 25f);
        gg = new GlyphGrid(font, GRID_WIDTH, GRID_HEIGHT, true);
        //use Ă to test glyph height
        playerGlyph = new GlyphActor('@', "[red orange]", gg.font);
        gg.addActor(playerGlyph);

        input.setInputProcessor(new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode){
                    case ESCAPE:
                    case Q:
                        Gdx.app.exit();
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

        regenerate();
        stage.addActor(gg);
    }

    public void move(int x, int y){

        // this prevents movements from restarting while a slide is already in progress.
        if(playerGlyph.hasActions()) return;

        x = Math.round(playerGlyph.getX() + x);
        y = Math.round(playerGlyph.getY() + y);
        if(x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT && bare[x][y] == '.') {
            playerGlyph.addAction(Actions.moveTo(x, y, 0.2f));
        }
        else{
                playerGlyph.addAction(Actions.rotateBy(360f, 1f));
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
        playerGlyph.setPosition(1, 1);
        gg.backgrounds = new int[GRID_WIDTH][GRID_HEIGHT];
        gg.map.clear();
    }

    public void recolor(){
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                char c = dungeon[x][y];
                switch (c){
                    case ' ':
                        gg.backgrounds[x][y] = 0;
                        break;
                    default:
                        gg.backgrounds[x][y] = 0xBBBBBBFF;
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

    @Override
    public void render() {
        recolor();
        handleHeldKeys();
        ScreenUtils.clear(Color.BLACK);
        Camera camera = gg.viewport.getCamera();
        camera.position.set(gg.gridWidth * 0.5f, gg.gridHeight * 0.5f, 0f);
        camera.update();
        stage.act();
        stage.draw();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        gg.resize(width, height);
    }
}
