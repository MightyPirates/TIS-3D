/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.mixin.neoforge;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.traits.neoforge.ModuleWithBakedModelNeoForge;
import li.cil.tis3d.client.renderer.block.neoforge.ModuleBakedModel;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.machine.CasingImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CasingBlockEntity.class)
public abstract class MixinCasingBlockEntity extends BlockEntity {
    @Shadow(remap = false) @Final
    private CasingImpl casing;

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
        super.onChunkUnloaded();
        tis3d$asCasingBlockEntity().dispose();
    }

    @Override
    public ModelData getModelData() {
        final ModelData modelData = super.getModelData();
        if (level == null) {
            return modelData;
        }

        final ModuleBakedModel.CasingModules data = new ModuleBakedModel.CasingModules();
        for (final Face face : Face.VALUES) {
            final Module module = casing.getModule(face);
            if (module instanceof final ModuleWithBakedModelNeoForge moduleWithModel) {
                if (moduleWithModel.hasModel()) {
                    data.setModule(casing.toWorld(face), moduleWithModel,
                        moduleWithModel.getModelData(level, getBlockPos(), getBlockState(), modelData));
                }
            }
        }

        if (!data.isEmpty()) {
            return ModelData.builder()
                .with(ModuleBakedModel.CasingModules.CASING_MODULES_PROPERTY, data)
                .build();
        }

        return modelData;
    }
}
