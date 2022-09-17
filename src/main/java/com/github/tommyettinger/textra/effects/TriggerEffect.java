/*
 * Copyright (c) 2021-2022 See AUTHORS file.
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
 * Allows clicking the affected text to trigger an event. You may want to use other markup with this, such as
 * underlining or especially a color change. Doesn't change over time. This is fully dependent on what the TypingLabel's
 * TypingListener/TypingAdapter does with the event by name.
 */
public class TriggerEffect extends Effect {
    private String event = "start";

    public TriggerEffect(TypingLabel label, String[] params) {
        super(label);
        label.trackingInput = true;

        // URL
        if (params.length > 0) {
            this.event = StringUtils.join(";", params);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        if(label.lastTouchedIndex == globalIndex){
            label.lastTouchedIndex = -1;
            label.triggerEvent(event, true);
        }
    }

}
