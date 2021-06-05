package li.cil.tis3d.common.mixin;

import li.cil.tis3d.common.block.entity.AbstractComputerBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(World.class)
public abstract class ChunkUnloadMixin {
    @Shadow
    @Final
    protected List<BlockEntityTickInvoker> blockEntityTickers;

    @Inject(method = "tickBlockEntities", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", args = {"ldc=blockEntities"}))
    private void onBlockEntityChunkUnload(final CallbackInfo ci) {
        if (!blockEntityTickers.isEmpty()) {
            for (final BlockEntityTickInvoker blockEntity : blockEntityTickers) {
                if (blockEntity instanceof AbstractComputerBlockEntity) {
                    ((AbstractComputerBlockEntity)blockEntity).onChunkUnload();
                }
            }
        }
    }
}
