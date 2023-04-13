/*
 * Copyright (c) 2022 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import static com.github.tommyettinger.textra.utils.ColorUtils.lerpColors;

public class RotationTest extends ApplicationAdapter {
    Font font;
    SpriteBatch batch;
    int[][] backgrounds;
    Layout layout;
    long startTime;

    GLProfiler profiler;

    static final int PIXEL_WIDTH = 800, PIXEL_HEIGHT = 640;

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Font test");
        config.setWindowedMode(PIXEL_WIDTH, PIXEL_HEIGHT);
        config.disableAudio(true);
        config.useVsync(true);
        config.setForegroundFPS(0);
        new Lwjgl3Application(new RotationTest(), config);
    }

    @Override
    public void create() {
        profiler = new GLProfiler(Gdx.graphics);
        profiler.enable();

        batch = new SpriteBatch();
//        font = new Font("RaeleusScriptius-standard.fnt", 0, 14, 0, 0).scale(0.75f, 0.75f);
        font = KnownFonts.getGentiumUnItalic().scaleTo(42, 32);
//        font = KnownFonts.getAStarry().scaleTo(16, 32);
//        font = KnownFonts.getInconsolata().scaleTo(16, 32);
//        font = KnownFonts.getCascadiaMono().scaleTo(12, 24);
//        font = KnownFonts.getIosevka();
//        font = KnownFonts.getIosevkaSlab().scale(0.75f, 0.75f);
//        font = KnownFonts.getIosevkaSlabMSDF().scaleTo(20, 20);
//        font = KnownFonts.getDejaVuSansMono().scale(0.75f, 0.75f);
//        font = KnownFonts.getCozette().scale(2, 2);
//        font = KnownFonts.getOpenSans().scale(0.75f, 0.75f);
//        font = KnownFonts.getAStarry();
//        font = KnownFonts.getGentium().scaleTo(48, 48);
//        font = KnownFonts.getLibertinusSerif();
//        font = KnownFonts.getKingthingsFoundation().scaleTo(45, 60);
//        font = KnownFonts.getOxanium().scaleTo(40, 50);
//        font = KnownFonts.getYanoneKaffeesatz().scaleTo(45, 60);
//        font = KnownFonts.getCanada().scaleTo(40, 58);
//        font = KnownFonts.getRobotoCondensed().scaleTo(37, 53);

//        font.fitCell(font.cellWidth, font.cellHeight, true);
//        font.fitCell(24, 24, true);

//        font.kerning = null; // for debugging

        layout = new Layout(font).setTargetWidth(Gdx.graphics.getWidth());
        backgrounds = new int[(int) Math.ceil(PIXEL_WIDTH / font.cellWidth)][(int) Math.ceil(PIXEL_HEIGHT / font.cellHeight)];
        int sw = 0x669E83FF, se = 0x2A8528FF, nw = 0xF0DDA0FF, ne = 0x7A4A31FF;
        backgrounds[0][0] = sw;
        backgrounds[0][backgrounds[0].length - 1] = nw;
        backgrounds[backgrounds.length - 1][0] = se;
        backgrounds[backgrounds.length - 1][backgrounds[0].length - 1] = ne;
        for (int x = 1; x < backgrounds.length - 1; x++) {
            backgrounds[x][0] = lerpColors(sw, se, x / (float) backgrounds.length);
            backgrounds[x][backgrounds[0].length - 1] = lerpColors(nw, ne, x / (float) backgrounds.length);
        }
        for (int x = 0; x < backgrounds.length; x++) {
            int s = backgrounds[x][0], e = backgrounds[x][backgrounds[0].length - 1];
            for (int y = 1; y < backgrounds[0].length - 1; y++) {
                backgrounds[x][y] = lerpColors(s, e, y / (float) backgrounds[0].length);
            }
        }

//        font.markup("[#00FF00FF]Hello, [~]World[~]Universe[.]$[=]$[^]$[^]!", glyphs[0] = new LongList());
//        font.markup("The [dark richer red]MAW[] of the [/][|lighter blue mint]wendigo[] [*]appears[*]!", glyphs[1] = new LongList());
//        font.markup("The [_][dark dull blue purple]BLADE[] of [*][/][|dark richest yellow]KINGS[] strikes!", glyphs[2] = new LongList());
//        font.markup("[;]Each cap[], [,]All lower[], [!]Caps lock[], [?]Unknown[]?", glyphs[3] = new LongList());

//        font.markup("[#007711FF]Hello, [~]World[~]Universe[.]$[=]$[^]$[^]!", layout);
//
//        font.markup("\n[*]Водяной[] — в славянской мифологии дух, обитающий в воде, хозяин вод[^][BLUE][[2][]."
//                + "\nВоплощение стихии воды как отрицательного и опасного начала[^][BLUE][[3][[citation needed][].", layout);
//
        font.markup("Test [*]TEST [/]Test [*]TEST[][.]test [=]Test [^]TEST [][_]Test [~]te[RED]s[WHITE]t[_] Test[]"
                        + "\n┬┴┬┴┬┴┬┴┬┴┬┴┬┴┬┴┬┴┬┴┬┴┬┴┬┴┬┴┬┴┬┴┬┴┬┴┬[RED]┴[]┬┴"
                        + "\nThe [#800000]MAW[] of the [/][#66DDFF]wendigo[/][] ([~]wendigo[]) [*]appears[*]!"
                        + "\nThe [_][#666666]BLADE[] of [*][/][#FFFF44]DYNAST-KINGS[] strikes!"
                        + "\n[_][;]Each cap, [,]All lower, [!]Caps lock[], [?]Unknown[]?"
                        + "\n[#BBAA44]φ[] = (1 + 5[^]0.5[^]) * 0.5 ┼┌─┤"
                        + "\n[#FF8822]¿Qué son estos? ¡Arribate, mijo![]"
                        + "\nPchnąć[] w tę łódź [#775522]jeża[] lub ośm skrzyń [#CC00CC]fig[]."
                , layout);

//        font.markup("\"You are ever more the [/]fool[/] than the pitiable cutpurse who [*]dares waylay[*] my castle road!\" the [dark rich gold]King[] admonished."
//                +" \"Forsooth! Had [_]I[_] my right mind, I would have [dark red]both of [_]your heads[] by morning. But alas, I am stricken with" +
//                " unreasonable mercy for your [~]wretched[~] souls. To [darker grey][*]the Trappists[] ye shall go; I am in need of" +
//                " a [darkest bronze]stout brew[].\"", layout);

//        font.markup("\"[/][~]HOSTILE ACTION DETECTED[].\" The computerized voice was barely audible over the klaxons blaring throughout [darker rich purple][_]Starship Andromalius[]."
//                +" \"[!]Would somebody shut that thing off[!]? We're quite aware by now!\" [orange]Captain Luiz Tigre[] shouted at no one in particular, while frantically flipping the remaining" +
//                " switches on the capacitor controls. \"Sir, we need to get the [silver]teleprojector[] online. Send a party aboard, say they're negotiators.\" [light sky]First Admiral Zototh[] said with urgency." +
//                " \"[*]Negotiators[*]? Are you serious?\" \"I said to [/]say[/] they're negotiators... just with really big guns.\"", layout);

        startTime = TimeUtils.millis();
    }

    @Override
    public void render() {
        profiler.reset();
        ScreenUtils.clear(0.4f, 0.5f, 0.9f, 1);
        
        float x = 0, y = layout.getHeight() + font.cellHeight * 2;
        batch.begin();
        font.enableShader(batch);
        batch.setPackedColor(Color.WHITE_FLOAT_BITS);

        font.drawBlocks(batch, backgrounds, 0f, 0f);
        long since = TimeUtils.timeSinceMillis(startTime);
        for (float g = 0; g < PIXEL_HEIGHT; g+= font.cellHeight) {

            font.drawGlyph(batch, 0xBB0011FE00000000L | '&',//0xBB0011FE00200000L
//                    2f * font.cellWidth,
                    (MathUtils.sinDeg(10f * g) * 0.4f + 0.5f) * font.cellWidth * backgrounds.length, g,
//                    (MathUtils.sinDeg(since * 0.01f + g) * 0.4f + 0.5f) * font.cellWidth * backgrounds.length, g,
                    since * 0.0625f);
        }
//        switch ((int)((since >>> 12) % 3)) {
//            case 0:
//                font.drawGlyphs(batch, layout,
//                    PIXEL_WIDTH * 0.5f, y, Align.left
//                    , since * 0.05f, 0f, 0f);
//            break;
//            case 1:
                font.drawGlyphs(batch, layout,
                        PIXEL_WIDTH * 0.5f, y, Align.center
                        , since * 0.015f, 0f, 0f
                );
//            break;
//            default:
//                font.drawGlyphs(batch, layout,
//                        PIXEL_WIDTH * 0.5f, y, Align.right
//                        , since * 0.05f, 0f, 0f
//                );
//        }
        font.drawText(batch, Gdx.graphics.getFramesPerSecond() + " FPS",
                font.cellWidth, Gdx.graphics.getHeight() - font.cellHeight * 2, 0x227711FE);
        batch.end();
        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
            System.out.printf("Calls: %d, draw calls: %d, shader switches: %d, texture bindings: %d, FPS: %d\n",
                    profiler.getCalls(), profiler.getDrawCalls(),
                    profiler.getShaderSwitches(), profiler.getTextureBindings(),
                    Gdx.graphics.getFramesPerSecond());

    }

    @Override
    public void resize(int width, int height) {
        font.resizeDistanceField(width, height);
    }
}
