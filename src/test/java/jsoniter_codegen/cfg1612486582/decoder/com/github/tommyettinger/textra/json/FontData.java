package jsoniter_codegen.cfg1612486582.decoder.com.github.tommyettinger.textra.json;
public class FontData implements com.github.tommyettinger.jsonbiter.spi.Decoder {
public static java.lang.Object decode_(com.github.tommyettinger.jsonbiter.JsonIterator iter) throws java.io.IOException { java.lang.Object existingObj = com.github.tommyettinger.jsonbiter.CodegenAccess.resetExistingObject(iter);
if (iter.readNull()) { return null; }
com.github.tommyettinger.textra.json.FontData obj = (existingObj == null ? new com.github.tommyettinger.textra.json.FontData() : (com.github.tommyettinger.textra.json.FontData)existingObj);
if (!com.github.tommyettinger.jsonbiter.CodegenAccess.readObjectStart(iter)) {
return obj;
}
com.github.tommyettinger.jsonbiter.spi.Slice field = com.github.tommyettinger.jsonbiter.CodegenAccess.readObjectFieldAsSlice(iter);
boolean once = true;
while (once) {
once = false;
switch (field.len()) {
case 5: 
if (
field.at(0)==97 && 
field.at(1)==116 && 
field.at(2)==108 && 
field.at(3)==97 && 
field.at(4)==115
) {
com.github.tommyettinger.jsonbiter.CodegenAccess.setExistingObject(iter, obj.atlas);obj.atlas= (com.github.tommyettinger.textra.json.AtlasData)jsoniter_codegen.cfg1612486582.decoder.com.github.tommyettinger.textra.json.AtlasData.decode_(iter);
continue;
}
break;
case 6: 
if (
field.at(0)==103 && 
field.at(1)==108 && 
field.at(2)==121 && 
field.at(3)==112 && 
field.at(4)==104 && 
field.at(5)==115
) {
com.github.tommyettinger.jsonbiter.CodegenAccess.setExistingObject(iter, obj.glyphs);obj.glyphs= (java.util.ArrayList)jsoniter_codegen.cfg1612486582.decoder.java.util.ArrayList_com.github.tommyettinger.textra.json.GlyphData.decode_(iter);
continue;
}
break;
case 7: 
if (
field.at(0)==107 && 
field.at(1)==101 && 
field.at(2)==114 && 
field.at(3)==110 && 
field.at(4)==105 && 
field.at(5)==110 && 
field.at(6)==103
) {
com.github.tommyettinger.jsonbiter.CodegenAccess.setExistingObject(iter, obj.kerning);obj.kerning= (java.util.ArrayList)jsoniter_codegen.cfg1612486582.decoder.java.util.ArrayList_com.github.tommyettinger.textra.json.KerningData.decode_(iter);
continue;
}
if (
field.at(0)==109 && 
field.at(1)==101 && 
field.at(2)==116 && 
field.at(3)==114 && 
field.at(4)==105 && 
field.at(5)==99 && 
field.at(6)==115
) {
com.github.tommyettinger.jsonbiter.CodegenAccess.setExistingObject(iter, obj.metrics);obj.metrics= (com.github.tommyettinger.textra.json.MetricsData)jsoniter_codegen.cfg1612486582.decoder.com.github.tommyettinger.textra.json.MetricsData.decode_(iter);
continue;
}
break;

}
iter.skip();
}
while (com.github.tommyettinger.jsonbiter.CodegenAccess.nextToken(iter) == ',') {
field = com.github.tommyettinger.jsonbiter.CodegenAccess.readObjectFieldAsSlice(iter);
switch (field.len()) {
case 5: 
if (
field.at(0)==97 && 
field.at(1)==116 && 
field.at(2)==108 && 
field.at(3)==97 && 
field.at(4)==115
) {
com.github.tommyettinger.jsonbiter.CodegenAccess.setExistingObject(iter, obj.atlas);obj.atlas= (com.github.tommyettinger.textra.json.AtlasData)jsoniter_codegen.cfg1612486582.decoder.com.github.tommyettinger.textra.json.AtlasData.decode_(iter);
continue;
}
break;
case 6: 
if (
field.at(0)==103 && 
field.at(1)==108 && 
field.at(2)==121 && 
field.at(3)==112 && 
field.at(4)==104 && 
field.at(5)==115
) {
com.github.tommyettinger.jsonbiter.CodegenAccess.setExistingObject(iter, obj.glyphs);obj.glyphs= (java.util.ArrayList)jsoniter_codegen.cfg1612486582.decoder.java.util.ArrayList_com.github.tommyettinger.textra.json.GlyphData.decode_(iter);
continue;
}
break;
case 7: 
if (
field.at(0)==107 && 
field.at(1)==101 && 
field.at(2)==114 && 
field.at(3)==110 && 
field.at(4)==105 && 
field.at(5)==110 && 
field.at(6)==103
) {
com.github.tommyettinger.jsonbiter.CodegenAccess.setExistingObject(iter, obj.kerning);obj.kerning= (java.util.ArrayList)jsoniter_codegen.cfg1612486582.decoder.java.util.ArrayList_com.github.tommyettinger.textra.json.KerningData.decode_(iter);
continue;
}
if (
field.at(0)==109 && 
field.at(1)==101 && 
field.at(2)==116 && 
field.at(3)==114 && 
field.at(4)==105 && 
field.at(5)==99 && 
field.at(6)==115
) {
com.github.tommyettinger.jsonbiter.CodegenAccess.setExistingObject(iter, obj.metrics);obj.metrics= (com.github.tommyettinger.textra.json.MetricsData)jsoniter_codegen.cfg1612486582.decoder.com.github.tommyettinger.textra.json.MetricsData.decode_(iter);
continue;
}
break;

}
iter.skip();
}
return obj;
}public java.lang.Object decode(com.github.tommyettinger.jsonbiter.JsonIterator iter) throws java.io.IOException {
return decode_(iter);
}
}
