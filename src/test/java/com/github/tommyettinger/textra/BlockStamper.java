/*
 * Copyright (c) 2022 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.tommyettinger.textra;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import java.io.IOException;

public class BlockStamper  extends ApplicationAdapter {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("textramode block stamper tool");
        config.setWindowedMode(800, 400);
        config.disableAudio(true);
        ShaderProgram.prependVertexCode = "#version 110\n";
        ShaderProgram.prependFragmentCode = "#version 110\n";
        config.useVsync(true);
        new Lwjgl3Application(new BlockStamper(), config);
    }

    @Override
    public void create() {
        PixmapIO.PNG png = new PixmapIO.PNG();
        png.setFlipY(false);
        FileHandle fontsHandle = Gdx.files.local("knownFonts");
        FileHandle[] children = fontsHandle.list("png");
        PER_CHILD:
        for(FileHandle fh : children){
            System.out.println("Operating on " + fh.name());
            Pixmap pm = new Pixmap(fh);
            int w = pm.getWidth(), h = pm.getHeight();
            for (int x = w - 3; x < w; x++) {
                for (int y = h - 3; y < h; y++) {
                    int color = pm.getPixel(x, y);
                    if(!((color & 0xFF) == 0 || (color >>> 8) == 0)) {
                        System.out.println("Had a transparency problem with " + fh.name());
                        continue PER_CHILD;
                    }
                }
            }
            pm.setColor(-1);
            pm.fillRectangle(w - 3, h - 3, 3, 3);
            try {
                png.write(fh, pm);
            } catch (IOException e) {
                System.out.println("Had a file IO problem with " + fh.name());
                e.printStackTrace();
            }
            FileHandle fnt = fontsHandle.child(fh.nameWithoutExtension() + ".fnt");
            if(fnt.exists()){
                String text = fnt.readString("UTF-8");
                if(text.contains("char id=0 ")){
                    fnt.writeString(text.replaceFirst(
                            "char id=0 .+", "char id=0 x="+(w-2)+" y="+(h-2)+" width=1 height=1 xoffset=0 yoffset=0 xadvance=1 page=0 chnl=15"),
                            false, "UTF-8");
                } else {
                    fnt.writeString(text.replaceFirst(
                                    "(chars count=\\d+(\\R))", "$1char id=0 x="+(w-2)+" y="+(h-2)+" width=1 height=1 xoffset=0 yoffset=0 xadvance=1 page=0 chnl=15$2"),
                            false, "UTF-8");

                }
            }
        }
        System.exit(0);
    }
}
