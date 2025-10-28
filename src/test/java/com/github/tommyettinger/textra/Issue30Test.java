package com.github.tommyettinger.textra;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * <a href="https://github.com/tommyettinger/textratypist/issues/29">Issue 29 test</a>.
 */
public class Issue30Test extends ApplicationAdapter {

    private Stage stage;

    public Issue30Test() {
    }

    @Override
    public void create() {
        Font font = KnownFonts.getDejaVuSans().scaleHeightTo(20);

        stage = new Stage(new FitViewport(320, 240));

        final String iconA = "a";
        final String iconB = "b";
        final String iconC = "c";

//        final TextureAtlas textureAtlas = new TextureAtlas();
//        textureAtlas.addRegion(iconA, new TextureRegion(new Texture(Gdx.files.internal(iconA + ".png"))));
//        textureAtlas.addRegion(iconB, new TextureRegion(new Texture(Gdx.files.internal(iconB + ".png"))));
//        textureAtlas.addRegion(iconC, new TextureRegion(new Texture(Gdx.files.internal(iconC + ".png"))));
//        font.addAtlas(textureAtlas);

        PixmapPacker packer = new PixmapPacker(1024, 1024, Pixmap.Format.RGBA8888, 2, false);
        packer.pack(iconA, new Pixmap(Gdx.files.internal(iconA + ".png")));
        packer.pack(iconB, new Pixmap(Gdx.files.internal(iconB + ".png")));
        packer.pack(iconC, new Pixmap(Gdx.files.internal(iconC + ".png")));
        font.addAtlas(packer.generateTextureAtlas(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear, false));

        final TypingLabel label = new TypingLabel("Press [+a]  to equip, [+b]  to drop item", font);

        final Table layout = new Table();
        layout.setFillParent(true);
        layout.add(label).center();

        stage.addActor(layout);

    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);
        stage.act();
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Issue 30 TextureAtlas test");
        config.setWindowedMode(800, 600);
        config.disableAudio(true);
        config.setForegroundFPS(10);
        config.useVsync(true);
        new Lwjgl3Application(new Issue30Test(), config);
    }
}