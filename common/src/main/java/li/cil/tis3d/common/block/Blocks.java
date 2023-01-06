package li.cil.tis3d.common.block;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;

public final class Blocks {
    private static final DeferredRegister<Block> BLOCKS = RegistryUtils.get(Registry.BLOCK_REGISTRY);

    // --------------------------------------------------------------------- //

    public static final RegistrySupplier<CasingBlock> CASING = BLOCKS.register("casing", CasingBlock::new);
    public static final RegistrySupplier<ControllerBlock> CONTROLLER = BLOCKS.register("controller", ControllerBlock::new);

    // --------------------------------------------------------------------- //

    public static void initialize() {
        BLOCKS.register();
    }
}
