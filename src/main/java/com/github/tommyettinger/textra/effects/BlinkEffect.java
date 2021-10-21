
package com.github.tommyettinger.textra.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.TypingLabel;

/** Blinks the entire text in two different colors at once, without interpolation. */
public class BlinkEffect extends Effect {
    private static final float DEFAULT_FREQUENCY = 1f;

    private int color1      = 0xFFFFFFFF; // First color of the effect, RGBA8888.
    private int color2      = 0xFFFFFFFF; // Second color of the effect, RGBA8888.
    private float frequency = 1; // How frequently the color pattern should move through the text.
    private float threshold = 0.5f; // Point to switch colors.

    public BlinkEffect(TypingLabel label, String[] params) {
        super(label);

        // Color 1
        if(params.length > 0) {
            Integer c = paramAsColor(params[0]);
            if(c != null) this.color1 = c;
        }

        // Color 2
        if(params.length > 1) {
            Integer c = paramAsColor(params[1]);
            if(c != null) this.color2 = c;
        }

        // Frequency
        if(params.length > 2) {
            this.frequency = paramAsFloat(params[2], 1);
        }

        // Threshold
        if(params.length > 3) {
            this.threshold = paramAsFloat(params[3], 0.5f);
        }

        // Validate parameters
        this.threshold = MathUtils.clamp(this.threshold, 0, 1);
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate progress
        float frequencyMod = (1f / frequency) * DEFAULT_FREQUENCY;
        float progress = calculateProgress(frequencyMod);

        // Calculate and assign color
        label.setInLayouts(globalIndex, (glyph & 0xFFFFFFFFL) | (long) (progress <= threshold ? color1 : color2) << 32);
    }

}
