package jsoniter_codegen.cfg1612486582.encoder.com.github.tommyettinger.textra.json;
public class FontData implements com.github.tommyettinger.jsonbiter.spi.Encoder {
public void encode(java.lang.Object obj, com.github.tommyettinger.jsonbiter.output.JsonStream stream) throws java.io.IOException {
if (obj == null) { stream.writeNull(); return; }
encode_((com.github.tommyettinger.textra.json.FontData)obj, stream);
}
public static void encode_(com.github.tommyettinger.textra.json.FontData obj, com.github.tommyettinger.jsonbiter.output.JsonStream stream) throws java.io.IOException {
stream.writeObjectStart();
boolean notFirst = false;
if (!(null == obj.atlas)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("atlas");

jsoniter_codegen.cfg1612486582.encoder.com.github.tommyettinger.textra.json.AtlasData.encode_((com.github.tommyettinger.textra.json.AtlasData)obj.atlas, stream);

}
if (!(null == obj.metrics)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("metrics");

jsoniter_codegen.cfg1612486582.encoder.com.github.tommyettinger.textra.json.MetricsData.encode_((com.github.tommyettinger.textra.json.MetricsData)obj.metrics, stream);

}
if (!(null == obj.glyphs)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("glyphs");

jsoniter_codegen.cfg1612486582.encoder.java.util.ArrayList_com.github.tommyettinger.textra.json.GlyphData.encode_((java.util.ArrayList)obj.glyphs, stream);

}
if (!(null == obj.kerning)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("kerning");

jsoniter_codegen.cfg1612486582.encoder.java.util.ArrayList_com.github.tommyettinger.textra.json.KerningData.encode_((java.util.ArrayList)obj.kerning, stream);

}
if (notFirst) { stream.writeObjectEnd(); } else { stream.write('}'); }
}
}
