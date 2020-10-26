package li.cil.tis3d.client.manual.provider;

import li.cil.tis3d.api.manual.ImageProvider;
import li.cil.tis3d.api.manual.ImageRenderer;
import li.cil.tis3d.client.manual.segment.render.ItemStackImageRenderer;
import li.cil.tis3d.client.manual.segment.render.MissingItemRenderer;
import li.cil.tis3d.common.API;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class TagImageProvider implements ImageProvider {
    private static final String WARNING_TAG_MISSING = API.MOD_ID + ".manual.warning.missing.tag";

    @Override
    public ImageRenderer getImage(final String data) {
        final ItemStack[] stacks = TagRegistry.item(new Identifier(data)).values().stream().map(ItemStack::new).filter(stack -> !stack.isEmpty()).toArray(ItemStack[]::new);
        if (stacks.length > 0) {
            return new ItemStackImageRenderer(stacks);
        } else {
            return new MissingItemRenderer(WARNING_TAG_MISSING);
        }
    }
}
