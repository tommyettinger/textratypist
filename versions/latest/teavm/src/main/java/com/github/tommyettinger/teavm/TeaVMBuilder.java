package com.github.tommyettinger.teavm;

import com.github.xpenatan.gdx.backends.teavm.config.TeaBuildConfiguration;
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuilder;
import com.github.xpenatan.gdx.backends.teavm.config.plugins.TeaReflectionSupplier;
import com.github.xpenatan.gdx.backends.teavm.gen.SkipClass;
import java.io.File;
import java.io.IOException;
import org.teavm.tooling.TeaVMTool;
import org.teavm.vm.TeaVMOptimizationLevel;

/** Builds the TeaVM/HTML application. */
@SkipClass
public class TeaVMBuilder {
    public static void main(String[] args) throws IOException {
        TeaBuildConfiguration teaBuildConfiguration = new TeaBuildConfiguration();
        teaBuildConfiguration.assetsPath.add(new File("../assets"));
        teaBuildConfiguration.webappPath = new File("build/dist").getCanonicalPath();

        // Register any extra classpath assets here:
        // teaBuildConfiguration.additionalAssetsClasspathFiles.add("com/github/tommyettinger/asset.extension");

        // Register any classes or packages that require reflection here:
         TeaReflectionSupplier.addReflectionClass("com.badlogic.gdx.graphics.Color");
         TeaReflectionSupplier.addReflectionClass("com.badlogic.gdx.graphics.g2d.GlyphLayout");
         TeaReflectionSupplier.addReflectionClass("com.badlogic.gdx.graphics.g2d.BitmapFont");
         TeaReflectionSupplier.addReflectionClass("com.badlogic.gdx.scenes.scene2d");
         TeaReflectionSupplier.addReflectionClass("com.badlogic.gdx.scenes.scene2d.ui");
         TeaReflectionSupplier.addReflectionClass("com.github.tommyettinger.textra.Styles");

        TeaVMTool tool = TeaBuilder.config(teaBuildConfiguration);
        tool.setMainClass(TeaVMLauncher.class.getName());
        tool.setDebugInformationGenerated(false);
        tool.setObfuscated(false);
        tool.setOptimizationLevel(TeaVMOptimizationLevel.FULL);
        TeaBuilder.build(tool);
    }
}
