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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import static com.badlogic.gdx.utils.Align.left;

public class FieldTest extends ApplicationAdapter {
    ScreenViewport viewport;
    Stage stage;
    TextraField ttField;
    TextField s2dField;
    TextraArea ttArea;
    TextArea s2dArea;
    FWSkin skin;

    @Override
    public void create() {
        viewport = new ScreenViewport();
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        skin = new FWSkin(Gdx.files.internal("uiskin.json"));
        stage = new Stage(viewport);
        stage.setDebugAll(true);

        Font font = KnownFonts.addEmoji(KnownFonts.getDINishExpanded(Font.DistanceFieldType.SDF)).scaleHeightTo(32);
        String text = "Emoji: '🎉'";
        String longText =
                "ABCDEFGHIJKLMNOPQRSTUVWXYabcdefghijklmnopqrstuvwxyABCDEFGHIJKLMNOPQRSTUVWXYabcdefghijklmnopqrstuvwxy";
//                "Satchmo is a cat, who is extremely fat; when he sits " +
//                        "down, throughout the town, we all think, 'What was that? Did it happen " +
//                        "again (that thunderous din)? What could ever make, such a powerful quake, but " +
//                        "a cat with a double chin?'";
//                "[*]Локус контроля[*] - свойство " +
//                "личности приписывать " +
//                "свои неудачи и успехи " +
//                "либо внешним факторам " +
//                "(погода, везение, другие " +
//                "люди, судьба-злодейка), " +
//                "либо внутренним (я сам, " +
//                "моё отношение, мои" +
//                "действия)";
        Font.GlyphRegion solid = font.mapping.get(font.solidBlock);
        Drawable pipe = new TextureRegionDrawable(solid), //font.mapping.get('|', solid)
                selection = new TextureRegionDrawable(solid).tint(Color.GRAY),
                background = new TextureRegionDrawable(solid).tint(Color.CLEAR);
        pipe.setMinWidth(2);
        pipe.setMinHeight(font.cellHeight);
        selection.setMinWidth(font.cellWidth);
        selection.setMinHeight(font.cellHeight);
        background.setMinHeight(1);
        background.setMinWidth(1);
        ttField = new TextraField(text,
//skin, font
                new Styles.TextFieldStyle(font, Color.WHITE.cpy(), pipe,
                        selection, background)
        );
        ttField.setWidth(500);
        ttField.setPasswordMode(false);
        ttField.setHeight(font.cellHeight * 3);
        ttField.setAlignment(left);

        ttArea = new TextraArea(longText, ttField.style, skin.get(ScrollPane.ScrollPaneStyle.class));

        s2dField = new TextField(text,
                skin
        );
        s2dField.setWidth(500);
        s2dField.setPasswordMode(false);
        s2dField.setHeight(font.cellHeight * 3);
        s2dField.setAlignment(left);

        s2dArea = new TextArea(longText, s2dField.getStyle());

        Table table = new Table();
        table.setFillParent(true);
        table.add(new TextraLabel("S2Df ", font)).width(60).height(font.cellHeight * 2);
        table.add(s2dField).width(200).height(s2dField.getStyle().font.getLineHeight() * 2).row();
        table.add(new TextraLabel("TTf  ", font)).width(60).height(font.cellHeight * 2);
        table.add(ttField).width(200).height(font.cellHeight * 2).row();
        table.add(new TextraLabel("S2Da ", font)).width(60).height(font.cellHeight * 2);
        table.add(s2dArea).width(200).height(s2dArea.getStyle().font.getLineHeight() * 5.5f).row();
        table.add(new TextraLabel("TTa  ", font)).width(60).height(font.cellHeight * 2);
        table.add(ttArea).width(200).height(font.cellHeight * 5).row();
        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.DARK_GRAY);

//        System.out.println(ttArea.label.workingLayout.lines);

        stage.act();
        stage.draw();

        if(Gdx.input.isKeyJustPressed(Input.Keys.INSERT)){
            System.out.println(ttArea.inner.label.getWorkingLayout().toString().replace('\n', '\\'));
            System.out.println();
            System.err.println(ttArea.inner.label.layout.toString().replace('\n', '\\'));
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        ttField.label.font.resizeDistanceField(width, height, stage.getViewport());
        ttArea.getFont().resizeDistanceField(width, height, stage.getViewport());
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("TextraLabel UI test");
        config.setWindowedMode(600, 600);
        config.disableAudio(true);
        config.setForegroundFPS(60);
        config.useVsync(true);
        new Lwjgl3Application(new FieldTest(), config);
    }

}