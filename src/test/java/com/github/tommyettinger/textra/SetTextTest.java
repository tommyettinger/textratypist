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

package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.tommyettinger.textra.utils.StringUtils;
import regexodus.Pattern;
import regexodus.Replacer;

import static com.badlogic.gdx.utils.Align.center;

public class SetTextTest extends ApplicationAdapter {
    ScreenViewport viewport;
    Stage stage;
    String text, textra;
    TextraLabel textraLabel;
    TypingLabel typingLabel;
    RandomXS128 random;
    long ctr = 1;
    @Override
    public void create() {
        viewport = new ScreenViewport();
        viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),true);
        stage = new Stage(viewport);
        stage.setDebugAll(true);
        random = new RandomXS128(ctr);

        Font font = new Font("mk/Military_Kid.fnt", "mk/Military_Kid.png", Font.DistanceFieldType.MSDF, 0, 0, 0f, 0f, false);
//        Font font = new Font(new BitmapFont(Gdx.files.internal("mk/Military_Kid.fnt")), Font.DistanceFieldType.MSDF, 0, 0, 0f, 0f, false);
        font.distanceFieldCrispness = 6;
        font.setDescent(-16);
        font.scale(0.5f, 0.5f);
//                KnownFonts.getRobotoCondensed();

        text = "[%150]Satchmo[%100] is a [%?blacken]{RAINBOW}cat{ENDRAINBOW}[%], [%50]who[%100] [%75]is[%100] extremely {SPEED=0.05}fat{NORMAL}; when he sits " +
                "{SHAKE}down{ENDSHAKE}, throughout the town, we all {WAVE}think{ENDWAVE}, 'What was that? Did it happen " +
                "again (that [*]thunderous[*] din)? What could ever make, such a [_]powerful[_] quake, but " +
                "a cat with a [~][_]double[_][~] chin?'";
//                "[*]Локус[*] [*]контроля[*] - свойство " +
//                "личности приписывать " +
//                "свои неудачи и успехи " +
//                "либо внешним факторам " +
//                "(погода, везение, другие " +
//                "люди, судьба-злодейка), " +
//                "либо внутренним (я сам, " +
//                "моё отношение, мои" +
//                "действия)";
        textra = text.replaceAll("\\{[^}]*}", "");
        typingLabel = new TypingLabel(
                text, new Styles.LabelStyle(), font);
        typingLabel.setWrap(true);
        typingLabel.setAlignment(center);
        typingLabel.setMaxLines(5);
        typingLabel.setEllipsis("...");
        typingLabel.setText(text);
        typingLabel.parseTokens();
        typingLabel.skipToTheEnd();
        textraLabel = new TextraLabel(
                "[#FFF]" + textra, new Styles.LabelStyle(), font);
        textraLabel.setWrap(true);
        textraLabel.setAlignment(center);
//        textraLabel.layout.setMaxLines(5);
        textraLabel.layout.setEllipsis("...");
        textraLabel.skipToTheEnd();
//        Stack stack = new Stack(textraLabel);
//        stack.setFillParent(true);
//        stack.pack();
//        stage.addActor(stack);
        Table root = new Table();
        root.add(typingLabel).right().fillY().growX();
        root.pack();
        root.setFillParent(true);
        stage.addActor(root);
        System.out.println("Typing: " + typingLabel);
        System.out.println("Textra: " + textraLabel);
    }

    private static final Replacer anReplacer = new Replacer(Pattern.compile("\\b(a)(\\p{G}+)(?=(?:({=brace}[\\[\\{])[^\\]\\}]*{\\:brace})*[àáâãäåæāăąǻǽaèéêëēĕėęěeìíîïĩīĭįıiòóôõöøōŏőœǿoùúûüũūŭůűųu])", Pattern.IGNORE_CASE | Pattern.UNICODE), "$1n$2");

    /**
     * A simple method that looks for any occurrences of the word 'a' followed by some non-zero amount of whitespace and
     * then any vowel starting the following word (such as 'a item'), then replaces each such improper 'a' with 'an'
     * (such as 'an item'). The regex used here isn't bulletproof, but it should be fairly robust, handling when you
     * have multiple whitespace chars, different whitespace chars (like carriage return and newline), accented vowels in
     * the following word (but not in the initial 'a', which is expected to use English spelling rules), and the case of
     * the initial 'a' or 'A'.
     * <br>
     * Gotta love Regexodus; this is a two-liner that uses features specific to that regular expression library.
     * @param text the (probably generated English) multi-word text to search for 'a' in and possibly replace with 'an'
     * @return a new String with every improper 'a' replaced
     */
    public static String correctABeforeVowel(final CharSequence text){
        return anReplacer.replace(text.toString());
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.DARK_GRAY);

        stage.act();
        stage.draw();
//        System.out.println("!!!  On frame #" + ctr);

        random.setSeed(++ctr);
        if ((ctr & 511) == 0) {
            System.out.println("typingLabel has " + typingLabel.getMaxLines() + " max lines and " + typingLabel.getEllipsis() + " ellipsis.");
            text = correctABeforeVowel(StringUtils.shuffleWords(text, random));
            textra = text.replaceAll("\\{[^}]*}", "");
            System.out.println(text);
//            typingLabel.activeEffects.clear();
//            typingLabel.tokenEntries.clear();
            typingLabel.setText(text); // broken regarding effects...
            typingLabel.parseTokens(); // this is needed when using setText().
//            typingLabel.restart(text); // this works on its own.
//            typingLabel.skipToTheEnd();
            textraLabel.setText("[RED]" + textra);
//            System.out.println(typingLabel.layout);
            System.out.println(typingLabel);
        }
    }

    @Override
    public void resize(int width, int height) {
        typingLabel.font.resizeDistanceField(width, height);
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
    }
    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("TextraLabel UI test");
        config.setWindowedMode(600, 480);
        config.disableAudio(true);
		config.setForegroundFPS(60);
//		config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.useVsync(true);
        new Lwjgl3Application(new SetTextTest(), config);
    }

}