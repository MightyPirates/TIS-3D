package li.cil.tis3d.client.renderer.block;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.traits.ModuleWithBakedModel;
import li.cil.tis3d.common.tileentity.CasingTileEntity;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class ModuleBakedModel implements IDynamicBakedModel {
    private final BakedModel proxy;

    // --------------------------------------------------------------------- //

    ModuleBakedModel(final BakedModel proxy) {
        this.proxy = proxy;
    }

    // --------------------------------------------------------------------- //
    // IBakedModel

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction side, @Nonnull final Random random, @Nonnull final IModelData extraData) {
        final ArrayList<BakedQuad> quads = new ArrayList<>();

        final CasingModules modules = extraData.getData(CasingModules.CASING_MODULES_PROPERTY);
        if (modules != null) {
            if (side != null) {
                final ModuleWithBakedModel module = modules.getModule(Face.fromDirection(side));
                if (module != null) {
                    quads.addAll(module.getQuads(state, side, random));
                }
            } else {
                for (final Face face : Face.VALUES) {
                    final ModuleWithBakedModel module = modules.getModule(face);
                    if (module != null) {
                        quads.addAll(module.getQuads(state, null, random));
                    }
                }
            }
        }

        if (quads.isEmpty()) {
            return proxy.getQuads(state, side, random, extraData);
        }

        return quads;
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

    @Nonnull
    @Override
    public IModelData getModelData(@Nonnull final BlockAndTintGetter world, @Nonnull final BlockPos pos, @Nonnull final BlockState state, @Nonnull final IModelData tileData) {
        final BlockEntity tileEntity = world.getBlockEntity(pos);
        if (!(tileEntity instanceof final CasingTileEntity casing)) {
            return tileData;
        }

        final CasingModules data = new CasingModules();
        for (final Face face : Face.VALUES) {
            final Module module = casing.getModule(face);
            if (module instanceof ModuleWithBakedModel) {
                data.setModule(face, (ModuleWithBakedModel) module);
            }
        }

        if (!data.isEmpty()) {
            return new ModelDataMap.Builder()
                .withInitial(CasingModules.CASING_MODULES_PROPERTY, data)
                .build();
        }

        return tileData;
    }

    // --------------------------------------------------------------------- //

    private static final class CasingModules {
        public static final ModelProperty<CasingModules> CASING_MODULES_PROPERTY = new ModelProperty<>();

        private final ModuleWithBakedModel[] modules = new ModuleWithBakedModel[Face.VALUES.length];

        public boolean isEmpty() {
            for (final ModuleWithBakedModel module : modules) {
                if (module != null) {
                    return false;
                }
            }

            return true;
        }

        public void setModule(final Face face, final ModuleWithBakedModel module) {
            modules[face.ordinal()] = module;
        }

        @Nullable
        public ModuleWithBakedModel getModule(final Face face) {
            return modules[face.ordinal()];
        }
    }
}
