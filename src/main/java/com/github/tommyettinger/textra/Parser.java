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

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.github.tommyettinger.textra.utils.CaseInsensitiveIntMap;
import com.github.tommyettinger.textra.utils.Palette;
import com.github.tommyettinger.textra.utils.StringUtils;
import regexodus.Matcher;
import regexodus.Pattern;
import regexodus.REFlags;
import regexodus.Replacer;

import java.util.Collections;

/**
 * Utility class to parse tokens from a {@link TypingLabel}; not intended for external use in most situations.
 */
public class Parser {
    private static final Pattern PATTERN_MARKUP_STRIP = Pattern.compile("((?<!\\[)\\[[^\\[\\]]*(\\]))");
    private static final Matcher MATCHER_MARKUP_STRIP = PATTERN_MARKUP_STRIP.matcher();
    private static final Replacer RESET_TAG = new Replacer(Pattern.compile("((?<!\\[)\\[ (?:\\]))"), "{RESET}");
    private static final Replacer UNDO_TAG =  new Replacer(Pattern.compile("((?<!\\[)\\[(?:\\]))"), "{UNDO}");
    private static final Replacer COLOR_MARKUP_TO_TAG = new Replacer(Pattern.compile("(?<!\\[)\\[(?:(?:#({=m}[A-Fa-f0-9]{3,8}))|(?:\\|?({=m}[\\pL\\pN][^\\[\\]]*)))(\\])"), "{COLOR=${\\m}}");
    private static final Replacer MARKUP_TO_TAG = new Replacer(Pattern.compile("(?<!\\[)\\[([^\\[\\]\\+][^\\[\\]]*)(\\])"), "{STYLE=$1}");
    private static final Pattern PATTERN_COLOR_HEX_NO_HASH = Pattern.compile("[A-Fa-f0-9]{3,8}");
    private static final Matcher MATCHER_COLOR_HEX_NO_HASH = PATTERN_COLOR_HEX_NO_HASH.matcher();

    private static final Replacer BRACKET_MINUS_TO_TAG = new Replacer(Pattern.compile("((?<!\\[)\\[-({=t}[^\\[\\]]*)(?:\\]))"), "{${\\t}}");

    private static final CaseInsensitiveIntMap BOOLEAN_TRUE = new CaseInsensitiveIntMap(new String[]{"true", "yes", "t", "y", "on", "1"}, new int[6]);
    private static final int INDEX_TOKEN = 1;
    private static final int INDEX_PARAM = 2;

    private static Pattern PATTERN_TOKEN_STRIP;
    private static Matcher MATCHER_TOKEN_STRIP;
    private static Matcher MATCHER_TOKEN_STRIP_2;
    private static String RESET_REPLACEMENT;

    /**
     * Replaces any square-bracket markup of the form {@code [-SOMETHING]} with the curly-brace tag form
     * <code>{SOMETHING}</code>. This allows you to produce curly-brace tags even when curly braces have some meaning
     * that is reserved by another tool, such as I18N bundles. As long as the other tool somehow consumes curly braces
     * from the source text, this can bring back curly braces before {@link Font#markup(String, Layout)} gets called.
     * From then on, something like {@code [-RAINBOW]} will effectively be <code>{RAINBOW}</code>.
     * <br>
     * If this finds no occurrence of {@code "[-"} in {@code text}, this simply returns {@code text} without allocating.
     *
     * @param text text that could have square-bracket-minus markup
     * @return {@code text} with any square-bracket-minus markup changed to curly-brace tags
     */
    public static String handleBracketMinusMarkup(String text) {
        if(text.contains("[-"))
            return BRACKET_MINUS_TO_TAG.replace(text);
        return text;
    }

    /**
     * Replaces any style markup using square brackets, such as <code>[_]</code> for underline, with the syntax
     * <code>{STYLE=_}</code> (changing {@code _} based on what was in the square brackets). This also changes
     * <code>[ ]</code> to <code>{RESET}</code> and <code>[]</code> to <code>{UNDO}</code>. This won't change escaped
     * brackets or the inline image syntax that uses <code>[+name of an image in an atlas]</code>.
     * @param text text that could have square-bracket style markup
     * @return {@code text} with square bracket style markup changed to curly-brace style tags
     */
    public static String preprocess(String text) {
        text = RESET_TAG.replace(text);
        text = UNDO_TAG.replace(text);
        text = COLOR_MARKUP_TO_TAG.replace(text);
        text = MARKUP_TO_TAG.replace(text);
        return text;
    }

    /**
     * Parses all tokens from the given {@link TypingLabel}.
     */
    public static void parseTokens(TypingLabel label) {
        // Compile patterns if necessary
        if (PATTERN_TOKEN_STRIP == null || TypingConfig.dirtyEffectMaps) {
            PATTERN_TOKEN_STRIP = compileTokenPattern();
            MATCHER_TOKEN_STRIP = PATTERN_TOKEN_STRIP.matcher();
            MATCHER_TOKEN_STRIP_2 = PATTERN_TOKEN_STRIP.matcher();
        }
        if (RESET_REPLACEMENT == null || TypingConfig.dirtyEffectMaps) {
            RESET_REPLACEMENT = getResetReplacement();
        }

        // Remove any previous entries
        label.tokenEntries.clear();

        // Parse all tokens with text replacements, namely color and var.
        parseReplacements(label);

        // Parse all regular tokens and properly register them
        parseRegularTokens(label);

        // Parse color markups and register SKIP tokens
//        parseColorMarkups(label);

        label.setText(label.getIntermediateText().toString(), false, false);

        // Sort token entries
        Collections.sort(label.tokenEntries);
//        label.tokenEntries.reverse();
    }

    /**
     * Parse tokens that only replace text, such as colors and variables.
     */
    private static void parseReplacements(TypingLabel label) {
        // Get text
        CharSequence text = label.layout.appendIntoDirect(new StringBuilder());

        if(label.font.omitCurlyBraces || label.font.enableSquareBrackets) {
            // Create string builder
            MATCHER_TOKEN_STRIP.setTarget(text);
            Matcher m = MATCHER_TOKEN_STRIP;
            int matcherIndexOffset = 0;

            // Iterate through matches
            while (true) {
                // Reset StringBuilder and matcher
                m.setTarget(text);
                m.setPosition(matcherIndexOffset);

                // Make sure there's at least one regex match
                if (!m.find()) break;

                // Get token and parameter
                final InternalToken internalToken = InternalToken.fromName(m.group(INDEX_TOKEN));
                final String param = m.group(INDEX_PARAM);

                // If token couldn't be parsed, move one index forward to continue the search
                if (internalToken == null) {
                    matcherIndexOffset++;
                    continue;
                }

                // Process tokens and handle replacement
                String replacement;
                switch (internalToken) {
                    case COLOR:
                        replacement = stringToColorMarkup(param);
                        break;
                    case STYLE:
                    case SIZE:
                        replacement = stringToStyleMarkup(param);
                        break;
                    case FONT:
                        replacement = "[@" + param + ']';
                        break;
                    case ENDCOLOR:
                    case CLEARCOLOR:
                        replacement = "[#" + label.getClearColor().toString() + ']';
                        break;
                    case CLEARSIZE:
                        replacement = "[%]";
                        break;
                    case CLEARFONT:
                        replacement = "[@]";
                        break;
                    case VAR:
                        replacement = null;

                        // Try to replace variable through listener.
                        if (label.getTypingListener() != null) {
                            replacement = label.getTypingListener().replaceVariable(param);
                        }

                        // If replacement is null, get value from maps.
                        if (replacement == null) {
                            replacement = label.getVariables().get(param.toUpperCase());
                        }

                        // If replacement is still null, get value from global scope
                        if (replacement == null) {
                            replacement = TypingConfig.GLOBAL_VARS.get(param.toUpperCase());
                        }

                        // Make sure we're not inserting "null" to the text.
                        if (replacement == null) replacement = param.toUpperCase();
                        break;
                    case IF:
                        // Process token
                        replacement = processIfToken(label, param);

                        // Make sure we're not inserting "null" to the text.
                        if (replacement == null) replacement = param.toUpperCase();
                        break;
                    case RESET:
                        replacement = RESET_REPLACEMENT + label.getDefaultToken();
                        break;
                    case UNDO:
                        replacement = "[]";
                        break;
                    default:
                        // We don't want to process this token now. Move one index forward to continue the search
                        matcherIndexOffset++;
                        continue;
                }

                // Update text with replacement
                m.setPosition(m.start());
                text = m.replaceFirst(replacement);
            }
        }
        // Set new text
        label.setIntermediateText(text, false, false);
    }

    private static String processIfToken(TypingLabel label, String paramsString) {
        // Split params
        final String[] params = paramsString == null ? new String[0] : paramsString.split(";");
        final String variable = params.length > 0 ? params[0] : null;

        // Ensure our params are valid
        if(params.length <= 1 || variable == null) {
            return null;
        }

        /*
            Get variable's value
         */
        String variableValue = null;

        // Try to get value through listener.
        if(label.getTypingListener() != null) {
            variableValue = label.getTypingListener().replaceVariable(variable);
        }

        // If value is null, get it from maps.
        if(variableValue == null) {
            variableValue = label.getVariables().get(variable.toUpperCase());
        }

        // If value is still null, get it from global scope
        if(variableValue == null) {
            variableValue = TypingConfig.GLOBAL_VARS.get(variable.toUpperCase());
        }

        // Ensure variable is never null
        if(variableValue == null) {
            variableValue = "";
        }

        // Iterate through params and try to find a match
        String defaultValue = null;
        for(int i = 1, n = params.length; i < n; i++) {
            String[] subParams = params[i].split("=", 2);
            String key = subParams[0];
            String value = subParams[subParams.length - 1];
            boolean isKeyValid = subParams.length > 1 && !key.isEmpty();

            // If key isn't valid, it must be a default value. Store it and carry on
            if(!isKeyValid) {
                defaultValue = value;
                break;
            }

            // Compare variable's value with key
            if(variableValue.equalsIgnoreCase(key)) {
                return value;
            }
        }

        // Try to return any default values captured during the iteration
        if(defaultValue != null) {
            return defaultValue;
        }

        // If we got this far, no values matched our variable.
        // Return the variable itself, which might be useful for debugging.
        return variable;
    }

    /**
     * Parses regular tokens that don't need replacement and register their indexes in the {@link TypingLabel}.
     */
    private static void parseRegularTokens(TypingLabel label) {
        // Get text
        MATCHER_MARKUP_STRIP.setTarget(label.getIntermediateText());
        String text = MATCHER_MARKUP_STRIP.replaceAll("");
        CharSequence text2 = label.getIntermediateText();
        if(label.font.omitCurlyBraces || label.font.enableSquareBrackets) {
            // Create matcher and StringBuilder
            MATCHER_TOKEN_STRIP.setTarget(text);
            MATCHER_TOKEN_STRIP_2.setTarget(text2);
            Matcher m = MATCHER_TOKEN_STRIP;
            Matcher m2 = MATCHER_TOKEN_STRIP_2;
            int matcherIndexOffset = 0, m2IndexOffset = 0;

            // Iterate through matches
            while (true) {
                // Reset matcher and StringBuilder
                m.setTarget(text);
                m2.setTarget(text2);
                m2.setPosition(m2IndexOffset);
                m.setPosition(matcherIndexOffset);

                // Make sure there's at least one regex match
                if (!m.find()) break;
                m2.find();
                // Get token name and category
                String tokenName = m.group(INDEX_TOKEN).toUpperCase();
                TokenCategory tokenCategory = null;
                InternalToken tmpToken = InternalToken.fromName(tokenName);
                if (tmpToken == null) {
                    if (TypingConfig.EFFECT_START_TOKENS.containsKey(tokenName)) {
                        tokenCategory = TokenCategory.EFFECT_START;
                    } else if (TypingConfig.EFFECT_END_TOKENS.containsKey(tokenName)) {
                        tokenCategory = TokenCategory.EFFECT_END;
                    }
                } else {
                    tokenCategory = tmpToken.category;
                }

                // Get token, param and index of where the token begins
                int groupCount = m.groupCount();
                final String paramsString = groupCount == INDEX_PARAM ? m.group(INDEX_PARAM) : null;
                final String[] params = paramsString == null ? new String[0] : paramsString.split(";");
                final String firstParam = params.length > 0 ? params[0] : null;
                final int index = m.start(0);
                int indexOffset = 0;

                // If token couldn't be parsed, move one index forward to continue the search
                if (tokenCategory == null) {
                    matcherIndexOffset++;
                    continue;
                }

                // Process tokens
                float floatValue = 0;
                String stringValue = null;
                Effect effect = null;

                switch (tokenCategory) {
                    case WAIT: {
                        floatValue = stringToFloat(firstParam, TypingConfig.DEFAULT_WAIT_VALUE);
//                    indexOffset = 1;
                        break;
                    }
                    case EVENT: {
                        stringValue = paramsString;
//                    indexOffset = -1;
                        break;
                    }
                    case SPEED: {
                        switch (tokenName) {
                            case "SPEED": {
                                float minModifier = TypingConfig.MIN_SPEED_MODIFIER;
                                float maxModifier = TypingConfig.MAX_SPEED_MODIFIER;
                                float modifier = MathUtils.clamp(stringToFloat(firstParam, 1), minModifier, maxModifier);
                                floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR / modifier;
                                break;
                            }
                            case "SLOWER":
                                floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR * 2f;
                                break;
                            case "SLOW":
                                floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR * 1.5f;
                                break;
                            case "NORMAL":
                                floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR;
                                break;
                            case "FAST":
                                floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR * 0.5f;
                                break;
                            case "FASTER":
                                floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR * 0.25f;
                                break;
                            case "NATURAL": {
                                float minModifier = TypingConfig.MIN_SPEED_MODIFIER;
                                float maxModifier = TypingConfig.MAX_SPEED_MODIFIER;
                                float modifier = MathUtils.clamp(stringToFloat(firstParam, 1), minModifier, maxModifier);
                                floatValue = -TypingConfig.DEFAULT_SPEED_PER_CHAR / modifier;
                                break;
                            }
                        }
                        break;
                    }
                    case EFFECT_START: {
                        Effect.EffectBuilder eb = TypingConfig.EFFECT_START_TOKENS.get(tokenName.toUpperCase());
                        if (eb != null) {
                            effect = eb.produce(label, params);
                        }
                        break;
                    }
                    case EFFECT_END: {
                        break;
                    }
                }

                // Register token
                TokenEntry entry = new TokenEntry(tokenName, tokenCategory, index + indexOffset, m.end(0), floatValue, stringValue);
                entry.effect = effect;
                label.tokenEntries.add(entry);

                // Set new text without tokens
                matcherIndexOffset = m.end();
                m2.setPosition(0);
                text2 = m2.replaceFirst("");
            }
        }
        // Update label text
        label.setIntermediateText(text2, false, false);
    }

    /**
     * Parse color markup tags and register SKIP tokens.
     */
    private static void parseColorMarkups(TypingLabel label) {
        // Get text
        final CharSequence text = label.getOriginalText();
//        System.out.println("Original: "+text);
        // Iterate through matches and register skip tokens
        MATCHER_MARKUP_STRIP.setTarget(text);
        Matcher m = MATCHER_MARKUP_STRIP;
        while (m.find()) {
            final String tag = m.group(0);
            final int index = m.start(0);
            label.tokenEntries.add(new TokenEntry("SKIP", TokenCategory.SKIP, index, m.end(0), 0, tag));
        }
    }

    /**
     * Returns a float value parsed from the given String, or the default value if the string couldn't be parsed.
     * This can be useful in Effects.
     */
    public static float stringToFloat(String str, float defaultValue) {
        if (str != null) {
            try {
                return Float.parseFloat(str.replaceAll("[^\\d.\\-+]", ""));
            } catch (Exception e) {
            }
        }
        return defaultValue;
    }

    /**
     * Returns a boolean value parsed from the given String, or the default value if the string couldn't be parsed.
     * This can be useful in Effects.
     */
    public static boolean stringToBoolean(String str) {
        if (str != null) {
            return BOOLEAN_TRUE.containsKey(str);
        }
        return false;
    }

    /**
     * Parses a color from the given string. Returns null if the color couldn't be parsed.
     * This can be useful in Effects. This uses the {@link ColorLookup} of the given label's
     * default Font to try to parse {@code str} (unless the ColorLookup is null), and if that
     * finds a named color, this returns its value. Otherwise, this parses the color as a hex
     * RGBA8888, hex RGB888, or hex RGB444 color. The RGB modes assume alpha is fully opaque.
     * This will attempt to parse the most chars it can and determines which RGB(A) format to
     * use based on the length of {@code str}.
     * @param label a TypingLabel; only used to get its default Font, which is only used for its ColorLookup
     * @param str a String containing either the name of a color as a ColorLookup can understand, or an RGB(A) hex int
     * @return an RGBA8888 int representing the color that str represents, or {@code 256} if no color could be looked up
     */
    public static int stringToColor(TypingLabel label, String str) {
        if (str != null) {
            // Try to parse named color
            ColorLookup lookup = label.getFont().getColorLookup();
            if(lookup != null) {
                int namedColor = lookup.getRgba(str);
                if (namedColor != 256) {
                    return namedColor;
                }
            }
            // Try to parse hex
            if (str.length() >= 3) {
                if (str.startsWith("#")) {
                    if (str.length() >= 9) return StringUtils.intFromHex(str, 1, 9);
                    if (str.length() >= 7) return StringUtils.intFromHex(str, 1, 7) << 8 | 0xFF;
                    if (str.length() >= 4) {
                        int rgb = StringUtils.intFromHex(str, 1, 4);
                        return
                                (rgb << 20 & 0xF0000000) | (rgb << 16 & 0x0F000000) |
                                (rgb << 16 & 0x00F00000) | (rgb << 12 & 0x000F0000) |
                                (rgb << 12 & 0x0000F000) | (rgb <<  8 & 0x00000F00) |
                                0xFF;
                    }

                } else {
                    if (str.length() >= 8) return StringUtils.intFromHex(str, 0, 8);
                    if (str.length() >= 6) return StringUtils.intFromHex(str, 0, 6) << 8 | 0xFF;
                    int rgb = StringUtils.intFromHex(str, 0, 3);
                    return
                            (rgb << 20 & 0xF0000000) | (rgb << 16 & 0x0F000000) |
                            (rgb << 16 & 0x00F00000) | (rgb << 12 & 0x000F0000) |
                            (rgb << 12 & 0x0000F000) | (rgb <<  8 & 0x00000F00) |
                            0xFF;
                }
            }
        }

        return 256;
    }

    /**
     * Encloses the given string in brackets to work as a regular color markup tag.
     */
    public static String stringToColorMarkup(String str) {
        if (str != null) {
            // If color isn't registered by name, try to parse it as a hex code.
            if (str.length() >= 3 && !Palette.NAMED.containsKey(str) && MATCHER_COLOR_HEX_NO_HASH.matches(str)) {
                return "[#" + str + "]";
            }
        }

        // Return no change
        return "[" + str + "]";
    }

    /**
     * Matches style names to syntax and encloses the given string in brackets to work as a style markup tag.
     */
    public static String stringToStyleMarkup(String str) {
        if (str != null) {
            if(str.isEmpty() || str.equalsIgnoreCase("UNDO"))
                return "[]";
            if (str.equals(" "))
                return "[ ]";
            if (str.equals("*") || str.equalsIgnoreCase("B") || str.equalsIgnoreCase("BOLD") || str.equalsIgnoreCase("STRONG"))
                return "[*]";
            if (str.equals("/") || str.equalsIgnoreCase("I") || str.equalsIgnoreCase("OBLIQUE") || str.equalsIgnoreCase("ITALIC"))
                return "[/]";
            if (str.equals("_") || str.equalsIgnoreCase("U") || str.equalsIgnoreCase("UNDER") || str.equalsIgnoreCase("UNDERLINE"))
                return "[_]";
            if (str.equals("~") || str.equalsIgnoreCase("STRIKE") || str.equalsIgnoreCase("STRIKETHROUGH"))
                return "[~]";
            if (str.equals(".") || str.equalsIgnoreCase("SUB") || str.equalsIgnoreCase("SUBSCRIPT"))
                return "[.]";
            if (str.equals("=") || str.equalsIgnoreCase("MID") || str.equalsIgnoreCase("MIDSCRIPT"))
                return "[=]";
            if (str.equals("^") || str.equalsIgnoreCase("SUPER") || str.equalsIgnoreCase("SUPERSCRIPT"))
                return "[^]";
            if (str.equals("!") || str.equalsIgnoreCase("UP") || str.equalsIgnoreCase("UPPER"))
                return "[!]";
            if (str.equals(",") || str.equalsIgnoreCase("LOW") || str.equalsIgnoreCase("LOWER"))
                return "[,]";
            if (str.equals(";") || str.equalsIgnoreCase("EACH") || str.equalsIgnoreCase("TITLE"))
                return "[;]";
            if (str.equals("@") || str.equalsIgnoreCase("NOFONT") || str.equalsIgnoreCase("ENDFONT"))
                return "[@]";
            if (str.equalsIgnoreCase("JOSTLE") || str.equalsIgnoreCase("WOBBLE") || str.equalsIgnoreCase("SCATTER"))
                return "[%?jostle]";
            if (str.equalsIgnoreCase("BLACK OUTLINE") || str.equalsIgnoreCase("BLACKEN"))
                return "[%?black outline]";
            if (str.equalsIgnoreCase("WHITE OUTLINE") || str.equalsIgnoreCase("WHITEN"))
                return "[%?white outline]";
            if (str.equalsIgnoreCase("SHINY") || str.equalsIgnoreCase("SHINE") || str.equalsIgnoreCase("GLOSSY"))
                return "[%?shiny]";
            if (str.equalsIgnoreCase("SHADOW") || str.equalsIgnoreCase("DROPSHADOW") || str.equalsIgnoreCase("DROP SHADOW"))
                return "[%?shadow]";
            if (str.equalsIgnoreCase("ERROR") || str.equalsIgnoreCase("REDLINE") || str.equalsIgnoreCase("RED LINE"))
                return "[%?error]";
            if (str.equalsIgnoreCase("WARN") || str.equalsIgnoreCase("YELLOWLINE") || str.equalsIgnoreCase("YELLOW LINE"))
                return "[%?warn]";
            if (str.equalsIgnoreCase("NOTE") || str.equalsIgnoreCase("INFO") || str.equalsIgnoreCase("BLUELINE") || str.equalsIgnoreCase("BLUE LINE"))
                return "[%?note]";
            if (str.equalsIgnoreCase("SMALLCAPS") || str.equalsIgnoreCase("SMALL CAPS"))
                return "[%?small caps]";
            if (str.equals("?") || str.equals("%?") || str.equals("%^") || str.equalsIgnoreCase("NOMODE") || str.equalsIgnoreCase("ENDMODE"))
                return "[%?]";
            if (str.equals("%") || str.equalsIgnoreCase("NOSCALE") || str.equalsIgnoreCase("ENDSCALE"))
                return "[%]";
            if (str.startsWith("@"))
                return "[@" + str.substring(1) + "]";
            if (str.endsWith("%"))
                return "[%" + str.substring(0, str.length() - 1) + "]";
            if (str.startsWith("%"))
                return "[%" + str.substring(1) + "]";
            if (str.startsWith("?"))
                return "[" + str + "]";
            if (str.startsWith("("))
                return "[" + str + "]";
            if (str.startsWith(" "))
                return "[" + str + "]";
            if(Palette.NAMED.containsKey(str))
                return "[" + str + "]";
            if (str.length() >= 3 && MATCHER_COLOR_HEX_NO_HASH.matches(str))
                return "[#" + str + "]";
        }
        // Return no change
        return "";
    }

    /**
     * Returns a compiled {@link Pattern} that groups the token name in the first group and the params in an optional
     * second one. Case-insensitive.
     */
    private static Pattern compileTokenPattern() {
        StringBuilder sb = new StringBuilder();
        sb.append("\\{(");
        Array<String> tokens = new Array<>();
        TypingConfig.EFFECT_START_TOKENS.keys().toArray(tokens);
        TypingConfig.EFFECT_END_TOKENS.keys().toArray(tokens);
        for (InternalToken token : InternalToken.values()) {
            tokens.add(token.name);
        }
        for (int i = 0; i < tokens.size; i++) {
            sb.append(tokens.get(i));
            if ((i + 1) < tokens.size) sb.append('|');
        }
        sb.append(")(?:\\=([^\\{\\}]+))?\\}");
        return Pattern.compile(sb.toString(), REFlags.IGNORE_CASE);
    }

    /**
     * Returns the replacement string intended to be used on {RESET} tokens.
     */
    private static String getResetReplacement() {
        Array<String> tokens = new Array<>();
        TypingConfig.EFFECT_END_TOKENS.keys().toArray(tokens);
        tokens.add("NORMAL");

        StringBuilder sb = new StringBuilder("[ ]");
        for (String token : tokens) {
            sb.append('{').append(token).append('}');
        }
        TypingConfig.dirtyEffectMaps = false;
        return sb.toString();
    }

}
