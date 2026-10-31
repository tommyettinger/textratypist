/**
 * Specific to JsonBiter/Jsoniter, so probably not useful anywhere else.
 * <br>
 * This was part of an experiment to see if loading .json.lzma fonts using alternative JSON libraries (not libGDX Json)
 * could speed up loading times. In short, both Fory-JSON and JsonBiter had enough startup overhead doing dynamic
 * class building to overshadow the time needed to load a relatively-small .json.lzma file.
 * The experiment was stopped there.
 */
public class StaticGenerator {
    public static void main(String[] args) throws Exception {
        com.github.tommyettinger.jsonbiter.static_codegen.StaticCodegen.main(new String[]{"com.github.tommyettinger.textra.json.FontConfig"});
    }
}
