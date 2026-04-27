package com.github.tommyettinger.batch;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Allows processing fragment shaders to use an unrolled loop for use in Texture Array Batches, and compiling those
 * shaders with the right size for {@link #MAX_TEXTURE_UNIT}. Typically, a {@link TextureArrayPolygonSpriteBatch} or
 * {@link TextureArrayCpuPolygonSpriteBatch} must be constructed in create() before this class can be used. Constructing
 * one of those Batches will set MAX_TEXTURE_UNIT based on the current GPU's actual capability, which is needed before
 * any shaders can be processed correctly.
 * <br>
 * Mostly taken from <a href="https://github.com/rednblackgames/hyperlap2d-runtime-libgdx/tree/master/src/main/java/games/rednblack/editor/renderer/utils">Hyperlap2D's GitHub repo</a>.
 * Originally licensed under Apache 2.0, like TextraTypist and libGDX.
 */
public final class TextureArrayShaderCompiler {
    private TextureArrayShaderCompiler() {
    }
    /**
     * Will be modified when a {@link TextureArrayPolygonSpriteBatch} or {@link TextureArrayCpuPolygonSpriteBatch} is
     * constructed for the first time, typically in create(). Before one of those Batches has been created, this class
     * is effectively not usable.
     */
    public static int MAX_TEXTURE_UNIT = 1;

    /**
     * The String that indicates a fragment shader needs processing to receive an unrolled loop in its source.
     */
    public static final String GET_TEXTURE_FROM_ARRAY_PLACEHOLDER = "<GET_TEXTURE_FROM_ARRAY_PLACEHOLDER>";

    /**
     * Processes the text of {@code fragment} with {@link #processArrayTextureShader(String)} and returns a new
     * ShaderProgram constructed from the given shader source FileHandles.
     * @param vertex a vertex shader source in a FileHandle
     * @param fragment a fragment shader source in a FileHandle
     * @return a new ShaderProgram using vertex and the processed fragment
     */
    public static ShaderProgram compileShader(FileHandle vertex, FileHandle fragment) {
        return compileShader(vertex.readString(), fragment.readString());
    }

    /**
     * Processes {@code fragment} with {@link #processArrayTextureShader(String)} and returns a new ShaderProgram
     * constructed from the given shader source Strings.
     * @param vertex a vertex shader source String
     * @param fragment a fragment shader source String
     * @return a new ShaderProgram using vertex and the processed fragment
     */
    public static ShaderProgram compileShader(String vertex, String fragment) {
        return new ShaderProgram(vertex, processArrayTextureShader(fragment));
    }

    /**
     * Takes a fragment shader source String and, if it contains {@link #GET_TEXTURE_FROM_ARRAY_PLACEHOLDER}, replaces
     * that text with code for an unrolled loop sized based on {@link #MAX_TEXTURE_UNIT}.
     * @param fragment a fragment shader source String
     * @return {@code fragment}, potentially after modifications
     */
    public static String processArrayTextureShader(String fragment) {
        if(!fragment.contains(GET_TEXTURE_FROM_ARRAY_PLACEHOLDER))
            return fragment;
        StringBuilder funcConditional = new StringBuilder("vec4 getTextureFromArray(vec2 uv) {\n");
        for (int i = 0; i < MAX_TEXTURE_UNIT; i++) {
            if (i != 0) funcConditional.append("else ");
            funcConditional.append("if (v_texture_index < ").append(i).append(".5) return texture2D(u_textures[").append(i).append("], uv);\n");
        }
        funcConditional.append("}\n");

        fragment = "#define MAX_TEXTURE_UNITS " + MAX_TEXTURE_UNIT + "\n" + fragment;
        return fragment.replace(GET_TEXTURE_FROM_ARRAY_PLACEHOLDER, funcConditional.toString());
    }
}
