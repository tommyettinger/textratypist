package com.github.tommyettinger.textra;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import java.nio.IntBuffer;
import java.util.IdentityHashMap;

/**
 * Desktop OpenGL 3 Batch backed by a GL_TEXTURE_2D_ARRAY.
 * Apache-2.0; derived from Pixscape HudBatch.
 * <br>
 * Between begin/end this batch caches its shader and texture-array bindings.
 * Before external rendering, flush this batch; after external rendering, call
 * {@link #invalidateGlState()} before drawing or flushing this batch again.
 * This includes interleaving another batch, changing the active texture unit,
 * or modifying the batch shader's uniforms. Every begin starts with fresh GL state.
 * Texture contents changed after registration require {@link #refresh(Texture)}.
 */
public final class TextureArrayGL30Batch implements Batch {
    public static final int DEFAULT_CAPACITY = 1000;
    public static final int MAX_CAPACITY = 8191;

    private static final int VERTICES_PER_QUAD = 4;
    private static final int INDICES_PER_QUAD = 6;

    private final Mesh mesh;
    private final float[] vertices;
    private final int capacity;
    private final BatchState state = new BatchState();
    private final ShaderProgram defaultShader;
    private ShaderProgram customShader;
    private int projectionUniformLocation;
    private int arrayUniformLocation;

    private final IdentityHashMap<Texture,Integer> layers = new IdentityHashMap<Texture,Integer>();
    private final Texture[] occupants;
    private final int layerWidth, layerHeight, layerCount, arrayHandle, copyFramebuffer;
    private final IntBuffer savedFramebuffer = IntBuffer.allocate(1);
    private int nextLayer;
    private float uvScaleX, uvScaleY;
    private int vertexFloatCount;
    private int renderSubmissions;
    private boolean drawing;
    private boolean projectionUniformDirty = true;
    private boolean arrayUniformDirty = true;
    private boolean shaderBindingDirty = true;
    private boolean arrayBindingDirty = true;
    private Texture lastTexture;
    private int lastLayer = -1;
    private int lastTextureWidth, lastTextureHeight;
    private float lastUvScaleX, lastUvScaleY;

    public TextureArrayGL30Batch(int layerWidth, int layerHeight, int layerCount, ShaderProgram shader) {
        this(DEFAULT_CAPACITY, layerWidth, layerHeight, layerCount, shader);
    }
    public TextureArrayGL30Batch(int capacity, int layerWidth, int layerHeight, int layerCount, ShaderProgram shader) {
        validateCapacity(capacity);
        requireGl30();
        if (layerWidth <= 0 || layerHeight <= 0 || layerCount <= 0) throw new IllegalArgumentException("Array dimensions must be positive");
        if (layerWidth > glLimit(GL20.GL_MAX_TEXTURE_SIZE) || layerHeight > glLimit(GL20.GL_MAX_TEXTURE_SIZE)
                || layerCount > glLimit(GL30.GL_MAX_ARRAY_TEXTURE_LAYERS))
            throw new IllegalArgumentException("Texture array exceeds GPU limits");
        if (shader == null) throw new IllegalArgumentException("shader is null");
        validateShader(shader);
        this.capacity = capacity;
        this.layerWidth = layerWidth;
        this.layerHeight = layerHeight;
        this.layerCount = layerCount;
        this.occupants = new Texture[layerCount];
        this.defaultShader = shader;
        cacheUniformLocations(shader);
        mesh = new Mesh(Mesh.VertexDataType.VertexBufferObjectWithVAO, false, capacity * 4, capacity * 6,
                new VertexAttribute(Usage.Position, 2, "a_position"),
                new VertexAttribute(Usage.ColorPacked, 4, "a_color"),
                new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"),
                new VertexAttribute(Usage.Generic, 1, "a_layer"));
        vertices = new float[capacity * 24];
        short[] indices = new short[capacity * 6];
        for (int q = 0, i = 0, v = 0; q < capacity; q++, v += 4) {
            indices[i++] = (short)v; indices[i++] = (short)(v + 1);
            indices[i++] = (short)(v + 2); indices[i++] = (short)(v + 2);
            indices[i++] = (short)(v + 3); indices[i++] = (short)v;
        }
        mesh.setIndices(indices);
        arrayHandle = Gdx.gl.glGenTexture();
        copyFramebuffer = Gdx.gl.glGenFramebuffer();
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        Gdx.gl.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, arrayHandle);
        Gdx.gl30.glTexImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, GL30.GL_RGBA8,
                layerWidth, layerHeight, layerCount, 0, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, null);
        int allocationError = Gdx.gl.glGetError();
        if (allocationError != GL20.GL_NO_ERROR) {
            Gdx.gl.glDeleteFramebuffer(copyFramebuffer);
            Gdx.gl.glDeleteTexture(arrayHandle);
            mesh.dispose();
            throw new IllegalStateException("Texture array allocation failed, GL error " + allocationError);
        }
        Gdx.gl.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_NEAREST);
        Gdx.gl.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_NEAREST);
        Gdx.gl.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE);
        Gdx.gl.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE);
        if (Gdx.graphics != null) state.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
    }
    private static int glLimit(int name) {
        IntBuffer b = IntBuffer.allocate(1);
        Gdx.gl.glGetIntegerv(name, b);
        return b.get(0);
    }

    static void validateCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("TextureArrayGL30Batch capacity must be greater than zero: " + capacity);
        }
        if (capacity > MAX_CAPACITY) {
            throw new IllegalArgumentException(
                    "TextureArrayGL30Batch capacity cannot exceed " + MAX_CAPACITY + " quads: " + capacity);
        }
    }

    @Override
    public void begin() {
        if (drawing) throw new IllegalStateException("TextureArrayGL30Batch.end must be called before begin.");
        requireGl30();

        state.syncRealTransformToVirtual();
        drawing = true;
        renderSubmissions = 0;
        invalidateGlState();

        Gdx.gl.glDepthMask(false);
        bindShaderAndUniforms();
    }

    @Override
    public void end() {
        if (!drawing) throw new IllegalStateException("TextureArrayGL30Batch.begin must be called before end.");
        flush();
        drawing = false;
        invalidateGlState();

        Gdx.gl.glDepthMask(true);
        if (state.isBlendingEnabled()) Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void setColor(Color tint) {
        state.setColor(tint);
    }

    @Override
    public void setColor(float r, float g, float b, float a) {
        state.setColor(r, g, b, a);
    }

    @Override
    public Color getColor() {
        return state.color();
    }

    @Override
    public void setPackedColor(float packedColor) {
        state.setPackedColor(packedColor);
    }

    @Override
    public float getPackedColor() {
        return state.packedColor();
    }

    @Override
    public void draw(Texture texture, float x, float y, float originX, float originY,
                     float width, float height, float scaleX, float scaleY, float rotation,
                     int srcX, int srcY, int srcWidth, int srcHeight,
                     boolean flipX, boolean flipY) {
        requireDrawing();
        float layer = requireTextureLayer(texture);
        ensureQuadCapacity();

        float worldOriginX = x + originX;
        float worldOriginY = y + originY;
        float fx = -originX;
        float fy = -originY;
        float fx2 = width - originX;
        float fy2 = height - originY;
        if (scaleX != 1f || scaleY != 1f) {
            fx *= scaleX;
            fy *= scaleY;
            fx2 *= scaleX;
            fy2 *= scaleY;
        }

        float x1;
        float y1;
        float x2;
        float y2;
        float x3;
        float y3;
        float x4;
        float y4;
        if (rotation != 0f) {
            float cos = MathUtils.cosDeg(rotation);
            float sin = MathUtils.sinDeg(rotation);
            x1 = cos * fx - sin * fy;
            y1 = sin * fx + cos * fy;
            x2 = cos * fx - sin * fy2;
            y2 = sin * fx + cos * fy2;
            x3 = cos * fx2 - sin * fy2;
            y3 = sin * fx2 + cos * fy2;
            x4 = x1 + (x3 - x2);
            y4 = y3 - (y2 - y1);
        } else {
            x1 = fx;
            y1 = fy;
            x2 = fx;
            y2 = fy2;
            x3 = fx2;
            y3 = fy2;
            x4 = fx2;
            y4 = fy;
        }

        x1 += worldOriginX;
        y1 += worldOriginY;
        x2 += worldOriginX;
        y2 += worldOriginY;
        x3 += worldOriginX;
        y3 += worldOriginY;
        x4 += worldOriginX;
        y4 += worldOriginY;

        float inverseWidth = 1f / texture.getWidth();
        float inverseHeight = 1f / texture.getHeight();
        float u = srcX * inverseWidth;
        float v = (srcY + srcHeight) * inverseHeight;
        float u2 = (srcX + srcWidth) * inverseWidth;
        float v2 = srcY * inverseHeight;
        if (flipX) {
            float swap = u;
            u = u2;
            u2 = swap;
        }
        if (flipY) {
            float swap = v;
            v = v2;
            v2 = swap;
        }
        putQuad(x1, y1, u, v, x2, y2, u, v2,
                x3, y3, u2, v2, x4, y4, u2, v, layer);
    }

    @Override
    public void draw(Texture texture, float x, float y, float width, float height,
                     int srcX, int srcY, int srcWidth, int srcHeight,
                     boolean flipX, boolean flipY) {
        draw(texture, x, y, 0f, 0f, width, height, 1f, 1f, 0f,
                srcX, srcY, srcWidth, srcHeight, flipX, flipY);
    }

    @Override
    public void draw(Texture texture, float x, float y,
                     int srcX, int srcY, int srcWidth, int srcHeight) {
        draw(texture, x, y, srcWidth, srcHeight,
                srcX, srcY, srcWidth, srcHeight, false, false);
    }

    @Override
    public void draw(Texture texture, float x, float y, float width, float height,
                     float u, float v, float u2, float v2) {
        requireDrawing();
        float layer = requireTextureLayer(texture);
        ensureQuadCapacity();
        float x2 = x + width;
        float y2 = y + height;
        putQuad(x, y, u, v, x, y2, u, v2,
                x2, y2, u2, v2, x2, y, u2, v, layer);
    }

    @Override
    public void draw(Texture texture, float x, float y) {
        if (texture == null) throw new IllegalArgumentException("texture is null");
        draw(texture, x, y, texture.getWidth(), texture.getHeight());
    }

    @Override
    public void draw(Texture texture, float x, float y, float width, float height) {
        draw(texture, x, y, width, height, 0f, 1f, 1f, 0f);
    }

    @Override
    public void draw(Texture texture, float[] spriteVertices, int offset, int count) {
        requireDrawing();
        validateSpriteVertices(spriteVertices, offset, count);
        float layer = requireTextureLayer(texture);

        int remainingSourceFloats = count;
        int sourceOffset = offset;
        while (remainingSourceFloats > 0) {
            int availableQuads = (vertices.length - vertexFloatCount)
                    / 24;
            if (availableQuads == 0) {
                flush();
                availableQuads = capacity;
            }
            int requestedQuads = remainingSourceFloats / 20;
            int copiedQuads = Math.min(availableQuads, requestedQuads);
            int sourceFloatCount = copiedQuads * 20;
            vertexFloatCount += copySpriteVertices(spriteVertices, sourceOffset, sourceFloatCount,
                    vertices, vertexFloatCount, layer, uvScaleX, uvScaleY,
                    state.vertexAdjustmentNeeded() ? state.vertexAdjustment() : null);
            sourceOffset += sourceFloatCount;
            remainingSourceFloats -= sourceFloatCount;
        }
    }

    @Override
    public void draw(TextureRegion region, float x, float y) {
        if (region == null) throw new IllegalArgumentException("region is null");
        draw(region, x, y, region.getRegionWidth(), region.getRegionHeight());
    }

    @Override
    public void draw(TextureRegion region, float x, float y, float width, float height) {
        requireRegion(region);
        requireDrawing();
        float layer = requireTextureLayer(region.getTexture());
        ensureQuadCapacity();
        float x2 = x + width;
        float y2 = y + height;
        putQuad(x, y, region.getU(), region.getV2(),
                x, y2, region.getU(), region.getV(),
                x2, y2, region.getU2(), region.getV(),
                x2, y, region.getU2(), region.getV2(), layer);
    }

    @Override
    public void draw(TextureRegion region, float x, float y, float originX, float originY,
                     float width, float height, float scaleX, float scaleY, float rotation) {
        requireRegion(region);
        requireDrawing();
        float layer = requireTextureLayer(region.getTexture());
        ensureQuadCapacity();
        putTransformedRegion(x, y, originX, originY, width, height,
                scaleX, scaleY, rotation,
                region.getU(), region.getV2(),
                region.getU(), region.getV(),
                region.getU2(), region.getV(),
                region.getU2(), region.getV2(), layer);
    }

    @Override
    public void draw(TextureRegion region, float x, float y, float originX, float originY,
                     float width, float height, float scaleX, float scaleY, float rotation,
                     boolean clockwise) {
        requireRegion(region);
        requireDrawing();
        float layer = requireTextureLayer(region.getTexture());
        ensureQuadCapacity();

        float u1;
        float v1;
        float u2;
        float v2;
        float u3;
        float v3;
        float u4;
        float v4;
        if (clockwise) {
            u1 = region.getU2();
            v1 = region.getV2();
            u2 = region.getU();
            v2 = region.getV2();
            u3 = region.getU();
            v3 = region.getV();
            u4 = region.getU2();
            v4 = region.getV();
        } else {
            u1 = region.getU();
            v1 = region.getV();
            u2 = region.getU2();
            v2 = region.getV();
            u3 = region.getU2();
            v3 = region.getV2();
            u4 = region.getU();
            v4 = region.getV2();
        }
        putTransformedRegion(x, y, originX, originY, width, height,
                scaleX, scaleY, rotation,
                u1, v1, u2, v2, u3, v3, u4, v4, layer);
    }

    @Override
    public void draw(TextureRegion region, float width, float height, Affine2 transform) {
        requireRegion(region);
        if (transform == null) throw new IllegalArgumentException("transform is null");
        requireDrawing();
        float layer = requireTextureLayer(region.getTexture());
        ensureQuadCapacity();

        float x1 = transform.m02;
        float y1 = transform.m12;
        float x2 = transform.m01 * height + transform.m02;
        float y2 = transform.m11 * height + transform.m12;
        float x3 = transform.m00 * width + transform.m01 * height + transform.m02;
        float y3 = transform.m10 * width + transform.m11 * height + transform.m12;
        float x4 = transform.m00 * width + transform.m02;
        float y4 = transform.m10 * width + transform.m12;

        putQuad(x1, y1, region.getU(), region.getV2(),
                x2, y2, region.getU(), region.getV(),
                x3, y3, region.getU2(), region.getV(),
                x4, y4, region.getU2(), region.getV2(), layer);
    }

    @Override
    public void flush() {
        if (vertexFloatCount == 0) return;
        bindShaderAndUniforms();
        bindTextureArray();

        if (state.isBlendingEnabled()) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            if (state.blendSrcFunc() != -1) {
                Gdx.gl.glBlendFuncSeparate(
                        state.blendSrcFunc(), state.blendDstFunc(),
                        state.blendSrcFuncAlpha(), state.blendDstFuncAlpha());
            }
        } else {
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        mesh.setVertices(vertices, 0, vertexFloatCount);
        int quadCount = vertexFloatCount / 24;
        mesh.render(activeShader(), GL20.GL_TRIANGLES, 0, quadCount * INDICES_PER_QUAD);
        renderSubmissions++;
        vertexFloatCount = 0;
    }

    @Override
    public void disableBlending() {
        if (!state.isBlendingEnabled()) return;
        if (drawing) flush();
        state.setBlendingEnabled(false);
    }

    @Override
    public void enableBlending() {
        if (state.isBlendingEnabled()) return;
        if (drawing) flush();
        state.setBlendingEnabled(true);
    }

    @Override
    public void setBlendFunction(int srcFunc, int dstFunc) {
        setBlendFunctionSeparate(srcFunc, dstFunc, srcFunc, dstFunc);
    }

    @Override
    public void setBlendFunctionSeparate(int srcFuncColor, int dstFuncColor,
                                         int srcFuncAlpha, int dstFuncAlpha) {
        if (state.blendSrcFunc() == srcFuncColor && state.blendDstFunc() == dstFuncColor
                && state.blendSrcFuncAlpha() == srcFuncAlpha
                && state.blendDstFuncAlpha() == dstFuncAlpha) {
            return;
        }
        if (drawing) flush();
        state.setBlendFunctionSeparate(srcFuncColor, dstFuncColor, srcFuncAlpha, dstFuncAlpha);
    }

    @Override
    public int getBlendSrcFunc() {
        return state.blendSrcFunc();
    }

    @Override
    public int getBlendDstFunc() {
        return state.blendDstFunc();
    }

    @Override
    public int getBlendSrcFuncAlpha() {
        return state.blendSrcFuncAlpha();
    }

    @Override
    public int getBlendDstFuncAlpha() {
        return state.blendDstFuncAlpha();
    }

    @Override
    public Matrix4 getProjectionMatrix() {
        return state.projectionMatrix();
    }

    @Override
    public Matrix4 getTransformMatrix() {
        return state.transformMatrix();
    }

    @Override
    public void setProjectionMatrix(Matrix4 projection) {
        if (drawing) flush();
        state.setProjectionMatrix(projection);
        projectionUniformDirty = true;
        if (drawing) bindShaderAndUniforms();
    }

    @Override
    public void setTransformMatrix(Matrix4 transform) {
        state.setTransformMatrix(transform, drawing);
    }

    @Override
    public void setShader(ShaderProgram shader) {
        ShaderProgram nextShader = shader != null ? shader : defaultShader;
        if (nextShader == activeShader()) return;
        validateShader(nextShader);
        if (drawing) flush();

        customShader = shader;
        cacheUniformLocations(nextShader);
        shaderBindingDirty = true;
        projectionUniformDirty = true;
        arrayUniformDirty = true;
        if (drawing) bindShaderAndUniforms();
    }

    @Override
    public ShaderProgram getShader() {
        return activeShader();
    }

    @Override
    public boolean isBlendingEnabled() {
        return state.isBlendingEnabled();
    }

    @Override
    public boolean isDrawing() {
        return drawing;
    }

    /** Recopy a resident texture after its GPU contents change. */
    public void refresh(Texture texture) {
        Integer layer = layers.get(texture);
        if (layer == null) throw new IllegalArgumentException("Texture is not resident");
        validateTextureSize(texture);
        if (drawing) flush();
        copyToLayer(texture, layer);
    }

    /**
     * Forget cached GL bindings and uniform values without flushing queued vertices.
     * Call after external GL work, having flushed before that work to preserve order.
     * The next draw submission restores the batch shader, uniforms and array binding.
     * This does not restore framebuffer, viewport, scissor, or other caller-owned state.
     */
    public void invalidateGlState() {
        shaderBindingDirty = true;
        arrayBindingDirty = true;
        projectionUniformDirty = true;
        arrayUniformDirty = true;
    }

    int renderSubmissionsForTest() {
        return renderSubmissions;
    }

    @Override
    public void dispose() {
        lastTexture = null;
        layers.clear();
        for (int i = 0; i < occupants.length; i++) occupants[i] = null;
        mesh.dispose();
        Gdx.gl.glDeleteTexture(arrayHandle);
        Gdx.gl.glDeleteFramebuffer(copyFramebuffer);
    }

    private void ensureQuadCapacity() {
        if (vertexFloatCount == vertices.length) flush();
    }

    private void putQuad(float x1, float y1, float u1, float v1,
                         float x2, float y2, float u2, float v2,
                         float x3, float y3, float u3, float v3,
                         float x4, float y4, float u4, float v4,
                         float layer) {
        requireUv(u1, v1); requireUv(u2, v2);
        requireUv(u3, v3); requireUv(u4, v4);
        float color = state.packedColor();
        int index = vertexFloatCount;
        index = putVertex(index, x1, y1, color, u1, v1, layer);
        index = putVertex(index, x2, y2, color, u2, v2, layer);
        index = putVertex(index, x3, y3, color, u3, v3, layer);
        vertexFloatCount = putVertex(index, x4, y4, color, u4, v4, layer);
    }

    private void putTransformedRegion(float x, float y, float originX, float originY,
                                      float width, float height, float scaleX, float scaleY,
                                      float rotation,
                                      float u1, float v1, float u2, float v2,
                                      float u3, float v3, float u4, float v4,
                                      float layer) {
        float worldOriginX = x + originX;
        float worldOriginY = y + originY;
        float fx = -originX;
        float fy = -originY;
        float fx2 = width - originX;
        float fy2 = height - originY;
        if (scaleX != 1f || scaleY != 1f) {
            fx *= scaleX;
            fy *= scaleY;
            fx2 *= scaleX;
            fy2 *= scaleY;
        }

        float x1;
        float y1;
        float x2;
        float y2;
        float x3;
        float y3;
        float x4;
        float y4;
        if (rotation != 0f) {
            float cos = MathUtils.cosDeg(rotation);
            float sin = MathUtils.sinDeg(rotation);
            x1 = cos * fx - sin * fy;
            y1 = sin * fx + cos * fy;
            x2 = cos * fx - sin * fy2;
            y2 = sin * fx + cos * fy2;
            x3 = cos * fx2 - sin * fy2;
            y3 = sin * fx2 + cos * fy2;
            x4 = x1 + (x3 - x2);
            y4 = y3 - (y2 - y1);
        } else {
            x1 = fx;
            y1 = fy;
            x2 = fx;
            y2 = fy2;
            x3 = fx2;
            y3 = fy2;
            x4 = fx2;
            y4 = fy;
        }

        x1 += worldOriginX;
        y1 += worldOriginY;
        x2 += worldOriginX;
        y2 += worldOriginY;
        x3 += worldOriginX;
        y3 += worldOriginY;
        x4 += worldOriginX;
        y4 += worldOriginY;

        putQuad(x1, y1, u1, v1, x2, y2, u2, v2,
                x3, y3, u3, v3, x4, y4, u4, v4, layer);
    }

    private int putVertex(int index, float x, float y, float color,
                          float u, float v, float layer) {
        if (state.vertexAdjustmentNeeded()) {
            Affine2 adjustment = state.vertexAdjustment();
            vertices[index++] = adjustment.m00 * x + adjustment.m01 * y + adjustment.m02;
            vertices[index++] = adjustment.m10 * x + adjustment.m11 * y + adjustment.m12;
        } else {
            vertices[index++] = x;
            vertices[index++] = y;
        }
        vertices[index++] = color;
        vertices[index++] = u * uvScaleX;
        vertices[index++] = v * uvScaleY;
        vertices[index++] = layer;
        return index;
    }

    private float requireTextureLayer(Texture texture) {
        if (texture == null) throw new IllegalArgumentException("texture is null");
        // Preserve dimension-change detection without hashing or dividing on a hit.
        if (texture == lastTexture && texture.getWidth() == lastTextureWidth
                && texture.getHeight() == lastTextureHeight) {
            uvScaleX = lastUvScaleX;
            uvScaleY = lastUvScaleY;
            return lastLayer;
        }
        validateTextureSize(texture);
        Integer existing = layers.get(texture);
        uvScaleX = (float) texture.getWidth() / layerWidth;
        uvScaleY = (float) texture.getHeight() / layerHeight;
        if (existing != null) return rememberTexture(texture, existing);
        int layer = nextLayer;
        Texture old = occupants[layer];
        if (old != null && vertexFloatCount > 0) flush();
        copyToLayer(texture, layer);
        if (old != null) layers.remove(old);
        occupants[layer] = texture;
        layers.put(texture, layer);
        nextLayer = (layer + 1) % layerCount;
        return rememberTexture(texture, layer);
    }

    private int rememberTexture(Texture texture, int layer) {
        lastTexture = texture;
        lastLayer = layer;
        lastTextureWidth = texture.getWidth();
        lastTextureHeight = texture.getHeight();
        lastUvScaleX = uvScaleX;
        lastUvScaleY = uvScaleY;
        return layer;
    }

    private void copyToLayer(Texture texture, int layer) {
        // Outside begin/end, external GL work may have invalidated our assumptions.
        if (!drawing) arrayBindingDirty = true;
        savedFramebuffer.clear();
        Gdx.gl.glGetIntegerv(GL30.GL_READ_FRAMEBUFFER_BINDING, savedFramebuffer);
        int previous = savedFramebuffer.get(0);
        Gdx.gl.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, copyFramebuffer);
        try {
            Gdx.gl.glFramebufferTexture2D(GL30.GL_READ_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0,
                    GL20.GL_TEXTURE_2D, texture.getTextureObjectHandle(), 0);
            if (Gdx.gl.glCheckFramebufferStatus(GL30.GL_READ_FRAMEBUFFER) != GL20.GL_FRAMEBUFFER_COMPLETE)
                throw new IllegalArgumentException("Texture is not framebuffer copy compatible");
            bindTextureArray();
            Gdx.gl30.glCopyTexSubImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, 0, 0, layer,
                    0, 0, texture.getWidth(), texture.getHeight());
            // Duplicate the last texels at the padded edges, so UV == 1 cannot sample old layer data.
            if (texture.getWidth() < layerWidth)
                Gdx.gl30.glCopyTexSubImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0,
                        texture.getWidth(), 0, layer, texture.getWidth() - 1, 0,
                        1, texture.getHeight());
            if (texture.getHeight() < layerHeight)
                Gdx.gl30.glCopyTexSubImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0,
                        0, texture.getHeight(), layer, 0, texture.getHeight() - 1,
                        texture.getWidth(), 1);
            if (texture.getWidth() < layerWidth && texture.getHeight() < layerHeight)
                Gdx.gl30.glCopyTexSubImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0,
                        texture.getWidth(), texture.getHeight(), layer,
                        texture.getWidth() - 1, texture.getHeight() - 1, 1, 1);
            int error = Gdx.gl.glGetError();
            if (error != GL20.GL_NO_ERROR)
                throw new IllegalStateException("Texture copy to array failed, GL error " + error);
        } finally {
            Gdx.gl.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, previous);
            if (!drawing) arrayBindingDirty = true;
        }
    }

    private void bindTextureArray() {
        if (!arrayBindingDirty) return;
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        Gdx.gl.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, arrayHandle);
        arrayBindingDirty = false;
    }

    private void bindShaderAndUniforms() {
        ShaderProgram shader = activeShader();
        if (shaderBindingDirty) {
            shader.bind();
            shaderBindingDirty = false;
        }
        if (projectionUniformDirty) {
            shader.setUniformMatrix(projectionUniformLocation, state.combinedMatrix());
            projectionUniformDirty = false;
        }
        if (arrayUniformDirty) {
            shader.setUniformi(arrayUniformLocation, 0);
            arrayUniformDirty = false;
        }
    }

    private ShaderProgram activeShader() {
        return customShader != null ? customShader : defaultShader;
    }

    private void cacheUniformLocations(ShaderProgram shader) {
        projectionUniformLocation = shader.getUniformLocation("u_projTrans");
        arrayUniformLocation = shader.getUniformLocation("u_array");
    }

    private static void validateShader(ShaderProgram shader) {
        if (!shader.isCompiled()) {
            throw new IllegalArgumentException(
                    "Texture-array shader is not compiled: " + shader.getLog());
        }
        requireAttribute(shader, "a_position");
        requireAttribute(shader, "a_color");
        requireAttribute(shader, "a_texCoord0");
        requireAttribute(shader, "a_layer");
        requireUniform(shader, "u_projTrans");
        requireUniform(shader, "u_array");
    }

    private static void requireAttribute(ShaderProgram shader, String name) {
        if (!shader.hasAttribute(name)) {
            throw new IllegalArgumentException(
                    "Texture-array shader is missing required attribute '" + name + "'.");
        }
    }

    private static void requireUniform(ShaderProgram shader, String name) {
        if (!shader.hasUniform(name)) {
            throw new IllegalArgumentException(
                    "Texture-array shader is missing required uniform '" + name + "'.");
        }
    }

    static void validateSpriteVertices(float[] spriteVertices, int offset, int count) {
        if (spriteVertices == null) throw new IllegalArgumentException("spriteVertices is null");
        if (offset < 0 || count < 0 || offset > spriteVertices.length - count) {
            throw new IndexOutOfBoundsException(
                    "Invalid spriteVertices range: offset=" + offset + ", count=" + count
                            + ", length=" + spriteVertices.length);
        }
        if (count % 20 != 0) {
            throw new IllegalArgumentException(
                    "spriteVertices count must contain complete 20-float LibGDX quads: " + count);
        }
        for (int i = offset; i < offset + count; i += 5)
            requireUv(spriteVertices[i + 3], spriteVertices[i + 4]);
    }

    private void validateTextureSize(Texture texture) {
        if (texture.getWidth() <= 0 || texture.getHeight() <= 0
                || texture.getWidth() > layerWidth || texture.getHeight() > layerHeight)
            throw new IllegalArgumentException("Texture dimensions must fit the array layer");
    }

    private static void requireUv(float u, float v) {
        if (!(u >= 0f && u <= 1f && v >= 0f && v <= 1f))
            throw new IllegalArgumentException("UV outside [0, 1]; texture wrapping is unsupported");
    }

    static int copySpriteVertices(float[] source, int offset, int count, float[] target,
                                  int targetOffset, float layer, float scaleU, float scaleV,
                                  Affine2 adjustment) {
        int out = targetOffset;
        for (int i = offset, end = offset + count; i < end; i += 5) {
            float x = source[i], y = source[i + 1];
            if (adjustment == null) {
                target[out++] = x;
                target[out++] = y;
            } else {
                target[out++] = adjustment.m00 * x + adjustment.m01 * y + adjustment.m02;
                target[out++] = adjustment.m10 * x + adjustment.m11 * y + adjustment.m12;
            }
            target[out++] = source[i + 2];
            target[out++] = source[i + 3] * scaleU;
            target[out++] = source[i + 4] * scaleV;
            target[out++] = layer;
        }
        return out - targetOffset;
    }

    private void requireDrawing() {
        if (!drawing) throw new IllegalStateException("TextureArrayGL30Batch.begin must be called before draw.");
    }

    private static void requireRegion(TextureRegion region) {
        if (region == null) throw new IllegalArgumentException("region is null");
    }

    private static void requireGl30() {
        if (Gdx.gl30 == null) {
            throw new IllegalStateException(
                    "TextureArrayGL30Batch requires a desktop OpenGL 3.3 context with GL30 support.");
        }
    }

private static final class BatchState {
    private final Color color = new Color(Color.WHITE);
    private float packedColor = Color.WHITE_FLOAT_BITS;

    private boolean blendingDisabled;
    private int blendSrcFunc = GL20.GL_SRC_ALPHA;
    private int blendDstFunc = GL20.GL_ONE_MINUS_SRC_ALPHA;
    private int blendSrcFuncAlpha = GL20.GL_SRC_ALPHA;
    private int blendDstFuncAlpha = GL20.GL_ONE_MINUS_SRC_ALPHA;

    private final Matrix4 projectionMatrix = new Matrix4();
    private final Matrix4 realTransformMatrix = new Matrix4();
    private final Matrix4 virtualTransformMatrix = new Matrix4();
    private final Matrix4 combinedMatrix = new Matrix4();
    private final Affine2 vertexAdjustment = new Affine2();
    private final Affine2 inverseRealScratch = new Affine2();
    private final Affine2 requestedTransformScratch = new Affine2();
    private boolean vertexAdjustmentNeeded;
    private boolean realTransformIdentity = true;

    BatchState() {
        updateCombinedMatrix();
    }

    void setColor(Color tint) {
        if (tint == null) throw new IllegalArgumentException("tint is null");
        color.set(tint);
        packedColor = tint.toFloatBits();
    }

    void setColor(float r, float g, float b, float a) {
        color.set(r, g, b, a);
        packedColor = color.toFloatBits();
    }

    Color color() {
        return color;
    }

    void setPackedColor(float packedColor) {
        Color.abgr8888ToColor(color, packedColor);
        this.packedColor = packedColor;
    }

    float packedColor() {
        return packedColor;
    }

    boolean isBlendingEnabled() {
        return !blendingDisabled;
    }

    void setBlendingEnabled(boolean enabled) {
        blendingDisabled = !enabled;
    }

    boolean setBlendFunctionSeparate(int srcColor, int dstColor, int srcAlpha, int dstAlpha) {
        if (blendSrcFunc == srcColor && blendDstFunc == dstColor
                && blendSrcFuncAlpha == srcAlpha && blendDstFuncAlpha == dstAlpha) {
            return false;
        }
        blendSrcFunc = srcColor;
        blendDstFunc = dstColor;
        blendSrcFuncAlpha = srcAlpha;
        blendDstFuncAlpha = dstAlpha;
        return true;
    }

    int blendSrcFunc() {
        return blendSrcFunc;
    }

    int blendDstFunc() {
        return blendDstFunc;
    }

    int blendSrcFuncAlpha() {
        return blendSrcFuncAlpha;
    }

    int blendDstFuncAlpha() {
        return blendDstFuncAlpha;
    }

    Matrix4 projectionMatrix() {
        return projectionMatrix;
    }

    Matrix4 transformMatrix() {
        return virtualTransformMatrix;
    }

    Matrix4 combinedMatrix() {
        return combinedMatrix;
    }

    void setProjectionMatrix(Matrix4 projection) {
        if (projection == null) throw new IllegalArgumentException("projection is null");
        syncRealTransformToVirtual();
        projectionMatrix.set(projection);
        updateCombinedMatrix();
    }

    void setTransformMatrix(Matrix4 transform, boolean drawing) {
        if (transform == null) throw new IllegalArgumentException("transform is null");

        if (!drawing) {
            realTransformMatrix.setAsAffine(transform);
            virtualTransformMatrix.setAsAffine(transform);
            realTransformIdentity = affineIsIdentity(realTransformMatrix);
            vertexAdjustment.idt();
            vertexAdjustmentNeeded = false;
            updateCombinedMatrix();
            return;
        }

        if (affineEquals(virtualTransformMatrix, transform)) return;

        requestedTransformScratch.set(transform);
        if (affineEquals(realTransformMatrix, transform)) {
            virtualTransformMatrix.setAsAffine(transform);
            vertexAdjustment.idt();
            vertexAdjustmentNeeded = false;
            return;
        }

        if (realTransformIdentity) {
            vertexAdjustment.set(requestedTransformScratch);
        } else {
            inverseRealScratch.set(realTransformMatrix);
            float determinant = inverseRealScratch.m00 * inverseRealScratch.m11
                    - inverseRealScratch.m01 * inverseRealScratch.m10;
            if (determinant == 0f) {
                throw new IllegalStateException(
                        "TextureArrayGL30Batch cannot apply a virtual transform because the active real transform is singular.");
            }
            inverseRealScratch.inv().mul(requestedTransformScratch);
            vertexAdjustment.set(inverseRealScratch);
        }
        virtualTransformMatrix.setAsAffine(transform);
        vertexAdjustmentNeeded = true;
    }

    void syncRealTransformToVirtual() {
        if (!affineEquals(realTransformMatrix, virtualTransformMatrix)) {
            realTransformMatrix.setAsAffine(virtualTransformMatrix);
            realTransformIdentity = affineIsIdentity(realTransformMatrix);
        }
        vertexAdjustment.idt();
        vertexAdjustmentNeeded = false;
        updateCombinedMatrix();
    }

    boolean vertexAdjustmentNeeded() {
        return vertexAdjustmentNeeded;
    }

    Affine2 vertexAdjustment() {
        return vertexAdjustment;
    }

    private void updateCombinedMatrix() {
        combinedMatrix.set(projectionMatrix).mul(realTransformMatrix);
    }

    private static boolean affineEquals(Matrix4 left, Matrix4 right) {
        return left.val[Matrix4.M00] == right.val[Matrix4.M00]
                && left.val[Matrix4.M01] == right.val[Matrix4.M01]
                && left.val[Matrix4.M03] == right.val[Matrix4.M03]
                && left.val[Matrix4.M10] == right.val[Matrix4.M10]
                && left.val[Matrix4.M11] == right.val[Matrix4.M11]
                && left.val[Matrix4.M13] == right.val[Matrix4.M13];
    }

    private static boolean affineIsIdentity(Matrix4 matrix) {
        return matrix.val[Matrix4.M00] == 1f
                && matrix.val[Matrix4.M01] == 0f
                && matrix.val[Matrix4.M03] == 0f
                && matrix.val[Matrix4.M10] == 0f
                && matrix.val[Matrix4.M11] == 1f
                && matrix.val[Matrix4.M13] == 0f;
    }
}


}
