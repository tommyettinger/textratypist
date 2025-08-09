# TextraTypist
![Animated preview](images/logo_animated.gif)

A text-display library centered around a label that prints over time, with both effects and styles.

In other words, this brings more features to text rendering in libGDX.

What does this look like? A little something like this...

![Still preview](docs/previews/Gentium%20(MSDF).png)

Or perhaps like this...

![Animated preview](images/preview.gif)

If you'd rather watch a video than read this text,
[Raymond "raeleus" Buckley made a video covering most of TextraTypist](https://www.youtube.com/watch?v=4rLoa_jycN8)!
It covers some things this file doesn't, such as usage of Skin Composer, so it's a good watch regardless.

## It's got labels!

There's a "normal" label here in the form of TextraLabel, which acts almost exactly like Label in scene2d.ui, but
allows the styles covered below. A lot of usage may prefer TypingLabel, though!

TypingLabel is a fairly normal scene2d.ui widget, and extends TextraLabel. However, it puts letters up on
the screen one at a time, unless it is told to skip ahead. This is a nostalgic effect found in many older text-heavy
games, and it looks like a typewriter is putting up each letter at some slower-than-instantaneous rate.

## How do I get it?

You probably want to get TextraTypist with Gradle! The dependency for a libGDX project's core module looks like:

```groovy
implementation "com.github.tommyettinger:textratypist:2.1.2"
```

This assumes you already depend on libGDX; TextraTypist depends on version 1.13.1 (and not 1.13.5).
A requirement for 1.11.0 was added in TextraTypist 0.5.0 because of some breaking changes in tooltip code in libGDX.
The requirement for 1.12.1 was added in 1.0.0 because some things probably changed, and 1.13.1 in TextraTypist 2.0.0,
but 1.13.1 should be pretty easy to update to. There are breaking changes in 1.13.5 that are expected to be reverted by
the time of the release after it, so using 1.13.1 or 1.14.0 (what name the version will have, I do not know) should both
be compatible by the time 1.14.0 is released.

If you use GWT, this should be compatible. It needs these dependencies in the html module:

```groovy
implementation "com.github.tommyettinger:textratypist:2.1.2:sources"
implementation "com.github.tommyettinger:regexodus:0.1.19:sources"
```

GWT also needs this in the GdxDefinition.gwt.xml file (since version 0.7.7):
```xml
<inherits name="regexodus.regexodus" />
<inherits name="com.github.tommyettinger.textratypist" />
```

RegExodus is the GWT-compatible regular-expression library this uses to match some complex patterns internally. Other
than libGDX itself, RegExodus is the only dependency this project has. The GWT inherits changed for TextraTypist and for
RegExodus because it turns out using the default package can cause real problems.

There is at least one release in the [Releases](https://github.com/tommyettinger/textratypist/releases) section of this
repo, but you're still encouraged to use Gradle to handle this library and its dependencies.

You can also use JitPack to get a current commit, which can be handy if there's a long span between releases.
Current gdx-liftoff and gdx-setup projects all can use JitPack dependencies without needing any extra configuration.
You would use this dependency in your core module:

```groovy
implementation 'com.github.tommyettinger:textratypist:d74c474fff'
```

You can change `d74c474fff` to any commit in the Commits tab of https://jitpack.io/#tommyettinger/textratypist ,
but you should not use `-SNAPSHOT` -- it can change without your requesting it to, which is not what you want!

You can also depend on FreeTypist using:

```groovy
implementation "com.github.tommyettinger:freetypist:2.1.2.0"
```

(Now, FreeTypist 2.1.2.0 uses TextraTypist 2.1.2 .)

And if you target HTML and have FreeType working somehow, you would use this Gradle dependency:

```groovy
implementation "com.github.tommyettinger:freetypist:2.1.2.0:sources"
```

And this inherits line:

```xml
<inherits name="com.github.tommyettinger.freetypist" />
```

FreeType doesn't work out-of-the-box on GWT, though [there is this](https://github.com/intrigus/gdx-freetype-gwt)].

## It's got effects!

Yes, it has more than the typewriter mode! Text can hang above and then drop into place. It can jump up and down in a
long wave. It can waver and shudder, as if it is sick. It can blink in different colors, move in a gradient smoothly
between two colors, or go across a whole rainbow. Lots of options; lots of fun. Effects are almost the same as in
typing-label, but there have been some changes and many additions. You can check [the TextraTypist wiki](https://github.com/tommyettinger/textratypist/wiki/Examples)
for more information.

As of 0.10.0, there are many new effects. Jolt, Spiral, Spin, Crowd, Shrink, Emerge, Heartbeat, Carousel, Squash, Scale,
Rotate, Attention, Highlight, Link, Trigger, Stylist, Cannon, Ocean, Sputter, Instant, Meet, Zipper, and Slam are all
new to TextraTypist (not in typing-label). You can see usage instructions and sample GIFs at
[the TextraTypist wiki's Tokens page](https://github.com/tommyettinger/textratypist/wiki/Tokens). Most of these effects
make use of the smooth scaling and rotation options that effects can use starting in TextraTypist 0.5.1 . Some make use
of mouse tracking, new in 0.7.0, such as how Link only responds to a click on a range of text.

You may want to create `TypingLabel`s even where you don't need the typing effect, because `TextraLabel` doesn't handle
any effects. You can call `skipToTheEnd()` on a TypingLabel or (in 0.7.0 and up) on some other classes to allow a
TypingLabel to be used for still text with effects.

Various standard tokens are also present, and these can manipulate the typing effect, variable replacement, and other
useful things:

- `{WAIT=f}` causes the typing effect to pause and wait for `f` seconds, as a float.
- `{SPEED=f}` changes the time it takes to type a typical glyph, from a default of `0.035` to `f`.
- `{SLOWER}` makes all glyphs take 2x as long to type.
- `{SLOW}` makes all glyphs take 1.5x as long to type.
- `{NORMAL}` makes all glyphs take the normal 1x as long to type.
- `{FAST}` makes all glyphs take 0.5x as long to type.
- `{FASTER}` makes all glyphs take 0.25x as long to type.
- `{NATURAL=f}` makes glyphs take randomly more or less time to type, but otherwise is the same as `{SPEED=f}`.
- `{COLOR=s}` changes the color of text; this has lots of options, so you can have e.g. "dark grey pink".
- `{STYLE=s}` changes the style of text (see below); this has a LOT of options.
- `{SIZE=f}` changes the size of text (coarsely, in 25% increments); this takes f as a percentage from 0 to 375.
- `{FONT=name}` changes the font, if there is a FontFamily available, by looking up `name`.
- `{CLEARCOLOR}` sets the text color to the default color, which is usually white.
- `{CLEARSIZE}` sets the size to 100%.
- `{CLEARFONT}` sets the font to the original font (not using the FontFamily).
- `{ENDCOLOR}` sets the text color to the default color, which is usually white. This is the same as `{CLEARCOLOR}`.
- `{VAR=name}` gets replaced by whatever String was associated with the variable `name`.
- `{IF=name;choice0=cat;choice1=b;=default}` checks the variable name, and compares it to each choice in the token.
  - If the value assigned to `name` is equal to a choice, the token is replaced by the choice's value, such as `cat`.
  - If no choice is equivalent and there is an empty String for a choice, the value associated with the empty String is
    used as a `default` value.
- `{EVENT=name}` triggers an event, sending `name` to the TypingListener, when the typing reaches this point.
- `{RESET}` Sets all formatting and speed changes to their initial values.
  - `label.setDefaultToken()` can be used to change the initial values, so text defaults to some different settings. 
- `{SKIP=n}` skips ahead in the typing effect, instantly displaying `n` characters.

Effects use curly braces by default, but if curly braces aren't a good option for your text (such as in I18N files), you
can use `[-EFFECT]` as an equivalent to `{EFFECT}`.

## And now, it's got style!

This library extends what the original typing-label can do -- it allows styles to be applied to text, such as bold,
underline, oblique, superscript, etc. Related to styles are scale changes, which can shrink or enlarge text without
changing your font, and the "font family" feature. A font can be assigned a "family" of other fonts and names to use to
refer to them; this acts like a normal style, but actually changes what Font is used to draw. The full list of styles is
long, but isn't as detailed as the effect tokens. You can enable styles with something like libGDX color markup, in
square brackets like `[*]`, or (if the markup is used in a `TypingLabel`) you can use `{STYLE=BOLD}` to do the same
thing. Tags and style names are both case-insensitive, but color names are case-sensitive. The square-bracket syntax
uses primarily punctuation, and is inspired by Markdown syntax (which GitHub uses, among other places).

In the following list, each entry looks something like:

`[*]` toggles bold mode. Can use style names `*`, `B`, `BOLD`, `STRONG`.

That means you can always use `[*]` to turn bold mode on or off, and in a TypingLabel you can additionally use the
case-insensitive syntax `{STYLE=*}`,  `{STYLE=B}`,  `{STYLE=BOLD}`, or  `{STYLE=STRONG}` to do the same thing.

The full list of styles and related square-bracket tags:

- `[]` undoes the last change to style/color/formatting, though it doesn't do anything to TypingLabel effects.
  - This acts much like `[]` does in libGDX BitmapFont markup, but works on more than colors. 
- `[ ]` resets all styles/colors/formatting and effects to the initial state.
  - This used different syntax before version 0.10.0; if updating from an older version, you probably want to change the
    old `[]` to the new `[ ]` , with a space inside the brackets. 
- `[(label)]` temporarily stores the current formatting state as `label`, so it can be re-applied later.
  - This can be useful if you want to insert a formatted snippet into an outer piece of text, without losing the
    formatting the outer text had before the insertion.
  - `label` can be any alphanumeric String. It probably shouldn't have spaces in it, but can have underscores.
- `[ label]` re-applies the formatting state stored as `label`, if there is one.
- `[*]` toggles bold mode. Can use style names `*`, `B`, `BOLD`, `STRONG`.
- `[/]` toggles oblique mode (like italics). Can use style names `/`, `I`, `OBLIQUE`, `ITALIC`.
- `[^]` toggles superscript mode (and turns off subscript or midscript mode). Can use style names `^`, `SUPER`, `SUPERSCRIPT`.
- `[=]` toggles midscript mode (and turns off superscript or subscript mode). Can use style names `=`, `MID`, `MIDSCRIPT`.
- `[.]` toggles subscript mode (and turns off superscript or midscript mode). Can use style names `.`, `SUB`, `SUBSCRIPT`.
- `[_]` toggles underline mode. Can use style names `_`, `U`, `UNDER`, `UNDERLINE`.
- `[~]` toggles strikethrough mode. Can use style names `~`, `STRIKE`, `STRIKETHROUGH`.
- `[!]` toggles all upper case mode (replacing any other case mode). Can use style names `!`, `UP`, `UPPER`.
- `[,]` toggles all lower case mode (replacing any other case mode). Can use style names `,`, `LOW`, `LOWER`.
- `[;]` toggles capitalize each word mode (replacing any other case mode). Can use style names `;`, `EACH`, `TITLE`.
- `[%DDD]`, where DDD is a percentage from 0 to 375, scales text to that multiple. Can be used with `{SIZE=150%}`, `{SIZE=%25}`, or similarly, `{STYLE=200%}` or `{STYLE=%125}`. Removes any special mode.
- `[%]` on its own sets text to the default 100% scale and removes any special mode. Can be used with `{STYLE=%}`.
- `[%?MODE]` removes the scale and sets a special mode; modes are listed below.
- `[%^MODE]` removes the scale and sets a special mode at the same time as small-caps mode; modes are listed below.
- `[@Name]`, where Name is a key/name in this Font's `family` variable, switches the current typeface to the named one. Can be used with `{STYLE=@Name}`.
- `[@]` on its own resets the typeface to this Font, ignoring its family. Can be used with `{STYLE=@}`.
- `[#HHHHHHHH]`, where HHHHHHHH is a hex RGB888 or RGBA8888 int color, changes the color. This is a normal `{COLOR=#HHHHHHHH}` tag.
- `[COLORNAME]`, where COLORNAME is a color name or description that will be looked up externally, changes the color.
  - By default, this looks up COLORNAME with `ColorUtils.describe()`, which tries to find any colors from `Palette` by
    name, and also allows describing mixes of colors or simple changes like "light" or "dull".
    - Palette contains all the UPPER_CASE names from libGDX's `Colors` class, and also about 50 additional lowercase
      color names (from [colorful-gdx](https://github.com/tommyettinger/colorful-gdx)).
      - You can preview `Palette` [by hue](https://tommyettinger.github.io/textratypist/ColorTableHue.html), [by lightness](https://tommyettinger.github.io/textratypist/ColorTableLightness.html), or [by name](https://tommyettinger.github.io/textratypist/ColorTableAlphabetical.html).
    - Adjectives can be "light", "dark", "rich", "dull", or stronger versions of those suffixed with "-er", "-est", or
      "-most". You can use any combination of adjectives, and can also combine multiple colors, such as "red orange". 
      - There are some more adjectives that act like pairs of the above four adjectives, for convenience:
        - Combining "light" and "rich" is the same as "bright".
        - Combining "light" and "dull" is the same as "pale".
        - Combining "dark" and "rich" is the same as "deep".
        - Combining "dark" and "dull" is the same as "weak".
    - Some examples: `[RED]`, `[green yellow]`, `[light blue]`, `[duller orange]`, `[darker rich BLUE lavender]`, `[pale pink orange]`, and `[deeper green navy]`.
    - There's a section at the bottom of this README.md that covers some tricky parts of color descriptions.
  - You can use `Font`'s `setColorLookup()` method with your own `ColorLookup` implementation to do what you want here.
    - This isn't a commonly-used feature, but could be handy for some more elaborate color handling.
  - The name can optionally be preceded by `|`, which allows looking up colors with names that contain punctuation.
    For example, `[|;_;]` would look up a color called `;_;`, "the color of sadness," and would not act like `[;]`.
    - Non-alphabetical characters are ignored by default, but a custom `ColorLookup` might not, nor does
      `ColorLookup.INSTANCE`, which looks up String names in the libGDX Colors class verbatim.
  - This also can be used with a color tag, such as `{COLOR=SKY}` (which Colors can handle right away) or
    with a description, such as `{COLOR=lighter orange-red}`, even inside a tag like `{GRADIENT}`.
- `[+region name]`, where region name is the name of a TextureRegion from a registered TextureAtlas, won't change the
  style, but will produce that TextureRegion in-line with the text.
  - This is commonly used with `KnownFonts.addEmoji()` to add the 3000+ Twemoji icons to a Font.
    - If you use Twemoji, the phrases `[+saxophone]` and `[+🎷]` will each show a saxophone icon.
    - This also works with multipart emoji, such as `[+call me hand, medium-dark skin tone]` and `[+🤙🏾]`.
    - The emoji [can be previewed here](https://tommyettinger.github.io/twemoji-atlas/).
  - Another option is `KnownFonts.addGameIcons()`, which adds icons from
    [the game-icons.net collection](https://game-icons.net). These use the same syntax: `[+crystal-wand]`.
    - The game icons [can be previewed here](https://tommyettinger.github.io/game-icons-net-atlas/).
  - There are also [OpenMoji](https://openmoji.org)'s emoji in either single-color line-art or full-color modes, and
  - [Noto Color Emoji](https://github.com/googlefonts/noto-emoji/tree/main) (which tends to use different textual names,
    but the syntax with a literal emoji, `[+💖]`, still works the same).
      - OpenMoji [can be previewed here for full-color](https://tommyettinger.github.io/openmoji-atlas/) or
        [here for the line-art version](https://tommyettinger.github.io/openmoji-atlas/black.html).
    - Noto Color Emoji [can be previewed here](https://tommyettinger.github.io/noto-emoji-atlas/).
- `[-SOME_EFFECT]` is equivalent to using curly braces around `SOME_EFFECT`; note the added dash.

The special modes that can be used in place of scaling are:

- `black outline` or `blacken`, which can be used with the style names `BLACK OUTLINE` or `BLACKEN`.
- `white outline` or `whiten`, which can be used with the style names `WHITE OUTLINE` or `WHITEN`.
- `shiny`, which can be used with the style names `SHINY`, `SHINE`, or `GLOSSY`.
- `drop shadow` or `shadow`, which can be used with the style names `SHADOW`, `DROPSHADOW`, or `DROP SHADOW`.
- `error`, which can be used with the style names `ERROR`, `REDLINE`, or `RED LINE`.
  - This adds a zigzag red line below text; the color can be changed using `Font.PACKED_ERROR_COLOR`.
- `warn`, which can be used with the style names `WARN`, `YELLOWLINE`, or `YELLOW LINE`.
  - This adds a dashed yellow line below text; the color can be changed using `Font.PACKED_WARN_COLOR`.
- `note`, which can be used with the style names `NOTE`, `INFO`, `BLUELINE`, or `BLUE LINE`.
  - This adds a wavy blue line below text; the color can be changed using `Font.PACKED_NOTE_COLOR`.
- `jostle`, which can be used with the style names `JOSTLE`, `WOBBLE`, or `SCATTER`.
  - The jostle mode can also be used with `[%?]`.
- `small caps`, which can be used with the style names `SMALLCAPS` or `SMALL CAPS`.
  - The small caps mode can also be used with `[%^]`. It cannot be used with the `[%?small caps]` syntax; it needs a caret.

The small caps mode can be used with any of the other modes except for jostle, by changing `%?` to `%^`. Other than
that, no two modes can be active at the same time, and no modes can be used at the same time as scaling.

Note that modes use slightly different syntax to avoid being confused with color names. When using square brackets, each
of the names given here in lower-case should be preceded by `%?` most of the time (small caps and jostle are special).
That means to enable the red-underline mode "error", you use the square-bracket tag `[%?error]`. If using the
curly-brace markup for TypingLabel, you would use the names given here in upper-case, and can use them like other style
names:`{STYLE=ERROR}`, for example. Small caps mode is, as mentioned, special; it is usually enabled with
`[%^small caps]`, but can also be enabled with `[%^]`, and can also be mixed with any other mode except jostle by
changing the normal `%?` to `%^`. Whenever small caps is active, the square-bracket tag uses `%^` instead of `%?`.
Jostle mode is also special; it is usually enabled with `[%?jostle]`, but can also be enabled with `[%?]` on its own.
Jostle can't be mixed with small caps.

The special modes are a bit overcomplicated in terms of syntax because I ran out of punctuation I could use.
The common example of a black outline around white text can be achieved with `[WHITE][%?blacken]Outlined![%][GRAY]`.
(The example uses `GRAY` as the normal color, but you could also use `[ ]` to reset the color to whatever base color was
configured on a `Layout` or the label that holds it. Note that `[ ]` also resets size, mode, and, well, everything.)

Several combinations of effects are available using the `{VAR=ZOMBIE}urgh, brains...{VAR=ENDZOMBIE}` syntax:

 - `{VAR=FIRE}` changes the following text to have fiery changing colors. You can end it with
   `{VAR=ENDFIRE}`.
 - `{VAR=SPUTTERINGFIRE}` changes the following text to have fiery changing colors and resize
   like popping flames. You can end it with `{VAR=ENDSPUTTERINGFIRE}`.
 - `{VAR=BLIZZARD}` changes the following text to waver in the wind and use icy colors,
   white to light blue. You can end it with `{VAR=ENDBLIZZARD}`.
 - `{VAR=SHIVERINGBLIZZARD}` changes the following text to waver in the wind and use icy
   colors, white to light blue, plus it will randomly make glyphs "shiver" as if cold. You can end it with
   `{VAR=ENDSHIVERINGBLIZZARD}`.
 - `{VAR=ELECTRIFY}` changes the following text to be a dull gray purple color and randomly
   makes glyphs turn light yellow and vibrate around. You can end it with `{VAR=ENDELECTRIFY}`.
 - `{VAR=ZOMBIE}` changes the following text to be "dark olive sage" (a dull gray-green
   color), makes glyphs rotate left and right slowly and randomly, makes glyphs drop down and get back up
   randomly, and when they first appear, has the glyphs emerge from the baseline (as if clawing out of a grave).
   You can end it with `{VAR=ENDZOMBIE}`.

These are defined in `TypingConfig.initializeGlobalVars()`, and you can define your own combinations in
exactly the same way these are defined. For example, `FIRE` is defined with
```java
        TypingConfig.GLOBAL_VARS.put("FIRE", "{OCEAN=0.7;1.25;0.11;1.0;0.65}");
        TypingConfig.GLOBAL_VARS.put("ENDFIRE", "{ENDOCEAN}");
```

The `OCEAN` effect doesn't care what colors it uses; it only defines an approximate pattern for how to transition
between those colors. That means, counterintuitively, `FIRE` is best implemented with `OCEAN` rather than `GRADIENT`.
Using the name `FIRE` is probably preferable to `OCEAN`, though, so the global var is here for that reason.

The ability to store formatting states using a label allows some more complex assembly of markup Strings from multiple
sources. You can call something like `font.storeState("spooky", "[/][darker gray][@?blacken]")` to permanently store
that formatting state (oblique darker gray text with a black outline) in `font`, and can then reset to that state just
by entering `[ spooky]` (note the opening space). You could also create some insert-able text that stores the current
formatting before it writes anything, and resets the formatting back when it is done writing. That would use something
like `"[(previous)][ ][BLUE][^][[citation needed][ previous]"` -- if this String gets inserted in the middle of a larger
block of text, it won't change the surrounding formatting, but will use blue superscript for its own text (the immortal
`[citation needed]`) and won't use any of the surrounding block's formatting for its own superscript note. If you have
multiple state-store tags with the same label, the value associated with that label will change as those tags are
encountered. You might want to use unique labels to avoid accidentally changing another label's value, but this usually
isn't needed.

## But wait, there's fonts!

Textratypist makes heavy use of its new `Font` class, which is a full overhaul of libGDX's BitmapFont that shares
essentially no code with its ancestor. A Font has various qualities that give it more power than BitmapFont, mostly
derived from how it stores (and makes available) the glyph images as TextureRegions in a map. There's nothing strictly
preventing you from adding your own images to the `mapping` of a Font, as long as they have the requisite information to
be used as a textual glyph, and then placing those images in with your text. This is used to implement emoji, as one
example, and can be used for custom icons and emoji.

Textratypist supports standard bitmap fonts and also distance field fonts, using SDF or MSDF. `TypingLabel` and
`TextraLabel` will automatically enable the ShaderProgram that
the appropriate distance field type needs (if it needs one) and disable it after rendering itself. You can change this
behavior by manually calling the `Font.enableShader(Batch)` method on your Font, and changing the Batch back to your
other ShaderProgram of choice with its `Batch.setShader()` method (often, you just pass null here to reset the shader).
Note that SDF and MSDF fonts need to be told about changes to the screen size, using `Font.resizeDistanceField()` or any
of various other places' methods that call `resizeDistanceField()`. Since 1.0.0, you typically want to use the overload
that takes a `Viewport`; if you don't have a `Viewport`, you don't need that overload. Every distance field font you are
currently rendering needs to have its distance field resized when the window resizes, in `ApplicationListener.resize()`.
Since version 2.0.2, if a FontFamily uses SDF fonts (not MSDF), then the shader will automatically render each Font in
that family with the right settings... except this feature needs a shader function that only exists on desktop OpenGL,
not mobile OpenGL ES or HTML's WebGL. That feature is automatically enabled on desktop for SDF fonts.

There are several preconfigured font settings in `KnownFonts`; the documentation for each font getter says what files
are needed to use that font. **[The old .fnt files have been moved here](https://github.com/tommyettinger/fonts)**.
[You can see previews and descriptions of most known fonts here.](https://tommyettinger.github.io/textratypist/apidocs/com/github/tommyettinger/textra/KnownFonts.html)
That list can sometimes be outdated, and the latest fonts (that currently use the `.json.lzma` format here)
[can be previewed here for SDF fonts](https://tommyettinger.github.io/textratypist/index.html),
[here for standard fonts](https://tommyettinger.github.io/textratypist/standard.html), or
[here for MSDF fonts](https://tommyettinger.github.io/textratypist/msdf.html).
Having KnownFonts isn't necessary for many fonts since version 1.0.0, because the `.json.lzma` fonts are now made all by
the same tool ([fontwriter](https://github.com/tommyettinger/fontwriter)), and tend to be configured correctly
out-of-the-box. The variety of font types isn't amazing, but it should be a good starting point. One nice thing to note
is the`KnownFonts.getStandardFamily()` method, which requires having 16 fonts in your assets, but naturally lets you
switch between any of those 16 fonts using the `[@Medieval]` syntax (where Medieval is one of the names it knows, in
this case for "KingThings Foundation").

The fonts here use the .json.lzma file extension (which are human-readable .json files inside LZMA compression). They
are compressed versions of larger .json fonts produced by fontwriter. The compression they use is GWT-compatible, so
these .json.lzma files can be used on any platform libGDX targets. You can still use the older .fnt files without issue,
and some .fnt files are still used here (mostly for pixel fonts). You also generally need a .png with each font, though
it can be in an atlas. In version 2.0.0 onward, more types of compression for font files are available, and these almost
always produce smaller files than the .dat files from the 1.x.x series of releases. In particular, `.json.lzma` is
recommended and is the default going forward, but also newly working are `.ubj`, `.ubj.lzma`, and `.json`. `.dat` still
works. While `.ubj.lzma` files are slightly smaller, they don't work on GWT in libGDX 1.13.1 (and neither do .ubj
files). The`.json.lzma` files have the additional advantage that you can extract a valid, human-readable `.json` file
from any `.json.lzma` file using a compatible tool such as [7-Zip](https://www.7-zip.org).

These .json.lzma fonts (and other compressed modes) can be used to load regular BitmapFont objects too, if you aren't
using TextraTypist (such as for TextArea and TextField, which have proven quite challenging to get working...).
[You can use these 3 files to load .json.lzma or other Structured JSON fonts in a non-TextraTypist project](https://github.com/tommyettinger/textratypist/tree/main/src/test/java/com/github/tommyettinger/fontwriter);
you would normally just copy those three files in their package to your own project.

The license files for each font are included in the same folder, in `knownFonts` here. All fonts provided here were
checked to ensure their licenses permit commercial use without fees, and all do. Most require attribution; check the
licenses for details. The needed files are listed in [KnownFonts documentation](https://tommyettinger.github.io/textratypist/apidocs/com/github/tommyettinger/textra/KnownFonts.html)
and, for .json.lzma fonts, [in the fontwriter previews](https://tommyettinger.github.io/fontwriter/).

KnownFonts includes several other ways to configure existing Font instances by adding a TextureAtlas to the glyphs they
know. This includes a few existing TextureAtlases of icons and... emoji!

## Did somebody say emoji?

The [Twemoji](https://github.com/jdecked/twemoji/tree/v15.0.3) icons are also present in an atlas of over-3000 32x32 images;
`KnownFonts.addEmoji()` can register them with
a Font so the `[+name]` syntax mentioned above can draw emoji inline.  Similarly, an atlas of over-4000 60x60 icons is
present from [Game-Icons.net](https://game-icons.net/), and `KnownFonts.addGameIcons()` can register them with a Font.
Both Twemoji and Game-Icons.net atlases cannot be registered in one Font at the same time; there isn't enough free space
in the portion of Unicode they can safely use. A way around this is to use the FontFamily feature, and add a font just
for icons or just for emoji to the family. There's an existing method for this; `KnownFonts.getGameIconsFont()` lets you
obtain a Font that is intended just to display Game-Icons, with some ceremony around its usage. `[@Icons][+rooster][@]`
is a quick example of how you could switch to the Font produced by `getGameIconsFont()`, draw an icon, and switch back.

There are previews for [Twemoji here](https://tommyettinger.github.io/twemoji-atlas/), with the emoji char and name to
look up each image. Likewise,there are previews for
[Game-Icons.net icons here](https://tommyettinger.github.io/game-icons-net-atlas/), with just the name needed to look up
each image. Remember that because the game-icons.net images are pure white with transparency, you can tint them any
color you want using the standard `[RED]`, `[light dull green]`, or `[#0022EEFF]` syntax.

The license files for Twemoji and the Game-Icons.net images are included in `knownFonts`, next to the license files for
fonts. While Twemoji has simple requirements for attribution, Game-Icons requires attribution to quite a few individual
contributors; see the end of this document for the list, which you can and should copy to give credit to everyone.

There are also line-art emoji from [OpenMoji](https://openmoji.org/), and full-color versions of the same emoji. These
may be a better fit for certain projects' art styles. More recently, the detailed full-color
[Noto Emoji](https://fonts.google.com/noto/specimen/Noto+Color+Emoji) have been added, too.

## Act now and get these features, free of charge!

You can rotate individual glyphs (if you draw them individually) or rotate whole blocks of text as a Layout, using an
optional overload of `Font.drawGlyph()` or `Font.drawGlyphs()`. Custom effects for `TypingLabel` can also individually
change the rotation of any glyph, as well as its position and scale on x and/or y. You can rotate a TextraLabel or
TypingLabel by using their `setRotation()` methods, and the rotation will now act correctly for labels with backgrounds
and/or with different alignment settings. The origin for rotations can be set in the label, and the whole label will
rotate around that origin point.

You can also, for some fonts, have box-drawing characters and block elements be automatically generated. This needs a
solid white block character (of any size, typically 1x1) present in the font at id 9608 (the Unicode full block index,
`'\u2588'`). This also enables a better guarantee of underline and strikethrough characters connecting properly, and
without smudging where two underscores or hyphens overlap each other. `Font` attempts to enable this in some cases, or
it can be set with a parameter, but if it fails then it falls back to using underscores for underline and hyphens for
strikethrough. All the fonts in `KnownFonts` either are configured to use a solid block or to specifically avoid it
because that font renders better without it. Note that if you create a `Font` from a libGDX `BitmapFont`, this defaults
to not even trying to make grid glyphs, because BitmapFonts rarely have a suitable solid block char. The underscore and
hyphen method doesn't necessarily position the lines where they should be, because it depends on how `_` and `-` look
in the font, but it's usually sufficient.

Some extra configuration is possible for box drawing characters that are actually used for that purpose (not just
underline or strikethrough). You can set `boxDrawingBreadth` on a `Font` to some multiplier to make box-drawing lines
thicker or thinner, without changing how they connect to each other.

Various features allow extra configuration here. You can set `boldStrength` to some value other than the default 1 if
you want more or less extra space applied from the bold style. You can also set `obliqueStrength` to change the angle
of the skew that oblique text gets drawn with. Colors for various effects can be changed as-needed;
`font.PACKED_SHADOW_COLOR` can be changed to use a darker, lighter, more opaque, or more transparent shadow color, for
instance. `font.PACKED_BLACK` affects the black outline mode, and `font.PACKED_WHITE` affects the white outline and
shiny modes. There's similar modes to change the colors of error, warning, and note underlines. All of these color
configurations apply per Font instance, so you could have two Font objects using the same typeface but with different
colors configured.

## Hold the phone, there's widgets!

Starting in the 0.4.0 release, there are various widgets that replace their
scene2d.ui counterparts and swap out `Label` for `TextraLabel`, allowing you to use markup in them.
The widgets are `ImageTextraButton`, `TextraButton`, `TextraCheckBox`, `TextraDialog`, `TextraLabel`,
`TextraListBox`, `TextraTooltip`, and `TextraWindow`, plus alternate versions of each that use a `TypingLabel` instead
of a `TextraLabel` and have `Typing` in their names.

While `TextArea` is not yet supported a counterpart to `TextArea` is planned, and just hasn't worked yet.
`TextraLabel` defaults to supporting multiple lines, and may be able to stand-in for some usage.
`TypingLabel` permits input tracking, too, so you can use it to make selectable regions of text -- read on!

## What about my input?

Input tracking has been an option for `TypingLabel` and code that uses it since 0.7.0 . This expanded in 0.7.4 to allow
the text in a `TypingLabel` to be made selectable with `label.setSelectable(true)`. You can access the
currently-selected text with `label.getSelectedText()` or copy it directly with `label.copySelectedText()`. When the
user completes a click and drag gesture over the TypingLabel (and it is selectable), an event is triggered as well; you
can listen for `"*SELECTED"` in a `TypingListener` and copy text as soon as it is selected, or only copy when some key
is pressed. Other useful features that use input tracking include the `{LINK}` tag, which makes a span of text a
clickable link to an Internet address, `{TRIGGER}`, which triggers an event on-click, and a few other tags that respond
to mouse hovering (`{ATTENTION}`, `{HIGHLIGHT}`, and `{STYLIST}`). These only work for `TypingLabel`, not `TextraLabel`,
so you may want to use a `TypingLabel` and call `skipToTheEnd()` to treat it like still text that happens to respond to
user input and can use animated styles like `{RAINBOW}`.

## What's this about compatibility?

You can read in a normal scene2d.ui skin JSON file with a variant on libGDX's `Skin` class, `FWSkin` (or one of the
classes that extends it), and doing that will load normal scene2d.ui styles and specialized TextraTypist styles. The
specialized styles are typically only different in that they use `Font` instead of `BitmapFont`, and are all nested in
the `Styles` class here. Having a specialized style means one Font can be reused in more places, without having to make
many copies of a BitmapFont (one per widget, sometimes)... Which was the case before TextraTypist 1.0.0 . Typically,
changing from Skin to FWSkin is straightforward. Code like this before:

```java
Skin skin = new Skin(Gdx.files.internal("my-skin.json"));
```

Would change to this after:

```java
FWSkin skin = new FWSkin(Gdx.files.internal("my-skin.json"));
```

You could also assign a `FWSkin` to a `Skin` variable, and this is the most compatible option, since your skin variable
will just be a normal `Skin`. There are some convenience methods in `FWSkin` to handle distance field fonts a little
more easily, though, so using `FWSkin` where possible is a good idea.

Why is it called `FWSkin`, you wonder? Well, it can load both Font and BitmapFont instances from `.fnt` files (needing
skin configuration only for `BitmapFont`), and can do the same for Structured JSON fonts, which here are usually created
by [fontwriter](https://github.com/tommyettinger/fontwriter), or FW. The initial purpose of `FWSkin` was just to load
from .fnt and .json/.dat font files equally well, but its scope expanded to include the new styles, as well as the newer
compressed JSON file types such as `.json.lzma`.

If you're used to using [Stripe](https://github.com/raeleus/stripe), there's a drop-in replacement that does both what
`FWSkin` does and the FreeType handling that Stripe does. This is the extra `FreeTypist` dependency, available in
[a separate repository](https://github.com/tommyettinger/freetypist). It allows
configuring FreeType by having a `"com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator"` in your skin JSON,
which is often produced by [Skin Composer](https://github.com/raeleus/skin-composer). You can take normal Skins produced by SkinComposer and compatible with
Stripe and use them with FreeTypist.

You can get it via Gradle, but it's probably a better option to just copy in the two files from
[this folder in freetypist](https://github.com/tommyettinger/freetypist/tree/main/src/main/java/com/github/tommyettinger/freetypist)
into your own code. Regardless of how you depend on FreeTypist, it needs a dependency on FreeType (including appropriate
"platform" dependencies) and on TextraTypist (currently 2.0.3). When features are added to FWSkin and TextraTypist in
general, FreeTypist should be updated also.

## Hey, a new major version!

Updating to 2.0.0 or higher from the 1.x series of releases should be straightforward, but it is backwards-incompatible
in some ways. Mostly, this involves changing any minor adjustments for font x/y/width/height, emoji placement, and other
fiddly tweaks that 1.x needed -- but this change is usually just removing those adjustments. Many more parts of
TextraTypist have defaults that "just work," though `.fnt` files are still as finicky as ever. Using `.dat`, or
preferably `.json.lzma` files from the current [knownFonts](knownFonts) folder, is a better option.

In this version and some earlier versions, SDF rendering has improved quite a bit, and now MSDF fonts usually will look
indistinguishable from SDF fonts. The .png files used for MSDF are larger than those for SDF, and SDF fonts can be used
with the normal mode or `SDF_OUTLINE` to give a black outline around all text, so there's generally more upsides to
using SDF than MSDF currently. There are also, rarely, some filtering artifacts that show up in MSDF fonts, but not in
SDF fonts. A major user of TextraTypist, [SquidSquad](https://github.com/yellowstonegames/SquidSquad), now just
distributes standard and SDF fonts, without MSDF fonts included in that repo to save some space.

Rotation may still have issues with underline and strikethrough moving a little. Underline, strikethrough, and "fancy
underlines" now don't apply to emoji or other icons, because they were consistently wrong no matter what I tried, and
because other major users of Twemoji, like Discord, don't draw lines over emoji at all.

Fallbacks to `Gdx.files.local()` have all been removed because they actually could cause the app to stop on GWT if they
ever were run. On desktop, using `Gdx.files.internal()` will check local files (in the same folder) first anyway, so the
local fallback wasn't even very useful.

Wrapping a `Font` in a `TextraLabel` should now behave nearly identically to wrapping a `BitmapFont` in a `Label`. The
few exceptions I've noticed is that some `BitmapFont`s won't consider drop shadow as part of the text for wrapping, or
won't wrap identically when `'.'` or `','` is the last character on a line. I have no idea why that happens.

Many more aspects of font display are configurable now! Things like the black or white outline modes can have their
outline thickness modified using `Font.setOutlineStrength()`. The oblique angle can be configured using
`Font.setObliqueStrength()`, the heaviness of bold styles can be changed with `Font.setBoldStrength()` (typically to
`0.5f` if the bold covers up too much, or `1.5f`, `2f`, or more if it isn't noticeable enough). Just as importantly,
`descent` doesn't need the extreme amount of fiddling it needed in earlier versions, and you can usually just leave it
as it is for Structured JSON fonts!

Version 2.1.0 through 2.1.2 are out, and while they have fewer breaking changes, there are still several of them.
Notably, the syntax for modes is no longer linked to the syntax for scaling, and you can set modes independently of both
the current scale and the current status of an outline around text. Some modes enable the outline and set its color; if
you disable that mode, the outline stays active unless disabled with `[#]`. Using the syntax to revert a change, `[]`,
or clear all formatting, `[ ]`, may help here, along with being able to save a full formatting state with `[(save1)]`
and revert back to it with `[ save1]`. A nice thing is that all scales are now valid, even tiny ones like `[%0.0001]`,
huge ones like `[%9999]`, or unusual ones like `[%456.123]`. Scales are stored in the Layout now, not per-glyph, and
most APIs that dealt with individual Line objects have been removed because they simply wouldn't work without the Layout
that contained that Line and all scaling data for all glyphs. Note that scales are no longer tracked as part of full
formatting state saved with `[(savedState)]`.

There are new modes, like neon and halo, and a mode is now usually enabled with `[?neon]` syntax, even though the older
`[%?neon]` and `[%^neon]` modes are equivalent and present for backwards-compatibility. Small caps mode now has no
special syntax to enable it, nor does Jostle mode, and they are normal modes that have the normal limit of only one mode
being active at a time. Outlining is no longer a mode, though, because it was used so heavily in practice. You can
toggle a black outline on or off with `[#]`, and modes like `[?red outline]` will both enable the outline and set its
color to red (white, blue, and yellow are also alternate options, in addition to the usual black). **Disabling a special
mode** is done with `[?]` now, not `[%]`. You can also revert the full saved state to the default using `[ ]` or to a
named saved state using `[ namedSavedState]`, or just revert one change using `[]`.

You can optionally justify a Layout by setting its
justification and targetWidth, then calling `Font.justify(Layout)`. There are various options for justification, based
roughly on [this libGDX PR by StartsMercury](https://github.com/libgdx/libgdx/pull/7609). Justification is still
considered an experimental feature here, and you have to justify Layouts yourself by calling justify(). It is expected
to work better on a TextraLabel than a TypingLabel.

Various bugs have been fixed, like one where all Unicode characters after `0xF800` would be treated as having a "fancy
underline", and underline/strikethrough have been reworked, again, to behave better with scaled text. Scaling text now
does a better job at respecting a common baseline, rather than the baseline sliding up as text got larger.

2.1.1 and 2.1.2 are only tiny single-issue bug-fix releases. 2.1.1 changes the copy constructor for Font so it now
copies all fields that should be copied. In 2.1.0, a few newly-added fields would not have changes propagated into
copied Fonts. 2.1.2 fixes checks for when the window is minimized, which could have checked incorrectly before.

## Why doesn't something work?

The quick checklist for the latest code (version 2.1.1 or newer commits from JitPack):

- Use FWSkin or one of its subclasses, not a plain scene2d.ui Skin. FreeTypistSkin is fine. Skin is not!
  - You can assign a FWSkin to a Skin, but it still really needs to be an FWSkin internally, or one of its subclasses. 
- Avoid deprecated methods that allocate Font objects without a good way to dispose them.
- Double-check syntax changes for outlined text, which is now an option everywhere, and scaling text using `[%1234]`.
- TextraField is not ready yet! Don't use it. Use a scene2d.ui TextField with a BitmapFont for now.
  - Yes, it is still in need of serious work!
- If a known font can't be found, you should probably copy in the latest version, which likely has changed to a 
  .json.lzma file and has a different .png as well.
- TextraTypist depends on libGDX 1.13.1, and breaking changes in 1.13.5 make that release incompatible.
  - The release expected after 1.13.5 should roll back 1.13.5's breaking changes in most places, making it (hopefully) compatible again.
  - You should avoid depending on 1.13.5 at this point in time, or any dependencies that pull in 1.13.5 .
    - These problematic dependency versions include GDX-TeaVM 1.2.1 and VisUI 1.5.7 . Use earlier versions! 

Some parts of TextraTypist act differently from their counterparts in scene2d.ui and Rafa Skoberg's typing-label.

A big quirk is that `Font` and `BitmapFont` have some core disagreements about how to parse a `.fnt` file, and the
results of creating a `Font` with `new Font("MyFont.fnt")` can be different from
`new Font(new BitmapFont(Gdx.files.internal("MyFont.fnt")))`. `BitmapFont` reads in padding information ([and does so
incorrectly according to the BMFont spec](https://github.com/libgdx/libgdx/pull/4297)), where `Font` ignores padding
information entirely. Some `.fnt` files have been made so they look right in libGDX by using padding, but they will look
wrong in other frameworks/engines without that padding. `Font` compromises by allowing manual adjustment of x and y
position for all glyphs (y often needs to be adjusted, either to a positive or negative value, for `.fnt` fonts, but
usually doesn't for Structured JSON fonts), as well as the width and
height of glyphs (these are useful less frequently, but can be helpful to stretch or squash a font). It may take some
tweaking to get a Font made from a BitmapFont to line up correctly with other widgets. You also may need to adjust the
offsetX, offsetY, and maybe xAdvance parameters if you load an atlas (such as with `addEmoji()` or `addGameIcons()`),
and the adjustments may be quite different for a Font made from a BitmapFont vs. a Font made directly from a .fnt file.
Since 0.8.1, `Font` can parse an extended version of the .fnt format that permits floats for any spatial metrics, and
not just ints. No files actually use this here and now, because the Structured JSON files produced by fontwriter all use
floats internally for everything. In general, Structured JSON fonts in version 2.0.0 and later solve a lot of the
configuration tweaking needed in earlier versions.

If you load text from a file and display it, you can sometimes get different results from creating that text in code, or
loading it on a different machine. This should only happen if the file actually is different -- that is, the files' line
endings use `\r\n` when checked out with Git on a Windows machine, or `\n` on MacOS or Linux machines. TextraTypist uses
`\r` to mark some kinds of "soft" line breaks that can be re-wrapped, and `\n` for "hard" line breaks that must always
create a new line. Having `\r\n` present generally shows up as two lines for every line break. A simple solution that
works for many projects is to include a `.gitattributes` file in your project root, [like the one here](.gitattributes).
This can be used to force all text files or all text files with a certain file extension to use `LF` mode, where only a
single `\n` is used for line breaks. It's still recommended to keep `.bat` files using `CRLF` mode, with `\r\n` used,
for compatibility. Using `.gitattributes` from the start is a good idea, and should keep files exactly the same on all
current OSes. Older Windows programs (like Notepad from Windows 7) aren't able to read `\n` line endings, but the
versions distributed with recent Windows can use `\n` easily, as can almost all code-oriented text editors.

Colors can be written out as hex strings, like `#FF7700`, `#9783EDFF`, or the short form `#F70`, given by name, or 
described using a simple syntax. The full list of (case-sensitive!) names can be seen ordered
[by hue](https://tommyettinger.github.io/textratypist/ColorTableHue.html),
[by lightness](https://tommyettinger.github.io/textratypist/ColorTableLightness.html),
or [by name](https://tommyettinger.github.io/textratypist/ColorTableAlphabetical.html). You can take one or more of
these color names, optionally add adjectives like "light" or "dull", and get a color that mixes the named colors and
applies changes from the adjectives. There are some tricky things here:

  - The only adjectives this understands are "light", "dark", "rich", "dull", "bright", "pale", "deep", and "weak", plus
    the stronger versions of those such as "darker", "palest", and "dullmost". Any adjectives this doesn't know, this
    ignores entirely. Color adjectives are case-insensitive.
    - "light" and "dark" change lightness.
    - "rich" and "dull" change saturation.
    - "bright" raises lightness and raises saturation.
    - "pale" raises lightness and lowers saturation.
    - "deep" lowers lightness and raises saturation.
    - "weak" lowers lightness and lowers saturation.
  - Color names are case-sensitive. Some names are from the libGDX `Colors` class, and are `ALL_CAPS`, sometimes with
    underscores. Other names are from colorful-gdx, and are `lowercased` single words. In a few cases, the same word
    refers to a different color value if you use ALL_CAPS or use lowercase (`ORANGE` and `orange` are a good example).
  - If you have multiple color names in a description, all color names will be mixed using `ColorUtils.unevenMix()`. You
    can have a number after any color name, which assigns a weight to that color for the mixing. Higher numbers will
    cause their preceding color to have more effect on the result; any non-negative integers are allowed.
  - If there isn't a color name present in a description, the result is the int 256 (`0x00000100`), or fully transparent
    very dark blue, which is used as a placeholder because visually it is the same as transparent black. If a color does
    wind up as 256 at the time it is finally rendered, it will probably be ignored.
  - You can add colors to `Palette` with its static `addColor()` method. This makes another color name usable, but won't
    retroactively make that color name parse correctly. You may have to call methods like `Font.markup()` again, so it's
    best if you can change colors before using them.

If you encounter issues with TypingLabel tokens, and you use ProGuard, the configuration for that tool needs a small
addition:
```
-keep class com.github.tommyettinger.textra.** { *; }
```
There may be more strict versions of this ProGuard instruction possible, but at the very least, the
`com.github.tommyettinger.textra.effects` package needs to be kept as-is, for reflection reasons. You may also need to
ensure the `com.github.tommyettinger.textra.Effect` class is kept. Keeping all of TextraTypist should be perfectly fine
for obfuscation purposes because this is an open-source library, but it does add a small amount to the size of the final
JAR or APK. Right now, that appears to be 202 KB if you don't include any assets, so I wouldn't worry about it.

If you're upgrading to TextraTypist 1.0.0 or later, and you haven't changed Skin usage at all, you'll probably encounter
some bugs. These are quick to fix by changing `Skin` to `FWSkin`, or if you used Stripe, `FreeTypistSkin` from
FreeTypist. There is also a `FWSkinLoader` for use with `AssetManager`, and FreeTypist has a `FreeTypistSkinLoader`.
FWSkin allows loading the new types of scene2d.ui styles that reuse Font instances rather than making new ones often.
It also allows loading BitmapFont and Font objects from .fnt, .json, and .dat files (where .dat is the compressed JSON
format this repo uses), requiring only configuration for BitmapFont in the skin .json .

If you're upgrading to TextraTypist 2.1.0 or later, more data is stored in `Layout` and scaling information is no longer
stored in each glyph. This allows new features, like how `[%110]` will actually make text use 110% scale instead of
rounding to 100%. It also allows quite a few new special modes, and changes which modes can overlap. Now any mode can be
used at the same time as an outline, and some modes change the color of the outline. `[#]` on its own toggles a black
outline. Special modes are no longer linked to scaling, and even though the old `[%?shadow]` syntax works, the new
`[?shadow]` syntax without a percent sign is preferred. Small caps no longer is "special", and is enabled like any other
mode: `[?small caps]`. Disabling the current mode can be done with `[?]`, though if a mode also changed an outline
color, you will need to disable the outline separately with `[#]` unless you want to only change its color back to
black. There are various new modes, like `[?neon]` and `[?halo]`, plus new `[?suggest]` and `[?context]` fancy underline
modes, and new `[?yellow outline]` `[?red outline]` `[?blue outline]` to change the outline color.
`[?jostle]` is also a special mode, now without any extra special treatment. In addition to mode and scaling changes in
2.1.0, there are options to specify a layout's justification settings, and to justify a Layout using a Font's justify()
method. This is unlikely to work well for a TypingLabel that is still typing out, and this is still pretty much an
experimental feature. You must call `Font.justify(Layout)` manually on a Layout you want to fit edge-to-edge in its
targetWidth, and as such its targetWidth must already be set to the desired value. For a TextraLabel and its `layout`,
this should work reasonably well, and there are several options for how to justify text in the `Justify` enum.

Distance field fonts might not be worth the hassle of resizing each font's distance field, but they do look much better
at very large sizes than standard fonts. Using a standard font
actually can look better for small-to-moderate size adjustments. The best approach when you don't need large
text seems to be to use a large standard font texture, without SDF or MSDF, and scale it down as needed. Since 1.0.0,
all fonts support emoji. Older versions did not support emoji in MSDF fonts.

Games that use custom `Batch` classes with additional attributes don't work out-of-the-box with `Font`, but it provides
an extension point to allow subclasses to function with whatever attributes the `Batch` needs. Overriding
`Font.drawVertices()` allows quite a lot of flexibility to handle unusual batches, and you can generally leave the
custom Font unchanged other than the `drawVertices()` override. If you implement `Font`'s copy constructor just by
calling `super(font);`, and still allow it to take a Font argument, then you can quickly take Fonts from KnownFonts and
make copies using your subclass. The JavaDocs for `Font.drawVertices()` detail what each of the 20 floats passed in via
an array to drawVertices are expected to do; custom Batches could have 24 or more floats and so would need to put the 20
existing floats in the positions their Batch expects.

The gdx-freetype extension produces BitmapFont outputs, and you can create a Font from a BitmapFont without any issues.
However, FreeType's "Auto" hinting settings both look worse than they normally should with Font, and can trigger the GPU
artifact covered immediately above. Instead of "AutoSlight", "AutoMedium", or "AutoFull" hinting, you can choose
"Slight", "Medium", or "Full", which makes the font look more legible and avoids the GPU half-pixel-offset issue. I
don't have any idea why this happens, but because hinting can be set either in the FreeType generator parameters or (if
you use [Stripe](https://github.com/raeleus/stripe) or [FreeTypist](https://github.com/tommyettinger/freetypist)) set in a Skin file with
`"hinting": "Full"`, it isn't hard to fix.

Underline, strikethrough, and "fancy underlines" like error mode don't apply to emoji or other inline images. This is
the same behavior that some apps that are also heavy emoji users, like Discord, use, and because inline images display
with such different code, simply avoiding the tricky line-through code for them is by far the best solution.

There's other issues with word wrap if you expect it to behave exactly like `Label` in libGDX. Here, we don't break
words, even if a single word is longer than the width of a `TextraLabel` or `TypingLabel`. The reason for this is
twofold: first, breaking words without proper hyphenation logic can change the meaning of those words, and second,
fixing this could be a ton of work. I do intend to try to make this configurable and match `Label` by default in some
near-future version. The word wrap behavior for multiple whitespace characters changed in version 0.10.0, and should be
essentially correct now. Remember that word wrap only makes sense in the context of scene2d.ui for a widget (such as a
TypingLabel or TextraLabel) if that widget has been sized by scene2d.ui, usually by being in a Table cell, or sometimes
by being in a Container. You may need to add a label to a Table or Container, then set the width and/or height of that
Cell or Container, to get wrap to act correctly.

A possibly-frequent issue (with an easy fix) that may start occurring with version 0.9.0 and later is that TextraTypist
now requires Java 8 or higher. All modern desktop OSes support Java 8, and this has been true for 9 years. Android has
supported Java 8 (language level, though only some APIs) for several years, and older versions can use "desugaring" to
translate more-recent Java code to be compatible with (much) older Android versions. GWT has supported language level 8
for years, as well; 2.8.2, which libGDX is built with, allows using Java 8 features, and 2.11.0, which
[an alternate libGDX backend](https://github.com/tommyettinger/gdx-backends) supports, allows using even more. RoboVM
doesn't support any new APIs added in Java 8, but it has supported language level 8 from the start. TextraTypist doesn't
use any APIs from Java 8, but does now use functional interfaces and method references. Having these features allows us
to remove some nasty reflection-based code, and that in turn helps usage on platforms where reflection is limited, such
as GWT and Graal Native Image. GWT was able to work before, but Graal Native Image would have needed a lot of
configuration to be added for every game/app that used TextraTypist. The other issue is that if TextraTypist continued
to target Java 7 for its library code, it wouldn't compile with Java 20 or later, and the LTS release 21 has been out
for over a year.

If you want to make your own Fonts, you can use Hiero or AngelCode BMFont as you always have been able to, but now you
can also use [FontWriter](https://github.com/tommyettinger/fontwriter) (though it is Windows-only for now). FontWriter
can output SDF and MSDF distance field fonts, as well as standard bitmap fonts, and it always ensures the files have
been processed how TextraTypist prefers them (they need a small white square in the lower right to use for block drawing
and underline/strikethrough, plus a specific transparency change makes certain overlapping renders with alpha keep their
intended colors). These processing changes could be done by running `BlockStamper` and `TransparencyProcessor` in the
TextraTypist tests, but that's a hassle, so using FontWriter is preferred. It outputs .json and .dat font files in
earlier versions, as well as .json.lzma, .ubj, and .ubj.lzma files in the current version, plus a vital .png texture.
The .json file is uncompressed, and the .dat, .json.lzma, .ubj, and .ubj.lzma files are all different ways of
compressing that same .json file.
You only need the .png file AND (either the .json file or one of its compressed versions, like .json.lzma), but the
.json.lzma file is small and can be extracted with a program like [7-Zip](https://www.7-zip.org) to get the original
plaint-text JSON file, so it is usually preferred.
The .json file can be hand-edited, but it isn't very easy to do that given how it is inside.

I'm not happy with FontWriter being Windows-only right now, and I'm looking at ways to automatically build it on Linux
and macOS. In the meantime, if you have a freely-usable font (one with a commercially-usable license), feel free to post
an issue on [FontWriter's repo](https://github.com/tommyettinger/fontwriter/issues) with links to the font and its
distributor, and I can make a Structured JSON version of it to host in FontWriter's known fonts. I like this approach
because it helps me better understand what types of fonts people want supplied, and making a new font with FontWriter
doesn't take me more than a few minutes once the license is clear and I have a .ttf or .otf file to work with.

## License

This is based very closely on [typing-label](https://github.com/rafaskb/typing-label), by Rafa Skoberg.
Typing-label is MIT-licensed according to its repo `LICENSE` file, but (almost certainly unintentionally) does not
include any license headers in any files. Since the only requirement of the MIT license is to leave any license text
as-is, this Apache-licensed project is fully compliant with MIT. The full MIT license text is in the file
`typing-label.LICENSE`, and the Apache 2 license for this project is in the file `LICENSE`. Apache license headers are
also present in all library source files here.

The Apache license does not typically apply to non-code resources in the
`src/test/resources` folder; individual fonts have their own licenses stored in that directory. If you copy a font into
your project from the knownFonts folder, you should also copy its license and display it where appropriate for your
project, such as with legal files distributed with your project. If a license requires attribution, you must credit the
author(s) requiring attribution appropriately, such as in your project's credits page.

Twemoji isn't a font, so it might be best to mention it separately. It's licensed under CC-BY 4.0, and requires
attribution to Twitter if used.
[Twemoji's guidelines for attribution are here](https://github.com/jdecked/twemoji/tree/v15.0.3?tab=readme-ov-file#attribution-requirements).
(The documentation still says Twitter, not X, and to my knowledge X doesn't employ any of the active Twemoji team, nor
do they update Twemoji or have an official page for it, so... I would link back to the Twemoji repo, so that it is up to
its developers).

Like Twemoji, Game-Icons.png isn't a font, and it has quite a few contributors to the project. Because all icons in the
project are on one PNG file, you must credit all the contributors who licensed their art under CC-BY, and it may be
ideal just to credit all the contributors, period. The list is [in the license](knownFonts%2FGame-Icons-License.txt).

OpenMoji is also not a font, but it clearly has a CC-BY-SA 4.0 license, and the BY clause should be satisfied by
attributing [the OpenMoji Project](https://openmoji.org/). The SA clause should be satisfied by any users of OpenMoji
continuing to provide attribution. There isn't a non-commercial clause for any assets here.

[Noto Color Emoji also have their own license](knownFonts/Noto-Emoji-License.txt), which is very permissive (OFL) and
should probably be shown with any other legal texts when distributed. 

The [Material Design](https://fonts.google.com/icons?icon.set=Material+Icons) icon set was made by Google and is made
available under [Apache 2.0](knownFonts/Material-Design-License.txt), the same license that TextraTypist uses.
It does not require attribution to use.

The logo was made by Raymond "raeleus" Buckley and contributed to this project. It can be used freely for any purpose,
but I request that it only be used to refer to this project unless substantially modified.

## Thanks

Wow, raeleus has really helped a tremendous amount. Both by testing TextraTypist in his Skin Composer app (which found
quite a lot of bugs, small and large), and advising on proper scene2d.ui layout practices (which were not easy to get
100% right), the large 0.5.2 release (and those after it) would not be what it is today without his input. Thank you!

Thanks to fraudo for helping me go step-by-step to figure out how badly I had screwed up rotation with backgrounds, and
for writing most of `LabelRotationTest`. Release 0.5.5 would still probably be in development for months without that
help, so thanks are in order.

Thanks to piotr-j (evilentity), mas omenos, and DMC from the libGDX Discord, for really thoroughly testing TextraTypist.
`IncongruityTest` was originally piotr-j's work, and it helped me figure out which fonts in KnownFonts had incorrect
bounds information. `TableWrapTest` was based closely on mas omenos' work, and was useful to locate a wrapping bug. DMC
managed to track down a very elusive ProGuard issue, which is now documented in this README.md , as well as noticing and
helping debug a variety of issues with code that I had no idea people were already using. Sanda Moen, fourlastor,
tecksup, and Siavash Ranbar helped track down some maddening bugs affecting word wrap; thanks to everyone who's put up
with those kinds of bug! IgorApplications has helped track down various SDF-related bugs and pointed out that a feature
(full-color emoji in SDF fonts) was possible, so thanks as well! Thanks to trurl101 for finding the (rather serious)
bug fixed by version 2.1.2 !

Of course, I have to thank Rafa Skoberg for writing quite a lot of the code here! About 1/3 of the effects are almost
purely by Rafa, much of the TypingLabel-related code is nearly unchanged from his work, and in general he showed what
libGDX UIs could be just by making the initial code.

Thanks to all the font designers who made fonts we use here; by making your fonts freely available, you perform a great
service to the people who depend on them.

Thanks to Twitter for generously contributing Twemoji to the world of open source; having broadly available emoji makes
them much more usable. Note that because this was a generous action by Twitter, it happened before its
acquisition/change to "X". Additional thanks to the several hard-working contributors who have continued working on
Twemoji even after exiting their former employer! We use the [jdecked/twemoji](https://github.com/jdecked/twemoji) fork.

Thanks to the many contributors to game-icons.net for producing high-quality free icons to game developers everywhere.
The icons in Game-Icons.png were made by:

- Lorc, http://lorcblog.blogspot.com
- Delapouite, https://delapouite.com
- John Colburn, http://ninmunanmu.com
- Felbrigg, http://blackdogofdoom.blogspot.co.uk
- John Redman, http://www.uniquedicetowers.com
- Carl Olsen, https://twitter.com/unstoppableCarl
- Sbed, http://opengameart.org/content/95-game-icons
- PriorBlue
- Willdabeast, http://wjbstories.blogspot.com
- Viscious Speed, http://viscious-speed.deviantart.com
- Lord Berandas, http://berandas.deviantart.com
- Irongamer, http://ecesisllc.wix.com/home
- HeavenlyDog, http://www.gnomosygoblins.blogspot.com
- Lucas
- Faithtoken, http://fungustoken.deviantart.com
- Skoll
- Andy Meneely, http://www.se.rit.edu/~andy/
- Cathelineau
- Kier Heyl
- Aussiesim
- Sparker, http://citizenparker.com
- Zeromancer
- Rihlsul
- Quoting
- Guard13007, https://guard13007.com
- DarkZaitzev, http://darkzaitzev.deviantart.com
- SpencerDub
- GeneralAce135
- Zajkonur
- Catsu
- Starseeker
- Pepijn Poolman
- Pierre Leducq
- Caro Asercion

(Projects that use TextraTypist can copy the above list of Game-Icons.png contributors to comply with its license.)

Thanks again to [the OpenMoji project](https://openmoji.org/)! That was clearly a lot of work. OpenMoji is licensed as
[CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/#), so it requires attribution if you use it by distributing OpenMoji assets with your product (the
"BY" clause). The "SA" (share-alike) clause is also a requirement for TextraTypist itself; because TextraTypist edited
and built upon the icons while compiling them into an atlas, the icon atlas is licensed under CC BY-SA 4.0 as well. 

Thanks also to the developers of the [Noto Fonts and Emoji](https://fonts.google.com/noto/specimen/Noto+Color+Emoji)!
They are OFL 1.1 licensed. The Noto Color Emoji here also used data from the MIT-licensed
[EmojiBase](https://github.com/milesj/emojibase/tree/master) project to create the atlas. Both OFL and MIT do not require attribution to use.

The [Material Design](https://fonts.google.com/icons?icon.set=Material+Icons) icon set, made by Google for use in
Android, is also present here, and has many icons that have no equivalent in Unicode's emoji set. Material icons are an
especially good fit for user interfaces in productivity apps or editors. The Material Design icons are licensed as
Apache 2.0, and as such do not require attribution to use.

## Historical and Special-Case Troubleshooting

If you happen to use both tommyettinger's TextraTypist library and tommyettinger's
[colorful-gdx](https://github.com/tommyettinger/colorful-gdx) library, you may encounter various issues. `ColorfulBatch`
appeared to be incompatible because it uses an extra attribute per-vertex (compared to SpriteBatch), but an adjustment
it already does seems to make it compatible without changes. Color description can be done by both
colorful-gdx's `SimplePalette` and `ColorUtils.describe()` here, but descriptions would really need to use the RGBA
color space to work as expected. Alternative shaders from colorful-gdx's `Shaders` class generally won't work correctly
with the known fonts here and the defaults for neutral colors (here, white is the neutral color, but in most shaders
that allow lightening, 50% gray is the neutral color). The easiest solution for all this is to use a normal, vanilla
`SpriteBatch` for TextraTypist rendering, and whatever `ShaderProgram` or `ColorfulBatch` you want for colorful-gdx
rendering.

Sometimes, you may need to enable or disable integer positioning for certain fonts to avoid a strange GPU-related visual
artifact that seems to only happen on some Nvidia GPUs. When this happens, glyphs may appear a half-pixel or so away
from where they should be, in seemingly randomly-picked directions. It looks awful, and the integer position code at
least should resolve it most of the time. Integer positions don't work well if you use world units that span multiple
pixels in length, but this bug is an absolute mystery, and also doesn't happen at all on integrated GPUs, and may not
happen on AMD GPUs. How it behaves on Apple Silicon graphics, I also do not know. The Issues tab is always available for
anyone who wants to try to debug this! It is possible that some fixes introduced in the 0.7.x releases may have already
eliminated this bug, but I'm not especially optimistic that it is always gone.

There are some known issues with scaling, rotation, and integer-positioning in 0.7.5 onward. You may see labels
slide a little relatively to their backgrounds when rotated smoothly, and some (typically very small) fonts may need
integer positions enabled to keep a stable baseline. Font debug lines may be quite incorrect in some of these versions,
also, even if the text displays correctly to users. Scaling has improved significantly in 0.7.8, as has the handling of
debug lines, but rotation still has some subtle bugs. A bug was fixed starting in 0.8.0 that made extra images in a Font
(such as emoji) scale differently and drift when the Font they were mixed with scaled. That same bug also made an
ordinary Font drift slightly as its scale changed; this is also fixed. Positions and sizes for background color and for
images from an atlas have improved in 0.8.2, so selecting text shouldn't cover up text as badly with the background, and
emoji should be fully surrounded by their selection background. Positions along the line vertically, while the text is
scaled, improved in 0.8.3 so that the scaling is relative to the center of the line, rather than the bottom of the line.
Some other code already expected scaling to be centered like that, so this change makes scaling look better, usually.
In 0.9.0, integer positioning can still be set, but it does nothing; in practice, setting it was causing more problems
than improvements. The few fonts that one would think would need integer positions (pixel fonts) actually look better
without it. There are still some rotation issues in 0.9.0, though they mostly happen when the descent is configured to
an extreme value, or sometimes other metrics. Lining up underline/strikethrough with rotated text is also a challenge,
and it doesn't work fully even in the most recent versions. The underline and strikethrough glide slightly around where
they should be for correctly rotated text, and fixing this in all cases has been just about impossible. The gliding is,
at least, very subtle now.

Word wrap periodically seems to break and need fixing across different releases. The most recent time this happened was
in 0.7.9, which also affected 0.8.0 and was fixed (I hope) in 0.8.1. A different wrapping-related bug was fixed more
recently, in 0.8.3 ; this was rare, and only affected TypingLabel when some effects were present. Word wrap should
behave identically to BitmapFont when the metrics line up exactly, with some key exceptions in version 2.0.0: the chars
`'.'` and `','` (at least those, and likely more) will sometimes wrap earlier with `Font` by about 1 pixel than with
`BitmapFont`. This can affect when things wrap in practice, though not always, and it would add an extra line or a few
when it does happen.

~~In version 0.7.4 and earlier, you would an earlier version of both dependencies (note, **this is an old version**):~~

```groovy
// OLD VERSION
implementation "com.github.tommyettinger:textratypist:0.7.4:sources"
implementation "com.github.tommyettinger:regexodus:0.1.13:sources"
```

~~and would use these GWT inherits instead:~~
```xml
<!-- OLD VERSION -->
<inherits name="regexodus" />
<inherits name="textratypist" />
```

