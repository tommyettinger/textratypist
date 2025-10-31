package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class PreviewSimpleTest extends ApplicationAdapter {

    Font fnt;
    SpriteBatch batch;
    Viewport viewport;
    Layout layout = new Layout().setTargetWidth(1200);
    int idx;
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
            // Tracking down a bug here... some modes seem to contribute too much to line width.
            // It seems to be either even-valued or odd-valued modes, since they alternate taking too much.
//            + "\n[?shiny][*]Special[*][?] [?whiten][/]Effects[/][?][#]...",
            + "\n[?small caps][*]Special[*][?] [?whiten][/]Effects[/][?][#]: [?shadow]drop shadow[?], [?jostle]RaNsoM nOtE[?], [?error]spell check[?]...",
//            + "\n[?suggest]Just some normal text...[?]",
//            + "\n[?shadow]drop shadow[?], [?jostle]RaNsoM nOtE[?], [?error]spell check[?]...",
//    distanceField = "\nWelcome to the [_][*][TEAL]Textra Zone[ ]!",
    emojiSupport = "\nPlus, there's [?neon][TEAL]emoji[ ] and more! [WHITE][+🥳] [+👍🏻] [+🤙🏼] [+👌🏽] [+🤘🏾] [+✌🏿]";

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Font Preview Generator");
        config.setWindowedMode(1200, 675);
        config.disableAudio(true);
        ShaderProgram.prependVertexCode = "#version 110\n";
        ShaderProgram.prependFragmentCode = "#version 110\n";
//        config.enableGLDebugOutput(true, System.out);
        config.setForegroundFPS(60);
        config.useVsync(true);
        new Lwjgl3Application(new PreviewSimpleTest(), config);
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        viewport = new StretchViewport(1200, 675);

        // WHAT WE NORMALLY USE
        jsonFiles = KnownFonts.JSON_NAMES.orderedItems().toArray(String[]::new);
        limit = jsonFiles.length + 1;
        all = new Font[limit * 2];
        int idx = 0;
        for (int i = 0; i < jsonFiles.length; i++) {
//            all[idx++] = KnownFonts.addEmoji(KnownFonts.getFont(jsonFiles[i], Font.DistanceFieldType.STANDARD)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
//            all[idx] = KnownFonts.addEmoji(KnownFonts.getFont(jsonFiles[i], Font.DistanceFieldType.MSDF)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
//            all[idx++] = KnownFonts.addEmoji(KnownFonts.getFont(jsonFiles[i], Font.DistanceFieldType.SDF)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
//            all[idx++] = KnownFonts.addEmoji(KnownFonts.getFont(jsonFiles[i], Font.DistanceFieldType.SDF_OUTLINE)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
            all[idx] = KnownFonts.addEmoji(KnownFonts.getFont(jsonFiles[i], Font.DistanceFieldType.SDF)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
            all[idx++ + limit] = KnownFonts.addEmoji(KnownFonts.getFont(jsonFiles[i], Font.DistanceFieldType.SDF_OUTLINE)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
        }
//        all[idx++] = KnownFonts.addEmoji(KnownFonts.getAStarryTall(Font.DistanceFieldType.STANDARD)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
//        all[idx] = KnownFonts.addEmoji(KnownFonts.getAStarryTall(Font.DistanceFieldType.MSDF)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
        all[idx] = KnownFonts.addEmoji(KnownFonts.getAStarryTall(Font.DistanceFieldType.SDF)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
        all[idx++ + limit] = KnownFonts.addEmoji(KnownFonts.getAStarryTall(Font.DistanceFieldType.SDF_OUTLINE)).scaleHeightTo(32f).useIntegerPositions(false).setTextureFilter();
        limit += idx;
        idx = 6;

        fnt = all[idx];
        generateLayout();
    }

    public void generateLayout() {
        Color baseColor = fnt.getDistanceField() == Font.DistanceFieldType.SDF_OUTLINE ? Color.WHITE : Color.DARK_GRAY;
        viewport.update(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), true);
        fnt.resizeDistanceField(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), viewport);

        layout.setBaseColor(baseColor);
        layout.setMaxLines(20);
        layout.setEllipsis(" and so on and so forth...");
        fnt.markup(text + emojiSupport, layout.clear());
    }
    
    @Override
    public void render() {
        if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.MINUS)){
            idx = (idx + limit - 1) % limit;
            fnt = all[idx];
            Gdx.graphics.setTitle(fnt.getName());
            generateLayout();
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.EQUALS)){
            idx = (idx + 1) % limit;
            fnt = all[idx];
            Gdx.graphics.setTitle(fnt.getName());
            generateLayout();
        }
        ScreenUtils.clear(0.75f, 0.75f, 0.75f, 1f);
        float x = Gdx.graphics.getBackBufferWidth() * 0.5f;
        float y = (Gdx.graphics.getBackBufferHeight() + layout.getHeight()) * 0.5f;// - font.descent * font.scaleY;
        batch.begin();
        fnt.enableShader(batch);
        fnt.resizeDistanceField(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), viewport);
        fnt.drawGlyphs(batch, layout, x, y, Align.center);
        batch.end();

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        fnt.resizeDistanceField(width, height, viewport);
    }
}
