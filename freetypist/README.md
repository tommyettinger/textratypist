# freetypist

Provides FreeType font loading for TextraTypist.

You can load anything FWSkin (from TextraTypist) can load, such as .fnt and (optionally compressed) Structured JSON
files from FontWriter, as well as anything FreeTypeSkin (from Stripe) can load, such as FreeType font config.

This depends on FreeType, so you must have the appropriate platform dependencies for that, if you don't already.

# Dependency

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
into your own project, which is probably the easiest route. This is what TextraTypist does for its tests.

# License

[Apache 2.0](LICENSE), the same as TextraTypist.


