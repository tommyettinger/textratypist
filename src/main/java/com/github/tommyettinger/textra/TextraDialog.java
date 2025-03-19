/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.tommyettinger.textra.Styles.TextButtonStyle;
import com.github.tommyettinger.textra.Styles.WindowStyle;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * Displays a dialog, which is a window with a title, a content table, and a button table. Methods are provided to add a
 * label to the content table and buttons to the button table, but any widgets can be added. When a button is clicked,
 * {@link #result(Object)} is called and the dialog is removed from the stage.
 *
 * @author Nathan Sweet
 */
public class TextraDialog extends TextraWindow {
    Table contentTable, buttonTable;
    private @Null Skin skin;
    ObjectMap<Actor, Object> values = new ObjectMap<>();
    boolean cancelHide;
    Actor previousKeyboardFocus, previousScrollFocus;
    FocusListener focusListener;

    protected InputListener ignoreTouchDown = new InputListener() {
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            event.cancel();
            return false;
        }
    };

    public TextraDialog(String title, Skin skin) {
        super(title, skin.get(WindowStyle.class));
        setSkin(skin);
        this.skin = skin;
        initialize();
    }

    public TextraDialog(String title, Skin skin, String windowStyleName) {
        super(title, skin.get(windowStyleName, WindowStyle.class));
        setSkin(skin);
        this.skin = skin;
        initialize();
    }

    public TextraDialog(String title, WindowStyle windowStyle) {
        super(title, windowStyle);
        initialize();
    }

    public TextraDialog(String title, Skin skin, Font replacementFont) {
        super(title, skin.get(WindowStyle.class), replacementFont);
        setSkin(skin);
        this.skin = skin;
        initialize();
    }

    public TextraDialog(String title, Skin skin, String windowStyleName, Font replacementFont) {
        super(title, skin.get(windowStyleName, WindowStyle.class), replacementFont);
        setSkin(skin);
        this.skin = skin;
        initialize();
    }

    public TextraDialog(String title, WindowStyle windowStyle, Font replacementFont) {
        super(title, windowStyle, replacementFont);
        initialize();
    }

    protected TextraLabel newLabel(String text, Styles.LabelStyle style) {
        return new TextraLabel(text, style);
    }

    protected TextraLabel newLabel(String text, Font font, Color color) {
        return new TextraLabel(text, font, color);
    }

    protected TypingLabel newTypingLabel(String text, Styles.LabelStyle style) {
        return new TypingLabel(text, style);
    }

    protected TypingLabel newTypingLabel(String text, Font font, Color color) {
        return new TypingLabel(text, font, color);
    }


    private void initialize() {
        setModal(true);

        defaults().space(6);
        add(contentTable = new Table(skin)).expand().fill();
        row();
        add(buttonTable = new Table(skin)).fillX();

        contentTable.defaults().space(6);
        buttonTable.defaults().space(6);

        buttonTable.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                if (!values.containsKey(actor)) return;
                while (actor.getParent() != buttonTable)
                    actor = actor.getParent();
                result(values.get(actor));
                if (!cancelHide) hide();
                cancelHide = false;
            }
        });

        focusListener = new FocusListener() {
            public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
                if (!focused) focusChanged(event);
            }

            public void scrollFocusChanged(FocusEvent event, Actor actor, boolean focused) {
                if (!focused) focusChanged(event);
            }

            private void focusChanged(FocusEvent event) {
                Stage stage = getStage();
                if (isModal() && stage != null && stage.getRoot().getChildren().size > 0
                        && stage.getRoot().getChildren().peek() == TextraDialog.this) { // TextraDialog is top most actor.
                    Actor newFocusedActor = event.getRelatedActor();
                    if (newFocusedActor != null && !newFocusedActor.isDescendantOf(TextraDialog.this)
                            && !(newFocusedActor.equals(previousKeyboardFocus) || newFocusedActor.equals(previousScrollFocus)))
                        event.cancel();
                }
            }
        };
    }

    protected void setStage(Stage stage) {
        if (stage == null)
            addListener(focusListener);
        else
            removeListener(focusListener);
        super.setStage(stage);
    }

    public Table getContentTable() {
        return contentTable;
    }

    public Table getButtonTable() {
        return buttonTable;
    }

    /**
     * Adds a TextraLabel to the content table. The dialog must have been constructed with a skin to use this method.
     */
    public TextraDialog text(@Null String text) {
        if (skin == null)
            throw new IllegalStateException("This method may only be used if the dialog was constructed with a Skin.");
        return text(text, skin.get(Styles.LabelStyle.class));
    }

    /**
     * Adds a TextraLabel to the content table.
     */
    public TextraDialog text(@Null String text, Styles.LabelStyle labelStyle) {
        return text(newLabel(text, labelStyle));
    }

    /**
     * Adds a TextraLabel to the content table.
     */
    public TextraDialog text(@Null String text, Font font) {
        return text(newLabel(text, font, Color.WHITE));
    }

    /**
     * Adds a TextraLabel to the content table.
     */
    public TextraDialog text(@Null String text, Font font, Color color) {
        return text(newLabel(text, font, color));
    }

    /**
     * Adds the given TextraLabel to the content table.
     * @param label a non-null TextraLabel
     */
    public TextraDialog text(TextraLabel label) {
        contentTable.add(label);
        return this;
    }

    /**
     * Adds a TypingLabel to the content table. The dialog must have been constructed with a skin to use this method.
     */
    public TextraDialog typing(@Null String text) {
        if (skin == null)
            throw new IllegalStateException("This method may only be used if the dialog was constructed with a Skin.");
        return typing(text, skin.get(Styles.LabelStyle.class));
    }

    /**
     * Adds a TypingLabel to the content table.
     */
    public TextraDialog typing(@Null String text, Styles.LabelStyle labelStyle) {
        return typing(newTypingLabel(text, labelStyle));
    }

    /**
     * Adds a TypingLabel to the content table.
     */
    public TextraDialog typing(@Null String text, Font font) {
        return typing(newTypingLabel(text, font, Color.WHITE));
    }

    /**
     * Adds a TypingLabel to the content table.
     */
    public TextraDialog typing(@Null String text, Font font, Color color) {
        return typing(newTypingLabel(text, font, color));
    }

    /**
     * Adds the given TypingLabel to the content table.
     * @param label a non-null TypingLabel
     */
    public TextraDialog typing(TypingLabel label) {
        contentTable.add(label);
        return this;
    }

    /**
     * Adds a text button to the button table. Null will be passed to {@link #result(Object)} if this button is clicked. The
     * dialog must have been constructed with a skin to use this method.
     */
    public TextraDialog button(@Null String text) {
        return button(text, null);
    }

    /**
     * Adds a text button to the button table. The dialog must have been constructed with a skin to use this method.
     *
     * @param object The object that will be passed to {@link #result(Object)} if this button is clicked. May be null.
     */
    public TextraDialog button(@Null String text, @Null Object object) {
        if (skin == null)
            throw new IllegalStateException("This method may only be used if the dialog was constructed with a Skin.");
        return button(text, object, skin.get(TextButtonStyle.class));
    }

    /**
     * Adds a text button to the button table.
     *
     * @param object The object that will be passed to {@link #result(Object)} if this button is clicked. May be null.
     */
    public TextraDialog button(@Null String text, @Null Object object, TextButtonStyle buttonStyle) {
        return button(font == null
                ? new TextraButton(text, buttonStyle)
                : new TextraButton(text, buttonStyle, font), object);
    }

    /**
     * Adds the given button to the button table.
     */
    public TextraDialog button(Button button) {
        return button(button, null);
    }

    /**
     * Adds the given button to the button table.
     *
     * @param object The object that will be passed to {@link #result(Object)} if this button is clicked. May be null.
     */
    public TextraDialog button(Button button, @Null Object object) {
        buttonTable.add(button);
        setObject(button, object);
        return this;
    }

    /**
     * {@link #pack() Packs} the dialog (but doesn't set the position), adds it to the stage, sets it as the keyboard and scroll
     * focus, clears any actions on the dialog, and adds the specified action to it. The previous keyboard and scroll focus are
     * remembered so they can be restored when the dialog is hidden.
     *
     * @param action May be null.
     */
    public TextraDialog show(Stage stage, @Null Action action) {
        clearActions();
        removeCaptureListener(ignoreTouchDown);

        previousKeyboardFocus = null;
        Actor actor = stage.getKeyboardFocus();
        if (actor != null && !actor.isDescendantOf(this)) previousKeyboardFocus = actor;

        previousScrollFocus = null;
        actor = stage.getScrollFocus();
        if (actor != null && !actor.isDescendantOf(this)) previousScrollFocus = actor;

        stage.addActor(this);
        pack();
        stage.cancelTouchFocus();
        stage.setKeyboardFocus(this);
        stage.setScrollFocus(this);
        if (action != null) addAction(action);

        return this;
    }

    /**
     * Centers the dialog in the stage and calls {@link #show(Stage, Action)} with a {@link Actions#fadeIn(float, Interpolation)}
     * action.
     */
    public TextraDialog show(Stage stage) {
        show(stage, sequence(Actions.alpha(0), Actions.fadeIn(0.4f, Interpolation.fade)));
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));
        return this;
    }

    /**
     * Removes the dialog from the stage, restoring the previous keyboard and scroll focus, and adds the specified action to the
     * dialog.
     *
     * @param action If null, the dialog is removed immediately. Otherwise, the dialog is removed when the action completes. The
     *               dialog will not respond to touch down events during the action.
     */
    public void hide(@Null Action action) {
        Stage stage = getStage();
        if (stage != null) {
            removeListener(focusListener);
            if (previousKeyboardFocus != null && previousKeyboardFocus.getStage() == null) previousKeyboardFocus = null;
            Actor actor = stage.getKeyboardFocus();
            if (actor == null || actor.isDescendantOf(this)) stage.setKeyboardFocus(previousKeyboardFocus);

            if (previousScrollFocus != null && previousScrollFocus.getStage() == null) previousScrollFocus = null;
            actor = stage.getScrollFocus();
            if (actor == null || actor.isDescendantOf(this)) stage.setScrollFocus(previousScrollFocus);
        }
        if (action != null) {
            addCaptureListener(ignoreTouchDown);
            addAction(sequence(action, Actions.removeListener(ignoreTouchDown, true), Actions.removeActor()));
        } else
            remove();
    }

    /**
     * Hides the dialog. Called automatically when a button is clicked. The default implementation fades out the dialog
     * over 400 milliseconds.
     */
    public void hide() {
        hide(fadeOut(0.4f, Interpolation.fade));
    }

    /**
     * Hides the dialog. Called automatically when a button is clicked. The default implementation fades out the dialog
     * over {@code durationSeconds} seconds.
     * @param durationSeconds how many seconds for the fade Action to last before this completely disappears
     */
    public void hide(float durationSeconds) {
        hide(fadeOut(durationSeconds, Interpolation.fade));
    }

    public void setObject(Actor actor, @Null Object object) {
        values.put(actor, object);
    }

    /**
     * If this key is pressed, {@link #result(Object)} is called with the specified object.
     *
     * @see Keys
     */
    public TextraDialog key(final int keycode, final @Null Object object) {
        addListener(new InputListener() {
            public boolean keyDown(InputEvent event, int keycode2) {
                if (keycode == keycode2) {
                    // Delay a frame to eat the keyTyped event.
                    Gdx.app.postRunnable(new Runnable() {
                        public void run() {
                            result(object);
                            if (!cancelHide) hide();
                            cancelHide = false;
                        }
                    });
                }
                return false;
            }
        });
        return this;
    }

    /**
     * Called when a button is clicked. The dialog will be hidden after this method returns unless {@link #cancel()} is
     * called.
     *
     * @param object The object specified when the button was added.
     */
    protected void result(@Null Object object) {
    }

    public void cancel() {
        cancelHide = true;
    }
}
