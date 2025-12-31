package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;

public class HeadlessTest extends ApplicationAdapter {

    Font fnt;
    Layout layout = new Layout().setTargetWidth(1200);
    int limit;
    String[] jsonFiles;
    Font[] all;

    static final String text = "Fonts can be rendered normally,{CURLY BRACKETS ARE IGNORED} but using [[tags], you can..."
            + "\n[#E74200]...use CSS-style hex colors like [*]#E74200[*]..."
            + "\n[darker purple blue]...use color names or descriptions, like [/]darker purple blue[/]...[ ]"
            + "\n[_]...and use [!]effects[!][_]!"
            + "\nNormal, [*]bold[*], [/]oblique[/] (like italic), [*][/]bold oblique[ ],"
            + "\n[_]underline (even for multiple words)[_], [~]strikethrough (same)[ ],"
            + "\nscaling: [%50]very [%75]small [%100]to [%150]quite [%200]large[ ], notes: [.]sub-[.], [=]mid-[=], and [^]super-[^]script,"
            + "\ncapitalization changes: [;]Each cap, [,]All lower, [!]Caps lock[ ],"
            + "\n[%^small caps][*]Special[*][%] [%^whiten][/]Effects[/][%]: [%?shadow]drop shadow[%], [%?jostle]RaNsoM nOtE[%], [%?error]spell check[%]...",
    emojiSupport = "\nPlus, there's [_][*][TEAL]emoji[ ] and more! [WHITE][+🥳] [+👍🏻] [+🤙🏼] [+👌🏽] [+🤘🏾] [+✌🏿]";

    public static void main(String[] args){
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        new HeadlessApplication(new HeadlessTest(), config);
    }

    @Override
    public void create() {
        Font.canUseTextures = false;
        jsonFiles = KnownFonts.JSON_NAMES.orderedItems().toArray(String[]::new);
        limit = jsonFiles.length + 1;
        all = new Font[limit];
        int idx = 0;
        for (int i = 0; i < jsonFiles.length; i++) {
//            all[idx++] = KnownFonts.addEmoji(KnownFonts.getFont(jsonFiles[i], Font.DistanceFieldType.STANDARD)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
//            all[idx] = KnownFonts.addEmoji(KnownFonts.getFont(jsonFiles[i], Font.DistanceFieldType.MSDF)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
//            all[idx++] = KnownFonts.addEmoji(KnownFonts.getFont(jsonFiles[i], Font.DistanceFieldType.SDF)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
//            all[idx++] = KnownFonts.addEmoji(KnownFonts.getFont(jsonFiles[i], Font.DistanceFieldType.SDF_OUTLINE)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
            all[idx++] = KnownFonts.addEmoji(KnownFonts.getFont(jsonFiles[i], Font.DistanceFieldType.SDF)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
//            all[idx++ + limit] = KnownFonts.addEmoji(KnownFonts.getFont(jsonFiles[i], Font.DistanceFieldType.SDF_OUTLINE)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
        }
//        all[idx++] = KnownFonts.addEmoji(KnownFonts.getAStarryTall(Font.DistanceFieldType.STANDARD)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
//        all[idx] = KnownFonts.addEmoji(KnownFonts.getAStarryTall(Font.DistanceFieldType.MSDF)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
        all[idx++] = KnownFonts.addEmoji(KnownFonts.getAStarryTall(Font.DistanceFieldType.SDF)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
//        all[idx++ + limit] = KnownFonts.addEmoji(KnownFonts.getAStarryTall(Font.DistanceFieldType.SDF_OUTLINE)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
//        limit += idx;

        for (int i = 0; i < limit; i++) {
            fnt = all[i];
            generateLayout();
        }
        Gdx.app.exit();
    }

    public void generateLayout() {
        Color baseColor = fnt.getDistanceField() == Font.DistanceFieldType.SDF_OUTLINE ? Color.WHITE : Color.DARK_GRAY;
        layout.setBaseColor(baseColor);
        layout.setMaxLines(20);
        layout.setEllipsis(" and so on and so forth...");
//        fnt.markup(text + emojiSupport, layout.clear()); // TODO: figure out why emoji make width NaN, but height is correct.
        fnt.markup(text, layout.clear());
        System.out.println(fnt.name + ":\nlays out with " + layout.lines() + " lines, " + layout.getWidth() + " width, and " + layout.getHeight() + " height.");

    }
    @Override
    public void render() {
//        if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.MINUS)){
//            idx = (idx + limit - 1) % limit;
//            fnt = all[idx];
//            generateLayout();
//        }
//        if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.EQUALS)){
//            idx = (idx + 1) % limit;
//            fnt = all[idx];
//            generateLayout();
//        }
//        ScreenUtils.clear(0.75f, 0.75f, 0.75f, 1f);
//        float x = Gdx.graphics.getBackBufferWidth() * 0.5f;
//        float y = (Gdx.graphics.getBackBufferHeight() + layout.getHeight()) * 0.5f;// - font.descent * font.scaleY;
//        batch.begin();
//        fnt.enableShader(batch);
//        fnt.resizeDistanceField(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), viewport);
//        fnt.drawGlyphs(batch, layout, x, y, Align.center);
//        batch.end();
    }

    @Override
    public void resize(int width, int height) {
//        viewport.update(width, height, true);
//        fnt.resizeDistanceField(width, height, viewport);
    }
}
