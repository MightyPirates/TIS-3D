package li.cil.tis3d.common.init;

import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.block.CasingBlock;
import li.cil.tis3d.common.block.ControllerBlock;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.block.entity.ControllerBlockEntity;
import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

/**
 * Manages setup, registration and lookup of blocks.
 */
public final class Blocks {
    public static final CasingBlock CASING = new CasingBlock(FabricBlockSettings.of(Material.METAL).strength(5, 10).build());
    public static final ControllerBlock CONTROLLER = new ControllerBlock(FabricBlockSettings.of(Material.METAL).strength(5, 10).build());

    // --------------------------------------------------------------------- //

    static void registerBlocks() {
        Registry.register(Registry.BLOCK, Constants.NAME_BLOCK_CASING, CASING);
        Registry.register(Registry.BLOCK, Constants.NAME_BLOCK_CONTROLLER, CONTROLLER);
    }

    static void registerBlockEntityTypes() {
        CasingBlockEntity.TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, Constants.NAME_BLOCK_CASING, BlockEntityType.Builder.create(CasingBlockEntity::new, CASING).build(null));
        ControllerBlockEntity.TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, Constants.NAME_BLOCK_CONTROLLER, BlockEntityType.Builder.create(ControllerBlockEntity::new, CONTROLLER).build(null));
    }

    // --------------------------------------------------------------------- //

    private Blocks() {
    }
}
