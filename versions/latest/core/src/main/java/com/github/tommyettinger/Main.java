package com.github.tommyettinger;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.github.tommyettinger.textra.*;
import com.github.tommyettinger.textra.utils.StringUtils;

import java.util.BitSet;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    Stage stage;
    Font font;
    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_INFO);
        /*
        LWJGL3:
[CASE] LOWER_CASE_LETTERS.cardinality(): 1438
[CASE] UPPER_CASE_LETTERS.cardinality(): 1122
[CASE] isLowerCase() total: 1627
[CASE] isUpperCase() total: 1164
[CASE] toLow.cardinality(): 1168
[CASE] toUp .cardinality(): 1185

        GWT:
CASE: LOWER_CASE_LETTERS.cardinality(): 1438
CASE: UPPER_CASE_LETTERS.cardinality(): 1122
CASE: isLowerCase() total: 26
CASE: isUpperCase() total: 26
CASE: toLow.cardinality(): 1173
CASE: toUp .cardinality(): 1265

        TeaVM:
CASE: LOWER_CASE_LETTERS.cardinality(): 1438
CASE: UPPER_CASE_LETTERS.cardinality(): 1122
CASE: isLowerCase() total: 1448
CASE: isUpperCase() total: 1132
CASE: toLow.cardinality(): 1228
CASE: toUp .cardinality(): 1228
         */


        Gdx.app.log("CASE", "LOWER_CASE_LETTERS.cardinality(): " + StringUtils.LOWER_CASE_LETTERS.cardinality());
        Gdx.app.log("CASE", "UPPER_CASE_LETTERS.cardinality(): " + StringUtils.UPPER_CASE_LETTERS.cardinality());
//        BitSet letters = StringUtils.LETTERS;
        int isLow = 0, isUp = 0, to;
        BitSet toLow = new BitSet(65536);
        BitSet toUp = new BitSet(65536);
        for (int i = 0; i < 65536; i++) {
//        for (int i = letters.nextSetBit(0); i >= 0; i = letters.nextSetBit(i+1)){
            if(Character.isLowerCase((char)i)) isLow++;
            if(Character.isUpperCase((char)i)) isUp ++;
            to = Character.toLowerCase((char)i);
            if(to != (char)i) toLow.set(i);
            to = Character.toUpperCase((char)i);
            if(to != (char)i) toUp .set(i);
        }
        Gdx.app.log("CASE", "isLowerCase() total: " + isLow);
        Gdx.app.log("CASE", "isUpperCase() total: " + isUp );
        Gdx.app.log("CASE", "toLow.cardinality(): " + toLow.cardinality());
        Gdx.app.log("CASE", "toUp .cardinality(): " + toUp .cardinality());
        stage = new Stage();
//        FWSkin skin = new FWSkin(Gdx.files.internal("stripped/stripped.json"), new TextureAtlas("stripped/stripped.atlas"));
//        FWSkin skin = new FWSkin(Gdx.files.internal("changa.json"), new TextureAtlas("changa.atlas"));
        FWSkin skin = new FWSkin(Gdx.files.internal("changa-old.json"), new TextureAtlas("changa-old.atlas"));
        BitmapFont bmfont = skin.getFont("one-50");
//        BitmapFont bmfont = new BitmapFont(Gdx.files.internal("one-50.fnt"));
        bmfont.getData().markupEnabled = true;
//        font = new Font(bmfont);
        font = skin.get("one-50", Font.class);


        Array<TypingLabel> labels = new Array<>(new TypingLabel[]{
                new TypingLabel("{RAINBOW}Ads Challenge!{ENDRAINBOW}", new Styles.LabelStyle(font, null))
        });
        TypingLabel label = labels.get(0);
        label.setWrap(true);
        label.setAlignment(Align.top);

        Table table = new Table();
        table.setFillParent(true);
        table.add(label).row();

//        bmfont.getData().markupEnabled = false;
//        Label s2dLabel = new Label("I can do it!", new Label.LabelStyle(bmfont, Color.WHITE));
        Label s2dLabel = new Label("[GOLD]Ads Challenge!", new Label.LabelStyle(bmfont, Color.WHITE));
//        Label s2dLabel = new Label("I [SKY]can [ROYAL]do[] it!", new Label.LabelStyle(bmfont, Color.WHITE));
        s2dLabel.setAlignment(Align.bottom);
        table.add(s2dLabel).bottom();

        stage.addActor(table);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);
        stage.act();
        stage.draw();
    }
}
