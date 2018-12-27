package li.cil.tis3d.client.mixin;

import li.cil.tis3d.common.block.CasingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MinecraftClient.class)
public abstract class PickModuleMixin {
    @Shadow
    public HitResult hitResult;

    @Shadow
    public ClientWorld world;

    @ModifyVariable(method = "doItemPick", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 0))
    private ItemStack pickModule(final ItemStack stack) {
        assert hitResult.type == HitResult.Type.BLOCK;
        final BlockPos blockPos = hitResult.getBlockPos();
        final BlockState blockState = world.getBlockState(blockPos);
        final Block block = blockState.getBlock();
        if (block instanceof CasingBlock) {
            return ((CasingBlock)block).getPickStack(world, blockPos, hitResult.side, blockState);
        } else {
            return stack;
        }
    }
}
