package li.cil.manual.client.provider;

import li.cil.manual.api.render.ContentRenderer;
import li.cil.manual.api.ManualModel;
import li.cil.manual.api.prefab.provider.AbstractRendererProvider;
import li.cil.manual.client.document.Strings;
import li.cil.manual.client.document.segment.render.ItemStackContentRenderer;
import li.cil.manual.client.document.segment.render.MissingContentRenderer;
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

    // --------------------------------------------------------------------- //

    @Override
    public boolean matches(final ManualModel manual) {
        return true;
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
            return Optional.of(new MissingContentRenderer(Strings.NO_SUCH_TAG));
        }
    }
}
