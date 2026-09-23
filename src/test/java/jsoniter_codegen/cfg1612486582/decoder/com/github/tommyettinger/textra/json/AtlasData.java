package jsoniter_codegen.cfg1612486582.decoder.com.github.tommyettinger.textra.json;
public class AtlasData implements com.github.tommyettinger.jsonbiter.spi.Decoder {
public static java.lang.Object decode_(com.github.tommyettinger.jsonbiter.JsonIterator iter) throws java.io.IOException { java.lang.Object existingObj = com.github.tommyettinger.jsonbiter.CodegenAccess.resetExistingObject(iter);
if (iter.readNull()) { return null; }
com.github.tommyettinger.textra.json.AtlasData obj = (existingObj == null ? new com.github.tommyettinger.textra.json.AtlasData() : (com.github.tommyettinger.textra.json.AtlasData)existingObj);
if (!com.github.tommyettinger.jsonbiter.CodegenAccess.readObjectStart(iter)) {
return obj;
}
com.github.tommyettinger.jsonbiter.spi.Slice field = com.github.tommyettinger.jsonbiter.CodegenAccess.readObjectFieldAsSlice(iter);
boolean once = true;
while (once) {
once = false;
switch (field.len()) {
case 19: 
if (
field.at(0)==100 && 
field.at(1)==105 && 
field.at(2)==115 && 
field.at(3)==116 && 
field.at(4)==97 && 
field.at(5)==110 && 
field.at(6)==99 && 
field.at(7)==101 && 
field.at(8)==82 && 
field.at(9)==97 && 
field.at(10)==110 && 
field.at(11)==103 && 
field.at(12)==101 && 
field.at(13)==77 && 
field.at(14)==105 && 
field.at(15)==100 && 
field.at(16)==100 && 
field.at(17)==108 && 
field.at(18)==101
) {
obj.distanceRangeMiddle= (float)iter.readFloat();
continue;
}
break;
case 4: 
if (
field.at(0)==115 && 
field.at(1)==105 && 
field.at(2)==122 && 
field.at(3)==101
) {
obj.size= (float)iter.readFloat();
continue;
}
if (
field.at(0)==116 && 
field.at(1)==121 && 
field.at(2)==112 && 
field.at(3)==101
) {
obj.type= (java.lang.String)iter.readString();
continue;
}
break;
case 5: 
if (
field.at(0)==119 && 
field.at(1)==105 && 
field.at(2)==100 && 
field.at(3)==116 && 
field.at(4)==104
) {
obj.width= (float)iter.readFloat();
continue;
}
break;
case 6: 
if (
field.at(0)==104 && 
field.at(1)==101 && 
field.at(2)==105 && 
field.at(3)==103 && 
field.at(4)==104 && 
field.at(5)==116
) {
obj.height= (float)iter.readFloat();
continue;
}
break;
case 7: 
if (
field.at(0)==121 && 
field.at(1)==79 && 
field.at(2)==114 && 
field.at(3)==105 && 
field.at(4)==103 && 
field.at(5)==105 && 
field.at(6)==110
) {
obj.yOrigin= (java.lang.String)iter.readString();
continue;
}
break;
case 13: 
if (
field.at(0)==100 && 
field.at(1)==105 && 
field.at(2)==115 && 
field.at(3)==116 && 
field.at(4)==97 && 
field.at(5)==110 && 
field.at(6)==99 && 
field.at(7)==101 && 
field.at(8)==82 && 
field.at(9)==97 && 
field.at(10)==110 && 
field.at(11)==103 && 
field.at(12)==101
) {
obj.distanceRange= (float)iter.readFloat();
continue;
}
break;

}
iter.skip();
}
while (com.github.tommyettinger.jsonbiter.CodegenAccess.nextToken(iter) == ',') {
field = com.github.tommyettinger.jsonbiter.CodegenAccess.readObjectFieldAsSlice(iter);
switch (field.len()) {
case 19: 
if (
field.at(0)==100 && 
field.at(1)==105 && 
field.at(2)==115 && 
field.at(3)==116 && 
field.at(4)==97 && 
field.at(5)==110 && 
field.at(6)==99 && 
field.at(7)==101 && 
field.at(8)==82 && 
field.at(9)==97 && 
field.at(10)==110 && 
field.at(11)==103 && 
field.at(12)==101 && 
field.at(13)==77 && 
field.at(14)==105 && 
field.at(15)==100 && 
field.at(16)==100 && 
field.at(17)==108 && 
field.at(18)==101
) {
obj.distanceRangeMiddle= (float)iter.readFloat();
continue;
}
break;
case 4: 
if (
field.at(0)==115 && 
field.at(1)==105 && 
field.at(2)==122 && 
field.at(3)==101
) {
obj.size= (float)iter.readFloat();
continue;
}
if (
field.at(0)==116 && 
field.at(1)==121 && 
field.at(2)==112 && 
field.at(3)==101
) {
obj.type= (java.lang.String)iter.readString();
continue;
}
break;
case 5: 
if (
field.at(0)==119 && 
field.at(1)==105 && 
field.at(2)==100 && 
field.at(3)==116 && 
field.at(4)==104
) {
obj.width= (float)iter.readFloat();
continue;
}
break;
case 6: 
if (
field.at(0)==104 && 
field.at(1)==101 && 
field.at(2)==105 && 
field.at(3)==103 && 
field.at(4)==104 && 
field.at(5)==116
) {
obj.height= (float)iter.readFloat();
continue;
}
break;
case 7: 
if (
field.at(0)==121 && 
field.at(1)==79 && 
field.at(2)==114 && 
field.at(3)==105 && 
field.at(4)==103 && 
field.at(5)==105 && 
field.at(6)==110
) {
obj.yOrigin= (java.lang.String)iter.readString();
continue;
}
break;
case 13: 
if (
field.at(0)==100 && 
field.at(1)==105 && 
field.at(2)==115 && 
field.at(3)==116 && 
field.at(4)==97 && 
field.at(5)==110 && 
field.at(6)==99 && 
field.at(7)==101 && 
field.at(8)==82 && 
field.at(9)==97 && 
field.at(10)==110 && 
field.at(11)==103 && 
field.at(12)==101
) {
obj.distanceRange= (float)iter.readFloat();
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
