package li.cil.tis3d.client.manual.provider;

import li.cil.tis3d.api.manual.ImageProvider;
import li.cil.tis3d.api.manual.ImageRenderer;
import li.cil.tis3d.client.manual.segment.render.ItemStackImageRenderer;
import li.cil.tis3d.client.manual.segment.render.MissingItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public final class OreDictImageProvider implements ImageProvider {
    @Override
    public ImageRenderer getImage(final String data) {
        final ItemStack[] stacks = OreDictionary.getOres(data).stream().filter(stack -> stack != null && stack.getItem() != null).toArray(ItemStack[]::new);
        if (stacks != null && stacks.length > 0) {
            return new ItemStackImageRenderer(stacks);
        } else {
            return new MissingItemRenderer("manual.Warning.OreDictMissing");
        }
    }
}
