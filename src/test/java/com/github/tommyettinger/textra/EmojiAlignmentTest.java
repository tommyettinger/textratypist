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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

public class EmojiAlignmentTest extends ApplicationAdapter {
    public Stage stage;
    public Font font, font2;
    TypingLabel typingLabel, typingLabel2;
    public float cw = 1, ch = 1, cw2 = 1, ch2 = 1;
    long startTime;
    @Override
    public void create() {
        startTime = TimeUtils.millis();
        stage = new Stage();
//        font = KnownFonts.getAStarry().scaleTo(8f, 16f);
//        font = KnownFonts.getOxanium().scaleTo(16f, 18f);
//        font = new Font("QuanPixel-standard.fnt", 0, 2, 0, 2).useIntegerPositions(true);
//        font2 = new Font(new BitmapFont(Gdx.files.internal("QuanPixel-standard.fnt")), 0, -2, 0, 2).useIntegerPositions(true);
        // the offsets applied to emoji change in position if the font is scaled after the emoji are added...
//        font.scale(2, 2);
//        font2.scale(2, 2);
//        KnownFonts.addEmoji(font, -4, -7, 0);
//        KnownFonts.addEmoji(font2, -4, -12, 0);

//        font = KnownFonts.getIosevka();
//        font2 = new Font(font);

        font = KnownFonts.getAStarry().scaleTo(8f, 16f);
        font2 = new Font(BitmapFontSupport.loadStructuredJson(Gdx.files.internal("A-Starry-standard.dat"), "A-Starry-standard.png"), Font.DistanceFieldType.STANDARD, 0, -32, 0, 0, true)
//                .setDescent(-12f).setLineMetrics(0f, -0.25f, 0f, 0f)
//                .setDescent(font.descent)
//                .setInlineImageMetrics(font.inlineImageOffsetX, font.inlineImageOffsetY, font.inlineImageXAdvance)
                .scaleTo(8f, 16f);
        font.useIntegerPositions(false);
        font2.useIntegerPositions(false);
        System.out.println("new Font(fntFile): " + font.debugString());
        System.out.println("new Font(bmpFont): " + font2.debugString());
        // for NowAlt
//        KnownFonts.addEmoji(font, -4, 0, 0);
//        KnownFonts.addEmoji(font2, -4, 0, 0);
        // for AStarry
        KnownFonts.addEmoji(font);
//        KnownFonts.addEmoji(font, -4, 22, 0); // (font, 12, 32, 0) will work, except for any chars after an emoji...
        KnownFonts.addEmoji(font2);
        cw = font.cellWidth;
        ch = font.cellHeight;
        cw2 = font2.cellWidth;
        ch2 = font2.cellHeight;
//        typingLabel = new TypingLabel("[#4455AA88]Why are all the moderators animals?\n\n\nThat's not actually true, there's a floppy!", font);
        typingLabel = new TypingLabel("[#4455AA88]Why are [_]all[_] the moderators [%?ERROR]animals?[+🦓][+🦉][+🐼][%][+🦓][+🦉][+🐼]\n\n\nThat's not actually true, there's a [+💾]!", font);
//        typingLabel.layout.setTargetWidth(400);
        typingLabel.setAlignment(Align.center);
        typingLabel.debug();

//        typingLabel2 = new TypingLabel("[#ff99aa87]Why are all the moderators animals?\n\n\nThat's not actually true, there's a floppy!", font2);
        typingLabel2 = new TypingLabel("[#ff99aa77]Why are [_]all[_] the moderators [%?WARN]animals?[+🦓][+🦉][+🐼][%][+🦓][+🦉][+🐼]\n\n\nThat's not actually true, there's a [+💾]!", font2);
//        typingLabel2.layout.setTargetWidth(400);
        typingLabel2.setAlignment(Align.center);
        typingLabel2.debug();
        Stack stack = new Stack(typingLabel, typingLabel2);
        stack.debug();
        Table root = new Table();
        root.setFillParent(true);
//        root.add(typingLabel);
        root.add(stack);//.align(Align.center);
        stage.addActor(root);
        stage.getBatch().setShader(new ShaderProgram(
                "attribute vec4 a_position;\n" +
                        "attribute vec4 a_color;\n" +
                        "attribute vec2 a_texCoord0;\n" +
                        "uniform mat4 u_projTrans;\n" +
                        "varying vec4 v_color;\n" +
                        "varying vec2 v_texCoords;\n" +
                        "\n" +
                        "void main()\n" +
                        "{\n" +
                        "   v_color = a_color;\n" +
                        "   v_color.a = v_color.a * (255.0/254.0);\n" +
                        "   v_texCoords = a_texCoord0;\n" +
                        "   gl_Position =  u_projTrans * a_position;\n" +
                        "}\n",
                "#ifdef GL_ES\n" +
                        "#define LOWP lowp\n" +
                        "precision mediump float;\n" +
                        "#else\n" +
                        "#define LOWP \n" +
                        "#endif\n" +
                        "varying vec2 v_texCoords;\n" +
                        "varying LOWP vec4 v_color;\n" +
                        "uniform sampler2D u_texture;\n" +
                        "void main()\n" +
                        "{\n" +
                        "   vec4 tgt = texture2D(u_texture, v_texCoords);\n" +
                        "   gl_FragColor.rgb = clamp(tgt.rgb * v_color.rgb * 0.5 + 1.0 - tgt.a, 0.0, 1.0);\n" +
                        "   gl_FragColor.a = clamp(v_color.a * tgt.a, 0.5, 1.0);\n" +
                        "}"
        ));
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);
        float factor = (TimeUtils.timeSinceMillis(startTime) & 0x1FFF) * 0x3p-13f + 1f;
        font.scaleTo(cw * factor, ch * factor);
        font2.scaleTo(cw2 * factor, ch2 * factor);
        typingLabel.invalidate();
        typingLabel2.invalidate();
        stage.act();
        stage.draw();
    }

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("TextraLabel Emoji Alignment test");
        config.setWindowedMode(1300, 480);
        config.disableAudio(true);
        ShaderProgram.prependVertexCode = "#version 110\n";
        ShaderProgram.prependFragmentCode = "#version 110\n";
//		config.enableGLDebugOutput(true, System.out);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
//        config.setForegroundFPS(60);
        new Lwjgl3Application(new EmojiAlignmentTest(), config);
    }

}