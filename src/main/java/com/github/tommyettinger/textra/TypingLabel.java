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

package com.github.tommyettinger.textra;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.reflect.ClassReflection;

import java.lang.StringBuilder;
import java.util.Arrays;
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

    // Internal state
    private final   StringBuilder    originalText          = new StringBuilder();
    private final   StringBuilder    intermediateText      = new StringBuilder();
    protected final Layout           workingLayout         = Layout.POOL.obtain();
    public final    FloatArray       offsets               = new FloatArray();
    private final   Array<Effect>    activeEffects         = new Array<Effect>();
    private         float            textSpeed             = TypingConfig.DEFAULT_SPEED_PER_CHAR;
    private         float            charCooldown          = textSpeed;
    private         int              rawCharIndex          = -2; // All chars, including color codes
    private         int              glyphCharIndex        = -1; // Only renderable chars, excludes color codes
    private         int              glyphCharCompensation = 0;
    private         int              cachedGlyphCharIndex  = -1; // Last glyphCharIndex sent to the cache
    private         boolean          parsed                = false;
    private         boolean          paused                = false;
    private         boolean          ended                 = false;
    private         boolean          skipping              = false;
    private         boolean          ignoringEvents        = false;
    private         boolean          ignoringEffects       = false;
    private         String           defaultToken          = "";

    ////////////////////////////
    /// --- Constructors --- ///
    ////////////////////////////

    public TypingLabel() {
        super();
        workingLayout.font(super.font);
        saveOriginalText("");
    }

    public TypingLabel(String text, Skin skin) {
        super(text = Parser.preprocess(text), skin);
        workingLayout.font(super.font);
        saveOriginalText(text);
    }

    public TypingLabel(String text, Skin skin, String styleName) {
        super(text = Parser.preprocess(text), skin, styleName);
        workingLayout.font(super.font);
        saveOriginalText(text);
    }

    public TypingLabel(String text, Label.LabelStyle style) {
        super(text = Parser.preprocess(text), style);
        workingLayout.font(super.font);
        saveOriginalText(text);
    }

    public TypingLabel(String text, Font font) {
        super(text = Parser.preprocess(text), font);
        workingLayout.font(font);
        saveOriginalText(text);
    }

    public TypingLabel(String text, Font font, Color color) {
        super(text = Parser.preprocess(text), font, color);
        workingLayout.font(font);
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
     *                           only the display text is changed while the original text is untouched. If {@code true},
     *                           then this runs {@link Parser#preprocess(CharSequence)} on the text, which should only
     *                           generally be run once per original text.
     * @see #restart(String)
     */
    protected void setText(String newText, boolean modifyOriginalText) {
        if(modifyOriginalText) newText = Parser.preprocess(newText);
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
//        newText = Parser.preprocess(newText);
        final boolean hasEnded = this.hasEnded();
        float actualWidth = layout.getWidth();
//        layout.setTargetWidth(Float.MAX_VALUE);
        workingLayout.setTargetWidth(actualWidth);
        font.markup(newText, layout.clear());
//        layout.setTargetWidth(actualWidth);
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
        if(text != originalText) {
            originalText.setLength(0);
            originalText.insert(0, text);
        }
        originalText.trimToSize();
    }

    /**
     * Restores the original text with all tokens unchanged to this label. Make sure to call {@link #parseTokens()} to
     * parse the tokens again.
     */
    protected void restoreOriginalText() {
//        float actualWidth = layout.getTargetWidth();
//        layout.setTargetWidth(Float.MAX_VALUE);
        super.setText(originalText.toString());
//        layout.setTargetWidth(actualWidth);
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
//        setSize(actualWidth, workingLayout.getHeight());
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
        workingLayout.baseColor = Color.WHITE_FLOAT_BITS;
        workingLayout.maxLines = Integer.MAX_VALUE;
        workingLayout.atLimit = false;
        workingLayout.ellipsis = null;
        Line.POOL.freeAll(workingLayout.lines);
        workingLayout.lines.clear();
        workingLayout.lines.add(Line.POOL.obtain());

        offsets.clear();
        activeEffects.clear();

        // Reset state
        textSpeed = TypingConfig.DEFAULT_SPEED_PER_CHAR;
        charCooldown = textSpeed;
        rawCharIndex = -2;
        glyphCharIndex = -1;
        glyphCharCompensation = 0;
        cachedGlyphCharIndex = -1;
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

    @Override
    public float getPrefWidth() {
        return wrap ? 0f : workingLayout.getWidth();
    }

    @Override
    public float getPrefHeight() {
        return workingLayout.getHeight() + font.cellHeight * 0.5f;
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
        int glyphCount = getLayoutSize(layout);
        offsets.setSize(glyphCount + glyphCount);
        Arrays.fill(offsets.items, 0, glyphCount + glyphCount, 0f);

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
                    if(glyph == 0xFFFFFFL) break; // invalid char
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

            int layoutSize = getLayoutSize(layout);
            int safeIndex = MathUtils.clamp(glyphCharIndex + 1, 0, layoutSize - 1);
            long baseChar; // Null character by default
            if(layoutSize > 0) {
                baseChar = getInLayout(layout, safeIndex);
                float intervalMultiplier = TypingConfig.INTERVAL_MULTIPLIERS_BY_CHAR.get((char)baseChar, 1);
                charCooldown += textSpeed * intervalMultiplier;
            }

            // If char progression is finished, or if text is empty, notify listener and abort routine
            int textLen = layoutSize;
            if(textLen == 0 || glyphCharIndex >= textLen) {
                if(!ended) {
                    ended = true;
                    skipping = false;
                    if(listener != null) listener.end();
                }
                return;
            }

            // Increase glyph char index for all characters
            if(rawCharIndex >= 0) {
                glyphCharIndex++;
            }

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
            int nextIndex = glyphCharIndex == 0 ? 0 : MathUtils.clamp(glyphCharIndex, 0, layoutSize - 1);
            Long nextChar = nextIndex == 0 ? null : getInLayout(layout, nextIndex);
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
        if (wrap) {
            font.regenerateLayout(workingLayout);
//            parseTokens();
        }
        else {
            for (Line ln : workingLayout.lines) {
                font.calculateSize(ln);
            }
        }

        invalidateHierarchy();
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
        Layout.POOL.free(workingLayout);
        Layout.POOL.free(layout);
        return super.remove();
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        if(wrap)
            workingLayout.setTargetWidth(width);
    }

    @Override
    public void layout() {
        // --- SUPERCLASS IMPLEMENTATION ---
        super.layout();

        float width = getWidth();
        if (wrap && (workingLayout.getTargetWidth() != width)) {
//            workingLayout.setTargetWidth(width);
            font.regenerateLayout(workingLayout);
//            parseTokens();
        }


        // --- END OF SUPERCLASS IMPLEMENTATION ---

//        for (int i = 0; i < workingLayout.lines(); i++) {
//            workingLayout.lines.get(i).glyphs.clear();
//        }
//        layoutCache();
    }

//    /**
//     * Ensures that there are enough offsets and that they are all initialized; also sets the line capacities.
//     * This should only be called when the text or the layout changes.
//     */
//    private void layoutCache() {
//        Array<Line> lines = workingLayout.lines;
//
//        // Remove exceeding glyphs from original array
//        int glyphCountdown = glyphCharIndex;
//        for(int i = 0; i < lines.size; i++) {
//            LongArray glyphs = lines.get(i).glyphs;
//            if(glyphs.size < glyphCountdown) {
//                glyphCountdown -= glyphs.size;
//                continue;
//            }
//
//            for(int j = 0; j < glyphs.size; j++) {
//                if(glyphCountdown < 0) {
//                    glyphs.removeRange(j, glyphs.size - 1);
//                    break;
//                }
//                glyphCountdown--;
//            }
//        }
//        // Store Line sizes and count how many glyphs we have
//        int glyphCount = 0;
//        for(int i = 0; i < layout.lines.size; i++) {
//            int size = layout.lines.get(i).glyphs.size;
//            glyphCount += size;
//        }
//        // Ensure there are enough x and y offset entries, and that they are all 0
//        offsets.setSize(glyphCount * 2);
//        Arrays.fill(offsets.items, 0, glyphCount * 2, 0f);
//    }

    /** Adds glyphs from layout to workingLayout as the char index progresses. */
    private void addMissingGlyphs() {
        // Add additional glyphs to layout array, if any
        int glyphLeft = glyphCharIndex - cachedGlyphCharIndex;
        if (glyphLeft < 1) return;
        // Next glyphs go here
        while (glyphLeft > 0) {

            // Put new glyph to this run
            cachedGlyphCharIndex++;
            long glyph = getInLayout(layout, cachedGlyphCharIndex);
            if (glyph != 0xFFFFFFL)
                workingLayout.add(glyph);
            // Advance glyph count
            glyphLeft--;
        }
    }

    /**
     * If your font uses {@link com.github.tommyettinger.textra.Font.DistanceFieldType#SDF} or {@link com.github.tommyettinger.textra.Font.DistanceFieldType#MSDF},
     * then this has to do some extra work to use the appropriate shader.
     * If {@link Font#enableShader(Batch)} was called before rendering a group of TypingLabels, then they will try to
     * share one Batch; otherwise this will change the shader to render SDF or MSDF, then change it back at the end of
     * each draw() call.
     * @param batch
     * @param parentAlpha
     */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.validate();
        boolean resetShader = font.distanceField != Font.DistanceFieldType.STANDARD && batch.getShader() != font.shader;
        if(resetShader)
            font.enableShader(batch);
        batch.setColor(1f, 1f, 1f, parentAlpha);
        final int lines = workingLayout.lines();
        float baseX = getX(align), baseY = getHeight() * 0.5f + getY(align);
        int o = 0;
        for (int ln = 0; ln < lines; ln++) {
            Line glyphs = workingLayout.getLine(ln);
            baseY -= glyphs.height;
            float x = baseX, y = baseY, drawn = 0;
            if(Align.isCenterHorizontal(align))
                x -= glyphs.width * 0.5f;
            else if(Align.isRight(align))
                x -= glyphs.width;
            if(font.kerning != null) {
                int kern = -1;
                float amt;
                long glyph;
                for (int i = 0, n = glyphs.glyphs.size; i < n; i++) {
                    kern = kern << 16 | (int) ((glyph = glyphs.glyphs.get(i)) & 0xFFFF);
                    amt = font.kerning.get(kern, 0) * font.scaleX;
                    float single = font.drawGlyph(batch, glyph, x + amt + offsets.get(o++), y + offsets.get(o++)) + amt;
                    x += single;
                    drawn += single;
                }
            }
            else {
                for (int i = 0, n = glyphs.glyphs.size; i < n && o < offsets.size - 1; i++) {
                    float single = font.drawGlyph(batch, glyphs.glyphs.get(i), x + offsets.get(o++), y + offsets.get(o++));
                    x += single;
                    drawn += single;
                }
            }
//            System.out.println("Line " + ln + " has width " + (drawn));
        }
        addMissingGlyphs();
        if(resetShader)
            batch.setShader(null);
    }

    @Override
    public String toString() {
        return workingLayout.toString();
    }

    public void setIntermediateText(CharSequence text, boolean modifyOriginalText, boolean restart) {
        final boolean hasEnded = this.hasEnded();
        if(text != intermediateText) {
            intermediateText.setLength(0);
            intermediateText.append(text);
        }
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
        return 0xFFFFFFL;
    }

    public Line getLineInLayout(Layout layout, int index){
        for (int i = 0, n = layout.lines(); i < n && index >= 0; i++) {
            LongArray glyphs = layout.getLine(i).glyphs;
            if(index < glyphs.size)
                return layout.getLine(i);
            else
                index -= glyphs.size;
        }
        return null;
    }

    public float getLineHeight(int index){
        for (int i = 0, n = workingLayout.lines(); i < n && index >= 0; i++) {
            LongArray glyphs = workingLayout.getLine(i).glyphs;
            if(index < glyphs.size)
                return workingLayout.getLine(i).height;
            else
                index -= glyphs.size;
        }
        return font.cellHeight;
    }

    public long getFromIntermediate(int index){
        if(index >= 0 && intermediateText.length() > index) return intermediateText.charAt(index);
        else return 0xFFFFFFL;
    }

    public void setInLayout(Layout layout, int index, long newGlyph){
        for (int i = 0, n = layout.lines(); i < n && index >= 0; i++) {
            LongArray glyphs = layout.getLine(i).glyphs;
            if(index < glyphs.size) {
                glyphs.set(index, newGlyph);
                return;
            }
            else
                index -= glyphs.size;
        }
    }

    public void setInWorkingLayout(int index, long newGlyph){
        for (int i = 0, n = layout.lines(); i < n && index >= 0; i++) {
            LongArray glyphs = workingLayout.getLine(i).glyphs;
            if(i < workingLayout.lines() && index < glyphs.size) {
                glyphs.set(index, newGlyph);
                return;
//            LongArray glyphs = layout.getLine(i).glyphs;
//            if(index < glyphs.size) {
//                char old = (char) glyphs.get(index);
//                glyphs.set(index, newGlyph);
//                if(i < workingLayout.lines() && index < workingLayout.getLine(i).glyphs.size) {
//                    char work;
//                    if((work = (char)workingLayout.getLine(i).glyphs.get(index)) != old)
//                        System.out.println("Different: " + old + " ! => " + work + " at index " + index);
//                    workingLayout.getLine(i).glyphs.set(index, newGlyph);
//                }
//                return;
            }
            else
                index -= glyphs.size;
        }
    }
}
