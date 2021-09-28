
package com.github.tommyettinger.textra.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.TypingLabel;

/** Blinks the entire text in two different colors at once, without interpolation. */
public class BlinkEffect extends Effect {
    private static final float DEFAULT_FREQUENCY = 1f;

    private Color color1    = null; // First color of the effect.
    private Color color2    = null; // Second color of the effect.
    private float frequency = 1; // How frequently the color pattern should move through the text.
    private float threshold = 0.5f; // Point to switch colors.

    public BlinkEffect(TypingLabel label, String[] params) {
        super(label);

        // Color 1
        if(params.length > 0) {
            this.color1 = paramAsColor(params[0]);
        }

        // Color 2
        if(params.length > 1) {
            this.color2 = paramAsColor(params[1]);
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
        if(this.color1 == null) this.color1 = new Color(Color.WHITE);
        if(this.color2 == null) this.color2 = new Color(Color.WHITE);
        this.threshold = MathUtils.clamp(this.threshold, 0, 1);
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        // Calculate progress
        float frequencyMod = (1f / frequency) * DEFAULT_FREQUENCY;
        float progress = calculateProgress(frequencyMod);

        // Calculate and assign color
        label.setInLayouts(globalIndex, (glyph & 0xFFFFFFFFL) | (long) Color.rgba8888(progress <= threshold ? color1 : color2) << 32);
    }

}
