package net.mcbrincie.apel.lib.objects;

import net.mcbrincie.apel.lib.renderers.ApelServerRenderer;
import net.mcbrincie.apel.lib.util.interceptor.DrawContext;
import org.joml.Vector3f;


/** The particle object class that represents a quadrilateral.  This can be a rectangle, a square, a trapezoid,
 * or any irregular shape with four sides.  There is no restriction on the vertices being co-planar.
 *
 * <p>The quadrilateral is drawn in order of the vertices provided (presume the line
 * from vertex2 to vertex3 is straight, ignoring the limitations of ASCII art):
 * <pre>
 *     vertex1 ------------------ vertex2
 *        \                          |
 *         \                         |
 *          \                        |
 *        vertex4 --------------- vertex3
 * </pre>
 */
@SuppressWarnings("unused")
public class ParticleQuad extends ParticleObject<ParticleQuad> {
    protected Vector3f vertex1;
    protected Vector3f vertex2;
    protected Vector3f vertex3;
    protected Vector3f vertex4;

    public static Builder<?> builder() {
        return new Builder<>();
    }

    private ParticleQuad(Builder<?> builder) {
        super(builder.particleEffect, builder.rotation, builder.offset, builder.amount, builder.beforeDraw,
              builder.afterDraw);
        this.setVertex1(builder.vertex1);
        this.setVertex2(builder.vertex2);
        this.setVertex3(builder.vertex3);
        this.setVertex4(builder.vertex4);
    }

    /** The copy constructor for a specific particle object. It copies all
     * the params, including the interceptors the particle object has
     *
     * @param quad The particle quad object to copy from
    */
    public ParticleQuad(ParticleQuad quad) {
        super(quad);
        this.vertex1 = quad.vertex1;
        this.vertex2 = quad.vertex2;
        this.vertex3 = quad.vertex3;
        this.vertex4 = quad.vertex4;
    }

    /** Sets all vertices at once instead of one at a time.
     * If you want to set one vertex at a time, then its recommend to use
     * the methods {@link #setVertex1(Vector3f)}, etc.
     *
     * @param vertices The vertices to modify
     *
     * @throws IllegalArgumentException if the number of vertices supplied isn't equal to 4
     */
    public void setVertices(Vector3f[] vertices) throws IllegalArgumentException {
        if (vertices.length != 4) {
            throw new IllegalArgumentException("The amount of vertices must be 4");
        }
        this.vertex1 = vertices[0];
        this.vertex2 = vertices[1];
        this.vertex3 = vertices[2];
        this.vertex4 = vertices[3];
    }

    /**
     * Sets the first individual vertex, it returns the previous vertex that was used. If you want to modify multiple
     * vertices at once then use {@code setVertices}.
     * <p>
     * This implementation is used by the constructor, so subclasses cannot override this method.
     *
     * @param newVertex The new vertex
     * @return The previous vertex
     *
     * @see ParticleQuad#setVertices(Vector3f[])
     */
    public final Vector3f setVertex1(Vector3f newVertex) {
        Vector3f prevVertex = this.vertex1;
        this.vertex1 = newVertex;
        return prevVertex;
    }

    /**
     * Sets the second individual vertex, it returns the previous vertex that was used. If you want to modify multiple
     * vertices at once then use {@code setVertices}.
     * <p>
     * This implementation is used by the constructor, so subclasses cannot override this method.
     *
     * @param newVertex The new vertex
     * @return The previous vertex
     *
     * @see ParticleQuad#setVertices(Vector3f[])
    */
    public final Vector3f setVertex2(Vector3f newVertex) {
        Vector3f prevVertex = this.vertex2;
        this.vertex2 = newVertex;
        return prevVertex;
    }

    /**
     * Sets the third individual vertex, it returns the previous vertex that was used. If you want to modify multiple
     * vertices at once then use {@code setVertices}.
     * <p>
     * This implementation is used by the constructor, so subclasses cannot override this method.
     *
     * @param newVertex The new vertex
     * @return The previous vertex
     *
     * @see ParticleQuad#setVertices(Vector3f[])
    */
    public final Vector3f setVertex3(Vector3f newVertex) {
        Vector3f prevVertex = this.vertex3;
        this.vertex3 = newVertex;
        return prevVertex;
    }

    /**
     * Sets the fourth individual vertex, it returns the previous vertex that was used. If you want to modify multiple
     * vertices at once then use {@code setVertices}.
     * <p>
     * This implementation is used by the constructor, so subclasses cannot override this method.
     *
     * @param newVertex The new vertex
     * @return The previous vertex
     *
     * @see ParticleQuad#setVertices(Vector3f[])
    */
    public final Vector3f setVertex4(Vector3f newVertex) {
        Vector3f prevVertex = this.vertex4;
        this.vertex4 = newVertex;
        return prevVertex;
    }

    /** Gets the first individual vertex.
     *
     * @return The first individual vertex
    */
    protected Vector3f getVertex1() {
        return this.vertex1;
    }

    /** Gets the second individual vertex.
     *
     * @return The second individual vertex
    */
    protected Vector3f getVertex2() {
        return this.vertex2;
    }

    /** Gets the third individual vertex.
     *
     * @return The third individual vertex
    */
    protected Vector3f getVertex3() {
        return this.vertex3;
    }

    /** Gets the fourth individual vertex.
     *
     * @return The fourth individual vertex
    */
    protected Vector3f getVertex4() {
        return this.vertex4;
    }

    @Override
    public void draw(ApelServerRenderer renderer, DrawContext drawContext) {
        // Defensive copy of `drawPos`
        Vector3f objectDrawPos = new Vector3f(drawContext.getPosition()).add(this.offset);

        int step = drawContext.getCurrentStep();
        renderer.drawLine(this.particleEffect, step, objectDrawPos, this.vertex1, this.vertex2, this.rotation, this.amount);
        renderer.drawLine(this.particleEffect, step, objectDrawPos, this.vertex2, this.vertex3, this.rotation, this.amount);
        renderer.drawLine(this.particleEffect, step, objectDrawPos, this.vertex3, this.vertex4, this.rotation, this.amount);
        renderer.drawLine(this.particleEffect, step, objectDrawPos, this.vertex4, this.vertex1, this.rotation, this.amount);
    }

    public static class Builder<B extends Builder<B>> extends ParticleObject.Builder<B, ParticleQuad> {
        protected Vector3f vertex1;
        protected Vector3f vertex2;
        protected Vector3f vertex3;
        protected Vector3f vertex4;

        private Builder() {}

        /**
         * Sets the vertices to produce a rectangle in the XY-plane.  This method is not cumulative; repeated calls to
         * this method or the four vertex methods will overwrite values.
         */
        // Constructor helper to transform width/height into vertices
        public B rectangle(float width, float height) {
            this.vertex1 = new Vector3f(width / 2, height / 2, 0);
            this.vertex2 = new Vector3f(width / 2, -height / 2, 0);
            this.vertex3 = new Vector3f(-width / 2, -height / 2, 0);
            this.vertex4 = new Vector3f(-width / 2, height / 2, 0);
            return self();
        }

        /**
         * Set vertex1 on the builder.  This method is not cumulative; repeated calls to {@code rectangle} or this
         * method will overwrite the value.
         */
        public B vertex1(Vector3f vertex1) {
            this.vertex1 = vertex1;
            return self();
        }

        /**
         * Set vertex2 on the builder.  This method is not cumulative; repeated calls to {@code rectangle} or this
         * method will overwrite the value.
         */
        public B vertex2(Vector3f vertex2) {
            this.vertex2 = vertex2;
            return self();
        }

        /**
         * Set vertex3 on the builder.  This method is not cumulative; repeated calls to {@code rectangle} or this
         * method will overwrite the value.
         */
        public B vertex3(Vector3f vertex3) {
            this.vertex3 = vertex3;
            return self();
        }

        /**
         * Set vertex4 on the builder.  This method is not cumulative; repeated calls to {@code rectangle} or this
         * method will overwrite the value.
         */
        public B vertex4(Vector3f vertex4) {
            this.vertex4 = vertex4;
            return self();
        }

        @Override
        public ParticleQuad build() {
            return new ParticleQuad(this);
        }
    }
}
