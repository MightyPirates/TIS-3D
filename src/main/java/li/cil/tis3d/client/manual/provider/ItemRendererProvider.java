package li.cil.tis3d.client.manual.provider;

import li.cil.tis3d.api.manual.RendererProvider;
import li.cil.tis3d.api.manual.ContentRenderer;
import li.cil.tis3d.client.manual.Strings;
import li.cil.tis3d.client.manual.segment.render.ItemStackContentRenderer;
import li.cil.tis3d.client.manual.segment.render.MissingItemRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class ItemRendererProvider extends ForgeRegistryEntry<RendererProvider> implements RendererProvider {
    private static final String PREFIX = "item:";

    @Override
    public boolean matches(final String path) {
        return path.startsWith(PREFIX);
    }

    @Override
    public ContentRenderer getRenderer(final String path) {
        final String name = path.substring(PREFIX.length());
        final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(name));
        if (item != null && item != Items.AIR) {
            return new ItemStackContentRenderer(new ItemStack(item));
        } else {
            return new MissingItemRenderer(Strings.WARNING_ITEM_MISSING);
        }
    }
}
