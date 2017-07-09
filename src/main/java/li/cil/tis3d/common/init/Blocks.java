package li.cil.tis3d.common.init;

import cpw.mods.fml.common.registry.GameRegistry;
import li.cil.tis3d.api.API;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.ProxyCommon;
import li.cil.tis3d.common.block.BlockCasing;
import li.cil.tis3d.common.block.BlockController;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import li.cil.tis3d.common.tileentity.TileEntityController;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

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

    public static void addRecipes() {
        addBlockRecipe(Constants.NAME_BLOCK_CASING, "blockIron", 8);
        addBlockRecipe(Constants.NAME_BLOCK_CONTROLLER, "gemDiamond", 1);
    }

    private static void addBlockRecipe(final String name, final Object specialIngredient, final int count) {
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(GameRegistry.findBlock(API.MOD_ID, name), count),
                                                   "IRI",
                                                   "RSR",
                                                   "IRI",
                                                   'I', "ingotIron",
                                                   'R', "dustRedstone",
                                                   'S', specialIngredient));
    }

    private Blocks() {
    }
}
