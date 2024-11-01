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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.LongArray;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.github.tommyettinger.textra.utils.ColorUtils;

import java.lang.StringBuilder;
import java.util.Arrays;
import java.util.Map;

import static com.github.tommyettinger.textra.Font.ALTERNATE;

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
 */
public class TypingLabel extends TextraLabel {
    ///////////////////////
    /// --- Members --- ///
    ///////////////////////

    // Collections
    private final ObjectMap<String, String> variables = new ObjectMap<>();
    final Array<TokenEntry> tokenEntries = new Array<>();

    // Config
    private final Color clearColor = new Color(TypingConfig.DEFAULT_CLEAR_COLOR);
    private TypingListener listener = null;

    // Internal state
    private final StringBuilder originalText = new StringBuilder();
    private final StringBuilder intermediateText = new StringBuilder();
    protected final Layout workingLayout = new Layout();
    /**
     * Contains two floats per glyph; even items are x offsets, odd items are y offsets.
     */
    public final FloatArray offsets = new FloatArray();
    /**
     * Contains two floats per glyph, as size multipliers; even items apply to x, odd items apply to y.
     */
    public final FloatArray sizing = new FloatArray();
    /**
     * Contains one float per glyph; each is a rotation in degrees to apply to that glyph (around its center).
     */
    public final FloatArray rotations = new FloatArray();
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
    protected final Array<Effect> activeEffects = new Array<>(Effect.class);
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
    private String defaultToken = "";

    ////////////////////////////
    /// --- Constructors --- ///
    ////////////////////////////

    /**
     * Creates a TypingLabel that uses the libGDX default font (lsans-15) and starts with no text.
     * The default font will not look very good when scaled, so this should usually stay its default font size.
     */
    public TypingLabel() {
        super();
        workingLayout.font(super.font);
        setText("", true);
    }

    /**
     * The skin should almost certainly be an {@link FWSkin} or one of its subclasses.
     * @param text markup text that can contain square-bracket tags and curly-brace tokens
     * @param skin almost always an {@link FWSkin} or one of its subclasses; must have a
     *             {@link Styles.LabelStyle} or {@link com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle} registered as "default"
     */
    public TypingLabel(String text, Skin skin) {
        this(text, skin.get(Styles.LabelStyle.class));
    }

    /**
     * The skin should almost certainly be an {@link FWSkin} or one of its subclasses.
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
            if(font.omitCurlyBraces || font.enableSquareBrackets)
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
        font.markup(newText, layout.clear());
        if (wrap) {
            workingLayout.setTargetWidth(getWidth());
            font.markup(newText, workingLayout.clear());
        } else {
            workingLayout.setTargetWidth(0f);
            font.markup(newText, workingLayout.clear());
            setWidth(workingLayout.getWidth() + (style != null && style.background != null ?
                    style.background.getLeftWidth() + style.background.getRightWidth() : 0.0f));
        }
        if (modifyOriginalText) saveOriginalText(newText);
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
        if(font.omitCurlyBraces || font.enableSquareBrackets)
            this.setText(Parser.preprocess(getDefaultToken() + originalText), false, false);
        else
            this.setText(getDefaultToken() + originalText, false, false);
        Parser.parseTokens(this);
        ended = actualEnd;
    }

    /**
     * Skips the char progression to the end, showing the entire label. Useful for when users don't want to wait for too
     * long. Ignores all subsequent events by default. Doesn't change running effects.
     * This calls {@link #act(float)} with a delta of {@link Float#MIN_VALUE}, which allows the text to be skipped
     * ahead without noticeably changing anything time-based.
     * @return this, for chaining
     */
    @Override
    public TypingLabel skipToTheEnd() {
        return skipToTheEnd(true);
    }

    /**
     * Skips the char progression to the end, showing the entire label. Useful for when users don't want to wait for too
     * long. This doesn't change running effects.
     * This calls {@link #act(float)} with a delta of {@link Float#MIN_VALUE}, which allows the text to be skipped
     * ahead without noticeably changing anything time-based.
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
     * This calls {@link #act(float)} with a delta of {@link Float#MIN_VALUE}, which allows the text to be skipped
     * ahead without noticeably changing anything time-based.
     *
     * @param ignoreEvents  If {@code true}, skipped events won't be reported to the listener.
     * @param ignoreEffects If {@code true}, all text effects will be instantly cancelled.
     * @return this, for chaining
     */
    public TypingLabel skipToTheEnd(boolean ignoreEvents, boolean ignoreEffects) {
        skipping = true;
        ignoringEvents = ignoreEvents;
        ignoringEffects = ignoreEffects;
        act(Float.MIN_VALUE);
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
     */
    public void restart(CharSequence newText) {
        workingLayout.atLimit = false;

        // Reset cache collections
        Line first = workingLayout.lines.first();
        first.glyphs.clear();
        first.width = first.height = 0;
        workingLayout.lines.clear();
        workingLayout.lines.add(first);
        offsets.clear();
        sizing.clear();
        rotations.clear();
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
        invalidate();
        saveOriginalText(newText);

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
        if(var != null)
            variables.remove(var.toUpperCase());
    }

    /**
     * Registers a set of variables and their respective replacement values to this label.
     * @param variableMap an ObjectMap of variable names to their replacement Strings
     */
    public void setVariables(ObjectMap<String, String> variableMap) {
        this.variables.clear();
        if (variableMap != null) {
            for (Entry<String, String> entry : variableMap.entries()) {
                this.variables.put(entry.key.toUpperCase(), entry.value);
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
     * Removes all variables from this label.
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
        font.calculateSize(workingLayout);
        int glyphCount = workingLayout.countGlyphs();
        offsets.setSize(glyphCount + glyphCount);
        Arrays.fill(offsets.items, 0, glyphCount + glyphCount, 0f);
        sizing.setSize(glyphCount + glyphCount);
        Arrays.fill(sizing.items, 0, glyphCount + glyphCount, 1f);
        rotations.setSize(glyphCount);
        Arrays.fill(rotations.items, 0, glyphCount, 0f);

        // Apply effects
        if (!ignoringEffects) {

            for (int i = activeEffects.size - 1; i >= 0; i--) {
                Effect effect = activeEffects.get(i);
                effect.update(delta);
                int start = effect.indexStart;
                int end = effect.indexEnd >= 0 ? effect.indexEnd : glyphCharIndex;

                // If effect is finished, remove it
                if (effect.isFinished()) {
                    activeEffects.removeIndex(i);
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
     * Proccess char progression according to current cooldown and process all tokens in the current index.
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
            if (tokenEntries.size > 0 && tokenEntries.peek().index == rawCharIndex) {
                TokenEntry entry = tokenEntries.pop();
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
                        for (int i = 0; i < activeEffects.size; i++) {
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
//            font.regenerateLayout(workingLayout);
        }
        font.calculateSize(workingLayout);


        invalidate();
    }

    @Override
    public boolean remove() {
        return super.remove();
    }

    @Override
    public void setSize(float width, float height) {
        // unfortunately, we can't call super.setSize(width, height) because
        // it changes layout, where we only want to change workingLayout.
        boolean changed = false;
        if (this.getWidth() != width) {
            this.setWidth(width);
            changed = true;
        }
        if(this.getHeight() != height) {
            this.setHeight(height);
            changed = true;
        }
        if(changed) {
            sizeChanged();
            if (wrap) {
                workingLayout.setTargetWidth(width);
                font.regenerateLayout(workingLayout); // TODO: should this be calculateSize or regenerateLayout ?
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
        float width = getWidth();
        if (style != null && style.background != null) {
            width = (width - (style.background.getLeftWidth() + style.background.getRightWidth()));
        }
        float actualWidth = font.calculateSize(workingLayout);
        if (wrap && (width == 0f || workingLayout.getTargetWidth() != width || actualWidth > width)) {
            if(width != 0f)
                workingLayout.setTargetWidth(width);
            font.regenerateLayout(workingLayout);
            invalidateHierarchy();
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

        float height = workingLayout.getHeight();
        if (Align.isBottom(align)) {
            baseX -= sn * height;
            baseY += cs * height;
        } else if (Align.isCenterVertical(align)) {
            baseX -= sn * height * 0.5f;
            baseY += cs * height * 0.5f;
        }
        float width = getWidth();
        height = getHeight();
        if (Align.isRight(align)) {
            baseX += cs * width;
            baseY += sn * width;
        } else if (Align.isCenterHorizontal(align)) {
            baseX += cs * width * 0.5f;
            baseY += sn * width * 0.5f;
        }

        if (Align.isTop(align)) {
            baseX -= sn * height;
            baseY += cs * height;
        } else if (Align.isCenterVertical(align)) {
            baseX -= sn * height * 0.5f;
            baseY += cs * height * 0.5f;
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
            ((TransformDrawable) background).draw(batch,
                    getX(), getY(),             // position
                    originX, originY,           // origin
                    getWidth(), getHeight(),    // size
                    1f, 1f,                     // scale
                    rot);                       // rotation
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
                         lim = Math.min(Math.min(rotations.size, offsets.size >> 1), sizing.size >> 1);
                         i < n && r < lim; i++, gi++) {
                        if (gi > end) break SELECTION_LINE;
                        long glyph = glyphs.glyphs.get(i);
                        if (font.family != null) f = font.family.connected[(int) (glyph >>> 16 & 15)];
                        if (f == null) f = font;
                        float descent = f.descent * f.scaleY;
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
                            if (reg != null && reg.offsetX < 0) {
                                float ox = reg.offsetX;
                                ox *= f.scaleX * ((glyph & ALTERNATE) != 0L ? 1f : ((glyph + 0x300000L >>> 20 & 15) + 1) * 0.25f);
                                if (ox < 0) {
                                    xChange -= cs * ox;
                                    yChange -= sn * ox;
                                }
                            }

                        }

                        if (f.kerning != null) {
                            kern = kern << 16 | (int) ((glyph = glyphs.glyphs.get(i)) & 0xFFFF);
                            float amt = f.kerning.get(kern, 0) * f.scaleX * ((glyph & ALTERNATE) != 0L ? 1f : ((glyph + 0x300000L >>> 20 & 15) + 1) * 0.25f);
                            xChange += cs * amt;
                            yChange += sn * amt;
                        } else {
                            kern = -1;
                        }
                        ++globalIndex;
                        if (selectionEnd < globalIndex)
                            break;
                        float xx = x + xChange + offsets.get(o++), yy = y + yChange + offsets.get(o++);
                        if (font.integerPosition) {
                            xx = (int) xx;
                            yy = (int) yy;
                        }

                        float scale = (glyph & ALTERNATE) != 0L ? 1f : ((glyph + 0x300000L >>> 20 & 15) + 1) * 0.25f, scaleX;
                        if ((char) glyph >= 0xE000 && (char) glyph < 0xF800) {
                            scaleX = scale * font.cellHeight / (f.mapping.get((int) glyph & 0xFFFF, f.defaultValue).xAdvance);
                        } else
                            scaleX = font.scaleX * scale * (1f + 0.5f * (-(glyph & Font.SUPERSCRIPT) >> 63));

                        single = Font.xAdvance(f, scaleX, glyph);
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
        gi = 0;
        globalIndex = startIndex - 1;

        EACH_LINE:
        for (int ln = 0; ln < lines; ln++) {
            Line glyphs = workingLayout.getLine(ln);

            if(glyphs.glyphs.size == 0 || (toSkip += glyphs.glyphs.size) < startIndex)
                continue;

            baseX += sn * glyphs.height;
            baseY -= cs * glyphs.height;

            float x = baseX, y = baseY;

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
                 lim = Math.min(Math.min(rotations.size, offsets.size >> 1), sizing.size >> 1);
                 i < n && r < lim; i++, gi++) {
                if (gi > end) break EACH_LINE;
                long glyph = glyphs.glyphs.get(i);
                if (font.family != null) f = font.family.connected[(int) (glyph >>> 16 & 15)];
                if (f == null) f = font;
                float descent = f.descent * f.scaleY;
                if(i == start){
                    x -= f.cellWidth * 0.5f;

                    x += cs * f.cellWidth * 0.5f;
                    y += sn * f.cellWidth * 0.5f;

//                    x += sn * descent * 0.5f;
//                    y -= cs * descent * 0.5f;

                    y += descent;
                    x += sn * (descent - 0.5f * glyphs.height);
                    y -= cs * (descent - 0.5f * glyphs.height);

                    Font.GlyphRegion reg = font.mapping.get((char) glyph);
                    if (reg != null && reg.offsetX < 0) {
                        float ox = reg.offsetX;
                        ox *= f.scaleX * ((glyph & ALTERNATE) != 0L ? 1f : ((glyph + 0x300000L >>> 20 & 15) + 1) * 0.25f);
                        if (ox < 0) {
                            xChange -= cs * ox;
                            yChange -= sn * ox;
                        }
                    }

                }

                if (f.kerning != null) {
                    kern = kern << 16 | (int) ((glyph = glyphs.glyphs.get(i)) & 0xFFFF);
                    float amt = f.kerning.get(kern, 0) * f.scaleX * ((glyph & ALTERNATE) != 0L ? 1f : ((glyph + 0x300000L >>> 20 & 15) + 1) * 0.25f);
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
                float xx = x + xChange + offsets.get(o++), yy = y + yChange + offsets.get(o++);
                if(font.integerPosition){
                    xx = (int)xx;
                    yy = (int)yy;
                }

                single = f.drawGlyph(batch, glyph, xx, yy, rotations.get(r++) + rot, sizing.get(s++), sizing.get(s++), bgc);
                if(trackingInput){
                    if(xx <= inX && inX <= xx + single && yy - glyphs.height * 0.5f <= inY && inY <= yy + glyphs.height * 0.5f) {
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
     * used as the ideal text before wrapping or other requirements edit it.
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
     * (inclusive) to end (exclusive). This can retrieve text from across multiple lines.
     * @param start inclusive start index
     * @param end exclusive end index
     * @return a String made of only the char portions of the glyphs from start to end
     */
    public String substring(int start, int end) {
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
     * @param selectable true if the text of this label should be selectable
     * @return this, for chaining
     */
    public TypingLabel setSelectable(boolean selectable) {
        this.selectable = selectable;
        this.trackingInput |= selectable;
        return this;
    }

    public float getTextSpeed() {
        return textSpeed;
    }

    public void setTextSpeed(float textSpeed) {
        this.textSpeed = textSpeed;
//        this.charCooldown = textSpeed;
    }


}
