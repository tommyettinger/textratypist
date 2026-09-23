package jsoniter_codegen.cfg1612486582.encoder.com.github.tommyettinger.textra.json;
public class BoundsData implements com.github.tommyettinger.jsonbiter.spi.Encoder {
public void encode(java.lang.Object obj, com.github.tommyettinger.jsonbiter.output.JsonStream stream) throws java.io.IOException {
if (obj == null) { stream.writeNull(); return; }
encode_((com.github.tommyettinger.textra.json.BoundsData)obj, stream);
}
public static void encode_(com.github.tommyettinger.textra.json.BoundsData obj, com.github.tommyettinger.jsonbiter.output.JsonStream stream) throws java.io.IOException {
stream.writeObjectStart();
boolean notFirst = false;
if (!(0 == obj.left)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("left");
stream.writeVal((float)obj.left);
}
if (!(0 == obj.bottom)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("bottom");
stream.writeVal((float)obj.bottom);
}
if (!(0 == obj.right)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("right");
stream.writeVal((float)obj.right);
}
if (!(0 == obj.top)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("top");
stream.writeVal((float)obj.top);
}
if (notFirst) { stream.writeObjectEnd(); } else { stream.write('}'); }
}
}
