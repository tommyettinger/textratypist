package com.github.tommyettinger.textra;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ArraySelection;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ObjectSet;

/** A select box (aka a drop-down list) allows a user to choose one of a number of values from a list. When inactive, the selected
 * value is displayed. When activated, it shows the list of values that may be selected.
 * <p>
 * {@link ChangeEvent} is fired when the selectbox selection changes.
 * <p>
 * The preferred size of the select box is determined by the maximum text bounds of the items and the size of the
 * {@link Styles.SelectBoxStyle#background}.
 * @author mzechner
 * @author Nathan Sweet */
public class TextraSelectBox extends Widget implements Disableable {
    static final Vector2 temp = new Vector2();

    Styles.SelectBoxStyle style;
    final Array<TextraLabel> items = new Array<>();
    SelectBoxScrollPane scrollPane;
    private float prefWidth, prefHeight;
    private ClickListener clickListener;
    boolean disabled;
    private int alignment = Align.left;
    boolean selectedPrefWidth;

    final ArraySelection<TextraLabel> selection = new ArraySelection<TextraLabel>(items) {
        public boolean fireChangeEvent () {
            if (selectedPrefWidth) invalidateHierarchy();
            return super.fireChangeEvent();
        }
    };

    public TextraSelectBox(Skin skin) {
        this(skin.get(Styles.SelectBoxStyle.class));
    }

    public TextraSelectBox(Skin skin, String styleName) {
        this(skin.get(styleName, Styles.SelectBoxStyle.class));
    }

    public TextraSelectBox(Styles.SelectBoxStyle style) {
        setStyle(style);
        setSize(getPrefWidth(), getPrefHeight());

        selection.setActor(this);
        selection.setRequired(true);

        scrollPane = newScrollPane();

        addListener(clickListener = new ClickListener() {
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                if (pointer == 0 && button != 0) return false;
                if (isDisabled()) return false;
                if (scrollPane.hasParent())
                    hideScrollPane();
                else
                    showScrollPane();
                return true;
            }
        });
    }

    /** Allows a subclass to customize the scroll pane shown when the select box is open. */
    protected SelectBoxScrollPane newScrollPane () {
        return new SelectBoxScrollPane(this);
    }

    /** Set the max number of items to display when the select box is opened. Set to 0 (the default) to display as many as fit in
     * the stage height. */
    public void setMaxListCount (int maxListCount) {
        scrollPane.maxListCount = maxListCount;
    }

    /** @return Max number of items to display when the box is opened, or <= 0 to display them all. */
    public int getMaxListCount () {
        return scrollPane.maxListCount;
    }

    protected void setStage (Stage stage) {
        if (stage == null) scrollPane.hide();
        super.setStage(stage);
    }

    public void setStyle (Styles.SelectBoxStyle style) {
        if (style == null) throw new IllegalArgumentException("style cannot be null.");
        this.style = style;

        if (scrollPane != null) {
            scrollPane.setStyle(style.scrollStyle);
            scrollPane.list.setStyle(style.listStyle);
        }
        invalidateHierarchy();
    }

    /** Returns the select box's style. Modifying the returned style may not have an effect until {@link #setStyle(Styles.SelectBoxStyle)}
     * is called. */
    public Styles.SelectBoxStyle getStyle () {
        return style;
    }

    /**
     * Sets the choices available in the SelectBox using an array or varargs of markup Strings.
     * Marks up each String in {@code newMarkupTexts} as a TextraLabel and adds that label to the choices.
     * @param newMarkupTexts an array or varargs of individual markup Strings, one per choice
     */
    public void setItemTexts (String... newMarkupTexts) {
        if (newMarkupTexts == null) throw new IllegalArgumentException("newMarkupTexts cannot be null.");
        float oldPrefWidth = getPrefWidth();

        items.clear();
        for (int i = 0; i < newMarkupTexts.length; i++) {
            items.add(newLabel(newMarkupTexts[i], style.font, style.fontColor));
        }
        selection.validate();
        scrollPane.list.setItems(items);

        invalidate();
        if (oldPrefWidth != getPrefWidth()) invalidateHierarchy();
    }

    /** Set the backing Array that makes up the choices available in the SelectBox. */
    public void setItems (TextraLabel... newItems) {
        if (newItems == null) throw new IllegalArgumentException("newItems cannot be null.");
        float oldPrefWidth = getPrefWidth();

        items.clear();
        items.addAll(newItems);
        selection.validate();
        scrollPane.list.setItems(items);

        invalidate();
        if (oldPrefWidth != getPrefWidth()) invalidateHierarchy();
    }

    /** Sets the items visible in the select box. */
    public void setItems (Array<? extends TextraLabel> newItems) {
        if (newItems == null) throw new IllegalArgumentException("newItems cannot be null.");
        float oldPrefWidth = getPrefWidth();

        if (newItems != items) {
            items.clear();
            items.addAll(newItems);
        }
        selection.validate();
        scrollPane.list.setItems(items);

        invalidate();
        if (oldPrefWidth != getPrefWidth()) invalidateHierarchy();
    }

    public void clearItems () {
        if (items.size == 0) return;
        items.clear();
        selection.clear();
        scrollPane.list.clearItems();
        invalidateHierarchy();
    }

    /** Returns the internal items array. If modified, {@link #setItems(Array)} must be called to reflect the changes. */
    public Array<TextraLabel> getItems () {
        return items;
    }

    public void layout () {
        Drawable bg = style.background;
        Font font = style.font;

        if (bg != null) {
            prefHeight = Math.max(bg.getTopHeight() + bg.getBottomHeight() + font.cellHeight - font.descent * font.scaleY,
                    bg.getMinHeight());
        } else
            prefHeight = font.cellHeight - font.descent * font.scaleY;

        if (selectedPrefWidth) {
            prefWidth = 0;
            if (bg != null) prefWidth = bg.getLeftWidth() + bg.getRightWidth();
            TextraLabel selected = getSelected();
            if (selected != null) {
//                selected.layout.setTargetWidth(Gdx.graphics.getBackBufferWidth());
                prefWidth += selected.font.calculateSize(selected.layout);
            }
        } else {
            float maxItemWidth = 0;
            TextraLabel item;
            for (int i = 0; i < items.size; i++) {
                item = items.get(i);
//                item.layout.setTargetWidth(Gdx.graphics.getBackBufferWidth());
                maxItemWidth = Math.max(item.font.calculateSize(item.layout), maxItemWidth);
            }

            prefWidth = maxItemWidth;
            if (bg != null) prefWidth = Math.max(prefWidth + bg.getLeftWidth() + bg.getRightWidth(), bg.getMinWidth());

            Styles.ListStyle listStyle = style.listStyle;
            ScrollPaneStyle scrollStyle = style.scrollStyle;
            float scrollWidth = maxItemWidth + listStyle.selection.getLeftWidth() + listStyle.selection.getRightWidth();
            bg = scrollStyle.background;
            if (bg != null) scrollWidth = Math.max(scrollWidth + bg.getLeftWidth() + bg.getRightWidth(), bg.getMinWidth());
            if (scrollPane == null || !scrollPane.isScrollingDisabledY()) {
                scrollWidth += Math.max(style.scrollStyle.vScroll != null ? style.scrollStyle.vScroll.getMinWidth() : 0,
                        style.scrollStyle.vScrollKnob != null ? style.scrollStyle.vScrollKnob.getMinWidth() : 0);
            }
            prefWidth = Math.max(prefWidth, scrollWidth);
        }
    }

    /** Returns appropriate background drawable from the style based on the current select box state. */
    protected @Null Drawable getBackgroundDrawable () {
        if (isDisabled() && style.backgroundDisabled != null) return style.backgroundDisabled;
        if (scrollPane.hasParent() && style.backgroundOpen != null) return style.backgroundOpen;
        if (isOver() && style.backgroundOver != null) return style.backgroundOver;
        return style.background;
    }

    /** Returns the appropriate label font color from the style based on the current button state. */
    protected Color getFontColor () {
        if (isDisabled() && style.disabledFontColor != null) return style.disabledFontColor;
        if (style.overFontColor != null && (isOver() || scrollPane.hasParent())) return style.overFontColor;
        return style.fontColor;
    }

    public void draw (Batch batch, float parentAlpha) {
        validate();

        Drawable background = getBackgroundDrawable();
        Color fontColor = getFontColor();

        Color color = getColor();
        float x = getX(), y = getY();
        float width = getWidth(), height = getHeight();

        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        if (background != null) background.draw(batch, x, y, width, height);

        TextraLabel selected = selection.first();
        if (selected != null) {
            if (background != null) {
                width -= background.getLeftWidth() + background.getRightWidth();
                height -= background.getBottomHeight() + background.getTopHeight();
                x += background.getLeftWidth();
                y += (int)(height * 0.5f + background.getBottomHeight());
            } else {
                y += (int)(height * 0.5f);
            }
            selected.setColor(fontColor.r, fontColor.g, fontColor.b, fontColor.a * parentAlpha);
            drawItem(batch, selected, x, y, width);
        }
    }

    protected void drawItem (Batch batch, TextraLabel item, float x, float y, float width) {
        item.setEllipsis("...");
        item.setWrap(false);
        item.layout.setTargetWidth(width);
        item.setPosition(x, y, Align.left);
        item.draw(batch, 1f);
    }

    /** Sets the alignment of the selected item in the select box. See {@link #getList()} and {@link List#setAlignment(int)} to set
     * the alignment in the list shown when the select box is open.
     * @param alignment See {@link Align}. */
    public void setAlignment (int alignment) {
        this.alignment = alignment;
    }

    /** Get the set of selected items, useful when multiple items are selected
     * @return a Selection object containing the selected elements */
    public ArraySelection<TextraLabel> getSelection () {
        return selection;
    }

    /** Returns the first selected item, or null. For multiple selections use {@link com.badlogic.gdx.scenes.scene2d.ui.SelectBox#getSelection()}. */
    public @Null TextraLabel getSelected () {
        return selection.first();
    }

    /** Sets the selection to only the passed item, if it is a possible choice, else selects the first item. */
    public void setSelected (@Null TextraLabel item) {
        if (items.contains(item, false))
            selection.set(item);
        else if (items.size > 0)
            selection.set(items.first());
        else
            selection.clear();
    }

    /** @return The index of the first selected item. The top item has an index of 0. Nothing selected has an index of -1. */
    public int getSelectedIndex () {
        ObjectSet<TextraLabel> selected = selection.items();
        return selected.size == 0 ? -1 : items.indexOf(selected.first(), false);
    }

    /** Sets the selection to only the selected index. */
    public void setSelectedIndex (int index) {
        selection.set(items.get(index));
    }

    /** When true the pref width is based on the selected item. */
    public void setSelectedPrefWidth (boolean selectedPrefWidth) {
        this.selectedPrefWidth = selectedPrefWidth;
    }

    public boolean getSelectedPrefWidth () {
        return selectedPrefWidth;
    }

    /** Returns the pref width of the select box if the widest item was selected, for use when
     * {@link #setSelectedPrefWidth(boolean)} is true. */
    public float getMaxPrefWidth () {
        float width = 0;
        TextraLabel item;
        for (int i = 0; i < items.size; i++) {
            item = items.get(i);
//            item.layout.setTargetWidth(Gdx.graphics.getBackBufferWidth());
            width = Math.max(item.font.calculateSize(item.layout), width);
        }
        Drawable bg = style.background;
        if (bg != null) width = Math.max(width + bg.getLeftWidth() + bg.getRightWidth(), bg.getMinWidth());
        return width;
    }

    public void setDisabled (boolean disabled) {
        if (disabled && !this.disabled) hideScrollPane();
        this.disabled = disabled;
    }

    public boolean isDisabled () {
        return disabled;
    }

    public float getPrefWidth () {
        validate();
        return prefWidth;
    }

    public float getPrefHeight () {
        validate();
        return prefHeight;
    }

    protected String toString (TextraLabel item) {
        return item.toString();
    }

    /** @deprecated Use {@link #showScrollPane()}. */
    @Deprecated
    public void showList () {
        showScrollPane();
    }

    public void showScrollPane () {
        if (items.size == 0) return;
        if (getStage() != null) scrollPane.show(getStage());
    }

    /** @deprecated Use {@link #hideScrollPane()}. */
    @Deprecated
    public void hideList () {
        hideScrollPane();
    }

    public void hideScrollPane () {
        scrollPane.hide();
    }

    /** Returns the list shown when the select box is open. */
    public TextraListBox<TextraLabel> getList () {
        return scrollPane.list;
    }

    /** Disables scrolling of the list shown when the select box is open. */
    public void setScrollingDisabled (boolean y) {
        scrollPane.setScrollingDisabled(true, y);
        invalidateHierarchy();
    }

    /** Returns the scroll pane containing the list that is shown when the select box is open. */
    public SelectBoxScrollPane getScrollPane () {
        return scrollPane;
    }

    public boolean isOver () {
        return clickListener.isOver();
    }

    public ClickListener getClickListener () {
        return clickListener;
    }

    protected void onShow (Actor scrollPane, boolean below) {
        scrollPane.getColor().a = 0;
        scrollPane.addAction(fadeIn(0.3f, Interpolation.fade));
    }

    protected void onHide (Actor scrollPane) {
        scrollPane.getColor().a = 1;
        scrollPane.addAction(sequence(fadeOut(0.15f, Interpolation.fade), removeActor()));
    }

    protected TextraLabel newLabel(String markupText, Font font, Color color) {
        return new TextraLabel(markupText, font, color);
    }

    /** The scroll pane shown when a select box is open.
     * @author Nathan Sweet */
    public static class SelectBoxScrollPane extends ScrollPane {
        final TextraSelectBox selectBox;
        int maxListCount;
        private final Vector2 stagePosition = new Vector2();
        final TextraListBox<TextraLabel> list;
        private InputListener hideListener;
        private Actor previousScrollFocus;

        public SelectBoxScrollPane (final TextraSelectBox selectBox) {
            super(null, selectBox.style.scrollStyle);
            this.selectBox = selectBox;

            setOverscroll(false, false);
            setFadeScrollBars(false);
            setScrollingDisabled(true, false);

            list = newList();
            list.setTouchable(Touchable.disabled);
            list.setTypeToSelect(true);
            setActor(list);

            list.addListener(new ClickListener() {
                public void clicked (InputEvent event, float x, float y) {
                    TextraLabel selected = list.getSelected();
                    // Force clicking the already selected item to trigger a change event.
                    if (selected != null) selectBox.selection.items().clear(51);
                    selectBox.selection.choose(selected);
                    hide();
                }

                public boolean mouseMoved (InputEvent event, float x, float y) {
                    int index = list.getItemIndexAt(y);
                    if (index != -1) list.setSelectedIndex(index);
                    return true;
                }
            });

            addListener(new InputListener() {
                public void exit (InputEvent event, float x, float y, int pointer, @Null Actor toActor) {
                    if (toActor == null || !isAscendantOf(toActor)) {
                        TextraLabel selected = selectBox.getSelected();
                        if (selected != null) list.getSelection().set(selected);
                    }
                }
            });

            hideListener = new InputListener() {
                public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                    Actor target = event.getTarget();
                    if (isAscendantOf(target)) return false;
                    list.getSelection().set(selectBox.getSelected());
                    hide();
                    return false;
                }

                public boolean keyDown (InputEvent event, int keycode) {
                    switch (keycode) {
                        case Keys.NUMPAD_ENTER:
                        case Keys.ENTER:
                            selectBox.selection.choose(list.getSelected());
                            // Fall thru.
                        case Keys.ESCAPE:
                            hide();
                            event.stop();
                            return true;
                    }
                    return false;
                }
            };
        }

        /** Allows a subclass to customize the select box list. */
        protected TextraListBox<TextraLabel> newList () {
            return new TextraListBox<>(selectBox.style.listStyle);
        }

        public void show (Stage stage) {
            if (list.isTouchable()) return;

            stage.addActor(this);
            stage.addCaptureListener(hideListener);
            stage.addListener(list.getKeyListener());

            selectBox.localToStageCoordinates(stagePosition.set(0, 0));

            // Show the list above or below the select box, limited to a number of items and the available height in the stage.
            float height = list.getCumulativeHeight(maxListCount <= 0 ? selectBox.items.size - 1 : Math.min(maxListCount, selectBox.items.size) - 1);
            Drawable scrollPaneBackground = getStyle().background;
            if (scrollPaneBackground != null) height += scrollPaneBackground.getTopHeight() + scrollPaneBackground.getBottomHeight();
            Drawable listBackground = list.getStyle().background;
            if (listBackground != null) height += listBackground.getTopHeight() + listBackground.getBottomHeight();

            float heightBelow = stagePosition.y;
            float heightAbove = stage.getHeight() - heightBelow - selectBox.getHeight();
            boolean below = true;
            if (height > heightBelow) {
                if (heightAbove > heightBelow) {
                    below = false;
                    height = Math.min(height, heightAbove);
                } else
                    height = heightBelow;
            }

            if (below)
                setY(stagePosition.y - height);
            else
                setY(stagePosition.y + selectBox.getHeight());
            setX(stagePosition.x);
            setHeight(height);
            validate();
            float width = Math.max(getPrefWidth(), selectBox.getWidth());
            setWidth(width);

            validate();
            scrollTo(0, list.getHeight() -
                    list.getCumulativeHeight(selectBox.getSelectedIndex()) - selectBox.getSelected().getPrefHeight() * 0.5f,
                    0, 0, true, true);
            updateVisualScroll();

            previousScrollFocus = null;
            Actor actor = stage.getScrollFocus();
            if (actor != null && !actor.isDescendantOf(this)) previousScrollFocus = actor;
            stage.setScrollFocus(this);

            list.getSelection().set(selectBox.getSelected());
            list.setTouchable(Touchable.enabled);
            clearActions();
            selectBox.onShow(this, below);
        }

        public void hide () {
            if (!list.isTouchable() || !hasParent()) return;
            list.setTouchable(Touchable.disabled);

            Stage stage = getStage();
            if (stage != null) {
                stage.removeCaptureListener(hideListener);
                stage.removeListener(list.getKeyListener());
                if (previousScrollFocus != null && previousScrollFocus.getStage() == null) previousScrollFocus = null;
                Actor actor = stage.getScrollFocus();
                if (actor == null || isAscendantOf(actor)) stage.setScrollFocus(previousScrollFocus);
            }

            clearActions();
            selectBox.onHide(this);
        }

        public void draw (Batch batch, float parentAlpha) {
            selectBox.localToStageCoordinates(temp.set(0, 0));
            if (!temp.equals(stagePosition)) hide();
            super.draw(batch, parentAlpha);
        }

        public void act (float delta) {
            super.act(delta);
            toFront();
        }

        protected void setStage (Stage stage) {
            Stage oldStage = getStage();
            if (oldStage != null) {
                oldStage.removeCaptureListener(hideListener);
                oldStage.removeListener(list.getKeyListener());
            }
            super.setStage(stage);
        }

        public TextraListBox<TextraLabel> getList () {
            return list;
        }

        public TextraSelectBox getSelectBox () {
            return selectBox;
        }
    }
}
