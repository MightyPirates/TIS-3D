/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.client.renderer.block.fabric;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import li.cil.tis3d.api.API;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

public record ModuleUnbakedModel(Direction face,
                                 BlockStateModel.Unbaked proxy) implements CustomUnbakedBlockStateModel {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(API.MOD_ID, "casing_module");

    public static final MapCodec<ModuleUnbakedModel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Direction.CODEC.fieldOf("face").forGetter(ModuleUnbakedModel::face),
        BlockStateModel.Unbaked.CODEC.fieldOf("proxy").forGetter(ModuleUnbakedModel::proxy)
    ).apply(instance, ModuleUnbakedModel::new));

    // --------------------------------------------------------------------- //
    // CustomUnbakedBlockStateModel

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return CODEC;
    }

    @Override
    public BlockStateModel bake(final ModelBaker baker) {
        return new ModuleBakedModel(proxy.bake(baker), face);
    }

    @Override
    public void resolveDependencies(final Resolver resolver) {
        proxy.resolveDependencies(resolver);
    }
}
