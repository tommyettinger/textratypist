package jsoniter_codegen.cfg1612486582.decoder.com.github.tommyettinger.textra.json;
public class MetricsData implements com.github.tommyettinger.jsonbiter.spi.Decoder {
public static java.lang.Object decode_(com.github.tommyettinger.jsonbiter.JsonIterator iter) throws java.io.IOException { java.lang.Object existingObj = com.github.tommyettinger.jsonbiter.CodegenAccess.resetExistingObject(iter);
if (iter.readNull()) { return null; }
com.github.tommyettinger.textra.json.MetricsData obj = (existingObj == null ? new com.github.tommyettinger.textra.json.MetricsData() : (com.github.tommyettinger.textra.json.MetricsData)existingObj);
if (!com.github.tommyettinger.jsonbiter.CodegenAccess.readObjectStart(iter)) {
return obj;
}
com.github.tommyettinger.jsonbiter.spi.Slice field = com.github.tommyettinger.jsonbiter.CodegenAccess.readObjectFieldAsSlice(iter);
boolean once = true;
while (once) {
once = false;
switch (field.len()) {
case 18: 
if (
field.at(0)==117 && 
field.at(1)==110 && 
field.at(2)==100 && 
field.at(3)==101 && 
field.at(4)==114 && 
field.at(5)==108 && 
field.at(6)==105 && 
field.at(7)==110 && 
field.at(8)==101 && 
field.at(9)==84 && 
field.at(10)==104 && 
field.at(11)==105 && 
field.at(12)==99 && 
field.at(13)==107 && 
field.at(14)==110 && 
field.at(15)==101 && 
field.at(16)==115 && 
field.at(17)==115
) {
obj.underlineThickness= (float)iter.readFloat();
continue;
}
break;
case 6: 
if (
field.at(0)==101 && 
field.at(1)==109 && 
field.at(2)==83 && 
field.at(3)==105 && 
field.at(4)==122 && 
field.at(5)==101
) {
obj.emSize= (float)iter.readFloat();
continue;
}
break;
case 8: 
if (
field.at(0)==97 && 
field.at(1)==115 && 
field.at(2)==99 && 
field.at(3)==101 && 
field.at(4)==110 && 
field.at(5)==100 && 
field.at(6)==101 && 
field.at(7)==114
) {
obj.ascender= (float)iter.readFloat();
continue;
}
break;
case 9: 
if (
field.at(0)==100 && 
field.at(1)==101 && 
field.at(2)==115 && 
field.at(3)==99 && 
field.at(4)==101 && 
field.at(5)==110 && 
field.at(6)==100 && 
field.at(7)==101 && 
field.at(8)==114
) {
obj.descender= (float)iter.readFloat();
continue;
}
break;
case 10: 
if (
field.at(0)==117 && 
field.at(1)==110 && 
field.at(2)==100 && 
field.at(3)==101 && 
field.at(4)==114 && 
field.at(5)==108 && 
field.at(6)==105 && 
field.at(7)==110 && 
field.at(8)==101 && 
field.at(9)==89
) {
obj.underlineY= (float)iter.readFloat();
continue;
}
if (
field.at(0)==108 && 
field.at(1)==105 && 
field.at(2)==110 && 
field.at(3)==101 && 
field.at(4)==72 && 
field.at(5)==101 && 
field.at(6)==105 && 
field.at(7)==103 && 
field.at(8)==104 && 
field.at(9)==116
) {
obj.lineHeight= (float)iter.readFloat();
continue;
}
break;

}
iter.skip();
}
while (com.github.tommyettinger.jsonbiter.CodegenAccess.nextToken(iter) == ',') {
field = com.github.tommyettinger.jsonbiter.CodegenAccess.readObjectFieldAsSlice(iter);
switch (field.len()) {
case 18: 
if (
field.at(0)==117 && 
field.at(1)==110 && 
field.at(2)==100 && 
field.at(3)==101 && 
field.at(4)==114 && 
field.at(5)==108 && 
field.at(6)==105 && 
field.at(7)==110 && 
field.at(8)==101 && 
field.at(9)==84 && 
field.at(10)==104 && 
field.at(11)==105 && 
field.at(12)==99 && 
field.at(13)==107 && 
field.at(14)==110 && 
field.at(15)==101 && 
field.at(16)==115 && 
field.at(17)==115
) {
obj.underlineThickness= (float)iter.readFloat();
continue;
}
break;
case 6: 
if (
field.at(0)==101 && 
field.at(1)==109 && 
field.at(2)==83 && 
field.at(3)==105 && 
field.at(4)==122 && 
field.at(5)==101
) {
obj.emSize= (float)iter.readFloat();
continue;
}
break;
case 8: 
if (
field.at(0)==97 && 
field.at(1)==115 && 
field.at(2)==99 && 
field.at(3)==101 && 
field.at(4)==110 && 
field.at(5)==100 && 
field.at(6)==101 && 
field.at(7)==114
) {
obj.ascender= (float)iter.readFloat();
continue;
}
break;
case 9: 
if (
field.at(0)==100 && 
field.at(1)==101 && 
field.at(2)==115 && 
field.at(3)==99 && 
field.at(4)==101 && 
field.at(5)==110 && 
field.at(6)==100 && 
field.at(7)==101 && 
field.at(8)==114
) {
obj.descender= (float)iter.readFloat();
continue;
}
break;
case 10: 
if (
field.at(0)==117 && 
field.at(1)==110 && 
field.at(2)==100 && 
field.at(3)==101 && 
field.at(4)==114 && 
field.at(5)==108 && 
field.at(6)==105 && 
field.at(7)==110 && 
field.at(8)==101 && 
field.at(9)==89
) {
obj.underlineY= (float)iter.readFloat();
continue;
}
if (
field.at(0)==108 && 
field.at(1)==105 && 
field.at(2)==110 && 
field.at(3)==101 && 
field.at(4)==72 && 
field.at(5)==101 && 
field.at(6)==105 && 
field.at(7)==103 && 
field.at(8)==104 && 
field.at(9)==116
) {
obj.lineHeight= (float)iter.readFloat();
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
