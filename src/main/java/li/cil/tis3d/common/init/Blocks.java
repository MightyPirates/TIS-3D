package li.cil.tis3d.common.init;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.block.BlockCasing;
import li.cil.tis3d.common.block.BlockController;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import li.cil.tis3d.common.tileentity.TileEntityController;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Arrays;
import java.util.List;

/**
 * Manages setup, registration and lookup of blocks.
 */
@GameRegistry.ObjectHolder(API.MOD_ID)
public final class Blocks {
    @GameRegistry.ObjectHolder(Constants.NAME_BLOCK_CASING)
    public static final Block CASING = null;
    @GameRegistry.ObjectHolder(Constants.NAME_BLOCK_CONTROLLER)
    public static final Block CONTROLLER = null;

    public static List<Block> getAllBlocks() {
        return Arrays.asList(
            CASING,
            CONTROLLER
        );
    }

    // --------------------------------------------------------------------- //

    public static void register(final IForgeRegistry<Block> registry) {
        registerBlock(registry, new BlockCasing(), Constants.NAME_BLOCK_CASING, TileEntityCasing.class);
        registerBlock(registry, new BlockController(), Constants.NAME_BLOCK_CONTROLLER, TileEntityController.class);
    }

    // --------------------------------------------------------------------- //

    private static void registerBlock(final IForgeRegistry<Block> registry, final Block block, final String name, final Class<? extends TileEntity> tileEntity) {
        registry.register(block.
            setHardness(5).
            setResistance(10).
            setTranslationKey(API.MOD_ID + "." + name).
            setCreativeTab(API.creativeTab).
            setRegistryName(name));

        final ResourceLocation registryName = new ResourceLocation(API.MOD_ID, name);
        GameRegistry.registerTileEntity(tileEntity, registryName);
    }

    // --------------------------------------------------------------------- //

    private Blocks() {
    }
}
