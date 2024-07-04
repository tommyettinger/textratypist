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
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.textra.utils.BlockUtils;
import com.github.tommyettinger.textra.utils.ColorUtils;

import static com.github.tommyettinger.textra.utils.ColorUtils.lerpColors;

public class GridTest extends ApplicationAdapter {
    Font font;
    Font[] fonts;
    Viewport viewport;
    Stage stage;
    SpriteBatch batch;
    int[][] backgrounds;
    char[][] lines;
    Layout layout;
    TypingLabel marquee, link;
    long startTime;
    RandomXS128 random;
    long glyph;

    static final int PIXEL_WIDTH = 1000, PIXEL_HEIGHT = 650;

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Font test");
        config.setWindowedMode(PIXEL_WIDTH, PIXEL_HEIGHT);
        config.disableAudio(true);
        config.useVsync(true);
        new Lwjgl3Application(new GridTest(), config);
    }

    @Override
    public void create() {
        random = new RandomXS128(123);
        lines = new char[40][10];
        int c = -1;
        for (int x = 0; x < 40; x++) {
            for (int y = 0; y < 10; y++) {
                do {
                    c = (c + 1) % BlockUtils.BOX_DRAWING.length;
                } while (BlockUtils.BOX_DRAWING[c].length == 0);
                lines[x][y] = (char) (0x2500 + c);
            }
        }

        batch = new SpriteBatch();
        viewport = new StretchViewport(PIXEL_WIDTH, PIXEL_HEIGHT);
        stage = new Stage(viewport, batch);

//        fonts = KnownFonts.getAllStandard();
//        for(Font f : fonts)
//            KnownFonts.addEmoji(f.scaleTo(20f, 25).fitCell(25, 25, true));
        font = KnownFonts.addEmoji(KnownFonts.getAStarryTall().scaleTo(16f, 24).fitCell(25, 25, true));
//        font = KnownFonts.addEmoji(KnownFonts.getIBM8x16().fitCell(16, 32, true).scaleTo(16, 32));

//        font = KnownFonts.getInconsolata();//.scaleTo(16, 32);
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
//        font = KnownFonts.getOxanium();
//        font = KnownFonts.getYanoneKaffeesatz().scaleTo(45, 60);
//        font = KnownFonts.getCanada().scaleTo(40, 58);
//        font = KnownFonts.getRobotoCondensed();
//        font = KnownFonts.getIBM8x16();
//        font = KnownFonts.getOpenSans();

//        font.fitCell(font.cellWidth, font.cellHeight, true);
//        font.fitCell(font.cellHeight, font.cellHeight, true);
//        font.scaleTo(font.cellWidth * 25f / font.cellHeight, 25f).fitCell(25f, 25f, true);

        layout = new Layout(font).setTargetWidth(Gdx.graphics.getWidth());
        backgrounds = new int[(int) Math.ceil(PIXEL_WIDTH / font.cellWidth)][(int) Math.ceil(PIXEL_HEIGHT / font.cellHeight)];
//        int sw = 0x669E83FF, se = 0x2A8528FF, nw = 0xF0DDA0FF, ne = 0x7A4A31FF;
        int sw = ColorUtils.describe("dark dull brown:2 pink");
        int se = ColorUtils.describe("darker rich brown:2 red");
        int nw = ColorUtils.describe("lighter dull brown:2 apricot");
        int ne = ColorUtils.describe("light rich brown:2 orange");
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
        for (int x = 0; x < backgrounds.length; x++) {
            for (int y = 0; y < backgrounds[0].length; y++) {
                backgrounds[x][y] ^= (x + y & 1) << 7;
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
        font.markup(
//                "Test"
                "Test [*]TEST [/]Test [*]TEST[ ][.]test [=]Test [^]TEST [ ][_]Test [~]test[_] Test[ ]"
                        + "\nThe [#800000]MAW[ ] of the [/][#66DDFF]wendigo[/] (wendigo)[ ] [*]appears[*]!"
                        + "\nThe [_][#666666]BLADE[ ] of [*][/][#FFFF44]DYNAST-KINGS[ ] strikes!"
                        + "\n[_][;]Each cap, [,]All lower, [!]Caps lock[ ], [?]Unknown[ ]?"
                        + "\n[#BBAA44]φ[ ] = (1 + 5[^]0.5[^]) * 0.5"
                        + "\n[#FF8822]¿Qué son estos? ¡Arribate, mijo![ ]"
                        + "\nPchnąć[ ] w tę łódź [#775522]jeża[ ] lub ośm skrzyń [#CC00CC]fig[ ]."
                , layout);

        marquee = new TypingLabel("{ROTATE=100}[/]{HIGHLIGHT=lightest magenta;1;1;1;0.5;true}EAT AT JOE'S{ENDHIGHLIGHT}", font);
//        marquee = new TypingLabel("{BLINK}{ROTATE=100}{JOLT=1;1;inf;0.07;lightest magenta;dark dull magenta gray}[/]EAT AT JOE'S", font);
        marquee.wrap = false;
//        marquee.parseTokens();
        marquee.setWidth(Gdx.graphics.getBackBufferWidth());
        marquee.setPosition(64, 350);
        marquee.skipToTheEnd();
        marquee.setRotation(-100f);
        link = new TypingLabel("Welcome to [sky]{STYLIST=1;0;0;0;0;1}{LINK=https://github.com/tommyettinger/textratypist}[_]TextraTypist[ ], for text stuff.", font);
//        link = new TypingLabel("Welcome to [sky][_]{LINK=https://github.com/tommyettinger/textratypist}TextraTypist[ ], for text stuff.", font);
//        link.parseTokens();
        link.setWidth(Gdx.graphics.getBackBufferWidth());
        link.setPosition(0f, font.cellHeight * 10.5f);
        link.skipToTheEnd();
//        font.markup("\"You are ever more the [/]fool[/] than the pitiable cutpurse who [*]dares waylay[*] my castle road!\" the [dark rich gold]King[] admonished."
//                +" \"Forsooth! Had [_]I[_] my right mind, I would have [dark red]both of [_]your heads[] by morning. But alas, I am stricken with" +
//                " unreasonable mercy for your [~]wretched[~] souls. To [darker grey][*]the Trappists[] ye shall go; I am in need of" +
//                " a [darkest bronze]stout brew[].\"", layout);

//        font.markup("\"[/][~]HOSTILE ACTION DETECTED[].\" The computerized voice was barely audible over the klaxons blaring throughout [darker rich purple][_]Starship Andromalius[]."
//                +" \"[!]Would somebody shut that thing off[!]? We're quite aware by now!\" [orange]Captain Luiz Tigre[] shouted at no one in particular, while frantically flipping the remaining" +
//                " switches on the capacitor controls. \"Sir, we need to get the [silver]teleprojector[] online. Send a party aboard, say they're negotiators.\" [light sky]First Admiral Zototh[] said with urgency." +
//                " \"[*]Negotiators[*]? Are you serious?\" \"I said to [/]say[/] they're negotiators... just with really big guns.\"", layout);

        stage.addActor(marquee);
        stage.addActor(link);
        startTime = TimeUtils.millis();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.2f, 0.3f, 0.4f, 1);
        
        float x = 0, y = font.cellHeight * 13.5f;
//        long since = TimeUtils.timeSinceMillis(startTime);
//        font = fonts[(int) (since >>> 10 & 0x7FFFFFFF) % fonts.length];
//        font = fonts[5];

//        marquee.act(Gdx.graphics.getDeltaTime());
//        link.act(Gdx.graphics.getDeltaTime());
        stage.act();
        Camera camera = this.viewport.getCamera();
        camera.update();
        Group root = stage.getRoot();
        if (root.isVisible()) {
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            font.enableShader(batch);
            font.drawBlocks(batch, backgrounds, 0f, 0f);
            root.draw(batch, 1.0F);
        for (int xx = 0; xx < lines.length; xx++) {
            for (int yy = 0; yy < lines[0].length; yy++) {
                font.drawGlyph(batch, 0xFFFFFFFE00000000L | lines[xx][yy], font.cellWidth * xx, y + font.cellHeight * (1 + yy));
            }
        }
//        long color = (long) DescriptiveColor.lerpColors(
//                DescriptiveColor.lerpColors(
//                        (int)((System.currentTimeMillis() >>> 10) * 0x9E3779B0 | 0xFE),
//                        (int)(((System.currentTimeMillis() >>> 10) + 1L) * 0x9E3779B0 | 0xFE),
//                        (System.currentTimeMillis() & 0x3FFL) * 0x1p-10f
//                ), 0x000000FF, 0.375f) << 32;
//        for (int i = 0, n = glyphs[0].size(); i < n; i++) {
//            glyphs[0].set(i, glyphs[0].get(i) & 0xFFFFFFFFL | color);
//        }
//        font.drawGlyphs(batch, layout,
////0f, y, Align.left
////                PIXEL_WIDTH * 0.5f
//                0, y - font.cellHeight * 2, Align.left
//        );

        // This seemed to be causing some side effects when it rendered Cozette; should be fixed now.
        font.drawGlyph(batch, font.markupGlyph("[RED][+🔜]"), 0, Gdx.graphics.getHeight() - font.cellHeight * 0.5f);
        // This seems fine.
//        font.drawGlyph(batch, font.markupGlyph("[RED]T"), 0, Gdx.graphics.getHeight() - font.cellHeight);
        // This also seems fine.
//        font.drawGlyph(batch, Font.markupGlyph((char) font.atlasLookup("🔜"), "[RED]", ColorLookup.INSTANCE), 0, Gdx.graphics.getHeight() - font.cellHeight);

        marquee.draw(batch, 1f);
        link.draw(batch, 1f);
            batch.end();
        }
        Gdx.graphics.setTitle(font.name + " at " + Gdx.graphics.getFramesPerSecond() + " FPS");
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        font.resizeDistanceField(width, height, viewport);
    }
}
