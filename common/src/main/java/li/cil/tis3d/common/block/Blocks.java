/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.common.block;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import li.cil.tis3d.api.API;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Function;

public final class Blocks {
    private static final DeferredRegister<Block> BLOCKS = RegistryUtils.get(Registries.BLOCK);

    // --------------------------------------------------------------------- //

    public static final RegistrySupplier<CasingBlock> CASING = register("casing", CasingBlock::new);
    public static final RegistrySupplier<ControllerBlock> CONTROLLER = register("controller", ControllerBlock::new);

    // --------------------------------------------------------------------- //

    public static void initialize() {
        BLOCKS.register();
    }

    // --------------------------------------------------------------------- //

    private static <T extends Block> RegistrySupplier<T> register(final String name, final Function<Properties, T> factory) {
        final ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK,
            Identifier.fromNamespaceAndPath(API.MOD_ID, name));
        return BLOCKS.register(name, () -> factory.apply(createProperties().setId(key)));
    }

    private static Properties createProperties() {
        return Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(1.5f, 6f);
    }
}
