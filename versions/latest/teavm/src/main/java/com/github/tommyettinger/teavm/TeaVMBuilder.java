package com.github.tommyettinger.teavm;

import com.github.xpenatan.gdx.backends.teavm.config.AssetFileHandle;
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuildConfiguration;
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuilder;
import com.github.xpenatan.gdx.backends.teavm.config.plugins.TeaReflectionSupplier;
import java.io.File;
import java.io.IOException;
import org.teavm.tooling.TeaVMTool;
import org.teavm.vm.TeaVMOptimizationLevel;

/** Builds the TeaVM/HTML application. */
public class TeaVMBuilder {
    public static void main(String[] args) throws IOException {
        TeaBuildConfiguration teaBuildConfiguration = new TeaBuildConfiguration();
        teaBuildConfiguration.assetsPath.add(new AssetFileHandle("../assets"));
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
        // For many (or most) applications, using the highest optimization won't add much to build time.
        // If your builds take too long, and runtime performance doesn't matter, you can change FULL to SIMPLE .
        tool.setOptimizationLevel(TeaVMOptimizationLevel.ADVANCED);
        tool.setDebugInformationGenerated(false);
        tool.setObfuscated(false);
        TeaBuilder.build(tool);
    }
}
