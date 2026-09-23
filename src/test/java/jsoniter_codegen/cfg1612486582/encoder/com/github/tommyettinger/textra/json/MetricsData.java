package jsoniter_codegen.cfg1612486582.encoder.com.github.tommyettinger.textra.json;
public class MetricsData implements com.github.tommyettinger.jsonbiter.spi.Encoder {
public void encode(java.lang.Object obj, com.github.tommyettinger.jsonbiter.output.JsonStream stream) throws java.io.IOException {
if (obj == null) { stream.writeNull(); return; }
encode_((com.github.tommyettinger.textra.json.MetricsData)obj, stream);
}
public static void encode_(com.github.tommyettinger.textra.json.MetricsData obj, com.github.tommyettinger.jsonbiter.output.JsonStream stream) throws java.io.IOException {
stream.writeObjectStart();
boolean notFirst = false;
if (!(0 == obj.emSize)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("emSize");
stream.writeVal((float)obj.emSize);
}
if (!(0 == obj.lineHeight)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("lineHeight");
stream.writeVal((float)obj.lineHeight);
}
if (!(0 == obj.ascender)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("ascender");
stream.writeVal((float)obj.ascender);
}
if (!(0 == obj.descender)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("descender");
stream.writeVal((float)obj.descender);
}
if (!(0 == obj.underlineY)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("underlineY");
stream.writeVal((float)obj.underlineY);
}
if (!(0 == obj.underlineThickness)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("underlineThickness");
stream.writeVal((float)obj.underlineThickness);
}
if (notFirst) { stream.writeObjectEnd(); } else { stream.write('}'); }
}
}
