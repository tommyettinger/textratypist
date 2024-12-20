package com.github.tommyettinger.teavm;

import com.github.tommyettinger.StandardUITest;
import com.github.tommyettinger.TypingUITest;
import com.github.xpenatan.gdx.backends.teavm.TeaApplicationConfiguration;
import com.github.xpenatan.gdx.backends.teavm.TeaApplication;
import com.github.tommyettinger.Main;

/**
 * Launches the TeaVM/HTML application.
 */
public class TeaVMLauncher {
    public static void main(String[] args) {
        TeaApplicationConfiguration config = new TeaApplicationConfiguration("canvas");
        // change these to both 0 to use all available space, or both -1 for the canvas size.
        config.width = 760;
        config.height = 640;
        config.antialiasing = true;
//        new TeaApplication(new StandardUITest(), config);
        new TeaApplication(new TypingUITest(), config);
    }
}
