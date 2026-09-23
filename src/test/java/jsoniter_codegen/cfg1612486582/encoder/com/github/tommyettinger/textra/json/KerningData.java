package jsoniter_codegen.cfg1612486582.encoder.com.github.tommyettinger.textra.json;
public class KerningData implements com.github.tommyettinger.jsonbiter.spi.Encoder {
public void encode(java.lang.Object obj, com.github.tommyettinger.jsonbiter.output.JsonStream stream) throws java.io.IOException {
if (obj == null) { stream.writeNull(); return; }
encode_((com.github.tommyettinger.textra.json.KerningData)obj, stream);
}
public static void encode_(com.github.tommyettinger.textra.json.KerningData obj, com.github.tommyettinger.jsonbiter.output.JsonStream stream) throws java.io.IOException {
stream.writeObjectStart();
boolean notFirst = false;
if (!(0 == obj.unicode1)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("unicode1");
stream.writeVal((int)obj.unicode1);
}
if (!(0 == obj.unicode2)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("unicode2");
stream.writeVal((int)obj.unicode2);
}
if (!(0 == obj.advance)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("advance");
stream.writeVal((float)obj.advance);
}
if (notFirst) { stream.writeObjectEnd(); } else { stream.write('}'); }
}
}
