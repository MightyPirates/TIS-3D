package li.cil.tis3d.client.manual.provider;

import li.cil.tis3d.api.manual.ContentRenderer;
import li.cil.tis3d.api.prefab.manual.AbstractRendererProvider;
import li.cil.tis3d.client.manual.Strings;
import li.cil.tis3d.client.manual.segment.render.ItemStackContentRenderer;
import li.cil.tis3d.client.manual.segment.render.MissingContentRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public final class TagRendererProvider extends AbstractRendererProvider {
    public TagRendererProvider() {
        super("tag");
    }

    @Override
    protected Optional<ContentRenderer> doGetRenderer(final String data) {
        final ITag<Item> tag = ItemTags.getAllTags().getTag(new ResourceLocation(data));
        if (tag != null && !tag.getValues().isEmpty()) {
            return Optional.of(new ItemStackContentRenderer(tag
                .getValues().stream()
                .map(ItemStack::new)
                .toArray(ItemStack[]::new)));
        } else {
            return Optional.of(new MissingContentRenderer(Strings.WARNING_TAG_MISSING));
        }
    }
}
