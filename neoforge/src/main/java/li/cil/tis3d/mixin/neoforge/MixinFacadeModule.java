/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.mixin.neoforge;

import li.cil.tis3d.api.module.traits.neoforge.ModuleWithBakedModelNeoForge;
import li.cil.tis3d.common.module.FacadeModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(FacadeModule.class)
public abstract class MixinFacadeModule implements ModuleWithBakedModelNeoForge {
    @Shadow(remap = false)
    private BlockState facadeState;

    @Override
    @SuppressWarnings("deprecation")
    public void collectParts(final BlockAndTintGetter level, final BlockPos pos, final BlockState state, final Direction direction, final RandomSource random, final List<BlockModelPart> parts) {
        final var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(facadeState);

        final List<BlockModelPart> facadeParts = new ArrayList<>();
        model.collectParts(level, pos, facadeState, random, facadeParts);

        for (final BlockModelPart part : facadeParts) {
            final List<BakedQuad> quads = part.getQuads(direction);
            if (!quads.isEmpty()) {
                parts.add(new SingleFacePart(quads, direction, part.useAmbientOcclusion(), part.particleIcon(),
                    ItemBlockRenderTypes.getChunkRenderType(facadeState)));
            }
        }
    }

    @SuppressWarnings("deprecation")
    private record SingleFacePart(List<BakedQuad> quads, Direction direction, boolean useAmbientOcclusion,
                                  TextureAtlasSprite particleIcon,
                                  ChunkSectionLayer renderLayer) implements BlockModelPart {
        @Override
        public List<BakedQuad> getQuads(@Nullable final Direction side) {
            return side == direction ? quads : List.of();
        }

        @Override
        public ChunkSectionLayer getRenderType(final BlockState state) {
            return renderLayer;
        }
    }
}
