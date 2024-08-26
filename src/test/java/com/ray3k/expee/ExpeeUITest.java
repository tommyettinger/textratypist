package com.ray3k.expee;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class ExpeeUITest extends ApplicationAdapter {

    private static class Node extends Tree.Node<Node, String, Label> {
        public Node (String text, Skin skin, String style) {
            super(new Label(text, skin, style));
            setValue(text);
        }
    }

    private Stage stage;
    private Skin skin;

    @Override
    public void create() {
        skin = new Skin(Gdx.files.internal("expeeui/expee-ui.json"));
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        Table root = new Table(skin);
        root.setBackground("wallpaper");
        root.setFillParent(true);
        stage.addActor(root);
        
        root.defaults().bottom().expandY();
        final Table startMenu = new Table(skin);
        final Button startButton = new Button(skin, "start");
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startMenu.setVisible(startButton.isChecked());
                startMenu.toFront();
            }
        });
        root.add(startButton);
        Table table = new Table(skin);
        table.background("taskbar");
        root.add(table).growX();
        table = new Table(skin);
        table.background("tray");
        table.add(new Label("11:20 PM", skin, "white")).padLeft(5.0f);
        root.add(table);
        
        
        Window window = new Window("", skin, "loading");
        table = new Table();
        Image image = new Image(skin.getDrawable("logo"));
        image.setScaling(Scaling.fit);
        table.add(image);
        
        table.row();
        table.add(new LoadingActor(skin)).pad(40.0f);
        window.add(table);
        
        window.setWidth(500);
        window.setHeight(400);
        window.setPosition(225.0f, 200.0f);
        stage.addActor(window);
        
        window = new Window("Expee UI", skin);
        window.getTitleTable().add(new Button(skin, "close")).right().expandX();
        table = new Table(skin);
        table.setBackground("pane");
        table.defaults().padLeft(10.0f);
        table.add(new Label("File", skin)).padLeft(5.0f);
        table.add(new Label("Edit", skin));
        table.add(new Label("View", skin));
        table.add().growX();
        Table icon = new Table(skin);
        icon.setBackground("expee-icon");
        table.add(icon);
        window.add(table).growX();
        
        window.row();
        table = new Table(skin);
        table.setBackground("pane-large");
        table.add(new TextButton("Back", skin, "back"));
        table.add(new Button(skin, "forward"));
        table.add().growX();
        window.add(table).growX();
        
        window.row();
        table = new Table(skin);
        table.setBackground("pane");
        table.add(new Label("Address ", skin, "address-bar")).padLeft(5.0f);
        SelectBox selectBox = new SelectBox(skin);
        selectBox.setItems("C:\\", "Desktop", "Documents", "Downloads");
        table.add(selectBox).growX();
        Button button = new Button(skin, "go");
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                final Dialog dialog = new Dialog("Are you sure?", skin, "dialog");
                ChangeListener changeListener = new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        dialog.hide();
                    }
                };
                Button button = new Button(skin, "close");
                button.addListener(changeListener);
                dialog.getTitleTable().add(button).right().expandX();
                dialog.getContentTable().add(new Label("Are you sure that you're sure?", skin)).pad(30.0f).padBottom(15.0f).grow();
                dialog.getContentTable().row();
                Table table = new Table();
                table.defaults().width(70.0f).height(20.0f);
                TextButton textButton = new TextButton("OK", skin);
                textButton.addListener(changeListener);
                table.add(textButton);
                textButton = new TextButton("Cancel", skin);
                textButton.addListener(changeListener);
                table.add(textButton).padLeft(20.0f);
                dialog.getContentTable().add(table).grow();
                dialog.show(stage);
            }
        });
        table.add(button);
        window.add(table).growX();
        
        window.row();
        table = new Table();
        Table t = new Table(skin);
        ScrollPane scrollPane = new ScrollPane(t, skin);
        scrollPane.setFadeScrollBars(false);
        table.add(scrollPane).grow();
        
        ButtonGroup<CheckBox> buttonGroup = new ButtonGroup<CheckBox>();
        CheckBox cb = new CheckBox("Radio Button 1", skin, "radio");
        buttonGroup.add(cb);
        t.defaults().left().top().expandX();
        t.add(cb).padTop(10.0f);
        
        t.row();
        cb = new CheckBox("Radio Button 2", skin, "radio");
        buttonGroup.add(cb);
        t.add(cb);
        
        t.row();
        cb = new CheckBox("Radio Button 3", skin, "radio");
        buttonGroup.add(cb);
        t.add(cb);
        
        t.row();
        cb = new CheckBox("Check Box 1", skin);
        t.add(cb).padTop(10.0f);
        
        t.row();
        cb = new CheckBox("Check Box 2", skin);
        t.add(cb);
        
        t.row();
        cb = new CheckBox("Check Box 3", skin);
        t.add(cb);
        
        t.row();
        Tree<Node, Label> tree = new Tree<>(skin);
        Node node = new Node("C:\\", skin, "drive");
        tree.add(node);
        Node subNode = new Node("Prawn.jpg", skin, "file");
        node.add(subNode);
        subNode = new Node("Pics", skin, "folder");
        node.add(subNode);
        subNode = new Node("Videos", skin, "folder");
        node.add(subNode);
        
        node = new Node("E:\\", skin, "drive");
        tree.add(node);
        subNode = new Node("Things.txt", skin, "file");
        node.add(subNode);
        subNode = new Node("Stuff.dat", skin, "file");
        node.add(subNode);
        
        node = new Node("F:\\", skin, "drive");
        tree.add(node);
        subNode = new Node("Bananas", skin, "folder");
        node.add(subNode);
        node = subNode;
        subNode = new Node("In Pajamas", skin, "folder");
        node.add(subNode);
        node = subNode;
        subNode = new Node("are coming down the stairs.txt", skin, "file");
        node.add(subNode);
        
        tree.expandAll();
        t.add(tree).padTop(10.0f);
        t.add().grow();
        
        window.add(table).grow();
        
        window.setWidth(300);
        window.setHeight(400);
        window.setPosition(700.0f, 40.0f);
        stage.addActor(window);
        
        startMenu.setBackground("start-menu");
        startMenu.setVisible(false);
        startMenu.setWidth(380.0f);
        startMenu.setHeight(480.0f);
        startMenu.setY(32.0f);
        table = new Table();
        table.add(new TextButton("All Programs ", skin, "programs")).bottom().expandY();
        startMenu.add(table).growY();
        table = new Table(skin);
        table.setBackground("start-menu-right");
        startMenu.add(table).grow();
        startMenu.row();
        table = new Table(skin);
        table.setBackground("start-menu-bottom");
        table.add(new TextButton("Log Off", skin, "logoff")).expandX().right();
        table.add(new TextButton("Turn Off Computer", skin, "shutdown")).padLeft(15.0f).padRight(5.0f);
        startMenu.add(table).bottom().colspan(2);
        stage.addActor(startMenu);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
                config.setTitle("Expee UI Sample");
                config.setWindowedMode(1024, 768);
                config.disableAudio(true);
		new Lwjgl3Application(new ExpeeUITest(), config);
	}
}
