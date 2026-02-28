package com.github.rednblackgames;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Taken from <a href="https://github.com/rednblackgames/hyperlap2d-runtime-libgdx/tree/master/src/main/java/games/rednblack/editor/renderer/utils">Hyperlap2D's GitHub repo</a>.
 */
public class ShaderCompiler {
    public static int MAX_TEXTURE_UNIT = 1;

    public static final String GET_TEXTURE_FROM_ARRAY_PLACEHOLDER = "<GET_TEXTURE_FROM_ARRAY_PLACEHOLDER>";

    public static ShaderProgram compileShader(FileHandle vertex, FileHandle fragment) {
        return compileShader(vertex.readString(), fragment.readString());
    }

    public static ShaderProgram compileShader(String vertex, String fragment) {
        if (!fragment.contains(GET_TEXTURE_FROM_ARRAY_PLACEHOLDER)) return new ShaderProgram(vertex, fragment);

        return compileUnrolledArrayTextureShader(vertex, fragment);
    }

    public static ShaderProgram compileUnrolledArrayTextureShader(String vertex, String fragment) {
        String funcConditional = "vec4 getTextureFromArray(vec2 uv) {\n";
        for (int i = 0; i < MAX_TEXTURE_UNIT; i++) {
            if (i != 0) funcConditional += " else ";
            funcConditional += "if (v_texture_index < " + i + ".5) return texture2D(u_textures[" + i + "], uv);\n";
        }
        funcConditional += "}\n";

        fragment = "#define MAX_TEXTURE_UNITS " + MAX_TEXTURE_UNIT + "\n" + fragment;
        fragment = fragment.replace(GET_TEXTURE_FROM_ARRAY_PLACEHOLDER, funcConditional);
        return new ShaderProgram(vertex, fragment);
    }
}