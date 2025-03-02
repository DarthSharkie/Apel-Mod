package net.mcbrincie.apel.lib.objects;

import io.netty.buffer.Unpooled;
import net.mcbrincie.apel.lib.renderers.ApelRenderer;
import net.mcbrincie.apel.lib.renderers.ApelServerRenderer;
import net.mcbrincie.apel.lib.util.interceptor.DrawContext;
import net.mcbrincie.apel.lib.util.math.bezier.BezierCurve;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import org.joml.Vector3f;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ParticleRecording extends ParticleObject<ParticleRecording> {
    protected List<ApelRenderer.Instruction> instructions;
    private int instructionNumber;

    public static <B extends Builder<B>> Builder<B> builder(ServerWorld serverWorld) {
        return new Builder<>(serverWorld);
    }

    public <B extends Builder<B>> ParticleRecording(Builder<B> builder) {
        super(builder.particleEffect, builder.rotation, builder.offset, builder.amount, builder.beforeDraw,
                builder.afterDraw);
        this.instructions = builder.instructions;
        this.instructionNumber = 0;
    }

    @Override
    public void draw(ApelServerRenderer renderer, DrawContext data) {

        Vector3f frameOrigin = null;
        while (this.instructionNumber < this.instructions.size()) {

            ApelRenderer.Instruction ins = this.instructions.get(this.instructionNumber);

            switch (ins) {
                case ApelRenderer.Frame(Vector3f origin) -> {
                    if (frameOrigin != null) {
                        // Second Frame seen, that's an indication drawing the current Frame is done
                        return;
                    } else {
                        frameOrigin = origin;
                    }
                }

                case ApelRenderer.PType(ParticleEffect pe) -> particleEffect = pe;

                case ApelRenderer.Particle(Vector3f pos) -> renderer.drawParticle(particleEffect, 0, pos);

                case ApelRenderer.Line(Vector3f drawPos, Vector3f start, Vector3f end, Vector3f lineRotation, int lineAmount) ->
                        renderer.drawLine(particleEffect, 0, drawPos, start, end, lineRotation, lineAmount);

                case ApelRenderer.Ellipse(
                        Vector3f center, float radius, float stretch, Vector3f ellipseRotation, int ellipseAmount
                ) -> renderer.drawEllipse(particleEffect, 0, center, radius, stretch, ellipseRotation, ellipseAmount);

                case ApelRenderer.Ellipsoid(
                        Vector3f drawPos, float xSemiAxis, float ySemiAxis, float zSemiAxis, Vector3f ellipsoidRotation,
                        int ellipsoidAmount
                ) -> renderer.drawEllipsoid(particleEffect, 0, drawPos, xSemiAxis, ySemiAxis, zSemiAxis, ellipsoidRotation,
                        ellipsoidAmount
                );

                case ApelRenderer.BezierCurve(
                        Vector3f drawPos, BezierCurve bezierCurve, Vector3f bezierRotation, int bezierAmount
                ) -> renderer.drawBezier(particleEffect, 0, drawPos, bezierCurve, bezierRotation, bezierAmount);

                case ApelRenderer.Cone(
                        Vector3f drawPos, float height, float radius, Vector3f coneRotation, int coneAmount
                ) -> renderer.drawCone(particleEffect, 0, drawPos, height, radius, coneRotation, coneAmount);

                case ApelRenderer.Cylinder(
                        Vector3f drawPos, float radius, float height, Vector3f cylinderRotation, int cylinderAmount
                ) -> renderer.drawCylinder(particleEffect, 0, drawPos, radius, height, cylinderRotation, cylinderAmount);
            }
            // Only increment after drawing, since some case(s) return early
            this.instructionNumber++;
        }
    }

    public static class Builder<B extends Builder<B>> extends ParticleObject.Builder<B, ParticleRecording> {
        private static final int READ_CHUNK_SIZE = 256;

        private final ServerWorld serverWorld;
        private final List<ApelRenderer.Instruction> instructions;

        protected File file;

        private Builder(ServerWorld serverWorld) {
            this.serverWorld = serverWorld;
            this.instructions = new ArrayList<>();
        }

        public B filename(String filename) {
            return this.file(new File(filename));
        }

        public B file(File file) {
            this.file = file;
            return self();
        }

        @Override
        public ParticleRecording build() {
            InputStream inputStream;
            try {
                inputStream = new FileInputStream(this.file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            RegistryByteBuf buffer = new RegistryByteBuf(Unpooled.buffer(READ_CHUNK_SIZE), serverWorld.getRegistryManager());
            readBytes(buffer, inputStream);
            while (buffer.readableBytes() > 0) {
                this.instructions.add(ApelRenderer.Instruction.from(buffer));

                // This number needs to be larger than the largest possible Instruction
                if (buffer.readableBytes() < 256) {
                    buffer.discardReadBytes();
                    readBytes(buffer, inputStream);
                }
            }
            return new ParticleRecording(this);
        }

        private void readBytes(RegistryByteBuf buffer, InputStream inputStream) {
            try {
                buffer.writeBytes(inputStream, buffer.writableBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
