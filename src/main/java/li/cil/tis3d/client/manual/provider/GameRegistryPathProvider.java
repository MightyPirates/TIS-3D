package li.cil.tis3d.client.manual.provider;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.manual.PathProvider;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public final class GameRegistryPathProvider implements PathProvider {
    @Override
    public String pathFor(@Nullable final ItemStack stack) {
        if (stack == null) {
            return null;
        }
        final Item item = stack.getItem();
        final Block block = Block.getBlockFromItem(item);
        if (block != Blocks.AIR) {
            final String modId = Block.REGISTRY.getNameForObject(block).getResourceDomain();
            if (API.MOD_ID.equals(modId)) {
                return "%LANGUAGE%/block/" + block.getUnlocalizedName().replaceFirst("^tile\\.tis3d\\.", "") + ".md";
            }
        } else {
            final ResourceLocation name = Item.REGISTRY.getNameForObject(item);
            if (name != null) {
                final String modId = name.getResourceDomain();
                if (API.MOD_ID.equals(modId)) {
                    return "%LANGUAGE%/item/" + item.getUnlocalizedName().replaceFirst("^item\\.tis3d\\.", "") + ".md";
                }
            }
        }
        return null;
    }

    @Override
    public String pathFor(final World world, final BlockPos pos) {
        if (!world.isBlockLoaded(pos)) {
            return null;
        }
        final Block block = world.getBlockState(pos).getBlock();
        final String modId = Block.REGISTRY.getNameForObject(block).getResourceDomain();
        if (API.MOD_ID.equals(modId)) {
            return "%LANGUAGE%/block/" + block.getUnlocalizedName().replaceFirst("^tile\\.tis3d\\.", "") + ".md";
        }
        return null;
    }
}