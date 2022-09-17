/*
 * Copyright (c) 2022 See AUTHORS file.
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

package com.github.tommyettinger.textra.utils;

public final class StringUtils {
    private StringUtils() {}

    public static String join(CharSequence delimiter, CharSequence... items) {
        if(items == null || items.length == 0)
            return "";
        StringBuilder sb = new StringBuilder();
        sb.append(items[0]);
        for (int i = 1; i < items.length; i++) {
            sb.append(delimiter).append(items[i]);
        }
        return sb.toString();
    }
}
