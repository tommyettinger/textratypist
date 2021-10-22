/*
 * Copyright (c) 2021-2021 See AUTHORS file.
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

import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Constructor;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import regexodus.Matcher;
import regexodus.Pattern;
import regexodus.REFlags;
import regexodus.Replacer;

/** Utility class to parse tokens from a {@link TypingLabel}. */
class Parser {
    private static final Pattern PATTERN_MARKUP_STRIP      = Pattern.compile("(\\[[^\\[\\]]*(\\]|$))");
    private static final Replacer MARKUP_TO_TAG            = new Replacer(Pattern.compile("(?<!\\[)\\[([^\\[\\]]+)(\\]|$)"), "{STYLE=$1}");
    private static final Pattern PATTERN_COLOR_HEX_NO_HASH = Pattern.compile("[A-Fa-f0-9]{6,8}");

    private static final String[] BOOLEAN_TRUE = {"true", "yes", "t", "y", "on", "1"};
    private static final int      INDEX_TOKEN  = 1;
    private static final int      INDEX_PARAM  = 2;

    private static Pattern PATTERN_TOKEN_STRIP;
    private static String  RESET_REPLACEMENT;

    static ColorLookup lookup = ColorLookup.GdxColorLookup.INSTANCE;

    static String preprocess(CharSequence text){
        return MARKUP_TO_TAG.replace(text).replace("[]", "{RESET}");
    }

    /** Parses all tokens from the given {@link TypingLabel}. */
    static void parseTokens(TypingLabel label) {
        // Compile patterns if necessary
        if(PATTERN_TOKEN_STRIP == null || TypingConfig.dirtyEffectMaps) {
            PATTERN_TOKEN_STRIP = compileTokenPattern();
        }
        if(RESET_REPLACEMENT == null || TypingConfig.dirtyEffectMaps) {
            RESET_REPLACEMENT = getResetReplacement();
        }

        // Remove any previous entries
        label.tokenEntries.clear();

        // Parse all tokens with text replacements, namely color and var.
        parseReplacements(label);

        // Parse all regular tokens and properly register them
        parseRegularTokens(label);

        // Parse color markups and register SKIP tokens
        parseColorMarkups(label);

        label.setText(label.getIntermediateText().toString(), false, false);

        // Sort token entries
        label.tokenEntries.sort();
        label.tokenEntries.reverse();
    }

    /** Parse tokens that only replace text, such as colors and variables. */
    private static void parseReplacements(TypingLabel label) {
        // Get text
        CharSequence text = label.layout.appendInto(new StringBuilder());

        // Create string builder
        StringBuilder sb = new StringBuilder(text.length());
        Matcher m = PATTERN_TOKEN_STRIP.matcher(text);
        int matcherIndexOffset = 0;

        // Iterate through matches
        while(true) {
            // Reset StringBuilder and matcher
            sb.setLength(0);
            m.setTarget(text);
            m.setPosition(matcherIndexOffset);

            // Make sure there's at least one regex match
            if(!m.find()) break;

            // Get token and parameter
            final InternalToken internalToken = InternalToken.fromName(m.group(INDEX_TOKEN));
            final String param = m.group(INDEX_PARAM);

            // If token couldn't be parsed, move one index forward to continue the search
            if(internalToken == null) {
                matcherIndexOffset++;
                continue;
            }

            // Process tokens and handle replacement
            String replacement = "";
            switch(internalToken) {
                case COLOR:
                    replacement = stringToColorMarkup(param);
                    break;
                case STYLE:
                    replacement = stringToStyleMarkup(param);
                    break;
                case ENDCOLOR:
                case CLEARCOLOR:
                    replacement = "[#" + label.getClearColor().toString() + "]";
                    break;
                case VAR:
                    replacement = null;

                    // Try to replace variable through listener.
                    if(label.getTypingListener() != null) {
                        replacement = label.getTypingListener().replaceVariable(param);
                    }

                    // If replacement is null, get value from maps.
                    if(replacement == null) {
                        replacement = label.getVariables().get(param.toUpperCase());
                    }

                    // If replacement is still null, get value from global scope
                    if(replacement == null) {
                        replacement = TypingConfig.GLOBAL_VARS.get(param.toUpperCase());
                    }

                    // Make sure we're not inserting "null" to the text.
                    if(replacement == null) replacement = param.toUpperCase();
                    break;
                case RESET:
                    replacement = RESET_REPLACEMENT + label.getDefaultToken();
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

        // Set new text
        label.setIntermediateText(text, false, false);
    }

    /** Parses regular tokens that don't need replacement and register their indexes in the {@link TypingLabel}. */
    private static void parseRegularTokens(TypingLabel label) {
        // Get text
        CharSequence text = label.getIntermediateText();
        // Create matcher and StringBuilder
        Matcher m = PATTERN_TOKEN_STRIP.matcher(text);
        StringBuilder sb = new StringBuilder(text.length());
        int matcherIndexOffset = 0;

        // Iterate through matches
        while(true) {
            // Reset matcher and StringBuilder
            m.setTarget(text);
            sb.setLength(0);
            m.setPosition(matcherIndexOffset);

            // Make sure there's at least one regex match
            if(!m.find()) break;

            // Get token name and category
            String tokenName = m.group(INDEX_TOKEN).toUpperCase();
            TokenCategory tokenCategory = null;
            InternalToken tmpToken = InternalToken.fromName(tokenName);
            if(tmpToken == null) {
                if(TypingConfig.EFFECT_START_TOKENS.containsKey(tokenName)) {
                    tokenCategory = TokenCategory.EFFECT_START;
                } else if(TypingConfig.EFFECT_END_TOKENS.containsKey(tokenName)) {
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
            if(tokenCategory == null) {
                matcherIndexOffset++;
                continue;
            }

            // Process tokens
            float floatValue = 0;
            String stringValue = null;
            Effect effect = null;

            switch(tokenCategory) {
                case WAIT: {
                    floatValue = stringToFloat(firstParam, TypingConfig.DEFAULT_WAIT_VALUE);
                    break;
                }
                case EVENT: {
                    stringValue = paramsString;
                    indexOffset = -1;
                    break;
                }
                case SPEED: {
                    switch(tokenName) {
                        case "SPEED":
                            float minModifier = TypingConfig.MIN_SPEED_MODIFIER;
                            float maxModifier = TypingConfig.MAX_SPEED_MODIFIER;
                            float modifier = MathUtils.clamp(stringToFloat(firstParam, 1), minModifier, maxModifier);
                            floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR / modifier;
                            break;
                        case "SLOWER":
                            floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR / 0.500f;
                            break;
                        case "SLOW":
                            floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR / 0.667f;
                            break;
                        case "NORMAL":
                            floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR;
                            break;
                        case "FAST":
                            floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR / 2.000f;
                            break;
                        case "FASTER":
                            floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR / 4.000f;
                            break;
                    }
                    break;
                }
                case EFFECT_START: {
                    Class<? extends Effect> clazz = TypingConfig.EFFECT_START_TOKENS.get(tokenName.toUpperCase());
                    try {
                        if(clazz != null) {
                            Constructor constructor = ClassReflection.getConstructors(clazz)[0];
                            int constructorParamCount = constructor.getParameterTypes().length;
                            if(constructorParamCount >= 2) {
                                effect = (Effect) constructor.newInstance(label, params);
                            } else {
                                effect = (Effect) constructor.newInstance(label);
                            }
                        }
                    } catch(ReflectionException e) {
                        String message = "Failed to initialize " + tokenName + " effect token. Make sure the associated class (" + clazz + ") has only one constructor with TypingLabel as first parameter and optionally String[] as second.";
                        throw new IllegalStateException(message, e);
                    }
                    break;
                }
                case EFFECT_END: {
                    break;
                }
            }

            // Register token
            TokenEntry entry = new TokenEntry(tokenName, tokenCategory, index + indexOffset, floatValue, stringValue);
            entry.effect = effect;
            label.tokenEntries.add(entry);

            // Set new text without tokens
            m.setPosition(0);
            text = m.replaceFirst("");
        }

        // Update label text
        label.setIntermediateText(text, false, false);
    }

    /** Parse color markup tags and register SKIP tokens. */
    private static void parseColorMarkups(TypingLabel label) {
        // Get text
        final CharSequence text = label.getIntermediateText();

        // Iterate through matches and register skip tokens
        Matcher m = PATTERN_MARKUP_STRIP.matcher(text);
        while(m.find()) {
            final String tag = m.group(0);
            final int index = m.start(0);
            label.tokenEntries.add(new TokenEntry("SKIP", TokenCategory.SKIP, index, 0, tag));
        }
    }

    /** Returns a float value parsed from the given String, or the default value if the string couldn't be parsed. */
    static float stringToFloat(String str, float defaultValue) {
        if(str != null) {
            try {
                return Float.parseFloat(str);
            } catch(Exception e) {
            }
        }
        return defaultValue;
    }

    /** Returns a boolean value parsed from the given String, or the default value if the string couldn't be parsed. */
    static boolean stringToBoolean(String str) {
        if(str != null) {
            for(String booleanTrue : BOOLEAN_TRUE) {
                if(booleanTrue.equalsIgnoreCase(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Parses a color from the given string. Returns null if the color couldn't be parsed. */
    static Integer stringToColor(TypingLabel label, String str) {
        if(str != null) {

            // Try to parse named color
            Integer namedColor = label.font.getColorLookup().getRgba(str);
            if(namedColor != null) {
                return namedColor;
            }

            // Try to parse hex
            if(str.length() >= 6) {
                try {
                    if(str.startsWith("#")) str = str.substring(1);
                    if(str.length() == 6) return Integer.parseInt(str, 16) << 8 | 0xFF;
                    if(str.length() == 7) return Integer.parseInt(str.substring(0, 6), 16) << 8 | 0xFF;
                    if(str.length() == 8) return Integer.parseInt(str, 16);
                } catch(NumberFormatException ignored) {
                }
            }
        }

        return null;
    }

    /** Encloses the given string in brackets to work as a regular color markup tag. */
    private static String stringToColorMarkup(String str) {
        if(str != null) {
            // If color isn't registered by name, try to parse it as a hex code.
            if(!Colors.getColors().containsKey(str) && str.length() >= 6 && PATTERN_COLOR_HEX_NO_HASH.matches(str)) {
                    return "[#" + str + "]";
            }
        }

        // Return color code
        return "[" + str + "]";
    }

    /** Matches style names to syntax and encloses the given string in brackets to work as a style markup tag. */
    private static String stringToStyleMarkup(String str) {
        if (str != null) {
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
            if (!Colors.getColors().containsKey(str) && str.length() >= 6 && PATTERN_COLOR_HEX_NO_HASH.matches(str))
                return "[#" + str + "]";
        }
        // Return unaltered code, enclosed
        return "[" + str + "]";
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
        for(InternalToken token : InternalToken.values()) {
            tokens.add(token.name);
        }
        for(int i = 0; i < tokens.size; i++) {
            sb.append(tokens.get(i));
            if((i + 1) < tokens.size) sb.append('|');
        }
        sb.append(")(?:=([^\\{\\}]+))?\\}");
        return Pattern.compile(sb.toString(), REFlags.IGNORE_CASE);
    }

    /** Returns the replacement string intended to be used on {RESET} tokens. */
    private static String getResetReplacement() {
        Array<String> tokens = new Array<>();
        TypingConfig.EFFECT_END_TOKENS.keys().toArray(tokens);
        tokens.add("NORMAL");

        StringBuilder sb = new StringBuilder("[]");
        for(String token : tokens) {
            sb.append('{').append(token).append('}');
        }
        return sb.toString();
    }

}
