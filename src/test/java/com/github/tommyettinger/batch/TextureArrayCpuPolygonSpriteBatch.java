package com.github.tommyettinger.batch;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * TextureArrayCpuPolygonSpriteBatch behaves like a SpriteBatch with the polygon drawing features of a
 * {@link PolygonSpriteBatch}, the transformation matrix optimizations of a {@link CpuSpriteBatch}, and optimizations
 * for Batches that switch between Textures frequently. This can be useful when you need any of: PolygonSpriteBatch
 * methods for drawing PolygonSprites, scene2d Groups with transform enabled, and/or drawing from multiple Textures.
 * <p>
 * This is an optimized version of the PolygonSpriteBatch that maintains an LFU texture-cache to combine draw calls with
 * different textures effectively. It also uses CpuSpriteBatch's optimizations that avoid flushing when the transform
 * matrix changes.
 * <p>
 * Use this Batch if you frequently utilize more than a single texture between calling {@link #begin()} and {@link #end()}. An
 * example would be if your Atlas is spread over multiple Textures or if you draw with individual Textures. This can be
 * a good "default" Batch implementation if you expect to use multiple Textures often, or use scene2d Groups often. In
 * TextraTypist, typically each Font has its own large Texture, and if you use emoji or icons, those typically use a
 * different large Texture. Switching between them has a performance cost, which is essentially eliminated by this
 * Batch. There is more logic in this Batch, and each vertex needs slightly more data, which counterbalances the
 * performance gains from more efficient Texture swaps. If you only use one Texture and don't use Groups where transform
 * is enabled, this Batch is expected to perform somewhat worse than a SpriteBatch. Using many Textures or using Group
 * transformation makes this perform relatively better than SpriteBatch in those cases. This is also a
 * PolygonSpriteBatch (and it extends {@link PolygonSpriteBatch}), which makes it suitable
 * for Spine animations.
 * <p>
 * Taken from <a href="https://github.com/rednblackgames/hyperlap2d-runtime-libgdx/tree/master/src/main/java/games/rednblack/editor/renderer/utils">Hyperlap2D's GitHub repo</a>.
 * Originally licensed under Apache 2.0, like TextraTypist and libGDX.
 *
 * @see Batch
 * @see SpriteBatch
 *
 * @author mzechner (Original SpriteBatch)
 * @author Nathan Sweet (Original SpriteBatch)
 * @author Valentin Milea (Transformation Matrix Extension)
 * @author VaTTeRGeR (TextureArray Extension)
 * @author fgnm (PolygonBatch Extension) */

public class TextureArrayCpuPolygonSpriteBatch extends TextureArrayPolygonSpriteBatch {
    private final Matrix4 virtualMatrix = new Matrix4();
    private final Affine2 adjustAffine = new Affine2();
    private boolean adjustNeeded;
    private boolean haveIdentityRealMatrix = true;

    private final Affine2 tmpAffine = new Affine2();

    /**
     * Constructs a CpuSpriteBatch with a size of 2000 and the default shader.
     *
     * @see SpriteBatch#SpriteBatch()
     */
    public TextureArrayCpuPolygonSpriteBatch() {
        this(2000);
    }

    /**
     * Constructs a CpuSpriteBatch with the default shader.
     *
     * @see SpriteBatch#SpriteBatch(int)
     */
    public TextureArrayCpuPolygonSpriteBatch(int size) {
        this(size, null);
    }

    /**
     * Constructs a CpuSpriteBatch with a custom shader.
     *
     * @see SpriteBatch#SpriteBatch(int, ShaderProgram)
     */
    public TextureArrayCpuPolygonSpriteBatch(int size, ShaderProgram defaultShader) {
        super(size, defaultShader);
    }

    /**
     * <p>
     * Flushes the batch and realigns the real matrix on the GPU. Subsequent draws won't need adjustment and will be slightly
     * faster as long as the transform matrix is not {@link #setTransformMatrix(Matrix4) changed}.
     * </p>
     * <p>
     * Note: The real transform matrix <em>must</em> be invertible. If a singular matrix is detected, GdxRuntimeException will be
     * thrown.
     * </p>
     *
     * @see SpriteBatch#flush()
     */
    public void flushAndSyncTransformMatrix() {
        flush();

        if (adjustNeeded) {
            // vertices flushed, safe now to replace matrix
            haveIdentityRealMatrix = checkIdt(virtualMatrix);

            if (!haveIdentityRealMatrix && virtualMatrix.det() == 0)
                throw new GdxRuntimeException("Transform matrix is singular, can't sync");

            adjustNeeded = false;
            super.setTransformMatrix(virtualMatrix);
        }
    }

    @Override
    public Matrix4 getTransformMatrix() {
        return (adjustNeeded ? virtualMatrix : super.getTransformMatrix());
    }

    /**
     * Sets the transform matrix to be used by this Batch. Even if this is called inside a {@link #begin()}/{@link #end()} block,
     * the current batch is <em>not</em> flushed to the GPU. Instead, for every subsequent draw() the vertices will be transformed
     * on the CPU to match the original batch matrix. This adjustment must be performed until the matrices are realigned by
     * restoring the original matrix, or by calling {@link #flushAndSyncTransformMatrix()}.
     */
    @Override
    public void setTransformMatrix(Matrix4 transform) {
        Matrix4 realMatrix = super.getTransformMatrix();

        if (checkEqual(realMatrix, transform)) {
            adjustNeeded = false;
        } else {
            if (isDrawing()) {
                virtualMatrix.setAsAffine(transform);
                adjustNeeded = true;

                // adjust = inverse(real) x virtual
                // real x adjust x vertex = virtual x vertex

                if (haveIdentityRealMatrix) {
                    adjustAffine.set(transform);
                } else {
                    tmpAffine.set(transform);
                    adjustAffine.set(realMatrix).inv().mul(tmpAffine);
                }
            } else {
                realMatrix.setAsAffine(transform);
                haveIdentityRealMatrix = checkIdt(realMatrix);
            }
        }
    }

    /**
     * Sets the transform matrix to be used by this Batch. Even if this is called inside a {@link #begin()}/{@link #end()} block,
     * the current batch is <em>not</em> flushed to the GPU. Instead, for every subsequent draw() the vertices will be transformed
     * on the CPU to match the original batch matrix. This adjustment must be performed until the matrices are realigned by
     * restoring the original matrix, or by calling {@link #flushAndSyncTransformMatrix()} or {@link #end()}.
     */
    public void setTransformMatrix(Affine2 transform) {
        Matrix4 realMatrix = super.getTransformMatrix();

        if (checkEqual(realMatrix, transform)) {
            adjustNeeded = false;
        } else {
            virtualMatrix.setAsAffine(transform);

            if (isDrawing()) {
                adjustNeeded = true;

                // adjust = inverse(real) x virtual
                // real x adjust x vertex = virtual x vertex

                if (haveIdentityRealMatrix) {
                    adjustAffine.set(transform);
                } else {
                    adjustAffine.set(realMatrix).inv().mul(transform);
                }
            } else {
                realMatrix.setAsAffine(transform);
                haveIdentityRealMatrix = checkIdt(realMatrix);
            }
        }
    }

    @Override
    public void draw(PolygonRegion region, float x, float y) {
        if (!adjustNeeded) {
            super.draw(region, x, y);
        } else {
            if (!drawing) throw new IllegalStateException("CpuPolygonSpriteBatch.begin must be called before draw.");

            final short[] triangles = this.triangles;
            final short[] regionTriangles = region.getTriangles();
            final int regionTrianglesLength = regionTriangles.length;
            final float[] regionVertices = region.getVertices();
            final int regionVerticesLength = regionVertices.length;

            final Texture texture = region.getRegion().getTexture();
            if (triangleIndex + regionTrianglesLength > triangles.length || vertexIndex + regionVerticesLength > vertices.length)
                flush();

            final float textureIndex = activateTexture(texture);

            int triangleIndex = this.triangleIndex;
            int vertexIndex = this.vertexIndex;
            final int startVertex = vertexIndex / VERTEX_SIZE;

            for (int i = 0; i < regionTrianglesLength; i++)
                triangles[triangleIndex++] = (short) (regionTriangles[i] + startVertex);
            this.triangleIndex = triangleIndex;

            final float[] vertices = this.vertices;
            final float color = this.colorPacked;
            final float[] textureCoords = region.getTextureCoords();

            Affine2 t = adjustAffine;
            for (int i = 0; i < regionVerticesLength; i += 2) {
                float x1 = regionVertices[i] + x;
                float y1 = regionVertices[i + 1] + y;
                vertices[vertexIndex++] = t.m00 * x1 + t.m01 * y1 + t.m02;
                vertices[vertexIndex++] = t.m10 * x1 + t.m11 * y1 + t.m12;
                vertices[vertexIndex++] = color;
                vertices[vertexIndex++] = textureCoords[i];
                vertices[vertexIndex++] = textureCoords[i + 1];
                vertices[vertexIndex++] = textureIndex;
            }
            this.vertexIndex = vertexIndex;
        }
    }

    @Override
    public void draw(PolygonRegion region, float x, float y, float originX, float originY, float width, float height,
                     float scaleX, float scaleY, float rotation) {
        if (!adjustNeeded) {
            super.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
        } else {
            if (!drawing) throw new IllegalStateException("CpuPolygonSpriteBatch.begin must be called before draw.");

            final short[] triangles = this.triangles;
            final short[] regionTriangles = region.getTriangles();
            final int regionTrianglesLength = regionTriangles.length;
            final float[] regionVertices = region.getVertices();
            final int regionVerticesLength = regionVertices.length;
            final TextureRegion textureRegion = region.getRegion();

            Texture texture = textureRegion.getTexture();
            if (triangleIndex + regionTrianglesLength > triangles.length
                    || vertexIndex + regionVerticesLength + regionVerticesLength * VERTEX_SIZE / 2 > vertices.length)
                flush();

            final float textureIndex = activateTexture(texture);

            int triangleIndex = this.triangleIndex;
            int vertexIndex = this.vertexIndex;
            final int startVertex = vertexIndex / VERTEX_SIZE;

            for (int i = 0; i < regionTrianglesLength; i++)
                triangles[triangleIndex++] = (short) (regionTriangles[i] + startVertex);
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
            Affine2 t = adjustAffine;
            for (int i = 0; i < regionVerticesLength; i += 2) {
                fx = (regionVertices[i] * sX - originX) * scaleX;
                fy = (regionVertices[i + 1] * sY - originY) * scaleY;
                float x1 = cos * fx - sin * fy + worldOriginX;
                float y1 = sin * fx + cos * fy + worldOriginY;
                vertices[vertexIndex++] = t.m00 * x1 + t.m01 * y1 + t.m02;
                vertices[vertexIndex++] = t.m10 * x1 + t.m11 * y1 + t.m12;
                vertices[vertexIndex++] = color;
                vertices[vertexIndex++] = textureCoords[i];
                vertices[vertexIndex++] = textureCoords[i + 1];
                vertices[vertexIndex++] = textureIndex;
            }
            this.vertexIndex = vertexIndex;
        }
    }

    @Override
    public void draw(PolygonRegion region, float x, float y, float width, float height) {
        if (!adjustNeeded) {
            super.draw(region, x, y, width, height);
        } else {
            if (!drawing) throw new IllegalStateException("CpuPolygonSpriteBatch.begin must be called before draw.");

            final short[] triangles = this.triangles;
            final short[] regionTriangles = region.getTriangles();
            final int regionTrianglesLength = regionTriangles.length;
            final float[] regionVertices = region.getVertices();
            final int regionVerticesLength = regionVertices.length;
            final TextureRegion textureRegion = region.getRegion();

            final Texture texture = textureRegion.getTexture();
            if (triangleIndex + regionTrianglesLength > triangles.length
                    || vertexIndex + regionVerticesLength * VERTEX_SIZE / 2 > vertices.length)
                flush();

            final float textureIndex = activateTexture(texture);

            int triangleIndex = this.triangleIndex;
            int vertexIndex = this.vertexIndex;
            final int startVertex = vertexIndex / VERTEX_SIZE;

            for (int i = 0, n = regionTriangles.length; i < n; i++)
                triangles[triangleIndex++] = (short) (regionTriangles[i] + startVertex);
            this.triangleIndex = triangleIndex;

            final float[] vertices = this.vertices;
            final float color = this.colorPacked;
            final float[] textureCoords = region.getTextureCoords();
            final float sX = width / textureRegion.getRegionWidth();
            final float sY = height / textureRegion.getRegionHeight();

            Affine2 t = adjustAffine;
            for (int i = 0; i < regionVerticesLength; i += 2) {
                float x1 = regionVertices[i] * sX + x;
                float y1 = regionVertices[i + 1] * sY + y;
                vertices[vertexIndex++] = t.m00 * x1 + t.m01 * y1 + t.m02;
                vertices[vertexIndex++] = t.m10 * x1 + t.m11 * y1 + t.m12;
                vertices[vertexIndex++] = color;
                vertices[vertexIndex++] = textureCoords[i];
                vertices[vertexIndex++] = textureCoords[i + 1];
                vertices[vertexIndex++] = textureIndex;
            }
            this.vertexIndex = vertexIndex;
        }
    }

    @Override
    public void draw(Texture texture, float x, float y) {
        if (!adjustNeeded) {
            super.draw(texture, x, y);
        } else {
            float width = texture.getWidth();
            float height = texture.getHeight();
            if (!drawing) throw new IllegalStateException("CpuPolygonSpriteBatch.begin must be called before draw.");

            final short[] triangles = this.triangles;
            final float[] vertices = this.vertices;

            if (triangleIndex + 6 > triangles.length || vertexIndex + SPRITE_SIZE > vertices.length) //
                flush();

            final float textureIndex = activateTexture(texture);

            int triangleIndex = this.triangleIndex;
            final int startVertex = vertexIndex / VERTEX_SIZE;
            triangles[triangleIndex++] = (short) startVertex;
            triangles[triangleIndex++] = (short) (startVertex + 1);
            triangles[triangleIndex++] = (short) (startVertex + 2);
            triangles[triangleIndex++] = (short) (startVertex + 2);
            triangles[triangleIndex++] = (short) (startVertex + 3);
            triangles[triangleIndex++] = (short) startVertex;
            this.triangleIndex = triangleIndex;

            final float fx2 = x + width;
            final float fy2 = y + height;
            final float u = 0;
            final float v = 1;
            final float u2 = 1;
            final float v2 = 0;

            float color = this.colorPacked;
            int idx = this.vertexIndex;
            Affine2 t = adjustAffine;
            vertices[idx++] = t.m00 * x + t.m01 * y + t.m02;
            vertices[idx++] = t.m10 * x + t.m11 * y + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u;
            vertices[idx++] = v;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * x + t.m01 * fy2 + t.m02;
            vertices[idx++] = t.m10 * x + t.m11 * fy2 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u;
            vertices[idx++] = v2;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * fx2 + t.m01 * fy2 + t.m02;
            vertices[idx++] = t.m10 * fx2 + t.m11 * fy2 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u2;
            vertices[idx++] = v2;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * fx2 + t.m01 * y + t.m02;
            vertices[idx++] = t.m10 * fx2 + t.m11 * y + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u2;
            vertices[idx++] = v;
            vertices[idx++] = textureIndex;
            this.vertexIndex = idx;
        }
    }

    @Override
    public void draw(Texture texture, float x, float y, float originX, float originY, float width, float height,
                     float scaleX, float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX,
                     boolean flipY) {
        if (!adjustNeeded) {
            super.draw(texture, x, y, originX, originY, width, height, scaleX, scaleY, rotation, srcX, srcY, srcWidth,
                    srcHeight, flipX, flipY);
        } else {
            if (!drawing) throw new IllegalStateException("CpuPolygonSpriteBatch.begin must be called before draw.");

            final short[] triangles = this.triangles;
            final float[] vertices = this.vertices;

            if (triangleIndex + 6 > triangles.length || vertexIndex + SPRITE_SIZE > vertices.length) //
                flush();

            final float textureIndex = activateTexture(texture);

            int triangleIndex = this.triangleIndex;
            final int startVertex = vertexIndex / VERTEX_SIZE;
            triangles[triangleIndex++] = (short) startVertex;
            triangles[triangleIndex++] = (short) (startVertex + 1);
            triangles[triangleIndex++] = (short) (startVertex + 2);
            triangles[triangleIndex++] = (short) (startVertex + 2);
            triangles[triangleIndex++] = (short) (startVertex + 3);
            triangles[triangleIndex++] = (short) startVertex;
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
            Affine2 t = adjustAffine;
            vertices[idx++] = t.m00 * x1 + t.m01 * y1 + t.m02;
            vertices[idx++] = t.m10 * x1 + t.m11 * y1 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u;
            vertices[idx++] = v;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * x2 + t.m01 * y2 + t.m02;
            vertices[idx++] = t.m10 * x2 + t.m11 * y2 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u;
            vertices[idx++] = v2;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * x3 + t.m01 * y3 + t.m02;
            vertices[idx++] = t.m10 * x3 + t.m11 * y3 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u2;
            vertices[idx++] = v2;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * x4 + t.m01 * y4 + t.m02;
            vertices[idx++] = t.m10 * x4 + t.m11 * y4 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u2;
            vertices[idx++] = v;
            vertices[idx++] = textureIndex;
            this.vertexIndex = idx;
        }
    }

    @Override
    public void draw(Texture texture, float x, float y, float width, float height) {
        if (!adjustNeeded) {
            super.draw(texture, x, y, width, height);
        } else {
            if (!drawing) throw new IllegalStateException("CpuPolygonSpriteBatch.begin must be called before draw.");

            final short[] triangles = this.triangles;
            final float[] vertices = this.vertices;

            if (triangleIndex + 6 > triangles.length || vertexIndex + SPRITE_SIZE > vertices.length) //
                flush();

            final float textureIndex = activateTexture(texture);

            int triangleIndex = this.triangleIndex;
            final int startVertex = vertexIndex / VERTEX_SIZE;
            triangles[triangleIndex++] = (short) startVertex;
            triangles[triangleIndex++] = (short) (startVertex + 1);
            triangles[triangleIndex++] = (short) (startVertex + 2);
            triangles[triangleIndex++] = (short) (startVertex + 2);
            triangles[triangleIndex++] = (short) (startVertex + 3);
            triangles[triangleIndex++] = (short) startVertex;
            this.triangleIndex = triangleIndex;

            final float fx2 = x + width;
            final float fy2 = y + height;
            final float u = 0;
            final float v = 1;
            final float u2 = 1;
            final float v2 = 0;

            float color = this.colorPacked;
            int idx = this.vertexIndex;
            Affine2 t = adjustAffine;
            vertices[idx++] = t.m00 * x + t.m01 * y + t.m02;
            vertices[idx++] = t.m10 * x + t.m11 * y + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u;
            vertices[idx++] = v;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * x + t.m01 * fy2 + t.m02;
            vertices[idx++] = t.m10 * x + t.m11 * fy2 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u;
            vertices[idx++] = v2;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * fx2 + t.m01 * fy2 + t.m02;
            vertices[idx++] = t.m10 * fx2 + t.m11 * fy2 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u2;
            vertices[idx++] = v2;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * fx2 + t.m01 * y + t.m02;
            vertices[idx++] = t.m10 * fx2 + t.m11 * y + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u2;
            vertices[idx++] = v;
            vertices[idx++] = textureIndex;
            this.vertexIndex = idx;
        }
    }

    @Override
    public void draw(Texture texture, float x, float y, float width, float height, float u, float v, float u2,
                     float v2) {
        if (!adjustNeeded) {
            super.draw(texture, x, y, width, height, u, v, u2, v2);
        } else {
            if (!drawing) throw new IllegalStateException("CpuPolygonSpriteBatch.begin must be called before draw.");

            final short[] triangles = this.triangles;
            final float[] vertices = this.vertices;

            if (triangleIndex + 6 > triangles.length || vertexIndex + SPRITE_SIZE > vertices.length) //
                flush();

            final float textureIndex = activateTexture(texture);

            int triangleIndex = this.triangleIndex;
            final int startVertex = vertexIndex / VERTEX_SIZE;
            triangles[triangleIndex++] = (short) startVertex;
            triangles[triangleIndex++] = (short) (startVertex + 1);
            triangles[triangleIndex++] = (short) (startVertex + 2);
            triangles[triangleIndex++] = (short) (startVertex + 2);
            triangles[triangleIndex++] = (short) (startVertex + 3);
            triangles[triangleIndex++] = (short) startVertex;
            this.triangleIndex = triangleIndex;

            final float fx2 = x + width;
            final float fy2 = y + height;

            float color = this.colorPacked;
            int idx = this.vertexIndex;
            Affine2 t = adjustAffine;
            vertices[idx++] = t.m00 * x + t.m01 * y + t.m02;
            vertices[idx++] = t.m10 * x + t.m11 * y + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u;
            vertices[idx++] = v;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * x + t.m01 * fy2 + t.m02;
            vertices[idx++] = t.m10 * x + t.m11 * fy2 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u;
            vertices[idx++] = v2;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * fx2 + t.m01 * fy2 + t.m02;
            vertices[idx++] = t.m10 * fx2 + t.m11 * fy2 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u2;
            vertices[idx++] = v2;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * fx2 + t.m01 * y + t.m02;
            vertices[idx++] = t.m10 * fx2 + t.m11 * y + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u2;
            vertices[idx++] = v;
            vertices[idx++] = textureIndex;
            this.vertexIndex = idx;
        }
    }

    @Override
    public void draw(Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth,
                     int srcHeight, boolean flipX, boolean flipY) {
        if (!adjustNeeded) {
            super.draw(texture, x, y, width, height, srcX, srcY, srcWidth, srcHeight, flipX, flipY);
        } else {
            if (!drawing) throw new IllegalStateException("CpuPolygonSpriteBatch.begin must be called before draw.");

            final short[] triangles = this.triangles;
            final float[] vertices = this.vertices;

            if (triangleIndex + 6 > triangles.length || vertexIndex + SPRITE_SIZE > vertices.length) //
                flush();

            final float textureIndex = activateTexture(texture);

            int triangleIndex = this.triangleIndex;
            final int startVertex = vertexIndex / VERTEX_SIZE;
            triangles[triangleIndex++] = (short) startVertex;
            triangles[triangleIndex++] = (short) (startVertex + 1);
            triangles[triangleIndex++] = (short) (startVertex + 2);
            triangles[triangleIndex++] = (short) (startVertex + 2);
            triangles[triangleIndex++] = (short) (startVertex + 3);
            triangles[triangleIndex++] = (short) startVertex;
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
            Affine2 t = adjustAffine;
            vertices[idx++] = t.m00 * x + t.m01 * y + t.m02;
            vertices[idx++] = t.m10 * x + t.m11 * y + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u;
            vertices[idx++] = v;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * x + t.m01 * fy2 + t.m02;
            vertices[idx++] = t.m10 * x + t.m11 * fy2 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u;
            vertices[idx++] = v2;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * fx2 + t.m01 * fy2 + t.m02;
            vertices[idx++] = t.m10 * fx2 + t.m11 * fy2 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u2;
            vertices[idx++] = v2;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * fx2 + t.m01 * y + t.m02;
            vertices[idx++] = t.m10 * fx2 + t.m11 * y + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u2;
            vertices[idx++] = v;
            vertices[idx++] = textureIndex;
            this.vertexIndex = idx;
        }
    }

    @Override
    public void draw(Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {
        if (!adjustNeeded) {
            super.draw(texture, x, y, srcX, srcY, srcWidth, srcHeight);
        } else {
            if (!drawing) throw new IllegalStateException("CpuPolygonSpriteBatch.begin must be called before draw.");

            final short[] triangles = this.triangles;
            final float[] vertices = this.vertices;

            if (triangleIndex + 6 > triangles.length || vertexIndex + SPRITE_SIZE > vertices.length) //
                flush();

            final float textureIndex = activateTexture(texture);

            int triangleIndex = this.triangleIndex;
            final int startVertex = vertexIndex / VERTEX_SIZE;
            triangles[triangleIndex++] = (short) startVertex;
            triangles[triangleIndex++] = (short) (startVertex + 1);
            triangles[triangleIndex++] = (short) (startVertex + 2);
            triangles[triangleIndex++] = (short) (startVertex + 2);
            triangles[triangleIndex++] = (short) (startVertex + 3);
            triangles[triangleIndex++] = (short) startVertex;
            this.triangleIndex = triangleIndex;

            final float u = srcX * invTexWidth;
            final float v = (srcY + srcHeight) * invTexHeight;
            final float u2 = (srcX + srcWidth) * invTexWidth;
            final float v2 = srcY * invTexHeight;
            final float fx2 = x + srcWidth;
            final float fy2 = y + srcHeight;

            float color = this.colorPacked;
            int idx = this.vertexIndex;
            Affine2 t = adjustAffine;
            vertices[idx++] = t.m00 * x + t.m01 * y + t.m02;
            vertices[idx++] = t.m10 * x + t.m11 * y + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u;
            vertices[idx++] = v;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * x + t.m01 * fy2 + t.m02;
            vertices[idx++] = t.m10 * x + t.m11 * fy2 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u;
            vertices[idx++] = v2;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * fx2 + t.m01 * y + t.m02;
            vertices[idx++] = t.m10 * fx2 + t.m11 * y + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u2;
            vertices[idx++] = v2;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * fx2 + t.m01 * y + t.m02;
            vertices[idx++] = t.m10 * fx2 + t.m11 * y + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u2;
            vertices[idx++] = v;
            vertices[idx++] = textureIndex;
            this.vertexIndex = idx;
        }
    }

    /**
     * Draws the polygon using the given vertices and triangles. Each vertex must be made up of 5 elements in this order: x, y,
     * color, u, v.
     */
    @Override
    public void draw(Texture texture, float[] polygonVertices, int verticesOffset, int verticesCount,
                     short[] polygonTriangles, int trianglesOffset, int trianglesCount) {
        if (!adjustNeeded) {
            super.draw(texture, polygonVertices, verticesOffset, verticesCount, polygonTriangles, trianglesOffset,
                    trianglesCount);
        } else {
            if (!drawing) throw new IllegalStateException("TextureArrayCpuPolygonSpriteBatch.begin must be called before draw.");

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
                triangles[triangleIndex++] = (short) (polygonTriangles[i] + startVertex);
            this.triangleIndex = triangleIndex;

            Affine2 t = adjustAffine;
            int vIn = vertexIndex;
            for (int offsetIn = verticesOffset; offsetIn < verticesCount + verticesOffset; offsetIn += 5, vIn += VERTEX_SIZE) {
                float x = polygonVertices[offsetIn];
                float y = polygonVertices[offsetIn + 1];

                vertices[vIn] = t.m00 * x + t.m01 * y + t.m02; // x
                vertices[vIn + 1] = t.m10 * x + t.m11 * y + t.m12; // y
                vertices[vIn + 2] = polygonVertices[offsetIn + 2]; // color
                vertices[vIn + 3] = polygonVertices[offsetIn + 3]; // u
                vertices[vIn + 4] = polygonVertices[offsetIn + 4]; // v
                vertices[vIn + 5] = textureIndex; // texture
            }
            this.vertexIndex += vCount;
        }
    }

    @Override
    public void draw(Texture texture, float[] spriteVertices, int offset, int count) {
        if (!adjustNeeded) {
            super.draw(texture, spriteVertices, offset, count);
        } else {
            if (!drawing) throw new IllegalStateException("TextureArrayCpuPolygonSpriteBatch.begin must be called before draw.");

            final short[] triangles = this.triangles;
            final float[] vertices = this.vertices;

            //Calculate how many vertices and triangles will be put in the batch
            int triangleCount = (count / 20) * 6;
            int verticesCount = (count / 5) * 6;
            if (this.triangleIndex + triangleCount > triangles.length || this.vertexIndex + verticesCount > vertices.length)
                flush();

            float textureIndex = activateTexture(texture);

            int triangleIndex = this.triangleIndex;

            int startVertex = vertexIndex / VERTEX_SIZE;
            for (int n = triangleIndex + triangleCount; triangleIndex < n; startVertex += 4) {
                triangles[triangleIndex++] = (short)startVertex;
                triangles[triangleIndex++] = (short)(startVertex + 1);
                triangles[triangleIndex++] = (short)(startVertex + 2);
                triangles[triangleIndex++] = (short)(startVertex + 2);
                triangles[triangleIndex++] = (short)(startVertex + 3);
                triangles[triangleIndex++] = (short)startVertex;
            }
            this.triangleIndex = triangleIndex;

            Affine2 t = adjustAffine;
            int idx = vertexIndex;
            for (int offsetIn = offset; offsetIn < count + offset; offsetIn += 5) {
                float x = spriteVertices[offsetIn];
                float y = spriteVertices[offsetIn + 1];

                vertices[idx++] = t.m00 * x + t.m01 * y + t.m02; // x
                vertices[idx++] = t.m10 * x + t.m11 * y + t.m12; // y
                vertices[idx++] = spriteVertices[offsetIn + 2]; // color
                vertices[idx++] = spriteVertices[offsetIn + 3]; // u
                vertices[idx++] = spriteVertices[offsetIn + 4]; // v
                vertices[idx++] = textureIndex; // texture index
            }
            this.vertexIndex = idx;
        }
    }

    @Override
    public void draw(TextureRegion region, float width, float height, Affine2 transform) {
        if (!adjustNeeded) {
            super.draw(region, width, height, transform);
        } else {
            if (!drawing) throw new IllegalStateException("CpuPolygonSpriteBatch.begin must be called before draw.");

            final short[] triangles = this.triangles;
            final float[] vertices = this.vertices;

            Texture texture = region.getTexture();
            if (triangleIndex + 6 > triangles.length || vertexIndex + SPRITE_SIZE > vertices.length) //
                flush();

            float textureIndex = activateTexture(texture);

            int triangleIndex = this.triangleIndex;
            final int startVertex = vertexIndex / VERTEX_SIZE;
            triangles[triangleIndex++] = (short) startVertex;
            triangles[triangleIndex++] = (short) (startVertex + 1);
            triangles[triangleIndex++] = (short) (startVertex + 2);
            triangles[triangleIndex++] = (short) (startVertex + 2);
            triangles[triangleIndex++] = (short) (startVertex + 3);
            triangles[triangleIndex++] = (short) startVertex;
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
            Affine2 t = adjustAffine;
            vertices[idx++] = t.m00 * x1 + t.m01 * y1 + t.m02;
            vertices[idx++] = t.m10 * x1 + t.m11 * y1 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u;
            vertices[idx++] = v;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * x2 + t.m01 * y2 + t.m02;
            vertices[idx++] = t.m10 * x2 + t.m11 * y2 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u;
            vertices[idx++] = v2;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * x3 + t.m01 * y3 + t.m02;
            vertices[idx++] = t.m10 * x3 + t.m11 * y3 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u2;
            vertices[idx++] = v2;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * x4 + t.m01 * y4 + t.m02;
            vertices[idx++] = t.m10 * x4 + t.m11 * y4 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u2;
            vertices[idx++] = v;
            vertices[idx++] = textureIndex;
            vertexIndex = idx;
        }
    }

    @Override
    public void draw(TextureRegion region, float x, float y) {
        if (!adjustNeeded) {
            super.draw(region, x, y);
        } else {
            float width = region.getRegionWidth();
            float height = region.getRegionHeight();
            if (!drawing) throw new IllegalStateException("CpuPolygonSpriteBatch.begin must be called before draw.");

            final short[] triangles = this.triangles;
            final float[] vertices = this.vertices;

            Texture texture = region.getTexture();
            if (triangleIndex + 6 > triangles.length || vertexIndex + SPRITE_SIZE > vertices.length) //
                flush();

            float textureIndex = activateTexture(texture);

            int triangleIndex = this.triangleIndex;
            final int startVertex = vertexIndex / VERTEX_SIZE;
            triangles[triangleIndex++] = (short) startVertex;
            triangles[triangleIndex++] = (short) (startVertex + 1);
            triangles[triangleIndex++] = (short) (startVertex + 2);
            triangles[triangleIndex++] = (short) (startVertex + 2);
            triangles[triangleIndex++] = (short) (startVertex + 3);
            triangles[triangleIndex++] = (short) startVertex;
            this.triangleIndex = triangleIndex;

            final float fx2 = x + width;
            final float fy2 = y + height;
            final float u = region.getU();
            final float v = region.getV2();
            final float u2 = region.getU2();
            final float v2 = region.getV();

            float color = this.colorPacked;
            int idx = this.vertexIndex;
            Affine2 t = adjustAffine;
            vertices[idx++] = t.m00 * x + t.m01 * y + t.m02;
            vertices[idx++] = t.m10 * x + t.m11 * y + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u;
            vertices[idx++] = v;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * x + t.m01 * fy2 + t.m02;
            vertices[idx++] = t.m10 * x + t.m11 * fy2 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u;
            vertices[idx++] = v2;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * fx2 + t.m01 * fy2 + t.m02;
            vertices[idx++] = t.m10 * fx2 + t.m11 * fy2 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u2;
            vertices[idx++] = v2;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * fx2 + t.m01 * y + t.m02;
            vertices[idx++] = t.m10 * fx2 + t.m11 * y + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u2;
            vertices[idx++] = v;
            vertices[idx++] = textureIndex;
            this.vertexIndex = idx;
        }
    }

    @Override
    public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height,
                     float scaleX, float scaleY, float rotation) {
        if (!adjustNeeded) {
            super.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
        } else {
            if (!drawing) throw new IllegalStateException("CpuPolygonSpriteBatch.begin must be called before draw.");

            final short[] triangles = this.triangles;
            final float[] vertices = this.vertices;

            Texture texture = region.getTexture();
            if (triangleIndex + 6 > triangles.length || vertexIndex + SPRITE_SIZE > vertices.length) //
                flush();

            final float textureIndex = activateTexture(texture);

            int triangleIndex = this.triangleIndex;
            final int startVertex = vertexIndex / VERTEX_SIZE;
            triangles[triangleIndex++] = (short) startVertex;
            triangles[triangleIndex++] = (short) (startVertex + 1);
            triangles[triangleIndex++] = (short) (startVertex + 2);
            triangles[triangleIndex++] = (short) (startVertex + 2);
            triangles[triangleIndex++] = (short) (startVertex + 3);
            triangles[triangleIndex++] = (short) startVertex;
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
            Affine2 t = adjustAffine;
            vertices[idx++] = t.m00 * x1 + t.m01 * y1 + t.m02;
            vertices[idx++] = t.m10 * x1 + t.m11 * y1 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u;
            vertices[idx++] = v;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * x2 + t.m01 * y2 + t.m02;
            vertices[idx++] = t.m10 * x2 + t.m11 * y2 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u;
            vertices[idx++] = v2;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * x3 + t.m01 * y3 + t.m02;
            vertices[idx++] = t.m10 * x3 + t.m11 * y3 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u2;
            vertices[idx++] = v2;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * x4 + t.m01 * y4 + t.m02;
            vertices[idx++] = t.m10 * x4 + t.m11 * y4 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u2;
            vertices[idx++] = v;
            vertices[idx++] = textureIndex;
            this.vertexIndex = idx;
        }
    }

    @Override
    public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height,
                     float scaleX, float scaleY, float rotation, boolean clockwise) {
        if (!adjustNeeded) {
            super.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation, clockwise);
        } else {
            if (!drawing) throw new IllegalStateException("CpuPolygonSpriteBatch.begin must be called before draw.");

            final short[] triangles = this.triangles;
            final float[] vertices = this.vertices;

            Texture texture = region.getTexture();
            if (triangleIndex + 6 > triangles.length || vertexIndex + SPRITE_SIZE > vertices.length) //
                flush();

            final float textureIndex = activateTexture(texture);

            int triangleIndex = this.triangleIndex;
            final int startVertex = vertexIndex / VERTEX_SIZE;
            triangles[triangleIndex++] = (short) startVertex;
            triangles[triangleIndex++] = (short) (startVertex + 1);
            triangles[triangleIndex++] = (short) (startVertex + 2);
            triangles[triangleIndex++] = (short) (startVertex + 2);
            triangles[triangleIndex++] = (short) (startVertex + 3);
            triangles[triangleIndex++] = (short) startVertex;
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
            Affine2 t = adjustAffine;
            vertices[idx++] = t.m00 * x1 + t.m01 * y1 + t.m02;
            vertices[idx++] = t.m10 * x1 + t.m11 * y1 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u1;
            vertices[idx++] = v1;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * x2 + t.m01 * y2 + t.m02;
            vertices[idx++] = t.m10 * x2 + t.m11 * y2 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u2;
            vertices[idx++] = v2;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * x3 + t.m01 * y3 + t.m02;
            vertices[idx++] = t.m10 * x3 + t.m11 * y3 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u3;
            vertices[idx++] = v3;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * x4 + t.m01 * y4 + t.m02;
            vertices[idx++] = t.m10 * x4 + t.m11 * y4 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u4;
            vertices[idx++] = v4;
            vertices[idx++] = textureIndex;
            this.vertexIndex = idx;
        }
    }

    @Override
    public void draw(TextureRegion region, float x, float y, float width, float height) {
        if (!adjustNeeded) {
            super.draw(region, x, y, width, height);
        } else {
            if (!drawing) throw new IllegalStateException("CpuPolygonSpriteBatch.begin must be called before draw.");

            final short[] triangles = this.triangles;
            final float[] vertices = this.vertices;

            Texture texture = region.getTexture();
            if (triangleIndex + 6 > triangles.length || vertexIndex + SPRITE_SIZE > vertices.length) //
                flush();

            final float textureIndex = activateTexture(texture);

            int triangleIndex = this.triangleIndex;
            final int startVertex = vertexIndex / VERTEX_SIZE;
            triangles[triangleIndex++] = (short) startVertex;
            triangles[triangleIndex++] = (short) (startVertex + 1);
            triangles[triangleIndex++] = (short) (startVertex + 2);
            triangles[triangleIndex++] = (short) (startVertex + 2);
            triangles[triangleIndex++] = (short) (startVertex + 3);
            triangles[triangleIndex++] = (short) startVertex;
            this.triangleIndex = triangleIndex;

            final float fx2 = x + width;
            final float fy2 = y + height;
            final float u = region.getU();
            final float v = region.getV2();
            final float u2 = region.getU2();
            final float v2 = region.getV();

            float color = this.colorPacked;
            int idx = this.vertexIndex;
            Affine2 t = adjustAffine;
            vertices[idx++] = t.m00 * x + t.m01 * y + t.m02;
            vertices[idx++] = t.m10 * x + t.m11 * y + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u;
            vertices[idx++] = v;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * x + t.m01 * fy2 + t.m02;
            vertices[idx++] = t.m10 * x + t.m11 * fy2 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u;
            vertices[idx++] = v2;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * fx2 + t.m01 * fy2 + t.m02;
            vertices[idx++] = t.m10 * fx2 + t.m11 * fy2 + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u2;
            vertices[idx++] = v2;
            vertices[idx++] = textureIndex;

            vertices[idx++] = t.m00 * fx2 + t.m01 * y + t.m02;
            vertices[idx++] = t.m10 * fx2 + t.m11 * y + t.m12;
            vertices[idx++] = color;
            vertices[idx++] = u2;
            vertices[idx++] = v;
            vertices[idx++] = textureIndex;
            this.vertexIndex = idx;
        }
    }

    private static boolean checkEqual(Matrix4 a, Matrix4 b) {
        if (a == b) return true;

        // matrices are assumed to be 2D transformations
        return (a.val[Matrix4.M00] == b.val[Matrix4.M00] && a.val[Matrix4.M10] == b.val[Matrix4.M10]
                && a.val[Matrix4.M01] == b.val[Matrix4.M01] && a.val[Matrix4.M11] == b.val[Matrix4.M11]
                && a.val[Matrix4.M03] == b.val[Matrix4.M03] && a.val[Matrix4.M13] == b.val[Matrix4.M13]);
    }

    private static boolean checkEqual(Matrix4 matrix, Affine2 affine) {
        final float[] val = matrix.getValues();

        // matrix is assumed to be 2D transformation
        return (val[Matrix4.M00] == affine.m00 && val[Matrix4.M10] == affine.m10 && val[Matrix4.M01] == affine.m01
                && val[Matrix4.M11] == affine.m11 && val[Matrix4.M03] == affine.m02 && val[Matrix4.M13] == affine.m12);
    }

    private static boolean checkIdt(Matrix4 matrix) {
        final float[] val = matrix.getValues();

        // matrix is assumed to be 2D transformation
        return (val[Matrix4.M00] == 1 && val[Matrix4.M10] == 0 && val[Matrix4.M01] == 0 && val[Matrix4.M11] == 1
                && val[Matrix4.M03] == 0 && val[Matrix4.M13] == 0);
    }
}