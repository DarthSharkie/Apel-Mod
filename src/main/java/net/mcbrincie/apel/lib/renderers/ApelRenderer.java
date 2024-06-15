package net.mcbrincie.apel.lib.renderers;

import net.mcbrincie.apel.lib.objects.ParticleObject;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import org.joml.Vector3f;

/**
 * ApelRenderer allows Apel animations to be rendered to multiple "canvases."
 * <p>
 * ApelRenderer offers a factory method to render to the {@link ServerWorld} powering the current instance of Minecraft.
 * This can be used as follows:
 * <pre>
 * void animate(ServerWorld world, PlayerEntity user) {
 *     // Setup a line
 *     Vector3f start = new Vector3f(-3, 1, 1);
 *     Vector3f end = new Vector3f(3, 1, 1);
 *     ParticleLine line1 = new ParticleLine(ParticleTypes.COMPOSTER, start, end, 5);
 *
 *     // Setup an Animator
 *     Vector3f origin = user.getPos().toVector3f().add(0, 1, 4);
 *     PointAnimator animator = new PointAnimator(1, line1, origin, 100);
 *
 *     // Perform the animation
 *     animator.beginAnimation(ApelRenderer.create(world));
 * }
 * </pre>
 */
public interface ApelRenderer {
    static ApelRenderer create(ServerWorld world) {
        return new ApelServerWorldRenderer(world);
    }

    /**
     * Instructs the renderer to draw the given particle effect at the given position.
     *
     * @param particleEffect The ParticleEffect to draw
     * @param drawPos The position at which to draw
     */
    void drawParticle(ParticleEffect particleEffect, Vector3f drawPos);

    /**
     * Instructs the renderer to draw a line of the given particle effect from {@code start} to {@code end} using
     * {@code count} particles.  Particles
     * will be spaced evenly along the defined line.
     *
     * @param particleEffect The ParticleEffect to use
     * @param start The 3D point at which to start
     * @param end The 3D point at which to end
     * @param count The number of particles to draw along the line
     */
    default void drawLine(ParticleEffect particleEffect, Vector3f start, Vector3f end, int count) {
        int amountSubOne = (count - 1);
        // Do not use 'sub', it modifies in-place
        float stepX = (end.x - start.x) / amountSubOne;
        float stepY = (end.y - start.y) / amountSubOne;
        float stepZ = (end.z - start.z) / amountSubOne;
        Vector3f curr = new Vector3f(start);
        for (int i = 0; i < count; i++) {
            drawParticle(particleEffect, curr);
            curr.add(stepX, stepY, stepZ);
        }
    }

    default void beforeFrame(ParticleObject particleObject, int step, Vector3f frameOrigin) {
    }

    default void afterFrame(ParticleObject particleObject, int step, Vector3f frameOrigin) {
    }

    ServerWorld getWorld();
}