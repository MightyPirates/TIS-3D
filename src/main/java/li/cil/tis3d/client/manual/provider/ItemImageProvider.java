package li.cil.tis3d.client.manual.provider;

import li.cil.tis3d.api.manual.ImageProvider;
import li.cil.tis3d.api.manual.ImageRenderer;
import li.cil.tis3d.client.manual.Strings;
import li.cil.tis3d.client.manual.segment.render.ItemStackImageRenderer;
import li.cil.tis3d.client.manual.segment.render.MissingItemRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class ItemImageProvider extends ForgeRegistryEntry<ImageProvider> implements ImageProvider {
    private static final String PREFIX = "item:";

    @Override
    public boolean matches(final String path) {
        return path.startsWith(PREFIX);
    }

    @Override
    public ImageRenderer getImage(final String path) {
        final String name = path.substring(PREFIX.length());
        final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(name));
        if (item != null && item != Items.AIR) {
            return new ItemStackImageRenderer(new ItemStack(item));
        } else {
            return new MissingItemRenderer(Strings.WARNING_ITEM_MISSING);
        }
    }
}
