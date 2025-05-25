package com.ray3k.tgmt.gwt;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.badlogic.gdx.graphics.g2d.freetype.gwt.FreetypeInjector;
import com.ray3k.tgmt.Core;

/** Launches the GWT application. */
public class GwtLauncher extends GwtApplication {
	@Override
	public GwtApplicationConfiguration getConfig() {

		GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(true);
		cfg.padVertical = 0;
		cfg.padHorizontal = 0;
		return cfg;
	}
	
	@Override
	public ApplicationListener createApplicationListener() {
		return new Core();
	}
	
	@Override
	public void onModuleLoad() {
		FreetypeInjector.inject(GwtLauncher.super::onModuleLoad);
	}
}
