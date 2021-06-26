package li.cil.manual.client.provider;

import li.cil.manual.api.ContentRenderer;
import li.cil.manual.api.Manual;
import li.cil.manual.api.prefab.AbstractRendererProvider;
import li.cil.manual.client.document.Strings;
import li.cil.manual.client.document.segment.render.ItemStackContentRenderer;
import li.cil.manual.client.document.segment.render.MissingContentRenderer;
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

    // --------------------------------------------------------------------- //

    @Override
    public boolean matches(final Manual manual) {
        return true;
    }

    @Override
    protected Optional<ContentRenderer> doGetRenderer(final String data) {
        final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(data));
        if (item != null && item != Items.AIR) {
            return Optional.of(new ItemStackContentRenderer(new ItemStack(item)));
        } else {
            return Optional.of(new MissingContentRenderer(Strings.NO_SUCH_ITEM));
        }
    }
}
