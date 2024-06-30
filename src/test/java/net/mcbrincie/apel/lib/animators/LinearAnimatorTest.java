package net.mcbrincie.apel.lib.animators;

import net.mcbrincie.apel.lib.objects.ParticlePoint;
import net.minecraft.particle.ParticleEffect;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LinearAnimatorTest {
    // null particle to avoid needing to load Minecraft
    private static final ParticlePoint POINT_WITH_NULL_PARTICLE = new ParticlePoint((ParticleEffect) null);

    @Test
    void testGetDistance() {
        // Given a LinearAnimator with a rendering interval
        LinearAnimator linearAnimator = new LinearAnimator(1, new Vector3f[]{
                new Vector3f(10, 0, 0), new Vector3f(-10, 0, 0), new Vector3f(9, 0, 0), new Vector3f(-9, 0, 0),
        }, POINT_WITH_NULL_PARTICLE, .04f);

        // When the distance is computed
        float distance = linearAnimator.getDistance();

        // Then it is 57: (10 to -10) + (-10 to 9) + (9 to -9), or 20 + 19 + 18 == 57.
        assertEquals(57f, distance);
    }

    @Test
    void testConvertToSteps() {
        // Given a LinearAnimator with a rendering interval
        LinearAnimator linearAnimator = new LinearAnimator(1, new Vector3f[]{
                new Vector3f(10, 0, 0), new Vector3f(-10, 0, 0), new Vector3f(9, 0, 0), new Vector3f(-9, 0, 0),
        }, POINT_WITH_NULL_PARTICLE, .04f);

        // When the distance is computed
        int steps = linearAnimator.convertToSteps();

        // Then it is 1425: (10 to -10) + (-10 to 9) + (9 to -9), or 20 + 19 + 18 == 57 / .04 == 1425.
        assertEquals(1425, steps);
    }
}