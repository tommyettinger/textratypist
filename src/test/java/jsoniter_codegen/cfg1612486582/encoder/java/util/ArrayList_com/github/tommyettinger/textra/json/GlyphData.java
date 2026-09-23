package jsoniter_codegen.cfg1612486582.encoder.java.util.ArrayList_com.github.tommyettinger.textra.json;
public class GlyphData implements com.github.tommyettinger.jsonbiter.spi.Encoder {
public void encode(java.lang.Object obj, com.github.tommyettinger.jsonbiter.output.JsonStream stream) throws java.io.IOException {
if (obj == null) { stream.writeNull(); return; }
encode_((java.util.ArrayList)obj, stream);
}
public static void encode_(java.lang.Object obj, com.github.tommyettinger.jsonbiter.output.JsonStream stream) throws java.io.IOException {
java.util.List list = (java.util.List)obj;
int size = list.size();
if (size == 0) { stream.write((byte)'[', (byte)']'); return; }
stream.writeArrayStart(); stream.writeIndentation();
java.lang.Object e = list.get(0);
if (e == null) { stream.writeNull(); } else {

jsoniter_codegen.cfg1612486582.encoder.com.github.tommyettinger.textra.json.GlyphData.encode_((com.github.tommyettinger.textra.json.GlyphData)e, stream);

}
for (int i = 1; i < size; i++) {
stream.writeMore();
e = list.get(i);
if (e == null) { stream.writeNull(); } else {

jsoniter_codegen.cfg1612486582.encoder.com.github.tommyettinger.textra.json.GlyphData.encode_((com.github.tommyettinger.textra.json.GlyphData)e, stream);

}
}
stream.writeArrayEnd();
}
}
