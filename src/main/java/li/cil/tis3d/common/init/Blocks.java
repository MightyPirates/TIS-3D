package li.cil.tis3d.common.init;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.block.BlockCasing;
import li.cil.tis3d.common.block.BlockController;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import li.cil.tis3d.common.tileentity.TileEntityController;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import org.dimdev.rift.listener.BlockAdder;
import org.dimdev.rift.listener.TileEntityTypeAdder;

import java.util.Arrays;
import java.util.List;

/**
 * Manages setup, registration and lookup of blocks.
 */
public final class Blocks implements BlockAdder, TileEntityTypeAdder {
    public static BlockCasing casing = null;
    public static BlockController controller = null;

    public static List<Block> getAllBlocks() {
        return Arrays.asList(
                casing,
                controller
        );
    }

    // --------------------------------------------------------------------- //

    @Override
    public void registerBlocks() {
        Block.registerBlock(Constants.NAME_BLOCK_CASING, casing = new BlockCasing(Block.Builder.create(Material.IRON)
            .hardnessAndResistance(5, 10)));
        Block.registerBlock(Constants.NAME_BLOCK_CONTROLLER, controller = new BlockController(Block.Builder.create(Material.IRON)
                .hardnessAndResistance(5, 10)));
    }

    @Override
    public void registerTileEntityTypes() {
        TileEntityCasing.TYPE = TileEntityType.registerTileEntityType(Constants.NAME_BLOCK_CASING.toString(), TileEntityType.Builder.create(TileEntityCasing::new));
        TileEntityController.TYPE = TileEntityType.registerTileEntityType(Constants.NAME_BLOCK_CONTROLLER.toString(), TileEntityType.Builder.create(TileEntityController::new));
    }

    // --------------------------------------------------------------------- //

    // --------------------------------------------------------------------- //

}
