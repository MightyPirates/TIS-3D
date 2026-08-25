/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.mixin.fabric;

import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.block.entity.fabric.ChunkUnloadListener;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CasingBlockEntity.class)
public abstract class MixinCasingBlockEntity extends BlockEntity implements ChunkUnloadListener {
    private MixinCasingBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    @Unique
    @SuppressWarnings("DataFlowIssue")
    private CasingBlockEntity tis3d$asCasingBlockEntity() {
        return (CasingBlockEntity) (Object) this;
    }

    @Override
    public void onChunkUnloaded() {
        tis3d$asCasingBlockEntity().dispose();
    }
}
