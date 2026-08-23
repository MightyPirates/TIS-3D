/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.mixin.neoforge;

import li.cil.tis3d.api.module.traits.neoforge.ModuleWithBakedModelNeoForge;
import li.cil.tis3d.common.module.FacadeModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(FacadeModule.class)
public abstract class MixinFacadeModule implements ModuleWithBakedModelNeoForge {
    @Shadow(remap = false)
    private BlockState facadeState;

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
