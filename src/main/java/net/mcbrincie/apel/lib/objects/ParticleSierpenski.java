package net.mcbrincie.apel.lib.objects;

import net.mcbrincie.apel.lib.renderers.ApelServerRenderer;
import net.minecraft.particle.ParticleEffect;
import org.joml.Vector3f;

public class ParticleSierpenski extends ParticleObject {
    private final float sideLength;

    public ParticleSierpenski(ParticleEffect particleEffect, float sideLength, int amount) {
        super(particleEffect);
        this.setAmount(amount);
        this.sideLength = sideLength;
    }

    @Override
    public void draw(ApelServerRenderer renderer, int step, Vector3f drawPos) {
        // Compute the three points, assuming drawPos is in the center of the triangle, then adjust by drawPos/offset.
        Vector3f top = new Vector3f(0f, (float) (this.sideLength / Math.sqrt(3.0)), 0);
        Vector3f left = new Vector3f(0f - this.sideLength / 2, (float) (0f - this.sideLength / (2*Math.sqrt(3))), 0);
        Vector3f right = new Vector3f(left.x() + this.sideLength, left.y(), 0);

        System.out.println(top);
        System.out.println(left);
        System.out.println(right);

        top.add(drawPos).add(this.offset);
        left.add(drawPos).add(this.offset);
        right.add(drawPos).add(this.offset);

        this.recursiveDraw(renderer, step, top, left, right, this.sideLength, this.amount);
    }

    private void recursiveDraw(ApelServerRenderer renderer, int step, Vector3f top, Vector3f left, Vector3f right, float length, int amount) {
        // Base case, don't draw less than half a block-width
        if (length < 0.5f) {
            return;
        }

        renderer.drawLine(this.particleEffect, step, top, left, amount);
        renderer.drawLine(this.particleEffect, step, left, right, amount);
        renderer.drawLine(this.particleEffect, step, top, right, amount);

        // Recurse into each corner
        Vector3f topLeftMidpoint = new Vector3f(top).lerp(left, 0.5f);
        Vector3f topRightMidpoint = new Vector3f(top).lerp(right, 0.5f);
        Vector3f leftRightMidpoint = new Vector3f(left).lerp(right, 0.5f);
        this.recursiveDraw(renderer, step, top, topLeftMidpoint, topRightMidpoint, length / 2, amount / 2);
        this.recursiveDraw(renderer, step, topLeftMidpoint, left, leftRightMidpoint, length / 2, amount / 2);
        this.recursiveDraw(renderer, step, topRightMidpoint, leftRightMidpoint, right, length / 2, amount / 2);

    }
}
