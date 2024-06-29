/*
 * Copyright (c) 2024 See AUTHORS file.
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

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * An {@link com.badlogic.gdx.assets.loaders.AssetLoader} to load a {@link FWSkin}. This enables you to deserialize
 * FreeType fonts from a Skin JSON and load it through an {@link AssetManager}. Example code:
 * <code>
 *     AssetManager assetManager = new AssetManager();
 *     assetManager.setLoader(Skin.class, new FWSkinLoader(assetManager.getFileHandleResolver()));
 *     assetManager.load("skin-name.json", Skin.class);
 * </code>
 */
public class FWSkinLoader extends SkinLoader {
    /**
     * Creates a loader with the associated resolver.
     * @param resolver Allows {@link AssetManager} to load resources from anywhere or implement caching strategies.
     */
    public FWSkinLoader(FileHandleResolver resolver) {
        super(resolver);
    }
    
    /** Override to allow subclasses of Skin to be loaded or the skin instance to be configured.
     * @param atlas The TextureAtlas that the skin will use.
     * @return A new Skin (or subclass of Skin) instance based on the provided TextureAtlas. */
    protected FWSkin newSkin (TextureAtlas atlas) {
        return new FWSkin(atlas);
    }
}
