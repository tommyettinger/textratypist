package com.ray3k.tgmt;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;

public class AspectRatioContainer<T extends Actor> extends WidgetGroup {
    private boolean prefSizeInvalid;
    private float prefWidth;
    private float prefHeight;
    private float lastPrefHeight;
    private Actor actor;
    private float ratio;
    private boolean bindToWidth = true;
    
    public AspectRatioContainer() {
        setTouchable(Touchable.childrenOnly);
        setTransform(false);
    }
    
    public AspectRatioContainer(T actor, float ratio) {
        this();
        setActor(actor);
        this.ratio = ratio;
    }
    
    public AspectRatioContainer(T actor, float width, float height) {
        this(actor, width / height);
    }
    
    @Override
    public void invalidate() {
        super.invalidate();
        prefSizeInvalid = true;
    }
    
    private void calcPrefSize() {
        prefSizeInvalid = false;
        if (bindToWidth) {
            prefWidth = 0;
            prefHeight = Math.round(getWidth() / ratio);
        } else {
            prefWidth = Math.round(getHeight() * ratio);
            prefHeight = 0;
        }
    }
    
    @Override
    public float getPrefWidth() {
        if (prefSizeInvalid) calcPrefSize();
        return prefWidth;
    }
    
    @Override
    public float getPrefHeight() {
        if (prefSizeInvalid) calcPrefSize();
        return prefHeight;
    }
    
    @Override
    public void layout() {
        if (actor == null) return;
        
        if (prefSizeInvalid) calcPrefSize();
        if (prefHeight != lastPrefHeight) {
            lastPrefHeight = prefHeight;
            invalidateHierarchy();
        }
        
        actor.setBounds(0, 0, getWidth(), getHeight());
        if (actor instanceof Layout) ((Layout)actor).validate();
    }
    
    public void setActor(T actor) {
        this.actor = actor;
        clearChildren();
        super.addActor(actor);
    }
    
    /** @deprecated Container may have only a single child.
     * @see #setActor(Actor) */
    @Deprecated
    public void addActor (Actor actor) {
        throw new UnsupportedOperationException("Use Container#setActor.");
    }
    
    /** @deprecated Container may have only a single child.
     * @see #setActor(Actor) */
    @Deprecated
    public void addActorAt (int index, Actor actor) {
        throw new UnsupportedOperationException("Use Container#setActor.");
    }
    
    /** @deprecated Container may have only a single child.
     * @see #setActor(Actor) */
    @Deprecated
    public void addActorBefore (Actor actorBefore, Actor actor) {
        throw new UnsupportedOperationException("Use Container#setActor.");
    }
    
    /** @deprecated Container may have only a single child.
     * @see #setActor(Actor) */
    @Deprecated
    public void addActorAfter (Actor actorAfter, Actor actor) {
        throw new UnsupportedOperationException("Use Container#setActor.");
    }
    
    public boolean removeActor (Actor actor) {
        if (actor == null) throw new IllegalArgumentException("actor cannot be null.");
        if (actor != this.actor) return false;
        setActor(null);
        return true;
    }
    
    public boolean removeActor (Actor actor, boolean unfocus) {
        if (actor == null) throw new IllegalArgumentException("actor cannot be null.");
        if (actor != this.actor) return false;
        this.actor = null;
        return super.removeActor(actor, unfocus);
    }
    
    public Actor removeActorAt (int index, boolean unfocus) {
        Actor actor = super.removeActorAt(index, unfocus);
        if (actor == this.actor) this.actor = null;
        return actor;
    }
    
    public float getRatio() {
        return ratio;
    }
    
    public void setRatio(float ratio) {
        this.ratio = ratio;
        invalidate();
    }
    
    public void setRatio(float width, float height) {
        this.ratio = width / height;
        invalidate();
    }
    
    public boolean isBindToWidth() {
        return bindToWidth;
    }
    
    public void setBindToWidth(boolean bindToWidth) {
        this.bindToWidth = bindToWidth;
        invalidate();
    }
}
