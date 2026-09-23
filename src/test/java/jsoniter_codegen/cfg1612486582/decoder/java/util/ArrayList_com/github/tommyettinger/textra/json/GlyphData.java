package jsoniter_codegen.cfg1612486582.decoder.java.util.ArrayList_com.github.tommyettinger.textra.json;
public class GlyphData implements com.github.tommyettinger.jsonbiter.spi.Decoder {
public static java.lang.Object decode_(com.github.tommyettinger.jsonbiter.JsonIterator iter) throws java.io.IOException { java.util.ArrayList col = (java.util.ArrayList)com.github.tommyettinger.jsonbiter.CodegenAccess.resetExistingObject(iter);
if (iter.readNull()) { com.github.tommyettinger.jsonbiter.CodegenAccess.resetExistingObject(iter); return null; }
if (!com.github.tommyettinger.jsonbiter.CodegenAccess.readArrayStart(iter)) {
return col == null ? new java.util.ArrayList(0): (java.util.ArrayList)com.github.tommyettinger.jsonbiter.CodegenAccess.reuseCollection(col);
}
Object a1 = (com.github.tommyettinger.textra.json.GlyphData)jsoniter_codegen.cfg1612486582.decoder.com.github.tommyettinger.textra.json.GlyphData.decode_(iter);
if (com.github.tommyettinger.jsonbiter.CodegenAccess.nextToken(iter) != ',') {
java.util.ArrayList obj = col == null ? new java.util.ArrayList(1): (java.util.ArrayList)com.github.tommyettinger.jsonbiter.CodegenAccess.reuseCollection(col);
obj.add(a1);
return obj;
}
Object a2 = (com.github.tommyettinger.textra.json.GlyphData)jsoniter_codegen.cfg1612486582.decoder.com.github.tommyettinger.textra.json.GlyphData.decode_(iter);
if (com.github.tommyettinger.jsonbiter.CodegenAccess.nextToken(iter) != ',') {
java.util.ArrayList obj = col == null ? new java.util.ArrayList(2): (java.util.ArrayList)com.github.tommyettinger.jsonbiter.CodegenAccess.reuseCollection(col);
obj.add(a1);
obj.add(a2);
return obj;
}
Object a3 = (com.github.tommyettinger.textra.json.GlyphData)jsoniter_codegen.cfg1612486582.decoder.com.github.tommyettinger.textra.json.GlyphData.decode_(iter);
if (com.github.tommyettinger.jsonbiter.CodegenAccess.nextToken(iter) != ',') {
java.util.ArrayList obj = col == null ? new java.util.ArrayList(3): (java.util.ArrayList)com.github.tommyettinger.jsonbiter.CodegenAccess.reuseCollection(col);
obj.add(a1);
obj.add(a2);
obj.add(a3);
return obj;
}
Object a4 = (com.github.tommyettinger.textra.json.GlyphData)jsoniter_codegen.cfg1612486582.decoder.com.github.tommyettinger.textra.json.GlyphData.decode_(iter);
java.util.ArrayList obj = col == null ? new java.util.ArrayList(8): (java.util.ArrayList)com.github.tommyettinger.jsonbiter.CodegenAccess.reuseCollection(col);
obj.add(a1);
obj.add(a2);
obj.add(a3);
obj.add(a4);
while (com.github.tommyettinger.jsonbiter.CodegenAccess.nextToken(iter) == ',') {
obj.add((com.github.tommyettinger.textra.json.GlyphData)jsoniter_codegen.cfg1612486582.decoder.com.github.tommyettinger.textra.json.GlyphData.decode_(iter));
}
return obj;
}public java.lang.Object decode(com.github.tommyettinger.jsonbiter.JsonIterator iter) throws java.io.IOException {
return decode_(iter);
}
}
