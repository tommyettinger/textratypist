package jsoniter_codegen.cfg1612486582.decoder.com.github.tommyettinger.textra.json;
public class BoundsData implements com.github.tommyettinger.jsonbiter.spi.Decoder {
public static java.lang.Object decode_(com.github.tommyettinger.jsonbiter.JsonIterator iter) throws java.io.IOException { java.lang.Object existingObj = com.github.tommyettinger.jsonbiter.CodegenAccess.resetExistingObject(iter);
if (iter.readNull()) { return null; }
com.github.tommyettinger.textra.json.BoundsData obj = (existingObj == null ? new com.github.tommyettinger.textra.json.BoundsData() : (com.github.tommyettinger.textra.json.BoundsData)existingObj);
if (!com.github.tommyettinger.jsonbiter.CodegenAccess.readObjectStart(iter)) {
return obj;
}
com.github.tommyettinger.jsonbiter.spi.Slice field = com.github.tommyettinger.jsonbiter.CodegenAccess.readObjectFieldAsSlice(iter);
boolean once = true;
while (once) {
once = false;
switch (field.len()) {
case 3: 
if (
field.at(0)==116 && 
field.at(1)==111 && 
field.at(2)==112
) {
obj.top= (float)iter.readFloat();
continue;
}
break;
case 4: 
if (
field.at(0)==108 && 
field.at(1)==101 && 
field.at(2)==102 && 
field.at(3)==116
) {
obj.left= (float)iter.readFloat();
continue;
}
break;
case 5: 
if (
field.at(0)==114 && 
field.at(1)==105 && 
field.at(2)==103 && 
field.at(3)==104 && 
field.at(4)==116
) {
obj.right= (float)iter.readFloat();
continue;
}
break;
case 6: 
if (
field.at(0)==98 && 
field.at(1)==111 && 
field.at(2)==116 && 
field.at(3)==116 && 
field.at(4)==111 && 
field.at(5)==109
) {
obj.bottom= (float)iter.readFloat();
continue;
}
break;

}
iter.skip();
}
while (com.github.tommyettinger.jsonbiter.CodegenAccess.nextToken(iter) == ',') {
field = com.github.tommyettinger.jsonbiter.CodegenAccess.readObjectFieldAsSlice(iter);
switch (field.len()) {
case 3: 
if (
field.at(0)==116 && 
field.at(1)==111 && 
field.at(2)==112
) {
obj.top= (float)iter.readFloat();
continue;
}
break;
case 4: 
if (
field.at(0)==108 && 
field.at(1)==101 && 
field.at(2)==102 && 
field.at(3)==116
) {
obj.left= (float)iter.readFloat();
continue;
}
break;
case 5: 
if (
field.at(0)==114 && 
field.at(1)==105 && 
field.at(2)==103 && 
field.at(3)==104 && 
field.at(4)==116
) {
obj.right= (float)iter.readFloat();
continue;
}
break;
case 6: 
if (
field.at(0)==98 && 
field.at(1)==111 && 
field.at(2)==116 && 
field.at(3)==116 && 
field.at(4)==111 && 
field.at(5)==109
) {
obj.bottom= (float)iter.readFloat();
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
