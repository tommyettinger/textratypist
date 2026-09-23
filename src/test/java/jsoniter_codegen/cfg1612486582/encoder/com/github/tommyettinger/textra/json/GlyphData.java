package jsoniter_codegen.cfg1612486582.encoder.com.github.tommyettinger.textra.json;
public class GlyphData implements com.github.tommyettinger.jsonbiter.spi.Encoder {
public void encode(java.lang.Object obj, com.github.tommyettinger.jsonbiter.output.JsonStream stream) throws java.io.IOException {
if (obj == null) { stream.writeNull(); return; }
encode_((com.github.tommyettinger.textra.json.GlyphData)obj, stream);
}
public static void encode_(com.github.tommyettinger.textra.json.GlyphData obj, com.github.tommyettinger.jsonbiter.output.JsonStream stream) throws java.io.IOException {
stream.writeObjectStart();
boolean notFirst = false;
if (!(0 == obj.unicode)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("unicode");
stream.writeVal((int)obj.unicode);
}
if (!(0 == obj.advance)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("advance");
stream.writeVal((float)obj.advance);
}
if (!(null == obj.planeBounds)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("planeBounds");

jsoniter_codegen.cfg1612486582.encoder.com.github.tommyettinger.textra.json.BoundsData.encode_((com.github.tommyettinger.textra.json.BoundsData)obj.planeBounds, stream);

}
if (!(null == obj.atlasBounds)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("atlasBounds");

jsoniter_codegen.cfg1612486582.encoder.com.github.tommyettinger.textra.json.BoundsData.encode_((com.github.tommyettinger.textra.json.BoundsData)obj.atlasBounds, stream);

}
if (notFirst) { stream.writeObjectEnd(); } else { stream.write('}'); }
}
}
