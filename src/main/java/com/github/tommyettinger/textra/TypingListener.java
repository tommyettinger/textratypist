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

/**
 * Simple listener for label events.
 */
public interface TypingListener {

    /**
     * Called each time an {@code EVENT} token is processed.
     *
     * @param event Name of the event specified in the token. e.g. <code>{EVENT=player_name}</code> will have
     *              <code>player_name</code> as argument.
     */
    void event(String event);

    /**
     * Called when the char progression reaches the end.
     */
    void end();

    /**
     * Called when variable tokens are replaced in text. This is an alternative method to deal with variables, other
     * than directly assigning replacement values to the label. Replacements returned by this method have priority over
     * direct values, unless {@code null} is returned.
     *
     * @param variable The variable name assigned to the <code>{VAR}</code> token. For example, in <code>{VAR=townName}</code>,
     *                 the variable will be <code>townName</code>
     * @return The replacement String, or {@code null} if this method should be ignored and the regular values should be
     * used instead.
     * @see TypingLabel#setVariable(String, String)
     * @see TypingLabel#setVariables(java.util.Map)
     * @see TypingLabel#setVariables(com.badlogic.gdx.utils.ObjectMap)
     */
    String replaceVariable(String variable);

    /**
     * Called when a new character is displayed. May be called many times per frame depending on the label
     * configurations and text speed. Useful to do a certain action each time a character is displayed, like playing a
     * sound effect.
     */
    void onChar(long ch);

}
