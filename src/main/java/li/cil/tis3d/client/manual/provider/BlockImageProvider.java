package li.cil.tis3d.client.manual.provider;

import li.cil.tis3d.api.CommonAPI;
import li.cil.tis3d.api.manual.ImageProvider;
import li.cil.tis3d.api.manual.ImageRenderer;
import li.cil.tis3d.client.manual.segment.render.ItemStackImageRenderer;
import li.cil.tis3d.client.manual.segment.render.MissingItemRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@Environment(EnvType.CLIENT)
public final class BlockImageProvider implements ImageProvider {
    private static final String WARNING_BLOCK_MISSING = CommonAPI.MOD_ID + ".manual.warning.missing.block";

    @Override
    public ImageRenderer getImage(final String data) {
        final Block block = Registry.BLOCK.get(new Identifier(data));
        if (Item.fromBlock(block) != Items.AIR) {
            return new ItemStackImageRenderer(new ItemStack(block, 1));
        } else {
            return new MissingItemRenderer(WARNING_BLOCK_MISSING);
        }
    }
}
