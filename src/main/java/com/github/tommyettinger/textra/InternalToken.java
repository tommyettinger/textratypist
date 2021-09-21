
package com.github.tommyettinger.textra;

enum InternalToken {
    // @formatter:off
	// Public
	WAIT          ("WAIT",          com.github.tommyettinger.textra.TokenCategory.WAIT     ),
	SPEED         ("SPEED",         com.github.tommyettinger.textra.TokenCategory.SPEED    ),
	SLOWER        ("SLOWER",        com.github.tommyettinger.textra.TokenCategory.SPEED    ),
	SLOW          ("SLOW",          com.github.tommyettinger.textra.TokenCategory.SPEED    ),
	NORMAL        ("NORMAL",        com.github.tommyettinger.textra.TokenCategory.SPEED    ),
	FAST          ("FAST",          com.github.tommyettinger.textra.TokenCategory.SPEED    ),
	FASTER        ("FASTER",        com.github.tommyettinger.textra.TokenCategory.SPEED    ),
	COLOR         ("COLOR",         com.github.tommyettinger.textra.TokenCategory.COLOR    ),
	CLEARCOLOR    ("CLEARCOLOR",    com.github.tommyettinger.textra.TokenCategory.COLOR    ),
	ENDCOLOR      ("ENDCOLOR",      com.github.tommyettinger.textra.TokenCategory.COLOR    ),
	VAR           ("VAR",           com.github.tommyettinger.textra.TokenCategory.VARIABLE ),
	EVENT         ("EVENT",         com.github.tommyettinger.textra.TokenCategory.EVENT    ),
	RESET         ("RESET",         com.github.tommyettinger.textra.TokenCategory.RESET    ),
	SKIP          ("SKIP",          com.github.tommyettinger.textra.TokenCategory.SKIP     );
	// @formatter:on

    final String        name;
    final com.github.tommyettinger.textra.TokenCategory category;

    private InternalToken(String name, com.github.tommyettinger.textra.TokenCategory category) {
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
