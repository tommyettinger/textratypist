# freetypist

Provides FreeType font loading for TextraTypist.

You can load anything FWSkin (from TextraTypist) can load, such as .fnt and (optionally compressed) Structured JSON
files from FontWriter, as well as anything FreeTypeSkin (from Stripe) can load, such as FreeType font config. This also
will load TextraTypist's Styles.Whatever types when it loads the corresponding scene2d.ui style.

This depends on FreeType, so you must have the appropriate platform dependencies for that, if you don't already.

# Dependency

Using Maven Central:

```gradle
implementation 'com.github.tommyettinger:freetypist:1.0.1'
```

Using JitPack:
(Instead of `53162a640a`, [you can use any recent commit listed here](https://jitpack.io/#tommyettinger/textratypist)
under Commits.)

```gradle
implementation 'com.github.tommyettinger.textratypist:freetypist:53162a640a'
```

You could also just copy the two source files,
[FreeTypistSkin.java](src/main/java/com/github/tommyettinger/freetypist/FreeTypistSkin.java)
and
[FreeTypistSkinLoader.java](src/main/java/com/github/tommyettinger/freetypist/FreeTypistSkinLoader.java),
into your own project, which is probably the easiest route. This is what TextraTypist does for its tests. You
still will need the FreeType dependencies, including its platform dependencies.

# License

[Apache 2.0](LICENSE), the same as TextraTypist.


