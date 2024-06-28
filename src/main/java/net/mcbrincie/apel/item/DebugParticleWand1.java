package net.mcbrincie.apel.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class DebugParticleWand1 extends Item {
    public DebugParticleWand1(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) return TypedActionResult.pass(user.getMainHandStack());
        animators((ServerWorld) world, user);
        return TypedActionResult.pass(user.getMainHandStack());
    }

    // When a user right-clicks the wand, this gets triggered
    private void animators(ServerWorld world, PlayerEntity user) {
    }
}
