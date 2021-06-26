package li.cil.tis3d.common.block;

import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.block.Block;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class Blocks {
    private static final DeferredRegister<Block> BLOCKS = RegistryUtils.create(ForgeRegistries.BLOCKS);

    // --------------------------------------------------------------------- //

    public static final RegistryObject<BlockCasing> CASING = BLOCKS.register("casing", BlockCasing::new);
    public static final RegistryObject<BlockController> CONTROLLER = BLOCKS.register("controller", BlockController::new);

    // --------------------------------------------------------------------- //

    public static void initialize() {
    }
}
