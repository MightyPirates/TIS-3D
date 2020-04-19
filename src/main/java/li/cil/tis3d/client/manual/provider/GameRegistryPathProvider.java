package li.cil.tis3d.client.manual.provider;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.manual.PathProvider;
import li.cil.tis3d.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public final class GameRegistryPathProvider implements PathProvider {
    @Override
    public String pathFor(final ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        final Item item = stack.getItem();
        final Block block = Block.getBlockFromItem(item);
        if (block != Blocks.AIR) {
            final String modId = Registry.BLOCK.getId(block).getNamespace();
            if (API.MOD_ID.equals(modId)) {
                return "%LANGUAGE%/block/" + block.getTranslationKey().replaceFirst("^tile\\.tis3d\\.", "") + ".md";
            }
        } else {
            final Identifier name = Registry.ITEM.getId(item);
            final String modId = name.getNamespace();
            if (API.MOD_ID.equals(modId)) {
                return "%LANGUAGE%/item/" + item.getTranslationKey().replaceFirst("^item\\.tis3d\\.", "") + ".md";
            }
        }
        return null;
    }

    @Override
    public String pathFor(final World world, final BlockPos pos) {
        if (!WorldUtils.isBlockLoaded(world, pos)) {
            return null;
        }
        final Block block = world.getBlockState(pos).getBlock();
        final String modId = Registry.BLOCK.getId(block).getNamespace();
        if (API.MOD_ID.equals(modId)) {
            return "%LANGUAGE%/block/" + block.getTranslationKey().replaceFirst("^block\\.tis3d\\.", "") + ".md";
        }
        return null;
    }
}
