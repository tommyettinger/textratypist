package com.github.tommyettinger.textra;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.github.tommyettinger.textra.Font.DistanceFieldType;

/**
 * Utility methods that return vertex or fragment shader source code to be used in
 * {@link TextureArrayPolygonSpriteBatch} or {@link TextureArrayCpuPolygonSpriteBatch}. These methods all require one of
 * those mentioned Batches to have been created before any shader source can be obtained, since creating either Texture
 * Array Batch calculates the number of texture units the GPU can handle.
 * <br>
 * These shader sources can be passed to
 * {@link KnownFonts#initialize(String, String, String, String, String, String, String, String)}
 * if the only batch or batches you intend to use for Font types are Texture Array Batches. To do this more easily, make
 * sure you have constructed a TextureArrayPolygonSpriteBatch or TextureArrayCpuPolygonSpriteBatch in create() or later,
 * then call {@link #initializeTextureArrayShaders()} before using any methods from KnownFonts or creating any Font.
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
                + "varying float v_texture_index;\n" //
                + "uniform sampler2D u_textures[MAX_TEXTURE_UNITS];\n" //
                + "\n" //
                + TextureArrayShaderCompiler.GET_TEXTURE_FROM_ARRAY_PLACEHOLDER + "\n"
                + "\n" //
                + "void main() {\n"//
                + "    gl_FragColor = v_color * getTextureFromArray(v_texCoords);\n" //
                + "}\n");
    }

    /**
     * The fragment shader used for any rendering with a {@link TextureArrayPolygonSpriteBatch} or
     * {@link TextureArrayCpuPolygonSpriteBatch} when {@link DistanceFieldType#SDF} is used.
     * This shader has the uniform {@code u_smoothing} and expects to be used with
     * {@link #defaultArrayVertexShader()} as its vertex shader (but any vertex shaders here are the same).
     *
     * @return a fragment shader String that works with TextureArray batches
     */
    public static String sdfArrayFragmentShader() {
        return TextureArrayShaderCompiler.processArrayTextureShader("#ifdef GL_ES\n" //
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
                + TextureArrayShaderCompiler.GET_TEXTURE_FROM_ARRAY_PLACEHOLDER + "\n"
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
    }
    /**
     * An alias for {@link #defaultArrayVertexShader()}; using that method is preferred.
     *
     * @return a vertex shader String that works with TextureArray batches
     */
    public static String sdfArrayVertexShader() {
        return defaultArrayVertexShader();
    }

    /**
     * The fragment shader used for any rendering with a {@link TextureArrayPolygonSpriteBatch} or
     * {@link TextureArrayCpuPolygonSpriteBatch} when {@link DistanceFieldType#SDF} is used.
     * This particular shader uses the dFdx() and dFdy() methods from GLSL, which are only defined in desktop OpenGL or
     * in mobile/browser OpenGL ES when the extension "GL_OES_standard_derivatives" is available and enabled.
     * This tends to look a little fuzzy compared to {@link #sdfArrayFragmentShader()}, and isn't quite as fast.
     * This shader has the uniform {@code u_smoothing} and expects to be used with
     * {@link #defaultArrayVertexShader()} as its vertex shader (but any vertex shaders here are the same).
     *
     * @return a fragment shader String that works with TextureArray batches
     */
    public static String sdfDerivativeArrayFragmentShader() {
        return TextureArrayShaderCompiler.processArrayTextureShader("#ifdef GL_ES\n" //
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
                + TextureArrayShaderCompiler.GET_TEXTURE_FROM_ARRAY_PLACEHOLDER + "\n"
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
    }
    /**
     * An alias for {@link #defaultArrayVertexShader()}; using that method is preferred.
     *
     * @return a vertex shader String that works with TextureArray batches
     */
    public static String sdfDerivativeArrayVertexShader() {
        return defaultArrayVertexShader();
    }

    /**
     * Returns either {@link #sdfArrayFragmentShader()} or {@link #sdfDerivativeArrayFragmentShader()}, depending on
     * whether derivatives are supported. This shader has the uniform {@code u_smoothing} and expects to be used with
     * {@link #defaultArrayVertexShader()} as its vertex shader (but any vertex shaders here are the same).
     *
     * @return a fragment shader String for an SDF shader
     */
    public static String sdfAdaptiveArrayFragmentShader() {
        return Gdx.app.getType() == Application.ApplicationType.Desktop || Gdx.graphics.supportsExtension("GL_OES_standard_derivatives")
                ? sdfDerivativeArrayFragmentShader() : sdfArrayFragmentShader();
    }

    /**
     * The fragment shader used for any rendering with a {@link TextureArrayPolygonSpriteBatch} or
     * {@link TextureArrayCpuPolygonSpriteBatch} when {@link DistanceFieldType#SDF_OUTLINE} is used.
     * This shader has the uniform {@code u_smoothing} and expects to be used with
     * {@link #defaultArrayVertexShader()} as its vertex shader (but any vertex shaders here are the same).
     * <br>
     * This draws a black outline around any text with an SDF_OUTLINE distance field, and leaves the color inside the
     * outlined area the same. If the outline's thickness isn't suitable for your purposes, you may want to adjust the
     * "closeness" constant in this code. You can replace the text {@code "const float closeness =  0.0625  ;"} (with
     * multiple spaces around the value to make distinguishing it easier), using any value between 0.0 and 0.5 to change
     * the outline thickness. Lower values lead to thicker outlines, and values closer to 0.5 should lead to thinner.
     *
     * @return a fragment shader String that works with TextureArray batches
     */
    public static String sdfOutlineArrayFragmentShader() {
        return TextureArrayShaderCompiler.processArrayTextureShader("#ifdef GL_ES\n" //
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
                + TextureArrayShaderCompiler.GET_TEXTURE_FROM_ARRAY_PLACEHOLDER + "\n"
                + "\n" //
                + "const float closeness =  0.0625  ;\n" // Between 0 and 0.5, 0 = thick outline, 0.5 = no outline
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
    }
    /**
     * An alias for {@link #defaultArrayVertexShader()}; using that method is preferred.
     *
     * @return a vertex shader String that works with TextureArray batches
     */
    public static String sdfOutlineArrayVertexShader() {
        return defaultArrayVertexShader();
    }

    /**
     * The fragment shader used for any rendering with a {@link TextureArrayPolygonSpriteBatch} or
     * {@link TextureArrayCpuPolygonSpriteBatch} when {@link DistanceFieldType#SDF_OUTLINE} is used.
     * This particular shader uses the dFdx() and dFdy() methods from GLSL, which are only defined in desktop OpenGL or
     * in mobile/browser OpenGL ES when the extension "GL_OES_standard_derivatives" is available and enabled.
     * This tends to look a little fuzzy compared to {@link #sdfOutlineArrayFragmentShader()}, and isn't quite as fast.
     * This shader has the uniform {@code u_smoothing} and expects to be used with
     * {@link #defaultArrayVertexShader()} as its vertex shader (but any vertex shaders here are the same).
     * <br>
     * This draws a black outline around any text with an SDF_OUTLINE distance field, and leaves the color inside the
     * outlined area the same. If the outline's thickness isn't suitable for your purposes, you may want to adjust the
     * "closeness" constant in this code. You can replace the text {@code "const float closeness =  0.0625  ;"} (with
     * multiple spaces around the value to make distinguishing it easier), using any value between 0.0 and 0.5 to change
     * the outline thickness. Lower values lead to thicker outlines, and values closer to 0.5 should lead to thinner.
     *
     * @return a fragment shader String that works with TextureArray batches
     */
    public static String sdfOutlineDerivativeArrayFragmentShader() {
        return TextureArrayShaderCompiler.processArrayTextureShader("#ifdef GL_ES\n" //
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
                + TextureArrayShaderCompiler.GET_TEXTURE_FROM_ARRAY_PLACEHOLDER + "\n"
                + "\n" //
                + "const float closeness =  0.0625  ;\n" // Between 0 and 0.5, 0 = thick outline, 0.5 = no outline
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
    }
    /**
     * An alias for {@link #defaultArrayVertexShader()}; using that method is preferred.
     *
     * @return a vertex shader String that works with TextureArray batches
     */
    public static String sdfOutlineDerivativeArrayVertexShader() {
        return defaultArrayVertexShader();
    }

    /**
     * Returns either {@link #sdfOutlineArrayFragmentShader()} or {@link #sdfOutlineDerivativeArrayFragmentShader()},
     * depending on whether derivatives are supported. This shader has the uniform {@code u_smoothing} and expects to be
     * used with {@link #defaultArrayVertexShader()} as its vertex shader (but any vertex shaders here are the same).
     * @return a fragment shader String for an SDF shader
     */
    public static String sdfOutlineAdaptiveArrayFragmentShader() {
        return Gdx.app.getType() == Application.ApplicationType.Desktop || Gdx.graphics.supportsExtension("GL_OES_standard_derivatives")
                ? sdfOutlineDerivativeArrayFragmentShader() : sdfOutlineArrayFragmentShader();
    }

    /**
     * The fragment shader used for any rendering with a {@link TextureArrayPolygonSpriteBatch} or
     * {@link TextureArrayCpuPolygonSpriteBatch} when {@link DistanceFieldType#MSDF} is used.
     * This shader has the uniform {@code u_smoothing} and expects to be used with
     * {@link #defaultArrayVertexShader()} as its vertex shader (but any vertex shaders here are the same).
     *
     * @return a fragment shader String that works with TextureArray batches
     */
    public static String msdfArrayFragmentShader() {
        return TextureArrayShaderCompiler.processArrayTextureShader("#ifdef GL_ES\n" //
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
                + TextureArrayShaderCompiler.GET_TEXTURE_FROM_ARRAY_PLACEHOLDER + "\n"
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
    }
    /**
     * An alias for {@link #defaultArrayVertexShader()}; using that method is preferred.
     *
     * @return a vertex shader String that works with TextureArray batches
     */
    public static String msdfArrayVertexShader() {
        return defaultArrayVertexShader();
    }

    /**
     * This is a convenience method to initialize the shaders in {@link KnownFonts} so they work with
     * {@link TextureArrayPolygonSpriteBatch} and/or {@link TextureArrayCpuPolygonSpriteBatch}. This can only be called
     * after one of those mentioned Batches has already been constructed, in create() or later. Because this calls
     * {@link KnownFonts#initialize(String, String, String, String, String, String, String, String)}, it can't be called
     * after any methods from KnownFonts have already been called, or after any Font is created. Unlike
     * {@link #initializeAdaptiveTextureArrayShaders()}, this always uses the same shaders on all platforms. It doesn't
     * ever use derivatives in shaders.
     */
    public static void initializeTextureArrayShaders() {
        final String vertexShader = defaultArrayVertexShader();
        KnownFonts.initialize(
                vertexShader, defaultArrayFragmentShader(),
                vertexShader, sdfArrayFragmentShader(),
                vertexShader, sdfOutlineArrayFragmentShader(),
                vertexShader, msdfArrayFragmentShader());

    }

    /**
     * This is a convenience method to initialize the shaders in {@link KnownFonts} so they work with
     * {@link TextureArrayPolygonSpriteBatch} and/or {@link TextureArrayCpuPolygonSpriteBatch}. This can only be called
     * after one of those mentioned Batches has already been constructed, in create() or later. Because this calls
     * {@link KnownFonts#initialize(String, String, String, String, String, String, String, String)}, it can't be called
     * after any methods from KnownFonts have already been called, or after any Font is created. This variant will adapt
     * the SDF shaders depending on whether derivatives are available, and will use them if they are. If used heavily,
     * the shaders that use derivatives may not perform as well as the ones that don't. They may look better
     * (subjectively) when used, which can only be on desktop and mobile platforms right now (no web browsers).
     */
    public static void initializeAdaptiveTextureArrayShaders() {
        final String vertexShader = defaultArrayVertexShader();
        KnownFonts.initialize(
                vertexShader, defaultArrayFragmentShader(),
                vertexShader, sdfAdaptiveArrayFragmentShader(),
                vertexShader, sdfOutlineAdaptiveArrayFragmentShader(),
                vertexShader, msdfArrayFragmentShader());

    }
}