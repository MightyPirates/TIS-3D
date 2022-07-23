package li.cil.tis3d.common.block.entity;

import li.cil.tis3d.common.block.Blocks;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class BlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = RegistryUtils.getInitializerFor(ForgeRegistries.BLOCK_ENTITY_TYPES);

    // --------------------------------------------------------------------- //

    public static final RegistryObject<BlockEntityType<CasingBlockEntity>> CASING = register(Blocks.CASING, CasingBlockEntity::new);
    public static final RegistryObject<BlockEntityType<ControllerBlockEntity>> CONTROLLER = register(Blocks.CONTROLLER, ControllerBlockEntity::new);

    // --------------------------------------------------------------------- //

    public static void initialize() {
    }

    // --------------------------------------------------------------------- //

    @SuppressWarnings("ConstantConditions") // .build(null) is fine
    private static <B extends Block, T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(final RegistryObject<B> block, final BlockEntityType.BlockEntitySupplier<T> factory) {
        return BLOCK_ENTITY_TYPES.register(block.getId().getPath(), () -> BlockEntityType.Builder.of(factory, block.get()).build(null));
    }
}
