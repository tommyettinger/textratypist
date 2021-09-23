package com.github.tommyettinger.textra;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.GlyphLayout.GlyphRun;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.reflect.ClassReflection;

import java.lang.StringBuilder;
import java.util.Map;

/**
 * An extension of {@link Label} that progressively shows the text as if it was being typed in real time, and allows the
 * use of tokens in the following format: <tt>{TOKEN=PARAMETER}</tt>.
 */
public class TypingLabel extends TextraLabel {
    ///////////////////////
    /// --- Members --- ///
    ///////////////////////

    // Collections
    private final   ObjectMap<String, String> variables    = new ObjectMap<String, String>();
    protected final Array<TokenEntry>         tokenEntries = new Array<TokenEntry>();

    // Config
    private final Color    clearColor = new Color(TypingConfig.DEFAULT_CLEAR_COLOR);
    private TypingListener listener   = null;
    boolean forceMarkupColor = TypingConfig.FORCE_COLOR_MARKUP_BY_DEFAULT;

    // Internal state
    private final StringBuilder      originalText          = new StringBuilder();
    private final StringBuilder      intermediateText      = new StringBuilder();
    private final Layout             workingLayout         = Pools.obtain(Layout.class);
    private final IntArray           lineCapacities        = new IntArray();
    private final FloatArray         offsets               = new FloatArray();
    private final IntArray           layoutLineBreaks      = new IntArray();
    private final Array<Effect>      activeEffects         = new Array<Effect>();
    private       float              textSpeed             = TypingConfig.DEFAULT_SPEED_PER_CHAR;
    private       float              charCooldown          = textSpeed;
    private       int                rawCharIndex          = -2; // All chars, including color codes
    private       int                glyphCharIndex        = -1; // Only renderable chars, excludes color codes
    private       int                glyphCharCompensation = 0;
    private       int                cachedGlyphCharIndex  = -1; // Last glyphCharIndex sent to the cache
    private       float              lastLayoutX           = 0;
    private       float              lastLayoutY           = 0;
    private       boolean            parsed                = false;
    private       boolean            paused                = false;
    private       boolean            ended                 = false;
    private       boolean            skipping              = false;
    private       boolean            ignoringEvents        = false;
    private       boolean            ignoringEffects       = false;
    private       String             defaultToken          = "";

    // Superclass mirroring
    boolean wrap;
    String  ellipsis;
    float   lastPrefHeight;
    boolean fontScaleChanged = false;

    ////////////////////////////
    /// --- Constructors --- ///
    ////////////////////////////

    public TypingLabel() {
        super();
        saveOriginalText("");
    }

    public TypingLabel(String text, Skin skin) {
        super(text, skin);
        saveOriginalText(text);
    }

    public TypingLabel(String text, Skin skin, String styleName) {
        super(text, skin, styleName);
        saveOriginalText(text);
    }

    public TypingLabel(String text, Label.LabelStyle style) {
        super(text, style);
        saveOriginalText(text);
    }

    public TypingLabel(String text, Font font) {
        super(text, font);
        saveOriginalText(text);
    }

    public TypingLabel(String text, Font font, Color color) {
        super(text, font, color);
        saveOriginalText(text);
    }

    /////////////////////////////
    /// --- Text Handling --- ///
    /////////////////////////////

    /**
     * Modifies the text of this label. If the char progression is already running, it's highly recommended to use
     * {@link #restart(String)} instead.
     */
    @Override
    public void setText(String newText) {
        this.setText(newText, true);
    }

    /**
     * Sets the text of this label.
     *
     * @param modifyOriginalText Flag determining if the original text should be modified as well. If {@code false},
     *                           only the display text is changed while the original text is untouched.
     * @see #restart(String)
     */
    protected void setText(String newText, boolean modifyOriginalText) {
        setText(newText, modifyOriginalText, true);
    }

    /**
     * Sets the text of this label.
     *
     * @param modifyOriginalText Flag determining if the original text should be modified as well. If {@code false},
     *                           only the display text is changed while the original text is untouched.
     * @param restart            Whether or not this label should restart. Defaults to true.
     * @see #restart(String)
     */
    protected void setText(String newText, boolean modifyOriginalText, boolean restart) {
        final boolean hasEnded = this.hasEnded();
        super.setText(newText);
        if(modifyOriginalText) saveOriginalText(newText);
        if(restart) {
            this.restart();
        }
        if(hasEnded) {
            this.skipToTheEnd(true, false);
        }
    }

    /** Similar to {@link Layout#toString()}, but returns the original text with all the tokens unchanged. */
    public StringBuilder getOriginalText() {
        return originalText;
    }

    /**
     * Copies the content of {@link #getOriginalText()} to the {@link StringBuilder} containing the original
     * text with all tokens unchanged.
     */
    protected void saveOriginalText(CharSequence text) {
        originalText.setLength(0);
        originalText.insert(0, text);
        originalText.trimToSize();
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

    /** Returns the {@link TypingListener} associated with this label. May be {@code null}. */
    public TypingListener getTypingListener() {
        return listener;
    }

    /** Sets the {@link TypingListener} associated with this label, or {@code null} to remove the current one. */
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
     * Sets whether or not this instance should enable markup color by force.
     *
     * @see TypingConfig#FORCE_COLOR_MARKUP_BY_DEFAULT
     */
    public void setForceMarkupColor(boolean forceMarkupColor) {
        this.forceMarkupColor = forceMarkupColor;
    }

    /** Returns the default token being used in this label. Defaults to empty string. */
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

    /** Parses all tokens of this label. Use this after setting the text and any variables that should be replaced. */
    public void parseTokens() {
        this.setText(getDefaultToken() + originalText, false, false);
        Parser.parseTokens(this);
        parsed = true;
    }

    /**
     * Skips the char progression to the end, showing the entire label. Useful for when users don't want to wait for too
     * long. Ignores all subsequent events by default.
     */
    public void skipToTheEnd() {
        skipToTheEnd(true);
    }

    /**
     * Skips the char progression to the end, showing the entire label. Useful for when users don't want to wait for too
     * long.
     *
     * @param ignoreEvents If {@code true}, skipped events won't be reported to the listener.
     */
    public void skipToTheEnd(boolean ignoreEvents) {
        skipToTheEnd(ignoreEvents, false);
    }

    /**
     * Skips the char progression to the end, showing the entire label. Useful for when users don't want to wait for too
     * long.
     *
     * @param ignoreEvents  If {@code true}, skipped events won't be reported to the listener.
     * @param ignoreEffects If {@code true}, all text effects will be instantly cancelled.
     */
    public void skipToTheEnd(boolean ignoreEvents, boolean ignoreEffects) {
        skipping = true;
        ignoringEvents = ignoreEvents;
        ignoringEffects = ignoreEffects;
    }

    /**
     * Cancels calls to {@link #skipToTheEnd()}. Useful if you need to restore the label's normal behavior at some event
     * after skipping.
     */
    public void cancelSkipping() {
        if(skipping) {
            skipping = false;
            ignoringEvents = false;
            ignoringEffects = false;
        }
    }

    /**
     * Returns whether or not this label is currently skipping its typing progression all the way to the end. This is
     * only true if skipToTheEnd is called.
     */
    public boolean isSkipping() {
        return skipping;
    }

    /** Returns whether or not this label is paused. */
    public boolean isPaused() {
        return paused;
    }

    /** Pauses this label's character progression. */
    public void pause() {
        paused = true;
    }

    /** Resumes this label's character progression. */
    public void resume() {
        paused = false;
    }

    /** Returns whether or not this label's char progression has ended. */
    public boolean hasEnded() {
        return ended;
    }

    /**
     * Restarts this label with the original text and starts the char progression right away. All tokens are
     * automatically parsed.
     */
    public void restart() {
        restart(getOriginalText().toString());
    }

    /**
     * Restarts this label with the given text and starts the char progression right away. All tokens are automatically
     * parsed.
     */
    public void restart(String newText) {
        // Reset cache collections
        Pools.free(workingLayout);
        lineCapacities.clear();
        offsets.clear();
        layoutLineBreaks.clear();
        activeEffects.clear();

        // Reset state
        textSpeed = TypingConfig.DEFAULT_SPEED_PER_CHAR;
        charCooldown = textSpeed;
        rawCharIndex = -2;
        glyphCharIndex = -1;
        glyphCharCompensation = 0;
        cachedGlyphCharIndex = -1;
        lastLayoutX = 0;
        lastLayoutY = 0;
        parsed = false;
        paused = false;
        ended = false;
        skipping = false;
        ignoringEvents = false;
        ignoringEffects = false;

        // Set new text
        this.setText(newText, true, false);
        invalidate();

        // Parse tokens
        tokenEntries.clear();
        parseTokens();
    }

    /** Returns an {@link ObjectMap} with all the variable names and their respective replacement values. */
    public ObjectMap<String, String> getVariables() {
        return variables;
    }

    /** Registers a variable and its respective replacement value to this label. */
    public void setVariable(String var, String value) {
        variables.put(var.toUpperCase(), value);
    }

    /** Registers a set of variables and their respective replacement values to this label. */
    public void setVariables(ObjectMap<String, String> variableMap) {
        this.variables.clear();
        for(Entry<String, String> entry : variableMap.entries()) {
            this.variables.put(entry.key.toUpperCase(), entry.value);
        }
    }

    /** Registers a set of variables and their respective replacement values to this label. */
    public void setVariables(Map<String, String> variableMap) {
        this.variables.clear();
        for(Map.Entry<String, String> entry : variableMap.entrySet()) {
            this.variables.put(entry.getKey().toUpperCase(), entry.getValue());
        }
    }

    /** Removes all variables from this label. */
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
        if(!parsed) {
            parseTokens();
        }

        // Update cooldown and process char progression
        if(skipping || (!ended && !paused)) {
            if(skipping || (charCooldown -= delta) < 0.0f) {
                processCharProgression();
            }
        }
//TODO: make drawing use a FloatArray of x offset and y offset pairs.

//        // Restore glyph offsets
//        if(activeEffects.size > 0) {
//            for(int i = 0; i < workingLayout.size; i++) {
//                TypingGlyph glyph = workingLayout.get(i);
//                glyph.xoffset = offsetCache.get(i * 2);
//                glyph.yoffset = offsetCache.get(i * 2 + 1);
//            }
//        }

        // Apply effects
        if(!ignoringEffects) {
            int workingLayoutSize = getLayoutSize(workingLayout);
            for(int i = activeEffects.size - 1; i >= 0; i--) {
                Effect effect = activeEffects.get(i);
                effect.update(delta);
                int start = effect.indexStart;
                int end = effect.indexEnd >= 0 ? effect.indexEnd : glyphCharIndex;

                // If effect is finished, remove it
                if(effect.isFinished()) {
                    activeEffects.removeIndex(i);
                    continue;
                }

                // Apply effect to glyph
                for(int j = Math.max(0, start); j <= glyphCharIndex && j <= end && j < workingLayoutSize; j++) {
                    long glyph = getInLayout(workingLayout, j);
                    if(glyph == 0xFFFF) break; // invalid char
                    effect.apply(glyph, j, delta);
                }
            }
        }
    }

    /** Proccess char progression according to current cooldown and process all tokens in the current index. */
    private void processCharProgression() {
        // Keep a counter of how many chars we're processing in this tick.
        int charCounter = 0;

        // Process chars while there's room for it
        while(skipping || charCooldown < 0.0f) {
            // Apply compensation to glyph index, if any
            if(glyphCharCompensation != 0) {
                if(glyphCharCompensation > 0) {
                    glyphCharIndex++;
                    glyphCharCompensation--;
                } else {
                    glyphCharIndex--;
                    glyphCharCompensation++;
                }

                // Increment cooldown and wait for it
                charCooldown += textSpeed;
                continue;
            }

            // Increase raw char index
            rawCharIndex++;

            // Get next character and calculate cooldown increment

//            LongArray glyphs = layout.getLine(0).glyphs;
            int layoutSize = getLayoutSize(layout);
            int safeIndex = MathUtils.clamp(rawCharIndex, 0, layoutSize - 1);
            long baseChar = 0L; // Null character by default
            if(layoutSize > 0) {
                baseChar = getInLayout(layout, safeIndex);
                float intervalMultiplier = TypingConfig.INTERVAL_MULTIPLIERS_BY_CHAR.get((char)baseChar, 1);
                charCooldown += textSpeed * intervalMultiplier;
            }

            // If char progression is finished, or if text is empty, notify listener and abort routine
            int textLen = layoutSize;
            if(textLen == 0 || rawCharIndex >= textLen) {
                if(!ended) {
                    ended = true;
                    skipping = false;
                    if(listener != null) listener.end();
                }
                return;
            }

            // Detect layout line breaks
            boolean isLayoutLineBreak = false;
            if(layoutLineBreaks.contains(glyphCharIndex)) {
                layoutLineBreaks.removeValue(glyphCharIndex);
                isLayoutLineBreak = true;
            }

            // Increase glyph char index for all characters, except new lines.
            if(rawCharIndex >= 0 && (char)baseChar != '\n' && !isLayoutLineBreak) glyphCharIndex++;

            // Process tokens according to the current index
            while(tokenEntries.size > 0 && tokenEntries.peek().index == rawCharIndex) {
                TokenEntry entry = tokenEntries.pop();
                String token = entry.token;
                TokenCategory category = entry.category;

                // Process tokens
                switch(category) {
                    case SPEED: {
                        textSpeed = entry.floatValue;
                        continue;
                    }
                    case WAIT: {
                        glyphCharIndex--;
                        glyphCharCompensation++;
                        charCooldown += entry.floatValue;
                        continue;
                    }
                    case SKIP: {
                        if(entry.stringValue != null) {
                            rawCharIndex += entry.stringValue.length();
                        }
                        continue;
                    }
                    case EVENT: {
                        if(this.listener != null && !ignoringEvents) {
                            listener.event(entry.stringValue);
                        }
                        continue;
                    }
                    case EFFECT_START:
                    case EFFECT_END: {
                        // Get effect class
                        boolean isStart = category == TokenCategory.EFFECT_START;
                        Class<? extends Effect> effectClass = isStart ? TypingConfig.EFFECT_START_TOKENS.get(token) : TypingConfig.EFFECT_END_TOKENS.get(token);

                        // End all effects of the same type
                        for(int i = 0; i < activeEffects.size; i++) {
                            Effect effect = activeEffects.get(i);
                            if(effect.indexEnd < 0) {
                                if(ClassReflection.isAssignableFrom(effectClass, effect.getClass())) {
                                    effect.indexEnd = glyphCharIndex - 1;
                                }
                            }
                        }

                        // Create new effect if necessary
                        if(isStart) {
                            entry.effect.indexStart = glyphCharIndex;
                            activeEffects.add(entry.effect);
                        }

                    }
                }
            }

            // Notify listener about char progression
            int nextIndex = rawCharIndex == 0 ? 0 : MathUtils.clamp(rawCharIndex, 0, layoutSize - 1);
            Character nextChar = nextIndex == 0 ? null : (char)getInLayout(layout, nextIndex);
            if(nextChar != null && listener != null) {
                listener.onChar(nextChar);
            }

            // Increment char counter
            charCounter++;

            // Break loop if this was our first glyph to prevent glyph issues.
            if(glyphCharIndex == -1) {
                charCooldown = textSpeed;
                break;
            }

            // Break loop if enough chars were processed
            charCounter++;
            int charLimit = TypingConfig.CHAR_LIMIT_PER_FRAME;
            if(!skipping && charLimit > 0 && charCounter > charLimit) {
                charCooldown = Math.max(charCooldown, textSpeed);
                break;
            }
        }
    }

    private int getLayoutSize(Layout layout) {
        int layoutSize = 0;
        for (int i = 0, n = layout.lines(); i < n; i++) {
            layoutSize += layout.getLine(i).glyphs.size;
        }
        return layoutSize;
    }

    @Override
    public boolean remove() {
        Pools.free(workingLayout);
        Pools.free(layout);
        return super.remove();
    }


    @Override
    public void layout() {
        // --- SUPERCLASS IMPLEMENTATION ---

        super.layout();

        // --- END OF SUPERCLASS IMPLEMENTATION ---

        lastLayoutX = getX();
        lastLayoutY = getY();

        // Perform cache layout operation, where the magic happens
        Pools.free(workingLayout);
        layoutCache();
    }

    /**
     * Reallocate glyph clones according to the updated {@link GlyphLayout}. This should only be called when the text or
     * the layout changes.
     */
    private void layoutCache() {
        Layout layout = super.layout;
        Array<Line> lines = layout.lines;

        // Reset layout line breaks
        layoutLineBreaks.clear();

        // Store GlyphRun sizes and count how many glyphs we have
        int glyphCount = 0;
        lineCapacities.setSize(lines.size);
        for(int i = 0; i < lines.size; i++) {
            int size = lines.get(i).glyphs.size;
            lineCapacities.set(i, size);
            glyphCount += size;
        }

        int workingLayoutSize = getLayoutSize(workingLayout);

        // Make sure our cache array can hold all glyphs
        if(workingLayoutSize < glyphCount) {
            offsets.setSize(glyphCount * 2);
        }

        // Clone original glyphs with independent instances
        int index = -1;
        float lastY = 0;
        for(int i = 0; i < lines.size; i++) {
            Line run = lines.get(i);
            LongArray glyphs = run.glyphs;
            for(int j = 0; j < glyphs.size; j++) {
                // Increment index
                index++;

                // Get original glyph
                long original = glyphs.get(j);

                // Store offset data
                offsets.set(index * 2, 0);
                offsets.set(index * 2 + 1, 0);
            }
        }

        // Remove exceeding glyphs from original array
        int glyphCountdown = glyphCharIndex;
        for(int i = 0; i < lines.size; i++) {
            LongArray glyphs = lines.get(i).glyphs;
            if(glyphs.size < glyphCountdown) {
                glyphCountdown -= glyphs.size;
                continue;
            }

            for(int j = 0; j < glyphs.size; j++) {
                if(glyphCountdown < 0) {
                    glyphs.removeRange(j, glyphs.size - 1);
                    break;
                }
                glyphCountdown--;
            }
        }

        layout.clear();
        layout.lines.addAll(workingLayout.lines);
    }

    /** Adds cached glyphs to the active BitmapFontCache as the char index progresses. */
    private void addMissingGlyphs() {
        // Add additional glyphs to layout array, if any
        int glyphLeft = glyphCharIndex - cachedGlyphCharIndex;
        if(glyphLeft < 1) return;

        // Get runs
        Layout layout = super.layout;
        Array<Line> lines = layout.lines;

        // Iterate through GlyphRuns to find the next glyph spot
        int glyphCount = 0;
        for(int lineIndex = 0; lineIndex < lineCapacities.size; lineIndex++) {
            int runCapacity = lineCapacities.get(lineIndex);
            if((glyphCount + runCapacity) < cachedGlyphCharIndex) {
                glyphCount += runCapacity;
                continue;
            }

            // Get run and increase glyphCount up to its current size
            LongArray glyphs = lines.get(lineIndex).glyphs;
            glyphCount += glyphs.size;

            // Next glyphs go here
            while(glyphLeft > 0) {

                // Skip run if this one is full
                int runSize = glyphs.size;
                if(runCapacity == runSize) {
                    break;
                }

                // Put new glyph to this run
                cachedGlyphCharIndex++;
                long glyph = getInLayout(workingLayout, cachedGlyphCharIndex);
                glyphs.add(glyph);

                // Advance glyph count
                glyphCount++;
                glyphLeft--;
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.validate();
        addMissingGlyphs();

        super.draw(batch, parentAlpha);
    }

    public void setIntermediateText(CharSequence text, boolean modifyOriginalText, boolean restart) {
        final boolean hasEnded = this.hasEnded();
        intermediateText.setLength(0);
        intermediateText.append(text);
        intermediateText.trimToSize();
        if(modifyOriginalText) saveOriginalText(text);
        if(restart) {
            this.restart();
        }
        if(hasEnded) {
            this.skipToTheEnd(true, false);
        }
    }

    public StringBuilder getIntermediateText() {
        return intermediateText;
    }

    public long getInLayout(Layout layout, int index){
        for (int i = 0, n = layout.lines(); i < n && index >= 0; i++) {
            LongArray glyphs = layout.getLine(i).glyphs;
            if(index < glyphs.size)
                return glyphs.get(index);
            else
                index -= glyphs.size;
        }
        return 0xFFFFL;
    }
}
