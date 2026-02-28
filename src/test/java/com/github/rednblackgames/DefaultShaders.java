package com.github.rednblackgames;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Taken from <a href="https://github.com/rednblackgames/hyperlap2d-runtime-libgdx/tree/master/src/main/java/games/rednblack/editor/renderer/utils">Hyperlap2D's GitHub repo</a>.
 */
public class DefaultShaders {
    public static String DEFAULT_ARRAY_VERTEX_SHADER = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
            + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
            + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
            + "attribute float " + TextureArrayPolygonSpriteBatch.TEXTURE_INDEX_ATTRIBUTE + ";\n" //
            + "uniform mat4 u_projTrans;\n" //
            + "varying vec4 v_color;\n" //
            + "varying vec2 v_texCoords;\n" //
            + "varying float v_texture_index;\n" //
            + "\n" //
            + "void main() {\n" //
            + "    v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
            + "    v_color.a = v_color.a * (255.0/254.0);\n" //
            + "    v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
            + "    v_texture_index = " + TextureArrayPolygonSpriteBatch.TEXTURE_INDEX_ATTRIBUTE + ";\n" //
            + "    gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
            + "}\n";
    public static String DEFAULT_ARRAY_FRAGMENT_SHADER = "#ifdef GL_ES\n" //
            + "#define LOWP lowp\n" //
            + "precision mediump float;\n" //
            + "#else\n" //
            + "#define LOWP\n" //
            + "#endif\n" //
            + "varying LOWP vec4 v_color;\n" //
            + "varying vec2 v_texCoords;\n" //
            + "varying float v_texture_index;\n" //
            + "uniform sampler2D u_textures[MAX_TEXTURE_UNITS];\n" //
            + "\n" //
            + ShaderCompiler.GET_TEXTURE_FROM_ARRAY_PLACEHOLDER + "\n"
            + "\n" //
            + "void main() {\n"//
            + "    gl_FragColor = v_color * getTextureFromArray(v_texCoords);\n" //
            + "}\n";

    public static String DISTANCE_FIELD_FRAGMENT_SHADER = "#ifdef GL_ES\n" //
            + "	precision mediump float;\n" //
            + "	precision mediump int;\n" //
            + "#endif\n" //
            + "\n" //
            + "uniform sampler2D u_textures[MAX_TEXTURE_UNITS];\n" //
            + "uniform float u_smoothing;\n" //
            + "varying vec4 v_color;\n" //
            + "varying vec2 v_texCoords;\n" //
            + "varying float v_texture_index;\n" //
            + "\n" //
            + ShaderCompiler.GET_TEXTURE_FROM_ARRAY_PLACEHOLDER + "\n"
            + "\n" //
            + "void main() {\n" //
            + "    float smoothing = 0.25 / u_smoothing;\n" //
            + "    float distance = getTextureFromArray(v_texCoords).a;\n" //
            + "    float alpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, distance);\n" //
            + "    gl_FragColor = vec4(v_color.rgb, alpha * v_color.a);\n" //
            + "}\n";
    public static String DISTANCE_FIELD_VERTEX_SHADER = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
            + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
            + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
            + "attribute float " + TextureArrayPolygonSpriteBatch.TEXTURE_INDEX_ATTRIBUTE + ";\n" //
            + "uniform mat4 u_projTrans;\n" //
            + "varying vec4 v_color;\n" //
            + "varying vec2 v_texCoords;\n" //
            + "varying float v_texture_index;\n" //
            + "\n" //
            + "void main() {\n" //
            + "    v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
            + "    v_color.a = v_color.a * (255.0/254.0);\n" //
            + "    v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
            + "    v_texture_index = " + TextureArrayPolygonSpriteBatch.TEXTURE_INDEX_ATTRIBUTE + ";\n" //
            + "    gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
            + "}\n";

    public static String DEFAULT_SCREE_READING_VERTEX_SHADER = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
            + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
            + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
            + "attribute float " + TextureArrayPolygonSpriteBatch.TEXTURE_INDEX_ATTRIBUTE + ";\n" //
            + "uniform mat4 u_projTrans;\n" //
            + "uniform vec4 u_screen_coords;\n" //
            + "varying vec4 v_color;\n" //
            + "varying vec2 v_texCoords;\n" //
            + "varying float v_texture_index;\n" //
            + "\n" //
            + "//A function that interpolate two vec2 using another vec2\n" //
            + "vec2 lerp2(vec2 a, vec2 b, vec2 t) {\n" //
            + "    return a + (b - a) * t;\n" //
            + "}\n" //
            + "\n" //
            + "void main() {\n" //
            + "    v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
            + "    v_color.a = v_color.a * (255.0/254.0);\n" //
            + "    v_texCoords = lerp2(u_screen_coords.xy, u_screen_coords.zw, " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0);\n"
            + "    v_texture_index = " + TextureArrayPolygonSpriteBatch.TEXTURE_INDEX_ATTRIBUTE + ";\n" //
            + "    gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
            + "}\n";
    public static String DEFAULT_SCREE_READING_FRAGMENT_SHADER = "#ifdef GL_ES\n" //
            + "#define LOWP lowp\n" //
            + "precision mediump float;\n" //
            + "#else\n" //
            + "#define LOWP \n" //
            + "#endif\n" //
            + "varying LOWP vec4 v_color;\n" //
            + "varying vec2 v_texCoords;\n" //
            + "varying float v_texture_index;\n" //
            + "uniform sampler2D u_textures[MAX_TEXTURE_UNITS];\n" //
            + "\n" //
            + ShaderCompiler.GET_TEXTURE_FROM_ARRAY_PLACEHOLDER + "\n"
            + "\n" //
            + "void main() {\n"//
            + "    gl_FragColor = v_color * getTextureFromArray(v_texCoords);\n"
            + "}\n";
}