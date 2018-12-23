package li.cil.tis3d.common.init;

import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.block.BlockCasing;
import li.cil.tis3d.common.block.BlockController;
import li.cil.tis3d.common.block.entity.TileEntityCasing;
import li.cil.tis3d.common.block.entity.TileEntityController;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.List;

/**
 * Manages setup, registration and lookup of blocks.
 */
public final class Blocks implements ModInitializer {
    public static BlockCasing casing = new BlockCasing(FabricBlockSettings.of(Material.METAL)
        .strength(5, 10).build());
    public static BlockController controller = new BlockController(FabricBlockSettings.of(Material.METAL)
        .strength(5, 10).build());

    public static List<Block> getAllBlocks() {
        return Arrays.asList(
            casing,
            controller
        );
    }

    // --------------------------------------------------------------------- //

    public void registerBlocks() {
        Registry.BLOCK.register(Constants.NAME_BLOCK_CASING, casing);
        Registry.BLOCK.register(Constants.NAME_BLOCK_CONTROLLER, controller);
    }

    public void registerTileEntityTypes() {
        TileEntityCasing.TYPE = Registry.register(Registry.BLOCK_ENTITY, Constants.NAME_BLOCK_CASING.toString(), BlockEntityType.Builder.create(TileEntityCasing::new).method_11034(null));
        TileEntityController.TYPE = Registry.register(Registry.BLOCK_ENTITY, Constants.NAME_BLOCK_CONTROLLER.toString(), BlockEntityType.Builder.create(TileEntityController::new).method_11034(null));
    }

    @Override
    public void onInitialize() {
        registerBlocks();
        registerTileEntityTypes();
    }
}
