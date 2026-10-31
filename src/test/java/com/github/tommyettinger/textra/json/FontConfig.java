package com.github.tommyettinger.textra.json;

import com.github.tommyettinger.jsonbiter.any.Any;
import com.github.tommyettinger.jsonbiter.output.EncodingMode;
import com.github.tommyettinger.jsonbiter.spi.*;
import com.github.tommyettinger.jsonbiter.static_codegen.StaticCodegenConfig;

import java.util.ArrayList;

/**
 * Specific to JsonBiter/Jsoniter, so probably not useful anywhere else.
 * <br>
 * This was part of an experiment to see if loading .json.lzma fonts using alternative JSON libraries (not libGDX Json)
 * could speed up loading times. In short, both Fory-JSON and JsonBiter had enough startup overhead doing dynamic
 * class building to overshadow the time needed to load a relatively-small .json.lzma file.
 * The experiment was stopped there.
 */
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
