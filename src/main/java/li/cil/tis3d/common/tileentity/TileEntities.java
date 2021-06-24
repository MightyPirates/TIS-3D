package li.cil.tis3d.common.tileentity;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public final class TileEntities {
    private static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, API.MOD_ID);

    // --------------------------------------------------------------------- //

    public static final RegistryObject<TileEntityType<TileEntityCasing>> CASING = register(Blocks.CASING, TileEntityCasing::new);
    public static final RegistryObject<TileEntityType<TileEntityController>> CONTROLLER = register(Blocks.CONTROLLER, TileEntityController::new);

    // --------------------------------------------------------------------- //

    public static void initialize() {
        TileEntities.TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    // --------------------------------------------------------------------- //

    @SuppressWarnings("ConstantConditions") // .build(null) is fine
    private static <B extends Block, T extends TileEntity> RegistryObject<TileEntityType<T>> register(final RegistryObject<B> block, final Supplier<T> factory) {
        return TILES.register(block.getId().getPath(), () -> TileEntityType.Builder.of(factory, block.get()).build(null));
    }
}
