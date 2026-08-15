package com.github.tommyettinger.teavm;

import com.github.xpenatan.gdx.teavm.backends.shared.config.AssetFileHandle;
import com.github.xpenatan.gdx.teavm.backends.shared.config.builder.TeaBuilder;
import com.github.xpenatan.gdx.teavm.backends.web.config.backend.WebBackend;
import java.io.File;
import org.teavm.tooling.TeaVMSourceFilePolicy;
import org.teavm.tooling.sources.DirectorySourceFileProvider;
import org.teavm.vm.TeaVMOptimizationLevel;

/** Builds the TeaVM/HTML application. */
public class TeaVMBuilder {
    public static void main(String[] args) {
        // Typically set by the Gradle task, but can also be set here or with the command-line arg "debug"
        boolean debug = false;
        // Typically set by the Gradle task, but can also be set here or with the command-line arg "run"
        boolean startJetty = false;
        for (String arg : args) {
            if ("debug".equals(arg)) debug = true;
            else if ("run".equals(arg)) startJetty = true;
        }
        WebBackend backend = new WebBackend()
                .setHtmlWidth(800) /* Change this to fit your game's requirements. */
                .setHtmlHeight(600) /* Change this to fit your game's requirements. */
                .setHtmlTitle("latest")
//                .setWebAssembly(true) /* Uncomment this line to use WASM output instead of JavaScript output. */
                .setStartJettyAfterBuild(startJetty)
                .setJettyPort(8080);
        new TeaBuilder(backend)
            .addAssets(new AssetFileHandle("../assets"))
            
            .setOptimizationLevel(debug ? TeaVMOptimizationLevel.SIMPLE : TeaVMOptimizationLevel.ADVANCED)
            .setMainClass(TeaVMLauncher.class.getName())
            .setObfuscated(!debug)
            .setDebugInformationGenerated(debug)
            .setSourceMapsFileGenerated(debug)
            .setSourceFilePolicy(TeaVMSourceFilePolicy.COPY)
            .addSourceFileProvider(new DirectorySourceFileProvider(new File("../core/src/main/java/")))
            // You can also register any classes or packages that require reflection here:
            //.addReflectionClass("com.github.tommyettinger.reflect")
            .build(new File("build/dist/" + (backend.isWebAssembly ? "wasm" : "js")));
    }
}