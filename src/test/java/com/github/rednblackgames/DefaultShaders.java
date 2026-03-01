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
    public static String DEFAULT_ARRAY_FRAGMENT_SHADER = ShaderCompiler.processArrayTextureShader
             ("#ifdef GL_ES\n" //
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
            + "}\n");

    public static String SDF_ARRAY_FRAGMENT_SHADER = ShaderCompiler.processArrayTextureShader
            ("#ifdef GL_ES\n" //
            + "precision mediump float;\n" //
            + "precision mediump int;\n" //
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
            + "	   if (u_smoothing > 0.0) {\n" //
            + "        float smoothing = 0.5 / u_smoothing;\n" //
            + "        vec4 color = getTextureFromArray(v_texCoords);\n" //
            + "        float alpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, color.a);\n" //
            + "        gl_FragColor = vec4(v_color.rgb * color.rgb, alpha * v_color.a);\n" //
            + "    } else {\n" //
            + "        gl_FragColor = v_color * getTextureFromArray(v_texCoords);\n" //
            + "    }\n" //
            + "}\n");
    public static String SDF_ARRAY_VERTEX_SHADER = DEFAULT_ARRAY_VERTEX_SHADER;

    public static String SDF_DERIVATIVE_ARRAY_FRAGMENT_SHADER = ShaderCompiler.processArrayTextureShader
            ("#ifdef GL_ES\n" //
            + "#extension GL_OES_standard_derivatives : enable\n" //
            + "precision mediump float;\n" //
            + "precision mediump int;\n" //
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
            + "	   if (u_smoothing > 0.0) {\n" //
            + "        vec4 color = getTextureFromArray(v_texCoords);\n" //
            + "        float smoothing = 0.8 * length(vec2(dFdx(color.a), dFdy(color.a)));\n" //
            + "        float alpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, color.a);\n" //
            + "        gl_FragColor = vec4(v_color.rgb * color.rgb, alpha * v_color.a);\n" //
            + "    } else {\n" //
            + "        gl_FragColor = v_color * getTextureFromArray(v_texCoords);\n" //
            + "    }\n" //
            + "}\n");
    public static String SDF_DERIVATIVE_ARRAY_VERTEX_SHADER = DEFAULT_ARRAY_VERTEX_SHADER;

    public static String SDF_OUTLINE_ARRAY_FRAGMENT_SHADER = ShaderCompiler.processArrayTextureShader
            ("#ifdef GL_ES\n" //
            + "precision mediump float;\n" //
            + "precision mediump int;\n" //
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
            + "const float closeness = 0.0625;\n" // Between 0 and 0.5, 0 = thick outline, 0.5 = no outline
            + "\n" //
            + "void main() {\n" //
            + "	   if (u_smoothing > 0.0) {\n" //
            + "        vec4 image = getTextureFromArray(v_texCoords);\n" //
            + "        float smoothing = 0.5 / u_smoothing;\n" //
            + "        float outlineFactor = smoothstep(0.5 - smoothing, 0.4 * smoothing + 0.5, image.a);\n" //
            + "        vec3 color = image.rgb * v_color.rgb * outlineFactor;\n" //
            + "        float alpha = smoothstep(closeness, closeness + 0.1, image.a);\n" //
            + "        gl_FragColor = vec4(color, v_color.a * alpha);\n" //
            + "    } else {\n" //
            + "        gl_FragColor = v_color * getTextureFromArray(v_texCoords);\n" //
            + "    }\n" //
            + "}\n");
    public static String SDF_OUTLINE_ARRAY_VERTEX_SHADER = DEFAULT_ARRAY_VERTEX_SHADER;

    public static String SDF_OUTLINE_DERIVATIVE_ARRAY_FRAGMENT_SHADER = ShaderCompiler.processArrayTextureShader
            ("#ifdef GL_ES\n" //
            + "#extension GL_OES_standard_derivatives : enable\n" //
            + "precision mediump float;\n" //
            + "precision mediump int;\n" //
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
            + "	   if (u_smoothing > 0.0) {\n" //
            + "        vec4 image = getTextureFromArray(v_texCoords);\n" //
            + "        float smoothing = 0.8 * length(vec2(dFdx(image.a), dFdy(image.a)));\n" //
            + "        float outlineFactor = smoothstep(0.5 - smoothing, 0.4 * smoothing + 0.5, image.a);\n" //
            + "        vec3 color = image.rgb * v_color.rgb * outlineFactor;\n" //
            + "        float alpha = smoothstep(closeness, closeness + 0.66 / u_smoothing, image.a);\n" //
            + "        gl_FragColor = vec4(color, v_color.a * alpha);\n" //
            + "    } else {\n" //
            + "        gl_FragColor = v_color * getTextureFromArray(v_texCoords);\n" //
            + "    }\n" //
            + "}\n");
    public static String SDF_OUTLINE_DERIVATIVE_ARRAY_VERTEX_SHADER = DEFAULT_ARRAY_VERTEX_SHADER;

    public static String MSDF_ARRAY_FRAGMENT_SHADER = ShaderCompiler.processArrayTextureShader
            ("#ifdef GL_ES\n" //
            + "precision mediump float;\n" //
            + "precision mediump int;\n" //
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
            + "float median(float r, float g, float b) {\n" //
            + "    return max(min(r, g), min(max(r, g), b));\n" //
            + "}\n" //
            + "\n" //
            + "void main() {\n" //
            + "	   if (u_smoothing > 0.0) {\n" //
            + "        vec4 msdf = getTextureFromArray(v_texCoords);\n" //
            + "        float distance = u_smoothing * (median(msdf.r, msdf.g, msdf.b) - 0.5);\n" //
            + "        float glyphAlpha = clamp(distance + 0.5, 0.0, 1.0);\n" //
            + "        gl_FragColor = vec4(v_color.rgb, glyphAlpha * v_color.a);\n" //
            + "    } else {\n" //
            + "        gl_FragColor = v_color * getTextureFromArray(v_texCoords);\n" //
            + "    }\n" //
            + "}\n");
    public static String MSDF_ARRAY_VERTEX_SHADER = DEFAULT_ARRAY_VERTEX_SHADER;
}