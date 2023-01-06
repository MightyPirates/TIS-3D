package li.cil.tis3d.mixin.forge;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.traits.forge.ModuleWithBakedModelForge;
import li.cil.tis3d.client.renderer.block.forge.ModuleBakedModel;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.machine.CasingImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mixin(CasingBlockEntity.class)
public abstract class MixinCasingBlockEntity extends BlockEntity {
    @Shadow(remap = false)
    private CasingImpl casing;

    private MixinCasingBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    @SuppressWarnings("DataFlowIssue")
    private CasingBlockEntity asCasingBlockEntity() {
        return (CasingBlockEntity) (Object) this;
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        asCasingBlockEntity().dispose();
    }

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(final Capability<T> capability, @Nullable final Direction facing) {
        final LazyOptional<T> instance = super.getCapability(capability, facing);
        if (instance.isPresent()) {
            return instance;
        }

        if (facing == null) {
            return LazyOptional.empty();
        }

        final Module module = asCasingBlockEntity().getModule(Face.fromDirection(facing));
        if (module instanceof final ICapabilityProvider capabilityProvider) {
            return capabilityProvider.getCapability(capability, facing);
        }

        return LazyOptional.empty();
    }

    @Override
    public @NotNull ModelData getModelData() {
        final ModelData modelData = super.getModelData();
        if (level == null) {
            return modelData;
        }

        final ModuleBakedModel.CasingModules data = new ModuleBakedModel.CasingModules();
        for (final Face face : Face.VALUES) {
            final Module module = casing.getModule(face);
            if (module instanceof final ModuleWithBakedModelForge moduleWithModel) {
                if (moduleWithModel.hasModel()) {
                    data.setModule(face, moduleWithModel, moduleWithModel.getModelData(level, getBlockPos(), getBlockState(), modelData));
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
