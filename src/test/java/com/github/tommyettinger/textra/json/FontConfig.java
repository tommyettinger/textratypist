package com.github.tommyettinger.textra.json;

import com.github.tommyettinger.jsonbiter.JsonIterator;
import com.github.tommyettinger.jsonbiter.any.Any;
import com.github.tommyettinger.jsonbiter.output.EncodingMode;
import com.github.tommyettinger.jsonbiter.output.JsonStream;
import com.github.tommyettinger.jsonbiter.spi.*;
import com.github.tommyettinger.jsonbiter.static_codegen.StaticCodegenConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FontConfig implements StaticCodegenConfig {

    @Override
    public void setup() {
        // register custom decoder or extensions before codegen
        // so that we can do codegen, we know in which case, we need to callback
        Any.registerEncoders();
        Config newConfig = JsoniterSpi.getDefaultConfig().copyBuilder()
                .decodingMode(DecodingMode.STATIC_MODE).encodingMode(EncodingMode.STATIC_MODE)
                .omitDefaultValue(true).indentationStep(2).escapeUnicode(false).build();
        JsoniterSpi.setDefaultConfig(newConfig);
        JsoniterSpi.setCurrentConfig(newConfig);

    }

    @Override
    public TypeLiteral[] whatToCodegen() {
        return new TypeLiteral[]{
                TypeLiteral.create(KerningData.class),
                new TypeLiteral<ArrayList<KerningData>>() {},
                TypeLiteral.create(BoundsData.class),
                TypeLiteral.create(MetricsData.class),
                TypeLiteral.create(AtlasData.class),
                TypeLiteral.create(GlyphData.class),
                new TypeLiteral<ArrayList<GlyphData>>() {},
                TypeLiteral.create(FontData.class)
        };
    }
}
