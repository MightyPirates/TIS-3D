package li.cil.tis3d.common.mixin;

import li.cil.tis3d.common.block.entity.TileEntityComputer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;
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
    private List<BlockEntity> unloadedBlockEntities;

    @Inject(method = "updateEntities", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/Profiler;endBegin(Ljava/lang/String;)V", args = {"ldc=blockEntities"}))
    private void onBlockEntityChunkUnload(CallbackInfo ci) {
        if (!unloadedBlockEntities.isEmpty()) {
            for (BlockEntity blockEntity : unloadedBlockEntities) {
                if (blockEntity instanceof TileEntityComputer) {
                    ((TileEntityComputer) blockEntity).onChunkUnload();
                }
            }
        }
    }
}
