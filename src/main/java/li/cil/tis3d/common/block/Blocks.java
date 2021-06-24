package li.cil.tis3d.common.block;

import li.cil.tis3d.api.API;
import net.minecraft.block.Block;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class Blocks {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, API.MOD_ID);

    // --------------------------------------------------------------------- //

    public static final RegistryObject<BlockCasing> CASING = BLOCKS.register("casing", BlockCasing::new);
    public static final RegistryObject<BlockController> CONTROLLER = BLOCKS.register("controller", BlockController::new);

    // --------------------------------------------------------------------- //

    public static void initialize() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
