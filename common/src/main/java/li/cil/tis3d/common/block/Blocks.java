/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.common.block;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;

public final class Blocks {
    private static final DeferredRegister<Block> BLOCKS = RegistryUtils.get(Registries.BLOCK);

    // --------------------------------------------------------------------- //

    public static final RegistrySupplier<CasingBlock> CASING = BLOCKS.register("casing", () -> new CasingBlock(Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(1.5f, 6f)));
    public static final RegistrySupplier<ControllerBlock> CONTROLLER = BLOCKS.register("controller", () -> new ControllerBlock(Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(1.5f, 6f)));

    // --------------------------------------------------------------------- //

    public static void initialize() {
        BLOCKS.register();
    }
}
