package li.cil.tis3d.client.manual.provider;

import li.cil.tis3d.api.manual.ImageProvider;
import li.cil.tis3d.api.manual.ImageRenderer;
import li.cil.tis3d.client.manual.Strings;
import li.cil.tis3d.client.manual.segment.render.ItemStackImageRenderer;
import li.cil.tis3d.client.manual.segment.render.MissingItemRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class TagImageProvider extends ForgeRegistryEntry<ImageProvider> implements ImageProvider {
    private static final String PREFIX = "tag:";

    @Override
    public boolean matches(final String path) {
        return path.startsWith(PREFIX);
    }

    @Override
    public ImageRenderer getImage(final String path) {
        final String data = path.substring(PREFIX.length());
        final ITag<Item> tag = ItemTags.getCollection().get(new ResourceLocation(data));
        if (tag == null || tag.getAllElements().isEmpty()) {
            return new MissingItemRenderer(Strings.WARNING_TAG_MISSING);
        }
        return new ItemStackImageRenderer(tag
            .getAllElements().stream()
            .map(ItemStack::new)
            .toArray(ItemStack[]::new));
    }
}
