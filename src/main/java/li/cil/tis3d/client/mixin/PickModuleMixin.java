package li.cil.tis3d.client.mixin;

import li.cil.tis3d.common.block.CasingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftClient.class)
public abstract class PickModuleMixin {
    @Shadow
    public HitResult hitResult;

    @Redirect(method = "doItemPick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getPickStack(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack pickModule(final Block block, final BlockView world, final BlockPos pos, final BlockState state) {
        if (block instanceof CasingBlock) {
            return ((CasingBlock) block).getPickStack(world, pos, hitResult.side, state);
        } else {
            return block.getPickStack(world, pos, state);
        }
    }
}
