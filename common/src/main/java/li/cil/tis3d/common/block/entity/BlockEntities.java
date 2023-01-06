package li.cil.tis3d.common.block.entity;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import li.cil.tis3d.common.block.Blocks;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = RegistryUtils.get(Registry.BLOCK_ENTITY_TYPE_REGISTRY);

    // --------------------------------------------------------------------- //

    public static final RegistrySupplier<BlockEntityType<CasingBlockEntity>> CASING = register(Blocks.CASING, CasingBlockEntity::new);
    public static final RegistrySupplier<BlockEntityType<ControllerBlockEntity>> CONTROLLER = register(Blocks.CONTROLLER, ControllerBlockEntity::new);

    // --------------------------------------------------------------------- //

    public static void initialize() {
        BLOCK_ENTITY_TYPES.register();
    }

    // --------------------------------------------------------------------- //

    @SuppressWarnings("ConstantConditions") // .build(null) is fine
    private static <B extends Block, T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> register(final RegistrySupplier<B> block, final BlockEntityType.BlockEntitySupplier<T> factory) {
        return BLOCK_ENTITY_TYPES.register(block.getId().getPath(), () -> BlockEntityType.Builder.of(factory, block.get()).build(null));
    }
}
