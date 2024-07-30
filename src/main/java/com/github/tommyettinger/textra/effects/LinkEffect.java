/*
 * Copyright (c) 2021-2023 See AUTHORS file.
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

package com.github.tommyettinger.textra.effects;

import com.badlogic.gdx.Gdx;
import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.TypingLabel;
import com.github.tommyettinger.textra.utils.StringUtils;

/**
 * Allows clicking the affected text to open a URL in the browser. You may want to use other markup with this, such as
 * underlining or especially a color change; using {@link StylistEffect} may be useful to indicate that the user is
 * currently hovering over a link. Doesn't change over time. This doesn't validate the URL or sanity-check it
 * in any way, so try not to allow users to write arbitrary URLS and send them to other users.
 * <br>
 * Parameters: {@code url}
 * <br>
 * The {@code url} can be any URL text not containing curly braces or brackets, and can actually have semicolons in it
 * and still be treated as one parameter. This URL is what clicking the affected text will try to open in a browser.
 * <br>
 * Example usage:
 * <code>
 * {LINK=https://libgdx.com}Everybody's favorite Java game framework!{ENDLINK}
 * </code>
 */
public class LinkEffect extends Effect {
    private String link = "https://libgdx.com";

    public LinkEffect(TypingLabel label, String[] params) {
        super(label);
        label.trackingInput = true;

        // URL
        if (params.length > 0) {
            this.link = StringUtils.join(";", params);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        if(label.lastTouchedIndex == globalIndex){
            label.lastTouchedIndex = -1;
            Gdx.net.openURI(link);
        }
    }

}
