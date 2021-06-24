package li.cil.tis3d.client.manual.provider;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.manual.PathProvider;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class ModPathProvider extends ForgeRegistryEntry<PathProvider> implements PathProvider {
    @Override
    public String pathFor(final ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        final Item item = stack.getItem();
        final Block block = Block.getBlockFromItem(item);
        if (block != Blocks.AIR) {
            final ResourceLocation name = block.getRegistryName();
            if (name == null) {
                return null;
            }

            final String modId = name.getNamespace();
            if (API.MOD_ID.equals(modId)) {
                return "%LANGUAGE%/block/" + block.getTranslationKey().replaceFirst("^block\\.tis3d\\.", "") + ".md";
            }
        } else {
            final ResourceLocation name = item.getRegistryName();
            if (name == null) {
                return null;
            }

            final String modId = name.getNamespace();
            if (API.MOD_ID.equals(modId)) {
                return "%LANGUAGE%/item/" + item.getTranslationKey().replaceFirst("^item\\.tis3d\\.", "") + ".md";
            }
        }

        return null;
    }

    @Override
    public String pathFor(final World world, final BlockPos pos, final Direction side) {
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityCasing) {
            final TileEntityCasing casing = (TileEntityCasing) tileEntity;
            final ItemStack moduleStack = casing.getStackInSlot(side.ordinal());
            final String path = pathFor(moduleStack);
            if (path != null) {
                return path;
            }
        }

        final Block block = world.getBlockState(pos).getBlock();
        final ResourceLocation name = block.getRegistryName();
        if (name == null) {
            return null;
        }

        final String modId = name.getNamespace();
        if (API.MOD_ID.equals(modId)) {
            return "%LANGUAGE%/block/" + block.getTranslationKey().replaceFirst("^block\\.tis3d\\.", "") + ".md";
        }

        return null;
    }
}