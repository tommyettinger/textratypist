# textratypist
Extra features for something like typing-label, with both effects and styles.

What does this look like? A little something like this...

![Animated preview](images/preview.gif)

## It's a label!

TypingLabel is a fairly normal scene2d.ui widget, much like a Label from libGDX. However, it puts letters up on
the screen one at a time, unless it is told to skip ahead. This is a nostalgic effect found in many older text-heavy
games, and it looks like a typewriter is putting up each letter at some slower-than-instantaneous rate.

## It's got effects!

Yes, it has more than the typewriter mode! Text can hang above and then drop into place. It can jump up and down in a
long wave. It can waver and shudder, as if it is sick. It can blink in different colors, move in a gradient smoothly
between two colors, or go across a whole rainbow. Lots of options; lots of fun. Effects are exactly the same as in
typing-label, so you should consult [its documentation](https://github.com/rafaskb/typing-label/wiki/Examples) for now.

## And now, it's got style!

This library extends what the original typing-label can do -- it allows styles to be applied to text, such as bold,
underline, oblique, superscript, etc. The full list of styles is long, but isn't as detailed as the effect tokens. You
can enable styles with something like libGDX color markup, in square brackets like `[*]`, or you can use `{STYLE=BOLD}`
to do the same thing. The full list of styles:

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
- `[#HHHHHHHH]`, where HHHHHHHH is a hex RGB888 or RGBA8888 int color, changes the color. This is a normal `{COLOR=#HHHHHHHH}` tag.
- `[COLORNAME]`, where COLORNAME is a typically-upper-case color name that will be looked up externally, changes the color.
  - By default, this looks up COLORNAME in libGDX's `Colors` class, but it can be configured to create colors differently.
    - You can use `Font`'s `setColorLookup()` method with your own `ColorLookup` implementation to do what you want here. 
  - The name can optionally be preceded by `|`, which allows looking up colors with names that contain punctuation.
      For example, `[|;_;]` would look up a color called `;_;`, "the color of sadness," and would not act like `[;]`.
  - This also can be used with a color tag, such as `{COLOR=SKY}` (which Colors can handle right away) or
    `{COLOR=Lighter Orange-Red}` (which would need that color to be defined).

## How do I get it?

You probably want to get this with Gradle! The dependency for a libGDX project's core module looks like:

```groovy
implementation "com.github.tommyettinger:textratypist:0.1.0"
```

If you use GWT, this should be compatible. It needs these dependencies in the html module:

```groovy
implementation "com.github.tommyettinger:textratypist:0.1.0:sources"
implementation "com.github.tommyettinger:regexodus:0.1.13:sources"
```

GWT also needs this in the GdxDefinition.gwt.xml file:
```xml
<inherits name="regexodus" />
<inherits name="textratypist" />
```

RegExodus is the GWT-compatible regular-expression library this uses to match some complex patterns internally. Other
than libGDX itself, RegExodus is the only dependency this project has.

There is at least one release in the [Releases](https://github.com/tommyettinger/textratypist/releases) section of this
repo, but you're still encouraged to use Gradle to handle this library and its dependencies.

## License

This is based very closely on [typing-label](https://github.com/rafaskb/typing-label), by Rafa Skoberg.
Typing-label is MIT-licensed according to its repo `LICENSE` file, but (almost certainly unintentionally) does not
include any license headers in any files. Since the only requirement of the MIT license is to leave any license text
as-is, this Apache-licensed project is fully compliant with MIT. The full MIT license text is in the file
`typing-label.LICENSE`, and the Apache 2 license for this project is in the file `LICENSE`. Apache license headers are
also present in all library source files here. The Apache license does not typically apply to non-code resources in the
`src/test/resources` folder; individual fonts have their own licenses stored in that directory.
