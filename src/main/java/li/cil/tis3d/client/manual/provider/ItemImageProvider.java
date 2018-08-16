package li.cil.tis3d.client.manual.provider;

import com.google.common.base.Strings;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.manual.ImageProvider;
import li.cil.tis3d.api.manual.ImageRenderer;
import li.cil.tis3d.client.manual.segment.render.ItemStackImageRenderer;
import li.cil.tis3d.client.manual.segment.render.MissingItemRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public final class ItemImageProvider implements ImageProvider {
    private static final String WARNING_ITEM_MISSING = API.MOD_ID + ".manual.warning.missing.item";

    @Override
    public ImageRenderer getImage(final String data) {
        final String name = data;
        final Item item = Item.REGISTRY.getObject(new ResourceLocation(name));
        if (item != null) {
            return new ItemStackImageRenderer(new ItemStack(item, 1));
        } else {
            return new MissingItemRenderer(WARNING_ITEM_MISSING);
        }
    }
}
