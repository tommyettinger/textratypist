package com.github.tommyettinger.textra;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;

public class TextraArea2 extends TextraField {

    public TextraArea2(@Null String text, Skin skin) {
        this(text, skin.get(Styles.TextFieldStyle.class));
    }

    public TextraArea2(@Null String text, Skin skin, Font replacementFont) {
        this(text, skin.get(Styles.TextFieldStyle.class), replacementFont);
    }

    public TextraArea2(@Null String text, Skin skin, String styleName) {
        this(text, skin.get(styleName, Styles.TextFieldStyle.class));
    }

    public TextraArea2(String text, Styles.TextFieldStyle style) {
        super(text, style);
        writeEnters = true;
        label.setWrap(true);
        label.workingLayout.targetWidth = 1f;
        label.setMaxLines(Integer.MAX_VALUE);
        label.setAlignment(Align.bottomLeft);
    }

    public TextraArea2(String text, Styles.TextFieldStyle style, Font replacementFont) {
        super(text, style, replacementFont);
        writeEnters = true;
        label.setWrap(true);
        label.workingLayout.targetWidth = 1f;
        label.setMaxLines(Integer.MAX_VALUE);
        label.setAlignment(Align.bottomLeft);
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        label.setSize(getWidth(), getHeight());
    }

    @Override
    protected void drawCursor (Drawable cursorPatch, Batch batch, Font font, float x, float y) {
        final float layoutHeight = label.workingLayout.getHeight(), linesHeight = label.getCumulativeLineHeight(cursor);
//		System.out.println("layoutHeight: " + layoutHeight + ", linesHeight: " + linesHeight);
        cursorPatch.draw(batch,
                x + textOffset + glyphPositions.get(cursor) - glyphPositions.get(visibleTextStart) + fontOffset,
                y + layoutHeight - linesHeight, cursorPatch.getMinWidth(), font.cellHeight);
    }

}
