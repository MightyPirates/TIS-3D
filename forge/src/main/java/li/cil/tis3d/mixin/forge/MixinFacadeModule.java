package li.cil.tis3d.mixin.forge;

import li.cil.tis3d.api.module.traits.forge.ModuleWithBakedModelForge;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.module.FacadeModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(FacadeModule.class)
public abstract class MixinFacadeModule implements ModuleWithBakedModelForge {
    @Shadow(remap = false)
    private BlockState facadeState;

    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "onData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;sendBlockUpdated(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;I)V"))
    public void updateModelData(final CompoundTag data, final CallbackInfo ci) {
        if (((FacadeModule) (Object) this).getCasing() instanceof final CasingBlockEntity casing) {
            casing.requestModelDataUpdate();
        }
    }

    @Override
    public ModelData getModelData(final BlockAndTintGetter level, final BlockPos pos, final BlockState state, final ModelData data) {
        final var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(facadeState);
        return model.getModelData(level, pos, facadeState, data);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction face, final RandomSource random, final ModelData data, final @Nullable RenderType renderType) {
        final var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(facadeState);
        return model.getQuads(facadeState, face, random, data, renderType);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(final RandomSource random, final ModelData data) {
        final var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(facadeState);
        return model.getRenderTypes(facadeState, random, data);
    }
}
