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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.IntFloatMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.github.tommyettinger.textra.effects.*;

/**
 * Configuration class that easily allows the user to fine tune the library's functionality.
 */
public class TypingConfig {

    /**
     * Default time in seconds that an empty {@code WAIT} token should wait for. Default value is {@code 0.250}.
     */
    public static float DEFAULT_WAIT_VALUE = 0.250f;

    /**
     * Time in seconds that takes for each char to appear in the default speed. Default value is {@code 0.05}.
     */
    public static float DEFAULT_SPEED_PER_CHAR = 0.05f;

    /**
     * Minimum value for the {@code SPEED} token. This value divides {@link #DEFAULT_SPEED_PER_CHAR} to calculate the
     * final speed. Keep it above zero. Default value is {@code 0.001}.
     */
    public static float MIN_SPEED_MODIFIER = 0.001f;

    /**
     * Maximum value for the {@code SPEED} token. This value divides {@link #DEFAULT_SPEED_PER_CHAR} to calculate the
     * final speed. Default value is {@code 100}.
     */
    public static float MAX_SPEED_MODIFIER = 100.0f;

    /**
     * Defines how many chars can appear per frame. Use a value less than {@code 1} to disable this limit. Default value
     * is {@code -1}.
     */
    public static int CHAR_LIMIT_PER_FRAME = -1;

    /**
     * Default color for the {@code CLEARCOLOR} token. Can be overridden by {@link TypingLabel#getClearColor()}.
     */
    public static Color DEFAULT_CLEAR_COLOR = new Color(Color.WHITE);

    /**
     * Returns a map of characters and their respective interval multipliers, of which the interval to the next char
     * should be multiplied for. The keys are technically primitive ints, but only a char is used for each key.
     */
    public static IntFloatMap INTERVAL_MULTIPLIERS_BY_CHAR = new IntFloatMap();

    /**
     * Map of global variables that affect all {@link TypingLabel} instances at once.
     */
    public static final ObjectMap<String, String> GLOBAL_VARS = new ObjectMap<>();

    /**
     * Defines several default variables with names in {@code ALLCAPS} with no separators, all of them creating effects
     * with complex parameters or combining multiple effects. The variables this defines:
     * <ul>
     *     <li><code>{VAR=FIRE}</code> changes the following text to have fiery changing colors. You can end it with
     *     <code>{VAR=ENDFIRE}</code>.</li>
     *     <li><code>{VAR=SPUTTERINGFIRE}</code> changes the following text to have fiery changing colors and resize
     *     like popping flames. You can end it with <code>{VAR=ENDSPUTTERINGFIRE}</code>.</li>
     *     <li><code>{VAR=BLIZZARD}</code> changes the following text to waver in the wind and use icy colors,
     *     white to light blue. You can end it with <code>{VAR=ENDBLIZZARD}</code>.</li>
     *     <li><code>{VAR=SHIVERINGBLIZZARD}</code> changes the following text to waver in the wind and use icy
     *     colors, white to light blue, plus it will randomly make glyphs "shiver" as if cold. You can end it with
     *     <code>{VAR=ENDSHIVERINGBLIZZARD}</code>.</li>
     *     <li><code>{VAR=ELECTRIFY}</code> changes the following text to be a dull gray purple color and randomly
     *     makes glyphs turn light yellow and vibrate around. You can end it with <code>{VAR=ENDELECTRIFY}</code>.</li>
     *     <li><code>{VAR=ZOMBIE}</code> changes the following text to be "dark olive sage" (a dull gray-green
     *     color), makes glyphs rotate left and right slowly and randomly, makes glyphs drop down and get back up
     *     randomly, and when they first appear, has the glyphs emerge from the baseline (as if clawing out of a grave).
     *     You can end it with <code>{VAR=ENDZOMBIE}</code>.</li>
     * </ul>
     */
    public static void initializeGlobalVars() {
        TypingConfig.GLOBAL_VARS.put("FIRE", "{OCEAN=0.7;1.25;0.11;1.0;0.65}");
        TypingConfig.GLOBAL_VARS.put("ENDFIRE", "{ENDOCEAN}");
        TypingConfig.GLOBAL_VARS.put("SPUTTERINGFIRE", "{OCEAN=0.7;1.25;0.11;1.0;0.65}{SPUTTER=0.2;0.25;4;inf}");
        TypingConfig.GLOBAL_VARS.put("ENDSPUTTERINGFIRE", "{ENDOCEAN}{ENDSPUTTER}");
        TypingConfig.GLOBAL_VARS.put("BLIZZARD", "{GRADIENT=88ccff;eef8ff;-0.5;5}{WIND=2;4;0.25;0.1}");
        TypingConfig.GLOBAL_VARS.put("ENDBLIZZARD", "{ENDGRADIENT}{ENDWIND}");
        TypingConfig.GLOBAL_VARS.put("SHIVERINGBLIZZARD", "{GRADIENT=88ccff;eef8ff;-0.5;5}{WIND=2;4;0.25;0.1}{JOLT=1;0.6;inf;0.1;;}");
        TypingConfig.GLOBAL_VARS.put("ENDSHIVERINGBLIZZARD", "{ENDGRADIENT}{ENDWIND}{ENDJOLT}");
        TypingConfig.GLOBAL_VARS.put("ELECTRIFY", "{JOLT=1;1.2;inf;0.3;dull lavender;light butter}");
        TypingConfig.GLOBAL_VARS.put("ENDELECTRIFY", "{ENDJOLT}");
        TypingConfig.GLOBAL_VARS.put("ZOMBIE", "{SICK=0.4}{CROWD}{EMERGE=0.1}[dark olive sage]");
        TypingConfig.GLOBAL_VARS.put("ENDZOMBIE", "{ENDSICK}{ENDCROWD}{ENDEMERGE}{CLEARCOLOR}");
    }
    /**
     * Map of start tokens and their effect classes. Internal use only.
     */
    static final OrderedMap<String, Effect.EffectBuilder> EFFECT_START_TOKENS = new OrderedMap<>();

    /**
     * Map of end tokens and their effect classes. Internal use only.
     */
    static final OrderedMap<String, Effect.EffectBuilder> EFFECT_END_TOKENS = new OrderedMap<>();

    /**
     * Whether effect tokens are dirty and need to be recalculated.
     */
    static boolean dirtyEffectMaps = true;

    /**
     * Registers a new effect to TypingLabel.
     *
     * @param startTokenName Name of the token that starts the effect, such as WAVE.
     * @param builder        Typically a lambda or method reference that creates an Effect.
     */
    public static void registerEffect(String startTokenName, Effect.EffectBuilder builder) {
        final String name = startTokenName.toUpperCase();
        final Effect.EffectBuilder b = (label, params) -> builder.produce(label, params).assignTokenName(name);
        EFFECT_START_TOKENS.put(name, b);
        EFFECT_END_TOKENS.put("END"+name, b);
        dirtyEffectMaps = true;
    }

    /**
     * Unregisters an effect from TypingLabel.
     *
     * @param startTokenName Name of the token that starts the effect, such as WAVE.
     */
    public static void unregisterEffect(String startTokenName) {
        String name = startTokenName.toUpperCase();
        EFFECT_START_TOKENS.remove(name);
        EFFECT_END_TOKENS.remove("END"+name);
        dirtyEffectMaps = true;
    }

    static {
        // Generate default char intervals
        INTERVAL_MULTIPLIERS_BY_CHAR.put(' ', 0.0f);
        INTERVAL_MULTIPLIERS_BY_CHAR.put(':', 1.5f);
        INTERVAL_MULTIPLIERS_BY_CHAR.put(',', 2.5f);
        INTERVAL_MULTIPLIERS_BY_CHAR.put('.', 2.5f);
        INTERVAL_MULTIPLIERS_BY_CHAR.put('!', 5.0f);
        INTERVAL_MULTIPLIERS_BY_CHAR.put('?', 5.0f);
        INTERVAL_MULTIPLIERS_BY_CHAR.put('\n', 2.5f);

        // Register default tokens
        registerEffect("EASE", EaseEffect::new);
        registerEffect("HANG", HangEffect::new);
        registerEffect("JUMP", JumpEffect::new);
        registerEffect("SHAKE", ShakeEffect::new);
        registerEffect("SICK", SickEffect::new);
        registerEffect("SLIDE", SlideEffect::new);
        registerEffect("WAVE", WaveEffect::new);
        registerEffect("WIND", WindEffect::new);
        registerEffect("RAINBOW", RainbowEffect::new);
        registerEffect("GRADIENT", GradientEffect::new);
        registerEffect("FADE", FadeEffect::new);
        registerEffect("BLINK", BlinkEffect::new);
        registerEffect("JOLT", JoltEffect::new);
        registerEffect("SPIRAL", SpiralEffect::new);
        registerEffect("SPIN", SpinEffect::new);
        registerEffect("CROWD", CrowdEffect::new);
        registerEffect("SHRINK", ShrinkEffect::new);
        registerEffect("EMERGE", EmergeEffect::new);
        registerEffect("HEARTBEAT", HeartbeatEffect::new);
        registerEffect("CAROUSEL", CarouselEffect::new);
        registerEffect("SQUASH", SquashEffect::new);
        registerEffect("SCALE", ScaleEffect::new);
        registerEffect("ROTATE", RotateEffect::new);
        registerEffect("HIGHLIGHT", HighlightEffect::new);
        registerEffect("LINK", LinkEffect::new);
        registerEffect("TRIGGER", TriggerEffect::new);
        registerEffect("ATTENTION", AttentionEffect::new);
        registerEffect("STYLIST", StylistEffect::new);
        registerEffect("CANNON", CannonEffect::new);
        registerEffect("OCEAN", OceanEffect::new);
        registerEffect("SPUTTER", SputterEffect::new);
        registerEffect("INSTANT", InstantEffect::new);
        registerEffect("SLAM", SlamEffect::new);
        registerEffect("MEET", MeetEffect::new);

        initializeGlobalVars();
    }
}
