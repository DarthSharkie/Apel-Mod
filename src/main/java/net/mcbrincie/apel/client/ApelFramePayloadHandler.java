package net.mcbrincie.apel.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.mcbrincie.apel.lib.renderers.ApelFramePayload;
import net.mcbrincie.apel.lib.renderers.ApelNetworkRenderer;
import net.mcbrincie.apel.lib.util.math.bezier.BezierCurve;
import net.mcbrincie.apel.lib.util.math.bezier.CubicBezierCurve;
import net.mcbrincie.apel.lib.util.math.bezier.LinearBezierCurve;
import net.mcbrincie.apel.lib.util.math.bezier.ParameterizedBezierCurve;
import net.mcbrincie.apel.lib.util.math.bezier.QuadraticBezierCurve;
import net.minecraft.particle.ParticleEffect;
import org.joml.Vector3f;

import java.util.List;

public class ApelFramePayloadHandler implements ClientPlayNetworking.PlayPayloadHandler<ApelFramePayload> {

    private final ParticleManagerRenderer renderer;

    public ApelFramePayloadHandler(ParticleManagerRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void receive(ApelFramePayload payload, ClientPlayNetworking.Context context) {
        // TODO: Can this be done once instead of every single payload?
        renderer.setParticleManager(context.client().particleManager);

        context.client().execute(() -> {

            Vector3f frameOrigin = new Vector3f(0);
            ParticleEffect particleEffect = null;
            for (ApelNetworkRenderer.Instruction ins : payload.instructions()) {
                switch (ins) {
                    case ApelNetworkRenderer.Frame(Vector3f origin) -> frameOrigin = origin;

                    case ApelNetworkRenderer.PType(ParticleEffect pe) -> particleEffect = pe;

                    case ApelNetworkRenderer.Particle(Vector3f pos) -> renderer.drawParticle(particleEffect, 0, pos);

                    case ApelNetworkRenderer.Line(Vector3f start, Vector3f end, int amount) -> renderer.drawLine(particleEffect, 0, start, end, amount);

                    case ApelNetworkRenderer.Ellipse(
                            Vector3f center, float radius, float stretch, Vector3f rotation, int amount
                    ) -> renderer.drawEllipse(particleEffect, 0, center, radius, stretch, rotation, amount);

                    case ApelNetworkRenderer.Ellipsoid(
                            Vector3f drawPos, float radius, float stretch1, float stretch2, Vector3f rotation,
                            int amount
                    ) -> renderer.drawEllipsoid(particleEffect, 0, drawPos, radius, stretch1, stretch2, rotation, amount);

                    case ApelNetworkRenderer.BezierCurve(
                            Vector3f drawPos, Vector3f start, List<Vector3f> controlPoints, Vector3f end,
                            Vector3f rotation, int amount
                    ) -> {
                        BezierCurve bezierCurve = switch(controlPoints.size()) {
                            case 0 -> new LinearBezierCurve(start, end);
                            case 1 -> new QuadraticBezierCurve(start, end, controlPoints.get(0));
                            case 2 -> new CubicBezierCurve(start, end, controlPoints.get(0), controlPoints.get(1));
                            default -> new ParameterizedBezierCurve(start, end, controlPoints);
                        };

                        renderer.drawBezier(particleEffect, 0, drawPos, bezierCurve, rotation, amount);
                    }
                }
            }
        });
    }
}
