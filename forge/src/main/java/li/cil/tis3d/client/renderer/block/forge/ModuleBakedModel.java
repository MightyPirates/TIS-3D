package li.cil.tis3d.client.renderer.block.forge;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.traits.forge.ModuleWithBakedModelForge;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleBakedModel implements IDynamicBakedModel {
    private final BakedModel proxy;

    // --------------------------------------------------------------------- //

    ModuleBakedModel(final BakedModel proxy) {
        this.proxy = proxy;
    }

    // --------------------------------------------------------------------- //
    // IBakedModel

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction side, final RandomSource random, final ModelData data, @Nullable final RenderType renderType) {
        final CasingModules modules = data.get(CasingModules.CASING_MODULES_PROPERTY);
        if (side != null) {
            if (modules != null) {
                final Face face = Face.fromDirection(side);
                final ModuleWithBakedModelForge module = modules.getModule(face);
                if (module != null && module.hasModel()) {
                    final ModelData moduleData = modules.getModuleData(face);
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
                for (final Face face : Face.VALUES) {
                    final ModuleWithBakedModelForge module = modules.getModule(face);
                    if (module != null && module.hasModel()) {
                        final ModelData moduleData = modules.getModuleData(face);
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
    public ChunkRenderTypeSet getRenderTypes(@NotNull final BlockState state, @NotNull final RandomSource random, @NotNull final ModelData data) {
        ChunkRenderTypeSet set = proxy.getRenderTypes(state, random, data);
        final CasingModules modules = data.get(CasingModules.CASING_MODULES_PROPERTY);
        if (modules != null) {
            for (final Face face : Face.VALUES) {
                final ModuleWithBakedModelForge module = modules.getModule(face);
                if (module != null && module.hasModel()) {
                    final ModelData moduleData = modules.getModuleData(face);
                    set = ChunkRenderTypeSet.union(set, module.getRenderTypes(random, moduleData));
                }
            }
        }
        return set;
    }

    // --------------------------------------------------------------------- //

    public static final class CasingModules {
        public static final ModelProperty<CasingModules> CASING_MODULES_PROPERTY = new ModelProperty<>();

        private final ModuleWithBakedModelForge[] modules = new ModuleWithBakedModelForge[Face.VALUES.length];
        private final ModelData[] moduleData = new ModelData[Face.VALUES.length];

        public boolean isEmpty() {
            for (final ModuleWithBakedModelForge module : modules) {
                if (module != null) {
                    return false;
                }
            }

            return true;
        }

        public void setModule(final Face face, final ModuleWithBakedModelForge module, final ModelData data) {
            modules[face.ordinal()] = module;
            moduleData[face.ordinal()] = data;
        }

        @Nullable
        public ModuleWithBakedModelForge getModule(final Face face) {
            return modules[face.ordinal()];
        }

        public ModelData getModuleData(final Face face) {
            return moduleData[face.ordinal()];
        }
    }
}
