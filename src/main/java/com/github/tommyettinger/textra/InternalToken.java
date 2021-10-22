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

enum InternalToken {
    // @formatter:off
	// Public
	WAIT          ("WAIT",          TokenCategory.WAIT     ),
	SPEED         ("SPEED",         TokenCategory.SPEED    ),
	SLOWER        ("SLOWER",        TokenCategory.SPEED    ),
	SLOW          ("SLOW",          TokenCategory.SPEED    ),
	NORMAL        ("NORMAL",        TokenCategory.SPEED    ),
	FAST          ("FAST",          TokenCategory.SPEED    ),
	FASTER        ("FASTER",        TokenCategory.SPEED    ),
    COLOR         ("COLOR",         TokenCategory.COLOR    ),
    STYLE         ("STYLE",         TokenCategory.COLOR    ),
	CLEARCOLOR    ("CLEARCOLOR",    TokenCategory.COLOR    ),
	ENDCOLOR      ("ENDCOLOR",      TokenCategory.COLOR    ),
	VAR           ("VAR",           TokenCategory.VARIABLE ),
	EVENT         ("EVENT",         TokenCategory.EVENT    ),
	RESET         ("RESET",         TokenCategory.RESET    ),
	SKIP          ("SKIP",          TokenCategory.SKIP     );
	// @formatter:on

    final String        name;
    final TokenCategory category;

    private InternalToken(String name, TokenCategory category) {
        this.name = name;
        this.category = category;
    }

    @Override
    public String toString() {
        return name;
    }

    static InternalToken fromName(String name) {
        if(name != null) {
            for(InternalToken token : values()) {
                if(name.equalsIgnoreCase(token.name)) {
                    return token;
                }
            }
        }
        return null;
    }
}
