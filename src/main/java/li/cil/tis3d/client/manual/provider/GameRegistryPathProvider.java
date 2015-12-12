package li.cil.tis3d.client.manual.provider;

import cpw.mods.fml.common.registry.GameData;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.manual.PathProvider;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class GameRegistryPathProvider implements PathProvider {
    @Override
    public String pathFor(final ItemStack stack) {
        if (stack != null) {
            final Item item = stack.getItem();
            final Block block = Block.getBlockFromItem(item);
            if (block != null) {
                final String modId = new ResourceLocation(GameData.getBlockRegistry().getNameForObject(block)).getResourceDomain();
                if (API.MOD_ID.equals(modId)) {
                    return "%LANGUAGE%/block/" + block.getUnlocalizedName().replaceFirst("^tile\\.tis3d\\.", "") + ".md";
                }
            } else {
                final String modId = new ResourceLocation(GameData.getItemRegistry().getNameForObject(item)).getResourceDomain();
                if (API.MOD_ID.equals(modId)) {
                    return "%LANGUAGE%/item/" + item.getUnlocalizedName().replaceFirst("^item\\.tis3d\\.", "") + ".md";
                }
            }
        }
        return null;
    }

    @Override
    public String pathFor(final World world, final int x, final int y, final int z) {
        if (world.blockExists(x, y, z)) {
            final Block block = world.getBlock(x, y, z);
            final String modId = new ResourceLocation(GameData.getBlockRegistry().getNameForObject(block)).getResourceDomain();
            if (API.MOD_ID.equals(modId)) {
                return "%LANGUAGE%/block/" + block.getUnlocalizedName().replaceFirst("^tile\\.tis3d\\.", "") + ".md";
            }
        }
        return null;
    }
}