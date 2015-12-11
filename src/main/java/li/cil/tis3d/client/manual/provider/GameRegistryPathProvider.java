package li.cil.tis3d.client.manual.provider;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.manual.PathProvider;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameData;

public class GameRegistryPathProvider implements PathProvider {
    @Override
    public String pathFor(final ItemStack stack) {
        if (stack != null) {
            final Item item = stack.getItem();
            final Block block = Block.getBlockFromItem(item);
            if (block != null) {
                final String modId = GameData.getBlockRegistry().getNameForObject(block).getResourceDomain();
                if (API.MOD_ID.equals(modId)) {
                    return "%LANGUAGE%/block/" + block.getUnlocalizedName().replaceFirst("^tile\\.", "") + ".md";
                }
            } else {
                final String modId = GameData.getItemRegistry().getNameForObject(item).getResourceDomain();
                if (API.MOD_ID.equals(modId)) {
                    return "%LANGUAGE%/item/" + item.getUnlocalizedName().replaceFirst("^item\\.", "") + ".md";
                }
            }
        }
        return null;
    }

    @Override
    public String pathFor(final World world, final BlockPos pos) {
        if (world.isBlockLoaded(pos)) {
            final Block block = world.getBlockState(pos).getBlock();
            final String modId = GameData.getBlockRegistry().getNameForObject(block).getResourceDomain();
            if (API.MOD_ID.equals(modId)) {
                return "%LANGUAGE%/block/" + block.getUnlocalizedName().replaceFirst("^tile\\.", "") + ".md";
            }
        }
        return null;
    }
}