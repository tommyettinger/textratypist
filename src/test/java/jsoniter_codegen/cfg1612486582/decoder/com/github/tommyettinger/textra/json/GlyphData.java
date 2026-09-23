package jsoniter_codegen.cfg1612486582.decoder.com.github.tommyettinger.textra.json;
public class GlyphData implements com.github.tommyettinger.jsonbiter.spi.Decoder {
public static java.lang.Object decode_(com.github.tommyettinger.jsonbiter.JsonIterator iter) throws java.io.IOException { java.lang.Object existingObj = com.github.tommyettinger.jsonbiter.CodegenAccess.resetExistingObject(iter);
if (iter.readNull()) { return null; }
com.github.tommyettinger.textra.json.GlyphData obj = (existingObj == null ? new com.github.tommyettinger.textra.json.GlyphData() : (com.github.tommyettinger.textra.json.GlyphData)existingObj);
if (!com.github.tommyettinger.jsonbiter.CodegenAccess.readObjectStart(iter)) {
return obj;
}
com.github.tommyettinger.jsonbiter.spi.Slice field = com.github.tommyettinger.jsonbiter.CodegenAccess.readObjectFieldAsSlice(iter);
boolean once = true;
while (once) {
once = false;
switch (field.len()) {
case 7: 
if (
field.at(0)==97 && 
field.at(1)==100 && 
field.at(2)==118 && 
field.at(3)==97 && 
field.at(4)==110 && 
field.at(5)==99 && 
field.at(6)==101
) {
obj.advance= (float)iter.readFloat();
continue;
}
if (
field.at(0)==117 && 
field.at(1)==110 && 
field.at(2)==105 && 
field.at(3)==99 && 
field.at(4)==111 && 
field.at(5)==100 && 
field.at(6)==101
) {
obj.unicode= (int)iter.readInt();
continue;
}
break;
case 11: 
if (
field.at(0)==112 && 
field.at(1)==108 && 
field.at(2)==97 && 
field.at(3)==110 && 
field.at(4)==101 && 
field.at(5)==66 && 
field.at(6)==111 && 
field.at(7)==117 && 
field.at(8)==110 && 
field.at(9)==100 && 
field.at(10)==115
) {
com.github.tommyettinger.jsonbiter.CodegenAccess.setExistingObject(iter, obj.planeBounds);obj.planeBounds= (com.github.tommyettinger.textra.json.BoundsData)jsoniter_codegen.cfg1612486582.decoder.com.github.tommyettinger.textra.json.BoundsData.decode_(iter);
continue;
}
if (
field.at(0)==97 && 
field.at(1)==116 && 
field.at(2)==108 && 
field.at(3)==97 && 
field.at(4)==115 && 
field.at(5)==66 && 
field.at(6)==111 && 
field.at(7)==117 && 
field.at(8)==110 && 
field.at(9)==100 && 
field.at(10)==115
) {
com.github.tommyettinger.jsonbiter.CodegenAccess.setExistingObject(iter, obj.atlasBounds);obj.atlasBounds= (com.github.tommyettinger.textra.json.BoundsData)jsoniter_codegen.cfg1612486582.decoder.com.github.tommyettinger.textra.json.BoundsData.decode_(iter);
continue;
}
break;

}
iter.skip();
}
while (com.github.tommyettinger.jsonbiter.CodegenAccess.nextToken(iter) == ',') {
field = com.github.tommyettinger.jsonbiter.CodegenAccess.readObjectFieldAsSlice(iter);
switch (field.len()) {
case 7: 
if (
field.at(0)==97 && 
field.at(1)==100 && 
field.at(2)==118 && 
field.at(3)==97 && 
field.at(4)==110 && 
field.at(5)==99 && 
field.at(6)==101
) {
obj.advance= (float)iter.readFloat();
continue;
}
if (
field.at(0)==117 && 
field.at(1)==110 && 
field.at(2)==105 && 
field.at(3)==99 && 
field.at(4)==111 && 
field.at(5)==100 && 
field.at(6)==101
) {
obj.unicode= (int)iter.readInt();
continue;
}
break;
case 11: 
if (
field.at(0)==112 && 
field.at(1)==108 && 
field.at(2)==97 && 
field.at(3)==110 && 
field.at(4)==101 && 
field.at(5)==66 && 
field.at(6)==111 && 
field.at(7)==117 && 
field.at(8)==110 && 
field.at(9)==100 && 
field.at(10)==115
) {
com.github.tommyettinger.jsonbiter.CodegenAccess.setExistingObject(iter, obj.planeBounds);obj.planeBounds= (com.github.tommyettinger.textra.json.BoundsData)jsoniter_codegen.cfg1612486582.decoder.com.github.tommyettinger.textra.json.BoundsData.decode_(iter);
continue;
}
if (
field.at(0)==97 && 
field.at(1)==116 && 
field.at(2)==108 && 
field.at(3)==97 && 
field.at(4)==115 && 
field.at(5)==66 && 
field.at(6)==111 && 
field.at(7)==117 && 
field.at(8)==110 && 
field.at(9)==100 && 
field.at(10)==115
) {
com.github.tommyettinger.jsonbiter.CodegenAccess.setExistingObject(iter, obj.atlasBounds);obj.atlasBounds= (com.github.tommyettinger.textra.json.BoundsData)jsoniter_codegen.cfg1612486582.decoder.com.github.tommyettinger.textra.json.BoundsData.decode_(iter);
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
