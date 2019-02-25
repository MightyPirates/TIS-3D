package li.cil.tis3d.common.mixin;

import li.cil.tis3d.common.block.entity.AbstractComputerBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
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
    private List<BlockEntity> field_18139;

    @Shadow
    @Final
    private boolean isClient;

    @Inject(method = "method_18471", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", args = {"ldc=blockEntities"}))
    private void onBlockEntityChunkUnload(final CallbackInfo ci) {
        if(!isClient) {
            if (!field_18139.isEmpty()) {
                for (final BlockEntity blockEntity : field_18139) {
                    if (blockEntity instanceof AbstractComputerBlockEntity) {
                        ((AbstractComputerBlockEntity) blockEntity).onChunkUnload();
                    }
                }
            }
        }
    }
}
