package com.github.tommyettinger.batch;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Utility methods that return vertex or fragment shader source code to be used in
 * {@link TextureArrayPolygonSpriteBatch} or {@link TextureArrayCpuPolygonSpriteBatch}. These methods all require one of
 * those mentioned Batches to have been created before any shader source can be obtained, since creating either Texture
 * Array Batch calculates the number of texture units the GPU can handle.
 * <br>
 * Mostly taken from <a href="https://github.com/rednblackgames/hyperlap2d-runtime-libgdx/tree/master/src/main/java/games/rednblack/editor/renderer/utils">Hyperlap2D's GitHub repo</a>.
 * Originally licensed under Apache 2.0, like TextraTypist and libGDX.
 */
public final class TextureArrayShaders {
    private TextureArrayShaders() {

    }

    /**
     * The vertex shader used for any rendering with a {@link TextureArrayPolygonSpriteBatch} or
     * {@link TextureArrayCpuPolygonSpriteBatch}, regardless of distance field. The other vertex shaders here are simply
     * aliases for this method.
     * @return a vertex shader String that works with TextureArray batches
     */
    public static String defaultArrayVertexShader() {
        return    "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
                + "attribute float " + TextureArrayPolygonSpriteBatch.TEXTURE_INDEX_ATTRIBUTE + ";\n" // changed from SpriteBatch
                + "uniform mat4 u_projTrans;\n" //
                + "varying vec4 v_color;\n" //
                + "varying vec2 v_texCoords;\n" //
                + "varying float v_texture_index;\n" // changed from SpriteBatch
                + "\n" //
                + "void main() {\n" //
                + "    v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                + "    v_color.a = v_color.a * (255.0/254.0);\n" //
                + "    v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
                + "    v_texture_index = " + TextureArrayPolygonSpriteBatch.TEXTURE_INDEX_ATTRIBUTE + ";\n" // changed from SpriteBatch
                + "    gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                + "}\n";
    }
    /**
     * The fragment shader used for any rendering with a {@link TextureArrayPolygonSpriteBatch} or
     * {@link TextureArrayCpuPolygonSpriteBatch} when no distance field effect is in use (or for a STANDARD font).
     *
     * @return a fragment shader String that works with TextureArray batches
     */
    public static String defaultArrayFragmentShader() {
        return TextureArrayShaderCompiler.processArrayTextureShader("#ifdef GL_ES\n" //
                + "#define LOWP lowp\n" //
                + "precision mediump float;\n" //
                + "#else\n" //
                + "#define LOWP\n" //
                + "#endif\n" //
                + "varying LOWP vec4 v_color;\n" //
                + "varying vec2 v_texCoords;\n" //
                + "varying float v_texture_index;\n" // changed from SpriteBatch
                + "uniform sampler2D u_textures[MAX_TEXTURE_UNITS];\n" // changed from SpriteBatch
                + "\n" //
                + TextureArrayShaderCompiler.GET_TEXTURE_FROM_ARRAY_PLACEHOLDER + "\n" // changed from SpriteBatch
                + "\n" //
                + "void main() {\n"//
                + "    gl_FragColor = v_color * getTextureFromArray(v_texCoords);\n" // changed from SpriteBatch
                + "}\n");
    }
}