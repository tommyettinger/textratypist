package jsoniter_codegen.cfg1612486582.decoder.com.github.tommyettinger.textra.json;
public class KerningData implements com.github.tommyettinger.jsonbiter.spi.Decoder {
public static java.lang.Object decode_(com.github.tommyettinger.jsonbiter.JsonIterator iter) throws java.io.IOException { java.lang.Object existingObj = com.github.tommyettinger.jsonbiter.CodegenAccess.resetExistingObject(iter);
if (iter.readNull()) { return null; }
com.github.tommyettinger.textra.json.KerningData obj = (existingObj == null ? new com.github.tommyettinger.textra.json.KerningData() : (com.github.tommyettinger.textra.json.KerningData)existingObj);
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
break;
case 8: 
if (
field.at(0)==117 && 
field.at(1)==110 && 
field.at(2)==105 && 
field.at(3)==99 && 
field.at(4)==111 && 
field.at(5)==100 && 
field.at(6)==101
) {
if (
field.at(7)==49
) {
obj.unicode1= (int)iter.readInt();
continue;
}
if (
field.at(7)==50
) {
obj.unicode2= (int)iter.readInt();
continue;
}
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
break;
case 8: 
if (
field.at(0)==117 && 
field.at(1)==110 && 
field.at(2)==105 && 
field.at(3)==99 && 
field.at(4)==111 && 
field.at(5)==100 && 
field.at(6)==101
) {
if (
field.at(7)==49
) {
obj.unicode1= (int)iter.readInt();
continue;
}
if (
field.at(7)==50
) {
obj.unicode2= (int)iter.readInt();
continue;
}
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
