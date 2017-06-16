package li.cil.tis3d.common.init;

import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.ProxyCommon;
import li.cil.tis3d.common.block.BlockCasing;
import li.cil.tis3d.common.block.BlockController;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import li.cil.tis3d.common.tileentity.TileEntityController;
import net.minecraft.block.Block;

/**
 * Manages setup, registration and lookup of blocks.
 */
public final class Blocks {
    public static Block casing;
    public static Block controller;

    public static void registerBlocks(final ProxyCommon proxy) {
        casing = proxy.registerBlock(Constants.NAME_BLOCK_CASING, BlockCasing::new, TileEntityCasing.class);
        controller = proxy.registerBlock(Constants.NAME_BLOCK_CONTROLLER, BlockController::new, TileEntityController.class);
    }

    private Blocks() {
    }
}
