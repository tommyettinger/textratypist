package com.uhh;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.github.tommyettinger.textra.Styles.LabelStyle;
import com.github.tommyettinger.textra.TypingLabel;

import java.util.function.Supplier;

public class AdaptiveLabel extends TypingLabel {

	private final LabelStyle typingLabelStyle;

	protected Supplier<String> textSupplier;

	public AdaptiveLabel(Supplier<String> textSupplier, LabelStyle typingLabelStyle) {
		super(textSupplier.get(), typingLabelStyle);
		this.typingLabelStyle = typingLabelStyle;
		this.textSupplier = textSupplier;
		setWrap(true);
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		resizeUI();
		super.draw(batch, parentAlpha);
	}

	private void resizeUI() {
		setFont(typingLabelStyle.font); // updates us to the resized font size
		setText(textSupplier.get(), true, false);
		skipToTheEnd();
//		invalidateHierarchy();
	}

    /// ///////////////////////////////
    /// --- Core Functionality --- ///
    /// ///////////////////////////////

    @Override
    public void act(float delta) {
        super.act(delta);
    }
}
