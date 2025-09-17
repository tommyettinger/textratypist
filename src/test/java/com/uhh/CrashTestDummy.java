package com.uhh;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.Styles.LabelStyle;
import com.ray3k.stripe.FreeTypeSkin;

public class CrashTestDummy extends ApplicationAdapter {

	/*
	 * Definite contributing factors:
	 */
	// Using an icons from a texture atlas
	// My frame-by-frame resizing AdaptiveLabel
	// Parent table with variable width + label with growX()

	public static final String REGULAR_FONT_NAME = "Exo-Medium";
	public FreeTypeFontGenerator regularFontGenerator;

	private TextureAtlas textureAtlas;
	private Skin skin;
	private LabelStyle defaultLabelStyle;
	private Stage stage;
	private Actor testParent;

	@Override
	public void create() {
		makeSkin();
		stage = new Stage(new ScreenViewport());
        stage.setDebugAll(true);

		testParent = getLabelParentTable();
		testParent.setPosition(100, 100);
		stage.addActor(testParent);
	}

	@Override
	public void render() {
		ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

		testParent.setWidth(stage.getWidth() * 0.25f);

		stage.act();
		stage.getViewport().apply();
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		resizeFont(width, height);
		defaultLabelStyle.font = skin.get(REGULAR_FONT_NAME, Font.class);
	}

	public Actor getLabelParentTable() {
//		AdaptiveLabel descriptionLabel = new AdaptiveLabel(() -> "Produces 5 PowerW every turn", defaultLabelStyle);
		AdaptiveLabel descriptionLabel = new AdaptiveLabel(() -> "Produces 5 Power[+powerIcon] every turn", defaultLabelStyle);
		Table table = new Table();
		table.add(descriptionLabel).growX();

		return table;
	}

	private void makeSkin() {
		regularFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("crash/" + REGULAR_FONT_NAME + ".ttf"));
		textureAtlas = new TextureAtlas(Gdx.files.internal("crash/skin.atlas"));
		skin = new FreeTypeSkin(textureAtlas);

		resizeFont(1920, 1080); // generates the font from its .ttf files and puts it in the skin

		Font regularFont = skin.get(REGULAR_FONT_NAME, Font.class);
		defaultLabelStyle = new LabelStyle(regularFont, Color.WHITE);
		skin.add("default", defaultLabelStyle);
	}

	private void resizeFont(float width, float height) {
		FreeTypeFontGenerator generator = regularFontGenerator;
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		int desiredSize = (int) (Math.ceil(Math.min(width, height) / 50f));
		int mainFontSize = Math.max(8, desiredSize); // too small crashes the generator (and is unreadable, to boot.)
		parameter.size = mainFontSize;
		BitmapFont font = generator.generateFont(parameter);
		font.getData().markupEnabled = true;
		Font mainFont = new Font(font);
		mainFont.addAtlas(textureAtlas);
		skin.add(REGULAR_FONT_NAME, mainFont);
	}

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("TextraCrashTest");
        //// Vsync limits the frames per second to what your hardware can display, and helps eliminate
        //// screen tearing. This setting doesn't always work on Linux, so the line after is a safeguard.
        configuration.useVsync(true);
        //// Limits FPS to the refresh rate of the currently active monitor, plus 1 to try to match fractional
        //// refresh rates. The Vsync setting above should limit the actual FPS to match the monitor.
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        //// If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
        //// useful for testing performance, but can also be very stressful to some hardware.
        //// You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.

        configuration.setWindowedMode(640, 480);
        configuration.disableAudio(true);
        new Lwjgl3Application(new CrashTestDummy(), configuration);
    }
}
