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
 * Simple listener for label events. You can derive from this and only override what you are interested in.
 */
public class TypingAdapter implements TypingListener {

    @Override
    public void event(String event) {
    }

    @Override
    public void end() {
    }

    @Override
    public String replaceVariable(String variable) {
        return null;
    }

    @Override
    public void onChar(long ch) {
    }

}
