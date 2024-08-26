package com.ray3k.expee;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class LoadingActor extends Image {
    private Animation<TextureRegion> animation;
    private float stateTime;

    public LoadingActor(Skin skin) {
        animation = new Animation<>(.1f, skin.getRegions("loading-bar"));
        stateTime = 0;
        setDrawable(new TextureRegionDrawable(animation.getKeyFrame(0)));
    }

    @Override
    public void act(float delta) {
        stateTime += delta;
        stateTime %= animation.getAnimationDuration();
        ((TextureRegionDrawable)getDrawable()).setRegion(animation.getKeyFrame(stateTime, true));
        super.act(delta);
    }
}
