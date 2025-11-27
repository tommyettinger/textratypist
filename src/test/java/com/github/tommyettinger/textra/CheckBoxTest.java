package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.textra.Styles;
import com.github.tommyettinger.textra.TextraCheckBox;

public class CheckBoxTest extends ApplicationAdapter {

    private static final float IMAGE_BUTTON_WIDTH = 3 * 32;
    private static final float IMAGE_BUTTON_HEIGHT = 32;

    private Viewport viewport;
    private Stage stage;
    private FWSkin skin;
    private Table table;

    private TextraCheckBox checkBox;
    private CheckBox checkBox2;
    private TypingCheckBox checkBox3;

    @Override
    public void create() {
        this.viewport = new ScreenViewport();
        this.stage = new Stage(viewport);
        skin = new FWSkin(Gdx.files.internal("shadeui/standard/uiskin-standard.json"));
        this.table = new Table(skin);
        table.setWidth(stage.getWidth());
        table.setHeight(stage.getHeight());
        table.setPosition(0, 0);
        table.align(Align.center);
        table.setDebug(true);
        table.setFillParent(true);

        table.add(createTableContent()); //buttons etc
        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);
    }
    private Table createTableContent() {
        Table table = new Table(skin);
        table.setWidth(stage.getWidth());
        table.setHeight(stage.getHeight());
        table.setPosition(0, 0);
        table.align(Align.bottom);
        table.padBottom(8);
        table.padLeft(8);
        table.setDebug(true);

//        TextraCheckBox - checkbox image isn't drawn: checkBox.getImageCell() is null
        table.row();
        checkBox = new TextraCheckBox("test", skin);
        checkBox.setDebug(true);
        table.add(checkBox);
        checkBox.setChecked(true);
        checkBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println("checkbox is " + checkBox.isChecked());
            }
        });

        //scenes2d.ui.CheckBox - ok
        table.row();
        checkBox2 = new CheckBox("test2", skin);
        table.add(checkBox2);

        //TextraCheckBoxCustom:
        //replaced
        // addActorBefore(image, label);
        // imageCell = getCell(image);
        // with
        // imageCell = add(image);
        // --> ok
        table.row();
        checkBox3 = new TypingCheckBox("test", skin);
        checkBox3.setDebug(true);
        table.add(checkBox3);
        checkBox3.setChecked(true);
        checkBox3.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println("checkbox3 is " + checkBox3.isChecked());
            }
        });

        return table;
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1f);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        System.out.println("textra check box:");
        System.out.println(checkBox.getImageCell());
        System.out.println(checkBox.getImage());
        System.out.println("scene 2d check box:");
        System.out.println(checkBox2.getImageCell());
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("CheckBox test");
        config.setWindowedMode(640, 575);
        config.disableAudio(true);
		config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new CheckBoxTest(), config);
    }
}
