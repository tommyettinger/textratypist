package jsoniter_codegen.cfg1612486582.encoder.com.github.tommyettinger.textra.json;
public class AtlasData implements com.github.tommyettinger.jsonbiter.spi.Encoder {
public void encode(java.lang.Object obj, com.github.tommyettinger.jsonbiter.output.JsonStream stream) throws java.io.IOException {
if (obj == null) { stream.writeNull(); return; }
encode_((com.github.tommyettinger.textra.json.AtlasData)obj, stream);
}
public static void encode_(com.github.tommyettinger.textra.json.AtlasData obj, com.github.tommyettinger.jsonbiter.output.JsonStream stream) throws java.io.IOException {
stream.writeObjectStart();
boolean notFirst = false;
if (!(null == obj.type)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("type");
stream.writeVal((java.lang.String)obj.type);
}
if (!(0 == obj.distanceRange)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("distanceRange");
stream.writeVal((float)obj.distanceRange);
}
if (!(0 == obj.distanceRangeMiddle)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("distanceRangeMiddle");
stream.writeVal((float)obj.distanceRangeMiddle);
}
if (!(0 == obj.size)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("size");
stream.writeVal((float)obj.size);
}
if (!(0 == obj.width)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("width");
stream.writeVal((float)obj.width);
}
if (!(0 == obj.height)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("height");
stream.writeVal((float)obj.height);
}
if (!(null == obj.yOrigin)) {
if (notFirst) { stream.writeMore(); } else { stream.writeIndentation(); notFirst = true; }
stream.writeObjectField("yOrigin");
stream.writeVal((java.lang.String)obj.yOrigin);
}
if (notFirst) { stream.writeObjectEnd(); } else { stream.write('}'); }
}
}
