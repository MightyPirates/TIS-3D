/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.client.renderer.block.neoforge;

import li.cil.tis3d.api.module.traits.neoforge.ModuleWithBakedModelNeoForge;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleBakedModel implements IDynamicBakedModel {
    private static final Direction[] DIRECTIONS = Direction.values();

    private final BakedModel proxy;

    // --------------------------------------------------------------------- //

    ModuleBakedModel(final BakedModel proxy) {
        this.proxy = proxy;
    }

    // --------------------------------------------------------------------- //
    // IBakedModel

    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction side, final RandomSource random, final ModelData data, @Nullable final RenderType renderType) {
        final CasingModules modules = data.get(CasingModules.CASING_MODULES_PROPERTY);
        if (side != null) {
            if (modules != null) {
                final ModuleWithBakedModelNeoForge module = modules.getModule(side);
                if (module != null && module.hasModel()) {
                    final ModelData moduleData = modules.getModuleData(side);
                    return module.getQuads(state, side, random, moduleData, renderType);
                }
            }

            if (renderType != null && renderType.equals(RenderType.solid())) {
                return proxy.getQuads(state, side, random, data, renderType);
            } else {
                return Collections.emptyList();
            }
        } else {
            final ArrayList<BakedQuad> quads = new ArrayList<>();

            if (modules != null) {
                for (final Direction moduleSide : DIRECTIONS) {
                    final ModuleWithBakedModelNeoForge module = modules.getModule(moduleSide);
                    if (module != null && module.hasModel()) {
                        final ModelData moduleData = modules.getModuleData(moduleSide);
                        quads.addAll(module.getQuads(state, null, random, moduleData, renderType));
                    }
                }
            }

            if (renderType != null && renderType.equals(RenderType.solid())) {
                quads.addAll(proxy.getQuads(state, null, random, data, renderType));
            }

            return quads;
        }
    }

    @Override
    public boolean useAmbientOcclusion() {
        return proxy.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return proxy.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return proxy.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return proxy.isCustomRenderer();
    }

    @SuppressWarnings("deprecation")
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return proxy.getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides() {
        return proxy.getOverrides();
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(final BlockState state, final RandomSource random, final ModelData data) {
        ChunkRenderTypeSet set = proxy.getRenderTypes(state, random, data);
        final CasingModules modules = data.get(CasingModules.CASING_MODULES_PROPERTY);
        if (modules != null) {
            for (final Direction side : DIRECTIONS) {
                final ModuleWithBakedModelNeoForge module = modules.getModule(side);
                if (module != null && module.hasModel()) {
                    final ModelData moduleData = modules.getModuleData(side);
                    set = ChunkRenderTypeSet.union(set, module.getRenderTypes(random, moduleData));
                }
            }
        }
        return set;
    }

    // --------------------------------------------------------------------- //

    public static final class CasingModules {
        public static final ModelProperty<CasingModules> CASING_MODULES_PROPERTY = new ModelProperty<>();

        private final ModuleWithBakedModelNeoForge[] modules = new ModuleWithBakedModelNeoForge[DIRECTIONS.length];
        private final ModelData[] moduleData = new ModelData[DIRECTIONS.length];

        public boolean isEmpty() {
            for (final ModuleWithBakedModelNeoForge module : modules) {
                if (module != null) {
                    return false;
                }
            }

            return true;
        }

        public void setModule(final Direction side, final ModuleWithBakedModelNeoForge module, final ModelData data) {
            modules[side.ordinal()] = module;
            moduleData[side.ordinal()] = data;
        }

        @Nullable
        public ModuleWithBakedModelNeoForge getModule(final Direction side) {
            return modules[side.ordinal()];
        }

        public ModelData getModuleData(final Direction side) {
            return moduleData[side.ordinal()];
        }
    }
}
