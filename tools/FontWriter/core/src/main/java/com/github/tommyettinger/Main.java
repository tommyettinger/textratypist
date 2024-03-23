package com.github.tommyettinger;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

import java.io.File;
import java.io.IOException;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    String[] args;
    public Main(String[] args) {
        if(args == null || args.length == 0) {
            System.out.println("This tool needs some parameters!");
            System.exit(1);
        }
        this.args = args;
    }
    @Override
    public void create() {
        String fontFileName = args[0], fontName = fontFileName.substring(Math.max(fontFileName.lastIndexOf('/'), 0), fontFileName.lastIndexOf('.'));
        String cmd = "distbin/msdf-atlas-gen.exe -font " + fontFileName + " -allglyphs" +
                " -type "+("standard".equals(args[1]) ? "softmask" : args[1])+" -imageout fonts/"+fontName+"-"+args[1]+".png -json fonts/"+fontName+"-"+args[1]+".json " +
                "-pxrange 8 -dimensions 2048 2048 -size " + args[2];
        ProcessBuilder builder =
                new ProcessBuilder(cmd.split(" "));
        builder.directory(new File(Gdx.files.getLocalStoragePath()));
        Gdx.files.local("fonts").mkdirs();
        Gdx.files.local("previews").mkdirs();
        builder.inheritIO();
        try {
            builder.start();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
//        blockStamp("fonts/"+fontName+"-"+args[1]+".png");
//        if(!"msdf".equals(args[1])){
//            processTransparency("fonts/"+fontName+"-"+args[1]+".png");
//        }
        Gdx.app.exit();
    }
}