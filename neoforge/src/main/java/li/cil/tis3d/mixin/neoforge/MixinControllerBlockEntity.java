/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.mixin.neoforge;

import li.cil.tis3d.common.block.entity.ControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ControllerBlockEntity.class)
public abstract class MixinControllerBlockEntity extends BlockEntity {
    private MixinControllerBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    @Unique
    @SuppressWarnings("DataFlowIssue")
    private ControllerBlockEntity tis3d$asControllerBlockEntity() {
        return (ControllerBlockEntity) (Object) this;
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        tis3d$asControllerBlockEntity().dispose();
    }
}
