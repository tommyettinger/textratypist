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

package com.github.tommyettinger.textra;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.LongArray;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.github.tommyettinger.textra.utils.ColorUtils;

import java.util.ArrayList;
import java.util.Map;

/**
 * An extension of {@link TextraLabel} that progressively shows the text as if it was being typed in real time, and
 * allows the use of tokens in the format: <code>{TOKEN=PARAMETER;ANOTHER_PARAMETER;MORE}</code>. These tokens can
 * add various effects to spans of text, such as the token {@code WIND} making text flutter and flap around, or
 * {@code BLINK} making it flash an alternate color repeatedly. These work in addition to the tags permitted by
 * TextraLabel, such as <code>[light blue]</code> for to change text color, or <code>[_]</code> to underline text.
 * For compatibility with other systems that may already use curly braces, such as some I18N techniques, you can use
 * <code>[-</code> instead of <code>{</code> and <code>]</code> instead of <code>}</code> to use tokens without writing
 * out curly braces.
 * <br>
 * This is meant to work with {@link FWSkin} or one of its subclasses, such as {@code FreeTypistSkin}, and isn't
 * guaranteed to work with a regular {@link Skin}. FWSkin can load the same JSON files Skin uses, and it extends Skin.
 * If you encounter an unusually high amount of native memory being used, the cause is most likely Font objects being
 * created from BitmapFont objects repeatedly, <em>which FWSkin is designed to avoid</em>. When using any scene2d.ui
 * widget from TextraTypist with an FWSkin, the correct and optimal style from {@link Styles} is used, and that avoids
 * creating more and more Font objects as widgets are created and destroyed. Subclasses of FWSkin are also perfectly
 * fine to use, such as the subclass in <a href="https://github.com/tommyettinger/freetypist">FreeTypist</a> that allows
 * using FreeType to generate a Font from skin JSON configuration.
 * <em>Using a regular libGDX Skin object, not an FWSkin, will be a problem.</em>
 */
public class TypingLabel extends TextraLabel {
    ///////////////////////
    /// --- Members --- ///
    ///////////////////////

    // Collections
    private final ObjectMap<String, String> variables = new ObjectMap<>();
    final ArrayList<TokenEntry> tokenEntries = new ArrayList<>();

    // Config
    private final Color clearColor = new Color(TypingConfig.DEFAULT_CLEAR_COLOR);
    private TypingListener listener = null;

    // Internal state
    private final StringBuilder originalText = new StringBuilder();
    private final StringBuilder intermediateText = new StringBuilder();
    protected final Layout workingLayout = new Layout();

    protected Justify defaultJustify = Justify.NONE;

    /**
     * If true, this will attempt to track which glyph the user's mouse or other pointer is over (see {@link #overIndex}
     * and {@link #lastTouchedIndex}).
     */
    public boolean trackingInput = false;

    /**
     * If true, this label will allow clicking and dragging to select a range of text, if {@link #trackingInput} is also
     * true. This does not allow the text to be edited unless so implemented by another class. If text can be selected,
     * then you can use {@link #getSelectedText()} to get the selected String, or {@link #copySelectedText()} to copy
     * that text directly. To copy automatically, use a listener that checks {@link TypingListener#event(String)}, and
     * when the event String is {@code "*SELECTED"}, that means a click-and-drag selected a range of text in this label,
     * and you can do what you want with the selected text (such as call {@link #copySelectedText()}).
     */
    public boolean selectable = false;

    public Drawable selectionDrawable = null;
    /**
     * The global glyph index (as used by {@link #setInWorkingLayout(int, long)}) of the last glyph touched by the user.
     * If nothing in this TypingLabel was touched during the last call to {@link #draw(Batch, float)}, then this will be
     * either -1 (if the last touch was, roughly, before the first glyph) or -2 (if the last touch was after the last
     * glyph). This only changes when a click, tap, or other touch was just issued.
     */
    public int lastTouchedIndex = -1;
    /**
     * The global glyph index (as used by {@link #setInWorkingLayout(int, long)}) of the last glyph hovered or dragged
     * over by the user (including a click and mouse movement without a click). If nothing in this TypingLabel was moved
     * over during the last call to {@link #draw(Batch, float)}, then this will be -1 . This changes whenever the mouse
     * or a pointer is over a glyph in this.
     */
    public int overIndex = -1;
    /**
     * The inclusive start index for the selected text, if there is a selection. This should be -1 if there is no
     * selection, or sometimes -2 if the selection went past the end of the text. This is essentially interchangeable
     * with {@link #selectionEnd}; as long as they are different, it doesn't matter which is higher or lower.
     */
    public int selectionStart = -1;
    /**
     * The inclusive end index for the selected text, if there is a selection. This should be -1 if there is no
     * selection, or sometimes -2 if the selection went past the end of the text. This is essentially interchangeable
     * with {@link #selectionStart}; as long as they are different, it doesn't matter which is higher or lower.
     */
    public int selectionEnd = -1;

    private final Vector2 temp = new Vector2(0f, 0f);

    protected boolean dragging = false;
    protected final ArrayList<Effect> activeEffects = new ArrayList<>();
    private float textSpeed = TypingConfig.DEFAULT_SPEED_PER_CHAR;
    private float charCooldown = textSpeed;
    private int rawCharIndex = -2; // All chars, including color codes
    private int glyphCharIndex = -1; // Only renderable chars, excludes color codes
    private int glyphCharCompensation = 0;
    private boolean parsed = false;
    private boolean paused = false;
    private boolean ended = false;
    private boolean skipping = false;
    private boolean ignoringEvents = false;
    private boolean ignoringEffects = false;
    private boolean onStage = false;
    private String defaultToken = "";

    ////////////////////////////
    /// --- Constructors --- ///
    ////////////////////////////

    /**
     * Creates a TypingLabel that uses the libGDX default font (lsans-15) and starts with no text.
     * The default font will not look very good when scaled, so this should usually stay its default font size.
     * This allocates a new Font every time it is called, so you should avoid this constructor in code that is called
     * more than a handful of times. Its only valid use is in debugging.
     */
    public TypingLabel() {
        super();
        workingLayout.font(super.font);
        setText("", true);
    }

    /**
     * The skin should almost certainly be an {@link FWSkin} or one of its subclasses.
     * Do not use scene2d.ui's {@link Skin} unless you are prepared to dispose of Fonts manually, without any help.
     * @param text markup text that can contain square-bracket tags and curly-brace tokens
     * @param skin almost always an {@link FWSkin} or one of its subclasses; must have a
     *             {@link Styles.LabelStyle} or {@link com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle} registered as "default"
     */
    public TypingLabel(String text, Skin skin) {
        this(text, skin.get(Styles.LabelStyle.class));
    }

    /**
     * The skin should almost certainly be an {@link FWSkin} or one of its subclasses.
     * Do not use scene2d.ui's {@link Skin} unless you are prepared to dispose of Fonts manually, without any help.
     * @param text markup text that can contain square-bracket tags and curly-brace tokens
     * @param skin almost always an {@link FWSkin} or one of its subclasses; must have a
     *             {@link Styles.LabelStyle} or {@link com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle} registered as "default"
     * @param replacementFont will be used instead of the Font loaded from skin
     */
    public TypingLabel(String text, Skin skin, Font replacementFont) {
        this(text, skin.get(Styles.LabelStyle.class), replacementFont);
    }

    /**
     * The skin should almost certainly be an {@link FWSkin} or one of its subclasses.
     * Do not use scene2d.ui's {@link Skin} unless you are prepared to dispose of Fonts manually, without any help.
     * @param text markup text that can contain square-bracket tags and curly-brace tokens
     * @param skin almost always an {@link FWSkin} or one of its subclasses; must have a
     *             {@link Styles.LabelStyle} or {@link com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle} registered with the given styleName
     * @param styleName the name of the {@link Styles.LabelStyle} to load from skin
     */
    public TypingLabel(String text, Skin skin, String styleName) {
        this(text, skin.get(styleName, Styles.LabelStyle.class));
    }

    /**
     * The skin should almost certainly be an {@link FWSkin} or one of its subclasses.
     * Do not use scene2d.ui's {@link Skin} unless you are prepared to dispose of Fonts manually, without any help.
     * @param text markup text that can contain square-bracket tags and curly-brace tokens
     * @param skin almost always an {@link FWSkin} or one of its subclasses; must have a
     *             {@link Styles.LabelStyle} or {@link com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle} registered with the given styleName
     * @param styleName the name of the {@link Styles.LabelStyle} to load from skin
     * @param replacementFont will be used instead of the Font loaded from skin
     */
    public TypingLabel(String text, Skin skin, String styleName, Font replacementFont) {
        this(text, skin.get(styleName, Styles.LabelStyle.class), replacementFont);
    }

    /**
     * Creates a TypingLabel with the given markup text and style, without needing a skin.
     * Note that you can obtain widget styles in {@link Styles} either by manually constructing the appropriate style,
     * or by loading a skin JSON file with {@link FWSkin} or one of its subclasses.
     * @param text markup text that can contain square-bracket tags and curly-brace tokens
     * @param style a style from {@link Styles} and not from scene2d.ui; often made manually
     */
    public TypingLabel(String text, Styles.LabelStyle style) {
        super(text = Parser.handleBracketMinusMarkup(text), style);
        workingLayout.font(super.font);
        workingLayout.setBaseColor(layout.baseColor);
        Color.abgr8888ToColor(clearColor, layout.getBaseColor());
        setText(text, true);
    }

    /**
     * Creates a TypingLabel with the given markup text, style, and Font, without needing a skin.
     * Note that you can obtain widget styles in {@link Styles} either by manually constructing the appropriate style,
     * or by loading a skin JSON file with {@link FWSkin} or one of its subclasses.
     * @param text markup text that can contain square-bracket tags and curly-brace tokens
     * @param style a style from {@link Styles} and not from scene2d.ui; often made manually
     * @param replacementFont will be used instead of the Font from the style
     */
    public TypingLabel(String text, Styles.LabelStyle style, Font replacementFont) {
        super(text = Parser.handleBracketMinusMarkup(text), style, replacementFont);
        workingLayout.font(super.font);
        workingLayout.setBaseColor(layout.baseColor);
        Color.abgr8888ToColor(clearColor, layout.getBaseColor());
        setText(text, true);
    }

    /**
     * Creates a TypingLabel with the given markup text and Font, without needing a skin.
     * @param text markup text that can contain square-bracket tags and curly-brace tokens
     * @param font will be used for all text
     */
    public TypingLabel(String text, Font font) {
        super(text = Parser.handleBracketMinusMarkup(text), font);
        workingLayout.font(font);
        setText(text, true);
    }

    /**
     * Creates a TypingLabel with the given markup text, Font, and font color, without needing a skin.
     * @param text markup text that can contain square-bracket tags and curly-brace tokens
     * @param font will be used for all text
     * @param color the default foreground color for text
     */
    public TypingLabel(String text, Font font, Color color) {
        super(text = Parser.handleBracketMinusMarkup(text), font, color);
        workingLayout.font(font);
        workingLayout.setBaseColor(layout.baseColor);
        Color.abgr8888ToColor(clearColor, layout.getBaseColor());
        setText(text, true);
    }

    /////////////////////////////
    /// --- Text Handling --- ///
    /////////////////////////////

    /**
     * Modifies the text of this label. If the char progression is already running, it's highly recommended to use
     * {@link #restart(CharSequence)} instead.
     * @param newText what to use as the new text (and original text) of this label
     */
    @Override
    public void setText(String newText) {
        this.setText(newText, true);
    }

    /**
     * Sets the text of this label. If the char progression is already running, it's highly recommended to use
     * {@link #restart(CharSequence)} instead. This overload allows specifying if the original text, which is used when
     * parsing the tokens (with {@link #parseTokens()}), should be changed to match the given text. If
     * {@code modifyOriginalText} is true, this will {@link Parser#preprocess(String) preprocess} the text, which
     * should generally be run once per original text and no more.
     * <br>
     * This overload calls {@link #setText(String, boolean, boolean)} with {@code restart} set to false.
     *
     * @param modifyOriginalText Flag determining if the original text should be modified as well. If {@code false},
     *                           only the display text is changed while the original text is untouched. If {@code true},
     *                           then this runs {@link Parser#preprocess(String)} on the text, which should only
     *                           generally be run once per original text.
     * @see #restart(CharSequence)
     */
    public void setText(String newText, boolean modifyOriginalText) {
        if (modifyOriginalText) {
            if(font.omitCurlyBraces)
                newText = Parser.preprocess("{NORMAL}" + getDefaultToken() + newText);
            else if(font.enableSquareBrackets)
                newText = Parser.preprocess(getDefaultToken() + newText);
            else
                newText = getDefaultToken() + newText;
        }
        setText(newText, modifyOriginalText, true);
    }

    /**
     * Sets the text of this label. If the char progression is already running, it's highly recommended to use
     * {@link #restart(CharSequence)} instead. This overload allows specifying if the original text, which is used when
     * parsing the tokens (with {@link #parseTokens()}), should be changed to match the given text. This will not ever
     * call {@link Parser#preprocess(String)}, which makes it different from {@link #setText(String, boolean)}.
     * You can also specify whether the text animation should restart or not here.
     *
     * @param modifyOriginalText Flag determining if the original text should be modified as well. If {@code false},
     *                           only the display text is changed while the original text is untouched.
     * @param restart            Whether this label should restart. Defaults to true.
     * @see #restart(CharSequence)
     */
    public void setText(String newText, boolean modifyOriginalText, boolean restart) {
        final boolean hasEnded = this.hasEnded();
        newText = Parser.handleBracketMinusMarkup(newText);
        font.markup(newText, layout.clear().setJustification(defaultJustify));

//        int glyphCount = layout.countGlyphs();
//        layout.offsets.setSize(glyphCount + glyphCount);
//        Arrays.fill(layout.offsets.items, 0, glyphCount + glyphCount, 0f);
//        layout.sizing.setSize(glyphCount + glyphCount);
//        Arrays.fill(layout.sizing.items, 0, glyphCount + glyphCount, 1f);
//        layout.rotations.setSize(glyphCount);
//        Arrays.fill(layout.rotations.items, 0, glyphCount, 0f);
//        layout.advances.setSize(glyphCount);
//        Arrays.fill(layout.advances.items, 0, glyphCount, 1f);

        if (wrap) {
            workingLayout.setTargetWidth(getWidth());
            font.markup(newText, workingLayout.clear().setJustification(defaultJustify));
        } else {
            workingLayout.setTargetWidth(0f);
            font.markup(newText, workingLayout.clear().setJustification(defaultJustify));
            setSuperWidth(workingLayout.getWidth() + (style != null && style.background != null ?
                    style.background.getLeftWidth() + style.background.getRightWidth() : 0.0f));
        }
        if (modifyOriginalText) saveOriginalText(newText);

//        glyphCount = workingLayout.countGlyphs();
//        getOffsets().setSize(glyphCount + glyphCount);
//        Arrays.fill(getOffsets().items, 0, glyphCount + glyphCount, 0f);
//        getSizing().setSize(glyphCount + glyphCount);
//        Arrays.fill(getSizing().items, 0, glyphCount + glyphCount, 1f);
//        getRotations().setSize(glyphCount);
//        Arrays.fill(getRotations().items, 0, glyphCount, 0f);
//        getAdvances().setSize(glyphCount);
//        Arrays.fill(getAdvances().items, 0, glyphCount, 1f);

        if (restart) {
            this.restart();
        }
        if (hasEnded) {
            this.skipToTheEnd(true, false);
        }
    }

    /**
     * Similar to {@link Layout#toString()}, but returns the original text with all the tokens unchanged.
     */
    public StringBuilder getOriginalText() {
        return originalText;
    }

    /**
     * Copies the content of {@link #getOriginalText()} to the {@link StringBuilder} containing the original
     * text with all tokens unchanged.
     */
    protected void saveOriginalText(CharSequence text) {
        if (text != originalText) {
            originalText.setLength(0);
            originalText.append(text);
        }
//        originalText.trimToSize();
    }

    /**
     * Restores the original text with all tokens unchanged to this label. Make sure to call {@link #parseTokens()} to
     * parse the tokens again.
     */
    protected void restoreOriginalText() {
        super.setText(originalText.toString());
        this.parsed = false;
    }

    ////////////////////////////
    /// --- External API --- ///
    ////////////////////////////

    /**
     * Returns the {@link TypingListener} associated with this label. May be {@code null}.
     */
    public TypingListener getTypingListener() {
        return listener;
    }

    /**
     * Sets the {@link TypingListener} associated with this label, or {@code null} to remove the current one.
     */
    public void setTypingListener(TypingListener listener) {
        this.listener = listener;
    }

    /**
     * Returns a {@link Color} instance with the color to be used on {@code CLEARCOLOR} tokens. Modify this instance to
     * change the token color. Default value is specified by {@link TypingConfig}.
     *
     * @see TypingConfig#DEFAULT_CLEAR_COLOR
     */
    public Color getClearColor() {
        return clearColor;
    }

    /**
     * Returns the default token being used in this label. Defaults to empty string.
     */
    public String getDefaultToken() {
        return defaultToken;
    }

    /**
     * Sets the default token being used in this label. This token will be used before the label's text, and after each
     * {RESET} call. Useful if you want a certain token to be active at all times without having to type it all the
     * time.
     */
    public void setDefaultToken(String defaultToken) {
        this.defaultToken = defaultToken == null ? "" : defaultToken;
        this.parsed = false;
    }

    /**
     * Parses all tokens of this label. Use this after setting the text and any variables that should be replaced.
     */
    public void parseTokens() {
        parsed = true;
        boolean actualEnd = ended;
        ended = false;
        if(font.omitCurlyBraces)
            this.setText(Parser.preprocess("{NORMAL}"+getDefaultToken() + originalText), false, false);
        else if(font.enableSquareBrackets)
            this.setText(Parser.preprocess(getDefaultToken() + originalText), false, false);
        else
            this.setText(getDefaultToken() + originalText, false, false);
        Parser.parseTokens(this);
        ended = actualEnd;
    }

    /**
     * Skips the char progression to the end, showing the entire label. Useful for when users don't want to wait for too
     * long. Ignores all subsequent events by default. Doesn't change running effects.
     * @return this, for chaining
     */
    @Override
    public TypingLabel skipToTheEnd() {
        return skipToTheEnd(true);
    }

    /**
     * Skips the char progression to the end, showing the entire label. Useful for when users don't want to wait for too
     * long. This doesn't change running effects.
     *
     * @param ignoreEvents If {@code true}, skipped events won't be reported to the listener.
     * @return this, for chaining
     */
    public TypingLabel skipToTheEnd(boolean ignoreEvents) {
        return skipToTheEnd(ignoreEvents, false);
    }

    /**
     * Skips the char progression to the end, showing the entire label. Useful for when users don't want to wait for too
     * long.
     *
     * @param ignoreEvents  If {@code true}, skipped events won't be reported to the listener.
     * @param ignoreEffects If {@code true}, all text effects will be instantly cancelled.
     * @return this, for chaining
     */
    public TypingLabel skipToTheEnd(boolean ignoreEvents, boolean ignoreEffects) {
        skipping = true;
        ignoringEvents = ignoreEvents;
        ignoringEffects = ignoreEffects;
        subAct(0f);
        return this;
    }

    /**
     * Cancels calls to {@link #skipToTheEnd()}. Useful if you need to restore the label's normal behavior at some event
     * after skipping.
     */
    public void cancelSkipping() {
        if (skipping) {
            skipping = false;
            ignoringEvents = false;
            ignoringEffects = false;
        }
    }

    /**
     * Returns whether this label is currently skipping its typing progression all the way to the end. This is
     * only true if skipToTheEnd is called.
     */
    public boolean isSkipping() {
        return skipping;
    }

    /**
     * Returns whether this label is paused.
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Pauses this label's character progression.
     */
    public void pause() {
        paused = true;
    }

    /**
     * Resumes this label's character progression.
     */
    public void resume() {
        paused = false;
    }

    /**
     * Returns whether this label's char progression has ended.
     */
    public boolean hasEnded() {
        return ended;
    }

    /**
     * Restarts this label with the original text and starts the char progression right away. All tokens are
     * automatically parsed.
     */
    public void restart() {
        restart(getOriginalText());
    }

    /**
     * Restarts this label with the given text and starts the char progression right away. All tokens are automatically
     * parsed. If you are reusing an existing TypingLabel and its size will change once it holds {@code newText}, you
     * may need to call {@code label.setSize(0, 0);} before calling this. This does not change its size by itself,
     * because restarting is also performed internally and changing the size internally could cause unexpected (read:
     * very buggy) behavior for code using this library.
     * @param newText the String, StringBuilder, or other CharSequence that this TypingLabel will start displaying
     */
    public void restart(CharSequence newText) {
        workingLayout.atLimit = false;

        // Reset cache collections
        Line first = workingLayout.lines.first();
        first.glyphs.clear();
        first.width = first.height = 0;
        workingLayout.lines.clear();
        workingLayout.lines.add(first);
        activeEffects.clear();

        // Reset state
        textSpeed = TypingConfig.DEFAULT_SPEED_PER_CHAR;
        charCooldown = textSpeed;
        rawCharIndex = -2;
        glyphCharIndex = -1;
        glyphCharCompensation = 0;
        parsed = false;
        paused = false;
        ended = false;
        skipping = false;
        ignoringEvents = false;
        ignoringEffects = false;

        // Set new text
        saveOriginalText(newText);
        invalidate();

        // Parse tokens
        parseTokens();
    }


    /**
     * Continues this label's typing effect after it has previously ended, appending the given text and starting the
     * char progression (again) right away. All tokens are automatically parsed. If this TypingLabel has not yet ended
     * when this is called, then this calls {@code skipToTheEnd(false, false)}.
     * @param newText a String, StringBuilder, or other CharSequence that will be appended to the finished text
     */
    public void appendText(CharSequence newText) {
        if(newText == null || newText.length() == 0) return;

        if(!ended) skipToTheEnd(false, false);

        workingLayout.atLimit = false;

//        // Reset cache collections
//        Line first = workingLayout.lines.first();
//        first.glyphs.clear();
//        first.width = first.height = 0;
//        workingLayout.lines.clear();
//        workingLayout.lines.add(first);
//        activeEffects.clear();

        // Reset state
//        textSpeed = TypingConfig.DEFAULT_SPEED_PER_CHAR;
        charCooldown = textSpeed;
//        rawCharIndex = -2;
//        glyphCharIndex = -1;
//        glyphCharCompensation = 0;
        parsed = false;
        paused = false;
        ended = false;
        skipping = false;
        ignoringEvents = false;
        ignoringEffects = false;

        // Set new text
        invalidate();
        saveOriginalText(originalText.append(newText));

        // Parse tokens
        parseTokens();
    }

    /**
     * Returns the {@link ObjectMap} with all the variable names and their respective replacement values
     * that this label uses to handle <code>{VAR=NAME}</code> replacements. This returns the map directly.
     */
    public ObjectMap<String, String> getVariables() {
        return variables;
    }

    /**
     * Registers a variable and its respective replacement value to this label.
     * @param var   the String name to use for a variable
     * @param value the String value to use as a replacement
     */
    public void setVariable(String var, String value) {
        if(var != null) {
            String old = variables.put(var.toUpperCase(), value);
            if (value.contains("[") || value.contains("{") || (old != null && (old.contains("[") || old.contains("{")))) {
                parsed = false;
            }
        }
    }

    /**
     * Removes a variable and its respective replacement value from this label's variable map.
     * @param var the String name of a variable to remove
     */
    public void removeVariable(String var) {
        if(var != null) {
            String old = variables.remove(var.toUpperCase());
            if(old != null && (old.contains("[") || old.contains("{"))){
                parsed = false;
            }
        }
    }

    /**
     * Registers a set of variables and their respective replacement values to this label.
     * @param variableMap an ObjectMap of variable names to their replacement Strings
     */
    public void setVariables(ObjectMap<String, String> variableMap) {
        this.variables.clear();
        if (variableMap != null) {
            for (Entry<String, String> entry : variableMap.entries()) {
                String value = entry.value;
                String old = this.variables.put(entry.key.toUpperCase(), value);
                if(value.contains("[") || value.contains("{") || (old != null && (old.contains("[") || old.contains("{")))) {
                    parsed = false;
                }
            }
        }
    }

    /**
     * Registers a set of variables and their respective replacement values to this label.
     * @param variableMap a Map of variable names to their replacement Strings; null keys will be ignored silently
     */
    public void setVariables(Map<String, String> variableMap) {
        this.variables.clear();
        if (variableMap != null) {
            for (Map.Entry<String, String> entry : variableMap.entrySet()) {
                if (entry.getKey() != null) {
                    String value = entry.getValue();
                    String old = this.variables.put(entry.getKey().toUpperCase(), value);
                    if(value.contains("[") || value.contains("{") || (old != null && (old.contains("[") || old.contains("{")))) {
                        parsed = false;
                    }
                }
            }
        }
    }

    /**
     * Removes all variables from this label. May require {@link #parseTokens()} to be called after, if any values for
     * variables present in this Label used square-bracket or curly-brace markup. This doesn't check.
     */
    public void clearVariables() {
        this.variables.clear();
    }

    //////////////////////////////////
    /// --- Core Functionality --- ///
    //////////////////////////////////

    @Override
    public void act(float delta) {
        super.act(delta);
        subAct(delta);
    }

    /**
     * Performs the non-Action-related logic of parsing tokens, skipping/advancing (when needed), ensuring the layout
     * and workingLayout fields match internally, calculating size changes, and handling all effects. This is called by
     * {@link #act(float)} after it calls {@link com.badlogic.gdx.scenes.scene2d.Actor#act(float)}, which is what
     * handles scene2d {@link com.badlogic.gdx.scenes.scene2d.Action}s. This is also called by {@link #skipToTheEnd()}
     * (and all its overloads) with a delta of {@code 0f}, to ensure there isn't any
     * span of time after starting to skip when the shown chars, size, and layout aren't up-to-date.
     * @param delta the (typically fractional) amount of time, in seconds, since the last frame
     */
    protected void subAct(float delta) {
        // Force token parsing
        if (!parsed) {
            parseTokens();
        }

        // Update cooldown and process char progression
        if (skipping || (!ended && !paused)) {
            if (skipping || (charCooldown -= delta) < 0.0f) {
                processCharProgression();
            }
        }
        int glyphCount = layout.countGlyphs();

        getOffsets().setSize(glyphCount + glyphCount);
        System.arraycopy(layout.offsets.items, 0, workingLayout.offsets.items, 0, glyphCount + glyphCount);
        getSizing().setSize(glyphCount + glyphCount);
        System.arraycopy(layout.sizing.items, 0, workingLayout.sizing.items, 0, glyphCount + glyphCount);
        getRotations().setSize(glyphCount);
        System.arraycopy(layout.rotations.items, 0, workingLayout.rotations.items, 0, glyphCount);
        getAdvances().setSize(glyphCount);
        System.arraycopy(layout.advances.items, 0, workingLayout.advances.items, 0, glyphCount);

        font.calculateSize(workingLayout);

        // do we want this instead?
//        int iLay = 0, iWork = 0;
//        for (; iLay < glyphCount; iLay++, iWork++) {
//            if(getInLayout(layout, iLay) != getInWorkingLayout(iWork)) continue;
//            int e = iLay << 1, o = iLay | 1, ew = iWork << 1, ow = ew | 1;
//            getOffsets().set(ew, layout.offsets.get(e));
//            getOffsets().set(ow, layout.offsets.get(o));
//            getSizing().set(ew, layout.sizing.get(e));
//            getSizing().set(ow, layout.sizing.get(o));
//            getRotations().set(iWork, layout.sizing.get(iLay));
//            getAdvances().set(iWork, layout.advances.get(iLay));
//        }

        // Apply effects
        if (!ignoringEffects) {

            for (int i = activeEffects.size() - 1; i >= 0; i--) {
                Effect effect = activeEffects.get(i);
                effect.update(delta);
                int start = effect.indexStart;
                int end = effect.indexEnd >= 0 ? effect.indexEnd : glyphCharIndex;

                // If effect is finished, remove it
                if (effect.isFinished()) {
                    activeEffects.remove(i);
                    continue;
                }

                // Apply effect to glyph
                for (int j = Math.max(0, start); j <= glyphCharIndex && j <= end && j < glyphCount; j++) {
                    long glyph = getInLayout(workingLayout, j);
                    if (glyph == 0xFFFFFFL) break; // invalid char
                    effect.apply(glyph, j, delta);
                }
            }
        }
    }

    /**
     * Returns a seeded random float between -2.4f and -0.4f. This is meant to be used to randomize the typing
     * speed-ups and slow-downs for natural typing, when the NATURAL tag is used. It returns a negative value because
     * when textSpeed is negative, that indicates natural typing should be used, and so we multiply negative textSpeed
     * by a negative random value to get a normal positive textSpeed.
     * @param seed any int; should be the same if a value should be replicable
     * @return a random float between -2.4f and -0.4f
     */
    private float randomize(int seed) {
        return NumberUtils.intBitsToFloat((int) ((seed ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L >>> 41) | 0x40000000) - 4.4f;
    }

    /**
     * Process char progression according to current cooldown and process all tokens in the current index.
     */
    private void processCharProgression() {
        // Keep a counter of how many chars we're processing in this tick.
        int charCounter = 0;
        // Process chars while there's room for it
        while (skipping || charCooldown < 0.0f) {
            // Apply compensation to glyph index, if any
            if (glyphCharCompensation != 0) {
                if (glyphCharCompensation > 0) {
                    glyphCharIndex++;
                    glyphCharCompensation--;
                } else {
                    glyphCharIndex--;
                    glyphCharCompensation++;
                }

                // Increment cooldown and wait for it
                if(textSpeed < 0f) {
                    // "natural" text speed
                    charCooldown += textSpeed * randomize(glyphCharIndex);
                }
                else
                    charCooldown += textSpeed;
                continue;
            }

            // Increase raw char index
            rawCharIndex++;

            // Get next character and calculate cooldown increment

            int layoutSize = layout.countGlyphs();

            // If char progression is finished, or if text is empty, notify listener and abort routine
            if (layoutSize == 0 || glyphCharIndex >= layoutSize) {
                if (!ended) {
                    ended = true;
                    skipping = false;
                    if (listener != null) listener.end();
                }
                break;
            }

            // Process tokens according to the current index
            if (tokenEntries.size() > 0 && tokenEntries.get(tokenEntries.size() - 1).index == rawCharIndex) {
                TokenEntry entry = tokenEntries.remove(tokenEntries.size() - 1);
                String token = entry.token;
                TokenCategory category = entry.category;
                rawCharIndex = entry.endIndex - 1;
                // Process tokens
                switch (category) {
                    case SPEED: {
                        textSpeed = entry.floatValue;
                        continue;
                    }
                    case WAIT: {
                        charCooldown += entry.floatValue;
                        continue;
                    }
                    case EVENT: {
                        triggerEvent(entry.stringValue, false);
                        continue;
                    }
                    case EFFECT_START:
                    case EFFECT_END: {
                        // Get effect class
                        boolean isStart = category == TokenCategory.EFFECT_START;
                        String effectName = isStart ? token : token.substring(3);

                        // End all effects of the same type
                        for (int i = 0, s = activeEffects.size(); i < s; i++) {
                            Effect effect = activeEffects.get(i);
                            if (effect.indexEnd < 0) {
                                if (effectName.equals(effect.name)) {
                                    effect.indexEnd = glyphCharIndex;
                                }
                            }
                        }

                        // Create new effect if necessary
                        if (isStart) {
                            entry.effect.indexStart = glyphCharIndex + 1;
                            activeEffects.add(entry.effect);
                        }
                        continue;
                    }
                }
                break;
            }
            int safeIndex = MathUtils.clamp(glyphCharIndex + 1, 0, layoutSize - 1);
            long baseChar; // Null character by default
            if (layoutSize > 0) {
                baseChar = getInLayout(layout, safeIndex);
                float intervalMultiplier = TypingConfig.INTERVAL_MULTIPLIERS_BY_CHAR.get((char) baseChar, 1);
                if(textSpeed < 0f) {
                    charCooldown += textSpeed * randomize(glyphCharIndex) * intervalMultiplier;
                }
                else
                    charCooldown += textSpeed * intervalMultiplier;
            }

            // Increase glyph char index for all characters
            if (rawCharIndex > 0) {
                glyphCharIndex++;
            }

            // Notify listener about char progression
            if (glyphCharIndex >= 0 && glyphCharIndex < layoutSize && rawCharIndex >= 0 && listener != null) {
                listener.onChar(getInLayout(layout, glyphCharIndex));
            }

            // Break loop if this was our first glyph to prevent glyph issues.
            if (glyphCharIndex == 0 && !skipping) {
                charCooldown = Math.abs(textSpeed);
                break;
            }

            // Break loop if enough chars were processed
            charCounter++;
            int charLimit = TypingConfig.CHAR_LIMIT_PER_FRAME;
            if (!skipping && charLimit > 0 && charCounter > charLimit && textSpeed != 0f) {
                charCooldown = Math.max(charCooldown, Math.abs(textSpeed));
                break;
            }
        }

        if (wrap) {
            float actualWidth = getWidth();
            if(actualWidth != 0f)
                workingLayout.setTargetWidth(actualWidth);
        }

        invalidate();
    }

    @Override
    public void setSize(float width, float height) {
        // If the window is minimized, we have invalid dimensions and shouldn't process resizing.
        if(Gdx.graphics.getWidth() <= 0 || Gdx.graphics.getHeight() <= 0) return;
        // unfortunately, we can't call super.setSize(width, height) because
        // it changes layout, where we only want to change workingLayout.
        boolean changed = false;
        if (this.getWidth() != width) {
            this.setSuperWidth(width);
            changed = true;
        }
        if(this.getHeight() != height) {
            this.setSuperHeight(height);
            changed = true;
        }
        if(changed) {
            sizeChanged();
            if (wrap) {
                workingLayout.setTargetWidth(width);
                workingLayout.justification = defaultJustify;
                font.regenerateLayout(workingLayout);
                // This needs to work on the hierarchy; see TableWrapTest for evidence.
                invalidateHierarchy();
            }
        }
    }

    @Override
    public float getPrefWidth() {
        if (!parsed) {
            parseTokens();
        }
        if(wrap) return 0f;
        float width = workingLayout.getWidth();
        if(style != null && style.background != null)
            width = Math.max(width + style.background.getLeftWidth() + style.background.getRightWidth(), style.background.getMinWidth());
        return width;
    }

    @Override
    public float getPrefHeight() {
        if (!parsed) {
            parseTokens();
        }
        float height = workingLayout.getHeight();
        if(style != null && style.background != null)
            height = Math.max(height + style.background.getBottomHeight() + style.background.getTopHeight(), style.background.getMinHeight());
        return height;
    }


    @Override
    public void layout() {
        // If the window is minimized, we have invalid dimensions and shouldn't process layout.
        if(Gdx.graphics.getWidth() <= 0 || Gdx.graphics.getHeight() <= 0) return;
        float width = getWidth();
        if (style != null && style.background != null) {
            width = (width - (style.background.getLeftWidth() + style.background.getRightWidth()));
        }
        float originalHeight = workingLayout.getHeight();
        float actualWidth = font.calculateSize(workingLayout);

        if (wrap) {
            if (width == 0f || workingLayout.getTargetWidth() != width || actualWidth > width) {
                if (width != 0f)
                    workingLayout.setTargetWidth(width);
                workingLayout.justification = defaultJustify;
                font.regenerateLayout(workingLayout);
                font.calculateSize(workingLayout);
// We definitely don't want to invalidateHierarchy() here, because it would invalidate a lot every frame!
            }

// If the call to calculateSize() changed workingLayout's height, we want to update height and invalidateHierarchy().
            float newHeight = workingLayout.getHeight();
            if(!MathUtils.isEqual(originalHeight, newHeight)) {
                setSuperHeight(newHeight);
                invalidateHierarchy();
                // We don't want to call setHeight() because it would calculateSize() again, which isn't needed.
            }
        }
        // once a TypingLabel has been added to the Stage, somewhere, we can restart the effect and have it
        // do more than what it could do before it knew its own dimensions. We only want to do this once, even if
        // the label restarts at some later point, because this is only needed when layout() was called while the
        // label was still not added to the Stage yet.
        if(!onStage && hasParent()){
            onStage = true;
            if(!ended)
                restart();
        }
    }

    /**
     * If your font uses {@link Font.DistanceFieldType#SDF} or {@link Font.DistanceFieldType#MSDF},
     * then this has to do some extra work to use the appropriate shader.
     * If {@link Font#enableShader(Batch)} was called before rendering a group of TypingLabels, then they will try to
     * share one Batch; otherwise this will change the shader to render SDF or MSDF, then change it back at the end of
     * each draw() call.
     *
     * @param batch probably should be a SpriteBatch
     * @param parentAlpha the alpha of the parent container, or 1.0f if there is none
     */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        drawSection(batch, parentAlpha, 0, -1);
    }
    /**
     * Renders a subsection of the glyphs in this label.
     * If your font uses {@link Font.DistanceFieldType#SDF} or {@link Font.DistanceFieldType#MSDF},
     * then this has to do some extra work to use the appropriate shader.
     * If {@link Font#enableShader(Batch)} was called before rendering a group of TypingLabels, then they will try to
     * share one Batch; otherwise this will change the shader to render SDF or MSDF, then change it back at the end of
     * each draw() call.
     *
     * @param batch probably should be a SpriteBatch
     * @param parentAlpha the alpha of the parent container, or 1.0f if there is none
     * @param startIndex the first index, inclusive, to start rendering at
     * @param endIndex the last index, exclusive, to stop rendering before; if negative this won't be limited
     */
    public void drawSection(Batch batch, float parentAlpha, int startIndex, int endIndex) {
        super.validate();

        final float rot = getRotation();
        final float originX = getOriginX();
        final float originY = getOriginY();
        final float sn = MathUtils.sinDeg(rot);
        final float cs = MathUtils.cosDeg(rot);

        int bgc;
        final int lines = workingLayout.lines();
        float baseX = getX(), baseY = getY();

        // These two blocks use different height measurements, so center vertical is offset once by half the layout
        // height, and once by half the widget height.
        float layoutHeight = workingLayout.getHeight();
        if (Align.isBottom(align)) {
            baseX -= sn * layoutHeight;
            baseY += cs * layoutHeight;
        } else if (Align.isCenterVertical(align)) {
            baseX -= sn * layoutHeight * 0.5f;
            baseY += cs * layoutHeight * 0.5f;
        }
        float widgetHeight = getHeight();
        if (Align.isTop(align)) {
            baseX -= sn * widgetHeight;
            baseY += cs * widgetHeight;
        } else if (Align.isCenterVertical(align)) {
            baseX -= sn * widgetHeight * 0.5f;
            baseY += cs * widgetHeight * 0.5f;
        }

        float widgetWidth = getWidth();
        if (Align.isRight(align)) {
            baseX += cs * widgetWidth;
            baseY += sn * widgetWidth;
        } else if (Align.isCenterHorizontal(align)) {
            baseX += cs * widgetWidth * 0.5f;
            baseY += sn * widgetWidth * 0.5f;
        }

        if (style != null && style.background != null) {
            Drawable background = style.background;
            if (Align.isLeft(align)) {
                baseX += cs * background.getLeftWidth();
                baseY += sn * background.getLeftWidth();
            } else if (Align.isRight(align)) {
                baseX -= cs * background.getRightWidth();
                baseY -= sn * background.getRightWidth();
            } else {
                baseX += cs * (background.getLeftWidth() - background.getRightWidth()) * 0.5f;
                baseY += sn * (background.getLeftWidth() - background.getRightWidth()) * 0.5f;
            }
            if (Align.isBottom(align)) {
                baseX -= sn * background.getBottomHeight();
                baseY += cs * background.getBottomHeight();
            } else if (Align.isTop(align)) {
                baseX += sn * background.getTopHeight();
                baseY -= cs * background.getTopHeight();
            } else {
                baseX -= sn * (background.getBottomHeight() - background.getTopHeight()) * 0.5f;
                baseY += cs * (background.getBottomHeight() - background.getTopHeight()) * 0.5f;
            }
            try {
                ((TransformDrawable) background).draw(batch,
                        getX(), getY(),             // position
                        originX, originY,           // origin
                        getWidth(), getHeight(),    // size
                        1f, 1f,                     // scale
                        rot);                       // rotation
            } catch (UnsupportedOperationException | ClassCastException itIsJustADrawable) {
                // TenPatch drawables do not support rotation, scale, or an origin, so we can use the
                // standard Drawable draw method and just assume people aren't trying to rotate TenPatches.
                // This also works in case the background is not a TransformDrawable.
                background.draw(batch,
                        getX(), getY(),             // position
                        getWidth(), getHeight());   // size
            }
        }

        if (layout.lines.isEmpty() || parentAlpha <= 0f) return;

//        baseY += workingLayout.lines.first().height * 0.25f;

        int o = 0, s = 0, r = 0, gi = 0;
        boolean resetShader = font.getDistanceField() != Font.DistanceFieldType.STANDARD && batch.getShader() != font.shader;
        if (resetShader)
            font.enableShader(batch);
        batch.getColor().set(getColor()).a *= parentAlpha;
        batch.setColor(batch.getColor());

        int globalIndex = startIndex - 1;

        float inX = 0, inY = 0;
        if(trackingInput) {
            if(hasParent())
                getParent().screenToLocalCoordinates(temp.set(Gdx.input.getX(), Gdx.input.getY()));
            else {
                // I have no idea why the y has to be flipped here, but not above.
                screenToLocalCoordinates(temp.set(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()));
            }

            inX = temp.x;
            inY = temp.y;

            if(!Gdx.input.isTouched())
                lastTouchedIndex = inY < getY() ? -2 : inY > getY() + getHeight() ? -1 :
                        inX < getX() ? -1 : inX > getX() + getWidth() ? -2 : -1;
            overIndex = -1;
        }

        float single, tempBaseX = baseX, tempBaseY = baseY;
        int toSkip = 0;
        boolean curly = false;

        if(selectable && selectionDrawable != null) {
            if(selectionStart != selectionEnd) {
                SELECTION_LINE:
                for (int ln = 0; ln < lines; ln++) {
                    Line glyphs = workingLayout.getLine(ln);

                    if (glyphs.glyphs.size == 0 || (toSkip += glyphs.glyphs.size) < startIndex)
                        continue;

                    float selectionDrawStartX = 0f, selectionDrawStartY = 0f;
                    float selectionWidth = 0f;

                    tempBaseX += sn * glyphs.height;
                    tempBaseY -= cs * glyphs.height;

                    float x = tempBaseX, y = tempBaseY;

                    final float worldOriginX = x + originX;
                    final float worldOriginY = y + originY;
                    float fx = -originX;
                    float fy = -originY;
                    x = cs * fx - sn * fy + worldOriginX;
                    y = sn * fx + cs * fy + worldOriginY;

                    float xChange = 0, yChange = 0;

                    if (Align.isCenterHorizontal(align)) {
                        x -= cs * (glyphs.width * 0.5f);
                        y -= sn * (glyphs.width * 0.5f);
                    } else if (Align.isRight(align)) {
                        x -= cs * glyphs.width;
                        y -= sn * glyphs.width;
                    }

                    Font f = null;
                    int kern = -1,
                            start = (toSkip - glyphs.glyphs.size < startIndex) ? startIndex - (toSkip - glyphs.glyphs.size) : 0,
                            end = endIndex < 0 ? glyphCharIndex : Math.min(glyphCharIndex, endIndex - 1);
                    for (int i = start, n = glyphs.glyphs.size,
                         lim = Math.min(Math.min(Math.min(getRotations().size, getAdvances().size), getOffsets().size >> 1), getSizing().size >> 1);
                         i < n && r < lim; i++, gi++) {
                        if (gi > end) break SELECTION_LINE;
                        long glyph = glyphs.glyphs.get(i);
                        char ch = (char) glyph;
                        if (font.family != null) f = font.family.connected[(int) (glyph >>> 16 & 15)];
                        if (f == null) f = font;
                        float descent = f.descent * f.scaleY;

                        if(font.omitCurlyBraces) {
                            if (curly) {
                                if(i == start)
                                    start++;
                                if (ch == '}') {
                                    curly = false;
                                    continue;
                                } else if (ch == '{') {
                                    curly = false;
                                }
                                else continue;
                            } else if (ch == '{') {
                                curly = true;
                                if(i == start)
                                    start++;
                                continue;
                            }
                        }

                        float a = getAdvances().get(r);

                        if (i == start) {
                            x -= f.cellWidth * 0.5f;

                            x += cs * f.cellWidth * 0.5f;
                            y += sn * f.cellWidth * 0.5f;

//                    x += sn * descent * 0.5f;
//                    y -= cs * descent * 0.5f;

                            y += descent;
                            x += sn * (descent /* - 0.5f * glyphs.height */);
                            y -= cs * (descent /* - 0.5f * glyphs.height */);

                            Font.GlyphRegion reg = font.mapping.get((char) glyph);
                            if (reg != null && reg.offsetX < 0 && !font.isMono && !((char) glyph >= '\uE000' && (char) glyph < '\uF800')) {
                                float ox = reg.offsetX;
                                ox *= f.scaleX * a;
                                if (ox < 0) {
                                    xChange -= cs * ox;
                                    yChange -= sn * ox;
                                }
                            }

                        }

                        if (f.kerning != null) {
                            kern = kern << 16 | (int) ((glyph = glyphs.glyphs.get(i)) & 0xFFFF);
                            float amt = f.kerning.get(kern, 0) * f.scaleX * a;
                            xChange += cs * amt;
                            yChange += sn * amt;
                        } else {
                            kern = -1;
                        }
                        ++globalIndex;
                        if (selectionEnd < globalIndex)
                            break;
                        float xx = x + xChange + getOffsets().get(o++), yy = y + yChange + getOffsets().get(o++);
                        if (font.integerPosition) {
                            xx = (int) xx;
                            yy = (int) yy;
                        }

                        float scale = a, scaleX;
                        if ((char) glyph >= 0xE000 && (char) glyph < 0xF800) {
                            scaleX = scale * font.cellHeight / (f.mapping.get((int) glyph & 0xFFFF, f.defaultValue).xAdvance);
                        } else
                            scaleX = font.scaleX * scale;

                        single = Font.xAdvance(f, scaleX, glyph) * a;
                        r++;
                        if(selectionWidth == 0f)
                        {
                            selectionDrawStartX = xx;
                            selectionDrawStartY = yy;
                        }
                        if (selectionStart <= globalIndex)
                            selectionWidth += single;
                        xChange += cs * single;
                        yChange += sn * single;
                    }
                    // draw one selection drawable
                    if(selectionWidth > 0f)
                        selectionDrawable.draw(batch, selectionDrawStartX, selectionDrawStartY, selectionWidth, glyphs.height);
                }
            }
        }


        o = 0;
        r = 0;
        gi = 0;
        globalIndex = startIndex - 1;
        curly = false;

        EACH_LINE:
        for (int ln = 0; ln < lines; ln++) {
            Line line = workingLayout.getLine(ln);

            if(line.glyphs.size == 0 || (toSkip += line.glyphs.size) < startIndex)
                continue;

            baseX += sn * line.height;
            baseY -= cs * line.height;

            float x = baseX, y = baseY;

            final float worldOriginX = x + originX;
            final float worldOriginY = y + originY;
            float fx = -originX;
            float fy = -originY;
            x = cs * fx - sn * fy + worldOriginX;
            y = sn * fx + cs * fy + worldOriginY;

            float xChange = 0, yChange = 0;

            if (Align.isCenterHorizontal(align)) {
                x -= cs * (line.width * 0.5f);
                y -= sn * (line.width * 0.5f);
            } else if (Align.isRight(align)) {
                x -= cs * line.width;
                y -= sn * line.width;
            }
            Font f = null;
            int kern = -1,
                    start = (toSkip - line.glyphs.size < startIndex) ? startIndex - (toSkip - line.glyphs.size) : 0,
                    end = endIndex < 0 ? glyphCharIndex : Math.min(glyphCharIndex, endIndex - 1);
            for (int i = start, n = line.glyphs.size,
                 lim = Math.min(Math.min(Math.min(getRotations().size, getAdvances().size), getOffsets().size >> 1), getSizing().size >> 1);
                 i < n && r < lim; i++, gi++) {
                if (gi > end) break EACH_LINE;
                long glyph = line.glyphs.get(i);
                char ch = (char) glyph;
                if (font.family != null) f = font.family.connected[(int) (glyph >>> 16 & 15)];
                if (f == null) f = font;
                float descent = f.descent * f.scaleY;

                if(font.omitCurlyBraces) {
                    if (curly) {
                        if(i == start)
                            start++;
                        if (ch == '}') {
                            curly = false;
                            continue;
                        } else if (ch == '{') {
                            curly = false;
                            --start;
                        }
                        else continue;
                    } else if (ch == '{') {
                        curly = true;
                        if(i == start)
                            start++;
                        continue;
                    }
                }
                float a = getAdvances().get(r);

                if(i == start){
                    x -= f.cellWidth * 0.5f;

                    x += cs * f.cellWidth * 0.5f;
                    y += sn * f.cellWidth * 0.5f;

                    y += descent;
                    x += sn * (descent - 0.5f * line.height);
                    y -= cs * (descent - 0.5f * line.height);

                    Font.GlyphRegion reg = font.mapping.get((char) glyph);
                    if (reg != null && reg.offsetX < 0 && !font.isMono && !((char) glyph >= '\uE000' && (char) glyph < '\uF800')) {
                        float ox = reg.offsetX;
                        ox *= f.scaleX * a;
                        if (ox < 0) {
                            xChange -= cs * ox;
                            yChange -= sn * ox;
                        }
                    }
                }

                if (f.kerning != null) {
                    kern = kern << 16 | (int) ((glyph = line.glyphs.get(i)) & 0xFFFF);
                    float amt = f.kerning.get(kern, 0) * f.scaleX * a;
                    xChange += cs * amt;
                    yChange += sn * amt;
                } else {
                    kern = -1;
                }
                ++globalIndex;
                if(endIndex >= 0 && globalIndex >= endIndex) break EACH_LINE;
                if(selectable && selectionDrawable == null && selectionStart <= globalIndex && selectionEnd >= globalIndex)
                    bgc = ColorUtils.offsetLightness((int)(glyph >>> 32), 0.5f);
                else
                    bgc = 0;
                float xx = x + xChange + getOffsets().get(o++), yy = y + yChange + getOffsets().get(o++);
                if(font.integerPosition){
                    xx = (int)xx;
                    yy = (int)yy;
                }

                single = f.drawGlyph(batch, glyph, xx, yy, getRotations().get(r) + rot, getSizing().get(s++), getSizing().get(s++), bgc, a);
                r++;
                if(trackingInput){
                    if(xx <= inX && inX <= xx + single && yy - line.height * 0.5f <= inY && inY <= yy + line.height * 0.5f) {
                        overIndex = globalIndex;
                        if (isTouchable()) {
                            if (Gdx.input.justTouched()) {
                                lastTouchedIndex = globalIndex;
                                selectionStart = -1;
                                selectionEnd = -1;
                            }
                            else if(selectable) {
                                if (Gdx.input.isTouched()) {
                                    int adjustedIndex = (lastTouchedIndex == -2) ? workingLayout.countGlyphs() : lastTouchedIndex;
                                    selectionStart = Math.min(adjustedIndex, globalIndex);
                                    selectionEnd = Math.max(adjustedIndex, globalIndex);
                                    dragging = true;
                                } else if(dragging){
                                    dragging = false;
                                    if(selectionStart != selectionEnd){
                                        triggerEvent("*SELECTED", true);
                                    }
                                    else {
                                        selectionStart = selectionEnd = -1;
                                    }
                                }
                            }
                        }
                    }
                }
                xChange += cs * single;
                yChange += sn * single;
            }

        }
//        invalidate();
//        addMissingGlyphs();
        if (resetShader)
            batch.setShader(null);
    }

    @Override
    public String toString() {
        return substring(0, Integer.MAX_VALUE);
    }

    /**
     * If this label is {@link #selectable} and there is a selected range of text, this returns that range of text;
     * otherwise, it returns the empty string.
     * @return the currently selected text, or the empty string if none is or can be selected
     */
    public String getSelectedText() {
        if(!selectable || (selectionStart >= selectionEnd && selectionStart < 0)) return "";
        return substring(selectionStart, selectionEnd+1);
    }

    /**
     * If this label is {@link #selectable} and there is a selected range of text, this copies that range of text to the
     * clipboard and returns true; otherwise, it returns false.
     * @return true if text was copied, or false if the clipboard hasn't received any text
     */
    public boolean copySelectedText() {
        if(!selectable || (selectionStart >= selectionEnd && selectionStart < 0)) return false;
        Gdx.app.getClipboard().setContents(substring(selectionStart, selectionEnd+1));
        return true;
    }

    /**
     * If this label is {@link #selectable} and there is a selected range of text, this
     * returns true; otherwise, it returns false.
     * @return true if there is selected text, or false otherwise
     */
    public boolean hasSelection() {
        return selectable && (selectionStart < selectionEnd || selectionStart >= 0);
    }

    public void setIntermediateText(CharSequence text, boolean modifyOriginalText, boolean restart) {
        final boolean hasEnded = this.hasEnded();
        if (text != intermediateText) {
            intermediateText.setLength(0);
            intermediateText.append(text);
        }
        if (modifyOriginalText) saveOriginalText(text);
        if (restart) {
            this.restart();
        }
        if (hasEnded) {
            this.skipToTheEnd(true, false);
        }
    }

    public StringBuilder getIntermediateText() {
        return intermediateText;
    }

    public long getInLayout(Layout layout, int index) {
        for (int i = 0, n = layout.lines(); i < n && index >= 0; i++) {
            LongArray glyphs = layout.getLine(i).glyphs;
            if (index < glyphs.size)
                return glyphs.get(index);
            else
                index -= glyphs.size;
        }
        return 0xFFFFFFL;
    }

    public long getInWorkingLayout(int index) {
        for (int i = 0, n = workingLayout.lines(); i < n && index >= 0; i++) {
            LongArray glyphs = workingLayout.getLine(i).glyphs;
            if (index < glyphs.size)
                return glyphs.get(index);
            else
                index -= glyphs.size;
        }
        return 0xFFFFFFL;
    }

    /**
     * Returns the meant-for-internal-use-only Layout that is frequently changed as this label is displayed. The working
     * layout may be useful to have, even if treated as read-only, so it is exposed here. Still, be very careful with
     * this method and the Layout it returns. The working layout is the one that gets shown, where {@link #layout} is
     * used as the ideal text before wrapping or other requirements edit it. The getters {@link #getRotations()},
     * {@link #getSizing()}, {@link #getOffsets()}, and {@link #getAdvances()} refer to the working layout's fields
     * {@link Layout#rotations}, {@link Layout#sizing}, and {@link Layout#offsets}, and {@link Layout#advances},
     * respectively.
     *
     * @return the mostly-internal working layout, which is the layout that gets displayed.
     */
    public Layout getWorkingLayout() {
        return workingLayout;
    }

    /**
     * The maximum number of {@link Line}s this label can display.
     *
     * @return the maximum number of {@link Line} objects this label can display
     */
    public int getMaxLines() {
        return workingLayout.maxLines;
    }

    /**
     * Sets the maximum number of {@link Line}s this Layout can display; this is always at least 1.
     * For effectively unlimited lines, pass {@link Integer#MAX_VALUE} to this.
     *
     * @param maxLines the limit for how many Line objects this Layout can display; always 1 or more
     */
    public void setMaxLines(int maxLines) {
        workingLayout.setMaxLines(maxLines);
    }

    /**
     * Gets the ellipsis, which may be null, or may be a String that can be placed at the end of the text if its
     * max lines are exceeded.
     *
     * @return an ellipsis String or null
     */
    public String getEllipsis() {
        return workingLayout.ellipsis;
    }

    /**
     * Sets the ellipsis text, which replaces the last few glyphs if non-null and the text added would exceed the
     * {@link #getMaxLines()} of this label's working layout. For the ellipsis to appear, this has to be called with a
     * non-null String (often {@code "..."}, or {@code "…"} if the font supports it), and
     * {@link #setMaxLines(int)} needs to have been called with a small enough number, such as 1.
     *
     * @param ellipsis a String for a Layout to end with if its max lines are exceeded, or null to avoid such truncation
     */
    public void setEllipsis(String ellipsis) {
        workingLayout.setEllipsis(ellipsis);
    }

    /**
     * Gets a String from the working layout of this label, made of only the char portions of the glyphs from start
     * (inclusive) to end (exclusive). This can retrieve text from across multiple lines. This delegates to
     * {@link #substring(int, int, boolean)} with multiLine set to true.
     * @param start inclusive start index
     * @param end exclusive end index
     * @return a String made of only the char portions of the glyphs from start to end
     */
    public String substring(int start, int end) {
        return substring(start, end, true);
    }
    /**
     * Gets a String from the working layout of this label, made of only the char portions of the glyphs from start
     * (inclusive) to end (exclusive). This can retrieve text from across multiple lines. If {@code multiLine} is true,
     * each Line will be separated by a {@code '\n'} (newline) char; otherwise, this will return one line of text except
     * where a newline was already present in the original text.
     * @param start inclusive start index
     * @param end exclusive end index
     * @param multiLine if true, this will return a String that contains a newline between {@code Line}s
     * @return a String made of only the char portions of the glyphs from start to end
     */
    public String substring(int start, int end, boolean multiLine) {
        start = Math.max(0, start);
        end = Math.min(Math.max(workingLayout.countGlyphs(), start), end);
        int index = start;
        StringBuilder sb = new StringBuilder(end - start);
        int glyphCount = 0;
        for (int i = 0, n = workingLayout.lines(); i < n && index >= 0; i++) {
            LongArray glyphs = workingLayout.getLine(i).glyphs;
            if (index < glyphs.size) {
                for (int fin = index - start - glyphCount + end; index < fin && index < glyphs.size; index++) {
                    char c = (char) glyphs.get(index);
                    if (c >= '\uE000' && c <= '\uF800') {
                        String name = font.namesByCharCode.get(c);
                        if (name != null) sb.append(name);
                        else sb.append(c);
                    } else {
                        if (c == '\u0002') sb.append('[');
                        else if(c != '\u200B') sb.append(c); // do not print zero-width space
                    }
                    glyphCount++;
                }
                if(glyphCount == end - start)
                    return sb.toString();
                index = 0;
            }
            else
                index -= glyphs.size;
            if(multiLine) sb.append('\n');
        }
        return "";
    }

    public Line getLineInLayout(Layout layout, int index) {
        for (int i = 0, n = layout.lines(); i < n && index >= 0; i++) {
            LongArray glyphs = layout.getLine(i).glyphs;
            if (index < glyphs.size)
                return layout.getLine(i);
            else
                index -= glyphs.size;
        }
        return null;
    }
    /**
     * Gets the height of the Line containing the glyph at the given index, in the working layout. If the index is out
     * of bounds, this just returns {@link Font#cellHeight}.
     * @param index the 0-based index of the glyph to measure
     * @return the height of the Line containing the specified glyph
     */
    public float getLineHeight(int index) {
        for (int i = 0, n = workingLayout.lines(); i < n && index >= 0; i++) {
            LongArray glyphs = workingLayout.getLine(i).glyphs;
            if (index < glyphs.size)
                return workingLayout.getLine(i).height;
            else
                index -= glyphs.size;
        }
        return font.cellHeight;
    }
    /**
     * Gets the height of the Line containing the glyph at the given index, plus the heights of all preceding lines, in
     * the working layout. If the index is out of bounds, this returns either 0 if index was too low, or the height of
     * all lines together if the index was too high.
     * @param index the 0-based index of the glyph to measure
     * @return the sum of the height of the Line containing the specified glyph and all preceding line heights
     */
    public float getCumulativeLineHeight(int index) {
        float cumulative = 0f;
        for (int i = 0, n = workingLayout.lines(); i < n && index >= 0; i++) {
            LongArray glyphs = workingLayout.getLine(i).glyphs;
            if (index < glyphs.size)
                return cumulative + workingLayout.getLine(i).height;
            index -= glyphs.size;
            cumulative += workingLayout.getLine(i).height;
        }
        return cumulative;
    }

    public long getFromIntermediate(int index) {
        if (index >= 0 && intermediateText.length() > index) return intermediateText.charAt(index);
        else return 0xFFFFFFL;
    }

    public void setInLayout(Layout layout, int index, long newGlyph) {
        for (int i = 0, n = layout.lines(); i < n && index >= 0; i++) {
            LongArray glyphs = layout.getLine(i).glyphs;
            if (index < glyphs.size) {
                glyphs.set(index, newGlyph);
                return;
            } else
                index -= glyphs.size;
        }
    }

    public void insertInLayout(Layout layout, int index, long newGlyph) {
        for (int i = 0, n = layout.lines(); i < n && index >= 0; i++) {
            LongArray glyphs = layout.getLine(i).glyphs;
            if (index <= glyphs.size) {
                glyphs.insert(index, newGlyph);
                return;
            } else
                index -= glyphs.size;
        }
    }

    public void insertInLayout(Layout layout, int index, CharSequence text) {
        long current = (Integer.reverseBytes(NumberUtils.floatToIntBits(layout.baseColor)) & -2L) << 32;
        for (int i = 0, n = layout.lines(); i < n && index >= 0; i++) {
            LongArray glyphs = layout.getLine(i).glyphs;
            if (index < glyphs.size) { // inserting mid-line
                current = glyphs.get(index) & 0xFFFFFFFFFFFF0000L;
                for (int j = 0; j < text.length(); j++) {
                    glyphs.insert(index + j, current | text.charAt(j));
                }
                return;
            } else if (index == glyphs.size) { // appending to a line
                if(index != 0)
                    current = glyphs.get(index - 1) & 0xFFFFFFFFFFFF0000L;
                for (int j = 0; j < text.length(); j++) {
                    glyphs.insert(index + j, current | text.charAt(j));
                }
                return;
            } else {
                index -= glyphs.size;
            }
        }
    }

    public void setInWorkingLayout(int index, long newGlyph) {
        for (int i = 0, n = workingLayout.lines(); i < n && index >= 0; i++) {
            LongArray glyphs = workingLayout.getLine(i).glyphs;
            if (i < workingLayout.lines() && index < glyphs.size) {
                glyphs.set(index, newGlyph);
                return;
            } else
                index -= glyphs.size;
        }
    }

    /**
     * Gets the length in glyphs of the working layout (what is displayed).
     * @return the length in glyphs of the working layout (what is displayed)
     */
    public int length() {
        return workingLayout.countGlyphs();
    }

    /**
     * Triggers an event with the given String name. If {@code always} is true, this will trigger the event even if the
     * typing animation has already ended. This requires a {@link TypingListener} to be set.
     * @param event the event name to trigger
     * @param always if true, the event will be triggered even if the animation has finished.
     */
    public void triggerEvent(String event, boolean always) {
        if (this.listener != null && (always || !ignoringEvents)) {
            listener.event(event);
        }
    }

    /**
     * Returns true if and only if {@link #selectable} is true and {@link #trackingInput} is true; otherwise false.
     * @return whether the text of this label is selectable
     */
    public boolean isSelectable() {
        return selectable && trackingInput;
    }

    /**
     * If given {@code true}, this makes the text of this label {@link #selectable} and ensures {@link #trackingInput}
     * is true. Otherwise, this makes the label not-selectable and doesn't change {@link #trackingInput}. The
     * application should usually be set to copy the selected text using {@link #copySelectedText()} when the user
     * expects it to be copied. Often, a {@link TypingListener} that checks for the event {@code "*SELECTED"} works.
     * This also sets the {@link #selectionDrawable} to something nicer-looking than the default, but it can only do
     * this if {@link #font} has a {@link Font#solidBlock}.
     * @param selectable true if the text of this label should be selectable
     * @return this, for chaining
     */
    public TypingLabel setSelectable(boolean selectable) {
        this.selectable = selectable;
        this.trackingInput |= selectable;
        if(selectable && font.mapping.containsKey(font.solidBlock)){
            Sprite spr = new Sprite(font.mapping.get(font.solidBlock));
            spr.setColor(0.5f, 0.5f, 0.5f, 0.5f);
            selectionDrawable = new SpriteDrawable(spr);
        }
        return this;
    }

    public float getTextSpeed() {
        return textSpeed;
    }

    public void setTextSpeed(float textSpeed) {
        this.textSpeed = textSpeed;
//        this.charCooldown = textSpeed;
    }

    /**
     * Contains one float per glyph; each is a rotation in degrees to apply to that glyph (around its center).
     * This should not be confused with {@link #getRotation()}, which refers to the rotation of the label itself.
     * This getter accesses the rotation of each glyph around its center instead.
     * This is a direct reference to the current {@link #getWorkingLayout() working layout}'s {@link Layout#rotations}.
     */
    public FloatArray getRotations() {
        return workingLayout.rotations;
    }

    /**
     * Contains two floats per glyph; even items are x offsets, odd items are y offsets.
     * This getter accesses the x- and y-offsets of each glyph from its normal position.
     * This is a direct reference to the current {@link #getWorkingLayout() working layout}'s {@link Layout#offsets}.
     */
    public FloatArray getOffsets() {
        return workingLayout.offsets;
    }

    /**
     * Contains two floats per glyph, as size multipliers; even items apply to x, odd items apply to y.
     * This getter accesses the x-and y-scaling of each glyph in its normal location, without changing line height or
     * the x-advance of each glyph. It is usually meant for temporary or changing effects, not permanent scaling.
     * This is a direct reference to the current {@link #getWorkingLayout() working layout}'s {@link Layout#sizing}.
     */
    public FloatArray getSizing() {
        return workingLayout.sizing;
    }

    /**
     * Contains one float per glyph; each is a multiplier that affects the x-advance of that glyph.
     * This getter uses the same types of values as {@link #getSizing()}, so if you change the x-scaling of a glyph with
     * that variable, you can also change its x-advance here by assigning the same value for that glyph here.
     * This is a direct reference to the current {@link #getWorkingLayout() working layout}'s {@link Layout#rotations}.
     */
    public FloatArray getAdvances() {
        return workingLayout.advances;
    }
}
