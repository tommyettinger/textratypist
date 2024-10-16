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

package com.github.tommyettinger.textra.effects;

import com.github.tommyettinger.textra.Effect;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.TypingLabel;
import com.github.tommyettinger.textra.utils.CaseInsensitiveIntMap;

/**
 * Enables style properties for the single glyph (or affected text, if all is true) underneath the pointer/mouse, and
 * disables them when not underneath. The style properties this effect controls are things like bold and underline.
 * There are many situations when you will want "all" mode enabled, such as to underline all of an area affected by
 * Stylist, or change all of it to oblique instead of just one glyph at a time. Stylist can use the textual syntax:
 * <code>
 *     {STYLIST=bold underline all}
 * </code>
 * or its abbreviated form:
 * <code>
 *     {STYLIST=b u a}
 * </code>
 * Or symbolic abbreviated form, matching square-bracket markup where possible:
 * <code>
 *     {STYLIST=* _ a}
 * </code>
 * It also just so happens that entering <code>{STYLIST}</code> will enable bold, underline, and 'all' mode.
 * <br>
 * All of these textual options are case-insensitive and apply to whole words only (separated by whitespace, tab, or
 * comma):
 * <ul>
 *     <li>Bold can be enabled with {@code bold}, {@code b}, or {@code *}.</li>
 *     <li>Oblique can be enabled with {@code oblique}, {@code o}, {@code italic}, {@code i}, or {@code /}.</li>
 *     <li>Underline can be enabled with {@code underline}, {@code u}, or {@code _}.</li>
 *     <li>Strikethrough can be enabled with {@code strikethrough}, {@code strike}, {@code s}, or {@code ~}.</li>
 *     <li>Subscript can be enabled with {@code subscript}, {@code sub}, or {@code .}.</li>
 *     <li>Midscript can be enabled with {@code midscript}, {@code mid}, or {@code =}.</li>
 *     <li>Superscript can be enabled with {@code superscript}, {@code super}, or {@code ^}.</li>
 *     <li>Enabling 'all' mode can be done by having {@code all} or {@code a} in the String.</li>
 * </ul>
 * <br>
 * Stylist can instead use the positional syntax:
 * <code>
 *     {STYLIST=y;n;y;n;0;y}
 * </code>
 * Which, in order, enables bold (y for yes), does not change oblique (n for no), enables underline, does not change
 * strikethrough, does not change any of the script options (subscript would be 1, midscript 2, or superscript 3, but
 * since this is 0 it won't change), and enables "all" mode as the last 'y' (which highlights the whole area using the
 * effect, instead of just the char being hovered over).
 * <br>
 * The textual syntax is preferred now, but the positional syntax was here first. Note that if a semicolon is present at
 * all, this will treat the input as positional.
 * <br>
 * Positional syntax parameters: {@code bold;oblique;underline;strikethrough;script;all}
 * <br>
 * Setting {@code bold} to true, y, 1, or some other true-indicating value makes this toggle bold.
 * Setting {@code oblique} to true, y, 1, or some other true-indicating value makes this toggle oblique.
 * Setting {@code underline} to true, y, 1, or some other true-indicating value makes this toggle underline.
 * Setting {@code strikethrough} to true, y, 1, or some other true-indicating value makes this toggle strikethrough.
 * Setting {@code script} to 0 will have this not affect script mode, 1 will have it enable subscript, 2 will have it
 * enable midscript, and 3 will have it enable superscript.
 * The parameter {@code all} makes the whole span of text become affected when true, or individual glyphs be the only
 * things affected when false.
 * <br>
 * Example usage:
 * <code>
 * {STYLIST=italic all}This whole span of text will become oblique/italicized on mouse-over.{ENDSTYLIST}
 * {STYLIST=bold strike}Individual glyphs will be set to bold and be struck through while moused-over.{ENDSTYLIST}
 * </code>
 */
public class StylistEffect extends Effect {
    private long effects = 0L;//Font.BOLD | Font.OBLIQUE | Font.UNDERLINE | Font.STRIKETHROUGH | Font.SUPERSCRIPT;
    private boolean all = false; // Whether this should stylize the whole responsive area.

    public StylistEffect(TypingLabel label, String[] params) {
        super(label);
        label.trackingInput = true;

        // Bold
        if (params.length > 0) {
            if(paramAsBoolean(params[0]))
                effects |= Font.BOLD;
            else if(params.length == 1){
                String[] split = params[0].split("[\\s\t,]+");
                int[] matching = new int[split.length];
                CaseInsensitiveIntMap set = new CaseInsensitiveIntMap(split, matching);
                if(set.containsKey("bold") || set.containsKey("b") || set.containsKey("*"))
                    effects |= Font.BOLD;
                if(set.containsKey("italic") || set.containsKey("i") || set.containsKey("/") || set.containsKey("oblique") || set.containsKey("o"))
                    effects |= Font.OBLIQUE;
                if(set.containsKey("underline") || set.containsKey("u") || set.containsKey("_"))
                    effects |= Font.UNDERLINE;
                if(set.containsKey("strikethrough") || set.containsKey("s") || set.containsKey("~") || set.containsKey("strike"))
                    effects |= Font.STRIKETHROUGH;

                if(set.containsKey("superscript") || set.containsKey("^") || set.containsKey("super"))
                    effects |= Font.SUPERSCRIPT;
                else if(set.containsKey("midscript") || set.containsKey("=") || set.containsKey("mid"))
                    effects |= Font.MIDSCRIPT;
                else if(set.containsKey("subscript") || set.containsKey(".") || set.containsKey("sub"))
                    effects |= Font.SUBSCRIPT;

                if(set.containsKey("all") || set.containsKey("a"))
                    this.all = true;

                return;
            }
        }
        else {
            // default case
            effects |= Font.BOLD | Font.UNDERLINE;
            all = true;
            return;
        }

        // Oblique
        if (params.length > 1) {
            if(paramAsBoolean(params[1]))
                effects |= Font.OBLIQUE;
        }

        // Underline
        if (params.length > 2) {
            if(paramAsBoolean(params[2]))
                effects |= Font.UNDERLINE;
        }

        // Strikethrough
        if (params.length > 3) {
            if(paramAsBoolean(params[3]))
                effects |= Font.STRIKETHROUGH;
        }

        // Script
        if (params.length > 4) {
            effects |= ((long)paramAsFloat(params[4], 0) & 3L) << 25;
        }

        // All
        if (params.length > 5) {
            this.all = paramAsBoolean(params[5]);
        }
    }

    @Override
    protected void onApply(long glyph, int localIndex, int globalIndex, float delta) {
        if(all) {
            if(label.overIndex < indexStart || label.overIndex > indexEnd) {
                label.setInWorkingLayout(globalIndex, (glyph & ~effects));
                return;
            }
        }
        else {
            if(label.overIndex != globalIndex) {
                label.setInWorkingLayout(globalIndex, (glyph & ~effects));
                return;
            }
        }
        // Calculate progress
        label.setInWorkingLayout(globalIndex, (glyph & ~effects) | effects);
    }

}
