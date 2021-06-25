package li.cil.tis3d.client.manual.provider;

import li.cil.tis3d.api.manual.ContentRenderer;
import li.cil.tis3d.api.prefab.manual.AbstractRendererProvider;
import li.cil.tis3d.client.manual.Strings;
import li.cil.tis3d.client.manual.segment.render.ItemStackContentRenderer;
import li.cil.tis3d.client.manual.segment.render.MissingContentRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public final class ItemRendererProvider extends AbstractRendererProvider {
    public ItemRendererProvider() {
        super("item");
    }

    @Override
    protected Optional<ContentRenderer> doGetRenderer(final String data) {
        final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(data));
        if (item != null && item != Items.AIR) {
            return Optional.of(new ItemStackContentRenderer(new ItemStack(item)));
        } else {
            return Optional.of(new MissingContentRenderer(Strings.WARNING_ITEM_MISSING));
        }
    }
}
