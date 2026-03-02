package com.github.rednblackgames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.nio.IntBuffer;
import java.util.Arrays;

/** Draws batched quads using indices.
 * <p>
 * This is an optimized version of the SpriteBatch that maintains an LFU texture-cache to combine draw calls with different
 * textures effectively.
 * <p>
 * Use this Batch if you frequently utilize more than a single texture between calling {@link #begin()} and
 * {@link #end()}. An example would be if your Atlas is spread over multiple Textures or if you draw with individual
 * Textures. This extends {@link com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch}), which makes it suitable for Spine
 * animations.
 * <p>
 * Taken from <a href="https://github.com/rednblackgames/hyperlap2d-runtime-libgdx/tree/master/src/main/java/games/rednblack/editor/renderer/utils">Hyperlap2D's GitHub repo</a>.
 * The tint field, which modified the Batch color, has been removed because it was unused in TextraTypist.
 *
 * @see Batch
 * @see SpriteBatch
 *
 * @author mzechner (Original SpriteBatch)
 * @author Nathan Sweet (Original SpriteBatch)
 * @author VaTTeRGeR (TextureArray Extension)
 * @author fgnm (PolygonBatch Extension) */

public class TextureArrayPolygonSpriteBatch extends com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch {

    public static final String TEXTURE_INDEX_ATTRIBUTE = "a_texture_index";

    static final int VERTEX_SIZE = 2 + 1 + 2 + 1; //Position + Color + Texture Coordinates + Texture Index
    static final int SPRITE_SIZE = 4 * VERTEX_SIZE; //A Sprite has 4 Vertices

    private final Mesh mesh;

    final float[] vertices;
    final short[] triangles;
    int vertexIndex, triangleIndex;
    float invTexWidth = 0, invTexHeight = 0;
    boolean drawing;

    private final Matrix4 transformMatrix = new Matrix4();
    private final Matrix4 projectionMatrix = new Matrix4();
    private final Matrix4 combinedMatrix = new Matrix4();

    private boolean blendingDisabled;
    private int blendSrcFunc = GL20.GL_SRC_ALPHA;
    private int blendDstFunc = GL20.GL_ONE_MINUS_SRC_ALPHA;
    private int blendSrcFuncAlpha = GL20.GL_SRC_ALPHA;
    private int blendDstFuncAlpha = GL20.GL_ONE_MINUS_SRC_ALPHA;

    private final ShaderProgram shader;
    private ShaderProgram customShader;
    private boolean ownsShader;

    protected float colorPacked = Color.WHITE_FLOAT_BITS;

    /** Number of render calls since the last {@link #begin()}. **/
    public int renderCalls = 0;

    /** Number of rendering calls, ever. Will not be reset unless set manually. **/
    public int totalRenderCalls = 0;

    /** The maximum number of triangles rendered in one batch so far. **/
    public int maxTrianglesInBatch = 0;

    /** The maximum number of available texture units for the fragment shader */
    private static int maxTextureUnits = -1;

    /** Textures in use (index: Texture Unit, value: Texture) */
    private final Texture[] usedTextures;

    /** LFU Array (index: Texture Unit Index - value: Access frequency) */
    private final int[] usedTexturesLFU;

    /** Gets sent to the fragment shader as a uniform {@code uniform sampler2d[X] u_textures} */
    private final IntBuffer textureUnitIndicesBuffer;

    /** The current number of textures in the LFU cache. Gets reset when calling {@link #begin()} **/
    private int currentTextureLFUSize = 0;

    /** The current number of texture swaps in the LFU cache. Gets reset when calling {@link #begin()} **/
    private int currentTextureLFUSwaps = 0;

    private static String shaderErrorLog = null;

    private boolean dirtyTextureArray = true;

    /** Constructs a TextureArrayPolygonSpriteBatch with the default shader, 2000 vertices, and 4000 triangles.
     * @see #TextureArrayPolygonSpriteBatch(int, int, ShaderProgram) */
    public TextureArrayPolygonSpriteBatch() {
        this(2000, null);
    }

    /** Constructs a TextureArrayPolygonSpriteBatch with the default shader, size vertices, and size * 2 triangles.
     * @param size The max number of vertices and number of triangles in a single batch. Max of 32767.
     * @see #TextureArrayPolygonSpriteBatch(int, int, ShaderProgram) */
    public TextureArrayPolygonSpriteBatch(int size) {
        this(size, size * 2, null);
    }

    /** Constructs a TextureArrayPolygonSpriteBatch with the specified shader, size vertices and size * 2 triangles.
     * @param size The max number of vertices and number of triangles in a single batch. Max of 32767.
     * @see #TextureArrayPolygonSpriteBatch(int, int, ShaderProgram) */
    public TextureArrayPolygonSpriteBatch(int size, ShaderProgram defaultShader) {
        this(size, size * 2, defaultShader);
    }

    /** Constructs a new PolygonSpriteBatch. Sets the projection matrix to an orthographic projection with y-axis point upwards,
     * x-axis point to the right and the origin being in the bottom left corner of the screen. The projection will be pixel perfect
     * with respect to the current screen resolution.
     * <p>
     * The defaultShader specifies the shader to use. Note that the names for uniforms for this default shader are different from
     * the ones expect for shaders set with {@link #setShader(ShaderProgram)}. See {@link SpriteBatch#createDefaultShader()}.
     * @param maxVertices The max number of vertices in a single batch. Max of 32767.
     * @param maxTriangles The max number of triangles in a single batch.
     * @param defaultShader The default shader to use. This is not owned by the PolygonSpriteBatch and must be disposed separately.
     *           May be null to use the default shader. */
    public TextureArrayPolygonSpriteBatch(int maxVertices, int maxTriangles, ShaderProgram defaultShader) {
        // 32767 is max vertex index.
        if (maxVertices > 32767)
            throw new IllegalArgumentException("Can't have more than 32767 vertices per batch: " + maxVertices);

        getMaxTextureUnits();
        if (maxTextureUnits == 0) {
            throw new IllegalStateException(
                    "Texture Arrays are not supported on this device:" + shaderErrorLog);
        }

        usedTextures = new Texture[maxTextureUnits];
        usedTexturesLFU = new int[maxTextureUnits];

        // This contains the numbers 0 ... maxTextureUnits - 1. We send these to the shader as a uniform.
        textureUnitIndicesBuffer = BufferUtils.newIntBuffer(maxTextureUnits);
        for (int i = 0; i < maxTextureUnits; i++) {
            textureUnitIndicesBuffer.put(i);
        }
        textureUnitIndicesBuffer.flip();

        Mesh.VertexDataType vertexDataType = Mesh.VertexDataType.VertexBufferObject;
        if (Gdx.gl30 != null) {
            vertexDataType = Mesh.VertexDataType.VertexBufferObjectWithVAO;
        }
        mesh = new Mesh(vertexDataType, false, maxVertices, maxTriangles * 3,
                new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"),
                new VertexAttribute(VertexAttributes.Usage.Generic, 1, TEXTURE_INDEX_ATTRIBUTE));

        vertices = new float[maxVertices * VERTEX_SIZE];
        triangles = new short[maxTriangles * 3];

        if (defaultShader == null) {
            shader = createDefaultShader();
            ownsShader = true;
        } else
            shader = defaultShader;

        projectionMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void draw (PolygonRegion region, float x, float y) {
        if (!drawing) throw new IllegalStateException("TextureArrayPolygonSpriteBatch.begin must be called before draw.");

        final short[] triangles = this.triangles;
        final short[] regionTriangles = region.getTriangles();
        final int regionTrianglesLength = regionTriangles.length;
        final float[] regionVertices = region.getVertices();
        final int regionVerticesLength = regionVertices.length;

        final Texture texture = region.getRegion().getTexture();
        if (triangleIndex + regionTrianglesLength > triangles.length
                || vertexIndex + regionVerticesLength * VERTEX_SIZE / 2 > vertices.length) flush();

        final float textureIndex = activateTexture(texture);

        int triangleIndex = this.triangleIndex;
        int vertexIndex = this.vertexIndex;
        final int startVertex = vertexIndex / VERTEX_SIZE;

        for (int i = 0; i < regionTrianglesLength; i++)
            triangles[triangleIndex++] = (short)(regionTriangles[i] + startVertex);
        this.triangleIndex = triangleIndex;

        final float[] vertices = this.vertices;
        final float color = this.colorPacked;
        final float[] textureCoords = region.getTextureCoords();

        for (int i = 0; i < regionVerticesLength; i += 2) {
            vertices[vertexIndex++] = regionVertices[i] + x;
            vertices[vertexIndex++] = regionVertices[i + 1] + y;
            vertices[vertexIndex++] = color;
            vertices[vertexIndex++] = textureCoords[i];
            vertices[vertexIndex++] = textureCoords[i + 1];
            vertices[vertexIndex++] = textureIndex;
        }
        this.vertexIndex = vertexIndex;
    }

    @Override
    public void draw (PolygonRegion region, float x, float y, float width, float height) {
        if (!drawing) throw new IllegalStateException("TextureArrayPolygonSpriteBatch.begin must be called before draw.");

        final short[] triangles = this.triangles;
        final short[] regionTriangles = region.getTriangles();
        final int regionTrianglesLength = regionTriangles.length;
        final float[] regionVertices = region.getVertices();
        final int regionVerticesLength = regionVertices.length;
        final TextureRegion textureRegion = region.getRegion();

        final Texture texture = textureRegion.getTexture();
        if (triangleIndex + regionTrianglesLength > triangles.length
                || vertexIndex + regionVerticesLength * VERTEX_SIZE / 2 > vertices.length) flush();

        final float textureIndex = activateTexture(texture);

        int triangleIndex = this.triangleIndex;
        int vertexIndex = this.vertexIndex;
        final int startVertex = vertexIndex / VERTEX_SIZE;

        for (int i = 0, n = regionTriangles.length; i < n; i++)
            triangles[triangleIndex++] = (short)(regionTriangles[i] + startVertex);
        this.triangleIndex = triangleIndex;

        final float[] vertices = this.vertices;
        final float color = this.colorPacked;
        final float[] textureCoords = region.getTextureCoords();
        final float sX = width / textureRegion.getRegionWidth();
        final float sY = height / textureRegion.getRegionHeight();

        for (int i = 0; i < regionVerticesLength; i += 2) {
            vertices[vertexIndex++] = regionVertices[i] * sX + x;
            vertices[vertexIndex++] = regionVertices[i + 1] * sY + y;
            vertices[vertexIndex++] = color;
            vertices[vertexIndex++] = textureCoords[i];
            vertices[vertexIndex++] = textureCoords[i + 1];
            vertices[vertexIndex++] = textureIndex;
        }
        this.vertexIndex = vertexIndex;
    }

    @Override
    public void draw (PolygonRegion region, float x, float y, float originX, float originY, float width, float height,
                      float scaleX, float scaleY, float rotation) {
        if (!drawing) throw new IllegalStateException("TextureArrayPolygonSpriteBatch.begin must be called before draw.");

        final short[] triangles = this.triangles;
        final short[] regionTriangles = region.getTriangles();
        final int regionTrianglesLength = regionTriangles.length;
        final float[] regionVertices = region.getVertices();
        final int regionVerticesLength = regionVertices.length;
        final TextureRegion textureRegion = region.getRegion();

        Texture texture = textureRegion.getTexture();
        if (triangleIndex + regionTrianglesLength > triangles.length
                || vertexIndex + regionVerticesLength * VERTEX_SIZE / 2 > vertices.length) flush();

        final float textureIndex = activateTexture(texture);

        int triangleIndex = this.triangleIndex;
        int vertexIndex = this.vertexIndex;
        final int startVertex = vertexIndex / VERTEX_SIZE;

        for (int i = 0; i < regionTrianglesLength; i++)
            triangles[triangleIndex++] = (short)(regionTriangles[i] + startVertex);
        this.triangleIndex = triangleIndex;

        final float[] vertices = this.vertices;
        final float color = this.colorPacked;
        final float[] textureCoords = region.getTextureCoords();

        final float worldOriginX = x + originX;
        final float worldOriginY = y + originY;
        final float sX = width / textureRegion.getRegionWidth();
        final float sY = height / textureRegion.getRegionHeight();
        final float cos = MathUtils.cosDeg(rotation);
        final float sin = MathUtils.sinDeg(rotation);

        float fx, fy;
        for (int i = 0; i < regionVerticesLength; i += 2) {
            fx = (regionVertices[i] * sX - originX) * scaleX;
            fy = (regionVertices[i + 1] * sY - originY) * scaleY;
            vertices[vertexIndex++] = cos * fx - sin * fy + worldOriginX;
            vertices[vertexIndex++] = sin * fx + cos * fy + worldOriginY;
            vertices[vertexIndex++] = color;
            vertices[vertexIndex++] = textureCoords[i];
            vertices[vertexIndex++] = textureCoords[i + 1];
            vertices[vertexIndex++] = textureIndex;
        }
        this.vertexIndex = vertexIndex;
    }

    @Override
    public void draw (Texture texture, float[] polygonVertices, int verticesOffset, int verticesCount, short[] polygonTriangles,
                      int trianglesOffset, int trianglesCount) {
        if (!drawing) throw new IllegalStateException("TextureArrayPolygonSpriteBatch.begin must be called before draw.");

        final short[] triangles = this.triangles;
        final float[] vertices = this.vertices;

        //Calculate how many vertices will be put in the batch
        int vCount = (verticesCount / 5) * 6;

        if (triangleIndex + trianglesCount > triangles.length || vertexIndex + vCount > vertices.length) //
            flush();

        final float textureIndex = activateTexture(texture);

        int triangleIndex = this.triangleIndex;
        final int vertexIndex = this.vertexIndex;
        final int startVertex = vertexIndex / VERTEX_SIZE;

        for (int i = trianglesOffset, n = i + trianglesCount; i < n; i++)
            triangles[triangleIndex++] = (short)(polygonTriangles[i] + startVertex);
        this.triangleIndex = triangleIndex;

        int vIn = vertexIndex;
        for (int offsetIn = verticesOffset; offsetIn < verticesCount + verticesOffset; offsetIn += 5, vIn += VERTEX_SIZE) {
            vertices[vIn] = polygonVertices[offsetIn]; // x
            vertices[vIn + 1] = polygonVertices[offsetIn + 1]; // y
            vertices[vIn + 2] = polygonVertices[offsetIn + 2]; // color
            vertices[vIn + 3] = polygonVertices[offsetIn + 3]; // u
            vertices[vIn + 4] = polygonVertices[offsetIn + 4]; // v
            vertices[vIn + 5] = textureIndex; // texture
        }

        this.vertexIndex += vCount;
    }

    @Override
    public void draw (Texture texture, float x, float y, float originX, float originY, float width, float height, float scaleX,
                      float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
        if (!drawing) throw new IllegalStateException("TextureArrayPolygonSpriteBatch.begin must be called before draw.");

        final short[] triangles = this.triangles;
        final float[] vertices = this.vertices;

        if (triangleIndex + 6 > triangles.length || vertexIndex + SPRITE_SIZE > vertices.length) //
            flush();

        final float textureIndex = activateTexture(texture);

        int triangleIndex = this.triangleIndex;
        final int startVertex = vertexIndex / VERTEX_SIZE;
        triangles[triangleIndex++] = (short)startVertex;
        triangles[triangleIndex++] = (short)(startVertex + 1);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 3);
        triangles[triangleIndex++] = (short)startVertex;
        this.triangleIndex = triangleIndex;

        // bottom left and top right corner points relative to origin
        final float worldOriginX = x + originX;
        final float worldOriginY = y + originY;
        float fx = -originX;
        float fy = -originY;
        float fx2 = width - originX;
        float fy2 = height - originY;

        // scale
        if (scaleX != 1 || scaleY != 1) {
            fx *= scaleX;
            fy *= scaleY;
            fx2 *= scaleX;
            fy2 *= scaleY;
        }

        // construct corner points, start from top left and go counterclockwise
        final float p1x = fx;
        final float p1y = fy;
        final float p2x = fx;
        final float p2y = fy2;
        final float p3x = fx2;
        final float p3y = fy2;
        final float p4x = fx2;
        final float p4y = fy;

        float x1;
        float y1;
        float x2;
        float y2;
        float x3;
        float y3;
        float x4;
        float y4;

        // rotate
        if (rotation != 0) {
            final float cos = MathUtils.cosDeg(rotation);
            final float sin = MathUtils.sinDeg(rotation);

            x1 = cos * p1x - sin * p1y;
            y1 = sin * p1x + cos * p1y;

            x2 = cos * p2x - sin * p2y;
            y2 = sin * p2x + cos * p2y;

            x3 = cos * p3x - sin * p3y;
            y3 = sin * p3x + cos * p3y;

            x4 = x1 + (x3 - x2);
            y4 = y3 - (y2 - y1);
        } else {
            x1 = p1x;
            y1 = p1y;

            x2 = p2x;
            y2 = p2y;

            x3 = p3x;
            y3 = p3y;

            x4 = p4x;
            y4 = p4y;
        }

        x1 += worldOriginX;
        y1 += worldOriginY;
        x2 += worldOriginX;
        y2 += worldOriginY;
        x3 += worldOriginX;
        y3 += worldOriginY;
        x4 += worldOriginX;
        y4 += worldOriginY;

        float u = srcX * invTexWidth;
        float v = (srcY + srcHeight) * invTexHeight;
        float u2 = (srcX + srcWidth) * invTexWidth;
        float v2 = srcY * invTexHeight;

        if (flipX) {
            float tmp = u;
            u = u2;
            u2 = tmp;
        }

        if (flipY) {
            float tmp = v;
            v = v2;
            v2 = tmp;
        }

        float color = this.colorPacked;
        int idx = this.vertexIndex;
        vertices[idx++] = x1;
        vertices[idx++] = y1;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v;
        vertices[idx++] = textureIndex;

        vertices[idx++] = x2;
        vertices[idx++] = y2;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v2;
        vertices[idx++] = textureIndex;

        vertices[idx++] = x3;
        vertices[idx++] = y3;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v2;
        vertices[idx++] = textureIndex;

        vertices[idx++] = x4;
        vertices[idx++] = y4;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v;
        vertices[idx++] = textureIndex;
        this.vertexIndex = idx;
    }

    @Override
    public void draw (Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth,
                      int srcHeight, boolean flipX, boolean flipY) {
        if (!drawing) throw new IllegalStateException("TextureArrayPolygonSpriteBatch.begin must be called before draw.");

        final short[] triangles = this.triangles;
        final float[] vertices = this.vertices;

        if (triangleIndex + 6 > triangles.length || vertexIndex + SPRITE_SIZE > vertices.length) //
            flush();

        final float textureIndex = activateTexture(texture);

        int triangleIndex = this.triangleIndex;
        final int startVertex = vertexIndex / VERTEX_SIZE;
        triangles[triangleIndex++] = (short)startVertex;
        triangles[triangleIndex++] = (short)(startVertex + 1);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 3);
        triangles[triangleIndex++] = (short)startVertex;
        this.triangleIndex = triangleIndex;

        float u = srcX * invTexWidth;
        float v = (srcY + srcHeight) * invTexHeight;
        float u2 = (srcX + srcWidth) * invTexWidth;
        float v2 = srcY * invTexHeight;
        final float fx2 = x + width;
        final float fy2 = y + height;

        if (flipX) {
            float tmp = u;
            u = u2;
            u2 = tmp;
        }

        if (flipY) {
            float tmp = v;
            v = v2;
            v2 = tmp;
        }

        float color = this.colorPacked;
        int idx = this.vertexIndex;
        vertices[idx++] = x;
        vertices[idx++] = y;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v;
        vertices[idx++] = textureIndex;

        vertices[idx++] = x;
        vertices[idx++] = fy2;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v2;
        vertices[idx++] = textureIndex;

        vertices[idx++] = fx2;
        vertices[idx++] = fy2;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v2;
        vertices[idx++] = textureIndex;

        vertices[idx++] = fx2;
        vertices[idx++] = y;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v;
        vertices[idx++] = textureIndex;
        this.vertexIndex = idx;
    }

    @Override
    public void draw (Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {
        if (!drawing) throw new IllegalStateException("TextureArrayPolygonSpriteBatch.begin must be called before draw.");

        final short[] triangles = this.triangles;
        final float[] vertices = this.vertices;

        if (triangleIndex + 6 > triangles.length || vertexIndex + SPRITE_SIZE > vertices.length) //
            flush();

        final float textureIndex = activateTexture(texture);

        int triangleIndex = this.triangleIndex;
        final int startVertex = vertexIndex / VERTEX_SIZE;
        triangles[triangleIndex++] = (short)startVertex;
        triangles[triangleIndex++] = (short)(startVertex + 1);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 3);
        triangles[triangleIndex++] = (short)startVertex;
        this.triangleIndex = triangleIndex;

        final float u = srcX * invTexWidth;
        final float v = (srcY + srcHeight) * invTexHeight;
        final float u2 = (srcX + srcWidth) * invTexWidth;
        final float v2 = srcY * invTexHeight;
        final float fx2 = x + srcWidth;
        final float fy2 = y + srcHeight;

        float color = this.colorPacked;
        int idx = this.vertexIndex;
        vertices[idx++] = x;
        vertices[idx++] = y;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v;
        vertices[idx++] = textureIndex;

        vertices[idx++] = x;
        vertices[idx++] = fy2;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v2;
        vertices[idx++] = textureIndex;

        vertices[idx++] = fx2;
        vertices[idx++] = fy2;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v2;
        vertices[idx++] = textureIndex;

        vertices[idx++] = fx2;
        vertices[idx++] = y;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v;
        vertices[idx++] = textureIndex;
        this.vertexIndex = idx;
    }

    @Override
    public void draw (Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2) {
        if (!drawing) throw new IllegalStateException("TextureArrayPolygonSpriteBatch.begin must be called before draw.");

        final short[] triangles = this.triangles;
        final float[] vertices = this.vertices;

        if (triangleIndex + 6 > triangles.length || vertexIndex + SPRITE_SIZE > vertices.length) //
            flush();

        final float textureIndex = activateTexture(texture);

        int triangleIndex = this.triangleIndex;
        final int startVertex = vertexIndex / VERTEX_SIZE;
        triangles[triangleIndex++] = (short)startVertex;
        triangles[triangleIndex++] = (short)(startVertex + 1);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 3);
        triangles[triangleIndex++] = (short)startVertex;
        this.triangleIndex = triangleIndex;

        final float fx2 = x + width;
        final float fy2 = y + height;

        float color = this.colorPacked;
        int idx = this.vertexIndex;
        vertices[idx++] = x;
        vertices[idx++] = y;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v;
        vertices[idx++] = textureIndex;

        vertices[idx++] = x;
        vertices[idx++] = fy2;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v2;
        vertices[idx++] = textureIndex;

        vertices[idx++] = fx2;
        vertices[idx++] = fy2;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v2;
        vertices[idx++] = textureIndex;

        vertices[idx++] = fx2;
        vertices[idx++] = y;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v;
        vertices[idx++] = textureIndex;
        this.vertexIndex = idx;
    }

    @Override
    public void draw (Texture texture, float x, float y) {
        draw(texture, x, y, texture.getWidth(), texture.getHeight());
    }

    @Override
    public void draw (Texture texture, float x, float y, float width, float height) {
        if (!drawing) throw new IllegalStateException("TextureArrayPolygonSpriteBatch.begin must be called before draw.");

        final short[] triangles = this.triangles;
        final float[] vertices = this.vertices;

        if (triangleIndex + 6 > triangles.length || vertexIndex + SPRITE_SIZE > vertices.length) //
            flush();

        final float textureIndex = activateTexture(texture);

        int triangleIndex = this.triangleIndex;
        final int startVertex = vertexIndex / VERTEX_SIZE;
        triangles[triangleIndex++] = (short)startVertex;
        triangles[triangleIndex++] = (short)(startVertex + 1);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 3);
        triangles[triangleIndex++] = (short)startVertex;
        this.triangleIndex = triangleIndex;

        final float fx2 = x + width;
        final float fy2 = y + height;
        final float u = 0;
        final float v = 1;
        final float u2 = 1;
        final float v2 = 0;

        float color = this.colorPacked;
        int idx = this.vertexIndex;
        vertices[idx++] = x;
        vertices[idx++] = y;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v;
        vertices[idx++] = textureIndex;

        vertices[idx++] = x;
        vertices[idx++] = fy2;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v2;
        vertices[idx++] = textureIndex;

        vertices[idx++] = fx2;
        vertices[idx++] = fy2;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v2;
        vertices[idx++] = textureIndex;

        vertices[idx++] = fx2;
        vertices[idx++] = y;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v;
        vertices[idx++] = textureIndex;
        this.vertexIndex = idx;
    }

    @Override
    public void draw (Texture texture, float[] spriteVertices, int offset, int count) {
        if (!drawing) throw new IllegalStateException("TextureArrayPolygonSpriteBatch.begin must be called before draw.");

        final short[] triangles = this.triangles;
        final float[] vertices = this.vertices;

        //Calculate how many vertices and triangles will be put in the batch
        int triangleCount = (count / 20) * 6;
        float verticesCount = ((float) (count / 5)) * 6;
        if (this.triangleIndex + triangleCount > triangles.length || this.vertexIndex + verticesCount > vertices.length)
            flush();

        float textureIndex = activateTexture(texture);

        final int vertexIndex = this.vertexIndex;
        int triangleIndex = this.triangleIndex;
        short vertex = (short)(vertexIndex / VERTEX_SIZE);
        for (int n = triangleIndex + triangleCount; triangleIndex < n; triangleIndex += 6, vertex += 4) {
            triangles[triangleIndex] = vertex;
            triangles[triangleIndex + 1] = (short)(vertex + 1);
            triangles[triangleIndex + 2] = (short)(vertex + 2);
            triangles[triangleIndex + 3] = (short)(vertex + 2);
            triangles[triangleIndex + 4] = (short)(vertex + 3);
            triangles[triangleIndex + 5] = vertex;
        }
        this.triangleIndex = triangleIndex;

        int vIn = vertexIndex;
        for (int offsetIn = offset; offsetIn < count + offset; offsetIn += 5, vIn += VERTEX_SIZE) {
            vertices[vIn] = spriteVertices[offsetIn]; // x
            vertices[vIn + 1] = spriteVertices[offsetIn + 1]; // y
            vertices[vIn + 2] = spriteVertices[offsetIn + 2]; // color
            vertices[vIn + 3] = spriteVertices[offsetIn + 3]; // u
            vertices[vIn + 4] = spriteVertices[offsetIn + 4]; // v
            vertices[vIn + 5] = textureIndex; // texture index
        }
        this.vertexIndex = (int) (vertexIndex + verticesCount);
    }

    @Override
    public void draw (TextureRegion region, float x, float y) {
        draw(region, x, y, region.getRegionWidth(), region.getRegionHeight());
    }

    @Override
    public void draw (TextureRegion region, float x, float y, float width, float height) {
        if (!drawing) throw new IllegalStateException("TextureArrayPolygonSpriteBatch.begin must be called before draw.");

        final short[] triangles = this.triangles;
        final float[] vertices = this.vertices;

        Texture texture = region.getTexture();
        if (triangleIndex + 6 > triangles.length || vertexIndex + SPRITE_SIZE > vertices.length) //
            flush();

        final float textureIndex = activateTexture(texture);

        int triangleIndex = this.triangleIndex;
        final int startVertex = vertexIndex / VERTEX_SIZE;
        triangles[triangleIndex++] = (short)startVertex;
        triangles[triangleIndex++] = (short)(startVertex + 1);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 3);
        triangles[triangleIndex++] = (short)startVertex;
        this.triangleIndex = triangleIndex;

        final float fx2 = x + width;
        final float fy2 = y + height;
        final float u = region.getU();
        final float v = region.getV2();
        final float u2 = region.getU2();
        final float v2 = region.getV();

        float color = this.colorPacked;
        int idx = this.vertexIndex;
        vertices[idx++] = x;
        vertices[idx++] = y;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v;
        vertices[idx++] = textureIndex;

        vertices[idx++] = x;
        vertices[idx++] = fy2;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v2;
        vertices[idx++] = textureIndex;

        vertices[idx++] = fx2;
        vertices[idx++] = fy2;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v2;
        vertices[idx++] = textureIndex;

        vertices[idx++] = fx2;
        vertices[idx++] = y;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v;
        vertices[idx++] = textureIndex;
        this.vertexIndex = idx;
    }

    @Override
    public void draw (TextureRegion region, float x, float y, float originX, float originY, float width, float height,
                      float scaleX, float scaleY, float rotation) {
        if (!drawing) throw new IllegalStateException("TextureArrayPolygonSpriteBatch.begin must be called before draw.");

        final short[] triangles = this.triangles;
        final float[] vertices = this.vertices;

        Texture texture = region.getTexture();
        if (triangleIndex + 6 > triangles.length || vertexIndex + SPRITE_SIZE > vertices.length) //
            flush();

        final float textureIndex = activateTexture(texture);

        int triangleIndex = this.triangleIndex;
        final int startVertex = vertexIndex / VERTEX_SIZE;
        triangles[triangleIndex++] = (short)startVertex;
        triangles[triangleIndex++] = (short)(startVertex + 1);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 3);
        triangles[triangleIndex++] = (short)startVertex;
        this.triangleIndex = triangleIndex;

        // bottom left and top right corner points relative to origin
        final float worldOriginX = x + originX;
        final float worldOriginY = y + originY;
        float fx = -originX;
        float fy = -originY;
        float fx2 = width - originX;
        float fy2 = height - originY;

        // scale
        if (scaleX != 1 || scaleY != 1) {
            fx *= scaleX;
            fy *= scaleY;
            fx2 *= scaleX;
            fy2 *= scaleY;
        }

        // construct corner points, start from top left and go counterclockwise
        final float p1x = fx;
        final float p1y = fy;
        final float p2x = fx;
        final float p2y = fy2;
        final float p3x = fx2;
        final float p3y = fy2;
        final float p4x = fx2;
        final float p4y = fy;

        float x1;
        float y1;
        float x2;
        float y2;
        float x3;
        float y3;
        float x4;
        float y4;

        // rotate
        if (rotation != 0) {
            final float cos = MathUtils.cosDeg(rotation);
            final float sin = MathUtils.sinDeg(rotation);

            x1 = cos * p1x - sin * p1y;
            y1 = sin * p1x + cos * p1y;

            x2 = cos * p2x - sin * p2y;
            y2 = sin * p2x + cos * p2y;

            x3 = cos * p3x - sin * p3y;
            y3 = sin * p3x + cos * p3y;

            x4 = x1 + (x3 - x2);
            y4 = y3 - (y2 - y1);
        } else {
            x1 = p1x;
            y1 = p1y;

            x2 = p2x;
            y2 = p2y;

            x3 = p3x;
            y3 = p3y;

            x4 = p4x;
            y4 = p4y;
        }

        x1 += worldOriginX;
        y1 += worldOriginY;
        x2 += worldOriginX;
        y2 += worldOriginY;
        x3 += worldOriginX;
        y3 += worldOriginY;
        x4 += worldOriginX;
        y4 += worldOriginY;

        final float u = region.getU();
        final float v = region.getV2();
        final float u2 = region.getU2();
        final float v2 = region.getV();

        float color = this.colorPacked;
        int idx = this.vertexIndex;
        vertices[idx++] = x1;
        vertices[idx++] = y1;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v;
        vertices[idx++] = textureIndex;

        vertices[idx++] = x2;
        vertices[idx++] = y2;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v2;
        vertices[idx++] = textureIndex;

        vertices[idx++] = x3;
        vertices[idx++] = y3;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v2;
        vertices[idx++] = textureIndex;

        vertices[idx++] = x4;
        vertices[idx++] = y4;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v;
        vertices[idx++] = textureIndex;
        this.vertexIndex = idx;
    }

    @Override
    public void draw (TextureRegion region, float x, float y, float originX, float originY, float width, float height,
                      float scaleX, float scaleY, float rotation, boolean clockwise) {
        if (!drawing) throw new IllegalStateException("TextureArrayPolygonSpriteBatch.begin must be called before draw.");

        final short[] triangles = this.triangles;
        final float[] vertices = this.vertices;

        Texture texture = region.getTexture();
        if (triangleIndex + 6 > triangles.length || vertexIndex + SPRITE_SIZE > vertices.length) //
            flush();
        final float textureIndex = activateTexture(texture);

        int triangleIndex = this.triangleIndex;
        final int startVertex = vertexIndex / VERTEX_SIZE;
        triangles[triangleIndex++] = (short)startVertex;
        triangles[triangleIndex++] = (short)(startVertex + 1);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 3);
        triangles[triangleIndex++] = (short)startVertex;
        this.triangleIndex = triangleIndex;

        // bottom left and top right corner points relative to origin
        final float worldOriginX = x + originX;
        final float worldOriginY = y + originY;
        float fx = -originX;
        float fy = -originY;
        float fx2 = width - originX;
        float fy2 = height - originY;

        // scale
        if (scaleX != 1 || scaleY != 1) {
            fx *= scaleX;
            fy *= scaleY;
            fx2 *= scaleX;
            fy2 *= scaleY;
        }

        // construct corner points, start from top left and go counterclockwise
        final float p1x = fx;
        final float p1y = fy;
        final float p2x = fx;
        final float p2y = fy2;
        final float p3x = fx2;
        final float p3y = fy2;
        final float p4x = fx2;
        final float p4y = fy;

        float x1;
        float y1;
        float x2;
        float y2;
        float x3;
        float y3;
        float x4;
        float y4;

        // rotate
        if (rotation != 0) {
            final float cos = MathUtils.cosDeg(rotation);
            final float sin = MathUtils.sinDeg(rotation);

            x1 = cos * p1x - sin * p1y;
            y1 = sin * p1x + cos * p1y;

            x2 = cos * p2x - sin * p2y;
            y2 = sin * p2x + cos * p2y;

            x3 = cos * p3x - sin * p3y;
            y3 = sin * p3x + cos * p3y;

            x4 = x1 + (x3 - x2);
            y4 = y3 - (y2 - y1);
        } else {
            x1 = p1x;
            y1 = p1y;

            x2 = p2x;
            y2 = p2y;

            x3 = p3x;
            y3 = p3y;

            x4 = p4x;
            y4 = p4y;
        }

        x1 += worldOriginX;
        y1 += worldOriginY;
        x2 += worldOriginX;
        y2 += worldOriginY;
        x3 += worldOriginX;
        y3 += worldOriginY;
        x4 += worldOriginX;
        y4 += worldOriginY;

        float u1, v1, u2, v2, u3, v3, u4, v4;
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

        float color = this.colorPacked;
        int idx = this.vertexIndex;
        vertices[idx++] = x1;
        vertices[idx++] = y1;
        vertices[idx++] = color;
        vertices[idx++] = u1;
        vertices[idx++] = v1;
        vertices[idx++] = textureIndex;

        vertices[idx++] = x2;
        vertices[idx++] = y2;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v2;
        vertices[idx++] = textureIndex;

        vertices[idx++] = x3;
        vertices[idx++] = y3;
        vertices[idx++] = color;
        vertices[idx++] = u3;
        vertices[idx++] = v3;
        vertices[idx++] = textureIndex;

        vertices[idx++] = x4;
        vertices[idx++] = y4;
        vertices[idx++] = color;
        vertices[idx++] = u4;
        vertices[idx++] = v4;
        vertices[idx++] = textureIndex;
        this.vertexIndex = idx;
    }

    @Override
    public void draw (TextureRegion region, float width, float height, Affine2 transform) {
        if (!drawing) throw new IllegalStateException("TextureArrayPolygonSpriteBatch.begin must be called before draw.");

        final short[] triangles = this.triangles;
        final float[] vertices = this.vertices;

        Texture texture = region.getTexture();
        if (triangleIndex + 6 > triangles.length || vertexIndex + SPRITE_SIZE > vertices.length) //
            flush();

        final float textureIndex = activateTexture(texture);

        int triangleIndex = this.triangleIndex;
        final int startVertex = vertexIndex / VERTEX_SIZE;
        triangles[triangleIndex++] = (short)startVertex;
        triangles[triangleIndex++] = (short)(startVertex + 1);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 3);
        triangles[triangleIndex++] = (short)startVertex;
        this.triangleIndex = triangleIndex;

        // construct corner points
        float x1 = transform.m02;
        float y1 = transform.m12;
        float x2 = transform.m01 * height + transform.m02;
        float y2 = transform.m11 * height + transform.m12;
        float x3 = transform.m00 * width + transform.m01 * height + transform.m02;
        float y3 = transform.m10 * width + transform.m11 * height + transform.m12;
        float x4 = transform.m00 * width + transform.m02;
        float y4 = transform.m10 * width + transform.m12;

        float u = region.getU();
        float v = region.getV2();
        float u2 = region.getU2();
        float v2 = region.getV();

        float color = this.colorPacked;
        int idx = vertexIndex;
        vertices[idx++] = x1;
        vertices[idx++] = y1;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v;
        vertices[idx++] = textureIndex;

        vertices[idx++] = x2;
        vertices[idx++] = y2;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v2;
        vertices[idx++] = textureIndex;

        vertices[idx++] = x3;
        vertices[idx++] = y3;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v2;
        vertices[idx++] = textureIndex;

        vertices[idx++] = x4;
        vertices[idx++] = y4;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v;
        vertices[idx++] = textureIndex;
        vertexIndex = idx;
    }

    @Override
    public void begin () {
        if (drawing) throw new IllegalStateException("TextureArrayPolygonSpriteBatch.end must be called before begin.");
        renderCalls = 0;

        Gdx.gl.glDepthMask(false);
        if (customShader != null)
            customShader.bind();
        else
            shader.bind();

        setupMatrices();

        drawing = true;
    }

    @Override
    public void end () {
        if (!drawing) throw new IllegalStateException("TextureArrayPolygonSpriteBatch.begin must be called before end.");
        if (vertexIndex > 0) flush();
        drawing = false;

        GL20 gl = Gdx.gl;
        gl.glDepthMask(true);
        if (isBlendingEnabled()) gl.glDisable(GL20.GL_BLEND);

        currentTextureLFUSize = 0;
        currentTextureLFUSwaps = 0;

        Arrays.fill(usedTextures, null);
        Arrays.fill(usedTexturesLFU, 0);
    }

    @Override
    public float getPackedColor () {
        return colorPacked;
    }

    /** @return The number of texture swaps the LFU cache performed since calling {@link #begin()}. */
    public int getTextureLFUSwaps () {
        return currentTextureLFUSwaps;
    }

    /** @return The current number of textures in the LFU cache. Gets reset when calling {@link #begin()}. */
    public int getTextureLFUSize () {
        return currentTextureLFUSize;
    }

    /** @return The maximum number of textures that the LFU cache can hold. This limit is imposed by the driver. */
    public int getTextureLFUCapacity () {
        return getMaxTextureUnits();
    }

    @Override
    public void disableBlending () {
        flush();
        blendingDisabled = true;
    }

    @Override
    public void enableBlending () {
        flush();
        blendingDisabled = false;
    }

    @Override
    public void setBlendFunction (int srcFunc, int dstFunc) {
        setBlendFunctionSeparate(srcFunc, dstFunc, srcFunc, dstFunc);
    }

    @Override
    public void setBlendFunctionSeparate (int srcFuncColor, int dstFuncColor, int srcFuncAlpha, int dstFuncAlpha) {
        if (blendSrcFunc == srcFuncColor && blendDstFunc == dstFuncColor && blendSrcFuncAlpha == srcFuncAlpha
                && blendDstFuncAlpha == dstFuncAlpha) return;
        flush();
        blendSrcFunc = srcFuncColor;
        blendDstFunc = dstFuncColor;
        blendSrcFuncAlpha = srcFuncAlpha;
        blendDstFuncAlpha = dstFuncAlpha;
    }

    @Override
    public int getBlendSrcFunc () {
        return blendSrcFunc;
    }

    @Override
    public int getBlendDstFunc () {
        return blendDstFunc;
    }

    @Override
    public int getBlendSrcFuncAlpha () {
        return blendSrcFuncAlpha;
    }

    @Override
    public int getBlendDstFuncAlpha () {
        return blendDstFuncAlpha;
    }

    @Override
    public void dispose () {
        mesh.dispose();
        if (ownsShader && shader != null) shader.dispose();
    }

    @Override
    public Matrix4 getProjectionMatrix () {
        return projectionMatrix;
    }

    @Override
    public Matrix4 getTransformMatrix () {
        return transformMatrix;
    }

    @Override
    public void setProjectionMatrix (Matrix4 projection) {
        if (drawing) flush();
        projectionMatrix.set(projection);
        if (drawing) setupMatrices();
    }

    @Override
    public void setTransformMatrix (Matrix4 transform) {
        if (drawing) flush();
        transformMatrix.set(transform);
        if (drawing) setupMatrices();
    }

    @Override
    public void flush () {
        if (vertexIndex == 0) return;

        renderCalls++;
        totalRenderCalls++;
        int trianglesInBatch = triangleIndex;
        if (trianglesInBatch > maxTrianglesInBatch) maxTrianglesInBatch = trianglesInBatch;

        if (dirtyTextureArray) {
            // Bind the textures
            for (int i = 0; i < currentTextureLFUSize; i++) {
                usedTextures[i].bind(i);
            }

            dirtyTextureArray = false;
        }

        // Set TEXTURE0 as active again before drawing.
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        Mesh mesh = this.mesh;
        mesh.setVertices(vertices, 0, vertexIndex);
        mesh.setIndices(triangles, 0, triangleIndex);
        if (blendingDisabled) {
            Gdx.gl.glDisable(GL20.GL_BLEND);
        } else {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            if (blendSrcFunc != -1) Gdx.gl.glBlendFuncSeparate(blendSrcFunc, blendDstFunc, blendSrcFuncAlpha, blendDstFuncAlpha);
        }

        mesh.render(customShader != null ? customShader : shader, GL20.GL_TRIANGLES, 0, triangleIndex);

        vertexIndex = 0;
        triangleIndex = 0;
    }

    protected void setupMatrices () {
        combinedMatrix.set(projectionMatrix).mul(transformMatrix);
        if (customShader != null) {
            customShader.setUniformMatrix("u_projTrans", combinedMatrix);
            Gdx.gl20.glUniform1iv(customShader.fetchUniformLocation("u_textures", false), maxTextureUnits, textureUnitIndicesBuffer);
        } else {
            shader.setUniformMatrix("u_projTrans", combinedMatrix);
            Gdx.gl20.glUniform1iv(shader.fetchUniformLocation("u_textures", false), maxTextureUnits, textureUnitIndicesBuffer);
        }
    }

    /** Assigns Texture units and manages the LFU cache.
     * @param texture The texture that shall be loaded into the cache, if it is not already loaded.
     * @return The texture slot that has been allocated to the selected texture */
    protected int activateTexture (Texture texture) {
        invTexWidth = 1.0f / texture.getWidth();
        invTexHeight = 1.0f / texture.getHeight();

        // This is our identifier for the textures
        final int textureHandle = texture.getTextureObjectHandle();

        // First try to see if the texture is already cached
        for (int i = 0; i < currentTextureLFUSize; i++) {
            // getTextureObjectHandle() just returns an int,
            // it's fine to call this method instead of caching the value.
            if (textureHandle == usedTextures[i].getTextureObjectHandle()) {
                // Increase the access counter.
                usedTexturesLFU[i]++;
                return i;
            }
        }

        // If a free texture unit is available we just use it
        // If not we have to flush and then throw out the least accessed one.
        if (currentTextureLFUSize < maxTextureUnits) {
            // Put the texture into the next free slot
            usedTextures[currentTextureLFUSize] = texture;

            dirtyTextureArray = true;

            // Increase the access counter.
            usedTexturesLFU[currentTextureLFUSize]++;
            return currentTextureLFUSize++;
        } else {
            // We have to flush if there is something in the pipeline already,
            // otherwise the texture index of previously rendered sprites gets invalidated
            if (vertexIndex > 0) {
                flush();
            }

            int slot = 0;
            int slotVal = usedTexturesLFU[0];

            int max = 0;
            int average = 0;

            // We search for the best candidate for a swap (least accessed) and collect some data
            for (int i = 0; i < maxTextureUnits; i++) {
                final int val = usedTexturesLFU[i];
                max = Math.max(val, max);
                average += val;
                if (val <= slotVal) {
                    slot = i;
                    slotVal = val;
                }
            }

            // The LFU weights will be normalized to the range 0...100
            final int normalizeRange = 100;
            for (int i = 0; i < maxTextureUnits; i++) {
                usedTexturesLFU[i] = usedTexturesLFU[i] * normalizeRange / max;
            }

            average = (average * normalizeRange) / (max * maxTextureUnits);

            // Give the new texture a fair (average) chance of staying.
            usedTexturesLFU[slot] = average;
            usedTextures[slot] = texture;

            dirtyTextureArray = true;

            // For statistics
            currentTextureLFUSwaps++;
            return slot;
        }
    }

    @Override
    public void setShader (ShaderProgram shader) {
        if (shader == customShader) return;
        if (drawing) {
            flush();
        }
        customShader = shader;
        if (drawing) {
            if (customShader != null)
                customShader.bind();
            else
                this.shader.bind();
            setupMatrices();
        }
    }

    @Override
    public ShaderProgram getShader () {
        if (customShader == null) {
            return shader;
        }
        return customShader;
    }

    @Override
    public boolean isBlendingEnabled () {
        return !blendingDisabled;
    }

    @Override
    public boolean isDrawing () {
        return drawing;
    }

    /** Queries the number of supported textures in a texture array by trying to create the default shader.<br>
     * The first call of this method is very expensive, after that it simply returns a cached value.
     * @return the number of supported textures in a texture array or zero if this feature is unsupported on this device.*/
    public static int getMaxTextureUnits () {
        if (maxTextureUnits == -1) {
            // Query the number of available texture units and decide on a safe number of texture units to use
            IntBuffer texUnitsQueryBuffer = BufferUtils.newIntBuffer(32);
            Gdx.gl.glGetIntegerv(GL20.GL_MAX_TEXTURE_IMAGE_UNITS, texUnitsQueryBuffer);

            int maxTextureUnitsLocal = texUnitsQueryBuffer.get();

            // Some OpenGL drivers (I'm looking at you, Intel!) do not report the right values,
            // so we take caution and test it first, reducing the number of slots if needed.
            // Will try to find the maximum amount of texture units supported.
            while (maxTextureUnitsLocal > 0) {
                ShaderCompiler.MAX_TEXTURE_UNIT = maxTextureUnitsLocal;
                try {
                    ShaderProgram tempProg = createDefaultShader();
                    tempProg.dispose();
                    break;
                } catch (Exception e) {
                    maxTextureUnitsLocal /= 2;
                    shaderErrorLog = e.getMessage();
                }
            }

            ShaderCompiler.MAX_TEXTURE_UNIT = maxTextureUnitsLocal;
            maxTextureUnits = maxTextureUnitsLocal;
        }

        return maxTextureUnits;
    }

    public static ShaderProgram createDefaultShader() {
        ShaderProgram shader = ShaderCompiler.compileShader(DefaultShaders.defaultArrayVertexShader(), DefaultShaders.defaultArrayFragmentShader());

        if (!shader.isCompiled()) {
            throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
        }

        return shader;
    }

    protected void switchTexture (Texture texture) {
        throw new GdxRuntimeException("Not implemented. Use TextureArrayPolygonSpriteBatch.activateTexture instead.");
    }
}