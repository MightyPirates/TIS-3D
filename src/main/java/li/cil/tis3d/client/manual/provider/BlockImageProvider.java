package li.cil.tis3d.client.manual.provider;

import com.google.common.base.Strings;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.manual.ImageProvider;
import li.cil.tis3d.api.manual.ImageRenderer;
import li.cil.tis3d.client.manual.segment.render.ItemStackImageRenderer;
import li.cil.tis3d.client.manual.segment.render.MissingItemRenderer;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public final class BlockImageProvider implements ImageProvider {
    private static final String WARNING_BLOCK_MISSING = API.MOD_ID + ".manual.warning.missing.block";

    @Override
    public ImageRenderer getImage(final String data) {
        final String name = data;
        final Block block = Block.REGISTRY.getObject(new ResourceLocation(name));
        if (Item.getItemFromBlock(block) != Items.AIR) {
            return new ItemStackImageRenderer(new ItemStack(block, 1));
        } else {
            return new MissingItemRenderer(WARNING_BLOCK_MISSING);
        }
    }
}
