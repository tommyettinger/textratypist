If you use GWT, this should be compatible. It needs these dependencies in the html module:

implementation "com.github.tommyettinger:textratypist:2.2.15:sources"
implementation "com.github.tommyettinger:regexodus:0.1.21:sources"

GWT also needs these `<inherits>` lines in the `GdxDefinition.gwt.xml` file:

<inherits name="regexodus.regexodus" />
<inherits name="com.github.tommyettinger.textratypist" />

RegExodus is the GWT-compatible regular-expression library that TextraTypist uses to match some complex patterns internally.