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

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class EmojiAlignmentTest extends ApplicationAdapter {
    public Stage stage;
    public Font font, font2;

    @Override
    public void create() {
        stage = new Stage();
//        font = KnownFonts.getOxanium().scaleTo(16f, 18f);
        font = new Font("Oxanium-standard.fnt").scaleTo(16f, 18f);
        // without the adjustments to y position or width in KnownFonts, this doesn't display emoji as well.
        font2 = new Font(new BitmapFont(Gdx.files.internal("Oxanium-standard.fnt"))).scaleTo(16f, 18f);
        KnownFonts.addEmoji(font);
        KnownFonts.addEmoji(font2);
        TypingLabel typingLabel = new TypingLabel("Why are all the moderators animals?[+🦓][+🦉][+🐼]\n\n\nThat's not actually true, there's a [+💾]!", font);
        typingLabel.layout.setTargetWidth(400);
        typingLabel.setAlignment(Align.center);
//        typingLabel.setWrap(true);
//        TypingLabel typingLabel2 = new TypingLabel("[#cc99aa]Why are all the moderators animals?[+🦓][+🦉][+🐼]\n\n\nThat's not actually true, there's a [+💾]!", font2);
//        typingLabel2.layout.setTargetWidth(400);
//        typingLabel2.setAlignment(Align.center);
//        typingLabel2.setWrap(true);
        Stack stack = new Stack(typingLabel);
        Table root = new Table();
        root.setFillParent(true);
        root.add(stack);
        stage.addActor(root);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);
        stage.act();
        stage.draw();
    }

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("TextraLabel Emoji Alignment test");
        config.setWindowedMode(640, 480);
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