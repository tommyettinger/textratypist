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

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntFloatMap;
import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.TypingLabel;
import com.github.tommyettinger.textra.utils.ColorUtils;

/**
 * Fades the text's color from between colors or alphas. Doesn't repeat itself.
 * This can either fade between two different colors, or two different alpha levels for the default color. The alpha
 * levels can be useful to make text go from invisible (alpha 0.0) to visible (alpha 1.0) instead of changing color.
 * <br>
 * Parameters: {@code color1;color2;duration}
 * <br>
 * The {@code color1} can be a named color or hex color, but if it isn't valid as one of those, it will be parsed as a
 * float and treated as an alpha transparency value instead of a color, between 0.0 and 1.0.
 * The {@code color2} can be a named color or hex color, but if it isn't valid as one of those, it will be parsed as a
 * float and treated as an alpha transparency value instead of a color, between 0.0 and 1.0.
 * The {@code duration} is how many seconds the fade should take to go from color1 to color2; defaults to 1.0 .
 * <br>
 * Example usage:
 * <code>
 * {FADE=RED;LIGHT BLUE;2.0}This text will fade from red to light blue over 2 seconds.{ENDFADE}
 * {FADE=0.0;1.0;0.5}This text will go from invisible to the default color in half a second.{ENDFADE}
 * </code>
 */
public class FadeEffect extends Effect {
    private int color1 = 256; // First color of the effect.
    private int color2 = 256; // Second color of the effect.
    private float alpha1 = 0; // First alpha of the effect, in case a color isn't provided.
    private float alpha2 = 1; // Second alpha of the effect, in case a color isn't provided.
    private float fadeDuration = 1; // Duration of the fade effect

    private final IntFloatMap timePassedByGlyphIndex = new IntFloatMap();

    public FadeEffect(TypingLabel label, String[] params) {
        super(label);

        // Color 1 or Alpha 1
        if (params.length > 0) {
            this.color1 = paramAsColor(params[0]);
            if (this.color1 == 256) {
                alpha1 = paramAsFloat(params[0], 0);
            }
        }

        // Color 2 or Alpha 2
        if (params.length > 1) {
            this.color2 = paramAsColor(params[1]);
            if (this.color2 == 256) {
                alpha2 = paramAsFloat(params[1], 1);
            }
        }

        // Fade duration
        if (params.length > 2) {
            this.fadeDuration = paramAsFloat(params[2], 1);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate progress
        float timePassed = timePassedByGlyphIndex.getAndIncrement(localIndex, 0, delta);
        float progress = MathUtils.clamp(timePassed / fadeDuration, 0, 1);

        // Calculate initial color
        if (this.color1 == 256) {
            label.setInWorkingLayout(globalIndex,
                    glyph = (glyph & 0xFFFFFF00FFFFFFFFL) | (long) MathUtils.lerp(glyph >>> 32 & 255, this.alpha1 * 255, 1f - progress) << 32);
        } else {
            label.setInWorkingLayout(globalIndex,
                    glyph = (glyph & 0xFFFFFFFFL) | (long) ColorUtils.lerpColors((int) (glyph >>> 32), this.color1, 1f - progress) << 32);
        }

        // Calculate final color
        if (this.color2 == 256) {
            label.setInWorkingLayout(globalIndex,
                    (glyph & 0xFFFFFF00FFFFFFFFL) | (long) MathUtils.lerp(glyph >>> 32 & 255, this.alpha2 * 255, progress) << 32);
        } else {
            label.setInWorkingLayout(globalIndex,
                    (glyph & 0xFFFFFFFFL) | (long) ColorUtils.lerpColors((int) (glyph >>> 32), this.color2, progress) << 32);
        }
    }

}
