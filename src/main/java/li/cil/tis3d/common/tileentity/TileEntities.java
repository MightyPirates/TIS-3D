package li.cil.tis3d.common.tileentity;

import li.cil.tis3d.common.block.Blocks;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class TileEntities {
    private static final DeferredRegister<BlockEntityType<?>> TILES = RegistryUtils.getInitializerFor(ForgeRegistries.BLOCK_ENTITIES);

    // --------------------------------------------------------------------- //

    public static final RegistryObject<BlockEntityType<CasingTileEntity>> CASING = register(Blocks.CASING, CasingTileEntity::new);
    public static final RegistryObject<BlockEntityType<ControllerTileEntity>> CONTROLLER = register(Blocks.CONTROLLER, ControllerTileEntity::new);

    // --------------------------------------------------------------------- //

    public static void initialize() {
    }

    // --------------------------------------------------------------------- //

    @SuppressWarnings("ConstantConditions") // .build(null) is fine
    private static <B extends Block, T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(final RegistryObject<B> block, final BlockEntityType.BlockEntitySupplier<T> factory) {
        return TILES.register(block.getId().getPath(), () -> BlockEntityType.Builder.of(factory, block.get()).build(null));
    }
}
